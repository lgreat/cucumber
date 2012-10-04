package gs.web.compare;

import gs.data.compare.*;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.source.DataSetContentType;
import gs.data.state.State;
import gs.data.test.Subject;
import gs.web.BaseControllerTestCase;

import java.util.*;

import static gs.web.compare.CompareStudentTeacherController.SCHOOL_CENSUS_VALUE_DESCENDING;
import static org.easymock.EasyMock.*;
import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;
import static gs.web.compare.CompareStudentTeacherController.TAB_NAME;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareStudentTeacherControllerTest extends BaseControllerTestCase {
    private CompareStudentTeacherController _controller;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;
    private ICensusDataSchoolValueDao _censusDataSchoolValueDao;
    private ICompareLabelInfoDao _compareLabelInfoDao;

    private Map<String, CompareLabelInfo> _labelToCompareLabelInfoMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareStudentTeacherController();

        _censusDataSetDao = createStrictMock(ICensusDataSetDao.class);
        _censusInfo = createStrictMock(ICensusInfo.class);
        _compareLabelDao = createStrictMock(ICompareLabelDao.class);
        _compareConfigDao = createStrictMock(ICompareConfigDao.class);
        _censusDataSchoolValueDao = createStrictMock(ICensusDataSchoolValueDao.class);
        _compareLabelInfoDao = createStrictMock(ICompareLabelInfoDao.class);
        
        _controller.setSuccessView("success");
        _controller.setCensusDataSetDao(_censusDataSetDao);
        _controller.setCensusInfo(_censusInfo);
        _controller.setCompareLabelDao(_compareLabelDao);
        _controller.setCompareConfigDao(_compareConfigDao);
        _controller.setCensusDataSchoolValueDao(_censusDataSchoolValueDao);
        _controller.setCompareLabelInfoDao(_compareLabelInfoDao);

        _labelToCompareLabelInfoMap = new HashMap<String, CompareLabelInfo>();

        _controller.setDataTypeIdToOrderMap(new HashMap<Integer, Integer>() {
            {
                put(1, 1);
                put(2, 4);
                put(3, 9);
                put(4, 16);
                put(9, 81);
            }
        });
    }

    private void replayAllMocks() {
        replayMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao, _censusDataSchoolValueDao, _compareLabelInfoDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_censusDataSetDao, _censusInfo, _compareLabelDao, _compareConfigDao, _censusDataSchoolValueDao, _compareLabelInfoDao);
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
        assertSame(_censusDataSchoolValueDao, _controller.getCensusDataSchoolValueDao());
        assertSame(_compareLabelInfoDao, _controller.getCompareLabelInfoDao());
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

    public void testGetCompareConfigsNull() {
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", DataSetContentType.getInstance("school"))).andReturn(null);
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
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", DataSetContentType.getInstance("school")))
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
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", DataSetContentType.getInstance("school")))
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
        expect(_compareConfigDao.getConfig(State.CA, "student_teacher", DataSetContentType.getInstance("school")))
                .andReturn(compareConfigs);
        replayAllMocks();
        List<CompareConfig> rval = _controller.getCompareConfigs(State.CA, "student_teacher");
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals("Expect state row to override default row", 2, rval.size());
        assertSame("Expect state row to override default row", compareConfig1, rval.get(0));
        assertSame(compareConfig3, rval.get(1));
    }

    public void testRetrieveSchoolCensusValuesSimple() {
        // Basic test of retrieveSchoolCensusValues - tests that SchoolCensusValues are loaded to the output list
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        List<CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        Collection<SchoolCensusValue> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);
        School school2 = getSchool(2, SchoolType.PRIVATE);
        schools.add(school2);

        CensusDataSet censusDataSet1 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        censusDataSets.add(censusDataSet1);
        censusDataSetToSchoolTypeMap.put(censusDataSet1, SchoolType.PUBLIC);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2008);
        censusDataSets.add(censusDataSet2);


        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet1, 40000);
        schoolCensusValues.add(censusValue1);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school2, censusDataSet2, 60000);
        schoolCensusValues.add(censusValue2);
        expect( _censusDataSchoolValueDao.findSchoolCensusValues(State.CA, censusDataSets, schools) ).andReturn(schoolCensusValues);

        replayAllMocks();
        rval = _controller.retrieveSchoolCensusValues(State.CA, censusDataSets, schools, censusDataSetToSchoolTypeMap);
        verifyAllMocks();

        assertEquals(2, rval.size());
    }

    public void testRetrieveSchoolCensusValuesRespectsSchoolType() {
        // tests that SchoolCensusValues are loaded to the output list only if the school type is as assigned
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        List<CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        Collection<SchoolCensusValue> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);
        School school2 = getSchool(2, SchoolType.PRIVATE);
        schools.add(school2);

        CensusDataSet censusDataSet1 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        censusDataSets.add(censusDataSet1);
        censusDataSetToSchoolTypeMap.put(censusDataSet1, SchoolType.PUBLIC);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2008);
        censusDataSets.add(censusDataSet2);
        censusDataSetToSchoolTypeMap.put(censusDataSet2, SchoolType.PUBLIC);

        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet1, 40000);
        schoolCensusValues.add(censusValue1);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school2, censusDataSet2, 60000);
        schoolCensusValues.add(censusValue2);
        expect( _censusDataSchoolValueDao.findSchoolCensusValues(State.CA, censusDataSets, schools) ).andReturn(schoolCensusValues);

        replayAllMocks();
        rval = _controller.retrieveSchoolCensusValues(State.CA, censusDataSets, schools, censusDataSetToSchoolTypeMap);
        verifyAllMocks();

        assertEquals(1, rval.size());
    }

    public void testRetrieveSchoolCensusValuesGetsLatest1() {
        // test that retrieveSchoolCensusValues gets the latest dataset when several are available
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        List<CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        Collection<SchoolCensusValue> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);

        CensusDataSet censusDataSet1 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        censusDataSets.add(censusDataSet1);
        censusDataSetToSchoolTypeMap.put(censusDataSet1, SchoolType.PUBLIC);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2008);
        censusDataSets.add(censusDataSet2);


        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet1, 40000);
        schoolCensusValues.add(censusValue1);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school1, censusDataSet2, 60000);
        schoolCensusValues.add(censusValue2);
        expect( _censusDataSchoolValueDao.findSchoolCensusValues(State.CA, censusDataSets, schools) ).andReturn(schoolCensusValues);

        replayAllMocks();
        rval = _controller.retrieveSchoolCensusValues(State.CA, censusDataSets, schools, censusDataSetToSchoolTypeMap);
        verifyAllMocks();

        assertEquals(1, rval.size());
        assertEquals(40000, rval.iterator().next().getValueInteger().intValue());

    }

    public void testRetrieveSchoolCensusValuesGetsLatest2() {
        // test that retrieveSchoolCensusValues gets correct SchoolCensusValue when an older manual dataset is provided
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        List<CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        Collection<SchoolCensusValue> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);

        CensusDataSet censusDataSet1 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        censusDataSets.add(censusDataSet1);
        censusDataSetToSchoolTypeMap.put(censusDataSet1, SchoolType.PUBLIC);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,0);
        censusDataSets.add(censusDataSet2);


        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet1, 40000);
        schoolCensusValues.add(censusValue1);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school1, censusDataSet2, 60000);
        schoolCensusValues.add(censusValue2);
        Calendar cal = Calendar.getInstance();
        cal.set(2008, Calendar.AUGUST, 1, 0, 0, 0);
        censusValue2.setModified(cal.getTime());
        expect( _censusDataSchoolValueDao.findSchoolCensusValues(State.CA, censusDataSets, schools) ).andReturn(schoolCensusValues);

        replayAllMocks();
        rval = _controller.retrieveSchoolCensusValues(State.CA, censusDataSets, schools, censusDataSetToSchoolTypeMap);
        verifyAllMocks();

        assertEquals(1, rval.size());
        assertEquals(40000, rval.iterator().next().getValueInteger().intValue());
    }

    public void testRetrieveSchoolCensusValuesGetsLatest3() {
        // test that retrieveSchoolCensusValues gets correct SchoolCensusValue when a later manual dataset is provided
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        List<CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        Collection<SchoolCensusValue> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);

        CensusDataSet censusDataSet1 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        censusDataSets.add(censusDataSet1);
        censusDataSetToSchoolTypeMap.put(censusDataSet1, SchoolType.PUBLIC);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,0);
        censusDataSets.add(censusDataSet2);


        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet1, 40000);
        schoolCensusValues.add(censusValue1);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school1, censusDataSet2, 60000);
        schoolCensusValues.add(censusValue2);
        Calendar cal = Calendar.getInstance();
        cal.set(2008, Calendar.NOVEMBER, 1, 0, 0, 0);
        censusValue2.setModified(cal.getTime());
        expect( _censusDataSchoolValueDao.findSchoolCensusValues(State.CA, censusDataSets, schools) ).andReturn(schoolCensusValues);

        replayAllMocks();
        rval = _controller.retrieveSchoolCensusValues(State.CA, censusDataSets, schools, censusDataSetToSchoolTypeMap);
        verifyAllMocks();

        assertEquals(1, rval.size());
        assertEquals(60000, rval.iterator().next().getValueInteger().intValue());
    }

    public void testPopulateStructsEmpty() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CompareConfigStruct[]> rval;

        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        SchoolCensusValue censusValue1 = new SchoolCensusValue();
        schoolCensusValues.add(censusValue1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        schoolCensusValues.clear();
        School school1 = getSchool(1);
        schools.add(school1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

    }

    public void testPopulateStructsSimple() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CompareConfigStruct[]> rval;
        SchoolCensusValue censusValue1;
        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        CompareLabel label= getLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet, label);

        //add one school and assert the cells.
        School school1 = getSchool(1);
        schools.add(school1);
        censusValue1 = getSchoolCensusValue(school1, censusDataSet, 40000);
        schoolCensusValues.add(censusValue1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1, rval.size());
        assertEquals(2,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "40000");

        //add two schools and assert the order of the cells.
        School school2 = getSchool(2);
        schools.add(school2);
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school2, censusDataSet, 60000);
        schoolCensusValues.add(censusValue2);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "40000");
        assertSimpleCell(rval.get("Average Salary")[2], "60000");

    }

    public void testPopulateStructsBasic() {
        // Basic tests of populateStructs
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CompareConfigStruct[]> rval;
        School school1 = getSchool(1, SchoolType.PUBLIC);
        schools.add(school1);
        School school2 = getSchool(2, SchoolType.PRIVATE);
        schools.add(school2);

        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2009);
        CensusDataSet censusDataSet2 = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO,2008);
        CompareLabel label2 = getLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet2,label2);
        CompareLabel label= getLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet,label);

        SchoolCensusValue censusValue1 = getSchoolCensusValue(school1, censusDataSet, 40000);
        schoolCensusValues.add(censusValue1);

        // test that no cell is created for the school2 cell since there is no data
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "40000");
        assertNull("Expect third cell to be null as its school type has no data.", rval.get("Average Salary")[2]);

        // this census data set has values for all schools
        SchoolCensusValue censusValue2 = getSchoolCensusValue(school2, censusDataSet2, 80000);
        schoolCensusValues.add(censusValue2);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "40000");
        assertSimpleCell(rval.get("Average Salary")[2], "80000");

        // try in different order to double check
        Collections.reverse(schoolCensusValues);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        assertHeaderCell(rval.get("Average Salary")[0], "Average Salary");
        assertSimpleCell(rval.get("Average Salary")[1], "40000");
        assertSimpleCell(rval.get("Average Salary")[2], "80000");
    }

    public void testPopulateStructsWithBreakdown() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareLabel>();
        Map<String, CompareConfigStruct[]> rval;

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

        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);
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
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToRowLabelMap, _labelToCompareLabelInfoMap);

        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Student Ethnicity").length);
        assertBreakdownCell(rval.get("Student Ethnicity")[2],0,"White","20%");
        assertBreakdownCell(rval.get("Student Ethnicity")[2],1,"Asian","40%");
        assertBreakdownCell(rval.get("Student Ethnicity")[2],2,"Hispanic","40%");

    }

    public void testGetCensusDataSetsNoConfigs() {
        // expect nothing to happen
        List<CompareConfig> compareConfigs = new ArrayList<CompareConfig>();
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (State.CA, compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
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

        Set<CensusDataType> expectedDataTypes = new HashSet<CensusDataType>(1);
        expectedDataTypes.add(CensusDataType.STUDENTS_ETHNICITY);

        expect(_censusDataSetDao.findDataSets(eq(State.CA), eq(expectedDataTypes), (Set<Integer>)isNull()))
                .andReturn(null);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (State.CA, compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
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
        Set<CensusDataType> expectedDataTypes = new HashSet<CensusDataType>(1);
        expectedDataTypes.add(CensusDataType.STUDENTS_ETHNICITY);

        List<CensusDataSet> expectedListOfDataSets = new ArrayList<CensusDataSet>();
        expectedListOfDataSets.add(new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009));
        expect(_censusDataSetDao.findDataSets(eq(State.CA), eq(expectedDataTypes), (Set<Integer>) isNull()))
                .andReturn(expectedListOfDataSets);
        Set<Integer> expectedDataTypeIds = new HashSet<Integer>(1);
        expectedDataTypeIds.add(CensusDataType.STUDENTS_ETHNICITY.getId());
        expect(_compareLabelDao
                       .findLabels(eq(State.CA), eq(TAB_NAME), eq(expectedDataTypeIds)))
                .andReturn(null);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (State.CA, compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
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
        Set<CensusDataType> expectedDataTypes = new HashSet<CensusDataType>(1);
        expectedDataTypes.add(CensusDataType.STUDENTS_ETHNICITY);

        List<CensusDataSet> expectedListOfDataSets = new ArrayList<CensusDataSet>();
        expectedListOfDataSets.add(new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009));
        expect(_censusDataSetDao.findDataSets(eq(State.CA), eq(expectedDataTypes), (Set<Integer>) isNull()))
                .andReturn(expectedListOfDataSets);
        CompareLabel label = new CompareLabel();
        label.setRowLabel("Ethnicity");
        label.setDataTypeId(CensusDataType.STUDENTS_ETHNICITY.getId());
        Set<Integer> expectedDataTypeIds = new HashSet<Integer>(1);
        expectedDataTypeIds.add(CensusDataType.STUDENTS_ETHNICITY.getId());
        List<CompareLabel> expectedListOfLabels = new ArrayList<CompareLabel>(1);
        expectedListOfLabels.add(label);
        expect(_compareLabelDao
                .findLabels(eq(State.CA), eq(TAB_NAME), eq(expectedDataTypeIds)))
                .andReturn(expectedListOfLabels);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (State.CA, compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        assertNotNull(rval);
        assertEquals(1, rval.size());
        assertSame(expectedListOfDataSets.get(0), rval.get(0));
        assertEquals(1, censusDataSetCompareLabelMap.size());
        assertSame(label, censusDataSetCompareLabelMap.get(expectedListOfDataSets.get(0)));
        assertEquals(1, rowLabelToOrder.size());
        assertEquals(81, rowLabelToOrder.get("Ethnicity").intValue());
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
        compareConfig.setGrade(Grade.G_3);
        compareConfig.setLevelCode(LevelCode.ELEMENTARY);
        compareConfig.setYear(2009);
        compareConfig.setSubject(Subject.ENGLISH);
        compareConfigs.add(compareConfig);
        Map<CensusDataSet, CompareLabel> censusDataSetCompareLabelMap = new HashMap<CensusDataSet, CompareLabel>();
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        Map<CensusDataSet, SchoolType> censusDataSetSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();

        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009);
        censusDataSet.setBreakdownId(5);
        censusDataSet.setGradeLevels(Grades.createGrades(Grade.G_3));
        censusDataSet.setLevelCode(LevelCode.ELEMENTARY);
        censusDataSet.setYear(2009);
        censusDataSet.setSubject(Subject.ENGLISH);
        CensusDataSet overrideCensusDataSet = new CensusDataSet(CensusDataType.STUDENTS_ETHNICITY, 2009);
        overrideCensusDataSet.setBreakdownId(5);
        overrideCensusDataSet.setGradeLevels(Grades.createGrades(Grade.G_3));
        overrideCensusDataSet.setLevelCode(LevelCode.ELEMENTARY);
        overrideCensusDataSet.setYear(0);
        overrideCensusDataSet.setSubject(Subject.ENGLISH);
        Set<CensusDataType> expectedDataTypes = new HashSet<CensusDataType>(1);
        expectedDataTypes.add(CensusDataType.STUDENTS_ETHNICITY);
        Set<Integer> expectedYears = new HashSet<Integer>(2);
        expectedYears.add(2009);
        expectedYears.add(0);

        List<CensusDataSet> expectedListOfDataSets = new ArrayList<CensusDataSet>();
        expectedListOfDataSets.add(censusDataSet);
        expectedListOfDataSets.add(overrideCensusDataSet);
        IBreakdownDao censusBreakdownDao = createStrictMock(IBreakdownDao.class);
        expect(censusBreakdownDao.getCensusBreakdown(5)).andReturn(new Breakdown(5)).times(3); // twice for 2009, once for 0
        replay(censusBreakdownDao);
        censusDataSet.setCensusBreakdownDao(censusBreakdownDao);
        overrideCensusDataSet.setCensusBreakdownDao(censusBreakdownDao);
        expect(_censusDataSetDao.findDataSets(eq(State.CA), eq(expectedDataTypes), eq(expectedYears)))
                .andReturn(expectedListOfDataSets);
        CompareLabel label = new CompareLabel();
        label.setRowLabel("Ethnicity");
        label.setDataTypeId(CensusDataType.STUDENTS_ETHNICITY.getId());
        label.setBreakdownId(5);
        label.setGrade(Grade.G_3);
        label.setLevelCode(LevelCode.ELEMENTARY);
        label.setSubject(Subject.ENGLISH);
        Set<Integer> expectedDataTypeIds = new HashSet<Integer>(1);
        expectedDataTypeIds.add(CensusDataType.STUDENTS_ETHNICITY.getId());
        List<CompareLabel> expectedListOfLabels = new ArrayList<CompareLabel>(1);
        expectedListOfLabels.add(label);
        expect(_compareLabelDao
                .findLabels(eq(State.CA), eq(TAB_NAME), eq(expectedDataTypeIds)))
                .andReturn(expectedListOfLabels);
        replayAllMocks();
        List<CensusDataSet> rval = _controller.getCensusDataSets
                (State.CA, compareConfigs, censusDataSetCompareLabelMap, rowLabelToOrder, censusDataSetSchoolTypeMap);
        verifyAllMocks();
        verify(censusBreakdownDao);
        assertNotNull(rval);
        assertEquals("Expect both manual and regular data sets", 2, rval.size());
        assertSame(censusDataSet, rval.get(0));
        assertSame(overrideCensusDataSet, rval.get(1));
        assertEquals(2, censusDataSetCompareLabelMap.size());
        assertSame(label, censusDataSetCompareLabelMap.get(censusDataSet));
        assertSame(label, censusDataSetCompareLabelMap.get(overrideCensusDataSet));
        assertEquals(1, rowLabelToOrder.size());
        assertEquals(81, rowLabelToOrder.get("Ethnicity").intValue());
        assertEquals(0, censusDataSetSchoolTypeMap.size());
    }

    public void testSortRows(){
        Map<String, CompareConfigStruct[]> rowLabelToCells = new HashMap<String, CompareConfigStruct[]>();
        CompareConfigStruct[] cs1 = new CompareConfigStruct[1];
        cs1[0] = new CompareConfigStruct();
        cs1[0].setHeaderText("Average Salary");
        rowLabelToCells.put("Average Salary",cs1);
        CompareConfigStruct[] cs2 = new CompareConfigStruct[2];
        cs2[0] = new CompareConfigStruct();
        cs2[0].setHeaderText("Student Ethnicity");
        cs2[1] = new CompareConfigStruct();
        List<BreakdownNameValue> breakdowns = new ArrayList<BreakdownNameValue>();
        breakdowns.add(getBreakdown("White", "30%", 30f));
        breakdowns.add(getBreakdown("Asian", "50%", 50f));
        breakdowns.add(getBreakdown("Black", "20%", 20f));
        cs2[1].setBreakdownList(breakdowns);
        rowLabelToCells.put("Student Ethnicity",cs2);
        CompareConfigStruct[] cs3 = new CompareConfigStruct[1];
        cs3[0] = new CompareConfigStruct();
        cs3[0].setHeaderText("Average years Teaching");
        rowLabelToCells.put("Average years Teaching",cs3);
        CompareConfigStruct[] cs4 = new CompareConfigStruct[2];
        cs4[0] = new CompareConfigStruct();
        cs4[0].setHeaderText("Students per teacher");
        cs4[1] = new CompareConfigStruct();
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
        
        List<CompareConfigStruct[]> rval = _controller.sortRows(rowLabelToCells,rowLabelToOrder);
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

    public void testSortRowsHomeLanguage() {
        Map<String, CompareConfigStruct[]> rowLabelToCells = new HashMap<String, CompareConfigStruct[]>();
        CompareConfigStruct[] cs = new CompareConfigStruct[2];
        cs[0] = new CompareConfigStruct();
        cs[0].setHeaderText("Home Language");
        rowLabelToCells.put("Home Language",cs);

        Map<String, Integer> rowLabelToOrder = new HashMap<String,Integer>();
        rowLabelToOrder.put("Home Language",1);

        cs[1] = new CompareConfigStruct();
        List<BreakdownNameValue> breakdowns = new ArrayList<BreakdownNameValue>();
        breakdowns.add(getBreakdown("Australian", "5%", 5f)); // mate!
        breakdowns.add(getBreakdown("Spanish", "15%", 15f)); // el capitan
        breakdowns.add(getBreakdown("Canadian", "4%", 4f)); // eh?
        breakdowns.add(getBreakdown("French", "7%", 7f)); // bon apetit
        breakdowns.add(getBreakdown("Italian", "6%", 6f)); // manicotti, amore mio!
        breakdowns.add(getBreakdown("Mandarin", "30%", 30f)); // gen nide qian niu pengyou shuo hua bu keyi. Bu keyi!! *screams and throws fork*
        cs[1].setBreakdownList(breakdowns);
        cs[1].setBreakdownValueMinimum(5);

        List<CompareConfigStruct[]> rval = _controller.sortRows(rowLabelToCells, rowLabelToOrder);
        assertNotNull(rval);
        assertEquals(1, rval.size());
        CompareConfigStruct languages = rval.get(0)[1];
        assertEquals(4, languages.getBreakdownList().size());
        assertEquals("Mandarin", languages.getBreakdownList().get(0).getName());
        assertEquals("Spanish", languages.getBreakdownList().get(1).getName());
        assertEquals("French", languages.getBreakdownList().get(2).getName());
        assertEquals("Italian", languages.getBreakdownList().get(3).getName());
    }

    public void testSortRowsRegressionNullCell(){
        Map<String, CompareConfigStruct[]> rowLabelToCells = new HashMap<String, CompareConfigStruct[]>();
        CompareConfigStruct[] cs1 = new CompareConfigStruct[1];
        cs1[0] = new CompareConfigStruct();
        cs1[0].setHeaderText("Average Salary");
        rowLabelToCells.put("Average Salary",cs1);
        CompareConfigStruct[] cs2 = new CompareConfigStruct[2];
        cs2[0] = new CompareConfigStruct();
        cs2[0].setHeaderText("Student Ethnicity");
        cs2[1] = new CompareConfigStruct();
        List<BreakdownNameValue> breakdowns = new ArrayList<BreakdownNameValue>();
        breakdowns.add(getBreakdown("White", "30%", 30f));
        breakdowns.add(getBreakdown("Asian", "50%", 50f));
        breakdowns.add(getBreakdown("Black", "20%", 20f));
        cs2[1].setBreakdownList(breakdowns);
        rowLabelToCells.put("Student Ethnicity",cs2);
        CompareConfigStruct[] cs3 = new CompareConfigStruct[1];
        cs3[0] = new CompareConfigStruct();
        cs3[0].setHeaderText("Average years Teaching");
        rowLabelToCells.put("Average years Teaching",cs3);
        CompareConfigStruct[] cs4 = new CompareConfigStruct[3];
        cs4[0] = new CompareConfigStruct();
        cs4[0].setHeaderText("Students per teacher");
        cs4[1] = new CompareConfigStruct();
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
        rowLabelToOrder.put("Student Ethnicity", 3);
        rowLabelToOrder.put("Average years Teaching", 4);

        List<CompareConfigStruct[]> rval = _controller.sortRows(rowLabelToCells,rowLabelToOrder);
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

    public void testGetValueAsText() {
        SchoolCensusValue value = new SchoolCensusValue();

        value.setValueText("text value");
        assertEquals("text value", _controller.getValueAsText(value));

        value.setValueText("15.15");
        assertEquals("15.15", _controller.getValueAsText(value));

        CensusDataSet dataSet = new CensusDataSet(CensusDataType.TEACHERS_PERCENT_IN_FIRST_YEAR, 2010); // PERCENT
        value.setDataSet(dataSet);
        value.setValueText(null);
        value.setValueFloat(100f);
        assertEquals("100%", _controller.getValueAsText(value));
        value.setValueFloat(99.51f);
        assertEquals("100%", _controller.getValueAsText(value));
        value.setValueFloat(99.49f);
        assertEquals("99%", _controller.getValueAsText(value));
        value.setValueFloat(1f);
        assertEquals("1%", _controller.getValueAsText(value));
        value.setValueFloat(0.9f);
        assertEquals("&lt;1%", _controller.getValueAsText(value));

        dataSet = new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO, 2010); // NUMBER
        value = new SchoolCensusValue(); // to prevent exception on setting dataSet twice
        value.setDataSet(dataSet);
        value.setValueFloat(99.9f);
        assertEquals("100", _controller.getValueAsText(value));
    }

    public void testSchoolCensusValueSorting() {
        // Test - just check SchoolCensusValue date sorting

        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        School s = new School();
        s.setId(1);
        SchoolCensusValue scv1 = new SchoolCensusValue(s,  new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO, 2008 ) );
        schoolCensusValues.add(scv1);
        SchoolCensusValue scv2 = new SchoolCensusValue(s,  new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO, 2012 ) );
        schoolCensusValues.add(scv2);
        SchoolCensusValue scv3 = new SchoolCensusValue(s,  new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO, 2011 ) );
        schoolCensusValues.add(scv3);
        SchoolCensusValue scv4 = new SchoolCensusValue(s,  new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO, 2010 ) );
        schoolCensusValues.add(scv4);

        Collections.sort(schoolCensusValues, SCHOOL_CENSUS_VALUE_DESCENDING);
        assertEquals(4, schoolCensusValues.size());
        assertEquals(scv2, schoolCensusValues.get(0));
        assertEquals(scv3, schoolCensusValues.get(1));
        assertEquals(scv4, schoolCensusValues.get(2));
        assertEquals(scv1, schoolCensusValues.get(3));

        // test adding modified dates
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 10, 5, 0, 0, 0);
        SchoolCensusValue scv5 = new SchoolCensusValue(s,  new CensusDataSet(CensusDataType.STUDENT_TEACHER_RATIO, 0 ) );
        scv5.setModified( cal.getTime() );
        schoolCensusValues.add(scv5);
        Collections.sort(schoolCensusValues, SCHOOL_CENSUS_VALUE_DESCENDING);
        assertEquals(scv2, schoolCensusValues.get(0));
        assertEquals(scv5, schoolCensusValues.get(1));
        assertEquals(scv3, schoolCensusValues.get(2));
        assertEquals(scv4, schoolCensusValues.get(3));
        assertEquals(scv1, schoolCensusValues.get(4));

    }

    private BreakdownNameValue getBreakdown(String name, String value, Float floatValue) {
        BreakdownNameValue breakdown = new BreakdownNameValue();
        breakdown.setName(name);
        breakdown.setValue(value);
        breakdown.setFloatValue(floatValue);
        return breakdown;
    }

    private void assertHeaderCell(CompareConfigStruct cell, String headerText) {
        assertNotNull(cell);
        assertTrue(cell.getIsHeaderCell());
        assertEquals(headerText, cell.getHeaderText());
    }

    private void assertSimpleCell(CompareConfigStruct cell, String cellValue) {
        assertNotNull(cell);
        assertTrue(cell.getIsSimpleCell());
        assertFalse(cell.getIsHeaderCell());
        assertEquals(cellValue, cell.getValue());
    }

    private void assertBreakdownCell(CompareConfigStruct cell,int index, String breakdownName,String breakdownValue) {
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
        return getSchoolCensusValue(school, dataSet, value, null);
    }

    private SchoolCensusValue getSchoolCensusValue(School school, CensusDataSet dataSet, float value, Date modified) {
        SchoolCensusValue rval = new SchoolCensusValue(school, dataSet);
        rval.setValueFloat(value);
        if( modified != null ) {
            rval.setModified(modified);
        }
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