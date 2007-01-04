package gs.web.community.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.orm.ObjectRetrievalFailureException;
import gs.data.community.*;
import gs.data.util.DigestUtil;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Handles stage 2 of registration.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registration2.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private static final int MAX_CHILDREN = 11;
    public static final int NUMBER_PREVIOUS_SCHOOLS = 3;
    public static final int ABOUT_ME_MAX_LENGTH = 3000;
    public static final int STUDENT_NAME_MAX_LENGTH = 50;
    public static final int OTHER_INTEREST_MAX_LENGTH = 255;

    public static final String ERROR_GRADE_MISSING = "Please select your child's grade and school.";
    public static final String ERROR_SCHOOL_MISSING = "Please select your child's school. " +
            "If you cannot find the school, please select \"My child's school is not listed.\"";
    public static final String ERROR_TERMS = "Please accept our Terms of Use to join the community.";

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private StateManager _stateManager;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private RegistrationConfirmationEmail _registrationConfirmationEmail;
    private AuthenticationManager _authenticationManager;

    private Set _contactSubs;

    protected void onBindOnNewForm(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBindOnNewForm(request, command);
        FollowUpCommand fupCommand = (FollowUpCommand) command;

        bindRequestData(request, fupCommand, errors);

        State state = fupCommand.getUserProfile().getState();
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        String city = fupCommand.getUserProfile().getCity();
        for (int x = 0; x < 1 || (fupCommand.getUserProfile().getNumSchoolChildren() != null &&
                x < fupCommand.getUserProfile().getNumSchoolChildren().intValue()); x++) {
            Student student = new Student();
            student.setState(state);
            fupCommand.addStudent(student);
            fupCommand.addCityName(city);
            loadCityList(request, fupCommand, errors, x+1);
        }
    }

    public void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBind(request, command);
        FollowUpCommand fupCommand = (FollowUpCommand) command;

        bindRequestData(request, fupCommand, errors);
        for (int x=0; x < fupCommand.getUserProfile().getNumSchoolChildren().intValue(); x++) {
            loadCityList(request, fupCommand, errors, x+1);
        }
    }

    protected void bindRequestData(HttpServletRequest request, FollowUpCommand fupCommand, BindException errors) throws NoSuchAlgorithmException {
        String userId = request.getParameter("id");
        String marker = request.getParameter("marker");
        fupCommand.setRecontact(request.getParameter("recontactStr"));
        if (request.getParameter("termsStr") != null) {
            fupCommand.setTerms("y".equals(request.getParameter("termsStr")));
        }
        if (userId != null) {

            User user = _userDao.findUserFromId(Integer.parseInt(userId));
            fupCommand.setUser(user);
            fupCommand.setUserProfile(user.getUserProfile());

            fupCommand.setMarker(marker);
            String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            if (!realHash.equals(marker)) {
                _log.warn("Registration stage 2 request with invalid hash: " + marker);
                errors.rejectValue("id", null,
                        "We're sorry, we cannot validate your request at this time. Once " +
                                "your account is validated, please update your profile again.");
            }
        }

        for (int x=0; fupCommand.getUserProfile().getNumSchoolChildren() != null &&
                x < fupCommand.getUserProfile().getNumSchoolChildren().intValue(); x++) {
            int childNum = x+1;
            if (request.getParameter("grade" + childNum) != null) {
                parseStudent(request, fupCommand, errors, childNum);
            }
        }
        fupCommand.getCityList().clear();
    }

    protected void loadCityList(HttpServletRequest request, FollowUpCommand fupCommand, BindException errors, int childNum) {
        State state;
        if (fupCommand.getNumStudents() >= childNum) {
            Student student = (Student) fupCommand.getStudents().get(childNum-1);
            state = student.getState();
        } else {
            state = fupCommand.getUserProfile().getState();
            if (state == null) {
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }
        }
        List cities = _geoDao.findCitiesByState(state);
        City city = new City();
        city.setName("My child's city is not listed");
        cities.add(0, city);
        fupCommand.addCityList(cities);
    }

    protected void parseStudent(HttpServletRequest request, FollowUpCommand fupCommand, BindException errors, int childNum) {
        String sGrade = request.getParameter("grade" + childNum);
        State state = _stateManager.getState(request.getParameter("state" + childNum));
        String sSchoolId = request.getParameter("school" + childNum);
        String city = request.getParameter("city" + childNum);

        Student student = new Student();

        if (!StringUtils.isEmpty(sGrade)) {
            student.setGrade(Grade.getGradeLevel(sGrade));
        }
        if (!StringUtils.isEmpty(sSchoolId)) {
            student.setSchoolId(new Integer(sSchoolId));
        }
        student.setState(state);
        student.setOrder(new Integer(childNum));

        fupCommand.addStudent(student);
        fupCommand.addCityName(city);
        loadSchoolList(student, city, fupCommand);
    }

    protected void loadSchoolList(Student student, String city, FollowUpCommand fupCommand) {
        State state = student.getState();
        Grade grade = student.getGrade();
        if (grade != null) {
            List schools = _schoolDao.findSchoolsInCityByGrade(state, city, grade);
            School school = new School();
            school.setId(new Integer(-1));
            school.setName("My child's school is not listed");
            schools.add(0, school);
            fupCommand.addSchools(schools);
        } else {
            fupCommand.addSchools(new ArrayList());
        }
        fupCommand.addSchoolName("");
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) {
        FollowUpCommand fupCommand = (FollowUpCommand)command;
        Integer userId = fupCommand.getUser().getId();
        if (userId == null) {
            _log.warn("Registration follow-up request with missing user id");
            errors.rejectValue("id", null,
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
            return;
        }
        User user = fupCommand.getUser();

        fupCommand.getSchoolNames().clear();
        for (int x=0; x < user.getUserProfile().getNumSchoolChildren().intValue(); x++) {
            Student student = (Student) fupCommand.getStudents().get(x);
            if (student.getGrade() == null) {
                errors.rejectValue("students[" + x + "]", null, ERROR_GRADE_MISSING);
            }
            School school = null;
            if (student.getSchoolId() != null && student.getSchoolId().intValue() != -1) {
                try {
                    school = _schoolDao.getSchoolById(student.getState(), student.getSchoolId());
                } catch (ObjectRetrievalFailureException orfe) {
                    _log.warn("Can't find school corresponding to id " +
                            student.getSchoolId() + " in " + student.getState());
                }
            } else if (student.getSchoolId() == null) {
                errors.rejectValue("students[" + x + "]", null, ERROR_SCHOOL_MISSING);
            }

            if (school != null) {
                student.setSchool(school);
                // a list of school names makes persisting the page MUCH easier
                // (order is preserved for students!!)
                fupCommand.addSchoolName(school.getName());
            } else if (student.getSchoolId() != null && student.getSchoolId().intValue() == -1) {
                fupCommand.addSchoolName("My child's school is not listed");
            } else {
                // to avoid index out of bounds exceptions, we have to add something to the list
                fupCommand.addSchoolName("");
            }
        }

        if (!fupCommand.getTerms()) {
            errors.rejectValue("terms", null, ERROR_TERMS);
        }

//        // now check if they are adding/removing children
//        if (request.getParameter("addChild") != null) {
//            addChild(user, fupCommand, errors);
//        } else if (request.getParameter("removeChild") != null) {
//            removeChild(user, fupCommand, errors);
//        }
    }

    protected void removeChild(User user, FollowUpCommand fupCommand, BindException errors) {
        // remove a child row. See addChild for additional notes
        UserProfile userProfile = user.getUserProfile();
        Integer numChildren = userProfile.getNumSchoolChildren();
        if (numChildren == null || numChildren.intValue() < 1) {
            numChildren = new Integer(0);
        } else {
            numChildren = new Integer(numChildren.intValue() - 1);
        }
        userProfile.setNumSchoolChildren(numChildren);
        _userDao.updateUser(user);
        fupCommand.getUserProfile().setNumSchoolChildren(numChildren);
        // see addChild above for why an error is generated
        errors.rejectValue("userProfile", null, "Removing child");
    }

    protected void addChild(User user, FollowUpCommand fupCommand, BindException errors) {
        // they've requested to add a child
        // refresh the page with an additional child
        UserProfile userProfile = user.getUserProfile();
        Integer numChildren = userProfile.getNumSchoolChildren();
        if (numChildren == null || numChildren.intValue() < 1) {
            // there is always one row on the page, so the minimum outcome from clicking
            // add is two rows.
            numChildren = new Integer(2);
        } else if (numChildren.intValue() >= MAX_CHILDREN) {
            numChildren = new Integer(MAX_CHILDREN);
        } else {
            numChildren = new Integer(numChildren.intValue() + 1);
        }
        userProfile.setNumSchoolChildren(numChildren);
        _userDao.updateUser(user);
        fupCommand.getUserProfile().setNumSchoolChildren(numChildren);
        // now that the numSchoolChildren value has been updated, we need to return to the page
        // so it will refresh with an additional child row.
        // I do this by rejecting a non-existant value. No error is displayed, so from the user's
        // perspective the page simply reloads with an additional child
        errors.rejectValue("userProfile", null, "Adding child");
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws NoSuchAlgorithmException {
        ModelAndView mAndV = new ModelAndView();

        FollowUpCommand fupCommand = (FollowUpCommand)command;
        User user = fupCommand.getUser();
        // get existing profile
        // in validation stage above, the command was populated with the actual DB user
        // so this is getting the actual DB user profile
        UserProfile existingProfile = user.getUserProfile();
        if (user.getStudents() != null) {
            user.getStudents().clear();
        }
        _contactSubs = new HashSet();
        deleteSubscriptionsForProduct(user, SubscriptionProduct.PARENT_CONTACT);
        if (existingProfile.getNumSchoolChildren().intValue() == 0) {
            // there is an odd case where they specified 0 children but then entered
            // a child's info in the default provided field.
            // try to detect this case, and increment their number of children.
            if (fupCommand.getStudents().size() == 1) {
                Student student = (Student) fupCommand.getStudents().get(0);
                // grade and state are automatically set, so only check the child name and
                // school id fields. If either of those have changed, assume they changed
                // their mind and actually have one child
                if (student.getSchoolId() != null ||
                        (student.getName() != null && !student.getName().equals("Child #1"))) {
                    user.addStudent(student);
                    if (Boolean.valueOf(fupCommand.getRecontact()).booleanValue()) {
                        addContactSubscriptionFromStudent(student, user);
                    }
                    existingProfile.setNumSchoolChildren(new Integer(1));
                }
            }
        } else {
            for (int x=0; x < fupCommand.getStudents().size(); x++) {
                Student student = (Student) fupCommand.getStudents().get(x);
                if ("y".equals(fupCommand.getRecontact())) {
                    addContactSubscriptionFromStudent(student, user);
                }
                if (student.getSchoolId() != null && student.getSchoolId().intValue() == -1) {
                    student.setSchoolId(null);
                }
                user.addStudent(student);
            }
        }
        saveSubscriptionsForUser(fupCommand, user);
        // save
        if (user.isEmailProvisional()) {
            user.setEmailValidated();
        }
        _userDao.updateUser(user);

        if (!user.isEmailProvisional()) {
            // registration is done, let's send a confirmation email
            try {
                _registrationConfirmationEmail.sendToUser(user, request);
            } catch (Exception ex) {
                _log.error("Error sending community registration confirmation email to " + user);
                _log.error(ex);
            }
        }

        PageHelper.setMemberAuthorized(request, response, user);
        AuthenticationManager.AuthInfo authInfo = _authenticationManager.generateAuthInfo(user);
        if (StringUtils.isEmpty(fupCommand.getRedirect())) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.ACCOUNT_INFO, null, null);
            fupCommand.setRedirect(builder.asFullUrl(request));
        }
        mAndV.setViewName("redirect:" + _authenticationManager.addParameterIfNecessary
            (fupCommand.getRedirect(), authInfo));

        return mAndV;
    }

    private void saveSubscriptionsForUser(FollowUpCommand fupCommand, User user) {
        for (int x=0; x < fupCommand.getSubscriptions().size(); x++) {
            Subscription sub = (Subscription)fupCommand.getSubscriptions().get(x);
            sub.setUser(user);
            _subscriptionDao.saveSubscription(sub);
            if (Boolean.valueOf(fupCommand.getRecontact()).booleanValue()) {
                addContactSubscriptionFromSubscription(sub);
            }
        }
    }

    private void deleteSubscriptionsForProduct(User user, SubscriptionProduct product) {
        List oldSubs = _subscriptionDao.getUserSubscriptions(user, product);
        if (oldSubs != null) {
            for (int x=0; x < oldSubs.size(); x++) {
                _subscriptionDao.removeSubscription(((Subscription)oldSubs.get(x)).getId());
            }
        }
    }

    private void addContactSubscriptionFromSubscription(Subscription otherSub) {
        if (otherSub.getProduct().equals(SubscriptionProduct.PREVIOUS_SCHOOLS)) {
            addContactSubscription(otherSub.getUser(), otherSub.getSchoolId(), otherSub.getState());
        }
    }

    private void addContactSubscriptionFromStudent(Student student, User user) {
        if (student.getSchoolId() != null) {
            addContactSubscription(user, student.getSchoolId().intValue(), student.getState());
        }
    }

    private void addContactSubscription(User user, int schoolId, State state) {
        String uniqueString = state.getAbbreviation() + schoolId;
        if (!_contactSubs.contains(uniqueString)) {
            _log.info("Saving subscription: " + user + ";" + schoolId + ";" + state);
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.PARENT_CONTACT);
            sub.setSchoolId(schoolId);
            sub.setState(state);
            _subscriptionDao.saveSubscription(sub);
            _contactSubs.add(uniqueString);
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public RegistrationConfirmationEmail getRegistrationConfirmationEmail() {
        return _registrationConfirmationEmail;
    }

    public void setRegistrationConfirmationEmail(RegistrationConfirmationEmail registrationConfirmationEmail) {
        _registrationConfirmationEmail = registrationConfirmationEmail;
    }

    public AuthenticationManager getAuthenticationManager() {
        return _authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        _authenticationManager = authenticationManager;
    }
}
