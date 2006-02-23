package gs.web.search;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Properties;
import java.util.Date;

import gs.data.state.State;
import gs.web.SessionContext;

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

        String stateString = null;

        if (command != null && (command instanceof FeedbackCommand)) {
            try {
                FeedbackCommand fc = (FeedbackCommand)command;
                stateString = fc.getState();
                Message msg = buildMessage(fc);
                if (!fc.test) {
                    Transport.send(msg);
                }
            } catch (Exception e) {
                _log.warn("Search Feedback could not be sent", e);
            }
        }

        if (stateString == null) {
            State state = SessionContext.getInstance(request).getStateOrDefault();
            stateString = state.getAbbreviationLowerCase();
        }

        StringBuffer path = new StringBuffer("/search/feedbackSubmit.page?state=");
        path.append(stateString.toLowerCase());
        RedirectView view = new RedirectView(path.toString(), true);
        return new ModelAndView(view);
    }

    /**
     * Assembles a message from the fields in the FeedbackCommand.
     * @param fc
     * @throws Exception
     */
    static Message buildMessage(FeedbackCommand fc) throws Exception {

        // Build the text of the message:
        StringBuffer buffer = new StringBuffer();

        buffer.append("QUERY: ");
        String query = StringUtils.isNotEmpty(fc.getQuery()) ? fc.getQuery() : "";
        buffer.append(query);
        buffer.append("\n\n");

        buffer.append("STATE: ");
        buffer.append(StringUtils.isNotEmpty(fc.getState()) ? fc.getState() : "");
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

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", "mail.greatschools.net");
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("search_feedback@greatschools.net"));
        msg.setSubject("Search Feedback: " + query);
        msg.setSentDate(new Date());
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress("search_feedback@greatschools.net"));
        msg.setText(buffer.toString());
        return msg;
    }
}
