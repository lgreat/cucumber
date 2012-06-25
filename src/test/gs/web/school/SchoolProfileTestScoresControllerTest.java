package gs.web.school;

import gs.data.school.*;
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

    public void testSortOrderOfTests1(){
        replayAllMocks();
        List<SchoolProfileTestScoresController.TestToGrades> testToGradesList = new ArrayList<SchoolProfileTestScoresController.TestToGrades>();

        SchoolProfileTestScoresController.TestToGrades testWithSubgroup = new SchoolProfileTestScoresController.TestToGrades();
        testWithSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithSubgroup.setIsSubgroup(true);
        testWithSubgroup.setTestDataTypeId(1);

        SchoolProfileTestScoresController.TestToGrades testWithNoSubgroup = new SchoolProfileTestScoresController.TestToGrades();
        testWithNoSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithNoSubgroup.setIsSubgroup(false);
        testWithNoSubgroup.setTestDataTypeId(1);

        testToGradesList.add(testWithSubgroup);
        testToGradesList.add(testWithNoSubgroup);

        Collections.sort(testToGradesList);
        assertEquals(testToGradesList.get(0),testWithNoSubgroup);
        assertEquals(testToGradesList.get(1),testWithSubgroup);

        verifyAllMocks();
    }

    public void testSortOrderOfTests2(){
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
        assertEquals(testToGradesList.get(0),noSubgroupGrade1);
        assertEquals(testToGradesList.get(1),withSubgroupGrade1);
        assertEquals(testToGradesList.get(2),noSubgroupGrade3);
        assertEquals(testToGradesList.get(3),noSubgroupGrade9);
        assertEquals(testToGradesList.get(4),noSubgroupGradeAllE);
        assertEquals(testToGradesList.get(5),noSubgroupGradeAllEM);
        assertEquals(testToGradesList.get(6),subgroupGradeAllEM);
    }
}