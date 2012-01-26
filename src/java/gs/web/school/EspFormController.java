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

    // TODO: If user is valid but school/state is not, redirect to landing page
    @RequestMapping(value="form.page", method=RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request,
                           @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId, 
                           @RequestParam(value=PARAM_STATE, required=false) State state) {
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        
        School school = getSchool(state, schoolId);
        if (school == null) {
            // TODO: proper error handling
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        int page = getPage(request);
        int maxPage = getMaxPageForSchool(school);
        if (page < 1 || page > maxPage) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        modelMap.put("school", school);
        modelMap.put("page", page);
        modelMap.put("maxPage", maxPage);
        modelMap.put("espSuperuser", user.hasRole(Role.ESP_SUPERUSER));

        putResponsesInModel(school, page, modelMap);

        return VIEW;
    }
    
    protected void putResponsesInModel(School school, int page, ModelMap modelMap) {
        Map<String, EspResponseStruct> responseMap = new HashMap<String, EspResponseStruct>();
        
        // fetch all responses for now to allow page to use ajax page switching if desired.
        List<EspResponse> responses = _espResponseDao.getResponses(school);

        Map<Integer, Integer> percentCompleteMap = new HashMap<Integer, Integer>();
        for (int x=1; x <= getMaxPageForSchool(school); x++) {
            percentCompleteMap.put(x, getPercentCompletionForPage(x, school));
        }
        modelMap.put("percentComplete", percentCompleteMap);

        for (EspResponse response: responses) {
            EspResponseStruct responseStruct = responseMap.get(response.getKey());
            if (responseStruct == null) {
                responseStruct = new EspResponseStruct();
                responseMap.put(response.getKey(), responseStruct);
            }
            responseStruct.addValue(response.getValue());
        }

        fetchExternalValues(responseMap, school); // fetch data that lives outside of esp_response

        modelMap.put("responseMap", responseMap);
    }

    protected static class EspResponseStruct {
        private String _value;
        private Map<String, String> _valueMap = new HashMap<String, String>();

        public String getValue() {
            return _value;
        }

        public Map<String, String> getValueMap() {
            return _valueMap;
        }
        
        public void addValue(String value) {
            _value = value;
            _valueMap.put(value, "1");
        }
    }

    protected void saveCensusData(School school, int data) {
        CensusDataSet dataSet = _dataSetDao.findDataSet(school.getDatabaseState(), CensusDataType.STUDENTS_ENROLLMENT, 0, null, /* breakdown */null);
        // TODO what if there is no existing data set?
        if (dataSet != null) {
            _dataSetDao.addValue(dataSet, school, data, "ESP");
        }
    }

    @RequestMapping(value="form.page", method=RequestMethod.POST)
    public void saveForm(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                         @RequestParam(value=PARAM_STATE, required=false) State state) throws IOException, JSONException {
        response.setContentType("application/json");
        
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "noAccess");
            errorObj.write(response.getWriter());
            response.getWriter().flush();
            return; // early exit
        }

        School school = getSchool(state, schoolId);
        if (school == null) {
            // TODO: proper error handling
            _log.error("School is null or inactive: " + school);
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "noSchool");
            errorObj.write(response.getWriter());
            response.getWriter().flush();
            return; // early exit
        }

        int page = getPage(request);

        // Deactivate page
        String[] visibleKeys = StringUtils.split(request.getParameter(FORM_VISIBLE_KEYS_PARAM), ",");
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.addAll(Arrays.asList(visibleKeys));
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);

        Date now = new Date();
        // Save page
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        Set<String> keysForExternalData = getKeysForExternalData(school);
        // this way saves null for anything not provided
        // and won't save any extra data that isn't in keysForPage
        // I'm not yet sure that's a good thing
        for (String key: keysForPage) {
            // Do not save null values -- these are keys that might be present on a page
            // but aren't included in the request.
            String[] responseValues = request.getParameterValues(key);
            
            if (responseValues == null || responseValues.length == 0) {
                continue;
            }
            boolean active = true;
            if (ENABLE_EXTERNAL_DATA_SAVING && keysForExternalData.contains(key)) {
                saveExternalValue(key, responseValues, school);
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

        _espResponseDao.saveResponses(school, responseList);
        JSONObject successObj = new JSONObject();
        // let page know new completion percentage
        successObj.put("percentComplete", getPercentCompletionForPage(page, school));
        successObj.write(response.getWriter());
        response.getWriter().flush();
    }

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
    
    protected int getMaxPageForSchool(School school) {
        return 2; // TODO: implement
    }

    protected int getPercentCompletionForPage(int page, School school) {
        Set<String> keys = getKeysForPercentCompletion(page);

        if (!keys.isEmpty() && school != null) {
            int count = _espResponseDao.getKeyCount(school, keys);
            if (count > 0) {
                float percent = ((float) count / keys.size()) * 100;
                return Math.round(percent);
            }
        }

        return Integer.valueOf("0");
    }

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
    
    protected void saveExternalValue(String key, String[] values, School school) {
        if (values == null || values.length == 0) {
            return; // early exit
        }
        if (StringUtils.equals("student_enrollment", key)) {
            _log.debug("Saving student_enrollment elsewhere: " + values[0]);
            saveCensusData(school, Integer.parseInt(values[0]));
        }

    }

    /**
     * The set of keys that exist in external places.
     * @param school This might depend on the type or other attribute of the school.
     */
    protected Set<String> getKeysForExternalData(School school) {
        Set<String> keys = new HashSet<String>();
        keys.add("student_enrollment");
        return keys;
    }

    /**
     * Fetch external value, e.g. from census or school table.
     */
    protected String[] getExternalValuesForKey(String key, School school) {
        if (StringUtils.equals("student_enrollment", key) && school.getEnrollment() != null) {
            _log.debug("Overwriting key " + key + " with value " + school.getEnrollment());
            return new String[] {String.valueOf(school.getEnrollment())};
        }
        return new String[0];
    }
}