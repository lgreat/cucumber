/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.data.util.email.EmailHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.AbstractSendEmailBean;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.springframework.mail.MailException;

/**
 * Bean that encapsulates the registration confirmation email. Because registration can
 * logically be "completed" in multiple places, this code was pulled out to a bean that
 * can be injected into each necessary controller.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmationEmail extends AbstractSendEmailBean {
    public static final String BEAN_ID = "registrationConfirmationEmail";
    public static final String HTML_EMAIL_LOCATION =
            "/gs/web/community/registration/registrationConfirmationEmail-html.txt";
    public static final String TEXT_EMAIL_LOCATION =
            "/gs/web/community/registration/registrationConfirmationEmail-plainText.txt";

    /**
     * Creates and sends an email to the given user welcoming them to the GreatSchools
     * community.
     * @param user User (must have valid email address) to send email to
     * @param request used to instantiate UrlBuilder for links in the email
     * @throws IOException on error reading email template from file
     * @throws MessagingException on error creating message
     * @throws MailException on error sending email
     */
    public void sendToUser(User user, String passwordPlaintext, HttpServletRequest request) throws IOException, MessagingException, MailException {
        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource(HTML_EMAIL_LOCATION);
        emailHelper.readPlainTextFromResource(TEXT_EMAIL_LOCATION);

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        String communityHost = sc.getSessionContextUtil().getCommunityHost(request);
        emailHelper.addInlineReplacement("GET_STARTED", "http://" + communityHost);
        emailHelper.addInlineReplacement("USER_EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("USER_PASSWORD", passwordPlaintext);
        emailHelper.addInlineReplacement("TAKE_TOUR", "http://" + communityHost + "/tour/index.html");
        emailHelper.addInlineReplacement("READ_FAQ", "http://" + communityHost + "/faq");

        emailHelper.send();
    }
}
