package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.breakdown.EthnicityDaoJava;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.test.Subject;
import gs.data.util.ListUtils;
import gs.web.SlowTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml", "classpath:gs/data/applicationContext-data.xml", "classpath:applicationContext.xml", "classpath:annotated-tests.xml"})
@Category(SlowTest.class)
public class SchoolProfileCensusHelperTest {

    @Autowired
    SchoolProfileCensusHelper _schoolProfileCensusHelper;

    ICensusDataSetDao _censusDataSetDao = createStrictMock(ICensusDataSetDao.class);
    ICensusDataSchoolValueDao _censusDataSchoolValueDao = createStrictMock(ICensusDataSchoolValueDao.class);


    @Before
    public void setUp() throws Exception {

        ReflectionTestUtils.setField(_schoolProfileCensusHelper, "_censusDataSetDao", _censusDataSetDao);
        ReflectionTestUtils.setField(_schoolProfileCensusHelper, "_censusDataSchoolValueDao", _censusDataSchoolValueDao);

        reset(_censusDataSetDao, _censusDataSchoolValueDao);
    }

    @Test
    public void testDoNothing() {
        System.out.println(System.nanoTime());
    }

    public List<ICensusDataConfigEntry> getCensusDataConfigEntries() {
        List<ICensusDataConfigEntry> entries = new ArrayList<ICensusDataConfigEntry>();
        CensusDataConfigEntry entry = new CensusDataConfigEntry();
        entry.setId(1);
        entry.setGroupId(1);
        entry.setDataType(CensusDataType.STUDENT_TEACHER_RATIO);
        entry.setSchoolType(SchoolType.PUBLIC.getSchoolTypeName());
        entry.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        entry.setHasSchoolData(true);
        entry.setHasDistrictData(true);
        entry.setHasStateData(true);
        entries.add(entry);

        entry = new CensusDataConfigEntry();
        entry.setId(2);
        entry.setGroupId(1);
        entry.setDataType(CensusDataType.PARENT_INVOLVEMENT);
        entry.setSchoolType(SchoolType.PUBLIC.getSchoolTypeName());
        entry.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        entry.setHasSchoolData(false);
        entry.setHasDistrictData(true);
        entry.setHasStateData(true);
        entries.add(entry);

        entry = new CensusDataConfigEntry();
        entry.setId(3);
        entry.setGroupId(1);
        entry.setDataType(CensusDataType.TOTAL_PER_PUPIL_SPENDING);
        entry.setSchoolType(SchoolType.PUBLIC.getSchoolTypeName());
        entry.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        entry.setHasSchoolData(false);
        entry.setHasDistrictData(false);
        entry.setHasStateData(true);
        entries.add(entry);

        entry = new CensusDataConfigEntry();
        entry.setId(4);
        entry.setGroupId(1);
        entry.setDataType(CensusDataType.VANDALISM_INCIDENTS);
        entry.setSchoolType(SchoolType.PUBLIC.getSchoolTypeName());
        entry.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        entry.setHasSchoolData(false);
        entry.setHasDistrictData(false);
        entry.setHasStateData(false);
        entries.add(entry);

        return entries;
    }

