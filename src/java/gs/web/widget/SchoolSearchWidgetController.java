package gs.web.widget;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.SchoolWithRatings;
import gs.data.school.LevelCode;
import gs.data.school.review.IReviewDao;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class SchoolSearchWidgetController extends SimpleFormController {
    private static final Logger _log = Logger.getLogger(SchoolSearchWidgetController.class);
    public static final String BEAN_ID = "/widget/schoolSearch.page";

    private static final String SEARCH_QUERY_PARAM = "searchQuery";
    private static final String DISPLAY_TAB_PARAM = "displayTab";
    private static final String COBRAND_HOSTNAME_PARAM = "cobrandHostname";

    private static final String TEXT_COLOR_PARAM = "textColor";
    private static final String BORDERS_COLOR_PARAM = "bordersColor";
    private static final String WIDTH_PARAM = "width";
    private static final String HEIGHT_PARAM = "height";
    private static final String ZOOM_PARAM = "zoom";

    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";
    private static final String STATE_PARAM = "state";
    private static final String CITY_NAME_PARAM = "cityName";
    private static final String NORMALIZED_ADDRESS_PARAM = "normalizedAddress";

    // relevant to location (proximity) search only
    private static final int DISTANCE_IN_MILES = 10;
    private static final int MAX_NUM_RESULTS = 200;

    private boolean _hidePreschools;

    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private StateManager _stateManager;

    // ==============================================================================
    // SPRING MVC METHODS
    // ==============================================================================

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SchoolSearchWidgetCommand command = new SchoolSearchWidgetCommand();
        command.setHidePreschools(_hidePreschools);
        return command;
    }

    protected void onBindOnNewForm(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) commandObj;

        if (request.getParameter(DISPLAY_TAB_PARAM) != null) {
            command.setDisplayTab(request.getParameter(DISPLAY_TAB_PARAM));
        }

        if (request.getParameter(COBRAND_HOSTNAME_PARAM) != null) {
            String cobrandHostname = request.getParameter(COBRAND_HOSTNAME_PARAM);
            // case-insensitive pattern modifier: http://jelaniharris.com/2009/case-insensitive-replaceall-in-java/
            command.setCobrandHostname(cobrandHostname.replaceAll("(?i)greatschools\\.net", "greatschools.org"));
        }

        String textColor = request.getParameter(TEXT_COLOR_PARAM);
        if (StringUtils.isNotBlank(textColor)) {
            command.setTextColor(textColor);
        }

        String bordersColor = request.getParameter(BORDERS_COLOR_PARAM);
        if (StringUtils.isNotBlank(bordersColor)) {
            command.setBordersColor(bordersColor);
        }

        String width = request.getParameter(WIDTH_PARAM);
        if (StringUtils.isNotBlank(width)) {
            try {
                command.setWidth(Integer.parseInt(width));
            } catch (Exception e) {
                // ignore this
            }
        }

        String height = request.getParameter(HEIGHT_PARAM);
        if (StringUtils.isNotBlank(height)) {
            try {
                command.setZoom(Integer.parseInt(height));
            } catch (Exception e) {
                // ignore this
            }
        }

        String zoom = request.getParameter(ZOOM_PARAM);
        if (StringUtils.isNotBlank(zoom)) {
            try {
                command.setZoom(Integer.parseInt(zoom));
            } catch (Exception e) {
                // ignore this
            }
        }

        String lat = request.getParameter(LAT_PARAM);
        if (StringUtils.isNotBlank(lat)) {
            try {
                command.setLat(Float.parseFloat(lat));
            } catch (Exception e) {
                // ignore this
            }
        }

        String lon = request.getParameter(LON_PARAM);
        if (StringUtils.isNotBlank(lon)) {
            try {
                command.setLon(Float.parseFloat(lon));
            } catch (Exception e) {
                // ignore this
            }
        }

        String state = request.getParameter(STATE_PARAM);
        if (StringUtils.isNotBlank(state)) {
            try {
                command.setState(state);
            } catch (Exception e) {
                // ignore this
            }
        }

        String normalizedAddress = request.getParameter(NORMALIZED_ADDRESS_PARAM);
        if (StringUtils.isNotBlank(normalizedAddress)) {
            try {
                command.setNormalizedAddress(normalizedAddress);
            } catch (Exception e) {
                // ignore this
            }
        }

        String cityName = request.getParameter(CITY_NAME_PARAM);
        if (StringUtils.isNotBlank(cityName)) {
            try {
                command.setCityName(cityName);
            } catch (Exception e) {
                // ignore this
            }
        }

        if (StringUtils.isNotBlank(request.getParameter(SEARCH_QUERY_PARAM))) {
            command.setSearchQuery(request.getParameter(SEARCH_QUERY_PARAM));

             // match this to what's in parseSearchQuery
            if (command.getLat() != 0 && command.getLon() != 0 &&
                    StringUtils.isNotBlank(command.getState()) &&
                    StringUtils.isNotBlank(command.getNormalizedAddress())) {
                parseSearchQuery(request.getParameter(SEARCH_QUERY_PARAM), command, request, errors);
            }
            // otherwise, no more processing; fall through to showing schoolSearch.jspx which then does client-side
            // geocoding on page load, and then automatically re-submits the search that includes info
            // from geocoding, back to this controller
        }
    }

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) commandObj;

        String searchQuery = request.getParameter(SEARCH_QUERY_PARAM);
        parseSearchQuery(searchQuery, command, request, errors);
    }

    /**
     * The way I have the form set up, an error is always generated by onBindAndValidate.
     * Therefore this method will never be reached.
     *
     * This is because the "success" view of the form is the same as the form itself, and this
     * was the easiest way to make that happen.
     */
    protected ModelAndView onSubmit(Object commandObj) throws Exception {
        throw new IllegalStateException("SchoolSearchWidgetController.onSubmit should never be called");
    }

    // ==============================================================================
    // BUSINESS LOGIC
    // ==============================================================================

    /**
     * Figure out what the user wants and give it to them. This method is only concerned with
     * determining the type of search. Once that is done, it should delegate to a specific load
     * method such as loadResultsForCity.
     */
    protected void parseSearchQuery(String searchQuery, SchoolSearchWidgetCommand command, HttpServletRequest request, BindException errors) {
        boolean hasResults = false;
        boolean shownError = false;

        // reset location marker latitude and longitude (but not current lat, lon)
        command.setShowLocationMarker(false);
        command.setLocationMarkerLat(0);
        command.setLocationMarkerLon(0);

        if (StringUtils.isNotBlank(searchQuery)) {
            searchQuery = searchQuery.trim();

            // All schools in city
            StringTokenizer tok = new StringTokenizer(searchQuery, ",");
            if (tok.countTokens() == 1 || tok.countTokens() == 2) {
                City city = null;
                if (tok.countTokens() == 1) {
                    // - Exact match for a unique Cityname (YES San Francisco, NO Lincoln)
                    city = _geoDao.findUniqueCity(searchQuery);
                } else if (tok.countTokens() == 2) {
                    // - Exact match for Cityname, State abbreviation
                    // - Exact match for Cityname, Statename
                    String cityStr = StringUtils.trim(tok.nextToken());
                    String stateStr = StringUtils.trim(tok.nextToken());
                    State state = getStateFromString(stateStr);
                    if (state != null) {
                        city = getCityFromString(state, cityStr);
                        if (city != null) {
                            city.setState(state);
                        }
                    }
                }

                // if a city was found, load all schools for that city
                if (city != null) {
                    command.setCity(city);
                    hasResults = loadResultsForCity(city, city.getState(), command);
                }
            }

            // if no results found for city matches, try proximity search
            if (!hasResults) {
                // - Exact match for 5 digit zip
                // - Exact match for Cityname, State abbreviation 5 digit zip
                // - Exact match for Cityname, State name 5 digit zip
                // - Exact match for Address, cityname, state abbreviation (or statename), 5 digit zip
                // - Exact match for Address, cityname, state abbreviation (or statename)
                // - Exact match for Address, cityname, 5 digit zip
                // - Exact match for Address, 5 digit zip

                if (command.getLat() != 0 && command.getLon() != 0 &&
                        StringUtils.isNotBlank(command.getState()) &&
                        StringUtils.isNotBlank(command.getNormalizedAddress())) {

                    State state = _stateManager.getState(command.getState());

                    if (StringUtils.isNotBlank(command.getCityName())) {
                        City city = getCityFromString(state, command.getCityName());
                        if (city != null) {
                            city.setState(state);
                            command.setCity(city);
                        } else {
                            command.setCity(null);
                        }
                    }

                    hasResults = loadResultsForLatLon(state, command.getLat(), command.getLon(), DISTANCE_IN_MILES, MAX_NUM_RESULTS, command.getNormalizedAddress(), command);
                } else {
                    errors.rejectValue("searchQuery", null, "An error occurred. Please try again.");
                    shownError = true;
                }
            }
        } else {
            errors.rejectValue("searchQuery", null, "Please enter a search query");
            shownError = true;
        }
        if (!hasResults) {
            if (!shownError) {
                errors.rejectValue("searchQuery", null, "No school results found for \"" + searchQuery + "\"");
            }
        } else {
            // this is needed so onSubmit does not get called 
            errors.reject("Show results");
            if (request.getParameter(DISPLAY_TAB_PARAM) == null) {
                // only set to map tab if another tab isn't specified
                command.setDisplayTab("map");
            }
        }
    }

    protected boolean loadResultsForLatLon(State state, float lat, float lon, float distanceInMiles, int maxNumResults, String address, SchoolSearchWidgetCommand command) {
        boolean hasResults = false;

        // level code filtering
        LevelCode lc;
        if (command.isPreschoolFilterChecked()
                && command.isElementaryFilterChecked()
                && command.isMiddleFilterChecked()
                && command.isHighFilterChecked()) {
            // do no filtering
            lc = null;
        } else {
            lc = LevelCode.createLevelCode(command.getLevelCodeString());
        }

        List<SchoolWithRatings> schools = _schoolDao.findNearbySchoolsWithRatings(state, lat, lon, distanceInMiles, maxNumResults, lc);

        if (schools != null && schools.size() > 0) {
            hasResults = true;

            // if city not already set, e.g. with certain zip codes,
            // set city to the one for the nearest school for which a mailing city is found 
            int i = 0;
            while (command.getCity() == null && i < schools.size()) {
                String cityName = schools.get(i).getSchool().getMailingCity();
                if (StringUtils.isNotBlank(cityName)) {
                    City city = getCityFromString(state, cityName);
                    if (city != null) {
                        city.setState(state);
                        command.setCity(city);
                    }
                }
                i++;
            }

            _reviewDao.loadRatingsIntoSchoolList(schools, state);
            command.setSchools(schools);
            command.setMapLocationPrefix("Schools near ");
            command.setMapLocationString(address);
            command.setMapLocationSuffix(":");
        }
        return hasResults;
    }

    /**
     * Load all schools in city into command.
     * @return false if no schools are found in city, true otherwise
     */
    protected boolean loadResultsForCity(City city, State state, SchoolSearchWidgetCommand command) {
        boolean hasResults = false;
        List<SchoolWithRatings> schools = _schoolDao.findSchoolsWithRatingsInCity(state, city.getName());
        applyLevelCodeFilters(schools, command); // edits list in place
        if (schools != null && schools.size() > 0) {
            hasResults = true;
            _reviewDao.loadRatingsIntoSchoolList(schools, state);
            command.setSchools(schools);
            command.setMapLocationPrefix("All schools in ");
            command.setMapLocationString(city.getName() + ", " + state.getAbbreviation());
            command.setMapLocationSuffix(":");
        }
        return hasResults;
    }

    /**
     * Obtain parent ratings for a list of schools.
     *
     * This groups the schools into two lists:
     * 1) Preschools
     * 2) Other schools
     * Then farms each list off to specially designed methods in IReviewDao that take a batch of
     * School Ids and returns a map of ids to Ratings objects.
     * This then loops through the school list, looks up the associated ratings object
     * in one of the maps, and attaches it to the data structure.
     */
