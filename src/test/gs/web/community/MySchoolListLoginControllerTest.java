package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;
import java.util.ArrayList;

public class MySchoolListLoginControllerTest extends BaseControllerTestCase {

    MySchoolListLoginController _controller;
    IUserDao _mockUserDao;
    ISubscriptionDao _mockSubscriptionDao;
    User _testUser;
    MySchoolListConfirmationEmail _email;

    public void setUp() throws Exception {
        super.setUp();

        _testUser = new User();
        _testUser.setEmail("eford@greatschools.net");
        _testUser.setId(1);

        _controller = new MySchoolListLoginController();
        _controller.setCommandClass(gs.web.community.LoginCommand.class);
        _controller.setFormView("/community/mySchoolListLogin");
        _controller.setSuccessView("/community/mySchoolList");
        _mockUserDao = createStrictMock(IUserDao.class);
        _mockSubscriptionDao = createStrictMock(ISubscriptionDao.class);
        _controller.setSubscriptionDao(_mockSubscriptionDao);
        _controller.setUserDao(_mockUserDao);
        _email = createStrictMock(MySchoolListConfirmationEmail.class);
        _controller.setMySchoolListConfirmationEmail(_email);
    }

    public void testRequestWithKnownUser() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);
        getRequest().setMethod("GET");
        replay(_email);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_email);
        // for now, known users will not be redirected to the msl - instead, they will be
        // allowed to change identity on the login page.
        assertEquals("Expected msl login form view", "/community/mySchoolListLogin", mAndV.getViewName());
    }

    public void testRequestWithUnknownUser() throws Exception {
        getRequest().setMethod("GET");
        replay(_email);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_email);
        assertEquals("Expected msl login form view", "/community/mySchoolListLogin", mAndV.getViewName());
    }

    public void testSubmitWithKnownEmail() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        assertNull("Member id should not be set", sc.getUser());
        getRequest().setParameter("email", _testUser.getEmail());
        getRequest().setMethod("POST");
        expect(_mockUserDao.findUserFromEmailIfExists(_testUser.getEmail())).andReturn(_testUser);
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        replay(_email);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("P3P header must be set", "CP=\"CAO PSA OUR\"", getResponse().getHeader("P3P"));
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        verify(_email);
        assertEquals("Expected the MSL page", _controller.getSuccessView(), mAndV.getViewName());
        assertEquals("Member cookie should now be in response", "1", getResponse().getCookie("MEMID").getValue());
    }

    public void testSubmitWithCommandParameters() throws Exception {
        // Regression test for GS-7623
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        assertNull("Member id should not be set", sc.getUser());
        getRequest().setParameter("email", _testUser.getEmail());
        getRequest().setMethod("POST");
        getRequest().setParameter("command", "add");
        getRequest().setParameter("ids", "8485");
        getRequest().setParameter("state", "CA");
        expect(_mockUserDao.findUserFromEmailIfExists(_testUser.getEmail())).andReturn(_testUser);
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        replay(_email);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        verify(_email);
        assertEquals("Expected the MSL page", _controller.getSuccessView(), mAndV.getViewName());
        assertEquals("Member cookie should now be in response", "1", getResponse().getCookie("MEMID").getValue());
        assertEquals("add", mAndV.getModel().get("command"));
        assertEquals("8485", mAndV.getModel().get("ids"));
        assertEquals("CA", mAndV.getModel().get("state"));
    }

    public void testSubmitWithUnknownEmail() throws Exception {
        assertNull("Member cookie should not be set", getResponse().getCookie("MEMID"));
        _testUser.setEmail("foo@flimflam.com");
        _testUser.setId(1234);
        getRequest().setParameter("email", _testUser.getEmail());
        getRequest().setMethod("POST");
        expect(_mockUserDao.findUserFromEmailIfExists(_testUser.getEmail())).andReturn(null);
        _mockUserDao.saveUser(_testUser);
        expect(_mockUserDao.findUserFromEmail(_testUser.getEmail())).andReturn(_testUser);
        _email.sendToUser(_testUser, getRequest());
        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        replay(_email);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        verify(_email);
        assertEquals("Expected the MSL page", _controller.getSuccessView(), mAndV.getViewName());
        assertEquals("Member cookie should be set set to user id", "1234", getResponse().getCookie("MEMID").getValue());
    }

    public void testSubmitWithKnownEmailAndParentAdvisor() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        assertNull("Member id should not be set", sc.getUser());
        getRequest().setParameter("email", _testUser.getEmail());
        getRequest().setParameter("pa", "pa");
        getRequest().setMethod("POST");
        expect(_mockUserDao.findUserFromEmailIfExists(_testUser.getEmail())).andReturn(_testUser);

        List<Subscription> subs = new ArrayList<Subscription>();
        State state = SessionContextUtil.getSessionContext(getRequest()).getStateOrDefault();
        Subscription sub = new Subscription();
        sub.setUser(_testUser);
        sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub.setState(state);
        subs.add(sub);
        _mockSubscriptionDao.addNewsletterSubscriptions(_testUser, subs);        

        replay(_mockUserDao);
        replay(_mockSubscriptionDao);
        replay(_email);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_mockUserDao);
        verify(_mockSubscriptionDao);
        verify(_email);

        assertEquals("Expected the MSL page", _controller.getSuccessView(), mAndV.getViewName());
        assertEquals("Member cookie should now be in response", "1", getResponse().getCookie("MEMID").getValue());
    }

    public void testOnBindAndValidate() throws Exception {
        LoginCommand command = new LoginCommand();
        BindException errors = new BindException(command, "");

        command.setEmail(null);
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertEquals("Expected one error", 1, errors.getErrorCount());
        FieldError error = (FieldError)errors.getAllErrors().get(0);
        assertEquals("Expected field: " + MySchoolListLoginController.EMAIL_FIELD_CODE,
            MySchoolListLoginController.EMAIL_FIELD_CODE, error.getField());
        assertEquals("Expected error message to be: " + MySchoolListLoginController.ERROR_EMPTY_EMAIL_ADDRESS,
            MySchoolListLoginController.ERROR_EMPTY_EMAIL_ADDRESS, error.getDefaultMessage());

        errors = new BindException(command, "");

        command.setEmail("");
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertEquals("Expected one error", 1, errors.getErrorCount());
        error = (FieldError)errors.getAllErrors().get(0);
        assertEquals("Expected field: " + MySchoolListLoginController.EMAIL_FIELD_CODE,
            MySchoolListLoginController.EMAIL_FIELD_CODE, error.getField());
        assertEquals("Expected error message to be: " + MySchoolListLoginController.ERROR_EMPTY_EMAIL_ADDRESS,
            MySchoolListLoginController.ERROR_EMPTY_EMAIL_ADDRESS, error.getDefaultMessage());

        errors = new BindException(command, "");

        command.setEmail("foo");
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertEquals("Expected one error", 1, errors.getErrorCount());
        error = (FieldError)errors.getAllErrors().get(0);
        assertEquals("Expected field: " + MySchoolListLoginController.EMAIL_FIELD_CODE,
            MySchoolListLoginController.EMAIL_FIELD_CODE, error.getField());
        assertEquals("Expected error message to be: " + MySchoolListLoginController.ERROR_INVALID_EMAIL_ADDRESS,
            MySchoolListLoginController.ERROR_INVALID_EMAIL_ADDRESS, error.getDefaultMessage());

        errors = new BindException(command, "");

        command.setEmail("foo@foo.com");
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertEquals("Expected no errors", 0, errors.getErrorCount());
    }
}
