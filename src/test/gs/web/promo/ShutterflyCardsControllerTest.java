package gs.web.promo;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.promo.IPromoDao;
import gs.data.community.promo.Promo;
import gs.data.community.promo.PromoCode;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ShutterflyCardsControllerTest extends BaseControllerTestCase {
    private ShutterflyCardsController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (ShutterflyCardsController) getApplicationContext().getBean(ShutterflyCardsController.BEAN_ID);
        MockJavaMailSender _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.net");
        _controller.setMailSender(_sender);

    }

    public void testUserMoreRecentJoinDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.set(2006, Calendar.NOVEMBER, 9);
        Date joinDate = calendar.getTime();

        testPromoEligibilityByJoinDate(joinDate,
                ShutterflyCardsController.INELIGIBLE_ALT,
                ShutterflyCardsController.INELIGIBLE_SRC,
                ShutterflyCardsController.INELIGIBLE_PAGE_NAME);
    }

    public void testUserSameDayJoinDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.set(2006, Calendar.NOVEMBER, 8);
        Date joinDate = calendar.getTime();

        testPromoEligibilityByJoinDate(joinDate,
                ShutterflyCardsController.INELIGIBLE_ALT,
                ShutterflyCardsController.INELIGIBLE_SRC,
                ShutterflyCardsController.INELIGIBLE_PAGE_NAME);
    }

    public void testUserPriorToCutOffDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.set(2006, Calendar.NOVEMBER, 7);
        Date joinDate = calendar.getTime();

        testPromoEligibilityByJoinDate(joinDate,
                ShutterflyCardsController.INELIGIBLE_ALT,
                ShutterflyCardsController.INELIGIBLE_SRC,
                ShutterflyCardsController.INELIGIBLE_PAGE_NAME);
    }

    //user has never received a promo before
    private void testPromoEligibilityByJoinDate(Date joinDate,
                                                String expectedImageAlt,
                                                String expectedImageSrc,
                                                String expectedPageName) throws Exception {
        MockHttpServletRequest request = getRequest();
        request.addParameter(ShutterflyCardsController.PARAM_EMAIL, "user@greatschools.net");

        User user = new User();
        user.setEmail("user@greatschools.net");
        user.setTimeAdded(joinDate);

        PromoCode promoCode = new PromoCode("ABCDEFGHIJKLMN", new Promo());

        _controller.setUserDao(getMockedUserDao(user));
        _controller.setPromoDao(getMockedPromoDao(null, promoCode));

        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());

        assertEquals("/promo/shutterfly/cards", mv.getViewName());
        assertEquals(expectedImageAlt, (String) mv.getModel().get(ShutterflyCardsController.MODEL_IMAGE_ALT));
        assertEquals(expectedImageSrc, (String) mv.getModel().get(ShutterflyCardsController.MODEL_IMAGE_SRC));
        assertEquals(expectedPageName, (String) mv.getModel().get(ShutterflyCardsController.MODEL_PAGE_NAME));
    }


    public void testUserWithRedeemedPromoCode() throws Exception {
        MockHttpServletRequest request = getRequest();
        request.addParameter(ShutterflyCardsController.PARAM_EMAIL, "user@greatschools.net");

        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.set(2006, Calendar.NOVEMBER, 7);
        Date joinDate = calendar.getTime();

        User user = new User();
        user.setEmail("user@greatschools.net");
        user.setTimeAdded(joinDate);

        PromoCode promoCode = new PromoCode("code", new Promo());
        promoCode.setMember(user);
        promoCode.setAssigned(new Date());

        _controller.setUserDao(getMockedUserDao(user));
        _controller.setPromoDao(getMockedPromoDao(promoCode, null));

        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());
        assertEquals("/promo/shutterfly/cards", mv.getViewName());

        assertEquals(ShutterflyCardsController.INELIGIBLE_ALT,
                (String) mv.getModel().get(ShutterflyCardsController.MODEL_IMAGE_ALT));
        assertEquals(ShutterflyCardsController.INELIGIBLE_SRC,
                (String) mv.getModel().get(ShutterflyCardsController.MODEL_IMAGE_SRC));

    }


    /**
     * Return a mocked UserDao that returns the given user
     * @return IUserDao
     * @param user a valid user object
     */
    private static IUserDao getMockedUserDao(final User user) {
        MockControl mockUserDaoControl = MockControl.createControl(IUserDao.class);
        IUserDao userDao = (IUserDao) mockUserDaoControl.getMock();
        userDao.findUserFromEmail(user.getEmail());
        mockUserDaoControl.setReturnValue(user);
        mockUserDaoControl.replay();

        return userDao;
    }

    /**
     * @param foundPromoCode    PromoCode that is returned by IPromoDao#findPromoCode
     * @param assignedPromoCode PromoCode that is returned by IPromoDao#assignPromoCode
     * @return IPromoDao
     * @see gs.data.community.promo.IPromoDao#findPromoCode(gs.data.community.User,gs.data.community.promo.Promo)
     * @see gs.data.community.promo.IPromoDao#assignPromoCode(gs.data.community.User,gs.data.community.promo.Promo)
     */
    private static IPromoDao getMockedPromoDao(final PromoCode foundPromoCode, final PromoCode assignedPromoCode) {

        MockControl mockPromoDaoControl = MockControl.createControl(IPromoDao.class);
        IPromoDao promoDao = (IPromoDao) mockPromoDaoControl.getMock();
        promoDao.findPromoCode(null, null);
        mockPromoDaoControl.setDefaultReturnValue(foundPromoCode);
        promoDao.assignPromoCode(null, null);
        mockPromoDaoControl.setDefaultReturnValue(assignedPromoCode);
        mockPromoDaoControl.replay();

        return promoDao;
    }


    public void testEligibleUserWithPromo() throws Exception {

    }

    public void testIneligibleUser() throws Exception {

    }
}
