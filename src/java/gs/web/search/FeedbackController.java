package gs.web.search;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Properties;
import java.util.Date;

/**
 * This controller handles the submit of the search feedback form.  There is
 * currently no validation, including of email.  Once a user submits the form
 * she is redirected to the feedback thank you page: search/feedback_submit.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class FeedbackController extends SimpleFormController {

    public static final String BEAN_ID = "/search/feedback.page";
    private static final Logger _log = Logger.getLogger(FeedbackController.class);

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws ServletException {

        if (command != null) {
            try {
                sendFeebackToHelpDesk((FeedbackCommand)command);
            } catch (Exception e) {
                _log.warn("Search Feedback could not be sent", e);
            }
        }
        RedirectView rv = new RedirectView();
        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    /**
     * Assembles a message from the fields in the FeedbackCommand and sends
     * it to the HelpDesk.
     * @param fc
     * @throws Exception
     */
    private static void sendFeebackToHelpDesk(FeedbackCommand fc) throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", "mail.greatschools.net");
        Session session = Session.getDefaultInstance(props, null);

        // Build the text of the message:
        StringBuffer buffer = new StringBuffer();

        buffer.append("QUERY: ");
        buffer.append(StringUtils.isNotEmpty(fc.getQuery()) ? fc.getQuery() : "");
        buffer.append("\n\n");

        buffer.append("DESCRIPTION:\n");
        buffer.append(StringUtils.isNotEmpty(fc.getDescription()) ? fc.getDescription() : "");
        buffer.append("\n\n");

        buffer.append("EXPECTED:\n");
        buffer.append(StringUtils.isNotEmpty(fc.getExpected()) ? fc.getExpected() : "");
        buffer.append("\n\n");

        buffer.append("COMMENT:\n");
        buffer.append(StringUtils.isNotEmpty(fc.getComment()) ? fc.getComment() : "");
        buffer.append("\n\n");

        buffer.append("EMAIL: ");
        buffer.append(StringUtils.isNotEmpty(fc.getEmail()) ? fc.getEmail() : "");
        buffer.append("\n");

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("search_feedback@greatschools.net"));
        msg.setSubject("Search Feedback");
        msg.setSentDate(new Date());
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress("search_feedback@greatschools.net"));
        msg.setText(buffer.toString());
        Transport.send(msg);
    }
}
