package gs.web.util;

import gs.web.BaseTestCase;
import gs.data.state.State;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;

import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Sep 24, 2008
 * Time: 5:08:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryStructureUrlFactoryTest extends BaseTestCase {
    public void setUp() {

    }
    
    public void testCreateNewCityBrowseURI() throws Exception {
        State state = State.CA;
        String cityName = "San Francisco";
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        LevelCode levelCode = null;

        // no filters

        String expectedRedirectURI = "/california/san-francisco/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        // type filters

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        expectedRedirectURI = "/california/san-francisco/public/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PRIVATE);
        expectedRedirectURI = "/california/san-francisco/private/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.CHARTER);
        expectedRedirectURI = "/california/san-francisco/charter/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.CHARTER);
        schoolTypes.add(SchoolType.PUBLIC);
        expectedRedirectURI = "/california/san-francisco/public-charter/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PRIVATE);
        schoolTypes.add(SchoolType.CHARTER);
        expectedRedirectURI = "/california/san-francisco/private-charter/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.PRIVATE);
        expectedRedirectURI = "/california/san-francisco/public-private/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.PRIVATE);
        schoolTypes.add(SchoolType.CHARTER);
        expectedRedirectURI = "/california/san-francisco/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        // level filters

        schoolTypes.clear();

        levelCode = LevelCode.PRESCHOOL;
        expectedRedirectURI = "/california/san-francisco/preschools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        levelCode = LevelCode.ELEMENTARY;
        expectedRedirectURI = "/california/san-francisco/elementary-schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        levelCode = LevelCode.MIDDLE;
        expectedRedirectURI = "/california/san-francisco/middle-schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        levelCode = LevelCode.HIGH;
        expectedRedirectURI = "/california/san-francisco/high-schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        // combined filters

        schoolTypes.clear();
        schoolTypes.add(SchoolType.CHARTER);
        schoolTypes.add(SchoolType.PUBLIC);
        levelCode = LevelCode.ELEMENTARY;
        expectedRedirectURI = "/california/san-francisco/public-charter/elementary-schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.CHARTER);
        levelCode = LevelCode.MIDDLE;
        expectedRedirectURI = "/california/san-francisco/charter/middle-schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));

        // hyphenated city
        schoolTypes.clear();
        levelCode = null;
        cityName = "Cardiff-By-The-Sea";
        expectedRedirectURI = "/california/cardiff_by_the_sea/schools/";
        assertEquals(expectedRedirectURI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode));
    }

    public void testCreateNewCityBrowseURIRoot() {
        boolean foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(null, "city name");
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when null state", foundException);

        foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(State.CA, null);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when null city name", foundException);

        foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(State.CA, "");
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when blank city name", foundException);

        assertEquals("/california/san-francisco/", DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(State.CA, "San Francisco"));
        assertEquals("/california/cardiff_by_the_sea/", DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(State.CA, "Cardiff-By-The-Sea"));
    }

    public void testCreateNewCityBrowseURIIllegalArguments() throws Exception {
        boolean foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURI(null, null, null, null);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException", foundException);

        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        schoolTypes.add(SchoolType.PRIVATE);
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.CHARTER);

        foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURI(null, "San Francisco", schoolTypes, LevelCode.ELEMENTARY_MIDDLE_HIGH);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException", foundException);

        foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURI(State.CA, "", schoolTypes, LevelCode.ELEMENTARY_MIDDLE_HIGH);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException", foundException);
    }

    public void testCreateNewCityBrowseURISchoolTypeLabel() throws Exception {
        boolean foundException = false;
        try {
            DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(null);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when null set of school types", foundException);

        // no need to test set has <= 3 school types because only 3 exist right now: public, private, charter

        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        assertEquals("", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.add(SchoolType.PUBLIC);
        assertEquals("public", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PRIVATE);
        assertEquals("private", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.CHARTER);
        assertEquals("charter", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.PRIVATE);
        assertEquals("public-private", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.CHARTER);
        assertEquals("public-charter", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PRIVATE);
        schoolTypes.add(SchoolType.CHARTER);
        assertEquals("private-charter", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.PRIVATE);
        schoolTypes.add(SchoolType.CHARTER);
        assertEquals("", DirectoryStructureUrlFactory.createNewCityBrowseURISchoolTypeLabel(schoolTypes));
    }

    public void testCreateNewCityBrowseURILevelLabel() throws Exception {
        assertEquals("schools", DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(null));
        assertEquals("preschools", DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(LevelCode.PRESCHOOL));
        assertEquals("elementary-schools", DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(LevelCode.ELEMENTARY));
        assertEquals("middle-schools", DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(LevelCode.MIDDLE));
        assertEquals("high-schools", DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(LevelCode.HIGH));
        assertEquals("schools", DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(LevelCode.ELEMENTARY_MIDDLE_HIGH));
    }
}
