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
    private static final String CITY_PARAM = "city";
    private static final String STATE_PARAM = "state";
    private static final String DISPLAY_TAB_PARAM = "displayTab";

    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private StateManager _stateManager;

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) super.formBackingObject(request);
        if (request.getParameter(DISPLAY_TAB_PARAM) != null) {
            command.setDisplayTab(request.getParameter(DISPLAY_TAB_PARAM));
        }
        State state = getStateFromString(request.getParameter(STATE_PARAM), true); // default to CA
        City city = getCityFromString(state, request.getParameter(CITY_PARAM), true); // default to SF
        command.setCity(city);
        command.setMapLocationPrefix("in ");
        command.setMapLocationString(city.getName() + ", " + state.getAbbreviation());
        return command;
    }

    protected State getStateFromString(String stateStr) {
        return getStateFromString(stateStr, false);
    }

    protected State getStateFromString(String stateStr, boolean defaultToCalifornia) {
        State state = null;
        if (stateStr != null) {
            state = _stateManager.getState(stateStr);
        }
        if (state == null && defaultToCalifornia) {
            state = State.CA;
        }
        return state;
    }

    protected City getCityFromString(State state, String cityStr) {
        return getCityFromString(state, cityStr, false);
    }

    protected City getCityFromString(State state, String cityStr, boolean defaultToSanFrancisco) {
        City city = null;
        if (cityStr != null) {
            city = _geoDao.findCity(state, cityStr);
        }
        if (city == null && defaultToSanFrancisco) {
            city = _geoDao.findCity(State.CA, "San Francisco");
        }
        return city;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        super.onBindAndValidate(request, commandObj, errors);

        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) commandObj;
        String searchQuery = request.getParameter(SEARCH_QUERY_PARAM);

        parseSearchQuery(searchQuery, command, errors);
    }

    protected void parseSearchQuery(String searchQuery, SchoolSearchWidgetCommand command, BindException errors) {
        boolean validSearch = false;
        boolean hasResults = false;
        boolean shownError = false;
        if (StringUtils.isNotBlank(searchQuery)) {
            StringTokenizer tok = new StringTokenizer(searchQuery, ",");
            if (tok.countTokens() == 2) {
                String cityStr = tok.nextToken();
                String stateStr = tok.nextToken();
                State state = getStateFromString(stateStr);
                if (state != null) {
                    City city = getCityFromString(state, cityStr);
                    if (city != null ) {
                        command.setCity(city);
                        validSearch = true;
                        List<SchoolWithRatings> schools = _schoolDao.findSchoolsWithRatingsInCity(state, cityStr);
                        applyLevelCodeFilters(schools, command); // edits list in place
                        if (schools != null && schools.size() > 0) {
                            hasResults = true;
                            loadRatingsIntoSchoolList(schools, state);
                            command.setSchools(schools);
                            command.setMapLocationPrefix("in ");
                            command.setMapLocationString(city.getName() + ", " + state.getAbbreviation());
                        }
                    } else {
                        errors.rejectValue("searchQuery", null, "I cannot find a city matching " + cityStr);
                        shownError = true;
                    }
                } else {
                    errors.rejectValue("searchQuery", null, "I cannot find a state matching " + stateStr);
                    shownError = true;
                }
            }
        } else {
            errors.rejectValue("searchQuery", null, "Please enter a search query");
            shownError = true;
        }
        if (!validSearch) {
            if (!shownError) {
                errors.rejectValue("searchQuery", null, "I do not understand that query");
            }
        } else if (!hasResults) {
            if (!shownError) {
                errors.rejectValue("searchQuery", null, "No results for that query");
            }
        } else {
            errors.reject("Show results");
            command.setDisplayTab("map");
        }
    }

    protected ModelAndView onSubmit(Object commandObj) throws Exception {
        throw new IllegalStateException("SchoolSearchWidgetController.onSubmit should never be called");
    }

    /**
     * Obtain parent ratings for a list of schools.
     *
     * Disgustingly inefficient. Ideally, this would group the schools into two lists:
     * 1) Preschools
     * 2) Other schools
     * Then farm each off to specially designed methods in IReviewDao that take a batch of School Ids and
     * return a map of ids to Ratings objects. This would then loop through the school list, look up the
     * associated ratings object in one of the maps, and attach it to the data structure.
     *
     * I bet that would be a lot faster, and certainly less trouble on the database.
     *
     * TODO: Make it so, Number One!
     */
    protected void loadRatingsIntoSchoolList(List<SchoolWithRatings> schools, State state) {
        long start = System.currentTimeMillis();
        
        // for each school
        List<Integer> gradeSchoolIds = new ArrayList<Integer>();
        List<Integer> preschoolIds = new ArrayList<Integer>();
        for (SchoolWithRatings schoolWithRatings: schools) {
            School school = schoolWithRatings.getSchool();
            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                preschoolIds.add(school.getId());
            } else {
                gradeSchoolIds.add(school.getId());
            }
        }
        // Retrieve parent ratings
        Map<Integer, Ratings> gradeSchoolMap = null;
        if (gradeSchoolIds.size() > 0) {
            gradeSchoolMap = _reviewDao.findGradeSchoolRatingsByIdList(gradeSchoolIds, state);
        }
        Map<Integer, Ratings> preschoolMap = null;
        if (preschoolIds.size() > 0) {
            preschoolMap = _reviewDao.findPreschoolRatingsByIdList(preschoolIds, state);
        }

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

    protected void applyLevelCodeFilters(List<SchoolWithRatings> schools, SchoolSearchWidgetCommand command) {
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
            _log.info(lc);
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
