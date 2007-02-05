/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.StateManager;
import gs.data.state.State;
import org.easymock.MockControl;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides test class for stage 1 registration's AJAX controller.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationAjaxControllerTest extends BaseControllerTestCase {
    private RegistrationAjaxController _controller;
    private IGeoDao _geoDao;
    private MockControl _geoControl;

    public void setUp() throws Exception {
        super.setUp();
        StateManager stateManager = new StateManager();
        _geoControl = MockControl.createControl(IGeoDao.class);
        _geoDao = (IGeoDao) _geoControl.getMock();

        _controller = new RegistrationAjaxController();
        _controller.setGeoDao(_geoDao);
        _controller.setStateManager(stateManager);
    }

    public void testOutputOption() {
        // test selected attributed added correctly
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        _controller.outputOption(pw, "value", "name", true);
        String expected = "<option selected=\"selected\" value=\"value\">name</option>";
        assertEquals(expected, sw.getBuffer().toString());

        // test selected attribute omitted
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        _controller.outputOption(pw, "value", "name", false);
        expected = "<option value=\"value\">name</option>";
        assertEquals(expected, sw.getBuffer().toString());

        // test method with fewer options defaults to omitting selected
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        _controller.outputOption(pw, "value", "name");
        expected = "<option value=\"value\">name</option>";
        assertEquals(expected, sw.getBuffer().toString());
    }

    public void testOutputCitySelect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // set up data
        getRequest().addParameter("state", "CA");
        _geoDao.findCitiesByState(State.CA);
        List cities = new ArrayList();
        City city1 = new City();
        city1.setName("Oakland");
        cities.add(city1);
        City city2 = new City();
        city2.setName("Fremont");
        cities.add(city2);
        _geoControl.setReturnValue(cities);
        _geoControl.replay();

        _controller.outputCitySelect(getRequest(), pw);
        _geoControl.verify();

        // I don't do a character-by-character comparison because I feel that would
        // be too brittle. Instead, I just verify the data is making it through.
        assertNotNull("Output null", sw.getBuffer());
        assertTrue("Output empty", sw.getBuffer().length() > 0);
        assertTrue("Output does not contain expected city name Oakland",
                sw.getBuffer().indexOf("Oakland") > -1);
        assertTrue("Output does not contain expected city name Fremont",
                sw.getBuffer().indexOf("Fremont") > -1);
    }
}
