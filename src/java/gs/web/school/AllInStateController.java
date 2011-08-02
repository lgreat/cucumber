package gs.web.school;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.web.geo.StateSpecificFooterHelper;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.document.Document;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.search.Searcher;
import gs.data.search.GSAnalyzer;
import gs.data.search.Indexer;
import gs.data.search.IndexField;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.util.Address;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;

/**
 * This controller builds the model for the "all schools",
 * "all cities", and "all districts" pages.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
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

    /** key for the current letter being displayed (for cities page) */
    public static final String MODEL_CURRENT_LETTER= "currentLetter";

    /** value for link rel canonical (for cities page) */
    public static final String MODEL_REL_CANONICAL= "relCanonical";

    /** The type of item that this page displays */
    public static final String SCHOOLS_TYPE = "school";
    public static final String CITIES_TYPE = "city";
    public static final String DISTRICTS_TYPE = "district";
    public static final int NUM_TOP_CITIES = 50;
    public static final String POPULAR_CITIES_TITLE = "Popular Cities";

    /** Used to get data */
    private Searcher _searcher;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;
    private IGeoDao _geoDao;

    /** The max number of items to display on a page */
    protected int SCHOOLS_PAGE_SIZE = 400; //default
    protected int CITIES_PAGE_SIZE = 1000000; //cities will always be a-m and  n-final letter
    protected int DISTRICTS_PAGE_SIZE = 400; //default

    /** Lucene query parser */
    private QueryParser _queryParser;

    private Logger _log = Logger.getLogger(AllInStateController.class);

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

        // set state in session context and call pagehelper to make sure state ad keyword is correct
        SessionContextUtil.getSessionContext(request).setState(state);
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            pageHelper.setStateAdKeyword();
        }

        ModelAndView mAndV;
        if (StringUtils.isNotBlank(path) && state != null) {
            Map<String, Object> model = buildModel(state, path);
            if (model.get(MODEL_TYPE).equals(SCHOOLS_TYPE)) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, state);
                String redirectPath = urlBuilder.asSiteRelative(request);
                mAndV = new ModelAndView(new RedirectView301(redirectPath));
                return mAndV;
            }
            String relCanonical = "http://" +
                request.getServerName() +
                ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "") +
                request.getRequestURI();
            model.put(MODEL_REL_CANONICAL, relCanonical);

            _stateSpecificFooterHelper.displayPopularCitiesForState(state, model);
            mAndV = new ModelAndView("school/allInState", model);
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
    protected Map<String, Object> buildModel(State state, String path) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();

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
        model.put(MODEL_STATE, state);
        if (DISTRICTS_TYPE.equals(type)) {
            buildDistrictModel(state, path, model, type, pageSize);
        } else {
            buildCitiesModel(state, path, model, type);
        }


        return model;
    }

    protected void buildCitiesModel(State state, String path, Map<String, Object> model, String type) throws Exception {
        // We need to remember this value to build the page title.

        String alpha = getCityAlphaFromPath(path);
        if (POPULAR_CITIES_TITLE.equals(alpha)) {
            model.put(MODEL_LIST, convertToSortedListOfMaps(
                    _geoDao.findTopCitiesByPopulationInState(state, NUM_TOP_CITIES)));
            model.put(MODEL_TITLE, POPULAR_CITIES_TITLE);
        } else {
            model.put(MODEL_CURRENT_LETTER, alpha);
        }
        model.put(MODEL_TITLE, buildTitle(type, state, alpha));

        // Get *all* the results for a state
        Hits hits = getHits(type, state);

        // Group these results by alpha order - a separate list for each letter.
        List<List<Map<String, Object>>> alphaGroups = getAlphaGroups(type, hits, state);

        StringBuffer linksBuffer = new StringBuffer();
//        List<List> pageGroups = new ArrayList<List>();
        Map<String, List> pageGroups = new HashMap<String, List>();
//        List<Map<String, Object>> workingGroup = new ArrayList<Map<String, Object>>();
//        boolean breakpoint = true;
        for (List<Map<String, Object>> alphaGroup : alphaGroups) {
            // here's the logic... each alpha group gets its own page
            String currentPage = StringUtils.substring((String)alphaGroup.get(0).get("city"), 0, 1).toUpperCase();
            pageGroups.put(currentPage, alphaGroup);
            linksBuffer.append(buildPageLink(state, currentPage, alpha, getSpan(alphaGroup, 1)));
        }

        model.put(MODEL_LINKS, linksBuffer.toString());

        if (StringUtils.length(alpha) == 1 && pageGroups.get(alpha) != null) {
            List list = pageGroups.get(alpha);
            model.put(MODEL_LIST, list);
        }
    }

    protected List<Map<String, Object>> convertToSortedListOfMaps(List<City> topCitiesByPopulationInState) {
        Collections.sort(topCitiesByPopulationInState, new Comparator<City>() {
            public int compare(City o1, City o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        List<Map<String, Object>> rval = new ArrayList<Map<String, Object>>(topCitiesByPopulationInState.size());
        for (City city: topCitiesByPopulationInState) {
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("name", city.getName());
            fields.put("city", city.getName());
            rval.add(fields);
        }
        return rval;
    }

    protected void buildDistrictModel(State state, String path, Map<String, Object> model, String type, int pageSize) throws Exception {
        // We need to remember this value to build the page title.
        int selectedSpanWidth = 1; //default

        int page = getPageFromPath(path);

        // Get *all* the results for a state
        Hits hits = getHits(type, state);

        // Group these results by alpha order - a separate list for each letter.
        List<List<Map<String, Object>>> alphaGroups = getAlphaGroups(type, hits, state);

        StringBuffer linksBuffer = new StringBuffer();
        List<List> pageGroups = new ArrayList<List>();
        List workingGroup = new ArrayList();
        boolean breakpoint = true;
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
    protected List<List<Map<String, Object>>> getAlphaGroups(String type, Hits hits, State state) throws IOException {
        List<List<Map<String,Object>>> alphaGroups = new ArrayList<List<Map<String,Object>>>();
        if (hits != null && hits.length() > 0) {
            List<Map<String, Object>> workingList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> numericList = new ArrayList<Map<String, Object>>();
            char currentLetter = 'a';
            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                String name = doc.get("name");
                String city = doc.get(Indexer.CITY);
                if (CITIES_TYPE.equals(type)) {
                    name = city;
                }

                // Add the current working list to the alphaGroups on each letter change.
                String lowerName = name.trim().toLowerCase();
                if ((lowerName.length() > 0) && (currentLetter != lowerName.charAt(0))) {
                    if (workingList.size() > 0) {
                        alphaGroups.add(workingList);
                        workingList = new ArrayList<Map<String, Object>>();
                    }
                    currentLetter = lowerName.charAt(0);
                }

                if (name.matches("^\\p{Alnum}.*")) {
                    Map<String, Object> fields = new HashMap<String, Object>();
                    fields.put("name", name);
                    String id = doc.get(Indexer.ID);
                    fields.put("id", id);
                    fields.put("city", city);
                    fields.put("county", doc.get(Indexer.COUNTY));
                    if (SCHOOLS_TYPE.equals(type)) {
                        School s = new School();
                        s.setName(name);
                        s.setDatabaseState(state);
                        s.setType(SchoolType.getSchoolType(doc.get(IndexField.SCHOOL_TYPE)));
                        try {
                            s.setId(new Integer(id));
                        } catch (NumberFormatException nfe) {
                            _log.warn("Could not parse school id: " + id, nfe);
                        }
                        fields.put("school", s);
                    } else if (DISTRICTS_TYPE.equals(type)) {
                        District d = new District();
                        d.setName(name);
                        d.setDatabaseState(state);
                        Address address = new Address();
                        address.setCity(city);
                        d.setPhysicalAddress(address);
                        try {
                            d.setId(new Integer(id));
                        } catch (NumberFormatException nfe) {
                            _log.warn("Could not parse district id: " + id, nfe);
                        }
                        fields.put("district", d);
                    }

                    if (name.matches("^\\p{Digit}.*")) {
                        numericList.add(fields);
                    } else {
                        workingList.add(fields);
                    }
                }
            }
            // Add the last working list.
            if (workingList.size() > 0) {
                alphaGroups.add(workingList);
            }

            // add any numeric groups to the end of the list - GS-4189
            if (numericList.size() > 0) {
                alphaGroups.add(numericList);
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

    protected String getCityAlphaFromPath(String path) {
        String alpha=POPULAR_CITIES_TITLE;
        if (StringUtils.isNotBlank(path)) {
            String[] elements = path.trim().split("/");
            String last = elements[elements.length-1];
            if (StringUtils.length(last) == 1 && StringUtils.isAlpha(last)) {
                alpha = StringUtils.upperCase(last);
            }
        }

        return alpha;
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
     * @param state - a State
     * @param index - The index of *this* link
     * @param selectedIndex - the index of the currently selected page
     * @param span - the Text that is wrapped by this link
     * @return a String
     */
    protected String buildPageLink(State state, String index,
                                   String selectedIndex, String span) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<span class=\"pageLink\">");
        if (!StringUtils.equals(index, selectedIndex)) {
            buffer.append("<a href=\"/schools/");
            buffer.append("cities/");
            buffer.append(state.getLongName());
            buffer.append("/");
            buffer.append(state.getAbbreviation());
            if (StringUtils.length(index) == 1) {
                buffer.append("/");
                buffer.append(index);
            }
            buffer.append("\">");
        }
        buffer.append(span);
        if (!StringUtils.equals(index, selectedIndex)) {
            buffer.append("</a>");
        }
        buffer.append("</span>\n");
        return buffer.toString();
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

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}