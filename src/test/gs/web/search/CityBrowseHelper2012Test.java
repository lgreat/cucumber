package gs.web.search;

/**
 * Created with IntelliJ IDEA.
 * User: cliu
 * Date: 1/14/14
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.geo.City;
import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;

public class CityBrowseHelper2012Test {
    GsMockHttpServletRequest _request;
    CityBrowseHelper2012 _cityBrowseHelper;
    SchoolSearchCommandWithFields _schoolSearchCommandWithFields;
    DirectoryStructureUrlFields _fields;
    SchoolSearchCommand _schoolSearchCommand;

    @Before
    public void setUp() {
        _request = new GsMockHttpServletRequest();
        _fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, _fields);
        _schoolSearchCommand = new SchoolSearchCommand();
        _schoolSearchCommandWithFields = new SchoolSearchCommandWithFields(_schoolSearchCommand, _fields);

        _cityBrowseHelper = new CityBrowseHelper2012();
    }

    @Test
    public void testMetaDescCalc() {
        assertEquals("View and map all San Francisco, CA schools. Plus, compare or save schools.", _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, null, null));
        assertEquals("View and map all San Francisco, CA middle schools. Plus, compare or save middle schools.",
                _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.MIDDLE, null));
        assertEquals("View and map all San Francisco, CA public elementary schools. Plus, compare or save public elementary schools.",
                _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.ELEMENTARY, new String[]{"public"}));

        assertEquals("Find the best preschools in San Francisco, California (CA) - view preschool ratings, reviews and map locations.",
                _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("View and map all schools in the Oakland Unified School District, CA. Plus, compare or save schools in this district.",
                _cityBrowseHelper.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, null, null));
        assertEquals("View and map all middle schools in the Oakland Unified School District, CA. Plus, compare or save middle schools in this district.",
                _cityBrowseHelper.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, LevelCode.MIDDLE, null));
        assertEquals("View and map all public elementary schools in the Oakland Unified School District, CA. Plus, compare or save public elementary schools in this district.",
                _cityBrowseHelper.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, LevelCode.ELEMENTARY, new String[]{"public"}));
    }

}
