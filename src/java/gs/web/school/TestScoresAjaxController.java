package gs.web.school;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.Subject;
import gs.data.test.TestDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author aroy@greatschools.org
 */
@Controller
public class TestScoresAjaxController {
    //the list of subjects that will be returned with API test data (aka proficiency data)
    public static final Subject[] TEST_DATA_SUBJECT_IDS = {Subject.READING, Subject.ENGLISH_LANGUAGE_ARTS, Subject.MATH, Subject.GENERAL_MATHEMATICS_GRADES_6_7_STANDARDS};
    //the list of grades that will be returned with the API test data
    public static final Grade[] TEST_DATA_GRADES = {Grade.G_4, Grade.G_8, Grade.G_10, Grade.ALL};
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";

    @Autowired
    private ITestDataSetDao _testDataSetDao;
    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "/school/testScoresAjax.page", method = RequestMethod.GET)
    public void getTestScores(HttpServletResponse response,
                              @RequestParam(value = PARAM_SCHOOL_ID, required = false) Integer schoolId,
                              @RequestParam(value = PARAM_STATE, required = false) State state) throws JSONException, IOException {
        School school = _schoolDao.getSchoolById(state, schoolId);

        List<SchoolTestValue> testValues = getSchoolTestValues(school);
        JSONObject output = new JSONObject();
        JSONArray testValueArray = new JSONArray();
        for (SchoolTestValue value: testValues) {
            JSONObject valueObject = new JSONObject();
            valueObject.put("gradeName", value.getDataSet().getGrade().getName());
            valueObject.put("year", value.getDataSet().getYear());
            valueObject.put("subject", value.getDataSet().getSubject().toString());
            valueObject.put("value", value.getValueInteger());
            testValueArray.put(valueObject);
        }
        output.put("testScores", testValueArray);
        output.write(response.getWriter());
        response.getWriter().flush();
    }

    protected List<SchoolTestValue> getSchoolTestValues(School school) {
        List<SchoolTestValue> schoolValues = new ArrayList<SchoolTestValue>();
        if (school == null) {
            return schoolValues;
        }

        List<TestDataSet> testDataSets = _testDataSetDao.findTestDataSetsForMobileApi
                (school.getDatabaseState(), TEST_DATA_SUBJECT_IDS, TEST_DATA_GRADES);

        for (TestDataSet testData : testDataSets) {
            SchoolTestValue schoolValue = _testDataSetDao.findValue(testData, school.getDatabaseState(), school.getId());
            if (schoolValue != null) {
                schoolValues.add(schoolValue);
            }
        }

        filterProficiencyResultsByEnglishSubject(schoolValues);
        return schoolValues;
    }

    /**
     * For each grade, if there is a proficiency result for Reading, use that;
     * otherwise, use English Language Arts. Do not include both.
     */
    protected void filterProficiencyResultsByEnglishSubject(List<SchoolTestValue> schoolTestValues) {
        if (schoolTestValues != null) {
            Set<Grade> hasEla = new HashSet<Grade>();
            Set<Grade> hasReading = new HashSet<Grade>();
            for (SchoolTestValue value : schoolTestValues) {
                if (Subject.READING.equals(value.getDataSet().getSubject())) {
                    hasReading.add(value.getDataSet().getGrade());
                } else if (Subject.ENGLISH_LANGUAGE_ARTS.equals(value.getDataSet().getSubject())) {
                    hasEla.add(value.getDataSet().getGrade());
                }
            }

            Iterator<SchoolTestValue> it = schoolTestValues.iterator();
            while (it.hasNext()) {
                SchoolTestValue result = it.next();
                if (Subject.ENGLISH_LANGUAGE_ARTS.equals(result.getDataSet().getSubject())) {
                    if (hasReading.contains(result.getDataSet().getGrade())
                            && hasEla.contains(result.getDataSet().getGrade())) {
                        it.remove();
                    }
                }
            }
        }
    }
}
