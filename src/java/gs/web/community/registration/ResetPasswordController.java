package gs.web.community.registration;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.validator.UserCommandValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides controller backing for the form that lets a user change their password.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ResetPasswordController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/resetPassword.page";
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String ERROR_PASSWORD_MISMATCH = "The password you entered is incorrect.";

    private IUserDao _userDao;
    private AuthenticationManager _authenticationManager;

    protected void createGenericValidationError(HttpServletRequest request, BindException errors) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null);
        String href = builder.asAnchor(request, "click here").asATag();
        errors.rejectValue("newPassword", null, "We're sorry, we cannot process your password change " +
                "request. Please make sure you have entered the entire link in the email " +
                "sent to you. To request a new email, please " + href + ".");
    }

    protected boolean suppressValidation(HttpServletRequest request, Object objCommand) {
        // don't do validation on a cancel
        return request.getParameter("cancel.x") != null;
    }

    /**
     * This grabs the hash string out of the request, makes sure a user can be obtained from it, and
     * checks that everything seems in order for the user to make a password change.
     */
    protected User validateRequest(HttpServletRequest request, ResetPasswordCommand command, BindException errors) throws NoSuchAlgorithmException {
        String hash = null;
        User user = null;
        String idString = request.getParameter("id");

        if (request.getParameter("oldPassword") != null) {
            // already authenticated user ... no need for fancy validation, them inputting their
            // existing password will be good enough
            user = SessionContextUtil.getSessionContext(request).getUser();
            // do nothing
        } else if (idString == null) {
            // non-authenticated user, but no string identifying them. Cause error
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            user = sessionContext.getUser();
            if (user == null) {
                _log.warn("Reset password request with no user specified.");
                errors.rejectValue("oldPassword", null, "Please log in to use this page.");
                return null;
            }
        } else {
            // non-authenticated user. Validate the string identifying them
            try {
                hash = idString.substring(0, DigestUtil.MD5_HASH_LENGTH);

                int id = Integer.parseInt(idString.substring(DigestUtil.MD5_HASH_LENGTH));

                try {
                    user = getUserDao().findUserFromId(id);
                } catch (ObjectRetrievalFailureException orfe) {
                    _log.warn("Reset password request for unknown user id: " + id);
                    createGenericValidationError(request, errors);
                }
            } catch (Exception ex2) {
                _log.warn("Invalid hash string in reset password request: " + request.getParameter("id"));
                createGenericValidationError(request, errors);
            }

            if (user == null) {
                // id is malformed. This has already been logged and an error generated
                return null;
            }
            // now confirm hash
            String actualHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            if (!hash.equals(actualHash)) {
                _log.warn("Reset password request has invalid hash: " + hash + " for user " +
                        user.getEmail());
                createGenericValidationError(request, errors);
                // if hash doesn't validate, do not continue
                return null;
            }
        }

        if (user == null) {
            _log.warn("Reset password request for unknown user");
            createGenericValidationError(request, errors);
        } else if (user.isPasswordEmpty()) {
            _log.warn("Reset password request for user " + user.getEmail() + " who has no password to begin with");
            createGenericValidationError(request, errors);
        } else if (user.isEmailProvisional()) {
            _log.warn("Reset password request for user " + user.getEmail() + " who is provisional");
            createGenericValidationError(request, errors);
        }

        return user;
    }

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object objCommand,
                                   BindException errors) throws NoSuchAlgorithmException {

        ResetPasswordCommand command = (ResetPasswordCommand) objCommand;

        validateRequest(request, command, errors);
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object objCommand,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (suppressValidation(request, objCommand)) {
            return;
        }
        ResetPasswordCommand command = (ResetPasswordCommand) objCommand;

        // we need to re-validate everything because this form submit may be spoofed
        User user = validateRequest(request, command, errors);
        if (errors.hasErrors()) {
            return;
        }

        if (request.getParameter("oldPassword") != null) {
            String oldPassword = command.getOldPassword();
            if (!user.matchesPassword(oldPassword)) {
                errors.rejectValue("oldPassword", null, ERROR_PASSWORD_MISMATCH);
            }
        }

        UserCommandValidator validator = new UserCommandValidator();
        validator.validatePasswordFields(command.getNewPassword(), command.getNewPasswordConfirm(),
                "newPassword", errors);

        command.setUser(user); // this is so onSubmit can just grab the user from the command
        if (errors.hasFieldErrors("newPassword")) {
            command.setNewPassword("");
            command.setNewPasswordConfirm("");
        }

        if (errors.hasFieldErrors("oldPassword")) {
            command.setOldPassword("");
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object objCommand,
                                 BindException errors) throws NoSuchAlgorithmException {
        ModelAndView mAndV = new ModelAndView();
        String targetUrl = "/account/";
        if (!suppressValidation(request, objCommand)) {
            // at this point everything has been validated. Proceed with the password change request
            ResetPasswordCommand command = (ResetPasswordCommand) objCommand;
            User user = command.getUser();

            user.setPlaintextPassword(command.getNewPassword());
            // check for users from the GS-8374 flow
            if (user.getUserProfile() == null) {
                // save user
                user.setUpdated(new Date());
                getUserDao().updateUser(user);
                // don't log these users in or notify community (they aren't registered!)
                // just forward them to our new page
                Map<String, Object> model = new HashMap<String, Object>();
                UserCommand userCommand = new UserCommand();
                userCommand.setEmail(user.getEmail());
                userCommand.setRedirectUrl("/email/management.page");
                model.put("userCmd", userCommand);
                mAndV = new ModelAndView("/community/registration/createUsername", model);
                return mAndV; // early exit!
            }
            // save user
            user.setUpdated(new Date());
            getUserDao().updateUser(user);
            // log in user automatically
            PageHelper.setMemberAuthorized(request, response, user);

            if (StringUtils.isNotBlank(request.getParameter("redirectParam"))) {
                targetUrl = request.getParameter("redirectParam");
            } else {
                targetUrl += "?msg=updatedPassword";
            }
        }
        mAndV.setViewName("redirect:" + targetUrl);

        return mAndV;
    }

    protected Object formBackingObject(HttpServletRequest request) {
        return new ResetPasswordCommand();
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

    public static class ResetPasswordCommand {
        private String _oldPassword;
        private String _newPassword;
        private String _newPasswordConfirm;
        private User _user;

        public String getOldPassword() {
            return _oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            _oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return _newPassword;
        }

        public void setNewPassword(String newPassword) {
            _newPassword = newPassword;
        }

        public String getNewPasswordConfirm() {
            return _newPasswordConfirm;
        }

        public void setNewPasswordConfirm(String newPasswordConfirm) {
            _newPasswordConfirm = newPasswordConfirm;
        }

        public User getUser() {
            return _user;
        }

        public void setUser(User user) {
            _user = user;
        }
    }
}