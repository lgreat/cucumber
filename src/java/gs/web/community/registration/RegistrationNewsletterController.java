package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.web.util.ReadWriteController;
import gs.data.community.*;
import gs.data.school.*;
import gs.data.util.DigestUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.security.NoSuchAlgorithmException;

/**
 * Handles stage 3 of registration: allowing the user to sign up for the multitude of
 * available and appropriate newsletters.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationNewsletterController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationFollowUpSuccess.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;

    protected void onBindOnNewForm(HttpServletRequest request, Object objCommand, BindException errors) throws NoSuchAlgorithmException {
        NewsletterCommand command = (NewsletterCommand) objCommand;
        User user = _userDao.findUserFromId(command.getUser().getId().intValue());
        command.setUser(user);

        // make sure the user is supposed to be here
        String hash = command.getMarker();
        String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        if (!realHash.equals(hash)) {
            _log.warn("Registration newsletter request with invalid hash: " + hash);
            errors.reject("bad_hash",
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
        }
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        if (request.getParameter("id") != null) {
            return populateCommand(Integer.parseInt(request.getParameter("id")));
        }
        return super.formBackingObject(request);
    }

    protected NewsletterCommand populateCommand(int id) {
        NewsletterCommand command = new NewsletterCommand();
        User user = _userDao.findUserFromId(id);
        command.setUser(user);
        List subs = _subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.MYSTAT);
        int availableSubs = SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER -
                ((subs != null)?subs.size():0);
        command.setAvailableMssSubs(availableSubs);
        parseStudents(command, user.getStudents(), subs);
        List studentSchools = command.getStudentSchools();
        command.setStudentSchools(studentSchools);
        if (studentSchools.size() > 0) {
            // they have listed some schools we can offer to subscribe them to
            if (studentSchools.size() <= availableSubs) {
                command.setAllMss(true); // we can offer to sign them up for all their children's schools
            }
        }
        return command;
    }

    private boolean hasMssToSchool(List mssSubs, School school) {
        for (int subIndex=0; subIndex < mssSubs.size(); subIndex++) {
            Subscription sub = (Subscription)mssSubs.get(subIndex);
            if (sub.getSchoolId() == school.getId().intValue() &&
                    sub.getState().equals(school.getDatabaseState())) {
                return true;
            }
        }
        return false;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object objCommand, BindException errors) throws NoSuchAlgorithmException {
        NewsletterCommand command = (NewsletterCommand) objCommand;

        User user = _userDao.findUserFromId(command.getUser().getId().intValue());
        command.setUser(user);

        // make sure the user is supposed to be here
        String hash = command.getMarker();
        String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        if (!realHash.equals(hash)) {
            _log.warn("Registration newsletter request with invalid hash: " + hash);
            errors.reject("bad_hash",
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
            return;
        }

        List subscriptions = new ArrayList();

        int numMssSubscriptions = 0;
        int availableMssSubs = command.getAvailableMssSubs();
        for (int x=0; x < command.getNumStudentSchools(); x++) {
            School school = (School)command.getStudentSchools().get(x);
            // there are two cases:
            // 1) they are presented exactly one checkbox (allMss) to sign up for all their schools.
            // 2) they are presented one checkbox per school, with name "{state}{id}".
            String paramString = school.getDatabaseState().getAbbreviation() + school.getId();
            if (request.getParameter("allMss") != null || request.getParameter(paramString) != null) {
                if (numMssSubscriptions >= availableMssSubs) {
                    errors.reject("too_many_mss",
                            "Please only select up to " + availableMssSubs +
                                    ((availableMssSubs==1)?" subscription.":" subscriptions."));
                    break;
                }
                numMssSubscriptions++;
                Subscription mssSub = new Subscription();
                mssSub.setUser(command.getUser());
                mssSub.setState(school.getDatabaseState());
                mssSub.setSchoolId(school.getId().intValue());
                mssSub.setProduct(SubscriptionProduct.MYSTAT);
                subscriptions.add(mssSub);
            }
        }

        if (request.getParameter("gradeK") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_KINDERGARTNER);
        }
        if (request.getParameter("grade1") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_FIRST_GRADER);
        }
        if (request.getParameter("grade2") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_SECOND_GRADER);
        }
        if (request.getParameter("grade3") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_THIRD_GRADER);
        }
        if (request.getParameter("grade4") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_FOURTH_GRADER);
        }
        if (request.getParameter("grade5") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_FIFTH_GRADER);
        }
        if (request.getParameter("gradeMiddle") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_MS);
        }
        if (request.getParameter("gradeHigh") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.MY_HS);
        }
        if (request.getParameter("advisor") != null) {
            addSubscriptionToList(subscriptions, command.getUser(), SubscriptionProduct.PARENT_ADVISOR);
        }

        command.setSubscriptions(subscriptions);
    }

    private void addSubscriptionToList(List subscriptions, User user, SubscriptionProduct product) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setState(user.getUserProfile().getState());
        sub.setProduct(product);
        subscriptions.add(sub);
    }

    protected ModelAndView onSubmit(Object objCommand) {
        NewsletterCommand command = (NewsletterCommand) objCommand;

        if (command.getSubscriptions() != null && command.getSubscriptions().size() > 0) {
            _subscriptionDao.addNewsletterSubscriptions(command.getUser(), command.getSubscriptions());
        }

        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(getSuccessView());
        return mAndV;
    }

    private void parseStudents(NewsletterCommand command, Set students, List subs) {
        if (students == null) {
            return;
        }
        List uniqueSchools = new ArrayList(); // to enforce unique constraint
        School school = null;
        Iterator iter = students.iterator();
        List studentSchools = new ArrayList();
        while (iter.hasNext()) {
            Student student = (Student) iter.next();
            // add school to our list
            if (student.getSchoolId() != null) {
                school = _schoolDao.getSchoolById(student.getState(), student.getSchoolId());
                String uniqueConstraint = school.getDatabaseState().getAbbreviation() +
                        school.getId();
                // we can only sign them up for a subset of their children's schools
                // what if they are already signed up for one or more of their children's schools?
                // Those are not included
                if ( (subs == null || !hasMssToSchool(subs, school)) &&
                        !uniqueSchools.contains(uniqueConstraint)) {
                    // if there is not already an mss subscription to the school
                    // and if this school hasn't already been added to the list
                    // then add it to the list
                    studentSchools.add(school);
                    uniqueSchools.add(uniqueConstraint);
                }
            }
            // determine newsletter appropriate to grade of child/school
            Grade grade = student.getGrade();
            if (grade != null) {
                // offer newsletter appropriate to the grade of the child
                if (grade.equals(Grade.KINDERGARTEN)) {
                    command.setHasK(true);
                } else if (grade.equals(Grade.G_1)) {
                    command.setHasFirst(true);
                } else if (grade.equals(Grade.G_2)) {
                    command.setHasSecond(true);
                } else if (grade.equals(Grade.G_3)) {
                    command.setHasThird(true);
                } else if (grade.equals(Grade.G_4)) {
                    command.setHasFourth(true);
                } else if (grade.equals(Grade.G_5)) {
                    command.setHasFifth(true);
                } else if (grade.equals(Grade.G_6)) {
                    command.setHasMiddle(true);
                } else if (grade.equals(Grade.G_7)) {
                    command.setHasMiddle(true);
                } else if (grade.equals(Grade.G_8)) {
                    command.setHasMiddle(true);
                } else if (grade.equals(Grade.G_9)) {
                    command.setHasHigh(true);
                } else if (grade.equals(Grade.G_10)) {
                    command.setHasHigh(true);
                } else if (grade.equals(Grade.G_11)) {
                    command.setHasHigh(true);
                } else if (grade.equals(Grade.G_12)) {
                    command.setHasHigh(true);
                }
            } else if (school != null) {
                // they didn't tell us what grade the child is, so we'll give them
                // all the options for the school
                LevelCode levelCode = school.getLevelCode();
                if (levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                    command.setHasK(true);
                    command.setHasFirst(true);
                    command.setHasSecond(true);
                    command.setHasThird(true);
                    command.setHasFourth(true);
                    command.setHasFifth(true);
                }
                if (levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                    command.setHasMiddle(true);
                }
                if (levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                    command.setHasHigh(true);
                }
            }
        }
        command.setStudentSchools(studentSchools);
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
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
}
