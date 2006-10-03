package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 18, 2006
 * Time: 10:13:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class ForgotPasswordController extends SimpleFormController {
    public static final String BEAN_ID = "/community/forgotPassword.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private JavaMailSender _mailSender;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        UserCommand userCommand = (UserCommand) command;

        if (request.getParameter("email") != null) {
            userCommand.setEmail(request.getParameter("email"));
        }
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (errors.hasErrors()) {
            return;
        }
        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        if (user == null) {
            // do nothing
        } else if (user.isEmailProvisional()) {
            errors.rejectValue("email", "password_empty", "You have chosen a new password, but haven't " +
                    "validated your email address yet. To validate your email address, follow the " +
                    "instructions in the email sent to you.");
        } else if (user.isPasswordEmpty()) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
            builder.addParameter("email", userCommand.getEmail());
            String msg = "You haven't chosen a password yet. To choose a password, please " +
                    builder.asAnchor(request, "click here").asATag();
            errors.rejectValue("email", "password_empty", msg);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        MimeMessage mm;
        if (user != null) {
            userCommand.setUser(user);
            mm = buildMultipartEmail(request, userCommand.getEmail(), user.getId());
        } else {
            mm = buildMultipartEmail(request, userCommand.getEmail(), null);
        }


        _mailSender.send(mm);
        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("userCmd", userCommand);
        return mAndV;
    }

    /**
     * Builds a multipart email message.
     * @param request
     * @param email
     * @param userId
     * @return multipart email message
     * @throws NoSuchAlgorithmException
     * @throws javax.mail.MessagingException
     */
    private MimeMessage buildMultipartEmail(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException, MessagingException {
        MimeMessage msg = _mailSender.createMimeMessage();

        msg.setFrom(new InternetAddress("gs-batch@greatschools.net"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        msg.setSubject("GreatSchools reset password link");

        // now we construct the body of the email, which is a Multipart.
        // alternative means the email consists of multiple parts, each of which contains the same content,
        // but in different formats
        Multipart mp = new MimeMultipart("alternative");

        // plain text part
        BodyPart plainTextBodyPart = new MimeBodyPart();
        if (userId != null) {
            plainTextBodyPart.setText(getEmailPlainTextUserExists(request, email, userId));
        } else {
            plainTextBodyPart.setText(getEmailPlainTextUserNotExist(request, email));
        }
        mp.addBodyPart(plainTextBodyPart);

        // HTML part
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        if (userId != null) {
            htmlBodyPart.setText(getEmailHTMLUserExists(request, email, userId), "US-ASCII", "html");
        } else {
            htmlBodyPart.setText(getEmailHTMLUserNotExist(request, email), "US-ASCII", "html");
        }
        mp.addBodyPart(htmlBodyPart);

        msg.setContent(mp);

        return msg;
    }

    protected String getEmailPlainTextUserExists(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        String hash = DigestUtil.hashStringInt(email, userId);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userId);
        emailContent.append("Hi!\n\n");
        emailContent.append("We recently received a request to reset your password");
        emailContent.append(" (").append(email).append(").\n\n");
        emailContent.append("Please click on the following link to choose a new password: ");
        emailContent.append(builder.asFullUrl(request)).append("\n\n");
        emailContent.append("If you are unable to click on the link above, try copying and ");
        emailContent.append("pasting the link into your browser.\n");
        emailContent.append("If you did not make this request, please ignore and delete this email.\n\n");
        emailContent.append("Thanks!\nThe GreatSchools Team\n");
        return emailContent.toString();
    }

    protected String getEmailPlainTextUserNotExist(HttpServletRequest request, String email) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null);
        emailContent.append("Hi!\n\n");
        emailContent.append("You requested your username and password on GreatSchools.net.");
        emailContent.append(" Unfortunately, we don't have an account associated with this email address ");
        emailContent.append(email).append(".\n\n");
        emailContent.append("Please visit the forgot password page (");
        emailContent.append(builder.asFullUrl(request));
        emailContent.append(") and try a different email address.\n\n");
        emailContent.append("Don't have an account on GreatSchools.net? Click here to create one: ");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n\n");
        emailContent.append("If you did not make this request, please ignore and delete this email.\n\n");
        emailContent.append("Thanks!\nThe GreatSchools Team\n");
        return emailContent.toString();
    }

    protected String getEmailHTMLUserExists(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("<html><body>\n");
        String hash = DigestUtil.hashStringInt(email, userId);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userId);
        emailContent.append("<p>Hi!</p>\n\n");
        emailContent.append("<p>We recently received a request to reset your password. Please ");
        emailContent.append(builder.asAbsoluteAnchor(request, "click here").asATag());
        emailContent.append(" to choose a new password.</p>\n\n");
        emailContent.append("<p>If you did not make this request, please ignore and delete this email.</p>\n\n");
        emailContent.append("<p>Thanks!</p>\n<p>The GreatSchools Team</p>\n");
        emailContent.append("</body></html>");
        return emailContent.toString();
    }

    protected String getEmailHTMLUserNotExist(HttpServletRequest request, String email) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("<html><body>\n");
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null);
        emailContent.append("<p>Hi!</p>\n\n");
        emailContent.append("<p>You requested your username and password on GreatSchools.net.");
        emailContent.append(" Unfortunately, we don't have an account associated with this email address: ");
        emailContent.append(email).append(".</p>\n\n");
        emailContent.append("<p>Please visit the ");
        emailContent.append(builder.asAbsoluteAnchor(request, "forgot password page").asATag());
        emailContent.append(" and try a different email address.</p>\n\n");
        emailContent.append("<p>Don't have an account on GreatSchools.net? ");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
        emailContent.append(builder.asAbsoluteAnchor(request, "Create an account").asATag());
        emailContent.append(".</p>\n\n");
        emailContent.append("<p>If you did not make this request, please ignore and delete this email.</p>\n\n");
        emailContent.append("<p>Thanks!</p>\n<p>The GreatSchools Team</p>\n");
        emailContent.append("</body></html>");
        return emailContent.toString();
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
