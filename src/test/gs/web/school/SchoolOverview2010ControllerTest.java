package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolMedia;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

public class SchoolOverview2010ControllerTest extends BaseControllerTestCase {

    private SchoolOverview2010Controller _controller;

    private IGeoDao _geoDao;

    private ISchoolMediaDao _schoolMediaDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverview2010Controller) getApplicationContext().getBean(SchoolOverview2010Controller.BEAN_ID);
        _geoDao = createStrictMock(IGeoDao.class);
        _schoolMediaDao = createStrictMock(ISchoolMediaDao.class);
        _controller.setGeoDao(_geoDao);
        _controller.setSchoolMediaDao(_schoolMediaDao);
    }

    public void testShouldIndex() {
        BpCity bpCity = new BpCity();

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(999);
        school.setCity("San Francisco");
        school.setStateAbbreviation(State.CA);
        school.setLevelCode(LevelCode.ELEMENTARY);

        replay(_geoDao);
        assertTrue("Should index a non-preschool without reviews", _controller.shouldIndex(school, 0L));
        verify(_geoDao);
        reset(_geoDao);

        school.setLevelCode(LevelCode.HIGH);
        replay(_geoDao);
        assertTrue("Should index a non-preschool with reviews", _controller.shouldIndex(school, 1L));
        verify(_geoDao);
        reset(_geoDao);

        // Start Preschool tests
        school.setLevelCode(LevelCode.PRESCHOOL);

        replay(_geoDao);
        assertTrue("Should index a preschool with reviews", _controller.shouldIndex(school, 1L));
        verify(_geoDao);
        reset(_geoDao);

        bpCity.setPopulation(SchoolOverview2010Controller.PRESCHOOL_CITY_POPULATION_BOUNDARY);
        //expect(_geoDao.findBpCity(school.getStateAbbreviation(), school.getCity())).andReturn(bpCity);
        replay(_geoDao);
        assertTrue("Should index a preschool with no reviews but large city", _controller.shouldIndex(school, 0L));
        verify(_geoDao);
        reset(_geoDao);

        //GS-12127 revert back to indexing all preschools
        bpCity.setPopulation(SchoolOverview2010Controller.PRESCHOOL_CITY_POPULATION_BOUNDARY - 1);
        //expect(_geoDao.findBpCity(school.getStateAbbreviation(), school.getCity())).andReturn(bpCity);
        replay(_geoDao);
        assertTrue("Should index a preschool with no reviews and small city", _controller.shouldIndex(school, 0L));
        verify(_geoDao);
        reset(_geoDao);

        replay(_geoDao);
        assertFalse("Should not index if school is null", _controller.shouldIndex(null, 0L));
        verify(_geoDao);
        reset(_geoDao);

        //GS-12127 revert back to indexing all preschools
        bpCity.setPopulation(SchoolOverview2010Controller.PRESCHOOL_CITY_POPULATION_BOUNDARY - 1);
        //expect(_geoDao.findBpCity(school.getStateAbbreviation(), school.getCity())).andReturn(bpCity);
        replay(_geoDao);
        assertTrue("Should index if preschool has null reviews and small city", _controller.shouldIndex(school, null));
        verify(_geoDao);
        reset(_geoDao);
    }

    public void testGetSchoolPhotos() throws Exception {
        School school = new School();
        List<SchoolMedia> schoolMedias = new ArrayList<SchoolMedia>();

        expect(_schoolMediaDao.getActiveBySchool(eq(school), eq(SchoolOverview2010Controller.MAX_SCHOOL_PHOTOS_IN_GALLERY))).andReturn(schoolMedias);
        replay(_schoolMediaDao);

        List<SchoolMedia> result = _controller.getSchoolPhotos(school);
        assertEquals(schoolMedias, result);

        verify(_schoolMediaDao);
    }
}
