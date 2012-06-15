package gs.web.school;

import gs.data.school.Grade;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author aroy@greatschools.org
 */
@Component
public class TestScoresHelper {
    public static final String LABEL_DATA_NOT_AVAILABLE = "Data not available";
    private static final Logger _log = Logger.getLogger(TestScoresHelper.class);

    @Autowired
    private ITestDataTypeDao _testDataTypeDao;
    @Autowired
    private ITestDataSetDao _testDataSetDao;
    @Autowired
    private ITestDataSchoolValueDao _testDataSchoolValueDao;
    @Autowired
    private ITestDescriptionDao _testDescriptionDao;
    @Autowired
    private ISubjectDao _subjectDao;
    @Autowired
    private IViewTestDataSetsDao _viewTestDataSetsDao;

    /**
     * Method to get the test scores for a school.
     *
     * @param school
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
    protected List<TestToGrades> getTestScores(School school) {

        List<TestToGrades> rval = new ArrayList<TestToGrades>();
        if (school == null) {
            _log.warn("School is null.");
            return rval;
        }

        //Get the list of test data set Ids to pull out for a school.Queries the test_data_sets table.
        List<Integer> testDataSetIds = getTestDataSetIds(school);
        if (testDataSetIds == null || testDataSetIds.isEmpty()) {
            _log.warn("No test data set ids to display for school id: " + school.getId());
            return rval;
        }

        //Get the list of testDataSet objects from the list of test data set Ids.Queries the testDataSet table.
        List<TestDataSet> testDataSets = getTestDataSets(school, testDataSetIds);
        if (testDataSets == null || testDataSets.isEmpty()) {
            _log.warn("No test data sets to display for school id: " + school.getId());
            return rval;
        }

        //Get a list of data type Ids.Data type Ids represent a test.
        Set<Integer> dataTypeIds = new HashSet<Integer>();
        for (TestDataSet testDataSet : testDataSets) {
            dataTypeIds.add(testDataSet.getDataTypeId());
        }

        //Get a Map of test data type Id to test data type object.Queries the testDataType table.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = getTestDataTypes(dataTypeIds);

        //A map used to store the test data type, grade, level code, subjects, test data set and test score value for the school.
        //Queries the TestDataSchoolValue table.
        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> schoolValues
                = populateSchoolValues(school, testDataSets, testDataTypeIdToTestDataType);

        //Convert the map that was constructed above, to a list of TestToGrades bean.This bean is used in the view.
        rval = populateTestScoresBean(school, schoolValues);

        //Removes empty data points.If all the years for a given subject do not have test values then do not display the subject.
        //If all the subjects in a given grade do not have test values then do not display the grade.
        //If all the grades in a given given test do not have test values then do not display the test.
        //Example : If ECA test for grade 4 for Math for years 2009,2010 and 2011 does not have data and grade 5 Math for year 2009 and 2010 has data.
        //Then do not display Math and grade 4.
        pruneEmpties(rval);

        //use this for debugging
        //printAllData(testDataTypeToGradeToSubjectsToDataSetToValueMap);

        return rval;
    }

    /**
     * Get the test data set Ids to show for the school.
     *
     * @param school
     * @return
     */
    protected List<Integer> getTestDataSetIds(School school) {
        return _viewTestDataSetsDao.getViewTestDataSetsIds(school.getDatabaseState());
    }

    /**
     * Get the test data sets given a school and test data set ids.
     *
     * @param school
     * @param testDataSetIds
     * @return
     */
    protected List<TestDataSet> getTestDataSets(School school, List<Integer> testDataSetIds) {
        return _testDataSetDao.findDataSets(school.getDatabaseState(), testDataSetIds, true);
    }

    /**
     * Method to get the TestDataTypes for a list of test data type Ids
     *
     * @param testDataTypeIds
     * @return
     */
    protected Map<Integer, TestDataType> getTestDataTypes(Set<Integer> testDataTypeIds) {
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();
        if (testDataTypeIds != null && !testDataTypeIds.isEmpty()) {
            List<TestDataType> testDataTypes = _testDataTypeDao.getDataTypes(testDataTypeIds);

            for (TestDataType testDataType : testDataTypes) {
                testDataTypeIdToTestDataType.put(testDataType.getId(), testDataType);
            }
        }
        return testDataTypeIdToTestDataType;
    }

