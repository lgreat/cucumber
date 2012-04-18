package gs.web.school;

import gs.data.school.*;
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

    public static final String VIEW = "school/testScores-mobile";

    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;

    private ISchoolDao _schoolDao;
    private ITestDataSetDao _testDataSetDao;
    private ITestDataTypeDao _testDataTypeDao;

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
        //Get the Data Type Ids.This specifies what test to show for a given school.
        List<Integer> dataTypeIds = getDataTypeIds(school);
        if (dataTypeIds == null || dataTypeIds.isEmpty()) {
            return rval;
        }

        //Get the Test Data sets.
        List<TestDataSet> testDataSets = getTestDataSets(school, dataTypeIds);
        if (testDataSets == null || testDataSets.isEmpty()) {
            return rval;
        }

        //Get the Map of dataTypeId to TestDataType.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = getTestDataTypes(dataTypeIds);
        if (testDataTypeIdToTestDataType == null || testDataTypeIdToTestDataType.isEmpty()) {
            return rval;
        }

        //A temporary map used to store the grade subjects testDataSet and value.
        //We use this map later to construct a list to display in the view.
        Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, SchoolTestValue>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap =
                new HashMap<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, SchoolTestValue>>>>();

        for (TestDataSet testDataSet : testDataSets) {
            //todo maybe do a batch fetch.
            //Get the test scores for the testDataSet.
            SchoolTestValue testValue = _testDataSetDao.findValue(testDataSet, school.getDatabaseState(), school.getId());
            if (testValue != null && testDataSet != null && testDataSet.getGrade() != null && testDataSet.getSubject() != null) {

                Grade grade = testDataSet.getGrade();
                Subject subject = testDataSet.getSubject();
                TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataSet.getDataTypeId());

                //Check if the test is already in the map.
                if (testDataTypeToGradeToSubjectsToDataSetToValueMap.get(testDataType) != null) {
                    //Test already present.
                    Map<Grade, Map<Subject, Map<TestDataSet, SchoolTestValue>>> gradeToSubjectsToDataSetToValueMap = testDataTypeToGradeToSubjectsToDataSetToValueMap.get(testDataType);
                    //Check if grade is already in the map.
                    if (gradeToSubjectsToDataSetToValueMap != null && gradeToSubjectsToDataSetToValueMap.get(grade) != null) {
                        //Grade already present.
                        Map<Subject, Map<TestDataSet, SchoolTestValue>> subjectToDataSet = gradeToSubjectsToDataSetToValueMap.get(grade);
                        //Check if subject is already in the map.
                        if (subjectToDataSet != null && subjectToDataSet.get(subject) != null) {
                            //Subject already present.
                            Map<TestDataSet, SchoolTestValue> dataSetToValue = subjectToDataSet.get(subject);
                            //Check if DataSet is not in the map.We dont care if its already there.That should never happen.
                            if (dataSetToValue != null && dataSetToValue.get(testDataSet) == null) {
                                dataSetToValue.put(testDataSet, testValue);
                            }
                        } else {
                            //Subject not present.
                            Map<TestDataSet, SchoolTestValue> dataSetToValue = new HashMap<TestDataSet, SchoolTestValue>();
                            dataSetToValue.put(testDataSet, testValue);
                            subjectToDataSet.put(subject, dataSetToValue);
                        }
                    } else {
                        //Grade not present.
                        Map<TestDataSet, SchoolTestValue> dataSetToValue = new HashMap<TestDataSet, SchoolTestValue>();
                        dataSetToValue.put(testDataSet, testValue);
                        Map<Subject, Map<TestDataSet, SchoolTestValue>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, SchoolTestValue>>();
                        subjectToDataSet.put(subject, dataSetToValue);
                        gradeToSubjectsToDataSetToValueMap.put(grade, subjectToDataSet);

                    }
                } else {
                    //Test not present.
                    Map<TestDataSet, SchoolTestValue> dataSetToValue = new HashMap<TestDataSet, SchoolTestValue>();
                    dataSetToValue.put(testDataSet, testValue);
                    Map<Subject, Map<TestDataSet, SchoolTestValue>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, SchoolTestValue>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    Map<Grade, Map<Subject, Map<TestDataSet, SchoolTestValue>>> gradeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<Subject, Map<TestDataSet, SchoolTestValue>>>();
                    gradeToSubjectsToDataSetToValueMap.put(grade, subjectToDataSet);
                    testDataTypeToGradeToSubjectsToDataSetToValueMap.put(testDataType, gradeToSubjectsToDataSetToValueMap);
                }
            }
        }

        //Convert the temporaryMap - 'testDataTypeToGradeToSubjectsToDataSetToValueMap' to a list of TestToGrades bean to use in the view.
        rval = populateBeansForTestScores(testDataTypeToGradeToSubjectsToDataSetToValueMap);
        return rval;
    }


    /**
     * Helper Method to populate the TestToGrades bean from a Map.
     *
     * @param map
     * @return This returns a list of TestToGrades bean, which is used to present data in the view.
     */
    protected List<TestToGrades> populateBeansForTestScores(Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, SchoolTestValue>>>> map) {
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();
        for (TestDataType testDataType : map.keySet()) {
            TestToGrades testToGrades = new TestToGrades();
            testToGrades.setTestLabel(testDataType.getName());

            //For every test construct a list of grades.
            List<GradeToSubjects> gradeToSubjectsList = new ArrayList<GradeToSubjects>();
            for (Grade grade : map.get(testDataType).keySet()) {
                GradeToSubjects gradeToSubjects = new GradeToSubjects();

                //For every grade construct a list of subjects.
                List<SubjectToYears> subjectToYearsList = new ArrayList<SubjectToYears>();
                for (Subject subject : map.get(testDataType).get(grade).keySet()) {
                    SubjectToYears subjectToYears = new SubjectToYears();
                    subjectToYears.setSubjectLabel(getSubjectLabel(subject));

                    //For every subject construct a list of years.
                    List<YearToTestScore> yearToTestScoreList = new ArrayList<YearToTestScore>();
                    for (TestDataSet testDataSet : map.get(testDataType).get(grade).get(subject).keySet()) {
                        //For a year set the test score.
                        YearToTestScore yearToTestScore = new YearToTestScore();
                        yearToTestScore.setYear(testDataSet.getYear());
                        yearToTestScore.setTestScore(map.get(testDataType).get(grade).get(subject).get(testDataSet).getValueFloat());
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

    /**
     * Method to get the TestDataSets for a given school
     *
     * @param school
     * @return
     */

    protected List<TestDataSet> getTestDataSets(School school, List<Integer> dataTypeIds) {
        List<TestDataSet> testDataSets = _testDataSetDao.findDataSets(school.getDatabaseState(),
                getYears(), dataTypeIds, getSubjects(), null, getBreakDownIds(),
                TEST_DATA_PROFICIENCY_BAND_ID, true, TEST_DATA_LEVEL_CODE);
        return testDataSets;
    }

    /**
     * Method to get the TestDataTypes for a list testDataTypeIds
     *
     * @param testDataTypeIds
     * @return
     */
    protected Map<Integer, TestDataType> getTestDataTypes(List<Integer> testDataTypeIds) {
        //TODO make a batch call?
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();
        for (Integer testDataTypeId : testDataTypeIds) {
            TestDataType testDataType = _testDataTypeDao.getDataType(testDataTypeId);
            testDataTypeIdToTestDataType.put(testDataType.getId(), testDataType);
        }
        return testDataTypeIdToTestDataType;
    }

    protected List<Integer> getYears() {
        List<Integer> years = new ArrayList<Integer>();
        years.add(2011);
        years.add(2010);
        years.add(2009);
        return years;
    }

    protected List<Integer> getDataTypeIds(School school) {
        //TOdO think about what if adding new test.
        //TODO think about if 3 grades have 3 data type ids.
        List<Integer> dataTypeIds = new ArrayList<Integer>();
        if ((school.getLevelCode().containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)
                || school.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL))) {
            if (school.getType().equals(SchoolType.PRIVATE)) {
                dataTypeIds.add(143);
            } else if (school.getType().equals(SchoolType.PUBLIC)) {
                dataTypeIds.add(49);
            }
        }
        if ((school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL))) {
            if (school.getType().equals(SchoolType.PRIVATE)) {
                dataTypeIds.add(155);
            } else if (school.getType().equals(SchoolType.PUBLIC)) {
                dataTypeIds.add(154);
            }
        }
        return dataTypeIds;
    }

    protected List<Subject> getSubjects() {
        List<Subject> subjects = new ArrayList<Subject>();
        subjects.add(Subject.MATH);
        subjects.add(Subject.ENGLISH_LANGUAGE_ARTS);
        subjects.add(Subject.ALGEBRA_I);
        subjects.add(Subject.ENGLISH);
        return subjects;
    }

    protected List<Grade> getGrades() {
        List<Grade> grades = new ArrayList<Grade>();
        grades.add(Grade.G_4);
        return grades;
    }

    protected List<Integer> getBreakDownIds() {
        List<Integer> breakdownIds = new ArrayList<Integer>();
        breakdownIds.add(TEST_DATA_BREAKDOWN_ID);
        return breakdownIds;
    }

    /**
     * Helper method to get the Subject Label to display
     *
     * @param subject
     * @return
     */
    //TODO this method is used in mobile api also.Therefore refactor into gsdata.
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
    //TODO this method is used in mobile api also.Therefore refactor into gsdata.
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
                    //this logic must be changed if we begin to offer more grades
                    gradeLabel = String.valueOf(i) + "th grade";
                } catch (NumberFormatException e) {
                    gradeLabel = "All grades";//GS-11883
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
        //        Integer _dataTypeId;
        List<GradeToSubjects> _grades;

        public String getTestLabel() {
            return _testLabel;
        }

        public void setTestLabel(String testLabel) {
            _testLabel = testLabel;
        }

//        public Integer getDataTypeId() {
//            return _dataTypeId;
//        }
//
//        public void setDataTypeId(Integer dataTypeId) {
//            _dataTypeId = dataTypeId;
//        }

        public List<GradeToSubjects> getGrades() {
            return _grades;
        }

        public void setGrades(List<GradeToSubjects> grades) {
            _grades = grades;
        }
    }

    public static class GradeToSubjects implements Comparable<GradeToSubjects> {
        //TODO remove the grade the levelCode
        //        Grade _grade;
//        LevelCode _levelCode;
        String _gradeLabel;
        List<SubjectToYears> _subjects;

        public String getGradeLabel() {
            return _gradeLabel;
        }

        public void setGradeLabel(String gradeLabel) {
            _gradeLabel = gradeLabel;
        }

//        public Grade getGrade() {
//            return _grade;
//        }
//
//        public void setGrade(Grade grade) {
//            _grade = grade;
//        }
//
//        public LevelCode getLevelCode() {
//            return _levelCode;
//        }
//
//        public void setLevelCode(LevelCode levelCode) {
//            _levelCode = levelCode;
//        }

        public List<SubjectToYears> getSubjects() {
            return _subjects;
        }

        public void setSubjects(List<SubjectToYears> subjects) {
            _subjects = subjects;
        }

        //TODO to compare grade label seems wrong.Should be comparing grades num.
        public int compareTo(GradeToSubjects gradeToSubjects) {
            return getGradeLabel().compareTo(gradeToSubjects.getGradeLabel());
        }
    }

    public static class SubjectToYears implements Comparable<SubjectToYears> {
        //TODO remove subject
//        Subject _subject;
        String _subjectLabel;
        List<YearToTestScore> _years;

//        public Subject getSubject() {
//            return _subject;
//        }
//
//        public void setSubject(Subject subject) {
//            _subject = subject;
//        }

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
        float _testScore;
        Integer _year;

        public float getTestScore() {
            return _testScore;
        }

        public void setTestScore(float testScore) {
            _testScore = testScore;
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