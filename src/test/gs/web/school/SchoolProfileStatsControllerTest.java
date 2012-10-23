package gs.web.school;

import gs.data.school.census.*;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class SchoolProfileStatsControllerTest extends BaseControllerTestCase {
    SchoolProfileStatsController _controller;

    ICensusDataConfigEntryDao _censusStateConfigDao;
    ICensusDataSchoolValueDao _censusDataSchoolValueDao;
    ICensusDataDistrictValueDao _censusDataDistrictValueDao;
    ICensusDataStateValueDao _censusDataStateValueDao;
    SchoolProfileDataHelper _schoolProfileDataHelper;
    SchoolProfileCensusHelper _schoolProfileCensusHelper;
    ICensusCacheDao _censusCacheDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolProfileStatsController();

        _censusStateConfigDao = createStrictMock(ICensusDataConfigEntryDao.class);
        _censusDataSchoolValueDao = createStrictMock(ICensusDataSchoolValueDao.class);
        _censusDataDistrictValueDao = createStrictMock(ICensusDataDistrictValueDao.class);
        _censusDataStateValueDao = createStrictMock(CensusDataStateValueDaoHibernate.class);
        _schoolProfileDataHelper = createStrictMock(SchoolProfileDataHelper.class);
        _schoolProfileCensusHelper = createStrictMock(SchoolProfileCensusHelper.class);
        _censusCacheDao = createStrictMock(ICensusCacheDao.class);

        _controller.setCensusStateConfigDao(_censusStateConfigDao);
        _controller.setCensusDataSchoolValueDao(_censusDataSchoolValueDao);
        _controller.setCensusDataDistrictValueDao(_censusDataDistrictValueDao);
        _controller.setCensusDataStateValueDao(_censusDataStateValueDao);
        _controller.setSchoolProfileDataHelper(_schoolProfileDataHelper);
        _controller.setSchoolProfileCensusHelper(_schoolProfileCensusHelper);
        _controller.setCensusCacheDao(_censusCacheDao);
    }

    public void replayAllMocks() {
        replayMocks(_censusStateConfigDao, _censusDataSchoolValueDao, _censusDataDistrictValueDao,
                _censusDataStateValueDao, _schoolProfileDataHelper, _schoolProfileCensusHelper, _censusCacheDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_censusStateConfigDao, _censusDataSchoolValueDao, _censusDataDistrictValueDao,
                _censusDataStateValueDao, _schoolProfileDataHelper, _schoolProfileCensusHelper, _censusCacheDao);
    }

    public void testBasics() {
        assertNotNull(_controller);
    }

    public void testFormatValueAsFloat() {
        assertEquals(0f, _controller.formatValueAsFloat(null));
        assertEquals(0f, _controller.formatValueAsFloat(""));
        assertEquals(0f, _controller.formatValueAsFloat("three"));
        assertEquals(5f, _controller.formatValueAsFloat("5"));
        assertEquals(5f, _controller.formatValueAsFloat("xx5yy"));
        assertEquals(new Float("5.5"), _controller.formatValueAsFloat("5.5"));
        assertEquals(new Float("5.5"), _controller.formatValueAsFloat("xx5.5yy"));
        assertEquals(-5f, _controller.formatValueAsFloat("-5"));
        assertEquals(0f, _controller.formatValueAsFloat("5.5.6")); // nfe
        assertEquals(0f, _controller.formatValueAsFloat("-5-6")); // nfe
    }

    public void testFormatValueAsString() {
        // PERCENT
        assertEquals("0%", _controller.formatValueAsString(0f, CensusDataType.ValueType.PERCENT));
        assertEquals("0%", _controller.formatValueAsString(0.2f, CensusDataType.ValueType.PERCENT));
        assertEquals("1%", _controller.formatValueAsString(0.5f, CensusDataType.ValueType.PERCENT));
        assertEquals("100%", _controller.formatValueAsString(99.9f, CensusDataType.ValueType.PERCENT));

        // MONETARY
        assertEquals("$0.0", _controller.formatValueAsString(0f, CensusDataType.ValueType.MONETARY));
        assertEquals("$15.0", _controller.formatValueAsString(15f, CensusDataType.ValueType.MONETARY));
        assertEquals("$15.5", _controller.formatValueAsString(15.5f, CensusDataType.ValueType.MONETARY));
        assertEquals("$15.55", _controller.formatValueAsString(15.55f, CensusDataType.ValueType.MONETARY));

        // DEFAULT
        assertEquals("0", _controller.formatValueAsString(0f, CensusDataType.ValueType.NUMBER));
        assertEquals("0", _controller.formatValueAsString(0.2f, CensusDataType.ValueType.NUMBER));
        assertEquals("1", _controller.formatValueAsString(0.5f, CensusDataType.ValueType.NUMBER));
        assertEquals("100", _controller.formatValueAsString(99.9f, CensusDataType.ValueType.NUMBER));
    }

    public void testCensusValueNotEmpty() {
        assertFalse(_controller.censusValueNotEmpty(null));
        assertFalse(_controller.censusValueNotEmpty(""));
        assertFalse(_controller.censusValueNotEmpty("n/a"));
        assertFalse(_controller.censusValueNotEmpty("N/A"));
        assertTrue(_controller.censusValueNotEmpty("    "));
        assertTrue(_controller.censusValueNotEmpty("0"));
        assertTrue(_controller.censusValueNotEmpty("five"));
        assertTrue(_controller.censusValueNotEmpty("no"));
        assertTrue(_controller.censusValueNotEmpty("empty"));
    }

    public void testSortEthnicityValues() {
        try {
            _controller.sortEthnicityValues(null);
        } catch (Exception e) {
            fail("Unexpected exception when passing null list to sort. Should handle it gracefully. " + e);
        }

        List<SchoolProfileStatsDisplayRow> statsRows = new ArrayList<SchoolProfileStatsDisplayRow>();
        _controller.sortEthnicityValues(statsRows);
        assertEquals(0, statsRows.size());

        SchoolProfileStatsDisplayRow row1 = new SchoolProfileStatsDisplayRow(1l, 1, "first", "5.0", "5.0", "5.0", null, 2012, false);
        statsRows.add(row1);
        _controller.sortEthnicityValues(statsRows);
        assertEquals(1, statsRows.size());
    }
}
