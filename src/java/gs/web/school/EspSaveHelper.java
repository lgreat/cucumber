package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.data.util.Address;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("espSaveHelper")
public class EspSaveHelper {
    private static final Log _log = LogFactory.getLog(EspSaveHelper.class);

    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private INoEditDao _noEditDao;
    @Autowired
    private EspFormValidationHelper _espFormValidationHelper;
    @Autowired
    private EspFormExternalDataHelper _espFormExternalDataHelper;
    @Autowired
    SchoolMediaDaoHibernate _schoolMediaDao;

    /**
     * This function performs validation, does data transformation and saves the responses to the database.
     * It handles both provisional and non-provisional data. It also handles external data.
     * @param user
     * @param school
     * @param keysForPage
     * @param keyToResponseMap
     * @param state
     * @param pageNum
     * @param errorFieldToMsgMap
     * @param responseList
     * @param isProvisionalData
     */
    public void saveEspFormData(User user, School school, Set<String> keysForPage,
                                Map<String, Object[]> keyToResponseMap, State state, int pageNum,
                                Map<String, String> errorFieldToMsgMap,List<EspResponse> responseList,
                                boolean isProvisionalData,boolean ignoreErrors) {

        // Save page
        Set<String> keysForExternalData = _espFormExternalDataHelper.getKeysForExternalData(school);

        // SAVE HOOKS GO HERE
        // Any data that needs to be transformed from a view specific format into a database representation
        // must be handled here. These methods should SET keys in requestParameterMap with the final
        // values if necessary. Any composite keys should be REMOVED from keysForPage and the composite key ADDED.
        // e.g. form posts address_street, address_city, address_zip which get composed by a method here
        // into a new param "address" with the concatenated values. The DB only knows about "address" while
        // the page has the fields split out.
        handleAddressSave(keyToResponseMap, keysForPage, school.getPhysicalAddress());
        String fieldError = handleSchoolPhone(keyToResponseMap, keysForPage);
        if (fieldError != null) {
            errorFieldToMsgMap.put("school_phone", fieldError);
        }
        fieldError = handleSchoolFax(keyToResponseMap, keysForPage);
        if (fieldError != null) {
            errorFieldToMsgMap.put("school_fax", fieldError);
        }
        handleSchoolAffiliation(keyToResponseMap, keysForPage);
        fieldError = handleEthnicity(keyToResponseMap, keysForPage);
        if (fieldError != null) {
            errorFieldToMsgMap.put("ethnicity", fieldError);
        }
        handleCensusDataTypes(keyToResponseMap, keysForPage, school);

        // Basic validation goes here
        // Note: This should only validate data going into esp_response. Data going to external places MUST be
        // validated by their respective save method below!
        errorFieldToMsgMap.putAll(_espFormValidationHelper.performValidation
                (keyToResponseMap, keysForPage, school));
        //If there are errors and if the errors should not be ignored, then exit.
        //There are cases where the errors should be ignored. example:- errors while promoting provisional data.
        if (!errorFieldToMsgMap.isEmpty() && !ignoreErrors) {
            return; // early exit
        }

        // Check if the State is locked for a data load
        boolean stateIsLocked = _noEditDao.isStateLocked(state);

        Date now = new Date(); // consistent time stamp for this save
        // this won't save any extra data that isn't in keysForPage
        // I'm not yet sure that's a good thing
        for (String key : keysForPage) {

            Object[] responseValues;

            responseValues = keyToResponseMap.get(key);
            // Do not save null values -- these are keys that might be present on a page
            // but aren't included in the POST (because the controls were disabled, the check boxes were
            // all deselected, etc.)
            if (responseValues == null || responseValues.length == 0) {
                continue;
            }
            boolean active = isProvisionalData ? false : true;
            // values that live elsewhere get saved out here
            // these values also go in esp_response but are disabled to clearly mark that they are not sourced from there
            if (keysForExternalData.contains(key)) {
                if (!stateIsLocked) {
                    String error = _espFormExternalDataHelper.saveExternalValue
                            (key, responseValues, school, user, now, isProvisionalData);
                    if (error != null) {
                        errorFieldToMsgMap.put(key, error);
                    }
                }
                active = false; // data saved elsewhere should be inactive
            }
            for (Object responseValue : responseValues) {
                if(errorFieldToMsgMap.get(key) != null){
                     continue;
                }

                EspResponse espResponse;
                if (StringUtils.equals("address", key)) {
                    espResponse = createEspResponse(user, school, now, key, active, (Address) responseValue);
                } else if (StringUtils.equals("census_ethnicity", key)) {
                    espResponse = createEspResponse(user, school, now, key, active, responseValue.toString());
                } else {
                    espResponse = createEspResponse(user, school, now, key, active, (String) responseValue);
                }
                if (espResponse != null) {
                    responseList.add(espResponse);
                }
            }
        }

        //If there are errors and if the errors should not be ignored, then exit.
        //There are cases where the errors should be ignored. example:- errors while promoting provisional data.
        if (!errorFieldToMsgMap.isEmpty() && !ignoreErrors) {
            return; // early exit
        }

        saveESPResponses(school, keysForPage, responseList, isProvisionalData, user, pageNum, now);
    }

