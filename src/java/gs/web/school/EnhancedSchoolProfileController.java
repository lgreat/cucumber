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
     * Merges the lists under one or more given key names into a single token.
     * @param mapResponses
     * @param delim delimeter
     * @param keys
     * @return String or null
     */
    protected static String mergeValuesForKeys(Map<String, List<String>> mapResponses, String delim, String... keys) {
        ArrayList<Object> glist = new ArrayList<Object>();
        for(String key : keys) {
            List<?> list = mapResponses.get(key);
            if(list != null) glist.addAll(list);
        }
        String s = glist.size() == 0 ? null : StringUtils.joinPretty(glist.iterator(), delim);
        return s;
    }

    /**
     * Merges the lists under one or more given key "pairs" into a single token.
     * FORMAT: {keyPairs.key1}, {keyPairs.val1}; {keyPairs.key2}, {keyPairs.val2}; ...
     * @param mapResponses
     * @param subDelim
     * @param delim
     * @param keyPairs
     * @return String or null
     */
    protected static String mergeValuesForDualKeys(Map<String, List<String>> mapResponses, String subDelim, String delim, String[][] keyPairs) {
        ArrayList<String> slist = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for(String[] entry : keyPairs) {
            sb.setLength(0);
            List<?> listLft = mapResponses.get(entry[0]);
            List<?> listRgt = mapResponses.get(entry[1]);
            String lft = listLft == null ? null : StringUtils.joinPretty(listLft.iterator(), " ");
            String rgt = listRgt == null ? null : StringUtils.joinPretty(listRgt.iterator(), " ");
            if(lft != null && rgt != null && lft.length() > 0 && rgt.length() > 0) {
                sb.append(lft);
                sb.append(subDelim);
                sb.append(rgt);
                slist.add(sb.toString());
            }
        }
        String s = slist.size() == 0 ? null : StringUtils.joinPretty(slist.iterator(), delim);
        return s;
    }

    private IEspResponseDao _espResponseDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        School school = (School) httpServletRequest.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);

        // esp raw responses
        List<EspResponse> listResponses = _espResponseDao.getResponses(school);
        Map<String, List<String>> responses = EspResponse.toValueMap(listResponses, true);
        model.put("responses", responses);

        // merge feeder school fields
        String feederSchools = mergeValuesForKeys(responses, "; ", "feeder_school_1", "feeder_school_2", "feeder_school_3");
        model.put("feederSchools", feederSchools);

        // merge destination school fields
        String destinationSchools = mergeValuesForKeys(responses, "; ", "destination_school_1", "destination_school_2", "destination_school_3");
        model.put("destinationSchools", destinationSchools);
        
        // merge college destinations
        String collegeDestinations = mergeValuesForKeys(responses, "; ", "college_destination_1", "college_destination_2", "college_destination_3");
        model.put("collegeDestinations", collegeDestinations);

        // merge student clubs
        String studentClubs = mergeValuesForKeys(responses, "; ", "student_clubs", "student_clubs_other_1", "student_clubs_other_2", "student_clubs_other_3");
        model.put("studentClubs", studentClubs);

        // merge academic awards
        String academicAwards = mergeValuesForDualKeys(responses, ", ", "; ", new String[][] {
                { "academic_award_1", "academic_award_1_year" },
                { "academic_award_2", "academic_award_2_year" },
                { "academic_award_3", "academic_award_3_year" },
        });
        model.put("academicAwards", academicAwards);
        
        // merge community service awards
        String serviceAwards = mergeValuesForDualKeys(responses, ", ", "; ", new String[][] {
                { "service_award_1", "service_award_1_year" },
                { "service_award_2", "service_award_2_year" },
                { "service_award_3", "service_award_3_year" },
        });
        model.put("serviceAwards", serviceAwards);
        
        // merge foreign language keys
        String foreignLanguage = mergeValuesForKeys(responses, "; ", "foreign_language", "foreign_language_other");
        model.put("foreign_language", foreignLanguage);

        // merge staff foreign language keys
        String staffLanguages = mergeValuesForKeys(responses, "; ", "staff_languages", "staff_languages_other");
        model.put("staff_languages", staffLanguages);

        // merge boys sports keys
        String boysSports = mergeValuesForKeys(responses, "; ", "boys_sports", "boys_sports_other");
        model.put("boys_sports", boysSports);

        // merge girls sports keys
        String girlsSports = mergeValuesForKeys(responses, "; ", "girls_sports", "girls_sports_other");
        model.put("girls_sports", girlsSports);

        // merge college prep keys
        String collegePrep = mergeValuesForKeys(responses, "; ", "college_prep", "college_prep_other");
        model.put("college_prep", collegePrep);

        // obtain "external" datapoints
        ICensusInfo ci = school.getCensusInfo();
        SchoolCensusValue cv;
        String s = null;
              
        cv = ci.getEnrollment(school);
        if (cv != null && cv.getValueInteger() != null) {
            s = String.valueOf(cv.getValueInteger());
        }
        model.put("student_enrollment", s);

        model.put("grade_levels", school.getGradeLevels().getCommaSeparatedString().replace(",", "; ").replace("  ", " "));

        cv = ci.getManual(school, CensusDataType.STUDENTS_ETHNICITY);
        s = cv == null ? null : cv.getValueText();
        model.put("students_ethnicity", s);
        
        cv = ci.getManual(school, CensusDataType.HEAD_OFFICIAL_NAME);
        s = cv == null ? null : cv.getValueText();
        model.put("administrator_name", s);

        cv = ci.getManual(school, CensusDataType.HEAD_OFFICIAL_EMAIL);
        s = cv == null ? null : cv.getValueText();
        model.put("administrator_email", s);

        cv = ci.getManual(school, CensusDataType.STUDENTS_PERCENT_FREE_LUNCH);
        s = cv == null ? null : cv.getValueText();
        model.put("private_free_reduced_lunch", s);

        cv = ci.getManual(school, CensusDataType.STUDENTS_SPECIAL_EDUCATION);
        s = cv == null ? null : cv.getValueText();
        model.put("private_special_ed", s);
        
        return new ModelAndView("/school/enhancedSchoolProfile2", model);
    }

    @Override
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        return super.shouldHandleRequest(fields) && fields.getExtraResourceIdentifier() == ExtraResourceIdentifier.ESP_DISPLAY_PAGE;
    }

    public void setEspResponseDao(IEspResponseDao dao) {
        this._espResponseDao = dao;
    }
}
