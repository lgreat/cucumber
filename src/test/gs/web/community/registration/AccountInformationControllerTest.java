package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.state.StateManager;
import gs.data.state.State;

import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AccountInformationControllerTest extends BaseControllerTestCase {
    private AccountInformationController _controller;

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

    private User _user;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new AccountInformationController();

        _userDao = createStrictMock(IUserDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _stateManager = createStrictMock(StateManager.class);

        _controller.setUserDao(_userDao);
        _controller.setGeoDao(_geoDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setStateManager(_stateManager);

        _user = new User();
        _user.setId(30);
        _user.setEmail("aroy@greatschools.net");
        _user.setPlaintextPassword("foobar");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setState(State.CA);
        _user.getUserProfile().setCity("San Diego");
        _user.setGender("m");
        _user.setStudents(new HashSet<Student>());

        SessionContextUtil.getSessionContext(getRequest()).setUser(_user);
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_subscriptionDao, _controller.getSubscriptionDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_stateManager, _controller.getStateManager());
    }

    private AccountInformationCommand formBackingObject() throws Exception {
        return (AccountInformationCommand) _controller.formBackingObject(getRequest());
    }

    private void defaultReplay() {
        replay(_userDao);
        replay(_geoDao);
        replay(_subscriptionDao);
        replay(_schoolDao);
        replay(_stateManager);
    }

    private void defaultVerify() {
        verify(_userDao);
        verify(_geoDao);
        verify(_subscriptionDao);
        verify(_schoolDao);
        verify(_stateManager);
    }

    public void testFormBackingObjectNoUser() throws Exception {
        // no user returns a default command
        SessionContextUtil.getSessionContext(getRequest()).setUser(null);

        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertNull(command.getGender());
        assertNull(command.getState());
        assertNull(command.getCity());
    }

    public void testFormBackingObjectNotValidatedUser() throws Exception {
        // non-community user returns a default command
        _user.setEmailProvisional("foobar");

        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertNull(command.getGender());
        assertNull(command.getState());
        assertNull(command.getCity());

        _user.setEmailValidated();
    }

    public void testFormBackingObjectSimpleUser() throws Exception {
        // test basic information is passed through
        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(new ArrayList<Subscription>());

        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertEquals(30, command.getMemberId());
        assertEquals("m", command.getGender());
        assertEquals("San Diego", command.getCity());
        assertEquals(State.CA, command.getState());
        assertEquals(0, command.getStudents().size());
        assertEquals("no", command.getParentAmbassador());
    }

    public void testFormBackingObjectUserWithPA() throws Exception {
        // test PA flag is set when > 0 subs is returned
        List<Subscription> subs = new ArrayList<Subscription>();
        subs.add(new Subscription());

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(subs);

        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertEquals("yes", command.getParentAmbassador());
    }

    public void testFormBackingObjectUserWithChild() throws Exception {
        // test basic student info passed through to command
        Student student = new Student();
        student.setGrade(Grade.G_3);
        student.setOrder(1);
        student.setState(State.CA);
        student.setSchoolId(1);
        Set<Student> students = new HashSet<Student>();
        students.add(student);
        _user.setStudents(students);

        School school = new School();
        school.setCity("Alameda");

        expect(_schoolDao.getSchoolById(student.getState(), student.getSchoolId())).andReturn(school);

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(new ArrayList<Subscription>());
        
        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertEquals(1, command.getNumStudents());
        assertEquals(1, command.getStudents().size());
        AccountInformationCommand.StudentCommand studentCommand = command.getStudents().get(0);
        assertEquals(Grade.G_3, studentCommand.getGrade());
        assertEquals("Alameda", studentCommand.getCity());
        assertEquals(State.CA, studentCommand.getState());
        assertEquals(1, studentCommand.getSchoolId());
    }

    public void testFormBackingObjectUserWithChildNoSchoolId() throws Exception {
        // expect student to default to parent's state/city when no school is specified
        Student student = new Student();
        student.setGrade(Grade.G_3);
        student.setOrder(1);
        student.setState(State.AK);
        Set<Student> students = new HashSet<Student>();
        students.add(student);
        _user.setStudents(students);

        School school = new School();
        school.setCity("Alameda");

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(new ArrayList<Subscription>());

        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertEquals(1, command.getStudents().size());
        AccountInformationCommand.StudentCommand studentCommand = command.getStudents().get(0);
        assertEquals("San Diego", studentCommand.getCity());
        assertEquals(State.CA, studentCommand.getState());
    }

}
