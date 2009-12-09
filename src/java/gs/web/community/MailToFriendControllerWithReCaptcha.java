package gs.web.community;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

public class MailToFriendControllerWithReCaptcha extends MailToFriendController {

    protected void onBind(HttpServletRequest request, Object command, BindException errors){
        super.onBind(request, command, errors);

        ICaptchaCommand captchaCommand = (ICaptchaCommand) command;
        if (errors.getErrorCount() == 0 ) {
            // Validate the captcha request/response pair
            String remoteAddr = request.getRemoteAddr();
            String challenge =  captchaCommand.getChallenge();
            String response = captchaCommand.getResponse();

            ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
            reCaptcha.setPrivateKey("6LdG4wkAAAAAAKXds4kG8m6hkVLiuQE6aT7rZ_C6");
            ReCaptchaResponse reCaptchaResponse =
                reCaptcha.checkAnswer(remoteAddr, challenge, response);

            if (!reCaptchaResponse.isValid()) {
                errors.rejectValue("response", null, "The Captcha response you entered is invalid. Please try again.");
            }
        }
    }
}
