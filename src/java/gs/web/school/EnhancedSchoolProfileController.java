package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.School;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.util.string.StringUtils;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.path.DirectoryStructureUrlFields.ExtraResourceIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Backs the display of ESP response data for a particular school.
 * @author jkirton
 */
public class EnhancedSchoolProfileController extends AbstractSchoolController implements IDirectoryStructureUrlController {
    
    /**
     * Appends the given string value list with either the raw value or pretty
     * value of the given EspResponse objects.
     * @param responses
     * @param values list receiving the
     * @param pretty pretty or raw esp response value?
     */
    protected static void extractEspResponseValues(List<EspResponse> responses, List<String> values, boolean pretty) {
        for(EspResponse r : responses) {
            if(r != null) values.add(pretty ? r.getPrettyValue() : r.getSafeValue());
        }
    }

    /**
     * Merges the lists under one or more given key names into a single list.
     * @param mapResponses
     * @param pretty use the esp response pretty value or the raw value?
     * @param keys
     * @return the extracted esp response values
     */
    protected static List<String> mergeValuesForKeys(Map<String, List<EspResponse>> mapResponses, boolean pretty, String... keys) {
        ArrayList<String> vlist = new ArrayList<String>();
        for(String key : keys) {
            List<EspResponse> responses = mapResponses.get(key);
            if(responses != null) extractEspResponseValues(responses, vlist, pretty);
        }
        return vlist;
    }

    /**
     * Merges the lists under one or more given key names into a single list. It also Handles adding another response value to a given value.
     * Example if u want "student_clubs" to be merged and within that want the response_value "dance" to be merged with value of the response_key "dance_style"
     * then the Map specialValuesToKeys = key=>"dance" value=>"dance_style"
     *
     * @param mapResponses
     * @param pretty                use the esp response pretty value or the raw value?
     * @param specialValuesToKeys   - Map of value to key.Map that tracks whether a value needs another response value added to it.
     * @param prettyForSpecialValue - use the esp response pretty value or the raw value for special
     * @param keys
     * @return the extracted esp response values
     */
    protected List<String> mergeValuesForKeys(Map<String, List<EspResponse>> mapResponses,
                                              boolean pretty, Map specialValuesToKeys, boolean prettyForSpecialValue, String... keys) {
        ArrayList<String> vlist = new ArrayList<String>();

        for (String key : keys) {
            List<EspResponse> responses = mapResponses.get(key);
            if (responses == null) {
                continue;
            }
            for (EspResponse response : responses) {
                if (response == null || response.getSafeValue() == null) {
                    continue;
                }
                String originalResponse = pretty ? response.getPrettyValue() : response.getSafeValue();
                String newValue = mergeValuesForValues(mapResponses, response, specialValuesToKeys, prettyForSpecialValue, originalResponse);
                vlist.add(newValue);
            }
        }
        return vlist;
    }

    protected String mergeValuesForValues(Map<String, List<EspResponse>> mapResponses, EspResponse response, Map specialValuesToKeys,
                                          boolean prettyForSpecialValue, String originalResponseValue) {
        String newValue = originalResponseValue;
        if (specialValuesToKeys != null && specialValuesToKeys.get(response.getSafeValue()) != null) {
            String specialKey = (String) specialValuesToKeys.get(response.getSafeValue());
            List<EspResponse> specialResponseToAdd = mapResponses.get(specialKey);

            if (specialResponseToAdd != null && !specialResponseToAdd.isEmpty()) {
                for (EspResponse specialResponse : specialResponseToAdd) {
                    if (specialResponse == null) {
                        continue;
                    }
                    String s = prettyForSpecialValue ? specialResponse.getPrettyValue() : specialResponse.getSafeValue();
                    if (org.apache.commons.lang.StringUtils.isNotBlank(s)) {
                        newValue += ", " + s;
                    }
                }
            }
        }
        return newValue;
    }

