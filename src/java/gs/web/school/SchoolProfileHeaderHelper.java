package gs.web.school;

import gs.data.cms.IPublicationDao;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.content.cms.CmsConstants;
import gs.data.content.cms.CmsDiscussionBoard;
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
import gs.data.util.NameValuePair;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

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
    private static final String DISCUSSION_BOARD_ID = "discussionBoardId";
    private static final String TOPIC_CENTER_ID = "topicCenterId";
    private static final String DISCUSSION_TOPIC = "discussionTopic";
    private static final String DISCUSSION_TOPIC_FULL = "discussionTopicFull";
    private static final String IS_LOCAL = "isLocal";
    private static final String DISCUSSION_TOPICS = "discussionTopics";

    public void updateModel(School school, Map<String, Object> model) {
        try {
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
                handleCommunitySidebar(school, model);
            }
        } catch (Exception e) {
            _log.error("Error fetching data for new school profile wrapper: " + e, e);
        }
    }

    protected void handleCommunitySidebar(School school, Map<String, Object> model) {
        // determine if this is a city with a local discussion board
        City city = _geoDao.findCity(school.getDatabaseState(), school.getCity());
        boolean foundLocalBoard = false;
        if (city != null) {
            LocalBoard localBoard = _localBoardDao.findByCityId(city.getId());
            if (localBoard != null) {
                model.put(DISCUSSION_BOARD_ID, localBoard.getBoardId());
                model.put(DISCUSSION_TOPIC, city.getDisplayName());
                model.put(DISCUSSION_TOPIC_FULL, city.getDisplayName());
                model.put(IS_LOCAL, true);
                foundLocalBoard = true;
            }
        }
        // if not, default to grade level board for lowest level of school
        if (!foundLocalBoard && school.getLevelCode() != null) {
            long topicCenterId = -1;
            LevelCode.Level lowestLevel = school.getLevelCode().getLowestLevel();
            if (LevelCode.Level.PRESCHOOL_LEVEL == lowestLevel) {
                topicCenterId = CmsConstants.PRESCHOOL_TOPIC_CENTER_ID;
                model.put(DISCUSSION_TOPIC, "Preschool");
                model.put(DISCUSSION_TOPIC_FULL, "Preschool");
            } else if (LevelCode.Level.ELEMENTARY_LEVEL == lowestLevel) {
                topicCenterId = CmsConstants.ELEMENTARY_SCHOOL_TOPIC_CENTER_ID;
                model.put(DISCUSSION_TOPIC, "Elementary");
                model.put(DISCUSSION_TOPIC_FULL, "Elementary School");
            } else if (LevelCode.Level.MIDDLE_LEVEL == lowestLevel) {
                topicCenterId = CmsConstants.MIDDLE_SCHOOL_TOPIC_CENTER_ID;
                model.put(DISCUSSION_TOPIC, "Middle");
                model.put(DISCUSSION_TOPIC_FULL, "Middle School");
            } else if (LevelCode.Level.HIGH_LEVEL == lowestLevel) {
                topicCenterId = CmsConstants.HIGH_SCHOOL_TOPIC_CENTER_ID;
                model.put(DISCUSSION_TOPIC, "High");
                model.put(DISCUSSION_TOPIC_FULL, "High School");
            }
            if (topicCenterId > -1) {
                CmsTopicCenter topicCenter =
                        _publicationDao.populateByContentId(topicCenterId, new CmsTopicCenter());
                if (topicCenter != null
                        && topicCenter.getDiscussionBoardId() != null
                        && topicCenter.getDiscussionBoardId() > -1) {
                    model.put(DISCUSSION_BOARD_ID, topicCenter.getDiscussionBoardId());
                    model.put(TOPIC_CENTER_ID, topicCenter.getContentKey().getIdentifier());
                }
            }
        }
        // now that we have the board, look up the list of other topics the user can navigate to
        if (model.get(DISCUSSION_BOARD_ID) != null) {
            // for each of these we need the topic title and the full uri to the discussion board
            List<NameValuePair<String, String>> topicSelectInfo
                    = new ArrayList<NameValuePair<String, String>>();
            Set<Long> discussionBoardIds = new HashSet<Long>();
            // first grab all the topic centers with discussion board
            Collection<CmsTopicCenter> topicCenters = getValidDiscussionTopics();
            // collect their ids
            for (CmsTopicCenter topic: topicCenters) {
                discussionBoardIds.add(topic.getDiscussionBoardId());
            }
            // pull out the relevant discussion boards by id
            Map<Long, CmsDiscussionBoard> discussionBoardsMap =
                    _publicationDao.populateAllById(discussionBoardIds, new CmsDiscussionBoard());
            // now put the full uri of the discussion board along with the title of the topic center
            // into the model
            for (CmsTopicCenter topic: topicCenters) {
                CmsDiscussionBoard board = discussionBoardsMap.get(topic.getDiscussionBoardId());
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION_BOARD,
                                                       (board != null)?board.getFullUri():topic.getFullUri(),
                                                       topic.getDiscussionBoardId());
                NameValuePair<String, String> topicToBoard
                        = new NameValuePair<String, String>
                        (topic.getTitle(), urlBuilder.asSiteRelative(null));
                topicSelectInfo.add(topicToBoard);
            }
            model.put(DISCUSSION_TOPICS, topicSelectInfo);
        }
    }

    protected Collection<CmsTopicCenter> getValidDiscussionTopics() {
        // TODO: this call pulls all topic center data out of the db, when we only need some topic centers.
        // When running on localhost there is significant network traffic during this call (some 5MB) and the delay
        // is noticeable.  Can this be improved?
        // We only need the topic centers with discussion boards
        Collection<CmsTopicCenter> topicCenters =
                _publicationDao.populateAllByContentType
                        (CmsConstants.DISCUSSION_BOARD_CONTENT_TYPE, new CmsTopicCenter());
        SortedSet<CmsTopicCenter> sortedTopics = new TreeSet<CmsTopicCenter>(new Comparator<CmsTopicCenter>() {
            public int compare(CmsTopicCenter o1, CmsTopicCenter o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        for (CmsTopicCenter topicCenter: topicCenters) {
            if (topicCenter.getDiscussionBoardId() != null && topicCenter.getDiscussionBoardId() > 0) {
                sortedTopics.add(topicCenter);
            }
        }
        CmsTopicCenter generalParenting = new CmsTopicCenter();
        generalParenting.setDiscussionBoardId(2420L);
        generalParenting.setTitle("General Parenting");
        generalParenting.setFullUri("/general");
        sortedTopics.add(generalParenting);
        return sortedTopics;
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
