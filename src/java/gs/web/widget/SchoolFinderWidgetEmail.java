package gs.web.widget;

import gs.web.util.AbstractSendEmailBean;
import gs.web.util.UrlBuilder;
import gs.data.community.User;
import gs.data.util.email.EmailHelper;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolFinderWidgetEmail extends AbstractSendEmailBean {
    public static final String BEAN_ID = "schoolFinderWidgetEmail";
    public static final String HTML_EMAIL_LOCATION =
            "/gs/web/widget/schoolFinderWidgetEmail-html.txt";

    /**
     * Creates and sends an email to the given user with their customized SchoolFinder widget code
     * @param user User (must have valid email address) to send email to
     * @param widgetCode Widget code
     * @throws IOException on error reading email template from file
     * @throws MessagingException on error creating message
     */
    public void sendToUser(User user, String widgetCode, HttpServletRequest request) throws IOException, MessagingException {
        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource(HTML_EMAIL_LOCATION);
        emailHelper.setSentToCustomMessage("<p>This confirmation message was sent to $EMAIL.</p>");
        emailHelper.setGreatSchoolsDescriptionWithNoLineBreaks(true);

        String cpn = "arwemcod";

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_CUSTOMIZATION);
        urlBuilder.addParameter("cpn", cpn);
        emailHelper.addInlineReplacement("WIDGET_CUSTOMIZATION_PAGE", urlBuilder.asFullUrl(request));

        emailHelper.addInlineReplacement("WIDGET_CODE", StringEscapeUtils.escapeHtml(widgetCode));

        emailHelper.send();
    }

}
