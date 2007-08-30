package gs.web.survey;

import gs.data.community.User;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.survey.Survey;
import gs.data.survey.UserResponse;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class UserResponseCommandSaTest extends TestCase {
    UserResponseCommand _command;
    public void setUp() throws Exception {
        super.setUp();
        _command = new UserResponseCommand();
    }

    public void testGetResponses() {
        UserResponse response = new UserResponse();
        final Integer answerId = 1234;
        final Integer questionId = 5678;

        response.setQuestionId(questionId);
        response.setAnswerId(answerId);
        response.setResponseValue("ok");

        _command.addToResponseMap(response);

        try {
            _command.getResponses();
            fail("user, survey, and school were never set");
        } catch(NullPointerException e) {
            assertTrue(true);
        }

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(9);

        User user = new User();
        user.setId(12354234);

        Survey survey = new Survey();
        survey.setId(982934);

        _command.setSchool(school);
        _command.setUser(user);
        _command.setSurvey(survey);

        List<UserResponse> responses = _command.getResponses();
        assertEquals(1, responses.size());
        UserResponse responseFromCmd = responses.get(0);

        assertEquals(questionId, responseFromCmd.getQuestionId());
        assertEquals(answerId, responseFromCmd.getAnswerId());
        assertEquals(user.getId(), responseFromCmd.getUserId());
        assertEquals(school.getId(), responseFromCmd.getSchoolId());
        assertEquals(survey.getId(), responseFromCmd.getSurveyId());
        assertEquals("ok", responseFromCmd.getResponseValue());
    }
}
