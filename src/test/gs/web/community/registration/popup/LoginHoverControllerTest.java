package gs.web.community.registration.popup;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.soap.ReportLoginRequest;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.soap.SoapRequestException;
import org.springframework.validation.BindException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import static org.easymock.classextension.EasyMock.*;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschoolure nimirs.net>
 */
public class LoginHoverControllerTest extends BaseControllerTestCase {
    private LoginHoverController _controller;
    private IUserDao _userDao;
    private User _user;
    private String _ip;
    private LoginHoverCommand _command;
    private BindException _errors;
    private ReportLoginRequest _soapRequest;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (LoginHoverController) appContext.getBean(LoginHoverController.BEAN_ID);

        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);

        _soapRequest = createMock(ReportLoginRequest.class);
        _controller.setReportLoginRequest(_soapRequest);

        _user = new User();
        _user.setEmail("testLoginController@greatschools.net");
        _user.setId(99);

        // this is the IP used when the request attribute is missing
        _ip = "127.0.0.1";

        _command = new LoginHoverCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");
    }

    public void testCommand() {
        assertFalse(_command.isJoinError());
        assertNull(_command.getHow());
        _command.setHow("unittest");
        assertEquals("unittest", _command.getHow());
    }

    public void testOnBindOnNewFormEmptyEmail() {
        _command.setEmail(null);
        _controller.onBindOnNewForm(getRequest(), _command, _errors);

        assertEquals("", _command.getEmail());
    }

    public void testOnBindOnNewFormEmailFromSC() {
        _command.setEmail(null);
        SessionContextUtil.getSessionContext(getRequest()).setEmail(_user.getEmail());
        _controller.onBindOnNewForm(getRequest(), _command, _errors);

        assertEquals(_user.getEmail(), _command.getEmail());
    }

    public void testMslOnlyUserValidation() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_userDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_userDao);

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("MSL only users do not get an error when trying to sign in", _errors.hasErrors());
    }

    public void testNonexistentUserValidation() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(null);
        replay(_userDao);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testBadEmailValidation() throws Exception {
        _command.setEmail("foo");
        replay(_userDao);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testProvisionalUser() throws Exception {
        _user.setPlaintextPassword("foobar");
        _user.setEmailProvisional("foobar");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_userDao);

        _command.setEmail("testLoginController@greatschools.net");

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testBadPassword() throws Exception {
        _user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_userDao);

        _command.setEmail("testLoginController@greatschools.net");
        _command.setPassword("wrongPassword");

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testDisabledUser() throws Exception {
        _user.setPlaintextPassword("foobar");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setActive(false);

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_userDao);

        _command.setEmail("testLoginController@greatschools.net");
        _command.setPassword("foobar");

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testNoErrors() throws Exception {
        _user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        replay(_userDao);

        _command.setEmail(_user.getEmail());
        _command.setPassword("foobar");

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertFalse("Controller should not have any errors on validate", _errors.hasErrors());
    }

    public void testJoinFormNoUserValidation() throws Exception {
        getRequest().setParameter("joinForm", "true");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(null);
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertFalse("Controller should not have any errors on validate", _errors.hasErrors());
    }

    public void testJoinFormMslUserValidation() throws Exception {
        getRequest().setParameter("joinForm", "true");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertFalse("Controller should not have any errors on validate", _errors.hasErrors());
    }
    
    public void testJoinFormExistingUserValidation() throws Exception {
        getRequest().setParameter("joinForm", "true");

        _user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);
        assertTrue("Existing user should receive errors on validate", _errors.hasErrors());
        assertTrue(_command.isJoinError());
    }

    public void testOnSubmitSuccess() throws Exception {
        // expect login to proceed despite error
        _user.setPlaintextPassword("foobar");
        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_userDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_userDao);

        _command.setPassword("foobar");

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Controller has errors on validate", _errors.hasErrors());

        _soapRequest.setTarget("http://community.greatschools.net/soap/user");
        _soapRequest.reportLoginRequest(_user, _ip);
        replay(_soapRequest);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_soapRequest);
        assertFalse("Controller has errors on submit", _errors.hasErrors());

        assertTrue(mAndV.getViewName().startsWith("redirect:"));
        assertTrue("Expect hover to be closed", StringUtils.contains(mAndV.getViewName(), "sendToDestination"));
    }

    public void testOnSubmitWithSoapError() throws Exception {
        // expect login to proceed despite error
        _user.setPlaintextPassword("foobar");
        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_userDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_userDao);

        _command.setPassword("foobar");

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Controller has errors on validate", _errors.hasErrors());

        _soapRequest.setTarget("http://community.greatschools.net/soap/user");
        _soapRequest.reportLoginRequest(_user, _ip);
        expectLastCall().andThrow(new SoapRequestException());
        replay(_soapRequest);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_soapRequest);
        assertFalse("Controller has errors on submit", _errors.hasErrors());

        assertTrue(mAndV.getViewName().startsWith("redirect:"));
        assertTrue("Expect hover to be closed", StringUtils.contains(mAndV.getViewName(), "sendToDestination"));
    }

    public void testOnSubmitJoin() throws Exception {
        // join submits are forwarded to registration
        getRequest().setParameter("joinForm", "true");
        replay(_userDao);
        replay(_soapRequest);

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_soapRequest);

        assertTrue(mAndV.getViewName().startsWith("redirect:"));
        assertTrue("Expect forward to registration", StringUtils.contains(mAndV.getViewName(), "registration"));
     }

    public void testOnSubmitMslUser() throws Exception {
        // MSL users are auto-forwarded to registration
        _command.setPassword("ignored!");

        expect(_userDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_userDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_userDao);
        replay(_soapRequest);

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Controller has errors on validate", _errors.hasErrors());

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_soapRequest);

        assertTrue(mAndV.getViewName().startsWith("redirect:"));
        assertTrue("Expect forward to registration", StringUtils.contains(mAndV.getViewName(), "registration"));
     }
}
