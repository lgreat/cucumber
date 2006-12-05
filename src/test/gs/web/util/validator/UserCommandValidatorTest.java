package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.community.registration.UserCommand;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.easymock.MockControl;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UserCommandValidatorTest extends BaseTestCase {
    private static final String GOOD_EMAIL = "UserCommandValidatorTest@greatschools.net";
    private static final String LONG_EMAIL128 =
            "12345678901234567890123456789012345678901234567890" +
                    "12345678901234567890123456789012345678901234567890" +
                    "1234567890123456789012345678";
    private static final String GOOD_PASSWORD_SHORT = "123456";
    private static final String GOOD_PASSWORD_LONG = "12345678901234";
    private static final String SHORT_PASSWORD = "12345";
    private static final String LONG_PASSWORD = "123456789012345";
    private static final State GOOD_STATE = State.CA;
    private static final String GOOD_CITY = "Oakland";
    private static final String BAD_CITY = "Juneau";
    private static final String GOOD_SCREEN_NAME_SHORT = "jqlx5t";
    private static final String GOOD_SCREEN_NAME_LONG = "12345678901234";
    private static final String SHORT_SCREEN_NAME = "12345";
    private static final String LONG_SCREEN_NAME = "123456789012345";
    private static final String BAD_SCREEN_NAME_SPACE = "my name";
    private static final String BAD_SCREEN_NAME_NONALPHANUMERIC = "Great$chools";
    private static final String GOOD_FIRST_NAME_LONG =
            "abcdefghijabcdefghijabcd";
    private static final String LONG_FIRST_NAME = GOOD_FIRST_NAME_LONG + "e";
    private static final String BAD_FIRST_NAME = "Anthony2";
//    private static final String GOOD_LAST_NAME_LONG =
//            "1234567890123456789012345678901234567890123456789012345678901234";
    private static final String LONG_LAST_NAME =
            "12345678901234567890123456789012345678901234567890123456789012345";

    private IUserDao _userDao;
    private MockControl _userControl;
    private UserCommandValidator _validator;
    private GsMockHttpServletRequest _request;

    protected void setUp() throws Exception {
        super.setUp();
        _validator = new UserCommandValidator();
        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _validator.setUserDao(_userDao);
        _request = new GsMockHttpServletRequest();
        _request.setServerName("www.greatschools.net");
    }

    private UserCommand setupCommand() {
        UserCommand command = new UserCommand();
        command.setEmail(GOOD_EMAIL);
        command.setConfirmEmail(GOOD_EMAIL);
        command.setPassword(GOOD_PASSWORD_SHORT);
        command.setConfirmPassword(GOOD_PASSWORD_SHORT);
        command.setState(GOOD_STATE);
        command.setCity(GOOD_CITY);
        command.setFirstName(GOOD_FIRST_NAME_LONG);
        //command.setLastName(GOOD_LAST_NAME64);
        command.setScreenName(GOOD_SCREEN_NAME_SHORT);
        command.setGender("m");
        command.setNumSchoolChildren(new Integer(0));
        command.setTerms(true);
        setupUserControl(GOOD_EMAIL, GOOD_SCREEN_NAME_SHORT);
        return command;
    }

    private void setupUserControl(String email, String screenName) {
        _userControl.reset();
        if (email != null) {
            _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
        }
        if (screenName != null) {
            _userControl.expectAndReturn(_userDao.findUserFromScreenNameIfExists(screenName),
                    null);
        }
        _userControl.replay();
    }

    public void testSuccess() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertFalse(errors.toString(), errors.hasErrors());

        setupUserControl(GOOD_EMAIL, GOOD_SCREEN_NAME_SHORT);

        command.setPassword(GOOD_PASSWORD_LONG);
        command.setConfirmPassword(GOOD_PASSWORD_LONG);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
    }

    // we no longer use this rule
    public void xtestEmailExists() {
        User user = _userDao.findUserFromId(1);
        assertNotNull(user); // expect DB to have pre-existing user

        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setEmail(user.getEmail());
        command.setConfirmEmail(user.getEmail());
        command.setPassword(GOOD_PASSWORD_SHORT);
        command.setConfirmPassword(GOOD_PASSWORD_SHORT);

        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        // existing emails allowed if id is set
        command.setId("1");
        errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testLongEmail() {
        UserCommand command = setupCommand();
        command.setEmail(LONG_EMAIL128);
        setupUserControl(null, GOOD_SCREEN_NAME_SHORT);
        Errors errors = new BindException(command, "");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("email"));
    }

    public void testShortPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(SHORT_PASSWORD);
        command.setConfirmPassword(SHORT_PASSWORD);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testEmptyPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword("");
        command.setConfirmPassword("");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(errors.toString(), 1, errors.getErrorCount());
    }

    public void testLongPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(LONG_PASSWORD);
        command.setConfirmPassword(LONG_PASSWORD);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testMismatchedPasswords() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(GOOD_PASSWORD_SHORT);
        command.setConfirmPassword(GOOD_PASSWORD_LONG);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void xtestMismatchedEmails() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setConfirmEmail(GOOD_EMAIL + "2");

        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testEmptyState() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setState(null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testEmptyCity() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setCity("");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void xtestMismatchedStateCity() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setCity(BAD_CITY);

        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoFirstName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setFirstName("");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testLongFirstName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setFirstName(LONG_FIRST_NAME);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testBadFirstName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setFirstName(BAD_FIRST_NAME);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void xtestNoLastName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setLastName("");

        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void xtestLongLastName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setLastName(LONG_LAST_NAME);

        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoScreenName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setScreenName("");
        setupUserControl(GOOD_EMAIL, null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testShortScreenName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setScreenName(SHORT_SCREEN_NAME);
        setupUserControl(GOOD_EMAIL, null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testLongScreenName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setScreenName(GOOD_SCREEN_NAME_LONG);
        setupUserControl(GOOD_EMAIL, GOOD_SCREEN_NAME_LONG);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());

        command.setScreenName(LONG_SCREEN_NAME);
        setupUserControl(GOOD_EMAIL, null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    /**
     * Test for non-alphanumeric and space
     */
    public void testBadScreenName() {
        UserCommand command = setupCommand();
        // test space
        Errors errors = new BindException(command, "");

        command.setScreenName(BAD_SCREEN_NAME_SPACE);
        setupUserControl(GOOD_EMAIL, null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        
        // test non-alphanumeric
        errors = new BindException(command, "");

        command.setScreenName(BAD_SCREEN_NAME_NONALPHANUMERIC);
        setupUserControl(GOOD_EMAIL, null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoGender() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setGender(null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testBadGender() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setGender("q");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        errors = new BindException(command, "");
        setupUserControl(GOOD_EMAIL, GOOD_SCREEN_NAME_SHORT);

        command.setGender("some weird String");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoNumChildren() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setNumSchoolChildren(null);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        errors = new BindException(command, "");
        setupUserControl(GOOD_EMAIL, GOOD_SCREEN_NAME_SHORT);
        command.setNumSchoolChildren(new Integer(-1));

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    /**
     * No children should not generate an error if "other" is selected for gender.
     */
    public void testOtherGenderOnChildren() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setNumSchoolChildren(null);
        command.setGender("u");

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    /**
     * No on terms should not generate an error unless "other" is selected for gender or 0 children.
     */
    public void testNormalGenderOnTerms() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setTerms(false);
        command.setNumSchoolChildren(new Integer(1));

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    public void testTermsOfService() {
        // "other" gender requires terms of service
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setGender("u");
        command.setTerms(false);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("terms"));

        // 0 children requires terms of service
        errors = new BindException(command, "");
        setupUserControl(GOOD_EMAIL, GOOD_SCREEN_NAME_SHORT);
        command.setNumSchoolChildren(new Integer(0));
        command.setGender("m");
        command.setTerms(false);

        _validator.validate(_request, command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("terms"));
    }
}