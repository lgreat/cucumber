package gs.web.school;

import gs.data.school.*;
import gs.data.test.*;
import gs.web.school.test.SubjectToTestValues;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileTestScores.page")
public class SchoolProfileTestScoresController extends AbstractSchoolProfileController {
    private static final Logger _log = Logger.getLogger(SchoolProfileTestScoresController.class);

    public static final String VIEW = "school/profileTestScores";
    public static final String ERROR_VIEW = "/school/error";

    public static final String LABEL_DATA_NOT_AVAILABLE = "Data not available";
    public static final String LABEL_SUBGROUP_TEST_SUFFIX = " by subgroup";

    @Autowired private ITestDataSetDao _testDataSetDao;
    @Autowired private ITestDataTypeDao _testDataTypeDao;
    @Autowired private ISubjectDao _subjectDao;
    @Autowired private ITestBreakdownDao _testBreakdownDao;
    @Autowired private ITestDescriptionDao _testDescriptionDao;

    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView getTestScores(HttpServletRequest request) {
        School school = getSchool(request);
        Map<String, Object> model = new HashMap<String, Object>();

        if (school != null) {
            if (school.isActive()) {
                model.put("testScores", getTestScores(school));
            } else {
                _log.error("School id: " + school.getId() + " in state: " + school.getDatabaseState() + " is inactive.");
                return new ModelAndView(ERROR_VIEW, model);
            }
            model.put("schoolType", school.getType().getSchoolTypeName());  // This is used when there is no test data
        } else {
            return new ModelAndView(ERROR_VIEW, model);
        }

        return new ModelAndView(VIEW, model);
    }

