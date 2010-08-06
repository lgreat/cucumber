package gs.web.about.feedback;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.IPQDao;
import gs.data.school.ISchoolDao;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.community.ICaptchaCommand;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author <a href="droy@greatschools.org">Dave Roy</a>
 */
public class ContactUsController extends SimpleFormController {
    public static final String CONFIRMATION_PARAM = "confirm";
    public static final String SHOW_CONFIRMATION_MODEL = "showConfirmMessage";
    protected static final String SUPPORT_EMAIL = "gs_support@greatschools.org";

    private JavaMailSender _mailSender;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IPQDao _pqDao;
    
    private final Log _log = LogFactory.getLog(getClass());

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        ContactUsCommand command = (ContactUsCommand)commandObj;

        School school = null;
        if (!StringUtils.isBlank(command.getSchoolId())) {
            try {
                Integer schoolId = new Integer(command.getSchoolId());
                school = _schoolDao.getSchoolById(command.getState(), schoolId);
                if (school != null) {
                    command.setSchoolName(school.getName());
                }
            } catch (Exception e) {
                // Don't set school name, but proceed
            }
            
        }
        sendSupportEmail(command, request, school);

        return new ModelAndView(new RedirectView("/about/feedback.page?" + CONFIRMATION_PARAM + "=true"));
    }

    @Override
    protected void onBindAndValidate(HttpServletRequest request, java.lang.Object objCommand, BindException errors) {
        ICaptchaCommand captchaCommand = (ICaptchaCommand) objCommand;
        //if (errors.getErrorCount() == 0 ) {
            // Validate the captcha request/response pair
            String remoteAddr = request.getRemoteAddr();
            String challenge =  captchaCommand.getChallenge();
            String response = captchaCommand.getResponse();

            ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
            reCaptcha.setPrivateKey("6LdG4wkAAAAAAKXds4kG8m6hkVLiuQE6aT7rZ_C6");
            ReCaptchaResponse reCaptchaResponse =
                reCaptcha.checkAnswer(remoteAddr, challenge, response);

            if (!reCaptchaResponse.isValid()) {
                errors.rejectValue("response", null, "The Captcha response you entered is invalid. Please try again.");
            }
        //}
    }

    @Override
    protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception {
        ContactUsCommand command = (ContactUsCommand)commandObj;
        Map<String,Object> map = new HashMap<String,Object>();

        captureRequestParameters(request, command, map);
        
        State stateOrDefault = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        command.setState(stateOrDefault);

        // Populate state options
        List<FormOption> allStateOptions = new ArrayList<FormOption>();
        for (State state : StateManager.getList()) {
            allStateOptions.add(new FormOption(state.getAbbreviation(), state.getAbbreviation()));
        }
        map.put("allStateOptions", allStateOptions);

        // Populate city options
        List<City>cities = _geoDao.findCitiesByState(stateOrDefault);
        List<FormOption> cityOptions = new ArrayList<FormOption>();
        for (City city : cities) {
            cityOptions.add(new FormOption(city.getName(), city.getName()));
        }
        map.put("cityOptions", cityOptions);

        // Populate school options if a city was chosen
        List<FormOption> schoolOptions = new ArrayList<FormOption>();
        if (!StringUtils.isBlank(command.getCityName())) {
            List<School> schools = _schoolDao.findSchoolsInCity(stateOrDefault, command.getCityName(), false);
            for (School school : schools) {
                schoolOptions.add(new FormOption(StringEscapeUtils.escapeHtml(school.getName()), school.getId().toString()));
            }
        }
        map.put("schoolOptions", schoolOptions);

        map.put("incorrectSchoolInfoTypeOptions", getIncorrectSchoolInfoTypeOptions());

        map.put(SHOW_CONFIRMATION_MODEL, StringUtils.equals(request.getParameter(CONFIRMATION_PARAM), "true"));

        return map;
    }

    protected List<FormOption> getIncorrectSchoolInfoTypeOptions() {
        List<FormOption> incorrectSchoolInfoTypeOptions = new ArrayList<FormOption>();
        incorrectSchoolInfoTypeOptions.add(new FormOption("School name, contact info, website, or grade range", "school_name"));
        incorrectSchoolInfoTypeOptions.add(new FormOption("Test scores", "test_scores"));
        incorrectSchoolInfoTypeOptions.add(new FormOption("Teacher or student statistics", "teachers_students"));
        incorrectSchoolInfoTypeOptions.add(new FormOption("GreatSchools Rating", "greatschools_rating"));
        incorrectSchoolInfoTypeOptions.add(new FormOption("Enhanced School Profile", "esp"));
        incorrectSchoolInfoTypeOptions.add(new FormOption("Closed school", "closed_school"));

        return incorrectSchoolInfoTypeOptions;
    }

    protected void captureRequestParameters(HttpServletRequest request, ContactUsCommand command, Map<String,Object> map) {
        if (!StringUtils.isBlank(request.getParameter("feedbackType"))) {
            command.setFeedbackType(ContactUsCommand.FeedbackType.valueOf(request.getParameter("feedbackType")));
            if (!StringUtils.isBlank(request.getParameter("city"))) {
                command.setCityName(request.getParameter("city"));
            }
            if (!StringUtils.isBlank(request.getParameter("schoolId"))) {
                command.setSchoolId(request.getParameter("schoolId"));
            }
        }
    }

    protected void sendSupportEmail(ContactUsCommand command, HttpServletRequest request, School school) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(SUPPORT_EMAIL);
        message.setFrom(command.getSubmitterEmail());
        message.setSentDate(new Date());

        switch (command.getFeedbackType()) {
            case incorrectSchoolDistrictInfo_incorrectSchool:
                customizeIncorrectSchoolInfoEmail(command, message);
                break;
            case incorrectSchoolDistrictInfo_incorrectDistrict:
                customizeIncorrectDistrictInfoEmail(command, message);
                break;
            case schoolRatingsReviews:
                customizeSchoolRatingsReviewsEmail(command, message);
                break;
            case esp:
                customizeEspEmail(command, message, request, school);
                break;
            case join:
                customizeJoinEmail(command, message);
                break;
            default:
                throw new RuntimeException ("Invalid feedback type in ContactUsCommand: " + command.getFeedbackType());

        }

        try {
            _mailSender.send(message);
        }
        catch (MailException me) {
            _log.error("Mailer failed to send feedback", me);
        }
    }

    protected void customizeIncorrectSchoolInfoEmail(ContactUsCommand command, SimpleMailMessage message) {
        StringBuffer body = new StringBuffer();

        message.setSubject("Incorrect school information");
        SchoolInfoFields fields = command.getSchoolInfoFields();
        body.append("Submitter name: ").append(command.getSubmitterName()).append("\n");
        body.append("Submitter email: ").append(command.getSubmitterEmail()).append("\n");
        body.append("School: ").append(command.getSchoolId()).append(" - ").append(command.getSchoolName()).append("\n");
        body.append("City name: " ).append(command.getCityName()).append("\n");
        body.append("State: " ).append(command.getState()).append("\n");
        body.append("Info type: " ).append(fields.getInfoType()).append("\n");
        body.append("Reference url: " ).append(fields.getReferenceUrl()).append("\n");
        body.append("Relationship to school: " ).append(fields.getRelationship()).append("\n");
        body.append("Question/comment: " ).append(fields.getComment()).append("\n");

        message.setText(body.toString());
    }

    protected void customizeIncorrectDistrictInfoEmail(ContactUsCommand command, SimpleMailMessage message) {
        StringBuffer body = new StringBuffer();

        message.setSubject("Incorrect district information");
        DistrictInfoFields fields = command.getDistrictInfoFields();
        body.append("Submitter name: ").append(command.getSubmitterName()).append("\n");
        body.append("Submitter email: ").append(command.getSubmitterEmail()).append("\n");
        body.append("District name: ").append(fields.getDistrictName()).append("\n");
        body.append("State: " ).append(command.getState()).append("\n");
        body.append("Reference url: " ).append(fields.getReferenceUrl()).append("\n");
        body.append("Relationship to school: " ).append(fields.getRelationship()).append("\n");
        body.append("Question/comment: " ).append(fields.getComment()).append("\n");

        message.setText(body.toString());
    }

    protected void customizeSchoolRatingsReviewsEmail(ContactUsCommand command, SimpleMailMessage message) {
        StringBuffer body = new StringBuffer();

        message.setSubject("School ratings and review feedback");
        JoinFields fields = command.getSchoolRatingsReviewsFields();
        body.append("Submitter name: ").append(command.getSubmitterName()).append("\n");
        body.append("Submitter email: ").append(command.getSubmitterEmail()).append("\n");
        body.append("School: ").append(command.getSchoolId()).append(" - ").append(command.getSchoolName()).append("\n");
        body.append("City name: " ).append(command.getCityName()).append("\n");
        body.append("State: " ).append(command.getState()).append("\n");
        body.append("Question/comment: " ).append(fields.getComment()).append("\n");

        message.setText(body.toString());
    }

    protected void customizeEspEmail(ContactUsCommand command, SimpleMailMessage message, HttpServletRequest request, School school) {
        StringBuffer body = new StringBuffer();

        message.setSubject("Enhanced School Profile help");
        EspFields fields = command.getEspFields();
        body.append("Title: ").append(fields.getTitle()).append("\n");
        body.append("Submitter name: ").append(command.getSubmitterName()).append("\n");
        body.append("Submitter email: ").append(command.getSubmitterEmail()).append("\n");
        body.append("School: ").append(command.getSchoolId()).append(" - ").append(command.getSchoolName()).append("\n");
        body.append("City name: " ).append(command.getCityName()).append("\n");
        body.append("State: " ).append(command.getState()).append("\n");
        body.append("Phone: " ).append(fields.getPhone()).append("\n");
        body.append("Question/comment: " ).append(fields.getComment()).append("\n");

        if (school != null && request != null) {
            PQ pq = _pqDao.findBySchool(school);

            if (pq != null) {
                UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP_LOGIN);
                String href = urlBuilder.asFullUrlXml(request);

                body.append("ESP Name: ").append(pq.getContactName()).append("\n");
                body.append("ESP Email: ").append(pq.getContactEmail()).append("\n");
                body.append("ESP Username: ").append(pq.getUserName()).append("\n");
                body.append("ESP Password: ").append(pq.getPassword()).append("\n");
                body.append("ESP Start link: ").append(href).append("\n");
            }
        }

        message.setText(body.toString());
    }

    protected void customizeJoinEmail(ContactUsCommand command, SimpleMailMessage message) {
        StringBuffer body = new StringBuffer();

        message.setSubject("Trouble joining or signing in");
        JoinFields fields = command.getJoinFields();
        body.append("Submitter name: ").append(command.getSubmitterName()).append("\n");
        body.append("Submitter email: ").append(command.getSubmitterEmail()).append("\n");
        body.append("Question/comment: " ).append(fields.getComment()).append("\n");

        message.setText(body.toString());
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    class FormOption {
        String _name;
        String _value;

        public FormOption(String name, String value) {
            _name = name;
            _value = value;
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public String getValue() {
            return _value;
        }

        public void setValue(String value) {
            _value = value;
        }

        public String toString() {
            if (_name != null && _value != null) {
                return "FormOption: " + _name + " => " + _value;
            }

            return "FormOption: name or value is null";
        }
    }

    public IPQDao getPQDao() {
        return _pqDao;
    }

    public void setPQDao(IPQDao pqDao) {
        this._pqDao = pqDao;
    }
}