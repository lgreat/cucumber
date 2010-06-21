package gs.web.school;

import gs.data.school.IPQDao;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
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
    private static final String PQ_START_TIME = "pq_startTime";
    private static final String PQ_END_TIME = "pq_endTime";
    private static final String PQ_HOURS = "pq_hours";

    public void updateModel(School school, Map<String, Object> model) {
        if (school != null) {
            // TODO: Better way to retrieve active PQ?
            List<PQ> pqs = _PQDao.findBySchool(school);
            if (pqs != null) {
                for (PQ pq: pqs) {
                    if (StringUtils.equals("live", pq.getLive())) {
                        updateWithPQ(model, pq);
                    }
                }
            }

            if (model.get(PQ_HOURS) == null) {
                ICensusInfo info = school.getCensusInfo();
                if (info != null) {
                    SchoolCensusValue hoursPerDay = info.getLatestValue(school,
                                                                        CensusDataType.HOURS_IN_SCHOOL_DAY);
                    if (hoursPerDay != null) {
                        model.put(PQ_HOURS, hoursPerDay.getValueInteger() + " hours per day");
                    }
                }
            }
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
}