    /*@Test
    public void testGetEthnicityLabelMap() {
        Breakdown breakdown1 = new Breakdown();
        breakdown1.setEthnicity(new EthnicityDaoJava().getEthnicity(1));
        Breakdown breakdown2 = new Breakdown();
        breakdown2.setEthnicity(new EthnicityDaoJava().getEthnicity(2));
        Breakdown breakdown3 = new Breakdown();
        breakdown3.setEthnicity(new EthnicityDaoJava().getEthnicity(3));
        Map<Integer, CensusDataSet> censusDataSetMap = new HashMap<Integer, CensusDataSet>();
        CensusDataSet cds1 = getCensusDataSet(1000001);
        cds1.setDataType(CensusDataType.STUDENTS_ETHNICITY);
        cds1.setBreakdownOnly(breakdown1);
        CensusDataSet cds2 = getCensusDataSet(1000002);
        cds2.setDataType(CensusDataType.STUDENTS_ETHNICITY);
        cds2.setBreakdownOnly(breakdown2);
        CensusDataSet cds3 = getCensusDataSet(1000003);
        cds3.setDataType(CensusDataType.STUDENTS_ETHNICITY);
        cds3.setBreakdownOnly(breakdown3);
        SchoolCensusValue scv1 = new SchoolCensusValue();
        SchoolCensusValue scv2 = new SchoolCensusValue();
        SchoolCensusValue scv3 = new SchoolCensusValue();
        scv1.setValueFloat(50f);
        scv2.setValueFloat(50f);
        scv3.setValueFloat(51f);
        cds1.setSchoolData(newHashSet(scv1));
        cds2.setSchoolData(newHashSet(scv2));
        cds3.setSchoolData(newHashSet(scv3));

        censusDataSetMap.put(1000001, cds1);
        censusDataSetMap.put(1000002, cds2);
        censusDataSetMap.put(1000003, cds3);


        Map<String,String> result = _schoolProfileCensusHelper.getEthnicityLabelValueMap(censusDataSetMap);

        assertEquals("expect resulting map to have same number of entries as input map", 3, result.size());
        int pos = 0;
        for (Map.Entry<String,String> entry : result.entrySet()) {
            if (pos == 0) {
                assertEquals("expect first item in map to have largest value", "51%", entry.getValue());
            } else {
                assertEquals("expect remaining items in map to be a tie", "50%", entry.getValue());
            }
            pos++;
        }
    }*/

    public static <T> HashSet<T> newHashSet(T... items) {
        HashSet set = new HashSet<T>(items.length);
        for (T item : items) {
            set.add(item);
        }
        return set;
    }

    public Set<Integer> getDataTypeIds() {
        Set<Integer> ids = new HashSet<Integer>();
        ids.add(CensusDataType.STUDENT_TEACHER_RATIO.getId());
        return ids;
    }

    public School getSchool() {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setName("Test school");
        return school;
    }

    public CensusDataSet getCensusDataSet(Integer id) {
        CensusDataSet censusDataSet = new CensusDataSet();
        censusDataSet.setId(id);
        censusDataSet.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        censusDataSet.setSubject(Subject.SCIENCE);
        censusDataSet.setBreakdown(null);
        censusDataSet.setDataType(CensusDataType.STUDENT_TEACHER_RATIO);
        censusDataSet.setYear(2010);
        Set<CensusDescription> censusDescriptions = new HashSet<CensusDescription>();
        censusDescriptions.add(CensusDescription.testInstance());
        censusDataSet.setCensusDescription(censusDescriptions);
        return censusDataSet;
    }

    public List<CensusDataSet> getCensusDataSets(int count) {
        List<CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();

        for (int i = 0; i < count; i++) {
            CensusDataSet censusDataSet = getCensusDataSet(1000000 + i);
            censusDataSets.add(censusDataSet);
        }

        return censusDataSets;
    }

    public CensusStateConfig censusStateConfig() {
        CensusStateConfig config = new CensusStateConfig(State.CA, getCensusDataConfigEntries());
        return config;
    }

    @Test
    public void testGetCensusDataSets() throws Exception {
        CensusStateConfig config = censusStateConfig();

        List<CensusDataSet> censusDataSets = getCensusDataSets(3);
        School school = getSchool();

        expect(_censusDataSetDao.findLatestDataSetsForDataTypes(State.CA, config.allDataTypeIds(), school)).andReturn(censusDataSets);

        replay(_censusDataSetDao);

        Map<Integer, CensusDataSet> censusDataSetMap = _schoolProfileCensusHelper.getCensusDataSets(State.CA, config.allDataTypeIds(), school);

        verify(_censusDataSetDao);

        assertTrue("Expect returned map to contain", censusDataSetMap.values().containsAll(censusDataSets));
    }

