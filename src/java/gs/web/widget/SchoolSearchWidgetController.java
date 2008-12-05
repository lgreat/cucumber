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
import gs.data.school.review.IReviewDao;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.util.StringTokenizer;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolSearchWidgetController extends SimpleFormController {
    private static final Logger _log = Logger.getLogger(SchoolSearchWidgetController.class);
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private StateManager _stateManager;

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) super.formBackingObject(request);
        if (request.getParameter("displayTab") != null) {
            command.setDisplayTab(request.getParameter("displayTab"));
        }
        return command;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        super.onBindAndValidate(request, commandObj, errors);

        SchoolSearchWidgetCommand command = (SchoolSearchWidgetCommand) commandObj;
        String searchQuery = request.getParameter("searchQuery");

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
                State state = _stateManager.getState(stateStr);
                if (state != null) {
                    City city = _geoDao.findCity(state, cityStr);
                    if (city != null ) {
                        command.setCity(city);
                        validSearch = true;
                        List<SchoolWithRatings> schools = _schoolDao.findSchoolsWithRatingsInCity(state, cityStr);
                        if (schools != null && schools.size() > 0) {
                            hasResults = true;
                            loadRatingsIntoSchoolList(schools);
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
     * Obtain parent ratings for a list of schools
     */
    protected void loadRatingsIntoSchoolList(List<SchoolWithRatings> schools) {
        // for each school
        for (SchoolWithRatings schoolWithRatings: schools) {
            School school = schoolWithRatings.getSchool();
            // Retrieve parent ratings
            schoolWithRatings.setParentRatings(_reviewDao.findRatingsBySchool(school));
        }
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
