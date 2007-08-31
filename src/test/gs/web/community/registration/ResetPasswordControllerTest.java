package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.soap.ChangePasswordRequest;
import gs.data.soap.SoapRequestException;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import static org.easymock.classextension.EasyMock.*;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

/**
 * Provides testing for the reset password controller.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ResetPasswordControllerTest extends BaseControllerTestCase {
    private ResetPasswordController _controller;

    private IUserDao _userDao;
    private ChangePasswordRequest _soapRequest;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ResetPasswordController();

        _userDao = createMock(IUserDao.class);
        _soapRequest = createMock(ChangePasswordRequest.class);

        _controller.setUserDao(_userDao);
        _controller.setAuthenticationManager(new AuthenticationManager());
        _controller.setSoapRequest(_soapRequest);

        _user = setupUser();
    }

    public void testNotifyCommunity() throws SoapRequestException {
        _soapRequest.changePasswordRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user));
        verify(_soapRequest);

        reset(_soapRequest);
        _soapRequest.changePasswordRequest(_user);
        expectLastCall().andThrow(new SoapRequestException());
        replay(_soapRequest);

        assertFalse(_controller.notifyCommunity(_user));
        verify(_soapRequest);
    }

    public void testOnSubmitNoSoapError() throws NoSuchAlgorithmException, SoapRequestException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");

        command.setUser(_user);
        command.setNewPassword("123456");
        _soapRequest.changePasswordRequest(_user);
        replay(_soapRequest);

        reset(_userDao);
        _userDao.updateUser(_user);
        replay(_userDao);

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_soapRequest);
        verify(_userDao);
    }

    public void testOnSubmitYesSoapError() throws NoSuchAlgorithmException, SoapRequestException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");

        command.setUser(_user);
        command.setNewPassword("123456");
        command.setOldPassword("654321");
        _soapRequest.changePasswordRequest(_user);
        expectLastCall().andThrow(new SoapRequestException());
        replay(_soapRequest);

        reset(_userDao);
        replay(_userDao);

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_soapRequest);
        verify(_userDao);
    }

    public void testResetPassword() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");

        // set password on user
        _user.setPlaintextPassword("foobar");
        assertTrue(_user.matchesPassword("foobar"));
        assertFalse(_user.matchesPassword("newPass"));
        // generate hash for user
        String userHash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        String hashString = userHash + _user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

        // set new password on command
        command.setNewPassword("newPass");
        command.setNewPasswordConfirm("newPass");
        reset(_userDao);
        setUserDaoToReturn(_user);
        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertFalse("Unexpected errors on onBindAndValidate", errors.hasErrors());

        reset(_userDao);
        User modifiedUser = new User();
        modifiedUser.setId(99);
        modifiedUser.setPlaintextPassword("newPass");
        _userDao.updateUser(isA(User.class));
        replay(_userDao);
        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertFalse("Unexpected errors on onSubmit", errors.hasErrors());
    }

    public void testSetUpdated() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = _user;

        // set password on user
        user.setPlaintextPassword("foobar");
        assertTrue(user.matchesPassword("foobar"));
        assertFalse(user.matchesPassword("newPass"));
        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);

        // set new password on command
        command.setNewPassword("newPass");
        command.setNewPasswordConfirm("newPass");
        command.setUser(user);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        user.setUpdated(cal.getTime());
        assertTrue(cal.getTime().equals(user.getUpdated()));

        reset(_userDao);
        User modifiedUser = new User();
        modifiedUser.setId(99);
        modifiedUser.setPlaintextPassword("newPass");
        _userDao.updateUser(isA(User.class));
        replay(_userDao);
        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertFalse("Unexpected errors on onSubmit", errors.hasErrors());
        assertFalse(cal.getTime().equals(user.getUpdated()));
    }

    public void testCancel() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");

        getRequest().setParameter("cancel.x", "cancel");

        User user = new User();
        user.setId(1234);
        user.setPlaintextPassword("foobar");
        command.setUser(user);
        command.setNewPassword("barbaz");

        assertTrue(command.getUser().matchesPassword("foobar"));
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertFalse(errors.hasErrors());
        assertEquals(mAndV.getViewName(), _controller.getSuccessView());
        assertTrue(command.getUser().matchesPassword("foobar"));
    }

    public void testUserFromCookie() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = _user;
        user.setPlaintextPassword("foobar");

        getSessionContext().setUser(user);
        try {
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

            command.setNewPassword("newPass");
            command.setNewPasswordConfirm("newPass");
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindAndValidate", errors.hasErrors());

            reset(_userDao);
            User modifiedUser = new User();
            modifiedUser.setId(99);
            modifiedUser.setPlaintextPassword("newPass");
            _userDao.updateUser(isA(User.class));
            replay(_userDao);

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            verify(_userDao);
            assertFalse("Unexpected errors on onSubmit", errors.hasErrors());

        } finally {
            getSessionContext().setUser(null);
        }
    }

    private User setupUser() {
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        user.setId(99);

        setUserDaoToReturn(user);
        return user;
    }

    private void setUserDaoToReturn(User user) {
        expect(_userDao.findUserFromId(user.getId())).andReturn(user);
        replay(_userDao);
    }

    /////////////////////////
    // onBindNewForm Tests //
    /////////////////////////

    public void testPasswordEmpty() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = _user;

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testUserProvisional() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = _user;

        // set password on user
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testInvalidHash() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = _user;

        // set password on user
        user.setPlaintextPassword("foobar");

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId() + 1);
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testWrongUser() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        user.setId(99);

        // set password on user
        user.setPlaintextPassword("foobar");

        User wrongUser = new User();
        wrongUser.setEmail("wrongUser@greatschools.net");
        wrongUser.setId(1);
        wrongUser.setPlaintextPassword("wrongwrong");
        reset(_userDao);
        setUserDaoToReturn(wrongUser);

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + "1";
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testMissingUser() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        user.setId(99);

        // set password on user
        user.setPlaintextPassword("foobar");
        reset(_userDao);
        expect(_userDao.findUserFromId(100)).andThrow(new ObjectRetrievalFailureException
                ("Can't find user with id 100", null));
        replay(_userDao);

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + (user.getId() + 1); // id should not exist
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testInvalidHashString() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");

        String hashString = "randomGarbage";
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testParameterMissing() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        getSessionContext().setUser(null);
        assertNull(getSessionContext().getUser());
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    /////////////////////////////
    // onBindAndValidate Tests //
    /////////////////////////////

    // This just tests that SOME password validation is done. Rigorous password validation testing is
    // done in the UserCommandValidatorTest
    public void testBadPassword() throws NoSuchAlgorithmException {
        ResetPasswordController.ResetPasswordCommand command = new ResetPasswordController.ResetPasswordCommand();
        BindException errors = new BindException(command, "");
        User user = _user;

        // set password on user
        user.setPlaintextPassword("foobar");
        assertTrue(user.matchesPassword("foobar"));
        assertFalse(user.matchesPassword("newPass"));

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_userDao);
        assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

        reset(_userDao);
        setUserDaoToReturn(user);

        // set new password on command
        command.setNewPassword("newPass");
        command.setNewPasswordConfirm("newpass"); // typo
        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Failed to get expected errors on onBindAndValidate", errors.hasErrors());
    }
}
