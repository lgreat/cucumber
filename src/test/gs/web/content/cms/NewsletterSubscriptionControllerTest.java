package gs.web.content.cms;

import gs.data.community.*;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.classextension.EasyMock.*;

public class NewsletterSubscriptionControllerTest extends BaseControllerTestCase {
    private NewsletterSubscriptionController _controller;
    private NewsletterSubscriptionCommand _cmd;
    protected GsMockHttpServletRequest _request;
    protected MockHttpServletResponse _response;
    private ISubscriptionDao _subscriptionDao;
    private IUserDao _userDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new NewsletterSubscriptionController();
        _cmd = new NewsletterSubscriptionCommand();
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setUserDao(_userDao);
    }
    public void replayAllMocks() {
        super.replayMocks(_subscriptionDao, _userDao);
    }

    public void verifyAllMocks() {
        super.verifyMocks(_subscriptionDao, _userDao);
    }

    public void testOnSubmitNullEmail() throws Exception {
        replayAllMocks();
        _controller.onSubmit(_request, _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitNoUser() throws Exception {
        _cmd.setEmail("someemail@someemail");
        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(null);
        replayAllMocks();
        _controller.onSubmit(_request, _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitUserAlreadySubscribed() throws Exception {
        _cmd.setEmail("someemail@someemail");

        User user = new User();
        user.setEmail("someemail@someemail");

        Set<Subscription> userSubs = new HashSet<Subscription>();
        Subscription sub = new Subscription();
        sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub.setUser(user);
        userSubs.add(sub);

        user.setSubscriptions(userSubs);

        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(user);
        replayAllMocks();
        _controller.onSubmit(_request, _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitUserNotSubscribedToParentAdvisorAndSponsor() throws Exception {
        _cmd.setEmail("someemail@someemail");

        User user = new User();
        user.setEmail("someemail@someemail");

        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(user);

        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));

        replayAllMocks();
        _controller.onSubmit(_request, _response, _cmd, null);
        verifyAllMocks();
    }

}