/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginController.java,v 1.12 2006/11/29 01:36:06 aroy Exp $
 */
package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.context.ISessionContext;
import gs.web.util.context.SessionContextUtil;
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
    public static String DEFAULT_REDIRECT_URL = null;

    private IUserDao _userDao;
    private AuthenticationManager _authenticationManager;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        if (DEFAULT_REDIRECT_URL == null) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null, null);
            DEFAULT_REDIRECT_URL = builder.asFullUrl(request);
        }

        LoginCommand loginCommand = (LoginCommand) command;

        if (StringUtils.isEmpty(loginCommand.getRedirect())) {
            loginCommand.setRedirect(LoginController.DEFAULT_REDIRECT_URL);
        }

        if (null == loginCommand.getEmail()) {
            ISessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            if (StringUtils.isNotEmpty(sessionContext.getEmail())) {
                loginCommand.setEmail(sessionContext.getEmail());
            } else {
                loginCommand.setEmail("");
            }
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
            // get registration form to auto fill in email
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, loginCommand.getEmail());
            if (StringUtils.isNotEmpty(loginCommand.getRedirect())) {
                builder.addParameter("redirect", loginCommand.getRedirect());
            }
            String href = builder.asAnchor(request, "join the community").asATag();
            errors.reject(USER_DOES_NOT_EXIST_ERROR_CODE + "_with_link",
                    "There is no account associated with that email address. Would you like to " +
                            href + "?");
        } else if (user.isEmailProvisional()) {
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());

            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + user.getId());
            String href = builder.asAnchor(request, "reset your account").asATag();
            builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, user.getEmail());
            String href2 = builder.asAnchor(request, "(Resend email)").asATag();
            errors.reject(USER_PROVISIONAL_CODE, "Before signing in, you must validate your account " +
                    "by clicking the link in your registration email. " +
                    href2 + "." +
                    " If you believe this message to be in error, please " + href + ".");
        } else if (user.isPasswordEmpty()) {
            //errors.reject(USER_NO_PASSWORD_CODE, "This user has no password.");
        } else {
            String password = loginCommand.getPassword();
            // validate password
            if ( (StringUtils.isNotEmpty(password) && StringUtils.isEmpty(user.getPasswordMd5())) ||
                    (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(user.getPasswordMd5())) ||
                    (!user.matchesPassword(password)) ) {
                UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null,
                        loginCommand.getEmail());
                String href = builder.asAnchor(request, "forget your password").asATag();
                errors.reject(INVALID_PASSWORD_CODE, "The password you entered is incorrect. Did " +
                        "you " + href + "?");
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws NoSuchAlgorithmException {
        LoginCommand loginCommand = (LoginCommand) command;
        String email = loginCommand.getEmail();
        User user = getUserDao().findUserFromEmail(email);
        PageHelper.setMemberAuthorized(request, response, user);

        ModelAndView mAndV = new ModelAndView();

        if (loginCommand.getRedirect() != null) {
            AuthenticationManager.AuthInfo authInfo = _authenticationManager.generateAuthInfo(user);
            loginCommand.setRedirect(_authenticationManager.addParameterIfNecessary
                    (loginCommand.getRedirect(), authInfo));
        }

        UrlUtil urlUtil = new UrlUtil();
        String redirectUrl;
        if (user.isPasswordEmpty()) {
            // TODO: how to deal with this case and authentication
            // for users who need passwords, send them to the registration page
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null,
                    email, loginCommand.getRedirect());
            redirectUrl = builder.asFullUrl(request);
        } else {
            if (StringUtils.isEmpty(loginCommand.getRedirect())) {
                loginCommand.setRedirect(LoginController.DEFAULT_REDIRECT_URL);
            }
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

    public AuthenticationManager getAuthenticationManager() {
        return _authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        _authenticationManager = authenticationManager;
    }
}
