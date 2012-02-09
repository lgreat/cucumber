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

    public static final String CAMPAIGN_KINDERCARE_2011 = "kindercare082011";
    public static final String CAMPAIGN_PRIMROSE_2012 = "primrose032012";
    public static final String TARGET_URL = "https://www.salesforce.com/servlet/servlet.WebToLead?encoding=UTF-8";
    public static final String PARAM_FIRST_NAME = "first_name";
    public static final String PARAM_LAST_NAME = "last_name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_ZIP = "zip";

    public static final String DEBUG_EMAIL = "ssprouse@greatschools.org";

    @RequestMapping(value = "/promo/leadGenAjax.page", method = RequestMethod.POST)
    public void generateLead(@ModelAttribute("command") LeadGenCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());

        String errors = validate(command);
        if (StringUtils.isBlank(errors)) {
            // log data
            logData(command);

            //submit data
            if (CAMPAIGN_KINDERCARE_2011.equals(command.getCampaign())) {
                submitLeadGen(request, command.getFirstName(), command.getLastName(), command.getEmail(), command.getZip());
                _log.info("Lead generated successfully for " + command.getEmail());
            }

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
        // validate not null/blank zip matching pattern
        if (command.getZip() == null || !command.getZip().matches(ZIP_PATTERN)) {
            errorList.add("zip");
        }
        // validate not null/blank child's age
        if (CAMPAIGN_PRIMROSE_2012.equals(command.getCampaign())) {
            if (StringUtils.isBlank(command.getChildsAge())) {
                errorList.add("childsAge");
            }
        }

        return StringUtils.join(errorList, ',');
    }

    // GS-11938 For Kindercare 2011 lead gen widget only
    public void submitLeadGen(HttpServletRequest request, String firstName, String lastName, String email, String zip) {

        HttpClient client = getHttpClient();
        PostMethod method = new PostMethod(TARGET_URL);

        method.addParameter(PARAM_FIRST_NAME, firstName);
        method.addParameter(PARAM_LAST_NAME, lastName);
        method.addParameter(PARAM_EMAIL, email);
        method.addParameter(PARAM_ZIP, zip);
        addInStaticData(method);

        if (UrlUtil.isDevEnvironment(request.getServerName())) {
            addInDebugData(method);
        }

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

    private void addInStaticData(PostMethod method) {
        method.addParameter("oid","00DA0000000IMJc");
        method.addParameter("retURL","http://");
        method.addParameter("Company","Web Household");
        method.addParameter("Method_of_Contact__c", "Web");
        method.addParameter("Rank__c","Warm");
        method.addParameter("LeadSource","Web");
        method.addParameter("Center_Number_1__c", "000000");
        method.addParameter("Contact_Preference__c", "Email");
        method.addParameter("owner","00GA0000000RL9u");
        method.addParameter("Campaign_ID", "701G0000000YWlz");
    }

    // GS-11938 For Kindercare 2011 lead gen widget only
    private void addInDebugData(PostMethod method) {
        //email = "dflagg@klcorp.com";
        
        //to test in debug mode
        method.addParameter("debug", "1");
        method.addParameter("debugEmail", DEBUG_EMAIL);
    }

    // for unit tests
    // GS-11938 For Kindercare 2011 lead gen widget only
    public HttpClient getHttpClient() {
        if (_httpClient == null) {
            return new HttpClient();
        }
        return _httpClient;
    }

    private void logData(LeadGenCommand command) {
        LeadGen leadGen =
                new LeadGen(command.getCampaign(), new Date(), command.getFirstName(),
                            command.getLastName(), command.getEmail(), command.getZip(), command.getChildsAge());
        _leadGenDao.save(leadGen);
    }

    public ILeadGenDao getLeadGenDao() {
        return _leadGenDao;
    }

    public void setLeadGenDao(ILeadGenDao leadGenDao) {
        _leadGenDao = leadGenDao;
    }
}
