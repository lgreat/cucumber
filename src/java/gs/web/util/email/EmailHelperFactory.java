/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util.email;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailHelperFactory {
    public static final String BEAN_ID = "emailHelperFactory";
    private JavaMailSender _mailSender;

    public EmailHelper getEmailHelper() {
        EmailHelper emailHelper = new EmailHelper();
        emailHelper.setMailSender(_mailSender);
        return emailHelper;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
