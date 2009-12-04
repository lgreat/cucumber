package gs.web.community;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the DeactivateUserController
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DeactivateUserControllerTest extends BaseControllerTestCase {
    private DeactivateUserController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new DeactivateUserController();

        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);

        _user = new User();
        _user.setId(1);
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setActive(true);
    }

    public void testEmptyRequest() throws Exception {
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testNoSecretNumber() throws Exception {
        getRequest().setParameter("id", "1");
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testGarbledRequest() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "Anthony");
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testNormalRequest() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "1");
        expect(_userDao.findUserFromId(1)).andReturn(_user);
        expect(_subscriptionDao.getUserSubscriptions(_user)).andReturn(new ArrayList<Subscription>());
        _userDao.saveUser(_user);
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect success", "true", getResponse().getContentAsString());
        assertEquals("Expect user to be deactivated", false, _user.getUserProfile().isActive());
    }

    public void testMissingUser() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "2");
        expect(_userDao.findUserFromId(2)).andThrow(new ObjectRetrievalFailureException(User.class, 2));
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }

    public void testNoUserProfile() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "1");
        User user = new User();
        user.setUserProfile(null);
        user.setId(1);
        expect(_userDao.findUserFromId(1)).andReturn(user);
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect failure", "false", getResponse().getContentAsString());
    }
    
    public void testDeleteSubscriptions() throws Exception {
        getRequest().setParameter("secret", DeactivateUserController.SECRET_NUMBER);
        getRequest().setParameter("id", "1");
        expect(_userDao.findUserFromId(1)).andReturn(_user);
        List<Subscription> subs = new ArrayList<Subscription>();
        Subscription sub1 = new Subscription(_user, SubscriptionProduct.PARENT_ADVISOR, State.CA);
        sub1.setId(1);
        subs.add(sub1);
        Subscription sub2 = new Subscription(_user, SubscriptionProduct.SCHOOL_CHOOSER_PACK_ELEMENTARY, State.CA);
        sub2.setId(2);
        subs.add(sub2);
        expect(_subscriptionDao.getUserSubscriptions(_user)).andReturn(subs);
        _userDao.saveUser(_user);
        _subscriptionDao.removeSubscription(1);
        _subscriptionDao.removeSubscription(2);
        replayMocks(_userDao, _subscriptionDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao, _subscriptionDao);
        assertEquals("Expect success", "true", getResponse().getContentAsString());
        assertEquals("Expect user to be deactivated", false, _user.getUserProfile().isActive());

    }
}
