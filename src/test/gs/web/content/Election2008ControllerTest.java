package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.state.State;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class Election2008ControllerTest extends BaseControllerTestCase {
    private Election2008Controller _controller;
    private Election2008Command _command;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new Election2008Controller();
        _command = new Election2008Command();
        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
    }

    public void testBasics() {
        assertSame(_controller.getUserDao(), _userDao);
        assertSame(_controller.getSubscriptionDao(), _subscriptionDao);
    }

    /**
     * Test that the onSubmit method inserts the command into the model
     */
    public void testOnSubmitNoParentAdvisor() {
        _command.setEmail("aroy@greatschools.org");

        replay(_userDao);
        replay(_subscriptionDao);

        BindException errors = new BindException(_command, "");
        ModelAndView mandv = _controller.onSubmit(getRequest(), getResponse(), _command, errors);

        assertNotNull(mandv.getModel().get("edin08Cmd"));
        Election2008EmailCommand com2 = (Election2008EmailCommand) mandv.getModel().get("edin08Cmd");
        assertEquals(_command.getEmail(), com2.getUserEmail());
        assertNotNull(com2.getAlert());

        verify(_userDao);
        verify(_subscriptionDao);
    }

    public void testOnSubmitWithParentAdvisor() {
        getRequest().setParameter("parentAdvisor", "checked");

        _command.setEmail("aroy@greatschools.org");

        User user = new User();
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(user);
        List<Subscription> subs = new ArrayList<Subscription>();
        State state = SessionContextUtil.getSessionContext(getRequest()).getStateOrDefault();
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub.setState(state);
        subs.add(sub);
        _subscriptionDao.addNewsletterSubscriptions(user, subs);

        replay(_userDao);
        replay(_subscriptionDao);

        BindException errors = new BindException(_command, "");
        ModelAndView mandv = _controller.onSubmit(getRequest(), getResponse(), _command, errors);

        assertNotNull(mandv.getModel().get("edin08Cmd"));
        Election2008EmailCommand com2 = (Election2008EmailCommand) mandv.getModel().get("edin08Cmd");
        assertEquals(_command.getEmail(), com2.getUserEmail());
        assertNotNull(com2.getAlert());

        verify(_userDao);
        verify(_subscriptionDao);
    }

    /*
    // commented out due to broken integration test from failing login
    public void testSyncInfoWithConstantContactEuccess() {
        _command.setEmail("aroy@greatschools.org");
        _command.setZip("92130");

        assertTrue("Expect successful sync", _controller.syncInfoWithConstantContact(_command));
    }
    */

    public void testSyncInfoWithConstantContactFailure() {
        _command.setEmail("");
        _command.setZip("92130");

        assertFalse("Expect failure to sync with empty email address",
                _controller.syncInfoWithConstantContact(_command));
    }

    public void testGetRandomStat() {
        String stat = _controller.getRandomStat(Election2008Controller.stats);
        assertTrue("Expect random stat to be drawn from array",
                ArrayUtils.contains(Election2008Controller.stats.toArray(), stat));        
    }

    public void testReferenceData() {
        Map map = _controller.referenceData(getRequest());

        assertNotNull("Expect presence of random stat in reference data", map.get("startlingStat"));
        assertTrue("Expect random stat to be drawn from array",
                ArrayUtils.contains(Election2008Controller.stats.toArray(), map.get("startlingStat")));
    }

    public void testSubscribeUserToParentAdvisorExistingUser() {
        String email = "test@greatschools.org";
        _command.setEmail(email);

        User user = new User();
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(user);

        testSubscribeUserToParentAdvisorHelper(user);
    }

    public void testSubscribeUserToParentAdvisorNewUser() {
        String email = "test@greatschools.org";
        _command.setEmail(email);

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(null);
        User user = new User();
        user.setHow("edin08");
        user.setEmail(_command.getEmail());
        _userDao.saveUser(user);

        testSubscribeUserToParentAdvisorHelper(user);
    }

    public void testCreateSubscriptionList() {
        User user = new User();

        List<Subscription> subs = new ArrayList<Subscription>();
        State state = SessionContextUtil.getSessionContext(getRequest()).getStateOrDefault();
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        subscription.setState(state);
        subs.add(subscription);

        assertEquals(subs, _controller.createSubscriptionList(user, getRequest()));
    }

    private void testSubscribeUserToParentAdvisorHelper(User user) {
        List<Subscription> subs = new ArrayList<Subscription>();
        State state = SessionContextUtil.getSessionContext(getRequest()).getStateOrDefault();
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub.setState(state);
        subs.add(sub);
        _subscriptionDao.addNewsletterSubscriptions(user, subs);

        replay(_userDao);
        replay(_subscriptionDao);

        _controller.subscribeUserToParentAdvisor(getRequest(), getResponse(), _command);

        verify(_userDao);
        verify(_subscriptionDao);
    }
}
