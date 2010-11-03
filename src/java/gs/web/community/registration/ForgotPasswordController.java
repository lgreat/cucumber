package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.MessagingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Provides backing for the forgot your password form.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ForgotPasswordController extends SimpleFormController {
    public static final String BEAN_ID = "/community/forgotPassword.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ForgotPasswordEmail _forgotPasswordEmail;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        ForgotPasswordCommand forgotPasswordCommand = (ForgotPasswordCommand) command;

        if (request.getParameter("email") != null) {
            forgotPasswordCommand.setEmail(request.getParameter("email"));
        }
        if (!StringUtils.isEmpty(request.getHeader("REFERER"))) {
            forgotPasswordCommand.setReferrer(request.getHeader("REFERER"));
        }
    }

    protected boolean suppressValidation(HttpServletRequest request, Object obj) {
        return isCancel(request) || super.suppressValidation(request, obj);
    }

    protected boolean isCancel(HttpServletRequest request) {
        return request.getParameter("cancel.x") != null || request.getParameter("cancel") != null;
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws Exception {
        // don't do validation on cancel
        // also don't both checking for a user if the emailValidator rejects the address
        if (suppressValidation(request, command) || errors.hasErrors()) {
            return;
        }
        ForgotPasswordCommand forgotPasswordCommand = (ForgotPasswordCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(forgotPasswordCommand.getEmail());
        boolean isMslSubscriber = false;
        if (user != null) {
            isMslSubscriber =  (user.getFavoriteSchools() != null && !user.getFavoriteSchools().isEmpty());
        }
        if (ForgotPasswordValidatorHelper.noSuchUser(user)) {
            // generate error
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, forgotPasswordCommand.getEmail());
            String href = builder.asAnchor(request, "join GreatSchools").asATag();
            errors.rejectValue("email", null, "There is no account associated with that email address. " +
                    "Would you like to " + href + "?");
//        } else if (user.isEmailProvisional()) {
//            UrlBuilder builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, user.getEmail());
//            String href2 = builder.asAnchor(request, "(resend email)").asATag();
//            errors.rejectValue("email", "password_empty", "You have chosen a new password, but haven't " +
//                    "validated your email address yet. To validate your email address, follow the " +
//                    "instructions in the email sent to you " + href2 + ".");
            _log.info("Forgot password: user " + forgotPasswordCommand.getEmail() + " is not in database");
        } else if (ForgotPasswordValidatorHelper.userNoPassword(user)) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, forgotPasswordCommand.getEmail());
            String joinLink = builder.asAHref(request, "Join now <span class=\"alertDoubleArrow\">&raquo;</span>");
            errors.rejectValue("email", null, "Hi, " + user.getEmail().split("@")[0] +
                    "! You have an email address on file, " +
                    "but still need to create a free account with GreatSchools. " + joinLink);
            _log.info("Forgot password: non-community user " + forgotPasswordCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else if (ForgotPasswordValidatorHelper.userDeactivated(user)) {
            String errmsg = "The account associated with that email address has been disabled. " +
                    "Please <a href=\"http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/report/email-moderator\">contact us</a> for more information.";
            errors.rejectValue("email", null, errmsg);
            _log.info("Forgot password: disabled community user " + forgotPasswordCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else {
            _log.info("Forgot password: community user " + forgotPasswordCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        ModelAndView mAndV = new ModelAndView();
        ForgotPasswordCommand forgotPasswordCommand = (ForgotPasswordCommand) command;
        if (!suppressValidation(request, command)) {
            sendEmail(forgotPasswordCommand, mAndV, request);
        } else {
            String redirectUrl = forgotPasswordCommand.getReferrer();
            if (StringUtils.isEmpty(redirectUrl) || StringUtils.contains(redirectUrl, "login_iframe")) {
                redirectUrl = "/account/";
            }
            mAndV.setViewName("redirect:" + redirectUrl);
        }

        return mAndV;
    }

    protected void sendEmail(ForgotPasswordCommand forgotPasswordCommand,
                             ModelAndView mAndV,
                             HttpServletRequest request) throws IOException, MessagingException, NoSuchAlgorithmException {
        User user = getUserDao().findUserFromEmailIfExists(forgotPasswordCommand.getEmail());
        _forgotPasswordEmail.sendToUser(user, request);
        mAndV.setViewName(getSuccessView());
        String msg = "An email has been sent to " + forgotPasswordCommand.getEmail() +
                " with instructions for selecting a new password.";
        mAndV.getModel().put("message", msg);
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ForgotPasswordEmail getForgotPasswordEmail() {
        return _forgotPasswordEmail;
    }

    public void setForgotPasswordEmail(ForgotPasswordEmail forgotPasswordEmail) {
        _forgotPasswordEmail = forgotPasswordEmail;
    }
}
