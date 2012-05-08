package gs.web.school;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author aroy@greatschools.org
 */
@Controller
public class TestScoresAjaxController {
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private TestScoresHelper _testScoresHelper;

    @RequestMapping(value = "/school/testScoresAjax.page", method = RequestMethod.GET)
    public void getTestScores(HttpServletResponse response,
                              @RequestParam(value = PARAM_SCHOOL_ID, required = false) Integer schoolId,
                              @RequestParam(value = PARAM_STATE, required = false) State state) throws JSONException, IOException {
        School school = _schoolDao.getSchoolById(state, schoolId);
        List<TestScoresHelper.TestToGrades> testsToGrades = _testScoresHelper.getTestScores(school);

        JSONObject output = new JSONObject();
        JSONArray testSubjectArray = new JSONArray();

        // pull out the first subject from the first test with values, print out all the grades/values under that
        if (testsToGrades != null && testsToGrades.size() > 0) {
            TestScoresHelper.TestToGrades topTest = testsToGrades.get(0);
            output.put("testLabel", topTest.getTestLabel());
            TestScoresHelper.GradeToSubjects topGrade = topTest.getGrades().get(0);
            output.put("gradeLabel", topGrade.getGradeLabel());
            for (TestScoresHelper.SubjectToYears subject: topGrade.getSubjects()) {
                JSONObject subjectJson = new JSONObject();
                subjectJson.put("label", subject.getSubjectLabel());
                JSONArray testValueArray = new JSONArray();
                for (TestScoresHelper.YearToTestScore year: subject.getYears()) {
                    JSONObject valueJson = new JSONObject();
                    valueJson.put("year", year.getYear());
                    valueJson.put("value", year.getTestScoreStr());
                    testValueArray.put(valueJson);
                }
                subjectJson.put("values", testValueArray);
                testSubjectArray.put(subjectJson);
            }
        }

        output.put("testSubjects", testSubjectArray);
        output.write(response.getWriter());
        response.getWriter().flush();
    }
}
