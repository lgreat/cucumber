package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.school.ISchoolDao;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.StateManager;
import gs.data.state.State;

import static org.easymock.classextension.EasyMock.*;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class AccountInformationAjaxControllerTest extends BaseControllerTestCase {
    private AccountInformationAjaxController _controller;

    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private StateManager _stateManager;
    private StringWriter _sw;
    private PrintWriter _pw;


    public void setUp() throws Exception {
        super.setUp();

        _controller = new AccountInformationAjaxController();

        _schoolDao = createStrictMock(ISchoolDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _stateManager = createStrictMock(StateManager.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setGeoDao(_geoDao);
        _controller.setStateManager(_stateManager);

        _sw = new StringWriter();
        _pw = new PrintWriter(_sw);
    }

    public void testBasics() {
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_stateManager, _controller.getStateManager());
    }

    public void replayAll() {
        replay(_schoolDao);
        replay(_geoDao);
        replay(_stateManager);
    }

    public void verifyAll() {
        verify(_schoolDao);
        verify(_geoDao);
        verify(_stateManager);
    }

    public void testOutputOption() {
        replayAll();
        // test with all parameters
        _controller.outputOption(_pw, "value", "name", true);
        assertEquals("<option selected=\"selected\" value=\"value\">name</option>",
                _sw.getBuffer().toString());

        _sw = new StringWriter();
        _pw = new PrintWriter(_sw);
        // test with no parameters
        _controller.outputOption(_pw, null, null, false);
        assertEquals("<option value=\"null\">null</option>",
                _sw.getBuffer().toString());

        _sw = new StringWriter();
        _pw = new PrintWriter(_sw);
        // test shorter method signature with no parameters
        _controller.outputOption(_pw, null, null);
        assertEquals("<option value=\"null\">null</option>",
                _sw.getBuffer().toString());
        verifyAll();
    }

    public void testOutputCitySelect() {
        List<City> cities = new ArrayList<City>();
        City city1 = new City();
        city1.setName("Alameda");
        City city2 = new City();
        city2.setName("San Diego");
        cities.add(city1);
        cities.add(city2);

        expect(_geoDao.findCitiesByState(State.CA)).andReturn(cities);

        replayAll();
        _controller.outputCitySelect(State.CA, _pw, true);
        verifyAll();

        assertEquals("<option selected=\"selected\" value=\"\">Choose city</option>" +
                "<option value=\"My city is not listed\">My city is not listed</option>" +
                "<option value=\"Alameda\">Alameda</option>" +
                "<option value=\"San Diego\">San Diego</option>" +
                "", _sw.getBuffer().toString());
    }

    public void testOutputCitySelectNoNotListed() {
        List<City> cities = new ArrayList<City>();
        City city1 = new City();
        city1.setName("Alameda");
        City city2 = new City();
        city2.setName("San Diego");
        cities.add(city1);
        cities.add(city2);

        expect(_geoDao.findCitiesByState(State.CA)).andReturn(cities);

        replayAll();
        _controller.outputCitySelect(State.CA, _pw, false);
        verifyAll();

        assertEquals("<option selected=\"selected\" value=\"\">Choose city</option>" +
                "<option value=\"Alameda\">Alameda</option>" +
                "<option value=\"San Diego\">San Diego</option>" +
                "", _sw.getBuffer().toString());
    }

    public void testOutputSchoolSelect() {
        List<School> schools = new ArrayList<School>();
        School school = new School();
        school.setId(1);
        school.setName("Alameda High School");
        schools.add(school);

        expect(_schoolDao.findSchoolsInCityByGrade(State.CA, "Alameda", Grade.G_9)).andReturn(schools);

        replayAll();
        _controller.outputSchoolSelect(State.CA, "Alameda", "9", _pw);
        verifyAll();

        assertEquals("<option selected=\"selected\" value=\"-2\">--</option>" +
                "<option value=\"-1\">My child's school is not listed</option>" +
                "<option value=\"1\">Alameda High School</option>" +
                "", _sw.getBuffer().toString());
    }

    public void testHandleRequestCity() throws Exception {
        // expect state only to delegate to city
        getRequest().setParameter("state", "CA");

        expect(_stateManager.getState("CA")).andReturn(State.CA);
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(new ArrayList<City>());

        replayAll();
        _controller.handleRequest(getRequest(), getResponse());
        verifyAll();
    }

    public void testHandleRequestSchool() throws Exception {
        // expect state only to delegate to city
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("grade", "9");
        getRequest().setParameter("city", "Alameda");

        expect(_stateManager.getState("CA")).andReturn(State.CA);
        expect(_schoolDao.findSchoolsInCityByGrade(State.CA, "Alameda", Grade.G_9)).andReturn(new ArrayList<School>());

        replayAll();
        _controller.handleRequest(getRequest(), getResponse());
        verifyAll();
    }
}
