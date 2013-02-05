package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ApiResult;
import gs.data.test.IApiResultDao;
import gs.web.util.ReadWriteAnnotationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/test/apiTest/")
public class ApiResultsController implements ReadWriteAnnotationController {
    public static final String VIEW = "school/apiResults";
    public static final Integer NUM_YEARS_FOR_HISTORICAL_DATA = 4;

    @Autowired
    private IApiResultDao _apiResultDao;
    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "apiTest.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        School school = _schoolDao.getSchoolById(State.CA, 1);

        //Get API results for the last 4 years order by the most recent first.
        List<ApiResult> historicalApiTestResults = _apiResultDao.getApiScoresOrderByMostRecent(school, NUM_YEARS_FOR_HISTORICAL_DATA);

        putApiTestResultForLatestYear(historicalApiTestResults, modelMap);

        if (modelMap.containsKey("apiTestResult")) {
            putApiTestScoreChange(historicalApiTestResults, modelMap);
            putTrendDataForApiGrowth(historicalApiTestResults, modelMap);
            putMostRecentStateRank(school,modelMap);
            putMostRecentSimilarSchoolsRank(school,modelMap);
            return VIEW;
        }
        return null;
    }

    protected void putApiTestResultForLatestYear(List<ApiResult> historicalApiTestResults, ModelMap modelMap) {
        if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {
            ApiResult apiTestResult = historicalApiTestResults.get(0);
            if (apiTestResult != null && apiTestResult.getYear() != null && apiTestResult.getTotal() != null) {
                modelMap.put("apiTestResult", apiTestResult);
            }
        }
    }

    protected void putTrendDataForApiGrowth(List<ApiResult> historicalApiTestResults,ModelMap modelMap){
        if(historicalApiTestResults != null && !historicalApiTestResults.isEmpty()){
            List<Map<Integer,Integer>> apiGrowthTrend = new ArrayList<Map<Integer, Integer>>();
            for(ApiResult apiResult: historicalApiTestResults){
                if(apiResult.getYear() != null && apiResult.getTotal() != null){
                    Map<Integer,Integer> apiYearAndGrowth = new HashMap<Integer, Integer>();
                    apiYearAndGrowth.put(apiResult.getYear(),apiResult.getTotal());
                    apiGrowthTrend.add(apiYearAndGrowth);
                }
            }
            //TODO check for consecutive years
            modelMap.put("apiGrowthTrend",apiGrowthTrend);
        }
    }

    protected void putApiTestScoreChange(List<ApiResult> historicalApiTestResults, ModelMap modelMap) {
        if (historicalApiTestResults != null && !historicalApiTestResults.isEmpty()) {
            ApiResult apiTestResult = historicalApiTestResults.get(0);
            int recentYear = apiTestResult.getYear();
            int previousYear = recentYear - 1;
            modelMap.put("previousYear", previousYear);

            for(ApiResult apiResult: historicalApiTestResults){
                 if(previousYear == apiResult.getYear()){
                     ApiResult previousYearApiTestResult = apiResult;
                     if (previousYearApiTestResult != null && previousYearApiTestResult.getTotalBase() != null) {
                         Integer scoreChange = apiTestResult.getTotal() - previousYearApiTestResult.getTotalBase();
                         modelMap.put("scoreChange", scoreChange);
                     }
                 }
            }
        }
    }

    protected void putMostRecentStateRank(School school, ModelMap modelMap) {
        ApiResult apiStateRank = _apiResultDao.getMostRecentStateRank(school);
        if (apiStateRank != null && apiStateRank.getYear() != null && apiStateRank.getApiStateRank() != null) {
            modelMap.put("apiStateRank", apiStateRank);
        }
    }

    protected void putMostRecentSimilarSchoolsRank(School school, ModelMap modelMap) {
        ApiResult apiSimilarSchoolsRank = _apiResultDao.getMostRecentSimilarSchoolsRank(school);
        if (apiSimilarSchoolsRank != null && apiSimilarSchoolsRank.getYear() != null && apiSimilarSchoolsRank.getApiSimilarRank() != null) {
            modelMap.put("apiSimilarSchoolsRank", apiSimilarSchoolsRank);
        }
    }

}