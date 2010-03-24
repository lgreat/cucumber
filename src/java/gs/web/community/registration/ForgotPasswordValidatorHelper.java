package gs.web.community.registration;

import gs.data.community.User;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.validator.EmailValidator;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordValidatorHelper {

    public static boolean emailInvalid(String email) {
        return !EmailValidator.getInstance().isValid(email);
    }

    public static boolean noSuchUser(User user) {
        return user == null || user.isEmailProvisional();
    }

    public static boolean userNoPassword(User user) {
        return user.isPasswordEmpty();
    }

    public static boolean userDeactivated(User user) {
        return user.getUserProfile() != null && !user.getUserProfile().isActive();
    }
}