    /**
     * A map used to store the test data type, grade, level code, subjects, test data set and test score value for the school.
     *
     * @param school
     * @param testDataSets
     * @param testDataTypeIdToTestDataType
     */
    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>
    populateSchoolValues(School school, List<TestDataSet> testDataSets,
                         Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

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
            if (testDataSet != null && testDataType != null && grade != null && levelCode != null && subject != null) {
                //For masking the test score.Masking : - sometimes the state does not give exact numbers, it saves <5% passed etc.
                //AK has a lot of masked school values.
                String testScoreValue = StringUtils.isNotBlank(value.getValueText()) ? StringEscapeUtils.escapeHtml(value
                        .getValueText()) : value.getValueInteger().toString();

                //Replace the temporary string "Data not available"  with the actual test score value.
                schoolValueMap.get(testDataType).get(grade).get(levelCode).get(subject).put(testDataSet, testScoreValue);
            }
        }
        return schoolValueMap;
    }

    /**
     * Creates a map used to store the test data type, grade, level code, subjects, test data set that should be
     * displayed for a given school irrespective of, if the school has test score value or not.The test score values are all
     * filled in temporarily with "Data not available" string.
     *
     * @param testDataSets
     * @param testDataTypeIdToTestDataType
     * @return a map to hold the est data type, grade, level code, subjects, test data set to value.
     */
    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>
    createSchoolValuesMap(List<TestDataSet> testDataSets, Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> schoolValueMap =
                new HashMap<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>();

        for (TestDataSet testDataSet : testDataSets) {
            Grade grade = testDataSet.getGrade();
            LevelCode levelCode = testDataSet.getLevelCode();
            Subject subject = testDataSet.getSubject();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataSet.getDataTypeId());
            if (testDataSet != null && testDataType != null && grade != null && levelCode != null && subject != null) {
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
                                    _log.warn("Data set:" + testDataSet.getId() + " already in the map");
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
        }
        return schoolValueMap;
    }

    /**
     * Helper Method to populate the TestToGrades bean from a Map.This bean is used to present data in the view.
     *
     * @param map
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
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

    /**
     * Helper method to get the Grade Label to display
     *
     * @param testData
     * @return
     */
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

    /**
     * A method to help print the map of test data type, grade, level code, subjects, test data set for debugging purposes.
     *
     * @param map
     */

    protected void printAllData(Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> map) {

        for (TestDataType testDataType : map.keySet()) {
            System.out.println("--testDataType---------------------" + testDataType.getName());
            for (Grade grade : map.get(testDataType).keySet()) {
                System.out.println("--grade--------------" + grade);
                for (LevelCode levelCode : map.get(testDataType).get(grade).keySet()) {
                    System.out.println("--levelCode----------" + levelCode.getCommaSeparatedString());
                    for (Subject subject : map.get(testDataType).get(grade).get(levelCode).keySet()) {
                        System.out.println("--subject------" + subject);

                        for (TestDataSet testDataSet : map.get(testDataType).get(grade).get(levelCode).get(subject).keySet()) {
                            System.out.println("--dataSetId--" + testDataSet.getId());
                            System.out.println("year:" + testDataSet.getYear() + " value:" + map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet));
                        }
                    }
                }
            }
        }
    }

    /**
     * A method to return a number when the Grade is of type 'All..'.This number is used to sort.
     *
     * @param grade
     * @return
     */
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

    /**
     * Removes empty data points.If all the years for a given subject do not have test values then do not display the subject.
     * If all the subjects in a given grade do not have test values then do not display the grade.
     * If all the grades in a given given test do not have test values then do not display the test.
     * Example : If ECA test for grade 4 for Math for years 2009,2010 and 2011 does not have data and grade 5 Math for year 2009 and 2010 has data.
     * Then do not display Math and grade 4.
     *
     * @param testToGradesList
     */

    public void pruneEmpties(List<TestToGrades> testToGradesList) {

        //For every test, remove the test from the list if there is no data for the test.
        List<TestToGrades> testToGradesListToRemove = new ArrayList<TestToGrades>();
        for (TestToGrades testToGrades : testToGradesList) {
            testToGrades.pruneEmpties();
            if (testToGrades.getGrades().isEmpty()) {
                testToGradesListToRemove.add(testToGrades);
            }
        }
        testToGradesList.removeAll(testToGradesListToRemove);
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
            return getGradeNum(getLowestGradeInTest()).compareTo(getGradeNum(testToGrades.getLowestGradeInTest()));
        }

        //For every grade in the test, remove the grade from the list if there is no data for the grade.
        public void pruneEmpties() {
            List<GradeToSubjects> gradesToSubjectsToRemove = new ArrayList<GradeToSubjects>();
            for (GradeToSubjects gradeToSubjects : getGrades()) {
                gradeToSubjects.pruneEmpties();
                if (gradeToSubjects.getSubjects().isEmpty()) {
                    gradesToSubjectsToRemove.add(gradeToSubjects);
                }
            }
            getGrades().removeAll(gradesToSubjectsToRemove);
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

        //For every subject in the grade, remove the subject from the list if there is no data for the subject.
        public void pruneEmpties() {
            List<SubjectToYears> subjectToYearsToRemove = new ArrayList<SubjectToYears>();
            for (SubjectToYears subjectToYears : getSubjects()) {
                subjectToYears.pruneEmpties();
                if (subjectToYears.getYears().isEmpty()) {
                    subjectToYearsToRemove.add(subjectToYears);
                }
            }
            getSubjects().removeAll(subjectToYearsToRemove);
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

        //Return false if there is data for at least one year in the subject.
        public boolean isUnavailable() {
            for (YearToTestScore yearToTestScore : getYears()) {
                if (!yearToTestScore.isUnavailable()) {
                    return false;
                }
            }
            return true;
        }

        //If there no data for at least one year in the subject then remove/clear all the years.
        public void pruneEmpties() {
            if (isUnavailable()) {
                getYears().clear();
            }
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

        //Return false if there is test value for the year.
        public boolean isUnavailable() {
            return getTestScoreStr().equals(LABEL_DATA_NOT_AVAILABLE);
        }

    }
}
