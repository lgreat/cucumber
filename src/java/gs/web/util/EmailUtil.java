/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides utility methods for creating email messages.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailUtil {
    private static final Logger _log = Logger.getLogger(EmailUtil.class);
    private static Map _resourceCache = new HashMap();

    public static MimeMessage createMessage(MimeMessage mimeMessage, EmailOptions options) throws MessagingException {
        // fromEmail, toEmail, and subject must be provided
        if (options == null || StringUtils.isEmpty(options.getFromEmail()) ||
                StringUtils.isEmpty(options.getToEmail()) || StringUtils.isEmpty(options.getSubject())) {
            throw new IllegalArgumentException("You must specify from, to, and subject");
        }
        if (options.isMultipart()) {
            // for multiparts, either body or resource for both parts must be provided
            MultiPartEmailOptions mpOptions = (MultiPartEmailOptions) options;
            if ((StringUtils.isEmpty(mpOptions.getHtmlBody()) &&
                    StringUtils.isEmpty(mpOptions.getHtmlResourceName())) ||
                    (StringUtils.isEmpty(mpOptions.getTextBody()) &&
                            StringUtils.isEmpty(mpOptions.getTextResourceName()))) {
                throw new IllegalArgumentException("You must specify either the body or the " +
                        "resource name from which to read the body for both the HTML " +
                        "and text parts");
            }
        } else {
            // for single parts, either body or resource must be provided
            SinglePartEmailOptions spOptions = (SinglePartEmailOptions) options;
            if (StringUtils.isEmpty(spOptions.getBody()) &&
                    StringUtils.isEmpty(spOptions.getResourceName())) {
                throw new IllegalArgumentException("You must specify the body of the email or " +
                        "the resource name from which to read the body");
            }
        }

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, options.isMultipart(),
                options.getEncoding());
        helper.setTo(options.getToEmail());
        if (!StringUtils.isEmpty(options.getFromName())) {
            try {
                helper.setFrom(options.getFromEmail(), options.getFromName());
            } catch (UnsupportedEncodingException uee) {
                helper.setFrom(options.getFromEmail());
            }
        } else {
            helper.setFrom(options.getFromEmail());
        }
        helper.setSubject(options.getSubject());
        helper.setSentDate(options.getSentDate());

        if (options instanceof MultiPartEmailOptions) {
            loadMultiPartBody(helper, (MultiPartEmailOptions) options);
        } else {
            loadSinglePartBody(helper, (SinglePartEmailOptions) options);
        }

        return helper.getMimeMessage();
    }

    protected static void loadMultiPartBody(MimeMessageHelper helper, MultiPartEmailOptions options) throws MessagingException {
        String textBody = options.getTextBody();
        if (StringUtils.isEmpty(textBody)) {
            textBody = getTextFromResource(new ClassPathResource(options.getTextResourceName()));
        }
        String htmlBody = options.getHtmlBody();
        if (StringUtils.isEmpty(htmlBody)) {
            htmlBody = getTextFromResource(new ClassPathResource(options.getHtmlResourceName()));
        }

        textBody = performReplacements(textBody, options.getInlineReplacements());
        htmlBody = performReplacements(htmlBody, options.getInlineReplacements());

        helper.setText(textBody, htmlBody);
    }

    protected static void loadSinglePartBody(MimeMessageHelper helper, SinglePartEmailOptions options) throws MessagingException {
        String body = options.getBody();
        if (StringUtils.isEmpty(body)) {
            body = getTextFromResource(new ClassPathResource(options.getResourceName()));
        }

        body = performReplacements(body, options.getInlineReplacements());

        helper.setText(body, options.isBodyHtml());
    }

    protected static String getTextFromResource(Resource resource) {
        String emailText;
        // check cache first
        emailText = (String) _resourceCache.get(resource.getFilename());
        if (emailText == null) {
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
                emailText = buffer.toString();
                _resourceCache.put(resource.getFilename(), emailText);
            } catch (IOException ioe) {
                _log.error(ioe);
                return null;
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
        return emailText;
    }

    protected static String performReplacements(String text, Map replacements) {
        Iterator keys = replacements.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            String value = replacements.get(key).toString();
            text = text.replaceAll("\\$" + key + "\\$", value);
        }
        return text;
    }

    private static abstract class EmailOptions {
        private String _toEmail;
        private String _fromEmail;
        private String _fromName;
        private String _subject;
        private Date _sentDate;
        private String _encoding;
        private Map _inlineReplacements;

        public EmailOptions() {
            _encoding = "UTF-8";
            _sentDate = new Date();
            _inlineReplacements = new HashMap();
        }

        public boolean isMultipart() {
            return this instanceof MultiPartEmailOptions;
        }

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

        public void addInlineReplacement(String key, String value) {
            _inlineReplacements.put(key, value);
        }
    }

    public static class SinglePartEmailOptions extends EmailOptions {
        private String _resourceName;
        private String _body;
        private boolean _bodyHtml = true;

        public String getResourceName() {
            return _resourceName;
        }

        public void setResourceName(String resourceName) {
            _resourceName = resourceName;
        }

        public String getBody() {
            return _body;
        }

        public void setBody(String body) {
            _body = body;
        }

        public boolean isBodyHtml() {
            return _bodyHtml;
        }

        public void setBodyHtml(boolean bodyHtml) {
            _bodyHtml = bodyHtml;
        }
    }

    public static class MultiPartEmailOptions extends EmailOptions {
        private String _htmlResourceName;
        private String _textResourceName;
        private String _htmlBody;
        private String _textBody;

        public String getHtmlResourceName() {
            return _htmlResourceName;
        }

        public void setHtmlResourceName(String htmlResourceName) {
            _htmlResourceName = htmlResourceName;
        }

        public String getTextResourceName() {
            return _textResourceName;
        }

        public void setTextResourceName(String textResourceName) {
            _textResourceName = textResourceName;
        }

        public String getTextBody() {
            return _textBody;
        }

        public void setTextBody(String textBody) {
            _textBody = textBody;
        }

        public String getHtmlBody() {
            return _htmlBody;
        }

        public void setHtmlBody(String htmlBody) {
            _htmlBody = htmlBody;
        }
    }
}
