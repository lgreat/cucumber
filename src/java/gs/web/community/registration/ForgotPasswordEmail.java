/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.util.AbstractSendEmailBean;
import gs.web.util.UrlBuilder;
import gs.data.community.User;
import gs.data.util.email.EmailHelper;
import gs.data.util.DigestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.mail.MailException;

/**
 * Represents the forgot your password email.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordEmail extends AbstractSendEmailBean {
    public static final String BEAN_ID = "forgotPasswordEmail";

    /**
     * Creates and sends an email to the given user with a link to where they can choose a new password
     * community.
     * @param user User (must have valid email address) to send email to
     * @param request used to instantiate UrlBuilder for links in the email
     * @throws java.io.IOException on error reading email template from file
     * @throws javax.mail.MessagingException on error creating message
     * @throws org.springframework.mail.MailException on error sending email
     */
    public void sendToUser(User user, HttpServletRequest request) throws IOException, MessagingException, MailException, NoSuchAlgorithmException {
        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setToEmail(user.getEmail());

        emailHelper.setHtmlBody(getEmailHTML(request, user.getEmail(), user.getId()));
        emailHelper.setTextBody(getEmailPlainText(request, user.getEmail(), user.getId()));

        emailHelper.send();
    }

    protected String getEmailPlainText(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        String hash = DigestUtil.hashStringInt(email, userId);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userId);
        emailContent.append("Dear GreatSchools member,\n\n");
        emailContent.append("You requested that we reset your password on GreatSchools. ");
        emailContent.append("Please click on the following link to select a new password: ");
        emailContent.append(builder.asFullUrl(request)).append("\n\n");
        emailContent.append("Thanks!\n\nThe GreatSchools Team\n");
        return emailContent.toString();
    }

    protected String getEmailHTML(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("<html>");
        emailContent.append("<head><style>p {\n" +
                "color:#444444; \n" +
                "font: 13px/16px Trebuchet MS, Helvetica, Arial, Verdana, sans-serif;\n" +
                "}\n" +
                "\n" +
                "a {\n" +
                "color: #3399aa;\n" +
                "text-decoration: underline;\n" +
                "}\n" +
                "</style></head>\n");
        emailContent.append("<body>\n");
        String hash = DigestUtil.hashStringInt(email, userId);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userId);
        emailContent.append("<p>Dear GreatSchools member,</p>\n\n");
        emailContent.append("<p>You requested that we reset your password on GreatSchools. ");
        emailContent.append("Please ");
        emailContent.append(builder.asAbsoluteAnchor(request, "click here to select a new password").asATag());
        emailContent.append(".</p>\n\n");
        emailContent.append("<p>Thanks!</p>\n<p>The GreatSchools Team</p>\n");
        emailContent.append("</body></html>");
        return emailContent.toString();
    }

}
