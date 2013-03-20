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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Autowired
    private EspHelper _espHelper;

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

        //Check if the user is provisional
        boolean isProvisionalUser = _espFormValidationHelper.isUserProvisional(user);
        modelMap.put("isProvisionalUser", isProvisionalUser);

        if(isProvisionalUser){
            putProvisionalResponsesInModel(user,school, modelMap); // fetch provisional responses for school
        }else{
            putResponsesInModel(school, modelMap); // fetch responses for school, including external data
        }

        List<SchoolMedia> schoolMedias = _schoolMediaDao.getAllActiveAndPendingBySchool(school.getId(), school.getDatabaseState());
        modelMap.put("schoolMedias", schoolMedias);

        putAfterSchoolIndicatorInModel(school, modelMap);
        putRepeatingFormIndicatorInModel(modelMap, "after_school_", 5);  // Add index of last active after_school data into model
        putRepeatingFormIndicatorInModel(modelMap, "summer_program_", 5);
        putPercentCompleteInModel(school, modelMap);

        //TODO do the below lines for provisional also?
        modelMap.put("ethnicityBreakdowns", EspFormExternalDataHelper.STATE_TO_ETHNICITY.get(school.getDatabaseState()));
        modelMap.put("censusDataTypes", EspFormExternalDataHelper.STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState()));

        modelMap.put("stateLocked", _noEditDao.isStateLocked(state));

        return VIEW;
    }

    /**
     *  Function to fetch the provisional responses and put in the model. When a provisional user saves a page, all the
     *  keys on that page are saved in the db as a key value pair. This key value pair is used to ascertain what
     *  keys on the form the provisional user has modified and hence have provisional data.
     *  Therefore if a page was not edited by the provisional user(i.e there is no provisional data on the page) then we display the active data.
     * @param user
     * @param school
     * @param modelMap
     */
    protected void putProvisionalResponsesInModel(User user, School school, ModelMap modelMap) {
        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();

        Map<String, String> provisionalKeysLookUpMap = new HashMap<String, String>();
        //Get all the keys on the form that the provisional user has modified.
        List<EspResponse> provisionalKeysList = _espResponseDao.getAllProvisionalResponseKeysByUserAndSchool(school, user.getId(), true);
        if (provisionalKeysList != null && !provisionalKeysList.isEmpty()) {
            for (EspResponse espResponse : provisionalKeysList) {
                String[] keys = espResponse.getValue().split(",");
                for (int i = 0; i < keys.length; i++) {
                    //value does not matter.This map is just used for look up.
                    provisionalKeysLookUpMap.put(keys[i], "");
                }
            }
        }

        //Get all the external keys for the school and convert to a look up map.
        Set<String> externalKeysSet = _espFormExternalDataHelper.getKeysForExternalData(school);
        //Map of all the external keys for the school.Used for look up.
        Map<String, String> externalKeysLookupMap = new HashMap<String, String>();
        for (String externalKey : externalKeysSet) {
            //value does not matter.This map is just used for look up.
            externalKeysLookupMap.put(externalKey, "");
        }

        //This list is used to remove external keys that the provisional user has modified from the master list of all external data points.
        List<String> externalKeysToRemove = new ArrayList<String>();

        //Map to store the external keys that the provisional user has modified.
        Map<String, String> provisionalExternalKeysToValueMap = new HashMap<String, String>();

        //Get all the responses that the provisional user has made for this school.
        List<EspResponse> provisionalResponses = _espResponseDao.getResponsesByUserAndSchool(school, user.getId(), true);

        for (EspResponse espResponse : provisionalResponses) {

            //All the keys for the responses should be in the Map.If not that means that a we have a provisional response
            //but the key was not marked for provisional data.
            if (provisionalKeysLookUpMap.containsKey(espResponse.getKey())) {
                //All provisional data should be inactive.
                if (!espResponse.isActive()) {

                    String value = espResponse.getSafeValue();

                    //If its an external data point then store it in a different map to be processed separately.
                    //Else if its not an external data point then put in response map for the view.
                    if (externalKeysLookupMap.containsKey(espResponse.getKey())) {

                        //If the response if multivalued, then store it as a delimited string.
                        // Example. for key grade_levels the value in the map will be k-,-1-,-2
                        if (StringUtils.isNotBlank(provisionalExternalKeysToValueMap.get(espResponse.getKey()))) {
                            value = provisionalExternalKeysToValueMap.get(espResponse.getKey())
                                    + _espFormExternalDataHelper.DATA_DELIMITER + value;
                        }
                        provisionalExternalKeysToValueMap.put(espResponse.getKey(), value);

                    } else {
                        putInResponseMap(responseMap, espResponse);
                    }

                } else {
                    _log.error("Found an active response for provisional user.User:-" + user.getId() + " Key:-" + espResponse.getKey() + " Response:-" + espResponse.getSafeValue());
                }
            } else if(!espResponse.getKey().contains("_page_")){
                _log.error("Found a provisional response but the key was not marked for provisional data.Key:-" + espResponse.getKey() + " Response:-" + espResponse.getSafeValue() + " User:-" + user.getId());
            }
        }

        //Prepare a list of external values that the user has modified.This sub-list needs to be removed
        //the master list of all external data points.
        //This has to be done separately and not in the loop above bcos sometimes the user may chose not to respond to
        //the question and in that case there will not be a provisional response for that external data point.
        for (String allProvisionalKeys : provisionalKeysLookUpMap.keySet()) {
            if (externalKeysLookupMap.containsKey(allProvisionalKeys)) {
                //Add to list of external keys that the provisional user has modified.
                externalKeysToRemove.add(allProvisionalKeys);
            }
        }

        //Get all the active(non-provisional) responses for the form.
        List<EspResponse> responses = _espResponseDao.getResponses(school);

        //For every response for school, check if there is a provisional response.
        //If there is no provisional response then put in the active response.
        for (EspResponse espResponse : responses) {
            if (!provisionalKeysLookUpMap.containsKey(espResponse.getKey())) {
                putInResponseMap(responseMap, espResponse);
            }
        }

        //TODO Will there be keys in provisionalKeysLookUpMap but wont have responses.will those need to be in the view.?
        //TODO what happens if the keys are moved from page to page?

        //Transform provisional external data into a format that the view/form expects.
        _espFormExternalDataHelper.transformProvisionalExternalValuesForDisplay(responseMap, provisionalExternalKeysToValueMap);

        //Remove external keys that the provisional user has modified from the master list of all external data points.
        //This will enable us to get external data only for non-provisional data points.
        externalKeysSet.removeAll(externalKeysToRemove);

        //Fetch the external data for non-provisional data points.
        _espFormExternalDataHelper.fetchExternalValues(responseMap, school, externalKeysSet);

        modelMap.put("responseMap", responseMap);
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
            putInResponseMap(responseMap,response);
        }

        _espFormExternalDataHelper.fetchExternalValues(responseMap, school); // fetch data that lives outside of esp_response

        // READ HOOKS GO HERE
        // This is for data that needs to be transformed from the database representation to a view-specific one
