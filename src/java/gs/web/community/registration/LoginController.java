/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginController.java,v 1.46 2008/12/04 02:05:57 aroy Exp $
 */
package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.soap.SoapRequestException;
import gs.web.soap.ReportLoginRequest;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
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
    private ReportLoginRequest _reportLoginRequest;

    protected void initializeRedirectUrl(HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String communityHost = sessionContext.getSessionContextUtil().getCommunityHost(request);
        DEFAULT_REDIRECT_URL = "http://" + communityHost + "/";
    }

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        LoginCommand loginCommand = (LoginCommand) command;

        if (null == loginCommand.getEmail()) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            if (StringUtils.isNotEmpty(sessionContext.getEmail())) {
                loginCommand.setEmail(sessionContext.getEmail());
            } else {
                loginCommand.setEmail("");
            }
        }
        String url = loginCommand.getRedirect();
        if (UrlUtil.isCommunityContentLink(url)) {

            if (StringUtils.contains(url, "/q-and-a")) {
                request.setAttribute("alertMessageType", "Question");
            } else if (StringUtils.contains(url, "/advice")) {
                request.setAttribute("alertMessageType", "Advice");
            } else if (StringUtils.contains(url, "/groups")) {
                request.setAttribute("alertMessageType", "Group");
            }
        }
        addMSLMessage(url, request);
    }

    protected void addMSLMessage(String redirectUrl, HttpServletRequest request) {
        if (StringUtils.contains(redirectUrl, "mySchoolList.page")) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            String nickname = sessionContext.getNickname();
            if (StringUtils.isNotBlank(nickname)) {
                UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, sessionContext.getEmail());
                builder.addParameter("redirect", redirectUrl);
                String joinLink = builder.asAHref(request, "Join now &gt;");
                request.setAttribute("message", "Hi, " + nickname + "! You've already subscribed to My School List, " +
                        "but still need to create an account with GreatSchools. " + joinLink);
            }
        }
    }

    protected void onBind(HttpServletRequest request, Object command) throws Exception {
        super.onBind(request, command);
        // make sure remember me check box is bound prior to validation
        LoginCommand loginCommand = (LoginCommand) command;
        loginCommand.setRememberMe(request.getParameter("loginCmd.rememberMe") != null);
        addMSLMessage(loginCommand.getRedirect(), request);
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws Exception {
        if (errors.hasErrors()) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());
        boolean isMslSubscriber = false;
        if (user != null) {
            isMslSubscriber = (user.getFavoriteSchools() != null && !user.getFavoriteSchools().isEmpty());
        }

        if (user == null || user.isEmailProvisional()) {
            errors.reject(null,
                    "There is no account associated with that email address.");
//        } else if (user.isEmailProvisional()) {
//            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());

//            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + user.getId());
//            String href = builder.asAnchor(request, "reset your account").asATag();
//            builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, user.getEmail());
//            String href2 = builder.asAnchor(request, "(Resend email)").asATag();
//            errors.reject(USER_PROVISIONAL_CODE, "Before signing in, you must validate your account " +
//                    "by clicking the link in your registration email. " +
//                    href2 + "." +
//                    " If you believe this message to be in error, please " + href + ".");
            _log.info("Community login: user " + loginCommand.getEmail() + " is not in database");
        } else if (user.isPasswordEmpty()) {
//            errors.reject("email", "There is no community account associated with that email address.");
            errors.reject("email");
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, user.getEmail());
            if (StringUtils.isNotBlank(loginCommand.getRedirect())) {
                builder.addParameter("redirect", loginCommand.getRedirect());
            }
            String joinLink = builder.asAHref(request, "Join now &gt;");
            request.setAttribute("message", "Hi, " + user.getEmail().split("@")[0] +
                    "! You've already subscribed to My School List, " +
                    "but still need to create an account with GreatSchools. " + joinLink);
            _log.info("Community login: non-community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {

            String errmsg = "The account associated with that email address has been disabled. " +
                    "Please <a href=\"http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/report/email-moderator\">contact us</a> for more information.";
            errors.reject(null, errmsg);
            _log.info("Community login: disabled community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else {
            String password = loginCommand.getPassword();
            // validate password
            if ( (StringUtils.isNotEmpty(password) && StringUtils.isEmpty(user.getPasswordMd5())) ||
                    (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(user.getPasswordMd5())) ||
                    (!user.matchesPassword(password)) ) {
                errors.reject(INVALID_PASSWORD_CODE, "The password you entered is incorrect.");
            }
            _log.info("Community login: community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        LoginCommand loginCommand = (LoginCommand) command;
        String email = loginCommand.getEmail();
        User user = getUserDao().findUserFromEmail(email);

        ModelAndView mAndV = new ModelAndView();

        if (StringUtils.isEmpty(loginCommand.getRedirect())) {
            if (DEFAULT_REDIRECT_URL == null) {
                initializeRedirectUrl(request);
            }
            loginCommand.setRedirect(DEFAULT_REDIRECT_URL);
        }

        UrlUtil urlUtil = new UrlUtil();
        String redirectUrl;
        if (user.isPasswordEmpty()) {
            // Log the user in to MSL
            PageHelper.setMemberCookie(request, response, user);
            // But they don't have a community password, so send them to the registration page
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null,
                    email, loginCommand.getRedirect());
            redirectUrl = builder.asFullUrl(request);
        } else {
            // The password has validated, so set the cookies and send them onward
            // only notify community on final step
            try {
                notifyCommunity(user, request);
            } catch (SoapRequestException sre) {
                _log.error("SOAP error - " + sre.getErrorCode() + ": " + sre.getErrorMessage());
                // If this fails, let login continue but log it. This is not a fatal error, nor
                // should it be user-facing.
            }
            PageHelper.setMemberAuthorized(request, response, user, loginCommand.isRememberMe());
            redirectUrl = urlUtil.buildUrl(loginCommand.getRedirect(), request);
        }

        mAndV.setViewName("redirect:" + redirectUrl);
        return mAndV;
    }

    protected void notifyCommunity(User user, HttpServletRequest request) throws SoapRequestException {
        ReportLoginRequest soapRequest = getReportLoginRequest();
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            soapRequest.setTarget("http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/soap/user");
        }
        String requestIP = (String)request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        soapRequest.reportLoginRequest(user, requestIP);
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

    public ReportLoginRequest getReportLoginRequest() {
        if (_reportLoginRequest == null) {
            _reportLoginRequest = new ReportLoginRequest();
        }
        return _reportLoginRequest;
    }

    public void setReportLoginRequest(ReportLoginRequest reportLoginRequest) {
        _reportLoginRequest = reportLoginRequest;
    }
}
