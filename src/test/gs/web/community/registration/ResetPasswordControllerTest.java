package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.easymock.MockControl;
import org.easymock.AbstractMatcher;

import java.security.NoSuchAlgorithmException;

/**
 * Provides testing for the reset password controller.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ResetPasswordControllerTest extends BaseControllerTestCase {
    private ResetPasswordController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ResetPasswordController();
        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _controller.setUserDao(_userDao);
    }

    public void testResetPassword() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = setupUser();

        // set password on user
        user.setPlaintextPassword("foobar");
        assertTrue(user.matchesPassword("foobar"));
        assertFalse(user.matchesPassword("newPass"));
        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

        // set new password on command
        command.setPassword("newPass");
        command.setConfirmPassword("newPass");
        _userControl.reset();
        setUserDaoToReturn(user);
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse("Unexpected errors on onBindAndValidate", errors.hasErrors());

        _userControl.reset();
        User modifiedUser = new User();
        modifiedUser.setId(new Integer(99));
        modifiedUser.setPlaintextPassword("newPass");
        _userDao.updateUser(modifiedUser);
        // checks that the user's password has actually changed
        _userControl.setMatcher(new UserPasswordMatcher());
        _userControl.replay();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse("Unexpected errors on onSubmit", errors.hasErrors());
        assertEquals(mAndV.getViewName(), _controller.getSuccessView());
    }

    public void testUserFromCookie() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = setupUser();
        user.setPlaintextPassword("foobar");

        getSessionContext().setUser(user);
        try {
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

            command.setPassword("newPass");
            command.setConfirmPassword("newPass");
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindAndValidate", errors.hasErrors());

            _userControl.reset();
            User modifiedUser = new User();
            modifiedUser.setId(new Integer(99));
            modifiedUser.setPlaintextPassword("newPass");
            _userDao.updateUser(modifiedUser);
            // checks that the user's password has actually changed
            _userControl.setMatcher(new UserPasswordMatcher());
            _userControl.replay();

            ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
            _userControl.verify();
            assertFalse("Unexpected errors on onSubmit", errors.hasErrors());
            assertEquals(mAndV.getViewName(), _controller.getSuccessView());

        } finally {
            getSessionContext().setUser(null);
        }
    }

    private class UserPasswordMatcher extends AbstractMatcher {
        protected boolean argumentMatches(Object oExpected, Object oActual) {
            User actual = (User) oActual;
            User expected = (User) oExpected;
            return actual.getPasswordMd5().equals(expected.getPasswordMd5()) &&
                    actual.getId().equals(expected.getId());
        }
    }

    private User setupUser() {
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        user.setId(new Integer(99));

        setUserDaoToReturn(user);
        return user;
    }

    private void setUserDaoToReturn(User user) {
        _userDao.findUserFromId(user.getId().intValue());
        _userControl.setReturnValue(user);
        _userControl.replay();
    }

    /////////////////////////
    // onBindNewForm Tests //
    /////////////////////////

    public void testPasswordEmpty() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = setupUser();

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testUserProvisional() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = setupUser();

        // set password on user
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testInvalidHash() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = setupUser();

        // set password on user
        user.setPlaintextPassword("foobar");

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), new Integer(user.getId().intValue() + 1));
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testWrongUser() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        user.setId(new Integer(99));

        // set password on user
        user.setPlaintextPassword("foobar");

        User wrongUser = new User();
        wrongUser.setEmail("wrongUser@greatschools.net");
        wrongUser.setId(new Integer(1));
        wrongUser.setPlaintextPassword("wrongwrong");
        setUserDaoToReturn(wrongUser);

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + "1";
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testMissingUser() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        user.setId(new Integer(99));

        // set password on user
        user.setPlaintextPassword("foobar");
        _userDao.findUserFromId(100);
        _userControl.setThrowable(new ObjectRetrievalFailureException
                ("Can't find user with id 100", null));
        _userControl.replay();

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + (user.getId().intValue() + 1); // id should not exist
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testInvalidHashString() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        String hashString = "randomGarbage";
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
    }

    public void testParameterMissing() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
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
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = setupUser();

        // set password on user
        user.setPlaintextPassword("foobar");
        assertTrue(user.matchesPassword("foobar"));
        assertFalse(user.matchesPassword("newPass"));

        // generate hash for user
        String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String hashString = userHash + user.getId();
        getRequest().addParameter("id", hashString);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _userControl.verify();
        assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

        _userControl.reset();
        setUserDaoToReturn(user);

        // set new password on command
        command.setPassword("newPass");
        command.setConfirmPassword("newpass"); // typo
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Failed to get expected errors on onBindAndValidate", errors.hasErrors());
    }
}
