package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import gs.web.school.usp.UspFormHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("espSaveHelper")
public class EspSaveHelper implements BeanFactoryAware {
    private static final Log _log = LogFactory.getLog(EspSaveHelper.class);
    public static final String OSP_GATEWAY_PAGE_NAME = "osp_gateway";

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

    private BeanFactory _beanFactory;

    /**
     * This function performs validation, does data transformation and saves the responses to the database.
     * It handles the data save by approved user, provisional user and also the activation of provisional data.
     * It also handles external data.
     *
     * @param user
     * @param school
     * @param state
     * @param pageNum
     * @param keysForPage
     * @param keyToResponseMap
     * @param responseList
     * @param errorFieldToMsgMap
     * @param saveBehaviour
     */
    public void saveOspFormData(User user, School school, State state, int pageNum,
                                Set<String> keysForPage,
                                Map<String, Object[]> keyToResponseMap,
                                List<EspResponse> responseList,
                                Map<String, String> errorFieldToMsgMap,
                                EspSaveBehaviour saveBehaviour) {

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

        //If there are validation errors and if the errors should not be ignored, then exit.
        //There are cases where the errors should be ignored. example:- errors while promoting provisional data.
        if (!errorFieldToMsgMap.isEmpty() && !saveBehaviour.isActivateProvisionalData()) {
            return; // early exit
        }

        // Check if the State is locked for a data load
        boolean stateIsLocked = _noEditDao.isStateLocked(state);

        Date now = new Date(); // consistent time stamp for this save

        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();

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

            allKeysWithActiveResponses.put(key, "");

            boolean active = saveBehaviour.isSaveProvisional() ? false : true;

            // values that live elsewhere get saved out here
            // these values also go in esp_response but are disabled to clearly mark that they are not sourced from there
            if (keysForExternalData.contains(key)) {
                if (!stateIsLocked) {
                    String error = _espFormExternalDataHelper.saveExternalValue
                            (key, responseValues, school, user, now, saveBehaviour.isSaveProvisional());
                    if (error != null) {
                        errorFieldToMsgMap.put(key, error);
                    }
                }
                active = false; // data saved elsewhere should be inactive
            }

            //Create EspResponse Object and add it to the list of responses.
            for (Object responseValue : responseValues) {
                if (errorFieldToMsgMap.get(key) != null) {
                    continue;
                }
                EspResponse espResponse = createEspResponse(user, school, now, key, active, responseValue.toString(), EspResponseSource.osp);

                if (espResponse != null) {
                    responseList.add(espResponse);
                }
            }
        }

        //If there are errors while saving external data and if the errors should not be ignored, then exit.
        //There are cases where the errors should be ignored. example:- errors while promoting provisional data.
        if (!errorFieldToMsgMap.isEmpty() && !saveBehaviour.isActivateProvisionalData()) {
            return; // early exit
        }

        //We need all keys that have active data.
        //Those keys, in conjunction with the keys that the approved user has just answered on a given page will determine
        //the esp status the school will be in.
        //We do not need this for provisional users bcos all their responses are inactive.
        //We do not need this when activating provisional data bcos keysForPage will contain all the keys on the form.
        if (saveBehaviour.isApprovedUserFormSave()) {
            fillInKeysForExistingActiveOSPResponses(school, allKeysWithActiveResponses);
        }

