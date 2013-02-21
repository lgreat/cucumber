package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.*;
import gs.data.util.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("apiTestResultsHelper")
public class ApiTestResultsHelper {
    public static final Integer NUM_YEARS_FOR_HISTORICAL_DATA = 4;
    public static final String MODEL_MOST_RECENT_API_RESULT = "mostRecentApiTestResult";
    public static final String MODEL_API_GROWTH_TREND = "apiGrowthTrend";
    public static final String MODEL_STATE_API_GROWTH_TREND = "apiStateGrowthTrend";
    public static final String MODEL_PREVIOUS_YEAR = "previousYear";
    public static final String MODEL_API_SCORE_CHANGE = "scoreChange";
    public static final String MODEL_API_STATE_RANK = "apiStateRank";
    public static final String MODEL_API_SIMILAR_SCHOOLS_RANK = "apiSimilarSchoolsRank";
    Integer API_STATE_GROWTH_DATA_TYPE_ID = 89;

    @Autowired
    private IApiResultDao _apiResultDao;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private ITestDataStateValueDao _testDataStateValueDao;

    public Map<String,Object> getApiTestResults(School school) {
        if (school == null) {
            throw new IllegalArgumentException("School must not be null");
        }

        Map<String,Object> apiTestResultsMap = new HashMap<String,Object>();
        apiTestResultsMap.putAll(getApiTestResultsForSchool(school));
        apiTestResultsMap.put(MODEL_STATE_API_GROWTH_TREND, getDataForStateApiGrowth(State.CA));
        return apiTestResultsMap;
    }

    public Map<String, Object> getApiTestResultsForSchool(School school) {
        if (school != null && school.isActive() && school.getId() != null) {
            //TODO remove hard coded school
            school = _schoolDao.getSchoolById(State.CA, 1);

            //Get API results for the last 4 years order by the most recent first.
            List<ApiResult> historicalApiTestResults = _apiResultDao.getApiScoresOrderByMostRecent(school, NUM_YEARS_FOR_HISTORICAL_DATA);
            if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {

                //Assumes that the API test results are in descending order of year.
                ApiResult apiTestResultForLatestYear = historicalApiTestResults.get(0);

                //To display API results, there should be results for at least 1 year of data.
                if (apiTestResultForLatestYear != null && apiTestResultForLatestYear.getYear() != null
                        && apiTestResultForLatestYear.getTotal() != null) {
                    Map<String, Object> apiTestResultsMap = new HashMap<String, Object>();

                    apiTestResultsMap.put(MODEL_MOST_RECENT_API_RESULT, apiTestResultForLatestYear);

                    putApiTestScoreChange(historicalApiTestResults, apiTestResultsMap);
                    putTrendDataForApiGrowth(historicalApiTestResults, apiTestResultsMap);
                    putMostRecentStateRank(school, apiTestResultsMap);
                    putMostRecentSimilarSchoolsRank(school, apiTestResultsMap);

                    return apiTestResultsMap;
                }
            }
        }
        return null;
    }

    public List<Map<String,Integer>> getDataForStateApiGrowth(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State must not be null");
        }

        List<Map<String,Integer>> results = new ArrayList<Map<String,Integer>>();
        List<StateTestValue> stateTestValues = _testDataStateValueDao.findValues(state, API_STATE_GROWTH_DATA_TYPE_ID, null, ListUtils.newArrayList(TestDataSetDisplayTarget.desktop.name()), Boolean.TRUE);

