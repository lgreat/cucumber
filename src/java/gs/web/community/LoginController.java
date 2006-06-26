/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginController.java,v 1.2 2006/06/26 23:13:56 apeterson Exp $
 */
package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lets user sign in
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class LoginController extends SimpleFormController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String USER_DOES_NOT_EXIST_ERROR_CODE = "not_a_member";
    public static final String BEAN_ID = "/community/login.page";
    public static final String DEFAULT_REDIRECT_URL = "http://www.greatschools.net";

    private IUserDao _userDao;


    /**
     * this method is called after validation but before submit.
     */
    public void onBindAndValidate(HttpServletRequest request,
                                 Object command,
                                 BindException errors) {
        if (errors.hasErrors()) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());

        if (user == null) {
            errors.reject(USER_DOES_NOT_EXIST_ERROR_CODE, "You're not a member yet.");
        } else {
            if (StringUtils.isEmpty(loginCommand.getRedirect())) {
                loginCommand.setRedirect(LoginController.DEFAULT_REDIRECT_URL);
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) {
        LoginCommand loginCommand = (LoginCommand) command;
        String email = loginCommand.getEmail();
        User user = getUserDao().findUserFromEmail(email);
        PageHelper.setMemberCookie(request, response, user);

        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName("redirect:" + loginCommand.getRedirect());

        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
