package gs.web.survey;

import gs.data.admin.IPropertyDao;
import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.review.Poster;
import gs.data.state.State;
import gs.data.survey.ISurveyDao;
import gs.data.survey.QuestionGroup;
import gs.data.survey.Survey;
import gs.data.survey.UserResponse;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyControllerTest extends BaseControllerTestCase {

    private SurveyController _controller;
    private ISurveyDao _surveyDao;
    private IUserDao _userDao;
    private IPropertyDao _propertyDao;
    private ISubscriptionDao _subscriptionDao;

    public void setUp() throws Exception {
        super.setUp();
        _surveyDao = createMock(ISurveyDao.class);
        _userDao = createMock(IUserDao.class);
        _propertyDao = createMock(IPropertyDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);

        _controller = new SurveyController();
        _controller.setSurveyDao(_surveyDao);
        _controller.setUserDao(_userDao);
        _controller.setPropertyDao(_propertyDao);
        _controller.setSubscriptionDao(_subscriptionDao);
    }

    public void testGetRequest() throws Exception {
        getRequest().setMethod("GET");
        expect(_propertyDao.getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR)).andReturn("2003-2004");
        replay(_propertyDao);

        expect(_surveyDao.getSurvey("test")).andReturn(createSurvey());
        replay(_surveyDao);

        _controller.handleRequest(getRequest(), getResponse());

        verify(_surveyDao);
        verify(_propertyDao);
    }

    public void testUserAndSchoolIsInCommand() throws Exception {
        User user = createUser(true);
        _sessionContext.setUser(user);
        School school = createSchool();
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        expect(_surveyDao.getSurvey("test")).andReturn(createSurvey());
        replay(_surveyDao);
        UserResponseCommand command = (UserResponseCommand) _controller.formBackingObject(getRequest());

        assertEquals(user, command.getUser());
        assertEquals(school, command.getSchool());
        verify(_surveyDao);
    }

    public void testPostRequestNewUser() throws Exception {
        getRequest().setMethod("POST");
        getRequest().setParameter("email", "dlee@greatschools.net");
        getRequest().setParameter("year", "2004");

        School school = createSchool();
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        Survey survey = createSurvey();
        User user = createUser(true);

        expect(_surveyDao.getSurvey("test")).andReturn(survey);
        _surveyDao.removeAllUserResponses(survey, school, user);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        expect(_userDao.findUserFromEmailIfExists(user.getEmail())).andReturn(null);
        _userDao.saveUser(user);
        expectLastCall().andAnswer(new IAnswer<Object>(){
            public Object answer() throws Throwable {
                User user = (User)getCurrentArguments()[0];
                user.setId(101);
                return user;
            }
        });

        replay(_userDao);

        _controller.handleRequest(getRequest(), getResponse());
        Cookie cookie = getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertEquals("set a member cookie if this is a new user", String.valueOf(101), cookie.getValue());

        verify(_surveyDao);
        verify(_userDao);
    }

    public void testInitBinder() throws Exception {
        SurveyController.PosterCustomProperyEditor editor = new SurveyController.PosterCustomProperyEditor();

        editor.setAsText("parent");
        assertEquals(Poster.PARENT, editor.getValue());
        editor.setValue(Poster.STUDENT);
        assertEquals(Poster.STUDENT.getName(), editor.getAsText());
    }

    public void testPostRequestExistingUser() throws Exception {
        getRequest().setMethod("POST");
        getRequest().setParameter("email", "dlee@greatschools.net");
        getRequest().setParameter("year", "2001");
        getRequest().setParameter("who", "student");
        getRequest().setParameter("responseMap[q1a1].values", "Band");

        School school = createSchool();
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        Survey survey = createSurvey();
        User user = createUser(false);

        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setWho(Poster.STUDENT);
        response.setYear(2001);
        response.setSurveyId(survey.getId());
        response.setAnswerId(1);
        response.setQuestionId(1);
        response.setSchoolId(school.getId());
        response.setResponseValue("Band");
        response.setState(school.getDatabaseState());

        expect(_surveyDao.getSurvey("test")).andReturn(survey);
        _surveyDao.removeAllUserResponses(survey, school, user);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        expect(_userDao.findUserFromEmailIfExists(user.getEmail())).andReturn(user);
        replay(_userDao);

        _controller.handleRequest(getRequest(), getResponse());

        verify(_surveyDao);
        verify(_userDao);
    }

    public void testPopulateUserResponses() throws Exception {
        UserResponseCommand urc = new UserResponseCommand();
        School school = createSchool();
        Survey survey = createSurvey();
        User user = createUser(false);

        urc.setSurvey(survey);
        urc.setUser(user);
        urc.setSchool(school);

        getRequest().addParameter("responseMap[q1a1].values", "Band");
        getRequest().addParameter("responseMap[q1a1].values", "Orchestra");
        getRequest().addParameter("responseMap[q1a1].values", "Choir");

        getRequest().addParameter("responseMap[q2a134].value", "Baseball");

        _controller.populateUserResponses(getRequest(), urc);

        List<UserResponse> responses = urc.getResponses();
        assertEquals(2, responses.size());

        UserResponse response = responses.get(1);
        assertEquals("Band,Orchestra,Choir",response.getResponseValue());
        assertEquals(school.getId(), response.getSchoolId());
        assertEquals(survey.getId(), response.getSurveyId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(new Integer(1), response.getQuestionId());
        assertEquals(new Integer(1), response.getAnswerId());

        response = responses.get(0);
        assertEquals("Baseball",response.getResponseValue());
        assertEquals(school.getId(), response.getSchoolId());
        assertEquals(survey.getId(), response.getSurveyId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(new Integer(2), response.getQuestionId());
        assertEquals(new Integer(134), response.getAnswerId());
    }

    public void testEmptyUserResponses() {
        getRequest().addParameter("responseMap[q1a1].values", "");
        getRequest().addParameter("responseMap[q1a1].values", "");
        getRequest().addParameter("responseMap[q1a1].values", "");

        UserResponseCommand urc = new UserResponseCommand();
        urc.setUser(createUser(false));
        urc.setSurvey(createSurvey());
        urc.setSchool(createSchool());

        _controller.populateUserResponses(getRequest(), urc);

        List<UserResponse> responses = urc.getResponses();
        assertEquals(0, responses.size());
    }

    public void testNoUserResponses() {
        UserResponseCommand urc = new UserResponseCommand();
        urc.setUser(createUser(false));
        urc.setSurvey(createSurvey());
        urc.setSchool(createSchool());

        _controller.populateUserResponses(getRequest(), urc);

        List<UserResponse> responses = urc.getResponses();
        assertEquals(0, responses.size());
    }

    public void testNoYearSelected() throws Exception {
        UserResponseCommand urc = new UserResponseCommand();
        BindException errors = new BindException(urc, "");
        _controller.onBindAndValidate(getRequest(), urc, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("year"));
    }

    public void testDontAgreeToTermsOfUse() throws Exception {
        UserResponseCommand urc = new UserResponseCommand();
        urc.setTerms(false);
        BindException errors = new BindException(urc, "");
        _controller.onBindAndValidate(getRequest(), urc, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("terms"));
    }

    public List<UserResponse> createResponses(School school, User user, Survey survey,
                                              Integer questionId, Integer answerId, String value) {
        List<UserResponse> responses = new ArrayList<UserResponse>();
        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setSurveyId(survey.getId());
        response.setSchoolId(school.getId());
        response.setState(school.getDatabaseState());
        response.setAnswerId(answerId);
        response.setQuestionId(questionId);
        response.setResponseValue(value);


        responses.add(response);

        return responses;
    }

    public void testOnSubmit() throws Exception {
        UserResponseCommand urc = new UserResponseCommand();
        urc.setUser(createUser(false));
        urc.setSchool(createSchool());
        urc.setSurvey(createSurvey());

        BindException errors = new BindException(urc, "");
        ModelAndView mAndV =_controller.onSubmit(getRequest(), getResponse(), urc, errors);
        UrlBuilder builder = new UrlBuilder(createSchool(), UrlBuilder.SCHOOL_PROFILE);

        assertEquals("put temp survey message cookie into response", SurveyController.TMP_MSG_COOKIE_VALUE,
                getResponse().getCookie("TMP_MSG").getValue());
        assertEquals("redirect:"+builder.asFullUrl(getRequest()), mAndV.getViewName());
    }

    public void testComputeAvailableYears() {
        List<Integer> years = _controller.computeSchoolYears("2003-2004");
        assertEquals(5, years.size());
        assertEquals(new Integer(2004), years.get(0));
        assertEquals(new Integer(2003), years.get(1));
        assertEquals(new Integer(2002), years.get(2));
        assertEquals(new Integer(2001), years.get(3));
        assertEquals(new Integer(2000), years.get(4));
    }

    public void testEmailSignUps() throws Exception {
        UserResponseCommand urc = new UserResponseCommand();
        School school = createSchool();
        school.setType(SchoolType.PUBLIC);
        urc.setSchool(school);

        User user = createUser(false);
        urc.setUser(user);

        urc.setNLSignUpChecked(true);

        BindException errors = new BindException(urc, null);

        //public or charter sign up for MSS
        _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription(user, SubscriptionProduct.MYSTAT, school)));
        replay(_subscriptionDao);

        _controller.onSubmit(getRequest(), getResponse(), urc, errors);                
        verify(_subscriptionDao);

        //if private school, should sign up for parent advisor
        reset(_subscriptionDao);
        school.setType(SchoolType.PRIVATE);
        _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription(user, SubscriptionProduct.PARENT_ADVISOR, school.getDatabaseState())));
        replay(_subscriptionDao);
        _controller.onSubmit(getRequest(), getResponse(), urc, errors);
        verify(_subscriptionDao);
    }

    public School createSchool() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(123);

        return school;
    }

    public User createUser(boolean newUser) {
        User user = new User();
        if (!newUser) {
            user.setId(1010);
        }
        user.setEmail("dlee@greatschools.net");

        return user;
    }

    public Survey createSurvey() {
        Survey survey = new Survey();
        survey.setDescription("description");
        survey.setId(1);
        survey.setQuestionGroups(Collections.<QuestionGroup>emptyList());
        survey.getTitle();

        return survey;
    }

    public void testQuestionAndAnswerIdPattern() {
        Matcher m = getMatcher("responseMap[q10a123].values");
        assertTrue(m.matches());
        assertEquals("10", m.replaceAll("$1"));
        assertEquals("123", m.replaceAll("$2"));

        m = getMatcher("email");
        assertFalse(m.matches());

        m = getMatcher("responseMap[q10a123].value");
        assertTrue(m.matches());
        assertEquals("10", m.replaceAll("$1"));
        assertEquals("123", m.replaceAll("$2"));
    }

    public Matcher getMatcher(final String input) {
        return SurveyController.QUESTION_ANSWER_IDS.matcher(input);
    }
}
