package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.FavoriteSchool;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import static org.easymock.EasyMock.*;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

/**
 * Test class for MySchoolListController
 */
public class MySchoolListControllerTest extends BaseControllerTestCase {
    private MySchoolListController _controller;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new MySchoolListController();

        _schoolDao = createMock(ISchoolDao.class);
        _userDao = createMock(IUserDao.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setUserDao(_userDao);

        _controller.setStateManager(new StateManager());
        
        _user = new User();
        _user.setId(1);
        _user.setEmail("aroy@greatschools.net");
    }

    public void testBasics() {
        _controller.setViewName("view");

        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_userDao, _controller.getUserDao());
        assertEquals("view", _controller.getViewName());
    }

    public void testRequestFromUnknownUserNoSchoolsAdded() throws Exception {
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("User should see MSL Intro page", MySchoolListController.INTRO_VIEW_NAME, mAndV.getViewName());
    }

    public void testRequestFromUnknownUserSchoolsAdded() throws Exception {
//        getRequest().setQueryString("command=add&ids=123,456&state=ca");
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
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    private School createStubSchool() {
        School school = new School();
        school.setName("East Beirut High");
        school.setDatabaseState(State.NJ);
        school.setLevelCode(LevelCode.ELEMENTARY_MIDDLE);
        return school;
    }

    public void testRequestFromKnownUserSchoolsAdded() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);
        expect(_schoolDao.getSchoolById(isA(State.class), isA(Integer.class))).andReturn(createStubSchool()).times(4);
        replay(_schoolDao);
        getRequest().setParameter(MySchoolListController.PARAM_SCHOOL_IDS, "12,3456");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    public void xtestRemoveSchool() throws Exception {

        getRequest().setParameter("command", "remove");
        getRequest().setParameter("ids", "1");
        getRequest().setParameter("state", "ca");

        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);

        _userDao.updateUser(sc.getUser());
        expect(_schoolDao.getSchoolById(isA(State.class), isA(Integer.class))).andReturn(new School()).times(3);
        replay(_schoolDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_schoolDao);
        assertEquals("User should see the main MSL page", MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());        
    }

    public void testAddSchools() {

    }

    public void testBuildModel() throws Exception {
        User user = new User();
        user.setEmail("eford@greatschools.net");
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
}