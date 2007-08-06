package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.queryParser.QueryParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.search.Searcher;
import gs.data.search.GSAnalyzer;
import gs.data.search.Indexer;

/**
 * This controller builds the model for the "all schools",
 * "all cities", and "all districts" pages.
 * 
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AllInStateController extends AbstractController {

    /** Spring bean id */
    public static final String BEAN_ID = "/schools/allInState.page";

    /** key for the list in the model Map */
    public static final String MODEL_LIST = "list";

    /** key for the page links in the model Map */
    public static final String MODEL_LINKS= "links";

    /** key for the page title in the model Map */
    public static final String MODEL_TITLE= "title";

    /** key for the State in the model Map */
    public static final String MODEL_STATE= "state";

    /** key for the page type (school|city|district) in the model Map */
    public static final String MODEL_TYPE= "type";

    /** The type of item that this page displays */
    public static final String SCHOOLS_TYPE = "school";
    public static final String CITIES_TYPE = "city";
    public static final String DISTRICTS_TYPE = "district";

    /** Used to get data */
    private Searcher _searcher;

    /** The max number of items to display on a page */
    protected int SCHOOLS_PAGE_SIZE = 400; //default
    protected int CITIES_PAGE_SIZE = 700; //default
    protected int DISTRICTS_PAGE_SIZE = 400; //default
    
    /** Lucene query parser */
    private QueryParser _queryParser;

    
    public AllInStateController() {
        super();
        _queryParser = new QueryParser("text", new GSAnalyzer());
        _queryParser.setDefaultOperator(QueryParser.Operator.AND);
    }

    /**
     * This controller handles request to /schools/*  and attempts to extract 3 types
     * of information from the path info:
     * <ol>
     *     <li>
     *         page type - ex:<br/>
     *         /schools/...  - for all schools page<br/>
     *         /schools/cities/... - for all cities page<br/>
     *         /schools/districts/... - for all districts page
     *     </li>
     *     <li>
     *         State - ex:<br/>
     *         /schools/California/CA
     *     </li>
     *     <li>
     *         Page index - ex:<br/>
     *         /schools/California/CA/3
     *     </li>*
     * </ol>
     * @param request (from Tomcat)
     * @param response (from Tomcat)
     * @return a ModelAndView type
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String path = request.getPathInfo();
        State state = getStateFromPath(path);

        ModelAndView mAndV;
        if (StringUtils.isNotBlank(path) && state != null) {
            mAndV = new ModelAndView("school/allInState", buildModel(path));
        } else {
            mAndV = new ModelAndView("status/error");
        }
        return mAndV;
    }


    /**
     * This method collects all of the search results into alphabetized groups and
     * then loads the model according the the supplied parameters.
     * @param path the url path info
     * @throws Exception - if something goes haywire.
     * @return a Map populated with the model elements.
     */
    protected Map buildModel(String path) throws Exception {

        Map model = new HashMap();

        // Determine the page type from path.
        String type = SCHOOLS_TYPE;
        int pageSize = SCHOOLS_PAGE_SIZE;
        if (path.contains("/cities/")) {
            type = CITIES_TYPE;
            pageSize = CITIES_PAGE_SIZE;
        } else if (path.contains("/districts/")) {
            type = DISTRICTS_TYPE;
            pageSize = DISTRICTS_PAGE_SIZE;
        }
        model.put(MODEL_TYPE, type);

        // We need to remember this value to build the page title. 
        int selectedSpanWidth = 1; //default

        State state = getStateFromPath(path);
        int page = getPageFromPath(path);

        // Get *all* the results for a state
        Hits hits = getHits(type, state);

        // Group these results by alpha order - a separate list for each letter.
        List<List> alphaGroups = getAlphaGroups(type, hits);

        StringBuffer linksBuffer = new StringBuffer();
        List<List> pageGroups = new ArrayList<List>();
        List workingGroup = new ArrayList();
        for (List alphaGroup : alphaGroups) {
            if (alphaGroup.size() > pageSize) {
                if (workingGroup.size() > 0) {
                    pageGroups.add(workingGroup);
                    linksBuffer.append(buildPageLink(type, state, pageGroups.size(), page, getSpan(workingGroup, 1)));
                    workingGroup = new ArrayList();
                }
                int fullChunks = alphaGroup.size() / pageSize;
                int remainder = alphaGroup.size() % pageSize;
                List subGroup;
                for (int j = 0; j < fullChunks; j++) {
                    subGroup = new ArrayList();
                    for (int jj = 0; jj < pageSize; jj++) {
                        subGroup.add(alphaGroup.get((j * pageSize) + jj));
                    }
                    pageGroups.add(subGroup);
                    if (pageGroups.size() == page) {
                        selectedSpanWidth = 2;
                    }
                    linksBuffer.append(buildPageLink(type, state, pageGroups.size(), page, getSpan(subGroup, 2)));
                }
                subGroup = new ArrayList();
                for (int k = alphaGroup.size() - remainder; k < alphaGroup.size(); k++) {
                    subGroup.add(alphaGroup.get(k));
                }
                pageGroups.add(subGroup);
                if (pageGroups.size() == page) {
                    selectedSpanWidth = 2;
                }
                linksBuffer.append(buildPageLink(type, state, pageGroups.size(), page, getSpan(subGroup, 2)));
            } else {
                if ((alphaGroup.size() + workingGroup.size()) < pageSize) {
                    workingGroup.addAll(alphaGroup);
                } else {
                    pageGroups.add(workingGroup);
                    linksBuffer.append(buildPageLink(type, state, pageGroups.size(), page, getSpan(workingGroup, 1)));
                    workingGroup = alphaGroup;
                }
            }
        }

        if (workingGroup.size() > 0) {
            pageGroups.add(workingGroup);
            linksBuffer.append(buildPageLink(type, state, pageGroups.size(), page, getSpan(workingGroup,1)));
        }

        model.put(MODEL_LINKS, linksBuffer.toString());

        if (page > 0 && page <= pageGroups.size()) {
            List list = pageGroups.get(page-1);
            model.put(MODEL_LIST, list);
            String span = getSpan(list, selectedSpanWidth);
            model.put(MODEL_TITLE, buildTitle(type, state, span));
        } 
        model.put(MODEL_STATE, state);

        return model;
    }


    /**
     * Returns a Hits object containing all of the matches for a particular type -
     * school, city, or district - within a state.
     * @param type ("school" | "city" | "district")
     * @param state - a <code>State</code>
     * @return a Hits containing matches or null if no matches could be found.
     * @throws Exception if there is a parsing or searching error.
     */
    protected Hits getHits(String type, State state) throws Exception {
        Hits hits = null;
        Query query = _queryParser.parse("type:" + type + " AND state:" + state.getAbbreviationLowerCase());
        hits = _searcher.search(query,
                new Sort(Indexer.SORTABLE_NAME), null, null);
        return hits;
    }


    /**
     * Builds a List of Lists grouped by alpha order.  Each list should contain only
     * items that begin with the same letter.
     * @param type ("school"|"city"|"district")
     * @param hits the Lucene results
     * @return a List<List>
     * @throws IOException - if something gets nasty.
     */
    protected List<List> getAlphaGroups(String type, Hits hits) throws IOException {
        List<List> alphaGroups = new ArrayList<List>();
        if (hits != null && hits.length() > 0) {
            List workingList = new ArrayList();
            char currentLetter = 'a';
            for (int i = 0; i < hits.length(); i++) {
                String name = hits.doc(i).get("name");
                String city = hits.doc(i).get(Indexer.CITY);
                if (CITIES_TYPE.equals(type)) {
                    name = city;
                }
                // Add the current working list to the alphaGroups on each letter change.
                if ((currentLetter != name.trim().toLowerCase().charAt(0))) {
                    if (workingList.size() > 0) {
                        alphaGroups.add(workingList);
                        workingList = new ArrayList();
                    }
                    currentLetter = name.trim().toLowerCase().charAt(0);
                }
                if (name.matches("^\\p{Alnum}.*")) {
                    Map fields = new HashMap();
                    fields.put("name", name);
                    fields.put("id", hits.doc(i).get(Indexer.ID));
                    fields.put("city", city);
                    fields.put("county", hits.doc(i).get(Indexer.COUNTY));
                    workingList.add(fields);
                }
            }
            // Add the last working list.
            if (workingList.size() > 0) {
                alphaGroups.add(workingList);
            }
        }
        return alphaGroups;
    }


    /**
     * Parses  a path string and tries to extract a state from the path.
     * Returns null if a state cannot be determined from the path.
     * @param path a url path String
     * @return a <code>State</code> object or null
     */
    protected State getStateFromPath(String path) {
        State state = null;
        if (StringUtils.isNotBlank(path)) {
            String[] elements = path.trim().split("/");
            for (String element : elements) {
                if (element.matches("[A-Z][A-Z]")) {
                    StateManager sm = new StateManager();
                    state = sm.getState(element);
                    break;
                }
            }
        }
        return state;
    }


    /**
     * Parses a path and returns a page index based on the last path component which
     * should be an integer index.  The last path component is the value after the last
     * "/" charater. This method always returns a positive int - if a
     * valid index cannot be determined from the path, then 1 is returned.
     *
     * @param path a String
     * @return an int > 0
     */
    protected int getPageFromPath(String path) {
        int page = 1;
        if (StringUtils.isNotBlank(path)) {
            String[] elements = path.trim().split("/");
            String last = elements[elements.length-1];
            try {
                page = Integer.parseInt(last);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return page > 0 ? page : 1;
    }


    /**
     * Helper method that builds the markup for a single page link.
     * @param type - (school|city|district)
     * @param state - a State
     * @param index - The index of *this* link
     * @param selectedIndex - the index of the currently selected page
     * @param span - the Text that is wrapped by this link
     * @return a String
     */
    protected String buildPageLink(String type, State state, int index,
                               int selectedIndex, String span) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<span class=\"pageLink\">");
        if (index != selectedIndex) {
            buffer.append("<a href=\"/schools/");
            if (CITIES_TYPE.equals(type)) {
                buffer.append("cities/");
            } else if (DISTRICTS_TYPE.equals(type)) {
                buffer.append("districts/");                
            }
            buffer.append(state.getLongName());
            buffer.append("/");
            buffer.append(state.getAbbreviation());
            if (index > 1) {
                buffer.append("/");
                buffer.append(index);
            }
            buffer.append("\">");
        }
        buffer.append(span);
        if (index != selectedIndex) {
            buffer.append("</a>");
        }
        buffer.append("</span>\n");
        return buffer.toString();
    }


    /**
     * Given a list of <code>Map</code>s, this method will construct a string using the
     * name of the first and last element in the list.  The returned span string is
     * based on the first (width) characters of the "name" value of the map elements.
     *
     * @param list A List of Maps
     * @param width The number of characters in each name used  build the span.
     * @return a String.
     */
    protected String getSpan(List list, int width) {
        StringBuffer buffer = new StringBuffer();
        if (list != null && list.size() > 0) {
            String first = ((String)((Map)list.get(0)).get("name")).trim();
            String last = ((String)((Map)list.get(list.size()-1)).get("name")).trim();
            if (first.length() < width || last.length() < width) {
                width = 1;
            }

            String start = first.substring(0, width).toLowerCase();
            String end = last.substring(0, width).toLowerCase();
            buffer.append(start);
            if (!start.equals(end)) {
                buffer.append("-");
                buffer.append(end);
            }
        }
        return buffer.toString().toUpperCase();
    }


    protected String buildTitle(String type, State state, String span) {
        StringBuffer buffer = new StringBuffer();
        if (DISTRICTS_TYPE.equals(type)) {
            buffer.append ("All school districts in ");
            buffer.append(state.getLongName());
            buffer.append(", ");
            buffer.append(state.getAbbreviation());
            buffer.append(": ");
        } else if (CITIES_TYPE.equals(type)) {
            buffer.append(state.getLongName());
            buffer.append(" School information by City: ");
        } else {
            buffer.append ("All schools in ");
            buffer.append(state.getLongName());
            buffer.append(", ");
            buffer.append(state.getAbbreviation());
            buffer.append(": ");
        }
        buffer.append(span);
        return buffer.toString();
    }


    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}