package gs.web.content;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008EmailConfirmController extends SimpleFormController {
    public static final String BEAN_ID = "/content/actionForEducationEmail.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private JavaMailSender _mailSender;

    protected void onBindAndValidate(HttpServletRequest request, java.lang.Object objCommand,
                                     BindException errors) {
        Election2008EmailCommand command = (Election2008EmailCommand) objCommand;

        EmailValidator emv = EmailValidator.getInstance();

        if (StringUtils.isEmpty(command.getUserEmail())) {
            errors.rejectValue("userEmail", null, "Please enter your email address.");
        } else if (!emv.isValid(command.getUserEmail())) {
            errors.rejectValue("userEmail", null, "Please enter a valid email address.");
        }

        if (StringUtils.isEmpty(command.getFriendEmail())) {
            errors.rejectValue("friendEmail", null, "Please enter your friend's email address.");
        } else if (!emv.isValid(command.getFriendEmail())) {
            errors.rejectValue("friendEmail", null, "Please enter a valid email address for your friend.");
        }

        if (StringUtils.isEmpty(command.getSubject())) {
            errors.rejectValue("subject", null, "Please enter a subject.");
        }

        if (StringUtils.isEmpty(command.getMessage())) {
            errors.rejectValue("message", null, "Sorry, the message cannot be empty.");
        }

        if (errors.getErrorCount() == 0 ) {
            // Validate the captcha request/response pair
            String remoteAddr = request.getRemoteAddr();
            String challenge =  command.getChallenge();
            String response = command.getResponse();

            ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
            reCaptcha.setPrivateKey("6LfZWAEAAAAAAKt3EpAJngyabjFSywONdA7xqI2C");
            ReCaptchaResponse reCaptchaResponse =
                reCaptcha.checkAnswer(remoteAddr, challenge, response);

            if (!reCaptchaResponse.isValid()) {
                errors.rejectValue("response", null, "The Captcha response you entered is invalid. Please try again.");
            }
        }

        if (errors.getErrorCount() > 0) {
            command.setPageNameSuffix(Election2008EmailCommand.SUFFIX_TELL_A_FRIEND_VALIDATION);
        }
    }

    protected boolean sendEmail(Election2008EmailCommand command) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(command.getMessage());
        smm.setTo(command.getFriendEmail());
        smm.setSubject(command.getSubject());
        smm.setFrom(command.getUserEmail());

        try {
            _mailSender.send(smm);
        } catch (MailException ex) {
            _log.error(ex.getMessage());
            return false;
        }
        return true;
    }

    protected ModelAndView onSubmit(Object objCommand) {
        Election2008EmailCommand command = (Election2008EmailCommand) objCommand;
        Election2008EmailCommand emailCommand = new Election2008EmailCommand();
        Map<String, Object> model = new HashMap<String, Object>();
        if (sendEmail(command)) {
            emailCommand.setUserEmail(command.getUserEmail());
            emailCommand.setAlert("Thank you! Your email has been sent.");
            emailCommand.setHideForm(true);
            emailCommand.setPageNameSuffix(Election2008EmailCommand.SUFFIX_TELL_A_FRIEND_CONFIRM);

            model.put("edin08Cmd", emailCommand);
        } else {
            command.setAlert("Email send failed. Please try again soon.");
            command.setPageNameSuffix(Election2008EmailCommand.SUFFIX_TELL_A_FRIEND_EMAIL_ERROR);

            model.put("edin08Cmd", command);
        }

        return new ModelAndView(getSuccessView(), model);
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
