package gs.web.compare;

import gs.data.compare.CompareConfig;
import gs.data.compare.CompareLabel;
import gs.data.compare.ICompareConfigDao;
import gs.data.compare.ICompareLabelDao;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.school.census.*;
import gs.web.BaseControllerTestCase;

import java.util.*;

import static org.easymock.EasyMock.*;
import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareStudentTeacherController extends BaseControllerTestCase {
    private CompareStudentTeacherController _controller;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareStudentTeacherController();

        _censusDataSetDao = createStrictMock(ICensusDataSetDao.class);
        _censusInfo = createStrictMock(ICensusInfo.class);
        _compareLabelDao = createStrictMock(ICompareLabelDao.class);
        _compareConfigDao = createStrictMock(ICompareConfigDao.class);

        
        _controller.setSuccessView("success");
        _controller.setCensusDataSetDao(_censusDataSetDao);
        _controller.setCensusInfo(_censusInfo);
        _controller.setCompareLabelDao(_compareLabelDao);
        _controller.setCompareConfigDao(_compareConfigDao);
    }

    private void replayAllMocks() {
        replayMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao);
    }

//    private void resetAllMocks() {
//        resetMocks(_censusDataSetDao, _censusInfo);
//    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertSame(_censusDataSetDao, _controller.getCensusDataSetDao());
        assertSame(_censusInfo, _controller.getCensusInfo());
        assertSame(_compareConfigDao, _controller.getCompareConfigDao());
        assertSame(_compareLabelDao, _controller.getCompareLabelDao());
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
        School school2 = getSchool(2);
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

        // add more recent data set, confirm that those values are used over the older ones
        CensusDataSet censusDataSet1b = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2010);
        CensusDataSet censusDataSet2b = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY,2010);
        censusDataSetToRowLabelMap.put(censusDataSet1b,label1);
        censusDataSetToRowLabelMap.put(censusDataSet2b,label2);
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
        assertBreakdownCell(rval.get("Student Ethnicity")[1],0,"White","30%");
        assertBreakdownCell(rval.get("Student Ethnicity")[1],1,"Asian","70%");
        assertEquals(2, rval.get("Student Ethnicity")[2].getBreakdownList().size());
        assertBreakdownCell(rval.get("Student Ethnicity")[2],0,"White","20%");
        assertBreakdownCell(rval.get("Student Ethnicity")[2],1,"Asian","80%");
    }

    public void testSortRows(){
        Map<String, CensusStruct[]> rowLabelToCells = new HashMap<String, CensusStruct[]>();
        CensusStruct[] cs1 = new CensusStruct[1];
        rowLabelToCells.put("Average Salary",cs1);
        CensusStruct[] cs2 = new CensusStruct[1];
        rowLabelToCells.put("Student Ethnicity",cs2);
        CensusStruct[] cs3 = new CensusStruct[1];
        rowLabelToCells.put("Average years Teaching",cs3);
        CensusStruct[] cs4 = new CensusStruct[1];
        rowLabelToCells.put("Students per teacher",cs4);
        Map<String, String> rowLabelToOrder = new HashMap<String,String>();
        rowLabelToOrder.put("Average Salary","1");
        rowLabelToOrder.put("Students per teacher","2");
        rowLabelToOrder.put("Student Ethnicity","3");
        rowLabelToOrder.put("Average years Teaching","4");
        
        LinkedHashMap<String, CensusStruct[]> sortedMap = _controller.sortRows(rowLabelToCells,rowLabelToOrder);
        List<String> labels = new LinkedList<String>(sortedMap.keySet());
        assertEquals(labels.get(0),"Average Salary");
        assertEquals(labels.get(1),"Students per teacher");
        assertEquals(labels.get(2),"Student Ethnicity");
        assertEquals(labels.get(3),"Average years Teaching");

    }

    public void testGetCompareConfig(){
        State state = State.fromString("CA");
        String tabName = "student_teacher";
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        expect(_compareConfigDao.getConfig(state,tabName,CensusDataSetType.SCHOOL)).andReturn(compareConfigs);
        replayAllMocks();
        List<CompareConfig> rval = _controller.getCompareConfig(state,tabName);
        verifyAllMocks();
        assertEquals(0, rval.size());
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