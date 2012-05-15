package gs.web.about.feedback;


import gs.data.geo.City;
import gs.data.geo.ICounty;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.community.ICaptchaCommand;
import gs.web.util.ReadWriteAnnotationController;

import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.AddEditSchoolOrDistrictCommandValidator;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;

import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: 12/20/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEditSchoolOrDistrictController extends SimpleFormController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());

    private INewEntityQueueDao _newEntityQueueDao;

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    private IGeoDao _geoDao;

    public INewEntityQueueDao getQueueDao() {
        return _newEntityQueueDao;
    }

    public void setQueueDao(INewEntityQueueDao _queueDao) {
        this._newEntityQueueDao = _queueDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    private ISchoolDao _schoolDao;
    // SPRING MVC METHODS

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {

        ICaptchaCommand captchaCommand = (ICaptchaCommand) command;
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

        super.onBindAndValidate(request, command, errors);
        AddEditSchoolOrDistrictCommandValidator validator = new AddEditSchoolOrDistrictCommandValidator();
        validator.validate(request, command, errors);
    }

    @Override
    protected ModelAndView onSubmit(Object o) throws ServletException {
        AddEditSchoolOrDistrictCommand command = (AddEditSchoolOrDistrictCommand)o;




        Map<String,String> model = new HashMap<String,String>();
        //model.put("action",new UrlBuilder(UrlBuilder.ADD_EDIT_SCHOOL_OR_DISTRICT).toString());
        model.put("name",command.getSubmitterName());
        model.put("schoolOrDistrict",command.getSchoolOrDistrict());

        NewEntityQueue newEntityQueue = new NewEntityQueue();
        char[] digits = {0,1,2,3,4,5,6,7,8,9};
        if(command.getSchoolId() != null && StringUtils.containsAny(command.getSchoolId(),digits) && StringUtils.containsOnly(command.getSchoolId(),digits)){
            model.put("schoolId",command.getSchoolId());
            School school = _schoolDao.getSchoolById(command.getState(),new Integer(command.getSchoolId()));
            model.put("name",school.getName());
        }else{
            newEntityQueue.setOriginalId(0);
        }
        //School school = _schoolDao.findSchool(State.fromString(command.getState()),)
        newEntityQueue.setContactName(command.getSubmitterName());
        newEntityQueue.setContactEmail(command.getSubmitterEmail());
        newEntityQueue.setContactConnection(command.getSubmitterConnectionToSchool());
        newEntityQueue.setType(SchoolType.CHARTER);
        newEntityQueue.setName("Test School");
        _newEntityQueueDao.saveNewEntityQueue(newEntityQueue,"web form");


        return new ModelAndView(getSuccessView(), model);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        AddEditSchoolOrDistrictCommand command = (AddEditSchoolOrDistrictCommand)o;
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        Map<String,Object> map = new HashMap<String,Object>();

        List<String> gradesList = new ArrayList();
        gradesList.add("PK");
        gradesList.add("KG");
        gradesList.add("1");
        gradesList.add("2");
        gradesList.add("3");
        gradesList.add("4");
        gradesList.add("5");
        gradesList.add("6");
        gradesList.add("7");
        gradesList.add("8");
        gradesList.add("9");
        gradesList.add("10");
        gradesList.add("11");
        gradesList.add("12");
        map.put("gradesList", gradesList);

        List<String> schoolOrDistrict = new ArrayList();
        schoolOrDistrict.add("school");
        schoolOrDistrict.add("district");
        map.put("schoolOrDistrict", schoolOrDistrict);

        List<String> addEdit = new ArrayList();
        addEdit.add("add");
        addEdit.add("edit");
        map.put("addEdit", addEdit);

        List<String> schoolType = new ArrayList();
        schoolType.add("public");
        schoolType.add("private");
        schoolType.add("charter");
        map.put("schoolType", schoolType);

        captureRequestParameters(request, command, map);

        State stateOrDefault = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        command.setState(stateOrDefault);
        request.setAttribute("state", stateOrDefault);
        request.setAttribute("cityName","");
        request.setAttribute("schoolId","");

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



        //map.put("addEditURL",new UrlBuilder(UrlBuilder.ADD_EDIT_SCHOOL_OR_DISTRICT).toString());


        return map;
    }

    protected void captureRequestParameters(HttpServletRequest request, AddEditSchoolOrDistrictCommand command, Map<String,Object> map) {
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


}
