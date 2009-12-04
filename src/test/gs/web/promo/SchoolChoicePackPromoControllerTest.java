package gs.web.promo;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.state.State;
import gs.data.integration.exacttarget.ExactTargetAPI;
import static org.easymock.classextension.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by chriskimm@greatschools.org
 */
public class SchoolChoicePackPromoControllerTest extends BaseControllerTestCase {

    private SchoolChoicePackPromoController _controller;
    private IUserDao _mockUserDao;
    private ISubscriptionDao _mockSubscriptionDao;
    private ExactTargetAPI _exactTargetAPI;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolChoicePackPromoController)getApplicationContext().getBean(SchoolChoicePackPromoController.BEAN_ID);
        _mockUserDao = createMock(IUserDao.class);
        _controller.setUserDao(_mockUserDao);
        _mockSubscriptionDao = createMock(ISubscriptionDao.class);
        _controller.setSubscriptionDao(_mockSubscriptionDao);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        _controller.setExactTargetAPI(_exactTargetAPI);
    }

    public void testHandleRequestInternal() throws Exception {
        String email = "foo@bar.net";
        getRequest().setParameter(SchoolChoicePackPromoController.EMAIL_PARAM, email);
        getRequest().setParameter(SchoolChoicePackPromoController.LEVELS_PARAM, "p,m");
        getRequest().setParameter(SchoolChoicePackPromoController.REDIRECT_FOR_CONFIRM, "/school-choice/?confirm=true");
        User u = new User();
        u.setEmail(email);
        u.setId(12345);
        expect(_mockUserDao.findUserFromEmailIfExists(email)).andReturn(u);
        replay(_mockUserDao);

        List<Subscription> subs = new ArrayList<Subscription>();
        Subscription communityNewsletterSubscription = new Subscription();
        communityNewsletterSubscription.setUser(u);
        communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        communityNewsletterSubscription.setState(State.CA);
        subs.add(communityNewsletterSubscription);

        Subscription p_sub = new Subscription();
        p_sub.setUser(u);
        p_sub.setProduct(SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL);
        expect(_mockSubscriptionDao.isUserSubscribed(u,
                SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL, null)).andReturn(Boolean.FALSE);
        _mockSubscriptionDao.saveSubscription(p_sub);

        Subscription m_sub = new Subscription();
        m_sub.setUser(u);
        m_sub.setProduct(SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE);
        expect(_mockSubscriptionDao.isUserSubscribed(u,
                SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE, null)).andReturn(Boolean.FALSE);
        _mockSubscriptionDao.saveSubscription(m_sub);        

        replay(_mockSubscriptionDao);

        _controller.handleRequestInternal(getRequest(), getResponse());

        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
    }

    // Will need to replace exact target mock with bean
    public void xtestExactTargetTrigger() throws Exception {
        User u = new User();
        u.setEmail("tester@greatschools.org");
        _controller.triggerPromoPackEmail(u, new String[] {"preschool", "elementary"});
    }

    /**
     * Verify that the new user is set to NEVER_SEND as their welcomeMessageStatus
     */
    public void testNewUser() throws Exception {
        String email = "tester@greatschools.org";
        User u = new User();
        u.setEmail(email);
        getRequest().setParameter(SchoolChoicePackPromoController.EMAIL_PARAM, email);
        getRequest().setParameter(SchoolChoicePackPromoController.LEVELS_PARAM, "p,m");
        getRequest().setParameter(SchoolChoicePackPromoController.REDIRECT_FOR_CONFIRM, "/school-choice/?confirm=true");
        expect(_mockUserDao.findUserFromEmailIfExists(email)).andReturn(null);
        _mockUserDao.saveUser(eqUserWithNeverSend());
        replay(_mockUserDao);

        Subscription p_sub = new Subscription();
        p_sub.setUser(u);
        p_sub.setProduct(SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL);
        expect(_mockSubscriptionDao.isUserSubscribed(isA(User.class),
                isA(SubscriptionProduct.class), (Date)isNull())).andReturn(Boolean.FALSE);
        _mockSubscriptionDao.saveSubscription(p_sub);

        Subscription m_sub = new Subscription();
        m_sub.setUser(u);
        m_sub.setProduct(SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE);
        expect(_mockSubscriptionDao.isUserSubscribed(isA(User.class),
                isA(SubscriptionProduct.class), (Date)isNull())).andReturn(Boolean.FALSE);
        _mockSubscriptionDao.saveSubscription(m_sub);

        replay(_mockSubscriptionDao);

        _controller.handleRequestInternal(getRequest(), getResponse());

        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
    }

    /**
     * Regression test for GS-7993.
     * Verify that a user who is signed in to MSL and submits an email address
     * -- an email address that's not already registered --
     * for the chooser tip sheet should be taken to the registration form. 
     */
    public void testMslUserTakenToRegistration() throws Exception {
        getRequest().setParameter(SchoolChoicePackPromoController.LEVELS_PARAM, "e");
        getRequest().setParameter(SchoolChoicePackPromoController.EMAIL_PARAM, "aroy@greatschools.org");
        getRequest().setParameter(SchoolChoicePackPromoController.REDIRECT_FOR_CONFIRM, "redirect");
        SessionContext context = SessionContextUtil.getSessionContext(getRequest());
        User mslUser = new User();
        context.setUser(mslUser);

        expect(_mockUserDao.findUserFromEmailIfExists("aroy@greatschools.org")).andReturn(null);
        _mockUserDao.saveUser(isA(User.class));

        expect(_mockSubscriptionDao.isUserSubscribed(isA(User.class), isA(SubscriptionProduct.class), (Date)isNull())).andReturn(true);

        _exactTargetAPI.sendTriggeredEmail(eq("chooser_pack_trigger"), isA(User.class), isA(Map.class));
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        replay(_exactTargetAPI);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        verify(_exactTargetAPI);

        assertNull(mAndV);
        assertNotNull(getResponse().getContentAsString());
        String json = getResponse().getContentAsString();
        assertTrue("Expect user to be forwarded to registration", StringUtils.contains(json, "\"showRegistration\":\"y\""));
    }

    public User eqUserWithNeverSend() {
        reportMatcher(new UserHasNeverSend());
        return null;
    }

    /**
     * Matches a user with NEVER_SEND as their welcome message status.
     */
    private class UserHasNeverSend implements IArgumentMatcher {
        public boolean matches(Object oActual) {
            if (!(oActual instanceof User)) {
                return false;
            }
            User actual = (User) oActual;
            actual.setId(1); // this mimics the save call in the dao
            return actual.getWelcomeMessageStatus().equals(WelcomeMessageStatus.NEVER_SEND);
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("User with NEVER_SEND as welcomeMessageStatus");
        }
    }
}
