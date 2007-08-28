/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MssPaControllerTest.java,v 1.18 2007/08/28 00:34:51 aroy Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.data.admin.IPropertyDao;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.EmailValidator;
import gs.web.util.validator.MaximumMssValidator;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;
import org.springframework.mock.web.MockHttpServletResponse;

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
    private IPropertyDao _propertyDao;

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
        _subscriptionDao = (ISubscriptionDao) getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _propertyDao = (IPropertyDao) getApplicationContext().getBean(IPropertyDao.BEAN_ID);

        _controller = new MssPaController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setCommandClass(NewsletterCommand.class);
        _controller.setFormView("/community/newsletters/popup/mss/page1");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page2.page");
        _controller.setPropertyDao(_propertyDao);
        
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

        final String email = "dlee@greatschools.net";

        User user = new User();
        user.setEmail(email);
        getSessionContext().setUser(user);
        getSessionContext().setEmail(email);

        command.setSchoolId(1);
        command.setState(State.CA);
        _controller.onBindOnNewForm(getRequest(), command, errors);

        assertFalse(errors.hasErrors());
        assertFalse(StringUtils.isEmpty(command.getSchoolName()));
        assertEquals(email, command.getEmail());

        command.setEmail(null);
        getSessionContext().setUser(null);
        getSessionContext().setEmail(null);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals("", command.getEmail());
    }

    public void testMaxedOutUserOnBindAndValidate() {
        _controller.setUserDao(new MockMaxedOutMssUserDao());
        NewsletterCommand command = new NewsletterCommand();
        command.setMystat(true);
        command.setEmail("anythingSinceWereUsingAMockDao");

        BindException errors = new BindException(command, "");
        _controller.onBindAndValidate(getRequest(), command, errors);

        assertTrue(errors.hasErrors());
        ObjectError error = errors.getGlobalError();

        assertEquals(MaximumMssValidator.ERROR_CODE, error.getCode());
        assertFalse(command.isMystat());

        _controller.setUserDao(_userDao);
    }

    public void testNonMaxedOutUserOnBindAndValidate() {
        NewsletterCommand command = new NewsletterCommand();
        command.setMystat(true);
        command.setEmail("anythingSinceWereUsingAMockDao");

        BindException errors = new BindException(command, "");
        _controller.onBindAndValidate(getRequest(), command, errors);

        assertFalse(errors.hasErrors());
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
        assertEquals("true", (String) model.get("newuser"));

        // repeat the request with the same email and make sure that "newuser" is empty
        modelView = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        assertTrue(StringUtils.isBlank((String) modelView.getModel().get("newuser")));

        _log.debug(modelView.getViewName());
        assertEquals(modelView.getViewName(), "redirect:/community/newsletters/popup/mss/page2.page");

        assertEquals(modelView.getModel().get("academicYear"), _propertyDao.getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR));

        ThreadLocalTransactionManager.commitOrRollback();

        User user = _userDao.findUserFromEmailIfExists(email);
        try {
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
        } finally {
            _userDao.removeUser(user.getId());
        }
        assertNull(_userDao.findUserFromEmailIfExists(email));
    }

    public void testSetHoverCookie() throws Exception {
        _controller.handleRequestInternal(getRequest(), getResponse());
        MockHttpServletResponse response = getResponse();
        Cookie[] cookies = response.getCookies();
        assertEquals("Expected to find hover cookie", "hover", cookies[0].getName());
        assertEquals("Expected hover cookie path to be /", "/", cookies[0].getPath());
        assertEquals("Expected cookie to expire in 15 days", 60 * 60 * 24 * 15, cookies[0].getMaxAge());
    }

    private class MockMaxedOutMssUserDao implements IUserDao {

        public void evict(User user) {
            
        }

        public User findUserFromEmail(String string) throws ObjectRetrievalFailureException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public User findUserFromEmailIfExists(String string) {
            User user = new User();
            user.setEmail("dlee@greatschools.net");
            Set subscriptions = new HashSet();

            for (int i = 0; i < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; i++) {
                Subscription sub = new Subscription();
                sub.setProduct(SubscriptionProduct.MYSTAT);
                sub.setSchoolId(i);
                sub.setState(State.CA);
                subscriptions.add(sub);
            }

            user.setSubscriptions(subscriptions);
            return user;
        }

        public User findUserFromId(int i) throws ObjectRetrievalFailureException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void saveUser(User user) throws DataIntegrityViolationException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void updateUser(User user) throws DataIntegrityViolationException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void removeUser(Integer integer) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public List findUsersModifiedSince(Date date) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public List findUsersModifiedBetween(Date date, Date date1) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public User findUserFromScreenNameIfExists(String screenName) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public List<User> findAllCommunityUsers() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
