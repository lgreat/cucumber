package gs.web.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.apache.commons.lang.StringUtils;

import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.data.api.ApiAccountUtils;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.web.util.ReadWriteAnnotationController;

import javax.mail.MessagingException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * This controller handles requests for/from the api account details page.
 *
 * @author chriskimm@greatschools.net
 */
@Controller
@RequestMapping("/api/admin/account.page")
public class AccountController implements ReadWriteAnnotationController {

    public static final String MODEL_MESSAGES = "messages";
    private EmailHelperFactory _emailHelperFactory;
    private static final Logger _log = Logger.getLogger("gs.web.api.admin.AccountController");
    public static final String MAIN_VIEW = "api/admin/account";

    // model data names
    private static final String MODEL_ACCOUNT = "account";
    private static final String MODEL_PREMIUM_OPTIONS = "premium_options";

    @Autowired
    private IApiAccountDao _apiAccountDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showPage(@RequestParam("id") int id, ModelMap model) {
        ApiAccount account = getApiAccountDao().getAccountById(id);
        String options = account.getConfig().get(ApiAccount.AccountConfig.PREMIUM_OPTIONS);
        model.addAttribute(MODEL_ACCOUNT, account);
        model.addAttribute(MODEL_PREMIUM_OPTIONS, ApiAccount.PREMIUM_OPTIONS);
        return MAIN_VIEW;
    }

    @RequestMapping(method=RequestMethod.POST, params = "action=toggle_active")
    public String toggleActive(@RequestParam("id") int id, ModelMap model) {
        ApiAccount account = getApiAccountDao().getAccountById(id);
        if (StringUtils.isBlank(account.getApiKey())) {
            account.setApiKey(ApiAccountUtils.generateAccountKey(account.getName()));
            sendKeyEmail(account);
            setMessageInModel(model, "Api Key Email sent.");
        } else {
            account.setApiKey(null);
        }
        getApiAccountDao().save(account);
        model.addAttribute(MODEL_ACCOUNT, account);
        model.addAttribute(MODEL_PREMIUM_OPTIONS, ApiAccount.PREMIUM_OPTIONS);
        return MAIN_VIEW;
    }



    @RequestMapping(method = RequestMethod.POST, params = "action=update")
    public String update(@ModelAttribute("account") ApiAccount acct,
                         @RequestParam("id") int id,
                         ModelMap model) {

        if (acct != null) {
            ApiAccount account = getApiAccountDao().getAccountById(id);
            account.setType(acct.getType());
            String opts;
            if (acct.getPremiumOptions() != null) {
                opts = StringUtils.join(acct.getPremiumOptions(), ",");
                account.getConfig().set("premium_options", opts);
            }
            getApiAccountDao().save(account);
        }

        setMessageInModel(model, "Account settings updated.");
        model.addAttribute(MODEL_PREMIUM_OPTIONS, ApiAccount.PREMIUM_OPTIONS);
        return MAIN_VIEW;
    }

    void setMessageInModel(ModelMap model, String message) {
        List<String> messages = (List<String>)model.get(MODEL_MESSAGES);
        if (messages == null) {
            messages = new ArrayList<String>();
        }
        messages.add(message);
        model.addAttribute(MODEL_MESSAGES, messages);
    }

    /**
     * Sends an email with the account key to the account email address.
     *
     * @param account an ApiAccount type
     */
    void sendKeyEmail(ApiAccount account) {
        try {
            EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
            emailHelper.setToEmail(account.getEmail());
            emailHelper.setFromEmail("api-support@greatschools.net");
            emailHelper.setFromName("GreatSchools API Support");
            emailHelper.setSubject("GreatSchools Api Key");
            StringBuffer message = new StringBuffer();

            message.append("\nKey: ");
            String value = account.getApiKey() != null ? account.getApiKey() : "";
            message.append(value);

            emailHelper.setTextBody(message.toString());
            emailHelper.send();
        } catch (MessagingException e) {
            _log.warning(e.toString());
        }
    }

    protected IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    protected void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }    
}
