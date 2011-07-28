package gs.web.promo;

import gs.data.promo.ILeadGenDao;
import gs.data.promo.LeadGen;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
@org.springframework.stereotype.Controller
public class LeadGenAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String SUCCESS = "OK";
    public static final String FAILURE = "0";
    private HttpClient _httpClient;

    private final static String ZIP_PATTERN = "^\\d{5}$";

    private ILeadGenDao _leadGenDao;

    //public static final String SUBMIT_URL = "https://www.salesforce.com/servlet/servlet.WebToLead?encoding=UTF-8";
    public static final String SUBMIT_URL = "/promo/leadGenAjax.page";
    public static final String PARAM_FIRST_NAME = "first_name";
    public static final String PARAM_LAST_NAME = "last_name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_ZIP = "zip";

    @RequestMapping(value = "/promo/leadGenAjax.page", method = RequestMethod.POST)
    public void generateLead(@ModelAttribute("command") LeadGenCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());

        String errors = validate(command);
        if (StringUtils.isBlank(errors)) {
            // log data
            logData(command);

            //submit data
            submitLeadGen(request, command.getFirstName(), command.getLastName(), command.getEmail(), command.getZip());
            _log.info("Lead generated successfully for " + command.getEmail());

            response.getWriter().print(SUCCESS);
            return;
        }

        _log.warn("Failure generating lead for " + command.getCampaign() + ": " + command.getEmail());
        response.getWriter().print(errors);
    }

    /**
     * Returns empty string if the command seems valid; otherwise, comma-separated list of fields with errors
     */
    protected String validate(LeadGenCommand command) {
        List<String> errorList = new ArrayList<String>();

        // validate not null firstname, lastname, email
        if (StringUtils.isBlank(command.getFirstName())) {
            errorList.add("firstName");
        }
        if (StringUtils.isBlank(command.getLastName())) {
            errorList.add("lastName");
        }
        if (StringUtils.isBlank(command.getEmail())) {
            errorList.add("email");
        } else {
            // validate format email
            EmailValidator emailValidator = EmailValidator.getInstance();
            if (!emailValidator.isValid(command.getEmail())) {
                _log.warn("Lead gen submitted with invalid email: " + command.getEmail());
                errorList.add("email");
            }
        }
        if (command.getZip() == null || !command.getZip().matches(ZIP_PATTERN)) {
            errorList.add("zip");
        }

        return StringUtils.join(errorList, ',');
    }

    public void submitLeadGen(HttpServletRequest request, String firstName, String lastName, String email,
                                        String zip) {
        if (!UrlUtil.isDevEnvironment(request.getServerName())) {
            HttpClient client = getHttpClient();
            PostMethod method = new PostMethod(SUBMIT_URL);

            method.addParameter(PARAM_FIRST_NAME, firstName);
            method.addParameter(PARAM_LAST_NAME, lastName);
            method.addParameter(PARAM_EMAIL, email);
            method.addParameter(PARAM_ZIP, zip);
            addInStaticData(method);

            try {
                int statusCode = client.executeMethod(method);
                if (statusCode != -1) {
                    String contents = method.getResponseBodyAsString();
                    method.releaseConnection();
                    if (statusCode != 200) {
                        _log.error("Error posting lead generation: " + contents);
                    }
                }
            } catch (IOException ioe) {
                _log.error("Error posting lead generation: " + ioe, ioe);
            }
        }
    }

    private void addInStaticData(PostMethod method) {
        /*
        <input type=hidden name="oid" value="00DJ00000004io2">
        <input type=hidden name="retURL" value="http://">
        <input type=hidden name="Company" id="Company" value="Web Household">
        <input type="hidden" name="Method_of_Contact" id="Method_of_Contact_c" value="Web">
        <input type="hidden" name="Rank" id="Rank_c" value="Warm">
        <input type="hidden" name="LeadSource" id="LeadSource" value="Web">
        <input type="hidden" name="Center_Number_1" id="Center_Number_1__c" value="000000">
        <input type="hidden" name="Contact_Preference" id="Contact_Preference__c" value="Email">
        <input type="hidden" name="owner" id="owner" value="00GA0000000RL9u">
        <input type="hidden" name="Campaign_ID" id="Campaign_ID" value="701J00000000eRs">
         */
        method.addParameter("oid","00DJ00000004io2");
        method.addParameter("retURL","http://");
        method.addParameter("Company","Web Household");
        method.addParameter("Method_of_Contact", "Web");
        method.addParameter("Rank","Warm");
        method.addParameter("LeadSource","Web");
        method.addParameter("Center_Number_1", "000000");
        method.addParameter("Contact_Preference", "Email");
        method.addParameter("owner","00GA0000000RL9u");
        method.addParameter("Campaign_ID", "701J00000000eRs");
    }

    private void addInDebugData(PostMethod method) {
        String email = "";
        //email = "dflagg@klcorp.com";
        
        //to test in debug mode
        method.addParameter("debug", "true");
        method.addParameter("debugEmail", email);
    }

    // for unit tests
    public HttpClient getHttpClient() {
        if (_httpClient == null) {
            return new HttpClient();
        }
        return _httpClient;
    }

    private void logData(LeadGenCommand command) {
        LeadGen leadGen =
                new LeadGen(command.getCampaign(), new Date(), command.getFirstName(),
                                      command.getLastName(), command.getEmail(), command.getZip());
        _leadGenDao.save(leadGen);
    }

    public ILeadGenDao getLeadGenDao() {
        return _leadGenDao;
    }

    public void setLeadGenDao(ILeadGenDao leadGenDao) {
        _leadGenDao = leadGenDao;
    }
}
