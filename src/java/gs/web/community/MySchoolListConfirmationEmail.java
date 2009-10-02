package gs.web.community;

import gs.web.util.AbstractSendEmailBean;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.util.email.EmailHelper;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.io.IOException;

import org.springframework.mail.MailException;
import org.apache.log4j.Logger;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MySchoolListConfirmationEmail extends AbstractSendEmailBean {
    public static final String BEAN_ID = "mySchoolListConfirmationEmail";
    public static final String HTML_EMAIL_LOCATION =
            "/gs/web/community/mySchoolListConfirmationEmail-html.txt";
    public static final String TEXT_EMAIL_LOCATION =
            "/gs/web/community/mySchoolListConfirmationEmail-plainText.txt";

    private static final Logger _log = Logger.getLogger(MySchoolListConfirmationEmail.class);

    /**
     * Creates and sends an email to the given user welcoming them to the GreatSchools
     * community.
     * @param user User (must have valid email address) to send email to
     * @param request used to instantiate UrlBuilder for links in the email
     * @throws java.io.IOException on error reading email template from file
     * @throws javax.mail.MessagingException on error creating message
     * @throws org.springframework.mail.MailException on error sending email
     */
    public void sendToUser(User user, HttpServletRequest request) throws IOException, MessagingException, MailException {
        if (!user.getUndeliverable()) {
            EmailHelper emailHelper = getEmailHelper();
            emailHelper.setToEmail(user.getEmail());
            emailHelper.readHtmlFromResource(HTML_EMAIL_LOCATION);
            emailHelper.readPlainTextFromResource(TEXT_EMAIL_LOCATION);

            String cpn = "mslst_welcome";

            SessionContext sc = SessionContextUtil.getSessionContext(request);
            String communityHost = sc.getSessionContextUtil().getCommunityHost(request);
            String comHostPrefix = "http://" + communityHost;

            emailHelper.addInlineReplacement("CHOOSING_A_SCHOOL",
                    comHostPrefix + "/category/Choosing-a-School?cpn=" + cpn);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER);
            urlBuilder.addParameter("cpn", cpn);
            emailHelper.addInlineReplacement("SCHOOL_CHOICE_CENTER",
                    urlBuilder.asFullUrl(request));
            urlBuilder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, null, (String)null);
            urlBuilder.addParameter("cpn", cpn);
            emailHelper.addInlineReplacement("MY_SCHOOL_LIST",
                    urlBuilder.asFullUrl(request));
            urlBuilder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, sc.getStateOrDefault(), null);
            urlBuilder.addParameter("email", user.getEmail());
            urlBuilder.addParameter("cpn", cpn);
            emailHelper.addInlineReplacement("NEWSLETTER_SUBSCRIBE",
                    urlBuilder.asFullUrl(request));
            
            emailHelper.send();
        } else {
            _log.warn("Not sending to undeliverable user: " + user);
        }
    }
}
