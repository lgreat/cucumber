package gs.web.school;

import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.content.cms.CmsConstants;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.survey.ISurveyDao;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.util.AdUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
@Component("schoolProfileHeaderHelper")
public class SchoolProfileHeaderHelper {
    public static final String BEAN_ID = "schoolProfileHeaderHelper";
    private static final Log _log = LogFactory.getLog(SchoolProfileHeaderHelper.class);

    private ITestDataSetDao _testDataSetDao;
    private ISurveyDao _surveyDao;
    private ILocalBoardDao _localBoardDao;
    private IGeoDao _geoDao;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;
    private ISchoolDao _schoolDao;
    private IEspResponseDao _espResponseDao;
    @Autowired
    private SchoolProfileHelper _schoolProfileHelper;
    public static final String PQ_START_TIME = "pq_startTime";
    public static final String PQ_END_TIME = "pq_endTime";
    public static final String PQ_HOURS = "pq_hours";
    public static final String HAS_SCHOOL_STATS = "hasSchoolStats";
    public static final String HAS_TEST_SCORES = "hasTestScores";
    public static final String HAS_SURVEY_DATA = "hasSurveyData";
    public static final String HAS_PQ = "hasPq";
    public static final String DISCUSSION_BOARD_ID = "discussionBoardId";
    public static final String DISCUSSION_TOPIC = "discussionTopic";
    public static final String DISCUSSION_TOPIC_FULL = "discussionTopicFull";
    public static final String IS_LOCAL = "isLocal";
    public static final String SURVEY_LEVEL_CODE = "surveyLevelCode";
    public static final String COMPARE_NEARBY_STRING = "compareNearbyString";

    private void logDuration(long durationInMillis, String eventName) {
        _log.info(eventName + " took " + durationInMillis + " milliseconds");
    }

