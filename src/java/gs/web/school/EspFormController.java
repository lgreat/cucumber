package gs.web.school;

import gs.data.community.User;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusDataSetDao;
import gs.data.security.Role;
import gs.data.state.State;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/esp/")
public class EspFormController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspFormController.class);
    public static final String VIEW = "school/espForm";
    public static final String PATH_TO_FORM = "/school/esp/form.page"; // used by UrlBuilder
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final String FORM_VISIBLE_KEYS_PARAM = "_visibleKeys";
    public static final boolean ENABLE_EXTERNAL_DATA_SAVING = false;
    
    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private ICensusDataSetDao _dataSetDao;
    @Autowired
    private EspFormValidationHelper _espFormValidationHelper;

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

        putResponsesInModel(school, page, modelMap); // fetch responses for school, including external data
        putPercentCompleteInModel(school, modelMap);

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
    
    protected void putResponsesInModel(School school, int page, ModelMap modelMap) {
        Map<String, EspResponseStruct> responseMap = new HashMap<String, EspResponseStruct>();
        
        // fetch all responses to allow page to use ajax page switching if desired.
        List<EspResponse> responses = _espResponseDao.getResponses(school);

        for (EspResponse response: responses) {
            EspResponseStruct responseStruct = responseMap.get(response.getKey());
            if (responseStruct == null) {
                responseStruct = new EspResponseStruct();
                responseMap.put(response.getKey(), responseStruct);
            }
            responseStruct.addValue(response.getValue());
        }

        fetchExternalValues(responseMap, school); // fetch data that lives outside of esp_response

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
        Set<String> keysForExternalData = getKeysForExternalData(school);

        // TODO: This is immutable per java api spec, will need to copy to a mutable map to get save hooks working
        Map<String, String[]> requestParameterMap = (Map<String, String[]>) request.getParameterMap();

        // SAVE HOOKS GO HERE
        // Any data that needs to be transformed from a view specific format into a database representation
        // must be handled here. These methods should SET keys in requestParameterMap with the final
        // values if necessary. Any composite keys should be REMOVED from keysForPage and the composite key ADDED.
        // e.g. form posts address_street, address_city, address_zip which get composed by a method here
        // into a new param "address" with the concatenated values. The DB only knows about "address" while
        // the page has the fields split out.
//        handleEndTimeSave(requestParameterMap, keysForPage);

        // Basic validation goes here
        // Note: This should only validate data going into esp_response. Data going to external places MUST be
        // validated by their respective save method below!
        Map<String, String> errorFieldToMsgMap = _espFormValidationHelper.performValidation
                (requestParameterMap, keysForPage, school);
        if (!errorFieldToMsgMap.isEmpty()) {
            outputJsonErrors(errorFieldToMsgMap, response);
            return; // early exit
        }

        Date now = new Date(); // consistent time stamp for this save
        // this won't save any extra data that isn't in keysForPage
        // I'm not yet sure that's a good thing
        for (String key: keysForPage) {
            String[] responseValues;
            
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
            if (ENABLE_EXTERNAL_DATA_SAVING && keysForExternalData.contains(key)) {
                String error = saveExternalValue(key, responseValues, school, user, now);
                if (error != null) {
                    errorFieldToMsgMap.put(key, error);
                }
                active = false; // data saved elsewhere should be inactive
            }
            for (String responseValue: responseValues) {
                EspResponse espResponse = new EspResponse();
                espResponse.setKey(key);
                espResponse.setValue(responseValue);
                espResponse.setSchool(school);
                espResponse.setMemberId(user.getId());
                espResponse.setCreated(now);
                espResponse.setActive(active);
                responseList.add(espResponse);
            }
        }

        if (!errorFieldToMsgMap.isEmpty()) {
            outputJsonErrors(errorFieldToMsgMap, response);
            return; // early exit
        }

        // Deactivate existing data first, then save
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);
        _espResponseDao.saveResponses(school, responseList);

        JSONObject successObj = new JSONObject();
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
     * Sample read hook. Modifies responseMap in place, adding any new key/value pairs that the view
     * might need to display the data.
     */
    protected void handleEndTimeRead(Map<String, EspResponseStruct> responseMap) {
        EspResponseStruct responseStruct = responseMap.get("end_time");
        if (responseStruct != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("militaryTimeFormatStr");
                Date date = sdf.parse(responseStruct.getValue());
                sdf.applyPattern("hh:mm:ss");
                EspResponseStruct textStruct = new EspResponseStruct();
                textStruct.addValue(sdf.format(date));
                sdf.applyPattern("ZZ");
                EspResponseStruct ampmStruct = new EspResponseStruct();
                ampmStruct.addValue(sdf.format(date));
                responseMap.put("end_time_text", textStruct);
                responseMap.put("end_time_ampm", ampmStruct);
            } catch (ParseException e) {
                _log.error(e, e);
                // TODO: ???
            }
        }
    }

    /**
     * Sample write hook. Modifies requestParameterMap in place, adding any new key/value pairs that the
     * DB will persist. If value is to be persisted, the key better be in keysOnPage!
     */
    protected void handleEndTimeSave(Map<String, String[]> requestParameterMap) {
        String[] endTimeTextArr = requestParameterMap.get("end_time_text");
        String[] endTimeAmPmArr = requestParameterMap.get("end_time_ampm");
        if (endTimeTextArr != null && endTimeAmPmArr != null && endTimeTextArr.length == 1 && endTimeAmPmArr.length == 1) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm ZZ");
                Date endTimeDate = sdf.parse(endTimeTextArr[0] + " " + endTimeAmPmArr[0]);
                sdf.applyPattern("hhmm");
                String endTime = sdf.format(endTimeDate);
                // TODO: The request map is immutable
                requestParameterMap.put("end_time", new String[] {endTime});
            } catch (ParseException e) {
                _log.error(e, e);
                // TODO: ??
            }
        }
    }

    /**
     * Checks if the user has access to the form for the school specified by the given state/schoolId.
     * Returns false any parameter is null, or if the user does not have an active esp membership
     * for the given state/schoolId and is not a superuser
     */
    protected boolean checkUserHasAccess(User user, State state, Integer schoolId) {
        if (user != null && state != null && schoolId > 0) {
            if (user.hasRole(Role.ESP_MEMBER)) {
                return _espMembershipDao.findEspMembershipByStateSchoolIdUserId
                        (state, schoolId, user.getId(), true) != null;
            } else if (user.hasRole(Role.ESP_SUPERUSER)) {
                return true;
            } else {
                _log.warn("User " + user + " does not have required role " + Role.ESP_MEMBER + " or " + Role.ESP_SUPERUSER + " to access ESP form.");
            }
        } else {
            _log.warn("Invalid or null user/state/schoolId: " + user + "/" + state + "/" + schoolId);
        }
        return false;
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
        if (checkUserHasAccess(user, state, schoolId)) {
            return user;
        }
        return null;
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
        return 2; // TODO: implement
    }

    /**
     * Percent completion for a page is currently {number of keys that have active values} / {total number of keys}
     */
    protected int getPercentCompletionForPage(int page, School school) {
        Set<String> keys = getKeysForPercentCompletion(page);

        if (!keys.isEmpty() && school != null) {
            int count = _espResponseDao.getKeyCount(school, keys);
            if (count > 0) {
                float percent = ((float) count / keys.size()) * 100;
                return Math.round(percent);
            }
        }

        return 0;
    }

    /**
     * Get the list of keys that count towards percent completion for a particular page.
     * TODO: This signature might be insufficient. Probably the school needs to get passed in
     */
    protected Set<String> getKeysForPercentCompletion(int page) {
        Set<String> keys = new HashSet<String>();
        if (page == 1) {
            keys.add("admissions_url");
            keys.add("student_enrollment");
        } else if (page == 2) {
            keys.add("academic_focus");
            keys.add("instructional_model");
            keys.add("instructional_model_other");
            keys.add("best_known_for");
            keys.add("college_destination_1");
            keys.add("college_destination_2");
            keys.add("college_destination_3");
        } else {
            _log.error("Unknown page provided to getKeysForPage: " + page);
        }
        return keys;
    }

    /**
     * Fetch the keys whose values live outside of esp_response and put them in responseMap
     * (overwriting existing keys if present).
     */
    protected void fetchExternalValues(Map<String, EspResponseStruct> responseMap, School school) {
        for (String key: getKeysForExternalData(school)) {
            // fetch data from external source
            String[] vals = getExternalValuesForKey(key, school);
            if (vals != null && vals.length > 0) {
                EspResponseStruct espResponse = new EspResponseStruct();
                for (String val: vals) {
                    espResponse.addValue(val);
                }
                responseMap.put(key, espResponse);
            } else {
                // don't let esp_response values for external data show up on form
                // external data has to come from external sources!
                responseMap.remove(key);
            }
        }
    }

    /**
     * Return error message on error.
     */
    protected String saveExternalValue(String key, String[] values, School school, User user, Date now) {
        if (values == null || values.length == 0 && school == null) {
            return null; // early exit
        }
        if (StringUtils.equals("student_enrollment", key)) {
            try {
                _log.debug("Saving student_enrollment elsewhere: " + values[0]);
                saveCensusInteger(school, Integer.parseInt(values[0]), CensusDataType.STUDENTS_ENROLLMENT, user);
            } catch (NumberFormatException nfe) {
                return "Must be an integer.";
            }
        } else if (StringUtils.equals("grade_levels", key)) {
            _log.debug("Saving grade_levels " + values + " elsewhere for school:" + school.getName());
            return saveGradeLevels(school, values, user, now);
        } else if (StringUtils.equals("school_type", key)) {
            _log.debug("Saving school type " + values[0] + " elsewhere for school:" + school.getName());
            return saveSchoolType(school, values[0], user, now);
        }
        return null;
    }

    protected void saveCensusInteger(School school, int data, CensusDataType censusDataType, User user) {
        _dataSetDao.addValue(findOrCreateManualDataSet(school, censusDataType), school, data, "ESP-" + user.getId());
    }

    protected CensusDataSet findOrCreateManualDataSet(School school, CensusDataType censusDataType) {
        CensusDataSet dataSet = _dataSetDao.findDataSet(school.getDatabaseState(), censusDataType, 0, null, null);
        if (dataSet == null) {
            dataSet = _dataSetDao.createDataSet(school.getDatabaseState(), censusDataType, 0, null, null);
        }
        return dataSet;
    }

    /**
     * Save grade levels to the db. Return error string if necessary.
     */
    protected String saveGradeLevels(School school, String[] data, User user, Date now) {
        List<String> gradesList = new ArrayList<String>();
        Collections.addAll(gradesList, data);
        if (!gradesList.isEmpty()) {
            Grades grades = Grades.createGrades(StringUtils.join(gradesList, ","));
            school.setGradeLevels(grades);
            saveSchool(school, user, now);
        } else {
            return "You must select a grade level.";
        }
        return null;
    }
    
    protected void saveSchool(School school, User user, Date now) {
        String modifiedBy = "ESP-" + user.getId();
        school.setManualEditBy(modifiedBy);
        school.setManualEditDate(now);
        school.setModified(now);
        _schoolDao.saveSchool(school.getDatabaseState(), school, modifiedBy);
    }

    /**
     * Save grade levels to the db
     */
    protected String saveSchoolType(School school, String data, User user, Date now) {
        SchoolType type = SchoolType.getSchoolType(data);
        if (type != null) {
            school.setType(type);
            saveSchool(school, user, now);
        } else {
            return "Must select a valid school type.";
        }
        return null;
    }

    /**
     * The set of keys that exist in external places.
     * @param school This might depend on the type or other attribute of the school.
     */
    protected Set<String> getKeysForExternalData(School school) {
        Set<String> keys = new HashSet<String>();
        keys.add("student_enrollment");
        keys.add("grade_levels");
        keys.add("school_type");
        keys.add("school_type_affiliation");
        keys.add("school_type_affiliation_other");
        return keys;
    }

    /**
     * Fetch external value, e.g. from census or school table. This returns an array of strings
     * representing the values for that key (e.g. {"KG", "1", "2"} for grade_level or {"100"} for enrollment
     */
    protected String[] getExternalValuesForKey(String key, School school) {
        if (StringUtils.equals("student_enrollment", key) && school.getEnrollment() != null) {
            _log.debug("Overwriting key " + key + " with value " + school.getEnrollment());
            return new String[]{String.valueOf(school.getEnrollment())};
        } else if (StringUtils.equals("grade_levels", key) && school.getGradeLevels() != null) {
            String gradeLevels = school.getGradeLevels().getCommaSeparatedString();
            return gradeLevels.split(",");
        } else if (StringUtils.equals("school_type", key) && school.getType() != null) {
            return new String[]{school.getType().getSchoolTypeName()};
        }
        return new String[0];
    }

    /**
     * A simple data structure that presents data to the view in two ways. ${struct.value} fetches a single
     * string value and should be used for keys with 1-to-1 mappings to values. ${struct.valueMap} returns a
     * map of answer_key to (irrelevant) which should be used for 1-to-many mappings of key to value. It would
     * be checked like ${not empty struct.valueMap['K']}.
     *
     * Behind the scenes, addValue both sets value and creates a key in valueMap. At this point in development
     * there is no way of knowing whether any given key is 1-to-1 or 1-to-many, so this structure basically
     * separates out needing to know that.
     */
    protected static class EspResponseStruct {
        private String _value;
        private Map<String, Boolean> _valueMap = new HashMap<String, Boolean>();

        public String getValue() {
            return _value;
        }

        public Map<String, Boolean> getValueMap() {
            return _valueMap;
        }

        public void addValue(String value) {
            _value = value;
            _valueMap.put(value, true);
        }
    }
}