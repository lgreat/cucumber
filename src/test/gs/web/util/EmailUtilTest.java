/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import gs.web.BaseTestCase;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailUtilTest extends BaseTestCase {
    private MockJavaMailSender _mailSender;

    public void setUp() throws Exception {
        super.setUp();
        _mailSender = new MockJavaMailSender();
    }

    public void testPerformReplacements() {
        String text;
        Map replacements = new HashMap();
        String newText;

        // test single replacements
        // value at end
        text = "My name is $NAME$";
        replacements.put("NAME", "Anthony");
        newText = EmailUtil.performReplacements(text, replacements);
        assertEquals("My name is Anthony", newText);
        // value at beginning
        text = "$NAME$ is my name";
        newText = EmailUtil.performReplacements(text, replacements);
        assertEquals("Anthony is my name", newText);

        // test multiple replacements
        text = "I think $NAME$ is $ADJECTIVE$.\n\n-$NAME$";
        replacements.put("ADJECTIVE", "smart");
        newText = EmailUtil.performReplacements(text, replacements);
        assertEquals("I think Anthony is smart.\n\n-Anthony", newText);
    }

    public void testGetTextFromResource() {

        String text = EmailUtil.getTextFromResource(new ClassPathResource("/gs/web/util/EmailUtil.class"));
        assertNotNull(text);

        text = EmailUtil.getTextFromResource(new ClassPathResource("/gs/web/util/EmailUtilxyz.class"));
        assertNull(text);
    }

    public void testCreateSinglePartMessageWithBody() throws MessagingException, IOException {
        EmailUtil.SinglePartEmailOptions options = new EmailUtil.SinglePartEmailOptions();
        options.setFromEmail("abc");
        options.setToEmail("abc");
        options.setSubject("abc");
        options.setBody("abc");

        MimeMessage mm = EmailUtil.createMessage(_mailSender.createMimeMessage(), options);
        assertEquals("abc", mm.getSubject());
        assertEquals("abc", mm.getFrom()[0].toString());
        assertEquals("abc", (String) mm.getContent());

    }

    public void testRequiresTo() throws MessagingException {
        EmailUtil.SinglePartEmailOptions options = new EmailUtil.SinglePartEmailOptions();
        options.setFromEmail("abc");
        options.setSubject("abc");
        options.setBody("abc");

        assertNull(options.getToEmail());
        try {
            EmailUtil.createMessage(_mailSender.createMimeMessage(), options);
            fail("To address should be required");
        } catch (IllegalArgumentException iae) {
        }
    }

    public void testRequiresFrom() throws MessagingException {
        EmailUtil.SinglePartEmailOptions options = new EmailUtil.SinglePartEmailOptions();
        options.setToEmail("abc");
        options.setSubject("abc");
        options.setBody("abc");

        assertNull(options.getFromEmail());
        try {
            EmailUtil.createMessage(_mailSender.createMimeMessage(), options);
            fail("From address should be required");
        } catch (IllegalArgumentException iae) {
        }
    }

    public void testRequiresSubject() throws MessagingException {
        EmailUtil.SinglePartEmailOptions options = new EmailUtil.SinglePartEmailOptions();
        options.setFromEmail("abc");
        options.setToEmail("abc");
        options.setBody("abc");

        assertNull(options.getSubject());
        try {
            EmailUtil.createMessage(_mailSender.createMimeMessage(), options);
            fail("Subject should be required");
        } catch (IllegalArgumentException iae) {
        }
    }

    public void testRequiresBody() throws MessagingException {
        EmailUtil.SinglePartEmailOptions options = new EmailUtil.SinglePartEmailOptions();
        options.setFromEmail("abc");
        options.setToEmail("abc");
        options.setSubject("abc");

        assertNull(options.getBody());
        try {
            EmailUtil.createMessage(_mailSender.createMimeMessage(), options);
            fail("Body should be required");
        } catch (IllegalArgumentException iae) {
        }
    }
}
