package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.FavoriteSchool;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.List;
import java.util.ArrayList;

/**
 * test class for LoginController
 *
 * @author David Lee <mailto:dlee@greatschools.net>
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