package gs.web.school;

import gs.data.community.User;
import gs.data.geo.LatLon;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.util.Address;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import gs.data.json.IJSONDao;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.json.JSONArray;
import java.net.*;
import java.io.*;

/**
 * Workflow for adding external data point with 1-to-1 form-to-database mapping:
 * 1) Add key to getKeysForExternalData
 * 2) Add block in fetchExternalValues or getExternalValuesForKey to fetch value from DB for view
 * 3) Add block in saveExternalValue to save value from view into the DB
 *
 * @author aroy@greatschools.org
 */
@Component("espFormExternalDataHelper")
public class EspFormExternalDataHelper {
    private static final Log _log = LogFactory.getLog(EspFormExternalDataHelper.class);

    public static final String CENSUS_ETHNICITY_LABEL = "Your school's student ethnicity breakdown";
    public static final String CENSUS_STUDENTS_LIMITED_ENGLISH_LABEL =
            "Percentage of students participating in ELL or ESL services";
    public static final String CENSUS_STUDENTS_PERCENT_FREE_LUNCH_LABEL =
            "Percentage of students eligible for free or reduced-price lunch";
    public static final String CENSUS_STUDENTS_WITH_DISABILITIES_LABEL =
            "Percentage of students that take advantage of some type of Special Education service";
    public static final String CENSUS_STUDENTS_SPECIAL_EDUCATION_LABEL =
            "Percentage of students that take advantage of some type of Special Education service";

    public static final String DATA_DELIMITER = "-,-";

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private ICensusDataSetDao _dataSetDao;
    @Autowired
    private IJSONDao _jsonDao;
    @Autowired
    private CensusCacheManager _censusCacheManager;

