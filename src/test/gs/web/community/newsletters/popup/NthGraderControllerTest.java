/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderControllerTest.java,v 1.12 2006/09/08 19:13:02 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.EmailValidator;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;
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

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
        ISubscriptionDao subscriptionDao = (ISubscriptionDao) getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _controller = new NthGraderController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(subscriptionDao);
        _controller.setCommandClass(NewsletterCommand.class);
    }

    //Scenario 1:  As part 2 in a multiple page process where email, schoolId, and state is supplied
    protected void setUpScenarioOne() {
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setFormView("/community/newsletters/popup/mss/page2");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page3.page");
        _controller.setValidators(new Validator[]{
                new SchoolIdValidator(),
                new EmailValidator(),
                new NewsletterCheckBoxValidator(),});

        List onLoadValidators = new ArrayList();
        onLoadValidators.add(new EmailValidator());
        onLoadValidators.add(new StateValidator());
        onLoadValidators.add(new SchoolIdValidator());
        _controller.setOnLoadValidators(onLoadValidators);

    }

    //Scenario 2:  As a regular page where only the state is supplied and the user types in his email
    //address
    protected void setUpScenarioTwo() {
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setFormView("/community/newsletters/popup/newsletters");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page3.page");

        _controller.setValidators(new Validator[]{
                new EmailValidator(),
                new NewsletterCheckBoxValidator(),});

        List onLoadValidators = new ArrayList();
        onLoadValidators.add(new StateValidator());
        _controller.setOnLoadValidators(onLoadValidators);
    }


    public void testSetNthGraderSelection() {
        setUpScenarioOne();
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        getRequest().setParameter(NthGraderController.PARAM_AUTOCHECK, "myhs");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertTrue(command.isMyHs());
    }

    private boolean hasErrorOnPageLoad(final NewsletterCommand command) {
        BindException errors = new BindException(command, "");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        return errors.hasErrors();
    }

    public void testInputOnPageLoad() {
        NewsletterCommand command = new NewsletterCommand();
        setUpScenarioOne();
        assertTrue(hasErrorOnPageLoad(command));

        command.setState(State.CA);
        assertTrue(hasErrorOnPageLoad(command));

        command.setEmail("dlee@greatschools.net");
        assertTrue(hasErrorOnPageLoad(command));

        command.setSchoolId(1);
        assertFalse(hasErrorOnPageLoad(command));


        setUpScenarioTwo();
        //don't set a state
        command = new NewsletterCommand();
        assertFalse(hasErrorOnPageLoad(command));
        assertEquals(State.CA, command.getState());
        assertEquals("", command.getEmail());


        //set a state
        command.setState(State.WY);
        assertFalse(hasErrorOnPageLoad(command));
        assertEquals(State.WY, command.getState());
    }

    public void testEmailFromSessionContext() {
        NewsletterCommand command = new NewsletterCommand();

        final String email = "dlee@greatschools.net";
        User user = new User();
        user.setEmail(email);
        getSessionContext().setUser(user);
        getSessionContext().setEmail(email);
        setUpScenarioTwo();

        BindException errors = new BindException(command, "");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals(email, command.getEmail());
        getSessionContext().setUser(null);
        command.setEmail(null);

        setUpScenarioTwo();
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals("", command.getEmail());
    }

    /** @noinspection OverlyLongMethod*/
    public void testOnSubmitScenario() {
        //scenario1 includes scenario2
        setUpScenarioOne();
        NewsletterCommand command = new NewsletterCommand();
        final String email = "wbeck+1234@greatschoo.net";

        command.setEmail(email);
        command.setState(State.CA);
        command.setSchoolId(1);

        Set products = new HashSet();
        products.add(SubscriptionProduct.MY_KINDERGARTNER);
        products.add(SubscriptionProduct.MY_FIRST_GRADER);
        products.add(SubscriptionProduct.MY_SECOND_GRADER);
        products.add(SubscriptionProduct.MY_THIRD_GRADER);
        products.add(SubscriptionProduct.MY_FOURTH_GRADER);
        products.add(SubscriptionProduct.MY_FIFTH_GRADER);
        products.add(SubscriptionProduct.MY_MS);
        products.add(SubscriptionProduct.MY_HS);
        products.add(SubscriptionProduct.PARENT_ADVISOR);

        command.setMyk(true);
        command.setMy1(true);
        command.setMy2(true);
        command.setMy3(true);
        command.setMy4(true);
        command.setMy5(true);
        command.setMyMs(true);
        command.setMyHs(true);
        command.setGn(true);

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, new BindException(command, null));
        ThreadLocalTransactionManager.commitOrRollback();
        User user = _userDao.findUserFromEmail(email);
        assertNotNull(user);
        assertEquals(email, user.getEmail());

        String memberId = user.getId().toString();

        Cookie cookie = getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertNotNull(cookie);
        assertEquals(CookieGenerator.DEFAULT_COOKIE_MAX_AGE, cookie.getMaxAge());
        assertEquals(CookieGenerator.DEFAULT_COOKIE_PATH, cookie.getPath());
        assertEquals(cookie.getValue(), memberId);

        for (Iterator iter = products.iterator(); iter.hasNext(); ) {
            hasStoredSubscription(user, (SubscriptionProduct) iter.next());
        }

        _userDao.removeUser(user.getId());
        ThreadLocalTransactionManager.commitOrRollback();
        assertNull(_userDao.findUserFromEmailIfExists(email));

        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
        assertEquals("CA", mAndV.getModel().get("state").toString());
        assertEquals(email, mAndV.getModel().get("email").toString());
        assertEquals("1", mAndV.getModel().get("schoolId").toString());
        assertNull(mAndV.getModel().get(SubscriptionSummaryController.PARAM_SHOW_FIND_SCHOOL_LINK));
    }

    private boolean hasStoredSubscription(final User user, final SubscriptionProduct newsletter) {
        Set subscriptions = user.getSubscriptions();
        assertNotNull(subscriptions);
        boolean hasNewsletter = false;

        for (Iterator iter = subscriptions.iterator(); iter.hasNext(); ) {
            Subscription sub = (Subscription) iter.next();

            if (sub.getProduct().equals(newsletter)) {
                if (State.CA.equals(sub.getState())) {
                    hasNewsletter = true;
                }
            }
        }

        return hasNewsletter;
    }
}
