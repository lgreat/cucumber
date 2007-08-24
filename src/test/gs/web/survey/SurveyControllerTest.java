package gs.web.survey;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.survey.ISurveyDao;
import gs.data.survey.QuestionGroup;
import gs.data.survey.Survey;
import gs.data.survey.UserResponse;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
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

    public void setUp() throws Exception {
        super.setUp();
        _surveyDao = createMock(ISurveyDao.class);
        _userDao = createMock(IUserDao.class);

        _controller = new SurveyController();
        _controller.setSurveyDao(_surveyDao);
        _controller.setUserDao(_userDao);

    }

    public void testGetRequest() throws Exception {
        getRequest().setMethod("GET");
        expect(_surveyDao.getSurvey("test")).andReturn(createSurvey());
        replay(_surveyDao);

        _controller.handleRequest(getRequest(), getResponse());

        verify(_surveyDao);
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