    /**
     * Fetch the keys whose values live outside of esp_response and put them in responseMap
     * (overwriting existing keys if present).
     */
    public void fetchExternalValues(Map<String, EspFormResponseStruct> responseMap, School school,Set<String> externalKeys) {
        for (String key: externalKeys) {

            // for keys where the external data doesn't map 1:1 between EspResponse and the form, handle them here
            if (StringUtils.equals("address", key)) {
                insertEspFormResponseStructForAddress(responseMap, school);
            } else if (StringUtils.equals("school_phone", key)) {
                insertEspFormResponseStructForPhone(responseMap, school);
            } else if (StringUtils.equals("school_fax", key)) {
                insertEspFormResponseStructForFax(responseMap, school);
            } else if (StringUtils.equals("census_ethnicity", key)) {
                insertEspFormResponseStructForEthnicity(responseMap, school);
            } else {
                // for keys where external data DOES map 1:1 with the form fields, fetch data from external source here
                String[] vals = getExternalValuesForKey(key, school);
                if (vals != null && vals.length > 0) {
                    EspFormResponseStruct espResponse = new EspFormResponseStruct();
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
    }

    public void fetchExternalValues(Map<String, EspFormResponseStruct> responseMap, School school) {
        fetchExternalValues(responseMap, school, getKeysForExternalData(school)) ;
    }

    //TODO comment
    protected void fetchProvisionalExternalValues(Map<String, EspFormResponseStruct> responseMap, Map<String, String> KeyToValueMap) {
        for (String key : KeyToValueMap.keySet()) {
            String value = KeyToValueMap.get(key);

            //TODO is StringUtils.isNotBlank(value) required in all the below cases?
            if (key.equals("address") && StringUtils.isNotBlank(value)) {
                insertEspFormResponseStructForProvisionalAddress(responseMap, value);
            } else if (key.equals("school_phone") && StringUtils.isNotBlank(value)) {
                insertEspFormResponseStructForProvisionalPhone(responseMap, value);
            } else if (key.equals("school_fax") && StringUtils.isNotBlank(value)) {
                insertEspFormResponseStructForProvisionalFax(responseMap, value);
            } else if (key.equals("census_ethnicity")) {
                //TODO for page 8?
            } else {
                // for keys where external data DOES map 1:1 with the form fields, fetch data from external source here
                String[] vals = getProvisionalExternalValuesForKey(key, value);
                if (vals != null && vals.length > 0) {
                    EspFormResponseStruct espResponse = new EspFormResponseStruct();
                    for (String val : vals) {
                        espResponse.addValue(val);
                    }
                    responseMap.put(key, espResponse);
                } else {
                    // don't let esp_response values for external data show up on form
                    // external data has to come from external sources!
                    //TODO is this needed?
                    responseMap.remove(key);
                }

            }
        }
    }

    /**
     * Converts an <code>Address</code> on a <code>School</code> to multiple <code>EspFormResponseStruct</code>s and adds
     * them to the specified map
     */
    void insertEspFormResponseStructForAddress(Map<String, EspFormResponseStruct> responseMap, School school) {
        Address address = school.getPhysicalAddress();
        if (address != null) {
            EspFormResponseStruct streetStruct = new EspFormResponseStruct();
            streetStruct.addValue(address.getStreet());
            responseMap.put("physical_address_street", streetStruct);
            EspFormResponseStruct cityStruct = new EspFormResponseStruct();
            cityStruct.addValue(address.getCity());
            responseMap.put("physical_address_city", cityStruct);
            EspFormResponseStruct stateStruct = new EspFormResponseStruct();
            stateStruct.addValue(address.getState().getAbbreviation());
            responseMap.put("physical_address_state", stateStruct);
            EspFormResponseStruct zipStruct = new EspFormResponseStruct();
            zipStruct.addValue(address.getZip());
            responseMap.put("physical_address_zip", zipStruct);
            responseMap.remove("address");
        }
    }

    void insertEspFormResponseStructForPhone(Map<String, EspFormResponseStruct> responseMap, School school) {
        String phone = school.getPhone();
        if (StringUtils.length(phone) >= 14) {
            // "(510) 337-7022"
            String areaCode = phone.substring(1,4);
            EspFormResponseStruct areaCodeStruct = new EspFormResponseStruct();
            areaCodeStruct.addValue(areaCode);
            responseMap.put("school_phone_area_code", areaCodeStruct);

            String officeCode = phone.substring(6,9);
            EspFormResponseStruct officeCodeStruct = new EspFormResponseStruct();
            officeCodeStruct.addValue(officeCode);
            responseMap.put("school_phone_office_code", officeCodeStruct);

            String lastFour = phone.substring(10,14);
            EspFormResponseStruct lastFourStruct = new EspFormResponseStruct();
            lastFourStruct.addValue(lastFour);
            responseMap.put("school_phone_last_four", lastFourStruct);
        }
    }

    void insertEspFormResponseStructForFax(Map<String, EspFormResponseStruct> responseMap, School school) {
        String fax = school.getFax();
        if (StringUtils.length(fax) >= 14) {
            // "(510) 865-2194"
            String areaCode = fax.substring(1,4);
            EspFormResponseStruct areaCodeStruct = new EspFormResponseStruct();
            areaCodeStruct.addValue(areaCode);
            responseMap.put("school_fax_area_code", areaCodeStruct);

            String officeCode = fax.substring(6,9);
            EspFormResponseStruct officeCodeStruct = new EspFormResponseStruct();
            officeCodeStruct.addValue(officeCode);
            responseMap.put("school_fax_office_code", officeCodeStruct);

            String lastFour = fax.substring(10,14);
            EspFormResponseStruct lastFourStruct = new EspFormResponseStruct();
            lastFourStruct.addValue(lastFour);
            responseMap.put("school_fax_last_four", lastFourStruct);
        }
    }

    void insertEspFormResponseStructForEthnicity(Map<String, EspFormResponseStruct> responseMap, School school) {
        List<SchoolCensusValue> censusValues = school.getCensusInfo().getManualValues(school, CensusDataType.STUDENTS_ETHNICITY);
        if (censusValues != null) {
            for (SchoolCensusValue value: censusValues) {
                if (value.getDataSet() != null && value.getDataSet().getBreakdown() != null && value.getValueInteger() != null) {
                    Integer breakdownId = value.getDataSet().getBreakdown().getId();
                    EspFormResponseStruct ethnicityValue = new EspFormResponseStruct();
                    ethnicityValue.addValue(String.valueOf(value.getValueInteger()));
                    responseMap.put("ethnicity_" + breakdownId, ethnicityValue);
                }
            }
        }
    }

    String[] getProvisionalExternalValuesForKey(String key, String value) {
        if ((StringUtils.equals("student_enrollment", key)
                || StringUtils.equals("administrator_name", key)
                || StringUtils.equals("administrator_email", key)
                || StringUtils.equals("grade_levels", key)
                || StringUtils.equals("school_url", key)
                || StringUtils.equals("school_type", key)
                || StringUtils.equals("school_type_affiliation_other",key)
                || StringUtils.equals("school_video", key)
                || StringUtils.equals("facebook_url", key))
                && StringUtils.isNotBlank(value)) {
            return value.split(DATA_DELIMITER);
        }
        else if (StringUtils.equals("school_type_affiliation", key) && StringUtils.isNotBlank(value)) {
            //TODO  religious?
            return new String[]{value};
        }
//        else if (StringUtils.equals("school_type_affiliation_other", key) && StringUtils.isNotBlank(value)) {
//            return new String[]{value};
//        }
        else if (StringUtils.equals("coed", key) && StringUtils.isNotBlank(value)) {
            //TODO school.subtype?  and also  see the commented out code below
            return new String[]{value};
        }
//            if (school.getSubtype().contains("coed")) {
//                return new String[] {"coed"};
//            }
//            if (school.getSubtype().contains("all_male")) {
//                return new String[] {"all_boys"};
//            }
//            if (school.getSubtype().contains("all_female")) {
//                return new String[] {"all_girls"};
//            }
        else if (StringUtils.startsWith(key, "census_")) {
            //TODO census?
//            if (STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState()) != null) {
//                for (EspCensusDataTypeConfiguration dataTypeConfig: STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState())) {
//                    if (StringUtils.equals("census_" + dataTypeConfig.getId(), key)) {
//                        SchoolCensusValue value = school.getCensusInfo().getManualValue(school, CensusDataType.getEnum(dataTypeConfig.getId()));
//                        if (value != null && value.getValueInteger() != null) {
//                            _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
//                            return new String[]{String.valueOf(value.getValueInteger())};
//                        }
//                    }
//                }
//            } else {
//                _log.error("Missing census data type configuration for " + key + " in " + school.getDatabaseState());
//            }
        }
        return new String[0];
    }

    /**
     * Fetch external value, e.g. from census or school table. This returns an array of strings
     * representing the values for that key (e.g. {"KG", "1", "2"} for grade_level or {"100"} for enrollment
     */
    String[] getExternalValuesForKey(String key, School school) {
        if (StringUtils.equals("student_enrollment", key) && school.getEnrollment() != null) {
            _log.debug("Overwriting key " + key + " with value " + school.getEnrollment());
            return new String[]{String.valueOf(school.getEnrollment())};
        } else if (StringUtils.equals("administrator_name", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManual(school, CensusDataType.HEAD_OFFICIAL_NAME);
            if (value != null && value.getValueText() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueText());
                return new String[]{value.getValueText()};
            }
        } else if (StringUtils.equals("administrator_email", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManual(school, CensusDataType.HEAD_OFFICIAL_EMAIL);
            if (value != null && value.getValueText() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueText());
                return new String[]{value.getValueText()};
            }
        } else if (StringUtils.equals("grade_levels", key) && school.getGradeLevels() != null) {
            String gradeLevels = school.getGradeLevels().getCommaSeparatedString();
            return gradeLevels.split(",");
        } else if (StringUtils.equals("school_url", key) && school.getWebSite() != null) {
            return new String[] {school.getWebSite()};
        } else if (StringUtils.equals("school_type", key) && school.getType() != null) {
            return new String[]{school.getType().getSchoolTypeName()};
        } else if (StringUtils.equals("school_type_affiliation", key) && school.getSubtype() != null) {
            if (school.getSubtype().contains("religious")) {
                return new String[] {"religious"};
            }
        } else if (StringUtils.equals("school_type_affiliation_other", key)) {
            if (school.getAffiliation() != null) {
                return new String[] {school.getAffiliation()};
            }
        } else if (StringUtils.equals("coed", key) && school.getSubtype() != null) {
            if (school.getSubtype().contains("coed")) {
                return new String[] {"coed"};
            }
            if (school.getSubtype().contains("all_male")) {
                return new String[] {"all_boys"};
            }
            if (school.getSubtype().contains("all_female")) {
                return new String[] {"all_girls"};
            }
        } else if (StringUtils.startsWith(key, "census_")) {
            if (STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState()) != null) {
                for (EspCensusDataTypeConfiguration dataTypeConfig: STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState())) {
                    if (StringUtils.equals("census_" + dataTypeConfig.getId(), key)) {
                        SchoolCensusValue value = school.getCensusInfo().getManualValue(school, CensusDataType.getEnum(dataTypeConfig.getId()));
                        if (value != null && value.getValueInteger() != null) {
                            _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
                            return new String[]{String.valueOf(value.getValueInteger())};
                        }
                    }
                }
            } else {
                _log.error("Missing census data type configuration for " + key + " in " + school.getDatabaseState());
            }
        } else if (StringUtils.equals("school_video", key)) {
            List<String> schoolVideosAsList = school.getMetadataAsList("school_video");
            if( schoolVideosAsList != null ) {
                return schoolVideosAsList.toArray( new String[schoolVideosAsList.size()] );
            } else {
                return new String[0];
            }
        } else if (StringUtils.equals("facebook_url", key)) {
            String facebookUrl = school.getMetadataValue("facebook_url");
            if( facebookUrl != null ) {
                return new String[] {facebookUrl};
            } else {
                return new String[0];
            }
        }
        return new String[0];
    }

    //TODO comment
    void insertEspFormResponseStructForProvisionalAddress(Map<String, EspFormResponseStruct> responseMap, String addressStr) {
        //TODO put address handle in address.java?
        //TODO validate lengths etc  and also write tests with whats already in the db
        String streetLine1 = addressStr.substring(0,addressStr.indexOf(", \n"));
        int index = streetLine1.length()+3;
        if(addressStr.indexOf("\n",index) >=0 ){
            String streetLine2 = addressStr.substring(index,addressStr.indexOf("\n",index));
            if(!StringUtils.isBlank(streetLine2)){
                index += streetLine2.length()+1;
            }
        }
        String city = addressStr.substring(index,addressStr.indexOf(", ",index));
        index += city.length()+2;

        //TODO validate state
        String state = addressStr.substring(index,addressStr.indexOf("  ",index));
        index += state.length()+2;

        String zip = addressStr.substring(index,addressStr.length());

        EspFormResponseStruct streetStruct = new EspFormResponseStruct();
        streetStruct.addValue(streetLine1);
        responseMap.put("physical_address_street", streetStruct);
        EspFormResponseStruct cityStruct = new EspFormResponseStruct();
        cityStruct.addValue(city);
        responseMap.put("physical_address_city", cityStruct);
        EspFormResponseStruct stateStruct = new EspFormResponseStruct();
        stateStruct.addValue(state);
        responseMap.put("physical_address_state", stateStruct);
        EspFormResponseStruct zipStruct = new EspFormResponseStruct();
        zipStruct.addValue(zip);
        responseMap.put("physical_address_zip", zipStruct);

        responseMap.remove("address");
    }

    //TODO comments
    void insertEspFormResponseStructForProvisionalPhone(Map<String, EspFormResponseStruct> responseMap, String phoneStr) {
        //TODO validate lengths etc and write tests with whats already in the db
        String areaCode = phoneStr.substring(1,phoneStr.indexOf(")"));
        String officeCode = phoneStr.substring(phoneStr.indexOf(") ")+2,phoneStr.indexOf("-"));
        String lastFour = phoneStr.substring(phoneStr.indexOf("-")+1,phoneStr.length());

        EspFormResponseStruct areaCodeStruct = new EspFormResponseStruct();
        areaCodeStruct.addValue(areaCode);
        responseMap.put("school_phone_area_code", areaCodeStruct);

        EspFormResponseStruct officeCodeStruct = new EspFormResponseStruct();
        officeCodeStruct.addValue(officeCode);
        responseMap.put("school_phone_office_code", officeCodeStruct);

        EspFormResponseStruct lastFourStruct = new EspFormResponseStruct();
        lastFourStruct.addValue(lastFour);
        responseMap.put("school_phone_last_four", lastFourStruct);

        responseMap.remove("school_phone");
    }

    //TODO comments
    void insertEspFormResponseStructForProvisionalFax(Map<String, EspFormResponseStruct> responseMap, String faxStr) {
        //TODO validate lengths etc and write tests with whats already in the db
        String areaCode = faxStr.substring(1,faxStr.indexOf(")"));
        String officeCode = faxStr.substring(faxStr.indexOf(") ")+2,faxStr.indexOf("-"));
        String lastFour = faxStr.substring(faxStr.indexOf("-")+1,faxStr.length());

        EspFormResponseStruct areaCodeStruct = new EspFormResponseStruct();
        areaCodeStruct.addValue(areaCode);
        responseMap.put("school_fax_area_code", areaCodeStruct);

        EspFormResponseStruct officeCodeStruct = new EspFormResponseStruct();
        officeCodeStruct.addValue(officeCode);
        responseMap.put("school_fax_office_code", officeCodeStruct);

        EspFormResponseStruct lastFourStruct = new EspFormResponseStruct();
        lastFourStruct.addValue(lastFour);
        responseMap.put("school_fax_last_four", lastFourStruct);

        responseMap.remove("school_fax");
    }

    /**
     * Return error message on error.
     */
    //TODO comment about provisional data
    public String saveExternalValue(String key, Object[] values, School school, User user, Date now,
                                    boolean isProvisionalData) {
        if (values == null || values.length == 0 && school == null) {
            return null; // early exit
        }
        if (StringUtils.equals("student_enrollment", key)) {
            try {
                _log.debug("Saving student_enrollment elsewhere: " + values[0]);
                int censusData =  Integer.parseInt((String) values[0]);
                if (!isProvisionalData) {
                    saveCensusInteger(school, censusData, CensusDataType.STUDENTS_ENROLLMENT, user);
                }
            } catch (NumberFormatException nfe) {
                return "Must be an integer.";
            }
        } else if (StringUtils.equals("administrator_name", key)) {
            _log.debug("Saving administrator_name elsewhere: " + values[0]);
            if (!isProvisionalData) {
                saveCensusString(school, (String) values[0], CensusDataType.HEAD_OFFICIAL_NAME, user);
            }
        } else if (StringUtils.equals("administrator_email", key)) {
            _log.debug("Saving administrator_email elsewhere: " + values[0]);
            if (!isProvisionalData) {
                saveCensusString(school, (String) values[0], CensusDataType.HEAD_OFFICIAL_EMAIL, user);
            }
        } else if (StringUtils.equals("school_url", key)) {
            _log.debug("Saving school home_page_url elsewhere: " + values[0]);
            String url = (String) values[0];
            if (!StringUtils.equals(url, school.getWebSite()) && !isProvisionalData) {
                school.setWebSite(StringEscapeUtils.escapeHtml(url));
                saveSchool(school, user, now);
            }
        } else if (StringUtils.equals("grade_levels", key)) {
            _log.debug("Saving grade_levels " + Arrays.toString(values) + " elsewhere for school:" + school.getName());
            return saveGradeLevels(school, (String[])values, user, now, isProvisionalData);
        } else if (StringUtils.equals("school_type", key)) {
            _log.debug("Saving school type " + values[0] + " elsewhere for school:" + school.getName());
            return saveSchoolType(school, (String)values[0], user, now, isProvisionalData);
        } else if (StringUtils.equals("school_type_affiliation", key)) {
            _log.debug("Saving school sub type " + values[0] + " elsewhere for school:" + school.getName());
            if (!isProvisionalData) {
                school.getSubtype().remove("religious");
                String subtype = (String) values[0];
                if (StringUtils.equals("religious", subtype)) {
                    school.getSubtype().add("religious");
                }
                saveSchool(school, user, now);
            }
        } else if (StringUtils.equals("school_type_affiliation_other", key)) {
            _log.debug("Saving affiliation " + values[0] + " elsewhere for school:" + school.getName());
            String affiliation = (String) values[0];
            if (!StringUtils.equals(school.getAffiliation(), affiliation) && !isProvisionalData) {
                school.setAffiliation(StringEscapeUtils.escapeHtml(affiliation));
                saveSchool(school, user, now);
            }
        } else if (StringUtils.equals("coed", key)) {
            _log.debug("Saving school sub type " + values[0] + " elsewhere for school:" + school.getName());
            if(!isProvisionalData){
                school.getSubtype().remove("coed");
                school.getSubtype().remove("all_male");
                school.getSubtype().remove("all_female");
                String subtype = (String) values[0];
                if (StringUtils.equals("coed", subtype)) {
                    school.getSubtype().add("coed");
                } else if (StringUtils.equals("all_boys", subtype)) {
                    school.getSubtype().add("all_male");
                } else if (StringUtils.equals("all_girls", subtype)) {
                    school.getSubtype().add("all_female");
                }
                saveSchool(school, user, now);
            }
        } else if (StringUtils.equals("address", key)) {
            Address address = (Address) values[0];
            // only save if different
            if (!StringUtils.equals(school.getPhysicalAddress().getStreet(), address.getStreet()) && !isProvisionalData) {
                _log.debug("Saving physical address " + address.getStreet() + " elsewhere for school:" + school.getName());
                school.getPhysicalAddress().setStreet(StringEscapeUtils.escapeHtml(address.getStreet()));
                setSchoolLatLon(school);
                saveSchool(school, user, now);
            }
            return null;
        } else if (StringUtils.equals("school_phone", key)) {
            String phone = (String) values[0];
            // only save if different
            if (!StringUtils.equals(school.getPhone(), phone)) {
                if (containsBadChars(phone)) {
                    return "Contains invalid characters.";
                }
                if(!isProvisionalData){
                    school.setPhone(StringEscapeUtils.escapeHtml(phone));
                    saveSchool(school, user, now);
                }
            }
            return null;
        } else if (StringUtils.equals("school_fax", key)) {
            String fax = (String) values[0];
            // only save if different
            if (!StringUtils.equals(school.getFax(), fax)) {
                if (containsBadChars(fax)) {
                    return "Contains invalid characters.";
                }
                if(!isProvisionalData){
                    school.setFax(fax);
                    saveSchool(school, user, now);
                }
            }
            return null;
        } else if (StringUtils.equals("census_ethnicity", key)) {
            Map<Integer, Integer> breakdownIdToValueMap = (Map<Integer, Integer>) values[0];
            return handleEthnicity(breakdownIdToValueMap, school, user, isProvisionalData);
        } else if (StringUtils.startsWith(key, "census_")) {
            if (STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState()) != null) {
                for (EspCensusDataTypeConfiguration dataTypeConfig: STATE_TO_CENSUS_DATATYPES.get(school.getDatabaseState())) {
                    if (StringUtils.equals("census_" + dataTypeConfig.getId(), key)) {
                        return saveCensusPercentFromString
                                (school, CensusDataType.getEnum(dataTypeConfig.getId()), user, key, (String) values[0], isProvisionalData);
                    }
                }
            } else {
                _log.error("Missing census data type configuration for " + key + " in " + school.getDatabaseState());
            }
        } else if (StringUtils.equals("school_video", key)) {
            if(!isProvisionalData){
                updateSchoolMetadata( school, values, "school_video", user, now, false);
            }
        } else if (StringUtils.equals("facebook_url", key)) {
            if (!isProvisionalData) {
                updateSchoolMetadata(school, values, "facebook_url", user, now, true);
            }
        } else {
            _log.error("Unknown external key: " + key);
        }
        return null;
    }

