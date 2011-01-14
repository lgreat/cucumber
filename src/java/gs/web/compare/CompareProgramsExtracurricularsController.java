package gs.web.compare;

import gs.data.school.IPQDao;
import gs.data.school.LevelCode;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.survey.ISurveyDao;
import gs.data.survey.SurveyResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools,
                                        Map<String, Object> model) {
        model.put(MODEL_TAB, TAB_NAME);

        for (ComparedSchoolBaseStruct baseStruct: schools) {
            ComparedSchoolProgramsExtracurricularsStruct struct =
                    (ComparedSchoolProgramsExtracurricularsStruct) baseStruct;
            _log.warn("Processing " + baseStruct.getName() + " (" + baseStruct.getUniqueIdentifier() + ")");
            School school = baseStruct.getSchool();
            PQ pq = _PQDao.findBySchool(school);
            if (pq != null) {
                _log.warn("  Found PQ");
                struct.setProgramSource(Principal);
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

    protected void processSurveyResults(ComparedSchoolProgramsExtracurricularsStruct school,
                                        List<SurveyResults> allResultsForSchool) {
        _log.warn("  Found " + allResultsForSchool.size() + " survey result(s)");

        for (SurveyResults results: allResultsForSchool) {
            _log.warn("  Processing survey results with " + results.getTotalResponses() + " response(s)");
            school.setNumResponses(school.getNumResponses() + results.getTotalResponses());
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