    /**
     * Method to get the test scores for a school.
     *
     * @param school
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
    protected List<TestToGrades> getTestScores(School school) {

        //A new map to represent a map of test data type Id to testDataSet object.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();
        //A new map to represent a map of test data type Id to max year.This is used to query the subgroup data for a test only for the most recent year.
        Map<Integer, Integer> testDataTypeIdToMaxYear = new HashMap<Integer, Integer>();
        //A map used to store the test data type, grade, level code, subjects, test data set and test score value for the school.
        Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> testScoresMap =
                new HashMap<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>>();

        //Get all the non-subgroup data points that a school should have.
        //(Fetch the data sets and the test score values if they exist.Fetch the data sets irrespective of, if the school has value or not).
        List<SchoolTestResult> nonSubgroupTestScores = _testDataSetDao.getTestDataSetsAndValues(school);
        //Fill in the testScoresMap with the non-subgroup data.
        populateTestScores(school, testDataTypeIdToTestDataType, testScoresMap, testDataTypeIdToMaxYear, nonSubgroupTestScores, false);

        //For each test get the subgroup data sets and values for the most recent year.(Making an assumption that the tests with subgroup data
        //are a subset of non-subgroup tests).
        for (Integer dataTypeId : testDataTypeIdToMaxYear.keySet()) {
            List<SchoolTestResult> subgroupTestScores = _testDataSetDao.getSubgroupTestDataSetsAndValues(school, testDataTypeIdToTestDataType.get(dataTypeId), testDataTypeIdToMaxYear.get(dataTypeId));
            if (hasSubGroupData(subgroupTestScores)) {
                //Fill in the testScoresMap with the subgroup data.
                populateTestScores(school, testDataTypeIdToTestDataType, testScoresMap, testDataTypeIdToMaxYear, subgroupTestScores, true);
            }
        }
        //A new map to represent a map of test data type Id to TestDescription object.
        Map<Integer, TestDescription> testDataTypeToDescription = getTestDataTypeToTestDescription(school, testDataTypeIdToTestDataType.keySet());

        //Use to debug
//        printAllData(testScoresMap);

        //Convert the map of test scores that was constructed above, to a list of TestToGrades bean.This bean is used in the view.
        return populateTestScoresBean(school, testScoresMap,testDataTypeToDescription);
    }

    /**
     * Checks if there is subgroup data available.If the max breakdown id is not 1(the list is ordered by breakdown_id desc)
     * then there is subgroup data.
     *
     * @param subgroupTestScores
     * @return
     */
    protected boolean hasSubGroupData(List<SchoolTestResult> subgroupTestScores) {
        if (!subgroupTestScores.isEmpty()) {
            Integer breakdownId = subgroupTestScores.get(0).getBreakdownId();
            if (breakdownId == 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loops over the results and populates the map with the test scores data.
     *
     * @param school                       -
     * @param testDataTypeIdToTestDataType - A map to represent a map of test data set Id to testDataSet object.
     * @param testScoresMap                - A map to hold the test data type, grade, level code, subjects, test data set to value.
     * @param testDataTypeIdToMaxYear      - A map to represent a map of test data set Id to max year.This is used to query the subgroup data for a test only for the most recent year.
     * @param testScoreResults             - results of the database query.Can be subgroup or non-subgroup results.
     * @param isSubgroup                   -  boolean to reflect whether the data represents subgroup data or non subgroup data.
     */
    protected void
    populateTestScores(School school, Map<Integer, TestDataType> testDataTypeIdToTestDataType,
                       Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> testScoresMap,
                       Map<Integer, Integer> testDataTypeIdToMaxYear, List<SchoolTestResult> testScoreResults, boolean isSubgroup) {

        for (SchoolTestResult testScoreResult : testScoreResults) {
            Integer testDataTypeId = testScoreResult.getTestDataTypeId();
            TestDataType testDataType = getTestDataType(testDataTypeIdToTestDataType, testDataTypeId);
            Integer testDataSetId = testScoreResult.getTestDataSetId();
            Grade grade = testScoreResult.getGrade();
            LevelCode levelCode = testScoreResult.getLevelCode();

            //If the grade=all then display only the level codes that the school has.
            //Note:This may or may not work with extra data.Re-visit this when working on GS-12963
            if (Grade.ALL.equals(grade)) {
                Set<LevelCode.Level> levelCodes = levelCode.getIndividualLevelCodes();
                for (LevelCode.Level level : levelCodes) {
                    if (!school.getLevelCode().containsLevelCode(level)) {
                        levelCode.remove(level);
                    }
                }
            }

            Subject subject = _subjectDao.findSubject(testScoreResult.getSubjectId());
            Integer year = testScoreResult.getYear();
            Float testScoreFloat = testScoreResult.getTestScoreFloat();
            String testScoreText = testScoreResult.getTestScoreText();
            Float stateAvgFloat = testScoreResult.getStateAvgFloat();
            String stateAvgText = testScoreResult.getStateAvgText();
            Integer breakdownId = testScoreResult.getBreakdownId();

            if (testDataType != null && testDataSetId != null && grade != null && levelCode != null && subject != null) {

                //default the value to 'Data not available.'
                String testScoreValue = LABEL_DATA_NOT_AVAILABLE;
                if (testScoreFloat != null || testScoreText != null) {
                    //For masking.Masking : - sometimes the state does not give exact numbers, it saves <5% passed etc.
                    //AK has a lot of masked school values.
                    testScoreValue = StringUtils.isNotBlank(testScoreText) ? StringEscapeUtils.escapeHtml(testScoreText) :
                            Integer.toString(Math.round(testScoreFloat));
                }

                //default the state average to 'Data not available.'
                String stateAvg = LABEL_DATA_NOT_AVAILABLE;
                if (stateAvgFloat != null || stateAvgText != null) {
                    //For masking.Masking : - sometimes the state does not give exact numbers, it saves <5% passed etc.
                    //AK has a lot of masked school values.
                    stateAvg = StringUtils.isNotBlank(stateAvgText) ? StringEscapeUtils.escapeHtml(stateAvgText) :
                            Integer.toString(Math.round(stateAvgFloat));
                }

                //Build a new custom test data set.
                CustomTestDataSet testDataSet = new CustomTestDataSet();
                testDataSet.setYear(year);
                testDataSet.setId(testDataSetId);
                testDataSet.setStateAverage(stateAvg);
                TestBreakdown breakdown = _testBreakdownDao.findBreakdown(breakdownId);
                if (breakdown != null) {
                    String breakdownLabel = _testBreakdownDao.findBreakdownName(breakdown, school.getDatabaseState());
                    if (breakdownLabel != null) {
                        testDataSet.setBreakdownLabel(breakdownLabel);
                        testDataSet.setBreakdownSortOrder(breakdown.getOrder());
                    }
                }

                //Construct a new map to represent a map of test data set Id to max year.
                //This is used to query the subgroup data for a test only for the most recent year.
                if (testDataTypeIdToMaxYear.containsKey(testDataTypeId)) {
                    Integer yr = testDataTypeIdToMaxYear.get(testDataTypeId);
                    if (yr > year) {
                        year = yr;
                    }
                }
                testDataTypeIdToMaxYear.put(testDataTypeId, year);

                //Build a new custom test data type.
                CustomTestDataType customTestDataType = new CustomTestDataType();
                customTestDataType.setId(testDataTypeId);
                customTestDataType.setDisplayName(testDataType.getDisplayName());
                customTestDataType.setDisplayType(testDataType.getDisplayType());
                //Group the subgroup data for a test into a new map of custom test data type.
                customTestDataType.setLabel(testDataType.getName() + (isSubgroup ? LABEL_SUBGROUP_TEST_SUFFIX : ""));
                //Fill the map with the test data type, grade, level code, subjects, test data set and value.
                buildTestScoresMap(testScoresMap, customTestDataType, grade, levelCode, subject, testDataSet, testScoreValue);
            }else{
                _log.error("Could not retrieve testDataType:" + testDataType + " testDataSetId:" + testDataSetId + " grade:" + grade + " levelCode :" + levelCode + " subject:" + subject);
            }
        }
    }

    /**
     * Fills in the map used to store the test data type, grade, level code, subjects, test data set and the value.
     *
     * @param testScoresMap
     * @param customTestDataType
     * @param grade
     * @param levelCode
     * @param subject
     * @param testDataSet
     * @param testScoreValue
     */
    protected void buildTestScoresMap(Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> testScoresMap,
                                      CustomTestDataType customTestDataType, Grade grade, LevelCode levelCode, Subject subject,
                                      CustomTestDataSet testDataSet, String testScoreValue) {
        Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = testScoresMap.get(customTestDataType);
        //Check if the test is already in the map.
        if (gradeToLevelCodeToSubjectsToDataSetToValueMap != null) {
            //Test already present.
            Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>> levelCodeToSubjectsToDataSetToValueMap = gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade);
            //Check if grade is already in the map.
            if (levelCodeToSubjectsToDataSetToValueMap != null) {
                //Grade already present.
                Map<Subject, Map<CustomTestDataSet, String>> subjectToDataSet = levelCodeToSubjectsToDataSetToValueMap.get(levelCode);
                //Check if level code is already in the map.
                if (subjectToDataSet != null) {
                    //Level code already present.
                    Map<CustomTestDataSet, String> dataSetToValue = subjectToDataSet.get(subject);
                    //Check if subject is already in the map.
                    if (dataSetToValue != null) {
                        //Subject already present.
                        //Check if DataSet is not in the map.We dont care if its already there.That should never happen.
                        if (dataSetToValue.get(testDataSet) == null) {
                            //Put the DataSet in the map.
                            dataSetToValue.put(testDataSet, testScoreValue);
                        }
                    } else {
                        //Subject not present.
                        dataSetToValue = new HashMap<CustomTestDataSet, String>();
                        dataSetToValue.put(testDataSet, testScoreValue);
                        subjectToDataSet.put(subject, dataSetToValue);
                    }
                } else {
                    //Level code not present
                    Map<CustomTestDataSet, String> dataSetToValue = new HashMap<CustomTestDataSet, String>();
                    dataSetToValue.put(testDataSet, testScoreValue);
                    subjectToDataSet = new HashMap<Subject, Map<CustomTestDataSet, String>>();
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
            gradeToLevelCodeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>();
            gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
            testScoresMap.put(customTestDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
        }
    }

    /**
     * Method to get the TestDataType. This method first checks if the data type is present in the testDataTypeIdToTestDataType map.
     * If it is present in the map it retrieves it from the map else it makes a database call to fetch it and puts it in the map.
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
    protected List<TestToGrades> populateTestScoresBean(
            School school, Map<CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<CustomTestDataSet, String>>>>> map,
            Map<Integer, TestDescription> testDataTypeToDescription) {

        boolean schoolHasData = false;
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();
        for (CustomTestDataType testDataType : map.keySet()) {
            TestToGrades testToGrades = new TestToGrades();
            testToGrades.setTestLabel(testDataType.getLabel());
            testToGrades.setTestDataTypeId(testDataType.getId());
            testToGrades.setIsSubgroup((testDataType.getLabel().indexOf(LABEL_SUBGROUP_TEST_SUFFIX) > 0));
            testToGrades.setDisplayName(testDataType.getDisplayName());
            testToGrades.setDisplayType(testDataType.getDisplayType());

            //Get the test information, like the source, scale and description.
            String description = "";
            String scale = "";
            String source = "";
            TestDescription testDescription = testDataTypeToDescription.get(testDataType.getId());
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
                } else if (TestScoresHelper.getGradeNum(testToGrades.getLowestGradeInTest()).compareTo(TestScoresHelper.getGradeNum(grade)) > 0) {
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
                        String subjectLabel;
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

                            if (!StringUtils.equals(LABEL_DATA_NOT_AVAILABLE, testScoreValue)) {
                                schoolHasData = true;
                            }
                            //For a year set the test score.
                            TestValues testValue = new TestValues();
                            testValue.setYear(testDataSet.getYear());
                            testValue.setTestScoreStr(testScoreValue);
                            testValue.setTestScoreLabel(map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet));
                            testValue.setStateAvg(testDataSet.getStateAverage());
                            testValue.setBreakdownLabel(testDataSet.getBreakdownLabel());
                            testValue.setBreakdownSortOrder(testDataSet.getBreakdownSortOrder());
                            testValuesList.add(testValue);

                            //Set the grade label.
                            gradeToSubjects.setGradeLabel(TestScoresHelper.getGradeLabel(grade,levelCode));
                        }
                        //Sort in order of years or in the order of breakdown order.
                        Collections.sort(testValuesList);
                        subjectToValues.setTestValues(testValuesList);
                        subjectToValuesList.add(subjectToValues);
                    }
                    //Sort subjects.
                    Collections.sort(subjectToValuesList);
                    gradeToSubjects.setSubjects(subjectToValuesList);
                    gradeToSubjectsList.add(gradeToSubjects);
                }
            }
            //Sort the grades.
            Collections.sort(gradeToSubjectsList);
            testToGrades.setGrades(gradeToSubjectsList);
            testToGradesList.add(testToGrades);
        }
        //Sort the tests.
        Collections.sort(testToGradesList);
        if (schoolHasData) {
            return testToGradesList;
        } else {
            // if every value for the school is "not available", then this school has no test score data
            // see GS-13489
            return new ArrayList<TestToGrades>();
        }
    }

    protected Map<Integer, TestDescription> getTestDataTypeToTestDescription(School school, Set<Integer> dataTypeIds) {
        Map<Integer, TestDescription> testDataTypeToTestDescription = new HashMap<Integer, TestDescription>();
        List<TestDescription> testDescriptions = _testDescriptionDao.findTestDescriptionsByStateAndDataTypeIds(school.getDatabaseState(), dataTypeIds);
        for (TestDescription testDescription : testDescriptions) {
            testDataTypeToTestDescription.put(testDescription.getDataTypeId(), testDescription);
        }
        return testDataTypeToTestDescription;
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
        Integer _testDataTypeId;
        Boolean _isSubgroup;
        String _displayName;
        TestDataTypeDisplayType _displayType;

        public String getDisplayName() {
            return _displayName;
        }

        public void setDisplayName(String _displayName) {
            this._displayName = _displayName;
        }

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

        public Integer getTestDataTypeId() {
            return _testDataTypeId;
        }

        public void setTestDataTypeId(Integer testDataTypeId) {
            _testDataTypeId = testDataTypeId;
        }

        public Boolean getIsSubgroup() {
            return _isSubgroup;
        }

        public void setIsSubgroup(Boolean isSubgroup) {
            _isSubgroup = isSubgroup;
        }

        public TestDataTypeDisplayType getDisplayType() {
            return _displayType;
        }

        public void setDisplayType(TestDataTypeDisplayType displayType) {
            _displayType = displayType;
        }

        //The tests should be sorted in the order of - the lowest grade in the test followed by test data type id.
        //However if the test has subgroup data then the test should be followed by subgroup test.
        public int compareTo(TestToGrades testToGrades) {
            Integer gradeNum1 = TestScoresHelper.getGradeNum(getLowestGradeInTest());
            Integer gradeNum2 = TestScoresHelper.getGradeNum(testToGrades.getLowestGradeInTest());
            Integer dataTypeId1 = getTestDataTypeId();
            Integer dataTypeId2 = testToGrades.getTestDataTypeId();

            int rval;
            if (gradeNum1.compareTo(gradeNum2) == 0) {
                if (dataTypeId1.compareTo(dataTypeId2) == 0) {
                    rval = getIsSubgroup() ? 1 : -1;
                } else {
                    rval = dataTypeId1.compareTo(dataTypeId2);
                }
            } else {
                rval = gradeNum1.compareTo(gradeNum2);
            }
            return rval;
        }
    }

    public static class GradeToSubjects implements Comparable<GradeToSubjects> {
        String _gradeLabel;
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

        public String getJsFriendlyGradeLabel() {
            String jsFriendlyGradeLabel = "";
            if (StringUtils.isNotBlank(_gradeLabel)) {
                jsFriendlyGradeLabel = _gradeLabel.replaceAll("\\s", "");
            }
            return jsFriendlyGradeLabel;
        }

        public int compareTo(GradeToSubjects gradeToSubjects) {
            if (gradeToSubjects != null && gradeToSubjects.getGrade() != null && getGrade() != null) {
                return TestScoresHelper.getGradeNum(getGrade()).compareTo(TestScoresHelper.getGradeNum(gradeToSubjects.getGrade()));
            }
            return 0;
        }
    }

    public static class TestValues implements Comparable<TestValues> {
        //testScoreStr is used to store the value to draw the bar graph.
        //For example for masked value like '>95' testScoreStr = 95
        String _testScoreStr;
        //testScoreLabel is used to display the value.
        //For example for masked value like '>95' _testScoreLabel = >95
        String _testScoreLabel;
        Integer _year;
        String _stateAvg;
        String _breakdownLabel;
        Integer _breakdownSortOrder;

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

        public String getBreakdownLabel() {
            return _breakdownLabel;
        }

        public void setBreakdownLabel(String breakdownLabel) {
            _breakdownLabel = breakdownLabel;
        }

        public Integer getBreakdownSortOrder() {
            return _breakdownSortOrder;
        }

        public void setBreakdownSortOrder(Integer breakdownSortOrder) {
            _breakdownSortOrder = breakdownSortOrder;
        }

        //For subgroup data , the sort order is based on the subgroup's(breakdown) sort order.
        //For non-subgroup data, the sort order is based on the year.
        public int compareTo(TestValues testValues) {
            if (testValues.getYear().compareTo(getYear()) == 0) {
                return getBreakdownSortOrder().compareTo(testValues.getBreakdownSortOrder());
            } else {
                return testValues.getYear().compareTo(getYear());
            }
        }
    }

    /**
     * Custom object to represent a test data type. Decided to go with a custom object instead of the TestDataType object bcos
     * a)Wanted to 2 separate objects for a test with and without subgroup data.For example for the TestDataType - DSTP
     * we want to create 2 different objects- one with no subgroup data called 'DSTP' another with subgroup data called 'DSTP_subgroup'.
     */

    public static class CustomTestDataType {

        public Integer _id;
        public String _label;
        public String _displayName;
        public TestDataTypeDisplayType _displayType;

        public String getDisplayName() {
            return _displayName;
        }

        public void setDisplayName(String _displayName) {
            this._displayName = _displayName;
        }

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

        public TestDataTypeDisplayType getDisplayType() {
            return _displayType;
        }

        public void setDisplayType(TestDataTypeDisplayType displayType) {
            _displayType = displayType;
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

        //There are tests with subgroup and non-subgroup data.
        //Id equality is not enough, since we create an additional object with the same id for the subgroup test.
        //Hence use the combination of id and label.
        public int hashCode() {
            return 53 * _id.hashCode() + _label.hashCode();
        }
    }

    /**
     * Custom object to represent a test data set. Decided to go with a custom object instead of the TestDataSet object bcos
     * a)Not using hibernate - therefore an additional query is  required to get the TestDataSet object itself.Wanted to avoid additional query.
     * b)State average,breakdown label, are not part of TestDataSet.Wanted an object to encapsulate all information.
     */
    public static class CustomTestDataSet {

        public Integer _id;
        public Integer _year;
        public String _stateAverage = "";
        public String _breakdownLabel = "";
        Integer _breakdownSortOrder;

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

        public Integer getBreakdownSortOrder() {
            return _breakdownSortOrder;
        }

        public void setBreakdownSortOrder(Integer breakdownSortOrder) {
            _breakdownSortOrder = breakdownSortOrder;
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

    void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    void setTestDataTypeDao(ITestDataTypeDao testDataTypeDao) {
        _testDataTypeDao = testDataTypeDao;
    }

    void setSubjectDao(ISubjectDao subjectDao) {
        _subjectDao = subjectDao;
    }

    void setTestBreakdownDao(ITestBreakdownDao testBreakdownDao) {
        _testBreakdownDao = testBreakdownDao;
    }

    void setTestDescriptionDao(ITestDescriptionDao testDescriptionDao) {
        _testDescriptionDao = testDescriptionDao;
    }
}
