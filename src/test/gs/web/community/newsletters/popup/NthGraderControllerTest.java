/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderControllerTest.java,v 1.2 2006/06/08 01:12:02 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;
import gs.web.util.validator.EmailValidator;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

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
 */
public class NthGraderControllerTest extends BaseControllerTestCase {
    private NthGraderController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
        _subscriptionDao = (ISubscriptionDao) getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _controller = new NthGraderController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
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
        command = new NewsletterCommand();
        assertTrue(hasErrorOnPageLoad(command));

        command.setState(State.CA);
        assertFalse(hasErrorOnPageLoad(command));
    }

    public void testOnSubmitScenarioOne() {
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

        command.setMyk(true);
        command.setMy1(true);
        command.setMy2(true);
        command.setMy3(true);
        command.setMy4(true);
        command.setMy5(true);
        command.setMyMs(true);
        command.setMyHs(true);

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, new BindException(command, null));
        ThreadLocalTransactionManager.commitOrRollback();
        User user = _userDao.getUserFromEmail(email);
        assertNotNull(user);
        assertEquals(email, user.getEmail());

        String memberId = user.getId().toString();
        assertEquals( memberId, getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE).getValue());

        for (Iterator iter = products.iterator(); iter.hasNext(); ) {
            hasStoredSubscription(user, (SubscriptionProduct) iter.next());
        }

        _userDao.removeUser(user.getId());
        ThreadLocalTransactionManager.commitOrRollback();
        assertNull(_userDao.getUserFromEmailIfExists(email));

        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
        assertEquals("CA", mAndV.getModel().get("state").toString());
        assertEquals(email, mAndV.getModel().get("email").toString());
        assertEquals("1", mAndV.getModel().get("schoolId").toString());
    }

    private boolean hasStoredSubscription(final User user, final SubscriptionProduct newsletter) {
        Set subscriptions = user.getSubscriptions();
        assertNotNull(subscriptions);
        boolean hasNewsletter = false;

        for (Iterator iter = subscriptions.iterator(); iter.hasNext(); ) {
            Subscription sub = (Subscription) iter.next();

            if (sub.getProduct() == newsletter) {
                if (sub.getState() == State.CA) {
                    hasNewsletter = true;
                }
            }
        }

        return hasNewsletter;
    }
}