    void updateSchoolMetadata(School school, Object[] pageValues, String metadataBaseKey, User user, Date now, boolean singleValued) {
        // Before replacing existing values, make sure there were changes
        List<String> existingList = new ArrayList<String>();
        if (singleValued) {
            String oldVal = school.getMetadataValue(metadataBaseKey);
            if (StringUtils.isNotBlank(oldVal)) {
                existingList.add(oldVal);
            }
        } else {
            existingList = school.getMetadataAsList(metadataBaseKey);
        }
        if (existingList == null) {
            existingList = new ArrayList<String>(0);
        }

        List<String> newList = new ArrayList<String>(pageValues.length);
        for (Object value : pageValues) {
            if (((String) value).length() > 0) {
                newList.add((String) value);
            }
        }
        boolean isSame = newList.equals(existingList);

        if (!isSame) {
            if (singleValued) {
                school.deleteMetadata(metadataBaseKey);
                if (!newList.isEmpty()) {
                    school.putMetadata(metadataBaseKey, newList.get(0));
                }
            } else {
                school.deleteMetadataList(metadataBaseKey);
                if (!newList.isEmpty()) {
                    school.putMetadataAsList(metadataBaseKey, newList);
                }
            }
            saveSchool(school, user, now);
        }
    }

    static boolean containsBadChars(String val) {
        return StringUtils.contains(val, "<") || StringUtils.contains(val, "\"");
    }

