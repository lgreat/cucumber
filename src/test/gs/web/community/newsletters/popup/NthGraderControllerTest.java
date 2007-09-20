/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderControllerTest.java,v 1.15 2007/09/20 23:51:51 aroy Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import java.util.*;

/**
 * Test Nthgrader controller
 *
 * The test has multiple setup methods that are used to set up the controller under the various
 * scenarios where it may be used.
 *
 * Scenario 1:  As part 2 in a multiple page process where email, schoolId, and state is supplied
 *
 * Scenario 2:  As a regular page where only the state is supplied and the user types in his email
 * address
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 * @noinspection ProhibitedExceptionDeclared,HardcodedFileSeparator,FeatureEnvy
 */
public class NthGraderControllerTest extends BaseControllerTestCase {
    private NthGraderController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private Validator _validator;
    private BindException _errors;
    private NewsletterCommand _command;

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _validator = createMock(Validator.class);
        _controller = new NthGraderController();
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setCommandClass(NewsletterCommand.class);

        _command = new NewsletterCommand();
        _errors = new BindException(_command, "");
    }

    //Scenario 1:  As part 2 in a multiple page process where email, schoolId, and state is supplied
    protected void setUpScenarioOne() {
        _controller.setFormView("/community/newsletters/popup/mss/page2");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page3.page");
    }

    //Scenario 2:  As a regular page where only the state is supplied and the user types in his email
    //address
    protected void setUpScenarioTwo() {
        _controller.setFormView("/community/newsletters/popup/newsletters");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page3.page");
    }


    public void testSetNthGraderSelection() {
        setUpScenarioOne();
        getRequest().setParameter(NthGraderController.PARAM_AUTOCHECK, "myhs");
        List<Validator> onLoadValidators = new ArrayList<Validator>();
        onLoadValidators.add(_validator);
        _controller.setOnLoadValidators(onLoadValidators);
        expect(_validator.supports(NewsletterCommand.class)).andReturn(true);
        _validator.validate(_command, _errors);
        replay(_userDao);
        replay(_subscriptionDao);
        replay(_schoolDao);
        replay(_validator);
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        verify(_userDao);
        verify(_subscriptionDao);
        verify(_schoolDao);
        verify(_validator);
        assertTrue(_command.isMyHs());
    }

    public void testEmailFromSessionContext() {

        final String email = "dlee@greatschools.net";
        User user = new User();
        user.setEmail(email);
        getSessionContext().setUser(user);
        getSessionContext().setEmail(email);
        setUpScenarioTwo();

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        assertEquals(email, _command.getEmail());
        getSessionContext().setUser(null);
        _command.setEmail(null);

        setUpScenarioTwo();
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        assertEquals("", _command.getEmail());
    }

    public void testOnSubmitScenario() {
        setUpScenarioOne();
        final String email = "aroy@greatschools.net";

        _command.setEmail(email);
        _command.setState(State.CA);
        _command.setSchoolId(1);

        Set<SubscriptionProduct> products = new HashSet<SubscriptionProduct>();
        products.add(SubscriptionProduct.MY_KINDERGARTNER);
        products.add(SubscriptionProduct.MY_FIRST_GRADER);
        products.add(SubscriptionProduct.MY_SECOND_GRADER);
        products.add(SubscriptionProduct.MY_THIRD_GRADER);
        products.add(SubscriptionProduct.MY_FOURTH_GRADER);
        products.add(SubscriptionProduct.MY_FIFTH_GRADER);
        products.add(SubscriptionProduct.MY_MS);
        products.add(SubscriptionProduct.MY_HS);
        products.add(SubscriptionProduct.PARENT_ADVISOR);
        products.add(SubscriptionProduct.SPONSOR_OPT_IN);

        _command.setMyk(true);
        _command.setMy1(true);
        _command.setMy2(true);
        _command.setMy3(true);
        _command.setMy4(true);
        _command.setMy5(true);
        _command.setMyMs(true);
        _command.setMyHs(true);
        _command.setGn(true);
        _command.setSponsor(true);

        User user = new User();
        user.setEmail(email);
        user.setId(1);
        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);

        _subscriptionDao.addNewsletterSubscriptions(eq(user), containsSubProducts(products));

        replay(_userDao);
        replay(_subscriptionDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_subscriptionDao);

        Cookie cookie = getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertNotNull(cookie);
        assertEquals(CookieGenerator.DEFAULT_COOKIE_MAX_AGE, cookie.getMaxAge());
        assertEquals(CookieGenerator.DEFAULT_COOKIE_PATH, cookie.getPath());
        assertEquals(cookie.getValue(), "1");

        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
        assertEquals("CA", mAndV.getModel().get("state").toString());
        assertEquals(email, mAndV.getModel().get("email").toString());
        assertEquals("1", mAndV.getModel().get("schoolId").toString());
        assertNull(mAndV.getModel().get(SubscriptionSummaryController.PARAM_SHOW_FIND_SCHOOL_LINK));
        assertNull(mAndV.getModel().get("newuser"));
    }

    private static List<Subscription> containsSubProducts(Set<SubscriptionProduct> products) {
        reportMatcher(new SubMatcher(products));
        return null;
    }

    private static class SubMatcher implements IArgumentMatcher {
        private Set<SubscriptionProduct> _products;

        public SubMatcher(Set<SubscriptionProduct> products) {
            _products = products;
        }

        public boolean matches(Object object) {
            if (!(object instanceof List)) {
                return false;
            }
            List<Subscription> subs = (List<Subscription>) object;
            for (SubscriptionProduct product: _products) {
                boolean found = false;
                for (Subscription sub: subs) {
                    if (product.equals(sub.getProduct())) {
                        found = true;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("containsSubProducts(").append(_products).append(")");
        }
    }
}
