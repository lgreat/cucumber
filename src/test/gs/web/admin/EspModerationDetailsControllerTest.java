package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.*;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createStrictMock;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 3/7/12
 * Time: 1:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class EspModerationDetailsControllerTest extends BaseControllerTestCase {
    private EspModerationDetailsController _espModerationDetailsController;

    private IEspMembershipDao _espMembershipDao;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private IRoleDao _roleDao;
    private ExactTargetAPI _exactTargetAPI;

    private EspMembership _espMembership, _anotherEspMembership;
    private School _school, _anotherSchool;
    private User _user, _anotherUser;
    private Role _role;
    private BindingResult _bindingResult;
    private EspModerationDetailsCommand _command;
    private ModelMap _modelMap;
    private String _view;

    private static final String ESP_MODERATION_DETAILS_VIEW = "admin/espModerationDetails";
    public static final String ESP_MODERATION_VIEW = "redirect:/admin/espModerationForm.page";

    public void setUp() throws Exception {
        super.setUp();
        _espModerationDetailsController = new EspModerationDetailsController();

        _espMembershipDao = createStrictMock(IEspMembershipDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _roleDao = createStrictMock(IRoleDao.class);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);

        _espModerationDetailsController.setEspMembershipDao(_espMembershipDao);
        _espModerationDetailsController.setSchoolDao(_schoolDao);
        _espModerationDetailsController.setUserDao(_userDao);
        _espModerationDetailsController.setRoleDao(_roleDao);
        _espModerationDetailsController.setExactTargetAPI(_exactTargetAPI);
        
        _role = new Role();
        _role.setId(1);
        _role.setKey(Role.ESP_MEMBER);
        
        _user =  createUser(456, "ABC", "ZXC", "me@xyz.com", _role);
        _espMembership = createEspMembership(12345, 987, State.CA, _user, "President");
        _school = createSchool(987, _espMembership.getState(), "Qwerty Elementary School");

        _anotherUser =  createUser(10, "G", "", "notme@zxc.com", _role);
        _anotherEspMembership = createEspMembership(98765, 789, State.CA, _anotherUser, "Principal");
        _anotherSchool = createSchool(789, _anotherEspMembership.getState(), "ASDFGH Elementary School");

        _command = new EspModerationDetailsCommand();

        _bindingResult = new BeanPropertyBindingResult(_command, "espModerationDetailsCmd");

        _modelMap = new ModelMap();

        getRequest().setParameter("id", _espMembership.getId().toString());
    }

    public void replayAll() {
        super.replayMocks(_espMembershipDao, _schoolDao, _userDao, _exactTargetAPI);
    }

    public void verifyAll() {
        super.verifyMocks(_espMembershipDao, _schoolDao, _userDao, _exactTargetAPI);
    }

    public void resetAll() {
        super.resetMocks(_espMembershipDao, _schoolDao, _userDao, _exactTargetAPI);
    }

    public void testEspModDetailsShowFormInvalidMember() {
        resetAll();
        expect(_espMembershipDao.findEspMembershipById(54321, false)).andReturn(null);

        replayAll();
        _view = _espModerationDetailsController.showForm(_command, _bindingResult, _modelMap, getRequest(), "54321");
        verifyAll();

        assertEquals(ESP_MODERATION_VIEW, _view);
    }

    public void testEspModDetailsShowFormValidMember() {
        Integer id = Integer.parseInt(getRequest().getParameter("id"));

        resetAll();

        expect(_espMembershipDao.findEspMembershipById(id, false)).andReturn(_espMembership);
        expect(_schoolDao.getSchoolById(_espMembership.getState(), _espMembership.getSchoolId())).andReturn(_school);

        replayAll();
        _view = _espModerationDetailsController.showForm(_command, _bindingResult, _modelMap, getRequest(),
                getRequest().getParameter("id"));
        verifyAll();

        assertEquals(_command.getEspMembership(), _espMembership);
        assertEquals(_modelMap.get("espModerationDetailsCmd"), _command);
        assertEquals(ESP_MODERATION_DETAILS_VIEW, _view);
    }
    
    public void testEspModeratorActionApprove() {
        Integer id = Integer.parseInt(getRequest().getParameter("id"));
        _espMembership.setStatus(EspMembershipStatus.PROCESSING);
        _command.setEspMembership(_espMembership);
        List<EspMembership> espMemberships = new ArrayList<EspMembership>();
        espMemberships.add(_espMembership);

        resetAll();

        expect(_espMembershipDao.findEspMembershipById(id, false)).andReturn(_espMembership);
        expect(_schoolDao.getSchoolById(_espMembership.getState(), _espMembership.getSchoolId())).andReturn(_school);
        _espMembershipDao.updateEspMembership(_espMembership);
        expectLastCall();
        _exactTargetAPI.sendTriggeredEmail((String) anyObject(), (User) anyObject(), (Map<String, String>) anyObject());
        expectLastCall();
        expect(_espMembershipDao.findEspMembershipsByUserId(new Integer(_user.getId()), true)).andReturn(espMemberships);

        replayAll();
        _view = _espModerationDetailsController.onModeratorAction(_command, _bindingResult, _modelMap, getRequest(),
                getResponse(), "approve", getRequest().getParameter("id"));
        verifyAll();

        assertEquals(ESP_MODERATION_VIEW, _view);
    }

    public void testOnSaveWithErrors() {
        Integer id = Integer.parseInt(getRequest().getParameter("id"));
        getRequest().setParameter("stateName", "CA");
        _command.setEspMembership(_anotherEspMembership);
        _anotherSchool.setActive(false);

        resetAll();

        expect(_espMembershipDao.findEspMembershipById(id, false)).andReturn(_espMembership).times(2);
        expect(_userDao.findUserFromEmailIfExists(_anotherUser.getEmail())).andReturn(_anotherUser);
        expect(_schoolDao.getSchoolById(_anotherEspMembership.getState(), _anotherEspMembership.getSchoolId())).andReturn(_anotherSchool);
        expect(_schoolDao.getSchoolById(_espMembership.getState(), _espMembership.getSchoolId())).andReturn(_school);

        replayAll();
        _view = _espModerationDetailsController.onSave(_command, _bindingResult, _modelMap, getRequest(),
                getRequest().getParameter("id"));
        verifyAll();

        assertEquals(_bindingResult.hasErrors(), true);
        assertEquals(_modelMap.get("emailError"), true);
        assertEquals(_modelMap.get("firstNameError"), true);
        assertEquals(_modelMap.get("lastNameError"), true);
        assertEquals(_modelMap.get("schoolIdError"), true);
        assertNotSame(_user.getUpdated(), _espMembership.getUpdated());
        assertEquals(ESP_MODERATION_DETAILS_VIEW, _view);
    }

    public void testOnSaveWithoutErrors() {
        Integer id = Integer.parseInt(getRequest().getParameter("id"));
        getRequest().setParameter("stateName", "DC");
        _espMembership = createEspMembership(12345, 456, State.DC, _user, "Teacher");
        _school = createSchool(456, State.DC, "ZXCVB Elementary School");
        _command.setEspMembership(_espMembership);
        _school.setActive(true);
        _school.setLevelCode(LevelCode.ALL_LEVELS);

        resetAll();

        expect(_espMembershipDao.findEspMembershipById(id, false)).andReturn(_espMembership);
        expect(_schoolDao.getSchoolById(_espMembership.getState(), _espMembership.getSchoolId())).andReturn(_school).atLeastOnce();
        _userDao.updateUser(_user);
        expectLastCall();
        _espMembershipDao.updateEspMembership(_espMembership);
        expectLastCall();

        replayAll();
        _view = _espModerationDetailsController.onSave(_command, _bindingResult, _modelMap, getRequest(),
                getRequest().getParameter("id"));
        verifyAll();

        assertEquals(_bindingResult.hasErrors(), false);
        assertEquals(_modelMap.get("newSchool"), _espMembership.getSchool());
        assertEquals(_user.getUpdated(), _espMembership.getUpdated());
        assertEquals(ESP_MODERATION_DETAILS_VIEW, _view);
    }
    
    private EspMembership createEspMembership(int id, int schoolId, State state, User user, String jobTitle) {
        EspMembership espMembership = new EspMembership();
        espMembership.setId(id);
        espMembership.setSchoolId(schoolId);
        espMembership.setState(state);
        espMembership.setUser(user);
        espMembership.setJobTitle(jobTitle);
        return espMembership;
    }

    private User createUser(int id, String firstName, String lastName, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.addRole(role);
        return user;
    }
    
    private School createSchool(int id, State state, String name) {
        School school = new School();
        school.setId(id);
        school.setStateAbbreviation(state);
        school.setName(name);
        return school;
    }
}