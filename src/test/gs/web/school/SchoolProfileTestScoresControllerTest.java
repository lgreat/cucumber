package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.test.*;
import gs.data.util.Pair;
import gs.web.BaseControllerTestCase;
import gs.web.school.test.TestToGrades;

import java.util.*;

import static org.easymock.EasyMock.*;

public class SchoolProfileTestScoresControllerTest extends BaseControllerTestCase {
    private SchoolProfileTestScoresController _controller;

    private ITestDataSetDao _testDataSetDao;
    private ITestDataTypeDao _testDataTypeDao;
    private ISubjectDao _subjectDao;
    private ITestBreakdownDao _testBreakdownDao;
    private ITestDescriptionDao _testDescriptionDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolProfileTestScoresController();

        _testDataSetDao = createMock(ITestDataSetDao.class);
        _testDataTypeDao = createMock(ITestDataTypeDao.class);
        _subjectDao = createMock(ISubjectDao.class);
        _testBreakdownDao = createMock(ITestBreakdownDao.class);
        _testDescriptionDao = createMock(ITestDescriptionDao.class);

        _controller.setTestDataSetDao(_testDataSetDao);
        _controller.setTestDataTypeDao(_testDataTypeDao);
        _controller.setSubjectDao(_subjectDao);
        _controller.setTestBreakdownDao(_testBreakdownDao);
        _controller.setTestDescriptionDao(_testDescriptionDao);
    }

    private void replayAllMocks() {
        replayMocks(_testDataSetDao, _testDataTypeDao, _subjectDao, _testBreakdownDao, _testDescriptionDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_testDataSetDao, _testDataTypeDao, _subjectDao, _testBreakdownDao, _testDescriptionDao);
    }

    private void resetAllMocks() {
        resetMocks(_testDataSetDao, _testDataTypeDao, _subjectDao, _testBreakdownDao, _testDescriptionDao);
    }

    public void testSortOrderOfTestsWithSameTestSameGrades(){
        replayAllMocks();
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();

        //Test with subgroup data.(Same test and same grade)
        TestToGrades testWithSubgroup = new TestToGrades();
        testWithSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithSubgroup.setIsSubgroup(true);
        testWithSubgroup.setTestDataTypeId(1);

        //Test with no subgroup data.(Same test and same grade)
        TestToGrades testWithNoSubgroup = new TestToGrades();
        testWithNoSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithNoSubgroup.setIsSubgroup(false);
        testWithNoSubgroup.setTestDataTypeId(1);

        testToGradesList.add(testWithSubgroup);
        testToGradesList.add(testWithNoSubgroup);

        Collections.sort(testToGradesList);
        //Test with non-subgroup data should be first followed by the test with subgroup data.
        assertEquals(testToGradesList.get(0),testWithNoSubgroup);
        assertEquals(testToGradesList.get(1),testWithSubgroup);

        verifyAllMocks();
    }

    public void testSortOrderOfTestsWithMultipleTests(){
        List<TestToGrades> testToGradesList = new ArrayList<TestToGrades>();

        TestToGrades noSubgroupGrade9 = new TestToGrades();
        noSubgroupGrade9.setLowestGradeInTest(Grade.G_9);
        noSubgroupGrade9.setIsSubgroup(false);
        noSubgroupGrade9.setTestDataTypeId(5);

        TestToGrades subgroupGradeAllEM = new TestToGrades();
        subgroupGradeAllEM.setLowestGradeInTest(Grade.ALLEM);
        subgroupGradeAllEM.setIsSubgroup(true);
        subgroupGradeAllEM.setTestDataTypeId(4);

        TestToGrades noSubgroupGrade1 = new TestToGrades();
        noSubgroupGrade1.setLowestGradeInTest(Grade.G_1);
        noSubgroupGrade1.setIsSubgroup(false);
        noSubgroupGrade1.setTestDataTypeId(1);

        TestToGrades noSubgroupGradeAllE = new TestToGrades();
        noSubgroupGradeAllE.setLowestGradeInTest(Grade.ALLE);
        noSubgroupGradeAllE.setIsSubgroup(false);
        noSubgroupGradeAllE.setTestDataTypeId(2);

        TestToGrades noSubgroupGrade3 = new TestToGrades();
        noSubgroupGrade3.setLowestGradeInTest(Grade.G_3);
        noSubgroupGrade3.setIsSubgroup(false);
        noSubgroupGrade3.setTestDataTypeId(3);

        TestToGrades noSubgroupGradeAllEM = new TestToGrades();
        noSubgroupGradeAllEM.setLowestGradeInTest(Grade.ALLEM);
        noSubgroupGradeAllEM.setIsSubgroup(false);
        noSubgroupGradeAllEM.setTestDataTypeId(4);

        TestToGrades withSubgroupGrade1 = new TestToGrades();
        withSubgroupGrade1.setLowestGradeInTest(Grade.G_1);
        withSubgroupGrade1.setIsSubgroup(true);
        withSubgroupGrade1.setTestDataTypeId(1);

        testToGradesList.add(noSubgroupGrade9);
        testToGradesList.add(subgroupGradeAllEM);
        testToGradesList.add(noSubgroupGrade1);
        testToGradesList.add(noSubgroupGradeAllE);
        testToGradesList.add(noSubgroupGrade3);
        testToGradesList.add(noSubgroupGradeAllEM);
        testToGradesList.add(withSubgroupGrade1);

        Collections.sort(testToGradesList);
        //The tests should be sorted in the order of - the lowest grade in the test followed by test data type id.
        //However if the test has subgroup data then the test should be followed by subgroup test.
        assertEquals(testToGradesList.get(0),noSubgroupGrade1);
        assertEquals(testToGradesList.get(1),withSubgroupGrade1);
        assertEquals(testToGradesList.get(2),noSubgroupGrade3);
        assertEquals(testToGradesList.get(3),noSubgroupGrade9);
        assertEquals(testToGradesList.get(4),noSubgroupGradeAllE);
        assertEquals(testToGradesList.get(5),noSubgroupGradeAllEM);
        assertEquals(testToGradesList.get(6),subgroupGradeAllEM);
    }

    // Regression test for GS-13489
    public void testPopulateTestScoresBeanPrunesSchoolsWithNoValues() {
        expect(_subjectDao.findSubjectName(Subject.ENGLISH, State.IN)).andReturn("English");
        replayAllMocks();
        School school = new School();
        school.setDatabaseState(State.IN);
        Map<SchoolProfileTestScoresController.CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet, Pair<String,Integer>>>>>>
                dataMap = new HashMap<SchoolProfileTestScoresController.CustomTestDataType, Map<Grade, Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>>>();
        Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>> map2 = new HashMap<SchoolProfileTestScoresController.CustomTestDataSet, Pair<String,Integer>>();
        SchoolProfileTestScoresController.CustomTestDataSet notAvailableDataSet = new SchoolProfileTestScoresController.CustomTestDataSet();
        notAvailableDataSet.setYear(2012);
        notAvailableDataSet.setId(1);
        map2.put(notAvailableDataSet, new Pair<String,Integer>(SchoolProfileTestScoresController.LABEL_DATA_NOT_AVAILABLE,null));
        Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>> map3 = new HashMap<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet, Pair<String,Integer>>>();
        map3.put(Subject.ENGLISH, map2);
        Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>> map4 = new HashMap<LevelCode, Map<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>();
        map4.put(LevelCode.ELEMENTARY_MIDDLE_HIGH, map3);
        Map<Grade, Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>> map5 = new HashMap<Grade, Map<LevelCode, Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>>();
        map5.put(Grade.G_3, map4);
        SchoolProfileTestScoresController.CustomTestDataType dataType = new SchoolProfileTestScoresController.CustomTestDataType();
        dataType.setId(10);
        dataType.setLabel("Aroy Test");
        dataMap.put(dataType, map5);
        Map<Integer, TestDescription> testDescriptionMap = new HashMap<Integer, TestDescription>();
        List<TestToGrades> rval = _controller.populateTestScoresBean(school, dataMap, testDescriptionMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertTrue("Expect school with only \"Not available\" values to have test scores pruned", rval.isEmpty());

        resetAllMocks();
        expect(_subjectDao.findSubjectName(Subject.ENGLISH, State.IN)).andReturn("English");
        replayAllMocks();
        // If school has a valid value, expect test scores to be retained, including any not availables
        SchoolProfileTestScoresController.CustomTestDataSet availableDataSet = new SchoolProfileTestScoresController.CustomTestDataSet();
        availableDataSet.setYear(2011);
        availableDataSet.setId(2);
        map2.put(availableDataSet, new Pair<String,Integer>("15", null));

        rval = _controller.populateTestScoresBean(school, dataMap, testDescriptionMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertFalse("Expect school with real values to have test scores retained", rval.isEmpty());
    }

    /**
     * Tests that null pointer exception is
     * not thrown if a Test data type is not found in the DB for the test data type id.
     * This may occur if the display target is set and the TestDataSet table is moved up,
     * however the TestDataType table is not moved up.
     */

    public void testGetTestDataTypeWithNoTestDataTypeRowInDB() {
        Integer testDataTypeId = 1;

        //An empty map.
        Map<Integer, TestDataType> testDataTypeIdToTestDataType = new HashMap<Integer, TestDataType>();

        //Test data type row not found for test data type id 1.
        TestDataType testDataType = null;

        resetAllMocks();
        //Test data type row not found for test data type id 1.
        expect(_testDataTypeDao.getDataType(testDataTypeId)).andReturn(testDataType);
        replayAllMocks();
        TestDataType rval = _controller.getTestDataType(testDataTypeIdToTestDataType,testDataTypeId);
        verifyAllMocks();
        assertNull("The test data type was not found.Hence return null",rval);
        assertNull("The test data type was not found.Hence put nothing in the map.",testDataTypeIdToTestDataType.get(testDataTypeId));
    }

    public void testPopulateTestScoresBeanSetsNumberTested() {
        expect(_subjectDao.findSubjectName(Subject.ENGLISH, State.IN)).andReturn("English");
        replayAllMocks();
        School school = new School();
        school.setDatabaseState(State.IN);
        Map<SchoolProfileTestScoresController.CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet, Pair<String,Integer>>>>>>
                dataMap = new HashMap<SchoolProfileTestScoresController.CustomTestDataType, Map<Grade, Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>>>();
        Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>> map2 = new HashMap<SchoolProfileTestScoresController.CustomTestDataSet, Pair<String,Integer>>();
        SchoolProfileTestScoresController.CustomTestDataSet notAvailableDataSet = new SchoolProfileTestScoresController.CustomTestDataSet();
        notAvailableDataSet.setYear(2012);
        notAvailableDataSet.setId(1);
        map2.put(notAvailableDataSet, new Pair<String,Integer>(SchoolProfileTestScoresController.LABEL_DATA_NOT_AVAILABLE,null));
        Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>> map3 = new HashMap<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet, Pair<String,Integer>>>();
        map3.put(Subject.ENGLISH, map2);
        Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>> map4 = new HashMap<LevelCode, Map<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>();
        map4.put(LevelCode.ELEMENTARY_MIDDLE_HIGH, map3);
        Map<Grade, Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>> map5 = new HashMap<Grade, Map<LevelCode, Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,Pair<String,Integer>>>>>();
        map5.put(Grade.G_3, map4);
        SchoolProfileTestScoresController.CustomTestDataType dataType = new SchoolProfileTestScoresController.CustomTestDataType();
        dataType.setId(10);
        dataType.setLabel("SSprouse Test");
        dataMap.put(dataType, map5);
        Map<Integer, TestDescription> testDescriptionMap = new HashMap<Integer, TestDescription>();
        List<TestToGrades> rval = _controller.populateTestScoresBean(school, dataMap, testDescriptionMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertTrue("Expect school with only \"Not available\" values to have test scores pruned", rval.isEmpty());

        resetAllMocks();
        expect(_subjectDao.findSubjectName(Subject.ENGLISH, State.IN)).andReturn("English");
        replayAllMocks();
        // If school has a valid value, expect test scores to be retained, including any not availables
        SchoolProfileTestScoresController.CustomTestDataSet availableDataSet = new SchoolProfileTestScoresController.CustomTestDataSet();
        availableDataSet.setYear(2011);
        availableDataSet.setId(2);
        map2.put(availableDataSet, new Pair<String,Integer>("15", 999));

        rval = _controller.populateTestScoresBean(school, dataMap, testDescriptionMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertFalse("Expect school with real values to have test scores retained", rval.isEmpty());
        assertNull("Expect number tested value to be null",
                rval.get(0).getGrades().get(0).getSubjects().get(0).getTestValues().get(0).getNumberTested());
        assertEquals("Expect number tested value to be set correctly", new Integer(999),
                rval.get(0).getGrades().get(0).getSubjects().get(0).getTestValues().get(1).getNumberTested());
    }
}