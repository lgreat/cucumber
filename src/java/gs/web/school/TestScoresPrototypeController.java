package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.test.*;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.xml.Sql;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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

    private static final String ERROR_VIEW = "/school/error";

    private static final Logger _log = Logger.getLogger(TestScoresPrototypeController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String schoolIdStr = request.getParameter(PARAM_SCHOOL_ID);
        String stateStr = request.getParameter(PARAM_STATE);
        Map<String, Object> model = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(stateStr) && StringUtils.isNotBlank(schoolIdStr) && (StringUtils.isNumeric(schoolIdStr))) {

            int schoolId = new Integer(schoolIdStr);
            State state = State.fromString(stateStr);

            try {
                School school = _schoolDao.getSchoolById(state, new Integer(schoolId));

                if (school.isActive()) {
                    Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> schoolValues =
                            populateSchoolValues(school);

                    List<TestToGrades> testScores = populateTestScoresBean(school, schoolValues);
                    model.put("testScores", testScores);

                } else {
                    _log.error("School id: " + schoolIdStr + " in state: " + stateStr + " is inactive.");
                    return new ModelAndView(ERROR_VIEW, model);
                }
            } catch (ObjectRetrievalFailureException ex) {
                _log.warn("Could not get a valid or active school: " +
                        schoolIdStr + " in state: " + stateStr, ex);
                return new ModelAndView(ERROR_VIEW, model);
            }
        } else {
            _log.warn("Could not get a valid or active school: " +
                    schoolIdStr + " in state: " + stateStr);
            return new ModelAndView(ERROR_VIEW, model);
        }

        return new ModelAndView(VIEW, model);
    }


    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>>
    populateSchoolValues(School school) {

        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();
        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> schoolValueMap =
                new HashMap<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>>();
        Set<Integer> existingDataSetIds = new HashSet<Integer>();

        createSchoolValuesMap(school, testDataTypeIdToTestDataType, schoolValueMap, existingDataSetIds);

        //Get the extra data.
        List<Map<String, Object>> additionalTestScores = _testDataSchoolValueDao.getSchoolTestScores(school, existingDataSetIds);

        for (Map value : additionalTestScores) {
            Integer testDataTypeId = (Integer) value.get("data_type_id");
            Integer testDataSetId = (Integer) value.get("id");
            Grade grade = Grade.getGradeLevel((String) value.get("grade"));
            LevelCode levelCode = LevelCode.createLevelCode((String) value.get("level_code"));
            Subject subject = _subjectDao.findSubject((Integer) value.get("subject_id"));
            Date yearDate = (Date) value.get("year");
            DateFormat df = new SimpleDateFormat("yyyy");
            String year = df.format(yearDate);
            Float valueFloat = (Float) value.get("value_float");
            String valueText = (String) value.get("value_text");
            Float stateAvg = (Float) value.get("stateAvgFloat");
            String testScoreValue = StringUtils.isNotBlank(valueText) ? StringEscapeUtils.escapeHtml(valueText) :
                    Integer.toString(Math.round(valueFloat));

            TestDataType testDataType = getTestDataType(testDataTypeIdToTestDataType, testDataTypeId);

            CustomTestDataSet testDataSet = new CustomTestDataSet();
            testDataSet.setYear(new Integer(year));
            testDataSet.setId(testDataSetId);
            testDataSet.setGrade(grade);
            testDataSet.setLevelCode(levelCode);
            if (stateAvg != null) {
                testDataSet.setStateAverage(Math.round(stateAvg));
            }

            if (schoolValueMap.get(testDataType) != null) {
                Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = schoolValueMap.get(testDataType);

                if (gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectsToDataSetToValueMap = gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade);
                    if (levelCodeToSubjectsToDataSetToValueMap.get(levelCode) != null) {
                        Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = levelCodeToSubjectsToDataSetToValueMap.get(levelCode);
                        if (subjectToDataSet.get(subject) != null) {
                            Map<CustomTestDataSet, String> testDataSetToValue = subjectToDataSet.get(subject);
                            testDataSetToValue.put(testDataSet, testScoreValue);
                        } else {
                            //Subject not present.
                            Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                            dataSetToValue.put(testDataSet, testScoreValue);
                            subjectToDataSet.put(subject, dataSetToValue);
                        }
                    } else {
                        //Level code not present
                        Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                        dataSetToValue.put(testDataSet, testScoreValue);
                        Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
                        subjectToDataSet.put(subject, dataSetToValue);
                        levelCodeToSubjectsToDataSetToValueMap.put(levelCode, subjectToDataSet);
                    }

                } else {
                    //Grade not present.
                    Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                    dataSetToValue.put(testDataSet, testScoreValue);
                    Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>();
                    levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                    gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
                }
            } else {
                //Test not present.
                Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                dataSetToValue.put(testDataSet, testScoreValue);
                Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>();
                levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>();
                gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
                schoolValueMap.put(testDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
            }
        }
        return schoolValueMap;
    }

    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>>
    createSchoolValuesMap(School school, Map<Integer, TestDataType> testDataTypeIdToTestDataType,
                          Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> schoolValueMap,
                          Set<Integer> existingDataSetIds) {

        List<Map<String, Object>> testScores = _testDataSetDao.getTestDataSets(school);

        for (Map value : testScores) {
            Integer testDataTypeId = (Integer) value.get("data_type_id");
            Integer testDataSetId = (Integer) value.get("id");
            Grade grade = Grade.getGradeLevel((String) value.get("grade"));
            LevelCode levelCode = LevelCode.createLevelCode((String) value.get("level_code"));
            Subject subject = _subjectDao.findSubject((Integer) value.get("subject_id"));
            Date yearDate = (Date) value.get("year");
            DateFormat df = new SimpleDateFormat("yyyy");
            String year = df.format(yearDate);
            Float valueFloat = (Float) value.get("value_float");
            String valueText = (String) value.get("value_text");
            Float stateAvg = (Float) value.get("stateAvgFloat");
            String testScoreValue = LABEL_DATA_NOT_AVAILABLE;
            if (valueFloat != null || valueText != null) {
                testScoreValue = StringUtils.isNotBlank(valueText) ? StringEscapeUtils.escapeHtml(valueText) :
                        Integer.toString(Math.round(valueFloat));
            }

            TestDataType testDataType = getTestDataType(testDataTypeIdToTestDataType, testDataTypeId);

            CustomTestDataSet testDataSet = new CustomTestDataSet();
            testDataSet.setYear(new Integer(year));
            testDataSet.setId(testDataSetId);
            testDataSet.setGrade(grade);
            testDataSet.setLevelCode(levelCode);
            if (stateAvg != null) {
                testDataSet.setStateAverage(Math.round(stateAvg));
            }

            existingDataSetIds.add(testDataSetId);

            //Check if the test is already in the map.
            if (schoolValueMap.get(testDataType) != null) {
                //Test already present.
                Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = schoolValueMap.get(testDataType);
                //Check if grade is already in the map.
                if (gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    //Grade already present.
                    Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectsToDataSetToValueMap = gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade);
                    //Check if level code is already in the map.
                    if (levelCodeToSubjectsToDataSetToValueMap.get(levelCode) != null) {
                        //Level code already present.
                        Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = levelCodeToSubjectsToDataSetToValueMap.get(levelCode);
                        //Check if subject is already in the map.
                        if (subjectToDataSet.get(subject) != null) {
                            //Subject already present.
                            Map<CustomTestDataSet, String> dataSetToValue = subjectToDataSet.get(subject);

                            //Check if DataSet is not in the map.We dont care if its already there.That should never happen.
                            if (dataSetToValue.get(testDataSet) == null) {
                                //The test score values are all filled in temporarily with "Data not available" string.
                                dataSetToValue.put(testDataSet, testScoreValue);
                            } else {
                                //TODO
                            }
                        } else {
                            //Subject not present.
                            Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                            dataSetToValue.put(testDataSet, testScoreValue);
                            subjectToDataSet.put(subject, dataSetToValue);
                        }
                    } else {
                        //Level code not present
                        Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                        dataSetToValue.put(testDataSet, testScoreValue);
                        Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
                        subjectToDataSet.put(subject, dataSetToValue);
                        levelCodeToSubjectsToDataSetToValueMap.put(levelCode, subjectToDataSet);
                    }
                } else {
                    //Grade not present.
                    Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                    dataSetToValue.put(testDataSet, testScoreValue);
                    Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>();
                    levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                    gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);

                }
            } else {
                //Test not present.
                Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                dataSetToValue.put(testDataSet, testScoreValue);
                Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>();
                levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>();
                gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
                schoolValueMap.put(testDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
            }
        }
        return schoolValueMap;
    }

    protected TestDataType getTestDataType(Map<Integer, TestDataType> testDataTypeIdToTestDataType, Integer testDataTypeId) {
        TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataTypeId);
        if (testDataTypeId != null && testDataType == null) {
            testDataType = _testDataTypeDao.getDataType(testDataTypeId);
            testDataTypeIdToTestDataType.put(testDataType.getId(), testDataType);
        }
        return testDataType;
    }

    protected List<TestToGrades> populateTestScoresBean(School school, Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> map) {
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
                        for (CustomTestDataSet testDataSet : map.get(testDataType).get(grade).get(levelCode).get(subject).keySet()) {

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
                            yearToTestScore.setStateAvg(testDataSet.getStateAverage());
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

    protected String getGradeLabel(CustomTestDataSet testData) {
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

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
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
        Integer _stateAvg;

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

        public Integer getStateAvg() {
            return _stateAvg;
        }

        public void setStateAvg(Integer stateAvg) {
            _stateAvg = stateAvg;
        }

        public int compareTo(YearToTestScore yearToTestScore) {
            return yearToTestScore.getYear().compareTo(getYear());
        }

    }

    public static class CustomTestDataSet {

        public Integer _id;
        public Integer _year;
        public Grade _grade;
        public LevelCode _levelCode;
        public Integer _stateAverage;
        public Integer _numTested;

        public Integer getId() {
            return _id;
        }

        public void setId(Integer id) {
            _id = id;
        }

        public Integer getYear() {
            return _year;
        }

        public void setYear(Integer year) {
            _year = year;
        }

        public Grade getGrade() {
            return _grade;
        }

        public void setGrade(Grade grade) {
            _grade = grade;
        }

        public LevelCode getLevelCode() {
            return _levelCode;
        }

        public void setLevelCode(LevelCode levelCode) {
            _levelCode = levelCode;
        }

        public Integer getStateAverage() {
            return _stateAverage;
        }

        public void setStateAverage(Integer stateAverage) {
            _stateAverage = stateAverage;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CustomTestDataSet)) {
                return false;
            }

            final CustomTestDataSet customTestDataSet = (CustomTestDataSet) o;

            if (!_id.equals(customTestDataSet.getId())) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            return 53 * _id.hashCode();
        }

    }

}