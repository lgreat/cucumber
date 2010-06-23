package gs.web.school;

import gs.data.cms.IPublicationDao;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.content.cms.CmsConstants;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.IPQDao;
import gs.data.school.LevelCode;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.survey.ISurveyDao;
import gs.data.test.ITestDataSetDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolProfileHeaderHelper {
    public static final String BEAN_ID = "schoolProfileHeaderHelper";
    private static final Log _log = LogFactory.getLog(SchoolProfileHeaderHelper.class);

    private IPQDao _PQDao;
    private ITestDataSetDao _testDataSetDao;
    private ISurveyDao _surveyDao;
    private ILocalBoardDao _localBoardDao;
    private IGeoDao _geoDao;
    private IPublicationDao _publicationDao;
    private static final String PQ_START_TIME = "pq_startTime";
    private static final String PQ_END_TIME = "pq_endTime";
    private static final String PQ_HOURS = "pq_hours";
    private static final String HAS_TEST_SCORES = "hasTestScores";
    private static final String HAS_SURVEY_DATA = "hasSurveyData";
    private static final String LOCAL_DISCUSSION_BOARD_ID = "localDiscussionBoardId";
    private static final String GRADE_DISCUSSION_BOARD_ID = "gradeDiscussionBoardId";

    public void updateModel(School school, Map<String, Object> model) {
        if (school != null) {
            // Determine PQ
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
                        model.put(PQ_HOURS, hoursPerDay.getValueFloat() + " hours per day");
                    }
                }
            }

            // Determine private school test scores
            boolean hasTestScores = true;
            if (StringUtils.equals("private", school.getType().getSchoolTypeName())) {
                hasTestScores = school.getStateAbbreviation().isPrivateTestScoresState() &&
                        _testDataSetDao.hasDisplayableData(school);
            } else if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                hasTestScores = false;
            }
            model.put(HAS_TEST_SCORES, hasTestScores);

            // TODO: Determine levelcode of survey with most results
            // Determine survey results
            model.put(HAS_SURVEY_DATA, _surveyDao.hasSurveyData(school));

            // Determine community module
            City city = _geoDao.findCity(school.getDatabaseState(), school.getCity());
            boolean foundLocalBoard = false;
            if (city != null) {
                LocalBoard localBoard = _localBoardDao.findByCityId(city.getId());
                if (localBoard != null) {
                    model.put(LOCAL_DISCUSSION_BOARD_ID, localBoard.getBoardId());
                    foundLocalBoard = true;
                }
            }
            if (!foundLocalBoard) {
                // default to grade level board for lowest level of school
                long topicCenterId = -1;
                LevelCode.Level lowestLevel = school.getLevelCode().getLowestLevel();
                if (LevelCode.Level.PRESCHOOL_LEVEL == lowestLevel) {
                    topicCenterId = CmsConstants.PRESCHOOL_TOPIC_CENTER_ID;
                } else if (LevelCode.Level.ELEMENTARY_LEVEL == lowestLevel) {
                    topicCenterId = CmsConstants.ELEMENTARY_SCHOOL_TOPIC_CENTER_ID;
                } else if (LevelCode.Level.MIDDLE_LEVEL == lowestLevel) {
                    topicCenterId = CmsConstants.MIDDLE_SCHOOL_TOPIC_CENTER_ID;
                } else if (LevelCode.Level.HIGH_LEVEL == lowestLevel) {
                    topicCenterId = CmsConstants.HIGH_SCHOOL_TOPIC_CENTER_ID;
                }
                if (topicCenterId > -1) {
                    CmsTopicCenter topicCenter =
                            _publicationDao.populateByContentId(topicCenterId, new CmsTopicCenter());
                    if (topicCenter != null
                            && topicCenter.getDiscussionBoardId() != null
                            && topicCenter.getDiscussionBoardId() > -1) {
                        model.put(GRADE_DISCUSSION_BOARD_ID, topicCenter.getDiscussionBoardId());
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

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
