package gs.web.community.registration;

import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.data.util.email.EmailHelper;
import gs.web.util.AbstractSendEmailBean;
import gs.web.util.UrlBuilder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailVerificationEmail extends AbstractSendEmailBean {
    public static final String BEAN_ID = "emailVerificationEmail";
    public static final String EMAIL_LOCATION =
            "/gs/web/community/registration/emailVerificationEmail.txt";
    public static final String EMAIL_PART_TWO_LOCATION =
            "/gs/web/community/registration/emailVerificationEmail.txt";
    public static final String EMAIL_CHANGED_EMAIL_LOCATION =
            "/gs/web/community/registration/emailVerificationEmail-changedEmail.txt";
    private String _verificationEmailSubject;
    private String _verificationEmailPartTwoSubject;
    private String _verificationEmailChangedEmailSubject;

    public void sendVerificationEmail(HttpServletRequest request, User user, String redirect)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        sendVerificationEmail(request, user, redirect, null);
    }

    public void sendVerificationEmail(HttpServletRequest request, User user, String redirect, Map<String,String> otherParams)
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

        if (otherParams != null) {
            for (String param : otherParams.keySet()) {
                if (!"id".equals(param) && !"date".equals(param) && !"redirect".equals(param)) {
                    builder.addParameter(param, otherParams.get(param));
                }
            }
        }

        String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();

        sendEmail(getVerificationEmailSubject(), EMAIL_LOCATION, user, verificationLink);
//        Map<String, String> emailAttributes = new HashMap<String, String>(1);
//        emailAttributes.put(VERIFICATION_LINK_ATTRIBUTE, verificationLink);
//        _exactTargetAPI.sendTriggeredEmail(VERIFICATION_EMAIL_KEY, user, emailAttributes);
    }

    // rolling back GS-10256 so this method is equivalent to the above
    public void sendSchoolReviewVerificationEmail(HttpServletRequest request, User user, String redirect)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        sendVerificationEmail(request, user, redirect);
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

        sendEmail(getVerificationEmailChangedEmailSubject(), EMAIL_CHANGED_EMAIL_LOCATION, user, verificationLink);
    }

    protected void sendEmail(String subject,
                             String emailLocation,
                             User user,
                             String verificationLink) throws IOException, MessagingException {
        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setSubject(subject);
        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource(emailLocation);

        if (user.getFirstName() != null) {
            emailHelper.addInlineReplacement("GREETING", "Hi " + user.getFirstName());
        } else {
            emailHelper.addInlineReplacement("GREETING", "Hi");
        }
        emailHelper.addInlineReplacement("VERIFICATION_LINK", verificationLink);
        emailHelper.addInlineReplacement("NEW_EMAIL", user.getEmail());

        emailHelper.send();
    }

    public String getVerificationEmailSubject() {
        return _verificationEmailSubject;
    }

    public void setVerificationEmailSubject(String verificationEmailSubject) {
        _verificationEmailSubject = verificationEmailSubject;
    }

    public String getVerificationEmailPartTwoSubject() {
        return _verificationEmailPartTwoSubject;
    }

    public void setVerificationEmailPartTwoSubject(String verificationEmailPartTwoSubject) {
        _verificationEmailPartTwoSubject = verificationEmailPartTwoSubject;
    }

    public String getVerificationEmailChangedEmailSubject() {
        return _verificationEmailChangedEmailSubject;
    }

    public void setVerificationEmailChangedEmailSubject(String verificationEmailChangedEmailSubject) {
        _verificationEmailChangedEmailSubject = verificationEmailChangedEmailSubject;
    }
}