    String saveCensusPercentFromString(School school, CensusDataType censusDataType, User user, String key, String value,
                                       boolean isProvisionalData) {
        if (StringUtils.isBlank(value)) {
            if(!isProvisionalData){
                // deactivate existing value
                SchoolCensusValue manualValue = school.getCensusInfo().getManualValue(school, censusDataType);
                if (manualValue != null) {
                    _dataSetDao.deactivateDataValue(manualValue, getCensusModifiedBy(user));
                    _censusCacheManager.deleteBySchool(school);
                }
            }
        } else {
            try {
                _log.debug("Saving " + key + " elsewhere: " + value);
                Integer val = Integer.parseInt(value);
                if (val < 0 || val > 100) {
                    return "Must be a number between 0 and 100.";
                }
                if (!isProvisionalData) {
                    saveCensusInteger(school, val, censusDataType, user);
                }
            } catch (NumberFormatException nfe) {
                return "Must be an integer.";
            }
        }
        return null;
    }

    protected String getCensusModifiedBy(User user) {
        return "ESP-" + user.getId();
    }

    void saveCensusString(School school, String data, CensusDataType censusDataType, User user) {
        _dataSetDao.addValue(findOrCreateManualDataSet(school, censusDataType), school, StringEscapeUtils.escapeHtml(data), getCensusModifiedBy(user));
        _censusCacheManager.deleteBySchool(school);
    }

