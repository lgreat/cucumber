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

        _controller.outputCitySelect(State.CA, pw, "1");
        _geoControl.verify();

        assertNotNull("Output null", sw.getBuffer());
        assertTrue("Output empty", sw.getBuffer().length() > 0);
        assertTrue("Output does not contain expected city name Oakland", sw.getBuffer().indexOf("Oakland") > -1);
        assertTrue("Output does not contain expected city name Fremont", sw.getBuffer().indexOf("Fremont") > -1);
    }

    public void testOutputSchoolSelect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // set up data
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

        assertNotNull("Output null", sw.getBuffer());
        assertTrue("Output empty", sw.getBuffer().length() > 0);
        assertTrue("Output does not contain expected high school Alameda High School",
                sw.getBuffer().indexOf("Alameda High School") > -1);
        assertTrue("Output does not contain expected high school Roy's Home for the Gifted",
                sw.getBuffer().indexOf("Roy's Home for the Gifted") > -1);
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
