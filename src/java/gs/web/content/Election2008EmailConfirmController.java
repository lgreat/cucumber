package gs.web.content;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

import gs.web.community.ICaptchaCommand;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008EmailConfirmController extends SimpleFormController {
    public static final String BEAN_ID = "/content/election2008Confirm.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private JavaMailSender _mailSender;

    protected void onBind(HttpServletRequest request, Object objCommand, BindException errors) {
        _log.error("onBind");
    }

    protected void onBindAndValidate(HttpServletRequest request, java.lang.Object objCommand,
                                     BindException errors) {
        _log.error("onBindAndValidate");
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

        _log.error("errors.hasErrors() == " + errors.hasErrors());
    }

    protected ModelAndView onSubmit(Object objCommand) {
        Election2008EmailCommand command = (Election2008EmailCommand) objCommand;

        _log.error("onSubmit");

        ModelAndView mv = new ModelAndView(getSuccessView());

        return mv;
    }


    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
