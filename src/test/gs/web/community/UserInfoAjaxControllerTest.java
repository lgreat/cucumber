package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UserInfoAjaxControllerTest extends BaseControllerTestCase {
    private UserInfoAjaxController _controller;
    private IUserDao _userDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new UserInfoAjaxController();

        _userDao = createStrictMock(IUserDao.class);

        _controller.setUserDao(_userDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao);
    }

    private void replayAllMocks() {
        replayMocks(_userDao);
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
    }

    public void testNoSessionContext() throws Exception {
        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, null);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNoUser() throws Exception {
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNoUserProfile() throws Exception {
        User user = new User();
        user.setEmail("testNoUserProfile@greatschools.net");
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNoAboutMe() throws Exception {
        User user = new User();
        user.setEmail("testNoUserProfile@greatschools.net");
        UserProfile userProfile = new UserProfile();
        userProfile.setAboutMe("duh");
        user.setUserProfile(userProfile);
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testHandlePost() throws Exception {
        User user = new User();
        user.setEmail("testNoUserProfile@greatschools.net");
        UserProfile userProfile = new UserProfile();
        userProfile.setAboutMe("duh");
        user.setUserProfile(userProfile);
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        getRequest().setParameter(UserInfoAjaxController.PARAM_ABOUT_ME, "This is about me.");
        _userDao.saveUser(user);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("This is about me.", user.getUserProfile().getAboutMe());
    }
}
