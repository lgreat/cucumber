package gs.web.school.usp;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
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

    public void setUp() throws Exception {
        super.setUp();
        _controller = new UspFormController();

        _userDao = EasyMock.createStrictMock(IUserDao.class);
        _uspHelper = EasyMock.createStrictMock(UspFormHelper.class);

        _controller.setUserDao(_userDao);
        _controller.setUspFormHelper(_uspHelper);
    }

    private void replayAllMocks() {
        replayMocks(_userDao);
        replayMocks(_uspHelper);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao);
        verifyMocks(_uspHelper);
    }

    private void resetAllMocks() {
        resetMocks(_userDao);
        resetMocks(_uspHelper);
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

        State state = State.CA;
        Integer schoolId = 1;
        School school = getSchool(state, schoolId);
        ModelMap modelMap = new ModelMap();

        expect(_uspHelper.getSchool(null, null)).andReturn(null);

        replayAllMocks();
        String view = _controller.showUspUserForm(modelMap, request, response, null, null);
        verifyAllMocks();

        assertEquals("", view);

        resetAllMocks();

        expect(_uspHelper.getSchool(state, schoolId)).andReturn(school);
        _uspHelper.formFieldsBuilderHelper(modelMap, request, response, school, state, null, false);
        expectLastCall();

        replayAllMocks();
        view = _controller.showUspUserForm(modelMap, request, response, schoolId, state);
        verifyAllMocks();

        assertEquals(UspFormController.FORM_VIEW, view);
    }

    public School getSchool(State state, Integer schoolId) {
        School school = new School();
        school.setId(schoolId);
        school.setDatabaseState(state);
        school.setName("QWERT Elementary School");
        return school;
    }
}