    @Test
    public void testDisplayRowSortOrderComparator() {
        SchoolCensusValue schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setValueFloat(5.0f);
        DistrictCensusValue districtCensusValue = new DistrictCensusValue();
        districtCensusValue.setValueFloat(5.0f);
        StateCensusValue stateCensusValue = new StateCensusValue();
        stateCensusValue.setValueFloat(5.0f);

        SchoolProfileStatsDisplayRow row1 = new SchoolProfileStatsDisplayRow(1l, 1, 1, "first", schoolCensusValue, districtCensusValue, stateCensusValue, null, 2012, false, 1);
        SchoolProfileStatsDisplayRow row2 = new SchoolProfileStatsDisplayRow(1l, 2, 1, "first", schoolCensusValue, districtCensusValue, stateCensusValue, null, 2012, false, 2);
        SchoolProfileStatsDisplayRow row3 = new SchoolProfileStatsDisplayRow(1l, 3, 1, "first", schoolCensusValue, districtCensusValue, stateCensusValue, null, 2012, false, 3);
        SchoolProfileStatsDisplayRow row4 = new SchoolProfileStatsDisplayRow(1l, 4, 1, "first", schoolCensusValue, districtCensusValue, stateCensusValue, null, 2012, false, 4);
        SchoolProfileStatsDisplayRow row5 = new SchoolProfileStatsDisplayRow(1l, 4, 1, "first", schoolCensusValue, districtCensusValue, stateCensusValue, null, 2012, false, null);

        List<SchoolProfileStatsDisplayRow> rows = ListUtils.newArrayList(row3, row4, row5, row1, row2);
        List<SchoolProfileStatsDisplayRow> expectedRows = ListUtils.newArrayList( row1, row2, row3, row4, row5 );

        assertFalse("Expect rows to unsorted", rows.equals(expectedRows));
        Collections.sort(rows, SchoolProfileCensusHelper.DISPLAY_ROW_SORT_ORDER_COMPARATOR);
        assertEquals("Expect rows to have been correctly sorted", expectedRows, rows);
    }

    @Test
    public void testSchoolValueDescendingComparator() {
        DistrictCensusValue districtCensusValue = new DistrictCensusValue();
        districtCensusValue.setValueFloat(5.0f);
        StateCensusValue stateCensusValue = new StateCensusValue();
        stateCensusValue.setValueFloat(5.0f);

        SchoolProfileStatsDisplayRow row1 = new SchoolProfileStatsDisplayRow(1l, 1, 1, "first", getASchoolValue(9f), districtCensusValue, stateCensusValue, null, 2012, false, null);
        SchoolProfileStatsDisplayRow row2 = new SchoolProfileStatsDisplayRow(1l, 2, 1, "first", getASchoolValue(8f), districtCensusValue, stateCensusValue, null, 2012, false, 1);
        SchoolProfileStatsDisplayRow row3 = new SchoolProfileStatsDisplayRow(1l, 3, 1, "first", getASchoolValue(7f), districtCensusValue, stateCensusValue, null, 2012, false, 2);
        SchoolProfileStatsDisplayRow row4 = new SchoolProfileStatsDisplayRow(1l, 4, 1, "first", getASchoolValue(6f), districtCensusValue, stateCensusValue, null, 2012, false, 3);
        SchoolProfileStatsDisplayRow row5 = new SchoolProfileStatsDisplayRow(1l, 4, 1, "first", getASchoolValue(5f), districtCensusValue, stateCensusValue, null, 2012, false, 4);

        List<SchoolProfileStatsDisplayRow> rows = ListUtils.newArrayList(row3, row4, row5, row1, row2);
        List<SchoolProfileStatsDisplayRow> expectedRows = ListUtils.newArrayList( row1, row2, row3, row4, row5 );

        assertFalse("Expect rows to unsorted", rows.equals(expectedRows));
        Collections.sort(rows, SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR);
        assertEquals("Expect rows to have been correctly sorted", expectedRows, rows);
    }

    @Test
    public void testGetComparator() throws Exception {
        assertEquals(SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR, _schoolProfileCensusHelper.getComparator(CensusGroup.Student_Ethnicity));
        assertEquals(SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR, _schoolProfileCensusHelper.getComparator(CensusGroup.Home_Languages_of_English_Learners));
        assertEquals(SchoolProfileCensusHelper.DISPLAY_ROW_SORT_ORDER_COMPARATOR, _schoolProfileCensusHelper.getComparator(CensusGroup.Attendance));
    }

    @Test
    public void testGetSortConfig() throws Exception {
        Map<CensusGroup, Comparator<SchoolProfileStatsDisplayRow>> map = _schoolProfileCensusHelper.getGroupSortConfig();
        assertEquals(SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR, map.get(CensusGroup.Student_Ethnicity));
        assertEquals(SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR, map.get(CensusGroup.Home_Languages_of_English_Learners));
        assertNull(map.get(CensusGroup.Attendance));
    }

