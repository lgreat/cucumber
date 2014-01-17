package gs.web.compare;

import gs.data.school.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static gs.web.compare.ComparedSchoolProgramsExtracurricularsStruct.SourceType.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareProgramsExtracurricularsController extends AbstractCompareSchoolController {
    private final Log _log = LogFactory.getLog(getClass());
    public static final String TAB_NAME = "programsExtracurriculars";
    private String _successView;
    private IEspResponseDao _espResponseDao;

    public static final String ROW_LABEL_ARTS = "Arts &amp; activities";
    public static final String ROW_LABEL_SPORTS = "Sports";
    public static final String ROW_LABEL_LANGUAGES = "Languages taught";
//    public static final String ROW_LABEL_SPECIAL_PROGRAMS = "Other special programs";
    public static final String ROW_LABEL_VOCATIONAL = "Vocational programs";
    public static final String ROW_LABEL_BEFORE_AFTER_SCHOOL = "Before/After school care";
    public static final String ROW_LABEL_LEARNING_DISABILITIES = "Special education";

    private static Map<String, String> _questionAnswerToLabelMap = new HashMap<String, String>() {
        {
            put("q1a1", ROW_LABEL_ARTS);
            put("q34a1", ROW_LABEL_ARTS);

            put("q1a3", ROW_LABEL_SPORTS);
            put("q34a3", ROW_LABEL_SPORTS);

            put("q1a2", ROW_LABEL_LANGUAGES);
            put("q34a2", ROW_LABEL_LANGUAGES);

            put("q1a4", ROW_LABEL_ARTS);
            put("q34a4", ROW_LABEL_ARTS);
//            put("q1a4", ROW_LABEL_SPECIAL_PROGRAMS);
//            put("q34a4", ROW_LABEL_SPECIAL_PROGRAMS);

            put("q1a5", ROW_LABEL_VOCATIONAL);

            put("q8", ROW_LABEL_BEFORE_AFTER_SCHOOL);

            put("q23", ROW_LABEL_LEARNING_DISABILITIES);
        }
    };
    
    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools,
                                        Map<String, Object> model) {
        model.put(MODEL_TAB, TAB_NAME);

        // these are the potential categories to collect data into
        List<String> categories = new ArrayList<String>();
        categories.add(ROW_LABEL_BEFORE_AFTER_SCHOOL);
        categories.add(ROW_LABEL_SPORTS);
        categories.add(ROW_LABEL_LANGUAGES);
        categories.add(ROW_LABEL_ARTS);
//        categories.add(ROW_LABEL_SPECIAL_PROGRAMS);
        categories.add(ROW_LABEL_LEARNING_DISABILITIES);
        categories.add(ROW_LABEL_VOCATIONAL);

        Map<String, Boolean> categoryHasResults = new HashMap<String, Boolean>();

        for (ComparedSchoolBaseStruct baseStruct: schools) {
            ComparedSchoolProgramsExtracurricularsStruct struct =
                    (ComparedSchoolProgramsExtracurricularsStruct) baseStruct;
//            _log.warn("Processing " + baseStruct.getName() + " (" + baseStruct.getUniqueIdentifier() + ")");

            Map<String, Set<String>> categoryResponses = new HashMap<String, Set<String>>();
            for (String category: categories) {
                categoryResponses.put(category, new TreeSet<String>());
            }
            struct.setCategoryResponses(categoryResponses);

            School school = baseStruct.getSchool();
            List<EspResponse> espResponses = _espResponseDao.getResponses(school);
            Map<String, List<EspResponse>> keyToResponseListMap = EspResponse.rollup(espResponses);
            if (!keyToResponseListMap.isEmpty()) {
//                _log.warn("  Found PQ");
                struct.setProgramSource(Principal);
                processESPResults(struct, keyToResponseListMap);
            }
            // now determine which categories have results for this school
            for (String category: categories) {
                if (struct.getCategoryResponses().get(category).size() > 0) {
                    categoryHasResults.put(category, true);
                }
            }
        }
        // any categories that results for any of the schools should be displayed
        // others are not shown
        List<String> categoriesForDisplay = new ArrayList<String>();
        for (String category: categories) {
            if (categoryHasResults.get(category) != null) {
                categoriesForDisplay.add(category);
            }
        }
        model.put("categories", categoriesForDisplay);
    }

    protected void processESPResults(ComparedSchoolProgramsExtracurricularsStruct school, Map<String, List<EspResponse>> keyToResponseListMap) {
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_ARTS), keyToResponseListMap.get("arts_media"));
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_ARTS), keyToResponseListMap.get("arts_music"));
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_ARTS), keyToResponseListMap.get("arts_performing_written"));
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_ARTS), keyToResponseListMap.get("arts_visual"));
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_SPORTS),
                keyToResponseListMap.get("boys_sports"), keyToResponseListMap.get("boys_sports_other"));
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_SPORTS), 
                keyToResponseListMap.get("girls_sports"), keyToResponseListMap.get("girls_sports_other"));
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_LANGUAGES),
                keyToResponseListMap.get("foreign_language"), keyToResponseListMap.get("foreign_language_other"));
        if (listContains(keyToResponseListMap.get("before_after_care"), "before")) {
            school.getCategoryResponses().get(ROW_LABEL_BEFORE_AFTER_SCHOOL).add("Before-school care");
        }
        if (listContains(keyToResponseListMap.get("before_after_care"), "after")) {
            school.getCategoryResponses().get(ROW_LABEL_BEFORE_AFTER_SCHOOL).add("After-school care");
        }
        parseESPValues(school.getCategoryResponses().get(ROW_LABEL_LEARNING_DISABILITIES), keyToResponseListMap.get("special_ed_programs"));
    }
    
    protected boolean listContains(List<EspResponse> responses, String value) {
        if (responses != null && responses.size() > 0) {
            for (EspResponse response: responses) {
                if (StringUtils.equals(value, response.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void parseESPValues(Set<String> category, List<EspResponse> espResponses) {
        parseESPValues(category, espResponses, null);
    }

    protected void parseESPValues(Set<String> category, List<EspResponse> espResponses, List<EspResponse> espOtherResponses) {
        if (espResponses != null && espResponses.size() > 0) {
            for (EspResponse response: espResponses) {
                category.add(response.getPrettyValue());
            }
        }
        if (espOtherResponses != null && espOtherResponses.size() > 0) {
            for (EspResponse response: espOtherResponses) {
                for (String value: response.getSafeValue().split(",")) {
                    String cleanedUpValue = StringUtils.capitalize(StringUtils.trim(value));
                    category.add(cleanedUpValue);
                }
            }
        }
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolProgramsExtracurricularsStruct();
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

    @Override
    protected String getTabName() {
        return TAB_NAME;
    }
}
