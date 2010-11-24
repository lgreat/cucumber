package gs.web.compare;

import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.census.*;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareStudentTeacherController extends BaseControllerTestCase {
    private CompareStudentTeacherController _controller;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareStudentTeacherController();

        _censusDataSetDao = createStrictMock(ICensusDataSetDao.class);
        _censusInfo = createStrictMock(ICensusInfo.class);

        _controller.setSuccessView("success");
        _controller.setCensusDataSetDao(_censusDataSetDao);
        _controller.setCensusInfo(_censusInfo);
    }

    private void replayAllMocks() {
        replayMocks(_censusDataSetDao, _censusInfo);
    }

    private void verifyAllMocks() {
        verifyMocks(_censusDataSetDao, _censusInfo);
    }

//    private void resetAllMocks() {
//        resetMocks(_censusDataSetDao, _censusInfo);
//    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertSame(_censusDataSetDao, _controller.getCensusDataSetDao());
        assertSame(_censusInfo, _controller.getCensusInfo());
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

    public void testPopulateStructs() {
        List<School> schools = new ArrayList<School>();
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap = new HashMap<CensusDataSet, SchoolType>();
        Map<CensusDataSet, CompareStudentTeacherController.CompareLabel> censusDataSetToRowLabelMap =
                new HashMap<CensusDataSet, CompareStudentTeacherController.CompareLabel>();
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
        School school1 = getSchoolWithId(1);
        schools.add(school1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull("Expect empty map", rval);
        assertTrue("Expect empty map", rval.isEmpty());

        //add one school and assert the cells.
        censusValue1 = new SchoolCensusValue();
        censusValue1.setSchool(school1);
        CensusDataSet censusDataSet = new CensusDataSet(CensusDataType.AVERAGE_SALARY,2009);
        CompareStudentTeacherController.CompareLabel label= new CompareStudentTeacherController.CompareLabel();
        label.setRowLabel("Average Salary");
        censusDataSetToRowLabelMap.put(censusDataSet,label);
        censusValue1.setDataSet(censusDataSet);
        censusValue1.setValueFloat(40000F);
        schoolCensusValues.add(censusValue1);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(2,rval.get("Average Salary").length);
        CensusStruct firstCell = rval.get("Average Salary")[0];
        assertTrue(firstCell.getIsHeaderCell());
        assertEquals("Average Salary",firstCell.getHeaderText());
        CensusStruct secondCell = rval.get("Average Salary")[1];
        assertFalse(secondCell.getIsHeaderCell());
        assertTrue(secondCell.getIsSimpleCell());
        assertEquals("40000",secondCell.getValue());

        //add two schools and assert the order of the cells.
        SchoolCensusValue censusValue2 = new SchoolCensusValue();
        School school2 = getSchoolWithId(2);
        schools.add(school2);
        censusValue2.setSchool(school2);
        censusValue2.setDataSet(censusDataSet);
        censusValue2.setValueFloat(60000F);
        schoolCensusValues.add(censusValue2);
        rval = _controller.populateStructs(schools, schoolCensusValues, censusDataSetToSchoolTypeMap, censusDataSetToRowLabelMap);
        assertNotNull(rval);
        assertFalse(rval.isEmpty());
        assertEquals(1,rval.size());
        assertEquals(3,rval.get("Average Salary").length);
        firstCell = rval.get("Average Salary")[0];
        assertTrue(firstCell.getIsHeaderCell());
        secondCell = rval.get("Average Salary")[1];
        assertFalse(secondCell.getIsHeaderCell());
        assertTrue(secondCell.getIsSimpleCell());
        assertEquals("40000",secondCell.getValue());
        CensusStruct thirdCell = rval.get("Average Salary")[2];
        assertFalse(thirdCell.getIsHeaderCell());
        assertTrue(thirdCell.getIsSimpleCell());
        assertEquals("60000",thirdCell.getValue());
    }

    public School getSchoolWithId(int Id) {
        School school = new School();
        school.setId(Id);
        return school;

    }


}
