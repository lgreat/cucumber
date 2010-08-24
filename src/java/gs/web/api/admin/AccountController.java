package gs.web.api.admin;

import gs.web.api.ApiAccountCommandValidator;
import gs.web.util.UrlBuilder;
import org.springframework.validation.BindingResult;
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
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * This controller handles requests for/from the api account details page.
 *
 * @author chriskimm@greatschools.org
 */
@Controller
@RequestMapping(value={"/api/admin/account.page","/api/admin/createAccount.page"})
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

    @Autowired
    private ApiAccountCommandValidator _apiAccountValidator;

    @RequestMapping(method = RequestMethod.GET)
    public String createAccount(ModelMap model) {
        model.addAttribute(MODEL_ACCOUNT, new ApiAccount());
        model.addAttribute(MODEL_PREMIUM_OPTIONS, ApiAccount.PREMIUM_OPTIONS);
        return MAIN_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET, params = "id")
    public String viewEditAccount(@RequestParam("id") int id, ModelMap model) {
        ApiAccount account = getApiAccountDao().getAccountById(id);
        model.addAttribute(MODEL_ACCOUNT, account);
        model.addAttribute(MODEL_PREMIUM_OPTIONS, ApiAccount.PREMIUM_OPTIONS);
        return MAIN_VIEW;
    }

    @RequestMapping(method=RequestMethod.POST, params = "action=toggle_active")
    public String toggleActive(HttpServletRequest request, @RequestParam("id") int id, ModelMap model) {
        ApiAccount account = getApiAccountDao().getAccountById(id);
        if (StringUtils.isBlank(account.getApiKey())) {
            account.setApiKey(ApiAccountUtils.generateAccountKey(account.getName()));
            account.setKeyGenerated(new Date());
            sendKeyEmail(request, account);
            setMessageInModel(model, "Api Key Email sent.");
        } else {
            account.setApiKey(null);
            account.setKeyGenerated(null);
        }
        getApiAccountDao().save(account);
        model.addAttribute(MODEL_ACCOUNT, account);
        model.addAttribute(MODEL_PREMIUM_OPTIONS, ApiAccount.PREMIUM_OPTIONS);
        return MAIN_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST, params = "action=create")
    public String create(@ModelAttribute("account") ApiAccount account, BindingResult result,
                         ModelMap model) {
        return createUpdateHelper(account, result, model);
    }

    @RequestMapping(method = RequestMethod.POST, params = "action=update")
    public String update(@ModelAttribute("account") ApiAccount acct, BindingResult result,
                         @RequestParam("id") int id,
                         ModelMap model) {

        ApiAccount account = getApiAccountDao().getAccountById(id);

        account.setName(acct.getName());
        account.setOrganization(acct.getOrganization());
        account.setEmail(acct.getEmail());        
        account.setWebsite(acct.getWebsite());
        account.setPhone(acct.getPhone());
        account.setIndustry(acct.getIndustry());
        account.setIntendedUse(acct.getIntendedUse());

        String type = acct.getType();
        account.setType(type);
        String opts;
        if ("f".equals(type)) {
            account.getConfig().set(ApiAccount.AccountConfig.PREMIUM_OPTIONS, "");            
        } else if (acct.getPremiumOptions() != null) {
            opts = StringUtils.join(acct.getPremiumOptions(), ",");
            account.getConfig().set(ApiAccount.AccountConfig.PREMIUM_OPTIONS, opts);
        }

        return createUpdateHelper(account, result, model);
    }

    private String createUpdateHelper(ApiAccount account, BindingResult result, ModelMap model) {
        // http://wheelersoftware.com/articles/spring-bean-validation-framework.html
        account.setConfirmEmail(account.getEmail());
        account.setTermsApproved(true);
        _apiAccountValidator.validate(account, result);

        if (result.hasErrors()) {
            setMessageInModel(model, "Please correct errors and re-submit the form.");
        } else {
            if (account.getId() != null) {
                setMessageInModel(model, "Account settings updated.");
                account.setAccountUpdated(new Date());
            } else {
                setMessageInModel(model, "Account created.");
                account.setAccountAdded(new Date());
            }
            getApiAccountDao().save(account);
        }

        model.addAttribute(MODEL_ACCOUNT, account);
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
    void sendKeyEmail(HttpServletRequest request, ApiAccount account) {
        try {
            EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
            emailHelper.enableHtmlWrapper();
            emailHelper.setSentToCustomMessage("");
            emailHelper.setToEmail(account.getEmail());
            emailHelper.setFromEmail("api-support@greatschools.org");
            emailHelper.setFromName("GreatSchools API Support");
            emailHelper.setSubject("Your GreatSchools API key is enclosed!");

            StringBuffer message = new StringBuffer();
            message.append("Thanks for your interest in the GreatSchools API. ");
            String value = account.getApiKey() != null ? account.getApiKey() : "";
            message.append("Your request has been approved and your API key is: ").append(value);
            message.append("<br/><br/>");
            message.append("For instructions on getting started, please ");
            UrlBuilder docsMainLink = new UrlBuilder(UrlBuilder.API_DOCS_MAIN, null);
            message.append(docsMainLink.asAbsoluteAnchor(request, "click here").asATag()).append(". ");
            message.append("Please keep in mind that our API has a 1,500 call limit per day. ");
            message.append("Should you make more than 1,500 calls in a day, you will be charged per the overage schedule below:");
            message.append("<br/><br/>");
            message.append("1,501-3,000 calls: $0.08/call").append("<br/>");
            message.append("3,001-4,500 calls: $0.05/call").append("<br/>");
            message.append("4,501-6,000 calls: $0.03/call").append("<br/>");
            message.append(">6,000 calls: You will be contacted by a GreatSchools representative directly.").append("<br/>");
            message.append("<br/>");
            message.append("Please save this email for future reference. ");
            message.append("If you have any questions or feedback on the API, email: ");
            String supportEmail = "API-support@greatschools.org";
            message.append("<a href=\"mailto:").append(supportEmail).append("\">").append(supportEmail).append("</a>.");
            message.append("<br/><br/>");
            message.append("Thank you,<br/>");
            message.append("The GreatSchools Team");

            emailHelper.setHtmlBody(message.toString());
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

    public ApiAccountCommandValidator getApiAccountValidator() {
        return _apiAccountValidator;
    }

    public void setApiAccountValidator(ApiAccountCommandValidator apiAccountValidator) {
        _apiAccountValidator = apiAccountValidator;
    }
}
