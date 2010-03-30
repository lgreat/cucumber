package gs.web.community.registration.popup;

import java.util.List;
import java.util.Map;

import gs.data.community.IUserDao;
import gs.web.BaseControllerTestCase;
import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.community.User;

import static org.easymock.classextension.EasyMock.*;

import gs.web.community.registration.UserCommand;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

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
    private UserCommandValidator _userCommandValidator;
    private UserCommandValidator _mockUserCommandValidator;

    @Override
    public void setUp() throws Exception {

        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationValidationAjaxController) appContext.getBean(RegistrationValidationAjaxController.BEAN_ID);

        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);

        _userCommandValidator = new UserCommandValidator();

        _mockUserCommandValidator = createStrictMock(UserCommandValidator.class);

        _controller.setUserCommandValidator(_userCommandValidator);

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

        getRequest().setAttribute("joinHoverType", "ChooserTipSheet");
        _controller.setUserCommandValidator(_mockUserCommandValidator);

        replay(_userDao);

        _mockUserCommandValidator.setUserDao(_userDao);
        expect(_mockUserCommandValidator.validateEmail(_command, getRequest(), _errors)).andReturn(_user);
        _mockUserCommandValidator.validateFirstName(_command, _errors);
        _mockUserCommandValidator.validateUsername(_command, _user, _errors);
        _mockUserCommandValidator.validatePassword(_command, _errors);
        _mockUserCommandValidator.validateTerms(_command, _errors);

        _mockUserCommandValidator.validateStateCity(_command, _errors);

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

    public RegistrationValidationAjaxController getController() {
        return _controller;
    }

    public void setController(RegistrationValidationAjaxController controller) {
        _controller = controller;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }

    public UserCommand getCommand() {
        return _command;
    }

    public void setCommand(UserCommand command) {
        _command = command;
    }

    public BindException getErrors() {
        return _errors;
    }

    public void setErrors(BindException errors) {
        _errors = errors;
    }

    public UserCommandValidator getUserCommandValidator() {
        return _userCommandValidator;
    }

    public void setUserCommandValidator(UserCommandValidator userCommandValidator) {
        _userCommandValidator = userCommandValidator;
    }
}