    void saveCensusInteger(School school, int data, CensusDataType censusDataType, User user) {
        _dataSetDao.addValue(findOrCreateManualDataSet(school, censusDataType), school, data, getCensusModifiedBy(user));
        _censusCacheManager.deleteBySchool(school);
    }

    CensusDataSet findOrCreateManualDataSet(School school, CensusDataType censusDataType) {
        return findOrCreateManualDataSet(school, censusDataType, null);
    }

    CensusDataSet findOrCreateManualDataSet(School school, CensusDataType censusDataType, Breakdown breakdown) {
        CensusDataSet dataSet = _dataSetDao.findDataSet(school.getDatabaseState(), censusDataType, 0, breakdown, null);
        if (dataSet == null) {
            dataSet = _dataSetDao.createDataSet(school.getDatabaseState(), censusDataType, 0, breakdown, null);
        }
        return dataSet;
    }

    String handleEthnicity(Map<Integer, Integer> breakdownIdToValueMap, School school, User user, boolean isProvisionalData) {
        int sum = 0;
        EspCensusDataTypeConfiguration ethnicityConfig = STATE_TO_ETHNICITY.get(school.getDatabaseState());
        if (ethnicityConfig == null) {
            _log.error("No ethnicity configuration for state " + school.getDatabaseState());
            return "Ethnicity data cannot be saved at this time.";
        }
        for (EspCensusBreakdownConfiguration breakdown: ethnicityConfig.getBreakdowns()) {
            Integer value = breakdownIdToValueMap.get(breakdown.getId());
            if (value != null) {
                if (value < 0) {
                    return "Please specify a positive number.";
                }
                sum += value;
            }
        }
        if (sum == 100) {
            if (!isProvisionalData) {
                for (EspCensusBreakdownConfiguration breakdownConfig : ethnicityConfig.getBreakdowns()) {
                    Integer breakdownId = breakdownConfig.getId();
                    Integer value = breakdownIdToValueMap.get(breakdownId);
                    Breakdown breakdown = new Breakdown(breakdownId);
                    CensusDataSet dataSet = findOrCreateManualDataSet(school, CensusDataType.STUDENTS_ETHNICITY, breakdown);
                    _dataSetDao.addValue(dataSet, school, value, getCensusModifiedBy(user));
                    _censusCacheManager.deleteBySchool(school);
                }
            }
        } else if (sum == 0) {
            if (!isProvisionalData) {
                List<SchoolCensusValue> censusValues = school.getCensusInfo().getManualValues(school, CensusDataType.STUDENTS_ETHNICITY);
                for (SchoolCensusValue val : censusValues) {
                    _dataSetDao.deactivateDataValue(val, getCensusModifiedBy(user));
                    _censusCacheManager.deleteBySchool(school);
                }
            }
        } else {
            return "Your ethnicity percents must add up to 100%.";
        }
        return null;
    }

