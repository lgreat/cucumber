package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.CommunityUtil;
import gs.web.BaseControllerTestCase;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;

import static gs.web.community.AvatarController.MEMBER_ID_PARAM;
import static gs.web.community.AvatarController.WIDTH_PARAM;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AvatarControllerTest extends BaseControllerTestCase {
    private AvatarController _controller;
    private IUserDao _userDao;
    private User _user;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new AvatarController();
        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);
        _user = new User();
        _user.setUserProfile(new UserProfile());
    }

    public void testBasics() {
        assertNotNull(_controller);
        assertSame(_userDao, _controller.getUserDao());
    }

    public void testNoMemberId() throws Exception {
        getRequest().setParameter(WIDTH_PARAM, "48");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals("redirect:" + CommunityUtil.getAvatarURLPrefix() + "stock/default-48.jpg",
                     mAndV.getViewName());
    }

    public void testCustomAvatar() throws Exception {
        getRequest().setParameter(MEMBER_ID_PARAM, "1234");
        _user.getUserProfile().setAvatarType("custom");
        _user.getUserProfile().setAvatarVersion(4);
        _user.setId(1234);
        expect(_userDao.findUserFromId(1234)).andReturn(_user);
        replayMocks(_userDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertNotNull(mAndV);
        assertEquals("redirect:" + CommunityUtil.getAvatarURLPrefix() + "34/12/1234-48.jpg?v=4",
                     mAndV.getViewName());
    }

    public void testStockAvatar() throws Exception {
        getRequest().setParameter(MEMBER_ID_PARAM, "1234");
        _user.getUserProfile().setAvatarType("stk-sunflower");
        _user.getUserProfile().setAvatarVersion(4);
        _user.setId(1234);
        expect(_userDao.findUserFromId(1234)).andReturn(_user);
        replayMocks(_userDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertNotNull(mAndV);
        assertEquals("redirect:" + CommunityUtil.getAvatarURLPrefix() + "stock/stk-sunflower-48.jpg",
                     mAndV.getViewName());
    }

    public void testUserDoesNotExist() throws Exception {
        getRequest().setParameter(MEMBER_ID_PARAM, "1234");
        _user.getUserProfile().setAvatarType("custom");
        _user.getUserProfile().setAvatarVersion(4);
        _user.setId(1234);
        expect(_userDao.findUserFromId(1234)).andThrow(new ObjectRetrievalFailureException(User.class, 1234));
        replayMocks(_userDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertNotNull(mAndV);
        assertEquals("redirect:" + CommunityUtil.getAvatarURLPrefix() + "stock/default-48.jpg",
                     mAndV.getViewName());
    }

    public void testUserHasNoUserProfile() throws Exception {
        getRequest().setParameter(MEMBER_ID_PARAM, "1234");
        _user.getUserProfile().setAvatarType("custom");
        _user.getUserProfile().setAvatarVersion(4);
        _user.setId(1234);
        _user.setUserProfile(null);
        expect(_userDao.findUserFromId(1234)).andReturn(_user);
        replayMocks(_userDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertNotNull(mAndV);
        assertEquals("redirect:" + CommunityUtil.getAvatarURLPrefix() + "stock/default-48.jpg",
                     mAndV.getViewName());
    }

}
