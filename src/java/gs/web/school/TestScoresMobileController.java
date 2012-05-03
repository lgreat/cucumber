package gs.web.school;

import gs.data.school.*;
import gs.data.school.Grade;
import gs.data.test.Subject;
import gs.web.util.PageHelper;
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

    public static final String VIEW = "school/testScores-mobile";

    public static final String LABEL_DATA_NOT_AVAILABLE = "Data not available";

    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;

    private RatingHelper _ratingHelper;
    private ISchoolDao _schoolDao;
    private ITestDataTypeDao _testDataTypeDao;
    private ITestDataSetDao _testDataSetDao;
    private ITestDataSchoolValueDao _testDataSchoolValueDao;
    private ITestDescriptionDao _testDescriptionDao;
    private ISubjectDao _subjectDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String schoolId = request.getParameter(PARAM_SCHOOL_ID);
        String stateStr = request.getParameter(PARAM_STATE);

        Map<String, Object> model = new HashMap<String, Object>();
        try {
            State state = State.fromString(stateStr);
            School school = _schoolDao.getSchoolById(state, new Integer(schoolId));
            model.put("school", school);
            model.put("schoolTestScores", getTestScores(school));
            //TODO do I need all this for the rating?
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());
            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);
            model.put("gs_rating", gsRating);
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

        //Get the list of test data set Ids to pull out for a school.
        Set<Integer> testDataSetIds = getTestDataSetIds(school);
        if (testDataSetIds == null || testDataSetIds.isEmpty()) {
            return rval;
        }

        //Get the list of testDataSet objects from the list of test data set Ids.This is used to query the school values.
        List<TestDataSet> testDataSets = getTestDataSets(school, testDataSetIds);
        if (testDataSets == null || testDataSets.isEmpty()) {
            return rval;
        }

        //Get a list of data type Ids.Data type Ids represent a test.
        Set<Integer> dataTypeIds = new HashSet<Integer>();
        for (TestDataSet testDataSet : testDataSets) {
            dataTypeIds.add(testDataSet.getDataTypeId());
        }

        //Get a Map of test data type Id to test data type object.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = getTestDataTypes(dataTypeIds);

        //A map used to store the test data type, grade, level code, subjects, test data set and test score value for the school.
        //If a school does not have a test score value, then it will not be in this map.
        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap
                = getTestDataTypeToGradeToSubjectsToDataSetToValueMap(school, testDataSets, testDataTypeIdToTestDataType);

        //A map used to store the test data type, grade, level code, subjects, test data set that should be
        //displayed for a given school irrespective of if the school has test score value or not.In other words,
        //this map represents what information we expect to show for a school.
        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> mapOfDataPointsToShow
                = getMapOfDataPointsToShow(testDataSets, testDataTypeIdToTestDataType);

        //Based on the data points that should be displayed for a school, fill in
        //the 'DataNotAvailable' if school does not have test score values.
        fillInMissingDataPoints(mapOfDataPointsToShow, testDataTypeToGradeToSubjectsToDataSetToValueMap);

        //Convert the temporaryMap that was constructed above, to a list of TestToGrades bean.This bean is used in the view.
        rval = populateTestScoresBean(school, testDataTypeToGradeToSubjectsToDataSetToValueMap);

        //use this for debugging
        //printAllData(testDataTypeToGradeToSubjectsToDataSetToValueMap);

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
            Integer[] arr = {77058, 76518, 79038, 78318, 71298, 71478, 71658, 71838, 72018, 72198, 72378, 72558, 72918, 73098, 73278, 73458, 73818, 73998, 74898, 75258, 86102, 85562, 88082, 87362, 80342, 80522, 80702, 80882, 81062, 81242, 81422, 81602, 81962, 82142, 82322, 82502, 82862, 83042, 83942, 84302, 121909, 121369, 123889, 123169, 116149, 116329, 116509, 116689, 116869, 117049, 117229, 117409, 117769, 117949, 118129, 118309, 118669, 118849, 119749, 120109};
            dataSetIds = new HashSet<Integer>(Arrays.asList(arr));
        } else if (school.getDatabaseState().equals(State.IN)) {
            Integer[] arr = {2076, 2116, 2081, 2121, 2086, 2126, 2091, 2131, 2096, 2136, 2101, 2141, 3358, 3363, 3508, 3513, 3658, 3663, 3808, 3813, 3958, 3963, 4108, 4113, 2158, 2163, 2308, 2313, 2458, 2463, 2608, 2613, 2758, 2763, 2908, 2913, 5909, 5914, 6059, 6064, 6209, 6214, 6359, 6364, 6509, 6514, 6659, 6664, 7266, 7276, 7281, 7291, 4559, 4564, 4709, 4714, 4859, 4864, 5009, 5014, 5159, 5164, 5309, 5314};
            dataSetIds = new HashSet<Integer>(Arrays.asList(arr));
        } else if (school.getDatabaseState().equals(State.DC)) {
            Integer[] arr = {2868, 3408, 2508, 3048, 2568, 3108, 2628, 3168, 2688, 3228, 2748, 3288, 2808, 3348, 5476, 6061, 5086, 5671, 5151, 5736, 5216, 5801, 5281, 5866, 5346, 5931, 5411, 5996, 6971, 7556, 6581, 7166, 6646, 7231, 6711, 7296, 6776, 7361, 6841, 7426, 6906, 7491};
            dataSetIds = new HashSet<Integer>(Arrays.asList(arr));
        } else if (school.getDatabaseState().equals(State.WI)) {
            Integer[] arr = {7672, 7682, 6142, 6147, 6312, 6322, 6737, 6742, 6907, 6912, 7077, 7082, 7247, 7257, 5717, 5727, 4187, 4192, 4357, 4367, 4782, 4787, 4952, 4957, 5122, 5127, 5292, 5302, 8189, 8199, 8099, 8104, 8109, 8119, 8134, 8139, 8144, 8149, 8154, 8159, 8164, 8174};
            dataSetIds = new HashSet<Integer>(Arrays.asList(arr));
        } else if (school.getDatabaseState().equals(State.KY)) {
            Integer[] arr = {143, 153, 193, 203, 168, 178, 673, 683, 723, 733, 698, 708, 748, 758, 798, 808, 773, 783};
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
    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>
    getTestDataTypeToGradeToSubjectsToDataSetToValueMap(School school, List<TestDataSet> testDataSets,
                                                        Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

        //Query the for school test score values.
        List<SchoolTestValue> values = _testDataSchoolValueDao.findValues(testDataSets, school.getDatabaseState(), school);

        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> testDataTypeToGradeToLevelCodeToSubjectsToDataSetToValueMap =
                new HashMap<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>();

        //For every test score returned, put it in the Map.
        for (SchoolTestValue value : values) {
            Grade grade = value.getDataSet().getGrade();
            LevelCode levelCode = value.getDataSet().getLevelCode();
            Subject subject = value.getDataSet().getSubject();
            //TODO maybe the dao should not join.
            TestDataSet testDataSet = value.getDataSet();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(value.getDataSet().getDataTypeId());
            //For masking.
            String testScoreValue = StringUtils.isNotBlank(value.getValueText()) ? StringEscapeUtils.escapeHtml(value.getValueText()) : value.getValueInteger().toString();

            if (testDataTypeToGradeToLevelCodeToSubjectsToDataSetToValueMap.get(testDataType) != null) {
                //Test already present.
                Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = testDataTypeToGradeToLevelCodeToSubjectsToDataSetToValueMap.get(testDataType);
                //Check if grade is already in the map.
                if (gradeToLevelCodeToSubjectsToDataSetToValueMap != null && gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    //Grade already present.
                    Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade);
                    //Check if levelCode is already in the map.
                    if (levelCodeToSubjectToDataSet != null && levelCodeToSubjectToDataSet.get(levelCode) != null) {
                        //Levelcode already present
                        Map<Subject, Map<TestDataSet, String>> subjectToDataSet = levelCodeToSubjectToDataSet.get(levelCode);
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
                        //Levelcode not present.
                        Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                        dataSetToValue.put(testDataSet, testScoreValue);
                        Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                        subjectToDataSet.put(subject, dataSetToValue);
                        levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                    }
                } else {
                    //Grade not present.
                    Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                    dataSetToValue.put(testDataSet, testScoreValue);
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<TestDataSet, String>>>();
                    levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                    gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);

                }
            } else {
                //Test not present.
                Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                dataSetToValue.put(testDataSet, testScoreValue);
                Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<TestDataSet, String>>>();
                levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>();
                gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
                testDataTypeToGradeToLevelCodeToSubjectsToDataSetToValueMap.put(testDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
            }
        }

        return testDataTypeToGradeToLevelCodeToSubjectsToDataSetToValueMap;
    }

    /**
     * A map used to store the testDataType, grade, subjects, testDataSet that should be
     * displayed for a given school irrespective of, if the school has test score value or not.
     *
     * @param testDataSets
     * @param testDataTypeIdToTestDataType
     * @return a map to hold the TestDataType to Grade to Subject to TestDataSet to value.
     */
    protected Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>
    getMapOfDataPointsToShow(List<TestDataSet> testDataSets, Map<Integer, TestDataType> testDataTypeIdToTestDataType) {

        Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> mapOfDataPointsToShow =
                new HashMap<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>>();

        for (TestDataSet testDataSet : testDataSets) {
            Grade grade = testDataSet.getGrade();
            LevelCode levelCode = testDataSet.getLevelCode();
            Subject subject = testDataSet.getSubject();
            TestDataType testDataType = testDataTypeIdToTestDataType.get(testDataSet.getDataTypeId());
            Integer year = testDataSet.getYear();

            //Check if the test is already in the map.
            if (mapOfDataPointsToShow.get(testDataType) != null) {
                //Test already present.
                Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = mapOfDataPointsToShow.get(testDataType);
                //Check if grade is already in the map.
                if (gradeToLevelCodeToSubjectsToDataSetToValueMap != null && gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade) != null) {
                    //Grade already present.
                    Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectsToDataSetToValueMap = gradeToLevelCodeToSubjectsToDataSetToValueMap.get(grade);
                    //Check if level code is already in the map.
                    if (levelCodeToSubjectsToDataSetToValueMap != null && levelCodeToSubjectsToDataSetToValueMap.get(levelCode) != null) {
                        //Levelcode already present.
                        Map<Subject, Map<TestDataSet, String>> subjectToDataSet = levelCodeToSubjectsToDataSetToValueMap.get(levelCode);
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
                        //Levelcode not present
                        Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                        dataSetToValue.put(testDataSet, "");
                        Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                        subjectToDataSet.put(subject, dataSetToValue);
                        levelCodeToSubjectsToDataSetToValueMap.put(levelCode, subjectToDataSet);
                    }
                } else {
                    //Grade not present.
                    Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                    dataSetToValue.put(testDataSet, "");
                    Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                    subjectToDataSet.put(subject, dataSetToValue);
                    Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<TestDataSet, String>>>();
                    levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                    gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);

                }
            } else {
                //Test not present.
                Map<TestDataSet, String> dataSetToValue = new HashMap<TestDataSet, String>();
                dataSetToValue.put(testDataSet, "");
                Map<Subject, Map<TestDataSet, String>> subjectToDataSet = new HashMap<Subject, Map<TestDataSet, String>>();
                subjectToDataSet.put(subject, dataSetToValue);
                Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodeToSubjectToDataSet = new HashMap<LevelCode, Map<Subject, Map<TestDataSet, String>>>();
                levelCodeToSubjectToDataSet.put(levelCode, subjectToDataSet);
                Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> gradeToLevelCodeToSubjectsToDataSetToValueMap = new HashMap<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>();
                gradeToLevelCodeToSubjectsToDataSetToValueMap.put(grade, levelCodeToSubjectToDataSet);
                mapOfDataPointsToShow.put(testDataType, gradeToLevelCodeToSubjectsToDataSetToValueMap);
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
    protected void fillInMissingDataPoints(Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> mapOfDataPointsToShow,
                                           Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> testDataTypeToGradeToSubjectsToDataSetToValueMap) {
        for (TestDataType dataType : testDataTypeToGradeToSubjectsToDataSetToValueMap.keySet()) {
            Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>> grades = testDataTypeToGradeToSubjectsToDataSetToValueMap.get(dataType);
            for (Grade grade : grades.keySet()) {
                Map<LevelCode, Map<Subject, Map<TestDataSet, String>>> levelCodes = grades.get(grade);
                for (LevelCode levelCode : levelCodes.keySet()) {
                    Map<Subject, Map<TestDataSet, String>> subjects = levelCodes.get(levelCode);
                    for (Subject subject : subjects.keySet()) {
                        Map<TestDataSet, String> existingDataSets = subjects.get(subject);
                        Map<TestDataSet, String> dataSetsToShow = mapOfDataPointsToShow.get(dataType).get(grade).get(levelCode).get(subject);

                        for (TestDataSet dataSet : dataSetsToShow.keySet()) {
                            if (!existingDataSets.containsKey(dataSet)) {
                                existingDataSets.put(dataSet, LABEL_DATA_NOT_AVAILABLE);
                            }
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
    protected List<TestToGrades> populateTestScoresBean(School school, Map<TestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<TestDataSet, String>>>>> map) {
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

                    //For every grade construct a list of subjects.
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

                            //For a year set the test score.
                            YearToTestScore yearToTestScore = new YearToTestScore();
                            yearToTestScore.setYear(testDataSet.getYear());
                            yearToTestScore.setTestScoreStr(map.get(testDataType).get(grade).get(levelCode).get(subject).get(testDataSet));
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

            }
            //Sort in order of grades.
            Collections.sort(gradeToSubjectsList);
            testToGrades.setGrades(gradeToSubjectsList);
            testToGradesList.add(testToGrades);
        }
        Collections.sort(testToGradesList);
        return testToGradesList;
    }


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

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
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

    public ISubjectDao getSubjectDao() {
        return _subjectDao;
    }

    public void setSubjectDao(ISubjectDao subjectDao) {
        _subjectDao = subjectDao;
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