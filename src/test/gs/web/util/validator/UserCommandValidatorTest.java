package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.web.BaseTestCase;
import gs.web.community.registration.UserCommand;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

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
            "12345678901234567890123456789012345678901234567890";
    private static final String LONG_FIRST_NAME = GOOD_FIRST_NAME_LONG + "1";
    private static final String GOOD_LAST_NAME_LONG =
            "1234567890123456789012345678901234567890123456789012345678901234";
    private static final String LONG_LAST_NAME =
            "12345678901234567890123456789012345678901234567890123456789012345";

    private IUserDao _userDao;
    private UserCommandValidator _validator = new UserCommandValidator();

    protected void setUp() throws Exception {
        super.setUp();
        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        IGeoDao geoDao = (IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID);
        _validator.setUserDao(_userDao);
        _validator.setGeoDao(geoDao);
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
        return command;
    }

    public void testSuccess() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());

        command.setPassword(GOOD_PASSWORD_LONG);
        command.setConfirmPassword(GOOD_PASSWORD_LONG);

        _validator.validate(command, errors);
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

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        // existing emails allowed if id is set
        command.setId("1");
        errors = new BindException(command, "");
        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testLongEmail() {
        UserCommand command = setupCommand();
        command.setEmail(LONG_EMAIL128);
        Errors errors = new BindException(command, "");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("email"));
    }

    public void testShortPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(SHORT_PASSWORD);
        command.setConfirmPassword(SHORT_PASSWORD);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testEmptyPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword("");
        command.setConfirmPassword("");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testLongPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(LONG_PASSWORD);
        command.setConfirmPassword(LONG_PASSWORD);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testMismatchedPasswords() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(GOOD_PASSWORD_SHORT);
        command.setConfirmPassword(GOOD_PASSWORD_LONG);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testMismatchedEmails() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setConfirmEmail(GOOD_EMAIL + "2");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testEmptyState() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setState(null);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testEmptyCity() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setCity("");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testMismatchedStateCity() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setCity(BAD_CITY);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoFirstName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setFirstName("");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testLongFirstName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setFirstName(LONG_FIRST_NAME);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void xtestNoLastName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setLastName("");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void xtestLongLastName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setLastName(LONG_LAST_NAME);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoScreenName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setScreenName("");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testShortScreenName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setScreenName(SHORT_SCREEN_NAME);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testLongScreenName() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setScreenName(GOOD_SCREEN_NAME_LONG);

        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());

        command.setScreenName(LONG_SCREEN_NAME);

        _validator.validate(command, errors);
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

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        
        // test non-alphanumeric
        errors = new BindException(command, "");

        command.setScreenName(BAD_SCREEN_NAME_NONALPHANUMERIC);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoGender() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setGender(null);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testBadGender() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setGender("q");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        errors = new BindException(command, "");

        command.setGender("some weird String");

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testNoNumChildren() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        command.setNumSchoolChildren(null);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        errors = new BindException(command, "");
        command.setNumSchoolChildren(new Integer(-1));

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }
}