        saveOspResponses(user, school, pageNum, now, keysForPage, allKeysWithActiveResponses, responseList, saveBehaviour);
    }

    /**
     * Saves the responses to the database.
     *
     * @param user
     * @param school
     * @param pageNum
     * @param now
     * @param keysForPage
     * @param allKeysWithActiveResponses
     * @param responseList
     * @param saveBehaviour
     */
    public void saveOspResponses(User user, School school, int pageNum, Date now,
                                 Set<String> keysForPage, Map<String, String> allKeysWithActiveResponses,
                                 List<EspResponse> responseList, EspSaveBehaviour saveBehaviour) {
        if (responseList == null || responseList.isEmpty()) {
            return;
        }
        if (keysForPage == null || keysForPage.isEmpty() || allKeysWithActiveResponses == null || allKeysWithActiveResponses.isEmpty()) {
            return;
        }

        if (!saveBehaviour.isSaveProvisional()) {
            Set<EspResponseSource> responseSourcesToDeactivate =
                    new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));

            Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                    new HashSet<EspResponseSource>();

            //Check if all the the OSP questions have answers(either existing or the current form save).
            // If so then the school is already in or about to enter OSP preferred state.
            //Therefore deactivate all the USP data as well as the OSP data for the page keys.
            boolean allOspQuestionsAnswered = getStateManager(school).allOSPQuestionsAnswered(allKeysWithActiveResponses);
            if (allOspQuestionsAnswered) {
                responseSourcesToDeactivateIfOspPreferred.add(EspResponseSource.usp);
            }

            _espResponseDao.deactivateResponses
                    (school, keysForPage, responseSourcesToDeactivate, responseSourcesToDeactivateIfOspPreferred);

            //If we are approving the provisional user thereby activating their data then,
            //delete the '_page_osp_gateway_' key. The existence of this key is used to determine if the provisional user
            //should be shown the gateway form on the dashboard.Therefore delete it when approving the provisional user.
            if (saveBehaviour.isActivateProvisionalData()) {
                String pageKey = getPageKeys("osp_gateway");
                Set<String> pageKeys = new HashSet<String>(Arrays.asList(pageKey));
                _espResponseDao.deleteResponses(school, user.getId(), pageKeys);
            }

        } else {
            //If the provisional user is saving the form, delete the keys that were stored for the page earlier.
            String pageKey = getPageKeys(pageNum);
            EspResponse response = deleteAndCreateProvisionalUserResponse(pageKey, keysForPage, school, user, now);
            if (response != null) {
                responseList.add(response);
            }
        }
        _espResponseDao.saveResponses(school, responseList);
    }

    /**
     * Function that handles the saving of the USP data and the USP gateway data.
     *
     * @param user
     * @param school
     * @param responseKeyValues
     * @param formFieldNames
     */
    public void saveUspFormData(User user, School school,
                                Map<String, Object[]> responseKeyValues,
                                Set<String> formFieldNames) {
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        Date now = new Date();
        Set<String> responseParams = responseKeyValues.keySet();
        Map<String, String> responseKeysLookUpMap = new HashMap<String, String>();

        //Set up the defaults.
        boolean isOspProvisional = false;
        boolean active = user.isEmailProvisional() ? false : true;
        EspResponseSource responseSource = EspResponseSource.usp;

        //TODO what if the OSP user is filling an USP form?
        //For the gateway form.
        if (user.hasRole(Role.ESP_SUPERUSER) || user.hasRole(Role.ESP_MEMBER)) {
            responseSource = EspResponseSource.osp;
            active = true;
        } else {
            isOspProvisional = _espFormValidationHelper.isUserProvisional(user);
            if (isOspProvisional) {
                responseSource = EspResponseSource.osp;
                active = false;
            }
        }

        for (String responseParam : responseParams) {
            /**
             * Skip params that do not match usp form field names
             */
            if (!formFieldNames.contains(responseParam)) {
                continue;
            }

            Object[] values = responseKeyValues.get(responseParam);
            if (values == null || values.length == 0) {
                continue;
            }

            /**
             * The param value is {response_key}__{response_value} with double underscore as the delimiter.
             */
            for (Object value : values) {
                String keyValuePair = (String) value;
                if (keyValuePair.contains("__")) {
                    String[] keyValue = keyValuePair.split("__");

                    if (keyValue.length == 2) {
                        EspResponse espResponse = createEspResponse(user, school, now, keyValue[0], active, keyValue[1],
                                responseSource);
                        if (espResponse != null) {
                            responseList.add(espResponse);
                            //value does not matter its just used for look up.
                            responseKeysLookUpMap.put(keyValue[0], "");
                        }
                    }
                }
            }
        }

        //We do not require the user to response to all USP fields.
        //OSP members are required to respond to all questions.Hence do this only for OSP.
        if (EspResponseSource.osp.equals(responseSource)) {
            handleSubsectionResponses(user, school, now, active, responseSource, responseList, responseKeysLookUpMap);
        }
        saveUspResponses(user, school, now, responseList, responseSource, responseKeysLookUpMap, isOspProvisional);
    }

    /**
     * Handles saving the USP data and OSP gateway form data(by provisional and approved users) to the database.
     *
     * @param user
     * @param school
     * @param now
     * @param responseList
     * @param espResponseSource
     * @param responseKeysLookUpMap
     * @param isOspProvisional
     */
    public void saveUspResponses(User user, School school, Date now, List<EspResponse> responseList,
                                 EspResponseSource espResponseSource, Map<String, String> responseKeysLookUpMap,
                                 boolean isOspProvisional) {

        if (responseList == null || responseList.isEmpty()) {
            return;
        }

        final boolean isOspSource = EspResponseSource.osp.equals(espResponseSource);

        Set<EspResponseSource> responseSourcesToDeactivate = new HashSet<EspResponseSource>() {{
            if (isOspSource) {
                add(EspResponseSource.osp);
                add(EspResponseSource.datateam);
            } else {
                add(EspResponseSource.usp);
            }
        }};

        EspStatusManager statusManager = getStateManager(school);
        //TODO no need to get the status if the source is OSP.
        EspStatus espStatus = statusManager.getEspStatus();

        if (!isOspSource && espStatus.equals(EspStatus.OSP_PREFERRED)) {
            //If  its a USP form save and the school is in OSP preferred status then do not save the data.
            return;
        } else if (!isOspSource) {
            //If its a USP form save and the school is not in OSP preferred status then deactivate data first.
            //TODO this is not needed for email unverified users.
            _espResponseDao.deactivateResponses(school, user.getId(), responseSourcesToDeactivate);
        } else if (isOspSource && !isOspProvisional) {
            //If its a OSP gateway form save and the user is not provisional, then check if the user has answered all questions
            //If all the questions were answered then the school will be in OSP preferred status. Therefore deactivate all USP data.
            if (statusManager.allOSPQuestionsAnswered(responseKeysLookUpMap)) {
                responseSourcesToDeactivate.add(EspResponseSource.usp);
            }
            _espResponseDao.deactivateResponses(school, responseKeysLookUpMap.keySet(), responseSourcesToDeactivate, null);
        } else if (isOspProvisional) {
            //If the provisional user is saving the form, delete the keys that were stored for the page earlier.
            String pageKey = getPageKeys(OSP_GATEWAY_PAGE_NAME);
            EspResponse response = deleteAndCreateProvisionalUserResponse(pageKey, responseKeysLookUpMap.keySet(), school, user, now);
            if (response != null) {
                responseList.add(response);
            }
        }

        _espResponseDao.saveResponses(school, responseList);
    }

    /**
     * Sometimes the questions are grouped together. For ex arts_media and arts_music.However the user can choose to answer
     * just 1 of the questions. Therefore mark all the other questions that the user did not answer to "none" to save in the database.
     *
     * @param user
     * @param school
     * @param now
     * @param active
     * @param responseSource
     * @param responseList
     * @param responseKeysLookUpMap
     */
    protected void handleSubsectionResponses(User user, School school, Date now, boolean active,
                                             EspResponseSource responseSource, List<EspResponse> responseList,
                                             Map<String, String> responseKeysLookUpMap) {
        Set<String> subsectionResponseKeys = UspFormHelper.RESPONSE_KEY_SUB_SECTION_LABEL.keySet();
        for (String subsectionResponseKey : subsectionResponseKeys) {
            if (!responseKeysLookUpMap.containsKey(subsectionResponseKey)) {
                EspResponse espResponse = createEspResponse(user, school, now, subsectionResponseKey, active,
                        UspFormHelper.NONE_RESPONSE_VALUE, responseSource);
                if (espResponse != null) {
                    responseList.add(espResponse);
                    //Value does not matter, its just a look up map.
                    responseKeysLookUpMap.put(subsectionResponseKey, "");
                }
            }
        }
    }

    /**
     * Handles the deletion of OSP provisional data and creating an ESPResponse object for the special key value( i.e _page_etc).
     *
     * @param pageKey
     * @param keysForPage
     * @param school
     * @param user
     * @param now
     * @return
     */
    protected EspResponse deleteAndCreateProvisionalUserResponse(String pageKey, Set<String> keysForPage,
                                                                 School school, User user, Date now) {
        //First delete any provisional data entered by the user for this school, then save.
        //We delete since there is no need to have historical provisional data.
        //When the provisional user has modified a page, save the keys on that page into the DB.These keys are used
        //when the provisional user is approved.
        Set<String> keysToDelete = new HashSet<String>();
        keysToDelete.addAll(keysForPage);
        keysToDelete.add(pageKey);
        //TODO do we need to check the source here? what if provisional user is filling in the USP form?
        _espResponseDao.deleteResponses(school, user.getId(), keysToDelete);

        return createEspResponse(user, school, now, pageKey, false,
                StringUtils.join(keysForPage, ","), EspResponseSource.osp);
    }

    /**
     * Add the keys with active OSP data to the Map.
     *
     * @param school
     * @param allActiveKeysLookUpMap
     */
    private void fillInKeysForExistingActiveOSPResponses(School school, Map<String, String> allActiveKeysLookUpMap) {
        Set<EspResponseSource> responseSources = new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp,
                EspResponseSource.datateam));
        List<EspResponse> responses = _espResponseDao.getResponses(school, responseSources);

        for (EspResponse espResponse : responses) {
            if (!allActiveKeysLookUpMap.containsKey(espResponse.getKey())) {
                allActiveKeysLookUpMap.put(espResponse.getKey(), "");
            }
        }
    }

    protected String getPageKeys(int pageNum) {
        return "_page_" + pageNum + "_keys";
    }

    protected String getPageKeys(String pageName) {
        return "_page_" + pageName + "_keys";
    }

    //Use the BeanFactoryAware so that we get the espStatusManager component with auto injections.Otherwise we have to
    //manually set the espResponseDao on the espStatusManager.
    private EspStatusManager getStateManager(School school) {
        return (EspStatusManager) _beanFactory.getBean("espStatusManager", new Object[]{school});
    }

    protected EspResponse createEspResponse(User user, School school, Date now, String key, boolean active, String responseValue,
                                            EspResponseSource espResponseSource) {
        if (StringUtils.isBlank(responseValue)) {
            return null;
        }
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(StringUtils.left(responseValue, OspFormController.MAX_RESPONSE_VALUE_LENGTH));
        espResponse.setSchool(school);
        espResponse.setMemberId(user.getId());
        espResponse.setCreated(now);
        espResponse.setActive(active);
        espResponse.setSource(espResponseSource);
        return espResponse;
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
        for (String key : keysForPage) {
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
            requestParameterMap.put("census_ethnicity", new Object[]{breakdownToValueMap});
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
            for (EspFormExternalDataHelper.EspCensusDataTypeConfiguration dataTypeConfig : dataTypeConfigs) {
                String key = "census_" + dataTypeConfig.getId();
                String keyUnavailable = key + "_unavailable";
                if (keysForPage.contains(keyUnavailable)
                        && requestParameterMap.get(keyUnavailable) != null
                        && requestParameterMap.get(keyUnavailable).length == 1
                        && Boolean.valueOf(requestParameterMap.get(keyUnavailable)[0].toString())) {
                    requestParameterMap.put(key, new Object[]{""}); // this will disable the existing value
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
                requestedParameterMap.put("school_phone", new String[]{phoneNumberString});
                keysForPage.add("school_phone");
                requestedParameterMap.remove("school_phone_area_code");
                requestedParameterMap.remove("school_phone_office_code");
                requestedParameterMap.remove("school_phone_last_four");
            } else if (StringUtils.isBlank(phoneNumberString)) {
                requestedParameterMap.put("school_phone", new String[]{""});
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
                requestedParameterMap.put("school_fax", new String[]{faxNumberString});
                keysForPage.add("school_fax");
                requestedParameterMap.remove("school_fax_area_code");
                requestedParameterMap.remove("school_fax_office_code");
                requestedParameterMap.remove("school_fax_last_four");
            } else if (StringUtils.isBlank(faxNumberString)) {
                requestedParameterMap.put("school_fax", new String[]{""});
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

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}