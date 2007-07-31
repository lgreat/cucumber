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
 * This controller build the model for the "all schools",
 * "all cities", and "all districts" pages.
 * 
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AllSchoolsController extends AbstractController {

    /** Spring bean id */
    public static final String BEAN_ID = "/schools/allSchools.page";

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

    /** Used to get Data */
    private Searcher _searcher;

    /** The max number of items to display on a page */
    private static int SCHOOLS_PAGE_SIZE = 400; //default
    private static int CITIES_PAGE_SIZE = 700; //default
    private static int DISTRICTS_PAGE_SIZE = 400; //default
    
    /** Lucene query parser */
    private QueryParser _queryParser;

    public AllSchoolsController() {
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
        int page = getPageFromPath(path);

        ModelAndView mAndV = new ModelAndView();

        if (StringUtils.isNotBlank(path) && state != null) {
            mAndV.setViewName("school/allSchools");
            if (path.contains("/cities/")) {
                mAndV.getModel().put(MODEL_TYPE, CITIES_TYPE);
                buildPageLinksAndModel(CITIES_TYPE, mAndV.getModel(),
                        state, page,CITIES_PAGE_SIZE);
            } else if (path.contains("/districts/")) {
                mAndV.getModel().put(MODEL_TYPE, DISTRICTS_TYPE);
                buildPageLinksAndModel(DISTRICTS_TYPE, mAndV.getModel(),
                        state, page, DISTRICTS_PAGE_SIZE);
            } else {
                mAndV.getModel().put(MODEL_TYPE, SCHOOLS_TYPE);
                buildPageLinksAndModel(SCHOOLS_TYPE, mAndV.getModel(),
                        state, page, SCHOOLS_PAGE_SIZE);
            }
        } else {
            mAndV.setViewName("status/error");
        }

        return mAndV;
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
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].matches("[A-Z][A-Z]")) {
                    StateManager sm = new StateManager();
                    state = sm.getState(elements[i]);
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
            if (type == CITIES_TYPE) {
                buffer.append("cities/");
            } else if (type == DISTRICTS_TYPE) {
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

    /**
     * This method collects all of the search results into alphabetized groups and
     * then loads the model according the the supplied parameters.
     * @param type ("school"|"district"|"city")
     * @param model a Map
     * @param state a State object
     * @param index the page index
     * @param pageSize the max number of items to load in the model.
     * @throws Exception - if something goes haywire.
     */
    protected void buildPageLinksAndModel(String type, Map model, State state,
                                          int index, int pageSize) throws Exception {

        int selectedSpanWidth = 1; //default

        Hits hits = getHits(type, state);
        List<List> alphaGroups = getAlphaGroups(type, hits);

        StringBuffer linksBuffer = new StringBuffer();
        List<List> pageGroups = new ArrayList<List>();
        List workingGroup = new ArrayList();
        for (int i = 0; i < alphaGroups.size(); i++) {
            List alphaGroup = (List)alphaGroups.get(i);
            if (alphaGroup.size() > pageSize) {
                if (workingGroup.size() > 0) {
                    pageGroups.add(workingGroup);
                    linksBuffer.append(buildPageLink(type, state, pageGroups.size(), index, getSpan(workingGroup, 1)));
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
                    if (pageGroups.size() == index) { selectedSpanWidth = 2; }
                    linksBuffer.append(buildPageLink(type, state, pageGroups.size(), index, getSpan(subGroup,2)));
                }
                subGroup = new ArrayList();
                for (int k = alphaGroup.size() - remainder; k < alphaGroup.size(); k++) {
                    subGroup.add(alphaGroup.get(k));
                }
                pageGroups.add(subGroup);
                if (pageGroups.size() == index) { selectedSpanWidth = 2; }
                linksBuffer.append(buildPageLink(type, state, pageGroups.size(), index, getSpan(subGroup,2)));
            } else {
                if ((alphaGroup.size() + workingGroup.size()) < pageSize) {
                    workingGroup.addAll(alphaGroup);
                } else {
                    pageGroups.add(workingGroup);
                    linksBuffer.append(buildPageLink(type, state, pageGroups.size(), index, getSpan(workingGroup,1)));
                    workingGroup = alphaGroup;
                }
            }
        }

        if (workingGroup.size() > 0) {
            pageGroups.add(workingGroup);
            linksBuffer.append(buildPageLink(type, state, pageGroups.size(), index, getSpan(workingGroup,1)));
        }

        model.put(MODEL_LINKS, linksBuffer.toString());

        if (index > 0 && index <= pageGroups.size()) {
            List list = pageGroups.get(index-1);
            model.put(MODEL_LIST, list);
            String span = getSpan(list, selectedSpanWidth);
            model.put(MODEL_TITLE, buildTitle(type, state, span));
        }

        model.put(MODEL_STATE, state);
    }

    protected String buildTitle(String type, State state, String span) {
        StringBuffer buffer = new StringBuffer();
        if (type == DISTRICTS_TYPE) {
            buffer.append ("All school districts in ");
            buffer.append(state.getLongName());
            buffer.append(", ");
            buffer.append(state.getAbbreviation());
            buffer.append(": ");
        } else if (type == CITIES_TYPE) {
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
                if (type == "city") {
                    name = city;
                }
                if ((currentLetter != name.trim().toLowerCase().charAt(0)) || i == hits.length()-1) {
                    if (workingList.size() > 0) {
                        alphaGroups.add(workingList);
                        workingList = new ArrayList();
                    }
                    currentLetter = name.trim().toLowerCase().charAt(0);
                }
                if (name.matches("^\\p{Alpha}.*")) {
                    Map fields = new HashMap();
                    fields.put("name", name);
                    fields.put("id", hits.doc(i).get(Indexer.ID));
                    fields.put("city", city);
                    fields.put("county", hits.doc(i).get(Indexer.COUNTY));
                    workingList.add(fields);
                }
            }
        }
        return alphaGroups;
    }

    /**
     * Returns a Hits object containing all of the matches for a particular type -
     * school, city, or district - within a state.
     * @param type ("school" | "city" | "district")
     * @param state - a <code>State</code>
     * @return a Hits containing matches or null if no matches could be found.
     */
    protected Hits getHits(String type, State state) {
        Hits hits = null;
        try {
            Query query = _queryParser.parse("type:" + type + " AND state:" + state.getAbbreviationLowerCase());
            hits = _searcher.search(query,
                    new Sort(Indexer.SORTABLE_NAME), null, null);
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        return hits;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}