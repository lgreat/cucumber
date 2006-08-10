package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.ReadWriteController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.internet.*;
import javax.mail.*;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * @author <a href="mailto:aroy@urbanasoft.com">Anthony Roy</a>
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private JavaMailSender _mailSender;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        UserCommand userCommand = (UserCommand) command;

        if (StringUtils.isNotEmpty(userCommand.getEmail())) {
            User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
            if (user != null && !user.isEmailValidated()) {
                // only allow setting the password on people with empty or provisional password
                // existing users have to authenticate and change account settings through other channels
                userCommand.setUser(user);
                if (request.getParameter("reset") != null &&
                        request.getParameter("reset").equals("true") &&
                        user.isEmailProvisional()) {
                    // reset provisional status
                    user.setPasswordMd5(null);
                    _userDao.updateUser(user);
                }
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());

        boolean userExists = false;

        if (user != null) {
            userExists = true;
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            getUserDao().saveUser(userCommand.getUser());
        }

        try {
            userCommand.getUser().setPlaintextPassword(userCommand.getPassword());
            // mark password as unauthenticated
            userCommand.getUser().setEmailProvisional();
            getUserDao().updateUser(userCommand.getUser());
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage());
            if (!userExists) {
                // for new users, cancel the account on error
                getUserDao().removeUser(userCommand.getUser().getId());
            }
            throw e;
        }

        SimpleMailMessage smm = buildEmailMessage(request, userCommand);
        try {
            _mailSender.send(smm);
        } catch (MailException me) {
            _log.error("Error sending email message.", me);
            if (userExists) {
                // for existing users, set them back to no password
                userCommand.getUser().setPasswordMd5(null);
                getUserDao().updateUser(userCommand.getUser());
            } else {
                // for new users, cancel the account on error
                getUserDao().removeUser(userCommand.getUser().getId());
            }
            throw me;
        }

        // gotten this far, now let's update their user profile
        UserProfile userProfile = userCommand.getUserProfile();

        userProfile.setUser(userCommand.getUser());
        userCommand.getUser().setUserProfile(userProfile);
        _userDao.updateUser(userCommand.getUser());

        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        FollowUpCommand fupCommand = new FollowUpCommand();
        fupCommand.setUser(userCommand.getUser());
        fupCommand.setUserProfile(userProfile);

        // generate secure hash so if the followup profile page is submitted, we know who it is
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        mAndV.getModel().put("idString", hash);
        mAndV.getModel().put("email", userCommand.getEmail());
        mAndV.getModel().put("followUpCmd", fupCommand);
        return mAndV;
    }

    private MimeMessage buildMultipartEmail(HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException, MessagingException {
        MimeMessage msg = _mailSender.createMimeMessage();

        InternetAddress fromAddress = new InternetAddress("gs-batch@greatschools.net");
        msg.setFrom(fromAddress);
        InternetAddress toAddress = new InternetAddress(userCommand.getEmail());
        msg.addRecipient(Message.RecipientType.TO, toAddress);
        msg.setSubject("GreatSchools subscription confirmation");

        Multipart mp = new MimeMultipart("alternative");

        BodyPart plainTextBodyPart = new MimeBodyPart();
        plainTextBodyPart.setText(getEmailPlainText(request, userCommand));
        mp.addBodyPart(plainTextBodyPart);

        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setText(getEmailPlainText(request, userCommand), "US-ASCII", "html");

        return msg;
    }

    private String getEmailPlainText(HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("Welcome to the GreatSchools.net community!\n\n");
        emailContent.append("This email is being sent to confirm a subscription request. ");
        emailContent.append("The request was generated for the email address ");
        emailContent.append(userCommand.getEmail()).append(".\n\n");
        emailContent.append("To confirm this subscription request, please click on the following link:\n");
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + userCommand.getUser().getId());
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n");
        emailContent.append("\nIf you did not make this request, or you do not remember the password ");
        emailContent.append("you chose, then please click on the following link to cancel the request ");
        emailContent.append("and leave your account unchanged:\n");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + userCommand.getUser().getId());
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n");

        return emailContent.toString();
    }

    private SimpleMailMessage buildEmailMessage(HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("Welcome to the GreatSchools.net community!\n\n");
        emailContent.append("This email is being sent to confirm a subscription request. ");
        emailContent.append("The request was generated for the email address ");
        emailContent.append(userCommand.getEmail()).append(".\n\n");
        emailContent.append("To confirm this subscription request, please click on the following link:\n");
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + userCommand.getUser().getId());
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n");
        emailContent.append("\nIf you did not make this request, or you do not remember the password ");
        emailContent.append("you chose, then please click on the following link to cancel the request ");
        emailContent.append("and leave your account unchanged:\n");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, hash + userCommand.getUser().getId());
        emailContent.append(builder.asFullUrl(request));
        emailContent.append("\n");

        String subject = "GreatSchools subscription confirmation";
        String fromAddress = "gs-batch@greatschools.net";

        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(emailContent.toString());
        smm.setTo(userCommand.getEmail());
        smm.setSubject(subject);
        smm.setFrom(fromAddress);

        return smm;
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