//        handleEndTimeRead(responseMap);

        modelMap.put("responseMap", responseMap);
    }

     /* This function is used to support populating the "after school" and "summer programs" fields on page5.  Those sets of data
     * have allow up to 5 sets of "after school" and "summer program" data.  When the form is drawn all of the existing
     * sets of data are visible and the remaining blank sets hidden.  This function finds the index of the last set
     * of data to show.  Note - if there is data for set 3 then data sets 1, 2 and 3 will be shown even if 1 and  2 are
     * blank.  <br/> Limitations:<br/>
     * 1. The index at the end is limited to a single digit<br/>
     * 2. There is an underscore prior to the ending digit.
     * @param modelMap
     * @param prefix
     * @param maxForms
     */
    protected void putRepeatingFormIndicatorInModel( ModelMap modelMap, String prefix, int maxForms ) {
        Map<String, EspFormResponseStruct> responseMap = (Map<String, EspFormResponseStruct>)modelMap.get("responseMap");
        int lastUsed = 1;   // Always show one set of data even if it blank

        if( responseMap != null && responseMap.size() > 0 ) {
            Pattern pattern = Pattern.compile("^" + prefix + ".*_\\d$");

            for( String key : responseMap.keySet() ) {
                Matcher m = pattern.matcher(key);
                if( m.find() ) {
                    int index = Character.getNumericValue( key.charAt(key.length()-1) );
                    if( index <= maxForms ) { // Ignore any out of range datasets
                        lastUsed = (index > lastUsed) ? index : lastUsed;
                    }
                }
            }
        }

        modelMap.put(prefix, new Integer(lastUsed));

    }

    /**
     * Determine if this school qualifies to display the field sets for "after school" and "summer program" programs.
     * @param modelMap
     * @param school
     */
    protected void putAfterSchoolIndicatorInModel( School school, ModelMap modelMap  ) {

        boolean qualified = false;

        if( school.getStateAbbreviation().equals(State.CA) ) {
            if( (school.getDistrictId() == 14) || (school.getDistrictId() == 717) ) {
                qualified = true;
            }
            if( (school.getCity().equals("San Francisco")) || (school.getCity().equals("Oakland")) ) {
                qualified = true;
            }
        }

        if( qualified ) {
            modelMap.put("after_school_qualified", "yes");
        }
    }

    /**
     * Method to construct an EspFormResponseStruct and put in the response map.
     * @param responseMap
     * @param response
     */
    protected void putInResponseMap(Map<String, EspFormResponseStruct> responseMap,EspResponse response){
        EspFormResponseStruct responseStruct = responseMap.get(response.getKey());
        if (responseStruct == null) {
            responseStruct = new EspFormResponseStruct();
            responseMap.put(response.getKey(), responseStruct);
        }
        responseStruct.addValue(response.getSafeValue());
    }

    @RequestMapping(value="form.page", method=RequestMethod.POST)
    public void saveForm(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                         @RequestParam(value=PARAM_STATE, required=false) State state) throws IOException, JSONException {
        response.setContentType("application/json");
        // Fetch parameters
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            outputJsonError("noUser", response);
            return; // early exit
        }

        School school = getSchool(state, schoolId);
        if (school == null) {
            outputJsonError("noSchool", response);
            return; // early exit
        }

        int page = getPage(request);

        boolean isProvisionalData = _espFormValidationHelper.isUserProvisional(user);

        // The parameter FORM_VISIBLE_KEYS_PARAM is the list of response_keys that are valid for the given
        // form submission (though they may not be included in the actual post). Parse these into a set that
        // we can use to deactivate existing data and pull the new data out of the request.
        // Composed parameters will be added below in the save hooks
        String[] visibleKeys = StringUtils.split(request.getParameter(FORM_VISIBLE_KEYS_PARAM), ",");
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.addAll(Arrays.asList(visibleKeys));

        // copy requestParameterMap to a mutable map, and allow value to be any Object, so that we can store
        // complex objects in the map if necessary (e.g. Address)
        Map<String, Object[]> requestParameterMap = cloneAndConvert(request.getParameterMap());

        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        _espHelper.saveEspFormData(user, school, keysForPage, requestParameterMap, state, page, errorFieldToMsgMap,responseList,
                isProvisionalData,false);

        if (!errorFieldToMsgMap.isEmpty()) {
            outputJsonErrors(errorFieldToMsgMap, response);
            return; // early exit
        }

        // Check if this is the first time this school has gotten any data
        boolean schoolHasNoUserCreatedRows = _espResponseDao.schoolHasNoUserCreatedRows(school, true);

        JSONObject successObj = new JSONObject();
        // if there were no keys saved before, and we're saving at least one now,
        // then the form has officially been started
        //TODO omniture for provisional data?
        if (schoolHasNoUserCreatedRows && !responseList.isEmpty() && !isProvisionalData) {
            successObj.put("formStarted", true);
        }

        // let page know new completion percentage
        // TODO: Will probably need to include global form percentage as well
        successObj.put("percentComplete", getPercentCompletionForPage(page, school));
        successObj.write(response.getWriter());
        response.getWriter().flush();
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
        if (school == null || (!school.isActive() && !school.isDemoSchool())) {
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
            add("application_deadline_date");
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
            // Fields for After School programs
            add("after_school_exists");
            add("after_school_name_1");
            add("after_school_description_1");
            add("after_school_activities_1");
            add("after_school_activities_other_1");
            add("after_school_operator_1");
            add("after_school_start_1");
            add("after_school_end_1");
            add("after_school_day_1");
            add("after_school_attendance_restriction_1");
            add("after_school_grade_1");
            add("after_school_fee_1");
            add("after_school_financial_aid_1");
            add("after_school_fee_learn_more_1");
            add("after_school_website_1");
            add("after_school_phone_1");
            add("after_school_name_2");
            add("after_school_description_2");
            add("after_school_activities_2");
            add("after_school_activities_other_2");
            add("after_school_operator_2");
            add("after_school_start_2");
            add("after_school_end_2");
            add("after_school_day_2");
            add("after_school_attendance_restriction_2");
            add("after_school_grade_2");
            add("after_school_fee_2");
            add("after_school_financial_aid_2");
            add("after_school_fee_learn_more_2");
            add("after_school_website_2");
            add("after_school_phone_2");
            add("after_school_name_3");
            add("after_school_description_3");
            add("after_school_activities_3");
            add("after_school_activities_other_3");
            add("after_school_operator_3");
            add("after_school_start_3");
            add("after_school_end_3");
            add("after_school_day_3");
            add("after_school_attendance_restriction_3");
            add("after_school_grade_3");
            add("after_school_fee_3");
            add("after_school_financial_aid_3");
            add("after_school_fee_learn_more_3");
            add("after_school_website_3");
            add("after_school_phone_3");
            add("after_school_name_4");
            add("after_school_description_4");
            add("after_school_activities_4");
            add("after_school_activities_other_4");
            add("after_school_operator_4");
            add("after_school_start_4");
            add("after_school_end_4");
            add("after_school_day_4");
            add("after_school_attendance_restriction_4");
            add("after_school_grade_4");
            add("after_school_fee_4");
            add("after_school_financial_aid_4");
            add("after_school_fee_learn_more_4");
            add("after_school_website_4");
            add("after_school_phone_4");
            add("after_school_name_5");
            add("after_school_description_5");
            add("after_school_activities_5");
            add("after_school_activities_other_5");
            add("after_school_operator_5");
            add("after_school_start_5");
            add("after_school_end_5");
            add("after_school_day_5");
            add("after_school_attendance_restriction_5");
            add("after_school_grade_5");
            add("after_school_fee_5");
            add("after_school_financial_aid_5");
            add("after_school_fee_learn_more_5");
            add("after_school_website_5");
            add("after_school_phone_5");
            // Fields for Summer programs
            add("summer_program_exists");
            add("summer_program_name_1");
            add("summer_program_description_1");
            add("summer_program_activities_1");
            add("summer_program_activities_other_1");
            add("summer_program_operator_1");
            add("summer_program_date_start_1");
            add("summer_program_date_end_1");
            add("summer_program_start_1");
            add("summer_program_end_1");
            add("summer_program_day_1");
            add("summer_program_attendance_restriction_1");
            add("summer_program_grade_1");
            add("summer_program_fee_1");
            add("summer_program_financial_aid_1");
            add("summer_program_fee_learn_more_1");
            add("summer_program_before_after_care_1");
            add("summer_program_before_after_care_start_1");
            add("summer_program_before_after_care_end_1");
            add("summer_program_website_1");
            add("summer_program_phone_1");
            add("summer_program_name_2");
            add("summer_program_description_2");
            add("summer_program_activities_2");
            add("summer_program_activities_other_2");
            add("summer_program_operator_2");
            add("summer_program_date_start_2");
            add("summer_program_date_end_2");
            add("summer_program_start_2");
            add("summer_program_end_2");
            add("summer_program_day_2");
            add("summer_program_attendance_restriction_2");
            add("summer_program_grade_2");
            add("summer_program_fee_2");
            add("summer_program_financial_aid_2");
            add("summer_program_fee_learn_more_2");
            add("summer_program_before_after_care_2");
            add("summer_program_before_after_care_start_2");
            add("summer_program_before_after_care_end_2");
            add("summer_program_website_2");
            add("summer_program_phone_2");
            add("summer_program_name_3");
            add("summer_program_description_3");
            add("summer_program_activities_3");
            add("summer_program_activities_other_3");
            add("summer_program_operator_3");
            add("summer_program_date_start_3");
            add("summer_program_date_end_3");
            add("summer_program_start_3");
            add("summer_program_end_3");
            add("summer_program_day_3");
            add("summer_program_attendance_restriction_3");
            add("summer_program_grade_3");
            add("summer_program_fee_3");
            add("summer_program_financial_aid_3");
            add("summer_program_fee_learn_more_3");
            add("summer_program_before_after_care_3");
            add("summer_program_before_after_care_start_3");
            add("summer_program_before_after_care_end_3");
            add("summer_program_website_3");
            add("summer_program_phone_3");
            add("summer_program_name_4");
            add("summer_program_description_4");
            add("summer_program_activities_4");
            add("summer_program_activities_other_4");
            add("summer_program_operator_4");
            add("summer_program_date_start_4");
            add("summer_program_date_end_4");
            add("summer_program_start_4");
            add("summer_program_end_4");
            add("summer_program_day_4");
            add("summer_program_attendance_restriction_4");
            add("summer_program_grade_4");
            add("summer_program_fee_4");
            add("summer_program_financial_aid_4");
            add("summer_program_fee_learn_more_4");
            add("summer_program_before_after_care_4");
            add("summer_program_before_after_care_start_4");
            add("summer_program_before_after_care_end_4");
            add("summer_program_website_4");
            add("summer_program_phone_4");
            add("summer_program_name_5");
            add("summer_program_description_5");
            add("summer_program_activities_5");
            add("summer_program_activities_other_5");
            add("summer_program_operator_5");
            add("summer_program_date_start_5");
            add("summer_program_date_end_5");
            add("summer_program_start_5");
            add("summer_program_end_5");
            add("summer_program_day_5");
            add("summer_program_attendance_restriction_5");
            add("summer_program_grade_5");
            add("summer_program_fee_5");
            add("summer_program_financial_aid_5");
            add("summer_program_fee_learn_more_5");
            add("summer_program_before_after_care_5");
            add("summer_program_before_after_care_start_5");
            add("summer_program_before_after_care_end_5");
            add("summer_program_website_5");
            add("summer_program_phone_5");

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
            add("act_pdf_filename");
            add("sat_pdf_filename");
        }});
    }};

    public void setEspFormExternalDataHelper(EspFormExternalDataHelper espFormExternalDataHelper) {
        _espFormExternalDataHelper = espFormExternalDataHelper;
    }

    public void setEspFormValidationHelper(EspFormValidationHelper espFormValidationHelper) {
        _espFormValidationHelper = espFormValidationHelper;
    }

    public void setNoEditDao(INoEditDao noEditDao) {
        _noEditDao = noEditDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }
}