    /**
     * Merges the lists under one or more given key "pairs" into a single token.
     * FORMAT: {keyPairs.key1}, {keyPairs.val1}; {keyPairs.key2}, {keyPairs.val2}; ...
     * @param responses
     * @param subDelim
     * @param delim
     * @param pretty use the pretty esp response value or the raw value?
     * @param keyPairs
     * @return String or null
     */
    protected static String mergeValuesForDualKeys(Map<String, List<EspResponse>> responses, String subDelim, String delim, boolean pretty, String[][] keyPairs) {
        ArrayList<String> slist = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for(String[] entry : keyPairs) {
            sb.setLength(0);
            List<EspResponse> listLft = responses.get(entry[0]);
            List<EspResponse> listRgt = responses.get(entry[1]);
            List<String> lftValueList = new ArrayList<String>(), rgtValueList = new ArrayList<String>();
            if(listLft != null) {
                extractEspResponseValues(listLft, lftValueList, pretty);                
                String lft = StringUtils.joinPretty(lftValueList.iterator(), " ");
                String rgt = null;
                if(listRgt != null) {
                    extractEspResponseValues(listRgt, rgtValueList, pretty);
                    rgt = StringUtils.joinPretty(rgtValueList.iterator(), " ");
                }
                if(lft != null && lft.length() > 0) {
                    sb.append(lft);
                    if(rgt != null && rgt.length() > 0) {
                        sb.append(subDelim);
                        sb.append(rgt);
                    }
                    slist.add(sb.toString());
                }
            }
        }
        String s = slist.size() == 0 ? null : StringUtils.joinPretty(slist.iterator(), delim);
        return s;
    }

    /**
     * Returns the value of the first response if there is one.  Null otherwise.
     * @param responses List of responses
     * @param pretty use the pretty esp response value or the raw value?
     * @return String or null
     */
    protected String getFirstValue(List<EspResponse> responses, boolean pretty) {
        if (responses == null) {
            return null;
        }

        EspResponse response = responses.get(0);
        if (response == null) {
            return null;
        }

        return pretty ? response.getPrettyValue() : response.getSafeValue();
    }

    /**
     * Same as new profile overview controller
     * Helper function to go through a list of EspResponse objects looking for one of the specified values
     * @param espResponses The EspResponse objects to check
     * @param valuesToLookFor The values to look for
     * @return True if any value is found in the EspResponses
     */
    private boolean checkEspResponseListForValue(List<EspResponse> espResponses, String[] valuesToLookFor) {
        if( (espResponses==null) || (espResponses.size()==0) ) {
            return false;
        }

        for( String val : valuesToLookFor ) {
            for( EspResponse r : espResponses ) {
                if( r.getValue().equals( val ) ) {
                    return true;    // Found, we are done
                }
            }
        }
        return false;   // If we get here the answer no match was found
    }