    /**
     * Calculate the new lat, lon if the school address has been changed by the user.
     */
    void setSchoolLatLon(School school) {
        if (school != null && school.getPhysicalAddress() != null) {
            Address schoolAddress = school.getPhysicalAddress();
            if (StringUtils.isNotBlank(schoolAddress.getStreet()) && StringUtils.isNotBlank(schoolAddress.getCity())
                    && schoolAddress.getState() != null && StringUtils.isNotBlank(schoolAddress.getZip())) {
                Float lat = null;
                Float lon = null;
                String address = schoolAddress.getStreet() + "," + schoolAddress.getCity() + "," + schoolAddress.getState().getAbbreviation() + " " + schoolAddress.getZip();
                try {
                    String geocodeUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" +
                            URLEncoder.encode(address, "UTF-8") + "&sensor=false";
                    URL url = new URL(geocodeUrl);
                    JSONObject result = _jsonDao.fetch(url, "UTF-8");
                    String status = result.getString("status");
                    if ("OK".equals(status)) {
                        JSONArray results = result.getJSONArray("results");

                        if (results.length() == 1) {
                            JSONObject firstMatch = results.getJSONObject(0);
                            String stateStr = null;
                            JSONArray addressComponents = firstMatch.getJSONArray("address_components");
                            for (int x=0; x < addressComponents.length(); x++) {
                                JSONObject component = addressComponents.getJSONObject(x);
                                JSONArray types = component.getJSONArray("types");
                                for (int y=0; y < types.length(); y++) {
                                    if (StringUtils.equals("administrative_area_level_1", types.getString(y))) {
                                        stateStr = component.getString("short_name");
                                        break;
                                    }
                                }
                                if (stateStr != null) {break;}
                            }
                            if (StringUtils.equalsIgnoreCase(school.getPhysicalAddress().getState().getAbbreviation(), stateStr)) {
                                JSONObject location = firstMatch.getJSONObject("geometry").getJSONObject("location");

                                lon = Float.parseFloat(location.getString("lng"));
                                lat = Float.parseFloat(location.getString("lat"));
                                // bounds check
                                if (LatLon.isSuspectLat(lat) || LatLon.isSuspectLon(lon)) {
                                    _log.error("Google returned out of bounds lat/lon for address " + address);
                                    lon = null;
                                    lat = null;
                                }
                            } else {
                                _log.error("Geocoded address state " + stateStr +
                                        " does not match physical address state " +
                                        school.getPhysicalAddress().getState() + " for address " + address);
                            }
                        } else {
                            _log.error("Google returns multiple results for address " + address);
                        }
                    } else {
                        _log.error("Google returns unknown status code " + status + " for address " + address);
                    }
                } catch (IOException ioE) {
                    _log.error("IO Exception while geo coding address " + address + ": " + ioE, ioE);
                } catch (JSONException jsonE) {
                    _log.error("JSON Exception while geo coding address " + address + ": " + jsonE, jsonE);
                } catch (Exception e) {
                    _log.error("Exception while geo coding address " + address + ": " + e, e);
                } finally {
                    school.setLat(lat);
                    school.setLon(lon);
                }
            }
        }
    }

