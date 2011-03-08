package gs.web.school;

import gs.data.community.local.ILocalBoardDao;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.state.State;
import gs.data.survey.ISurveyDao;
import gs.data.test.ITestDataSetDao;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.BaseTestCase;
import gs.web.geo.StateSpecificFooterHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gs.web.school.SchoolProfileHeaderHelper.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolProfileHeaderHelperTest extends BaseTestCase {
    private SchoolProfileHeaderHelper _helper;
    private IPQDao _PQDao;
    private ITestDataSetDao _testDataSetDao;
    private ISurveyDao _surveyDao;
    private ILocalBoardDao _localBoardDao;
    private IGeoDao _geoDao;
    private ICensusInfo _censusInfo;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;
    private ISchoolDao _schoolDao;
    
    private Map<String, Object> _model;
    private School _school;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();

        _helper = new SchoolProfileHeaderHelper();

        _PQDao = createStrictMock(IPQDao.class);
        _testDataSetDao = createStrictMock(ITestDataSetDao.class);
        _surveyDao = createStrictMock(ISurveyDao.class);
        _localBoardDao = createStrictMock(ILocalBoardDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _censusInfo = createStrictMock(ICensusInfo.class);
        _stateSpecificFooterHelper = createStrictMock(StateSpecificFooterHelper.class);
        _ratingsConfigDao = createStrictMock(IRatingsConfigDao.class);
        _testManager = createStrictMock(TestManager.class);
        _schoolDao = createStrictMock(ISchoolDao.class);

        _helper.setPQDao(_PQDao);
        _helper.setTestDataSetDao(_testDataSetDao);
        _helper.setSurveyDao(_surveyDao);
        _helper.setLocalBoardDao(_localBoardDao);
        _helper.setGeoDao(_geoDao);
        _helper.setStateSpecificFooterHelper(_stateSpecificFooterHelper);
        _helper.setRatingsConfigDao(_ratingsConfigDao);
        _helper.setTestManager(_testManager);
        _helper.setSchoolDao(_schoolDao);

        _model = new HashMap<String, Object>();

        _school = new School();
        _school.setCensusInfo(_censusInfo);
    }
    
    private void replayAllMocks() {
        replayMocks(_PQDao, _testDataSetDao, _surveyDao, _localBoardDao, _geoDao, _censusInfo, _stateSpecificFooterHelper, _ratingsConfigDao, _testManager, _schoolDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_PQDao, _testDataSetDao, _surveyDao, _localBoardDao, _geoDao, _censusInfo, _stateSpecificFooterHelper, _ratingsConfigDao, _testManager, _schoolDao);
    }

    public void testBasics() {
        assertSame(_PQDao, _helper.getPQDao());
        assertSame(_testDataSetDao, _helper.getTestDataSetDao());
        assertSame(_surveyDao, _helper.getSurveyDao());
        assertSame(_localBoardDao, _helper.getLocalBoardDao());
        assertSame(_geoDao, _helper.getGeoDao());
        assertSame(_schoolDao, _helper.getSchoolDao());
    }
    
    public void testDeterminePQNone() {
        expect(_PQDao.findBySchool(_school)).andReturn(null);
        expect(_censusInfo.getLatestValue(_school, CensusDataType.HOURS_IN_SCHOOL_DAY)).andReturn(null);
        
        replayAllMocks();
        _helper.determinePQ(_school, _model);
        verifyAllMocks();

        assertNull(_model.get(PQ_START_TIME));
        assertNull(_model.get(PQ_HOURS));
        assertNull(_model.get(PQ_END_TIME));
    }

    public void testDeterminePQCensus() {
        expect(_PQDao.findBySchool(_school)).andReturn(null);
        SchoolCensusValue value = new SchoolCensusValue();
        value.setValueFloat(2.5f);
        expect(_censusInfo.getLatestValue(_school, CensusDataType.HOURS_IN_SCHOOL_DAY)).andReturn(value);

        replayAllMocks();
        _helper.determinePQ(_school, _model);
        verifyAllMocks();

        assertNull(_model.get(PQ_START_TIME));
        assertEquals("3 hours per day", _model.get(PQ_HOURS));
        assertNull(_model.get(PQ_END_TIME));
    }

    public void testDeterminePQ() {
        PQ pq = new PQ();
        pq.setStartTime("8:00");
        pq.setEndTime("3:30");
        expect(_PQDao.findBySchool(_school)).andReturn(pq);

        replayAllMocks();
        _helper.determinePQ(_school, _model);
        verifyAllMocks();

        assertEquals("8:00", _model.get(PQ_START_TIME));
        assertEquals("8:00 - 3:30", _model.get(PQ_HOURS));
        assertEquals("3:30", _model.get(PQ_END_TIME));
    }

    public void testDetermineSurveyResultsNone() {
        expect(_surveyDao.findSurveyIdWithMostResultsForSchool(_school)).andReturn(null);

        replayAllMocks();
        _helper.determineSurveyResults(_school, _model);
        verifyAllMocks();

        assertEquals(false, _model.get(HAS_SURVEY_DATA));
        assertNull(_model.get(SURVEY_LEVEL_CODE));
    }

    public void testDetermineSurveyResults() {
        expect(_surveyDao.findSurveyIdWithMostResultsForSchool(_school)).andReturn(3);
        expect(_surveyDao.findSurveyLevelCodeById(3)).andReturn("e");

        replayAllMocks();
        _helper.determineSurveyResults(_school, _model);
        verifyAllMocks();

        assertEquals(true, _model.get(HAS_SURVEY_DATA));
        assertEquals("e", _model.get(SURVEY_LEVEL_CODE));
    }

    public void testDetermineTestScoresPublic() {
        _school.setType(SchoolType.PUBLIC);

        replayAllMocks();
        _helper.determineTestScores(_school, _model);
        verifyAllMocks();

        assertEquals(true, _model.get(HAS_TEST_SCORES));
    }

    public void testDetermineTestScoresCharter() {
        _school.setType(SchoolType.CHARTER);

        replayAllMocks();
        _helper.determineTestScores(_school, _model);
        verifyAllMocks();

        assertEquals(true, _model.get(HAS_TEST_SCORES));
    }

    public void testDetermineTestScoresPrivateNo() {
        _school.setType(SchoolType.PRIVATE);
        _school.setStateAbbreviation(State.MN);

        expect(_testDataSetDao.hasDisplayableData(_school)).andReturn(false);

        replayAllMocks();
        _helper.determineTestScores(_school, _model);
        verifyAllMocks();

        assertEquals(false, _model.get(HAS_TEST_SCORES));
    }

    // TODO: aroy: Test depends on State.MN being a private test scores state
    // This is a brittle external dependency, but I don't know how to mock this out.
    public void testDetermineTestScoresPrivateYes() {
        _school.setType(SchoolType.PRIVATE);
        _school.setStateAbbreviation(State.MN);

        expect(_testDataSetDao.hasDisplayableData(_school)).andReturn(true);

        replayAllMocks();
        _helper.determineTestScores(_school, _model);
        verifyAllMocks();

        assertEquals(true, _model.get(HAS_TEST_SCORES));
    }

    public void testHandleCompareNearbyStringPreschool() {
        _school.setLevelCode(LevelCode.PRESCHOOL);
        
        replayAllMocks();
        _helper.handleCompareNearbyString(_school, _model);
        verifyAllMocks();
        assertNull(_model.get(COMPARE_NEARBY_STRING));
    }

    public void testHandleCompareNearbyStringNoNearby() {
        _school.setLevelCode(LevelCode.ELEMENTARY);
        _school.setDatabaseState(State.CA);
        _school.setId(1);

        expect(_schoolDao.findNearbySchoolsNoRating(_school, 7)).andReturn(new ArrayList<NearbySchool>());
        replayAllMocks();
        _helper.handleCompareNearbyString(_school, _model);
        verifyAllMocks();
        assertNull(_model.get(COMPARE_NEARBY_STRING));
    }

    public void testHandleCompareNearbyString() {
        _school.setLevelCode(LevelCode.ELEMENTARY);
        _school.setDatabaseState(State.CA);
        _school.setId(1);

        List<NearbySchool> nearbySchools = new ArrayList<NearbySchool>();
        NearbySchool nearbySchool1 = new NearbySchool();
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        school1.setId(2);
        school1.setLevelCode(LevelCode.MIDDLE);
        nearbySchool1.setNeighbor(school1);
        nearbySchool1.setSchool(_school);
        nearbySchools.add(nearbySchool1);
        NearbySchool nearbySchool2 = new NearbySchool();
        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setId(3);
        school2.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        nearbySchool2.setNeighbor(school2);
        nearbySchool2.setSchool(_school);
        nearbySchools.add(nearbySchool2);
        expect(_schoolDao.findNearbySchoolsNoRating(_school, 7)).andReturn(nearbySchools);
        replayAllMocks();
        _helper.handleCompareNearbyString(_school, _model);
        verifyAllMocks();
        assertNotNull(_model.get(COMPARE_NEARBY_STRING));
        assertEquals("CA1,CA2,CA3", _model.get(COMPARE_NEARBY_STRING));
    }

    public void testHandleCompareNearbyStringPreschoolNearby() {
        _school.setLevelCode(LevelCode.ELEMENTARY);
        _school.setDatabaseState(State.CA);
        _school.setId(1);

        List<NearbySchool> nearbySchools = new ArrayList<NearbySchool>();
        NearbySchool nearbySchool1 = new NearbySchool();
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        school1.setId(2);
        school1.setLevelCode(LevelCode.PRESCHOOL); // expect this to be skipped
        nearbySchool1.setNeighbor(school1);
        nearbySchool1.setSchool(_school);
        nearbySchools.add(nearbySchool1);
        NearbySchool nearbySchool2 = new NearbySchool();
        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setId(3);
        school2.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        nearbySchool2.setNeighbor(school2);
        nearbySchool2.setSchool(_school);
        nearbySchools.add(nearbySchool2);
        expect(_schoolDao.findNearbySchoolsNoRating(_school, 7)).andReturn(nearbySchools);
        replayAllMocks();
        _helper.handleCompareNearbyString(_school, _model);
        verifyAllMocks();
        assertNotNull(_model.get(COMPARE_NEARBY_STRING));
        assertEquals("CA1,CA3", _model.get(COMPARE_NEARBY_STRING));
    }

    public void testHandleCompareNearbyStringDifferentState() {
        _school.setLevelCode(LevelCode.ELEMENTARY);
        _school.setDatabaseState(State.CA);
        _school.setId(1);

        List<NearbySchool> nearbySchools = new ArrayList<NearbySchool>();
        NearbySchool nearbySchool1 = new NearbySchool();
        School school1 = new School();
        school1.setDatabaseState(State.WY); // expect this to be skipped
        school1.setId(2);
        school1.setLevelCode(LevelCode.ELEMENTARY);
        nearbySchool1.setNeighbor(school1);
        nearbySchool1.setSchool(_school);
        nearbySchools.add(nearbySchool1);
        NearbySchool nearbySchool2 = new NearbySchool();
        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setId(3);
        school2.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        nearbySchool2.setNeighbor(school2);
        nearbySchool2.setSchool(_school);
        nearbySchools.add(nearbySchool2);
        expect(_schoolDao.findNearbySchoolsNoRating(_school, 7)).andReturn(nearbySchools);
        replayAllMocks();
        _helper.handleCompareNearbyString(_school, _model);
        verifyAllMocks();
        assertNotNull(_model.get(COMPARE_NEARBY_STRING));
        assertEquals("CA1,CA3", _model.get(COMPARE_NEARBY_STRING));
    }
}
