package gs.web.api;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;

import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import javax.mail.MessagingException;
import java.util.logging.Logger;

/**
 * @author chriskimm@greatschools.org
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {

    private IApiAccountDao _apiAccountDao;
    private EmailHelperFactory _emailHelperFactory;
    private static final Logger _log = Logger.getLogger("gs.web.api.RegistrationController");
    static final String API_REQUEST_EMAIL_ADDRESS = "api-request@greatschools.org";
    static final String API_WELCOME_EMAIL_TRIGGER_KEY = "apiWelcomeMessage";
    private ExactTargetAPI _exactTargetAPI;

    @Override
    protected ModelAndView onSubmit(Object o) throws Exception {
        ApiAccount command = (ApiAccount)o;
        _apiAccountDao.save(command);
        sendRequestEmail(command);
        ModelAndView successView = new ModelAndView(getSuccessView());
        triggerWelcomeMessageEmail(command.getEmail());
        successView.getModel().put("registration","success");
        return successView;
    }

    public IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    public void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }

    void sendRequestEmail(ApiAccount account) {
        try {
            EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
            emailHelper.setToEmail(API_REQUEST_EMAIL_ADDRESS);
            emailHelper.setFromEmail(API_REQUEST_EMAIL_ADDRESS);
            emailHelper.setFromName("GreatSchools API ");
            emailHelper.setSubject("GreatSchools Api Account Request");
            StringBuffer message = new StringBuffer();

            message.append("\nName: ");
            String value = account.getName() != null ? account.getName() : "";
            message.append(value);

            message.append("\nWebsite: ");
            value = account.getWebsite() != null ? account.getWebsite() : "";
            message.append(value);

            message.append("\nIndustry: ");
            value = account.getIndustry() != null ? account.getIndustry() : "";
            message.append(value);

            message.append("\nEmail: ");
            value = account.getEmail() != null ? account.getEmail() : "";
            message.append(value);

            message.append("\nOrganization: ");
            value = account.getOrganization() != null ? account.getOrganization() : "";
            message.append(value);

            message.append("\nIntended Use: ");
            value = account.getIntendedUse() != null ? account.getIntendedUse() : "";
            message.append(value);

            emailHelper.setTextBody(message.toString());
            emailHelper.send();
        } catch (MessagingException e) {
            _log.warning(e.toString());
        }
    }

    void triggerWelcomeMessageEmail(String email){
        // the sendTriggeredEmail takes an User object which is used to extract
        // the user's email address.Therefore I am faking a user.
        User user = new User();
        user.setEmail(email);
        _exactTargetAPI.sendTriggeredEmail(API_WELCOME_EMAIL_TRIGGER_KEY,user);
    }

    public void setExactTargetAPI(ExactTargetAPI etAPI) {
        _exactTargetAPI = etAPI;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }
}
