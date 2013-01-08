package gs.web.test;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestApiDao;
import gs.data.test.TestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/test/apiTest.page")
public class ApiTestController {
    public static final String VIEW = "test/apiTest";

    @Autowired
    private ITestApiDao _testApiDao;
    @Autowired
    private ISchoolDao _schoolDao;

    public String showForm(ModelMap modelMap, HttpServletRequest request) {
         School school = _schoolDao.getSchoolById(State.CA,1);
           List<TestApi> results = _testApiDao.getApiScoresBySchoolByYear(2012, school);
        System.out.println("---results---------------------"+results.size());
       return VIEW;
    }

}