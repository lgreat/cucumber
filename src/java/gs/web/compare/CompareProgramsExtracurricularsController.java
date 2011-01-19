package gs.web.compare;

import gs.data.school.IPQDao;
import gs.data.school.LevelCode;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.survey.*;
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

    private static Map<String, String> _questionAnswerToLabelMap = new HashMap<String, String>() {
        {
            put("q1a1", "Arts &amp; activities");
            put("q34a1", "Arts &amp; activities");

            put("q1a3", "Sports");
            put("q34a3", "Sports");

            put("q1a2", "Languages taught");
            put("q34a2", "Languages taught");

            put("q1a4", "Other special programs");
            put("q34a4", "Other special programs");

            put("q1a5", "Vocational programs");

//            put("q8", "Before/After school care");

            put("q10", "Schools attended before this school");
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

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools,
                                        Map<String, Object> model) {
        model.put(MODEL_TAB, TAB_NAME);
        
        List<String> categories = new ArrayList<String>();
        categories.add("Arts &amp; activities");
        categories.add("Sports");
        categories.add("Languages taught");
        categories.add("Before/After school care");
        categories.add("Other special programs");
        categories.add("Vocational programs");
        categories.add("Schools attended before this school");

        model.put("categories", categories);

        for (ComparedSchoolBaseStruct baseStruct: schools) {
            ComparedSchoolProgramsExtracurricularsStruct struct =
                    (ComparedSchoolProgramsExtracurricularsStruct) baseStruct;
            _log.warn("Processing " + baseStruct.getName() + " (" + baseStruct.getUniqueIdentifier() + ")");

            Map<String, Set<String>> categoryResponses = new HashMap<String, Set<String>>();
            for (String category: categories) {
                categoryResponses.put(category, new LinkedHashSet<String>());
            }
            struct.setCategoryResponses(categoryResponses);

            School school = baseStruct.getSchool();
            PQ pq = _PQDao.findBySchool(school);
            if (pq != null) {
                _log.warn("  Found PQ");
                struct.setProgramSource(Principal);
                processPQResults(struct, pq);
            } else {
                _log.warn("  Did not find PQ, checking for survey results");
                Set<LevelCode.Level> levels = school.getLevelCode().getIndividualLevelCodes();
                List<SurveyResults> allResultsForSchool = new ArrayList<SurveyResults>();
                for (LevelCode.Level level: levels) {
                    _log.warn("  Looking for " + level.getLongName() + " surveys");
                    SurveyResults results = _surveyDao.getSurveyResultsForSchool(level.getName(), baseStruct.getSchool());
                    if (results != null && results.getTotalResponses() > 0) {
                        allResultsForSchool.add(results);
                    }
                }
                processSurveyResults(struct, allResultsForSchool);
            }
        }
    }

    protected void processPQResults(ComparedSchoolProgramsExtracurricularsStruct school, PQ pq) {
        parsePQValues(school.getCategoryResponses(), "Arts &amp; activities", pq.getArts(), _pqArtsMap);
        parsePQValues(school.getCategoryResponses(), "Sports", pq.getBoysSports(), _pqSportsMap);
        parsePQValues(school.getCategoryResponses(), "Sports", pq.getGirlsSports(), _pqSportsMap);
        parsePQValues(school.getCategoryResponses(), "Languages taught", pq.getForeignLanguageClasses(), _pqLanguagesMap);
        if (StringUtils.equals("checked", pq.getBeforecare())) {
            school.getCategoryResponses().get("Before/After school care").add("Before school care");
        }
        if (StringUtils.equals("checked", pq.getAftercare())) {
            school.getCategoryResponses().get("Before/After school care").add("After school care");
        }
    }

    protected void parsePQValues(Map<String, Set<String>> categoryResponses, String categoryName, String pqValues, Map<String, String> valueMap) {
        if (StringUtils.isNotBlank(pqValues)) {
            for (String value:pqValues.split(":")) {
                if (valueMap.get(value) != null) {
                    categoryResponses.get(categoryName).add(valueMap.get(value));
                } else {
                    _log.warn("Can't find display text for PQ " + categoryName + " \"" + value + "\"");
                }
            }
        }

    }

    protected void processSurveyResults(ComparedSchoolProgramsExtracurricularsStruct school,
                                        List<SurveyResults> allResultsForSchool) {
        _log.warn("  Found " + allResultsForSchool.size() + " survey result(s)");

        for (SurveyResults results: allResultsForSchool) {
            _log.warn("  Processing survey results with " + results.getTotalResponses() + " response(s)");
            school.setNumResponses(school.getNumResponses() + results.getTotalResponses());
            for (SurveyResultPage page: results.getPages()) {
                _log.warn("    Processing page " + page.getName());
                for (SurveyResultGroup group: page.getGroups()) {
                    _log.warn("      Processing group " + group.getDisplayText());
                    for (SurveyResultQuestion question: group.getQuestions()) {
                        _log.warn("        Processing question " + question.getQuestion().getId());
                        if ("COMPLEX".equals(question.getDisplayType())) {
                            for (Answer answer: question.getQuestion().getAnswers()) {
                                String key = "q" + question.getQuestion().getId() + "a" + answer.getId();
                                if (_questionAnswerToLabelMap.get(key) != null) {
                                    _log.warn("          Found " + key);
                                    Map<Object, Integer> responseValuesMap = question.getResponseValuesAsMap();
                                    for (AnswerValue answerValue: answer.getAnswerValues()) {
                                        Integer numPositiveResponses = responseValuesMap.get(answerValue.getSymbol());
                                        if (numPositiveResponses != null && numPositiveResponses > 0) {
                                            _log.warn("            Adding \"" + answerValue.getDisplay() + "\" to " + _questionAnswerToLabelMap.get(key));
                                            school.getCategoryResponses().get(_questionAnswerToLabelMap.get(key)).add(answerValue.getDisplay());
                                        }
                                    }
                                }
                            }
                        } else {
                            String key = "q" + question.getQuestion().getId();
                            if (_questionAnswerToLabelMap.get(key) != null) {
                                _log.warn("          Found " + key);
                                for (Object o: question.getResponseValuesAsMap().keySet()) {
                                    try {
                                        _log.warn("            Adding \"" + school.getName() + "\" to " + _questionAnswerToLabelMap.get(key));
                                        School responseSchool = (School) o;
                                        school.getCategoryResponses().get(_questionAnswerToLabelMap.get(key)).add(responseSchool.getName());
                                    } catch (Exception e) {
                                        _log.error(e, e);
                                    }
                                }
                                _log.warn(question.getResponseValuesAsSetOrderedByOccurrence());
                                _log.warn(question.getNumResponses());
                                _log.warn(question.getNumResponseUsers());
                            }
                        }
//                        if ("COMPLEX".equals(question.getDisplayType())) {
//                            for (Answer answer: question.getQuestion().getAnswers()) {
//                                if (answer.getTitle() != null) {
//                                    _log.warn("          Found " + answer.getTitle() + " answer");
//                                    if (_categoryToBeanMap.get(answer.getTitle()) != null) {
//                                        Map<Object, Integer> responseValuesMap = question.getResponseValuesAsMap();
//                                        for (AnswerValue answerValue: answer.getAnswerValues()) {
//                                            Integer numPositiveResponses = responseValuesMap.get(answerValue.getSymbol());
//                                            if (numPositiveResponses != null && numPositiveResponses > 0) {
//                                                _log.warn("            Adding \"" + answerValue.getDisplay() + "\" to " + _categoryToBeanMap.get(answer.getTitle()));
//                                                school.getCategoryResponses().get(_categoryToBeanMap.get(answer.getTitle())).add(answerValue.getDisplay());
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            for (Answer answer: question.getQuestion().getAnswers()) {
//                                if (_categoryToBeanMap.get(answer.getType().name()) != null) {
//
//                                } else {
//                                    for (AnswerValue answerValue: answer.getAnswerValues()) {
//                                        if (_categoryToBeanMap.get(answerValue.getSymbol()) != null) {
//                                            Map<Object, Integer> responseValuesMap = question.getResponseValuesAsMap();
//                                            Integer numPositiveResponses = responseValuesMap.get(answerValue.getSymbol());
//                                            if (numPositiveResponses != null && numPositiveResponses > 0) {
//                                                _log.warn("            Adding \"" + answerValue.getDisplay() + "\" to " + _categoryToBeanMap.get(answerValue.getSymbol()));
//                                                school.getCategoryResponses().get(_categoryToBeanMap.get(answerValue.getSymbol())).add(answerValue.getDisplay());
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
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
