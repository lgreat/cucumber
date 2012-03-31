package gs.web.search;


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

public class CityBrowseHelperTest {
    GsMockHttpServletRequest _request;
    CityBrowseHelper _cityBrowseHelper;
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

        _cityBrowseHelper = new CityBrowseHelper(_schoolSearchCommandWithFields);
    }

    @Test
    public void testTitleCalcCode() {
        // These all have standard headers
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, null, null));
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.ELEMENTARY_MIDDLE, null));
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.MIDDLE_HIGH, null));

        // These useful views get nice SEO friendly titles
        assertEquals("San Francisco Elementary Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.ELEMENTARY, null));
        assertEquals("San Francisco Middle Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.MIDDLE, null));
        assertEquals("San Francisco High Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.HIGH, null));
        assertEquals("San Francisco Preschools and Daycare Centers - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("San Francisco Public Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, null, new String[]{"public"}));
        assertEquals("San Francisco Private Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, null, new String[]{"private"}));
        assertEquals("San Francisco Public Charter Schools - San Francisco, CA | GreatSchools", CityBrowseHelper.getTitle("San Francisco", State.CA, null, new String[]{"charter"}));

        assertEquals("Washington, DC Preschools and Daycare Centers - Washington, DC | GreatSchools", CityBrowseHelper.getTitle("Washington, DC", State.DC, LevelCode.PRESCHOOL, null));

        assertEquals("San Francisco Public and Private Schools - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, null, new String[]{"public", "private"}));
        assertEquals("San Francisco Public and Public Charter Schools - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, null, new String[]{"public", "charter"}));
        assertEquals("San Francisco Private and Public Charter Schools - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, null, new String[]{"private", "charter"}));

        assertEquals("San Francisco Public and Private Elementary Schools - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.ELEMENTARY, new String[]{"public", "private"}));
        assertEquals("San Francisco Public and Public Charter Middle Schools - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.MIDDLE, new String[]{"public", "charter"}));
        assertEquals("San Francisco Private and Public Charter High Schools - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.HIGH, new String[]{"private", "charter"}));
        assertEquals("San Francisco Private Preschools and Daycare Centers - San Francisco, CA | GreatSchools",
                CityBrowseHelper.getTitle("San Francisco", State.CA, LevelCode.PRESCHOOL, new String[]{"private"}));
    }

    @Test
    public void testGetRelCanonicalForCityBrowse() {
        City city = new City();
        city.setName("Alameda");
        city.setId(0);
        city.setState(State.CA);

        _schoolSearchCommandWithFields.setCityFromUrl(city);
        _schoolSearchCommand.setState("CA");

        assertEquals("http://localhost/california/alameda/schools/", _cityBrowseHelper.getRelCanonical(_request));
    }

    @Test
    public void testMetaDescCalc() {
        assertEquals("View and map all San Francisco schools. Plus, compare or save schools.", _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, null, null));
        assertEquals("View and map all San Francisco middle schools. Plus, compare or save middle schools.",
                _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.MIDDLE, null));
        assertEquals("View and map all San Francisco public elementary schools. Plus, compare or save public elementary schools.",
                _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.ELEMENTARY, new String[]{"public"}));

        assertEquals("Find the best preschools in San Francisco, California (CA) - view preschool ratings, reviews and map locations.",
                _cityBrowseHelper.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("View and map all schools in the Oakland Unified School District. Plus, compare or save schools in this district.",
                _cityBrowseHelper.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, null, null));
        assertEquals("View and map all middle schools in the Oakland Unified School District. Plus, compare or save middle schools in this district.",
                _cityBrowseHelper.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, LevelCode.MIDDLE, null));
        assertEquals("View and map all public elementary schools in the Oakland Unified School District. Plus, compare or save public elementary schools in this district.",
                _cityBrowseHelper.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, LevelCode.ELEMENTARY, new String[]{"public"}));
    }

}
