package gs.web.community;

import gs.data.community.*;
import gs.data.state.StateManager;
import gs.data.admin.IPropertyDao;
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
public class BetaController extends SimpleFormController {

    public static final String BEAN_ID = "/community/beta.page";

    private JavaMailSender _mailSender;
    private static final Logger _log = Logger.getLogger(BetaController.class);
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    /** Used to find the index of the next shutterfly promo code */
    private IPropertyDao _propertyDao;

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
        String code = getShutterflyCode(command.getEmail());
        emailText = emailText.replaceAll("\\$BETA_PROMO_CODE\\$", code);
        emailText = emailText.replaceAll("\\$EMAIL\\$", command.getEmail());
        emailText = emailText.replaceAll("\\$STATE\\$", command.getState().getAbbreviation());

        StringBuffer logMessage = new StringBuffer("Beta email created using promo code:");
        logMessage.append(code);
        logMessage.append(" to:");
        logMessage.append(command.getEmail());
        _log.info(logMessage.toString());

        helper.setText(emailText, true);
        return helper.getMimeMessage();
    }


    /**
     * This method first checks to see if the user already was sent a code.
     * If she already has a code, then use that one, if not get a brand new
     * code.
     *
     * @return
     */
    String getShutterflyCode(String emailAddress) {
        String code = null;
        if (StringUtils.isNotBlank(emailAddress)) {
            String email = emailAddress.trim();
            code = _propertyDao.getProperty(email);
            if (code == null) {
                code = getShutterflyCode();
                _propertyDao.setProperty(email,  code);
            }
        }
        return code;
    }

    /**
     * Returns the next Shutterfly promotion code as determined by the index
     * stored in the property table.
     * @return a String promo code. see GS-2121
     */
    String getShutterflyCode() {
        // get the index from the property table
        int index = 1;
        String indexProp = _propertyDao.getProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX);
        if (indexProp == null) {
            _propertyDao.setProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX, String.valueOf(index));
        } else {
            index = Integer.parseInt(indexProp);
        }

        // increment the index property now
        _propertyDao.setProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX,
                String.valueOf(index + 1));

        String code = null;
        LineNumberReader lineReader = null;
        try {
            Resource resource = new ClassPathResource("gs/web/community/shutterfly_promo_codes.txt");
            File codeFile = resource.getFile();
            lineReader = new LineNumberReader(new BufferedReader(new FileReader(codeFile)));
            lineReader.setLineNumber(index);
            for (int i = 1; i < index; i++) {
                lineReader.readLine();
            }
            code = lineReader.readLine();
            if (index > 9999) {
                sendShutterflyPromoLimitAlert(index);
            }
        } catch (IOException ioe) {
            _log.error(ioe);
        } catch (MessagingException me) {
            _log.error("Could not send shutterfly alert message.", me);
        } finally {
            try {
                // NPE if lineReader could not be created
                lineReader.close();
            } catch(IOException ioe) {
                _log.error("Can't close shutterfly promo file", ioe);
            }
        }
        return code;
    }

    /**
     * This method sends an email alert indicating that 10000 shutterfly promo
     * codes have been used. Emails are sent to:
     * chriskimm@greatschools.net
     * alingane@greatschools.net
     * mdavis@greatschools.net
     */
    void sendShutterflyPromoLimitAlert(int count) throws MessagingException {
        if (count > 9999) {
            MimeMessage mimeMessage = _mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.addTo("chriskimm@greatschools.net");
            helper.addTo("alingane@greatschools.net");
            helper.addTo("mdavis@greatschools.net");
            helper.setFrom("beta@greatschools.net");
            helper.setSubject("Shutterfly Beta Promo Code limit reached");
            helper.setSentDate(new Date());

            StringBuffer buffer = new StringBuffer();
            buffer.append(count);
            buffer.append(" Shutterfly promotion codes have been sent.\n\n");
            buffer.append("This is an auto generated email sent by the GreatSchools beta system.");
            helper.setText(buffer.toString(), false);
            _mailSender.send(helper.getMimeMessage());
        }
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


    /** Spring setter */
    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}