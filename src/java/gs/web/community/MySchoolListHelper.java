package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MySchoolListHelper {

    IUserDao _userDao;

    protected void addToMSL(User user, Set<School> schools) {
        Set<FavoriteSchool> favoriteSchools = user.getFavoriteSchools();
        if (favoriteSchools == null) {
            favoriteSchools = new HashSet<FavoriteSchool>();
        }

        for (School school : schools) {
            FavoriteSchool favoriteSchool = new FavoriteSchool(school, user);
            favoriteSchools.add(favoriteSchool);
        }

        user.setFavoriteSchools(favoriteSchools);
        getUserDao().updateUser(user);
    }

    protected void addToMSL(User user, School school) {
        Set<FavoriteSchool> favoriteSchools = user.getFavoriteSchools();
        if (favoriteSchools == null) {
            favoriteSchools = new HashSet<FavoriteSchool>();
        }

        FavoriteSchool fav = new FavoriteSchool(school, user);
        favoriteSchools.add(fav);

        user.setFavoriteSchools(favoriteSchools);
        getUserDao().updateUser(user);
    }

    protected void removeFromMSL(User user, School school) {
        Set<FavoriteSchool> favoriteSchools = user.getFavoriteSchools();
        if (favoriteSchools == null) {
            favoriteSchools = new HashSet<FavoriteSchool>();
        }

        FavoriteSchool toRemove = new FavoriteSchool(school, user);
        if (favoriteSchools.contains(toRemove)) {
            favoriteSchools.remove(toRemove);
        }
        user.setFavoriteSchools(favoriteSchools);
        getUserDao().updateUser(user);
    }

    protected void removeFromMSL(User user, Set<School> schools) {
        Set<FavoriteSchool> favoriteSchools = user.getFavoriteSchools();
        if (favoriteSchools == null) {
            favoriteSchools = new HashSet<FavoriteSchool>();
        }
        
        for (School school : schools) {
            FavoriteSchool toRemove = new FavoriteSchool(school, user);
            if (favoriteSchools.contains(toRemove)) {
                favoriteSchools.remove(toRemove);
            }
        }
        user.setFavoriteSchools(favoriteSchools);
        getUserDao().updateUser(user);
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
