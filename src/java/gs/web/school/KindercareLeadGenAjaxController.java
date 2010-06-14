package gs.web.school;

import gs.data.school.IKindercareLeadGenDao;
import gs.data.school.ISchoolDao;
import gs.data.school.KindercareLeadGen;
import gs.data.school.School;
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
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
@org.springframework.stereotype.Controller
@RequestMapping("/school/kindercareLeadGenAjax.page")
public class KindercareLeadGenAjaxController implements ReadWriteAnnotationController {
    // QA URL
//    public static final String KINDERCARE_SUBMIT_URL =
//            "http://klc-services-qa.whitehorse.com/klcleadservice/LeadService.asmx/SubmitLead";
    // LIVE URL
    public static final String KINDERCARE_SUBMIT_URL =
            "http://services.knowledgelearning.com/klcleadservice/LeadService.asmx/SubmitLead";
    public static final String SECURITY_KEY = "92p4ojsddf";
    public static final String PARTNER = "GS";
    public static final String PARAM_SECURITY_KEY = "securityKey";
    public static final String PARAM_FIRST_NAME = "firstName";
    public static final String PARAM_LAST_NAME = "lastName";
    public static final String PARAM_EMAIL = "emailAddress";
    public static final String PARAM_PHONE = "phoneNumber";
    public static final String PARAM_ZIP = "zipcode";
    public static final String PARAM_PREFERRED_CONTACT_METHOD = "preferredContactMethod";
    public static final String PARAM_OPT_IN = "kinderCareOptIn";
    public static final String PARAM_PARTNERS_OPT_IN = "kinderCarePartnersOptIn";
    public static final String PARAM_CENTER_ID = "centerID";
    public static final String PARAM_PARTNER = "partner";
    public static final String[] OPTIONAL_PARAMS = {
            "comment",
            "neededMonday",
            "neededTuesday",
            "neededWednesday",
            "neededThursday",
            "neededFriday",
            "timeNeeded",
            "child1BirthDate",
            "child1LeadDate",
            "child1PottyTrained",
            "child2BirthDate",
            "child2LeadDate",
            "child2PottyTrained",
            "child3BirthDate",
            "child3LeadDate",
            "child3PottyTrained",
            "child4BirthDate",
            "child4LeadDate",
            "child4PottyTrained",
            "child5BirthDate",
            "child5LeadDate",
            "child5PottyTrained",
            "child6BirthDate",
            "child6LeadDate",
            "child6PottyTrained"
    };
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String SUCCESS = "1";
    public static final String FAILURE = "0";

    private ISchoolDao _schoolDao;
    private IKindercareLeadGenDao _kindercareLeadGenDao;
    private HttpClient _httpClient;

    @RequestMapping(method = RequestMethod.POST)
    public void generateLead(@ModelAttribute("command") KindercareLeadGenCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());
        // collect data for soap request
        School school = _schoolDao.getSchoolById(command.getState(), command.getSchoolId());

        if (validate(command, school)) {
            // log data
            logData(command, school.getVendorId());

            if (validateSubmitRequest(command,school)) {
                // submit soap request
                submitKindercareLeadGen(request, command.getFirstName(), command.getLastName(), command.getEmail(),
                                        school.getVendorId(), command.isInformed(), command.isOffers());
                _log.info("Lead generated successfully for " + command.getEmail());
            }

            response.getWriter().print(SUCCESS);
        }

        _log.warn("Failure generating lead for " + command.getEmail());
        response.getWriter().print(FAILURE);
    }

    /**
     * Returns true if the command seems valid to submit to Kindercare
     */
    protected boolean validateSubmitRequest(KindercareLeadGenCommand command, School school) {
        // validate not null school
        if (school == null) {
            _log.warn("Lead gen submitted with nonexistent school " + command.getState() + ":" + command.getSchoolId());
            return false;
        }
        
        // validate not null firstname, lastname, email
        if (StringUtils.isBlank(command.getFirstName())
                || StringUtils.isBlank(command.getLastName())
                || StringUtils.isBlank(command.getEmail())) {
            return false;
        }
        // validate format email
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(command.getEmail())) {
            _log.warn("Lead gen submitted with invalid email: " + command.getEmail());
            return false;
        }

        if (StringUtils.isBlank(school.getVendorId()))  {
            _log.warn("Lead gen submitted for school with no Kindercare center id in vendor ID field");
            return false;
        }
        return true;
    }

    /**
     * Returns true if the command seems valid
     */
    protected boolean validate(KindercareLeadGenCommand command, School school) {
        // validate not null school
        if (school == null) {
            _log.warn("Lead gen submitted with nonexistent school " + command.getState() + ":" + command.getSchoolId());
            return false;
        }
        // validate not null school vendor ID
        if (StringUtils.isBlank(school.getVendorId())) {
            _log.warn("Lead gen submitted for school with no Kindercare center id in vendor ID field");
            return false;
        }
        return true;
    }

    private void logData(KindercareLeadGenCommand command, String centerId) {
        KindercareLeadGen leadGen =
                new KindercareLeadGen(command.getSchoolId(), command.getState(), new Date(), command.getFirstName(),
                                      command.getLastName(), command.getEmail(), command.isInformed(),
                                      command.isOffers(), centerId);
        _kindercareLeadGenDao.save(leadGen);
    }

    public void submitKindercareLeadGen(HttpServletRequest request, String firstName, String lastName, String email,
                                        String centerId, boolean kinderCareOptIn, boolean kinderCarePartnersOptIn) {
        if (!UrlUtil.isDevEnvironment(request.getServerName())) {
            HttpClient client = getHttpClient();
            PostMethod method = new PostMethod(KINDERCARE_SUBMIT_URL);
            method.addParameter(PARAM_SECURITY_KEY, SECURITY_KEY);
            method.addParameter(PARAM_FIRST_NAME, firstName);
            method.addParameter(PARAM_LAST_NAME, lastName);
            method.addParameter(PARAM_EMAIL, email);
            method.addParameter(PARAM_PHONE, "");
            method.addParameter(PARAM_ZIP, "");
            method.addParameter(PARAM_PREFERRED_CONTACT_METHOD, "");
            method.addParameter(PARAM_OPT_IN, String.valueOf(kinderCareOptIn));
            method.addParameter(PARAM_PARTNERS_OPT_IN, String.valueOf(kinderCarePartnersOptIn));
            method.addParameter(PARAM_CENTER_ID, centerId);
            method.addParameter(PARAM_PARTNER, PARTNER);

            for (String param: OPTIONAL_PARAMS) {
                method.addParameter(param, "");
            }

            try {
                int statusCode = client.executeMethod(method);
                if (statusCode != -1) {
                    String contents = method.getResponseBodyAsString();
                    method.releaseConnection();
                    if (statusCode != 200) {
                        _log.error("Error posting lead generation to Kindercare: " + contents);
                    }
                }
            } catch (IOException ioe) {
                _log.error("Error posting lead generation to Kindercare: " + ioe, ioe);
            }
        }
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IKindercareLeadGenDao getKindercareLeadGenDao() {
        return _kindercareLeadGenDao;
    }

    public void setKindercareLeadGenDao(IKindercareLeadGenDao kindercareLeadGenDao) {
        _kindercareLeadGenDao = kindercareLeadGenDao;
    }

    // for unit tests
    public HttpClient getHttpClient() {
        if (_httpClient == null) {
            return new HttpClient();
        }
        return _httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }
}
