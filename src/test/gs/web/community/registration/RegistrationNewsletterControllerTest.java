package gs.web.community.registration;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.Grade;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.easymock.MockControl;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationNewsletterControllerTest extends BaseControllerTestCase {
    private RegistrationNewsletterController _controller;

    private IUserDao _mockUserDao;
    private MockControl _userControl;
    private ISubscriptionDao _mockSubscriptionDao;
    private MockControl _subscriptionControl;
    private ISchoolDao _mockSchoolDao;
    private MockControl _schoolControl;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationNewsletterController) appContext.getBean(RegistrationNewsletterController.BEAN_ID);

        _subscriptionControl = MockControl.createControl(ISubscriptionDao.class);
        _mockSubscriptionDao = (ISubscriptionDao)_subscriptionControl.getMock();
        _controller.setSubscriptionDao(_mockSubscriptionDao);

        _userControl = MockControl.createControl(IUserDao.class);
        _mockUserDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_mockUserDao);

        _schoolControl = MockControl.createControl(ISchoolDao.class);
        _mockSchoolDao = (ISchoolDao)_schoolControl.getMock();
        _controller.setSchoolDao(_mockSchoolDao);
    }

    public void testFormBackingObject() throws Exception {
        NewsletterCommand command = (NewsletterCommand) _controller.formBackingObject(getRequest());
        assertNull(command.getUser().getId());

        getRequest().setParameter("id", "99");

        User user = new User();
        user.setId(new Integer(99));
        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(null);
        _subscriptionControl.replay();

        command = (NewsletterCommand) _controller.formBackingObject(getRequest());
        _userControl.verify();
        _subscriptionControl.verify();
        assertEquals(99, command.getUser().getId().intValue());
    }

    public void testPopulateCommandAllMss() {
        User user = new User();
        user.setId(new Integer(99));

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(null);
        _subscriptionControl.replay();

        Student student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setSchoolId(new Integer(15));
        user.addStudent(student);
        School school15 = new School();
        school15.setId(new Integer(15));
        school15.setDatabaseState(State.CA);
        school15.setLevelCode(LevelCode.MIDDLE);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15);
        _schoolControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        _schoolControl.verify();
        assertNotNull(command);
        assertTrue(command.getAllMss());
    }

    public void testPopulateCommandNotAllMss() {
        User user = new User();
        user.setId(new Integer(99));

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(null);
        _subscriptionControl.replay();

        for (int x=0; x < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1; x++) {
            Student student = new Student();
            student.setState(State.CA);
            student.setGrade(Grade.G_6);
            student.setSchoolId(new Integer(x+15));
            user.addStudent(student);

            School school = new School();
            school.setId(new Integer(x+15));
            school.setDatabaseState(State.CA);
            school.setLevelCode(LevelCode.MIDDLE);
            _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(x+15)),
                    school);
        }
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1, user.getStudents().size());
        _schoolControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        _schoolControl.verify();
        assertNotNull(command);
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1, command.getNumStudents());
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1, command.getNumStudentSchools());
        assertFalse(command.getAllMss());
    }

    public void testPopulateCommandDuplicateSchools() {
        User user = new User();
        user.setId(new Integer(99));

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        List subscriptions = new ArrayList();
        Subscription sub1;
        // leave only a single subscription spot open
        for (int x=1; x < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; x++) {
            sub1 = new Subscription();
            sub1.setProduct(SubscriptionProduct.MYSTAT);
            sub1.setUser(user);
            sub1.setSchoolId(30);
            subscriptions.add(sub1);
        }
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(subscriptions);
        _subscriptionControl.replay();

        Student student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setSchoolId(new Integer(15));
        user.addStudent(student);

        student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_8);
        student.setSchoolId(new Integer(15));
        user.addStudent(student);

        student = new Student();
        student.setState(State.MD);
        student.setGrade(Grade.G_10);
        student.setSchoolId(new Integer(15));
        user.addStudent(student);

        School school15 = new School();
        school15.setId(new Integer(15));
        school15.setDatabaseState(State.CA);
        school15.setLevelCode(LevelCode.MIDDLE);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15, MockControl.ONE_OR_MORE);

        School schoolMD = new School();
        schoolMD.setId(new Integer(15));
        schoolMD.setDatabaseState(State.MD);
        schoolMD.setLevelCode(LevelCode.HIGH);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.MD, new Integer(15)),
                schoolMD, MockControl.ONE_OR_MORE);
        _schoolControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        _schoolControl.verify();
        assertNotNull(command);
        assertEquals(3, command.getNumStudents());
        // one of the duplicate schools was dropped out
        assertEquals(2, command.getNumStudentSchools());
    }

    public void testPopulateCommandUserAlreadySubscribed() {
        User user = new User();
        user.setId(new Integer(99));

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        List subscriptions = new ArrayList();
        Subscription sub1;
        // leave only a single subscription spot open
        for (int x=1; x < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; x++) {
            sub1 = new Subscription();
            sub1.setProduct(SubscriptionProduct.MYSTAT);
            sub1.setUser(user);
            sub1.setSchoolId(15);
            subscriptions.add(sub1);
        }
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(subscriptions);
        _subscriptionControl.replay();

        Student student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setSchoolId(new Integer(15));
        user.addStudent(student);

        student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setSchoolId(new Integer(30));
        user.addStudent(student);

        School school15 = new School();
        school15.setId(new Integer(15));
        school15.setDatabaseState(State.CA);
        school15.setLevelCode(LevelCode.MIDDLE);
        School school30 = new School();
        school30.setId(new Integer(30));
        school30.setDatabaseState(State.CA);
        school30.setLevelCode(LevelCode.HIGH);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(30)),
                school30);
        _schoolControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        _schoolControl.verify();
        assertNotNull(command);
        assertEquals(2, command.getNumStudents());
        // one school has been removed ... the one the user is already subscribed to
        assertEquals(1, command.getNumStudentSchools());
        assertEquals(30, ((School)command.getStudentSchools().get(0)).getId().intValue());
    }

    public void testAvailableSubs() {
        User user = new User();
        user.setId(new Integer(99));

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        List subs = new ArrayList();
        subs.add(new Subscription());
        subs.add(new Subscription());
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(subs);
        _subscriptionControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        assertEquals((SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER - 2), command.getAvailableMssSubs());
    }

    public void testStudentSchools() {
        User user = new User();
        user.setId(new Integer(99));

        Student student1 = new Student();
        student1.setState(State.CA);
        student1.setGrade(Grade.KINDERGARTEN);
        user.addStudent(student1);
        Student student2 = new Student();
        student2.setState(State.CA);
        student2.setGrade(Grade.G_6);
        student2.setSchoolId(new Integer(15));
        user.addStudent(student2);
        Student student3 = new Student();
        student3.setState(State.CA);
        student3.setGrade(Grade.G_10);
        student3.setSchoolId(new Integer(30));
        user.addStudent(student3);

        _userControl.expectAndReturn(_mockUserDao.findUserFromId(99), user);
        _userControl.replay();

        _subscriptionControl.expectAndReturn(_mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT),
                null);
        _subscriptionControl.replay();

        School school15 = new School();
        school15.setId(new Integer(15));
        school15.setDatabaseState(State.CA);
        school15.setLevelCode(LevelCode.MIDDLE);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15);
        School school30 = new School();
        school30.setId(new Integer(30));
        school30.setDatabaseState(State.CA);
        school30.setLevelCode(LevelCode.HIGH);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(30)),
                school30);
        _schoolControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        _schoolControl.verify();
        List schools = command.getStudentSchools();
        assertEquals(2, command.getNumStudentSchools());
        assertEquals(2, schools.size());
        assertTrue(schools.contains(school15));
        assertTrue(schools.contains(school30));
        assertEquals(3, command.getNumStudents());
        assertTrue(command.getAllMss());
        assertTrue(command.getHasK());
        assertTrue(command.getHasMiddle());
        assertTrue(command.getHasHigh());
    }

    private void setupBindAndValidate(NewsletterCommand command) {
        User user = new User();
        user.setId(new Integer(99));
        UserProfile profile = new UserProfile();
        profile.setState(State.CA);
        user.setUserProfile(profile);
        command.setUser(user);

        List studentSchools = new ArrayList();
        School school1 = new School();
        school1.setId(new Integer(1));
        school1.setDatabaseState(State.CA);
        School school2 = new School();
        school2.setId(new Integer(2));
        school2.setDatabaseState(State.CA);
        studentSchools.add(school1);
        studentSchools.add(school2);
        command.setStudentSchools(studentSchools);
        command.setAvailableMssSubs(0);

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user, MockControl.ONE_OR_MORE);
        _userControl.replay();

        getRequest().setParameter("gradeK", "true");
        getRequest().setParameter("grade1", "true");
        getRequest().setParameter("grade2", "true");
        getRequest().setParameter("grade3", "true");
        getRequest().setParameter("grade4", "true");
        getRequest().setParameter("grade5", "true");
        getRequest().setParameter("gradeMiddle", "true");
        getRequest().setParameter("gradeHigh", "true");
        getRequest().setParameter("advisor", "true");

        assertNull(command.getSubscriptions());
    }

    public void testValidateFail() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        setupBindAndValidate(command);

        command.setAvailableMssSubs(0);
        getRequest().setParameter("allMss", "true");
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
        List subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(9, subs.size());
    }

    public void testOnBindAndValidateNoSchools() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        setupBindAndValidate(command);

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        List subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(9, subs.size());
    }

    public void testOnBindAndValidateAllSchools() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        setupBindAndValidate(command);

        command.setAvailableMssSubs(4);
        getRequest().setParameter("allMss", "true");
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        List subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(11, subs.size());
    }

    public void testOnBindAndValidateSpecificSchools() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        setupBindAndValidate(command);

        getRequest().setParameter("CA1", "true");
        getRequest().setParameter("CA2", "true");
        getRequest().setParameter("MD57", "true"); // bogus -- should be ignored
        command.setAvailableMssSubs(4);
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        List subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(11, subs.size());
    }

    public void testOnSubmit() {
        NewsletterCommand command = new NewsletterCommand();

        User user = new User();
        user.setId(new Integer(99));
        command.setUser(user);
        List subs = new ArrayList();

        Subscription sub = new Subscription();
        sub.setSchoolId(1);
        sub.setProduct(SubscriptionProduct.MYSTAT);
        subs.add(sub);

        sub = new Subscription();
        sub.setSchoolId(2);
        sub.setProduct(SubscriptionProduct.MYSTAT);
        subs.add(sub);

        sub = new Subscription();
        sub.setProduct(SubscriptionProduct.MY_KINDERGARTNER);
        subs.add(sub);

        command.setSubscriptions(subs);

        _mockSubscriptionDao.addNewsletterSubscriptions(user, subs);
        _subscriptionControl.replay();

        ModelAndView mAndV = _controller.onSubmit(command);
        _subscriptionControl.verify();
        assertNotNull(mAndV);
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());
    }
}
