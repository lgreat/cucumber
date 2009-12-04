package gs.web.community.registration.popup;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import gs.data.community.User;
import gs.data.soap.SoapRequestException;
import gs.web.community.registration.LoginCommand;
import gs.web.community.registration.LoginController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;
import gs.web.util.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class LoginHoverController extends LoginController {
    public static final String BEAN_ID = "/community/registration/popup/loginOrRegisterHover.page";

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

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws Exception {
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
            // remember me is supposed to default to true on the login form
            // since we both use the same command, it's common courtesy for me to
            // set it to true when I'm sending it that way
            loginCommand.setRememberMe(true);
        }
    }

    public void validateLoginForm(HttpServletRequest request,
                                  LoginHoverCommand loginCommand,
                                  BindException errors) throws Exception {
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
            String link = "<a href=\"/community/registration/popup/registrationHover.page?email=";
            link += URLEncoder.encode(loginCommand.getEmail(), "UTF-8");
            link += "\">Join now!</a>";
            errors.reject(null,
                    "There is no account associated with that email address. " + link);
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
                                 BindException errors) throws Exception {
        LoginHoverCommand loginCommand = (LoginHoverCommand) command;
        String email = loginCommand.getEmail();
        ModelAndView mAndV = new ModelAndView();

        if (request.getParameter("joinForm") != null) {
            mAndV.setViewName("redirect:/community/registration/popup/registrationHover.page?email=" +
                    URLEncoder.encode(email, "UTF-8") + "&how=" + loginCommand.getHow());
            return mAndV;
        }
        User user = getUserDao().findUserFromEmail(email);

        String redirectUrl;
        if (user.isPasswordEmpty()) {
            // Log the user in to MSL
            PageHelper.setMemberCookie(request, response, user);
            // But they don't have a community password, so send them to the registration page
            redirectUrl = "/community/registration/popup/registrationHover.page?email=" +
                    URLEncoder.encode(email, "UTF-8") + "&msl=1&how=" + loginCommand.getHow();
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
}