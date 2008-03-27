package gs.web.geo;

import gs.web.BaseControllerTestCase;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;

import static org.easymock.classextension.EasyMock.*;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NearbyCitiesAjaxControllerTest extends BaseControllerTestCase {
    private NearbyCitiesAjaxController _controller;
    private StateManager _stateManager;
    private IGeoDao _geoDao;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new NearbyCitiesAjaxController();

        _stateManager = createStrictMock(StateManager.class);
        _geoDao = createStrictMock(IGeoDao.class);

        _controller.setStateManager(_stateManager);
        _controller.setGeoDao(_geoDao);
    }

    public void testBasics() {
        assertSame(_stateManager, _controller.getStateManager());
        assertSame(_geoDao, _controller.getGeoDao());
    }
    public void testHandleRequest() throws Exception {
        // set up data
        getRequest().setParameter("state", "CA");

        expect(_stateManager.getState("CA")).andReturn(State.CA);
        List<City> cities = new ArrayList<City>();
        City city1 = new City();
        city1.setName("Oakland");
        cities.add(city1);
        City city2 = new City();
        city2.setName("Fremont");
        cities.add(city2);
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(cities);
        replay(_geoDao);
        replay(_stateManager);

        _controller.handleRequest(getRequest(), getResponse());
        verify(_geoDao);
        verify(_stateManager);

        assertNotNull("Output null", getResponse().getContentAsString());
        assertTrue("Output empty", getResponse().getContentAsString().length() > 0);
        assertTrue("Output does not contain expected city name Oakland", getResponse().getContentAsString().indexOf("Oakland") > -1);
        assertTrue("Output does not contain expected city name Fremont", getResponse().getContentAsString().indexOf("Fremont") > -1);
    }

    public void testOutputCitySelect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // set up data
        List<City> cities = new ArrayList<City>();
        City city1 = new City();
        city1.setName("Oakland");
        cities.add(city1);
        City city2 = new City();
        city2.setName("Fremont");
        cities.add(city2);
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(cities);
        replay(_geoDao);
        replay(_stateManager);

        _controller.outputCitySelect(State.CA, pw);
        verify(_geoDao);
        verify(_stateManager);

        assertNotNull("Output null", sw.getBuffer());
        assertTrue("Output empty", sw.getBuffer().length() > 0);
        assertTrue("Output does not contain expected city name Oakland", sw.getBuffer().indexOf("Oakland") > -1);
        assertTrue("Output does not contain expected city name Fremont", sw.getBuffer().indexOf("Fremont") > -1);
    }

    public void testOpenSelectTag() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // test with all parameters
        _controller.openSelectTag(pw, "name", "id", "class", "onChange");
        assertTrue("Expected output not received",
                sw.getBuffer().toString().indexOf
                        ("<select name=\"name\" id=\"id\" class=\"class\" onchange=\"onChange\">") > -1);

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        // test with no parameters
        _controller.openSelectTag(pw, null, null, null, null);
        assertTrue("Expected output not received",
                sw.getBuffer().toString().indexOf
                        ("<select name=\"null\">") > -1);
    }

    public void testOutputOption() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // test with all parameters
        _controller.outputOption(pw, "value", "name", true);
        assertEquals("<option selected=\"selected\" value=\"value\">name</option>",
                sw.getBuffer().toString());

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        // test with no parameters
        _controller.outputOption(pw, null, null, false);
        assertEquals("<option value=\"null\">null</option>",
                sw.getBuffer().toString());

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        // test shorter method signature with no parameters
        _controller.outputOption(pw, null, null);
        assertEquals("<option value=\"null\">null</option>",
                sw.getBuffer().toString());
    }
}
