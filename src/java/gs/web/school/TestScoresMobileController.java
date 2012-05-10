package gs.web.school;

import gs.data.school.*;
import gs.web.util.PageHelper;
import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class TestScoresMobileController implements Controller, IDeviceSpecificControllerPartOfPair {

    public static final String PARAM_SCHOOL_ID = "id";

    public static final String PARAM_STATE = "state";

    public static final String VIEW = "school/testScores-mobile";

    private static final String ERROR_VIEW = "/school/error";

    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;
    private TestScoresHelper _testScoresHelper;

    private RatingHelper _ratingHelper;
    private ISchoolDao _schoolDao;

    private static final Logger _log = Logger.getLogger(TestScoresMobileController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String schoolIdStr = request.getParameter(PARAM_SCHOOL_ID);
        String stateStr = request.getParameter(PARAM_STATE);
        Map<String, Object> model = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(stateStr) && StringUtils.isNotBlank(schoolIdStr) && (StringUtils.isNumeric(schoolIdStr))) {

            int schoolId = new Integer(schoolIdStr);
            State state = State.fromString(stateStr);

            try {
                School school = _schoolDao.getSchoolById(state, new Integer(schoolId));

                if (school.isActive()) {
                    model.put("school", school);
                    model.put("schoolTestScores", _testScoresHelper.getTestScores(school));

                    PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
                    boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());
                    Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);
                    model.put("gs_rating", gsRating);

                } else {
                    _log.error("School id: " + schoolIdStr + " in state: " + stateStr + " is inactive.");
                    return new ModelAndView(ERROR_VIEW, model);
                }

            } catch (ObjectRetrievalFailureException ex) {
                _log.warn("Could not get a valid or active school: " +
                        schoolIdStr + " in state: " + stateStr, ex);
                return new ModelAndView(ERROR_VIEW, model);
            }

        } else {
            _log.warn("Could not get a valid or active school: " +
                    schoolIdStr + " in state: " + stateStr);
            return new ModelAndView(ERROR_VIEW, model);
        }

        return new ModelAndView(VIEW, model);
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public TestScoresHelper getTestScoresHelper() {
        return _testScoresHelper;
    }

    public void setTestScoresHelper(TestScoresHelper testScoresHelper) {
        _testScoresHelper = testScoresHelper;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }
}