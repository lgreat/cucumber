package gs.web.school;

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
    public static final String MODEL_API_TEST_ETHNICITY_MAP = "apiTestEthnicityMap";

    @Autowired
    private IApiResultDao _apiResultDao;
    @Autowired
    private ITestDataStateValueDao _testDataStateValueDao;

    public Map<String,Object> getApiTestResults(School school) {
        if (school == null) {
            throw new IllegalArgumentException("School must not be null");
        }

        Map<String,Object> apiTestResultsMap = new HashMap<String,Object>();
        Map<String, Object> testResultsForSchool = getApiTestResultsForSchool(school);
        if (testResultsForSchool != null && testResultsForSchool.size() > 0) {
            apiTestResultsMap.putAll(testResultsForSchool);
        } else {
            return null;
        }
        apiTestResultsMap.put(MODEL_STATE_API_GROWTH_TREND, getDataForStateApiGrowth(school.getDatabaseState()));
        return apiTestResultsMap;
    }

    public Map<String, Object> getApiTestResultsForSchool(School school) {
        if (school != null && school.isActive() && school.getId() != null) {

            //Get API results for the last 4 years order by the most recent first.
            List<ApiResult> historicalApiTestResults = _apiResultDao.getApiScoresOrderByMostRecent(school, NUM_YEARS_FOR_HISTORICAL_DATA);
            if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {

                //Assumes that the API test results are in descending order of year.
                ApiResult apiTestResultForLatestYear = historicalApiTestResults.get(0);

                //To display API results, there should be results for at least 1 year of data.
                if (apiTestResultForLatestYear != null && apiTestResultForLatestYear.getYear() != null
                        && apiTestResultForLatestYear.getTotal() != null && apiTestResultForLatestYear.getTotal() != 0) {
                    Map<String, Object> apiTestResultsMap = new HashMap<String, Object>();

                    apiTestResultsMap.put(MODEL_MOST_RECENT_API_RESULT, apiTestResultForLatestYear);

                    apiTestResultsMap.put(MODEL_API_TEST_ETHNICITY_MAP, buildApiTestResultsMap(apiTestResultForLatestYear));

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

    /**
     * Builds of a map of ethnicity name to ApiResult, so that view logic is simpler and doesnt need to check
     * if each ethnicity exists
     * @param originalApiResult
     * @return
     */
    public Map<String, ApiResultForView> buildApiTestResultsMap(ApiResult originalApiResult) {

        Map<String, ApiResultForView> map = new LinkedHashMap<String, ApiResultForView>();

        if (originalApiResult.getTotal() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "All Students",
                originalApiResult.getTotal(),
                originalApiResult.getTotalNumTested(),
                originalApiResult
            );
            map.put("All Students", apiResultForView);
        }
        if (originalApiResult.getAfricanAmerican() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "African American",
                originalApiResult.getAfricanAmerican(),
                originalApiResult.getAfricanAmericanNumTested(),
                originalApiResult
            );
            map.put("African American", apiResultForView);
        }
        if (originalApiResult.getAmericanIndian() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "American Indian",
                originalApiResult.getAmericanIndian(),
                originalApiResult.getAmericanIndianNumTested(),
                originalApiResult
            );
            map.put("American Indian", apiResultForView);
        }
        if (originalApiResult.getAsian() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "Asian",
                originalApiResult.getAsian(),
                originalApiResult.getAsianNumTested(),
                originalApiResult
            );
            map.put("Asian", apiResultForView);
        }
        if (originalApiResult.getEnglishLangLearners() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "English Language Learners",
                originalApiResult.getEnglishLangLearners(),
                originalApiResult.getEnglishLangLearnersNumTested(),
                originalApiResult
            );
            map.put("English Language Learners", apiResultForView);
        }
        if (originalApiResult.getFilipino() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "Filipino",
                originalApiResult.getFilipino(),
                originalApiResult.getFilipinoNumTested(),
                originalApiResult
            );
            map.put("Filipino", apiResultForView);
        }
        if (originalApiResult.getHispanic() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "Hispanic",
                originalApiResult.getHispanic(),
                originalApiResult.getHispanicNumTested(),
                originalApiResult
            );
            map.put("Hispanic", apiResultForView);
        }
        if (originalApiResult.getPacificIslander() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "Pacific Islander",
                originalApiResult.getPacificIslander(),
                originalApiResult.getPacificIslanderNumTested(),
                originalApiResult
            );
            map.put("Pacific Islander", apiResultForView);
        }
        if (originalApiResult.getSocioEconDisadv() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "Socioeconomic Disadvantage",
                originalApiResult.getSocioEconDisadv(),
                originalApiResult.getSocioEconDisadvNumTested(),
                originalApiResult
            );
            map.put("Socioeconomic Disadvantage", apiResultForView);
        }
        if (originalApiResult.getDisabled() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "Students with disability",
                originalApiResult.getDisabled(),
                originalApiResult.getDisabledNumTested(),
                originalApiResult
            );
            map.put("Students with disability", apiResultForView);
        }
        if (originalApiResult.getWhite() != null) {
            ApiResultForView apiResultForView = new ApiResultForView(
                "White",
                originalApiResult.getWhite(),
                originalApiResult.getWhiteNumTested(),
                originalApiResult
            );
            map.put("White", apiResultForView);
        }

        return map;
    }

    public List<Map<String,Integer>> getDataForStateApiGrowth(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State must not be null");
        }

        List<Map<String,Integer>> results = new ArrayList<Map<String,Integer>>();
        List<StateTestValue> stateTestValues = _testDataStateValueDao.findValues(state, TestDataType.API_GROWTH, null, ListUtils.newArrayList(TestDataSetDisplayTarget.desktop.name()), Boolean.TRUE);

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
            int mostRecentYear = historicalApiTestResults.get(0).getYear();
            for (ApiResult apiResult : historicalApiTestResults) {
                // skip any results that are not within (number years for historical data)
                if (apiResult.getYear() != null && apiResult.getTotal() != null && apiResult.getTotal() != 0
                        && apiResult.getYear() > (mostRecentYear - NUM_YEARS_FOR_HISTORICAL_DATA)) {

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
                    && previousYearApiTestResult.getTotalBase() != null && previousYearApiTestResult.getTotalBase() != 0) {
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

    public void setTestDataStateValueDao(ITestDataStateValueDao testDataStateValueDao) {
        _testDataStateValueDao = testDataStateValueDao;
    }
    // Public class so view layer can access
    public static class ApiResultForView {
        private String _title;
        private Integer _apiScore;
        private Integer _numTested;

        private ApiResult _originalApiResult;

        ApiResultForView(String title, Integer apiScore, Integer numTested, ApiResult originalApiResult) {
            _title = title;
            _apiScore = apiScore;
            _numTested = numTested;
            _originalApiResult = originalApiResult;
        }

        public String getTitle() {
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public Integer getApiScore() {
            return _apiScore;
        }

        public void setApiScore(Integer apiScore) {
            _apiScore = apiScore;
        }

        public Integer getNumTested() {
            return _numTested;
        }

        public void setNumTested(Integer numTested) {
            _numTested = numTested;
        }

        public ApiResult getOriginalApiResult() {
            return _originalApiResult;
        }

        public void setOriginalApiResult(ApiResult originalApiResult) {
            _originalApiResult = originalApiResult;
        }
    }
}