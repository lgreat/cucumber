package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.apache.log4j.Logger;
import gs.data.community.*;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import java.util.Date;
import java.io.UnsupportedEncodingException;

/**
 * This controller handles requests to subscribe to the Beta group
 * through beta.page.  Only subscribes are handled by this controller.
 * Unsubscribes are handled by BetaUnsubscribeController.
 * Email addresss are validated in the command object before being
 * passed to the onSubmit(...) method.   *
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaController extends SimpleFormController {

    public static final String BEAN_ID = "/community/beta.page";

    private JavaMailSender _mailSender;
    private static final Logger _log = Logger.getLogger(BetaController.class);
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    /**
     * Handles the form POST subscription request
     *
     * @see EmailValidator
     * @see BetaSubNotExistsValidator
     * @param command a <code>BetaEmailCommand</code> object
     * @return
     */
    public ModelAndView onSubmit(Object command) {

        BetaEmailCommand bsc = (BetaEmailCommand)command;
        String email = bsc.getEmail();
        addToBetaGroup(email);
        try {
             _mailSender.send(createMessage(_mailSender.createMimeMessage(), email));
        } catch (MessagingException mess) {
            _log.warn(mess);
        } catch (MailException me) {
            _log.warn(me);
        }

        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(getSuccessView());
        // add email to the model so it can be used by the unsubscribe link
        mAndV.getModel().put("email", email);

        return mAndV;
    }

    /**
     * This method adds the provided email address to the beta email list.  Validation
     * of the email address and user/subscription validation (such as: if the
     * subscription already exists) are done in the EmailValidator.
     * @param email A valid email address as a <code>String</code> type
     */
    private void addToBetaGroup(String email) {
        User user = _userDao.getUserFromEmailIfExists(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
             _userDao.saveUser(user);
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.BETA_GROUP);
        _subscriptionDao.saveSubscription(subscription);
    }

    private static MimeMessage createMessage(MimeMessage mimeMessage, String email) throws MessagingException {

        StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.append("Thanks for joining the GreatSchools Beta Group! Over the next year, we'll be launching<br/>");
        messageBuffer.append("new features on GreatSchools, including powerful community features, exclusive school<br/>");
        messageBuffer.append("ratings, and more!<br/><br/>");
        messageBuffer.append("As a member of our beta group, you'll have the opportunity to take these new features<br/>");
        messageBuffer.append("on a test drive - and tell us what you think about them.<br/>");
        messageBuffer.append("Here's how it works:");
        messageBuffer.append("<ul><li>We'll notify you occasionally about new features before they're released.</li>");
        messageBuffer.append("<li>If you decide to try them, we may ask you to fill out a short survey or provide us<br/>");
        messageBuffer.append("with feedback about your experience after you're done.</ul>");
        messageBuffer.append("You'll have the opportunity to decide before each beta test whether or not you'd<br/>");
        messageBuffer.append("like to participate.");
        messageBuffer.append("<br/><br/>Thanks so much for your help!  Together, we can build great tools for parents!");
        messageBuffer.append("<br/><br/>- The Team at GreatSchools");
        messageBuffer.append("<br/><br/>********<br/><br/>");
        messageBuffer.append("GreatSchools is an independant nonprofit organization that has been improving K-12<br/> ");
        messageBuffer.append("education since 1998 by inspiring parents like you to get involved.  Learn more at<br/>");
        messageBuffer.append("<a href=\"http://www.greatschools.net/?cpn=beta\">GreatSchools.net</a>");
        messageBuffer.append("<br/><br/><a href=\"http://www.greatschools.net/community/betaUnsubscribe.page?email=");
        messageBuffer.append(email);
        messageBuffer.append("\">Please click here to unsubscribe</a>");

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(email);
        try {
            helper.setFrom("beta@greatschools.net", "GreatSchools.net");
        } catch (UnsupportedEncodingException uee) {
            helper.setFrom("beta@greatschools.net");
        }
        helper.setSubject("Welcome to the GreatSchools Beta Group!");
        helper.setSentDate(new Date());
        helper.setText(messageBuffer.toString(), true);
        return helper.getMimeMessage();
    }

    /**
     * Spring setter
     * @param _mailSender
     */
    public void setMailSender(JavaMailSender _mailSender) {
        this._mailSender = _mailSender;
    }

    /**
     * Spring setter.
     * @param _userDao
     */
    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    /**
     * Spring setter
     * @param _subscriptionDao
     */
    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        this._subscriptionDao = subscriptionDao;
    }
}