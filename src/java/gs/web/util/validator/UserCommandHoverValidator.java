package gs.web.util.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import gs.data.community.User;
import gs.web.community.registration.UserCommand;
import gs.web.community.registration.popup.LoginHoverController;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Validate registration hover page /community/registration/popup/registrationHover.page
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class UserCommandHoverValidator extends UserCommandValidator implements IRequestAwareValidator {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "userValidator";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        User user = validateEmail(command, request, errors);
        if (user != null && errors.hasFieldErrors("email")) {
            // User exists but email failed validation?
            // This can only happen if they are already registered or are disabled
            return; // other errors are irrelevant
        }

        if (user != null && user.isFacebookUser()) {
            errors.rejectValue("email", null, ERROR_FACEBOOK_USER);
            return;
        }

        validateUsername(command, user, errors);
        validateTerms(command, errors);
        validatePassword(command, errors);
        validateStateCity(command, errors);
    }

    public User validateEmail(UserCommand command, HttpServletRequest request, Errors errors) {
        User user = null;
        String email = command.getEmail();
        if (StringUtils.isEmpty(email)) {
            errors.rejectValue("email", null, ERROR_EMAIL_MISSING);
            _log.info("Registration error: " + ERROR_EMAIL_MISSING);
        } else if (email.length() > EMAIL_MAXIMUM_LENGTH) {
            errors.rejectValue("email", null, ERROR_EMAIL_LENGTH);
            _log.info("Registration error: " + ERROR_EMAIL_LENGTH);
        } else {
            user = getUserDao().findUserFromEmailIfExists(email);

            if (user != null) {
                if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {
                    String errmsg = "The account associated with that email address has been disabled. " +
                            "Please <a href=\"http://" +
                            SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                            "/report/email-moderator\">contact us</a> for more information.";
                    errors.rejectValue("email", null, errmsg);
                    _log.info("Registration error: " + errmsg);
                } else if (user.isEmailValidated()) {
                    String loginUrl = LoginHoverController.BEAN_ID + "?email=" + email;
                    String errmsg = ERROR_EMAIL_TAKEN + " <a href=\"" + loginUrl + "\">&nbsp;Sign in&nbsp;&gt;</a>";
                    errors.rejectValue("email", null, errmsg);
                }
            }
        }
        return user;
    }
}
