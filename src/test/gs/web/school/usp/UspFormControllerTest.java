package gs.web.school.usp;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.BaseControllerTestCase;
import org.easymock.classextension.EasyMock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.expect;

public class UspFormControllerTest extends BaseControllerTestCase {
    UspFormController _controller;
    private IUserDao _userDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new UspFormController();

        _userDao = EasyMock.createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);
    }

    private void replayAllMocks() {
        replayMocks(_userDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao);
    }

    private void resetAllMocks() {
        resetMocks(_userDao);
    }

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
        assertEquals(expectedJson, getResponse().getContentAsString());
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
        assertEquals(expectedJson, getResponse().getContentAsString());
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

        //Email is valid. However isLogin is false
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(request,response,"someone@somedomain.com",true,"password");
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":true}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals(expectedJson, getResponse().getContentAsString());
    }

}