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

import static org.easymock.classextension.EasyMock.*;

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
        _soapRequest = createMock(ChangeEmailRequest.class);

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

    public void testNotifyCommunity() throws SoapRequestException {
        _soapRequest.changeEmailRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);

        reset(_soapRequest);

        _soapRequest.changeEmailRequest(_user);
        expectLastCall().andThrow(new SoapRequestException());
        replay(_soapRequest);

        assertFalse(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);

    }

    // verify that the soap request is given a target on staging
    public void testNotifyCommunityOnStaging() throws SoapRequestException {
        _request.setServerName("staging.greatschools.net");

        _soapRequest.setTarget("http://community.staging.greatschools.net/soap/user");
        _soapRequest.changeEmailRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);
    }

    // verify that the soap request is given a target on dev
    public void testNotifyCommunityOnDev() throws SoapRequestException {
        _request.setServerName("dev.greatschools.net");

        _soapRequest.setTarget("http://community.dev.greatschools.net/soap/user");
        _soapRequest.changeEmailRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);
    }

    // verify that the soap request is NOT given a target on developer workstation
    public void testNotifyCommunityOnWorkstation() throws SoapRequestException {
        _request.setServerName("aroy.office.greatschools.net");

        _soapRequest.changeEmailRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);
    }

    // verify that the soap request is NOT given a target on live
    public void testNotifyCommunityOnWww() throws SoapRequestException {
        _request.setServerName("www.greatschools.net");

        _soapRequest.changeEmailRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);
    }

    // verify that the soap request is NOT given a target on live
    public void testNotifyCommunityOnWwwCobrand() throws SoapRequestException {
        _request.setServerName("sfgate.greatschools.net");

        _soapRequest.changeEmailRequest(_user);
        replay(_soapRequest);
        assertTrue(_controller.notifyCommunity(_user, _request));
        verify(_soapRequest);
    }
}
