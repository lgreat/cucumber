package gs.web.school;

import gs.data.school.*;
import gs.data.school.Grade;
import gs.data.test.Subject;
import org.apache.commons.lang.StringUtils;
import gs.data.source.DataSetContentType;
import gs.data.test.*;
import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import gs.web.path.DirectoryStructureUrlFields;
import org.springframework.orm.ObjectRetrievalFailureException;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class TestScoresMobileController implements Controller, IDeviceSpecificControllerPartOfPair {

    public static final String PARAM_SCHOOL_ID = "id";

    public static final String PARAM_STATE = "state";

    public static final Integer TEST_DATA_BREAKDOWN_ID = 1;

    public static final LevelCode TEST_DATA_LEVEL_CODE = null;

    public static final Integer TEST_DATA_PROFICIENCY_BAND_ID = null;

    public static final String DATA_TYPE_CONTENT_TYPE = "school";

    public static final String KEY_DATA_TYPE_IDS = "dataTypeIds";

    public static final String KEY_SUBJECTS = "subjects";

    public static final String KEY_YEARS = "years";

    public static final String VIEW = "school/testScores-mobile";

    public static final String LABEL_DATA_NOT_AVAILABLE = "Data not available";

    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;

    private ISchoolDao _schoolDao;
    private ITestScoresConfigDao _testScoresConfigDao;
    private ITestDataTypeDao _testDataTypeDao;
    private ITestDataSetDao _testDataSetDao;
    private ITestDataSchoolValueDao _testDataSchoolValueDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String schoolId = request.getParameter(PARAM_SCHOOL_ID);
        String stateStr = request.getParameter(PARAM_STATE);

        Map<String, Object> model = new HashMap<String, Object>();
        try {
            State state = State.fromString(stateStr);
            School school = _schoolDao.getSchoolById(state, new Integer(schoolId));
            model.put("school", school);
            model.put("schoolTestScores", getTestScores(school));
        } catch (ObjectRetrievalFailureException e) {
            //TODO what?
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

        List<TestToGrades> rval = new ArrayList<TestToGrades>();
        if (school == null) {
            return rval;
        }

        //Get the configurations for each level of the school.The configurations specify the tests, the years and subjects to display.
        Map<LevelCode.Level, Map> configurationMap = getTestScoresConfigurationMap(school);
        //List of testDataSets.Used to query the schoolValue.
        List<TestDataSet> testDataSetList = new ArrayList<TestDataSet>();

        //Map of testDataTypeId to testDataType object.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();

        for (LevelCode.Level level : configurationMap.keySet()) {
            //Get the Data Type Ids for a level.This specifies what test to show for a given school.
            Set<Integer> dataTypeIds = getDataTypeIds(configurationMap, level);
            //Get the Subjects for a level.This specifies what subjects to show for a given school.
            Set<Subject> subjects = getSubjects(configurationMap, level);
            //Get the years for a level.This specifies what years to show the data for.
            Set<Integer> years = getYears(configurationMap, level);

            if (dataTypeIds == null || dataTypeIds.isEmpty() || subjects == null || subjects.isEmpty() || years == null || years.isEmpty()) {
                continue;
            }

            testDataTypeIdToTestDataType.putAll(getTestDataTypes(dataTypeIds));
            //Get the Test Data sets for each level.
            //TODO look at all the params.
            List<TestDataSet> testDataSets = getTestDataSets(school, dataTypeIds, years, subjects, getGrades(), getBreakDownIds(),
                    TEST_DATA_PROFICIENCY_BAND_ID, true, TEST_DATA_LEVEL_CODE);
            if (testDataSets != null && !testDataSets.isEmpty()) {
                testDataSetList.addAll(testDataSets);
            }
        }

        if (testDataSetList != null && !testDataSetList.isEmpty()) {
            //A map used to store the testDataType, grade, subjects, testDataSet and test score value for the school.
            //If a school does not have a test score value, then it will not be in this map.
            Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap
                    = getTestDataTypeToGradeToSubjectsToDataSetToValueMap(school, testDataSetList, testDataTypeIdToTestDataType);

            //A map used to store the testDataType, grade, subjects, testDataSet that should be
            //displayed for a given school irrespective of if the school has test score value or not.
            Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> mapOfDataPointsToShow
                    = getMapOfDataPointsToShow(testDataSetList, testDataTypeIdToTestDataType);

            //Based on the data points that should be displayed for a school, fill in
            //the 'DataNotAvailable' if school does not have test score values.
            fillInMissingDataPoints(mapOfDataPointsToShow, testDataTypeToGradeToSubjectsToDataSetToValueMap);

            //Convert the temporaryMap that was constructed above, to a list of TestToGrades bean.This bean is used in the view.
            rval = populateTestScoresBean(testDataTypeToGradeToSubjectsToDataSetToValueMap);
        }
        return rval;
    }

    /**
     * Get the configurations for a school.The configurations specify the tests, the years and subjects to display for each level.
     *
     * @param school
     * @return Map of Level to a map.For example for level 'e' the key will be 'e' to map of the subjects.The key would be 'subjects'
     *         and value would be Set<Subject> that represent the subjects to display.
     */
    protected Map<LevelCode.Level, Map> getTestScoresConfigurationMap(School school) {
        List<TestScoresConfig> configs = new ArrayList<TestScoresConfig>();
        if (school != null) {
            configs = _testScoresConfigDao.getConfiguration(school.getDatabaseState(), school.getLevelCode().getIndividualLevelCodes(),
                    true, DataSetContentType.getInstance(DATA_TYPE_CONTENT_TYPE), school.getType());
        }

        Map<LevelCode.Level, Map> configMap = new HashMap<LevelCode.Level, Map>();

        //TODO is there a better way?
        for (TestScoresConfig config : configs) {
            Set<Integer> dataTypeIds = new HashSet<Integer>();
            Set<Subject> subjects = new HashSet<Subject>();
            Set<Integer> years = new HashSet<Integer>();

            if (configMap.containsKey(config.getLevel())) {
                Map info = configMap.get(config.getLevel());
                dataTypeIds = (Set<Integer>) info.get(KEY_DATA_TYPE_IDS);
                subjects = (Set<Subject>) info.get(KEY_SUBJECTS);
                years = (Set<Integer>) info.get(KEY_YEARS);
            }

            dataTypeIds.add(config.getDataTypeId());
            subjects.add(config.getSubject());
            years.add(config.getYear());

            if (!configMap.containsKey(config.getLevel())) {
                Map info = new HashMap();
                info.put(KEY_DATA_TYPE_IDS, dataTypeIds);
                info.put(KEY_SUBJECTS, subjects);
                info.put(KEY_YEARS, years);
                configMap.put(config.getLevel(), info);
            }
        }

        return configMap;
    }

    /**
     * Helper method that gets the Set of years for a level from the configuration map.
     *
     * @param configurationMap
     * @param level
     * @return
     */
    protected Set<Integer> getYears(Map configurationMap, LevelCode.Level level) {
        Set<Integer> years = new HashSet<Integer>();
        if (configurationMap.containsKey(level)) {
            Map info = (HashMap) configurationMap.get(level);
            if (info != null && info.containsKey(KEY_YEARS)) {
                years = (Set<Integer>) info.get(KEY_YEARS);
            }
        }
        return years;
    }

    /**
     * Helper method that gets the Set of data type Ids for a level from the configuration map.
     *
     * @param configurationMap
     * @param level
     * @return
     */
    protected Set<Integer> getDataTypeIds(Map configurationMap, LevelCode.Level level) {
        Set<Integer> dataTypeIds = new HashSet<Integer>();
        if (configurationMap.containsKey(level)) {
            Map info = (HashMap) configurationMap.get(level);
            if (info != null && info.containsKey(KEY_DATA_TYPE_IDS)) {
                dataTypeIds = (Set<Integer>) info.get(KEY_DATA_TYPE_IDS);
            }
        }
        return dataTypeIds;
    }

    /**
     * Helper method that gets the Set of subjects for a level from the configuration map.
     *
     * @param configurationMap
     * @param level
     * @return
     */
    protected Set<Subject> getSubjects(Map configurationMap, LevelCode.Level level) {
        Set<Subject> subjects = new HashSet<Subject>();
        if (configurationMap.containsKey(level)) {
            Map info = (HashMap) configurationMap.get(level);
            if (info != null && info.containsKey(KEY_SUBJECTS)) {
                subjects = (Set<Subject>) info.get(KEY_SUBJECTS);
            }
        }
        return subjects;
    }

    /**
     * Method to get the TestDataTypes for a list testDataTypeIds
     *
     * @param testDataTypeIds
     * @return
     */
    protected Map<Integer, TestDataType> getTestDataTypes(Set<Integer> testDataTypeIds) {
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();

        List<TestDataType> testDataTypes = _testDataTypeDao.getDataTypes(testDataTypeIds);

        for (TestDataType testDataType : testDataTypes) {
            testDataTypeIdToTestDataType.put(testDataType.getId(), testDataType);
        }
        return testDataTypeIdToTestDataType;
    }

    protected List<TestDataSet> getTestDataSets(School school, Set<Integer> dataTypeIds, Set<Integer> years,
                                                Set<Subject> subjects, Set<Grade> grades, Set<Integer> breakdownIds,
                                                Integer proficiencyBandId, Boolean activeOnly, LevelCode levelCode) {

        List<TestDataSet> testDataSets = _testDataSetDao.findDataSets(school.getDatabaseState(),
                years, dataTypeIds, subjects, grades, breakdownIds,
                proficiencyBandId, activeOnly, levelCode);
        return testDataSets;
    }

    /**
     * A map used to store the testDataType, grade, subjects, testDataSet and test score value for the school.
     * If a school does not have a test score value, then it will not be in this map.
     *
     * @param school
     * @param testDataSets
     * @param testDataTypeIdToTestDataType
     */
    protected Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>>
    getTestDataTypeToGradeToSubjectsToDataSetToValueMap(School school, List<TestDataSet> testDataSets,
                                                        Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

        //Query the for school test score values.
        List<SchoolTestValue> values = _testDataSchoolValueDao.findValues(testDataSets, school.getDatabaseState(), school);

        Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap =
                new HashMap<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>>();

        //For every test score returned, put it in the Map.
        for (SchoolTestValue value : values) {
            Grade grade = value.getDataSet().getGrade();
            Subject subject = value.getDataSet().getSubject();
            //TODO maybe the dao should not join.
            TestDataSet testDataSet = value.getDataSet();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(value.getDataSet().getDataTypeId());
            String testScoreValue = StringUtils.isNotBlank(value.getValueText()) ? value.getValueText() : value.getValueFloat().toString();

            if (testDataTypeToGradeToSubjectsToDataSetToValueMap.get(testDataType) != null) {
                //Test already present.
                Map<Grade, Map<Subject, Map<TestDataSet, String>>> gradeToSubjectsToDataSetToValueMap = testDataTypeToGradeToSubjectsToDataSetToValueMap.get(testDataType);
                //Check if grade is already in the map.
                if (gradeToSubjectsToDataSetToValueMap != null && gradeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    //Grade already present.
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = gradeToSubjectsToDataSetToValueMap.get(grade);
                    //Check if subject is already in the map.
                    if (subjectToDataSet != null && subjectToDataSet.get(subject) != null) {
                        //Subject already present.
                        Map<TestDataSet, String> dataSetToValue = subjectToDataSet.get(subject);
                        //Check if DataSet is not in the map.We dont care if its already there.That should never happen.
                        if (dataSetToValue != null && dataSetToValue.get(testDataSet) == null) {
                            dataSetToValue.put(testDataSet, testScoreValue);
                        }
                    } else {
                        //Subject not present.
                        Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                        dataSetToValue.put(testDataSet, testScoreValue);
                        subjectToDataSet.put(subject, dataSetToValue);
                    }
                } else {
                    //Grade not present.
                    Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                    dataSetToValue.put(testDataSet, testScoreValue);
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    gradeToSubjectsToDataSetToValueMap.put(grade, subjectToDataSet);

                }
            } else {
                //Test not present.
                Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                dataSetToValue.put(testDataSet, testScoreValue);
                Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<Grade, Map<Subject, Map<TestDataSet, String>>> gradeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<Subject, Map<TestDataSet, String>>>();
                gradeToSubjectsToDataSetToValueMap.put(grade, subjectToDataSet);
                testDataTypeToGradeToSubjectsToDataSetToValueMap.put(testDataType, gradeToSubjectsToDataSetToValueMap);
            }
        }

        return testDataTypeToGradeToSubjectsToDataSetToValueMap;
    }

    /**
     * A map used to store the testDataType, grade, subjects, testDataSet that should be
     * displayed for a given school irrespective of, if the school has test score value or not.
     *
     * @param testDataSets
     * @param testDataTypeIdToTestDataType
     * @return a map to hold the TestDataType to Grade to Subject to TestDataSet to value.
     */
    protected Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>>
    getMapOfDataPointsToShow(List<TestDataSet> testDataSets, Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

        Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> mapOfDataPointsToShow =
                new HashMap<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>>();

        for (TestDataSet testDataSet : testDataSets) {
            Grade grade = testDataSet.getGrade();
            Subject subject = testDataSet.getSubject();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataSet.getDataTypeId());
            Integer year = testDataSet.getYear();

//            Map<Integer, String> map1 = new HashMap<Integer, String>();
//            map1.put(year, "");
//            Map<Subject, Map<Integer, String>> map2 = new HashMap<Subject, Map<Integer, String>>();
//            map2.put(subject, map1);
//            Map<Grade, Map<Subject, Map<Integer, String>>> map3 = new HashMap<Grade, Map<Subject, Map<Integer, String>>>();
//            map3.put(grade, map2);
//            Map<TestDataType, Map<Grade, Map<Subject, Map<Integer, String>>>> map4 = new HashMap<TestDataType, Map<Grade, Map<Subject, Map<Integer, String>>>>();
//            map4.put(testDataType, map3);
//            temp.put(testDataSet,map4);

            //Check if the test is already in the map.
            if (mapOfDataPointsToShow.get(testDataType) != null) {
                //Test already present.
                Map<Grade, Map<Subject, Map<TestDataSet, String>>> gradeToSubjectsToDataSetToValueMap = mapOfDataPointsToShow.get(testDataType);
                //Check if grade is already in the map.
                if (gradeToSubjectsToDataSetToValueMap != null && gradeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    //Grade already present.
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = gradeToSubjectsToDataSetToValueMap.get(grade);
                    //Check if subject is already in the map.
                    if (subjectToDataSet != null && subjectToDataSet.get(subject) != null) {
                        //Subject already present.
                        Map<TestDataSet, String> dataSetToValue = subjectToDataSet.get(subject);
                        //Check if DataSet is not in the map.We dont care if its already there.That should never happen.
                        if (dataSetToValue != null && dataSetToValue.get(testDataSet) == null) {
                            dataSetToValue.put(testDataSet, "");
                        }
                    } else {
                        //Subject not present.
                        Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                        dataSetToValue.put(testDataSet, "");
                        subjectToDataSet.put(subject, dataSetToValue);
                    }
                } else {
                    //Grade not present.
                    Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                    dataSetToValue.put(testDataSet, "");
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    gradeToSubjectsToDataSetToValueMap.put(grade, subjectToDataSet);

                }
            } else {
                //Test not present.
                Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                dataSetToValue.put(testDataSet, "");
                Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<Grade, Map<Subject, Map<TestDataSet, String>>> gradeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<Subject, Map<TestDataSet, String>>>();
                gradeToSubjectsToDataSetToValueMap.put(grade, subjectToDataSet);
                mapOfDataPointsToShow.put(testDataType, gradeToSubjectsToDataSetToValueMap);
            }
        }
        return mapOfDataPointsToShow;
    }

    /**
     * Based on the data points that should be displayed for a school, fill in the 'DataNotAvailable' if school does not have test score values.
     *
     * @param mapOfDataPointsToShow
     * @param testDataTypeToGradeToSubjectsToDataSetToValueMap
     *
     */
    protected void fillInMissingDataPoints(Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> mapOfDataPointsToShow,
                                           Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap) {
        for (TestDataType dataType : testDataTypeToGradeToSubjectsToDataSetToValueMap.keySet()) {
            Map<Grade, Map<Subject, Map<TestDataSet, String>>> grades = testDataTypeToGradeToSubjectsToDataSetToValueMap.get(dataType);

            for (Grade grade : grades.keySet()) {
                Map<Subject, Map<TestDataSet, String>> subjects = grades.get(grade);
                for (Subject subject : subjects.keySet()) {

                    Map<TestDataSet, String> existingDataSets = subjects.get(subject);
                    Map<TestDataSet, String> dataSetsToShow = mapOfDataPointsToShow.get(dataType).get(grade).get(subject);

                    for (TestDataSet dataSet : dataSetsToShow.keySet()) {
                        if (!existingDataSets.containsKey(dataSet)) {
                            existingDataSets.put(dataSet, LABEL_DATA_NOT_AVAILABLE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper Method to populate the TestToGrades bean from a Map.
     *
     * @param map
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
    protected List<TestToGrades> populateTestScoresBean(Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> map) {
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();
        for (TestDataType testDataType : map.keySet()) {
            TestToGrades testToGrades = new TestToGrades();
            testToGrades.setTestLabel(testDataType.getName());

            //For every test construct a list of grades.
            List<GradeToSubjects> gradeToSubjectsList = new ArrayList<GradeToSubjects>();
            for (Grade grade : map.get(testDataType).keySet()) {
                GradeToSubjects gradeToSubjects = new GradeToSubjects();
                gradeToSubjects.setGrade(grade);

                //For every grade construct a list of subjects.
                List<SubjectToYears> subjectToYearsList = new ArrayList<SubjectToYears>();
                for (Subject subject : map.get(testDataType).get(grade).keySet()) {
                    SubjectToYears subjectToYears = new SubjectToYears();
                    //TODO do not get subject labels like this.
                    subjectToYears.setSubjectLabel(getSubjectLabel(subject));

                    //For every subject construct a list of years.
                    List<YearToTestScore> yearToTestScoreList = new ArrayList<YearToTestScore>();
                    for (TestDataSet testDataSet : map.get(testDataType).get(grade).get(subject).keySet()) {

                        //For a year set the test score.
                        YearToTestScore yearToTestScore = new YearToTestScore();
                        yearToTestScore.setYear(testDataSet.getYear());
                        yearToTestScore.setTestScoreStr(map.get(testDataType).get(grade).get(subject).get(testDataSet));
                        yearToTestScoreList.add(yearToTestScore);

                        //TODO this seems like the wrong place.
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
            //Sort in order of grades.
            Collections.sort(gradeToSubjectsList);
            testToGrades.setGrades(gradeToSubjectsList);
            testToGradesList.add(testToGrades);
        }
        return testToGradesList;
    }

    protected Set<Integer> getBreakDownIds() {
        Set<Integer> breakdownIds = new HashSet<Integer>();
        breakdownIds.add(TEST_DATA_BREAKDOWN_ID);
        return breakdownIds;
    }

    protected Set<Grade> getGrades() {
        return null;
    }

    /**
     * Helper method to get the Subject Label to display
     *
     * @param subject
     * @return
     */
    //TODO this method is used in mobile api also.Therefore refactor into gsdata.Also think about if its needed.
    protected String getSubjectLabel(Subject subject) {
        String subjectLabel = null;

        if (Subject.MATH.equals(subject) || Subject.GENERAL_MATHEMATICS_GRADES_6_7_STANDARDS.equals(subject)) {
            if (Subject.MATH.equals(subject)) {
                subjectLabel = "Math";
            } else if (Subject.GENERAL_MATHEMATICS_GRADES_6_7_STANDARDS.equals(subject)) {
                subjectLabel = "General Math";
            }
        } else if (Subject.READING.equals(subject) || Subject.ENGLISH_LANGUAGE_ARTS.equals(subject)) {
            if (Subject.READING.equals(subject)) {
                subjectLabel = "Reading";
            } else if (Subject.ENGLISH_LANGUAGE_ARTS.equals(subject)) {
                subjectLabel = "English Language Arts";
            }
        } else if (Subject.ENGLISH.equals(subject)) {
            subjectLabel = "English";
        } else if (Subject.ALGEBRA_I.equals(subject)) {
            subjectLabel = "Algebra";
        } else {
            throw new IllegalStateException("Invalid subject: " + subject.getSubjectId() + " " + Subject.getName(subject));
        }

        return subjectLabel;
    }

    /**
     * Helper method to get the Grade Label to display
     *
     * @param testData
     * @return
     */
    //TODO this method is used in mobile api also.Therefore refactor into gsdata.Also think about if its needed.
    protected String getGradeLabel(TestDataSet testData) {
        if (testData.getGrade().getName() != null) {
            String gradeLabel;
            if (Grade.ALL.equals(testData.getGrade())) {
                if (LevelCode.ELEMENTARY.equals(testData.getLevelCode())) {
                    gradeLabel = "Elementary school";
                } else if (LevelCode.MIDDLE.equals(testData.getLevelCode())) {
                    gradeLabel = "Middle school";
                } else if (LevelCode.HIGH.equals(testData.getLevelCode())) {
                    gradeLabel = "High school";
                } else {
                    gradeLabel = "All grades";
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
     * Beans to encapsulate the test scores for the school.This bean is used to present data to the view.
     */

    public static class TestToGrades {
        String _testLabel;
        List<GradeToSubjects> _grades;

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
                return getGrade().compareTo(gradeToSubjects.getGrade());
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
        Integer _year;

        public String getTestScoreStr() {
            return _testScoreStr;
        }

        public void setTestScoreStr(String testScoreStr) {
            _testScoreStr = testScoreStr;
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

    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        return true;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public ITestDataTypeDao getTestDataTypeDao() {
        return _testDataTypeDao;
    }

    public void setTestDataTypeDao(ITestDataTypeDao testDataTypeDao) {
        _testDataTypeDao = testDataTypeDao;
    }

    public ITestScoresConfigDao getTestScoresConfigDao() {
        return _testScoresConfigDao;
    }

    public void setTestScoresConfigDao(ITestScoresConfigDao testScoresConfigDao) {
        _testScoresConfigDao = testScoresConfigDao;
    }

    public ITestDataSchoolValueDao getTestDataSchoolValueDao() {
        return _testDataSchoolValueDao;
    }

    public void setTestDataSchoolValueDao(ITestDataSchoolValueDao testDataSchoolValueDao) {
        _testDataSchoolValueDao = testDataSchoolValueDao;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }
}