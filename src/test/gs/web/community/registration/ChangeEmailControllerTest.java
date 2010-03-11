/**
 * Copyright (c) 2006 GreatSchools.org. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;
import gs.data.community.UserProfile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import static org.easymock.classextension.EasyMock.*;

/**
 * Provides testing for the controller that changes a user's email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ChangeEmailControllerTest extends BaseControllerTestCase {
    private ChangeEmailController _controller;
    private IUserDao _userDao;
    private EmailVerificationEmail _emailVerificationEmail;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ChangeEmailController();

        _userDao = createMock(IUserDao.class);
        _emailVerificationEmail = createMock(EmailVerificationEmail.class);

        _controller.setUserDao(_userDao);
        _controller.setEmailVerificationEmail(_emailVerificationEmail);
        _controller.setRequireEmailValidation(true);

        _user = new User();
        _user.setId(123);
        _user.setEmail("oldEmail@address.org");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setScreenName("screenName");
        _user.setPlaintextPassword("foobar");
    }

    public void testValidate() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");


        expect(_userDao.findUserFromEmailIfExists("email@address.org")).andReturn(null);
        replayMocks(_userDao, _emailVerificationEmail);

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        verifyMocks(_userDao, _emailVerificationEmail);
        assertFalse(errors.hasErrors());
    }

    public void testSetUpdated() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        _user.setUpdated(cal.getTime());
        assertTrue(_user.getUpdated().equals(cal.getTime()));
        SessionContext context = SessionContextUtil.getSessionContext(getRequest());
        context.setUser(_user);

        _userDao.updateUser(_user);
        _emailVerificationEmail.sendChangedEmailAddress(getRequest(), _user);
        replayMocks(_userDao, _emailVerificationEmail);

        getRequest().setParameter("submit", "submit");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verifyMocks(_userDao, _emailVerificationEmail);
        assertNotNull(mAndV);
        assertEquals("email@address.org", _user.getEmail());
        assertFalse(_user.getUpdated().equals(cal.getTime()));
    }

    public void testMismatchedEmails() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("emial@address.org");

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
    }

    public void testOnSubmit() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        SessionContext context =SessionContextUtil.getSessionContext(getRequest());
        context.setUser(_user);

        _userDao.updateUser(_user);

        _emailVerificationEmail.sendChangedEmailAddress(getRequest(), _user);
        replayMocks(_userDao, _emailVerificationEmail);

        getRequest().setParameter("submit", "submit");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verifyMocks(_userDao, _emailVerificationEmail);
        assertNotNull(mAndV);
        assertEquals("email@address.org", _user.getEmail());
    }

    public void testCancel() throws NoSuchAlgorithmException {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        SessionContext context = SessionContextUtil.getSessionContext(getRequest());
        context.setUser(_user);

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertEquals("oldEmail@address.org", _user.getEmail());
    }

    public void testEmailLength() throws NoSuchAlgorithmException {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        String email = "";
        for (int x=0; x < 117; x++) {
            email += (x % 10);
        }
        email += "@address.org";
        command.setNewEmail(email);
        command.setConfirmNewEmail(email);

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
    }

    public void testUserExists() throws NoSuchAlgorithmException {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        String email = "aroy@greatschools.org";
        command.setNewEmail(email);
        command.setConfirmNewEmail(email);

        User user = new User();
        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);
        replayMocks(_userDao, _emailVerificationEmail);

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        verifyMocks(_userDao, _emailVerificationEmail);
        assertTrue(errors.hasErrors());
    }
}
