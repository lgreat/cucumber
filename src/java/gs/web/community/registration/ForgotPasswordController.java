package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        SimpleMailMessage smm;
        if (user != null) {
            userCommand.setUser(user);
            smm = buildEmailMessageForExistingUser(request, userCommand);
        } else {
            smm = buildEmailMessageForNewUser(request, userCommand);
        }


        _mailSender.send(smm);
        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("userCmd", userCommand);
        return mAndV;
    }

    protected SimpleMailMessage buildEmailMessageForExistingUser
            (HttpServletRequest request, UserCommand userCommand) throws NoSuchAlgorithmException {
        StringBuffer emailContent = new StringBuffer();
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, hash + userCommand.getUser().getId());
        emailContent.append("Hi!\n\n");
        emailContent.append("We recently received a request to ");
        emailContent.append(builder.asAbsoluteAnchor(request, "reset your password").asATag());
        emailContent.append(" (").append(userCommand.getEmail()).append(").\n\n");
        emailContent.append("If you are unable to open the link above, try copying and");
        emailContent.append("pasting the following link into your browser:\n");
        emailContent.append(builder.asFullUrl(request)).append("\n\n");
        emailContent.append("If you did not make this request, please ignore and delete this email.\n\n");
        emailContent.append("Thanks!\nThe GreatSchools Team\n");

        String subject = "GreatSchools reset password link";
        String fromAddress = "gs-batch@greatschools.net";

        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(emailContent.toString());
        smm.setTo(userCommand.getEmail());
        smm.setSubject(subject);
        smm.setFrom(fromAddress);

        return smm;
    }

    protected SimpleMailMessage buildEmailMessageForNewUser(HttpServletRequest request, UserCommand userCommand) {
        StringBuffer emailContent = new StringBuffer();
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null);
        emailContent.append("Hi!\n\n");
        emailContent.append("You requested your username and password on GreatSchools.net.");
        emailContent.append(" Unfortunately, we don't have an account associated with this email address ");
        emailContent.append(userCommand.getEmail()).append(".\n\n");
        emailContent.append("Please visit the ");
        emailContent.append(builder.asAbsoluteAnchor(request, "forgot password page").asATag());
        emailContent.append(" and try a different email address.\n\n");
        emailContent.append("Don't have an account on GreatSchools.net? ");
        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, userCommand.getEmail());
        emailContent.append(builder.asAbsoluteAnchor(request, "Create an account").asATag());
        emailContent.append("If you did not make this request, please ignore and delete this email.\n\n");
        emailContent.append("Thanks!\nThe GreatSchools Team\n");

        String subject = "GreatSchools reset password link";
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
