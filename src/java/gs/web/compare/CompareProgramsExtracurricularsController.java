package gs.web.compare;

import gs.data.school.IPQDao;
import gs.data.school.LevelCode;
import gs.data.school.PQ;
import gs.data.school.School;
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
    private IPQDao _PQDao;

    public static final String ROW_LABEL_ARTS = "Arts &amp; activities";
    public static final String ROW_LABEL_SPORTS = "Sports";
    public static final String ROW_LABEL_LANGUAGES = "Languages taught";
//    public static final String ROW_LABEL_SPECIAL_PROGRAMS = "Other special programs";
    public static final String ROW_LABEL_VOCATIONAL = "Vocational programs";
    public static final String ROW_LABEL_BEFORE_AFTER_SCHOOL = "Before/After school care";
    public static final String ROW_LABEL_LEARNING_DISABILITIES = "Learning disabilities";

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
    
    private static Map<String, String> _pqArtsMap = new HashMap<String, String>() {
        {
            put("b", "Band");
            put("o", "Orchestra");
            put("r", "Private music lessons");
            put("t", "Theater/drama");
            put("c", "Chorus");
            put("p", "Photography");
            put("d", "Drawing/painting");
            put("e", "Ceramics");
            put("n", "Dance");
            put("v", "Video/film production");
            put("l", "Electronics/technology");
            put("g", "Gardening");
            put("y", "Yearbook");
            put("s", "Student newspaper");
            put("h", "Physical education");
            put("i", "Creative writing");
        }
    };

    private static Map<String, String> _pqLanguagesMap = new HashMap<String, String>() {
        {
            put("c", "Cantonese");
            put("e", "English");
            put("f", "French");
            put("g", "German");
            put("i", "Italian");
            put("j", "Japanese");
            put("k", "Korean");
            put("m", "Mandarin");
            put("r", "Russian");
            put("s", "Spanish");
            put("t", "Tagalog");
        }
    };

    private static Map<String, String> _pqSportsMap = new HashMap<String, String>() {
        {
            put("a","Basketball");
            put("c","Cheerleading");
            put("r","Cross country");
            put("f","Field hockey");
            put("g","Golf");
            put("y","Gymnastics");
            put("m","Ice hockey");
            put("l","Lacrosse");
            put("d","Soccer");
            put("h","Softball");
            put("w","Swimming");
            put("t","Tennis");
            put("k","Track");
            put("v","Volleyball");
            put("p","Water polo");
            put("s","Baseball");
            put("o","Football");
            put("i","Wrestling");
        }
    };

    private static Map<String, String> _pqSpecialEdMap = new HashMap<String, String>() {
        {
            put("a", "Autism");
            put("u", "Deaf-blindness");
            put("f", "Deafness");
            put("e", "Serious emotional disturbance");
            put("h", "Hearing impairments");
            put("l", "Language or speech impairment");
            put("d", "Specific learning disabilities");
            put("z", "Limited intellectual functioning");
            put("o", "Orthopedic impairments");
            put("v", "Visual impairments");
            put("b", "Multiple disabilities");
            put("x", "Other health impairments");
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
            PQ pq = _PQDao.findBySchool(school);
            // PQ takes precedence over parent surveys
            if (pq != null) {
//                _log.warn("  Found PQ");
                struct.setProgramSource(Principal);
                processPQResults(struct, pq);
            } else {
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

    protected void processPQResults(ComparedSchoolProgramsExtracurricularsStruct school, PQ pq) {
        parsePQValues(school.getCategoryResponses(), ROW_LABEL_ARTS, pq.getArts(), pq.getArtsOther(), _pqArtsMap);
        parsePQValues(school.getCategoryResponses(), ROW_LABEL_SPORTS, pq.getBoysSports(), pq.getBoysSportsOther(),
                      _pqSportsMap);
        parsePQValues(school.getCategoryResponses(), ROW_LABEL_SPORTS, pq.getGirlsSports(), pq.getGirlsSportsOther(),
                      _pqSportsMap);
        parsePQValues(school.getCategoryResponses(), ROW_LABEL_LANGUAGES, pq.getForeignLanguageClasses(),
                      pq.getForeignLanguageClassesOther(), _pqLanguagesMap);
        if (StringUtils.equals("checked", pq.getBeforecare())) {
            school.getCategoryResponses().get(ROW_LABEL_BEFORE_AFTER_SCHOOL).add("Before-school care");
        }
        if (StringUtils.equals("checked", pq.getAftercare())) {
            school.getCategoryResponses().get(ROW_LABEL_BEFORE_AFTER_SCHOOL).add("After-school care");
        }
//        school.getCategoryResponses().get(ROW_LABEL_SPECIAL_PROGRAMS).add("See " + ROW_LABEL_ARTS);
        parsePQValues(school.getCategoryResponses(), ROW_LABEL_LEARNING_DISABILITIES, pq.getSpecialEdPrograms(),
                      _pqSpecialEdMap);
    }

    protected void parsePQValues(Map<String, Set<String>> categoryResponses, String categoryName, String pqValues, Map<String, String> valueMap) {
        parsePQValues(categoryResponses, categoryName, pqValues, null, valueMap);
    }

    protected void parsePQValues(Map<String, Set<String>> categoryResponses, String categoryName, String pqValues, String pqOtherValue, Map<String, String> valueMap) {
        if (StringUtils.isNotBlank(pqValues)) {
            for (String value: pqValues.split(":")) {
                if (valueMap.get(value) != null) {
                    categoryResponses.get(categoryName).add(valueMap.get(value));
                } else {
                    _log.warn("Can't find display text for PQ " + categoryName + " \"" + value + "\"");
                }
            }
        }
        if (StringUtils.isNotBlank(pqOtherValue)) {
            for (String value: pqOtherValue.split(",")) {
                String cleanedUpValue = StringUtils.capitalize(StringUtils.trim(value));
                if (StringUtils.isNotBlank(cleanedUpValue)) {
                    categoryResponses.get(categoryName).add(cleanedUpValue);
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

    public IPQDao getPQDao() {
        return _PQDao;
    }

    public void setPQDao(IPQDao PQDao) {
        _PQDao = PQDao;
    }
}
