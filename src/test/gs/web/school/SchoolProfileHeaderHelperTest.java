package gs.web.school;

import gs.data.community.local.ILocalBoardDao;
import gs.data.geo.IGeoDao;
import gs.data.school.IPQDao;
import gs.data.school.PQ;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.state.State;
import gs.data.survey.ISurveyDao;
import gs.data.test.ITestDataSetDao;
import gs.web.BaseTestCase;

import java.util.HashMap;
import java.util.Map;

import static gs.web.school.SchoolProfileHeaderHelper.*;
import static org.easymock.EasyMock.*;

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

        _helper.setPQDao(_PQDao);
        _helper.setTestDataSetDao(_testDataSetDao);
        _helper.setSurveyDao(_surveyDao);
        _helper.setLocalBoardDao(_localBoardDao);
        _helper.setGeoDao(_geoDao);
        
        _model = new HashMap<String, Object>();

        _school = new School();
        _school.setCensusInfo(_censusInfo);
    }
    
    private void replayAllMocks() {
        replayMocks(_PQDao, _testDataSetDao, _surveyDao, _localBoardDao, _geoDao, _censusInfo);
    }

    private void verifyAllMocks() {
        verifyMocks(_PQDao, _testDataSetDao, _surveyDao, _localBoardDao, _geoDao, _censusInfo);
    }

    public void testBasics() {
        assertSame(_PQDao, _helper.getPQDao());
        assertSame(_testDataSetDao, _helper.getTestDataSetDao());
        assertSame(_surveyDao, _helper.getSurveyDao());
        assertSame(_localBoardDao, _helper.getLocalBoardDao());
        assertSame(_geoDao, _helper.getGeoDao());
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
        assertEquals("2.5 hours per day", _model.get(PQ_HOURS));
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
}
