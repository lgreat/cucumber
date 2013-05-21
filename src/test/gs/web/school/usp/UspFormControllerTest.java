package gs.web.school.usp;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.community.registration.UserStateStruct;
import org.easymock.classextension.EasyMock;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

public class UspFormControllerTest extends BaseControllerTestCase {
    UspFormController _controller;
    private IUserDao _userDao;
    private UspFormHelper _uspHelper;
    private ISchoolDao _schoolDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new UspFormController();

        _userDao = EasyMock.createStrictMock(IUserDao.class);
        _schoolDao = EasyMock.createStrictMock(ISchoolDao.class);
        _uspHelper = EasyMock.createStrictMock(UspFormHelper.class);

        _controller.setUserDao(_userDao);
        _controller.setUspFormHelper(_uspHelper);
        _controller.setSchoolDao(_schoolDao);
    }

    private void replayAllMocks() {
        replayMocks(_userDao, _schoolDao, _uspHelper);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao, _schoolDao, _uspHelper);
    }

    private void resetAllMocks() {
        resetMocks(_userDao, _schoolDao, _uspHelper);
    }

    /**
     * Test with Nulls.
     */
    public void testCheckUserStateNulls() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        //Null email. Hence email should be invalid
        replayAllMocks();
        _controller.checkUserState(request,response,null,false,null);
        verifyAllMocks();

        String expectedJson = "{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":false,\"isNewUser\":true,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":false}";
        assertEquals("application/json", getResponse().getContentType());
        assertEquals(expectedJson, getResponse().getContentAsString());
    }

    public void testCheckUserStateWithProvisionalUser() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        request = getRequest();
        response = getResponse();
        //provisional user
        User user = new User();
        user.setId(1);
        user.setPlaintextPassword("password");
        user.setEmailVerified(false);
        user.setEmailProvisional("password");

        //Email is valid. However isLogin is false
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(request,response,"someone@somedomain.com",false,null);
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":false}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals("User is provisional.",expectedJson, getResponse().getContentAsString());
    }

    public void testCheckUserStateWithValidatedUserWrongPassword() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        request = getRequest();
        response = getResponse();

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        //Email is valid. But password does not match.
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(request,response,"someone@somedomain.com",true,null);
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":false,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":true}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals("User is email validated.But passwords dont match.",expectedJson, getResponse().getContentAsString());
    }

    public void testCheckUserStateWithValidatedUser() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        request = getRequest();
        response = getResponse();

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        //Email is valid.
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(request,response,"someone@somedomain.com",true,"password");
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":true}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals("User is email validated.",expectedJson, getResponse().getContentAsString());
    }

    public void testShowUserForm() {
        resetAllMocks();

        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        ModelMap modelMap = new ModelMap();

        replayAllMocks();
        String view = _controller.showUspUserForm(modelMap, request, response, null, null);
        verifyAllMocks();

        assertEquals("", view);

        resetAllMocks();

        State state = State.CA;
        Integer schoolId = 1;
        School school = getSchool(state, schoolId);

        expect(_schoolDao.getSchoolById(state,schoolId)).andReturn(school);
        _uspHelper.formFieldsBuilderHelper(modelMap, request, response, school, state, null, false);

        replayAllMocks();
        view = _controller.showUspUserForm(modelMap, request, response, schoolId, state);
        verifyAllMocks();

        assertEquals(UspFormController.FORM_VIEW, view);
    }

    public void testDetermineRedirectsWithNulls() {
        User user = new User();
        UserStateStruct userStateStruct = new UserStateStruct();
        School school = new School();
        HttpServletRequest request = getRequest();

        String url = _controller.determineRedirects(null, null, null, null, false);
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

        //user already has previous answers
        String url = _controller.determineRedirects(user, userStateStruct, school, request,true);
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

        String url = _controller.determineRedirects(user, userStateStruct, school, request,false);
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

        String url = _controller.determineRedirects(user, userStateStruct, school, request,false);
        assertEquals("User has been registered.",
                "http://www.greatschools.org/california/city/1-SchoolName/", url);
    }


    public School getSchool(State state, Integer schoolId) {
        School school = new School();
        school.setId(schoolId);
        school.setDatabaseState(state);
        school.setName("QWERT Elementary School");
        school.setActive(true);
        return school;
    }
}