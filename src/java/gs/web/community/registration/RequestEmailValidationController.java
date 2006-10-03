package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.javamail.JavaMailSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import gs.web.util.UrlBuilder;
import gs.data.util.DigestUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RequestEmailValidationController extends AbstractController {
    public static final String BEAN_ID = "/community/requestEmailValidation.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private JavaMailSender _mailSender;
    private String _viewName;
    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();
        String email = request.getParameter("email");
        User user = _userDao.findUserFromEmailIfExists(email);
        UserCommand userCommand = new UserCommand();
        userCommand.setUser(user);

        if (user == null || user.isPasswordEmpty()) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
            // get registration form to auto fill in email
            builder.addParameter("email", email);
            String href = builder.asAnchor(request, "Register here").asATag();

            model.put("message", "You are not a member yet. " + href + ".");
        } else if (user.isEmailProvisional()) {
            MimeMessage mm = RequestEmailValidationController.buildMultipartEmail
                    (_mailSender.createMimeMessage(), request, userCommand);
            _mailSender.send(mm);
        } else if (user.isEmailValidated()) {
            model.put("message", "Your account has already been validated.");
        }

        return new ModelAndView(_viewName, model);
    }

    /**
     * Builds a multipart email message.
     * @param msg create msg with JavaMailSender.createMimeMessage()
     * @param request
     * @param userCommand
     * @return multipart email message
     * @throws java.security.NoSuchAlgorithmException
     * @throws javax.mail.MessagingException
     */
    public static MimeMessage buildMultipartEmail
            (MimeMessage msg, HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException, MessagingException {
        msg.setFrom(new InternetAddress("gs-batch@greatschools.net"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(userCommand.getEmail()));
        msg.setSubject("GreatSchools subscription confirmation");

        // now we construct the body of the email, which is a Multipart.
        // alternative means the email consists of multiple parts, each of which contains the same content,
        // but in different formats
        Multipart mp = new MimeMultipart("alternative");

        // plain text part
        BodyPart plainTextBodyPart = new MimeBodyPart();
        plainTextBodyPart.setText(getEmailPlainText(request, userCommand));
        mp.addBodyPart(plainTextBodyPart);

        // HTML part
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setText(getEmailHTML(request, userCommand), "US-ASCII", "html");
        mp.addBodyPart(htmlBodyPart);

        msg.setContent(mp);

        return msg;
    }

    private static String getEmailHTML(HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("<html><body>");
        emailContent.append("<h3>Welcome to the GreatSchools.net community!</h3>\n\n");
        emailContent.append("<p>This email is being sent to confirm a subscription request. ");
        emailContent.append("The request was generated for the email address ");
        emailContent.append(userCommand.getEmail()).append(".</p>\n\n");
        emailContent.append("<p>To confirm this subscription request, please ");
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + userCommand.getUser().getId());
        if (!StringUtils.isEmpty(userCommand.getRedirectUrl())) {
            builder.addParameter("redirect", userCommand.getRedirectUrl());
        }
        emailContent.append(builder.asAbsoluteAnchor(request, "click here").asATag());
        emailContent.append(".</p>\n\n");
        emailContent.append("<p>If you did not make this request, or you do not remember the password ");
        emailContent.append("you chose, then please ");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + userCommand.getUser().getId());
        emailContent.append(builder.asAbsoluteAnchor(request, "click here to cancel the request").asATag());
        emailContent.append(" and leave your account unchanged.</p>\n<br/>\n");
        emailContent.append("GreatSchools.net<br/>\n");
        emailContent.append("301 Howard St., Suite 1440<br/>\n");
        emailContent.append("San Francisco, CA 94105<br/>\n");
        emailContent.append("</body></html>");

        return emailContent.toString();
    }

    private static String getEmailPlainText(HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("Welcome to the GreatSchools.net community!\n\n");
        emailContent.append("This email is being sent to confirm a subscription request. ");
        emailContent.append("The request was generated for the email address ");
        emailContent.append(userCommand.getEmail()).append(".\n\n");
        emailContent.append("To confirm this subscription request, please click on the following link:\n");
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + userCommand.getUser().getId());
        if (!StringUtils.isEmpty(userCommand.getRedirectUrl())) {
            builder.addParameter("redirect", userCommand.getRedirectUrl());
        }
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n");
        emailContent.append("\nIf you did not make this request, or you do not remember the password ");
        emailContent.append("you chose, then please click on the following link to cancel the request ");
        emailContent.append("and leave your account unchanged:\n");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + userCommand.getUser().getId());
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n\n");
        emailContent.append("GreatSchools.net\n");
        emailContent.append("301 Howard St., Suite 1440\n");
        emailContent.append("San Francisco, CA 94105\n");

        return emailContent.toString();
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
