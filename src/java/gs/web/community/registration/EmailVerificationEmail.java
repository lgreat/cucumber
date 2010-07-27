package gs.web.community.registration;

import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailVerificationEmail {
    public static final String BEAN_ID = "emailVerificationEmail";
    public static final String VERIFICATION_EMAIL_KEY = "Acct_Verification";
    public static final String SCHOOL_REVIEW_VERIFICATION_EMAIL_KEY = "Acct_Verification_SR";
    public static final String CHANGED_VERIFICATION_EMAIL_KEY = "Acct_Email_Change";
    public static final String VERIFICATION_LINK_ATTRIBUTE = "HTML__verifyLink";
    private ExactTargetAPI _exactTargetAPI;

    public void sendVerificationEmail(HttpServletRequest request, User user, String redirect)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        Date now = new Date();
        String nowAsString = String.valueOf(now.getTime());
        hash = DigestUtil.hashString(hash + nowAsString);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION,
                                            null,
                                            hash + user.getId());
        builder.addParameter("date", nowAsString);
        builder.addParameter("redirect", redirect);

        String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();

//        sendEmail(getVerificationEmailSubject(), EMAIL_LOCATION, user, verificationLink);
        Map<String, String> emailAttributes = new HashMap<String, String>(1);
        emailAttributes.put(VERIFICATION_LINK_ATTRIBUTE, verificationLink);
        _exactTargetAPI.sendTriggeredEmail(VERIFICATION_EMAIL_KEY, user, emailAttributes);
    }

    public void sendSchoolReviewVerificationEmail(HttpServletRequest request, User user, String redirect)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        Date now = new Date();
        String nowAsString = String.valueOf(now.getTime());
        hash = DigestUtil.hashString(hash + nowAsString);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION,
                                            null,
                                            hash + user.getId());
        builder.addParameter("date", nowAsString);
        builder.addParameter("redirect", redirect);

        String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();

//        sendEmail(getVerificationEmailSubject(), EMAIL_LOCATION, user, verificationLink);
        Map<String, String> emailAttributes = new HashMap<String, String>(1);
        emailAttributes.put(VERIFICATION_LINK_ATTRIBUTE, verificationLink);
        _exactTargetAPI.sendTriggeredEmail(SCHOOL_REVIEW_VERIFICATION_EMAIL_KEY, user, emailAttributes);
    }

    public void sendChangedEmailAddress(HttpServletRequest request, User user) throws IOException, MessagingException, NoSuchAlgorithmException {
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        Date now = new Date();
        String nowAsString = String.valueOf(now.getTime());
        hash = DigestUtil.hashString(hash + nowAsString);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION,
                                            null,
                                            hash + user.getId());
        builder.addParameter("date", nowAsString);
        builder.addParameter("edit", "1");

        String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();

//        sendEmail(getVerificationEmailChangedEmailSubject(), EMAIL_CHANGED_EMAIL_LOCATION, user, verificationLink);
        Map<String, String> emailAttributes = new HashMap<String, String>(1);
        emailAttributes.put(VERIFICATION_LINK_ATTRIBUTE, verificationLink);
        _exactTargetAPI.sendTriggeredEmail(CHANGED_VERIFICATION_EMAIL_KEY, user, emailAttributes);
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }
}
