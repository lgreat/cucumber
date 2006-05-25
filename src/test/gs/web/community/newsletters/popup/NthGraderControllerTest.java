/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderControllerTest.java,v 1.1 2006/05/25 21:47:17 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.validator.EmailValidator;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NthGraderControllerTest extends BaseControllerTestCase {
    private NthGraderController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private static final String EMAIL = "someemail@greatschools.net";

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
        _subscriptionDao = (ISubscriptionDao) getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);

        _controller = new NthGraderController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setCommandClass(NewsletterCommand.class);
        _controller.setFormView("/community/newsletters/popup/mss/page2");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page3.page");
        _controller.setValidators(new Validator[]{
                new SchoolIdValidator(),
                new EmailValidator(),
                new NewsletterCheckBoxValidator(),});

        List onLoadValidators = new ArrayList();
        onLoadValidators.add(new StateValidator());
        onLoadValidators.add(new SchoolIdValidator());
        _controller.setOnLoadValidators(onLoadValidators);

    }

    public void testNoInputOnBindOnNewForm() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        _controller.onBindOnNewForm(getRequest(), command, errors);

        //not passing in request parameters..should get errors
        assertTrue(errors.hasErrors());

    }

    public void testGoodInputOnBindOnNewForm() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");

        command.setSchoolId(1);
        command.setState(State.CA);
        command.setEmail(EMAIL);

        _controller.onBindOnNewForm(getRequest(), command, errors);

        assertFalse(errors.hasErrors());
    }


    public void testOnSubmit() {
        NewsletterCommand command = new NewsletterCommand();
        command.setSchoolId(1);
        command.setState(State.CA);
        command.setEmail(EMAIL);

        command.setMyk(true);
        command.setMy1(true);
        command.setMy2(true);
        command.setMy3(true);
        command.setMy4(true);
        command.setMy5(true);
        command.setMyMs(true);
        command.setMyHs(true);

        ModelAndView modelView = _controller.onSubmit(command);
        Map model = modelView.getModel();
        assertEquals(_controller.getSuccessView(), modelView.getViewName());
        assertEquals(command.getState().toString(), model.get("state").toString());
        assertEquals(command.getEmail(), model.get("email").toString());
        assertEquals(String.valueOf(command.getSchoolId()), model.get("schoolId").toString());

        ThreadLocalTransactionManager.commitOrRollback();

        User user = _userDao.getUserFromEmail(EMAIL);
        assertNotNull(user);

        Set subsriptions = user.getSubscriptions();
        assertEquals(8, subsriptions.size());

        Map subMap = new HashMap();
        subMap.put(SubscriptionProduct.MY_KINDERGARTNER.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_FIRST_GRADER.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_SECOND_GRADER.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_THIRD_GRADER.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_FOURTH_GRADER.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_FIFTH_GRADER.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_MS.getName(), Integer.valueOf("0"));
        subMap.put(SubscriptionProduct.MY_HS.getName(), Integer.valueOf("0"));

        for (Iterator iter = subsriptions.iterator(); iter.hasNext();) {
            Subscription subscription = (Subscription) iter.next();
            String productName = subscription.getProduct().getName();
            subMap.put(productName, Integer.valueOf("1"));
        }

        Set keys = subMap.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Integer value = (Integer) subMap.get(key);

            if (value.intValue() != 1) {
                fail(key + " not saved in database");
            }
        }

        _userDao.removeUser(user.getId());
    }

}
