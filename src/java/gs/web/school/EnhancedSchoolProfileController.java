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
            if(r != null) values.add(pretty ? r.getPrettyValue() : r.getValue());
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
            if(listLft != null && listRgt != null) {
                extractEspResponseValues(listLft, lftValueList, pretty);
                extractEspResponseValues(listRgt, rgtValueList, pretty);
                String lft = StringUtils.joinPretty(lftValueList.iterator(), " ");
                String rgt = StringUtils.joinPretty(rgtValueList.iterator(), " ");
                if(lft != null && rgt != null && lft.length() > 0 && rgt.length() > 0) {
                    sb.append(lft);
                    sb.append(subDelim);
                    sb.append(rgt);
                    slist.add(sb.toString());
                }
            }
        }
        String s = slist.size() == 0 ? null : StringUtils.joinPretty(slist.iterator(), delim);
        return s;
    }

    private IEspResponseDao _espResponseDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        School school = (School) httpServletRequest.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);

        // esp raw responses
        List<EspResponse> listResponses = _espResponseDao.getResponses(school);
        Map<String, List<EspResponse>> responses = EspResponse.rollup(listResponses);
        model.put("responses", responses);

        List<String> values;

        // merge feeder school fields
        values = mergeValuesForKeys(responses, true, "feeder_school_1", "feeder_school_2", "feeder_school_3");
        model.put("feederSchools", StringUtils.joinPretty(values.iterator(), "; "));

        // merge destination school fields
        values = mergeValuesForKeys(responses, true, "destination_school_1", "destination_school_2", "destination_school_3");
        model.put("destinationSchools", StringUtils.joinPretty(values.iterator(), "; "));
        
        // merge college destinations
        values = mergeValuesForKeys(responses, true, "college_destination_1", "college_destination_2", "college_destination_3");
        model.put("collegeDestinations", StringUtils.joinPretty(values.iterator(), "; "));

        values = new ArrayList<String>();
        
        // merge instructional_model
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "instructional_model"));
        values.addAll(mergeValuesForKeys(responses, false, "instructional_model_other"));
        model.put("instructional_model", StringUtils.joinPretty(values.iterator(), "; "));

        // merge transportation
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "transportation"));
        values.addAll(mergeValuesForKeys(responses, false, "transportation_other"));
        model.put("transportation", StringUtils.joinPretty(values.iterator(), "; "));

        // merge student clubs
        values.clear();
        values.addAll(mergeValuesForKeys(responses, true, "student_clubs"));
        values.addAll(mergeValuesForKeys(responses, false, "student_clubs_other_1", "student_clubs_other_2", "student_clubs_other_3"));
        model.put("studentClubs", StringUtils.joinPretty(values.iterator(), "; "));

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
        String academicAwards = mergeValuesForDualKeys(responses, ", ", "; ", true, new String[][] {
                { "academic_award_1", "academic_award_1_year" },
                { "academic_award_2", "academic_award_2_year" },
                { "academic_award_3", "academic_award_3_year" },
        });
        model.put("academicAwards", academicAwards);
        
        // merge community service awards
        String serviceAwards = mergeValuesForDualKeys(responses, ", ", "; ", true, new String[][] {
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

        _schoolProfileHeaderHelper.updateModel(httpServletRequest, httpServletResponse, school, model);

        return new ModelAndView("/school/enhancedSchoolProfile2", model);
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