    /**
     * Save grade levels to the db. Return error string if necessary.
     */
    String saveGradeLevels(School school, String[] data, User user, Date now, boolean isProvisionalData) {
        List<String> gradesList = new ArrayList<String>();
        Collections.addAll(gradesList, data);
        if (!gradesList.isEmpty()) {
            Grades grades = Grades.createGrades(StringUtils.join(gradesList, ","));
            if (StringUtils.isBlank(grades.getCommaSeparatedString())) {
                return "You must select a grade level.";
            }
            LevelCode newLevelCode = LevelCode.createLevelCode(grades, school.getName());
            if (newLevelCode.equals(LevelCode.PRESCHOOL)) {
                return "You can not set preschool as your only grade.";
            }
            if (!isProvisionalData) {
                // GS-12570 Preserve 'UG'
                if (school.getGradeLevels().contains(Grade.UNGRADED)) {
                    grades.addLevel(Grade.UNGRADED);
                }
                if (!grades.equals(school.getGradeLevels())) {
                    school.setGradeLevels(grades);
                    school.setLevelCode(newLevelCode);
                    saveSchool(school, user, now);
                }
            }

        } else {
            return "You must select a grade level.";
        }
        return null;
    }

    void saveSchool(School school, User user, Date now) {
        String modifiedBy = getCensusModifiedBy(user);
        school.setManualEditBy(modifiedBy);
        school.setManualEditDate(now);
        school.setModified(now);
        _schoolDao.saveSchool(school.getDatabaseState(), school, modifiedBy);
    }

    /**
     * Save grade levels to the db
     */
    String saveSchoolType(School school, String data, User user, Date now, boolean isProvisionalData) {
        SchoolType type = SchoolType.getSchoolType(data);
        if (type != null) {
            // only save if different
            if (type != school.getType() && !isProvisionalData) {
                school.setType(type);
                saveSchool(school, user, now);
            }
        } else {
            return "Must select a valid school type.";
        }
        return null;
    }

    /**
     * The set of keys that exist in external places.
     * @param school This might depend on the type or other attribute of the school.
     */
    public Set<String> getKeysForExternalData(School school) {
        Set<String> keys = new HashSet<String>();
        keys.add("student_enrollment");
        keys.add("grade_levels");
        keys.add("school_type");
        keys.add("school_type_affiliation");
        keys.add("school_type_affiliation_other");
        keys.add("coed");
        keys.add("address");
        keys.add("school_phone");
        keys.add("school_fax");
        keys.add("school_url");
        keys.add("administrator_name");
        keys.add("administrator_email");
        keys.add("facebook_url");
        keys.add("school_video");

        if (school.getType() == SchoolType.PRIVATE) {
            keys.add("census_ethnicity");
            keys.add("census_6");
            keys.add("census_8");
            keys.add("census_15");
            keys.add("census_22");
        }
        return keys;
    }


    protected static class EspCensusBreakdownConfiguration {
        private Integer _id;
        private String _name;

