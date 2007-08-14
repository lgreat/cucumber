package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * Provides backing for the forgot your password form.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
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
        UserCommand userCommand = (UserCommand) command;

        if (request.getParameter("email") != null) {
            userCommand.setEmail(request.getParameter("email"));
        }
    }

    protected boolean suppressValidation(HttpServletRequest request) {
        return isCancel(request);
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
                                     BindException errors) throws NoSuchAlgorithmException {
        // don't do validation on cancel
        // also don't both checking for a user if the emailValidator rejects the address
        if (suppressValidation(request) || errors.hasErrors()) {
            return;
        }
        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        if (user == null || user.isEmailProvisional()) {
            // generate error
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, userCommand.getEmail());
            String href = builder.asAnchor(request, "join the community").asATag();
            errors.rejectValue("email", null, "There is no account associated with that email address. " +
                    "Would you like to " + href + "?");
//        } else if (user.isEmailProvisional()) {
//            UrlBuilder builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, user.getEmail());
//            String href2 = builder.asAnchor(request, "(resend email)").asATag();
//            errors.rejectValue("email", "password_empty", "You have chosen a new password, but haven't " +
//                    "validated your email address yet. To validate your email address, follow the " +
//                    "instructions in the email sent to you " + href2 + ".");
        } else if (user.isPasswordEmpty()) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, userCommand.getEmail());
            String href = builder.asAnchor(request, "join the community").asATag();
            errors.rejectValue("email", null, "There is no community account associated with that email address. " +
                    "Would you like to " + href + "?");
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        ModelAndView mAndV = new ModelAndView();
        if (!suppressValidation(request)) {
            UserCommand userCommand = (UserCommand) command;
            User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
            _forgotPasswordEmail.sendToUser(user, request);
            mAndV.setViewName(getSuccessView());
            String msg = "An email has been sent to " + userCommand.getEmail() +
                    " with instructions for selecting a new password.";
            mAndV.getModel().put("message", msg);
        } else {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null);
            mAndV.setViewName("redirect:" + builder.asFullUrl(request));
        }

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
