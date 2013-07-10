package gs.web.about.feedback;


import gs.data.geo.City;
import gs.data.geo.County;
import gs.data.geo.ICounty;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.community.ICaptchaCommand;
import gs.web.jsp.about.feedback.PreschoolSubtypeSelectorTagHandler;
import gs.web.util.ReadWriteAnnotationController;

import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.AddEditSchoolOrDistrictCommandValidator;
import gs.web.util.validator.AddFeedbackCommandValidator;
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
import java.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: 12/20/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddFeedbackController extends SimpleFormController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());

    private INewEntityQueueDao _newEntityQueueDao;

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    private IGeoDao _geoDao;

    public INewEntityQueueDao getNewEntityQueueDao() {
        return _newEntityQueueDao;
    }

    public void setNewEntityQueueDao(INewEntityQueueDao _newEntityQueueDao) {
        this._newEntityQueueDao = _newEntityQueueDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    private ISchoolDao _schoolDao;

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    private IDistrictDao _districtDao;
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

        _log.warn("step 1");

        //play around and see what can be fed back to the page if it doesnt make it to onSubmit

        super.onBindAndValidate(request, command, errors);
        AddFeedbackCommandValidator validator = new AddFeedbackCommandValidator();
        validator.validate(request, command, errors);
    }

    @Override
    protected ModelAndView onSubmit(Object o) throws ServletException {
        AddFeedbackCommand command = (AddFeedbackCommand)o;

        _log.warn("step 2");


        Map<String,String> model = new HashMap<String,String>();
        Map<String,Integer> yesNo = new HashMap<String,Integer>();
        yesNo.put("No",0);
        yesNo.put("Yes",1);
        //model.put("action",new UrlBuilder(UrlBuilder.ADD_EDIT_SCHOOL_OR_DISTRICT).toString());
        model.put("name",command.getSubmitterName());
        model.put("schoolOrDistrict",command.getSchoolOrDistrict());
        model.put("testvar",":" + command.getSchoolId() + ":");
        char[] digits = {0,1,2,3,4,5,6,7,8,9};
        if(command.getSchoolId() != null){
            model.put("testvar1","yes");

        }
        if(StringUtils.containsAny(command.getSchoolId(),digits)){
            model.put("testvar2","yes");

        }
        if(StringUtils.containsOnly(command.getSchoolId(),digits)){
            model.put("testvar3","yes");

        }
        if(StringUtils.isNumeric(command.getSchoolId())){
            model.put("testvar4","yes");

        }
        Map<String,String> categoryMap = new HashMap<String,String>();
        categoryMap.put("tests_ratings","Thank you for your feedback about Tests or Ratings!");
        categoryMap.put("functionality","Thank you for your feedback about our site.");
        categoryMap.put("osp","Thank you for your feedback about our Official School Profile.");
        NewEntityQueue newEntityQueue = new NewEntityQueue();
        newEntityQueue.setOriginalId(0);
        newEntityQueue.setStatus("unprocessed");
        //if(command.getSchoolId() != null && StringUtils.containsAny(command.getSchoolId(),digits) && StringUtils.containsOnly(command.getSchoolId(),digits)){
        if(command.getSchoolId() != null && StringUtils.isNotBlank(command.getSchoolId()) && StringUtils.isNumeric(command.getSchoolId())){
            model.put("schoolId",command.getSchoolId());
            School school = _schoolDao.getSchoolById(command.getState(),new Integer(command.getSchoolId()));
            model.put("name",school.getName());
            newEntityQueue.setGsId(new Integer(command.getSchoolId()));
            newEntityQueue.setStateId(school.getStateId());
            newEntityQueue.setNcesCode(school.getNcesCode());
            newEntityQueue.setDistrictId(school.getDistrictId());
            categoryMap.put("tests_ratings","Thank you for your feedback regarding tests or ratings of " + school.getName() + "!");
            categoryMap.put("osp","Thank you for your feedback about our Official School Profile for " + school.getName() + ".");
            //newEntityQueue.setOriginalId(0);
        }else{
            //newEntityQueue.setOriginalId(0);
        }
        if(command.getDistrictId() != null && !(command.getDistrictId().equals("")) && command.getAddEdit().equals("add")){
            newEntityQueue.setDistrictId(new Integer(command.getDistrictId()));
        }
        String thankyouMessage = categoryMap.get(command.getCategory()) != null ? categoryMap.get(command.getCategory()) : "Thank you for your feedback!";
        model.put("thankyouMessage",thankyouMessage);
        //School school = _schoolDao.findSchool(State.fromString(command.getState()),)
        newEntityQueue.setSchoolOrDistrict(command.getSchoolOrDistrict());
        newEntityQueue.setGradeLevels(new Grades(command.getGrades()));
        //default to public
        SchoolType schoolType = SchoolType.PUBLIC;
        if(command.getSchoolType() != null){
            schoolType = command.getSchoolType().equals("public") ? SchoolType.PUBLIC
                    :  command.getSchoolType().equals("charter") ? SchoolType.CHARTER
                    :  command.getSchoolType().equals("private") ? SchoolType.PRIVATE
                    : SchoolType.PUBLIC;
        }
        newEntityQueue.setType(schoolType);


        newEntityQueue.setContactName(command.getSubmitterName());
        newEntityQueue.setContactEmail(command.getSubmitterEmail());
        newEntityQueue.setContactConnection(command.getSubmitterConnectionToSchool());
        if(command.getSubmitterConnectionToSchool() != null && command.getSubmitterConnectionToSchool().equals("Other")){
            newEntityQueue.setContactConnection(command.getSubmitterConnectionToSchoolText());
        }
        newEntityQueue.setVerificationUrl(command.getVerificationUrl());
        newEntityQueue.setContactNotes(command.getContactNotes());
        newEntityQueue.setOpen(command.getOpen());
        newEntityQueue.setName(command.getName());
        newEntityQueue.setStreet(command.getStreet());
        newEntityQueue.setStreetLine2(command.getStreetLine2());
        newEntityQueue.setCity(command.getCity());
        newEntityQueue.setStateAbbreviation(command.getState());
        newEntityQueue.setZipcode(command.getZipcode());
        String countyName = command.getCounty();
        if(countyName == null || countyName.equals("")){
            countyName = command.getCounty1();        }
        _log.warn("county:" + countyName + ":");
        newEntityQueue.setCounty(countyName);
        if(command.getEnrollment() != null && StringUtils.isNotBlank(command.getEnrollment())){
            newEntityQueue.setEnrollment(new Integer(command.getEnrollment()));
        }
        newEntityQueue.setPhone(command.getPhone());
        newEntityQueue.setFax(command.getFax());
        newEntityQueue.setWebSite(command.getWebSite());
        newEntityQueue.setHeadOfficialName(command.getHeadOfficialName());
        newEntityQueue.setHeadOfficialEmail(command.getHeadOfficialEmail());
        newEntityQueue.setStartTime(command.getStartTime());
        newEntityQueue.setEndTime(command.getEndTime());
        newEntityQueue.setAffiliation(command.getAffiliation());
        newEntityQueue.setAssociation(command.getAssociation());
        newEntityQueue.setLowAge(command.getLowAge());
        newEntityQueue.setHighAge(command.getHighAge());

        if(command.getGender() != null){
            newEntityQueue.setGender(SchoolSubtype.create(command.getGender()));
        }
        if(command.getPreschoolSubtype() != null){
            newEntityQueue.setPreschoolSubtype(SchoolSubtype.create(command.getPreschoolSubtype()));
        }

        if(command.getBilingual() != null && StringUtils.isNotBlank(command.getBilingual())){
            newEntityQueue.setBilingual(new Integer(yesNo.get(command.getBilingual())));
        }
        if(command.getSpecialEd() != null && StringUtils.isNotBlank(command.getSpecialEd())){
            newEntityQueue.setSpecialEd(new Integer(yesNo.get(command.getSpecialEd())));
        }
        if(command.getComputers() != null && StringUtils.isNotBlank(command.getComputers())){
            newEntityQueue.setComputers(new Integer(yesNo.get(command.getComputers())));
        }
        if(command.getExtendedCare() != null && StringUtils.isNotBlank(command.getExtendedCare())){
            newEntityQueue.setExtendedCare(new Integer(yesNo.get(command.getExtendedCare())));
        }

        newEntityQueue.setOperatingSystem(command.getOperatingSystem());
        newEntityQueue.setBrowser(command.getBrowser());

        newEntityQueue.setCategory(command.getCategory());
        if(command.getOpen() != null && command.getOpen().equals("Yes")){
            newEntityQueue.setOpen("Yes");
        }
        if(command.getOpen() != null && command.getOpen().equals("No")){
            String whenOpen = "No";
            if(command.getOpenSeason() != null){
                whenOpen = command.getOpenSeason();
            }
            if(command.getOpenYear() != null){
                whenOpen += " " + command.getOpenYear();
            }
            newEntityQueue.setOpen(whenOpen);
        }

        _newEntityQueueDao.saveNewEntityQueue(newEntityQueue,"web form");

        String message = "Hello,\n" +
                "\n" +
                "Thank you for contacting GreatSchools!\n" +
                "We�ve received your request and will get back to you with an answer as soon as possible.\n" +
                "\n" +
                "Please do not reply to this automated email - we will respond to you from your support request.\n" +
                "\n" +
                "Sincerely,\n" +
                "GreatSchools Support\n";
        sendTheEmail(message,command.getSubmitterEmail());


        return new ModelAndView(getSuccessView(), model);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        AddFeedbackCommand command = (AddFeedbackCommand)o;
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

        //List<String> schoolType = new ArrayList();
        Map<String,String> svMap = new HashMap();
        svMap.put("public","public");
        svMap.put("private","private");
        svMap.put("public charter","charter");
        //schoolType.add("public");
        //schoolType.add("private");
        //schoolType.add("charter");
        map.put("schoolType", svMap);

        List<String> gender = new ArrayList();
        gender.add("coed");
        gender.add("all_male");
        gender.add("all_female");
        map.put("gender", gender);

        Map<String,String> ynMap = new HashMap();
        ynMap.put("Yes","Y");
        ynMap.put("No","N");
        map.put("bilingual", ynMap);
        map.put("specialEd", ynMap);
        map.put("computers", ynMap);
        map.put("extendedCare", ynMap);
        map.put("open", ynMap);

        PreschoolSubtypeSelectorTagHandler psth = new PreschoolSubtypeSelectorTagHandler();
        String[] stValues = psth.getOptionValues();
        String[] stDisplay = psth.getOptionDisplayNames();
        java.util.Arrays.sort(stValues, java.text.Collator.getInstance());
        java.util.Arrays.sort(stDisplay, java.text.Collator.getInstance());

        Map<String,String> preschoolSubtype = new HashMap();
        for( int i = 0; i < stValues.length; i++)
        {
            preschoolSubtype.put(stValues[i],stDisplay[i]);
        }
        //map.put("preschoolSubtype",preschoolSubtype);
        map.put("preschoolSubtype",stValues);
        map.put("psMap",preschoolSubtype);

        List<FormOption> relation = new ArrayList<FormOption>();
        relation.add(new FormOption("School Administrator","School Administrator"));
        relation.add(new FormOption("School Staff","School Staff"));
        relation.add(new FormOption("District Staff","District Staff"));
        relation.add(new FormOption("Parent","Parent"));
        relation.add(new FormOption("Other","Other"));
        map.put("submitterConnectionToSchool", relation);

        List<FormOption> openSeason = new ArrayList<FormOption>();
        openSeason.add(new FormOption("Fall","Fall"));
        openSeason.add(new FormOption("Winter","Winter"));
        openSeason.add(new FormOption("Spring","Spring"));
        openSeason.add(new FormOption("Summer","Summer"));
        map.put("openSeason", openSeason);

        List<FormOption> openYear = new ArrayList<FormOption>();
        Integer year = new Integer(Calendar.getInstance().get(Calendar.YEAR));
        Integer nextyear = year + 1;
        openYear.add(new FormOption(year.toString(),year.toString()));
        openYear.add(new FormOption(nextyear.toString(),nextyear.toString()));
        map.put("openYear", openYear);

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
        map.put("stateOptions", allStateOptions);

        // Populate city options
        List<City>cities = _geoDao.findCitiesByState(stateOrDefault);
        List<FormOption> cityOptions = new ArrayList<FormOption>();
        for (City city : cities) {
            cityOptions.add(new FormOption(city.getName(), city.getName()));
        }
        map.put("cityOptions", cityOptions);

        // Populate county options
        /*
        List<ICounty> counties = _geoDao.findCounties(stateOrDefault);
        List<FormOption> countyOptions = new ArrayList<FormOption>();
        for (ICounty county : counties) {
            countyOptions.add(new FormOption(county.getName(), county.getName()));
        }
        map.put("countyOptions", countyOptions);
        */

        // Populate school options if a city was chosen
        List<FormOption> schoolOptions = new ArrayList<FormOption>();
        if (!StringUtils.isBlank(command.getCityName())) {
            List<School> schools = _schoolDao.findSchoolsInCity(stateOrDefault, command.getCityName(), false);
            for (School school : schools) {
                schoolOptions.add(new FormOption(StringEscapeUtils.escapeHtml(school.getName()), school.getId().toString()));
            }
        }
        List<FormOption> districtOptions = new ArrayList<FormOption>();
        if (!StringUtils.isBlank(command.getCityName())) {
            List<District> districts = _districtDao.findDistrictsInCity(stateOrDefault, command.getCityName());
            for (District district : districts) {
                districtOptions.add(new FormOption(StringEscapeUtils.escapeHtml(district.getName()), district.getId().toString()));
            }
        }
        map.put("schoolOptions", schoolOptions);
        map.put("districtOptions", districtOptions);



        //map.put("addEditURL",new UrlBuilder(UrlBuilder.ADD_EDIT_SCHOOL_OR_DISTRICT).toString());


        return map;
    }

    protected void captureRequestParameters(HttpServletRequest request, AddFeedbackCommand command, Map<String,Object> map) {
        if (!StringUtils.isBlank(request.getParameter("feedbackType"))) {
            command.setFeedbackType(ContactUsCommand.FeedbackType.valueOf(request.getParameter("feedbackType")));
            if (!StringUtils.isBlank(request.getParameter("city"))) {
                command.setCityName(request.getParameter("city"));
            }
            if (!StringUtils.isBlank(request.getParameter("schoolId"))) {
                command.setSchoolId(request.getParameter("schoolId"));
            }
	    command.setState(SessionContextUtil.getSessionContext(request).getStateOrDefault());
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


    JavaMailSender mailSender;

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTheEmail(String messageText,String emailText) {

        //... * Do the business calculations....
        //... * Call the collaborators to persist the order
        final String message = messageText;
        final String email = emailText;

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws MessagingException {
                mimeMessage.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(email));
                try{
                    mimeMessage.setFrom(new InternetAddress("gs_support@greatschools.org","GreatSchools Support"));
                }catch(Exception e){
                }
                mimeMessage.setSubject("Thank you for contacting GreatSchools!");
                mimeMessage.setText(
                        message
                );

            }
        };
        try{
            mailSender.send(preparator);
        }
        catch (MailException ex) {
            //log it and go on
            System.err.println(ex.getMessage());
        }
    }





}