        public EspCensusBreakdownConfiguration(Integer id, String name) {
            _id = id;
            _name = name;
        }

        public Integer getId() {
            return _id;
        }

        public String getName() {
            return _name;
        }
    }

    protected static class EspCensusDataTypeConfiguration {
        private Integer _id;
        private String _name;
        private String _genericName;
        private List<EspCensusBreakdownConfiguration> _breakdowns;

        public EspCensusDataTypeConfiguration(Integer id, String name, String genericName) {
            this(id, name, new ArrayList<EspCensusBreakdownConfiguration>());
            _genericName = genericName;
        }

        public EspCensusDataTypeConfiguration(Integer id, String name, List<EspCensusBreakdownConfiguration> breakdowns) {
            _id = id;
            _name = name;
            _breakdowns = breakdowns;
        }

        public Integer getId() {
            return _id;
        }

        public String getName() {
            return _name;
        }

        public String getGenericName() {
            return _genericName;
        }

        public List<EspCensusBreakdownConfiguration> getBreakdowns() {
            return _breakdowns;
        }
    }

    public static final Map<State, List<EspCensusDataTypeConfiguration>> STATE_TO_CENSUS_DATATYPES
            = new HashMap<State, List<EspCensusDataTypeConfiguration>>() {{
        put(State.WI, Arrays.asList(
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_LIMITED_ENGLISH.getId(), "English Proficient",
                            CENSUS_STUDENTS_LIMITED_ENGLISH_LABEL),
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_PERCENT_FREE_LUNCH.getId(), "Economically Disadvantaged",
                            CENSUS_STUDENTS_PERCENT_FREE_LUNCH_LABEL),
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_WITH_DISABILITIES.getId(), "Disabled",
                            CENSUS_STUDENTS_WITH_DISABILITIES_LABEL)
        ));

        put(State.DC, Arrays.asList(
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_LIMITED_ENGLISH.getId(), "ELL",
                            CENSUS_STUDENTS_LIMITED_ENGLISH_LABEL),
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_PERCENT_FREE_LUNCH.getId(), "Free and Reduced Lunch",
                            CENSUS_STUDENTS_PERCENT_FREE_LUNCH_LABEL),
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_SPECIAL_EDUCATION.getId(), "Special Education",
                            CENSUS_STUDENTS_SPECIAL_EDUCATION_LABEL)
        ));

        put(State.IN, Arrays.asList(
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_LIMITED_ENGLISH.getId(), "ELL",
                            CENSUS_STUDENTS_LIMITED_ENGLISH_LABEL),
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_PERCENT_FREE_LUNCH.getId(), "Free or Reduced Price Meals",
                            CENSUS_STUDENTS_PERCENT_FREE_LUNCH_LABEL),
            new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_SPECIAL_EDUCATION.getId(), "Special Ed",
                            CENSUS_STUDENTS_SPECIAL_EDUCATION_LABEL)
        ));
    }};

    public static final Map<State, EspCensusDataTypeConfiguration> STATE_TO_ETHNICITY =
            new HashMap<State, EspCensusDataTypeConfiguration>() {{
            put(State.WI, new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_ETHNICITY.getId(), CENSUS_ETHNICITY_LABEL,
                            Arrays.asList(
                                    new EspCensusBreakdownConfiguration(4, "American Indian"),
                                    new EspCensusBreakdownConfiguration(7, "Asian"),
                                    new EspCensusBreakdownConfiguration(2, "Black"),
                                    new EspCensusBreakdownConfiguration(3, "Hispanic"),
                                    new EspCensusBreakdownConfiguration(209, "Native Hawaiian or Other Pacific Islander"),
                                    new EspCensusBreakdownConfiguration(1, "White"),
                                    new EspCensusBreakdownConfiguration(6, "Two or More")))
            );
            put(State.DC, new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_ETHNICITY.getId(), CENSUS_ETHNICITY_LABEL,
                            Arrays.asList(
                                    new EspCensusBreakdownConfiguration(10, "American Indian/Alaskan Native"),
                                    new EspCensusBreakdownConfiguration(5, "Asian/Pacific Islander"),
                                    new EspCensusBreakdownConfiguration(2, "Black"),
                                    new EspCensusBreakdownConfiguration(3, "Hispanic"),
                                    new EspCensusBreakdownConfiguration(1, "White"),
                                    new EspCensusBreakdownConfiguration(6, "Multiracial")))
            );
            put(State.IN, new EspCensusDataTypeConfiguration
                    (CensusDataType.STUDENTS_ETHNICITY.getId(), CENSUS_ETHNICITY_LABEL,
                            Arrays.asList(
                                    new EspCensusBreakdownConfiguration(4, "American Indian"),
                                    new EspCensusBreakdownConfiguration(7, "Asian"),
                                    new EspCensusBreakdownConfiguration(2, "Black"),
                                    new EspCensusBreakdownConfiguration(3, "Hispanic"),
                                    new EspCensusBreakdownConfiguration(209, "Native Hawaiian or Other Pacific Islander"),
                                    new EspCensusBreakdownConfiguration(1, "White"),
                                    new EspCensusBreakdownConfiguration(6, "Multiracial")))
            );
    }};
}
