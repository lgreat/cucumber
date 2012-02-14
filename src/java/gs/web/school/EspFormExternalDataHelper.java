package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusDataSetDao;
import gs.data.school.census.SchoolCensusValue;
import gs.data.util.Address;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import gs.data.admin.cobrand.ICobrandDao;
import gs.data.admin.cobrand.Cobrand;
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
    private static final String G_GEO_SUCCESS = "200";

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private ICensusDataSetDao _dataSetDao;
    @Autowired
    private ICobrandDao _cobrandDao;
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

    /**
     * Fetch external value, e.g. from census or school table. This returns an array of strings
     * representing the values for that key (e.g. {"KG", "1", "2"} for grade_level or {"100"} for enrollment
     */
    String[] getExternalValuesForKey(String key, School school) {
        if (StringUtils.equals("student_enrollment", key) && school.getEnrollment() != null) {
            _log.debug("Overwriting key " + key + " with value " + school.getEnrollment());
            return new String[]{String.valueOf(school.getEnrollment())};
        } else if (StringUtils.equals("average_class_size", key)) {
            SchoolCensusValue value = school.getCensusInfo().getManual(school, CensusDataType.CLASS_SIZE);
            if (value != null && value.getValueInteger() != null) {
                _log.debug("Overwriting key " + key + " with value " + value.getValueInteger());
                return new String[]{String.valueOf(value.getValueInteger())};
            }
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
//            if (school.getSubtype().contains("independent_study")) {
//                return new String[] {"independent_study"};
//            }
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
        } else if (StringUtils.equals("average_class_size", key)) {
            try {
                _log.debug("Saving average_class_size elsewhere: " + values[0]);
                saveCensusInteger(school, Integer.parseInt((String)values[0]), CensusDataType.CLASS_SIZE, user);
            } catch (NumberFormatException nfe) {
                return "Must be an integer.";
            }
        } else if (StringUtils.equals("administrator_name", key)) {
            _log.debug("Saving administrator_name elsewhere: " + values[0]);
            saveCensusString(school, (String) values[0], CensusDataType.HEAD_OFFICIAL_NAME, user);
        } else if (StringUtils.equals("administrator_email", key)) {
            _log.debug("Saving administrator_email elsewhere: " + values[0]);
            saveCensusString(school, (String) values[0], CensusDataType.HEAD_OFFICIAL_EMAIL, user);
        } else if (StringUtils.equals("school_url", key)) {
            _log.debug("Saving school home_page_url elsewhere: " + values[0]);
            String url = (String) values[0];
            if (!StringUtils.equals(url, school.getWebSite())) {
                school.setWebSite(url);
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
//            school.getSubtype().remove("independent_study");
            String subtype = (String) values[0];
            if (StringUtils.equals("religious", subtype)) {
                school.getSubtype().add("religious");
//            } else if (StringUtils.equals("independent_study", subtype)) {
//                school.getSubtype().add("independent_study");
            }
            saveSchool(school, user, now);
        } else if (StringUtils.equals("school_type_affiliation_other", key)) {
            _log.error("Saving affiliation " + values[0] + " elsewhere for school:" + school.getName());
            String affiliation = (String) values[0];
            if (!StringUtils.equals(school.getAffiliation(), affiliation)) {
                school.setAffiliation(affiliation);
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
                school.getPhysicalAddress().setStreet((address.getStreet()));
                //Todo
                setSchoolLatLon(school);
                saveSchool(school, user, now);
            }
            return null;
        } else if (StringUtils.equals("school_phone", key)) {
            String phone = (String) values[0];
            // only save if different
            if (!StringUtils.equals(school.getPhone(), phone)) {
                school.setPhone(phone);
                saveSchool(school, user, now);
            }
            return null;
        } else if (StringUtils.equals("school_fax", key)) {
            String fax = (String) values[0];
            // only save if different
            if (!StringUtils.equals(school.getFax(), fax)) {
                school.setFax(fax);
                saveSchool(school, user, now);
            }
            return null;
        }
        return null;
    }

    void saveCensusString(School school, String data, CensusDataType censusDataType, User user) {
        _dataSetDao.addValue(findOrCreateManualDataSet(school, censusDataType), school, data, "ESP-" + user.getId());
    }

    void saveCensusInteger(School school, int data, CensusDataType censusDataType, User user) {
        _dataSetDao.addValue(findOrCreateManualDataSet(school, censusDataType), school, data, "ESP-" + user.getId());
    }

    CensusDataSet findOrCreateManualDataSet(School school, CensusDataType censusDataType) {
        CensusDataSet dataSet = _dataSetDao.findDataSet(school.getDatabaseState(), censusDataType, 0, null, null);
        if (dataSet == null) {
            dataSet = _dataSetDao.createDataSet(school.getDatabaseState(), censusDataType, 0, null, null);
        }
        return dataSet;
    }

    /**
     * Calculate the new lat, lon if the school address has been changed by the user.
     */
    void setSchoolLatLon(School school) {
        Float lat = null;
        Float lon = null;

        if (school != null && school.getPhysicalAddress() != null) {
            Address schoolAddress = school.getPhysicalAddress();

            if (StringUtils.isNotBlank(schoolAddress.getStreet()) && StringUtils.isNotBlank(schoolAddress.getCity())
                    && schoolAddress.getState() != null && StringUtils.isNotBlank(schoolAddress.getZip())) {

                String address = schoolAddress.getStreet() + "," + schoolAddress.getCity() + "," + schoolAddress.getState().toString() + "," + schoolAddress.getZip();
                try {
                    String geocodeUrl = "http://maps.google.com/maps/geo?q=" + URLEncoder.encode(address, "UTF-8") +
                            "&output=json&sensor=false" + (getGoogleApiKey() != null ? "&key=" + getGoogleApiKey() : "");
                    URL url = new URL(geocodeUrl);
                    JSONObject result = _jsonDao.fetch(url, "UTF-8");
                    JSONObject status = result.getJSONObject("Status");
                    String code = status.getString("code");

                    if (G_GEO_SUCCESS.equals(code)) {
                        JSONArray placemarks = result.getJSONArray("Placemark");

                        if (placemarks.length() == 1) {
                            JSONObject firstMatch = placemarks.getJSONObject(0);
                            JSONObject addressDetails = firstMatch.getJSONObject("AddressDetails");
                            JSONObject country = addressDetails.getJSONObject("Country");
                            // get the state
                            JSONObject adminArea = country.getJSONObject("AdministrativeArea");
                            String stateAbbrev = adminArea.getString("AdministrativeAreaName");
                            // Extra check to ensure that the geo coded state is the same as the school state.

                            if (StringUtils.isNotBlank(stateAbbrev) && school.getStateAbbreviation() != null
                                    && stateAbbrev.equals(school.getStateAbbreviation().getAbbreviation())) {
                                JSONObject point = firstMatch.getJSONObject("Point");
                                JSONArray coordinates = point.getJSONArray("coordinates");
                                if (coordinates.length() == 3) {
                                    lon = Float.parseFloat(coordinates.getString(0));
                                    lat = Float.parseFloat(coordinates.getString(1));
                                }
                            }
                        }
                    }

                } catch (IOException ioE) {
                    _log.error("IO Exception while geo coding " + ioE);
                } catch (JSONException jsonE) {
                    _log.error("JSON Exception while geo coding " + jsonE);
                } catch (Exception e) {
                    _log.error("Exception while geo coding " + e);
                } finally {
                    school.setLat(lat);
                    school.setLon(lon);
                }
            }
        }
    }

    protected String getGoogleApiKey() {
        String googleApiKey;
        //TODO what should the server name be?
        //TODO move this into a more common place.
        Cobrand c = _cobrandDao.getCobrandByHostname("dev.greatschools.org");
        if (c == null) {
            return null;
        } else {
            googleApiKey = c.getGoogleMapsKey();
        }
        return googleApiKey;
    }

    /**
     * Save grade levels to the db. Return error string if necessary.
     */
    String saveGradeLevels(School school, String[] data, User user, Date now) {
        List<String> gradesList = new ArrayList<String>();
        Collections.addAll(gradesList, data);
        if (!gradesList.isEmpty()) {
            Grades grades = Grades.createGrades(StringUtils.join(gradesList, ","));
            if (grades.containsOnly(Grade.PRESCHOOL)) {
                return "You can not set preschool as your only grade.";
            } else if (StringUtils.isBlank(grades.getCommaSeparatedString())) {
                return "You must select a grade level.";
            }
            if (!grades.equals(school.getGradeLevels())) {
                school.setGradeLevels(grades);
                school.setLevelCode(LevelCode.createLevelCode(grades, school.getName()));
                saveSchool(school, user, now);
            }
        } else {
            return "You must select a grade level.";
        }
        return null;
    }

    void saveSchool(School school, User user, Date now) {
        String modifiedBy = "ESP-" + user.getId();
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
        return keys;
    }
}
