/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.util.email.EmailHelperFactory;
import gs.web.util.email.EmailHelper;
import gs.web.util.UrlBuilder;
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
public class RegistrationConfirmationEmail {
    public static final String BEAN_ID = "registrationConfirmationEmail";
    public static final String HTML_EMAIL_LOCATION =
            "/gs/web/community/registration/registrationConfirmationEmail-html.txt";
    public static final String TEXT_EMAIL_LOCATION =
            "/gs/web/community/registration/registrationConfirmationEmail-plainText.txt";

    /* Spring injected properties */
    private EmailHelperFactory _emailHelperFactory;
    private String _subject;
    private String _fromEmail;
    private String _fromName;

    /**
     * Creates and sends an email to the given user welcoming them to the GreatSchools
     * community.
     * @param user User (must have valid email address) to send email to
     * @param request used to instantiate UrlBuilder for links in the email
     * @throws IOException on error reading email template from file
     * @throws MessagingException on error creating message
     * @throws MailException on error sending email
     */
    public void sendToUser(User user, HttpServletRequest request) throws IOException, MessagingException, MailException {
        EmailHelper emailHelper = _emailHelperFactory.getEmailHelper();
        emailHelper.setToEmail(user.getEmail());
        emailHelper.setSubject(_subject);
        emailHelper.setFromEmail(_fromEmail);
        emailHelper.setFromName(_fromName);
        emailHelper.readHtmlFromResource(HTML_EMAIL_LOCATION);
        emailHelper.readPlainTextFromResource(TEXT_EMAIL_LOCATION);

        addLinkReplacement(emailHelper, request, UrlBuilder.COMMUNITY_LANDING, "COMMUNITY_LANDING_PAGE",
                "Get started here");
        addLinkReplacement(emailHelper, request, UrlBuilder.RESEARCH, "TEST_SCORES_PAGE",
                "Begin your search here");
        addLinkReplacement(emailHelper, request, UrlBuilder.HOME, "EXPECT_IN_CLASSROOM",
                "Get started here");

        emailHelper.send();
    }

    /**
     * Helper method to register an href as a replacement.
     * @param emailHelper object to register replacement with
     * @param request for instantiating UrlBuilder
     * @param vpage location to generate href to
     * @param key key to register replacement under
     * @param linkText text of the link (used in UrlBuilder.asAbsoluteAnchor)
     */
    protected void addLinkReplacement(EmailHelper emailHelper, HttpServletRequest request, 
                                      UrlBuilder.VPage vpage, String key, String linkText) {
        UrlBuilder builder = new UrlBuilder(vpage, null);
        emailHelper.addInlineReplacement(key,
                builder.asAbsoluteAnchor(request, linkText).asATag());
    }

    public String getSubject() {
        return _subject;
    }

    public void setSubject(String subject) {
        _subject = subject;
    }

    public String getFromEmail() {
        return _fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        _fromEmail = fromEmail;
    }

    public String getFromName() {
        return _fromName;
    }

    public void setFromName(String fromName) {
        _fromName = fromName;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }
}
