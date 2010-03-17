package gs.web.school;

import gs.data.school.IKindercareLeadGenDao;
import gs.data.school.ISchoolDao;
import gs.data.school.KindercareLeadGen;
import gs.data.school.School;
import gs.data.soap.KindercareLeadGenRequest;
import gs.data.soap.SoapRequestException;
import gs.web.util.UrlUtil;
import gs.web.util.context.SubCookie;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
@org.springframework.stereotype.Controller
@RequestMapping("/school/kindercareLeadGenAjax.page")
public class KindercareLeadGenAjaxController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String SUCCESS = "1";
    public static final String FAILURE = "0";

    private ISchoolDao _schoolDao;
    private IKindercareLeadGenDao _kindercareLeadGenDao;
    private KindercareLeadGenRequest _soapRequest;

    @RequestMapping(method = RequestMethod.POST)
    public String generateLead(@ModelAttribute("command") KindercareLeadGenCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());
        // collect data for soap request
        School school = _schoolDao.getSchoolById(command.getState(), command.getSchoolId());



        if (validate(command, school)) {
            // log data
            logData(command, school.getNotes());

            SubCookie kindercareCookie;

            if (validateSOAPRequest(command,school)) {
                // submit soap request
                submitSOAPRequest(request, command.getFirstName(), command.getLastName(), command.getEmail(),
                              school.getNotes(), command.isInformed(), command.isOffers());

                
            }

           

            _log.info("Lead generated successfully for " + command.getEmail());

            response.getWriter().print(SUCCESS);

            return null;
        }

        _log.warn("Failure generating lead for " + command.getEmail());
        response.getWriter().print(FAILURE);
        return null;
    }

    protected boolean validateSOAPRequest(KindercareLeadGenCommand command, School school) {
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

        if (StringUtils.isBlank(school.getNotes()))  {
            _log.warn("Lead gen submitted for school with no Kindercare center id in notes field");
            return false;
        }
        return true;
    }

    protected boolean validate(KindercareLeadGenCommand command, School school) {

        // validate not null school
        if (school == null) {
            _log.warn("Lead gen submitted with nonexistent school " + command.getState() + ":" + command.getSchoolId());
            return false;
        }
        // validate not null school notes
        if (StringUtils.isBlank(school.getNotes())) {
            _log.warn("Lead gen submitted for school with no Kindercare center id in notes field");
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

    public void submitSOAPRequest(HttpServletRequest request, String firstName, String lastName, String email,
                                  String centerId, boolean kinderCareOptIn, boolean kinderCarePartnersOptIn) {
        KindercareLeadGenRequest soapRequest = getSoapRequest();
        if (UrlUtil.isDevEnvironment(request.getServerName())) {
            soapRequest.setTarget(null);
        }
        try {
            soapRequest.submit(firstName, lastName, email,
                               centerId, kinderCareOptIn, kinderCarePartnersOptIn);
        } catch (SoapRequestException e) {
            _log.error("Error submitting soap request to Kindercare: " + e, e);
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

    // provided for unit tests. Standard execution always instantiates a new one
    public KindercareLeadGenRequest getSoapRequest() {
        if (_soapRequest == null) {
            return new KindercareLeadGenRequest();
        }
        return _soapRequest;
    }

    public void setSoapRequest(KindercareLeadGenRequest soapRequest) {
        _soapRequest = soapRequest;
    }
}
