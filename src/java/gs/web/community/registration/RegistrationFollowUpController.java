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
import gs.data.school.Grades;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.web.util.ReadWriteController;
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

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private StateManager _stateManager;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;

    private Set _contactSubs;

    protected void onBindOnNewForm(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBindOnNewForm(request, command);
        _log.info("onBindOnNewForm");
        bindRequestData(request, (FollowUpCommand) command, errors);
    }

    protected void bindRequestData(HttpServletRequest request, FollowUpCommand fupCommand, BindException errors) throws NoSuchAlgorithmException {
        String userId = request.getParameter("id");
        String marker = request.getParameter("marker");
        String recontact = request.getParameter("recontact");
        if (userId != null) {

            User user = _userDao.findUserFromId(Integer.parseInt(userId));
            fupCommand.setUser(user);
            fupCommand.setUserProfile(user.getUserProfile());

            fupCommand.setMarker(marker);
            String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            if (!realHash.equals(marker)) {
                _log.warn("Registration stage 2 request with invalid hash: " + marker);
                errors.rejectValue("id", "bad_hash",
                        "We're sorry, we cannot validate your request at this time. Once " +
                                "your account is validated, please update your profile again.");
            }
        } else {
            return; // exit early
        }
        State state = fupCommand.getUserProfile().getState();
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        Student student = new Student();
        student.setState(state);
        fupCommand.addStudent(student);
        for (int x = 1; x < fupCommand.getUserProfile().getNumSchoolChildren().intValue(); x++) {
            student = new Student();
            student.setState(state);
            fupCommand.addStudent(student);
        }
        fupCommand.setRecontact(recontact);
        loadSchoolLists(request, fupCommand);
        //loadCityList(request, state, fupCommand);
    }

    protected void loadCityList(HttpServletRequest request, State state, FollowUpCommand fupCommand) {
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        List cities = _geoDao.findCitiesByState(state);
        fupCommand.addCityList(cities);
        for (int x = 1; x < fupCommand.getUserProfile().getNumSchoolChildren().intValue(); x++) {
            fupCommand.addCityList(cities);
        }
    }

    protected void loadSchoolLists(HttpServletRequest request, FollowUpCommand fupCommand) {
        State state = fupCommand.getUserProfile().getState();
        String city = fupCommand.getUserProfile().getCity();

        // TODO: remove debug code
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        if (city == null) {
            city = "Alameda";
        }
        _log.info("Obtaining school list for " + state + ": " + city);
        List schools = _schoolDao.findSchoolsInCity(state, city, true);
        _log.info("Adding school list (size=" + ((schools!=null)?schools.size():0) + ") for child #1");
        fupCommand.addSchools(schools);
        fupCommand.addSchoolName("");

        fupCommand.getCityNames().clear();
        _log.info("Adding city (" + city + ") for child #1");
        fupCommand.addCityName(city);
        for (int x = 1; x < fupCommand.getNumStudents(); x++) {
            _log.info("Adding school list for child #" + (x+1));
            fupCommand.addSchools(schools);
            fupCommand.addSchoolName("");
            _log.info("Adding city (" + city + ") for child #" + (x+1));
            fupCommand.addCityName(city);
        }
    }

    public void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBind(request, command);
        _log.info("onBind");
        bindRequestData(request, (FollowUpCommand) command, errors);
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        FollowUpCommand fupCommand = (FollowUpCommand)command;
        Integer userId = fupCommand.getUser().getId();
        if (userId == null) {
            _log.warn("Registration follow-up request with missing user id");
            errors.rejectValue("id", null,
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
            return;
        }
        User user = _userDao.findUserFromId(userId.intValue());
        // update the command with some useful info from the previous stage of registration
        fupCommand.setUser(user);
        fupCommand.getUserProfile().setNumSchoolChildren(user.getUserProfile().getNumSchoolChildren());
        fupCommand.getUserProfile().setState(user.getUserProfile().getState());
        // the recontact flag is not bound, so check for it here
        boolean recontact = false;
        if (request.getParameter("recontact") != null) {
            fupCommand.setRecontact("true");
            _log.info("recontact=" + fupCommand.getRecontact());
        }

        // make sure the user is supposed to be here
        String hash = request.getParameter("marker");
        fupCommand.setMarker(hash);
        String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        if (!realHash.equals(hash)) {
            _log.warn("Registration follow-up request with invalid hash: " + hash);
            errors.rejectValue("id", null,
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
        }

        // Parse the children out of the request and get them into the command
        int loopCount = 1; // always loop at least once
        Integer numSchoolChildren = user.getUserProfile().getNumSchoolChildren();
        if (numSchoolChildren != null && numSchoolChildren.intValue() > 1) {
            loopCount = numSchoolChildren.intValue();
        }
        fupCommand.getSchoolNames().clear();
        fupCommand.getStudents().clear();
        for (int x=0; x < loopCount; x++) {
            int childNum = x+1;

            // add student into command now so we can register errors as they occur
            Student student = new Student();
            fupCommand.addStudent(student);

            // collect as much info as possible
            String sGrade = request.getParameter("grade" + childNum);
            String schoolId = request.getParameter("school" + childNum);
            String sState = request.getParameter("state" + childNum);

            _log.info("grade=" + sGrade);
            _log.info("schoolId=" + schoolId);
            _log.info("state=" + sState);

            Grade grade = null;
            if (!StringUtils.isEmpty(sGrade)) {
                grade = Grade.getGradeLevel(sGrade);
            } else {
                errors.rejectValue("students[" + x + "]", null, "Please choose a grade");
            }
            _log.info("grade=" + grade);
            State state = null;
            if (!StringUtils.isEmpty(sState)) {
                state = _stateManager.getState(sState);
            }
            _log.info("state=" + state);
            School school = null;
            if (!StringUtils.isEmpty(schoolId)) {
                try {
                    school = _schoolDao.getSchoolById(state, new Integer(schoolId));
                } catch (ObjectRetrievalFailureException orfe) {
                    _log.warn("Can't find school corresponding to id " +
                            schoolId + " in " + state);
                }
            } else {
                errors.rejectValue("students[" + x + "]", null, "Please choose a school");
            }

            if (school != null && grade != null) {
                Grades schoolGrades = school.getGradeLevels();
                if (!schoolGrades.contains(grade)) {
                    // Always provide the error message if they are adding/removing children.
                    // Only provide the error message once if they are submitting (i.e. allow them
                    // to override).
                    if (request.getParameter("addChild") != null ||
                            request.getParameter("removeChild") != null ||
                            request.getParameter("ignoreError" + childNum) == null) {
                        errors.rejectValue("students[" + x + "]", null,
                                "According to our records, the selected grade does not " +
                                        "exist in that school. Click on \"Submit\" below to " +
                                        "override this error.");
                    }
                }
            }
            _log.info("school=" + school);            
            // now that we've assembled as much info as possible from the request, let's
            // package it into a Student object and throw it in the command.
            student.setGrade(grade);
            student.setState(state);
            if (school != null) {
                student.setSchool(school);
                // a list of school names makes persisting the page MUCH easier
                // (order is preserved for students!!)
                fupCommand.addSchoolName(school.getName());
            } else {
                // to avoid index out of bounds exceptions, we have to add something to the list
                fupCommand.addSchoolName("");
            }
            student.setOrder(new Integer(childNum));
            // student object was already added to command, so we're done.
        }

        // now check if they are adding/removing children
        if (request.getParameter("addChild") != null) {
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
        } else if (request.getParameter("removeChild") != null) {
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
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) {
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
                if (Boolean.valueOf(fupCommand.getRecontact()).booleanValue()) {
                    addContactSubscriptionFromStudent(student, user);
                }
                user.addStudent(student);
            }
        }
        deleteSubscriptionsForProduct(user, SubscriptionProduct.PREVIOUS_SCHOOLS);
        saveSubscriptionsForUser(fupCommand, user);
        // save
        _userDao.updateUser(user);

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("id", user.getId());
        mAndV.getModel().put("marker", fupCommand.getMarker());
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
}
