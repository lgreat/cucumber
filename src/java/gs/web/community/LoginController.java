/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginController.java,v 1.5 2006/07/13 20:02:38 apeterson Exp $
 */
package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lets user sign in.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class LoginController extends SimpleFormController {

    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/community/login.page";

    public static final String REDIRECT_TO_REFERER_VALUE = "referer";

    public static final String USER_DOES_NOT_EXIST_ERROR_CODE = "not_a_member";
    public static final String DEFAULT_REDIRECT_URL = "";

    private IUserDao _userDao;


    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        LoginCommand loginCommand = (LoginCommand) command;

        // Automatic redirecting to the referer is posible with special value
        if (StringUtils.equals(loginCommand.getRedirect(), REDIRECT_TO_REFERER_VALUE)) {
            // Clear it out, and then set it if possible.
            loginCommand.setRedirect("");
            // Some user agents don't send a referer.
            if (StringUtils.isNotBlank(request.getHeader("Referer"))) {
                loginCommand.setRedirect(request.getHeader("Referer"));
            }
        }

        // If nothing specified, advance to go, do not collect $200.
        if (StringUtils.isEmpty(loginCommand.getRedirect())) {
            loginCommand.setRedirect("http://" + request.getServerName() + LoginController.DEFAULT_REDIRECT_URL);
        }

        if (null == loginCommand.getEmail()) {
            loginCommand.setEmail("");
        }
    }

    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) {
        if (errors.hasErrors()) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());

        if (user == null) {
            errors.reject(USER_DOES_NOT_EXIST_ERROR_CODE, "You're not a member yet.");
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
        UrlUtil urlUtil = new UrlUtil();
        String redirectUrl = urlUtil.buildUrl(loginCommand.getRedirect(), request);
        _log.debug(redirectUrl);
        mAndV.setViewName("redirect:" + redirectUrl);

        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
