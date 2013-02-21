package gs.web.school;

import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.*;
import gs.data.util.ListUtils;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class ApiTestResultsHelperTest extends BaseControllerTestCase {
    private ApiTestResultsHelper _helper;

    private IApiResultDao _apiResultDao;
    private ISchoolDao _schoolDao;
    private ITestDataStateValueDao _testDataStateValueDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _helper = new ApiTestResultsHelper();

        _apiResultDao = createMock(IApiResultDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _testDataStateValueDao = createStrictMock(ITestDataStateValueDao.class);

        _helper.setApiResultDao(_apiResultDao);
        _helper.setSchoolDao(_schoolDao);
        _helper.setTestDataStateValueDao(_testDataStateValueDao);
    }

    private void replayAllMocks() {
        replayMocks(_apiResultDao, _schoolDao, _testDataStateValueDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_apiResultDao, _schoolDao, _testDataStateValueDao);
    }

    private void resetAllMocks() {
        resetMocks(_apiResultDao, _schoolDao, _testDataStateValueDao);
    }


    public void testApiTestResults(){
        School school = null;
        Map<String, Object> results = _helper.getApiTestResultsForSchool(school);
        assertNull("School is null",results);

        school = new School();
        school.setActive(false);
        results = _helper.getApiTestResultsForSchool(school);
        assertNull("School is inactive",results);

        school = new School();
        school.setActive(true);
        school.setId(1);
        resetAllMocks();
        expect(_schoolDao.getSchoolById(State.CA,1)).andReturn(school);
        expect(_apiResultDao.getApiScoresOrderByMostRecent(school,_helper.NUM_YEARS_FOR_HISTORICAL_DATA)).andReturn(null);
        replayAllMocks();
        results = _helper.getApiTestResultsForSchool(school);
        verifyAllMocks();
        assertNull("Query returns null",results);


        school = new School();
        school.setActive(true);
        school.setId(1);
        resetAllMocks();
        expect(_schoolDao.getSchoolById(State.CA,1)).andReturn(school);
        expect(_apiResultDao.getApiScoresOrderByMostRecent(school,_helper.NUM_YEARS_FOR_HISTORICAL_DATA))
                .andReturn(new ArrayList<ApiResult>());
        replayAllMocks();
        results = _helper.getApiTestResultsForSchool(school);
        verifyAllMocks();
        assertNull("Query returns empty list",results);


        school = new School();
        school.setActive(true);
        school.setId(1);
        resetAllMocks();
        List<ApiResult> apiResults = new ArrayList<ApiResult>();
        apiResults.add(constructApiResultObj(null, null));
        expect(_schoolDao.getSchoolById(State.CA,1)).andReturn(school);
        expect(_apiResultDao.getApiScoresOrderByMostRecent(school,_helper.NUM_YEARS_FOR_HISTORICAL_DATA))
                .andReturn(apiResults);
        replayAllMocks();
        results = _helper.getApiTestResultsForSchool(school);
        verifyAllMocks();
        assertNull("First item in list has null year and null total",results);


        school = new School();
        school.setActive(true);
        school.setId(1);
        resetAllMocks();
        apiResults = new ArrayList<ApiResult>();
        apiResults.add(null);
        expect(_schoolDao.getSchoolById(State.CA,1)).andReturn(school);
        expect(_apiResultDao.getApiScoresOrderByMostRecent(school,_helper.NUM_YEARS_FOR_HISTORICAL_DATA))
                .andReturn(apiResults);
        replayAllMocks();
        results = _helper.getApiTestResultsForSchool(school);
        verifyAllMocks();
        assertNull("First item in list is null",results);


        school = new School();
        school.setActive(true);
        school.setId(1);
        resetAllMocks();
        expect(_schoolDao.getSchoolById(State.CA,1)).andReturn(school);
        expect(_apiResultDao.getApiScoresOrderByMostRecent(school,_helper.NUM_YEARS_FOR_HISTORICAL_DATA))
                .andReturn(constructApiResultObjListWith1Item());
        ApiResult apiStateRank = new ApiResult();
        apiStateRank.setYear(2008);
        apiStateRank.setApiStateRank(1);
        expect(_apiResultDao.getMostRecentStateRank(school))
                .andReturn(apiStateRank);
        expect(_apiResultDao.getMostRecentSimilarSchoolsRank(school))
                .andReturn(null);
        replayAllMocks();
        results = _helper.getApiTestResultsForSchool(school);
        verifyAllMocks();
        assertNotNull("Query returns only 1 year of API test score results",results);
        ApiResult apiTestResultForLatestYear = (ApiResult)results.get(_helper.MODEL_MOST_RECENT_API_RESULT);
        assertEquals(new Integer(650),apiTestResultForLatestYear.getTotal());
        assertEquals(new Integer(2011),apiTestResultForLatestYear.getYear());
        assertNull("",results.get(_helper.MODEL_API_GROWTH_TREND));
        assertNull("", results.get(_helper.MODEL_PREVIOUS_YEAR));
        assertNull("",results.get(_helper.MODEL_API_SCORE_CHANGE));
        assertNotNull("", results.get(_helper.MODEL_API_STATE_RANK));
        ApiResult apiStateRankResult = (ApiResult)results.get(_helper.MODEL_API_STATE_RANK);
        assertEquals(new Integer(1),apiStateRankResult.getApiStateRank());
        assertEquals(new Integer(2008),apiStateRankResult.getYear());
        assertNull("",results.get(_helper.MODEL_API_SIMILAR_SCHOOLS_RANK));
    }

    public void testGetStateTestValues() {
        State state = State.CA;
        Integer testYear = null;
        Integer dataTypeId = _helper.API_STATE_GROWTH_DATA_TYPE_ID;
        String displayTarget = TestDataSetDisplayTarget.desktop.name();
        Boolean eagerFetch = true;
        List<Map<String,Integer>> results = null;


        resetAllMocks();
        expect(_testDataStateValueDao.findValues(eq(state), eq(dataTypeId), eq(testYear), eq(ListUtils.newArrayList(displayTarget)), eq(eagerFetch)))
            .andReturn(ListUtils.newArrayList(
                getSampleStateTestValue(1, 2010, 500, 10000)
            )
        );
        replayAllMocks();

        results = _helper.getDataForStateApiGrowth(state);

        verifyAllMocks();
        assertTrue("Expect results to be empty since only one year of data was provided, and multiple are needed", results.isEmpty());


        resetAllMocks();
        expect(_testDataStateValueDao.findValues(eq(state), eq(dataTypeId), eq(testYear), eq(ListUtils.newArrayList(displayTarget)), eq(eagerFetch)))
            .andReturn(ListUtils.newArrayList(
                getSampleStateTestValue(1, 2010, 500, 10000),
                getSampleStateTestValue(1, 2011, 500, 10000)
            )
        );
        replayAllMocks();

        results = _helper.getDataForStateApiGrowth(state);

        verifyAllMocks();
        assertTrue("Expect results to be populated since valid data was provided", !results.isEmpty());
        assertEquals("Expect first item in list to have correct keys filled", new Integer(500), results.get(0).get("apiGrowth"));
        assertEquals("Expect first item in list to have correct keys filled", new Integer(2010), results.get(0).get("year"));
        assertEquals("Expect first item in list to have correct keys filled", new Integer(10000), results.get(0).get("numTested"));
    }

    public StateTestValue getSampleStateTestValue(Integer id, Integer year, Integer apiGrowth, Integer numTested) {
        Integer dataTypeId = _helper.API_STATE_GROWTH_DATA_TYPE_ID;
        return new StateTestValue(1,
            new TestDataSet(
                    id, year, Grade.ALL, LevelCode.ELEMENTARY_MIDDLE_HIGH, dataTypeId, Subject.MATH, 0, 0, true
            ),
            Float.valueOf(apiGrowth), String.valueOf(apiGrowth), numTested, true
        );
    };

    private ApiResult constructApiResultObj(Integer total,Integer year){
        ApiResult apiResult = new ApiResult();
        apiResult.setTotal(total);
        apiResult.setYear(year);
        return apiResult;
    }

    private List<ApiResult> constructApiResultObjListWith1Item(){
        List<ApiResult> apiResults = new ArrayList<ApiResult>();
        apiResults.add(constructApiResultObj(650,2011));
        return apiResults;
    }

    private List<ApiResult> constructApiResultObjList(){
        List<ApiResult> apiResults = new ArrayList<ApiResult>();
        apiResults.add(constructApiResultObj(600, 2012));
        apiResults.add(constructApiResultObj(650,2011));
        return apiResults;
    }


}