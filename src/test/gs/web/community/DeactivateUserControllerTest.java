package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Test class for the DeactivateUserController
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DeactivateUserControllerTest extends BaseControllerTestCase {
    private DeactivateUserController _controller;
    private IUserDao _userDao;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new DeactivateUserController();

        _userDao = createMock(IUserDao.class);
        _controller.setUserDao(_userDao);

        _user = new User();
        _user.setId(1);
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setActive(true);
    }

    public void testEmptyRequest() throws Exception {
        replay(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_userDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testNoSecretNumber() throws Exception {
        getRequest().setParameter("id", "1");
        replay(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_userDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testGarbledRequest() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "Anthony");
        replay(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_userDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());        
    }

    public void testNormalRequest() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "1");
        expect(_userDao.findUserFromId(1)).andReturn(_user);
        _userDao.saveUser(_user);
        replay(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_userDao);
        assertEquals("Expect success", "true", getResponse().getContentAsString());
        assertEquals("Expect user to be deactivated", false, _user.getUserProfile().isActive());
    }

    public void testMissingUser() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "2");
        expect(_userDao.findUserFromId(2)).andThrow(new ObjectRetrievalFailureException(User.class, 2));
        replay(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_userDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testNoUserProfile() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "1");
        User user = new User();
        user.setUserProfile(null);
        user.setId(1);
        expect(_userDao.findUserFromId(1)).andReturn(user);
        replay(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verify(_userDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }
}
