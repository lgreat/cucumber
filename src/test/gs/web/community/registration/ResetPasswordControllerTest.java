package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 20, 2006
 * Time: 9:31:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResetPasswordControllerTest extends BaseControllerTestCase {
    private ResetPasswordController _controller;

    private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (ResetPasswordController) appContext.getBean(ResetPasswordController.BEAN_ID);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    public void testResetPassword() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            assertTrue(user.matchesPassword("foobar"));
            assertFalse(user.matchesPassword("newPass"));
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String hashString = userHash + user.getId();
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

            // set new password on command
            command.setPassword("newPass");
            command.setConfirmPassword("newPass");
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindAndValidate", errors.hasErrors());

            ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse("Unexpected errors on onSubmit", errors.hasErrors());
            assertEquals(mAndV.getViewName(), _controller.getSuccessView());
            user = _userDao.findUserFromEmail(user.getEmail());
            assertTrue(user.matchesPassword("newPass"));
            assertFalse(user.matchesPassword("foobar"));
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    /////////////////////////
    // onBindNewForm Tests //
    /////////////////////////

    public void testPasswordEmpty() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String hashString = userHash + user.getId();
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testUserProvisional() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.updateUser(user);
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String hashString = userHash + user.getId();
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testInvalidHash() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), new Integer(user.getId().intValue() + 1));
            String hashString = userHash + user.getId();
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testWrongUser() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String hashString = userHash + "1";
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testMissingUser() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String hashString = userHash + (user.getId().intValue() + 1); // id should not exist
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testInvalidHashString() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            String hashString = "randomGarbage";
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testParameterMissing() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindOnNewForm", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    /////////////////////////////
    // onBindAndValidate Tests //
    /////////////////////////////

    // This just tests that SOME password validation is done. Rigorous password validation testing is
    // done in the UserCommandValidatorTest
    public void testBadPassword() throws NoSuchAlgorithmException {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        user.setEmail("testResetPassword@greatschools.net");
        _userDao.saveUser(user);
        try {
            // set password on user
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            assertTrue(user.matchesPassword("foobar"));
            assertFalse(user.matchesPassword("newPass"));
            // generate hash for user
            String userHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String hashString = userHash + user.getId();
            getRequest().addParameter("id", hashString);
            _controller.onBindOnNewForm(getRequest(), command, errors);
            assertFalse("Unexpected errors on onBindOnNewForm", errors.hasErrors());

            // set new password on command
            command.setPassword("newPass");
            command.setConfirmPassword("newpass"); // typo
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue("Failed to get expected errors on onBindAndValidate", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }
}
