package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.test.*;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

                    model.put("testScores", getTestScores(school));

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
    //TODO change the comments to reflect the custom data types.

    /**
     * Method to get the test scores for a school.
     *
     * @param school
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
    protected List<TestToGrades>
    getTestScores(School school) {

        //A new map to represent a map of test data set Id to testDataSet object.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();
        //A new map to represent a map of test data set Id to max year.This is used to query the subgroup data for a test only for the most recent year.
        Map<Integer, Integer> testDataTypeIdToMaxYear = new HashMap<Integer, Integer>();
        //A map used to store the test data type, grade, level code, subjects, test data set and test score value for the school.
        Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> testScoresMap =
                new HashMap<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>>();

        //Get all the non-subgroup data points that a school should have.
        //(Fetch the data sets irrespective of the school has value or not and the values if they exist).
        List<Map<String, Object>> nonSubgroupTestScores = _testDataSetDao.getTestDataSetsAndValues(school);
        //Fill in the schoolValueMap with the non-subgroup data.
        populateTestScores(testDataTypeIdToTestDataType, testScoresMap, testDataTypeIdToMaxYear, nonSubgroupTestScores,false);

        //For each test get the subgroup data sets and values for the most recent year.(Making an assumption that the tests with subgroup data
        //are a subset of non-subgroup tests).
        for (Integer dataTypeId : testDataTypeIdToMaxYear.keySet()) {
            List<Map<String, Object>> subgroupTestScores = _testDataSetDao.getSubgroupTestDataSetsAndValues(school, testDataTypeIdToTestDataType.get(dataTypeId), testDataTypeIdToMaxYear.get(dataTypeId));
            populateTestScores(testDataTypeIdToTestDataType, testScoresMap, testDataTypeIdToMaxYear, subgroupTestScores,true);
        }

        //Convert the map of test scores that was constructed above, to a list of TestToGrades bean.This bean is used in the view.
        List<TestToGrades> testScores = populateTestScoresBean(school, testScoresMap);

        //Use to debug
        //printAllData(schoolValueMap);

        return testScores;
    }

    /**
     * Loops over the results and populates the map with the test scores data.
     *
     * @param testDataTypeIdToTestDataType - A map to represent a map of test data set Id to testDataSet object.
     * @param schoolValueMap               - A map to hold the test data type, grade, level code, subjects, test data set to value.
     * @param testDataTypeIdToMaxYear      - A map to represent a map of test data set Id to max year.This is used to query the subgroup data for a test only for the most recent year.
     * @param testScoreResults             - results of the database query.Can be subgroup or non-subgroup results.
     * @param isSubgroup                   -  boolean to reflect whether the data represents subgroup data or non subgroup data.
     */
    protected void
    populateTestScores(Map<Integer, TestDataType> testDataTypeIdToTestDataType,
                          Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> schoolValueMap,
                          Map<Integer, Integer> testDataTypeIdToMaxYear, List<Map<String, Object>> testScoreResults,boolean isSubgroup) {

        for (Map value : testScoreResults) {
            Integer testDataTypeId = (Integer) value.get("data_type_id");
            TestDataType testDataType = getTestDataType(testDataTypeIdToTestDataType, testDataTypeId);
            Integer testDataSetId = (Integer) value.get("id");
            Grade grade = Grade.getGradeLevel((String) value.get("grade"));
            LevelCode levelCode = LevelCode.createLevelCode((String) value.get("level_code"));
            Subject subject = _subjectDao.findSubject((Integer) value.get("subject_id"));
            Date yearDate = (Date) value.get("year");
            DateFormat df = new SimpleDateFormat("yyyy");
            String year = df.format(yearDate);
            Float valueFloat = (Float) value.get("value_float");
            String valueText = (String) value.get("value_text");
            Float stateAvgFloat = (Float) value.get("stateAvgFloat");
            String stateAvgStr = (String) value.get("stateAvgText");
            Integer breakdownId = (Integer) value.get("breakdown_id");
            String breakdownLabel = (String) value.get("name");

            //default the value to 'Data not available.'
            String testScoreValue = LABEL_DATA_NOT_AVAILABLE;
            if (valueFloat != null || valueText != null) {
                testScoreValue = StringUtils.isNotBlank(valueText) ? StringEscapeUtils.escapeHtml(valueText) :
                        Integer.toString(Math.round(valueFloat));
            }

            //default the state average to 'Data not available.'
            String stateAvg = LABEL_DATA_NOT_AVAILABLE;
            if (stateAvgFloat != null || stateAvgStr != null) {
                stateAvg = StringUtils.isNotBlank(stateAvgStr) ? StringEscapeUtils.escapeHtml(stateAvgStr) :
                        Integer.toString(Math.round(stateAvgFloat));
            }

            Integer yearInt = new Integer(year);

            //Build a new custom test data set.
            CustomTestDataSet testDataSet = new CustomTestDataSet();
            testDataSet.setYear(yearInt);
            testDataSet.setId(testDataSetId);
            testDataSet.setGrade(grade);
            testDataSet.setLevelCode(levelCode);
            testDataSet.setStateAverage(stateAvg);
            //If its non subgroup data then set the breakdown label.
            if (breakdownLabel != null) {
                testDataSet.setBreakdownLabel(breakdownLabel);
            }

            //Construct a new map to represent a map of test data set Id to max year.This is used to query the subgroup data for a test only for the most recent year.
            if (testDataTypeIdToMaxYear.containsKey(testDataTypeId)) {
                Integer yr = testDataTypeIdToMaxYear.get(testDataTypeId);
                if (yr > yearInt) {
                    yearInt = yr;
                }
            }
            testDataTypeIdToMaxYear.put(testDataTypeId, yearInt);

            //Build a new custom test data type.
            CustomTestDataType customTestDataType = new CustomTestDataType();
            customTestDataType.setId(testDataTypeId);
            //Group subgroup data for a test into a new map of custom test data type.
            customTestDataType.setLabel(testDataType.getName()+ (isSubgroup? "_subgroup": ""));
            buildTestScoresMap(schoolValueMap, customTestDataType, grade, levelCode, subject, testDataSet, testScoreValue);

        }
    }

    /**
     * Fills in the map used to store the test data type, grade, level code, subjects, test data set.
     * @param schoolValueMap
     * @param customTestDataType
     * @param grade
     * @param levelCode
     * @param subject
     * @param testDataSet
     * @param testScoreValue
     */

    protected void buildTestScoresMap( Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> schoolValueMap,
                                     CustomTestDataType customTestDataType,Grade grade,LevelCode levelCode,Subject subject,
                                     CustomTestDataSet testDataSet,String testScoreValue) {
        //Check if the test is already in the map.
        if (schoolValueMap.get(customTestDataType) != null) {
            //Test already present.
            Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = schoolValueMap.get(customTestDataType);
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
                            //Put the DataSet in the map.
                            dataSetToValue.put(testDataSet, testScoreValue);
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
            schoolValueMap.put(customTestDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
        }
}

    /**
     * Method to get the TestDataType. This method first checks if the data type is present in the testDataTypeIdToTestDataType map.
     * If it is present in the map it returns it from the map else it makes a database call to fetch it and puts it in the map.
     *
     * @param testDataTypeIdToTestDataType - A map of test data set Id to testDataSet object.
     * @param testDataTypeId               - test data type id to return the test data type object for.
     * @return
     */
    protected TestDataType getTestDataType(Map<Integer, TestDataType> testDataTypeIdToTestDataType, Integer testDataTypeId) {
        TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataTypeId);
        if (testDataTypeId != null && testDataType == null) {
            testDataType = _testDataTypeDao.getDataType(testDataTypeId);
            testDataTypeIdToTestDataType.put(testDataType.getId(), testDataType);
        }
        return testDataType;
    }

    /**
     * Helper Method to populate the TestToGrades bean from a Map.This bean is used to present data in the view.
     *
     * @param map
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
    protected List<TestToGrades> populateTestScoresBean(School school, Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> map) {
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();
        for (CustomTestDataType testDataType : map.keySet()) {
            TestToGrades testToGrades = new TestToGrades();
            testToGrades.setTestLabel(testDataType.getLabel());

            //Get the test information, like the source, scale and description.
            //TODO maybe make one call?
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
                } else if (getGradeNumForSorting(testToGrades.getLowestGradeInTest()).compareTo(getGradeNumForSorting(grade)) > 0) {
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
                    List<SubjectToTestValues> subjectToValuesList = new ArrayList<SubjectToTestValues>();
                    for (Subject subject : map.get(testDataType).get(grade).get(levelCode).keySet()) {
                        SubjectToTestValues subjectToValues = new SubjectToTestValues();
                        String subjectLabel = "";
                        try {
                            subjectLabel = _subjectDao.findSubjectName(subject, school.getDatabaseState());
                        } catch (IllegalArgumentException e) {
                            subjectLabel = Subject.getName(subject);
                        }
                        subjectToValues.setSubjectLabel(subjectLabel);

                        //For every subject construct a list of years.
                        List<TestValues> testValuesList = new ArrayList<TestValues>();
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
                            TestValues testValue = new TestValues();
                            testValue.setYear(testDataSet.getYear());
                            testValue.setTestScoreStr(testScoreValue);
                            testValue.setTestScoreLabel(map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet));
                            testValue.setStateAvg(testDataSet.getStateAverage());
                            testValue.setBreakdown(testDataSet.getBreakdownLabel());
                            testValuesList.add(testValue);

                            //Set the grade label.
                            gradeToSubjects.setGradeLabel(getGradeLabel(testDataSet));
                        }
                        //Sort in order of years.
                        Collections.sort(testValuesList);
                        subjectToValues.setTestValues(testValuesList);
                        subjectToValuesList.add(subjectToValues);
                    }
                    //Sort in order of subjects.
                    Collections.sort(subjectToValuesList);
                    gradeToSubjects.setSubjects(subjectToValuesList);
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

    /**
     * Helper method to get the Grade Label to display
     *
     * @param testData
     * @return
     */
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

    /**
     * A method to help print the map of test data type, grade, level code, subjects, test data set for debugging purposes.
     *
     * @param map
     */
    protected void printAllData(Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> map) {

        for (CustomTestDataType testDataType : map.keySet()) {
            System.out.println("--testDataType---------------------" + testDataType.getLabel());
            for (Grade grade : map.get(testDataType).keySet()) {
                System.out.println("--grade--------------" + grade);
                for (LevelCode levelCode : map.get(testDataType).get(grade).keySet()) {
                    System.out.println("--levelCode----------" + levelCode.getCommaSeparatedString());
                    for (Subject subject : map.get(testDataType).get(grade).get(levelCode).keySet()) {
                        System.out.println("--subject------" + subject);

                        for (CustomTestDataSet testDataSet : map.get(testDataType).get(grade).get(levelCode).get(subject).keySet()) {
                            System.out.println("--dataSetId--" + testDataSet.getId());
                            System.out.println("--breakdownId--" + testDataSet.getBreakdownLabel());
                            System.out.println("year:" + testDataSet.getYear() + " value:" + map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet));
                        }
                    }
                }
            }
        }
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

    /**
     * A method to return a number when the Grade is of type 'All..'.This number is used to sort.
     *
     * @param grade
     * @return
     */
    public static Integer getGradeNumForSorting(Grade grade) {
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

    /**
     * Beans to encapsulate the test scores for the school.This bean is used to present data to the view.
     */
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
            return getGradeNumForSorting(getLowestGradeInTest()).compareTo(getGradeNumForSorting(testToGrades.getLowestGradeInTest()));
        }

    }

    public static class GradeToSubjects implements Comparable<GradeToSubjects> {
        String _gradeLabel;
        //TODO maybe do not use grade object if using json.since it checks each object to convert to json.
        Grade _grade;
        List<SubjectToTestValues> _subjects;

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

        public List<SubjectToTestValues> getSubjects() {
            return _subjects;
        }

        public void setSubjects(List<SubjectToTestValues> subjects) {
            _subjects = subjects;
        }

        public int compareTo(GradeToSubjects gradeToSubjects) {
            if (gradeToSubjects != null && gradeToSubjects.getGrade() != null && getGrade() != null) {
                return getGradeNumForSorting(getGrade()).compareTo(getGradeNumForSorting(gradeToSubjects.getGrade()));
            }
            return 0;
        }

    }

    public static class SubjectToTestValues implements Comparable<SubjectToTestValues> {
        String _subjectLabel;
        List<TestValues> _testValues;

        public String getSubjectLabel() {
            return _subjectLabel;
        }

        public void setSubjectLabel(String subjectLabel) {
            _subjectLabel = subjectLabel;
        }

        public List<TestValues> getTestValues() {
            return _testValues;
        }

        public void setTestValues(List<TestValues> testValues) {
            _testValues = testValues;
        }

        public int compareTo(SubjectToTestValues subjectToTestValues) {
            return getSubjectLabel().compareTo(subjectToTestValues.getSubjectLabel());
        }
    }

    public static class TestValues implements Comparable<TestValues> {
        //TODO do we need both testScoreStr and testScoreLabel?
        String _testScoreStr;
        String _testScoreLabel;
        Integer _year;
        String _stateAvg;
        String _breakdown;

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

        public String getStateAvg() {
            return _stateAvg;
        }

        public void setStateAvg(String stateAvg) {
            _stateAvg = stateAvg;
        }

        public String getBreakdown() {
            return _breakdown;
        }

        public void setBreakdown(String breakdown) {
            _breakdown = breakdown;
        }

        public int compareTo(TestValues testValues) {
            return testValues.getYear().compareTo(getYear());
        }
    }

    /**
     * Custom object to represent a test data set. Decided to go with a custom object instead of the TestDataSet object bcos
     * a)Not using hibernate - therefore an additional query is  required to get the TestDataSet object itself.Wanted to avoid additional query.
     * b)State average is not part of TestDataSet.Wanted an object to encapsulate all information including the state average.
     */
    public static class CustomTestDataSet {

        public Integer _id;
        public Integer _year;
        public Grade _grade;
        public LevelCode _levelCode;
        public String _stateAverage = "";
        public String _breakdownLabel = "";

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

        public String getStateAverage() {
            return _stateAverage;
        }

        public void setStateAverage(String stateAverage) {
            _stateAverage = stateAverage;
        }

        public String getBreakdownLabel() {
            return _breakdownLabel;
        }

        public void setBreakdownLabel(String breakdownLabel) {
            _breakdownLabel = breakdownLabel;
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

    /**
     * Custom object to represent a test data set. Decided to go with a custom object instead of the TestDataType object bcos
     * a)Wanted to 2 separate objects for a test with and without subgroup data.For example for the TestDataType - DSTP
     * we want to create 2 different objects- one with no subgroup data called 'DSTP' another with subgroup data called 'DSTP_subgroup'.
     */

    public static class CustomTestDataType {

        public Integer _id;
        public String _label;

        public Integer getId() {
            return _id;
        }

        public void setId(Integer id) {
            _id = id;
        }

        public String getLabel() {
            return _label;
        }

        public void setLabel(String label) {
            _label = label;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CustomTestDataType)) {
                return false;
            }

            final CustomTestDataType customTestDataType = (CustomTestDataType) o;

            if (!_id.equals(customTestDataType.getId()) || !_label.equals(customTestDataType.getLabel())) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            return 53 * _id.hashCode() + _label.hashCode();
        }

    }

}