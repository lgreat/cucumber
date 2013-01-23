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

@Controller
@RequestMapping("/test/apiTest/")
public class ApiResultsController implements ReadWriteAnnotationController {
    public static final String VIEW = "school/apiResults";

    @Autowired
    private IApiResultDao _apiResultDao;
    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "apiTest.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        School school = _schoolDao.getSchoolById(State.CA, 1);
        ApiResult apiTestResult = _apiResultDao.getMostRecentApiScores(school);

        if (apiTestResult != null && apiTestResult.getYear() != null && apiTestResult.getTotal() != null) {
            modelMap.put("apiTestResult", apiTestResult);

            int recentYear = apiTestResult.getYear();
            int previousYear = recentYear - 1;
            modelMap.put("previousYear", previousYear);
            ApiResult previousYearApiTestResult = _apiResultDao.getApiScoresByYear(school,previousYear);

            if (previousYearApiTestResult != null && previousYearApiTestResult.getTotalBase() != null) {
                Integer scoreChange = apiTestResult.getTotal() - previousYearApiTestResult.getTotalBase();
                modelMap.put("scoreChange", scoreChange);
            }

            ApiResult apiStateRank = _apiResultDao.getMostRecentStateRank(school);
            if (apiStateRank != null && apiStateRank.getYear() != null && apiStateRank.getApiStateRank() != null) {
                modelMap.put("apiStateRank", apiStateRank);
            }

            ApiResult apiSimilarSchoolsRank = _apiResultDao.getMostRecentSimilarSchoolsRank(school);
            if (apiSimilarSchoolsRank != null && apiSimilarSchoolsRank.getYear() != null && apiStateRank.getApiSimilarRank() != null) {
                modelMap.put("apiSimilarSchoolsRank", apiSimilarSchoolsRank);
            }


            return VIEW;
        }

        return null;
    }


}