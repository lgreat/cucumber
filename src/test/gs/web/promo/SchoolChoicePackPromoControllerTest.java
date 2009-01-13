package gs.web.promo;

import gs.web.BaseControllerTestCase;
import gs.data.community.*;
import gs.data.state.State;
import static org.easymock.EasyMock.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by chriskimm@greatschools.net
 */
public class SchoolChoicePackPromoControllerTest extends BaseControllerTestCase {

    private SchoolChoicePackPromoController _controller;
    private IUserDao _mockUserDao;
    private ISubscriptionDao _mockSubscriptionDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolChoicePackPromoController)getApplicationContext().getBean(SchoolChoicePackPromoController.BEAN_ID);
        _mockUserDao = createMock(IUserDao.class);
        _controller.setUserDao(_mockUserDao);
        _mockSubscriptionDao = createMock(ISubscriptionDao.class);
        _controller.setSubscriptionDao(_mockSubscriptionDao);
    }

    public void testHandleRequestInternal() throws Exception {
        String email = "chriskimm@greatschools.net";
        getRequest().setParameter(SchoolChoicePackPromoController.EMAIL_PARAM, email);
        getRequest().setParameter(SchoolChoicePackPromoController.LEVELS_PARAM, "p,m");
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
        
        _mockSubscriptionDao.addNewsletterSubscriptions(u, subs);

        replay(_mockSubscriptionDao);

        _controller.handleRequestInternal(getRequest(), getResponse());

        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
    }

    public void testExactTargetTrigger() throws Exception {
        User u = new User();
        u.setEmail("chriskimm@greatschools.net");
        _controller.triggerPromoPackEmail(u, new String[] {"preschool", "elementary"});
    }
}
