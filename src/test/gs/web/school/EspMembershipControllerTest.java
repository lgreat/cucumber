package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.school.EspMembership;
import gs.data.school.IEspMembershipDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindingResult;

import static org.easymock.EasyMock.*;

public class EspMembershipControllerTest extends BaseControllerTestCase {

    private IEspMembershipDao _espMembershipDao;
    private IUserDao _userDao;

    EspMembershipController _controller;
    EspMembershipCommand _command;
    BindingResult _bindingResult;

    public void setUp() throws Exception {
        super.setUp();
        _espMembershipDao = createMock(IEspMembershipDao.class);
        _userDao = createMock(IUserDao.class);

        _controller = new EspMembershipController();
        _controller.setUserDao(_userDao);
        _controller.setEspMembershipDao(_espMembershipDao);

        _bindingResult = createMock(BindingResult.class);

    }

    public void replayAllMocks() {
        super.replayMocks(_userDao, _espMembershipDao);
    }

    public void verifyAllMocks() {
        super.verifyMocks(_userDao, _espMembershipDao);

    }

    public void testCreateEspMembershipNullEmail() {
        EspMembershipCommand command = new EspMembershipCommand();
        BindingResult bindingResult = createMock(BindingResult.class);
        String view = _controller.createEspMembership(command, bindingResult, getRequest(), getResponse());
        assertEquals("There is no email set.Therefore return the form view", EspMembershipController.FORM_VIEW, view);
    }

    public void testCreateEspMembership() {
        EspMembershipCommand command = new EspMembershipCommand();
        command.setEmail("someone@greatschools.org");

        User user = new User();
        user.setEmail(command.getEmail());

        expect(_userDao.findUserFromEmailIfExists(command.getEmail())).andReturn(user);
        _userDao.updateUser(user);
        _userDao.updateUser(user);
        _espMembershipDao.saveEspMembership(isA(EspMembership.class));

        replayAllMocks();
        String view = _controller.createEspMembership(command, _bindingResult, getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("Everything went well.Return the success view.", EspMembershipController.SUCCESS_VIEW, view);
    }

//    public void testCheckIfUserExists() {
//        EspMembershipCommand command = new EspMembershipCommand();
//        _controller.checkIfUserExists(getRequest(), getResponse(), command);
//    }

    public void testSetFieldsOnUserUsingCommand_NullCommand() {
        EspMembershipCommand command = new EspMembershipCommand();
        User user = new User();
        _controller.setFieldsOnUserUsingCommand(command, user);
        assertNull("First name is not set in the command.Therefore should be null.", user.getFirstName());
        assertNull("Last name is not set in the command.Therefore should be null.", user.getLastName());
        assertEquals("Default gender should be set.", "u", user.getGender());
        assertEquals("Default how for ESP should be set.", "esp", user.getHow());
    }

    public void testSetFieldsOnUserUsingCommand_PopulatedCommand() {
        EspMembershipCommand command = new EspMembershipCommand();
        command.setFirstName("fname");
        command.setLastName("lname");
        User user = new User();
        user.setGender("f");
        user.setHow("collegebound");
        _controller.setFieldsOnUserUsingCommand(command, user);
        assertEquals("First name is set in the command.", command.getFirstName(), user.getFirstName());
        assertEquals("Last name is set in the command.", command.getLastName(), user.getLastName());
        assertEquals("Gender is already set.Therefore do not overwrite", "f", user.getGender());
        assertEquals("How is already set.Therefore do not overwrite", "collegebound", user.getHow());
    }

    public void testSetUsersPassword_NullPassword() {
        EspMembershipCommand command = new EspMembershipCommand();
        User user = new User();
        user.setId(23);
        try {
            _controller.setUsersPassword(command, user);
        } catch (Exception e) {
            //TODO what to do?
        }
        assertTrue("No password was set in the command.Therefore should be empty.", user.isPasswordEmpty());
        assertFalse("No password was set in the command.Therefore no provisional password exists.", user.isEmailProvisional());

    }

    public void testSetUsersPassword_SpacePassword() {
        EspMembershipCommand command = new EspMembershipCommand();
        command.setPassword("        ");
        User user = new User();
        user.setId(23);
        try {
            _controller.setUsersPassword(command, user);
        } catch (Exception e) {
            //TODO what to do?
        }
        assertFalse("Spaces are a valid password.", user.isPasswordEmpty());
        assertTrue("Since a valid password was entered, a provisional password exists.", user.isEmailProvisional());

    }

    public void testSetUsersPassword_ValidPassword() {
        EspMembershipCommand command = new EspMembershipCommand();
        command.setPassword("somepassword");
        User user = new User();
        user.setId(23);
        try {
            _controller.setUsersPassword(command, user);
        } catch (Exception e) {
            //TODO what to do?
        }
        assertFalse("A valid password was entered.", user.isPasswordEmpty());
        assertTrue("Since a valid password was entered, a provisional password exists.", user.isEmailProvisional());
    }

    public void testUpdateUserProfile_NoUserProfile() {
        EspMembershipCommand command = new EspMembershipCommand();
        User user = new User();
        _controller.updateUserProfile(command, user);
        assertNotNull("New user profile should have been created.", user.getUserProfile());
        assertNotNull("The user should be set on the user profile.", user.getUserProfile().getUser());
        assertEquals("The user object passed into the method should be set in the user profile.", user, user.getUserProfile().getUser());
    }

    public void testUpdateUserProfile_ExistingUserProfile() {
        EspMembershipCommand command = new EspMembershipCommand();
        User user = new User();
        UserProfile userProfile = new UserProfile();
        userProfile.setId(23);
        user.setUserProfile(userProfile);
        _controller.updateUserProfile(command, user);
        assertNotNull("A user profile already exists.It should be just updated.", user.getUserProfile());
        assertNotNull("The user should be set on the user profile.", user.getUserProfile().getUser());

    }

    public void testSetUserProfileFieldsFromCommand_NullCommand() {
        EspMembershipCommand command = new EspMembershipCommand();
        UserProfile userProfile = new UserProfile();
        _controller.setUserProfileFieldsFromCommand(command, userProfile);
        assertNull("User name is not set on the command.Therefore should be null", userProfile.getScreenName());
        assertNull("City is not set on the command.Therefore should be null", userProfile.getCity());
        assertNull("State is not set on the command.Therefore should be null", userProfile.getState());
    }

    public void testSetUserProfileFieldsFromCommand_PopulatedCommand() {
        EspMembershipCommand command = new EspMembershipCommand();
        command.setUserName("someusername");
        command.setState(State.CA);
        command.setCity("someCity");
        UserProfile userProfile = new UserProfile();
        _controller.setUserProfileFieldsFromCommand(command, userProfile);
        assertEquals("User name is set on the command.", command.getUserName(), userProfile.getScreenName());
        assertEquals("City is set on the command.", command.getCity(), userProfile.getCity());
        assertEquals("State is set on the command.", command.getState(), userProfile.getState());
    }

}