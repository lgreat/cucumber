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
    private IEspMembershipDao _espMembershipDao;
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
            responseStruct.addValue(response.getValue());
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

    protected void handleAddressSave(Map<String, Object[]> requestParameterMap, Set<String> keysForPage, Address existingAddress) {
        String[] street = (String[]) requestParameterMap.get("physical_address_street");

        keysForPage.remove("physical_address_street");

        if (street != null && street.length == 1) {
            
            Address address = new Address(street[0], existingAddress.getCity(), existingAddress.getState(), existingAddress.getZip());

            requestParameterMap.put("address", new Object[]{address});
            keysForPage.add("address");
            _log.error("Yes address: " + address);
        } else {
            _log.error("No address");
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
        if (SchoolType.PRIVATE == school.getType()) {
            return 8;
        }
        return 7;
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
        } else if (page == 3) {
        } else if (page == 4) {
        } else if (page == 5) {
        } else if (page == 6) {
        } else if (page == 7) {
        } else if (page == 8) {
        } else {
            _log.error("Unknown page provided to getKeysForPercentCompletion: " + page);
        }
        return keys;
    }

    protected Map<String, Object[]> cloneAndConvert(Map<String,String[]> requestParameterMap) {
        return new HashMap<String, Object[]>(requestParameterMap);
    }
}