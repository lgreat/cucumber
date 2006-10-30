/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.Grade;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import org.easymock.MockControl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides test class for stage 2 registration's AJAX controller.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Registration2AjaxControllerTest extends BaseControllerTestCase {
    private Registration2AjaxController _controller;
    private ISchoolDao _schoolDao;
    private MockControl _schoolControl;
    private IGeoDao _geoDao;
    private MockControl _geoControl;

    public void setUp() throws Exception {
        super.setUp();
        StateManager stateManager = new StateManager();
        _schoolControl = MockControl.createControl(ISchoolDao.class);
        _schoolDao = (ISchoolDao) _schoolControl.getMock();
        _geoControl = MockControl.createControl(IGeoDao.class);
        _geoDao = (IGeoDao) _geoControl.getMock();

        _controller = new Registration2AjaxController();
        _controller.setSchoolDao(_schoolDao);
        _controller.setGeoDao(_geoDao);
        _controller.setStateManager(stateManager);
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

        _controller.outputCitySelect(State.CA, pw, "1");
        _geoControl.verify();

        assertNotNull(sw.getBuffer());
        assertTrue(sw.getBuffer().length() > 0);
        assertTrue(sw.getBuffer().indexOf("Oakland") > -1);
        assertTrue(sw.getBuffer().indexOf("Fremont") > -1);
    }

    public void testOutputSchoolSelect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        _schoolDao.findSchoolsInCityByGrade(State.CA, "Alameda", Grade.G_12);
        List schools = new ArrayList();
        School school1 = new School();
        school1.setName("Alameda High School");
        school1.setId(new Integer(1));
        schools.add(school1);
        School school2 = new School();
        school2.setName("Roy's Home for the Gifted");
        school2.setId(new Integer(314));
        schools.add(school2);
        _schoolControl.setReturnValue(schools);
        _schoolControl.replay();

        _controller.outputSchoolSelect(State.CA, "Alameda", "12", pw, "1");
        _schoolControl.verify();

        assertNotNull(sw.getBuffer());
        assertTrue(sw.getBuffer().length() > 0);
        assertTrue(sw.getBuffer().indexOf("Alameda High School") > -1);
        assertTrue(sw.getBuffer().indexOf("Roy's Home for the Gifted") > -1);
    }
}
