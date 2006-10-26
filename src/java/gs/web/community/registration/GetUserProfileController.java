package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import gs.data.community.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GetUserProfileController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/getUserProfile.page";
    public static final String USER_ID_PARAMETER_NAME = "memberId";
    public static final String USER_LIST_PARAMETER_NAME = "members";
    public static final int ERROR_CODE = HttpServletResponse.SC_FORBIDDEN;

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private String _viewName;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map model = new HashMap();

        String paramValues[] = request.getParameterValues(USER_ID_PARAMETER_NAME);
        if (paramValues == null) {
            _log.warn("getUserProfile called with no parameter named " + USER_ID_PARAMETER_NAME);
            response.sendError(ERROR_CODE);
            return null;
        }
        List userInfoList = new ArrayList();
        for (int x=0; x < paramValues.length; x++) {
            try {
                Integer userId = new Integer(paramValues[x]);

                User user = _userDao.findUserFromId(userId.intValue());

                userInfoList.add(getUserProfileInfo(user, _subscriptionDao));
            } catch (Exception e) {
                _log.error("Error loading user profile with memberId=" + paramValues[x], e);
            }
        }
        model.put(USER_LIST_PARAMETER_NAME, userInfoList);
        return new ModelAndView(_viewName, model);
    }

    public static UserProfileInfo getUserProfileInfo(User user, ISubscriptionDao subscriptionDao) throws IllegalStateException {
        UserProfile userProfile = user.getUserProfile();
        int userId = user.getId().intValue();
        if (userProfile == null) {
            throw new IllegalStateException("getUserProfile called for user who has not " +
                    "completed registration (userProfile is null): id=" + userId);
        } else if (user.isEmailProvisional()) {
            throw new IllegalStateException("getUserProfile called for user who is " +
                    "still provisional: id=" + userId);
        }
        Set students = user.getStudents();
        List interestCodes = Arrays.asList(userProfile.getInterestsAsArray());
        List interests = new ArrayList();
        for (int y=0; y < interestCodes.size(); y++) {
            interests.add(new Interest((String)interestCodes.get(y),
                    (String)UserProfile.getInterestsMap().get(interestCodes.get(y))));
        }

        List previousSchools = subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.PREVIOUS_SCHOOLS);
        List myStats = subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        Set favoriteSchools = user.getFavoriteSchools();

        return new UserProfileInfo(user, userProfile, students, interests,
                previousSchools, myStats, favoriteSchools);
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public static class UserProfileInfo {
        private User _user;
        private UserProfile _userProfile;
        private Set _students;
        private List _interests;
        private List _previousSchools;
        private List _myStats;
        private Set _favoriteSchools;

        public UserProfileInfo(User user, UserProfile userProfile, Set students, List interests,
                               List previousSchools, List myStats, Set favoriteSchools) {
            _user = user;
            _userProfile = userProfile;
            _students = students;
            _interests = interests;
            _previousSchools = previousSchools;
            _myStats = myStats;
            _favoriteSchools = favoriteSchools;
        }

        public User getUser() {
            return _user;
        }

        public UserProfile getUserProfile() {
            return _userProfile;
        }

        public Set getStudents() {
            return _students;
        }

        public List getInterests() {
            return _interests;
        }

        public List getPreviousSchools() {
            return _previousSchools;
        }

        public List getMyStats() {
            return _myStats;
        }

        public Set getFavoriteSchools() {
            return _favoriteSchools;
        }
    }

    public static class Interest {
        private String _code;
        private String _value;

        public Interest(String code, String value) {
            _code = code;
            _value = value;
        }

        public String getCode() {
            return _code;
        }

        public String getValue() {
            return _value;
        }
    }
}