    /**
     * Saves the responses to the database.
     * @param school
     * @param keysForPage
     * @param responseList
     * @param isProvisionalData
     * @param user
     * @param pageNum
     * @param now
     */
    protected void saveESPResponses(School school, Set<String> keysForPage, List<EspResponse> responseList,
                                    boolean isProvisionalData, User user, int pageNum, Date now) {

        if (keysForPage != null && !keysForPage.isEmpty()) {
            if (!isProvisionalData) {
                // Deactivate existing data first, then save
                _espResponseDao.deactivateResponsesByKeys(school, keysForPage);
            } else {
                //First delete any provisional data entered by the user for this school, then save.
                //We delete since there is no need to have historical provisional data.
                //When the provisional user has modified a page, save the keys on that page into the DB.These keys are used
                //when the provisional user is approved.
                Set<String> keysToDelete = new HashSet<String>();
                keysToDelete.addAll(keysForPage);

                //delete the keys that were stored for the page.
                String key = getPageKeys(pageNum);
                keysToDelete.add(key);
                _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), keysToDelete);

                EspResponse espResponse = createEspResponse(user, school, now, getPageKeys(pageNum), false, StringUtils.join(keysForPage, ","));
                if (espResponse != null) {
                    responseList.add(espResponse);
                }
            }
            if (responseList != null && !responseList.isEmpty()) {
                _espResponseDao.saveResponses(school, responseList);
            }
        }

    }

    public void saveUspFormData(User user, School school, State state,
                                Map<String, Object[]> responseKeyValues,
                                Set<String> responseKeys) {
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        Date now = new Date();
        for(String key : responseKeys) {
            Object[] values = responseKeyValues.get(key);
            if(values == null || values.length == 0) {
                continue;
            }
            for(Object value : values) {
                EspResponse espResponse = createUspResponse(user, school, now, key, true, (String) value);
                if(espResponse != null) {
                    responseList.add(espResponse);
                }
            }
        }

        saveUspResponses(school, responseKeys, responseList, user);
    }

    protected void saveUspResponses(School school, Set<String> keys, List<EspResponse> responseList,
                                    User user) {
        _espResponseDao.deactivateResponsesByKeys(school, keys);
        if (keys != null && !keys.isEmpty()) {
            if (responseList != null && !responseList.isEmpty()) {
                _espResponseDao.saveResponses(school, responseList);
            }
        }
    }


    protected String getPageKeys(int pageNum){
        return "_page_" + pageNum + "_keys";
    }

    protected EspResponse createEspResponse(User user, School school, Date now, String key, boolean active, String responseValue) {
        if (StringUtils.isBlank(responseValue)) {
            return null;
        }
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(StringUtils.left(responseValue, EspFormController.MAX_RESPONSE_VALUE_LENGTH));
        espResponse.setSchool(school);
        espResponse.setMemberId(user.getId());
        espResponse.setCreated(now);
        espResponse.setActive(active);
        espResponse.setSource(EspResponseSource.osp);
        return espResponse;
    }

    protected EspResponse createUspResponse(User user, School school, Date now, String key, boolean active, String responseValue) {
        if (StringUtils.isBlank(responseValue)) {
            return null;
        }
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(StringUtils.left(responseValue, EspFormController.MAX_RESPONSE_VALUE_LENGTH));
        espResponse.setSchool(school);
        espResponse.setMemberId(user.getId());
        espResponse.setCreated(now);
        espResponse.setActive(active);
        espResponse.setSource(EspResponseSource.usp);
        return espResponse;
    }

    protected EspResponse createEspResponse(User user, School school, Date now, String key, boolean active, Address responseValue) {
        return createEspResponse(user, school, now, key, active, responseValue.toString());
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

    public void setNoEditDao(INoEditDao noEditDao) {
        _noEditDao = noEditDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

    public void setEspFormExternalDataHelper(EspFormExternalDataHelper espFormExternalDataHelper) {
        _espFormExternalDataHelper = espFormExternalDataHelper;
    }

    public void setEspFormValidationHelper(EspFormValidationHelper espFormValidationHelper) {
        _espFormValidationHelper = espFormValidationHelper;
    }


}