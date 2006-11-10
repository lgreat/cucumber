/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.web.util.email.EmailHelperFactory;
import gs.data.community.User;

import javax.mail.MessagingException;
import javax.mail.Message;
import java.io.IOException;
import java.util.List;

/**
 * Provides testing for the RegistrationConfirmationEmail bean.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmationEmailTest extends BaseControllerTestCase {
    private static final String FROM_EMAIL = "aroy@greatschools.net";
    private static final String SUBJECT = "Testing";
    private RegistrationConfirmationEmail _email;
    private MockJavaMailSender _mailSender;

    public void setUp() throws Exception {
        super.setUp();
        _email = new RegistrationConfirmationEmail();
        _email.setFromEmail(FROM_EMAIL);
        _email.setSubject(SUBJECT);
        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");
        EmailHelperFactory factory = new EmailHelperFactory();
        factory.setMailSender(_mailSender);
        _email.setEmailHelperFactory(factory);
    }

    public void testSend() throws MessagingException, IOException {
        User user = new User();
        user.setEmail("aroy+1@greatschools.net");
        _email.sendToUser(user, getRequest());
        List msgs = _mailSender.getSentMessages();
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        Message msg = (Message) msgs.get(0);
        assertEquals(SUBJECT, msg.getSubject());
        assertEquals(FROM_EMAIL, msg.getFrom()[0].toString());
        assertNotNull(msg.getContent());

    }
}
