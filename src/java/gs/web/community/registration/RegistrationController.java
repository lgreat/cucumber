package gs.web.community.registration;

import gs.data.community.ISubscriptionDao;
import gs.data.community.IUserDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jun 15, 2006
 * Time: 9:21:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationController extends SimpleFormController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private JavaMailSender _mailSender;

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        UserCommand userCommand = (UserCommand) command;

        getUserDao().saveUser(userCommand.getUser());

        try {
            userCommand.getUser().setPlaintextPassword(userCommand.getPassword());
            getUserDao().updateUser(userCommand.getUser());
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage());
            getUserDao().removeUser(userCommand.getUser().getId());
            throw e;
        }

        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("email", userCommand.getEmail());
        return mAndV;
    }

    private SimpleMailMessage buildEmailMessage(UserCommand userCommand) {
        StringBuffer emailContent = new StringBuffer();
        emailContent.append("Welcome to the GreatSchools.net community!\n\n");
        emailContent.append("This email is being sent to confirm a subscription request. ");
        emailContent.append("The request was generated for the email address ");
        emailContent.append(userCommand.getEmail()).append(".\n\n");
        emailContent.append("To confirm this subscription request, please click on the following link:\n");
        emailContent.append("http://localhost:8080/gs-web/community/registrationConfirm.page?email=");
        emailContent.append(userCommand.getEmail());
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

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }


}
