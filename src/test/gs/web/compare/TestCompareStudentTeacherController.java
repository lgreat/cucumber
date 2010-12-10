package gs.web.compare;

import gs.data.compare.CompareConfig;
import gs.data.compare.CompareLabel;
import gs.data.compare.ICompareConfigDao;
import gs.data.compare.ICompareLabelDao;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.test.Subject;
import gs.web.BaseControllerTestCase;

import java.util.*;

import static org.easymock.EasyMock.*;
import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;
import static gs.web.compare.CompareStudentTeacherController.TAB_NAME;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareStudentTeacherController extends BaseControllerTestCase {
    private CompareStudentTeacherController _controller;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;
    private ISchoolCensusValueDao _schoolCensusValueDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareStudentTeacherController();

        _censusDataSetDao = createStrictMock(ICensusDataSetDao.class);
        _censusInfo = createStrictMock(ICensusInfo.class);
        _compareLabelDao = createStrictMock(ICompareLabelDao.class);
        _compareConfigDao = createStrictMock(ICompareConfigDao.class);
        _schoolCensusValueDao = createStrictMock(ISchoolCensusValueDao.class);
        
        _controller.setSuccessView("success");
        _controller.setCensusDataSetDao(_censusDataSetDao);
        _controller.setCensusInfo(_censusInfo);
        _controller.setCompareLabelDao(_compareLabelDao);
        _controller.setCompareConfigDao(_compareConfigDao);
        _controller.setSchoolCensusValueDao(_schoolCensusValueDao);
    }

    private void replayAllMocks() {
        replayMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao, _schoolCensusValueDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao, _schoolCensusValueDao);
    }

