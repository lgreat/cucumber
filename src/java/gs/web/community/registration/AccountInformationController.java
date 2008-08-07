package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
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

        // when it's not a form submission, then load the student info from the database
        if (!isFormSubmission(request)) {
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
                    AccountInformationCommand.StudentCommand studentCommand =
                            new AccountInformationCommand.StudentCommand();
                    studentCommand.setState(student.getState());
                    studentCommand.setGrade(student.getGrade());
                    if (student.getSchoolId() != null) {
                        School school = _schoolDao.getSchoolById(student.getState(), student.getSchoolId());
                        studentCommand.setCity(school.getCity());
                        studentCommand.setSchoolId(student.getSchoolId());
                    } else {
                        // if no school with child, use city/state from user
                        studentCommand.setCity(command.getCity());
                        studentCommand.setState(command.getState());
                    }
                    command.addStudentCommand(studentCommand);
                }

                List<Subscription> parentAmbassadorSubs = _subscriptionDao.getUserSubscriptions
                        (user, SubscriptionProduct.PARENT_CONTACT);
                if (parentAmbassadorSubs != null && parentAmbassadorSubs.size() > 0) {
                    command.setParentAmbassador("yes");
                } else {
                    command.setParentAmbassador("no");
                }
            }
        } else {
            // when it is a form submission, just let the binding pull the student info from the request
            if (request.getParameter("numStudents") != null) {
                int numStudents = Integer.valueOf(request.getParameter("numStudents"));
                for (int x=0; x < numStudents; x++) {
                    command.addStudentCommand(new AccountInformationCommand.StudentCommand());
                }
            }
        }

        return command;
    }

    protected Map referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception {
        populateDropdowns((AccountInformationCommand) commandObj);
        return super.referenceData(request, commandObj, errors);
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

        if (!suppressValidation(request, commandObj, errors)) {
            if (StringUtils.isBlank(command.getCity())) {
                errors.rejectValue("city", null, "Please select your city.");
            }
            int counter = 0;
            for (AccountInformationCommand.StudentCommand student: command.getStudents()) {
                Grade grade = student.getGrade();
                int schoolId = student.getSchoolId();
                if (StringUtils.isBlank(student.getCity())) {
                    errors.rejectValue("students[" + counter + "]", null, "Please select your child's city.");
                } else if (grade == null || schoolId == -2) {
                    errors.rejectValue("students[" + counter + "]", null, "Please select your child's grade and school.");
                }
                counter++;
            }
        }
    }

    /**
     * Redirects to the login page if no user exists in session
     */
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        ModelAndView mAndV = super.showForm(request, response, errors);

        AccountInformationCommand command = (AccountInformationCommand) mAndV.getModel().get(getCommandName());
        if (command == null || command.getMemberId() < 1) {
            State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, state, BEAN_ID);
            mAndV.setViewName("redirect:" + urlBuilder.asSiteRelative(request));
        }
        return mAndV;
    }

    protected boolean isFormChangeRequest(HttpServletRequest request) {
        return request.getParameter("addChild") != null ||
                request.getParameter("removeChild") != null ||
                super.isFormChangeRequest(request);
    }

    protected void onFormChange(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        super.onFormChange(request, response, commandObj, errors);
        AccountInformationCommand command = (AccountInformationCommand) commandObj;
        if (request.getParameter("addChild") != null) {
            AccountInformationCommand.StudentCommand studentCommand = new AccountInformationCommand.StudentCommand();
            studentCommand.setState(command.getState());
            studentCommand.setCity(command.getCity());
            command.addStudentCommand(studentCommand);
        } else if (request.getParameter("removeChild") != null) {
            // page gives it to us 1-indexed
            int childNum = Integer.valueOf(request.getParameter("removeChild")) - 1;
            command.getStudents().remove(childNum);
        }
    }

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
                "/dashboard?msg=B5B3-2863-F4CD-6C77";
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

        deleteSubscriptionsForProduct(user, SubscriptionProduct.PARENT_CONTACT);
        if (command.getNumStudents() > 0) {
            if (StringUtils.equals("yes", command.getParentAmbassador())) {
                addParentAmbassadorSubscriptions(command.getStudents(), user);
            }
            int counter=1;
            for (AccountInformationCommand.StudentCommand studentCommand: command.getStudents()) {
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

        // saves gender, state, city, students
        _userDao.saveUser(user);

        return new ModelAndView(getSuccessView());
    }

    protected void addParentAmbassadorSubscriptions(List<AccountInformationCommand.StudentCommand> students, User user) {
        Set<String> uniqSubs = new HashSet<String>();
        for (AccountInformationCommand.StudentCommand student: students) {
            if (student.getSchoolId() > -1) {
                String uniqueString = student.getState().getAbbreviation() + student.getSchoolId();
                if (!uniqSubs.contains(uniqueString)) {
                    Subscription sub = new Subscription();
                    sub.setUser(user);
                    sub.setProduct(SubscriptionProduct.PARENT_CONTACT);
                    sub.setSchoolId(student.getSchoolId());
                    sub.setState(student.getState());
                    _subscriptionDao.saveSubscription(sub);
                    uniqSubs.add(uniqueString);
                }
            }
        }
    }

    /**
     * Gets all subscriptions of a particular product for this user and deletes them
     * @param user User in question
     * @param product Product to delete all subscriptions from
     */
    protected void deleteSubscriptionsForProduct(User user, SubscriptionProduct product) {
        List<Subscription> oldSubs = _subscriptionDao.getUserSubscriptions(user, product);
        if (oldSubs != null) {
            for (Subscription oldSub : oldSubs) {
                _subscriptionDao.removeSubscription(oldSub.getId());
            }
        }
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
            if (!StringUtils.isEmpty(text) && !StringUtils.equals("--", text)) {
                _grade = Grade.getGradeLevel(text);
            } else {
                _grade = null;
            }
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

    // for unit tests
    protected GradePropertyEditor getGradePropertyEditor() {
        return new GradePropertyEditor();
    }

    protected class StudentComparator implements Comparator<Student> {
        public int compare(Student s1, Student s2) {
            return s1.getOrder().compareTo(s2.getOrder());
        }
    }
}
