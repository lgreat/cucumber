package gs.web.school.usp;

import gs.data.community.User;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.community.registration.UserStateStruct;

import javax.servlet.http.HttpServletRequest;

public class UspFormHelperTest extends BaseControllerTestCase {
    UspFormHelper _helper;

    public void setUp() throws Exception {
        super.setUp();
        _helper = new UspFormHelper();
    }

    private void replayAllMocks() {
        replayMocks();
    }

    private void verifyAllMocks() {
        verifyMocks();
    }

    private void resetAllMocks() {
        resetMocks();
    }

    public void testDetermineRedirectsWithNulls() {
        User user = new User();
        UserStateStruct userStateStruct = new UserStateStruct();
        School school = new School();
        HttpServletRequest request = getRequest();

        String url = _helper.determineRedirects(null, null, null, null);
        assertEquals("Url should be blank since all params were null", null, url);
    }

    public void testDetermineRedirectsWithUserLoggedIn() throws Exception {

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        UserStateStruct userStateStruct = new UserStateStruct();
        userStateStruct.setUserLoggedIn(true);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        HttpServletRequest request = getRequest();

        String url = _helper.determineRedirects(user, userStateStruct, school, request);
        assertEquals("User is logged in.",
                "http://www.greatschools.org/school/usp/form.page?schoolId=1&showExistingAnswersMsg=true&state=CA", url);

    }

    public void testDetermineRedirectsWithUserInSession() throws Exception {

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        UserStateStruct userStateStruct = new UserStateStruct();
        userStateStruct.setUserInSession(true);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        HttpServletRequest request = getRequest();

        String url = _helper.determineRedirects(user, userStateStruct, school, request);
        assertEquals("User is in the session in.",
                "http://www.greatschools.org/school/usp/thankYou.page?schoolId=1&state=CA", url);
    }

    public void testDetermineRedirectsWithUserRegistered() throws Exception {
        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        UserStateStruct userStateStruct = new UserStateStruct();
        userStateStruct.setUserRegistered(true);

        School school = new School();
        school.setId(1);
        school.setName("schoolName");
        school.setCity("city");
        school.setDatabaseState(State.CA);
        HttpServletRequest request = getRequest();

        String url = _helper.determineRedirects(user, userStateStruct, school, request);
        assertEquals("User has been registered.",
                "http://www.greatschools.org/california/city/1-SchoolName/", url);
    }

}