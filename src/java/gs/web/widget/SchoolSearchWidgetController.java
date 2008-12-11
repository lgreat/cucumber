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
import gs.data.school.review.Ratings;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolSearchWidgetController extends SimpleFormController {
    private static final Logger _log = Logger.getLogger(SchoolSearchWidgetController.class);

    private static final String SEARCH_QUERY_PARAM = "searchQuery";
    private static final String DISPLAY_TAB_PARAM = "displayTab";

    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private StateManager _stateManager;

    protected void onBindOnNewForm(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) commandObj;

        if (request.getParameter(DISPLAY_TAB_PARAM) != null) {
            command.setDisplayTab(request.getParameter(DISPLAY_TAB_PARAM));
        }

        if (StringUtils.isNotBlank(request.getParameter(SEARCH_QUERY_PARAM))) {
            command.setSearchQuery(request.getParameter(SEARCH_QUERY_PARAM));
            parseSearchQuery(request.getParameter(SEARCH_QUERY_PARAM), command, errors);
        }
    }

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

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) commandObj;
        String searchQuery = request.getParameter(SEARCH_QUERY_PARAM);

        parseSearchQuery(searchQuery, command, errors);
    }

    /**
     * Figure out what the user wants and give it to them. This method is only concerned with
     * determining the type of search. Once that is done, it should delegate to a specific load
     * method such as loadResultsForCity.
     */
    protected void parseSearchQuery(String searchQuery, SchoolSearchWidgetCommand command, BindException errors) {
        boolean hasResults = false;
        boolean shownError = false;
        if (StringUtils.isNotBlank(searchQuery)) {
            // check for "CITY, STATE"
            StringTokenizer tok = new StringTokenizer(searchQuery, ",");
            if (tok.countTokens() == 2) {
                String cityStr = StringUtils.trim(tok.nextToken());
                String stateStr = StringUtils.trim(tok.nextToken());
                State state = getStateFromString(stateStr);
                if (state != null) {
                    City city = getCityFromString(state, cityStr);
                    if (city != null ) {
                        command.setCity(city);
                        hasResults = loadResultsForCity(city, state, command);
                    }
                }
                // TODO: check for "CITY, STATE ZIP
            }
            // TODO: check for "UNIQUE CITY"
            // TODO: check for "ZIP"
            // TODO: check for "ADDRESS etc."
        } else {
            errors.rejectValue("searchQuery", null, "Please enter a search query");
            shownError = true;
        }
        if (!hasResults) {
            if (!shownError) {
                errors.rejectValue("searchQuery", null, "No school results found for \"" + searchQuery + "\"");
            }
        } else {
            errors.reject("Show results");
            command.setDisplayTab("map");
        }
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
            loadRatingsIntoSchoolList(schools, state);
            command.setSchools(schools);
            command.setMapLocationPrefix("in ");
            command.setMapLocationString(city.getName() + ", " + state.getAbbreviation());
        }
        return hasResults;
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
    protected void loadRatingsIntoSchoolList(List<SchoolWithRatings> schools, State state) {
        long start = System.currentTimeMillis();
        
        // Two lists: grade schools, preschools
        List<Integer> gradeSchoolIds = new ArrayList<Integer>();
        List<Integer> preschoolIds = new ArrayList<Integer>();
        // for each school, group it into one of the above lists
        for (SchoolWithRatings schoolWithRatings: schools) {
            School school = schoolWithRatings.getSchool();
            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                preschoolIds.add(school.getId());
            } else {
                gradeSchoolIds.add(school.getId());
            }
        }
        // Retrieve parent ratings
        // grade schools
        Map<Integer, Ratings> gradeSchoolMap = null;
        if (gradeSchoolIds.size() > 0) {
            gradeSchoolMap = _reviewDao.findGradeSchoolRatingsByIdList(gradeSchoolIds, state);
        }
        // preschools
        Map<Integer, Ratings> preschoolMap = null;
        if (preschoolIds.size() > 0) {
            preschoolMap = _reviewDao.findPreschoolRatingsByIdList(preschoolIds, state);
        }
        // for each school, look up its rating in one of the above maps and attach it to
        // the data structure
        for (SchoolWithRatings schoolWithRatings: schools) {
            School school = schoolWithRatings.getSchool();
            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                schoolWithRatings.setParentRatings(preschoolMap.get(school.getId()));
            } else {
                schoolWithRatings.setParentRatings(gradeSchoolMap.get(school.getId()));
            }
        }
        long end = System.currentTimeMillis();

        _log.info("Bulk retrieval of parent ratings took " + ((float)(end - start)) / 1000.0 + "s");
    }

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
