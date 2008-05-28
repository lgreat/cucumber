package gs.web.community;

import gs.web.community.MailToFriendController;
import gs.web.community.ICaptchaCommand;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: May 27, 2008
 * Time: 4:03:28 PM
 * To change this template use File | Settings | File Templates.
 */
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
            reCaptcha.setPrivateKey("6LfZWAEAAAAAAKt3EpAJngyabjFSywONdA7xqI2C");
            ReCaptchaResponse reCaptchaResponse =
                reCaptcha.checkAnswer(remoteAddr, challenge, response);

            if (!reCaptchaResponse.isValid()) {
                errors.rejectValue("response", null, "The Captcha response you entered is invalid. Please try again.");
            }
        }
    }
}
