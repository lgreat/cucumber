package gs.web.content;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;

import gs.web.community.ICaptchaCommand;
import gs.web.community.MailToFriendCommand;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008EmailConfirmController extends SimpleFormController {
    public static final String BEAN_ID = "/content/election2008Confirm.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private JavaMailSender _mailSender;

    protected void onBindAndValidate(HttpServletRequest request, java.lang.Object objCommand,
                                     BindException errors) {
        Election2008EmailCommand command = (Election2008EmailCommand) objCommand;

        EmailValidator emv = EmailValidator.getInstance();

        if (!emv.isValid(command.getUserEmail())) {
            errors.rejectValue("userEmail", null, "Please enter a valid email address.");
        }

        if (!emv.isValid(command.getFriendEmail())) {
            errors.rejectValue("friendEmail", null, "Please enter your friend's email address.");
        }

        if (StringUtils.isEmpty(command.getSubject())) {
            errors.rejectValue("subject", null, "Please enter a subject.");
        }

        if (StringUtils.isEmpty(command.getMessage())) {
            errors.rejectValue("message", null, "Sorry, the message cannot be empty.");
        }

        ICaptchaCommand captchaCommand = (ICaptchaCommand) objCommand;
        if (errors.getErrorCount() == 0 ) {
            // Validate the captcha request/response pair
            String remoteAddr = request.getRemoteAddr();
            String challenge =  captchaCommand.getChallenge();
            String response = captchaCommand.getResponse();

            ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
            reCaptcha.setPrivateKey("6LfZWAEAAAAAAKt3EpAJngyabjFSywONdA7xqI2C");
            ReCaptchaResponse reCaptchaResponse =
                reCaptcha.checkAnswer(remoteAddr, challenge, response);

            if (!reCaptchaResponse.isValid()) {
                errors.rejectValue("response", null, "The Captcha response you entered is invalid. Please try again.");
            }
        }
    }

    protected void sendEmail(Election2008EmailCommand command) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(command.getMessage());
        smm.setTo(command.getFriendEmail());
        smm.setSubject(command.getSubject());
        smm.setFrom(command.getUserEmail());

        try {
            _mailSender.send(smm);
        } catch (MailException ex) {
            _log.info(ex.getMessage());
        }
    }

    protected ModelAndView onSubmit(Object objCommand) {
        Election2008EmailCommand command = (Election2008EmailCommand) objCommand;
        sendEmail(command);

        return new ModelAndView(getSuccessView());
    }


    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
