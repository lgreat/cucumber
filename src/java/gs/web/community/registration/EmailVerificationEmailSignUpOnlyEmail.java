package gs.web.community.registration;

import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.data.util.email.EmailHelper;
import gs.web.util.UrlBuilder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class EmailVerificationEmailSignUpOnlyEmail extends EmailVerificationEmail {

    public static final String BEAN_ID = "emailVerificationEmailSignUpOnlyEmail";
    public static final String EMAIL_LOCATION = "/gs/web/community/registration/emailVerificationEmailSignUpOnlyEmail.txt";

    public void sendVerificationEmail(HttpServletRequest request, User user, String redirect) throws IOException, MessagingException, NoSuchAlgorithmException {
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        Date now = new Date();
        String nowAsString = String.valueOf(now.getTime());
        hash = DigestUtil.hashString(hash + nowAsString);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + user.getId());
        builder.addParameter("date", nowAsString);
        builder.addParameter("redirect", redirect);

        String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();

        sendEmail(getVerificationEmailSubject(), EMAIL_LOCATION, user, verificationLink);
    }

    protected void sendEmail(String subject,
                             String emailLocation,
                             User user,
                             String verificationLink) throws IOException, MessagingException {
        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setSubject(subject);
        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource(emailLocation);

        emailHelper.addInlineReplacement("VERIFICATION_LINK", verificationLink);
        emailHelper.addInlineReplacement("NEW_EMAIL", user.getEmail());

        emailHelper.send();
    }

}
