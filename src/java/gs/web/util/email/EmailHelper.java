/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util.email;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Provides for and hides all the messiness around sending emails. Obtain an instance of this
 * class via the factory bean: gs.web.util.email.EmailHelperFactory, set the relevant fields, and
 * fire it off with a call to send().
 *
 * The encoding defaults to "UTF-8", and the sentDate defaults to the date and time when the
 * EmailHelper object is instantiated from the factory.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailHelper {
    protected static Map _resourceCache = new HashMap(); // test case needs access
    protected Logger _log = Logger.getLogger(getClass());

    /** Factory injected mail sender */
    private JavaMailSender _mailSender;

    private String _toEmail;
    private String _fromEmail;
    private String _fromName;
    private String _subject;
    private Date _sentDate;
    private String _encoding;
    private Map _inlineReplacements;
    private String _htmlBody;
    private String _textBody;

    /**
     * Package-protected, for general use instantiate via the factory bean:
     * gs.web.util.email.EmailHelperFactory.
     * Default values: <br/>
     * encoding = "UTF-8" <br/>
     * sentDate = new Date() <br/>
     */
    EmailHelper() {
        _encoding = "UTF-8";
        _sentDate = new Date();
        _inlineReplacements = new HashMap();
    }

    /****************************
     * Publicly exposed methods * (not including property getter/setters)
     ****************************/

    /**
     * Send the message encapsulated by this object
     * @throws MessagingException on error creating the MimeMessage
     * @throws MailException on error sending the email
     * @throws IllegalStateException if isValid returns false
     */
    public void send() throws MessagingException, MailException, IllegalStateException {
        if (!isValid()) {
            _log.error("Attempt to send email with missing properties.");
            throw new IllegalStateException("Not enough properties set. " +
                    "You must set at least toEmail, fromEmail, subject, and either " +
                    "textBody or htmlBody");
        }
        _mailSender.send(createMimeMessage());
    }

    /**
     * Reads the plain text body part from a resource
     * @param textResourceName
     * @throws IOException on error reading resource
     */
    public void readPlainTextFromResource(String textResourceName) throws IOException {
        setTextBody(readStringFromResource(textResourceName));
    }

    /**
     * Reads the html body part from a resource
     * @param htmlResourceName
     * @throws IOException on error reading resource
     */
    public void readHtmlFromResource(String htmlResourceName) throws IOException {
        setHtmlBody(readStringFromResource(htmlResourceName));
    }

    /**
     * Clears the static resource cache. Not sure why this would be used.
     */
    public static void clearResourceCache() {
        _resourceCache.clear();
    }

    /**
     * Equivalent to getInlineReplacements().put(key, value);
     * Tells this class to replace all instances of "$key" in the body of the email with "value",
     * actual replacement occurs during creation of the MimeMessage when send() is called.
     * @param key String to replace
     * @param value Value to replace it with
     */
    public void addInlineReplacement(String key, String value) {
        _inlineReplacements.put(key, value);
    }

    /**
     * Checks the properties on this object and returns true only if enough properties are
     * set to send a valid email.
     * @return false if not enough properties are set for a valid email to be constructed.
     */
    public boolean isValid() {
        // There must be a toEmail, fromEmail, and subject, along with either a plain text
        // body or an html body
        return StringUtils.isNotEmpty(_toEmail) && StringUtils.isNotEmpty(_fromEmail) &&
                StringUtils.isNotEmpty(_subject) &&
                (StringUtils.isNotEmpty(_textBody) || StringUtils.isNotEmpty(_htmlBody));
    }

    /**
     * Checks if both the htmlBody and the textBody have been set.
     * @return true only if both htmlBody and textBody are set
     */
    public boolean isMultipart() {
        return _htmlBody != null && _textBody != null;
    }

    /****************************
     * Protected helper methods *
     ****************************/

    /**
     * Helper method used to get an instance of MimeMessage representing this object.
     * @return MimeMessage populated off of this object
     * @throws MessagingException on error populating the MimeMessage
     */
    protected MimeMessage createMimeMessage() throws MessagingException {
        MimeMessage message = _mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart(), _encoding);
        helper.setTo(getToEmail());
        if (StringUtils.isNotEmpty(_fromName)) {
            try {
                helper.setFrom(_fromEmail, _fromName);
            } catch (UnsupportedEncodingException uee) {
                helper.setFrom(_fromEmail);
            }
        } else {
            helper.setFrom(_fromEmail);
        }
        helper.setSubject(_subject);
        helper.setSentDate(_sentDate);

        if (isMultipart()) {
            helper.setText(performReplacements(_textBody), performReplacements(_htmlBody));
        } else {
            if (StringUtils.isNotEmpty(_textBody)) {
                helper.setText(performReplacements(_textBody), false);
            } else {
                helper.setText(performReplacements(_htmlBody), true);
            }
        }

        return helper.getMimeMessage();
    }

    protected String performReplacements(String text) {
        return performReplacements(text, _inlineReplacements);
    }

    /**
     * Helper method that takes all keys in replacements, searches for strings of the
     * form "$KEY" and replaces them with the associated value.
     * @param text String to perform replacements on.
     * @param replacements Map where each key, prefixed with a dollar-sign, is replaced in
     * text by the associated value.
     * @return a new string object containing the replacements, or the same string object
     * if no replacements occurred.
     */
    protected String performReplacements(String text, Map replacements) {
        if (replacements != null && replacements.size() > 0) {
            Iterator keys = replacements.keySet().iterator();
            String key;
            String value;
            while (keys.hasNext()) {
                key = keys.next().toString();
                value = replacements.get(key).toString();
                text = text.replaceAll("\\$" + key, value);
            }
        }
        return text;
    }

    /**
     * Checks the cache for a resource, if not found reads it from disk. This method is
     * thread-safe since at worst multiple threads will all read the resource from disk
     * before the cache is created (using Map.put, also thread-safe).
     * resource filename.
     * @param path Path to resource
     * @return Contents of the resource as a String, or null on error.
     * @throws IOException on error reading the resource
     */
    protected String readStringFromResource(String path) throws IOException {
        if (path == null) {
            _log.warn("Null resource path passed to getTextFromResource, returning null");
            return null;
        }
        String text;
        // check cache first
        text = (String) _resourceCache.get(path);
        if (text == null) {
            Resource resource = new ClassPathResource(path);
            // if not in cache, read from disk
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\n");
                }
                text = buffer.toString();
                _resourceCache.put(resource.getFilename(), text);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        _log.error(e);
                    }
                }
            }
        }
        return text;
    }

    /*********************
     * GETTERS / SETTERS *
     *********************/

    public String getToEmail() {
        return _toEmail;
    }

    public void setToEmail(String toEmail) {
        _toEmail = toEmail;
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

    public String getSubject() {
        return _subject;
    }

    public void setSubject(String subject) {
        _subject = subject;
    }

    public Date getSentDate() {
        return _sentDate;
    }

    public void setSentDate(Date sentDate) {
        _sentDate = sentDate;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(String encoding) {
        _encoding = encoding;
    }

    public Map getInlineReplacements() {
        return _inlineReplacements;
    }

    public void setInlineReplacements(Map inlineReplacements) {
        _inlineReplacements = inlineReplacements;
    }

    public String getHtmlBody() {
        return _htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        _htmlBody = htmlBody;
    }

    public String getTextBody() {
        return _textBody;
    }

    public void setTextBody(String textBody) {
        _textBody = textBody;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