//    private void resetAllMocks() {
//        resetMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao, _schoolCensusValueDao);
//    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertSame(_censusDataSetDao, _controller.getCensusDataSetDao());
        assertSame(_censusInfo, _controller.getCensusInfo());
        assertSame(_compareConfigDao, _controller.getCompareConfigDao());
        assertSame(_compareLabelDao, _controller.getCompareLabelDao());
        assertSame(_schoolCensusValueDao, _controller.getSchoolCensusValueDao());
        assertEquals(ComparedSchoolStudentTeacherStruct.class, _controller.getStruct().getClass());
    }

    public void testEmptyList() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(),
                                         new ArrayList<ComparedSchoolBaseStruct>(), model);
        verifyAllMocks();
        assertEquals(CompareStudentTeacherController.TAB_NAME, model.get(MODEL_TAB));
    }

    public void testPopulateStructsEmpty() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CensusStruct[]> rval;

        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        SchoolCensusValue censusValue1 = new SchoolCensusValue();
        schoolCensusValues.add(censusValue1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        schoolCensusValues.clear();
        School school1 = getSchool(1);
        schools.add(school1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

    }

    public void testPopulateStructsSimple() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CensusStruct[]> rval;
        SchoolCensusValue censusValue1;
        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.AVERAGE_SALARY,2009);
        CompareLabel label= getLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet,label);

        //add one school and assert the cells.
        School school1 = getSchool(1);
        schools.add(school1);
        censusValue1 = getSchoolCensusValue(school1, censusDataSet, 40000);
        schoolCensusValues.add(censusValue1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(2,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "$40000");

        //add two schools and assert the order of the cells.
        School school2 = getSchool(2);
        schools.add(school2);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school2, censusDataSet, 60000);
        schoolCensusValues.add(censusValue2);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "$40000");
        assertSimpleCell(rval.get("Average Salary")[2], "$60000");

    }

    public void testPopulateStructsRespectsSchoolType() {
        // test that schools are assigned values only if the compare config doesn't define the value as
        // belonging to a different school type
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CensusStruct[]> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);
        School school2 = getSchool(2, SchoolType.PRIVATE);
        schools.add(school2);

        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.AVERAGE_SALARY,2009);
        censusDataSetToSchoolTypeMap.put(censusDataSet, SchoolType.PUBLIC);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.AVERAGE_SALARY,2008);
        CompareLabel label2 = getLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet2,label2);
        CompareLabel label= getLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet,label);

        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet, 40000);
        schoolCensusValues.add(censusValue1);

        // test that a census data set with a school type will only apply to schools of that type
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "$40000");
        assertNull("Expect third cell to be null as its school type has no data.", rval.get("Average Salary")[2]);

        // this census data set has values for all schools
        // test that the public data set overrides this one for school1
        SchoolCensusValue censusValue3 = getSchoolCensusValue(school1, censusDataSet2, 70000);
        schoolCensusValues.add(censusValue3);
        SchoolCensusValue censusValue4 = getSchoolCensusValue(school2, censusDataSet2, 80000);
        schoolCensusValues.add(censusValue4);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "$40000");
        assertSimpleCell(rval.get("Average Salary")[2], "$80000");

        // try in different order to double check
        Collections.reverse(schoolCensusValues);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "$40000");
        assertSimpleCell(rval.get("Average Salary")[2], "$80000");
    }

    public void testPopulateStructsWithBreakdown() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CensusStruct[]> rval;

        //add first school with white,asian breakdowns
        CensusDataSet censusDataSet1 = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2009);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2009);
        CompareLabel label1= getLabel("Student Ethnicity", "White");
        CompareLabel label2= getLabel("Student Ethnicity", "Asian");
        censusDataSetToRowLabelMap.put(censusDataSet1,label1);
        censusDataSetToRowLabelMap.put(censusDataSet2,label2);
        School school1 = getSchool(1);
        schools.add(school1);
        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet1, 40);
        schoolCensusValues.add(censusValue1);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school1, censusDataSet2, 60);
        schoolCensusValues.add(censusValue2);

        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(2,rval.get("Student Ethnicity").length);
        assertHeaderCell(rval.get("Student Ethnicity")[0], "Student Ethnicity");
        assertBreakdownCell(rval.get("Student Ethnicity")[1],0,"White","40%");
        assertBreakdownCell(rval.get("Student Ethnicity")[1],1,"Asian","60%");

        //add second school with white, asian, hispanic breakdowns
        CensusDataSet censusDataSet3 = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2009);
        CompareLabel label3= getLabel("Student Ethnicity", "Hispanic");
        censusDataSetToRowLabelMap.put(censusDataSet3,label3);
        School school2 = getSchool(2, SchoolType.PRIVATE);
        schools.add(school2);
        SchoolCensusValue censusValue3 = getSchoolCensusValue(school2, censusDataSet1, 20);
        schoolCensusValues.add(censusValue3);
        SchoolCensusValue censusValue4 = getSchoolCensusValue(school2, censusDataSet2, 40);
        schoolCensusValues.add(censusValue4);
        SchoolCensusValue censusValue5 = getSchoolCensusValue(school2, censusDataSet3, 40);
        schoolCensusValues.add(censusValue5);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);

        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Student Ethnicity").length);
        assertBreakdownCell(rval.get("Student Ethnicity")[2],0,"White","20%");
        assertBreakdownCell(rval.get("Student Ethnicity")[2],1,"Asian","40%");
        assertBreakdownCell(rval.get("Student Ethnicity")[2],2,"Hispanic","40%");

        // add more recent data set for private schools, confirm that those values are used over the older ones
        CensusDataSet censusDataSet1b = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2010);
        CensusDataSet censusDataSet2b = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2010);
        censusDataSetToRowLabelMap.put(censusDataSet1b,label1);
        censusDataSetToRowLabelMap.put(censusDataSet2b,label2);
        censusDataSetToSchoolTypeMap.put(censusDataSet1b, SchoolType.PRIVATE);
        censusDataSetToSchoolTypeMap.put(censusDataSet2b, SchoolType.PRIVATE);
        SchoolCensusValue censusValue1b = getSchoolCensusValue(school1, censusDataSet1b, 30);
        schoolCensusValues.add(censusValue1b);
        SchoolCensusValue censusValue2b = getSchoolCensusValue(school1, censusDataSet2b, 70);
        schoolCensusValues.add(censusValue2b);
        SchoolCensusValue censusValue3b = getSchoolCensusValue(school2, censusDataSet1b, 20);
        schoolCensusValues.add(censusValue3b);
        SchoolCensusValue censusValue4b = getSchoolCensusValue(school2, censusDataSet2b, 80);
        schoolCensusValues.add(censusValue4b);

        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);

        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Student Ethnicity").length);
        assertHeaderCell(rval.get("Student Ethnicity")[0], "Student Ethnicity");
        assertEquals(2, rval.get("Student Ethnicity")[1].getBreakdownList().size());
        assertBreakdownCell(rval.get("Student Ethnicity")[1],0,"White","40%");
        assertBreakdownCell(rval.get("Student Ethnicity")[1],1,"Asian","60%");
        assertEquals(2, rval.get("Student Ethnicity")[2].getBreakdownList().size());
        assertBreakdownCell(rval.get("Student Ethnicity")[2],0,"White","20%");
        assertBreakdownCell(rval.get("Student Ethnicity")[2],1,"Asian","80%");
    }

    public void testGetCensusDataSetsNoConfigs() {
        // expect nothing to happen
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(0, rval.size());
        assertEquals(0, censusDataSetCompareLabelMap.size());
        assertEquals(0, rowLabelToOrder.size());
        assertEquals(0, censusDataSetSchoolTypeMap.size());
    }

    public void testGetCensusDataSetsNoDataSet() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig = new CompareConfig();
        compareConfig.setState(State.CA);
        compareConfig.setDataTypeId(CensusDataType.STUDENTS_ETHNICITY.getId());
        compareConfig.setTabName(TAB_NAME);
        compareConfig.setId(1);
        compareConfigs.add(compareConfig);
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();

        expect(_censusDataSetDao.findDataSet(State.CA, CensusDataType.STUDENTS_ETHNICITY, null, null, null, null, null))
                .andReturn(null);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(0, rval.size());
        assertEquals(0, censusDataSetCompareLabelMap.size());
        assertEquals(0, rowLabelToOrder.size());
        assertEquals(0, censusDataSetSchoolTypeMap.size());
    }

    public void testGetCensusDataSetsNoLabel() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig = new CompareConfig();
        compareConfig.setState(State.CA);
        compareConfig.setDataTypeId(CensusDataType.STUDENTS_ETHNICITY.getId());
        compareConfig.setTabName(TAB_NAME);
        compareConfig.setId(1);
        compareConfigs.add(compareConfig);
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();

        expect(_censusDataSetDao.findDataSet(State.CA, CensusDataType.STUDENTS_ETHNICITY, null, null, null, null, null))
                .andReturn(new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009));
        expect(_compareLabelDao
                       .findLabel(State.CA, CensusDataType.STUDENTS_ETHNICITY, TAB_NAME, null, null, null, null))
                .andReturn(null);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(0, rval.size());
        assertEquals(0, censusDataSetCompareLabelMap.size());
        assertEquals(0, rowLabelToOrder.size());
        assertEquals(0, censusDataSetSchoolTypeMap.size());
    }

    public void testGetCensusDataSetsOneConfigSimple() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig = new CompareConfig();
        compareConfig.setState(State.CA);
        compareConfig.setDataTypeId(CensusDataType.STUDENTS_ETHNICITY.getId());
        compareConfig.setTabName(TAB_NAME);
        compareConfig.setId(1);
        compareConfig.setOrderNum(1);
        compareConfigs.add(compareConfig);
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();

        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009);
        expect(_censusDataSetDao.findDataSet(State.CA, CensusDataType.STUDENTS_ETHNICITY, null, null, null, null, null))
                .andReturn(censusDataSet);
        CompareLabel label = new CompareLabel();
        label.setRowLabel("Ethnicity");
        expect(_compareLabelDao
                       .findLabel(State.CA, CensusDataType.STUDENTS_ETHNICITY, TAB_NAME, null, null, null, null))
                .andReturn(label);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(1, rval.size());
        assertSame(censusDataSet, rval.get(0));
        assertEquals(1, censusDataSetCompareLabelMap.size());
        assertSame(label, censusDataSetCompareLabelMap.get(censusDataSet));
        assertEquals(1, rowLabelToOrder.size());
        assertEquals(1, rowLabelToOrder.get("Ethnicity").intValue());
        assertEquals(0, censusDataSetSchoolTypeMap.size());
    }

    public void testGetCensusDataSetsOneConfigComplex() {
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        CompareConfig compareConfig = new CompareConfig();
        compareConfig.setState(State.CA);
        compareConfig.setDataTypeId(CensusDataType.STUDENTS_ETHNICITY.getId());
        compareConfig.setTabName(TAB_NAME);
        compareConfig.setId(1);
        compareConfig.setOrderNum(1);
        compareConfig.setBreakdownId(5);
        compareConfig.setGradeLevels(Grades.createGrades(Grade.G_3));
        compareConfig.setLevelCode(LevelCode.ELEMENTARY);
        compareConfig.setYear(2009);
        compareConfig.setSubject(Subject.ENGLISH);
        compareConfigs.add(compareConfig);
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();

        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009);
        expect(_censusDataSetDao
                       .findDataSet(eq(State.CA), eq(CensusDataType.STUDENTS_ETHNICITY), eq(2009), isA(Breakdown.class),
                                    eq(Subject.ENGLISH), eq(LevelCode.ELEMENTARY), eq(Grades.createGrades(Grade.G_3))))
                .andReturn(censusDataSet);
        CompareLabel label = new CompareLabel();
        label.setRowLabel("Ethnicity");
        expect(_compareLabelDao
                       .findLabel(eq(State.CA), eq(CensusDataType.STUDENTS_ETHNICITY), eq(TAB_NAME),
                                  eq(Grades.createGrades(Grade.G_3)), isA(Breakdown.class), eq(LevelCode.ELEMENTARY),
                                  eq(Subject.ENGLISH)))
                .andReturn(label);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(1, rval.size());
        assertSame(censusDataSet, rval.get(0));
        assertEquals(1, censusDataSetCompareLabelMap.size());
        assertSame(label, censusDataSetCompareLabelMap.get(censusDataSet));
        assertEquals(1, rowLabelToOrder.size());
        assertEquals(1, rowLabelToOrder.get("Ethnicity").intValue());
        assertEquals(0, censusDataSetSchoolTypeMap.size());
    }

    public void testSortRows(){
        Map<String, CensusStruct[]> rowLabelToCells = new HashMap<String, CensusStruct[]>();
        CensusStruct[] cs1 = new CensusStruct[1];
        cs1[0] = new CensusStruct();
        cs1[0].setHeaderText("Average Salary");
        rowLabelToCells.put("Average Salary",cs1);
        CensusStruct[] cs2 = new CensusStruct[2];
        cs2[0] = new CensusStruct();
        cs2[0].setHeaderText("Student Ethnicity");
        cs2[1] = new CensusStruct();
        List<BreakdownNameValue> breakdowns = new ArrayList<BreakdownNameValue>();
        breakdowns.add(getBreakdown("White", "30%", 30f));
        breakdowns.add(getBreakdown("Asian", "50%", 50f));
        breakdowns.add(getBreakdown("Black", "20%", 20f));
        cs2[1].setBreakdownList(breakdowns);
        rowLabelToCells.put("Student Ethnicity",cs2);
        CensusStruct[] cs3 = new CensusStruct[1];
        cs3[0] = new CensusStruct();
        cs3[0].setHeaderText("Average years Teaching");
        rowLabelToCells.put("Average years Teaching",cs3);
        CensusStruct[] cs4 = new CensusStruct[2];
        cs4[0] = new CensusStruct();
        cs4[0].setHeaderText("Students per teacher");
        cs4[1] = new CensusStruct();
        breakdowns = new ArrayList<BreakdownNameValue>();
        breakdowns.add(getBreakdown("1st grade", "12:1", null));
        breakdowns.add(getBreakdown("2nd grade", "12:1", null));
        breakdowns.add(getBreakdown("3rd grade", "8:1", null));
        breakdowns.add(getBreakdown("4th grade", "10:1", null));
        breakdowns.add(getBreakdown("5th grade", "15:1", null));
        cs4[1].setBreakdownList(breakdowns);
        rowLabelToCells.put("Students per teacher", cs4);
        Map<String, Integer> rowLabelToOrder = new HashMap<String,Integer>();
        rowLabelToOrder.put("Average Salary",1);
        rowLabelToOrder.put("Students per teacher",2);
        rowLabelToOrder.put("Student Ethnicity",3);
        rowLabelToOrder.put("Average years Teaching",4);
        
        List<CensusStruct[]> rval = _controller.sortRows(rowLabelToCells,rowLabelToOrder);
        assertNotNull(rval);
        assertEquals(4, rval.size());
        assertEquals("Average Salary", rval.get(0)[0].getHeaderText());
        assertEquals("Students per teacher", rval.get(1)[0].getHeaderText());
        assertEquals("Student Ethnicity", rval.get(2)[0].getHeaderText());
        assertEquals("Average years Teaching", rval.get(3)[0].getHeaderText());

        assertEquals("Asian", rval.get(2)[1].getBreakdownList().get(0).getName());
        assertEquals("50%", rval.get(2)[1].getBreakdownList().get(0).getValue());
        assertEquals("White", rval.get(2)[1].getBreakdownList().get(1).getName());
        assertEquals("30%", rval.get(2)[1].getBreakdownList().get(1).getValue());
        assertEquals("Black", rval.get(2)[1].getBreakdownList().get(2).getName());
        assertEquals("20%", rval.get(2)[1].getBreakdownList().get(2).getValue());

        assertEquals("3rd grade", rval.get(1)[1].getBreakdownList().get(0).getName());
        assertEquals("8:1", rval.get(1)[1].getBreakdownList().get(0).getValue());
        assertEquals("4th grade", rval.get(1)[1].getBreakdownList().get(4).getName());
        assertEquals("10:1", rval.get(1)[1].getBreakdownList().get(4).getValue());
    }

    private BreakdownNameValue getBreakdown(String name, String value, Float floatValue) {
        BreakdownNameValue breakdown = new BreakdownNameValue();
        breakdown.setName(name);
        breakdown.setValue(value);
        breakdown.setFloatValue(floatValue);
        return breakdown;
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

    private void assertBreakdownCell(CensusStruct cell,int index, String breakdownName,String breakdownValue) {
        assertNotNull(cell);
        assertFalse(cell.getIsSimpleCell());
        assertFalse(cell.getIsHeaderCell());
        assertEquals(breakdownName, cell.getBreakdownList().get(index).getName());
        assertEquals(breakdownValue, cell.getBreakdownList().get(index).getValue());
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

    private SchoolCensusValue getSchoolCensusValue(School school, CensusDataSet dataSet, float value) {
        SchoolCensusValue rval = new SchoolCensusValue(school, dataSet);
        rval.setValueFloat(value);
        return rval;
    }

    private CompareLabel getLabel(String rowLabel) {
        return getLabel(rowLabel, null);
    }

    private CompareLabel getLabel(String rowLabel, String breakdownLabel) {
        CompareLabel rval = new CompareLabel();
        rval.setRowLabel(rowLabel);
        rval.setBreakdownLabel(breakdownLabel);
        return rval;
    }
}