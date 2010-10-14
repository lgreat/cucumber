package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class EmailVerificationLinkValidator implements Validator {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final Long EXPIRE_DURATION_IN_MILLIS = 5L * 24L * 60L * 60L * 1000L; // 5 days in milliseconds

    private IUserDao _userDao;

    public boolean supports(Class aClass) {
        return aClass.equals(EmailVerificationLinkCommand.class);
    }

    public void validate(Object o, Errors errors) {
        EmailVerificationLinkCommand command = (EmailVerificationLinkCommand) o;

        String dateSentAsString = command.getDateSentAsString();

        String hashPlusUserId = command.getHashPlusUserId();
        Integer userId = command.getUserId();

        if (hashPlusUserId == null) {
            _log.warn("Cannot verify email with null hashPlusUserId");
            errors.rejectValue("hashPlusUserId", "hashPlusUserId", "Cannot verify email with null hashPlusUserId");
        } else if (hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH || userId == null) {
            _log.warn("Email verification request with badly formed hashPlusUserId: " + hashPlusUserId +
                    "Expecting hash of length " + DigestUtil.MD5_HASH_LENGTH + " followed by userId.");
            errors.rejectValue("hashPlusUserId", "hashPlusUserId", "Cannot verify email with malformed hashPlusUserId");
        }

        // make sure dateSent is less than X days ago (GS-8865)
        Date now = new Date();
        if (command.getDateSentAsString() == null) {
            errors.rejectValue("dateSentAsString", "nullDate", "dateSentAsString is null");
        } else if (now.getTime() - Long.valueOf(dateSentAsString) > EXPIRE_DURATION_IN_MILLIS) {
            errors.rejectValue("dateSentAsString", "linkExpired", "Email verification link has expired.");
            _log.debug("Email verification link " + hashPlusUserId + " sent at " + dateSentAsString + " is expired.");
        }

        if (!errors.hasFieldErrors("hashPlusUserId") && userId != null) {
            User user;
            try {
                user = getUserDao().findUserFromId(userId);
                command.setUser(user);
                if (!verificationHashMatchesUser(command)) {
                    errors.rejectValue("hashPlusUserId", "verificationLinkUserMismatch", "Verification link hash does not match user");
                }
            } catch (ObjectRetrievalFailureException orfe) {
                _log.warn("Community registration request for unknown user id: " + userId);
                errors.rejectValue("hashPlusUserId", "userNotFound", "User with id" + userId + "not found.");
            }
        }
    }

    public boolean verificationHashMatchesUser(EmailVerificationLinkCommand command) {
        User user = command.getUser();
        String dateSentAsString = command.getDateSentAsString();
        Integer id = command.getUserId();
        String hash = command.getHash();
        boolean validHash = false;
        String actualHash = null;
        try {
            if (id != null) {
                actualHash = DigestUtil.hashStringInt(user.getEmail(), id);
                actualHash = DigestUtil.hashString(actualHash + dateSentAsString);
            }
            validHash = (id != null && hash != null && hash.equals(actualHash));
            if (!validHash) {
                _log.warn("Community registration request has invalid hash: " + hash + " for user " + user.getEmail());
            }
        } catch (NoSuchAlgorithmException e) {
            _log.warn("Failed to hash string", e);
            //Nothing can be done
        } finally {
            return validHash;
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
