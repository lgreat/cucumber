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

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmationEmail {
    public static final String BEAN_ID = "registrationConfirmationEmail";

    /* Spring injected properties */
    private EmailHelperFactory _emailHelperFactory;
    private String _subject;
    private String _fromEmail;
    private String _fromName;


    public void sendToUser(User user, HttpServletRequest request) throws IOException, MessagingException {
        EmailHelper emailHelper = _emailHelperFactory.getEmailHelper();
        emailHelper.setToEmail(user.getEmail());
        emailHelper.setSubject(_subject);
        emailHelper.setFromEmail(_fromEmail);
        emailHelper.setFromName(_fromName);
        emailHelper.readHtmlFromResource("/gs/web/community/registration/registrationConfirmationEmail-html.txt");
        emailHelper.readPlainTextFromResource("/gs/web/community/registration/registrationConfirmationEmail-plainText.txt");
        UrlBuilder builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null);
        emailHelper.addInlineReplacement("COMMUNITY_LANDING_PAGE",
                builder.asAbsoluteAnchor(request, "Get started here").asATag());
        builder = new UrlBuilder(UrlBuilder.RESEARCH, null);
        emailHelper.addInlineReplacement("TEST_SCORES_PAGE",
                builder.asAbsoluteAnchor(request, "Begin your search here").asATag());
        builder = new UrlBuilder(UrlBuilder.HOME, null);
        emailHelper.addInlineReplacement("EXPECT_IN_CLASSROOM",
                builder.asAbsoluteAnchor(request, "Get started here").asATag());

        emailHelper.send();
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
