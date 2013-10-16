package gs.web.geo;

import gs.data.hubs.HubCityMapping;
import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.easymock.EasyMock.*;
import org.easymock.classextension.EasyMock;
import static gs.web.geo.CityHubHelper.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 8/19/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CityHubHelperTest extends BaseControllerTestCase {
    private static Logger _logger = Logger.getLogger(CityHubHelperTest.class);

    private CityHubHelper _cityHubHelper;
    private IHubConfigDao _hubConfigDao;
    private IHubCityMappingDao _hubCityMappingDao;
    private AnchorListModelFactory _anchorListModelFactory;

    private static Map<String, String> _configKeyValues = new HashMap<String, String>(){{
        String importantKeyPrefix = CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX;
        String keyEnrollmentDatesKeyPrefix = CityHubHelper.KEY_ENROLLMENT_DATES_KEY_PREFIX;

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_YEAR, 30);
        cal1 = setTimeToStartOfDay(cal1);
        put(importantKeyPrefix + "_1_description", "Application deadline for DCPS");
        put(importantKeyPrefix + "_1_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal1.getTime()));
        put(importantKeyPrefix + "_1_url", "dcps.dc.gov");

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_YEAR, 10);
        cal2 = setTimeToStartOfDay(cal2);
        put(importantKeyPrefix + "_2_description", "Application deadline for PCSB");
        put(importantKeyPrefix + "_2_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal2.getTime()));
        put(importantKeyPrefix + "_2_url", "https://www.pcsb.org?");

        Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.DAY_OF_YEAR, 20);
        cal3 = setTimeToStartOfDay(cal3);
        put(importantKeyPrefix + "_3_description", "Application deadline for PCSB");
        put(importantKeyPrefix + "_3_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal3.getTime()));
        put(importantKeyPrefix + "_3_url", "https://www.pcsb.org?");

        // this should never be returned in the sorted config list. the date is set to 20 days back from current day.
        Calendar cal5 = Calendar.getInstance();
        cal5.add(Calendar.DAY_OF_YEAR, -20);
        cal5 = setTimeToStartOfDay(cal5);
        put(importantKeyPrefix + "_5_description", "Application deadline for PCSB");
        put(importantKeyPrefix + "_5_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal5.getTime()));
        put(importantKeyPrefix + "_5_url", "https://www.pcsb.org?");

        Calendar cal4 = Calendar.getInstance();
        cal4.add(Calendar.DAY_OF_YEAR, 15);
        cal4 = setTimeToStartOfDay(cal4);
        put(keyEnrollmentDatesKeyPrefix + "_public_elementary_1_description", "Application deadline for Public elementary schools");
        put(keyEnrollmentDatesKeyPrefix + "_public_elementary_1_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal4.getTime()));

        Calendar cal6 = Calendar.getInstance();
        cal6 = setTimeToStartOfDay(cal6);
        put(keyEnrollmentDatesKeyPrefix + "_private_high_1_description", "Application deadline for Private high schools");
        put(keyEnrollmentDatesKeyPrefix + "_private_high_1_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal6.getTime()));

        Calendar cal7 = Calendar.getInstance();
        cal7.add(Calendar.DAY_OF_YEAR, -49);
        cal7 = setTimeToStartOfDay(cal7);
        put(keyEnrollmentDatesKeyPrefix + "_private_middle_1_description", "Application process for Private middle schools begins");
        put(keyEnrollmentDatesKeyPrefix + "_private_middle_1_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal7.getTime()));

        Calendar cal8 = Calendar.getInstance();
        cal8.add(Calendar.DAY_OF_YEAR, 79);
        cal8 = setTimeToStartOfDay(cal8);
        put(keyEnrollmentDatesKeyPrefix + "_private_middle_2_description", "Application deadline for Private middle schools begins");
        put(keyEnrollmentDatesKeyPrefix + "_private_middle_2_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal8.getTime()));

        Calendar cal9 = Calendar.getInstance();
        cal9.add(Calendar.DAY_OF_YEAR, -1);
        cal9 = setTimeToStartOfDay(cal9);
        put(keyEnrollmentDatesKeyPrefix + "_charter_preschool_1_description", "Application deadline for charter preschools");
        put(keyEnrollmentDatesKeyPrefix + "_charter_preschool_1_date", (new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMddyyyy)).format(cal9.getTime()));
    }};

    public void setUp() throws Exception {
        super.setUp();
        _cityHubHelper = new CityHubHelper();

        _hubConfigDao = createStrictMock(IHubConfigDao.class);
        _hubCityMappingDao = createStrictMock(IHubCityMappingDao.class);
        _anchorListModelFactory = EasyMock.createStrictMock(AnchorListModelFactory.class);

        _cityHubHelper.setHubConfigDao(_hubConfigDao);
        _cityHubHelper.setHubCityMappingDao(_hubCityMappingDao);
        _cityHubHelper.setAnchorListModelFactory(_anchorListModelFactory);
    }

    private void replayAllMocks() {
        replayMocks(_hubConfigDao, _hubCityMappingDao, _anchorListModelFactory);
    }

    private void verifyAllMocks() {
        verifyMocks(_hubConfigDao, _hubCityMappingDao, _anchorListModelFactory);
    }

    private void resetAllMocks() {
        resetMocks(_hubConfigDao, _hubCityMappingDao, _anchorListModelFactory);
    }

    public void testHubConfigNonExistingHub() throws Exception {
        resetAllMocks();

        String city = "qwerty";
        State state = State.DC;
        Integer collectionId = null;

        expect(_hubCityMappingDao.getCollectionIdFromCityAndState(city, state)).andReturn(collectionId);

        replayAllMocks();
        List<HubConfig> configList = _cityHubHelper.getHubConfig(city, state);
        verifyAllMocks();

        assertEquals("Expect empty config to be returned for null hub id", 0, configList.size());
    }

    public void testHubConfig() throws Exception {
        resetAllMocks();

        String city = "Washington";
        State state = State.DC;
        Integer collectionId = 1;

        List<HubConfig> sampleConfigList = getSampleHubConfigList(collectionId, city, state);

        expect(_hubCityMappingDao.getCollectionIdFromCityAndState(city, state)).andReturn(collectionId);
        expect(_hubConfigDao.getAllConfigFromCollectionId(collectionId)).andReturn(sampleConfigList);

        replayAllMocks();
        List<HubConfig> configList = _cityHubHelper.getHubConfig(city, state);
        verifyAllMocks();

        assertEquals("Expected config list must be the same as the one returned by the controller.", sampleConfigList, configList);
    }

    public void testConfigKeyPrefixesSortedByDate() throws ParseException {
        List<String> sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(null);
        assertTrue("Expected list sorted by date must be empty.", sortedKeys.isEmpty());

        ModelMap modelMap = new ModelMap(){{
            addAllAttributes(_configKeyValues);
        }};
        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);
        assertTrue("Expected list sorted by date must be empty.", sortedKeys.isEmpty());

        modelMap.put(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, "qwertyu");
        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);
        assertTrue("Expected list should be empty.", sortedKeys.isEmpty());

        String keyPrefix = CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX;
        List<String> configDateKeyPrefixListWithIndex = new ArrayList<String>(){{
            String keyPrefix = CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX;
            add(keyPrefix + "_1");
            add(keyPrefix + "_2");
            add(keyPrefix + "_3");
        }};
        modelMap.put(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configDateKeyPrefixListWithIndex);
        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);
        assertEquals("The list should not be sorted because no date key with any of the prefixes exists in the map.", configDateKeyPrefixListWithIndex, sortedKeys);

        try{
            modelMap.put(keyPrefix + "_1_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_1_date")));
            modelMap.put(keyPrefix + "_2_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_2_date")));
            modelMap.put(keyPrefix + "_3_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_3_date")));
        }
        catch (ParseException ex) {
            _logger.error("CityHubHelperTest - error while trying to convert string to java date object", ex.getCause());
        }
        modelMap.put(keyPrefix + "_4_date", null);

        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);

        assertEquals(keyPrefix + "_2 should be first", "importantEvent_2", sortedKeys.get(0));
        assertEquals(keyPrefix + "_3 should be second", "importantEvent_3", sortedKeys.get(1));
        assertEquals(keyPrefix + "_1 should be the last", "importantEvent_1", sortedKeys.get(2));

        keyPrefix = CityHubHelper.KEY_ENROLLMENT_DATES_KEY_PREFIX;
        configDateKeyPrefixListWithIndex = new ArrayList<String>(){{
            String keyPrefix = CityHubHelper.KEY_ENROLLMENT_DATES_KEY_PREFIX;
            add(keyPrefix + "_public_elementary_1");
            add(keyPrefix + "_private_high_1");
            add(keyPrefix + "_private_middle_1");
            add(keyPrefix + "_private_middle_2");
            add(keyPrefix + "_charter_preschool_1");
        }};
        modelMap.put(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configDateKeyPrefixListWithIndex);
        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);
        assertEquals("The list should not be sorted because no date key with any of the prefixes exists in the map.", configDateKeyPrefixListWithIndex, sortedKeys);

        try{
            modelMap.put(keyPrefix + "_public_elementary_1_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_public_elementary_1_date")));
            modelMap.put(keyPrefix + "_private_high_1_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_private_high_1_date")));
            modelMap.put(keyPrefix + "_private_middle_1_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_private_middle_1_date")));
            modelMap.put(keyPrefix + "_private_middle_2_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_private_middle_2_date")));
            modelMap.put(keyPrefix + "_charter_preschool_1_date", new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(_configKeyValues.get(keyPrefix + "_charter_preschool_1_date")));
        }
        catch (ParseException ex) {
            _logger.error("CityHubHelperTest - error while trying to convert string to java date object", ex.getCause());
        }
        modelMap.put(keyPrefix + "_charter_preschool_4_date", null);

        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);

        assertEquals(keyPrefix + "_private_middle_1 should be first", keyPrefix +  "_private_middle_1", sortedKeys.get(0));
        assertEquals(keyPrefix + "_charter_preschool_1 should be second", keyPrefix + "_charter_preschool_1", sortedKeys.get(1));
        assertEquals(keyPrefix + "_private_high_1 should be third", keyPrefix + "_private_high_1", sortedKeys.get(2));
        assertEquals(keyPrefix + "_public_elementary_1 should be fourth", keyPrefix + "_public_elementary_1", sortedKeys.get(3));
        assertEquals(keyPrefix + "_private_middle_2 should be the last", keyPrefix + "_private_middle_2", sortedKeys.get(4));
    }

    public void testGetFilteredConfigMap() throws Exception {
        List<HubConfig> hubConfigs = null;
        String keyPrefix = null;

        ModelMap modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        assertEquals("Congig key prefixes with index list must be empty", 0, ((List<String>) modelMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY)).size());

        Integer collectionId = 1;
        String city = "Washington";
        State state = State.DC;

        final HubCityMapping hubCityMapping = getSampleHubCityMapping(1, collectionId, city, state.getAbbreviation());
        hubConfigs = new ArrayList<HubConfig>(){{
            add(setSampleHubConfig(1, hubCityMapping, "key1", "value1"));
            add(setSampleHubConfig(1, hubCityMapping, "key2", "value2"));
            add(setSampleHubConfig(1, hubCityMapping, "key3", "value3"));
            add(setSampleHubConfig(1, hubCityMapping, "key4", "value4"));
            add(setSampleHubConfig(1, hubCityMapping, "key5", "value5"));
        }};
        keyPrefix = IMPORTANT_EVENT_KEY_PREFIX;
        modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        assertEquals("Config key prefixes with index list must be empty. None of the keys in the list passed have " +
                "important event key prefix", 0, ((List<String>) modelMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY)).size());

        hubConfigs = getSampleHubConfigList(collectionId, city, state);
        modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        List<String> configKeyPrefixesWithIndex = (List<String>) modelMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY);
        assertEquals("Expect the list size to be 3, there are 3 important event keys with dates", 3, configKeyPrefixesWithIndex.size());
        assertEquals("Expected an url value to have http prefix appended if it already doesn't have.", "http://dcps.dc.gov",
                (String) modelMap.get(keyPrefix + "_1_url"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 20);
        cal = setTimeToStartOfDay(cal);
        assertEquals("Expect the month to be set correct (Calendar.MONTH is 0 based, so add 1 to that)", cal.get(Calendar.MONTH) + 1,
                modelMap.get(keyPrefix + "_3_date_month"));
        assertEquals("Expect the month to be set correct", cal.get(Calendar.DAY_OF_MONTH),
                modelMap.get(keyPrefix + "_3_date_dayOfMonth"));
        assertEquals("Expect the month to be set correct", cal.get(Calendar.YEAR),
                modelMap.get(keyPrefix + "_3_date_year"));
        assertNull("Expect important event 5 to not be included in the sorted list", modelMap.get(keyPrefix + "_5_date"));

        hubConfigs.add(setSampleHubConfig(collectionId, hubCityMapping, "importantEvent_4_date", "invalid date format"));
        modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        assertNull("Expect the event 4 date key to not exist because that should throw an exception", modelMap.get(keyPrefix + "_4_date"));

        keyPrefix = CityHubHelper.KEY_ENROLLMENT_DATES_KEY_PREFIX;
        hubConfigs = getSampleHubConfigList(collectionId, city, state);
        modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        configKeyPrefixesWithIndex = (List<String>) modelMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY);
        assertEquals("Expect the list size to be 2, there are 2 key enrollment dates keys", 2, configKeyPrefixesWithIndex.size());
        assertEquals("Expected an url value to have http prefix appended if it already doesn't have.", "Application deadline for Public elementary schools",
                (String) modelMap.get(keyPrefix + "_public_elementary_1_description"));
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 79);
        cal = setTimeToStartOfDay(cal);
        assertEquals("Expect the date to be correct", new SimpleDateFormat(CityHubHelper.DATE_PATTERN_MMMdyyyy).format(cal.getTime()),
                modelMap.get(keyPrefix + "_private_middle_2_date_MMMdyyyy"));
        assertNull("Expect public charter preschool 1 to not be included in the sorted list", modelMap.get(keyPrefix +
                "_charter_preschool_1_date"));
    }

    public void testGetCollectionBrowseLinks() {
        resetAllMocks();

        Integer collectionId = null;
        String city = "ashdjjka";
        State state = State.DC;
        GsMockHttpServletRequest request = getRequest();
        List<SchoolType> schoolTypes = SchoolType.sortOrder;
        List<LevelCode> levelCodes = new ArrayList<LevelCode>(){{
            add(LevelCode.PRESCHOOL);
            add(LevelCode.ELEMENTARY);
            add(LevelCode.MIDDLE);
            add(LevelCode.HIGH);
        }};

        // test to ignore null anchor objects in the list that will be added to the model map
        expect(_anchorListModelFactory.createBrowseLinksWithFilter((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (LevelCode[]) anyObject(), (State) anyObject(), (String) anyObject(), (String) anyObject())).andReturn(null).times(4);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (SchoolType[]) anyObject(), (State) anyObject(), (String) anyObject(), (String) anyObject())).andReturn(null).times(3);

        replayAllMocks();
        AnchorListModel browseLinks = _cityHubHelper.getCollectionBrowseLinks(request, collectionId, city, state);
        verifyAllMocks();

        assertEquals("Expect the browse links list to be empty", 0, browseLinks.getResults().size());

        // test to see whether all anchor objects have been added to the list in the correct order
        resetAllMocks();

        String anchorContent = "Preschools";
        Anchor levelCodeAnchor1 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(0)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(levelCodeAnchor1);

        anchorContent = "Elementary Schools";
        Anchor levelCodeAnchor2 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(1)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(levelCodeAnchor2);

        anchorContent = "Middle Schools";
        Anchor levelCodeAnchor3 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(2)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(levelCodeAnchor3);

        anchorContent = "High Schools";
        Anchor levelCodeAnchor4 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(3)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(levelCodeAnchor4);

        anchorContent = "Public Schools";
        Anchor stAnchor1 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{schoolTypes.get(0), schoolTypes.get(2)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(stAnchor1);

        anchorContent = "Private Schools";
        Anchor stAnchor2 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{schoolTypes.get(1)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(stAnchor2);

        anchorContent = "Charter Schools";
        Anchor stAnchor3 = new Anchor("http://www.greatschools.org", anchorContent);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{schoolTypes.get(2)}),
                eq(state), eq(city), eq(anchorContent))).andReturn(stAnchor3);

        replayAllMocks();
        browseLinks = _cityHubHelper.getCollectionBrowseLinks(request, collectionId, city, state);
        verifyAllMocks();

        assertEquals("Expect the size of browse links list to be 7", 7, browseLinks.getResults().size());
        assertEquals("Expect the 1st item to be preschool result", levelCodeAnchor1, browseLinks.getResults().get(0));
        assertEquals("Expect the 2nd item to be elementary school result", levelCodeAnchor2, browseLinks.getResults().get(1));
        assertEquals("Expect the 3rd item to be middle school result", levelCodeAnchor3, browseLinks.getResults().get(2));
        assertEquals("Expect the 3rd item to be high school result", levelCodeAnchor4, browseLinks.getResults().get(3));
        assertEquals("Expect the 4th item to be public school type result", stAnchor1, browseLinks.getResults().get(4));
        assertEquals("Expect the 5th item to be private school type result", stAnchor2, browseLinks.getResults().get(5));
        assertEquals("Expect the 6th item to be charter school type result", stAnchor3, browseLinks.getResults().get(6));

        // test to ignore null anchor objects in the list that will be added to the model map
        resetAllMocks();

        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(0)}),
                eq(state), eq(city), eq(levelCodeAnchor1.getContents()))).andReturn(levelCodeAnchor1);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(1)}),
                eq(state), eq(city), eq(levelCodeAnchor2.getContents()))).andReturn(null);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(2)}),
                eq(state), eq(city), eq(levelCodeAnchor3.getContents()))).andReturn(null);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{levelCodes.get(3)}),
                eq(state), eq(city), eq(levelCodeAnchor4.getContents()))).andReturn(levelCodeAnchor4);

        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{schoolTypes.get(0), schoolTypes.get(2)}),
                eq(state), eq(city), eq(stAnchor1.getContents()))).andReturn(stAnchor1);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{schoolTypes.get(1)}),
                eq(state), eq(city), eq(stAnchor2.getContents()))).andReturn(stAnchor2);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(eq(request), eq(collectionId), aryEq(new Object[]{schoolTypes.get(2)}),
                eq(state), eq(city), eq(stAnchor3.getContents()))).andReturn(null);

        replayAllMocks();
        browseLinks = _cityHubHelper.getCollectionBrowseLinks(request, collectionId, city, state);
        verifyAllMocks();

        assertEquals("Expect the size of browse links list to be 4", 4, browseLinks.getResults().size());
        assertEquals("Expect the 1st item to be preschool result", levelCodeAnchor1, browseLinks.getResults().get(0));
        assertEquals("Expect the 2nd item to be high school result", levelCodeAnchor4, browseLinks.getResults().get(1));
        assertEquals("Expect the 3rd item to be public school type result", stAnchor1, browseLinks.getResults().get(2));
        assertEquals("Expect the 4th item to be private school type result", stAnchor2, browseLinks.getResults().get(3));
    }

    public void testGetSortedKeyPrefixWithLevelCodeAndSchoolTypeMap() {
        Map<String, List<String>> sortedConfigKeyPrefixesMap =
                _cityHubHelper.getSortedKeyPrefixWithLevelCodeAndSchoolTypeMap(null, null);
        assertEquals("Expect the map to be an empty object", true, sortedConfigKeyPrefixesMap.isEmpty());

        String keyPrefix = KEY_ENROLLMENT_DATES_KEY_PREFIX;
        sortedConfigKeyPrefixesMap = _cityHubHelper.getSortedKeyPrefixWithLevelCodeAndSchoolTypeMap(keyPrefix,
                new ArrayList<String>());
        assertEquals("Expect the map to have 12 keys (all 12 possible school type and level code key combination - " +
                "4 level codes and 3 school types", 12, sortedConfigKeyPrefixesMap.size());
        assertNull("Expect any key value in the map to be null", sortedConfigKeyPrefixesMap.get(keyPrefix + "_public_elementary_"));

        sortedConfigKeyPrefixesMap = _cityHubHelper.getSortedKeyPrefixWithLevelCodeAndSchoolTypeMap(KEY_ENROLLMENT_DATES_KEY_PREFIX,
                getSampleSortedKeyEnrollmentDatePrefixes());
        assertEquals("Expect the map to have 12 keys (all 12 possible school type and level code key combination - " +
                "4 level codes and 3 school types", 12, sortedConfigKeyPrefixesMap.size());
        assertNull("Expect the value for charter_elementary to be null", sortedConfigKeyPrefixesMap.get(keyPrefix +
                "_charter_elementary_"));
        assertEquals("Expect the value for charter_elementary to have 2 key prefixes with index", 2,
                sortedConfigKeyPrefixesMap.get(keyPrefix + "_private_middle_").size());
        assertEquals("Expect the sorted order in sample list to be maintained after filtering. keyEnrollmentDates_public_elementary_2" +
                " should be first", keyPrefix + "_public_elementary_2", sortedConfigKeyPrefixesMap.get(keyPrefix +
                "_public_elementary_").get(0));
        assertEquals("Expect the sorted order in sample list to be maintained after filtering. keyEnrollmentDates_public_elementary_3" +
                " should be second", keyPrefix + "_public_elementary_3", sortedConfigKeyPrefixesMap.get(keyPrefix +
                "_public_elementary_").get(1));
        assertEquals("Expect the sorted order in sample list to be maintained after filtering. keyEnrollmentDates_public_elementary_1" +
                " should be second", keyPrefix + "_public_elementary_1", sortedConfigKeyPrefixesMap.get(keyPrefix +
                "_public_elementary_").get(2));
        assertEquals("Expect the value for charter_elementary to have 1 key prefixes with index", 1,
                sortedConfigKeyPrefixesMap.get(keyPrefix + "_charter_middle_").size());
    }

    public void testGetCollectionNicknameFromConfigList() {
        String collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(null, null);

        assertNull("Expect null to be retunred for null objects passed", collectionNickname);

        Integer collectionId = 1;
        String city = "Washington";
        State state = State.DC;
        List<HubConfig> configList = getSampleHubConfigList(collectionId, city, state);

        collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(configList, collectionId);

        assertNull("Expect collection nickname to be null", collectionNickname);

        HubCityMapping hubCityMapping = getSampleHubCityMapping(1, collectionId, city, state.getAbbreviation());
        configList.add(setSampleHubConfig(1, hubCityMapping, _cityHubHelper.COLLECTION_NICKNAME_KEY, "DC"));
        collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(configList, collectionId);

        assertEquals("Expect collection nickname for DC hub to be DC", "DC", collectionNickname);

        Integer anotherCollectionId = 2;
        String anotherCity = "Detroit";
        State anotherState = State.MI;
        configList.addAll(getSampleHubConfigList(anotherCollectionId, anotherCity, anotherState));
        HubCityMapping anotherHubCityMapping = getSampleHubCityMapping(2, anotherCollectionId, anotherCity, anotherState.getAbbreviation());
        configList.add(setSampleHubConfig(2, anotherHubCityMapping, _cityHubHelper.COLLECTION_NICKNAME_KEY, "Detroit"));

        collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(configList, anotherCollectionId);

        assertEquals("Expect collection nickname for Detroit hub to be Detroit (Passing in list with mix of configs from 2" +
                " different hubs", "Detroit", collectionNickname);


        collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(configList, collectionId);

        assertEquals("Expect collection nickname for DC hub to be DC (Passing in list with mix of configs from 2" +
                " different hubs", "DC", collectionNickname);
    }

    private List<HubConfig> getSampleHubConfigList(final Integer collectionId, final String city, final State state) {
        List<HubConfig> sampleConfigList = new ArrayList<HubConfig>();
        HubCityMapping hubCityMapping = getSampleHubCityMapping(1, collectionId, city, state.getAbbreviation());

        for(String key : _configKeyValues.keySet()) {
            sampleConfigList.add(setSampleHubConfig(1, hubCityMapping, key, _configKeyValues.get(key)));
        }

        return sampleConfigList;
    }

    private HubConfig setSampleHubConfig(final Integer id, final HubCityMapping hubCityMapping, final String key,
                                         final String value) {
        HubConfig hubConfig = new HubConfig();
        hubConfig.setId(id);
        hubConfig.setHubCityMapping(hubCityMapping);
        hubConfig.setQuay(key);
        hubConfig.setValue(value);
        return hubConfig;
    }

    private HubCityMapping getSampleHubCityMapping(final Integer id, final Integer collectionId, final String city,
                                                   final String state) {
        HubCityMapping hubCityMapping = new HubCityMapping();
        hubCityMapping.setId(id);
        hubCityMapping.setCollectionId(collectionId);
        hubCityMapping.setCity(city);
        hubCityMapping.setState(state);
        return hubCityMapping;
    }

    public static Calendar setTimeToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public List<String> getSampleSortedKeyEnrollmentDatePrefixes() {
        return new ArrayList<String>(){{
            String keyPrefix = KEY_ENROLLMENT_DATES_KEY_PREFIX;
            add(keyPrefix + "_public_preschool_1");
            add(keyPrefix + "_charter_middle_1");
            add(keyPrefix + "_public_elementary_2");
            add(keyPrefix + "_private_high_1");
            add(keyPrefix + "_public_preschool_2");
            add(keyPrefix + "_public_elementary_3");
            add(keyPrefix + "_private_middle_1");
            add(keyPrefix + "_private_middle_2");
            add(keyPrefix + "_charter_preschool_1");
            add(keyPrefix + "_public_elementary_1");
            add(keyPrefix + "_charter_high_3");
        }};
    }
}
