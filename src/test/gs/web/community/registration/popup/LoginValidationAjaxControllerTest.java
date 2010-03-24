package gs.web.community.registration.popup;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.BaseControllerTestCase;
import gs.web.community.LoginController;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import javax.servlet.http.Cookie;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschoolure nimirs.net>
 */
public class LoginValidationAjaxControllerTest extends BaseControllerTestCase {
    private LoginValidationAjaxController _controller;
    private IUserDao _userDao;
    private User _user;
    private LoginHoverCommand _command;
    private BindException _errors;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (LoginValidationAjaxController) appContext.getBean(LoginValidationAjaxController.BEAN_ID);

        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);

        _user = new User();
        _user.setEmail("testLoginController@greatschools.org");
        _user.setId(99);


        _command = new LoginHoverCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");


        // Overridden so that successive calls to addCookie with the same cookie will overwrite the previous value
        // This is the behavior of the real HttpServletResponse so I'm unclear on why the mock one fails so hard
        _response = new MockHttpServletResponse() {
            @Override
            public void addCookie(Cookie cookie) {
                if (getCookie(cookie.getName()) != null) {
                    getCookie(cookie.getName()).setValue(cookie.getValue());
                }
                super.addCookie(cookie);
            }
        };

    }

    public void testCommand() {
        assertFalse(_command.isJoinError());
        assertNull(_command.getHow());
        _command.setHow("unittest");
        assertEquals("unittest", _command.getHow());
    }

    public void testNonexistentUserValidation() throws Exception {
        Map<Object,Object> errors;
        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(null);
        replay(_userDao);
        errors = _controller.validateLoginForm(getRequest(), _command);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", errors.get("noSuchUser") != null);
    }

    public void testBadEmailValidation() throws Exception {
        _command.setEmail("foo");
        replay(_userDao);
        Map<Object,Object> errors = _controller.validateLoginForm(getRequest(), _command);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", errors.get("email") != null);
    }

    public void testProvisionalUser() throws Exception {
        _user.setPlaintextPassword("foobar");
        _user.setEmailProvisional("foobar");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_userDao);

        _command.setEmail("testLoginController@greatschools.org");

         Map<Object,Object> errors = _controller.validateLoginForm(getRequest(), _command);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", errors.get("userNotValidated") != null);
    }

    public void testBadPassword() throws Exception {
        _user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_userDao);

        _command.setEmail("testLoginController@greatschools.org");
        _command.setPassword("wrongPassword");

        Map<Object,Object> errors = _controller.validateLoginForm(getRequest(), _command);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", errors.get("passwordMismatch") != null);
    }

    public void testDisabledUser() throws Exception {
        _user.setPlaintextPassword("foobar");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setActive(false);

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_userDao);

        _command.setEmail("testLoginController@greatschools.org");
        _command.setPassword("foobar");

        Map<Object,Object> errors = _controller.validateLoginForm(getRequest(), _command);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", errors.get("userDeactivated") != null);
    }

    public void testNoErrors() throws Exception {
        _user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        replay(_userDao);

        _command.setEmail(_user.getEmail());
        _command.setPassword("foobar");

        Map<Object,Object> errors = _controller.validateLoginForm(getRequest(), _command);
        verify(_userDao);
        assertFalse("Controller should not have any errors on validate", errors.size() > 0);
    }

    public void testSubmit() throws Exception {
        _user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_userDao);
        
        _command.setEmail(_user.getEmail());
        _command.setPassword("foobar");

        _controller.handle(getRequest(),getResponse(),_command, _errors);
        verify(_userDao);
        System.out.println(getResponse().getContentAsString());
        assertEquals("{}", getResponse().getContentAsString());
    }

}
