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
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jun 20, 2006
 * Time: 11:51:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserCommandValidatorTest extends BaseTestCase {
    private static final String GOOD_EMAIL = "UserCommandValidatorTest@greatschools.net";
    private static final String GOOD_PASSWORD6 = "123456";
    private static final String GOOD_PASSWORD16 = "1234567890123456";
    private static final String SHORT_PASSWORD5 = "12345";
    private static final String LONG_PASSWORD17 = "12345678901234567";
    private static final State GOOD_STATE = State.CA;
    private static final String GOOD_CITY = "Oakland";
    private static final String BAD_CITY = "Juneau";

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
        command.setPassword(GOOD_PASSWORD6);
        command.setConfirmPassword(GOOD_PASSWORD6);
        command.setState(GOOD_STATE);
        command.setCity(GOOD_CITY);
        return command;
    }

    public void testSuccess() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());

        command.setPassword(GOOD_PASSWORD16);
        command.setConfirmPassword(GOOD_PASSWORD16);

        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testEmailExists() {
        User user = _userDao.findUserFromId(1);
        assertNotNull(user); // expect DB to have pre-existing user

        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setEmail(user.getEmail());
        command.setConfirmEmail(user.getEmail());
        command.setPassword(GOOD_PASSWORD6);
        command.setConfirmPassword(GOOD_PASSWORD6);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());

        // existing emails allowed if id is set
        command.setId("1");
        errors = new BindException(command, "");
        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testShortPassword() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(SHORT_PASSWORD5);
        command.setConfirmPassword(SHORT_PASSWORD5);

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

        command.setPassword(LONG_PASSWORD17);
        command.setConfirmPassword(LONG_PASSWORD17);

        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
    }

    public void testMismatchedPasswords() {
        UserCommand command = setupCommand();
        Errors errors = new BindException(command, "");

        command.setPassword(GOOD_PASSWORD6);
        command.setConfirmPassword(GOOD_PASSWORD16);

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

        // confirm email field not checked if id is set
        command.setId("1");
        errors = new BindException(command, "");
        _validator.validate(command, errors);
        assertFalse(errors.hasErrors());
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
}