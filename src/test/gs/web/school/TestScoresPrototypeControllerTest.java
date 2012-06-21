package gs.web.school;

import gs.data.school.*;
import gs.data.test.*;
import gs.web.BaseControllerTestCase;

import java.util.*;

public class TestScoresPrototypeControllerTest extends BaseControllerTestCase {
    private ISchoolDao _schoolDao;
    private ITestDataSetDao _testDataSetDao;
    private ITestDataTypeDao _testDataTypeDao;
    private TestScoresPrototypeController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    private void replayAllMocks() {
        replayMocks(_schoolDao, _testDataSetDao, _testDataTypeDao, _testDataSetDao, _testDataTypeDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_schoolDao, _testDataSetDao, _testDataTypeDao, _testDataSetDao, _testDataTypeDao);
    }

    public void testSortOrderOfTests1(){
        List<TestScoresPrototypeController.TestToGrades> testToGradesList = new ArrayList<TestScoresPrototypeController.TestToGrades>();

        TestScoresPrototypeController.TestToGrades testWithSubgroup = new TestScoresPrototypeController.TestToGrades();
        testWithSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithSubgroup.setIsSubgroup(true);
        testWithSubgroup.setTestDataTypeId(1);

        TestScoresPrototypeController.TestToGrades testWithNoSubgroup = new TestScoresPrototypeController.TestToGrades();
        testWithNoSubgroup.setLowestGradeInTest(Grade.G_1);
        testWithNoSubgroup.setIsSubgroup(false);
        testWithNoSubgroup.setTestDataTypeId(1);

        testToGradesList.add(testWithSubgroup);
        testToGradesList.add(testWithNoSubgroup);

        Collections.sort(testToGradesList);
        assertEquals(testToGradesList.get(0),testWithNoSubgroup);
        assertEquals(testToGradesList.get(1),testWithSubgroup);
    }

    public void testSortOrderOfTests2(){
        List<TestScoresPrototypeController.TestToGrades> testToGradesList = new ArrayList<TestScoresPrototypeController.TestToGrades>();

        TestScoresPrototypeController.TestToGrades noSubgroupGrade9 = new TestScoresPrototypeController.TestToGrades();
        noSubgroupGrade9.setLowestGradeInTest(Grade.G_9);
        noSubgroupGrade9.setIsSubgroup(false);
        noSubgroupGrade9.setTestDataTypeId(5);

        TestScoresPrototypeController.TestToGrades subgroupGradeAllEM = new TestScoresPrototypeController.TestToGrades();
        subgroupGradeAllEM.setLowestGradeInTest(Grade.ALLEM);
        subgroupGradeAllEM.setIsSubgroup(true);
        subgroupGradeAllEM.setTestDataTypeId(4);

        TestScoresPrototypeController.TestToGrades noSubgroupGrade1 = new TestScoresPrototypeController.TestToGrades();
        noSubgroupGrade1.setLowestGradeInTest(Grade.G_1);
        noSubgroupGrade1.setIsSubgroup(false);
        noSubgroupGrade1.setTestDataTypeId(1);

        TestScoresPrototypeController.TestToGrades noSubgroupGradeAllE = new TestScoresPrototypeController.TestToGrades();
        noSubgroupGradeAllE.setLowestGradeInTest(Grade.ALLE);
        noSubgroupGradeAllE.setIsSubgroup(false);
        noSubgroupGradeAllE.setTestDataTypeId(2);

        TestScoresPrototypeController.TestToGrades noSubgroupGrade3 = new TestScoresPrototypeController.TestToGrades();
        noSubgroupGrade3.setLowestGradeInTest(Grade.G_3);
        noSubgroupGrade3.setIsSubgroup(false);
        noSubgroupGrade3.setTestDataTypeId(3);

        TestScoresPrototypeController.TestToGrades noSubgroupGradeAllEM = new TestScoresPrototypeController.TestToGrades();
        noSubgroupGradeAllEM.setLowestGradeInTest(Grade.ALLEM);
        noSubgroupGradeAllEM.setIsSubgroup(false);
        noSubgroupGradeAllEM.setTestDataTypeId(4);

        TestScoresPrototypeController.TestToGrades withSubgroupGrade1 = new TestScoresPrototypeController.TestToGrades();
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