package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.FavoriteSchool;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import static org.easymock.EasyMock.*;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

/**
 * Test class for MySchoolListController
 */
public class MySchoolListControllerTest extends BaseControllerTestCase {
    private MySchoolListController _controller;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new MySchoolListController();

        _schoolDao = createMock(ISchoolDao.class);
        _userDao = createMock(IUserDao.class);
        _geoDao = createStrictMock(IGeoDao.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setUserDao(_userDao);
        _controller.setGeoDao(_geoDao);

        _controller.setStateManager(new StateManager());
        
        _user = new User();
        _user.setId(1);
        _user.setEmail("aroy@greatschools.org");
    }

    public void testBasics() {
        _controller.setViewName("view");

        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_geoDao, _controller.getGeoDao());
        assertEquals("view", _controller.getViewName());
    }

    public void testRequestFromUnknownUserNoSchoolsAdded() throws Exception {
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("User should see MSL Intro page", MySchoolListController.INTRO_VIEW_NAME, mAndV.getViewName());
    }

    public void testRequestFromUnknownUserSchoolsAdded() throws Exception {
        getRequest().setParameter(MySchoolListController.PARAM_COMMAND, "add");
        getRequest().setParameter(MySchoolListController.PARAM_SCHOOL_IDS, "123,456");
        getRequest().setParameter(MySchoolListController.PARAM_STATE, "CA");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        RedirectView v = (RedirectView)mAndV.getView();
        assertEquals("User should see MSL Login page",
                "/mySchoolListLogin.page?command=add&ids=123,456&state=CA",
                v.getUrl());
    }

    public void testRequestFromKnownUserNoSchoolsAdded() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);
        expect(_schoolDao.getSchoolById(isA(State.class), isA(Integer.class))).andReturn(createStubSchool()).times(4);
        replay(_schoolDao);
        expect(_geoDao.findCity(State.NJ, "Beirut")).andReturn(createStubCity());
        replay(_geoDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        verify(_geoDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    City createStubCity() {
        City city = new City();
        city.setName("Beirut");
        city.setState(State.NJ);
        city.setId(2);
        return city;
    }

    School createStubSchool() {
        School school = new School();
        school.setName("East Beirut High");
        school.setDatabaseState(State.NJ);
        school.setLevelCode(LevelCode.PRESCHOOL);
        school.setCity("Beirut");
        return school;
    }

    City createStubCity2() {
        City city = new City();
        city.setName("Alameda");
        city.setState(State.CA);
        city.setId(1);
        return city;
    }

    School createStubSchool2() {
        School school = new School();
        school.setName("Alameda High Schoo");
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setLevelCode(LevelCode.HIGH);
        school.setCity("Alameda");
        return school;
    }

    FavoriteSchool createStubFavoriteSchool(int id, User user) {
        FavoriteSchool fs = new FavoriteSchool();
        fs.setId(id);
        fs.setSchoolId(id);
        fs.setLevelCode(LevelCode.ALL_LEVELS);
        fs.setState(State.CA);
        fs.setUser(user);
        return fs;
    }


    public void testRequestFromKnownUserSchoolsAdded() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);
        sc.setCity(createStubCity());
        expect(_schoolDao.getSchoolById(isA(State.class), isA(Integer.class))).andReturn(createStubSchool()).times(4);
        replay(_schoolDao);
//        expect(_geoDao.findCity(State.NJ, "Beirut")).andReturn(createStubCity());
        replay(_geoDao);
        getRequest().setParameter(MySchoolListController.PARAM_SCHOOL_IDS, "12,3456");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        verify(_geoDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    public void testRemoveSchool() throws Exception {

        getRequest().setParameter("command", "remove");
        getRequest().setParameter("ids", "1");
        getRequest().setParameter("state", "ca");

        User user = new User();
        user.setId(1);
        user.setEmail("eford@greatschools.org");
        Set<FavoriteSchool> favorites = new HashSet<FavoriteSchool>();
        favorites.add(createStubFavoriteSchool(1, user));
        favorites.add(createStubFavoriteSchool(2, user));
        favorites.add(createStubFavoriteSchool(3, user));
        favorites.add(createStubFavoriteSchool(4, user));
        user.setFavoriteSchools(favorites);

        getSessionContext().setUser(user);

        _userDao.updateUser(user);
        expect(_schoolDao.getSchoolById(isA(State.class), isA(Integer.class))).andReturn(createStubSchool2()).times(3);
        replay(_schoolDao);
        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(createStubCity());
        replay(_geoDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        verify(_geoDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    public void testAddSchools() throws Exception {
        getRequest().setParameter("command", "add");
        getRequest().setParameter("ids", "9,10");
        getRequest().setParameter("state", "ca");

        User user = new User();
        user.setId(1);
        user.setEmail("eford@greatschools.org");
        Set<FavoriteSchool> favorites = new HashSet<FavoriteSchool>();
        favorites.add(createStubFavoriteSchool(1, user));
        favorites.add(createStubFavoriteSchool(2, user));
        favorites.add(createStubFavoriteSchool(3, user));
        favorites.add(createStubFavoriteSchool(4, user));
        user.setFavoriteSchools(favorites);

        getSessionContext().setUser(user);

        _userDao.updateUser(user);

        expect(_schoolDao.getSchoolById(isA(State.class), isA(Integer.class))).andReturn(createStubSchool2()).anyTimes();
        replay(_schoolDao);
        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(createStubCity());
        replay(_geoDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        verify(_geoDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    public void testBuildModel() throws Exception {
        User user = new User();
        user.setEmail("eford@greatschools.org");
        user.setId(1);
        Set<FavoriteSchool> favs = new HashSet<FavoriteSchool>();
        FavoriteSchool fs1 = new FavoriteSchool();
        fs1.setSchoolId(1);
        fs1.setState(State.CA);
        fs1.setUser(user);
        favs.add(fs1);
        FavoriteSchool fs2 = new FavoriteSchool();
        fs2.setSchoolId(32);
        fs2.setState(State.AK);
        fs2.setUser(user);
        favs.add(fs2);
        user.setFavoriteSchools(favs);
        expect(_schoolDao.getSchoolById(State.AK, 32)).andReturn(createStubSchool());
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(createStubSchool());
        replay(_schoolDao);
        Map<String, Object> model = _controller.buildModel(user);
        verify(_schoolDao);
        List schools = (List)model.get("schools");
        assertEquals("Expected 2 msl schools", 2, schools.size());

    }

    public void testConvertFavoriteSchoolsToSchoolsNull() {
        List<School> schools = _controller.convertFavoriteSchoolsToSchools(null);
        assertNotNull("Expect not null list out when given null list", schools);
        assertEquals("Expect empty list out when given null list", 0, schools.size());
    }

    public void testConvertFavoriteSchoolsToSchoolsEmptyList() {
        List<FavoriteSchool> faves = new ArrayList<FavoriteSchool>();
        List<School> schools = _controller.convertFavoriteSchoolsToSchools(faves);
        assertNotNull("Expect not null list out when given empty list", schools);
        assertEquals("Expect empty list out when given empty list", 0, schools.size());
    }

    public void testConvertFavoriteSchoolsToSchoolsOneStateOneSchool() {
        List<FavoriteSchool> faves = new ArrayList<FavoriteSchool>();
        FavoriteSchool fave = new FavoriteSchool();
        fave.setState(State.CA);
        fave.setSchoolId(1);
        faves.add(fave);

        School school = new School();

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        replay(_schoolDao);
        List<School> schools = _controller.convertFavoriteSchoolsToSchools(faves);
        verify(_schoolDao);

        assertNotNull("Expect not null list out when given favorite school list in", schools);
        assertEquals("Expect same number of results as favorite schools", faves.size(), schools.size());
        assertSame(school, schools.get(0));
    }

    public void testConvertFavoriteSchoolsToSchoolsTwoStatesOneSchoolEach() {
        List<FavoriteSchool> faves = new ArrayList<FavoriteSchool>();
        FavoriteSchool fave = new FavoriteSchool();
        fave.setState(State.CA);
        fave.setSchoolId(1);
        faves.add(fave);

        fave = new FavoriteSchool();
        fave.setState(State.AK);
        fave.setSchoolId(2);
        faves.add(fave);

        School school1 = new School();
        School school2 = new School();

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school1);
        expect(_schoolDao.getSchoolById(State.AK, 2)).andReturn(school2);
        replay(_schoolDao);
        List<School> schools = _controller.convertFavoriteSchoolsToSchools(faves);
        verify(_schoolDao);

        assertNotNull("Expect not null list out when given favorite school list in", schools);
        assertEquals("Expect same number of results as favorite schools", faves.size(), schools.size());
        boolean hasAK = false;
        boolean hasCA = false;
        for (School school: schools) {
            if (school == school1) {
                hasCA = true;
            } else if (school == school2) {
                hasAK = true;
            }
        }
        assertTrue(hasCA);
        assertTrue(hasAK);
    }

    public void testConvertFavoriteSchoolsToSchoolsHandlesException() {
        List<FavoriteSchool> faves = new ArrayList<FavoriteSchool>();
        FavoriteSchool fave = new FavoriteSchool();
        fave.setState(State.CA);
        fave.setSchoolId(1);
        fave.setUser(_user);
        faves.add(fave);

        expect(_schoolDao.getSchoolById(State.CA, 1)).andThrow(new ObjectRetrievalFailureException(School.class, 1));
        replay(_schoolDao);
        List<School> schools = null;
        try {
            schools = _controller.convertFavoriteSchoolsToSchools(faves);
        } catch (Exception e) {
            e.printStackTrace();
            fail("No exception should be thrown: " + e.getClass().getName());
        }
        verify(_schoolDao);

        assertNotNull("Expect not null list out when exception thrown", schools);
        assertEquals("Expect empty list out when exception thrown", 0, schools.size());
    }

    public void testGetSchoolIds() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Set<Integer> s_1 = new HashSet<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(4);
        }};

        request.setParameter(MySchoolListController.PARAM_SCHOOL_IDS, "1,2,3,4");
        Set<Integer> ids = _controller.getSchoolIds(request);
        assertEquals("Set should contain 1,2,3,4", s_1, ids);

        request = new MockHttpServletRequest();
        request.setParameter("ids", new String[]{"1","2","5"});
        ids = _controller.getSchoolIds(request);
        Set<Integer> s_2 = new HashSet<Integer>() {{
            add(1);
            add(2);
            add(5);
        }};
        assertEquals("Set should contain 1,2,5", s_2, ids);
    }
}