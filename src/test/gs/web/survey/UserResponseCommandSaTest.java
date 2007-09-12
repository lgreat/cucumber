package gs.web.survey;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.survey.Survey;
import gs.data.survey.UserResponse;
import gs.data.survey.SurveyPage;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        SurveyPage page = new SurveyPage();
        page.setId(1);

        _command.setSchool(school);
        _command.setUser(user);
        _command.setSurvey(survey);
        _command.setPage(page);

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

    public void testShowNLPromo() {
        School school = new School();
        school.setType(SchoolType.PRIVATE);
        school.setDatabaseState(State.CA);
        school.setId(23);

        User user = new User();
        user.setEmail("dlee@greatschools.net");

        _command.setSchool(school);
        _command.setUser(null);

        assertTrue("user unknown", _command.isNLPromoShown());

        _command.setUser(user);
        assertTrue("user does not have any newsletters", _command.isNLPromoShown());

        Subscription sub = new Subscription(user, SubscriptionProduct.PARENT_ADVISOR, State.CA);

        Set<Subscription> subs = new HashSet<Subscription>();
        subs.add(sub);
        user.setSubscriptions(subs);

        assertFalse("user already has a Parent Advisor and this is a private school", _command.isNLPromoShown());

        school.setType(SchoolType.PUBLIC);
        assertTrue("user does not have any mss yet and this is a public school", _command.isNLPromoShown());

        Subscription sub2 = new Subscription(user, SubscriptionProduct.MYSTAT, school);
        subs.add(sub2);
        assertFalse("user already has a MSS for this school", _command.isNLPromoShown());
    }
}
