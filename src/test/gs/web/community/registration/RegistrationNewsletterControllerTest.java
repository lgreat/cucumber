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
            student.setSchoolId(new Integer(15));
            user.addStudent(student);
        }
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1, user.getStudents().size());
        School school15 = new School();
        school15.setId(new Integer(15));
        school15.setLevelCode(LevelCode.MIDDLE);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15, MockControl.ONE_OR_MORE);
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

    public void testPopulateCommandUserAlreadySubscribed() {
        User user = new User();
        user.setId(new Integer(99));

        _mockUserDao.findUserFromId(99);
        _userControl.setReturnValue(user);
        _userControl.replay();

        List subscriptions = new ArrayList();
        Subscription sub1 = new Subscription();
        sub1.setProduct(SubscriptionProduct.MYSTAT);
        sub1.setUser(user);
        sub1.setSchoolId(15);
        subscriptions.add(sub1);
        _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        _subscriptionControl.setReturnValue(subscriptions);
        _subscriptionControl.replay();

        Student student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setSchoolId(new Integer(15));
        user.addStudent(student);
        for (int x=0; x < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; x++) {
            student = new Student();
            student.setState(State.CA);
            student.setGrade(Grade.G_6);
            student.setSchoolId(new Integer(30));
            user.addStudent(student);
        }
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1, user.getStudents().size());
        School school15 = new School();
        school15.setId(new Integer(15));
        school15.setLevelCode(LevelCode.MIDDLE);
        School school30 = new School();
        school30.setId(new Integer(30));
        school30.setLevelCode(LevelCode.HIGH);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(30)),
                school30, SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER);
        _schoolControl.replay();

        NewsletterCommand command = _controller.populateCommand(99);
        _userControl.verify();
        _subscriptionControl.verify();
        _schoolControl.verify();
        assertNotNull(command);
        assertFalse(command.getAllMss());
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER+1, command.getNumStudents());
        // one school has been removed ... the one the user is already subscribed to
        assertEquals(SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER, command.getNumStudentSchools());
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
        school15.setLevelCode(LevelCode.MIDDLE);
        _schoolControl.expectAndReturn(_mockSchoolDao.getSchoolById(State.CA, new Integer(15)),
                school15);
        School school30 = new School();
        school30.setId(new Integer(30));
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

    public void testOnBindAndValidate() {
        NewsletterCommand command = new NewsletterCommand();
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

        BindException errors = new BindException(command, "");
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
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        List subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(9, subs.size());

        command.setAvailableMssSubs(4);
        getRequest().setParameter("allMss", "true");
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(11, subs.size());

        getRequest().setParameter("allMss", null);
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        subs = command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(9, subs.size());

        getRequest().setParameter("CA1", "true");
        getRequest().setParameter("CA2", "true");
        getRequest().setParameter("MD57", "true"); // bogus -- should be ignored
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        subs = command.getSubscriptions();
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
