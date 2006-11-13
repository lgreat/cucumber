/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util.email;

import gs.web.BaseTestCase;
import gs.web.util.MockJavaMailSender;

import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.io.IOException;

import org.springframework.mail.MailSendException;

/**
 * Provides testing for the EmailHelper class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailHelperTest extends BaseTestCase {
    private MockJavaMailSender _mailSender;
    private EmailHelper _emailHelper;

    public void setUp() throws Exception {
        super.setUp();
        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");
        _emailHelper = new EmailHelper();
        _emailHelper.setMailSender(_mailSender);
    }

    public void testIsValid() {
        assertFalse(_emailHelper.isValid());
        _emailHelper.setToEmail("aroy@greatschools.net");
        assertFalse(_emailHelper.isValid());
        _emailHelper.setFromEmail("aroy@greatschools.net");
        assertFalse(_emailHelper.isValid());
        _emailHelper.setSubject("Testing EmailHelper");
        assertFalse(_emailHelper.isValid());
        _emailHelper.setTextBody("This is a test of the EmailHelper");
        assertTrue(_emailHelper.isValid());
    }

    public void testPerformReplacements() {
        String text;
        Map replacements = new HashMap();
        String newText;

        // test single replacements
        // value at end
        text = "My name is $NAME";
        replacements.put("NAME", "Anthony");
        newText = _emailHelper.performReplacements(text, replacements);
        assertEquals("My name is Anthony", newText);
        // value at beginning
        text = "$NAME is my name";
        newText = _emailHelper.performReplacements(text, replacements);
        assertEquals("Anthony is my name", newText);

        // test multiple replacements
        text = "I think $NAME is $ADJECTIVE.\n\n-$NAME";
        replacements.put("ADJECTIVE", "smart");
        newText = _emailHelper.performReplacements(text, replacements);
        assertEquals("I think Anthony is smart.\n\n-Anthony", newText);
    }

    public void testReadStringFromResource() throws IOException {
        String text = _emailHelper.readStringFromResource("/gs/web/util/email/EmailHelper.class");
        assertNotNull(text);
        try {
            _emailHelper.readStringFromResource("/gs/web/util/email/EmailHelperxyz.class");
            fail("Expected FileNotFoundException not thrown");
        } catch (IOException e) {
            // good
        }

        text = _emailHelper.readStringFromResource(null);
        assertNull(text);
    }
    
    public void testReadPlainTextStringFromResource() throws IOException {
        assertNull(_emailHelper.getTextBody());
        assertNull(_emailHelper.getHtmlBody());
        _emailHelper.readPlainTextFromResource("/gs/web/util/email/EmailHelper.class");
        assertNotNull(_emailHelper.getTextBody());
        assertNull(_emailHelper.getHtmlBody());
    }

    public void testReadHtmlStringFromResource() throws IOException {
        assertNull(_emailHelper.getTextBody());
        assertNull(_emailHelper.getHtmlBody());
        _emailHelper.readHtmlFromResource("/gs/web/util/email/EmailHelper.class");
        assertNull(_emailHelper.getTextBody());
        assertNotNull(_emailHelper.getHtmlBody());
    }

    public void testResourceCache() throws IOException {
        EmailHelper._resourceCache.put("/gs/web/util/email/EmailHelperxyz.class", "text");
        try {
            String text = _emailHelper.readStringFromResource("/gs/web/util/email/EmailHelperxyz.class");
            assertEquals("text", text);
        } catch (IOException ioe) {
            fail("Reading from cache should avoid I/O");
        }
        EmailHelper.clearResourceCache();

        try {
            _emailHelper.readStringFromResource("/gs/web/util/email/EmailHelperxyz.class");
            fail("This call should have attempted I/O and failed");
        } catch (IOException ioe) {
            // okay
        }
    }

    public void testCreateMimeMessage() throws MessagingException, IOException {
        _emailHelper.setToEmail("aroy@greatschools.net");
        _emailHelper.setFromEmail("aroy+1@greatschools.net");
        _emailHelper.setSubject("Testing EmailHelper");
        _emailHelper.setTextBody("This is a $TEST of the EmailHelper");
        Map replacements = new HashMap();
        replacements.put("TEST", "test");
        _emailHelper.setInlineReplacements(replacements);

        MimeMessage mm = _emailHelper.createMimeMessage();
        assertEquals("Testing EmailHelper", mm.getSubject());
        assertEquals("aroy+1@greatschools.net", mm.getFrom()[0].toString());
        assertEquals("This is a test of the EmailHelper", (String) mm.getContent());
    }

    public void testCreateMimeMessageHtml() throws MessagingException, IOException {
        _emailHelper.setToEmail("aroy@greatschools.net");
        _emailHelper.setFromEmail("aroy+1@greatschools.net");
        _emailHelper.setSubject("Testing EmailHelper");
        _emailHelper.setHtmlBody("This is a $TEST of the EmailHelper");
        Map replacements = new HashMap();
        replacements.put("TEST", "test");
        _emailHelper.setInlineReplacements(replacements);

        MimeMessage mm = _emailHelper.createMimeMessage();
        assertEquals("Testing EmailHelper", mm.getSubject());
        assertEquals("aroy+1@greatschools.net", mm.getFrom()[0].toString());
        assertEquals("This is a test of the EmailHelper", (String) mm.getContent());
    }

    public void testSend() throws MessagingException, IOException {
        _emailHelper.setToEmail("aroy@greatschools.net");
        _emailHelper.setFromEmail("aroy+1@greatschools.net");
        _emailHelper.setSubject("Testing EmailHelper");
        _emailHelper.setTextBody("This is a test of the EmailHelper");

        _emailHelper.send();

        List msgs = _mailSender.getSentMessages();
        Message msg = (Message) msgs.get(0);
        assertEquals("Testing EmailHelper", msg.getSubject());
        assertEquals("aroy+1@greatschools.net", msg.getFrom()[0].toString());
        assertEquals("This is a test of the EmailHelper", (String) msg.getContent());
    }

    public void testThrowErrorOnNotValid() throws MessagingException {
        assertFalse(_emailHelper.isValid());
        try {
            _emailHelper.send();
            fail("Expected IllegalStateException not thrown");
        } catch (IllegalStateException ise) {
            // okay
        }
    }

    public void testSendThrowsMessagingException() throws MessagingException {
        _emailHelper.setToEmail("aroy@greatschools.net");
        _emailHelper.setFromEmail("aroy+1@greatschools.net");
        _emailHelper.setSubject("Testing EmailHelper");
        _emailHelper.setTextBody("This is a test of the EmailHelper");

        _mailSender.setThrowOnSendMessage(true);
        try {
            _emailHelper.send();
            fail("Expected MessagingException not thrown on send");
        } catch (MailSendException mse) {
            // okay
        }
        _mailSender.setThrowOnSendMessage(false);
    }

    public void testSetters() throws MessagingException, IOException {
        _emailHelper.setToEmail("aroy@greatschools.net");
        _emailHelper.setFromEmail("aroy@greatschools.net");
        _emailHelper.setFromName("Anthony");
        _emailHelper.setSubject("Testing EmailHelper");
        _emailHelper.setTextBody("This is a test of the EmailHelper");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date sentTime = cal.getTime();
        _emailHelper.setSentDate(sentTime);
        _emailHelper.setEncoding("RoyEncoding");

        _emailHelper.send();

        assertEquals("Anthony", _emailHelper.getFromName());
        assertEquals("aroy@greatschools.net", _emailHelper.getFromEmail());
        assertEquals("Testing EmailHelper", _emailHelper.getSubject());
        assertEquals(sentTime, _emailHelper.getSentDate());
        assertEquals("RoyEncoding", _emailHelper.getEncoding());

        List msgs = _mailSender.getSentMessages();
        MimeMessage msg = (MimeMessage) msgs.get(0);
        assertEquals(sentTime.toString(), msg.getSentDate().toString());
    }
}
