/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.data.util.email.EmailHelper;
import gs.web.util.AbstractSendEmailBean;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.springframework.mail.MailException;
import org.apache.commons.codec.binary.Base64;

/**
 * Bean that encapsulates the registration confirmation email. Because registration can
 * logically be "completed" in multiple places, this code was pulled out to a bean that
 * can be injected into each necessary controller.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmationEmail extends AbstractSendEmailBean {
    public static final String BEAN_ID = "registrationConfirmationEmail";
    public static final String HTML_EMAIL_LOCATION =
            "/gs/web/community/registration/registrationConfirmationEmail-html.txt";
    public static final String TEXT_EMAIL_LOCATION =
            "/gs/web/community/registration/registrationConfirmationEmail-plainText.txt";

    private IGeoDao _geoDao;

    /**
     * Creates and sends an email to the given user welcoming them to the GreatSchools
     * community.
     * @param user User (must have valid email address) to send email to
     * @param request used to instantiate UrlBuilder for links in the email
     * @throws IOException on error reading email template from file
     * @throws MessagingException on error creating message
     * @throws MailException on error sending email
     */
    public void sendToUser(User user, String passwordPlaintext, HttpServletRequest request) throws IOException, MessagingException, MailException {
        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource(HTML_EMAIL_LOCATION);
        emailHelper.readPlainTextFromResource(TEXT_EMAIL_LOCATION);

        String cpn = "gssu_welcome";

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        String communityHost = sc.getSessionContextUtil().getCommunityHost(request);
        String comHostPrefix = "http://" + communityHost;
        City city = _geoDao.findCity(user.getUserProfile().getState(), user.getUserProfile().getCity());
        String localEncodedUrl = new String(Base64.encodeBase64(("/q-and-a/browse?tab=local&cpn=" + cpn + "#browseQuestions").getBytes()));
        String localUrl = comHostPrefix + "/location/change/remote?rloc_id=" + city.getId() + "&redir_url=" + localEncodedUrl;
        
        emailHelper.addInlineReplacement("USER_USERNAME", user.getUserProfile().getScreenName());
        emailHelper.addInlineReplacement("USER_EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("USER_PASSWORD", passwordPlaintext);
        emailHelper.addInlineReplacement("LOCAL_TAB", localUrl);
        emailHelper.addInlineReplacement("CITY_NAME", city.getName());
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, null, null);
        urlBuilder.addParameter("cpn", cpn);
        emailHelper.addInlineReplacement("ALL_ARTICLES", urlBuilder.asFullUrl(request));
        urlBuilder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, null, null);
        urlBuilder.addParameter("cpn", cpn);
        emailHelper.addInlineReplacement("MY_SCHOOL_LIST", urlBuilder.asFullUrl(request));

        emailHelper.send();
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
