package gs.web.school;

import gs.data.community.User;
import gs.data.geo.LatLon;
import gs.data.school.*;
import gs.data.school.census.*;
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

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private ICensusDataSetDao _dataSetDao;
    @Autowired
    private IJSONDao _jsonDao;

    /**
     * Fetch the keys whose values live outside of esp_response and put them in responseMap
     * (overwriting existing keys if present).
     */
    public void fetchExternalValues(Map<String, EspFormResponseStruct> responseMap, School school) {
        for (String key: getKeysForExternalData(school)) {

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
            // TODO: what if the phone isn't 14 digits? DB limit = 31
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
            // TODO: what if the fax isn't 14 digits? DB limit = 31
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
        } else if (StringUtils.equals("census_frpl", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManualValue(school, CensusDataType
                    .STUDENTS_PERCENT_FREE_LUNCH);
            if (value != null && value.getValueInteger() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
                return new String[]{String.valueOf(value.getValueInteger())};
            }
        } else if (StringUtils.equals("census_ell_esl", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManualValue(school, CensusDataType.STUDENTS_LIMITED_ENGLISH);
            if (value != null && value.getValueInteger() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
                return new String[]{String.valueOf(value.getValueInteger())};
            }
        } else if (StringUtils.equals("census_special_ed", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManualValue(school, CensusDataType.STUDENTS_SPECIAL_EDUCATION);
            if (value != null && value.getValueInteger() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
                return new String[]{String.valueOf(value.getValueInteger())};
            }
        } else if (StringUtils.equals("census_disabilities", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManualValue(school, CensusDataType.STUDENTS_WITH_DISABILITIES);
            if (value != null && value.getValueInteger() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
                return new String[]{String.valueOf(value.getValueInteger())};
            }
        }
        return new String[0];
    }

    /**
     * Return error message on error.
     */
    public String saveExternalValue(String key, Object[] values, School school, User user, Date now) {
        if (values == null || values.length == 0 && school == null) {
            return null; // early exit
        }
        if (StringUtils.equals("student_enrollment", key)) {
            try {
                _log.debug("Saving student_enrollment elsewhere: " + values[0]);
                saveCensusInteger(school, Integer.parseInt((String)values[0]), CensusDataType.STUDENTS_ENROLLMENT, user);
            } catch (NumberFormatException nfe) {
                return "Must be an integer.";
            }
        } else if (StringUtils.equals("administrator_name", key)) {
            String value = (String) values[0];
            _log.debug("Saving administrator_name elsewhere: " + values[0]);
            saveCensusString(school, (String) values[0], CensusDataType.HEAD_OFFICIAL_NAME, user);
        } else if (StringUtils.equals("administrator_email", key)) {
            _log.debug("Saving administrator_email elsewhere: " + values[0]);
            saveCensusString(school, (String) values[0], CensusDataType.HEAD_OFFICIAL_EMAIL, user);
        } else if (StringUtils.equals("school_url", key)) {
            _log.debug("Saving school home_page_url elsewhere: " + values[0]);
            String url = (String) values[0];
            if (!StringUtils.equals(url, school.getWebSite())) {
                school.setWebSite(StringEscapeUtils.escapeHtml(url));
                saveSchool(school, user, now);
            }
        } else if (StringUtils.equals("grade_levels", key)) {
            _log.debug("Saving grade_levels " + Arrays.toString(values) + " elsewhere for school:" + school.getName());
            return saveGradeLevels(school, (String[])values, user, now);
        } else if (StringUtils.equals("school_type", key)) {
            _log.debug("Saving school type " + values[0] + " elsewhere for school:" + school.getName());
            return saveSchoolType(school, (String)values[0], user, now);
        } else if (StringUtils.equals("school_type_affiliation", key)) {
            _log.error("Saving school sub type " + values[0] + " elsewhere for school:" + school.getName());
            school.getSubtype().remove("religious");
            String subtype = (String) values[0];
            if (StringUtils.equals("religious", subtype)) {
                school.getSubtype().add("religious");
            }
            saveSchool(school, user, now);
        } else if (StringUtils.equals("school_type_affiliation_other", key)) {
            _log.error("Saving affiliation " + values[0] + " elsewhere for school:" + school.getName());
            String affiliation = (String) values[0];
            if (!StringUtils.equals(school.getAffiliation(), affiliation)) {
                school.setAffiliation(StringEscapeUtils.escapeHtml(affiliation));
                saveSchool(school, user, now);
            }
        } else if (StringUtils.equals("coed", key)) {
            _log.error("Saving school sub type " + values[0] + " elsewhere for school:" + school.getName());
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
        } else if (StringUtils.equals("address", key)) {
            Address address = (Address) values[0];
            // only save if different
            if (!StringUtils.equals(school.getPhysicalAddress().getStreet(), address.getStreet())) {
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
                school.setPhone(StringEscapeUtils.escapeHtml(phone));
                saveSchool(school, user, now);
            }
            return null;
        } else if (StringUtils.equals("school_fax", key)) {
            String fax = (String) values[0];
            // only save if different
            if (!StringUtils.equals(school.getFax(), fax)) {
                if (containsBadChars(fax)) {
                    return "Contains invalid characters.";
                }
                school.setFax(fax);
                saveSchool(school, user, now);
            }
            return null;
        } else if (StringUtils.equals("census_ethnicity", key)) {
            Map<Integer, Integer> breakdownIdToValueMap = (Map<Integer, Integer>) values[0];
            return handleEthnicity(breakdownIdToValueMap, school, user);
        } else if (StringUtils.equals("census_frpl", key)) {
            return saveCensusPercentFromString(school, CensusDataType.STUDENTS_PERCENT_FREE_LUNCH, user, key, (String) values[0]);
        } else if (StringUtils.equals("census_ell_esl", key)) {
            return saveCensusPercentFromString(school, CensusDataType.STUDENTS_LIMITED_ENGLISH, user, key, (String) values[0]);
        } else if (StringUtils.equals("census_special_ed", key)) {
            return saveCensusPercentFromString(school, CensusDataType.STUDENTS_SPECIAL_EDUCATION, user, key, (String) values[0]);
        } else if (StringUtils.equals("census_disabilities", key)) {
            return saveCensusPercentFromString(school, CensusDataType.STUDENTS_WITH_DISABILITIES, user, key, (String) values[0]);
        } else {
            _log.error("Unknown external key: " + key);
        }
        return null;
    }
    
    static boolean containsBadChars(String val) {
        return StringUtils.contains(val, "<") || StringUtils.contains(val, "\"");
    } 
    
    String saveCensusPercentFromString(School school, CensusDataType censusDataType, User user, String key, String value) {
        if (StringUtils.isBlank(value)) {
            // deactivate existing value
            SchoolCensusValue manualValue = school.getCensusInfo().getManualValue(school, censusDataType);
            if (manualValue != null) {
                _dataSetDao.deactivateDataValue(manualValue, getCensusModifiedBy(user));
            }
        } else {
            try {
                _log.debug("Saving " + key + " elsewhere: " + value);
                Integer val = Integer.parseInt(value);
                if (val < 0 || val > 100) {
                    return "Must be a number between 0 and 100.";
                }
                saveCensusInteger(school, val, censusDataType, user);
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
    }

    void saveCensusInteger(School school, int data, CensusDataType censusDataType, User user) {
        _dataSetDao.addValue(findOrCreateManualDataSet(school, censusDataType), school, data, getCensusModifiedBy(user));
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

    String handleEthnicity(Map<Integer, Integer> breakdownIdToValueMap, School school, User user) {
        int sum = 0;
        for (Integer value: breakdownIdToValueMap.values()) {
            if (value < 0) {
                return "Please specify a positive number.";
            }
            sum += value;
        }
        if (sum == 100) {
            for (Integer breakdownId: breakdownIdToValueMap.keySet()) {
                Integer value = breakdownIdToValueMap.get(breakdownId);
                Breakdown breakdown = new Breakdown(breakdownId);
                CensusDataSet dataSet = findOrCreateManualDataSet(school, CensusDataType.STUDENTS_ETHNICITY, breakdown);
                _dataSetDao.addValue(dataSet, school, value, getCensusModifiedBy(user));
            }
        } else if (sum == 0) {
            List<SchoolCensusValue> censusValues = school.getCensusInfo().getManualValues(school, CensusDataType.STUDENTS_ETHNICITY);
            for (SchoolCensusValue val: censusValues) {
                _dataSetDao.deactivateDataValue(val, getCensusModifiedBy(user));
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
    String saveGradeLevels(School school, String[] data, User user, Date now) {
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
            // GS-12570 Preserve 'UG'
            if (school.getGradeLevels().contains(Grade.UNGRADED)) {
                grades.addLevel(Grade.UNGRADED);
            }
            if (!grades.equals(school.getGradeLevels())) {
                school.setGradeLevels(grades);
                school.setLevelCode(newLevelCode);
                saveSchool(school, user, now);
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
    String saveSchoolType(School school, String data, User user, Date now) {
        SchoolType type = SchoolType.getSchoolType(data);
        if (type != null) {
            // only save if different
            if (type != school.getType()) {
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
//        if (school.getType() == SchoolType.PRIVATE && EspFormController.isFruitcakeSchool(school)) {
            keys.add("census_ethnicity");
            keys.add("census_ell_esl");
            keys.add("census_frpl");
            keys.add("census_special_ed");
            keys.add("census_disabilities");
//        }
        return keys;
    }
}
