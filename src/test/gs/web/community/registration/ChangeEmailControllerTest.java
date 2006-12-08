/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.easymock.MockControl;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

/**
 * Provides testing for the controller that changes a user's email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailControllerTest extends BaseControllerTestCase {
    private ChangeEmailController _controller;
    private IUserDao _userDao;
    private MockControl _userControl;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ChangeEmailController();

        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();

        _controller.setUserDao(_userDao);

        _controller.setRpcServerUrl
                ("http://aroy.dev.greatschools.net/cgi-bin/xmlrpc/changeEmail.cgi");
        _controller.setTimeOutMs(5000);
    }

    public void testValidate() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists("email@address.org"), null);
        _userControl.replay();

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
    }

    public void testSetUpdated() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        User user = new User();
        user.setEmail("oldEmail@address.org");
        user.setId(new Integer(123));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        user.setUpdated(cal.getTime());
        assertTrue(user.getUpdated().equals(cal.getTime()));
        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setUser(user);

        _userDao.updateUser(user);
        _userControl.replay();

        getRequest().setParameter("submit", "submit");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertNotNull(mAndV);
        assertEquals("email@address.org", user.getEmail());
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
        assertFalse(user.getUpdated().equals(cal.getTime()));
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

        User user = new User();
        user.setEmail("oldEmail@address.org");
        user.setId(new Integer(123));
        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setUser(user);

        _userDao.updateUser(user);
        _userControl.replay();

        getRequest().setParameter("submit", "submit");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertNotNull(mAndV);
        assertEquals("email@address.org", user.getEmail());
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
    }

    public void testCancel() throws NoSuchAlgorithmException {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        User user = new User();
        user.setEmail("oldEmail@address.org");
        user.setId(new Integer(123));
        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setUser(user);

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertEquals("oldEmail@address.org", user.getEmail());
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
        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user);
        _userControl.replay();

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
    }

    public void testNotifyCommunity() {
        User user = new User();
        user.setEmail("aroy@greatschools.net");
        user.setId(new Integer(123));
        
        String email = _controller.notifyCommunity(user);
        assertNotNull(email);
        assertEquals("aroy+1@greatschools.net", email);
    }

    public void testNotifyCommunityWithRpcFault() {
        User user = new User();
        user.setEmail("aroy@greatschools.net");
        user.setId(new Integer(123));

        _controller.setRpcServerUrl(_controller.getRpcServerUrl() + "?test=error");

        String email = _controller.notifyCommunity(user);
        assertNull(email);
    }

    public void testNotifyCommunityWithUnexpectedError() {
        User user = new User();
        user.setEmail("aroy@greatschools.net");
        user.setId(new Integer(123));

        _controller.setRpcServerUrl(_controller.getRpcServerUrl() + "?test=errorUnexpected");

        String email = _controller.notifyCommunity(user);
        assertNull(email);
    }

    public void testNotifyCommunityWithExpectedError() {
        User user = new User();
        user.setEmail("aroy@greatschools.net");
        user.setId(new Integer(123));

        _controller.setRpcServerUrl(_controller.getRpcServerUrl() + "?test=errorNoUser");

        String email = _controller.notifyCommunity(user);
        assertNotNull(email);
        assertEquals("aroy@greatschools.net", email);
    }

    public void testNotifyCommunityWithBlankResponse() {
        User user = new User();
        user.setEmail("aroy@greatschools.net");
        user.setId(new Integer(123));

        _controller.setRpcServerUrl(_controller.getRpcServerUrl() + "?test=blank");

        String email = _controller.notifyCommunity(user);
        assertNull(email);
    }
}
