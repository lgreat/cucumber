package gs.web.school;

import gs.data.school.IPQDao;
import gs.data.school.LevelCode;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.test.ITestDataSetDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolProfileHeaderHelper {
    public static final String BEAN_ID = "schoolProfileHeaderHelper";
    private static final Log _log = LogFactory.getLog(SchoolProfileHeaderHelper.class);

    private IPQDao _PQDao;
    private ITestDataSetDao _testDataSetDao;
    private static final String PQ_START_TIME = "pq_startTime";
    private static final String PQ_END_TIME = "pq_endTime";
    private static final String PQ_HOURS = "pq_hours";
    private static final String HAS_TEST_SCORES = "hasTestScores";

    public void updateModel(School school, Map<String, Object> model) {
        if (school != null) {
            PQ pq = _PQDao.findBySchool(school);
            if (pq != null) {
                 updateWithPQ(model, pq);
            }

            if (model.get(PQ_HOURS) == null) {
                ICensusInfo info = school.getCensusInfo();
                if (info != null) {
                    SchoolCensusValue hoursPerDay = info.getLatestValue(school,
                                                                        CensusDataType.HOURS_IN_SCHOOL_DAY);
                    if (hoursPerDay != null) {
                        model.put(PQ_HOURS, hoursPerDay.getValueText() + " hours per day");
                    }
                }
            }

            boolean hasTestScores = true;
            if (StringUtils.equals("private", school.getType().getSchoolTypeName())) {
                hasTestScores = school.getStateAbbreviation().isPrivateTestScoresState() &&
                        _testDataSetDao.hasDisplayableData(school);
            } else if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                hasTestScores = false;
            }
            model.put(HAS_TEST_SCORES, hasTestScores);

        }
    }

    protected void updateWithPQ(Map<String, Object> model, PQ pq) {
        if (StringUtils.isNotBlank(pq.getStartTime()) && StringUtils.isNotBlank(pq.getEndTime())) {
            model.put(PQ_START_TIME, pq.getStartTime());
            model.put(PQ_END_TIME, pq.getEndTime());
            model.put(PQ_HOURS, pq.getStartTime() + " - " + pq.getEndTime());
        }
    }

    public IPQDao getPQDao() {
        return _PQDao;
    }

    public void setPQDao(IPQDao PQDao) {
        _PQDao = PQDao;
    }

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }
}
