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
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
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

    protected boolean suppressValidation(HttpServletRequest request) {
        return isCancel(request);
    }

    protected boolean suppressValidation(HttpServletRequest request, Object obj) {
        return isCancel(request) || super.suppressValidation(request, obj);
    }

    protected boolean isCancel(HttpServletRequest request) {
        return request.getParameter("cancel.x") != null || request.getParameter("cancel") != null;
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        // don't do validation on cancel
        // also don't both checking for a user if the emailValidator rejects the address
        if (suppressValidation(request) || errors.hasErrors()) {
            return;
        }
        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        if (user == null || user.isEmailProvisional()) {
            // generate error
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, userCommand.getEmail());
            String href = builder.asAnchor(request, "join the community").asATag();
            errors.rejectValue("email", null, "There is no account associated with that email address. " +
                    "Would you like to " + href + "?");
//        } else if (user.isEmailProvisional()) {
//            UrlBuilder builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, user.getEmail());
//            String href2 = builder.asAnchor(request, "(resend email)").asATag();
//            errors.rejectValue("email", "password_empty", "You have chosen a new password, but haven't " +
//                    "validated your email address yet. To validate your email address, follow the " +
//                    "instructions in the email sent to you " + href2 + ".");
        } else if (user.isPasswordEmpty()) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null,
                    userCommand.getEmail());
            String msg = "You haven't chosen a password yet. To choose a password, please " +
                    builder.asAnchor(request, "click here").asATag();
            errors.rejectValue("email", "password_empty", msg);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        ModelAndView mAndV = new ModelAndView();
        if (!suppressValidation(request)) {
            UserCommand userCommand = (UserCommand) command;
            User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
            MimeMessage mm;
            userCommand.setUser(user);
            mm = buildMultipartEmail(request, userCommand.getEmail(), user.getId());
            _mailSender.send(mm);
            mAndV.setViewName(getSuccessView());
            String msg = "An email has been sent to " + userCommand.getEmail() +
                    " with instructions for selecting a new password.";
            mAndV.getModel().put("message", msg);
        } else {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null);
            mAndV.setViewName("redirect:" + builder.asFullUrl(request));
        }

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
        plainTextBodyPart.setText(getEmailPlainTextUserExists(request, email, userId));
        mp.addBodyPart(plainTextBodyPart);

        // HTML part
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setText(getEmailHTMLUserExists(request, email, userId), "US-ASCII", "html");
        mp.addBodyPart(htmlBodyPart);

        msg.setContent(mp);

        return msg;
    }

    protected String getEmailPlainTextUserExists(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        String hash = DigestUtil.hashStringInt(email, userId);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userId);
        emailContent.append("Dear GreatSchools member,\n\n");
        emailContent.append("You requested that we reset your password on GreatSchools. ");
        emailContent.append("Please click on the following link to select a new password: ");
        emailContent.append(builder.asFullUrl(request)).append("\n\n");
        emailContent.append("Thanks!\n\nThe GreatSchools Team\n");
        return emailContent.toString();
    }

    protected String getEmailHTMLUserExists(HttpServletRequest request, String email, Integer userId) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("<html>");
        emailContent.append("<head><style>p {\n" +
                "color:#444444; \n" +
                "font: 13px/16px Trebuchet MS, Helvetica, Arial, Verdana, sans-serif;\n" +
                "}\n" +
                "\n" +
                "a {\n" +
                "color: #3399aa;\n" +
                "text-decoration: underline;\n" +
                "}\n" +
                "</style></head>\n");
        emailContent.append("<body>\n");
        String hash = DigestUtil.hashStringInt(email, userId);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userId);
        emailContent.append("<p>Dear GreatSchools member,</p>\n\n");
        emailContent.append("<p>You requested that we reset your password on GreatSchools. ");
        emailContent.append("Please ");
        emailContent.append(builder.asAbsoluteAnchor(request, "click here to select a new password").asATag());
        emailContent.append(".</p>\n\n");
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
