package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.easymock.EasyMock.*;

public class MySchoolListHelperTest extends TestCase {
    IUserDao _userDao;
    MySchoolListHelper _mySchoolListHelper;

    public void setUp() throws Exception {
        _userDao = createStrictMock(IUserDao.class);
        _mySchoolListHelper = new MySchoolListHelper();
        _mySchoolListHelper.setUserDao(_userDao);
    }

    private User getNewTestUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@greatschools.org");
        return user;
    }

    public void testAddToMSL() {
        User user = getNewTestUser();

        School school = new School();
        school.setDatabaseState(State.NV);
        school.setId(1);
        school.setLevelCode(LevelCode.ELEMENTARY);

        _userDao.updateUser(eq(user));
        replay(_userDao);

        _mySchoolListHelper.addToMSL(user, school);
        assertEquals("User should have one more favorite school", 1, user.getFavoriteSchools().size());

        Iterator<FavoriteSchool> iterator = user.getFavoriteSchools().iterator();
        FavoriteSchool fav = iterator.next();
        assertEquals("Favorite should contain proper values", fav.getSchoolId(), school.getId());
        assertEquals("Favorite should contain proper values", fav.getState(), school.getDatabaseState());
        assertEquals("Favorite should contain proper values", fav.getLevelCode(), school.getLevelCode());
        
        verify(_userDao);
    }

    public void testAddToMSL2() {
        User user = getNewTestUser();

        Set<School> schools = new HashSet<School>();
        School school = new School();
        school.setDatabaseState(State.NV);
        school.setId(1);
        schools.add(school);

        School school2 = new School();
        school2.setDatabaseState(State.WA);
        school2.setId(2);
        schools.add(school2);

        _userDao.updateUser(eq(user));
        replay(_userDao);
        _mySchoolListHelper.addToMSL(user, schools);
        assertEquals("User should have one more favorite school", 2, user.getFavoriteSchools().size());
        verify(_userDao);

        reset(_userDao);
        schools.clear();

        School school3 = new School();
        school3.setDatabaseState(State.WA);
        school3.setId(3);

        schools.add(school2);
        schools.add(school3);

        _userDao.updateUser(eq(user));
        replay(_userDao);

        _mySchoolListHelper.addToMSL(user, schools);
        assertEquals("User should have one more favorite school", 3, user.getFavoriteSchools().size());

    }

    public void testRemoveFromMSL() {
        User user = getNewTestUser();

        School school = new School();
        school.setDatabaseState(State.NV);
        school.setId(1);
        school.setLevelCode(LevelCode.ELEMENTARY);

        Set<FavoriteSchool> favoriteSchools = new HashSet<FavoriteSchool>();
        FavoriteSchool favoriteSchool = new FavoriteSchool(school, user);
        favoriteSchools.add(favoriteSchool);
        user.setFavoriteSchools(favoriteSchools);

        _userDao.updateUser(eq(user));
        replay(_userDao);

        _mySchoolListHelper.removeFromMSL(user, school);
        assertEquals("User should have one less favorite school", 0, user.getFavoriteSchools().size());

        verify(_userDao);
    }

    public void testRemoveFromMSL2() {
        User user = getNewTestUser();

        School school = new School();
        school.setDatabaseState(State.NV);
        school.setId(1);
        school.setLevelCode(LevelCode.ELEMENTARY);

        School school2 = new School();
        school2.setDatabaseState(State.WA);
        school2.setId(2);
        school.setLevelCode(LevelCode.ELEMENTARY);
        Set<School> schools = new HashSet<School>();
        schools.add(school);
        schools.add(school2);

        Set<FavoriteSchool> favoriteSchools = new HashSet<FavoriteSchool>();
        FavoriteSchool favoriteSchool = new FavoriteSchool(school, user);
        FavoriteSchool favoriteSchool2 = new FavoriteSchool(school2, user);
        favoriteSchools.add(favoriteSchool);
        favoriteSchools.add(favoriteSchool2);
        user.setFavoriteSchools(favoriteSchools);

        _userDao.updateUser(eq(user));
        replay(_userDao);

        _mySchoolListHelper.removeFromMSL(user, schools);
        assertEquals("User should have two less favorite school", 0, user.getFavoriteSchools().size());

        verify(_userDao);
    }


}
