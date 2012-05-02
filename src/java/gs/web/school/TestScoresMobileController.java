package gs.web.school;

import gs.data.school.*;
import gs.data.school.Grade;
import gs.data.test.Subject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.Arrays;

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
    private ITestDescriptionDao _testDescriptionDao;

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

        Set<Integer> dataSetIds = getTestDataSetIds(school);

        //List of testDataSets.Used to query the schoolValue.
        List<TestDataSet> testDataSets = new ArrayList<TestDataSet>();
        if (dataSetIds != null && !dataSetIds.isEmpty()) {
            testDataSets = getTestDataSets(school, dataSetIds);
        }

        if (testDataSets != null && !testDataSets.isEmpty()) {
            //Data TypeIds
            Set<Integer> dataTypeIds = new HashSet<Integer>();
            for (TestDataSet testDataSet : testDataSets) {
                dataTypeIds.add(testDataSet.getDataTypeId());
            }

            //Map of testDataTypeId to testDataType object.
            Map<Integer, TestDataType> testDataTypeIdToTestDataType = getTestDataTypes(dataTypeIds);

            //A map used to store the testDataType, grade, subjects, testDataSet and test score value for the school.
            //If a school does not have a test score value, then it will not be in this map.
            Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap
                    = getTestDataTypeToGradeToSubjectsToDataSetToValueMap(school, testDataSets, testDataTypeIdToTestDataType);

            //A map used to store the testDataType, grade, subjects, testDataSet that should be
            //displayed for a given school irrespective of if the school has test score value or not.
            Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> mapOfDataPointsToShow
                    = getMapOfDataPointsToShow(testDataSets, testDataTypeIdToTestDataType);

            //Based on the data points that should be displayed for a school, fill in
            //the 'DataNotAvailable' if school does not have test score values.
            fillInMissingDataPoints(mapOfDataPointsToShow, testDataTypeToGradeToSubjectsToDataSetToValueMap);

            //Convert the temporaryMap that was constructed above, to a list of TestToGrades bean.This bean is used in the view.
            rval = populateTestScoresBean(school, testDataTypeToGradeToSubjectsToDataSetToValueMap);
        }
        return rval;
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

    protected Set<Integer> getTestDataSetIds(School school) {
        Set<Integer> dataSetIds = new HashSet<Integer>();
        if (school.getDatabaseState().equals(State.CA)) {
            Integer[] arr = {77058, 77238, 76518, 71298, 71478, 71658, 71838, 72018, 72198, 72378, 72558, 72918, 73098, 73278, 73458, 73818, 73998, 74898, 75258, 86102, 86282, 85562, 80342, 80522, 80702, 80882, 81062, 81242, 81422, 81602, 81962, 82142, 82322, 82502, 82862, 83042, 83942, 84302, 121909, 122089, 121369, 116149, 116329, 116509, 116689, 116869, 117049, 117229, 117409, 117769, 117949, 118129, 118309, 118669, 118849, 119749, 120109};
            dataSetIds = new HashSet<Integer>(Arrays.asList(arr));
        } else if (school.getDatabaseState().equals(State.IN)) {
            Integer[] arr = {2076, 2116, 2081, 2121, 2086, 2126, 2091, 2131, 2096, 2136, 2101, 2141, 3358, 3363, 3508, 3513, 3658, 3663, 3808, 3813, 3958, 3963, 4108, 4113, 2158, 2163, 2308, 2313, 2458, 2463, 2608, 2613, 2758, 2763, 2908, 2913, 5909, 5914, 6059, 6064, 6209, 6214, 6359, 6364, 6509, 6514, 6659, 6664, 7266, 7276, 7281, 7291, 4559, 4564, 4709, 4714, 4859, 4864, 5009, 5014, 5159, 5164, 5309, 5314};
            dataSetIds = new HashSet<Integer>(Arrays.asList(arr));
        }
        return dataSetIds;
    }

    protected List<TestDataSet> getTestDataSets(School school, Set<Integer> ids) {

        List<TestDataSet> testDataSets = _testDataSetDao.findDataSets(school.getDatabaseState(), ids);
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
            //For masking.
            String testScoreValue = StringUtils.isNotBlank(value.getValueText()) ? StringEscapeUtils.escapeHtml(value.getValueText()) : value.getValueFloat().toString();

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
    protected List<TestToGrades> populateTestScoresBean(School school, Map<TestDataType, Map<Grade, Map<Subject, Map<TestDataSet, String>>>> map) {
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();
        for (TestDataType testDataType : map.keySet()) {
            TestToGrades testToGrades = new TestToGrades();
            testToGrades.setTestLabel(testDataType.getName());
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
     * Helper method to get the Subject Label to display
     *
     * @param subject
     * @return
     */

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
            subjectLabel = Subject.getName(subject);
//            throw new IllegalStateException("Invalid subject: " + subject.getSubjectId() + " " + Subject.getName(subject));
        }

        return subjectLabel;
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
                    gradeLabel += levelsList.size() == 1 ? " school" : " schools";
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
        String _description;
        String _source;
        String _scale;

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

    public ITestDescriptionDao getTestDescriptionDao() {
        return _testDescriptionDao;
    }

    public void setTestDescriptionDao(ITestDescriptionDao testDescriptionDao) {
        _testDescriptionDao = testDescriptionDao;
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