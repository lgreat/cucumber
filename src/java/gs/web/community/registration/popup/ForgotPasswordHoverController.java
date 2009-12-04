package gs.web.community.registration.popup;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import gs.data.community.User;
import gs.web.community.registration.ForgotPasswordCommand;
import gs.web.community.registration.ForgotPasswordController;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * @author greatschools.org>
 */
public class ForgotPasswordHoverController extends ForgotPasswordController {
    public static final String BEAN_ID = "/community/registration/popup/forgotPasswordHover.page";

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
        ForgotPasswordCommand forgotPasswordHoverCommand = (ForgotPasswordCommand) command;
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
        ForgotPasswordCommand forgotPasswordHoverCommand = (ForgotPasswordCommand) command;
        String redirectUrl = "/community/registration/popup/loginOrRegisterHover.page";
        if (!suppressValidation(request, command)) {
            if (forgotPasswordHoverCommand.isMsl()) {
                redirectUrl = "/community/registration/popup/registrationHover.page?email=" +
                        URLEncoder.encode(forgotPasswordHoverCommand.getEmail(), "UTF-8") + "&msl=1";
            } else {
                sendEmail(forgotPasswordHoverCommand, mAndV, request);
            }
        }
        mAndV.setViewName("redirect:" + redirectUrl);

        return mAndV;
    }
}
