package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.test.*;
import gs.web.BaseControllerTestCase;

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
        List<SchoolProfileTestScoresController.TestToGrades> testToGradesList = new ArrayList<SchoolProfileTestScoresController.TestToGrades>();

        //Test with subgroup data.(Same test and same grade)
        SchoolProfileTestScoresController.TestToGrades testWithSubgroup = new SchoolProfileTestScoresController.TestToGrades();
        testWithSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithSubgroup.setIsSubgroup(true);
        testWithSubgroup.setTestDataTypeId(1);

        //Test with no subgroup data.(Same test and same grade)
        SchoolProfileTestScoresController.TestToGrades testWithNoSubgroup = new SchoolProfileTestScoresController.TestToGrades();
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
        List<SchoolProfileTestScoresController.TestToGrades> testToGradesList = new ArrayList<SchoolProfileTestScoresController.TestToGrades>();

        SchoolProfileTestScoresController.TestToGrades noSubgroupGrade9 = new SchoolProfileTestScoresController.TestToGrades();
        noSubgroupGrade9.setLowestGradeInTest(Grade.G_9);
        noSubgroupGrade9.setIsSubgroup(false);
        noSubgroupGrade9.setTestDataTypeId(5);

        SchoolProfileTestScoresController.TestToGrades subgroupGradeAllEM = new SchoolProfileTestScoresController.TestToGrades();
        subgroupGradeAllEM.setLowestGradeInTest(Grade.ALLEM);
        subgroupGradeAllEM.setIsSubgroup(true);
        subgroupGradeAllEM.setTestDataTypeId(4);

        SchoolProfileTestScoresController.TestToGrades noSubgroupGrade1 = new SchoolProfileTestScoresController.TestToGrades();
        noSubgroupGrade1.setLowestGradeInTest(Grade.G_1);
        noSubgroupGrade1.setIsSubgroup(false);
        noSubgroupGrade1.setTestDataTypeId(1);

        SchoolProfileTestScoresController.TestToGrades noSubgroupGradeAllE = new SchoolProfileTestScoresController.TestToGrades();
        noSubgroupGradeAllE.setLowestGradeInTest(Grade.ALLE);
        noSubgroupGradeAllE.setIsSubgroup(false);
        noSubgroupGradeAllE.setTestDataTypeId(2);

        SchoolProfileTestScoresController.TestToGrades noSubgroupGrade3 = new SchoolProfileTestScoresController.TestToGrades();
        noSubgroupGrade3.setLowestGradeInTest(Grade.G_3);
        noSubgroupGrade3.setIsSubgroup(false);
        noSubgroupGrade3.setTestDataTypeId(3);

        SchoolProfileTestScoresController.TestToGrades noSubgroupGradeAllEM = new SchoolProfileTestScoresController.TestToGrades();
        noSubgroupGradeAllEM.setLowestGradeInTest(Grade.ALLEM);
        noSubgroupGradeAllEM.setIsSubgroup(false);
        noSubgroupGradeAllEM.setTestDataTypeId(4);

        SchoolProfileTestScoresController.TestToGrades withSubgroupGrade1 = new SchoolProfileTestScoresController.TestToGrades();
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
        Map<SchoolProfileTestScoresController.CustomTestDataType, Map<Grade, Map<LevelCode, Map<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet, String>>>>>
                dataMap = new HashMap<SchoolProfileTestScoresController.CustomTestDataType, Map<Grade, Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,String>>>>>();
        Map<SchoolProfileTestScoresController.CustomTestDataSet,String> map2 = new HashMap<SchoolProfileTestScoresController.CustomTestDataSet, String>();
        SchoolProfileTestScoresController.CustomTestDataSet notAvailableDataSet = new SchoolProfileTestScoresController.CustomTestDataSet();
        notAvailableDataSet.setYear(2012);
        notAvailableDataSet.setId(1);
        map2.put(notAvailableDataSet, SchoolProfileTestScoresController.LABEL_DATA_NOT_AVAILABLE);
        Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,String>> map3 = new HashMap<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet, String>>();
        map3.put(Subject.ENGLISH, map2);
        Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,String>>> map4 = new HashMap<LevelCode, Map<Subject, Map<SchoolProfileTestScoresController.CustomTestDataSet,String>>>();
        map4.put(LevelCode.ELEMENTARY_MIDDLE_HIGH, map3);
        Map<Grade, Map<LevelCode,Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,String>>>> map5 = new HashMap<Grade, Map<LevelCode, Map<Subject,Map<SchoolProfileTestScoresController.CustomTestDataSet,String>>>>();
        map5.put(Grade.G_3, map4);
        SchoolProfileTestScoresController.CustomTestDataType dataType = new SchoolProfileTestScoresController.CustomTestDataType();
        dataType.setId(10);
        dataType.setLabel("Aroy Test");
        dataMap.put(dataType, map5);
        Map<Integer, TestDescription> testDescriptionMap = new HashMap<Integer, TestDescription>();
        List<SchoolProfileTestScoresController.TestToGrades> rval = _controller.populateTestScoresBean(school, dataMap, testDescriptionMap);
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
        map2.put(availableDataSet, "15");

        rval = _controller.populateTestScoresBean(school, dataMap, testDescriptionMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertFalse("Expect school with real values to have test scores retained", rval.isEmpty());
    }
}