    @Test
    public void testShowRow() throws Exception {
        SchoolProfileStatsDisplayRow row = new SchoolProfileStatsDisplayRow(
            1l, CensusDataType.STUDENTS_ETHNICITY.getId(), 1, "blah", getASchoolValue(10f), getADistrictValue(20f),
            getAStateValue(30f), null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district,state) values for ethncitiy", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.STUDENTS_ETHNICITY.getId(), 1, "blah", getASchoolValue(10f), null,
                null, null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district,state) values for ethncitiy", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.STUDENTS_ETHNICITY.getId(), 1, "blah", null, getADistrictValue(20f),
                null, null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district,state) values for ethncitiy", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.STUDENTS_ETHNICITY.getId(), 1, "blah", null, null,
                getAStateValue(30f), null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district,state) values for ethncitiy", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.STUDENTS_ETHNICITY.getId(), 1, "blah", null, null,
                null, null, null, false, null );
        assertFalse("Expect false for showRow since it doesnt have at least one of (school,district,state) values for ethncitiy", _schoolProfileCensusHelper.showRow(row));


        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.RENEWAL_STATUS.getId(), 1, "blah", getASchoolValue(10f), getADistrictValue(20f),
                getAStateValue(30f), null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district) values", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.RENEWAL_STATUS.getId(), 1, "blah", getASchoolValue(10f), null,
                null, null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district) values", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.RENEWAL_STATUS.getId(), 1, "blah", null, getADistrictValue(20f),
                null, null, null, false, null );
        assertTrue("Expect true for showRow since it has at least one of (school,district) values", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.RENEWAL_STATUS.getId(), 1, "blah", null, null,
                getAStateValue(30f), null, null, false, null );
        assertFalse("Expect false for showRow since it doesnt have at least one of (school,district) values", _schoolProfileCensusHelper.showRow(row));

        row = new SchoolProfileStatsDisplayRow(
                1l, CensusDataType.RENEWAL_STATUS.getId(), 1, "blah", null, null,
                null, null, null, false, null );
        assertFalse("Expect false for showRow since it doesnt have at least one of (school,district) values", _schoolProfileCensusHelper.showRow(row));
    }

    @Test
    public void testSortEthnicityValues() {
        SchoolCensusValue schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setValueFloat(5.0f);
        DistrictCensusValue districtCensusValue = new DistrictCensusValue();
        districtCensusValue.setValueFloat(5.0f);
        StateCensusValue stateCensusValue = new StateCensusValue();
        stateCensusValue.setValueFloat(5.0f);

        try {
            _schoolProfileCensusHelper.sortDisplayRows(null);
        } catch (Exception e) {
            fail("Unexpected exception when passing null list to sort. Should handle it gracefully. " + e);
        }

        ArrayList<SchoolProfileStatsDisplayRow> statsRows = new ArrayList<SchoolProfileStatsDisplayRow>();
        GroupOfStudentTeacherViewRows rows = new GroupOfStudentTeacherViewRows(CensusGroup.Student_Ethnicity, statsRows);
        Map<CensusGroup, GroupOfStudentTeacherViewRows> map = new HashMap<CensusGroup, GroupOfStudentTeacherViewRows>();
        map.put(CensusGroup.Student_Ethnicity, rows);
        _schoolProfileCensusHelper.sortDisplayRows(map);

        assertEquals(0, statsRows.size());

        SchoolProfileStatsDisplayRow row1 = new SchoolProfileStatsDisplayRow(1l, 1, 1, "first", schoolCensusValue, districtCensusValue, stateCensusValue, null, 2012, false, null);
        statsRows.add(row1);
        _schoolProfileCensusHelper.sortDisplayRows(map);
        assertEquals(1, statsRows.size());
    }

    private SchoolCensusValue getASchoolValue(Float value) {
        SchoolCensusValue censusValue = new SchoolCensusValue();
        censusValue.setValueFloat(value);
        return censusValue;
    }

    private DistrictCensusValue getADistrictValue(Float value) {
        DistrictCensusValue censusValue = new DistrictCensusValue();
        censusValue.setValueFloat(value);
        return censusValue;
    }

    private StateCensusValue getAStateValue(Float value) {
        StateCensusValue censusValue = new StateCensusValue();
        censusValue.setValueFloat(value);
        return censusValue;
    }

}