//    protected void loadRatingsIntoSchoolList(List<SchoolWithRatings> schools, State state) {
//        long start = System.currentTimeMillis();
//
//        // Two lists: grade schools, preschools
//        List<Integer> gradeSchoolIds = new ArrayList<Integer>();
//        List<Integer> preschoolIds = new ArrayList<Integer>();
//        // for each school, group it into one of the above lists
//        for (SchoolWithRatings schoolWithRatings: schools) {
//            School school = schoolWithRatings.getSchool();
//            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
//                preschoolIds.add(school.getId());
//            } else {
//                gradeSchoolIds.add(school.getId());
//            }
//        }
//        // Retrieve parent ratings
//        // grade schools
//        Map<Integer, Ratings> gradeSchoolMap = null;
//        if (gradeSchoolIds.size() > 0) {
//            gradeSchoolMap = _reviewDao.findGradeSchoolRatingsByIdList(gradeSchoolIds, state);
//        }
//        // preschools
//        Map<Integer, Ratings> preschoolMap = null;
//        if (preschoolIds.size() > 0) {
//            preschoolMap = _reviewDao.findPreschoolRatingsByIdList(preschoolIds, state);
//        }
//        // for each school, look up its rating in one of the above maps and attach it to
//        // the data structure
//        for (SchoolWithRatings schoolWithRatings: schools) {
//            School school = schoolWithRatings.getSchool();
//            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
//                schoolWithRatings.setParentRatings(preschoolMap.get(school.getId()));
//            } else {
//                schoolWithRatings.setParentRatings(gradeSchoolMap.get(school.getId()));
//            }
//        }
//        long end = System.currentTimeMillis();
//
//        _log.info("Bulk retrieval of parent ratings took " + ((float)(end - start)) / 1000.0 + "s");
//    }

    /**
     * Manually filter out schools with level codes that don't match the filter.
     *
     * TODO: This can be done on the DB side when fetching the initial group of schools with ratings
     */
    protected void applyLevelCodeFilters(List<SchoolWithRatings> schools, SchoolSearchWidgetCommand command) {
        if (schools == null) {
            return; // exit early
        }
        String lcs = command.getLevelCodeString();
        List<SchoolWithRatings> itemsToRemove = new ArrayList<SchoolWithRatings>();
        if (StringUtils.isBlank(lcs)) {
            // filter out all schools
            itemsToRemove = schools;
        } else if (command.isPreschoolFilterChecked()
                && command.isElementaryFilterChecked()
                && command.isMiddleFilterChecked()
                && command.isHighFilterChecked()) {
            // do no filtering
        } else {
            LevelCode lc = LevelCode.createLevelCode(lcs);
            for (SchoolWithRatings schoolWithRatings: schools) {
                School school = schoolWithRatings.getSchool();
                if (!lc.containsSimilarLevelCode(school.getLevelCode())) {
                    itemsToRemove.add(schoolWithRatings);
                }
            }
        }
        schools.removeAll(itemsToRemove);
    }    

    // ==============================================================================
    // UTILITY METHODS
    // ==============================================================================

    protected State getStateFromString(String stateStr) {
        State state = null;
        if (StringUtils.length(stateStr) == 2) {
            state = _stateManager.getState(stateStr);
        } else if (StringUtils.length(stateStr) > 2) {
            state = _stateManager.getStateByLongName(stateStr);
        }
        return state;
    }

    protected City getCityFromString(State state, String cityStr) {
        City city = null;
        if (cityStr != null) {
            city = _geoDao.findCity(state, cityStr);
        }
        return city;
    }

    // ==============================================================================
    // SETTERS & GETTERS
    // ==============================================================================

    public boolean isHidePreschools() {
        return _hidePreschools;
    }

    public void setHidePreschools(boolean hidePreschools) {
        _hidePreschools = hidePreschools;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