    public void updateModel(HttpServletRequest request, HttpServletResponse response,
                            School school, Map<String, Object> model) {
        long startTime;
        long totalTime = System.currentTimeMillis();
        try {
            if (school != null) {
                startTime = System.currentTimeMillis();
                determinePQ(school, model); // Determine PQ
                logDuration(System.currentTimeMillis() - startTime, "Determining PQ and hours per day");

                startTime = System.currentTimeMillis();
                determineTestScores(school, model); // Determine private school test scores
                logDuration(System.currentTimeMillis() - startTime, "Determining presence of test scores");

                // Determine school stats (for preschools)
                if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                    if (StringUtils.isNotBlank(school.getStateId())
                            || StringUtils.isNotBlank(school.getNcesCode())) {
                        // for PKs with state_id or nces_code, link to school stats
                        model.put(HAS_SCHOOL_STATS, true);
                    }
                }

                startTime = System.currentTimeMillis();
                determineSurveyResults(school, model); // Determine survey results
                logDuration(System.currentTimeMillis() - startTime, "Determining survey data");

                startTime = System.currentTimeMillis();
                City city = handleCommunitySidebar(school, model); // Determine community module
                logDuration(System.currentTimeMillis() - startTime, "Handling community sidebar");

                startTime = System.currentTimeMillis();
                handleGSRating(request, school);
                logDuration(System.currentTimeMillis() - startTime, "Handling GS Rating");

                startTime = System.currentTimeMillis();
                _schoolProfileHelper.handleAdKeywords(request, school);
                logDuration(System.currentTimeMillis() - startTime, "Handling ad keywords");

                startTime = System.currentTimeMillis();
                _schoolProfileHelper.handleCityCookie(request, response, city);
                logDuration(System.currentTimeMillis() - startTime, "Handling city id cookie");

                startTime = System.currentTimeMillis();
                _schoolProfileHelper.handleStateSpecificFooter(request, school, model);
                logDuration(System.currentTimeMillis() - startTime, "Handling state specific footer");

                startTime = System.currentTimeMillis();
                handleCompareNearbyString(school, model);
                logDuration(System.currentTimeMillis() - startTime, "Handling compare nearby string");

                startTime = System.currentTimeMillis();
                // TODO: we could refactor handlePinItButton to reuse urlbuilder used for relCanonical
                _schoolProfileHelper.handlePinItButton(request, school, model);
                logDuration(System.currentTimeMillis() - startTime, "Handling PinIt button");

                startTime = System.currentTimeMillis();
                // TODO-13689 need to confirm or change the traffic driver code passed in
                String k12AffiliateUrl = AdUtil.getK12AffiliateLinkForSchool(school, "so");
                if (k12AffiliateUrl != null) {
                    model.put("k12AffiliateUrl", k12AffiliateUrl);
                }
                logDuration(System.currentTimeMillis() - startTime, "Handling K12 affiliate URL");
            }
        } catch (Exception e) {
            _log.error("Error fetching data for new school profile wrapper: " + e, e);
        }
        logDuration(System.currentTimeMillis() - totalTime, "Entire SchoolProfileHeaderHelper");
    }

    protected void handleCompareNearbyString(School school, Map<String, Object> model) {
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            return; // no preschools on compare
        }
        List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchoolsNoRating(school, 7);
        String compareNearbyString = school.getDatabaseState().getAbbreviation() + school.getId() + ",";
        for (NearbySchool nearbySchool: nearbySchools) {
            if (LevelCode.PRESCHOOL.equals(nearbySchool.getNeighbor().getLevelCode())
                    || nearbySchool.getNeighbor().getDatabaseState() != school.getDatabaseState()) {
                continue;
            }
            compareNearbyString += nearbySchool.getNeighbor().getDatabaseState().getAbbreviation()
                    + nearbySchool.getNeighbor().getId()
                    + ",";
        }
        compareNearbyString = compareNearbyString.substring(0, compareNearbyString.length()-1); // strip comma
        if (compareNearbyString.indexOf(",") > -1) { // must have at least 2 schools to compare
            model.put(COMPARE_NEARBY_STRING, compareNearbyString);
        }
    }

    protected void handleGSRating(HttpServletRequest request, School school) throws IOException {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper == null) {
            return;
        }
        boolean isFromCache = true;
        if (pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
            isFromCache = false;
        }

        IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(school.getDatabaseState(), isFromCache);

        if (null != ratingsConfig) {
            SchoolTestValue schoolTestValue =
                    _testManager.getOverallRating(school, ratingsConfig.getYear());

            if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
                request.setAttribute("gs_rating", schoolTestValue.getValueInteger());
                if (schoolTestValue.getValueInteger() > 0 && schoolTestValue.getValueInteger() < 11) {
                    pageHelper.addAdKeyword("gs_rating", String.valueOf(schoolTestValue.getValueInteger()));
                }
            }
        }
    }

    protected void determineSurveyResults(School school, Map<String, Object> model) {
        Integer surveyId = _surveyDao.findSurveyIdWithMostResultsForSchool(school);
        if (surveyId != null) {
            String levelCode = _surveyDao.findSurveyLevelCodeById(surveyId);
            model.put(SURVEY_LEVEL_CODE, levelCode);
            model.put(HAS_SURVEY_DATA, true);
        } else {
            model.put(HAS_SURVEY_DATA, false);
        }
    }

    protected void determineTestScores(School school, Map<String, Object> model) {
        boolean hasTestScores = true;
        if (StringUtils.equals("private", school.getType().getSchoolTypeName())) {
            hasTestScores = school.getStateAbbreviation().isPrivateTestScoresState() &&
                    _testDataSetDao.hasDisplayableData(school);
        } else if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            hasTestScores = false;
        }
        model.put(HAS_TEST_SCORES, hasTestScores);
    }

    protected void determinePQ(School school, Map<String, Object> model) {
        List<EspResponse> espResponses = _espResponseDao.getResponses(school);
        model.put(HAS_PQ, espResponses != null && espResponses.size() > 0);
        if (espResponses != null) {
            Map<String, List<EspResponse>> espKeyToResponseMap = EspResponse.rollup(espResponses);
            if (espKeyToResponseMap.get("start_time") != null && espKeyToResponseMap.get("end_time") != null) {
                String startTime = espKeyToResponseMap.get("start_time").get(0).getSafeValue();
                String endTime = espKeyToResponseMap.get("end_time").get(0).getSafeValue();
                model.put(PQ_START_TIME, startTime);
                model.put(PQ_END_TIME, endTime);
                model.put(PQ_HOURS, startTime + " - " + endTime);
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

    protected City handleCommunitySidebar(School school, Map<String, Object> model) {
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
            LevelCode.Level lowestLevel = school.getLevelCode().getLowestNonPreSchoolLevel();
            // only display Preschool board for p-only schools
            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                model.put(DISCUSSION_TOPIC, "preschool");
                model.put(DISCUSSION_TOPIC_FULL, "Preschool");
                model.put(DISCUSSION_BOARD_ID, CmsConstants.PRESCHOOL_DISCUSSION_BOARD_ID);
            } else if (LevelCode.Level.ELEMENTARY_LEVEL == lowestLevel) {
                model.put(DISCUSSION_TOPIC, "elementary");
                model.put(DISCUSSION_TOPIC_FULL, "Elementary School");
                model.put(DISCUSSION_BOARD_ID, CmsConstants.ELEMENTARY_SCHOOL_DISCUSSION_BOARD_ID);
            } else if (LevelCode.Level.MIDDLE_LEVEL == lowestLevel) {
                model.put(DISCUSSION_TOPIC, "middle");
                model.put(DISCUSSION_TOPIC_FULL, "Middle School");
                model.put(DISCUSSION_BOARD_ID, CmsConstants.MIDDLE_SCHOOL_DISCUSSION_BOARD_ID);
            } else if (LevelCode.Level.HIGH_LEVEL == lowestLevel) {
                model.put(DISCUSSION_TOPIC, "high");
                model.put(DISCUSSION_TOPIC_FULL, "High School");
                model.put(DISCUSSION_BOARD_ID, CmsConstants.HIGH_SCHOOL_DISCUSSION_BOARD_ID);
            }
        }
        return city;
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

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }

    public IRatingsConfigDao getRatingsConfigDao() {
        return _ratingsConfigDao;
    }

    public void setRatingsConfigDao(IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public TestManager getTestManager() {
        return _testManager;
    }

    public void setTestManager(TestManager testManager) {
        this._testManager = testManager;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

    public SchoolProfileHelper getSchoolProfileHelper() {
        return _schoolProfileHelper;
    }

    public void setSchoolProfileHelper(SchoolProfileHelper schoolProfileHelper) {
        _schoolProfileHelper = schoolProfileHelper;
    }
}
