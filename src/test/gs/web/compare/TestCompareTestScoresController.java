package gs.web.compare;

import gs.data.compare.CompareConfig;
import gs.data.compare.CompareLabel;
import gs.data.compare.ICompareConfigDao;
import gs.data.compare.ICompareLabelDao;
import gs.data.school.*;
import gs.data.school.census.CensusDataSetType;
import gs.data.state.State;
import gs.data.test.*;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

import static gs.web.compare.CompareTestScoresController.TAB_NAME;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareTestScoresController extends BaseControllerTestCase {
    private CompareTestScoresController _controller;
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;
    private ITestDataTypeDao _testDataTypeDao;
    private ITestDataSetDao _testDataSetDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareTestScoresController();

        _compareLabelDao = createStrictMock(ICompareLabelDao.class);
        _compareConfigDao = createStrictMock(ICompareConfigDao.class);
        _testDataTypeDao = createStrictMock(ITestDataTypeDao.class);
        _testDataSetDao = createStrictMock(ITestDataSetDao.class);

        _controller.setSuccessView("success");
        _controller.setCompareLabelDao(_compareLabelDao);
        _controller.setCompareConfigDao(_compareConfigDao);
        _controller.setTestDataTypeDao(_testDataTypeDao);
        _controller.setTestDataSetDao(_testDataSetDao);
    }

    private void replayAllMocks() {
        replayMocks(_compareLabelDao, _compareConfigDao, _testDataTypeDao, _testDataSetDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_compareLabelDao, _compareConfigDao, _testDataTypeDao, _testDataSetDao);
    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolBaseStruct.class, _controller.getStruct().getClass());
        assertSame(_compareLabelDao, _controller.getCompareLabelDao());
        assertSame(_compareConfigDao, _controller.getCompareConfigDao());
        assertSame(_testDataTypeDao, _controller.getTestDataTypeDao());
        assertSame(_testDataSetDao, _controller.getTestDataSetDao());
    }


    public void testGetCompareConfigsNull() {
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", CensusDataSetType.SCHOOL)).andReturn(null);
        replayAllMocks();
        List<CompareConfig> rval = _controller.getCompareConfigs(State.CA, "student_teacher");
        verifyAllMocks();
        assertNull(rval);
    }

    public void testGetCompareConfigsSimple() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig1 = new CompareConfig();
        compareConfig1.setState(State.CA);
        compareConfig1.setDataTypeId(9);
        compareConfigs.add(compareConfig1);
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", CensusDataSetType.SCHOOL))
                .andReturn(compareConfigs);
        replayAllMocks();
        List<CompareConfig> rval = _controller.getCompareConfigs(State.CA, "student_teacher");
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(1, rval.size());
        assertSame(compareConfig1, rval.get(0));
    }

    public void testGetCompareConfigsStateOverride() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig1 = new CompareConfig();
        compareConfig1.setState(State.CA);
        compareConfig1.setDataTypeId(9);
        compareConfig1.setYear(-1);
        compareConfigs.add(compareConfig1);
        CompareConfig compareConfig2 = new CompareConfig();
        compareConfig2.setState(null);
        compareConfig2.setDataTypeId(9);
        compareConfig2.setYear(null);
        compareConfigs.add(compareConfig2);
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", CensusDataSetType.SCHOOL))
                .andReturn(compareConfigs);
        replayAllMocks();
        List<CompareConfig> rval = _controller.getCompareConfigs(State.CA, "student_teacher");
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals("Expect state row to override default row", 1, rval.size());
        assertSame("Expect state row to override default row", compareConfig1, rval.get(0));
    }

    public void testGetCompareConfigsComplex() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig1 = new CompareConfig();
        compareConfig1.setState(State.CA);
        compareConfig1.setDataTypeId(9);
        compareConfig1.setYear(-1);
        compareConfigs.add(compareConfig1);
        CompareConfig compareConfig2 = new CompareConfig();
        compareConfig2.setState(null);
        compareConfig2.setDataTypeId(9);
        compareConfig2.setYear(null);
        compareConfigs.add(compareConfig2);
        CompareConfig compareConfig3 = new CompareConfig();
        compareConfig3.setState(null);
        compareConfig3.setDataTypeId(9);
        compareConfig3.setYear(2010);
        compareConfig3.setSchoolType(SchoolType.PRIVATE);
        compareConfigs.add(compareConfig3);
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", CensusDataSetType.SCHOOL))
                .andReturn(compareConfigs);
        replayAllMocks();
        List<CompareConfig> rval = _controller.getCompareConfigs(State.CA, "student_teacher");
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals("Expect state row to override default row", 2, rval.size());
        assertSame("Expect state row to override default row", compareConfig1, rval.get(0));
        assertSame(compareConfig3, rval.get(1));
    }

    public void testGetTestDataSetsNoConfigs() {
        // expect nothing to happen
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        Map<TestDataSet, CompareLabel> testDataSetCompareLabelMap = new HashMap<TestDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<TestDataSet, SchoolType> testDataSetSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
        replayAllMocks();
        List<TestDataSet> rval = _controller.getTestDataSets
                (State.CA, compareConfigs, testDataSetCompareLabelMap, rowLabelToOrder, testDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(0, rval.size());
        assertEquals(0, testDataSetCompareLabelMap.size());
        assertEquals(0, rowLabelToOrder.size());
        assertEquals(0, testDataSetSchoolTypeMap.size());
    }

    public void testPopulateStructsEmpty() {
        List<School> schools = new ArrayList<School>();
        List<SchoolTestValue> schoolTestValues = new ArrayList<SchoolTestValue>();
        Map<TestDataSet, SchoolType> testDataSetToSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
        Map<TestDataSet, CompareLabel> testDataSetToRowLabelMap =
                new HashMap<TestDataSet, CompareLabel>();
        Map<String, CensusStruct[]> rval;

        rval = _controller.populateStructs(schools, schoolTestValues, testDataSetToSchoolTypeMap, testDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        SchoolTestValue testValue1 = new SchoolTestValue();
        schoolTestValues.add(testValue1);
        rval = _controller.populateStructs(schools, schoolTestValues, testDataSetToSchoolTypeMap, testDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        schoolTestValues.clear();
        School school1 = getSchool(1);
        schools.add(school1);
        rval = _controller.populateStructs(schools, schoolTestValues, testDataSetToSchoolTypeMap, testDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());
    }

    public void testPopulateStructsSimple() {
        List<School> schools = new ArrayList<School>();
        List<SchoolTestValue> schoolTestValues = new ArrayList<SchoolTestValue>();
        Map<TestDataSet, SchoolType> testDataSetToSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
        Map<TestDataSet, CompareLabel> testDataSetToRowLabelMap =
                new HashMap<TestDataSet, CompareLabel>();
        Map<String, CensusStruct[]> rval;
        SchoolTestValue testValue1;
        TestDataSet testDataSet = new TestDataSet();
        testDataSet.setDataTypeId(getTestDataType().getId());
        CompareLabel label= getLabel("API Growth", "");
        testDataSetToRowLabelMap.put(testDataSet,label);

        expect(_testDataTypeDao.getDataType(getTestDataType().getId())).andReturn(getTestDataType()).anyTimes();
        replayAllMocks();

        //add one school and assert the cells.
        School school1 = getSchool(1);
        schools.add(school1);
        testValue1 = getSchoolTestValue(school1, testDataSet, 40000);
        schoolTestValues.add(testValue1);
        rval = _controller.populateStructs(schools, schoolTestValues, testDataSetToSchoolTypeMap, testDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(2,rval.get("API Growth").length);
        assertHeaderCell(rval.get("API Growth")[0], "API Growth");
        assertSimpleCell(rval.get("API Growth")[1], "40000.0");

        //add two schools and assert the order of the cells.
        School school2 = getSchool(2);
        schools.add(school2);
        SchoolTestValue testValue2 = getSchoolTestValue(school2, testDataSet, 60000);
        schoolTestValues.add(testValue2);
        rval = _controller.populateStructs(schools, schoolTestValues, testDataSetToSchoolTypeMap, testDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("API Growth").length);
        assertHeaderCell(rval.get("API Growth")[0], "API Growth");
        assertSimpleCell(rval.get("API Growth")[1], "40000.0");
        assertSimpleCell(rval.get("API Growth")[2], "60000.0");

        verifyAllMocks();
    }

//    public void testGetTestDataSetsNoDataSet() {
//        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
//        CompareConfig compareConfig = new CompareConfig();
//        compareConfig.setState(State.CA);
//        compareConfig.setDataTypeId(110);
//        compareConfig.setTabName(TAB_NAME);
//        compareConfig.setId(1);
//        compareConfigs.add(compareConfig);
//        Map<TestDataSet, CompareLabel> testDataSetCompareLabelMap = new HashMap<TestDataSet, CompareLabel>();
//        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
//        Map<TestDataSet, SchoolType> testDataSetSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
//
//        expect(_testDataTypeDao.getDataType(getTestDataType().getId())).andReturn(getTestDataType());
//        expect(_testDataSetDao.findDataSet(State.CA, getTestDataType(), null, null, null, null, null))
//                .andReturn(null);
//        replayAllMocks();
//        List<TestDataSet> rval = _controller.getTestDataSets
//                (State.CA, compareConfigs, testDataSetCompareLabelMap, rowLabelToOrder, testDataSetSchoolTypeMap);
//        verifyAllMocks();
//        assertNotNull(rval);
//        assertEquals(0, rval.size());
//        assertEquals(0, testDataSetCompareLabelMap.size());
//        assertEquals(0, rowLabelToOrder.size());
//        assertEquals(0, testDataSetSchoolTypeMap.size());
//    }

    public void testGetTestDataSetsNoDataType() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig = new CompareConfig();
        compareConfig.setState(State.CA);
        compareConfig.setDataTypeId(110);
        compareConfig.setTabName(TAB_NAME);
        compareConfig.setId(1);
        compareConfigs.add(compareConfig);
        Map<TestDataSet, CompareLabel> testDataSetCompareLabelMap = new HashMap<TestDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<TestDataSet, SchoolType> testDataSetSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();

        expect(_testDataTypeDao.getDataType(getTestDataType().getId())).andReturn(null);
        replayAllMocks();
        List<TestDataSet> rval = _controller.getTestDataSets
                (State.CA, compareConfigs, testDataSetCompareLabelMap, rowLabelToOrder, testDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(0, rval.size());
        assertEquals(0, testDataSetCompareLabelMap.size());
        assertEquals(0, rowLabelToOrder.size());
        assertEquals(0, testDataSetSchoolTypeMap.size());
    }

//    public void testGetTestDataSetsNoLabel() {
//        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
//        CompareConfig compareConfig = new CompareConfig();
//        compareConfig.setState(State.CA);
//        compareConfig.setDataTypeId(getTestDataType().getId());
//        compareConfig.setTabName(TAB_NAME);
//        compareConfig.setId(1);
//        compareConfigs.add(compareConfig);
//        Map<TestDataSet, CompareLabel> testDataSetCompareLabelMap = new HashMap<TestDataSet, CompareLabel>();
//        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
//        Map<TestDataSet, SchoolType> testDataSetSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
//
//        expect(_testDataTypeDao.getDataType(getTestDataType().getId())).andReturn(getTestDataType());
//        expect(_testDataSetDao.findDataSet(State.CA, getTestDataType(), null, null, null, null, null))
//                .andReturn(new TestDataSet());
//        expect(_compareLabelDao
//                       .findLabel(State.CA, getTestDataType().getId(), TAB_NAME, null, null, null, null))
//                .andReturn(null);
//        replayAllMocks();
//        List<TestDataSet> rval = _controller.getTestDataSets
//                (State.CA, compareConfigs, testDataSetCompareLabelMap, rowLabelToOrder, testDataSetSchoolTypeMap);
//        verifyAllMocks();
//        assertNotNull(rval);
//        assertEquals(0, rval.size());
//        assertEquals(0, testDataSetCompareLabelMap.size());
//        assertEquals(0, rowLabelToOrder.size());
//        assertEquals(0, testDataSetSchoolTypeMap.size());
//    }
//
//    public void testGetTestDataSetsOneConfigSimple() {
//        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
//        CompareConfig compareConfig = new CompareConfig();
//        compareConfig.setState(State.CA);
//        compareConfig.setDataTypeId(getTestDataType().getId());
//        compareConfig.setTabName(TAB_NAME);
//        compareConfig.setId(1);
//        compareConfig.setOrderNum(1);
//        compareConfigs.add(compareConfig);
//        Map<TestDataSet, CompareLabel> testDataSetCompareLabelMap = new HashMap<TestDataSet, CompareLabel>();
//        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
//        Map<TestDataSet, SchoolType> testDataSetSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
//
//        TestDataSet testDataSet = new TestDataSet();
//        expect(_testDataSetDao.findDataSet(State.CA, getTestDataType(), null, null, null, null, null))
//                .andReturn(testDataSet);
//        CompareLabel label = new CompareLabel();
//        label.setRowLabel("Ethnicity");
//        expect(_compareLabelDao
//                       .findLabel(State.CA, getTestDataType().getId(), TAB_NAME, null, null, null, null))
//                .andReturn(label);
//        replayAllMocks();
//        List<TestDataSet> rval = _controller.getTestDataSets
//                (State.CA, compareConfigs, testDataSetCompareLabelMap, rowLabelToOrder, testDataSetSchoolTypeMap);
//        verifyAllMocks();
//        assertNotNull(rval);
//        assertEquals(1, rval.size());
//        assertSame(testDataSet, rval.get(0));
//        assertEquals(1, testDataSetCompareLabelMap.size());
//        assertSame(label, testDataSetCompareLabelMap.get(testDataSet));
//        assertEquals(1, rowLabelToOrder.size());
//        assertEquals(1, rowLabelToOrder.get("Ethnicity").intValue());
//        assertEquals(0, testDataSetSchoolTypeMap.size());
//    }
//
//    public void testGetTestDataSetsOneConfigComplex() {
//        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
//        CompareConfig compareConfig = new CompareConfig();
//        compareConfig.setState(State.CA);
//        compareConfig.setDataTypeId(getTestDataType().getId());
//        compareConfig.setTabName(TAB_NAME);
//        compareConfig.setId(1);
//        compareConfig.setOrderNum(1);
//        compareConfig.setBreakdownId(5);
//        compareConfig.setGradeLevels(Grades.createGrades(Grade.G_3));
//        compareConfig.setLevelCode(LevelCode.ELEMENTARY);
//        compareConfig.setYear(2009);
//        compareConfig.setSubject(Subject.ENGLISH);
//        compareConfigs.add(compareConfig);
//        Map<TestDataSet, CompareLabel> testDataSetCompareLabelMap = new HashMap<TestDataSet, CompareLabel>();
//        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
//        Map<TestDataSet, SchoolType> testDataSetSchoolTypeMap = new HashMap<TestDataSet, SchoolType>();
//
//        TestDataSet testDataSet = new TestDataSet();
//        expect(_testDataSetDao
//                       .findDataSet(eq(State.CA), eq(getTestDataType()), eq(2009), isA(Breakdown.class),
//                                    eq(Subject.ENGLISH), eq(LevelCode.ELEMENTARY), eq(Grades.createGrades(Grade.G_3))))
//                .andReturn(testDataSet);
//        CompareLabel label = new CompareLabel();
//        label.setRowLabel("Ethnicity");
//        expect(_compareLabelDao
//                       .findLabel(eq(State.CA), eq(getTestDataType().getId().intValue()), eq(TAB_NAME),
//                                  eq(Grades.createGrades(Grade.G_3)), isA(Breakdown.class), eq(LevelCode.ELEMENTARY),
//                                  eq(Subject.ENGLISH)))
//                .andReturn(label);
//        replayAllMocks();
//        List<TestDataSet> rval = _controller.getTestDataSets
//                (State.CA, compareConfigs, testDataSetCompareLabelMap, rowLabelToOrder, testDataSetSchoolTypeMap);
//        verifyAllMocks();
//        assertNotNull(rval);
//        assertEquals(1, rval.size());
//        assertSame(testDataSet, rval.get(0));
//        assertEquals(1, testDataSetCompareLabelMap.size());
//        assertSame(label, testDataSetCompareLabelMap.get(testDataSet));
//        assertEquals(1, rowLabelToOrder.size());
//        assertEquals(1, rowLabelToOrder.get("Ethnicity").intValue());
//        assertEquals(0, testDataSetSchoolTypeMap.size());
//    }

    public void testSortRows(){
        Map<String, CensusStruct[]> rowLabelToCells = new HashMap<String, CensusStruct[]>();
        CensusStruct[] cs1 = new CensusStruct[1];
        cs1[0] = new CensusStruct();
        cs1[0].setHeaderText("API Growth");
        rowLabelToCells.put("API Growth",cs1);
        CensusStruct[] cs2 = new CensusStruct[2];
        cs2[0] = new CensusStruct();
        cs2[0].setHeaderText("CST English Grade 3");
        cs2[1] = new CensusStruct();
        rowLabelToCells.put("CST English Grade 3",cs2);
        CensusStruct[] cs3 = new CensusStruct[1];
        cs3[0] = new CensusStruct();
        cs3[0].setHeaderText("CST Math Grade 3");
        rowLabelToCells.put("CST Math Grade 3",cs3);
        CensusStruct[] cs4 = new CensusStruct[3];
        cs4[0] = new CensusStruct();
        cs4[0].setHeaderText("CST English Grade 8");
        cs4[1] = new CensusStruct();
        rowLabelToCells.put("CST English Grade 8", cs4);
        Map<String, Integer> rowLabelToOrder = new HashMap<String,Integer>();
        rowLabelToOrder.put("CST English Grade 3",1);
        rowLabelToOrder.put("CST Math Grade 3",2);
        rowLabelToOrder.put("CST English Grade 8",3);
        rowLabelToOrder.put("API Growth",4);

        List<CensusStruct[]> rval = _controller.sortRows(rowLabelToCells,rowLabelToOrder);
        assertNotNull(rval);
        assertEquals(4, rval.size());
        assertEquals("CST English Grade 3", rval.get(0)[0].getHeaderText());
        assertEquals("CST Math Grade 3", rval.get(1)[0].getHeaderText());
        assertEquals("CST English Grade 8", rval.get(2)[0].getHeaderText());
        assertEquals("API Growth", rval.get(3)[0].getHeaderText());
    }

    private void assertHeaderCell(CensusStruct cell, String headerText) {
        assertNotNull(cell);
        assertTrue(cell.getIsHeaderCell());
        assertEquals(headerText, cell.getHeaderText());
    }

    private void assertSimpleCell(CensusStruct cell, String cellValue) {
        assertNotNull(cell);
        assertTrue(cell.getIsSimpleCell());
        assertFalse(cell.getIsHeaderCell());
        assertEquals(cellValue, cell.getValue());
    }

    private TestDataType getTestDataType() {
        return TestDataType.ROLLUP_RATING;
    }

    private School getSchool(int id) {
        return getSchool(id, SchoolType.PUBLIC);
    }

    private School getSchool(int id, SchoolType schoolType) {
        School school = new School();
        school.setId(id);
        school.setType(schoolType);
        return school;
    }

    private SchoolTestValue getSchoolTestValue(School school, TestDataSet dataSet, float value) {
        SchoolTestValue rval = new SchoolTestValue();
        rval.setSchool(school);
        rval.setDataSet(dataSet);
        rval.setValueFloat(value);
        return rval;
    }

    private CompareLabel getLabel(String rowLabel, String breakdownLabel) {
        CompareLabel rval = new CompareLabel();
        rval.setRowLabel(rowLabel);
        rval.setBreakdownLabel(breakdownLabel);
        return rval;
    }
}
