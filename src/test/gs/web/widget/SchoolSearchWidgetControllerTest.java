package gs.web.widget;

import gs.web.BaseControllerTestCase;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.school.ISchoolDao;
import gs.data.school.SchoolWithRatings;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.review.IReviewDao;
import gs.data.state.StateManager;
import gs.data.state.State;

import static org.easymock.classextension.EasyMock.*;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class SchoolSearchWidgetControllerTest extends BaseControllerTestCase {
    private SchoolSearchWidgetController _controller;
    private SchoolSearchWidgetCommand _command;
    private BindException _errors;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private StateManager _stateManager;


    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolSearchWidgetController();

        _geoDao = createStrictMock(IGeoDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _stateManager = createStrictMock(StateManager.class);

        _controller.setGeoDao(_geoDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setStateManager(_stateManager);

        _command = new SchoolSearchWidgetCommand();

        _errors = new BindException(_command, "");
    }

    protected void replayAll() {
        replay(_geoDao);
        replay(_schoolDao);
        replay(_reviewDao);
        replay(_stateManager);
    }

    protected void verifyAll() {
        verify(_geoDao);
        verify(_schoolDao);
        verify(_reviewDao);
        verify(_stateManager);
    }

    protected void resetAll() {
        reset(_geoDao);
        reset(_schoolDao);
        reset(_reviewDao);
        reset(_stateManager);
    }

    public void testBasics() {
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_reviewDao, _controller.getReviewDao());
        assertSame(_stateManager, _controller.getStateManager());
    }

    public void testCommandBasics() {
        City city = new City();
        _command.setCity(city);
        _command.setDisplayTab("map");
        _command.setMapLocationPrefix("prefix");
        _command.setMapLocationString("string");
        _command.setPreschoolFilterChecked(false);
        _command.setElementaryFilterChecked(false);
        _command.setMiddleFilterChecked(false);
        _command.setHighFilterChecked(false);
        _command.setSearchQuery("query");
        List<SchoolWithRatings> schools = new ArrayList<SchoolWithRatings>();
        _command.setSchools(schools);
        
        assertSame(city, _command.getCity());
        assertEquals("map", _command.getDisplayTab());
        assertEquals("prefix", _command.getMapLocationPrefix());
        assertEquals("string", _command.getMapLocationString());
        assertFalse(_command.isPreschoolFilterChecked());
        assertFalse(_command.isElementaryFilterChecked());
        assertFalse(_command.isMiddleFilterChecked());
        assertFalse(_command.isHighFilterChecked());
        assertEquals("query", _command.getSearchQuery());
        assertSame(schools, _command.getSchools());
    }

    public void testCommandGetLevelCodeString() {
        assertEquals("Expect all filters to be on by default", "p,e,m,h", _command.getLevelCodeString());
        _command.setPreschoolFilterChecked(false);
        assertEquals("e,m,h", _command.getLevelCodeString());
        _command.setElementaryFilterChecked(false);
        assertEquals("m,h", _command.getLevelCodeString());
        _command.setMiddleFilterChecked(false);
        assertEquals("h", _command.getLevelCodeString());
        _command.setHighFilterChecked(false);
        assertEquals("", _command.getLevelCodeString());
        _command.setMiddleFilterChecked(true);
        assertEquals("m", _command.getLevelCodeString());
        _command.setMiddleFilterChecked(false);
        _command.setElementaryFilterChecked(true);
        assertEquals("e", _command.getLevelCodeString());
        _command.setElementaryFilterChecked(false);
        _command.setPreschoolFilterChecked(true);
        assertEquals("p", _command.getLevelCodeString());
        _command.setHighFilterChecked(true);
        assertEquals("p,h", _command.getLevelCodeString());
        _command.setPreschoolFilterChecked(false);
        _command.setHighFilterChecked(false);
        _command.setElementaryFilterChecked(true);
        _command.setMiddleFilterChecked(true);
        assertEquals("e,m", _command.getLevelCodeString());
    }

    public void testGetStateFromString() {
        expect(_stateManager.getState("CA")).andReturn(State.CA);
        replayAll();
        assertEquals(State.CA, _controller.getStateFromString("CA"));
        verifyAll();

        resetAll();
        expect(_stateManager.getStateByLongName("California")).andReturn(State.CA);
        replayAll();
        assertEquals(State.CA, _controller.getStateFromString("California"));
        verifyAll();

        resetAll();
        replayAll();
        assertNull(_controller.getStateFromString(null));
    }

    public void testGetCityFromString() {
        City city = new City();
        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(city);
        replayAll();
        assertSame(city, _controller.getCityFromString(State.CA, "Alameda"));
        verifyAll();

        resetAll();
        expect(_geoDao.findCity(State.CA, "foobar")).andReturn(null);
        replayAll();
        assertNull(_controller.getCityFromString(State.CA, "foobar"));
        verifyAll();

        resetAll();
        replayAll();
        assertNull(_controller.getCityFromString(State.CA, null));
        verifyAll();
    }

    public void testLoadResultsForCityNull() {
        City city = new City();
        city.setName("Alameda");
        expect(_schoolDao.findSchoolsWithRatingsInCity(State.CA, city.getName())).andReturn(null);
        replayAll();
        assertFalse("Expect no results", _controller.loadResultsForCity(city, State.CA, _command));
        verifyAll();
        assertEquals(0, _command.getSchools().size());
    }

    public void testLoadResultsForCityEmpty() {
        City city = new City();
        city.setName("Alameda");
        expect(_schoolDao.findSchoolsWithRatingsInCity(State.CA, city.getName())).andReturn(new ArrayList<SchoolWithRatings>());
        replayAll();
        assertFalse("Expect no results", _controller.loadResultsForCity(city, State.CA, _command));
        verifyAll();
        assertEquals(0, _command.getSchools().size());
    }

    public void testLoadResultsForCity() {
        City city = new City();
        city.setName("Alameda");
        List<SchoolWithRatings> schools = new ArrayList<SchoolWithRatings>();
        SchoolWithRatings struct1 = new SchoolWithRatings();
        School school1 = new School();
        school1.setId(1);
        school1.setLevelCode(LevelCode.ELEMENTARY);
        struct1.setSchool(school1);
        schools.add(struct1);
        expect(_schoolDao.findSchoolsWithRatingsInCity(State.CA, city.getName())).andReturn(schools);
        _reviewDao.loadRatingsIntoSchoolList(isA(List.class), isA(State.class));
        replayAll();
        assertTrue("Expect results", _controller.loadResultsForCity(city, State.CA, _command));
        verifyAll();
        assertEquals(1, _command.getSchools().size());
        assertSame(struct1, _command.getSchools().get(0));
    }

    public void testOnSubmitThrowsError() throws Exception {
        try {
            _controller.onSubmit(_command);
            fail("Expect exception to be thrown from onSubmit");
        } catch (IllegalStateException ise) {
            // ok
        }
    }

    public void testOnBindOnNewForm() throws Exception {
        getRequest().setParameter("displayTab", "map");
        getRequest().setParameter("searchQuery", "nowhere");

        replayAll();
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        verifyAll();

        assertEquals("map", _command.getDisplayTab());
        assertEquals("nowhere", _command.getSearchQuery());
    }

    public void testOnBindAndValidate() throws Exception {
        replayAll();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verifyAll();
        assertTrue("Always expect errors", _errors.hasErrors());
    }

    public void testParseSearchQueryCityState() {
        expect(_stateManager.getState("CA")).andReturn(State.CA);
        City city = new City();
        city.setName("Alameda");
        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(city);

        List<SchoolWithRatings> schools = new ArrayList<SchoolWithRatings>();
        SchoolWithRatings struct1 = new SchoolWithRatings();
        School school1 = new School();
        school1.setId(1);
        school1.setLevelCode(LevelCode.ELEMENTARY);
        struct1.setSchool(school1);
        schools.add(struct1);
        // look up school and GS rating
        expect(_schoolDao.findSchoolsWithRatingsInCity(State.CA, city.getName())).andReturn(schools);
        // look up parent ratings
        _reviewDao.loadRatingsIntoSchoolList(isA(List.class), isA(State.class));

        replayAll();
        _controller.parseSearchQuery("Alameda, CA", _command, getRequest(), _errors);
        verifyAll();

        assertSame(schools, _command.getSchools());
        assertTrue("Always expect errors", _errors.hasErrors());
        assertEquals("Expect map tab to display when results", "map", _command.getDisplayTab());

    }
}
