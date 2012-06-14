package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileTestScores.page")
public class SchoolProfileTestScoresController extends AbstractSchoolProfileController {

    @Autowired
    @Qualifier("/sandbox/testScores.page")
    private TestScoresPrototypeController _testScoresPrototypeController;

    @RequestMapping(method= RequestMethod.GET)
    public String getTestScores(ModelMap model,
                                HttpServletRequest request,
                                @RequestParam(value = "schoolId", required = false) Integer schoolId,
                                @RequestParam(value = "state", required = false) State state) {
        School school = getSchool(request, state, schoolId);

        model.put("testScores", _testScoresPrototypeController.populateTestScoresBean
                (school, _testScoresPrototypeController.populateSchoolValues(school)));
        return "school/testScoresPrototype";
    }
}
