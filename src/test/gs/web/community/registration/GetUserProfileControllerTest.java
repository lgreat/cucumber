package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.*;
import gs.data.state.State;
import gs.data.school.Grade;
import gs.data.school.School;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.easymock.MockControl;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GetUserProfileControllerTest  extends BaseControllerTestCase {
    private GetUserProfileController _controller;
    private MockControl _userControl;
    private IUserDao _mockUserDao;
    private MockControl _subscriptionControl;
    private ISubscriptionDao _mockSubscriptionDao;


    public void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (GetUserProfileController) appContext.getBean(GetUserProfileController.BEAN_ID);
        _userControl = MockControl.createControl(IUserDao.class);
        _mockUserDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_mockUserDao);
        _subscriptionControl = MockControl.createControl(ISubscriptionDao.class);
        _mockSubscriptionDao = (ISubscriptionDao)_subscriptionControl.getMock();
        _controller.setSubscriptionDao(_mockSubscriptionDao);
    }

    public void testGetUserProfile() throws IOException {
        User user = new User();
        user.setId(new Integer(99));
        user.setEmail("GetUserProfileControllerTest@greatschools.net");
        user.setFirstName("First");
        user.setLastName("Last");
        Student student = new Student();
        student.setName("name");
        student.setGrade(Grade.G_10);
        student.setState(State.CA);
        student.setSchoolId(new Integer(1));
        user.addStudent(student);
        Set favoriteSchools = new HashSet();
        School school = new School();
        school.setId(new Integer(2));
        favoriteSchools.add(school);
        user.setFavoriteSchools(favoriteSchools);
        UserProfile userProfile = new UserProfile();
        userProfile.setScreenName("gupct");
        userProfile.setCity("Oakland");
        userProfile.setState(State.CA);
        userProfile.setOtherInterest("Computers");
        userProfile.setAboutMe("About me");
        userProfile.setPrivate(false);
        userProfile.setNumSchoolChildren(new Integer(2));
        userProfile.setNumPreKChildren(new Integer(1));
        userProfile.addInterest("PTA");
        user.setUserProfile(userProfile);

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.PREVIOUS_SCHOOLS);
        _subscriptionControl.setReturnValue(null);
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(null);
        _subscriptionControl.replay();

        getRequest().addParameter("memberId", "99");

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        _userControl.verify();
        _subscriptionControl.verify();
        assertEquals(HttpServletResponse.SC_OK, getResponse().getStatus());
        assertNotNull(mAndV);
        assertNotNull(mAndV.getViewName());
        assertNotNull(mAndV.getModel());
        List userList = (List) mAndV.getModel().get(GetUserProfileController.USER_LIST_PARAMETER_NAME);
        assertNotNull(userList);
        GetUserProfileController.UserProfileInfo info =
                (GetUserProfileController.UserProfileInfo)userList.get(0);
        assertEquals(user, info.getUser());
        assertEquals(userProfile, info.getUserProfile());
        assertNotNull(info.getInterests());
        assertEquals(1, info.getInterests().size());
        assertNotNull(info.getStudents());
        assertEquals(1, info.getStudents().size());
        assertNotNull(info.getFavoriteSchools());
        assertEquals(1, info.getFavoriteSchools().size());
        assertNull(info.getPreviousSchools());
        assertNull(info.getMyStats());
    }

    public void testNoUser() throws IOException {
        _mockUserDao.findUserFromId(99);
        _userControl.setThrowable(new ObjectRetrievalFailureException("Can't find user with id 99", null));
        _userControl.replay();

        getRequest().addParameter("memberId", "99");

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        _userControl.verify();
        assertEquals(HttpServletResponse.SC_OK, getResponse().getStatus());
        assertNotNull(mAndV);
        assertNotNull(mAndV.getViewName());
        assertNotNull(mAndV.getModel());
        List userList = (List) mAndV.getModel().get(GetUserProfileController.USER_LIST_PARAMETER_NAME);
        assertNotNull(userList);
        assertEquals(0, userList.size());
    }

    public void testNoParameter() throws IOException {
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, getResponse().getStatus());
        assertNull(mAndV);
    }

    public void testNullUserProfile() throws IOException {
        User user = new User();
        user.setId(new Integer(1234));

        _mockUserDao.findUserFromId(1234);
        _userControl.setReturnValue(user);
        _userControl.replay();

        getRequest().addParameter("memberId", "1234");

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals(HttpServletResponse.SC_OK, getResponse().getStatus());
        assertNotNull(mAndV);
        assertNotNull(mAndV.getViewName());
        assertNotNull(mAndV.getModel());
        List userList = (List) mAndV.getModel().get(GetUserProfileController.USER_LIST_PARAMETER_NAME);
        assertNotNull(userList);
        assertEquals(0, userList.size());
    }

    public void testMultipleParameters() throws IOException {
        User user = new User();
        user.setId(new Integer(1234));
        UserProfile userProfile = new UserProfile();
        userProfile.setId(new Integer(1234));
        user.setUserProfile(userProfile);

        _mockUserDao.findUserFromId(1234);
        _userControl.setReturnValue(user);

        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.PREVIOUS_SCHOOLS);
        _subscriptionControl.setReturnValue(null);
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(null);

        getRequest().addParameter("memberId", "1234");

        user = new User();
        user.setId(new Integer(1235));
        userProfile = new UserProfile();
        userProfile.setId(new Integer(1235));
        user.setUserProfile(userProfile);

        _mockUserDao.findUserFromId(1235);
        _userControl.setReturnValue(user);

        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.PREVIOUS_SCHOOLS);
        _subscriptionControl.setReturnValue(null);
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(null);

        _userControl.replay();
        _subscriptionControl.replay();
        getRequest().addParameter("memberId", "1235");

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals(HttpServletResponse.SC_OK, getResponse().getStatus());
        assertNotNull(mAndV);
        assertNotNull(mAndV.getViewName());
        assertNotNull(mAndV.getModel());
        List userList = (List) mAndV.getModel().get(GetUserProfileController.USER_LIST_PARAMETER_NAME);
        assertNotNull(userList);
        assertEquals(2, userList.size());
    }
}
