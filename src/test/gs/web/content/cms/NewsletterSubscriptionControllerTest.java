package gs.web.content.cms;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.BaseControllerTestCase;
import gs.web.community.registration.EmailVerificationEmail;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

public class NewsletterSubscriptionControllerTest extends BaseControllerTestCase {
    private NewsletterSubscriptionController _controller;
    private NewsletterSubscriptionCommand _cmd;
    protected MockHttpServletResponse _response;
    private ISubscriptionDao _subscriptionDao;
    private IUserDao _userDao;
    private EmailVerificationEmail _emailVerificationEmail;
    private ExactTargetAPI _exactTargetAPI;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new NewsletterSubscriptionController();
        _cmd = new NewsletterSubscriptionCommand();
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _emailVerificationEmail = createStrictMock(EmailVerificationEmail.class);
        _controller.setEmailVerificationEmail(_emailVerificationEmail);
        _userDao = createStrictMock(IUserDao.class);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setUserDao(_userDao);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        _controller.setExactTargetAPI(_exactTargetAPI);
    }
    public void replayAllMocks() {
        super.replayMocks(_subscriptionDao, _userDao,_emailVerificationEmail, _exactTargetAPI);
    }

    public void verifyAllMocks() {
        super.verifyMocks(_subscriptionDao, _userDao,_emailVerificationEmail, _exactTargetAPI);
    }

    public void testOnSubmitNullEmail() throws Exception {
        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitNullEmailHomePage() throws Exception {
        _cmd.setNlSignUpFromHomePage(true);
        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitNewUser() throws Exception {
        _cmd.setEmail("someemail@someemail");
        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class));
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        Map<String,String> otherParams = new HashMap<String,String>();
        otherParams.put("esw","w");
        _emailVerificationEmail.sendVerificationEmail(eq(getRequest()), isA(User.class), eq("http://www.greatschools.org/?showSubscriptionThankYouHover=true"), eq(otherParams));
        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();

    }

    public void testOnSubmitNewUserHomePage() throws Exception {
        _cmd.setEmail("someemail@someemail");
        _cmd.setNlSignUpFromHomePage(true);
        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class));
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        _exactTargetAPI.sendTriggeredEmail(eq(NewsletterSubscriptionController.EXACT_TARGET_HOME_PAGE_PITCH_KEY), isA(User.class));
        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
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
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitUserAlreadySubscribedHomePage() throws Exception {
        _cmd.setEmail("someemail@someemail");
        _cmd.setNlSignUpFromHomePage(true);

        User user = new User();
        user.setEmail("someemail@someemail");

        Set<Subscription> userSubs = new HashSet<Subscription>();
        Subscription sub = new Subscription();
        sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub.setUser(user);
        userSubs.add(sub);

        user.setSubscriptions(userSubs);

        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(user);

        _exactTargetAPI.sendTriggeredEmail(eq(NewsletterSubscriptionController.EXACT_TARGET_HOME_PAGE_PITCH_KEY), isA(User.class));

        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitUserNotSubscribedToParentAdvisorAndSponsor() throws Exception {
        _cmd.setEmail("someemail@someemail");

        User user = new User();
        user.setEmail("someemail@someemail");

        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(user);

        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));

        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();
    }

    public void testOnSubmitUserNotSubscribedToParentAdvisorAndSponsorHomePage() throws Exception {
        _cmd.setEmail("someemail@someemail");
        _cmd.setNlSignUpFromHomePage(true);

        User user = new User();
        user.setEmail("someemail@someemail");

        expect(_userDao.findUserFromEmailIfExists(_cmd.getEmail())).andReturn(user);

        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));

        _exactTargetAPI.sendTriggeredEmail(eq(NewsletterSubscriptionController.EXACT_TARGET_HOME_PAGE_PITCH_KEY), isA(User.class));

        replayAllMocks();
        _controller.onSubmit(getRequest(), _response, _cmd, null);
        verifyAllMocks();
    }

}