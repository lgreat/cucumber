package gs.web.survey;

import gs.data.admin.IPropertyDao;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.school.review.Poster;
import gs.data.state.StateManager;
import gs.data.survey.ISurveyDao;
import gs.data.survey.Survey;
import gs.data.survey.SurveyPage;
import gs.data.survey.UserResponse;
import gs.data.util.email.EmailContentHelper;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.web.school.SchoolPageInterceptor;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.*;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class ReviewSurveyController extends SimpleFormController implements ReadWriteController {

    protected final static Log _log = LogFactory.getLog(ReviewSurveyController.class);

    public static final String BEAN_ID = "reviewSurveyController";

    private ISurveyDao _surveyDao;
    private String _viewName;
    private IUserDao _userDao;
    private IPropertyDao _propertyDao;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private StateManager _stateManager;

    public static final String MODEL_SCHOOL_YEARS = "schoolYears";
    protected final static String CURRENT_PAGE = "currentPage";
    protected final static Pattern QUESTION_ANSWER_IDS = Pattern.compile("^responseMap\\[q(\\d+)a(\\d+)\\]\\.values*$");

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
            throws Exception {
        Map<String, Object> referenceData = new HashMap<String, Object>();
        UserResponseCommand urc = (UserResponseCommand) command;
        //is of form 2005-2006
        String curAcadYear = getPropertyDao().getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR);
        referenceData.put(MODEL_SCHOOL_YEARS, computeSchoolYears(curAcadYear));

        return referenceData;
    }

    protected List<Integer> computeSchoolYears(String acadYear) {
        Integer currentYear = Integer.valueOf(acadYear.substring(acadYear.length() - 4, acadYear.length()));
        List<Integer> availableYears = new ArrayList<Integer>();
        availableYears.add(currentYear);

        for (int i = 1; i < 5; i++) {
            availableYears.add(currentYear - i);
        }
        return availableYears;
    }

    protected void initBinder(HttpServletRequest request,
                              ServletRequestDataBinder binder) {
        binder.setDisallowedFields(new String[]{"responseMap*"});
        binder.registerCustomEditor(Poster.class, new PosterCustomProperyEditor());
        binder.registerCustomEditor(LevelCode.Level.class, new LevelEditor());
    }

    //TODO dlee - move editor into its own class and refactor ParentReviewController to share this editor
    static class PosterCustomProperyEditor extends PropertyEditorSupport {
        public String getAsText() {
            Poster value = (Poster) getValue();
            if (value == null) {
                return null;
            } else {
                return value.getName();
            }
        }

        public void setAsText(final String text) {
            setValue(Poster.createPoster(text));
        }
    }

    protected Object formBackingObject(HttpServletRequest request) {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Survey survey = getSurveyDao().getSurvey("droytest");
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        UserResponseCommand urc = new UserResponseCommand();
        urc.setSurvey(survey);
        urc.setSchool(school);
        urc.setUser(user);
        if (user != null) {
            urc.setEmail(user.getEmail());
        }

        SurveyPage page = survey.getPages().get(0);
        urc.setPage(page);
        request.setAttribute(CURRENT_PAGE, 0);

        return urc;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors)
            throws Exception {
        UserResponseCommand urc = (UserResponseCommand) command;
        populateUserResponses(request, urc);
        if (!urc.getTerms()) {
            errors.rejectValue("terms", null, "Please accept our terms of use.");
        }

        if (urc.getYear() == 0) {
            errors.rejectValue("year", "survey_no_school_year_selected", "Please tell us what school year your responses are based on.");
        }

        for (Object o : errors.getAllErrors()) {
            ObjectError e = (ObjectError)o;
            _log.warn(e.toString());
        }
    }

    protected void populateUserResponses(HttpServletRequest request, UserResponseCommand urc) {
        Enumeration<String> params = request.getParameterNames();
        Matcher m;
        String paramName;
        while (params.hasMoreElements()) {
            paramName = params.nextElement();
            m = QUESTION_ANSWER_IDS.matcher(paramName);

            if (m.matches()) {
                Integer qId = Integer.valueOf(m.replaceAll("$1"));
                Integer aId = Integer.valueOf(m.replaceAll("$2"));
                String[] paramValues = request.getParameterValues(paramName);

                if (null != paramValues) {
                    UserResponse response = new UserResponse();
                    String responseValue = StringUtils.join(paramValues, ",");

                    if (!StringUtils.containsOnly(responseValue, ",")) {
                        response.setResponseValue(StringUtils.join(paramValues, ","));
                        response.setAnswerId(aId);
                        response.setQuestionId(qId);
                        urc.addToResponseMap(response);
                    }
                }
            }
        }
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
                                    BindException errors) throws Exception {
        UserResponseCommand urc = (UserResponseCommand) command;

        User user = urc.getUser();
        boolean isExistingUser = true;
        if (null == user) {
            user = getUserDao().findUserFromEmailIfExists(urc.getEmail());
            if (null == user) {
                user = new User();
                user.setEmail(urc.getEmail());
                getUserDao().saveUser(user);
                // Because of hibernate caching, it's possible for a list_active record
                // (with list_member id) to be commited before the list_member record is
                // committed. Adding this commitOrRollback prevents this.
                ThreadLocalTransactionManager.commitOrRollback();
//                PageHelper.setMemberCookie(request, response, user);
                isExistingUser = false;
            }
            // don't set member cookie per GS-8301
            // actually DO set the member cookie because of bug exposed by GS-8301
            PageHelper.setMemberCookie(request, response, user);
            urc.setUser(user);
        }

        School school = urc.getSchool();

        //user needs to have been populated before call to getResponses
        List<UserResponse> responses = urc.getResponses();


        if (isExistingUser) {
            if (_surveyDao.hasTakenASurvey(user, school)) {
                _surveyDao.removeAllUserResponses(urc.getSurvey(), urc.getPage(), school, user);
            }
            _surveyDao.saveSurveyResponses(responses);
        } else {
            _surveyDao.saveSurveyResponses(responses);
        }

        return new ModelAndView("redirect:http://localhost:8080/survey/surveyForm.page?state=CA&id=1");
    }


    // This static map contains a cache of all of the warning emails "recently" sent.
    private static Map<String, Date> sendHistory = new HashMap<String, Date>();


    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}