        //To display API results, there should be results for at least 1 year of data.
        if (!stateTestValues.isEmpty() && stateTestValues.size() > 1) {
            int mostRecentYear = stateTestValues.get(0).getDataSet().getYear();

            // skip any results that are not within (number years for historical data)
            for (StateTestValue stateTestValue : stateTestValues) {
                if (stateTestValue.getDataSet().getYear() > mostRecentYear-NUM_YEARS_FOR_HISTORICAL_DATA) {
                    Map<String,Integer> stateApiYearAndGrowth = convertStateTestValueToMap(stateTestValue);
                    results.add(stateApiYearAndGrowth);
                }
            }
        }
        return results;
    }

    protected Map<String,Integer> convertStateTestValueToMap(StateTestValue stateTestValue) {
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("apiGrowth", Math.round(stateTestValue.getValueFloat()));
        map.put("year", stateTestValue.getDataSet().getYear());
        map.put("numTested", stateTestValue.getNumberTested());
        return map;
    }

    /**
     * This method gets the data to display the trend line for api growth.
     * @param historicalApiTestResults
     * @param modelMap
     */
    protected void putTrendDataForApiGrowth(List<ApiResult> historicalApiTestResults, Map modelMap) {
        if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty() && historicalApiTestResults.size() > 1) {
            List<Map<String, Integer>> apiGrowthTrend = new ArrayList<Map<String, Integer>>();
            for (ApiResult apiResult : historicalApiTestResults) {
                if (apiResult.getYear() != null && apiResult.getTotal() != null) {

                    Map<String, Integer> apiYearAndGrowth = new HashMap<String, Integer>();
                    apiYearAndGrowth.put("apiGrowth", apiResult.getTotal());
                    apiYearAndGrowth.put("year", apiResult.getYear());
                    apiYearAndGrowth.put("numTested", apiResult.getTotalNumTested());
                    apiGrowthTrend.add(apiYearAndGrowth);
                }
            }
            modelMap.put(MODEL_API_GROWTH_TREND, apiGrowthTrend);
        }
    }

    /**
     * This method gets the change in API test score.
     * @param historicalApiTestResults
     * @param modelMap
     */
    protected void putApiTestScoreChange(List<ApiResult> historicalApiTestResults, Map modelMap) {
        //This method assumes that the API test results are in descending order of year.
        if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()
                && historicalApiTestResults.size() > 1 ) {
            ApiResult apiTestResult = historicalApiTestResults.get(0);
            int recentYear = apiTestResult.getYear();
            int previousYear = recentYear - 1;
            modelMap.put(MODEL_PREVIOUS_YEAR, previousYear);
            ApiResult previousYearApiTestResult = historicalApiTestResults.get(1);
            if (previousYear == previousYearApiTestResult.getYear() && previousYearApiTestResult != null
                    && previousYearApiTestResult.getTotalBase() != null) {
                Integer scoreChange = apiTestResult.getTotal() - previousYearApiTestResult.getTotalBase();
                modelMap.put(MODEL_API_SCORE_CHANGE, scoreChange);
            }
        }

    }

    /**
     * Method to get the most recent state rank.
     * The most recent year for state rank need not always be the most recent year of API test results.Hence a separate query.
     * @param school
     * @param modelMap
     */
    protected void putMostRecentStateRank(School school, Map modelMap) {
        ApiResult apiStateRank = _apiResultDao.getMostRecentStateRank(school);
        if (apiStateRank != null && apiStateRank.getYear() != null && apiStateRank.getApiStateRank() != null) {
            modelMap.put(MODEL_API_STATE_RANK, apiStateRank);
        }
    }

    /**
     * Method to get the most recent similar school rank.
     * The most recent year for similar school rank need not always be the most recent year of API test results.Hence a separate query.
     * @param school
     * @param modelMap
     */
    protected void putMostRecentSimilarSchoolsRank(School school, Map modelMap) {
        ApiResult apiSimilarSchoolsRank = _apiResultDao.getMostRecentSimilarSchoolsRank(school);
        if (apiSimilarSchoolsRank != null && apiSimilarSchoolsRank.getYear() != null && apiSimilarSchoolsRank.getApiSimilarRank() != null) {
            modelMap.put(MODEL_API_SIMILAR_SCHOOLS_RANK, apiSimilarSchoolsRank);
        }
    }

    void setApiResultDao(IApiResultDao apiResultDao) {
        _apiResultDao = apiResultDao;
    }

    void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setTestDataStateValueDao(ITestDataStateValueDao testDataStateValueDao) {
        _testDataStateValueDao = testDataStateValueDao;
    }
}