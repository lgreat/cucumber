package gs.web.school;

import gs.data.community.User;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.data.util.Address;
import gs.data.util.CommunityUtil;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/esp/")
public class EspFormController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspFormController.class);
    public static final int MAX_RESPONSE_VALUE_LENGTH = 3000;
    public static final String VIEW = "school/espForm";
    public static final String PATH_TO_FORM = "/school/esp/form.page"; // used by UrlBuilder
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final String FORM_VISIBLE_KEYS_PARAM = "_visibleKeys";

    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private INoEditDao _noEditDao;
    @Autowired
    private EspFormValidationHelper _espFormValidationHelper;
    @Autowired
    private EspFormExternalDataHelper _espFormExternalDataHelper;
    @Autowired SchoolMediaDaoHibernate _schoolMediaDao;

    // TODO: If user is valid but school/state is not, redirect to landing page
    @RequestMapping(value="form.page", method=RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request,
                           @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId, 
                           @RequestParam(value=PARAM_STATE, required=false) State state) {
        // Fetch parameters
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        
        School school = getSchool(state, schoolId);
        if (school == null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        int page = getPage(request);
        int maxPage = getMaxPageForSchool(school);
        if (page < 1 || page > maxPage) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        // populate basic model data
        modelMap.put("school", school);
        modelMap.put("page", (long)page);
        modelMap.put("maxPage", maxPage);
        modelMap.put("espSuperuser", user.hasRole(Role.ESP_SUPERUSER));

        // required for photo uploader
        modelMap.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        List<SchoolMedia> schoolMedias = _schoolMediaDao.getAllActiveAndPendingBySchool(school.getId(), school.getDatabaseState());
        modelMap.put("schoolMedias", schoolMedias);

        putResponsesInModel(school, modelMap); // fetch responses for school, including external data
        putPercentCompleteInModel(school, modelMap);
        modelMap.put("ethnicityBreakdowns", EspFormExternalDataHelper.STATE_TO_ETHNICITY.get(school.getDatabaseState()));
        modelMap.put("censusDataTypes", EspFormExternalDataHelper.STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState()));

        modelMap.put("stateLocked", _noEditDao.isStateLocked(state));

        return VIEW;
    }
    
    protected void putPercentCompleteInModel(School school, ModelMap modelMap) {
        // Map must be keyed by long to be accessible via EL
        Map<Long, Integer> percentCompleteMap = new HashMap<Long, Integer>();
        for (int x=1; x <= getMaxPageForSchool(school); x++) {
            percentCompleteMap.put((long)x, getPercentCompletionForPage(x, school));
        }
        modelMap.put("percentComplete", percentCompleteMap);
    }
    
    protected void putResponsesInModel(School school, ModelMap modelMap) {
        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        
        // fetch all responses to allow page to use ajax page switching if desired.
        List<EspResponse> responses = _espResponseDao.getResponses(school);

        for (EspResponse response: responses) {
            EspFormResponseStruct responseStruct = responseMap.get(response.getKey());
            if (responseStruct == null) {
                responseStruct = new EspFormResponseStruct();
                responseMap.put(response.getKey(), responseStruct);
            }
            responseStruct.addValue(response.getSafeValue());
        }

        _espFormExternalDataHelper.fetchExternalValues(responseMap, school); // fetch data that lives outside of esp_response

        // READ HOOKS GO HERE
        // This is for data that needs to be transformed from the database representation to a view-specific one
//        handleEndTimeRead(responseMap);

        modelMap.put("responseMap", responseMap);
    }

    @RequestMapping(value="form.page", method=RequestMethod.POST)
    public void saveForm(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                         @RequestParam(value=PARAM_STATE, required=false) State state) throws IOException, JSONException {
        response.setContentType("application/json");
        // Fetch parameters
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            outputJsonError("noAccess", response);
            return; // early exit
        }

        School school = getSchool(state, schoolId);
        if (school == null) {
            outputJsonError("noSchool", response);
            return; // early exit
        }

        int page = getPage(request);

        // The parameter FORM_VISIBLE_KEYS_PARAM is the list of response_keys that are valid for the given
        // form submission (though they may not be included in the actual post). Parse these into a set that
        // we can use to deactivate existing data and pull the new data out of the request.
        // Composed parameters will be added below in the save hooks
        String[] visibleKeys = StringUtils.split(request.getParameter(FORM_VISIBLE_KEYS_PARAM), ",");
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.addAll(Arrays.asList(visibleKeys));

        // Save page
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        Set<String> keysForExternalData = _espFormExternalDataHelper.getKeysForExternalData(school);

        // copy requestParameterMap to a mutable map, and allow value to be any Object, so that we can store
        // complex objects in the map if necessary (e.g. Address)
        Map<String, Object[]> requestParameterMap = cloneAndConvert(request.getParameterMap());

        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        // SAVE HOOKS GO HERE
        // Any data that needs to be transformed from a view specific format into a database representation
        // must be handled here. These methods should SET keys in requestParameterMap with the final
        // values if necessary. Any composite keys should be REMOVED from keysForPage and the composite key ADDED.
        // e.g. form posts address_street, address_city, address_zip which get composed by a method here
        // into a new param "address" with the concatenated values. The DB only knows about "address" while
        // the page has the fields split out.
        handleAddressSave(requestParameterMap, keysForPage, school.getPhysicalAddress());
        String fieldError = handleSchoolPhone(requestParameterMap, keysForPage);
        if (fieldError != null) {
            errorFieldToMsgMap.put("school_phone", fieldError);
        }
        fieldError = handleSchoolFax(requestParameterMap, keysForPage);
        if (fieldError != null) {
            errorFieldToMsgMap.put("school_fax", fieldError);
        }
        handleSchoolAffiliation(requestParameterMap, keysForPage);
        fieldError = handleEthnicity(requestParameterMap, keysForPage);
        if (fieldError != null) {
            errorFieldToMsgMap.put("ethnicity", fieldError);
        }
        handleCensusDataTypes(requestParameterMap, keysForPage, school);

        // Basic validation goes here
        // Note: This should only validate data going into esp_response. Data going to external places MUST be
        // validated by their respective save method below!
        errorFieldToMsgMap.putAll(_espFormValidationHelper.performValidation
                (requestParameterMap, keysForPage, school));
        if (!errorFieldToMsgMap.isEmpty()) {
            outputJsonErrors(errorFieldToMsgMap, response);
            return; // early exit
        }

        // Check if the State is locked for a data load
        boolean stateIsLocked = _noEditDao.isStateLocked(state);

        Date now = new Date(); // consistent time stamp for this save
        // this won't save any extra data that isn't in keysForPage
        // I'm not yet sure that's a good thing
        for (String key: keysForPage) {
            Object[] responseValues;
            
            responseValues = requestParameterMap.get(key);

            // Do not save null values -- these are keys that might be present on a page
            // but aren't included in the POST (because the controls were disabled, the check boxes were
            // all deselected, etc.)
            if (responseValues == null || responseValues.length == 0) {
                continue;
            }
            boolean active = true;
            // values that live elsewhere get saved out here
            // these values also go in esp_response but are disabled to clearly mark that they are not sourced from there
            if (keysForExternalData.contains(key)) {
                if (!stateIsLocked) {
                    String error = _espFormExternalDataHelper.saveExternalValue
                            (key, responseValues, school, user, now);
                    if (error != null) {
                        errorFieldToMsgMap.put(key, error);
                    }
                }
                active = false; // data saved elsewhere should be inactive
            }
            for (Object responseValue: responseValues) {
                EspResponse espResponse;
                if (StringUtils.equals("address", key)) {
                    espResponse = createEspResponse(user, school, now, key, active, (Address) responseValue);
                } else if (StringUtils.equals("census_ethnicity", key) ) {
                    espResponse = createEspResponse(user, school, now, key, active, responseValue.toString());
                } else {
                    espResponse = createEspResponse(user, school, now, key, active, (String) responseValue);
                }
                if (espResponse != null) {
                    responseList.add(espResponse);
                }
            }
        }

        if (!errorFieldToMsgMap.isEmpty()) {
            outputJsonErrors(errorFieldToMsgMap, response);
            return; // early exit
        }

        // Check if this is the first time this school has gotten any data
        boolean schoolHasNoUserCreatedRows = _espResponseDao.schoolHasNoUserCreatedRows(school, true);

        // Deactivate existing data first, then save
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);
        _espResponseDao.saveResponses(school, responseList);

        JSONObject successObj = new JSONObject();
        // if there were no keys saved before, and we're saving at least one now,
        // then the form has officially been started
        if (schoolHasNoUserCreatedRows && !responseList.isEmpty()) {
            successObj.put("formStarted", true);
        }

        // let page know new completion percentage
        // TODO: Will probably need to include global form percentage as well
        successObj.put("percentComplete", getPercentCompletionForPage(page, school));
        successObj.write(response.getWriter());
        response.getWriter().flush();
    }

    protected EspResponse createEspResponse(User user, School school, Date now, String key, boolean active, String responseValue) {
        if (StringUtils.isBlank(responseValue)) {
            return null;
        }
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(StringUtils.left(responseValue, MAX_RESPONSE_VALUE_LENGTH));
        espResponse.setSchool(school);
        espResponse.setMemberId(user.getId());
        espResponse.setCreated(now);
        espResponse.setActive(active);
        return espResponse;
    }

    protected EspResponse createEspResponse(User user, School school, Date now, String key, boolean active, Address responseValue) {
        return createEspResponse(user, school, now, key, active, responseValue.toString());
    }

    protected void outputJsonErrors(Map<String, String> errorFieldToMsgMap, HttpServletResponse response) throws JSONException, IOException {
        JSONObject errorObj = new JSONObject();
        for (String errorField: errorFieldToMsgMap.keySet()) {
            errorObj.put(errorField, errorFieldToMsgMap.get(errorField));
        }
        errorObj.put("errors", true);
        errorObj.write(response.getWriter());
        response.getWriter().flush();
    }

    protected void outputJsonError(String msg, HttpServletResponse response) throws JSONException, IOException {
        JSONObject errorObj = new JSONObject();
        errorObj.put("error", msg);
        errorObj.write(response.getWriter());
        response.getWriter().flush();
    }
    
    protected void handleSchoolAffiliation(Map<String, Object[]> requestParameterMap, Set<String> keysForPage) {
        if (keysForPage.contains("school_type_affiliation") && (
                requestParameterMap.get("school_type_affiliation") == null
                        || requestParameterMap.get("school_type_affiliation").length == 0)) {
            requestParameterMap.put("school_type_affiliation", new Object[]{""}); // force delete
        }
    }

    protected String handleEthnicity(Map<String, Object[]> requestParameterMap, Set<String> keysForPage) {
        Map<Integer, Integer> breakdownToValueMap = new HashMap<Integer, Integer>();
        Set<String> keysToRemove = new HashSet<String>();
        String error = null;
        boolean unavailable = keysForPage.contains("census_ethnicity_unavailable")
                && requestParameterMap.get("census_ethnicity_unavailable") != null
                && requestParameterMap.get("census_ethnicity_unavailable").length == 1
                && Boolean.valueOf(requestParameterMap.get("census_ethnicity_unavailable")[0].toString());
        for (String key: keysForPage) {
            if (StringUtils.startsWith(key, "ethnicity_") && requestParameterMap.get(key) != null && requestParameterMap.get(key).length == 1) {
                keysToRemove.add(key);
                try {
                    Integer breakdownId = new Integer(key.substring("ethnicity_".length()));
                    String sValue = requestParameterMap.get(key)[0].toString();
                    Integer value;
                    if (unavailable || StringUtils.length(sValue) == 0) {
                        value = 0;
                    } else {
                        value = new Integer(sValue);
                    }
                    breakdownToValueMap.put(breakdownId, value);
                } catch (Exception e) {
                    _log.error(e, e);
                    error = "Value must be numeric.";
                }
            }
        }
        keysForPage.removeAll(keysToRemove);
        if (breakdownToValueMap.size() > 0) {
            keysForPage.add("census_ethnicity");
            requestParameterMap.put("census_ethnicity", new Object[] {breakdownToValueMap});
        }
        return error;
    }

    /**
     * Make sure that census data types handle "this data is unavailable" correctly by disabling any active
     * manual value in the DB.
     */
    protected void handleCensusDataTypes(Map<String, Object[]> requestParameterMap, Set<String> keysForPage, School school) {
        List<EspFormExternalDataHelper.EspCensusDataTypeConfiguration> dataTypeConfigs =
                EspFormExternalDataHelper.STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState());
        if (dataTypeConfigs != null) {
            for (EspFormExternalDataHelper.EspCensusDataTypeConfiguration dataTypeConfig: dataTypeConfigs) {
                String key = "census_" + dataTypeConfig.getId();
                String keyUnavailable = key + "_unavailable";
                if (keysForPage.contains(keyUnavailable) 
                        && requestParameterMap.get(keyUnavailable) != null
                        && requestParameterMap.get(keyUnavailable).length == 1
                        && Boolean.valueOf(requestParameterMap.get(keyUnavailable)[0].toString())) {
                    requestParameterMap.put(key, new Object[] {""}); // this will disable the existing value
                }
            }
        }
    }

    protected void handleAddressSave(Map<String, Object[]> requestParameterMap, Set<String> keysForPage, Address existingAddress) {
        String[] street = (String[]) requestParameterMap.get("physical_address_street");

        keysForPage.remove("physical_address_street");

        if (street != null && street.length == 1) {
            
            Address address = new Address(street[0], existingAddress.getCity(), existingAddress.getState(), existingAddress.getZip());

            requestParameterMap.put("address", new Object[]{address});
            keysForPage.add("address");
        }
    }
    
    protected String handleSchoolPhone(Map<String, Object[]> requestedParameterMap, Set<String> keysForPage) {
        String schoolPhoneAreaCode = (String) getSingleValue(requestedParameterMap, "school_phone_area_code");
        String schoolPhoneOfficeCode = (String) getSingleValue(requestedParameterMap, "school_phone_office_code");
        String schoolPhoneLastFour = (String) getSingleValue(requestedParameterMap, "school_phone_last_four");

        keysForPage.remove("school_phone_area_code");
        keysForPage.remove("school_phone_office_code");
        keysForPage.remove("school_phone_last_four");
        if (schoolPhoneAreaCode != null && schoolPhoneOfficeCode != null && schoolPhoneLastFour != null) {
            String phoneNumberString = schoolPhoneAreaCode + schoolPhoneOfficeCode + schoolPhoneLastFour;
            
            if (phoneNumberString.matches("\\d+")) {
                phoneNumberString = "(" + schoolPhoneAreaCode + ") " + schoolPhoneOfficeCode + "-" + schoolPhoneLastFour;
                requestedParameterMap.put("school_phone", new String[] {phoneNumberString});
                keysForPage.add("school_phone");
                requestedParameterMap.remove("school_phone_area_code");
                requestedParameterMap.remove("school_phone_office_code");
                requestedParameterMap.remove("school_phone_last_four");
            } else if (StringUtils.isBlank(phoneNumberString)){
                requestedParameterMap.put("school_phone", new String[] {""});
                keysForPage.add("school_phone");
                requestedParameterMap.remove("school_phone_area_code");
                requestedParameterMap.remove("school_phone_office_code");
                requestedParameterMap.remove("school_phone_last_four");
            } else {
                return "Phone number must be numeric";
            }
        }
        return null;
    }
    
    protected String handleSchoolFax(Map<String, Object[]> requestedParameterMap, Set<String> keysForPage) {
        String schoolFaxAreaCode = (String) getSingleValue(requestedParameterMap, "school_fax_area_code");
        String schoolFaxOfficeCode = (String) getSingleValue(requestedParameterMap, "school_fax_office_code");
        String schoolFaxLastFour = (String) getSingleValue(requestedParameterMap, "school_fax_last_four");

        keysForPage.remove("school_fax_area_code");
        keysForPage.remove("school_fax_office_code");
        keysForPage.remove("school_fax_last_four");

        if (schoolFaxAreaCode != null && schoolFaxOfficeCode != null && schoolFaxLastFour != null) {
            String faxNumberString = schoolFaxAreaCode + schoolFaxOfficeCode + schoolFaxLastFour;

            if (faxNumberString.matches("\\d+")) {
                faxNumberString = "(" + schoolFaxAreaCode + ") " + schoolFaxOfficeCode + "-" + schoolFaxLastFour;
                requestedParameterMap.put("school_fax", new String[] {faxNumberString});
                keysForPage.add("school_fax");
                requestedParameterMap.remove("school_fax_area_code");
                requestedParameterMap.remove("school_fax_office_code");
                requestedParameterMap.remove("school_fax_last_four");
            } else if (StringUtils.isBlank(faxNumberString)){
                requestedParameterMap.put("school_fax", new String[] {""});
                keysForPage.add("school_fax");
                requestedParameterMap.remove("school_fax_area_code");
                requestedParameterMap.remove("school_fax_office_code");
                requestedParameterMap.remove("school_fax_last_four");
            } else {
                return "Fax number must be numeric";
            }
        }
        return null;
    }
    
    protected Object getSingleValue(Map<String, Object[]> map, String key) {
        Object[] values = map.get(key);
        if (values != null && values.length >= 1) {
            return values[0];
        }
        return null;
    }

    /**
     * Pulls the user out of the session context. Returns null if there is no user, or if the user fails
     * checkUserAccess
     */
    protected User getValidUser(HttpServletRequest request, State state, Integer schoolId) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }
        if (_espFormValidationHelper.checkUserHasAccess(user, state, schoolId)) {
            return user;
        }
        return null;
    }
    
    public static final Map<State, List<String>> FRUITCAKE_STATE_TO_CITY = new HashMap<State, List<String>>(3) {
        {
            List<String> wiCities = new ArrayList<String>(1);
            wiCities.add("Milwaukee");
            put(State.WI, wiCities);
            List<String> inCities = new ArrayList<String>(4);
            inCities.add("Beech Grove");
            inCities.add("Indianapolis");
            inCities.add("West Newton");
            inCities.add("Speedway");
            put(State.IN, inCities);
            put(State.DC, null); // null means accept all
        }
    };
    
    public static boolean isFruitcakeSchool(School s) {
        if (s != null && s.getDatabaseState() != null && s.getCity() != null) {
            if (FRUITCAKE_STATE_TO_CITY.containsKey(s.getDatabaseState())) {
                List<String> cities = FRUITCAKE_STATE_TO_CITY.get(s.getDatabaseState());
                // null means accept all
                return cities == null || cities.contains(s.getCity());
            }
        }
        return false;
    }

    /**
     * Parses the state and schoolId out of the request and fetches the school. Returns null if
     * it can't parse parameters, can't find school, or the school is inactive
     */
    protected School getSchool(State state, Integer schoolId) {
        if (state == null || schoolId == null) {
            return null;
        }
        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolId);
        } catch (Exception e) {
            // handled below
        }
        if (school == null || !school.isActive()) {
            _log.error("School is null or inactive: " + school);
            return null;
        }
        
        if (school.isPreschoolOnly()) {
            _log.error("School is preschool only! " + school);
            return null;
        }

        return school;
    }

    /**
     * Parses the page out of the request, returning -1 in error cases
     */
    protected int getPage(HttpServletRequest request) {
        int page = -1;
        if (request.getParameter(PARAM_PAGE) != null) {
            try {
                page = Integer.parseInt(request.getParameter(PARAM_PAGE));
            } catch (NumberFormatException nfe) {
                // fall through
            }
        }
        return page;
    }

    /**
     * How many pages are on the form for a given school
     */
    protected int getMaxPageForSchool(School school) {
        if (isFruitcakeSchool(school) && school.getType() == SchoolType.PRIVATE) {
            return 8;
        }
        return 7;
    }

    /**
     * Percent completion for a page is currently always 0
     * TODO: Implementation needed
     */
    protected int getPercentCompletionForPage(int page, School school) {
        return 0;
    }

    protected Map<String, Object[]> cloneAndConvert(Map<String,String[]> requestParameterMap) {
        return new HashMap<String, Object[]>(requestParameterMap);
    }
    
    public static Map<Integer, Set<String>> KEYS_BY_PAGE = new HashMap<Integer, Set<String>>() {{
        put(1, new HashSet<String>() {{
            add("age_pk_start");
            add("average_class_size");
            add("before_after_care");
            add("before_after_care_end");
            add("before_after_care_start");
            add("coed");
            add("early_childhood_programs");
            add("end_time");
            add("grade_levels");
            add("school_type");
            add("school_type_affiliation");
            add("start_time");
            add("transportation");
            add("transportation_other");
            add("transportation_shuttle");
            add("transportation_shuttle_other");
        }});
        put(2, new HashSet<String>() {{
            add("academic_focus");
            add("best_known_for");
            add("immersion");
            add("immersion_language");
            add("instructional_model");
            add("instructional_model_other");
        }});
        put(3, new HashSet<String>() {{
            add("admissions_url");
            add("application_deadline");
            add("application_fee");
            add("application_fee_amount");
            add("application_process");
            add("applications_received");
            add("applications_received_year");
            add("destination_school_1");
            add("destination_school_2");
            add("destination_school_3");
            add("fee_waivers");
            add("feeder_school_1");
            add("feeder_school_2");
            add("feeder_school_3");
            add("financial_aid");
            add("financial_aid_type");
            add("financial_aid_type_other");
            add("students_accepted");
            add("students_accepted_year");
            add("students_vouchers");
            add("tuition_high");
            add("tuition_low");
            add("tuition_year");
        }});
        put(4, new HashSet<String>() {{
            add("boarding");
            add("college_destination_1");
            add("college_destination_2");
            add("college_destination_3");
            add("college_prep");
            add("college_prep_other");
            add("ell_languages");
            add("ell_level");
            add("extra_learning_resources");
            add("extra_learning_resources_other");
            add("facilities");
            add("foreign_language");
            add("foreign_language_other");
            add("partnerships");
            add("partnerships_name_1");
            add("partnerships_name_2");
            add("partnerships_name_3");
            add("partnerships_name_4");
            add("partnerships_name_5");
            add("partnerships_url_1");
            add("partnerships_url_2");
            add("partnerships_url_3");
            add("partnerships_url_4");
            add("partnerships_url_5");
            add("post_graduation_2yr");
            add("post_graduation_4yr");
            add("post_graduation_military");
            add("post_graduation_vocational");
            add("post_graduation_workforce");
            add("post_graduation_year");
            add("schedule");
            add("schedule_exists");
            add("skills_training");
            add("skills_training_other");
            add("spec_ed_level");
            add("special_ed_programs");
            add("special_ed_programs_exists");
            add("staff_languages");
            add("staff_languages_other");
            add("staff_resources");
        }});
        put(5, new HashSet<String>() {{
            add("academic_award_1");
            add("academic_award_1_year");
            add("academic_award_2");
            add("academic_award_2_year");
            add("academic_award_3");
            add("academic_award_3_year");
            add("academic_award_exists");
            add("arts_media");
            add("arts_music");
            add("arts_performing_written");
            add("arts_visual");
            add("boys_sports");
            add("boys_sports_other");
            add("girls_sports");
            add("girls_sports_other");
            add("service_award_1");
            add("service_award_2");
            add("service_award_3");
            add("service_award_exists");
            add("student_clubs");
            add("student_clubs_dance");
            add("student_clubs_language");
            add("student_clubs_other_1");
            add("student_clubs_other_2");
            add("student_clubs_other_3");
        }});
        put(6, new HashSet<String>() {{
            add("anything_else");
            add("bullying_policy");
            add("dress_code");
            add("parent_involvement");
            add("parent_involvement_other");
            add("photo_upload");
            add("school_colors");
            add("school_mascot");
            add("school_video");
        }});
        put(7, new HashSet<String>() {{
            add("administrator_email");
            add("administrator_name");
            add("contact_method");
            add("contact_method_email");
            add("contact_method_other");
            add("contact_method_phone");
            add("facebook_url");
            add("physical_address_city");
            add("physical_address_street");
            add("physical_address_zip");
            add("school_fax");
            add("school_phone");
            add("school_url");
        }});
        put(8, new HashSet<String>() {{
            add("page_8_touched");
        }});
    }};
}