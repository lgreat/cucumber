/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MssPaControllerTest.java,v 1.19 2007/09/06 23:19:45 aroy Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.admin.IPropertyDao;
import gs.web.BaseControllerTestCase;
import gs.web.util.validator.MaximumMssValidator;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import java.util.*;

/**
 * The purpose is to test the MssPaController.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class MssPaControllerTest extends BaseControllerTestCase {
    private MssPaController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private IPropertyDao _propertyDao;
    private ISchoolDao _schoolDao;
    private Validator _stateValidator;
    private Validator _schoolIdValidator;
    private NewsletterCommand _command;
    private BindException _errors;

    protected void setUp() throws Exception {
        super.setUp();

        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);
        _propertyDao = createMock(IPropertyDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _stateValidator = createMock(Validator.class);
        _schoolIdValidator = createMock(Validator.class);

        _controller = new MssPaController();
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setPropertyDao(_propertyDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setCommandClass(NewsletterCommand.class);
        _controller.setFormView("/community/newsletters/popup/mss/page1");
        _controller.setSuccessView("redirect:/community/newsletters/popup/mss/page2.page");

        List<Validator> onLoadValidators = new ArrayList<Validator>();
        onLoadValidators.add(_stateValidator);
        onLoadValidators.add(_schoolIdValidator);
        _controller.setOnLoadValidators(onLoadValidators);

        _command = new NewsletterCommand();
        _errors = new BindException(_command, "");
    }

    public void testBadInputOnBindOnNewForm() {
        expect(_stateValidator.supports(NewsletterCommand.class)).andReturn(true);
        _stateValidator.validate(_command, _errors);
        replay(_stateValidator);
        expect(_schoolIdValidator.supports(NewsletterCommand.class)).andReturn(true);
        _schoolIdValidator.validate(_command, _errors);
        replay(_schoolIdValidator);
        replay(_schoolDao);

        _errors.reject("foo"); // simulate an error
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        verify(_stateValidator);
        verify(_schoolIdValidator);
        verify(_schoolDao);

        //not passing in request parameters..should get errors
        assertTrue(_errors.hasErrors());
        assertEquals("", _command.getEmail());
    }

    public void testGoodInputOnBindOnNewForm() {
        expect(_stateValidator.supports(NewsletterCommand.class)).andReturn(true);
        _stateValidator.validate(_command, _errors);
        replay(_stateValidator);
        expect(_schoolIdValidator.supports(NewsletterCommand.class)).andReturn(true);
        _schoolIdValidator.validate(_command, _errors);
        replay(_schoolIdValidator);

        School school = new School();
        school.setId(1);
        school.setName("My School");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        replay(_schoolDao);
        final String email = "dlee@greatschools.net";

        User user = new User();
        user.setEmail(email);
        getSessionContext().setUser(user);
        getSessionContext().setEmail(email);

        _command.setSchoolId(1);
        _command.setState(State.CA);
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        verify(_stateValidator);
        verify(_schoolIdValidator);
        verify(_schoolDao);

        assertFalse(_errors.hasErrors());
        assertFalse(StringUtils.isEmpty(_command.getSchoolName()));
        assertEquals(email, _command.getEmail());
        assertEquals("My School", _command.getSchoolName());
    }

    public void testMaxedOutUserOnBindAndValidate() {
        _command.setMystat(true);
        _command.setEmail("dlee@greatschools.net");

        User user = new User();
        user.setEmail("dlee@greatschools.net");
        Set<Subscription> subscriptions = new HashSet<Subscription>();

        for (int i = 0; i < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; i++) {
            Subscription sub = new Subscription();
            sub.setProduct(SubscriptionProduct.MYSTAT);
            sub.setSchoolId(i);
            sub.setState(State.CA);
            subscriptions.add(sub);
        }
        user.setSubscriptions(subscriptions);
        expect(_userDao.findUserFromEmailIfExists("dlee@greatschools.net")).andReturn(user);
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);

        assertTrue(_errors.hasErrors());
        ObjectError error = _errors.getGlobalError();

        assertEquals(MaximumMssValidator.ERROR_CODE, error.getCode());
        assertFalse(_command.isMystat());
    }

    public void testNonMaxedOutUserOnBindAndValidate() {
        _command.setMystat(true);
        _command.setEmail("dlee@greatschools.net");

        expect(_userDao.findUserFromEmailIfExists("dlee@greatschools.net")).andReturn(new User());
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verify(_userDao);

        assertFalse(_errors.hasErrors());
    }

    public void testGoodInputOnSubmit() {
        final String email = "blahblahblah@greatschools.net";
        final int schoolId = 1;

        _command.setSchoolId(schoolId);
        _command.setState(State.CA);
        _command.setEmail(email);
        _command.setMystat(true);
        _command.setGn(true);

        User user = new User();
        user.setId(1);

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);
        replay(_userDao);
        
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), (List)anyObject());
        replay(_subscriptionDao);

        expect(_propertyDao.getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR)).andReturn("2007");
        replay(_propertyDao);

        ModelAndView modelView = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_subscriptionDao);
        verify(_propertyDao);
        Map model = modelView.getModel();
        assertEquals(_command.getState().toString(), model.get("state").toString());
        assertEquals(_command.getEmail(), model.get("email").toString());
        assertEquals("2007", model.get("academicYear").toString());
        assertEquals(String.valueOf(_command.getSchoolId()), model.get("schoolId").toString());
        assertTrue(StringUtils.isBlank((String) modelView.getModel().get("newuser")));
//        assertEquals("true", (String) model.get("newuser"));
        assertEquals("redirect:/community/newsletters/popup/mss/page2.page", modelView.getViewName());
    }

    public void testOnSubmitWithNullUser() {
        final String email = "blahblahblah@greatschools.net";
        final int schoolId = 1;

        _command.setSchoolId(schoolId);
        _command.setState(State.CA);
        _command.setEmail(email);
        _command.setMystat(true);
        _command.setGn(true);

        User user = new User();
        user.setId(1);

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(null);
        _userDao.saveUser(isA(User.class));
        // mock the saveUser call to set the User's id (required for the static PageHelper.setMemberCookie call)
        expectLastCall().andAnswer(new IAnswer<Object>(){
            public Object answer() throws Throwable {
                User user = (User)getCurrentArguments()[0];
                user.setId(1);
                return user;
            }
        });
        replay(_userDao);

        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), (List)anyObject());
        replay(_subscriptionDao);

        expect(_propertyDao.getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR)).andReturn("2007");
        replay(_propertyDao);

        ModelAndView modelView = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
        verify(_subscriptionDao);
        verify(_propertyDao);
        Map model = modelView.getModel();
        assertEquals(_command.getState().toString(), model.get("state").toString());
        assertEquals(_command.getEmail(), model.get("email").toString());
        assertEquals("2007", model.get("academicYear").toString());
        assertEquals(String.valueOf(_command.getSchoolId()), model.get("schoolId").toString());
        assertEquals("Expect newuser to be set", "true",  modelView.getModel().get("newuser"));
        assertEquals("redirect:/community/newsletters/popup/mss/page2.page", modelView.getViewName());
    }

    public void testSetHoverCookie() throws Exception {
        _controller.handleRequestInternal(getRequest(), getResponse());
        MockHttpServletResponse response = getResponse();
        Cookie[] cookies = response.getCookies();
        assertEquals("Expected to find hover cookie", "hover", cookies[0].getName());
        assertEquals("Expected hover cookie path to be /", "/", cookies[0].getPath());
        assertEquals("Expected cookie to expire in 15 days", 60 * 60 * 24 * 15, cookies[0].getMaxAge());
    }
}
