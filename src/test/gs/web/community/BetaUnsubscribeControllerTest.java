package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.community.*;
import gs.data.state.State;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;
import java.util.HashSet;

public class BetaUnsubscribeControllerTest extends BaseControllerTestCase {

    private BetaUnsubscribeController _controller;
    private IUserDao _mockUserDao;
    private ISubscriptionDao _mockSubscriptionDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new BetaUnsubscribeController();
        _controller.setSuccessView("successView");
        _mockUserDao = createMock(IUserDao.class);
        _mockSubscriptionDao = createMock(ISubscriptionDao.class);
        _controller.setUserDao(_mockUserDao);
        _controller.setSubscriptionDao(_mockSubscriptionDao);
    }

    public void testUnsubscribeRequest_Unknown_User() throws Exception {
        String email = "tester@greatschools.net";
        BetaSignupCommand command = new BetaSignupCommand();
        command.setEmail(email);
        expect(_mockUserDao.findUserFromEmailIfExists(email)).andReturn(null);
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        ModelAndView mAndV = _controller.onSubmit(command);
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        assertEquals("Expected success view", _controller.getSuccessView(), mAndV.getViewName());
    }

    public void testUnsubscribeRequest_Known_User() throws Exception {
        String email = "tester@greatschools.net";
        User user = new User();
        user.setEmail(email);
        Set<Subscription> subs = new HashSet<Subscription>();
        Subscription sub = new Subscription(user, SubscriptionProduct.BETA_GROUP, State.CA);
        sub.setId(1234);
        subs.add(sub);
        user.setSubscriptions(subs);
        BetaSignupCommand command = new BetaSignupCommand();
        command.setEmail(email);
        expect(_mockUserDao.findUserFromEmailIfExists(email)).andReturn(user);
        _mockSubscriptionDao.removeSubscription(1234);
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        ModelAndView mAndV = _controller.onSubmit(command);
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        assertEquals("Expected success view", _controller.getSuccessView(), mAndV.getViewName());
    }

    public void testUnsubscribeRequest_Known_User_No_Sub() throws Exception {
        String email = "tester@greatschools.net";
        User user = new User();
        user.setEmail(email);
        BetaSignupCommand command = new BetaSignupCommand();
        command.setEmail(email);
        expect(_mockUserDao.findUserFromEmailIfExists(email)).andReturn(user);
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        ModelAndView mAndV = _controller.onSubmit(command);
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        assertEquals("Expected success view", _controller.getSuccessView(), mAndV.getViewName());
    }
}
