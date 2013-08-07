package gs.web.community.registration.popup;

import gs.data.community.*;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.BaseControllerTestCase;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.community.registration.RegistrationOrLoginBehavior;
import gs.web.community.registration.UserRegistrationOrLoginService;
import org.apache.commons.lang.StringUtils;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverControllerTest extends BaseControllerTestCase {
    private RegistrationHoverController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ITableDao _tableDao;
    private RegistrationHoverCommand _command;
    private BindException _errors;
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;
    private EmailVerificationEmail _emailVerificationEmail;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new RegistrationHoverController();

        _userDao = createStrictMock(IUserDao.class);
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _tableDao = createStrictMock(ITableDao.class);
        _emailVerificationEmail = org.easymock.classextension.EasyMock.createStrictMock(EmailVerificationEmail.class);

        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);

        _command = new RegistrationHoverCommand();
        _command.setEmail("RegistrationHoverControllerTest@greatschools.org");
        //_command.getUser().setId(123); // to fake the database save
        _command.setPassword("foobar");
        _command.setConfirmPassword("foobar");
        _command.setNewsletter(false);
        _command.setJoinHoverType(RegistrationHoverCommand.JoinHoverType.LearningDifficultiesNewsletter);
        _errors = new BindException(_command, "");
        _userRegistrationOrLoginService = org.easymock.classextension.EasyMock.createStrictMock(UserRegistrationOrLoginService.class);
        _controller.setUserRegistrationOrLoginService(_userRegistrationOrLoginService);
        _controller.setEmailVerificationEmail(_emailVerificationEmail);

        getRequest().setRemoteAddr("127.0.0.1");
        getRequest().setServerName("dev.greatschools.org");
    }

    public void replayMocks() {
        replay(_userDao);
        replay(_subscriptionDao);
        replay(_tableDao);
        org.easymock.classextension.EasyMock.replay(_userRegistrationOrLoginService);
        org.easymock.classextension.EasyMock.replay(_emailVerificationEmail);
    }

    public void verifyMocks() {
        verify(_userDao);
        verify(_subscriptionDao);
        verify(_tableDao);
        org.easymock.classextension.EasyMock.verify(_userRegistrationOrLoginService);
        org.easymock.classextension.EasyMock.verify(_emailVerificationEmail);
    }

    public void resetMocks() {
        reset(_userDao);
        reset(_subscriptionDao);
        reset(_tableDao);
        org.easymock.classextension.EasyMock.reset(_userRegistrationOrLoginService);
        org.easymock.classextension.EasyMock.reset(_emailVerificationEmail);
    }

    public void testCalculateHoverToShow() {
        User user = new User();
        user.setId(1);
        String result;

        _command.setJoinHoverType(RegistrationHoverCommand.JoinHoverType.SchoolReview);
        result = _controller.calculateHoverToShow(_command);
        assertEquals(
            "Expect controller to set hover popup to custom school review hover, since join hover version was SchoolReview",
            "validateEmailSchoolReview",
            result
        );

        _command.setJoinHoverType(RegistrationHoverCommand.JoinHoverType.Auto);
        user.setEmailVerified(true);
        result = _controller.calculateHoverToShow(_command);
        assertEquals(
            "Expect controller to set hover popup to standard 'validateEmail' hover since brand new user was created",
            "validateEmail",
            result
        );

        // TODO: need more assertions
    }


    public void testOnSubmitNewUser() throws Exception {
        UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();

        User user = new User();
        user.setId(1);

        expect(_userRegistrationOrLoginService.isIPBlocked(_request)).andReturn(false);
        expect(_userRegistrationOrLoginService.registerUser(eq(_command), isA(RegistrationOrLoginBehavior.class), isA(
            BindingResult.class), eq(getRequest()))).andReturn(summary);
        _emailVerificationEmail.sendVerificationEmail(_request, null, "/");

        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
    }

    public void testSitePrefCookieSet() throws Exception {
        setUpSubmitExpectations();
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(getResponse().getCookie("site_pref"));
    }

    public void testOnSubmitExistingUser() throws Exception {
        User user = new User();
        user.setId(123);
        user.setEmail("RegistrationHoverControllerTest@greatschools.org");

        setUpSubmitExpectations();
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
    }

    public void testExistingUserUpdated() throws Exception {
        
        User user = new User();
        user.setId(123);
        user.setEmail("RegistrationHoverControllerTest@greatschools.org");

        user.setFirstName("");
        _command.setFirstName("John");

        setUpSubmitExpectations();
        replayMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);

        verifyMocks();
    }

    public void testOnSubmitProvisionalUser() throws Exception {
        User user = new User();
        user.setId(123);
        user.setEmail("RegistrationHoverControllerTest@greatschools.org");
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");
        UserProfile profile = new UserProfile();
        profile.setId(123);
        user.setUserProfile(profile);

        setUpSubmitExpectations();
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
    }

    public void testOnSubmitNewUserWithSubscriptions() throws Exception {
        _command.setNewsletter(true);


        UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();
        User user = new User();
        user.setId(1);
        summary.setUser(user);

        expect(_userRegistrationOrLoginService.isIPBlocked(_request)).andReturn(false);

        expect(_userRegistrationOrLoginService.registerUser(eq(_command), isA(RegistrationOrLoginBehavior.class), isA(
            BindingResult.class), eq(getRequest()))).andReturn(summary);

        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        _emailVerificationEmail.sendVerificationEmail(_request, user, "/");
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
    }

    public void testOnSubmitBlockedIP() throws Exception {
        _controller.setErrorView("error");
        expect(_userRegistrationOrLoginService.isIPBlocked(_request)).andReturn(true);
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect error view", StringUtils.equals(mAndV.getViewName(), "error"));
    }

    private void setUpSubmitExpectations() throws Exception {
        UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();
        expect(_userRegistrationOrLoginService.isIPBlocked(_request)).andReturn(false);
        expect(_userRegistrationOrLoginService.registerUser(eq(_command), isA(RegistrationOrLoginBehavior.class), isA(
            BindingResult.class), eq(getRequest()))).andReturn(summary);
        _emailVerificationEmail.sendVerificationEmail(_request, null, "/");
    }

    public void testRedirect() throws Exception {
        setUpSubmitExpectations();
        _command.setRedirectUrl("/path");
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertEquals("redirect:/path", mAndV.getViewName());

        resetMocks();

        setUpSubmitExpectations();
        _command.setRedirectUrl("/path/");
        replayMocks();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertEquals("redirect:/path/", mAndV.getViewName());

        resetMocks();

        setUpSubmitExpectations();
        _command.setRedirectUrl("/path?foo=bar");
        replayMocks();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertEquals("redirect:/path?foo=bar", mAndV.getViewName());

        resetMocks();

        setUpSubmitExpectations();
        _command.setRedirectUrl("/path?foo=bar&taz=mil");
        replayMocks();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertEquals("redirect:/path?foo=bar&taz=mil", mAndV.getViewName());

        resetMocks();

        setUpSubmitExpectations();
        _command.setRedirectUrl("/path#anchor");
        replayMocks();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertEquals("redirect:/path#anchor", mAndV.getViewName());

        resetMocks();

        setUpSubmitExpectations();
        _command.setRedirectUrl("/path?foo=bar#anchor");
        replayMocks();
        mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertEquals("redirect:/path?foo=bar#anchor", mAndV.getViewName());
    }
}
