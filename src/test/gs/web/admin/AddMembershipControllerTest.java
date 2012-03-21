package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 3/21/12
 * Time: 10:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddMembershipControllerTest extends BaseControllerTestCase{
    private AddMembershipController _addMembershipController;

    private IUserDao _userDao;
    private IEspMembershipDao _espMembershipDao;
    private ISchoolDao _schoolDao;

    private static final String ADD_MEMBERSHIP_VIEW = "admin/addMembership";
    
    private User _user, _anotherUser;
    private Role _espRole;
    private School _school, _anotherSchool;
    private EspMembership _espMembership;
    private AddMembershipCommand _command;
    private ModelMap _modelMap;
    private String _view;
    private BindingResult _bindingResult;
    
    public void setUp() throws Exception {
        super.setUp();
        
        _addMembershipController = new AddMembershipController();
        
        _userDao = createStrictMock(IUserDao.class);
        _espMembershipDao = createStrictMock(IEspMembershipDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        
        _addMembershipController.setUserDao(_userDao);
        _addMembershipController.setEspMembershipDao(_espMembershipDao);
        _addMembershipController.setSchoolDao(_schoolDao);
        
        _espRole = new Role();
        _espRole.setId(1);
        _espRole.setKey(Role.ESP_MEMBER);

        /* createUser(int id, String firstName, String lastName, boolean hasRole, boolean verified) */
        _user = createUser(456, "ABC", "XYZ", true, true);
        _anotherUser = createUser(457, "CBA", "ZYX", false, false);

        /* createEspMembership(int id, int schoolId, State state, User user, String jobTitle, boolean isActive) */
        _espMembership = createEspMembership(12345, 789, State.CA, _user, "President", true);

        /* createSchool(int id, State state, String schoolName, boolean isActive, LevelCode levelCode) */
        _school = createSchool(789, _espMembership.getState(), "ASDFG Elementary School", true, LevelCode.ELEMENTARY);
        _anotherSchool = createSchool(987, State.DC, "GFDSA School", false, LevelCode.PRESCHOOL);
        
        _command = new AddMembershipCommand();
        _modelMap = new ModelMap();
    }

    public void replayAll() {
        super.replayMocks(_userDao, _espMembershipDao, _schoolDao);
    }

    public void verifyAll() {
        super.verifyMocks(_userDao, _espMembershipDao, _schoolDao);
    }
    
    public void resetAll() {
        super.resetMocks(_userDao, _espMembershipDao, _schoolDao);
    }
    
    public void testAddMembershipDisplay() {
        resetAll();
        
        replayAll();
        _view = _addMembershipController.display(_command, _bindingResult, _modelMap, getRequest());
        verifyAll();

        assertEquals(ADD_MEMBERSHIP_VIEW, _view);
    }

    public void testInitialValidation() {
        /* invalid member ID and 2 invalid school Ids - blank space and "3df" */

        setSampleData("asd",  "789,  ,3df", "CA");
        resetAll();

        replayAll();
        _view = _addMembershipController.onSubmit(_command, _bindingResult, _modelMap,
                getRequest().getParameter("memberId"), getRequest().getParameter("state"),
                getRequest().getParameter("schoolId"));
        verifyAll();

        assertEquals(_modelMap.get("hasErrors"), true);
        assertEquals(_bindingResult.getErrorCount(), 3);
        assertEquals(ADD_MEMBERSHIP_VIEW, _view);

        /* invalid state abbreviation */

        setSampleData("646880", "789, 876832, 48478, 52365", "ahjld");
        resetAll();

        replayAll();
        _view = _addMembershipController.onSubmit(_command, _bindingResult, _modelMap,
                getRequest().getParameter("memberId"), getRequest().getParameter("state"),
                getRequest().getParameter("schoolId"));
        verifyAll();

        assertEquals(_modelMap.get("hasErrors"), true);
        assertEquals(_bindingResult.getErrorCount(), 1);
        assertEquals(ADD_MEMBERSHIP_VIEW, _view);

        /* non existing member */

        setSampleData("10000", "789, 876832, 48478, 52365", "CA");
        resetAll();

        expect(_userDao.findUserFromId(10000)).andThrow(new ObjectRetrievalFailureException("user does not exist", new Throwable()));

        replayAll();
        _view = _addMembershipController.onSubmit(_command, _bindingResult, _modelMap,
                getRequest().getParameter("memberId"), getRequest().getParameter("state"),
                getRequest().getParameter("schoolId"));
        verifyAll();

        assertEquals(_modelMap.get("hasErrors"), true);
        assertEquals(_bindingResult.getErrorCount(), 1);
        assertEquals(ADD_MEMBERSHIP_VIEW, _view);

        /* member not email verified, doesn't have esp role, no active memberships for the member */

        setSampleData("457", "789, 876832, 48478, 52365", "CA");
        resetAll();

        expect(_userDao.findUserFromId(_anotherUser.getId())).andReturn(_anotherUser);
        expect(_espMembershipDao.findEspMembershipsByUserId(_anotherUser.getId(), true)).andReturn(new ArrayList<EspMembership>());
        expect(_espMembershipDao.findEspMembershipsByUserId(_anotherUser.getId(), false)).andReturn(new ArrayList<EspMembership>());
        
        replayAll();
        _view = _addMembershipController.onSubmit(_command, _bindingResult, _modelMap,
                getRequest().getParameter("memberId"), getRequest().getParameter("state"),
                getRequest().getParameter("schoolId"));
        verifyAll();
        
        assertEquals(_modelMap.get("hasErrors"), true);
        assertEquals(_bindingResult.getErrorCount(), 3);
        assertEquals(_modelMap.get("user"), _anotherUser);
        assertEquals(ADD_MEMBERSHIP_VIEW, _view);
    }

    public void testValidateSchoolState() {
        /* school 788 does not exist in CA, school 987 is preschool only and inactive, member 456 already has membership
         with school 789
          */

        List<EspMembership> allMemberships = new ArrayList<EspMembership>();
        allMemberships.add(createEspMembership(12345, 788, State.DC, _user, "Principal", false));
        allMemberships.add(_espMembership);
        allMemberships.add(createEspMembership(12345, 987, State.CA, _user, "Consultant", false));

        List<EspMembership> activeMemberships = new ArrayList<EspMembership>();
        activeMemberships.add(_espMembership);

        setSampleData("456", "788, 789, 987", "CA");

        resetAll();

        expect(_userDao.findUserFromId(_user.getId())).andReturn(_user);
        expect(_espMembershipDao.findEspMembershipsByUserId(_user.getId(), true)).andReturn(activeMemberships);
        expect(_espMembershipDao.findEspMembershipsByUserId(_user.getId(), false)).andReturn(allMemberships);
        expect(_schoolDao.getSchoolById(State.CA, 788)).andThrow(new ObjectRetrievalFailureException("School not found in state", new Throwable()));
        expect(_schoolDao.getSchoolById(State.CA, _school.getId())).andReturn(_school);
        expect(_schoolDao.getSchoolById(State.CA, _anotherSchool.getId())).andReturn(_anotherSchool);

        replayAll();
        _view = _addMembershipController.onSubmit(_command, _bindingResult, _modelMap,
                getRequest().getParameter("memberId"), getRequest().getParameter("state"),
                getRequest().getParameter("schoolId"));
        verifyAll();

        assertEquals(_modelMap.get("hasErrors"), true);
        assertEquals(_bindingResult.getErrorCount(), 4);
        assertNotNull(_modelMap.get("schools"));
        assertEquals(_modelMap.get("firstEspMembership"), _espMembership);
        assertEquals(ADD_MEMBERSHIP_VIEW, _view);
    }
    
    public void testOnSubmitSuccess() {
        List<EspMembership> allMemberships = new ArrayList<EspMembership>();
        allMemberships.add(_espMembership);

        List<EspMembership> activeMemberships = new ArrayList<EspMembership>();
        activeMemberships.add(_espMembership);
        
        School validSchool1 = createSchool(790, _espMembership.getState(), "QWERTY School", true, LevelCode.ALL_LEVELS);
        School validSchool2 = createSchool(791, _espMembership.getState(), "POIUY School", true, LevelCode.ELEMENTARY_MIDDLE_HIGH);

        setSampleData("456", "790, 791", "CA");

        resetAll();
        expect(_userDao.findUserFromId(_user.getId())).andReturn(_user);
        expect(_espMembershipDao.findEspMembershipsByUserId(_user.getId(), true)).andReturn(activeMemberships);
        expect(_espMembershipDao.findEspMembershipsByUserId(_user.getId(), false)).andReturn(allMemberships);
        expect(_schoolDao.getSchoolById(State.CA, validSchool1.getId())).andReturn(validSchool1);
        expect(_schoolDao.getSchoolById(State.CA, validSchool2.getId())).andReturn(validSchool2);
        _espMembershipDao.saveEspMembership((EspMembership) anyObject());
        expectLastCall().times(2);

        replayAll();
        _view = _addMembershipController.onSubmit(_command, _bindingResult, _modelMap,
                getRequest().getParameter("memberId"), getRequest().getParameter("state"),
                getRequest().getParameter("schoolId"));
        verifyAll();

        assertEquals(_modelMap.get("hasErrors"), null);
        assertEquals(_bindingResult.getErrorCount(), 0);
        assertEquals(_modelMap.get("user"), _user);
        assertEquals(_modelMap.get("espMemberships"), allMemberships);
        assertEquals(_modelMap.get("firstEspMembership"), _espMembership);
        assertEquals(_modelMap.get("onSubmitSuccess"), true);
        assertEquals(ADD_MEMBERSHIP_VIEW, _view);
    }
    
    private User createUser(int id, String firstName, String lastName, boolean hasRole, boolean verified) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if(hasRole) {
            user.addRole(_espRole);
        }
        user.setEmailVerified(verified);
        return user;
    }
    
    private EspMembership createEspMembership(int id, int schoolId, State state, User user, String jobTitle, boolean isActive) {
        EspMembership espMembership = new EspMembership();
        espMembership.setId(id);
        espMembership.setSchoolId(schoolId);
        espMembership.setState(state);
        espMembership.setUser(user);
        espMembership.setJobTitle(jobTitle);
        espMembership.setActive(isActive);
        return espMembership;
    }
    
    private School createSchool(int id, State state, String schoolName, boolean isActive, LevelCode levelCode) {
        School school = new School();
        school.setId(id);
        school.setStateAbbreviation(state);
        school.setName(schoolName);
        school.setActive(isActive);
        school.setLevelCode(levelCode);
        return school;
    }

    private void setSampleData(String memberId, String schoolIds, String state) {
        getRequest().setParameter("memberId", memberId);
        getRequest().setParameter("schoolId", schoolIds);
        getRequest().setParameter("state", state);
        _bindingResult = new BeanPropertyBindingResult(_command, "addMembershipCommand");        
    }
}
