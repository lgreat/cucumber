/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.soap.SoapRequestException;
import gs.web.soap.ChangeEmailRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;
import gs.data.community.UserProfile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import static org.easymock.EasyMock.*;

/**
 * Provides testing for the controller that changes a user's email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailControllerTest extends BaseControllerTestCase {
    private ChangeEmailController _controller;
    private IUserDao _userDao;
    private User _user;
    private ChangeEmailRequest _soapRequest;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ChangeEmailController();

        _userDao = createMock(IUserDao.class);
        _soapRequest = new ChangeEmailRequest() {
            public void changeEmailRequest(User user) {}
        };

        _controller.setUserDao(_userDao);
        _controller.setSoapRequest(_soapRequest);

        _user = new User();
        _user.setId(123);
        _user.setEmail("oldEmail@address.org");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setScreenName("screenName");
    }

    public void testValidate() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");


        expect(_userDao.findUserFromEmailIfExists("email@address.org")).andReturn(null);
        replay(_userDao);

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
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
        replay(_userDao);

        getRequest().setParameter("submit", "submit");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertNotNull(mAndV);
        assertEquals("email@address.org", _user.getEmail());
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
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

    public void testOnSubmit() throws NoSuchAlgorithmException {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        SessionContext context =SessionContextUtil.getSessionContext(getRequest());
        context.setUser(_user);

        _userDao.updateUser(_user);
        replay(_userDao);

        getRequest().setParameter("submit", "submit");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertNotNull(mAndV);
        assertEquals("email@address.org", _user.getEmail());
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
    }

    public void testCancel() throws NoSuchAlgorithmException {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        SessionContext context = SessionContextUtil.getSessionContext(getRequest());
        context.setUser(_user);

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertEquals("oldEmail@address.org", _user.getEmail());
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
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

        String email = "aroy@greatschools.net";
        command.setNewEmail(email);
        command.setConfirmNewEmail(email);

        User user = new User();
        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);
        replay(_userDao);

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertTrue(errors.hasErrors());
    }

    public void testNotifyCommunity() {
        assertTrue(_controller.notifyCommunity(_user));

        _soapRequest = new ChangeEmailRequest() {
            public void changeEmailRequest(User user) throws SoapRequestException {
                throw new SoapRequestException();
            }
        };

        _controller.setSoapRequest(_soapRequest);

        assertFalse(_controller.notifyCommunity(_user));
    }
}
