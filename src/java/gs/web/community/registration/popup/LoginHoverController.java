package gs.web.community.registration.popup;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.soap.SoapRequestException;
import gs.web.community.registration.AuthenticationManager;
import gs.web.community.registration.LoginCommand;
import gs.web.soap.ReportLoginRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginHoverController extends SimpleFormController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/community/registration/popup/loginOrRegisterHover.page";

    private IUserDao _userDao;
    private AuthenticationManager _authenticationManager;
    private ReportLoginRequest _reportLoginRequest;

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
        loginCommand.setRememberMe(true);
    }

    protected void onBind(HttpServletRequest request, Object command) throws Exception {
        super.onBind(request, command);
        // make sure remember me check box is bound prior to validation
        LoginCommand loginCommand = (LoginCommand) command;
        loginCommand.setRememberMe(request.getParameter("loginCmd.rememberMe") != null);
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        boolean isJoinForm = (request.getParameter("joinForm") != null);
        LoginHoverCommand loginCommand = (LoginHoverCommand) command;

        if (isJoinForm) {
            validateJoinForm(loginCommand, errors);
        } else {
            validateLoginForm(request, loginCommand, errors);
        }
    }

    public void validateJoinForm(LoginHoverCommand loginCommand,
                                 BindException errors) {
        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());
        if (user != null && user.isEmailValidated()) {
            errors.rejectValue("email", null, "The email address you entered has already " +
                    "been registered with GreatSchools. Sign in now!");
            loginCommand.setEmail("");
            loginCommand.setJoinError(true);
        }
    }

    public void validateLoginForm(HttpServletRequest request,
                                  LoginHoverCommand loginCommand,
                                  BindException errors) throws NoSuchAlgorithmException{
        EmailValidator emailValidator = new EmailValidator();
        emailValidator.validate(loginCommand, errors);
        if (errors.hasErrors()) {
            return;
        }

        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());
        boolean isMslSubscriber = false;
        if (user != null) {
            isMslSubscriber = (user.getFavoriteSchools() != null && !user.getFavoriteSchools().isEmpty());
        }

        if (user == null || user.isEmailProvisional()) {
            errors.reject(null,
                    "There is no account associated with that email address.");
            _log.info("Community login: user " + loginCommand.getEmail() + " is not in database");
        } else if (user.isPasswordEmpty()) {
            // MSL case, let them through
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
                errors.reject(null, "The password you entered is incorrect.");
            }
            _log.info("Community login: community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws NoSuchAlgorithmException {
        LoginCommand loginCommand = (LoginCommand) command;
        String email = loginCommand.getEmail();
        ModelAndView mAndV = new ModelAndView();

        if (request.getParameter("joinForm") != null) {
            mAndV.setViewName("redirect:/community/registration/popup/registrationHover.page?email=" + email);
            return mAndV;
        }
        User user = getUserDao().findUserFromEmail(email);

        String redirectUrl;
        if (user.isPasswordEmpty()) {
            // Log the user in to MSL
            PageHelper.setMemberCookie(request, response, user);
            // But they don't have a community password, so send them to the registration page
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null,
                    email, loginCommand.getRedirect());
            redirectUrl = "/community/registration/popup/registrationHover.page?email=" + email + "&msl=1";
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

            redirectUrl = "/community/registration/popup/sendToDestination.page";
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
