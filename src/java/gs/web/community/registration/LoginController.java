/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginController.java,v 1.2 2006/08/10 23:18:27 aroy Exp $
 */
package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * Lets user sign in.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginController extends SimpleFormController {

    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/community/loginOrRegister.page";

    public static final String USER_DOES_NOT_EXIST_ERROR_CODE = "not_a_member";
    public static final String INVALID_PASSWORD_CODE = "invalid_password";
    public static final String NOT_MATCHING_PASSWORDS_CODE = "not_matching_passwords";
    public static final String USER_PROVISIONAL_CODE = "provisional";
    public static final String USER_NO_PASSWORD_CODE = "user_no_password";
    public static final String DEFAULT_REDIRECT_URL = "http://www.greatschools.net";

    private IUserDao _userDao;


    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        LoginCommand loginCommand = (LoginCommand) command;

        if (StringUtils.isEmpty(loginCommand.getRedirect())) {
            loginCommand.setRedirect(LoginController.DEFAULT_REDIRECT_URL);
        }

        if (null == loginCommand.getEmail()) {
            loginCommand.setEmail("");
        }
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (errors.hasErrors()) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());

        if (user == null) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
            // get registration form to auto fill in email
            builder.addParameter("email", loginCommand.getEmail());
            String href = builder.asAnchor(request, "Register here.").asATag();
            errors.reject(USER_DOES_NOT_EXIST_ERROR_CODE + "_with_link", "You're not a member yet. " + href);
        } else if (user.isEmailProvisional()) {
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());

            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + user.getId());
            String href = builder.asAnchor(request, "click here").asATag();
            errors.reject(USER_PROVISIONAL_CODE, "Your account is marked as provisional. " +
                    "Please follow the link in your email to validate your account." +
                    " If you believe this message to be in error, please " + href +
                    " to reset your account.");
        } else if (user.isPasswordEmpty()) {
            //errors.reject(USER_NO_PASSWORD_CODE, "This user has no password.");
        } else {
            String password = loginCommand.getPassword();
            // validate password
            if ( (StringUtils.isNotEmpty(password) && StringUtils.isEmpty(user.getPasswordMd5())) ||
                    (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(user.getPasswordMd5())) ||
                    (!user.matchesPassword(password)) ) {
                    errors.reject(INVALID_PASSWORD_CODE, "Your password is incorrect.");
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
        UrlUtil urlUtil = new UrlUtil();
        String redirectUrl;
        if (user.isPasswordEmpty()) {
            // for users who need passwords, send them to the registration page
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
            builder.addParameter("email", email);
            builder.addParameter("redirect", loginCommand.getRedirect());
            redirectUrl = builder.asFullUrl(request);
        } else {
            redirectUrl = urlUtil.buildUrl(loginCommand.getRedirect(), request);
        }

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
