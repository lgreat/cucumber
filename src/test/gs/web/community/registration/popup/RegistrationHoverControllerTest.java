package gs.web.community.registration.popup;

import gs.data.community.*;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.state.State;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.BaseControllerTestCase;
import org.apache.commons.lang.StringUtils;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverControllerTest extends BaseControllerTestCase {
    private RegistrationHoverController _controller;
    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ISubscriptionDao _subscriptionDao;
    private ITableDao _tableDao;
    private RegistrationHoverCommand _command;
    private BindException _errors;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new RegistrationHoverController();

        _userDao = createStrictMock(IUserDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _tableDao = createStrictMock(ITableDao.class);

        _controller.setUserDao(_userDao);
        _controller.setGeoDao(_geoDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setTableDao(_tableDao);

        _command = new RegistrationHoverCommand();
        _command.setEmail("RegistrationHoverControllerTest@greatschools.org");
        _command.getUser().setId(123); // to fake the database save
        _command.setPassword("foobar");
        _command.setConfirmPassword("foobar");
        _command.setNewsletter(false);
        _controller.setRequireEmailValidation(false);
        _errors = new BindException(_command, "");

        getRequest().setRemoteAddr("127.0.0.1");
        getRequest().setServerName("dev.greatschools.org");
    }

    public void replayMocks() {
        replay(_userDao);
        replay(_geoDao);
        replay(_subscriptionDao);
        replay(_tableDao);
    }

    public void verifyMocks() {
        verify(_userDao);
        verify(_geoDao);
        verify(_subscriptionDao);
        verify(_tableDao);
    }

    public void resetMocks() {
        reset(_userDao);
        reset(_geoDao);
        reset(_subscriptionDao);
        reset(_tableDao);
    }

    public void testOnBindOnNewForm() {
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(new ArrayList<City>());
        replayMocks();
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        verifyMocks();

        assertFalse(_command.isMslOnly());

        resetMocks();
        getRequest().setParameter("msl", "true");
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(new ArrayList<City>());
        replayMocks();
        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        verifyMocks();

        assertTrue(_command.isMslOnly());
    }

    public void testOnBindAndValidate() throws Exception {
        assertFalse(_errors.hasErrors());
        User user = new User();
        user.setId(123);
        user.setPlaintextPassword("foobar");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(user);
        replayMocks();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        verifyMocks();
        assertTrue("Expect validation to occur", _errors.hasErrors());
    }

    public void testOnSubmitNewUser() throws Exception {
        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(null);
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class)); // create new user
        _userDao.updateUser(isA(User.class)); // set password
        _userDao.updateUser(isA(User.class)); // update user profile
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect hover to be closed", StringUtils.contains(mAndV.getViewName(), "sendToDestination"));
    }

    public void testOnSubmitExistingUser() throws Exception {
        User user = new User();
        user.setId(123);
        user.setEmail("RegistrationHoverControllerTest@greatschools.org");

        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(null);
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(user);
        _userDao.updateUser(user); // set password
        _userDao.updateUser(user); // update user profile
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect hover to be closed", StringUtils.contains(mAndV.getViewName(), "sendToDestination"));
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

        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(null);
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(user);
        _userDao.updateUser(user); // set password
        _userDao.updateUser(user); // update user profile
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect hover to be closed", StringUtils.contains(mAndV.getViewName(), "sendToDestination"));
    }

    public void testOnSubmitNewUserWithSubscriptions() throws Exception {
        _command.setNewsletter(true);
        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(null);
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class)); // create new user
        _userDao.updateUser(isA(User.class)); // set password
        _userDao.updateUser(isA(User.class)); // update user profile
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect hover to be closed", StringUtils.contains(mAndV.getViewName(), "sendToDestination"));
    }

    public void testOnSubmitBlockedIP() throws Exception {
        _controller.setErrorView("error");
        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(createMock(ITableRow.class));
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect error view", StringUtils.equals(mAndV.getViewName(), "error"));
    }

    public void testOnSubmitErrorRollsBackUserToProvisional() throws Exception {
        _command.setNewsletter(true);
        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(null);
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class)); // create new user
        _userDao.updateUser(isA(User.class)); // set password
        _userDao.updateUser(isA(User.class)); // update user profile
        _userDao.updateUser(isA(User.class)); // rollback to provisional
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        expectLastCall().andThrow(new RuntimeException("Some hibernate exception that happened during subscription processing"));
        replayMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyMocks();
        assertNotNull(mAndV);
        assertTrue("Expect user to be rolled back to provisional status",
                _command.getUser().isEmailProvisional());        
        assertEquals("Expect error view", _controller.getErrorView(), mAndV.getViewName());
    }
}
