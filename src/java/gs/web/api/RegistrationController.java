package gs.web.api;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;

import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import javax.mail.MessagingException;
import java.util.logging.Logger;

/**
 * @author chriskimm@greatschools.net
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {

    private IApiAccountDao _apiAccountDao;
    private EmailHelperFactory _emailHelperFactory;
    private static final Logger _log = Logger.getLogger("gs.web.api.RegistrationController");
    static final String API_REQUEST_EMAIL_ADDRESS = "chriskimm@greatschools.net";

    @Override
    protected ModelAndView onSubmit(Object o) throws Exception {
        ApiAccount command = (ApiAccount)o;
        _apiAccountDao.save(command);
        sendRequestEmail(command);
        return new ModelAndView(getSuccessView());
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
            emailHelper.setFromEmail("api-support@greatschools.net");
            emailHelper.setFromName("GreatSchools API ");
            emailHelper.setSubject("GreatSchools Api Account Request");
            StringBuffer message = new StringBuffer();

            message.append("\nName: ");
            String value = account.getName() != null ? account.getName() : "";
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

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }
}
