/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import gs.data.util.email.EmailHelperFactory;
import gs.data.util.email.EmailHelper;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a base class for beans that wish to send emails.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public abstract class AbstractSendEmailBean {
    /* Spring injected properties */
    private EmailHelperFactory _emailHelperFactory;
    private String _subject;
    private String _fromEmail;
    private String _fromName;

    /**
     * Returns an EmailHelper populated with the subject, fromEmail, and fromName properties.
     * @return
     */
    protected EmailHelper getEmailHelper() {
        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject(getSubject());
        emailHelper.setFromEmail(getFromEmail());
        if (StringUtils.isNotEmpty(getFromName())) {
            emailHelper.setFromName(getFromName());
        }
        return emailHelper;
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