    private IEspResponseDao _espResponseDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        School school = (School) httpServletRequest.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);

        // GS-12514 Catch bookmarks from old pqview and send them to new URL
        String requestUrl = UrlUtil.getRequestURL(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.contains(requestUrl, "/school/enhancedSchoolProfile.page")) {
            UrlBuilder canonicalUrl = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP_DISPLAY);
            return new ModelAndView(new RedirectView301(canonicalUrl.asFullUrl(httpServletRequest)));
        }

        // GS-13082 Redirect to new profile if eligible
        if (shouldRedirectToNewProfile(school)) {
            return getRedirectToNewProfileModelAndView(school, httpServletRequest, NewProfileTabs.programsCulture);
        }

        // esp raw responses
        List<EspResponse> listResponses = _espResponseDao.getResponses(school);
        Map<String, List<EspResponse>> responses = EspResponse.rollup(listResponses);
        model.put("responses", responses);

        List<EspResponse> beforeAfterCare = responses.get("before_after_care");
        model.put("hasBeforeCare", checkEspResponseListForValue(beforeAfterCare, new String[]{"before"}));
        model.put("hasAfterCare", checkEspResponseListForValue(beforeAfterCare, new String[]{"after"}));
        model.put("hasNoBeforeAfterCare", checkEspResponseListForValue(beforeAfterCare, new String[]{"neither"}));

        boolean admissionsContactSchool = false;
        if (org.apache.commons.lang.StringUtils.equals(getFirstValue(responses.get("admissions_contact_school"), false), "yes")) {
            admissionsContactSchool = true;
        }

        model.put("admissions_contact_school", admissionsContactSchool);

        List<String> values;

        // merge feeder school fields
        values = mergeValuesForKeys(responses, false, "feeder_school_1", "feeder_school_2", "feeder_school_3");
        model.put("feederSchools", StringUtils.joinPretty(values.iterator(), "; "));

        // merge destination school fields
        values = mergeValuesForKeys(responses, false, "destination_school_1", "destination_school_2", "destination_school_3");
        model.put("destinationSchools", StringUtils.joinPretty(values.iterator(), "; "));
        
        // merge college destinations
        values = mergeValuesForKeys(responses, false, "college_destination_1", "college_destination_2", "college_destination_3");
        model.put("collegeDestinations", StringUtils.joinPretty(values.iterator(), "; "));

        values = new ArrayList<String>();
        
        // merge instructional_model
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "instructional_model"));
        values.addAll(mergeValuesForKeys(responses, false, "instructional_model_other"));
        model.put("instructional_model", StringUtils.joinPretty(values.iterator(), "; "));

        // merge academic_focus
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "academic_focus"));
        values.addAll(mergeValuesForKeys(responses, false, "academic_focus_other"));
        model.put("academic_focus", StringUtils.joinPretty(values.iterator(), "; "));

        // merge extra_learning_resources
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "extra_learning_resources"));
        values.addAll(mergeValuesForKeys(responses, false, "extra_learning_resources_other"));
        model.put("extra_learning_resources", StringUtils.joinPretty(values.iterator(), "; "));

        // merge skills_training
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "skills_training"));
        values.addAll(mergeValuesForKeys(responses, false, "skills_training_other"));
        model.put("skills_training", StringUtils.joinPretty(values.iterator(), "; "));

        // merge transportation
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "transportation"));
        values.addAll(mergeValuesForKeys(responses, false, "transportation_other"));
        model.put("transportation", StringUtils.joinPretty(values.iterator(), "; "));

        // merge immersion_language
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "immersion_language"));
        values.addAll(mergeValuesForKeys(responses, false, "immersion_language_other"));
        model.put("immersion_language", StringUtils.joinPretty(values.iterator(), "; "));

        // merge student clubs
        values.clear();
        Map specialValues = new HashMap();
        specialValues.put("dance","student_clubs_dance");
        specialValues.put("language_club","student_clubs_language");

        values.addAll(mergeValuesForKeys(responses, true, specialValues, false, "student_clubs"));
        values.addAll(mergeValuesForKeys(responses, false, "student_clubs_other_1", "student_clubs_other_2", "student_clubs_other_3"));
        model.put("student_clubs", StringUtils.joinPretty(values.iterator(), "; "));

        // merge transportation
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "parent_involvement"));
        values.addAll(mergeValuesForKeys(responses, false, "parent_involvement_other"));
        model.put("parent_involvement", StringUtils.joinPretty(values.iterator(), "; "));

        // merge foreign language keys
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "foreign_language"));
        values.addAll(mergeValuesForKeys(responses, false, "foreign_language_other"));
        model.put("foreign_language", StringUtils.joinPretty(values.iterator(), "; "));

        // merge staff foreign language keys
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "staff_languages"));
        values.addAll(mergeValuesForKeys(responses, false, "staff_languages_other"));
        model.put("staff_languages", StringUtils.joinPretty(values.iterator(), "; "));

        // merge staff foreign language keys
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "financial_aid_type"));
        values.addAll(mergeValuesForKeys(responses, false, "financial_aid_type_other"));
        model.put("financial_aid_type", StringUtils.joinPretty(values.iterator(), "; "));

        // merge boys sports keys
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "boys_sports"));
        values.addAll(mergeValuesForKeys(responses, false, "boys_sports_other"));
        model.put("boys_sports", StringUtils.joinPretty(values.iterator(), "; "));

        // merge girls sports keys
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "girls_sports"));
        values.addAll(mergeValuesForKeys(responses, false, "girls_sports_other"));
        model.put("girls_sports", StringUtils.joinPretty(values.iterator(), "; "));

        // merge college prep keys
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "college_prep"));
        values.addAll(mergeValuesForKeys(responses, false, "college_prep_other"));
        model.put("college_prep", StringUtils.joinPretty(values.iterator(), "; "));

        // merge academic awards
        String academicAwards = mergeValuesForDualKeys(responses, ", ", "; ", false, new String[][] {
                { "academic_award_1", "academic_award_1_year" },
                { "academic_award_2", "academic_award_2_year" },
                { "academic_award_3", "academic_award_3_year" },
        });
        model.put("academicAwards", academicAwards);

        // merge community service awards
        String serviceAwards = mergeValuesForDualKeys(responses, ", ", "; ", false, new String[][] {
                { "service_award_1", "service_award_1_year" },
                { "service_award_2", "service_award_2_year" },
                { "service_award_3", "service_award_3_year" },
        });
        model.put("serviceAwards", serviceAwards);



        
        // obtain "external" datapoints
        ICensusInfo ci = school.getCensusInfo();
        SchoolCensusValue cv;
        String s = null;
              
        cv = ci.getEnrollment(school);
        if (cv != null && cv.getValueInteger() != null) {
            s = String.valueOf(cv.getValueInteger());
        }
        model.put("student_enrollment", s);

        model.put("grade_levels", school.getGradeLevels().getRangeString().replace(",", "; ").replace("  ", " "));

        model.put("school_phone", school.getPhone());

        // commenting out private data points as they are not correct and not in scope for 19.6
