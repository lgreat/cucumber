/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.geo.bestplaces.BpCounty;
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
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        _controller.outputOption(pw, "value", "name", true);

        String expected = "<option selected=\"selected\" value=\"value\">name</option>";
        assertEquals(expected, sw.getBuffer().toString());

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        _controller.outputOption(pw, "value", "name", false);

        expected = "<option value=\"value\">name</option>";
        assertEquals(expected, sw.getBuffer().toString());
    }

    public void testOutputCountySelect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        getRequest().addParameter("state", "ca");
        _geoDao.findCounties(State.CA);
        List counties = new ArrayList();
        BpCounty county1 = new BpCounty();
        county1.setCountyFips("1234");
        county1.setName("Alameda");
        counties.add(county1);
        BpCounty county2 = new BpCounty();
        county2.setCountyFips("5678");
        county2.setName("Sacramento");
        counties.add(county2);
        _geoControl.setReturnValue(counties);
        _geoControl.replay();

        _controller.outputCountySelect(getRequest(), pw);
        _geoControl.verify();

        assertNotNull(sw.getBuffer());
        assertTrue(sw.getBuffer().length() > 0);
        assertTrue(sw.getBuffer().indexOf("Alameda") > -1);
        assertTrue(sw.getBuffer().indexOf("1234") > -1);
        assertTrue(sw.getBuffer().indexOf("Sacramento") > -1);
        assertTrue(sw.getBuffer().indexOf("5678") > -1);
    }

    public void testOutputCitySelect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

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

        assertNotNull(sw.getBuffer());
        assertTrue(sw.getBuffer().length() > 0);
        assertTrue(sw.getBuffer().indexOf("Oakland") > -1);
        assertTrue(sw.getBuffer().indexOf("Fremont") > -1);
    }
}
