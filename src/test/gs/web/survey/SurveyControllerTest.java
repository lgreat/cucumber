package gs.web.survey;

import gs.data.admin.IPropertyDao;
import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.review.Poster;
import gs.data.state.State;
import gs.data.survey.ISurveyDao;
import gs.data.survey.QuestionGroup;
import gs.data.survey.*;
import gs.data.survey.UserResponse;
import gs.data.util.email.EmailHelperFactory;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
    private MockJavaMailSender _mailSender;

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

        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");

        EmailHelperFactory emailHelperFactory = new EmailHelperFactory();
        emailHelperFactory.setMailSender(_mailSender);
        _controller.setEmailHelperFactory(emailHelperFactory);
    }

    public void testGetRequest() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("level", "e");
        expect(_propertyDao.getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR)).andReturn("2003-2004");
        replay(_propertyDao);

        expect(_surveyDao.getSurvey("e")).andReturn(createSurvey());
        replay(_surveyDao);

        _controller.handleRequest(getRequest(), getResponse());

        verify(_surveyDao);
        verify(_propertyDao);
    }

    public void testFormBackingObject() throws Exception {
        User user = createUser(true);
        _sessionContext.setUser(user);
        School school = createSchool();
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setAttribute("year", "2007");
        getRequest().setParameter("level", "m");

        expect(_surveyDao.getSurvey("m")).andReturn(createSurvey());
        replay(_surveyDao);
        UserResponseCommand command = (UserResponseCommand) _controller.formBackingObject(getRequest());

        assertEquals(user, command.getUser());
        assertEquals(school, command.getSchool());
        assertEquals(2007, command.getYear());
        verify(_surveyDao);
    }

    public void testPostRequestNewUser() throws Exception {
        getRequest().setMethod("POST");
        getRequest().setParameter("email", "dlee@greatschools.net");
        getRequest().setParameter("year", "2004");
        getRequest().setParameter("level", "test");

        School school = createSchool();
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        Survey survey = createSurvey();
        User user = createUser(true);

        expect(_surveyDao.getSurvey("test")).andReturn(survey);
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
        assertEquals("get thank you for taking survey email", 1, _mailSender.getSentMessages().size());

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

    public void testPostRequestExistingUserFirstSurveyTaken() throws Exception {
        getRequest().setMethod("POST");
        getRequest().setParameter("email", "dlee@greatschools.net");
        getRequest().setParameter("year", "2001");
        getRequest().setParameter("who", "student");
        getRequest().setParameter("level", "test");
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
        expect(_surveyDao.hasTakenASurvey(user, school)).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        expect(_userDao.findUserFromEmailIfExists(user.getEmail())).andReturn(user);
        replay(_userDao);

        _controller.handleRequest(getRequest(), getResponse());
        assertEquals("get thank you for taking survey email", 1, _mailSender.getSentMessages().size());

        verify(_surveyDao);
        verify(_userDao);
    }

    public void testPostRequestExistingUserHasTakenSurveyBefore() throws Exception {
        getRequest().setMethod("POST");
        getRequest().setParameter("email", "dlee@greatschools.net");
        getRequest().setParameter("year", "2001");
        getRequest().setParameter("who", "student");
        getRequest().setParameter("level", "test");
        getRequest().setParameter("responseMap[q1a1].values", "Band");

        School school = createSchool();
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        Survey survey = createSurvey();
        SurveyPage page = survey.getPages().get(0);
        User user = createUser(false);

        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setWho(Poster.STUDENT);
        response.setYear(2001);
        response.setSurveyId(survey.getId());
        response.setSurveyPageId(page.getId());
        response.setAnswerId(1);
        response.setQuestionId(1);
        response.setSchoolId(school.getId());
        response.setResponseValue("Band");
        response.setState(school.getDatabaseState());

        expect(_surveyDao.getSurvey("test")).andReturn(survey);
        expect(_surveyDao.hasTakenASurvey(user, school)).andReturn(true);
        _surveyDao.removeAllUserResponses(survey, page, school, user);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        expect(_userDao.findUserFromEmailIfExists(user.getEmail())).andReturn(user);
        replay(_userDao);

        Cookie cookie = getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertNull("Member cookie should not yet exist", cookie);

        _controller.handleRequest(getRequest(), getResponse());
        assertNull("should not get email", _mailSender.getSentMessages());

        cookie = getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertEquals("set a member cookie if this is a new user", String.valueOf(user.getId()), cookie.getValue());

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
        Survey survey = createSurvey();
        urc.setSurvey(survey);
        urc.setPage(survey.getPages().get(0));
        BindException errors = new BindException(urc, "");
        expect(_surveyDao.hasTakenASurvey((User)anyObject(), (School)anyObject())).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        ModelAndView mAndV =_controller.onSubmit(getRequest(), getResponse(), urc, errors);
        UrlBuilder builder = new UrlBuilder(createSchool(), UrlBuilder.SCHOOL_PROFILE);

        assertEquals("put temp survey message cookie into response", SurveyController.TMP_MSG_COOKIE_VALUE,
                getResponse().getCookie("TMP_MSG").getValue());
        assertEquals("redirect:"+builder.asFullUrl(getRequest()), mAndV.getViewName());
    }

    public void testOnSubmitMultiPage() throws Exception {
        UserResponseCommand urc = new UserResponseCommand();
        urc.setUser(createUser(false));
        urc.setSchool(createSchool());
        Survey survey = createSurvey(3);
        urc.setSurvey(survey);
        urc.setPage(survey.getPages().get(0));
        BindException errors = new BindException(urc, "");
        expect(_surveyDao.hasTakenASurvey((User)anyObject(), (School)anyObject())).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        getRequest().setParameter("level", "m");
        getRequest().setParameter("year", "2006");
        
        ModelAndView mAndV =_controller.onSubmit(getRequest(), getResponse(), urc, errors);
        reset(_surveyDao);
        expect(_surveyDao.hasTakenASurvey((User)anyObject(), (School)anyObject())).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        UrlBuilder builder = new UrlBuilder(createSchool(), UrlBuilder.SCHOOL_TAKE_SURVEY);
        assertEquals("redirect:" + builder.asFullUrl(getRequest()) + "&level=m&p=2&year=2006", mAndV.getViewName());

        urc.setPage(survey.getPages().get(1));
        mAndV =_controller.onSubmit(getRequest(), getResponse(), urc, errors);
        reset(_surveyDao);
        expect(_surveyDao.hasTakenASurvey((User)anyObject(), (School)anyObject())).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        assertEquals("redirect:" + builder.asFullUrl(getRequest()) + "&level=m&p=3&year=2006", mAndV.getViewName());

        urc.setPage(survey.getPages().get(2));
        mAndV =_controller.onSubmit(getRequest(), getResponse(), urc, errors);
        builder = new UrlBuilder(createSchool(), UrlBuilder.SCHOOL_PROFILE);
        assertEquals("redirect:" + builder.asFullUrl(getRequest()), mAndV.getViewName());
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
        Survey survey = createSurvey();
        urc.setSurvey(survey);
        urc.setPage(survey.getPages().get(0));
        urc.setNLSignUpChecked(true);

        BindException errors = new BindException(urc, null);

        expect(_surveyDao.hasTakenASurvey((User)anyObject(), (School)anyObject())).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);

        //public or charter sign up for MSS
        _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription(user, SubscriptionProduct.MYSTAT, school)));
        replay(_subscriptionDao);

        _controller.onSubmit(getRequest(), getResponse(), urc, errors);
        verify(_subscriptionDao);

        //if private school, should sign up for parent advisor
        reset(_subscriptionDao);
        reset(_surveyDao);
        school.setType(SchoolType.PRIVATE);
        expect(_surveyDao.hasTakenASurvey((User)anyObject(), (School)anyObject())).andReturn(false);
        _surveyDao.saveSurveyResponses((List<UserResponse>)anyObject());
        replay(_surveyDao);
        _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription(user, SubscriptionProduct.PARENT_ADVISOR, school.getDatabaseState())));
        replay(_subscriptionDao);
        _controller.onSubmit(getRequest(), getResponse(), urc, errors);
        verify(_subscriptionDao);
    }

    public School createSchool() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(123);
        school.setName("Alameda High School");

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
        return createSurvey(1);
    }

    public Survey createSurvey(int pageCount) {
        Survey survey = new Survey();
        survey.setDescription("description with " + pageCount + " pages");
        survey.setId(1);
        List<SurveyPage> pages = new ArrayList<SurveyPage>();
        for (int i = 0; i < pageCount; i++) {
            SurveyPage page = new SurveyPage();
            int index = i+1;
            page.setId(index);
            page.setIndex(index);
            page.setTitle("page " + index);
            page.setQuestionGroups(Collections.<QuestionGroup>emptyList());
            pages.add(page);
        }
        survey.setPages(pages);
        survey.setTitle("Survey with " + pageCount + " pages.");
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

    public void xtestSendRealEmail() throws Exception {
        EmailHelperFactory emailHelperFactory = new EmailHelperFactory();
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("mail.greatschools.net");
        emailHelperFactory.setMailSender(mailSender);

        _controller.setEmailHelperFactory(emailHelperFactory);

        User user = createUser(false);
        user.setEmail("dlee@greatschools.net");
        School school = createSchool();

        _controller.sendEmail(user, school, getRequest());
    }
}
