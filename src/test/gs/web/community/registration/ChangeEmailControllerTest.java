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

/**
 * Provides ...
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
    }

    public void testValidate() throws Exception {
        ChangeEmailController.ChangeEmailCommand command = new ChangeEmailController.ChangeEmailCommand();
        BindException errors = new BindException(command, "emailCmd");

        command.setNewEmail("email@address.org");
        command.setConfirmNewEmail("email@address.org");

        assertFalse(errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertFalse(errors.hasErrors());
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

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertNotNull(mAndV);
        assertEquals("email@address.org", user.getEmail());
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
    }

}
