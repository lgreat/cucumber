package gs.web.community.registration.popup;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.community.registration.ForgotPasswordEmail;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordHoverController extends SimpleFormController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/community/registration/popup/forgotPasswordHover.page";

    private IUserDao _userDao;
    private ForgotPasswordEmail _forgotPasswordEmail;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        ForgotPasswordHoverCommand forgotPasswordHoverCommand = (ForgotPasswordHoverCommand) command;

        if (request.getParameter("email") != null) {
            forgotPasswordHoverCommand.setEmail(request.getParameter("email"));
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
                                     BindException errors) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // don't do validation on cancel
        // also don't both checking for a user if the emailValidator rejects the address
        if (suppressValidation(request, command) || errors.hasErrors()) {
            return;
        }
        ForgotPasswordHoverCommand forgotPasswordHoverCommand = (ForgotPasswordHoverCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(forgotPasswordHoverCommand.getEmail());
        boolean isMslSubscriber = false;
        if (user != null) {
            isMslSubscriber =  (user.getFavoriteSchools() != null && !user.getFavoriteSchools().isEmpty());
        }
        if (user == null || user.isEmailProvisional()) {
            // generate error
            String href = "<a href=\"/community/registration/popup/registrationHover.page?email=" +
                    URLEncoder.encode(forgotPasswordHoverCommand.getEmail(), "UTF-8") + "\">join GreatSchools</a>";
            errors.rejectValue("email", null, "There is no account associated with that email address. " +
                    "Would you like to " + href + "?");
            _log.info("Forgot password: user " + forgotPasswordHoverCommand.getEmail() + " is not in database");
        } else if (user.isPasswordEmpty()) {
            // let through
            forgotPasswordHoverCommand.setMsl(true);
        } else if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {
            String errmsg = "The account associated with that email address has been disabled. " +
                    "Please <a href=\"http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/report/email-moderator\">contact us</a> for more information.";
            errors.rejectValue("email", null, errmsg);
            _log.info("Forgot password: disabled community user " + forgotPasswordHoverCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else {
            _log.info("Forgot password: community user " + forgotPasswordHoverCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        ModelAndView mAndV = new ModelAndView();
        ForgotPasswordHoverCommand forgotPasswordHoverCommand = (ForgotPasswordHoverCommand) command;
        String redirectUrl = "/community/registration/popup/loginOrRegisterHover.page";
        if (!suppressValidation(request, command)) {
            if (forgotPasswordHoverCommand.isMsl()) {
                redirectUrl = "/community/registration/popup/registrationHover.page?email=" +
                        URLEncoder.encode(forgotPasswordHoverCommand.getEmail(), "UTF-8") + "&msl=1";
            } else {
                User user = getUserDao().findUserFromEmailIfExists(forgotPasswordHoverCommand.getEmail());
                _forgotPasswordEmail.sendToUser(user, request);
                String msg = "An email has been sent to " + forgotPasswordHoverCommand.getEmail() +
                        " with instructions for selecting a new password.";
                mAndV.getModel().put("message", msg);
            }
        }
        mAndV.setViewName("redirect:" + redirectUrl);

        return mAndV;
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
