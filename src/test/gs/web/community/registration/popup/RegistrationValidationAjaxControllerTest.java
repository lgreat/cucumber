package gs.web.community.registration.popup;

import gs.data.community.IUserDao;
import gs.web.BaseControllerTestCase;
import gs.data.community.User;

import static org.easymock.classextension.EasyMock.*;
import static gs.web.community.registration.popup.RegistrationValidationAjaxController.*;

import gs.web.community.registration.UserCommand;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

import javax.servlet.http.Cookie;

/**
 * @author ssprouse
 */
public class RegistrationValidationAjaxControllerTest extends BaseControllerTestCase {

    private RegistrationValidationAjaxController _controller;

    private IUserDao _userDao;
    private User _user;
    private UserCommand _command;
    private BindException _errors;
    private UserCommandValidator _mockUserCommandValidator;

    @Override
    public void setUp() throws Exception {

        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationValidationAjaxController) appContext.getBean(RegistrationValidationAjaxController.BEAN_ID);

        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);

        UserCommandValidator userCommandValidator = new UserCommandValidator();

        _mockUserCommandValidator = createStrictMock(UserCommandValidator.class);

        _controller.setUserCommandValidator(userCommandValidator);

        _user = new User();
        _user.setEmail("megajoin@greatschools.org");
        _user.setId(99);

        _command = new UserCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");

        // Overridden so that successive calls to addCookie with the same cookie will overwrite the previous value
        // This is the behavior of the real HttpServletResponse so I'm unclear on why the mock one fails so hard
        _response = new MockHttpServletResponse() {
            @Override
            public void addCookie(Cookie cookie) {
                if (getCookie(cookie.getName()) != null) {
                    getCookie(cookie.getName()).setValue(cookie.getValue());
                }
                super.addCookie(cookie);
            }
        };
    }

    public void testBehaviorWithChooserTipSheet() throws Exception {
        _command.setFirstName("Samson");
        _command.setScreenName("testRegistrationValidationController");
        _command.setBrainDrainNewsletter(false);
        _command.setChooserRegistration(true);
        _command.setConfirmPassword("abcdefg");
        _command.setEmail("testRegistrationValidationController@greatschools.org");
        _command.setPassword("abcdefg");
        _command.setTerms(true);

        getRequest().setParameter("joinHoverType", "ChooserTipSheet");
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        replay(_userDao);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validateEmail(_command, getRequest(), _errors)).andReturn(_user);
        _mockUserCommandValidator.validateFirstName(_command, _errors);
        _mockUserCommandValidator.validateUsername(_command, _user, _errors);
        expect(_mockUserCommandValidator.validatePasswordFormat("abcdefg", "password", _errors)).andReturn(true);
        expect(_mockUserCommandValidator.validatePasswordEquivalence("abcdefg", "abcdefg", "confirmPassword", _errors)).andReturn(true);
        _mockUserCommandValidator.validateTerms(_command, _errors);

        replay(_mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);

        verify(_mockUserCommandValidator);
        verify(_userDao);
    }

    public void testFailureWithNoTerms() throws Exception {
        _command.setFirstName("Samson");
        _command.setScreenName("testRegistrationValidationController");
        _command.setBrainDrainNewsletter(false);
        _command.setChooserRegistration(true);
        _command.setConfirmPassword("abcdefg");
        _command.setEmail("testRegistrationValidationController@greatschools.org");
        _command.setPassword("abcdefg");
        _command.setTerms(false);

        expect(_userDao.findUserFromEmailIfExists("testRegistrationValidationController@greatschools.org")).andReturn(_user);

        replay(_userDao);

        getRequest().setAttribute("joinHoverType", "ChooserTipSheet");

        _controller.handle(getRequest(), getResponse(), _command, _errors);

        verify(_userDao);
        System.err.println(getResponse().getContentAsString());
        assertTrue("Controller does not have expected errors on validate", StringUtils.containsIgnoreCase(getResponse().getContentAsString(), "Please read and accept our Terms of Use to join GreatSchools."));
    }

    // Set up a command
    private void setupCommand() {
        _command.setFirstName("Anthony");
        _command.setEmail("testRegistrationValidationController@greatschools.org");
        _command.setScreenName("testRegistrationValidationController");
        _command.setPassword("abcdefg");
        _command.setConfirmPassword("abcdefg");
        _command.setTerms(true);
    }

    public void testFieldValidationFirstName() throws Exception {
        setupCommand();

        getRequest().setParameter(FIELD_PARAMETER, FIRST_NAME);
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        _mockUserCommandValidator.setUserDao(_userDao);
        _mockUserCommandValidator.validateFirstName(_command, _errors);

        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
    }

    public void testFieldValidationEmail() throws Exception {
        setupCommand();

        getRequest().setParameter(FIELD_PARAMETER, EMAIL);
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validateEmail(_command, getRequest(), _errors)).andReturn(null);

        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
    }

    public void testFieldValidationFormatEmail() throws Exception {
        setupCommand();

        getRequest().setParameter(FIELD_PARAMETER, EMAIL);
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validateEmail(_command, getRequest(), _errors)).andReturn(null);


        _command.setEmail("notAnEmail&notADomain,foo");
        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
        assertNotNull(_errors.getFieldErrors());
        assertEquals("Expect invalid email format to be rejected", 1, _errors.getFieldErrors().size());
        assertNotNull(_errors.getFieldError("email"));

        resetMocks(_userDao, _mockUserCommandValidator);
        _errors = new BindException(_command, "");
        _command.setEmail("email@example.com");
        
        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validateEmail(_command, getRequest(), _errors)).andReturn(null);

        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
        assertNotNull(_errors.getFieldErrors());
        assertEquals(0, _errors.getFieldErrors().size());
        assertNull(_errors.getFieldError("email"));
    }

    public void testFieldValidationUsername() throws Exception {
        setupCommand();

        getRequest().setParameter(FIELD_PARAMETER, USERNAME);
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _mockUserCommandValidator.validateUsername(_command, null, _errors);

        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
    }

    public void testFieldValidationPassword() throws Exception {
        setupCommand();

        getRequest().setParameter(FIELD_PARAMETER, PASSWORD);
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validatePasswordFormat(_command.getPassword(), "password", _errors))
                .andReturn(true);

        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
    }

    public void testFieldValidationConfirmPassword() throws Exception {
        setupCommand();

        getRequest().setParameter(FIELD_PARAMETER, CONFIRM_PASSWORD);
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validatePasswordEquivalence
                (_command.getPassword(), _command.getConfirmPassword(), "confirmPassword", _errors))
                .andReturn(true);

        replayMocks(_userDao, _mockUserCommandValidator);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        verifyMocks(_userDao, _mockUserCommandValidator);
    }

    public void testFailureBecauseFacebookAccount() throws Exception {
        _request.setParameter(RegistrationValidationAjaxController.FIELD_PARAMETER, RegistrationValidationAjaxController.EMAIL);
        _command.setFirstName("Samson");
        _command.setScreenName("testRegistrationValidationController");
        _command.setEmail("testRegistrationValidationController@greatschools.org");
        _command.setTerms(true);

        _user.setFacebookId("abc");
        _user.setHow("facebook");

        expect(_userDao.findUserFromEmailIfExists("testRegistrationValidationController@greatschools.org")).andReturn(
            _user
        );

        replay(_userDao);

        getRequest().setAttribute("joinHoverType", "ChooserTipSheet");

        _controller.handle(getRequest(), getResponse(), _command, _errors);

        verify(_userDao);
        assertTrue(
            "Expect error message since user is a 'Facebook user' and must log in with Facebook",
            StringUtils.containsIgnoreCase(
                getResponse().getContentAsString(),
                "This account is linked to a Facebook account. Please sign in using Facebook."
            )
        );
    }

    /*
    public void testBasics() throws Exception {
        _command.setFirstName("Samson");
        _command.setScreenName("ssprouse");
        _command.setBrainDrainNewsletter(false);
        _command.setChooserRegistration(true);
        _command.setConfirmPassword("abcdefg");
        _command.setEmail("ssprouse+10@greatschools.org");
        _command.setPassword("abcdefg");
        _command.setTerms(true);

        getRequest().setAttribute("joinHoverType", "ChooserTipSheet");
        //expect(_userCommandValidator.validate(getRequest(), _command, _errors));

        //expect(_userCommandValidator.validateStateCity(getRequest(), _command, _errors))

        //replay(_userDao);
        _controller.handle(getRequest(), getResponse(), _command, _errors);
        //verify(_userDao);
        System.err.println(getResponse().getContentAsString());
        //assertTrue("Controller does not have expected errors on validate", StringUtils.containsIgnoreCase(getResponse().getContentAsString(),"Please read and accept our Terms of Use to join GreatSchools."));
    }
    */
}
