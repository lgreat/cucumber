package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.lang.StringUtils;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import gs.data.geo.IGeoDao;
import gs.data.community.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.school.ISchoolDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.beans.PropertyEditorSupport;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AccountInformationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/accountInformation.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

    /**
     * Populates command with user from database
     */
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AccountInformationCommand command = new AccountInformationCommand();

        // pull user from session / database
        User user = SessionContextUtil.getSessionContext(request).getUser();
        if (user != null && user.isEmailValidated()) {
            command.setMemberId(user.getId());
            command.setGender(user.getGender());
            command.setState(user.getUserProfile().getState());
            command.setCity(user.getUserProfile().getCity());

            List<Student> students = new ArrayList<Student>(user.getStudents());
            Collections.sort(students, new StudentComparator());
            for (Student student: students) {
                AccountInformationCommand.StudentCommand studentCommand = new AccountInformationCommand.StudentCommand();
                School school = _schoolDao.getSchoolById(student.getState(), student.getSchoolId());
                studentCommand.setCity(school.getCity());
                studentCommand.setGrade(student.getGrade());
                studentCommand.setSchoolId(student.getSchoolId());
                studentCommand.setState(student.getState());
                command.addStudentCommand(studentCommand);
            }

            List<Subscription> parentAmbassadorSubs = _subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.PARENT_CONTACT);
            if (parentAmbassadorSubs != null && parentAmbassadorSubs.size() > 0) {
                command.setParentAmbassador("yes");
            } else {
                command.setParentAmbassador("no");
            }
        }

        return command;
    }

    /**
     * Even if no bind is occurring, populate the various drop downs
     */
    protected void onBindOnNewForm(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        super.onBindOnNewForm(request, commandObj, errors);
        populateDropdowns((AccountInformationCommand) commandObj);
    }

    /**
     * After bind has occurred, populate the various drop downs
     */
    protected void onBind(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        super.onBind(request, commandObj, errors);
        populateDropdowns((AccountInformationCommand) commandObj);
    }

    protected void populateDropdowns(AccountInformationCommand command) {
        // load the city list for the user
        command.setProfileCityList(_geoDao.findCitiesByState(command.getState()));

        // load the city and school list for each child (state and grade dropdowns are static)
        for (AccountInformationCommand.StudentCommand student: command.getStudents()) {
            command.addCityList(_geoDao.findCitiesByState(student.getState()));
            if (student.getGrade() != null) {
                List<School> schools = _schoolDao.findSchoolsInCityByGrade(student.getState(), student.getCity(), student.getGrade());
                command.addSchools(schools);
            } else {
                command.addSchools(new ArrayList<School>());
            }
        }
    }

    /**
     * Do custom validation
     */
    protected void onBindAndValidate(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        super.onBindAndValidate(request, commandObj, errors);

        AccountInformationCommand command = (AccountInformationCommand) commandObj;

        int counter = 0;
        for (AccountInformationCommand.StudentCommand student: command.getStudents()) {
            Grade grade = student.getGrade();
            State state = student.getState();
            int schoolId = student.getSchoolId();
            if (schoolId > 0) {
                School school = _schoolDao.getSchoolById(state, schoolId);
                if (!school.getGradeLevels().contains(grade)) {
                    errors.rejectValue("students[" + counter + "].schoolId", null, "Selected school does not have that grade");
                }
            }
            counter++;
        }
    }

    /**
     * Redirects to the login page if no user exists in session
     */
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        ModelAndView mAndV = super.showForm(request, response, errors);

        AccountInformationCommand command = (AccountInformationCommand) mAndV.getModel().get(getCommandName());
        if (command == null || command.getGender() == null) {
            State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, state, BEAN_ID);
            mAndV.setViewName("redirect:" + urlBuilder.asSiteRelative(request));
        }
        return mAndV;
    }

//    protected void parseStudent(HttpServletRequest request, AccountInformationCommand command, int childNum) {
//        String sGrade = request.getParameter("grade" + childNum);
//        State state = _stateManager.getState(request.getParameter("state" + childNum));
//        String sSchoolId = request.getParameter("school" + childNum);
//        String city = request.getParameter("city" + childNum);
//
//        Student student = new Student();
//
//        if (!StringUtils.isEmpty(sGrade)) {
//            student.setGrade(Grade.getGradeLevel(sGrade));
//        }
//        if (!StringUtils.isEmpty(sSchoolId)) {
//            student.setSchoolId(new Integer(sSchoolId));
//        }
//        student.setState(state);
//        student.setOrder(childNum);
//
//        command.addStudent(student);
//        command.addCityName(city);
//        loadSchoolList(student, city, command);
//    }

    /**
     * Register a custom editor for Grade
     */
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Grade.class, new GradePropertyEditor());
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) {
        if (StringUtils.isEmpty(getSuccessView())) {
            String redirectUrl = "redirect:http://" +
                SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                "/dashboard";
            setSuccessView(redirectUrl);
        }

        AccountInformationCommand command = (AccountInformationCommand) commandObj;

        User user = _userDao.findUserFromId(command.getMemberId());

        user.setGender(command.getGender());
        user.getUserProfile().setState(command.getState());
        user.getUserProfile().setCity(command.getCity());

        if (user.getStudents() != null) {
            user.getStudents().clear();
        }
        if (command.getNumStudents() > 0) {
            int counter=1;
            for (AccountInformationCommand.StudentCommand studentCommand: command.getStudents()) {
                if (StringUtils.equals("yes", command.getParentAmbassador())) {
                    // TODO: add PA sub for child
                    //addContactSubscriptionFromStudent(student, user);
                }
                Student student = new Student();
                student.setGrade(studentCommand.getGrade());
                student.setState(studentCommand.getState());
                student.setUpdated(new Date());
                student.setOrder(counter++);
                if (studentCommand.getSchoolId() == -1) {
                    student.setSchoolId(null);
                } else {
                    student.setSchoolId(studentCommand.getSchoolId());
                }
                user.addStudent(student);
            }
        }

        // saves gender, state, city
        _userDao.saveUser(user);

        if (StringUtils.equalsIgnoreCase("yes", command.getParentAmbassador())) {
            _log.error("Parent Ambassador");
        }

        //return new ModelAndView(getSuccessView());
        return new ModelAndView("redirect:accountInformation.page");
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    /**
     * Uses Grade.getGradeLevel to convert from string to Grade.
     */
    protected class GradePropertyEditor extends PropertyEditorSupport {
        private Grade _grade;
        public void setAsText(String text) {
            _grade = Grade.getGradeLevel(text);
        }
        public String getAsText() {
            if (_grade != null) {
                return _grade.getName();
            }
            return "null";
        }
        public Object getValue() {
            return _grade;
        }
        public void setValue(Object value) {
            _grade = (Grade) value;
        }
    }

    protected class StudentComparator implements Comparator<Student> {
        public int compare(Student s1, Student s2) {
            return s1.getOrder().compareTo(s2.getOrder());
//            int gradeCompare = s1.getGrade().compareTo(s2.getGrade());
//            if (gradeCompare != 0) {
//                return gradeCompare;
//            }
//            return s1.getSchoolId().compareTo(s2.getSchoolId());
        }
    }
}
