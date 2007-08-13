package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.ReadWriteController;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class CommunitySubscriptionControllerTest extends BaseControllerTestCase {
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private CommunitySubscriptionController _controller;
    private String _viewName = "/some/path";
    private String _email = "test@test.com";

    public void testControllerIsReadWrite() {
        assertTrue("Controller must be read/write to save data", _controller instanceof ReadWriteController);
    }

    public void testShouldSaveSubscriptionForExistingUser() throws Exception {
        _request.setParameter(CommunitySubscriptionController.EMAIL_PARAM, _email);
        _sessionContext.setState(State.FL);

        User user = new User();
        user.setEmail(_email);

        expectShouldFindUserFromEmail(user);
        expectShouldSaveSubscription(user, State.FL);

        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertNull("No model should be returned from controller", modelAndView);
        assertEquals("Expect success", "success", _response.getContentAsString());

        verify(_userDao);
        verify(_subscriptionDao);
    }

    public void testShouldSaveSubscriptionForNewUser() throws Exception {
        _request.setParameter(CommunitySubscriptionController.EMAIL_PARAM, _email);
        _sessionContext.setState(State.AZ);

        User user = new User();
        user.setEmail(_email);

        expectShouldSaveUserIfNotFound(user);
        expectShouldSaveSubscription(user, State.AZ);

        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertNull("No model should be returned from controller", modelAndView);
        assertEquals("Expect success", "success", _response.getContentAsString());

        verify(_userDao);
        verify(_subscriptionDao);
    }

    public void testShouldAddErrorsToModel() throws Exception {
        _request.setParameter(CommunitySubscriptionController.EMAIL_PARAM, _email);
        _sessionContext.setState(State.CO);

        User user = new User();
        user.setEmail(_email);

        expectShouldFindUserFromEmail(user);
        expectShouldThrowExceptionOnSaveSubscription(user, State.CO);

        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertNull("No model should be returned from controller", modelAndView);
        assertEquals("Expect failure", "failure", _response.getContentAsString());
        assertEquals("Expected 200 success code, so default error page doesn't display", HttpServletResponse.SC_OK, _response.getStatus());

        verify(_userDao);
        verify(_subscriptionDao);
    }

    private void expectShouldThrowExceptionOnSaveSubscription(User user, State state) {
        Subscription subscription = new Subscription(user, SubscriptionProduct.COMMUNITY, state);
        _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription[]{subscription}));
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(_subscriptionDao);
    }

    private void expectShouldSaveUserIfNotFound(User user) {
        expect(_userDao.findUserFromEmailIfExists(_email)).andReturn(null).once();
        _userDao.saveUser(user);
        expectLastCall();
        replay(_userDao);
    }

    private void expectShouldSaveSubscription(User user, State state) {
        Subscription subscription = new Subscription(user, SubscriptionProduct.COMMUNITY, state);
        _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription[]{subscription}));
        expectLastCall().once();
        replay(_subscriptionDao);
    }

    private void expectShouldFindUserFromEmail(User user) {
        expect(_userDao.findUserFromEmailIfExists(_email)).
                andReturn(user).
                once();
        replay(_userDao);
    }

    protected void setUp() throws Exception {
        super.setUp();
        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);
        _controller = new CommunitySubscriptionController();
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setViewName(_viewName);
        _request.setMethod("GET");
    }
}
