package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("apiTestResultsHelper")
public class ApiTestResultsHelper {
    public static final Integer NUM_YEARS_FOR_HISTORICAL_DATA = 4;
    public static final String MODEL_MOST_RECENT_API_RESULT = "mostRecentApiTestResult";
    public static final String MODEL_API_GROWTH_TREND = "apiGrowthTrend";
    public static final String MODEL_PREVIOUS_YEAR = "previousYear";
    public static final String MODEL_API_SCORE_CHANGE = "scoreChange";
    public static final String MODEL_API_STATE_RANK = "apiStateRank";
    public static final String MODEL_API_SIMILAR_SCHOOLS_RANK = "apiSimilarSchoolsRank";

    @Autowired
    private IApiResultDao _apiResultDao;
    @Autowired
    private ISchoolDao _schoolDao;

    public Map<String, Object> getApiTestResults(School school) {
        if (school != null && school.isActive()) {
            //TODO remove hard coded school
            school = _schoolDao.getSchoolById(State.CA, 1);

            //Get API results for the last 4 years order by the most recent first.
            List<ApiResult> historicalApiTestResults = _apiResultDao.getApiScoresOrderByMostRecent(school, NUM_YEARS_FOR_HISTORICAL_DATA);
            if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {

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

    protected void putTrendDataForApiGrowth(List<ApiResult> historicalApiTestResults, Map modelMap) {
        if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {
            List<Map<Integer, Integer>> apiGrowthTrend = new ArrayList<Map<Integer, Integer>>();
            int previousYear = 0;
            for (ApiResult apiResult : historicalApiTestResults) {
                if (apiResult.getYear() != null && apiResult.getTotal() != null) {

                    int year = apiResult.getYear();
                    if (previousYear == 0) {
                        //continue;
                    } else if (previousYear == (year - 1)) {
                        //consecutive
                    }else if(year > previousYear){
                        Map<Integer, Integer> apiYearAndGrowth = new HashMap<Integer, Integer>();
                        apiYearAndGrowth.put((year-1), -1);
                        apiGrowthTrend.add(apiYearAndGrowth);
                    }
                    Map<Integer, Integer> apiYearAndGrowth = new HashMap<Integer, Integer>();
                    apiYearAndGrowth.put(apiResult.getYear(), apiResult.getTotal());
                    apiGrowthTrend.add(apiYearAndGrowth);
                }
                previousYear = apiResult.getYear();
            }
            //TODO check for consecutive years
            modelMap.put(MODEL_API_GROWTH_TREND, apiGrowthTrend);
        }
    }

    protected void putApiTestScoreChange(List<ApiResult> historicalApiTestResults, Map modelMap) {
        if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {
            ApiResult apiTestResult = historicalApiTestResults.get(0);
            int recentYear = apiTestResult.getYear();
            int previousYear = recentYear - 1;
            modelMap.put(MODEL_PREVIOUS_YEAR, previousYear);

            for (ApiResult apiResult : historicalApiTestResults) {
                if (previousYear == apiResult.getYear()) {
                    ApiResult previousYearApiTestResult = apiResult;
                    if (previousYearApiTestResult != null && previousYearApiTestResult.getTotalBase() != null) {
                        Integer scoreChange = apiTestResult.getTotal() - previousYearApiTestResult.getTotalBase();
                        modelMap.put(MODEL_API_SCORE_CHANGE, scoreChange);
                    }
                }
            }
        }
    }

    protected void putMostRecentStateRank(School school, Map modelMap) {
        ApiResult apiStateRank = _apiResultDao.getMostRecentStateRank(school);
        if (apiStateRank != null && apiStateRank.getYear() != null && apiStateRank.getApiStateRank() != null) {
            modelMap.put(MODEL_API_STATE_RANK, apiStateRank);
        }
    }

    protected void putMostRecentSimilarSchoolsRank(School school, Map modelMap) {
        ApiResult apiSimilarSchoolsRank = _apiResultDao.getMostRecentSimilarSchoolsRank(school);
        if (apiSimilarSchoolsRank != null && apiSimilarSchoolsRank.getYear() != null && apiSimilarSchoolsRank.getApiSimilarRank() != null) {
            modelMap.put(MODEL_API_SIMILAR_SCHOOLS_RANK, apiSimilarSchoolsRank);
        }
    }

}