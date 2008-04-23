package gs.web.survey;

import gs.data.admin.IPropertyDao;
import gs.data.community.*;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.school.review.Poster;
import gs.data.state.StateManager;
import gs.data.survey.ISurveyDao;
import gs.data.survey.Survey;
import gs.data.survey.SurveyPage;
import gs.data.survey.UserResponse;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.joda.time.DateTime;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyController extends SimpleFormController implements ReadWriteController {

    protected final static Log _log = LogFactory.getLog(SurveyController.class);

    public static final String BEAN_ID = "surveyController";

    /**
     * list of school years to show
     */
    public static final String MODEL_SCHOOL_YEARS = "schoolYears";
    public static final String TMP_MSG_COOKIE_VALUE = "fromSurvey";
    public static final String TMP_MSG_COOKIE_PREFIX = "fromSurvey";
    protected final static String CURRENT_PAGE = "currentPage";

    private ISurveyDao _surveyDao;
    private String _viewName;
    private IUserDao _userDao;
    private IPropertyDao _propertyDao;
    private ISubscriptionDao _subscriptionDao;
    private EmailHelperFactory _emailHelperFactory;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private StateManager _stateManager;

    protected final static Pattern QUESTION_ANSWER_IDS = Pattern.compile("^responseMap\\[q(\\d+)a(\\d+)\\]\\.values*$");

    List<School> buildSchoolList(UserResponseCommand urc, LevelCode.Level level, boolean next) {
        List tempList = null;
        if (next) {
            tempList = getSchoolDao().findSchoolsInCity(urc.getNextState(), urc.getNextCity(), false);
        } else {
            tempList = getSchoolDao().findSchoolsInCity(urc.getPrevState(), urc.getPrevCity(), false);
        }
        ArrayList<School> schoolList = new ArrayList<School>(CollectionUtils.select(tempList,
                LevelPredicateFactory.createLevelPredicate(level)));

        // add default school to the beginning of the list
        schoolList.add(0, getSurveyDao().getDefaultNextPrevSchool());

        // add "my school not listed" to the end of the list
        schoolList.add(getSurveyDao().getSchoolNotListed());

        return schoolList;
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
            throws Exception {
        Map referenceData = new HashMap();
        //is of form 2005-2006
        String curAcadYear = getPropertyDao().getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR);
        referenceData.put(MODEL_SCHOOL_YEARS, computeSchoolYears(curAcadYear));

        UserResponseCommand urc = (UserResponseCommand) command;

        if (urc.getPage().containsNextOrPreviousSchoolQuestion()) {
            List<School> previousSchools = new ArrayList<School>();
            List<School> nextSchools = new ArrayList<School>();
            List<City> nextCities;
            List<City> previousCities;

            previousCities = getGeoDao().findCitiesByState(urc.getPrevState());
            nextCities = getGeoDao().findCitiesByState(urc.getNextState());

            LevelCode.Level surveyLevel = urc.getLevel();
            LevelCode.Level prevLevel = null;
            LevelCode.Level nextLevel = null;

            School schoolNotListed = getSurveyDao().getSchoolNotListed();

            if (LevelCode.Level.ELEMENTARY_LEVEL.equals(surveyLevel)) {
                previousSchools.add(getSurveyDao().getDefaultNextPrevSchool());
                previousSchools.addAll(getSurveyDao().getBeforeESOptions());
                nextSchools = buildSchoolList(urc, LevelCode.Level.MIDDLE_LEVEL, true);
                prevLevel = null;
                nextLevel = LevelCode.Level.MIDDLE_LEVEL;
            }

            if (LevelCode.Level.MIDDLE_LEVEL.equals(surveyLevel)) {
                previousSchools = buildSchoolList(urc, LevelCode.Level.ELEMENTARY_LEVEL, false);
                nextSchools = buildSchoolList(urc, LevelCode.Level.HIGH_LEVEL, true);
                prevLevel = LevelCode.Level.ELEMENTARY_LEVEL;
                nextLevel = LevelCode.Level.HIGH_LEVEL;
            }

            if (LevelCode.Level.HIGH_LEVEL.equals(surveyLevel)) {
                previousSchools = buildSchoolList(urc, LevelCode.Level.MIDDLE_LEVEL, false);
                nextSchools.add(getSurveyDao().getDefaultNextPrevSchool());
                nextSchools.addAll(getSurveyDao().getAfterHSOptions());
                prevLevel = LevelCode.Level.MIDDLE_LEVEL;
                nextLevel = null;
            }

            referenceData.put("prevLevel", prevLevel);
            referenceData.put("nextLevel", nextLevel);

            referenceData.put("prevCities", previousCities);
            referenceData.put("prevSchools", previousSchools);

            referenceData.put("nextCities", nextCities);
            referenceData.put("nextSchools", nextSchools);

            referenceData.put("schoolNotListed", schoolNotListed);
            referenceData.put("states", getStateManager().getSortedAbbreviations());
        }

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
        String levelParam = request.getParameter("level");
        Survey survey = getSurveyDao().getSurvey(levelParam);
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        UserResponseCommand urc = new UserResponseCommand();
        urc.setSurvey(survey);
        urc.setSchool(school);
        urc.setUser(user);
        if (user != null) {
            urc.setEmail(user.getEmail());
        }

        String yearParam = (String) request.getAttribute("year");
        if (StringUtils.isBlank(yearParam)) {
            yearParam = request.getParameter("year");
        }
        if (StringUtils.isNotBlank(yearParam)) {
            try {
                urc.setYear(Integer.parseInt(yearParam));
            } catch (NumberFormatException e) {
                _log.warn("incorrect format for year parameter", e);
            }
        }

        int index = getPageIndexFromRequest(request);

        if (index < 1 || index > survey.getPages().size()) {
            index = 1;
        }
        SurveyPage page = survey.getPages().get(index - 1);
        urc.setPage(page);
        request.setAttribute(CURRENT_PAGE, index);

        if (page.containsNextOrPreviousSchoolQuestion()) {
            if ("GET".equals(request.getMethod())) {
                //set defaults
                urc.setPrevState(school.getDatabaseState());
                urc.setPrevCity(school.getCity());

                urc.setNextState(school.getDatabaseState());
                urc.setNextCity(school.getCity());
            }
        }

        List responses = _surveyDao.getSurveyResponses(survey.getId(), school);
        if (responses != null) {
            urc.setPreviousResponseCount(responses.size());
        }

        return urc;
    }

    /**
     * This is a help method to extract the page index from request parameters.
     *
     * @param request an HttpServletRequest type
     * @return an int index
     */
    int getPageIndexFromRequest(HttpServletRequest request) {
        int index = 1;

        String pageParam = (String) request.getAttribute("p");
        if (StringUtils.isBlank(pageParam)) {
            pageParam = request.getParameter("p");
        }

        if (StringUtils.isNotBlank(pageParam)) {
            try {
                index = Integer.parseInt(pageParam);
            } catch (Exception e) {
                _log.warn("Error parsing page index", e);
            }
        }
        return index;
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

        //save previous school
        if (urc.getPrevSchoolId() != 0) {
            UserResponse response = new UserResponse();
            String responseValue = urc.getPrevState().getAbbreviation() + urc.getPrevSchoolId();
            response.setResponseValue(responseValue);
            //TODO dlee fix hardcoding
            response.setAnswerId(1);
            response.setQuestionId(10);
            urc.addToResponseMap(response);
        }

        //save future school
        if (urc.getNextSchoolId() != 0) {
            UserResponse response = new UserResponse();
            String responseValue = urc.getNextState().getAbbreviation() + urc.getNextSchoolId();
            response.setResponseValue(responseValue);
            //TODO dlee fix hardcoding
            response.setAnswerId(1);
            response.setQuestionId(11);
            urc.addToResponseMap(response);
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
                PageHelper.setMemberCookie(request, response, user);
                isExistingUser = false;
            }
            PageHelper.setMemberCookie(request, response, user);
            urc.setUser(user);
        }

        School school = urc.getSchool();

        //user needs to have been populated before call to getResponses
        List<UserResponse> responses = urc.getResponses();

        if (isExistingUser) {
            if (!_surveyDao.hasTakenASurvey(user, school)) {
                sendEmail(user, school, request);
            } else {
                _surveyDao.removeAllUserResponses(urc.getSurvey(), urc.getPage(), school, user);
            }
            _surveyDao.saveSurveyResponses(responses);
        } else {
            sendEmail(user, school, request);
            _surveyDao.saveSurveyResponses(responses);
        }

        //does this user want an email newsletter?
        if (urc.isNLSignUpChecked()) {
            Subscription sub;
            if (SchoolType.PRIVATE.equals(school.getType())) {
                sub = new Subscription(user, SubscriptionProduct.PARENT_ADVISOR, school.getDatabaseState());
            } else {
                sub = new Subscription(user, SubscriptionProduct.MYSTAT, school);
            }
            getSubscriptionDao().addNewsletterSubscriptions(user, Arrays.asList(sub));
        }

        Survey survey = urc.getSurvey();
        SurveyPage sp = urc.getPage();

        int curPageIndex = sp.getIndex();


        String level = request.getParameter("level");
        checkSubmitCount(school, level, sp.getId(), request);

        String redirectURL;
        if (curPageIndex >= survey.getPages().size()) {
            UrlBuilder builder = new UrlBuilder(urc.getSchool(), UrlBuilder.SURVEY_RESULTS);
            StringBuffer buffer = new StringBuffer(builder.asFullUrl(request));
            buffer.append("&level=");
            buffer.append(level);
            buffer.append("&thanks=true");
            redirectURL = buffer.toString();
        } else {
            UrlBuilder builder = new UrlBuilder(urc.getSchool(), UrlBuilder.SCHOOL_TAKE_SURVEY);
            StringBuffer buffer = new StringBuffer(builder.asFullUrl(request));
            buffer.append("&level=");
            buffer.append(level);
            buffer.append("&p=");
            int nextPage = curPageIndex + 1;
            buffer.append(nextPage);

            String yearParam = (String) request.getAttribute("year");
            if (StringUtils.isBlank(yearParam)) {
                yearParam = request.getParameter("year");
            }
            buffer.append("&year=");
            buffer.append(yearParam);
            redirectURL = buffer.toString();
        }
        return new ModelAndView("redirect:" + redirectURL);
    }


    // This static map contains a cache of all of the warning emails "recently" sent.
    private static Map<String, Date> sendHistory = new HashMap<String, Date>();

    protected void checkSubmitCount(School school, String level, int page, HttpServletRequest request) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -4);
        LevelCode.Level l = LevelCode.Level.getLevelCode(level);
        if (l != null) {
            int taken = _surveyDao.getNumSurveysTaken(school, page, l, cal.getTime());
            if (taken > 3) {
                // only send an email if one has not been sent in the last day.
                String key = school.getDatabaseState().getAbbreviation() + String.valueOf(school.getId());
                Date lastSend = sendHistory.get(key);
                Date now = new Date();
                if (lastSend == null || !DateUtils.isSameDay(lastSend, now)) {
                    try {
                        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
                        emailHelper.setToEmail("parentsurveyfeedback@greatschools.net");
                        emailHelper.setFromEmail("survey@greatschools.net");
                        emailHelper.setFromName("GreatSchools Survey System");
                        emailHelper.setSubject("Survey submit alert for " + school.getName());
                        StringBuffer message = new StringBuffer();
                        message.append(taken);
                        message.append(" surveys have been submitted in the past 4 hours.\n");
                        message.append(school.toString());
                        message.append("\nThis message was generated on: ");
                        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
                        if (new UrlUtil().isDevEnvironment(sessionContext.getHostName())) {
                            message.append(" DEVELOPMENT");
                        } else {
                            message.append(" PRODUCTION");
                        }
                        emailHelper.setTextBody(message.toString());
                        emailHelper.send();
                        sendHistory.put(key, now);
                    } catch (MessagingException e) {
                        _log.warn(e);
                    }
                }
            }
        }
    }

    protected void sendEmail(User user, School school, HttpServletRequest request) throws MessagingException, IOException {
        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setToEmail(user.getEmail());
        emailHelper.setFromEmail("survey@greatschools.net");
        emailHelper.setFromName("GreatSchools");
        emailHelper.setSubject("Thanks for completing the survey about " + school.getName());
        emailHelper.setExtraStyles(".bold {font-weight:bold;} .italic {font-style:italic;} li {padding-bottom:1em;}");
        emailHelper.setSentToCustomMessage("<p>This message was sent to $EMAIL</p>");
        emailHelper.readHtmlFromResource("/gs/web/survey/thankYouEmail.txt");

        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        builder.addParameter("cpn", "autoprtsurvey");
        String parentReviewHref = builder.asFullUrl(request);

        String communityHost = UrlBuilder.getCommunitySiteBaseUrl(request) + "?cpn=autoprtsurvey";

        Map replacements = new HashMap();
        replacements.put("SCHOOL_NAME", school.getName());
        replacements.put("PARENT_REVIEW_URL", parentReviewHref);
        replacements.put("COMMUNITY_URL", communityHost);

        emailHelper.setInlineReplacements(replacements);
        emailHelper.send();
    }

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

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
