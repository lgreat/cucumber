package gs.web.school;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class FindSchoolControllerTest extends BaseControllerTestCase {
     /**/
    ISchoolDao _schoolDao;
    IGeoDao _geoDao;
    FindSchoolController.FindSchoolCommand _command;
    FindSchoolController _controller;

    public void setUp() throws Exception {
        super.setUp();

        _schoolDao = createMock(ISchoolDao.class);
        _geoDao = createMock(IGeoDao.class);

        _controller = new FindSchoolController();
        _controller.setCommandClass(FindSchoolController.FindSchoolCommand.class);
        _controller.setGeoDao(_geoDao);
        _controller.setSchoolDao(_schoolDao);
    }

    public void testHandleRequestReturnCityInState() throws Exception {
        getRequest().setParameter("state", "WY");
        getRequest().setParameter("filter", "city");
        getRequest().setMethod("GET");

        List<City> cities = Arrays.asList(createCity("San Francisco"), createCity("Oakland"), createCity("San Jose"));

        expect(_geoDao.findCitiesByState(State.WY)).andReturn(cities);
        replay(_geoDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_geoDao);

        assertEquals("{\"cities\":[{\"name\":\"San Francisco\"},{\"name\":\"Oakland\"},{\"name\":\"San Jose\"}]}", getResponse().getContentAsString());
    }

    public void testHandleRequestReturnSchoolList() throws Exception {
        getRequest().setParameter("state", "WY");
        getRequest().setParameter("filter", "school");
        getRequest().setParameter("level", "e");
        getRequest().setParameter("city", "San Francisco");
        getRequest().setMethod("GET");

        List<School> schools = Arrays.asList(createSchool("e"), createSchool("m"), createSchool("h"), createSchool("e,m"), createSchool("m,h"));

        expect(_schoolDao.findSchoolsInCity(State.WY, "San Francisco", false)).andReturn(schools);
        replay(_schoolDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_schoolDao);

        assertEquals("{\"schools\":[{\"id\":1,\"name\":\"e\"},{\"id\":1,\"name\":\"e,m\"}]}", getResponse().getContentAsString());
    }

    public City createCity(String cityName) {
        City city = new City();
        city.setName(cityName);

        return city;
    }

    public School createSchool(String levelCode) {
        School s = new School();
        s.setId(1);
        s.setName(levelCode);
        s.setLevelCode(LevelCode.createLevelCode(levelCode));
        return s;
    }
}