//        cv = ci.getManual(school, CensusDataType.STUDENTS_ETHNICITY);
//        s = cv == null ? null : cv.getValueText();
//        model.put("students_ethnicity", s);

        cv = ci.getManual(school, CensusDataType.HEAD_OFFICIAL_NAME);
        s = cv == null ? null : cv.getValueText();
        model.put("administrator_name", s);

        cv = ci.getManual(school, CensusDataType.HEAD_OFFICIAL_EMAIL);
        s = cv == null ? null : cv.getValueText();
        model.put("administrator_email", s);

//        cv = ci.getManual(school, CensusDataType.STUDENTS_PERCENT_FREE_LUNCH);
//        s = cv == null ? null : cv.getValueText();
//        model.put("private_free_reduced_lunch", s);
//
//        cv = ci.getManual(school, CensusDataType.STUDENTS_SPECIAL_EDUCATION);
//        s = cv == null ? null : cv.getValueText();
//        model.put("private_special_ed", s);

        // subtypes
        String coed = null;
        if (school.getSubtype().contains("coed")) {
            coed = "Coed";
        } else if (school.getSubtype().contains("all_male")) {
            coed = "All boys";
        } else if (school.getSubtype().contains("all_female")) {
            coed = "All girls";
        }
        model.put("coed", coed);

        //school video
        handleSchoolVideos(model,school);

        _schoolProfileHeaderHelper.updateModel(httpServletRequest, httpServletResponse, school, model);

        return new ModelAndView("/school/enhancedSchoolProfile2", model);
    }

    protected void handleSchoolVideos(Map<String, Object> model, School school) {
        List<String> schoolVideos = school.getMetadataAsList(School.METADATA_SCHOOL_VIDEO);
        if (schoolVideos != null && !schoolVideos.isEmpty()) {
            model.put("schoolVideo", schoolVideos.get(0).toString().trim());
        }
    }

    @Override
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        return super.shouldHandleRequest(fields) && fields.getExtraResourceIdentifier() == ExtraResourceIdentifier.ESP_DISPLAY_PAGE;
    }

    public void setEspResponseDao(IEspResponseDao dao) {
        this._espResponseDao = dao;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }
}