package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.test.*;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestScoresPrototypeController implements Controller, IControllerFamilySpecifier {

    private ControllerFamily _controllerFamily;

    public static final String PARAM_SCHOOL_ID = "id";

    public static final String PARAM_STATE = "state";

    public static final String VIEW = "school/testScoresPrototype";

    public static final String LABEL_DATA_NOT_AVAILABLE = "Data not available";

    private ITestDataSetDao _testDataSetDao;

    private ITestDataSchoolValueDao _testDataSchoolValueDao;

    private ITestDataTypeDao _testDataTypeDao;

    private ISchoolDao _schoolDao;

    private ISubjectDao _subjectDao;

    private ITestDescriptionDao _testDescriptionDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String schoolIdStr = request.getParameter(PARAM_SCHOOL_ID);
        String stateStr = request.getParameter(PARAM_STATE);

        int schoolId = new Integer(schoolIdStr);
        State state = State.fromString(stateStr);

        School school = _schoolDao.getSchoolById(state, new Integer(schoolId));

        Map<String, Object> model = new HashMap<String, Object>();
        List<TestDataSet> dataSets = getTestDataSets();
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = getTestDataTypes(dataSets);

        Map<TestDataType, Boolean> testHasData = new HashMap<TestDataType, Boolean>();
        Map<TestDataType, Map<Grade, Boolean>> gradeForTestHasData = new HashMap<TestDataType, Map<Grade, Boolean>>();

        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> schoolValues =
                populateSchoolValues(school, dataSets, testDataTypeIdToTestDataType, testHasData, gradeForTestHasData);

        pruneEmpties(schoolValues,testHasData,gradeForTestHasData,school);

        List<TestToGrades> rval = populateTestScoresBean(school, schoolValues);
        model.put("schoolValues", rval);
        return new ModelAndView(VIEW, model);
    }

    protected void pruneEmpties(Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> schoolValues,
                                Map<TestDataType, Boolean> testHasData, Map<TestDataType, Map<Grade, Boolean>> gradeForTestHasData, School school) {

        Grades gradesInSchool = school.getGradeLevels();

        for (TestDataType testDataType : schoolValues.keySet()) {

            List<Grade> gradesToRemove = new ArrayList<Grade>();
            for (Grade grade : schoolValues.get(testDataType).keySet()) {

                //Check if school has grade
                //If it does not then check if there is data for that grade.
                //If there is no data remove it.
                boolean schoolHasGrade = gradesInSchool.contains(grade);
                if (!schoolHasGrade) {
                    boolean gradeHasData = (gradeForTestHasData.get(testDataType).get(grade) != null);
                    if (!gradeHasData) {
                        gradesToRemove.add(grade);
                    }
                }

                for (LevelCode levelCode : schoolValues.get(testDataType).get(grade).keySet()) {
                    for (Subject subject : schoolValues.get(testDataType).get(grade).get(levelCode).keySet()) {
                        for (TestDataSet testDataSet : schoolValues.get(testDataType).get(grade).get(levelCode).get(subject).keySet()) {
                        }
                    }
                }
            }

            for (Grade gradeToRemove : gradesToRemove) {
                schoolValues.get(testDataType).remove(gradeToRemove);
            }

        }
    }

    protected List<TestDataSet> getTestDataSets() {
        Set<Integer> breakdownIds = new HashSet<Integer>();
        breakdownIds.add(1);
        List<TestDataSet> testDataSets = _testDataSetDao.findDataSets(State.DE, null, null, null, null, breakdownIds, null, true, null);
        return testDataSets;
    }

    protected List<SchoolTestValue> getTestDataSchoolValues(List<TestDataSet> testDataSets, School school) {
        List<SchoolTestValue> values = _testDataSchoolValueDao.findValues(testDataSets, school);
        return values;
    }

    protected Integer getTests() {
        return 4;
    }

    protected void getGrades() {

    }

    protected void getSubjects() {

    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    protected Map<Integer, TestDataType> getTestDataTypes(List<TestDataSet> testDataSets) {
        Set<Integer> testDataTypeIds = new HashSet<Integer>();

        for (TestDataSet testDataSet : testDataSets) {
            testDataTypeIds.add(testDataSet.getDataTypeId());
        }

        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();

        if (testDataTypeIds != null && !testDataTypeIds.isEmpty()) {
            List<TestDataType> testDataTypes = _testDataTypeDao.getDataTypes(testDataTypeIds);

            for (TestDataType testDataType : testDataTypes) {
                testDataTypeIdToTestDataType.put(testDataType.getId(), testDataType);
            }
        }
        return testDataTypeIdToTestDataType;
    }

    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>
    populateSchoolValues(School school, List<TestDataSet> testDataSets,
                         Map<Integer, TestDataType> testDataTypeIdToTestDataType, Map<TestDataType, Boolean> testHasData, Map<TestDataType, Map<Grade, Boolean>> gradeForTestHasData) {

        //Get a Map to represent the test data type, grade, level code, subjects, test data set and test score value for the school.
        //The test score values are all filled in with "Data not available" string temporarily.
        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> schoolValueMap = createSchoolValuesMap(testDataSets, testDataTypeIdToTestDataType);

        //Query the for school test score values.
        List<SchoolTestValue> values = _testDataSchoolValueDao.findValues(testDataSets, school);

        //For every test score value, put it in the map constructed above i.e replace the
        //temporary string "Data not available"  with the actual test score value.
        for (SchoolTestValue value : values) {
            //TODO maybe the dao should not join.
            TestDataSet testDataSet = value.getDataSet();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataSet.getDataTypeId());
            Grade grade = testDataSet.getGrade();
            LevelCode levelCode = testDataSet.getLevelCode();
            Subject subject = testDataSet.getSubject();
            //For masking the test score.Masking : - sometimes the state does not give exact numbers, it saves <5% passed etc.
            //AK has a lot of masked school values.
            String testScoreValue = StringUtils.isNotBlank(value.getValueText()) ? StringEscapeUtils.escapeHtml(value
                    .getValueText()) : value.getValueInteger().toString();

            //Replace the temporary string "Data not available"  with the actual test score value.
            schoolValueMap.get(testDataType).get(grade).get(levelCode).get(subject).put(testDataSet, testScoreValue);

            testHasData.put(testDataType, true);

            Map<Grade, Boolean> gradeHasData = new HashMap<Grade, Boolean>();
            if (gradeForTestHasData.containsKey(testDataType)) {
                gradeHasData = gradeForTestHasData.get(testDataType);
            }
            gradeHasData.put(grade, true);
            //TODO do I need the following line?
            gradeForTestHasData.put(testDataType, gradeHasData);
        }
        return schoolValueMap;
    }

    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>
    createSchoolValuesMap(List<TestDataSet> testDataSets, Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> schoolValueMap =
                new HashMap<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>();

        for (TestDataSet testDataSet : testDataSets) {
            Grade grade = testDataSet.getGrade();
            LevelCode levelCode = testDataSet.getLevelCode();
            Subject subject = testDataSet.getSubject();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataSet.getDataTypeId());

            //Check if the test is already in the map.
            if (schoolValueMap.get(testDataType) != null) {
                //Test already present.
                Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = schoolValueMap.get(testDataType);
                //Check if grade is already in the map.
                if (gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    //Grade already present.
                    Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectsToDataSetToValueMap = gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade);
                    //Check if level code is already in the map.
                    if (levelCodeToSubjectsToDataSetToValueMap.get(levelCode) != null) {
                        //Level code already present.
                        Map<Subject, Map<TestDataSet, String>> subjectToDataSet = levelCodeToSubjectsToDataSetToValueMap.get(levelCode);
                        //Check if subject is already in the map.
                        if (subjectToDataSet.get(subject) != null) {
                            //Subject already present.
                            Map<TestDataSet, String> dataSetToValue = subjectToDataSet.get(subject);

                            //Check if DataSet is not in the map.We dont care if its already there.That should never happen.
                            if (dataSetToValue.get(testDataSet) == null) {
                                //The test score values are all filled in temporarily with "Data not available" string.
                                dataSetToValue.put(testDataSet, LABEL_DATA_NOT_AVAILABLE);
                            } else {
                                //TODO
                            }
                        } else {
                            //Subject not present.
                            Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                            dataSetToValue.put(testDataSet, LABEL_DATA_NOT_AVAILABLE);
                            subjectToDataSet.put(subject, dataSetToValue);
                        }
                    } else {
                        //Level code not present
                        Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                        dataSetToValue.put(testDataSet, LABEL_DATA_NOT_AVAILABLE);
                        Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                        subjectToDataSet.put(subject, dataSetToValue);
                        levelCodeToSubjectsToDataSetToValueMap.put(levelCode, subjectToDataSet);
                    }
                } else {
                    //Grade not present.
                    Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                    dataSetToValue.put(testDataSet, LABEL_DATA_NOT_AVAILABLE);
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<TestDataSet, String>>>();
                    levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                    gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);

                }
            } else {
                //Test not present.
                Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                dataSetToValue.put(testDataSet, LABEL_DATA_NOT_AVAILABLE);
                Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<TestDataSet, String>>>();
                levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>();
                gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
                schoolValueMap.put(testDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
            }
        }
        return schoolValueMap;
    }

    protected List<TestToGrades> populateTestScoresBean(School school, Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> map) {
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();
        for (TestDataType testDataType : map.keySet()) {
            TestToGrades testToGrades = new TestToGrades();
            testToGrades.setTestLabel(testDataType.getDisplayName());

            //Get the test information, like the source, scale and description.
            TestDescription testDescription = _testDescriptionDao.findTestDescriptionByStateAndDataTypeId(school.getDatabaseState(), testDataType.getId());
            String description = "";
            String scale = "";
            String source = "";
            if (testDescription != null) {
                description = StringUtils.isNotBlank(testDescription.getDescription()) ? testDescription.getDescription() : "";
                scale = StringUtils.isNotBlank(testDescription.getScale()) ? StringEscapeUtils.escapeHtml(testDescription.getScale()) : "";
                source = StringUtils.isNotBlank(testDescription.getSource()) ? StringEscapeUtils.escapeHtml(testDescription.getSource()) : "";
            }
            testToGrades.setDescription(description);
            testToGrades.setScale(scale);
            testToGrades.setSource(source);

            //For every test construct a list of grades.
            List<GradeToSubjects> gradeToSubjectsList = new ArrayList<GradeToSubjects>();
            for (Grade grade : map.get(testDataType).keySet()) {

                //Set the lowest grade for the test to be able to sort multiple tests.
                if (testToGrades.getLowestGradeInTest() == null) {
                    testToGrades.setLowestGradeInTest(grade);
                } else if (getGradeNum(testToGrades.getLowestGradeInTest()).compareTo(getGradeNum(grade)) > 0) {
                    testToGrades.setLowestGradeInTest(grade);
                }

                for (LevelCode levelCode : map.get(testDataType).get(grade).keySet()) {
                    GradeToSubjects gradeToSubjects = new GradeToSubjects();
                    if (Grade.ALL.equals(grade)) {
                        gradeToSubjects.setGrade(Grade.getGradeFromLevelCode(levelCode));
                    } else {
                        gradeToSubjects.setGrade(grade);
                    }

                    //For every level code construct a list of subjects.
                    List<SubjectToYears> subjectToYearsList = new ArrayList<SubjectToYears>();
                    for (Subject subject : map.get(testDataType).get(grade).get(levelCode).keySet()) {
                        SubjectToYears subjectToYears = new SubjectToYears();
                        String subjectLabel = "";
                        try {
                            subjectLabel = _subjectDao.findSubjectName(subject, school.getDatabaseState());
                        } catch (IllegalArgumentException e) {
                            subjectLabel = Subject.getName(subject);
                        }
                        subjectToYears.setSubjectLabel(subjectLabel);

                        //For every subject construct a list of years.
                        List<YearToTestScore> yearToTestScoreList = new ArrayList<YearToTestScore>();
                        for (TestDataSet testDataSet : map.get(testDataType).get(grade).get(levelCode).get(subject).keySet()) {

                            String testScoreValue = map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet);
                            //For masking the test score.Masking : - sometimes the state does not give exact numbers, it saves <5% passed etc.
                            //AK has a lot of masked school values.
                            Pattern p = Pattern.compile("\\d*(\\.*\\d+)");
                            Matcher m = p.matcher(testScoreValue);
                            if (m.find()) {
                                testScoreValue = m.group();
                            }

                            //For a year set the test score.
                            YearToTestScore yearToTestScore = new YearToTestScore();
                            yearToTestScore.setYear(testDataSet.getYear());
                            yearToTestScore.setTestScoreStr(testScoreValue);
                            yearToTestScore.setTestScoreLabel(map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet));
                            yearToTestScoreList.add(yearToTestScore);

                            //Set the grade label.
                            gradeToSubjects.setGradeLabel(getGradeLabel(testDataSet));
                        }
                        //Sort in order of years.
                        Collections.sort(yearToTestScoreList);
                        subjectToYears.setYears(yearToTestScoreList);
                        subjectToYearsList.add(subjectToYears);
                    }
                    //Sort in order of subjects.
                    Collections.sort(subjectToYearsList);
                    gradeToSubjects.setSubjects(subjectToYearsList);
                    gradeToSubjectsList.add(gradeToSubjects);
                }

            }
            //Sort in order of grades.
            Collections.sort(gradeToSubjectsList);
            testToGrades.setGrades(gradeToSubjectsList);
            testToGradesList.add(testToGrades);
        }
        //Sort the tests in order of lowest grades.
        Collections.sort(testToGradesList);
        return testToGradesList;
    }

    protected String getGradeLabel(TestDataSet testData) {
        if (testData.getGrade().getName() != null) {
            String gradeLabel = "";
            if (Grade.ALL.equals(testData.getGrade())) {
                List<String> levelsList = new ArrayList<String>();
                if (testData.getLevelCode().containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                    levelsList.add("Elementary");
                }
                if (testData.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                    levelsList.add("Middle");
                }
                if (testData.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                    levelsList.add("High");
                }
                if (levelsList.size() >= 3) {
                    gradeLabel = "All grades";
                } else if (levelsList.size() > 0 && levelsList.size() <= 2) {
                    gradeLabel = StringUtils.join(levelsList, " and ");
                    gradeLabel += " school";
                }

            } else {
                try {
                    Integer i = Integer.valueOf(testData.getGrade().getName());
                    gradeLabel = "Grade " + String.valueOf(i);
                } catch (NumberFormatException e) {
                    gradeLabel = "All grades";
                }
            }
            return gradeLabel;
        }
        return "";
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ITestDataTypeDao getTestDataTypeDao() {
        return _testDataTypeDao;
    }

    public void setTestDataTypeDao(ITestDataTypeDao testDataTypeDao) {
        _testDataTypeDao = testDataTypeDao;
    }

    public ITestDataSchoolValueDao getTestDataSchoolValueDao() {
        return _testDataSchoolValueDao;
    }

    public void setTestDataSchoolValueDao(ITestDataSchoolValueDao testDataSchoolValueDao) {
        _testDataSchoolValueDao = testDataSchoolValueDao;
    }

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public ISubjectDao getSubjectDao() {
        return _subjectDao;
    }

    public void setSubjectDao(ISubjectDao subjectDao) {
        _subjectDao = subjectDao;
    }

    public ITestDescriptionDao getTestDescriptionDao() {
        return _testDescriptionDao;
    }

    public void setTestDescriptionDao(ITestDescriptionDao testDescriptionDao) {
        _testDescriptionDao = testDescriptionDao;
    }

    public static Integer getGradeNum(Grade grade) {
        if (Grade.ALL.equals(grade)) {
            return 13;
        } else if (Grade.ALLE.equals(grade)) {
            return 14;
        } else if (Grade.ALLEM.equals(grade)) {
            return 15;
        } else if (Grade.ALLM.equals(grade)) {
            return 15;
        } else if (Grade.ALLMH.equals(grade)) {
            return 16;
        } else if (Grade.ALLH.equals(grade)) {
            return 17;
        }
        //If the grade is of not type 'All..', then return the grade value.
        return grade.getValue();
    }

    public static class TestToGrades implements Comparable<TestToGrades> {
        String _testLabel;
        List<GradeToSubjects> _grades;
        String _description;
        String _source;
        String _scale;
        Grade _lowestGradeInTest;

        public String getTestLabel() {
            return _testLabel;
        }

        public void setTestLabel(String testLabel) {
            _testLabel = testLabel;
        }

        public List<GradeToSubjects> getGrades() {
            return _grades;
        }

        public void setGrades(List<GradeToSubjects> grades) {
            _grades = grades;
        }

        public String getDescription() {
            return _description;
        }

        public void setDescription(String description) {
            _description = description;
        }

        public String getSource() {
            return _source;
        }

        public void setSource(String source) {
            _source = source;
        }

        public String getScale() {
            return _scale;
        }

        public void setScale(String scale) {
            _scale = scale;
        }

        public Grade getLowestGradeInTest() {
            return _lowestGradeInTest;
        }

        public void setLowestGradeInTest(Grade lowestGradeInTest) {
            _lowestGradeInTest = lowestGradeInTest;
        }

        public int compareTo(TestToGrades testToGrades) {
            return getGradeNum(getLowestGradeInTest()).compareTo(getGradeNum(testToGrades.getLowestGradeInTest()));
        }

    }

    public static class GradeToSubjects implements Comparable<GradeToSubjects> {
        String _gradeLabel;
        Grade _grade;
        List<SubjectToYears> _subjects;

        public String getGradeLabel() {
            return _gradeLabel;
        }

        public void setGradeLabel(String gradeLabel) {
            _gradeLabel = gradeLabel;
        }

        public Grade getGrade() {
            return _grade;
        }

        public void setGrade(Grade grade) {
            _grade = grade;
        }

        public List<SubjectToYears> getSubjects() {
            return _subjects;
        }

        public void setSubjects(List<SubjectToYears> subjects) {
            _subjects = subjects;
        }

        public int compareTo(GradeToSubjects gradeToSubjects) {
            if (gradeToSubjects != null && gradeToSubjects.getGrade() != null && getGrade() != null) {
                return getGradeNum(getGrade()).compareTo(getGradeNum(gradeToSubjects.getGrade()));
            }
            return 0;
        }

    }

    public static class SubjectToYears implements Comparable<SubjectToYears> {
        String _subjectLabel;
        List<YearToTestScore> _years;

        public String getSubjectLabel() {
            return _subjectLabel;
        }

        public void setSubjectLabel(String subjectLabel) {
            _subjectLabel = subjectLabel;
        }

        public List<YearToTestScore> getYears() {
            return _years;
        }

        public void setYears(List<YearToTestScore> years) {
            _years = years;
        }

        public int compareTo(SubjectToYears subjectToYears) {
            return getSubjectLabel().compareTo(subjectToYears.getSubjectLabel());
        }


    }

    public static class YearToTestScore implements Comparable<YearToTestScore> {
        String _testScoreStr;
        String _testScoreLabel;
        Integer _year;

        public String getTestScoreStr() {
            return _testScoreStr;
        }

        public void setTestScoreStr(String testScoreStr) {
            _testScoreStr = testScoreStr;
        }

        public String getTestScoreLabel() {
            return _testScoreLabel;
        }

        public void setTestScoreLabel(String testScoreLabel) {
            _testScoreLabel = testScoreLabel;
        }

        public Integer getYear() {
            return _year;
        }

        public void setYear(Integer year) {
            _year = year;
        }

        public int compareTo(YearToTestScore yearToTestScore) {
            return yearToTestScore.getYear().compareTo(getYear());
        }

    }


}