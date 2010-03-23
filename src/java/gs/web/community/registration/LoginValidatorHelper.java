package gs.web.community.registration;

import gs.data.community.User;
import org.apache.commons.lang.StringUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginValidatorHelper {
    public static boolean noSuchUser(User user, boolean requireEmailValidation) {
        return user == null || (user.isEmailProvisional() && !requireEmailValidation);
    }

    public static boolean userNotValidated(User user, boolean requireEmailValidation) {
        return user.isEmailProvisional() && requireEmailValidation;
    }

    public static boolean userNoPassword(User user) {
        return user.isPasswordEmpty();
    }

    public static boolean userDeactivated(User user) {
        return user.getUserProfile() != null && !user.getUserProfile().isActive();
    }

    public static boolean passwordMismatch(User user, String password) throws NoSuchAlgorithmException {
        return (StringUtils.isNotEmpty(password) && StringUtils.isEmpty(user.getPasswordMd5())) ||
                (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(user.getPasswordMd5())) ||
                (!user.matchesPassword(password));
    }
}
