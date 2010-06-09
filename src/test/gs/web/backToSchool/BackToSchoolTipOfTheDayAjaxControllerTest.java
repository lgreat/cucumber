package gs.web.backToSchool;

import gs.data.community.*;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseControllerTestCase;
import gs.web.BaseTestCase;
import gs.web.util.context.SessionContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;

public class BackToSchoolTipOfTheDayAjaxControllerTest extends BaseControllerTestCase {

    ISubscriptionDao _subscriptionDao;

    BackToSchoolTipOfTheDayAjaxController _controller;

    public void setUp() {
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);

        _controller = new BackToSchoolTipOfTheDayAjaxController();

        _controller.setSubscriptionDao(_subscriptionDao);

    }

    public void testAddSubscriptionToUser() {

        User user = new User();
        user.setEmail("test@greatschools.org");
        user.setId(99999);

        _subscriptionDao.saveSubscription(isA(Subscription.class));

        replay(_subscriptionDao);

        assertTrue(_controller.addSubscriptionToUser("btstip_e", user));

        verify(_subscriptionDao);

    }



}
