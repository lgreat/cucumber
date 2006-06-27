/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginControllerTest.java,v 1.2 2006/06/27 23:32:55 dlee Exp $
 */
package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.BaseControllerTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * test class for LoginController
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class LoginControllerTest extends BaseControllerTestCase {
    LoginController _controller;
    IUserDao _userDao;
    User _testUser;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (LoginController) appContext.getBean(LoginController.BEAN_ID);
        _userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);

        _testUser = new User();
        _testUser.setEmail("logincontrollertest@greatschools.net");
        _userDao.saveUser(_testUser);
    }

    protected void tearDown() throws Exception {
        _userDao.removeUser(_testUser.getId());
        super.tearDown();
    }

    //email does not exist in the database
    public void testOnBindAndValidateBad() {
        LoginCommand command = new LoginCommand();
        command.setEmail("logincontrollernouserexists@greatschools.net");
        BindException errors = new BindException(command, "");
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        String errorCode = errors.getGlobalError().getCode();
        _log.debug(errorCode);
        assertEquals(LoginController.USER_DOES_NOT_EXIST_ERROR_CODE, errorCode);
    }

    //should use redirect url from request
    public void testOnSubmit() {
        LoginCommand command = new LoginCommand();
        command.setEmail(_testUser.getEmail());
        command.setRedirect("/some/page/toredict.page");
        BindException errors = new BindException(command, "");
        assertFalse(errors.hasErrors());

        ModelAndView mAndV = new ModelAndView();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertEquals("redirect:" + command.getRedirect(), mAndV.getViewName());
    }

    //should use default redirect url since one is not specified
    public void testOnSubmitNoRedirectSpecified() {
        LoginCommand command = new LoginCommand();
        command.setEmail(_testUser.getEmail());
        BindException errors = new BindException(command, "");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertFalse(errors.hasErrors());

        ModelAndView mAndV = new ModelAndView();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertEquals("redirect:" + LoginController.DEFAULT_REDIRECT_URL, mAndV.getViewName());
    }
}
