package gs.web.compare;

import gs.data.school.*;
import gs.data.survey.*;
import gs.data.util.NameValuePair;
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
    private ISurveyDao _surveyDao;
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
            // PQ takes precedence over parent surveys
            if (!keyToResponseListMap.isEmpty()) {
//                _log.warn("  Found PQ");
                struct.setProgramSource(Principal);
                processESPResults(struct, keyToResponseListMap);
            } else if (!school.isSchoolForNewProfile()) { // GS-13226
//                _log.warn("  Did not find PQ, checking for survey results");
                Set<LevelCode.Level> levels = school.getLevelCode().getIndividualLevelCodes();
                List<SurveyResults> allResultsForSchool = new ArrayList<SurveyResults>();
                for (LevelCode.Level level: levels) {
//                    _log.warn("  Looking for " + level.getLongName() + " surveys");
                    SurveyResults results = _surveyDao.getSurveyResultsForSchool(level.getName(), baseStruct.getSchool());
                    if (results != null && results.getTotalResponses() > 0) {
                        allResultsForSchool.add(results);
                    }
                }
                processSurveyResults(struct, allResultsForSchool);
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

    protected void processSurveyResults(ComparedSchoolProgramsExtracurricularsStruct school,
                                        List<SurveyResults> allResultsForSchool) {
//        _log.warn("  Found " + allResultsForSchool.size() + " survey result(s)");

        for (SurveyResults results: allResultsForSchool) {
//            _log.warn("  Processing survey results with " + results.getTotalResponses() + " response(s)");
            school.setNumResponses(school.getNumResponses() + results.getTotalResponses());
            for (SurveyResultPage page: results.getPages()) {
//                _log.warn("    Processing page " + page.getName());
                for (SurveyResultGroup group: page.getGroups()) {
//                    _log.warn("      Processing group " + group.getDisplayText());
                    for (SurveyResultQuestion question: group.getQuestions()) {
//                        _log.warn("        Processing question " + question.getQuestion().getId());
                        try {
                            if ("COMPLEX".equals(question.getDisplayType())) {
                                for (Answer answer: question.getQuestion().getAnswers()) {
                                    String key = "q" + question.getQuestion().getId() + "a" + answer.getId();
                                    if (_questionAnswerToLabelMap.get(key) != null) {
//                                        _log.warn("          Found " + key);
                                        Map<Object, Integer> responseValuesMap = question.getResponseValuesAsMap();
                                        for (AnswerValue answerValue: answer.getAnswerValues()) {
                                            Integer numPositiveResponses = responseValuesMap.get(answerValue.getSymbol());
                                            if (numPositiveResponses != null && numPositiveResponses > 0) {
//                                                _log.warn("            Adding \"" + answerValue.getDisplay() + "\" to " + _questionAnswerToLabelMap.get(key));
                                                school.getCategoryResponses().get(_questionAnswerToLabelMap.get(key)).add(answerValue.getDisplay());
                                            }
                                        }
                                    }
                                }
                            } else if ("LINKED".equals(question.getDisplayType())) {
                                SurveyResultLinkedQuestion linkedQuestion = (SurveyResultLinkedQuestion) question;
                                String key = "q" + linkedQuestion.getQuestion1().getId();
                                if (StringUtils.equals(_questionAnswerToLabelMap.get(key), ROW_LABEL_BEFORE_AFTER_SCHOOL)) {
//                                    _log.warn("          Found " + key);
                                    if (linkedQuestion.isShowResponses()) {
                                        for (String answerValue: linkedQuestion.getQuestion1ResponseValuesAsMap().keySet()) {
                                            if (linkedQuestion.getQuestion1ResponseValuesAsMap().get(answerValue) > 0) {
                                                if (StringUtils.equalsIgnoreCase("before school", answerValue)) {
                                                    school.getCategoryResponses().get(_questionAnswerToLabelMap.get(key)).add("Before-school care");
                                                } else if (StringUtils.equalsIgnoreCase("after school", answerValue)) {
                                                    school.getCategoryResponses().get(_questionAnswerToLabelMap.get(key)).add("After-school care");
                                                } else if (StringUtils.equalsIgnoreCase("none", answerValue)) {
                                                    school.getCategoryResponses().get(_questionAnswerToLabelMap.get(key)).add("No extended care");
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    key = "q" + linkedQuestion.getQuestion2().getId();
                                    if (_questionAnswerToLabelMap.get(key) != null) {
//                                        _log.warn("          Found " + key);
                                        if (linkedQuestion.isShowResponses()) {
                                            for (NameValuePair<String, Integer> answerValue: linkedQuestion.getResponseValuesAsList()) {
//                                                _log.warn("            " + answerValue.getKey() + ":" + answerValue.getValue());
                                                if (answerValue.getValue() > 0) {
                                                    if (StringUtils.contains(answerValue.getKey(), "self-contained")) {
                                                        school.getCategoryResponses()
                                                                .get(_questionAnswerToLabelMap.get(key))
                                                                .add("Self-contained");
                                                    } else if (StringUtils.contains(answerValue.getKey(), "pull-out")) {
                                                        school.getCategoryResponses()
                                                                .get(_questionAnswerToLabelMap.get(key))
                                                                .add("Pull-out");
                                                    } else if (StringUtils.contains(answerValue.getKey(), "full inclusion")) {
                                                        school.getCategoryResponses()
                                                                .get(_questionAnswerToLabelMap.get(key))
                                                                .add("Full inclusion");
                                                    }
                                                }
                                            }
                                        }
                                   }
                                }
                            }
                        } catch (Exception e) {
                            _log.error("Error processing question " + question.getQuestion().getId() + ": " + e, e);
                        }
                    }
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

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
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
