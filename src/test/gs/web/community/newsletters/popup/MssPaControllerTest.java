/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MssPaControllerTest.java,v 1.5 2006/06/12 21:48:17 dlee Exp $
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
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import java.util.*;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class MssPaControllerTest extends BaseControllerTestCase {
    private MssPaController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
        _subscriptionDao = (ISubscriptionDao) getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);

        _controller = new MssPaController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setCommandClass(NewsletterCommand.class);
        _controller.setFormView("/community/newsletters/popup/mss/page1");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page2.page");

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
        assertEquals("", command.getEmail());
    }

    public void testGoodInputOnBindOnNewForm() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");

        command.setSchoolId(1);
        command.setState(State.CA);
        _controller.onBindOnNewForm(getRequest(), command, errors);

        assertFalse(errors.hasErrors());
        assertFalse(StringUtils.isEmpty(command.getSchoolName()));
    }


    public void testGoodInputOnSubmit() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        final String email = "blahblahblah@greatschools.net";
        final int schoolId = 1;

        command.setSchoolId(schoolId);
        command.setState(State.CA);
        command.setEmail(email);
        command.setMystat(true);
        command.setGn(true);

        ModelAndView modelView = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        Map model = modelView.getModel();
        assertEquals(command.getState().toString(), model.get("state").toString());
        assertEquals(command.getEmail(), model.get("email").toString());
        assertEquals(String.valueOf(command.getSchoolId()), model.get("schoolId").toString());

        _log.debug(modelView.getViewName());
        assertEquals(modelView.getViewName(), "redirect:/community/newsletters/popup/mss/page2.page");

        ThreadLocalTransactionManager.commitOrRollback();

        User user = _userDao.getUserFromEmailIfExists(email);
        assertEquals("blahblahblah@greatschools.net", user.getEmail());
        Set subscriptions = user.getSubscriptions();

        Cookie cookie = getResponse().getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertNotNull(cookie);
        assertEquals(CookieGenerator.DEFAULT_COOKIE_MAX_AGE, cookie.getMaxAge());
        assertEquals(CookieGenerator.DEFAULT_COOKIE_PATH, cookie.getPath());
        assertEquals(cookie.getValue(), user.getId().toString());
        
        boolean hasMss = false;
        boolean hasGn = false;

        assertEquals(2, subscriptions.size());

        for (Iterator iter = subscriptions.iterator(); iter.hasNext();) {
            Subscription sub = (Subscription) iter.next();
            SubscriptionProduct product = sub.getProduct();

            if (product.isNewsletter()) {
                if (product.getName().equals(SubscriptionProduct.MYSTAT.getName())) {
                    if (sub.getSchoolId() == schoolId) {
                        hasMss = true;
                    }
                }

                if (product.getName().equals(SubscriptionProduct.PARENT_ADVISOR.getName())) {
                    hasGn = true;
                }
            }
        }

        assertTrue(hasMss);
        assertTrue(hasGn);

        _userDao.removeUser(user.getId());
        assertNull(_userDao.getUserFromEmailIfExists(email));
    }
}
