package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.test.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml", "classpath:gs/data/applicationContext-data.xml", "classpath:annotated-tests.xml"})
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

   /* @Test
    public void testSplitDataSets() throws Exception {
        CensusStateConfig config = censusStateConfig();

        Map<Integer, CensusDataSet> censusDataSetMap = new HashMap<Integer, CensusDataSet>();
        CensusDataSet censusDataSet = getCensusDataSet(1000000);
        censusDataSet.setDataType(CensusDataType.STUDENT_TEACHER_RATIO);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        censusDataSet = getCensusDataSet(1000001);
        censusDataSet.setDataType(CensusDataType.PARENT_INVOLVEMENT);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        censusDataSet = getCensusDataSet(1000002);
        censusDataSet.setDataType(CensusDataType.TOTAL_PER_PUPIL_SPENDING);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        censusDataSet = getCensusDataSet(1000003);
        censusDataSet.setDataType(CensusDataType.VANDALISM_INCIDENTS);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);


        SchoolProfileCensusHelper.CensusDataHolder groupedCensusDataSets = _schoolProfileCensusHelper.splitDataSets(censusDataSetMap, config);

        assertEquals("Expect dataSetsForSchoolData to only contain one item, since only one censusDataConfigEntry has schoolData flag true", 1, groupedCensusDataSets._dataSetsForSchoolData.size());
        assertEquals("Expect dataSetsForDistrictData to contain two items, since two censusDataConfigEntry has districtData flag true", 2, groupedCensusDataSets._dataSetsForDistrictData.size());
        assertEquals("Expect dataSetsForStateData to contain three items, since three censusDataConfigEntry has schoolData flag true", 3, groupedCensusDataSets._dataSetsForStateData.size());

    }*/

    /*@Test
    public void testFindSchoolCensusValuesAndHandleOverrides() throws Exception {
        School school = getSchool();

        ////////////////////////////////////////////////////////////////////////////////////////
        // Census Data Sets
        ////////////////////////////////////////////////////////////////////////////////////////

        Map<Integer, CensusDataSet> censusDataSetMap = new HashMap<Integer, CensusDataSet>();
        CensusDataSet censusDataSet = getCensusDataSet(1000000);
        censusDataSet.setDataType(CensusDataType.STUDENT_TEACHER_RATIO);
        censusDataSet.setYear(2010);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        // manual override data set
        censusDataSet = getCensusDataSet(1000001);
        censusDataSet.setDataType(CensusDataType.STUDENT_TEACHER_RATIO);
        censusDataSet.setYear(0);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        ////////////////////////////////////////////////////////////////////////////////////////

        censusDataSet = getCensusDataSet(1000002);
        censusDataSet.setDataType(CensusDataType.TOTAL_PER_PUPIL_SPENDING);
        censusDataSet.setYear(2010);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        // manual override data set
        censusDataSet = getCensusDataSet(1000003);
        censusDataSet.setDataType(CensusDataType.TOTAL_PER_PUPIL_SPENDING);
        censusDataSet.setYear(0);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);


        ////////////////////////////////////////////////////////////////////////////////////////

        censusDataSet = getCensusDataSet(1000004);
        censusDataSet.setDataType(CensusDataType.ABSENT_MORE_THAN_21_DAYS);
        censusDataSet.setYear(2010);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        // manual override data set
        censusDataSet = getCensusDataSet(1000005);
        censusDataSet.setDataType(CensusDataType.ABSENT_MORE_THAN_21_DAYS);
        censusDataSet.setYear(0);
        censusDataSetMap.put(censusDataSet.getId(), censusDataSet);

        ////////////////////////////////////////////////////////////////////////////////////////
        // Census Values
        ////////////////////////////////////////////////////////////////////////////////////////

        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();

        SchoolCensusValue schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setDataSet(censusDataSetMap.get(1000000));
        schoolCensusValue.setId(1000000);
        schoolCensusValue.setSchool(school);
        schoolCensusValue.setModified(null);
        schoolCensusValues.add(schoolCensusValue);

        schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setDataSet(censusDataSetMap.get(1000001));
        schoolCensusValue.setId(1000001);
        schoolCensusValue.setSchool(school);
        schoolCensusValue.setModified(new Date()); // recent date, valid override
        schoolCensusValues.add(schoolCensusValue);

        ////////////////////////////////////////////////////////////////////////////////////////

        schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setDataSet(censusDataSetMap.get(1000002));
        schoolCensusValue.setId(1000002);
        schoolCensusValue.setSchool(school);
        schoolCensusValue.setModified(null);
        schoolCensusValues.add(schoolCensusValue);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2009);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);

        schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setDataSet(censusDataSetMap.get(1000003));
        schoolCensusValue.setId(1000003);
        schoolCensusValue.setSchool(school);
        schoolCensusValue.setModified(calendar.getTime()); // october of year before census data set year. recent-enough valid override
        schoolCensusValues.add(schoolCensusValue);

        ////////////////////////////////////////////////////////////////////////////////////////

        schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setDataSet(censusDataSetMap.get(1000004));
        schoolCensusValue.setId(1000004);
        schoolCensusValue.setSchool(school);
        schoolCensusValue.setModified(null);
        schoolCensusValues.add(schoolCensusValue);

        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2009);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);

        schoolCensusValue = new SchoolCensusValue();
        schoolCensusValue.setDataSet(censusDataSetMap.get(1000005));
        schoolCensusValue.setId(1000005);
        schoolCensusValue.setSchool(school);
        schoolCensusValue.setModified(calendar.getTime()); // october of year before census data set year. not recent enough to be considered an override
        schoolCensusValues.add(schoolCensusValue);

        ////////////////////////////////////////////////////////////////////////////////////////

        expect(_censusDataSchoolValueDao.findSchoolCensusValues(eq(State.CA), eq(censusDataSetMap.values()), eq(Arrays.asList(new School[]{school})))).andReturn(schoolCensusValues);
        replay(_censusDataSchoolValueDao);

        Map<Integer, SchoolCensusValue> schoolCensusValueMap = _schoolProfileCensusHelper.handleSchoolValueOverrides(censusDataSetMap, school);

        verify(_censusDataSchoolValueDao);

        assertFalse(schoolCensusValueMap.containsKey(1000000));
        assertTrue(schoolCensusValueMap.containsKey(1000001));
        assertFalse(schoolCensusValueMap.containsKey(1000002));
        assertTrue(schoolCensusValueMap.containsKey(1000003));
        assertTrue(schoolCensusValueMap.containsKey(1000004));
        assertFalse(schoolCensusValueMap.containsKey(1000005));
    }*/
/*
    @Test
    public void testGetSchoolCensusValues() {
        HttpServletRequest request = new MockHttpServletRequest();

        _schoolProfileCensusHelper.getSchoolCensusValues(request);


    }*/

}
