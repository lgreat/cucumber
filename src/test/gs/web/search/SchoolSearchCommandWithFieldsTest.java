package gs.web.search;

import gs.data.hubs.IHubCityMappingDao;
import gs.data.search.FieldSort;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.util.context.SessionContext;
import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.easymock.EasyMock.*;


public class SchoolSearchCommandWithFieldsTest extends BaseControllerTestCase {
    private SchoolSearchCommandWithFields _schoolSearchCommandWithFields;
    private IHubCityMappingDao _hubCityMappingDao;

    public void setUp() throws Exception {
        super.setUp();
        _schoolSearchCommandWithFields = EasyMock.createStrictMock(SchoolSearchCommandWithFields.class);
        _hubCityMappingDao = createStrictMock(IHubCityMappingDao.class);
        _schoolSearchCommandWithFields.setHubCityMappingDao(_hubCityMappingDao);
    }

    private void replayAllMocks() {
        replayMocks(_hubCityMappingDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_hubCityMappingDao);
    }

    private void resetAllMocks() {
        resetMocks(_hubCityMappingDao);
    }

    @Test
    public void testGetFieldSort() throws Exception {
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = new SchoolSearchCommand();
        command.setSortBy("DISTANCE");
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        assertEquals("Expect to receive distance sort", FieldSort.DISTANCE, commandWithFields.getFieldSort());
        assertEquals("Expect sortBy to still be DISTANCE", "DISTANCE", command.getSortBy());

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = new SchoolSearchCommand();
        command.setSortBy("JUNK");
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        assertNull("Expect to receive null sort", commandWithFields.getFieldSort());
        assertNull("Expect sortBy to have been nulled-out by getFieldSort() method since sortBy wasnt valid enum", command.getSortBy());
    }

    @Test
    public void testIsHubsLocalSearch_isHubsLocalSchoolSearchByName() {
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = new SchoolSearchCommand();
        command.setSearchString("school");
        command.setCollectionId("1");
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        assertTrue("Expect the search to be from local hub if the by name search has collection id",
                commandWithFields.isHubsLocalSearch());
    }

    @Test
    public void testIsHubsLocalSearch_isNotHubsLocalSchoolSearchByName() {
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = new SchoolSearchCommand();
        command.setSearchString("school");
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        assertFalse("Expect the search to be not local hub if the by name search has no collection id",
                commandWithFields.isHubsLocalSearch());
    }

    @Test
    public void testIsHubsLocalSearch_isHubCityBrowse() {
        resetAllMocks();
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;
        String city = "detroit";
        State state = State.MI;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/" + state.getLongName().toLowerCase() + "/" + city + "/schools/");
        SessionContext sessionContext = new SessionContext();
        sessionContext.setState(state);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        fields = new DirectoryStructureUrlFields(request);
        command = new SchoolSearchCommand();
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        commandWithFields.setHubCityMappingDao(_hubCityMappingDao);

        expect(_hubCityMappingDao.getCollectionIdFromCityAndState(city, state)).andReturn(1);

        replayAllMocks();
        boolean isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertTrue("Expect the browse to be hub browse if city and sate are a part of collection", isLocalHubSearch);

        resetAllMocks();

        replayAllMocks();
        isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertTrue("Expect no dao call to be made if the local hub search check is made more than once", isLocalHubSearch);
        assertTrue("Expect no dao call to be made if the local hub search check is made more than once",
                commandWithFields.getHasAlreadyCheckedForIsHubLocalSearch());
    }

    @Test
    public void testIsHubsLocalSearch_isCityBrowseNotInHub() {
        resetAllMocks();
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;
        String city = "anchorage";
        State state = State.AK;

        MockHttpServletRequest request = getSampleCityBrowseRequest(city, state);
        fields = new DirectoryStructureUrlFields(request);
        command = new SchoolSearchCommand();
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        commandWithFields.setHubCityMappingDao(_hubCityMappingDao);

        expect(_hubCityMappingDao.getCollectionIdFromCityAndState(city, state)).andReturn(null);

        replayAllMocks();
        boolean isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertFalse("Expect city browse (not hub browse) if city and sate are not part of any collection", isLocalHubSearch);

        resetAllMocks();

        replayAllMocks();
        isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertFalse("Expect no dao call to be made if the local hub search check is made more than once", isLocalHubSearch);
        assertTrue("Expect no dao call to be made if the local hub search check is made more than once",
                commandWithFields.getHasAlreadyCheckedForIsHubLocalSearch());
    }

    @Test
    public void testIsHubsLocalSearch_isNearbySearchResultCityInHub() {
        resetAllMocks();
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;
        String city = "detroit";
        State state = State.MI;

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = setSampleNearbySearchCommand(city, state);
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        commandWithFields.setHubCityMappingDao(_hubCityMappingDao);

        expect(_hubCityMappingDao.getCollectionIdFromCityAndState(city, state)).andReturn(1);

        replayAllMocks();
        boolean isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertTrue("Expect to be local hub search if city returned by nearby search result (from google reverse geocode" +
                " is a part of a hub", isLocalHubSearch);

        resetAllMocks();

        replayAllMocks();
        isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertTrue("Expect no dao call to be made if the local hub search check is made more than once", isLocalHubSearch);
        assertTrue("Expect no dao call to be made if the local hub search check is made more than once",
                commandWithFields.getHasAlreadyCheckedForIsHubLocalSearch());
    }

    @Test
    public void testIsHubsLocalSearch_isNearbySearchResultCityNotInHub() {
        resetAllMocks();
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;
        String city = "anchorage";
        State state = State.AK;

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = setSampleNearbySearchCommand(city, state);
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        commandWithFields.setHubCityMappingDao(_hubCityMappingDao);

        expect(_hubCityMappingDao.getCollectionIdFromCityAndState(city, state)).andReturn(null);

        replayAllMocks();
        boolean isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertFalse("Expect to be local hub search if city returned by nearby search result (from google reverse geocode" +
                " is a part of a hub", isLocalHubSearch);

        resetAllMocks();

        replayAllMocks();
        isLocalHubSearch = commandWithFields.isHubsLocalSearch();
        verifyAllMocks();

        assertFalse("Expect no dao call to be made if the local hub search check is made more than once", isLocalHubSearch);
        assertTrue("Expect no dao call to be made if the local hub search check is made more than once",
                commandWithFields.getHasAlreadyCheckedForIsHubLocalSearch());
    }

    public SchoolSearchCommand setSampleNearbySearchCommand(String city, State state) {
        SchoolSearchCommand command =  new SchoolSearchCommand();
        command.setCity(city);
        command.setState(state.getAbbreviation());
        command.setLat(new Double("42.4595697"));
        command.setLon(new Double("-83.2398943"));
        command.setDistance("5");
        command.setLocationSearchString("48075"); // assuming google reverse geocoding would return Detroit, MI for zip 48075
        return command;
    }

    public MockHttpServletRequest getSampleCityBrowseRequest(String city, State state) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/" + state.getLongName().toLowerCase() + "/" + city + "/schools/");
        SessionContext sessionContext = new SessionContext();
        sessionContext.setState(state);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        return request;
    }
}
