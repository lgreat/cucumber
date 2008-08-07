package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.school.ISchoolDao;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.state.StateManager;
import gs.data.state.State;

import static org.easymock.classextension.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;

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
    private AccountInformationCommand _command;
    private BindException _errors;

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

        _command = new AccountInformationCommand();
        _errors = new BindException(_command, "");
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_subscriptionDao, _controller.getSubscriptionDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_stateManager, _controller.getStateManager());
    }

    /*******************
     * HELPFUL METHODS *
     ******************/

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

    private AccountInformationCommand.StudentCommand createStudentCommand
            (State state, String city, Grade grade, int schoolId) {
        AccountInformationCommand.StudentCommand student = new AccountInformationCommand.StudentCommand();
        student.setState(state);
        student.setCity(city);
        student.setGrade(grade);
        student.setSchoolId(schoolId);
        return student;
    }

    private Student createStudent(State state, Grade grade, Integer schoolId) {
        return createStudent(state, grade, schoolId, 0);
    }

    private Student createStudent(State state, Grade grade, Integer schoolId, int order) {
        Student student = new Student();
        student.setState(state);
        student.setGrade(grade);
        student.setSchoolId(schoolId);
        student.setOrder(order);
        return student;
    }

    /***********************
     * FORM BACKING OBJECT *
     ***********************/

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
        Set<Student> students = new HashSet<Student>();
        students.add(createStudent(State.CA, Grade.G_3, 1, 2));
        students.add(createStudent(State.CA, Grade.G_9, 2, 1));
        _user.setStudents(students);

        School school1 = new School();
        school1.setCity("Alameda");
        School school2 = new School();
        school2.setCity("Oakland");

        // because of the order values on the students, these calls are in reverse order
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(school2);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school1);

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(new ArrayList<Subscription>());
        
        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertEquals(2, command.getNumStudents());
        assertEquals(2, command.getStudents().size());
        AccountInformationCommand.StudentCommand studentCommand = command.getStudents().get(1);
        assertEquals(Grade.G_3, studentCommand.getGrade());
        assertEquals("Alameda", studentCommand.getCity());
        assertEquals(State.CA, studentCommand.getState());
        assertEquals(1, studentCommand.getSchoolId());
        studentCommand = command.getStudents().get(0);
        assertEquals(Grade.G_9, studentCommand.getGrade());
        assertEquals("Oakland", studentCommand.getCity());
        assertEquals(State.CA, studentCommand.getState());
        assertEquals(2, studentCommand.getSchoolId());
    }

    public void testFormBackingObjectUserWithChildNoSchoolId() throws Exception {
        // expect student to default to parent's state/city when no school is specified
        Set<Student> students = new HashSet<Student>();
        students.add(createStudent(State.AK, Grade.G_3, null));
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

    public void testFormBackingObjectUserWithChildAsFormSubmission() throws Exception {
        // test on form submission, database is not accessed for student info
        // instead, empty placeholders are put into the command (for later binding)
        // fake student who should be ignored
        Set<Student> students = new HashSet<Student>();
        students.add(createStudent(State.CA, Grade.G_3, 1));
        _user.setStudents(students);

        getRequest().setParameter("numStudents", "1");
        getRequest().setMethod("POST");
        defaultReplay();
        AccountInformationCommand command = formBackingObject();
        defaultVerify();

        assertEquals(1, command.getNumStudents());
        assertEquals(1, command.getStudents().size());
        AccountInformationCommand.StudentCommand studentCommand = command.getStudents().get(0);
        assertNull(null, studentCommand.getGrade());
        assertNull(studentCommand.getCity());
        assertNull(studentCommand.getState());
    }

    /***********************
     * POPULATE DROP DOWNS *
     ***********************/

    public void testPopulateDropdownsWithoutStudent() {
        // verify that profile city list is populated from database
        List<City> cities = new ArrayList<City>();
        _command.setState(State.CA);

        expect(_geoDao.findCitiesByState(_command.getState())).andReturn(cities);

        defaultReplay();
        _controller.populateDropdowns(_command);
        defaultVerify();

        assertSame(cities, _command.getProfileCityList());
    }

    public void testPopulateDropdownsWithStudent() {
        // verify that profile city list is populated from database
        // verify that student city list and school list are also populated
        List<City> cities = new ArrayList<City>();

        _command.setState(State.CA);

        _command.addStudentCommand(createStudentCommand(State.CA, "Alameda", Grade.G_9, 1));

        List<City> studentCities = new ArrayList<City>();
        List<School> studentSchools = new ArrayList<School>();

        expect(_geoDao.findCitiesByState(_command.getState())).andReturn(cities);
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(studentCities);
        expect(_schoolDao.findSchoolsInCityByGrade(State.CA, "Alameda", Grade.G_9)).
                andReturn(studentSchools);

        defaultReplay();
        _controller.populateDropdowns(_command);
        defaultVerify();

        assertSame(cities, _command.getProfileCityList());
        assertEquals("Expect one school list for the one student", 1, _command.getSchools().size());
        assertSame("Expect actual school list from db", studentSchools, _command.getSchools().get(0));
        assertEquals("Expect one city list for the one student", 1, _command.getCityList().size());
        assertSame("Expect actual city list from db", studentCities, _command.getCityList().get(0));
    }

    /***************************
     * ON BINDS AND VALIDATION *
     ***************************/

    public void testReferenceData() throws Exception {
        // verify that the dropdowns are populated when the form first loads
        List<City> cities = new ArrayList<City>();
        _command.setState(State.CA);

        expect(_geoDao.findCitiesByState(_command.getState())).andReturn(cities);

        defaultReplay();
        _controller.referenceData(getRequest(), _command, _errors);
        defaultVerify();

        assertSame(cities, _command.getProfileCityList());
    }

    public void testOnBindAndValidateRejectsEmptyCity() throws Exception {
        _command.setCity("");

        defaultReplay();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        defaultVerify();
        assertTrue("Expect an error on blank city", _errors.hasFieldErrors("city"));
    }

    public void testOnBindAndValidateRejectsEmptyStudentCity() throws Exception {
        _command.setCity("Alameda");

        _command.addStudentCommand(createStudentCommand(State.CA, "", Grade.G_9, 1));

        defaultReplay();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        defaultVerify();
        assertTrue("Expect an error on blank student city", _errors.hasFieldErrors("students[0]"));
    }

    public void testOnBindAndValidateRejectsEmptyStudentGrade() throws Exception {
        _command.setCity("Alameda");

        _command.addStudentCommand(createStudentCommand(State.CA, "Alameda", null, 1));

        defaultReplay();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        defaultVerify();
        assertTrue("Expect an error on blank student grade", _errors.hasFieldErrors("students[0]"));
    }

    public void testOnBindAndValidateRejectsEmptyStudentSchool() throws Exception {
        _command.setCity("Alameda");

        // note -2 is the code for "hasn't picked a school"
        // -1 is a valid value that is the code for "My school isn't listed"
        _command.addStudentCommand(createStudentCommand(State.CA, "Alameda", Grade.G_9, -2));

        defaultReplay();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        defaultVerify();
        assertTrue("Expect an error on blank student school id", _errors.hasFieldErrors("students[0]"));
    }

    public void testOnBindAndValidateSkipsValidationOnFormChange() throws Exception {
        // verify this doesn't perform validation when isFormChangeRequest returns true
        // create two potential validation errors
        _command.setCity("");
        _command.addStudentCommand(createStudentCommand(State.CA, "Alameda", Grade.G_9, -2));

        // this sets isFormRequest to true (see that test), which supresses validation
        getRequest().setParameter("addChild", "submit");

        defaultReplay();
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        defaultVerify();
        
        assertFalse("Expect no errors because of form change request", _errors.hasErrors());
        assertFalse(_errors.hasFieldErrors("city"));
        assertFalse(_errors.hasFieldErrors("student[0]"));
    }

    /************************
     * FORM CHANGE REQUESTS *
     ************************/

    public void testIsFormChangeRequest() {
        // verify this detects the correct parameters
        defaultReplay();
        assertFalse("Default requests are not form change requests", _controller.isFormChangeRequest(getRequest()));
        getRequest().setParameter("addChild", "submit");
        assertTrue("Requests submitted through addChild are form change requests", 
                _controller.isFormChangeRequest(getRequest()));
        getRequest().removeParameter("addChild");
        assertFalse("Default requests are not form change requests", _controller.isFormChangeRequest(getRequest()));
        getRequest().setParameter("removeChild", "1");
        assertTrue("Requests submitted through removeChild are form change requests",
                _controller.isFormChangeRequest(getRequest()));
        defaultVerify();
    }

    public void testOnFormChangeAddChild() throws Exception {
        // verify this adds a child into the command, defaulting to parent's location
        getRequest().addParameter("addChild", "submit");
        _command.setState(State.CA);
        _command.setCity("San Diego");

        assertEquals("Expect no students by default", 0, _command.getNumStudents());
        defaultReplay();
        _controller.onFormChange(getRequest(), getResponse(), _command, _errors);
        defaultVerify();
        assertEquals("Expect one student to have been added", 1, _command.getNumStudents());
        assertEquals("Expect student to inherit parent's state", State.CA, _command.getStudents().get(0).getState());
        assertEquals("Expect student to inherit parent's city", "San Diego", _command.getStudents().get(0).getCity());
    }

    public void testOnFormChangeRemoveChild() throws Exception {
        // verify this removes a child from the command
        getRequest().addParameter("removeChild", "1"); // 1-based from page

        _command.addStudentCommand(createStudentCommand(State.CA, "San Diego", Grade.G_9, 1));

        assertEquals("Expect one student", 1, _command.getNumStudents());
        defaultReplay();
        _controller.onFormChange(getRequest(), getResponse(), _command, _errors);
        defaultVerify();
        assertEquals("Expect one student to have been removed", 0, _command.getNumStudents());
    }

    /*****************
     * MISCELLANEOUS *
     *****************/

    public void testShowForm() throws Exception {
        // verify this redirects to login when no command is in the request
        List<City> cities = new ArrayList<City>();
        _command.setState(State.CA);

        expect(_geoDao.findCitiesByState(_command.getState())).andReturn(cities);

        defaultReplay();
        ModelAndView mAndV = _controller.showForm(getRequest(), getResponse(), _errors);
        defaultVerify();

        assertTrue("Expect the view to be redirected", mAndV.getViewName().startsWith("redirect:"));
        assertTrue("Expect the view to be the login page", mAndV.getViewName().contains("loginOrRegister.page"));
    }

    public void testInitBinder() throws Exception {
        // verify this registers the custom grade binder
        ServletRequestDataBinder binder = createStrictMock(ServletRequestDataBinder.class);

        binder.registerCustomEditor(isA(Class.class), isA(AccountInformationController.GradePropertyEditor.class));
        replay(binder);
        defaultReplay();
        _controller.initBinder(getRequest(), binder);
        defaultVerify();
        verify(binder);
    }

    public void testGradePropertyEditor() {
        // verify this handles edge cases
        AccountInformationController.GradePropertyEditor editor = _controller.getGradePropertyEditor();

        editor.setValue(null);
        editor.setAsText(null);
        assertNull(editor.getValue());
        assertEquals("null", editor.getAsText());
        editor.setAsText("--");
        assertNull(editor.getValue());
        assertEquals("null", editor.getAsText());

        editor.setAsText("5");
        assertEquals(Grade.G_5, editor.getValue());
        assertEquals("5", editor.getAsText());
    }

    /*************
     * ON SUBMIT *
     *************/

    public void testDeleteSubscriptionsForProduct() {
        // verify this removes all the subscriptions
        List<Subscription> subs = new ArrayList<Subscription>();
        Subscription sub1 = new Subscription();
        sub1.setId(1);
        Subscription sub2 = new Subscription();
        sub2.setId(2);
        subs.add(sub1);
        subs.add(sub2);

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).andReturn(subs);
        _subscriptionDao.removeSubscription(1);
        _subscriptionDao.removeSubscription(2);
        defaultReplay();
        _controller.deleteSubscriptionsForProduct(_user, SubscriptionProduct.PARENT_CONTACT);
        defaultVerify();
    }

    public void testDeleteSubscriptionsForProductNoSubs() {
        // verify this does nothing when there are no subscriptions
        List<Subscription> subs = new ArrayList<Subscription>();

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).andReturn(subs);
        defaultReplay();
        _controller.deleteSubscriptionsForProduct(_user, SubscriptionProduct.PARENT_CONTACT);
        defaultVerify();
    }

    public void testAddParentAmbassadorSubscriptions() {
        // verify this adds subscriptions without duplicates
        List<AccountInformationCommand.StudentCommand> students = new ArrayList<AccountInformationCommand.StudentCommand>();
        // two students at same school
        students.add(createStudentCommand(State.CA, "Alameda", Grade.G_9, 1));
        students.add(createStudentCommand(State.CA, "Alameda", Grade.G_10, 1));
        // this is the subscription that should be created
        Subscription expectedSub1 = new Subscription();
        expectedSub1.setUser(_user);
        expectedSub1.setProduct(SubscriptionProduct.PARENT_CONTACT);
        expectedSub1.setState(State.CA);
        expectedSub1.setSchoolId(1);
        // one student at different school
        students.add(createStudentCommand(State.CA, "Alameda", Grade.G_3, 2));
        // this is the subscription that should be created
        Subscription expectedSub2 = new Subscription();
        expectedSub2.setUser(_user);
        expectedSub2.setProduct(SubscriptionProduct.PARENT_CONTACT);
        expectedSub2.setState(State.CA);
        expectedSub2.setSchoolId(2);

        // only two get saved
        // to be really thorough I should write a
        _subscriptionDao.saveSubscription(expectedSub1);
        _subscriptionDao.saveSubscription(expectedSub2);

        defaultReplay();
        _controller.addParentAmbassadorSubscriptions(students, _user);
        defaultVerify();
    }

    public void testOnSubmitNoChildren() {
        // test non-child info passed through
        _user.getStudents().add(createStudent(State.CA, Grade.G_9, 1));

        _command.setMemberId(15);
        _command.setState(State.AK);
        _command.setCity("Anchorage");
        _command.setGender("f");

        expect(_userDao.findUserFromId(15)).andReturn(_user);
        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(new ArrayList<Subscription>());
        _userDao.saveUser(_user);

        defaultReplay();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        defaultVerify();
        assertEquals(State.AK, _user.getUserProfile().getState());
        assertEquals("Anchorage", _user.getUserProfile().getCity());
        assertEquals("f", _user.getGender());
        assertEquals(0, _user.getStudents().size());
    }

    public void testOnSubmit() {
        // test all info passed through and PA subs created
        _command.setMemberId(15);
        _command.setState(State.AK);
        _command.setCity("Anchorage");
        _command.setGender("f");
        _command.setParentAmbassador("yes");

        _command.addStudentCommand(createStudentCommand(State.CA, "Alameda", Grade.G_9, 1));
        _command.addStudentCommand(createStudentCommand(State.CA, "Alameda", Grade.G_6, -1));

        // this is the subscription that should be created
        Subscription expectedSub1 = new Subscription();
        expectedSub1.setUser(_user);
        expectedSub1.setProduct(SubscriptionProduct.PARENT_CONTACT);
        expectedSub1.setState(State.CA);
        expectedSub1.setSchoolId(1);

        expect(_userDao.findUserFromId(15)).andReturn(_user);
        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).
                andReturn(new ArrayList<Subscription>());
        _subscriptionDao.saveSubscription(expectedSub1);
        _userDao.saveUser(_user);

        defaultReplay();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        defaultVerify();

        assertEquals(State.AK, _user.getUserProfile().getState());
        assertEquals("Anchorage", _user.getUserProfile().getCity());
        assertEquals("f", _user.getGender());
        assertEquals(2, _user.getStudents().size());
    }
}
