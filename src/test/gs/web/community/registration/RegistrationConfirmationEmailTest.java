/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.web.util.UrlBuilder;
import gs.web.util.email.EmailHelperFactory;
import gs.web.util.email.EmailHelper;
import gs.data.community.User;

import javax.mail.MessagingException;
import javax.mail.Message;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides testing for the RegistrationConfirmationEmail bean.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmationEmailTest extends BaseControllerTestCase {
    private static final String FROM_EMAIL = "aroy@greatschools.net";
    private static final String FROM_NAME = "Anthony";
    private static final String SUBJECT = "Testing";
    private RegistrationConfirmationEmail _email;
    private MockJavaMailSender _mailSender;
    private EmailHelperFactory _factory;

    public void setUp() throws Exception {
        super.setUp();
        _email = new RegistrationConfirmationEmail();
        _email.setFromEmail(FROM_EMAIL);
        _email.setFromName(FROM_NAME);
        _email.setSubject(SUBJECT);
        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");
        _factory = new EmailHelperFactory();
        _factory.setMailSender(_mailSender);
        _email.setEmailHelperFactory(_factory);
    }

    public void testSend() throws MessagingException, IOException {
        // verify init
        assertEquals(FROM_EMAIL, _email.getFromEmail());
        assertEquals(SUBJECT, _email.getSubject());
        assertEquals(FROM_NAME, _email.getFromName());
        // setup
        User user = new User();
        user.setEmail("aroy+1@greatschools.net");
        // call
        _email.sendToUser(user, getRequest());
        // verify
        List msgs = _mailSender.getSentMessages();
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        Message msg = (Message) msgs.get(0);
        assertEquals(SUBJECT, msg.getSubject());
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_NAME) > -1);
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_EMAIL) > -1);
        assertNotNull(msg.getContent());
    }

    public void testAddLinkReplacement() {
        // setup
        EmailHelper helper = _factory.getEmailHelper();
        // call
        _email.addLinkReplacement(helper, getRequest(), UrlBuilder.COMMUNITY_LANDING, "KEY", "click here");
        // verify
        Map replacements = helper.getInlineReplacements();
        assertNotNull(replacements);
        assertEquals(1, replacements.size());
        String value = (String) replacements.get("KEY");
        assertNotNull(value);
        assertTrue(value.indexOf(">click here</a>") > -1);
    }
}
