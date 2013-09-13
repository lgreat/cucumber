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
        put("importantEvent_1_description", "Application deadline for DCPS");
        put("importantEvent_1_date", "10-31-2013");
        put("importantEvent_1_url", "dcps.dc.gov");
        put("importantEvent_2_description", "Application deadline for PCSB");
        put("importantEvent_2_date", "10-16-2013");
        put("importantEvent_2_url", "https://www.pcsb.org?");
        put("importantEvent_3_description", "Application deadline for PCSB");
        put("importantEvent_3_date", "10-29-2013");
        put("importantEvent_3_url", "https://www.pcsb.org?");
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
        Integer hubId = null;

        expect(_hubCityMappingDao.getHubIdFromCityAndState(city, state)).andReturn(hubId);

        replayAllMocks();
        List<HubConfig> configList = _cityHubHelper.getHubConfig(city, state);
        verifyAllMocks();

        assertEquals("Expect empty config to be returned for null hub id", 0, configList.size());
    }

    public void testHubConfig() throws Exception {
        resetAllMocks();

        String city = "Washington";
        State state = State.DC;
        Integer hubId = 1;

        List<HubConfig> sampleConfigList = getSampleHubConfigList(hubId, city, state);

        expect(_hubCityMappingDao.getHubIdFromCityAndState(city, state)).andReturn(hubId);
        expect(_hubConfigDao.getAllConfigFromHubId(hubId)).andReturn(sampleConfigList);

        replayAllMocks();
        List<HubConfig> configList = _cityHubHelper.getHubConfig(city, state);
        verifyAllMocks();

        assertEquals("Expected config list must be the same as the one returned by the controller.", sampleConfigList, configList);
    }

    public void testCOnfigKeyPrefixesSortedByDate() throws ParseException {
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

        List<String> configDateKeyPrefixListWithIndex = new ArrayList<String>(){{
            add("importantEvent_1");
            add("importantEvent_2");
            add("importantEvent_3");
        }};
        modelMap.put(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configDateKeyPrefixListWithIndex);
        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);
        assertEquals("The list should not be sorted because no date key with any of the prefixes exists in the map.", configDateKeyPrefixListWithIndex, sortedKeys);

        try{
            modelMap.put("importantEvent_1_date", new SimpleDateFormat(DATE_FORMAT).parse(_configKeyValues.get("importantEvent_1_date")));
            modelMap.put("importantEvent_2_date", new SimpleDateFormat(DATE_FORMAT).parse(_configKeyValues.get("importantEvent_2_date")));
            modelMap.put("importantEvent_3_date", new SimpleDateFormat(DATE_FORMAT).parse(_configKeyValues.get("importantEvent_3_date")));
        }
        catch (ParseException ex) {
            _logger.error("CityHubHelperTest - error while trying to convert string to java date object", ex.getCause());
        }
        modelMap.put("importantEvent_4_date", null);

        sortedKeys = _cityHubHelper.getConfigKeyPrefixesSortedByDate(modelMap);

        assertEquals("importantEvent_2 should be first", "importantEvent_2", sortedKeys.get(0));
        assertEquals("importantEvent_3 should be second", "importantEvent_3", sortedKeys.get(1));
        assertEquals("importantEvent_1 should be the last", "importantEvent_1", sortedKeys.get(2));
    }

    public void testGetFilteredConfigMap() throws Exception {
        List<HubConfig> hubConfigs = null;
        String keyPrefix = null;

        ModelMap modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        assertEquals("Congig key prefixes with index list must be empty", 0, ((List<String>) modelMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY)).size());

        Integer hubId = 1;
        String city = "Washington";
        State state = State.DC;

        final HubCityMapping hubCityMapping = getSampleHubCityMapping(1, hubId, city, state.getAbbreviation());
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

        hubConfigs = getSampleHubConfigList(hubId, city, state);
        modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        List<String> configKeyPrefixesWithIndex = (List<String>) modelMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY);
        assertEquals("Expect the list size to be 3, there are 3 important event keys with dates", 3, configKeyPrefixesWithIndex.size());
        assertEquals("Expected an url value to have http prefix appended if it already doesn't have.", "http://dcps.dc.gov",
                (String) modelMap.get("importantEvent_1_url"));
        assertEquals("Expect the date string to be converted to date object", new SimpleDateFormat(DATE_FORMAT).parse("10-29-2013"),
                modelMap.get("importantEvent_3_date"));
        assertEquals("Expect the month in a date is identified and set correctly", 10, modelMap.get("importantEvent_2_date_month"));

        hubConfigs.add(setSampleHubConfig(hubId, hubCityMapping, "importantEvent_4_date", "invalid date format"));
        modelMap = _cityHubHelper.getFilteredConfigMap(hubConfigs, keyPrefix);

        assertNull("Expect the event 4 date key to not exist because that should throw an exception", modelMap.get("importantEvent_4_date"));
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
                isA(LevelCode.class), (State) anyObject(), (String) anyObject())).andReturn(null).times(4);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                isA(SchoolType.class), (State) anyObject(), (String) anyObject())).andReturn(null).times(3);

        replayAllMocks();
        AnchorListModel browseLinks = _cityHubHelper.getCollectionBrowseLinks(request, collectionId, city, state);
        verifyAllMocks();

        assertEquals("Expect the browse links list to be empty", 0, browseLinks.getResults().size());

        // test to see whether all anchor objects have been added to the list in the correct order
        resetAllMocks();

        Anchor levelCodeAnchor1 = new Anchor("http://www.greatschools.org", "Preschool");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(0), state, city)).andReturn(levelCodeAnchor1);

        Anchor levelCodeAnchor2 = new Anchor("http://www.greatschools.org", "Elementary");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(1), state, city)).andReturn(levelCodeAnchor2);

        Anchor levelCodeAnchor3 = new Anchor("http://www.greatschools.org", "Middle");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(2), state, city)).andReturn(levelCodeAnchor3);

        Anchor levelCodeAnchor4 = new Anchor("http://www.greatschools.org", "High");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(3), state, city)).andReturn(levelCodeAnchor4);

        Anchor stAnchor1 = new Anchor("http://www.greatschools.org", "Public");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, schoolTypes.get(0), state, city)).andReturn(stAnchor1);

        Anchor stAnchor2 = new Anchor("http://www.greatschools.org", "Private");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, schoolTypes.get(1), state, city)).andReturn(stAnchor2);

        Anchor stAnchor3 = new Anchor("http://www.greatschools.org", "Charter");
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, schoolTypes.get(2), state, city)).andReturn(stAnchor3);

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

        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(0), state, city)).andReturn(levelCodeAnchor1);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(1), state, city)).andReturn(null);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(2), state, city)).andReturn(null);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, levelCodes.get(3), state, city)).andReturn(levelCodeAnchor4);

        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, schoolTypes.get(0), state, city)).andReturn(stAnchor1);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, schoolTypes.get(1), state, city)).andReturn(stAnchor2);
        expect(_anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, schoolTypes.get(2), state, city)).andReturn(null);

        replayAllMocks();
        browseLinks = _cityHubHelper.getCollectionBrowseLinks(request, collectionId, city, state);
        verifyAllMocks();

        assertEquals("Expect the size of browse links list to be 4", 4, browseLinks.getResults().size());
        assertEquals("Expect the 1st item to be preschool result", levelCodeAnchor1, browseLinks.getResults().get(0));
        assertEquals("Expect the 2nd item to be high school result", levelCodeAnchor4, browseLinks.getResults().get(1));
        assertEquals("Expect the 3rd item to be public school type result", stAnchor1, browseLinks.getResults().get(2));
        assertEquals("Expect the 4th item to be private school type result", stAnchor2, browseLinks.getResults().get(3));
    }

    private List<HubConfig> getSampleHubConfigList(int hubId, String city, State state) {
        List<HubConfig> sampleConfigList = new ArrayList<HubConfig>();
        HubCityMapping hubCityMapping = getSampleHubCityMapping(1, hubId, city, state.getAbbreviation());

        for(String key : _configKeyValues.keySet()) {
            sampleConfigList.add(setSampleHubConfig(1, hubCityMapping, key, _configKeyValues.get(key)));
        }

        return sampleConfigList;
    }

    private HubConfig setSampleHubConfig(Integer id, HubCityMapping hubCityMapping, String key, String value) {
        HubConfig hubConfig = new HubConfig();
        hubConfig.setId(id);
        hubConfig.setHubCityMapping(hubCityMapping);
        hubConfig.setQuay(key);
        hubConfig.setValue(value);
        return hubConfig;
    }

    private HubCityMapping getSampleHubCityMapping(Integer id, Integer hubId, String city, String state) {
        HubCityMapping hubCityMapping = new HubCityMapping();
        hubCityMapping.setId(id);
        hubCityMapping.setHubId(hubId);
        hubCityMapping.setCity(city);
        hubCityMapping.setState(state);
        return hubCityMapping;
    }
}