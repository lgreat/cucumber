package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.ISessionContext;
import gs.web.util.validator.UserCommandValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * Provides controller backing for the form that lets a user change their password.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ResetPasswordController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/resetPassword.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;

    protected void createGenericValidationError(HttpServletRequest request, BindException errors) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null);
        String href = builder.asAnchor(request, "click here").asATag();
        errors.rejectValue("user", "invalid_hash", "We're sorry, we cannot process your password change " +
                "request. Please make sure you have entered the entire link in the email " +
                "sent to you. To request a new email, please " + href + ".");
    }

    protected boolean suppressValidation(HttpServletRequest request) {
        // don't do validation on a cancel
        return request.getParameter("cancel.x") != null;
    }

    /**
     * This grabs the hash string out of the request, makes sure a user can be obtained from it, and
     * checks that everything seems in order for the user to make a password change.
     */
    protected User validateRequest(HttpServletRequest request, UserCommand command, BindException errors) throws NoSuchAlgorithmException {
        String hash = null;
        User user = null;
        String idString = request.getParameter("id");

        if (!StringUtils.isEmpty(request.getParameter("oldPassword"))) {
            // already authenticated user ... no need for fancy validation, them inputting their
            // existing password will be good enough
            user = SessionContextUtil.getSessionContext(request).getUser();
            // do nothing
        } else if (idString == null) {
            // non-authenticated user, but no string identifying them. Cause error
            ISessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            user = sessionContext.getUser();
            if (user == null) {
                _log.warn("Reset password request with no user specified.");
                errors.reject("unknown_user", "Please log in to use this page.");
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

        if (user.isPasswordEmpty()) {
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
                                   Object command,
                                   BindException errors) throws NoSuchAlgorithmException {

        UserCommand userCommand = (UserCommand) command;

        User user = validateRequest(request, userCommand, errors);
        if (user != null) {
            userCommand.setUser(user);
        }
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (suppressValidation(request)) {
            return;
        }
        UserCommand userCommand = (UserCommand) command;

        // we need to re-validate everything because this form submit may be spoofed
        User user = validateRequest(request, userCommand, errors);
        if (errors.hasErrors()) {
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        if (oldPassword != null) {
            if (!user.matchesPassword(oldPassword)) {
                errors.reject("incorrect_password", "The old password is incorrect");
            }
        }

        UserCommandValidator validator = new UserCommandValidator();
        validator.validatePassword(userCommand, errors);

        userCommand.setUser(user); // this is so onSubmit can just grab the user from the command
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws NoSuchAlgorithmException {
        ModelAndView mAndV = new ModelAndView();
        if (!suppressValidation(request)) {
            // at this point everything has been validated. Proceed with the password change request
            UserCommand userCommand = (UserCommand) command;
            User user = userCommand.getUser();

            user.setPlaintextPassword(userCommand.getPassword());
            getUserDao().updateUser(user);
            // log in user automatically
            PageHelper.setMemberAuthorized(request, response, user);            
            mAndV.getModel().put("message", "Your password has been changed");
        }

        mAndV.setViewName(getSuccessView());
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
