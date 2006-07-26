package gs.web.community;

import gs.data.community.*;
import gs.data.state.StateManager;
import gs.web.util.ReadWriteController;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Date;

/**
 * This controller handles requests to subscribe to the Beta group
 * through beta.page.  Only subscribes are handled by this controller.
 * Unsubscribes are handled by BetaUnsubscribeController.
 * Email addresss are validated in the command object before being
 * passed to the onSubmit(...) method.   *
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaController extends SimpleFormController implements ReadWriteController {

    public static final String BEAN_ID = "/community/beta.page";

    private JavaMailSender _mailSender;
    private static final Logger _log = Logger.getLogger(BetaController.class);
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    /** Form param: an email address */
    private static final String EMAIL_PARAM = "email";

    /** Form param: a 2-letter State abbreviation */
    private static final String STATE_PARAM = "state";

    /**
     * Binds the request parameters to the fields in <code>BetaSignupCommand</code>.
     * @param httpServletRequest
     * @return an Object of type BetaSignupCommand
     * @throws Exception
     */
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {

        BetaSignupCommand command = new BetaSignupCommand();
        String paramEmail = httpServletRequest.getParameter(EMAIL_PARAM);
        command.setEmail(paramEmail);

        String paramState = httpServletRequest.getParameter(STATE_PARAM);
        StateManager sm = new StateManager();
        if (!StringUtils.isBlank(paramState)) {
            command.setState(sm.getState(paramState));
        }

        return command;
    }

    /**
     * Handles the form POST subscription request
     *
     * @param command a <code>BetaSignupCommand</code> object
     * @return a ModelAndView
     * @see gs.web.util.validator.EmailValidator
     * @see BetaSubNotExistsValidator
     */
    public ModelAndView onSubmit(Object command) {

        BetaSignupCommand bsc = (BetaSignupCommand) command;
        boolean isNewUser = addToBetaGroup(bsc);

        try {
            _mailSender.send(createMessage(_mailSender.createMimeMessage(), bsc));
        } catch (MessagingException mess) {
            _log.warn(mess);
        } catch (MailException me) {
            _log.warn(me);
        }

        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(getSuccessView());
        // add email to the model so it can be used by the unsubscribe link
        mAndV.getModel().put("email", bsc.getEmail());

        // used for omniture conversion event tracking
        mAndV.getModel().put("events", isNewUser ? "event4" : "");
        return mAndV;
    }

    /**
     * This method adds the provided email address to the beta email list.  Validation
     * of the email address and user/subscription validation (such as: if the
     * subscription already exists) are done in the EmailValidator.
     *
     * @param command A valid email address as a <code>String</code> type
     */
    private boolean addToBetaGroup(BetaSignupCommand command) {
        boolean isNewsUser = false;
        User user = _userDao.findUserFromEmailIfExists(command.getEmail());
        if (user == null) {
            user = new User();
            user.setEmail(command.getEmail());
            _userDao.saveUser(user);
            isNewsUser = true;
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setState(command.getState());
        subscription.setProduct(SubscriptionProduct.BETA_GROUP);
        _subscriptionDao.saveSubscription(subscription);
        return isNewsUser;
    }

    /**
     * This is a utility method to created the <code>MimeMessage</code object from a stub
     * MimeMessage.
     *
     * @param mimeMessage
     * @return
     * @throws MessagingException
     */
    MimeMessage createMessage(MimeMessage mimeMessage, BetaSignupCommand command) throws MessagingException {

        Resource resource = new ClassPathResource("gs/web/community/betaConfirmationEmail.txt");
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(command.getEmail());
        try {
            helper.setFrom("beta@greatschools.net", "GreatSchools");
        } catch (UnsupportedEncodingException uee) {
            helper.setFrom("beta@greatschools.net");
        }
        helper.setSubject("Welcome to the GreatSchools Beta Group!");
        helper.setSentDate(new Date());

        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

        } catch (IOException ioe) {
            _log.error(ioe);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                _log.error(e);
            }
        }
        String emailText = buffer.toString();
        emailText = emailText.replaceAll("\\$EMAIL\\$", command.getEmail());
        emailText = emailText.replaceAll("\\$STATE\\$", command.getState().getAbbreviation());

         helper.setText(emailText, true);
        return helper.getMimeMessage();
    }

     /**
     * Spring setter
     *
     * @param _mailSender
     */
    public void setMailSender(JavaMailSender _mailSender) {
        this._mailSender = _mailSender;
    }

    /**
     * Spring setter.
     *
     * @param _userDao
     */
    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    /**
     * Spring setter
     *
     * @param subscriptionDao
     */
    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        this._subscriptionDao = subscriptionDao;
    }
}