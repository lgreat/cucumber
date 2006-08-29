package gs.web.community.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.orm.ObjectRetrievalFailureException;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.Student;
import gs.data.util.DigestUtil;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.Grades;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.ReadWriteController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationSuccess.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private StateManager _stateManager;
    private ISchoolDao _schoolDao;

    // WARNING: This is the master list of interests. Only edit this if you know what you are doing!
    // WARNING: These two arrays must be kept in sync.
    // To add an interest, simply add the code and relevant value (don't forget the comma!).
    // To remove an interest, remove the code and relevant value (don't forget the comma!).
    // CODES are what the database stores.
    // VALUES are what the user sees next to the check box.
    public static final String[] INTEREST_CODES = {
            "PTA",
            "SCHOOL_IMPROVEMENT",
            "SPECIAL_EDUCATION",
            "PROGRAMS_GIFTED_STUDENTS",
            "SPORTS",
            "MUSIC_DRAMA",
            "CHARTER_SCHOOLS"
    };
    // WARNING: This array must be kept in sync with INTEREST_CODES.
    public static final String[] INTEREST_VALUES = {
            "PTA",
            "School Improvement",
            "Special Education",
            "Programs for Gifted Students",
            "Sports",
            "Music/Drama",
            "Charter Schools"
    };

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (errors.hasErrors()) {
            return;
        }
        FollowUpCommand fupCommand = (FollowUpCommand)command;
        Integer userId = fupCommand.getUser().getId();
        User user = _userDao.findUserFromId(userId.intValue());
        fupCommand.setUser(user);
        fupCommand.getUserProfile().setNumSchoolChildren(user.getUserProfile().getNumSchoolChildren());
        fupCommand.getUserProfile().setState(user.getUserProfile().getState());
        boolean fupprivate = false;
        if (request.getParameter("private") != null) {
            fupprivate = true;
        }
        fupCommand.setPrivate(fupprivate);
        parseInterests(request, fupCommand);

        String hash = request.getParameter("marker");
        fupCommand.setMarker(hash);
        String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        if (!realHash.equals(hash)) {
            _log.warn("Registration follow-up request with invalid hash: " + hash);
            // TODO: this error is never caught by the page
            errors.rejectValue("id", "bad_hash",
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
        }

        int loopCount = 1; // always loop at least once
        Integer numSchoolChildren = user.getUserProfile().getNumSchoolChildren();
        if (numSchoolChildren != null && numSchoolChildren.intValue() > 1) {
            loopCount = numSchoolChildren.intValue();
        }
        for (int x=0; x < loopCount; x++) {
            int childNum = x+1;
            // collect as much info as possible
            String childname = request.getParameter("childname" + childNum);
            String sGrade = request.getParameter("grade" + childNum);
            String schoolName = request.getParameter("school" + childNum);
            String schoolId = request.getParameter("schoolId" + childNum);
            String sState = request.getParameter("state" + childNum);

            Grade grade = null;
            if (!StringUtils.isEmpty(sGrade)) {
                grade = Grade.getGradeLevel(sGrade);
            }
            State state = null;
            if (!StringUtils.isEmpty(sState)) {
                state = _stateManager.getState(sState);
            }
            School school = null;
            if (!StringUtils.isEmpty(schoolId)) {
                try {
                    school = _schoolDao.getSchoolById(state, new Integer(schoolId));
                } catch (ObjectRetrievalFailureException orfe) {
                    _log.warn("Can't find school corresponding to selection: " + schoolName + "(" +
                            schoolId + ")");
                }
            }
            if (school != null && !school.getName().equals(schoolName)) {
                // if the name doesn't match, ignore the school ... they've probably tried to blank out
                // the field
                school = null;
            }
            boolean gradeError = false;
            if (school != null) {
                Grades schoolGrades = school.getGradeLevels();
                if (!schoolGrades.contains(grade)) {
                    gradeError = true;
                    // can't register error here because property hasn't been set yet
                    // (will get an array index out of bounds exception)
                }
            }
            Student student = new Student();
            student.setName(childname);
            student.setGrade(grade);
            student.setState(state);
            if (school != null) {
                student.setSchool(school);
                fupCommand.addSchoolName(school.getName());
            } else {
                fupCommand.addSchoolName(""); // to avoid index out of bounds exceptions
            }
            student.setOrder(new Integer(childNum));
            fupCommand.addStudent(student);
            if (gradeError) {
                // Always provide the error message if they are adding/removing children.
                // Only provide the error message once if they are submitting (i.e. allow them
                // to override).
                if (request.getParameter("addChild") != null ||
                        request.getParameter("removeChild") != null ||
                        request.getParameter("ignoreError" + childNum) == null) {
                    errors.rejectValue("students[" + x + "]", "bad_grade",
                            "According to our records, the selected grade does not " +
                                    "exist in that school. Click on \"Submit\" below to " +
                                    "override this error.");
                }
            }
        }

        if (request.getParameter("addChild") != null) {
            // they've requested to add a child
            // refresh the page with an additional child
            UserProfile userProfile = user.getUserProfile();
            Integer numChildren = userProfile.getNumSchoolChildren();
            if (numChildren == null || numChildren.intValue() == 0) {
                // there is always one row on the page, so the minimum outcome from clicking
                // add is two rows.
                numChildren = new Integer(2);
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
            errors.rejectValue("userProfile", "add_child", "Adding child");
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
            errors.rejectValue("userProfile", "remove_child", "Removing child");
        }
    }

    private void parseInterests(HttpServletRequest request, FollowUpCommand fupCommand) {
        fupCommand.getUserProfile().setInterests(null);
        for (int x=0; x < INTEREST_CODES.length; x++) {
            if (request.getParameter(INTEREST_CODES[x]) != null) {
                fupCommand.getUserProfile().addInterest(INTEREST_CODES[x]);
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) {
        ModelAndView mAndV = new ModelAndView();

        FollowUpCommand fupCommand = (FollowUpCommand)command;
        User user = fupCommand.getUser();
        UserProfile profile = fupCommand.getUserProfile();
        // get existing profile
        UserProfile existingProfile = user.getUserProfile();
        // update existing profile with new information
        existingProfile.setAboutMe(profile.getAboutMe());
        existingProfile.setPrivate(profile.isPrivate());
        existingProfile.setInterests(profile.getInterests());
        existingProfile.setOtherInterest(profile.getOtherInterest());
        if (user.getStudents() != null) {
            user.getStudents().clear();
        }
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
                    existingProfile.setNumSchoolChildren(new Integer(1));
                }
            }
        } else {
            for (int x=0; x < fupCommand.getStudents().size(); x++) {
                user.addStudent((Student)fupCommand.getStudents().get(x));
            }
        }
        // save
        _userDao.updateUser(user);

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("name", user.getFirstName());
        return mAndV;
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
}
