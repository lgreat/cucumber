package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

public class CommunitySubscriptionControllerTest extends BaseControllerTestCase {
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private CommunitySubscriptionController _controller;
    private String _viewName = "/some/path";
    private String _email = "test@test.com";

    public void testShouldSaveSubscriptionForExistingUser() throws Exception {
        _request.setParameter(CommunitySubscriptionController.EMAIL_PARAM, _email);
        _sessionContext.setState(State.FL);

        User user = new User();
        user.setEmail(_email);

        expectShouldFindUserFromEmail(user);
        expectShouldSaveSubscription(user, State.FL);

        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view", _viewName, modelAndView.getViewName());
    }

    public void testShouldSaveSubscriptionForNewUser() throws Exception {
        _request.setParameter(CommunitySubscriptionController.EMAIL_PARAM, _email);
        _sessionContext.setState(State.AZ);

        User user = new User();
        user.setEmail(_email);

        expectShouldSaveUserIfNotFound(user);
        expectShouldSaveSubscription(user, State.AZ);

        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view", _viewName, modelAndView.getViewName());
    }

    public void testShouldAddErrorsToModel() throws Exception {
        _request.setParameter(CommunitySubscriptionController.EMAIL_PARAM, _email);
        _sessionContext.setState(State.CO);

        User user = new User();
        user.setEmail(_email);

        expectShouldFindUserFromEmail(user);
        expectShouldThrowExceptionOnSaveSubscription(user, State.CO);

        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view", _viewName, modelAndView.getViewName());
        assertNotNull("Expected error in model", modelAndView.getModel().get(CommunitySubscriptionController.ERROR));
    }

    private void expectShouldThrowExceptionOnSaveSubscription(User user, State state) {
        _subscriptionDao.saveSubscription(new Subscription(user, SubscriptionProduct.COMMUNITY, state));
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
        _subscriptionDao.saveSubscription(subscription);
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


    protected void tearDown() throws Exception {
        super.tearDown();
        verify(_userDao);
        verify(_subscriptionDao);
    }
}
