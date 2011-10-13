package gs.web.email;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.geo.City;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class NewslettersSignUpController extends SimpleFormController implements ReadWriteController {
    private String _viewName;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    protected final Log _log = LogFactory.getLog(getClass());


    protected void onBindOnNewForm(HttpServletRequest request, Object o) throws Exception {
        NewslettersSignUpCommand command = (NewslettersSignUpCommand) o;

        // set the cities option
        City city = new City();
        city.setName("My city is not listed");
        List<City> cities = new ArrayList<City>();
        cities.add(0, city);
        command.setCityList(cities);
    }

    protected ModelAndView showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException be)
            throws Exception {
        NewslettersSignUpCommand command = (NewslettersSignUpCommand) be.getTarget();

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), command);

        return new ModelAndView(_viewName, model);

    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException be) throws Exception {
        NewslettersSignUpCommand command = (NewslettersSignUpCommand) o;
        if (command.getEmail() != null) {
            User user = getUserDao().findUserFromEmailIfExists(command.getEmail());

//            boolean isNewMember = false;
//
//            // If the user does not yet exist, add to list_member
//            if (user == null) {
//                user = new User();
//                user.setEmail(command.getEmail());
//                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
//                _userDao.saveUser(user);
//                isNewMember = true;
//            }

            State state = user.getState();
            if (user.getUserProfile() != null) {
                // your location
                state = user.getUserProfile().getState();
            }
            if (state == null) {
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }

            List<Subscription> subscriptions = new ArrayList<Subscription>();

            doMySchoolStats(user, command, subscriptions, request);

            submitMyNthList(user, command, state);
            boolean userAlreadySubscribedToDailyTip = false;
            boolean userAlreadySubscribedToSponsor = false;
            List<Subscription> existingSubscriptions = _subscriptionDao.getUserSubscriptions(user);
            if (existingSubscriptions != null) {
                for (Object subscription : existingSubscriptions) {
                    Subscription s = (Subscription) subscription;
                    if (s.getProduct().equals(SubscriptionProduct.DAILY_TIP)) {
                        userAlreadySubscribedToDailyTip = true;
                    } else if (s.getProduct().equals(SubscriptionProduct.SPONSOR_OPT_IN)) {
                        userAlreadySubscribedToSponsor = true;
                    }
                }
            }

            if (command.isDailytip() && !userAlreadySubscribedToDailyTip) {
                Subscription s = new Subscription(user, SubscriptionProduct.DAILY_TIP, state);
                subscriptions.add(s);
            }

            if (command.isSponsor() && !userAlreadySubscribedToSponsor) {
                Subscription s = new Subscription(user, SubscriptionProduct.SPONSOR_OPT_IN, state);
                subscriptions.add(s);
            }

            ThreadLocalTransactionManager.commitOrRollback();
            _subscriptionDao.addNewsletterSubscriptions(user, subscriptions);

            Map<String, Object> model = new HashMap<String, Object>();
            model.put(getCommandName(), command);

            if (command.isTooManySchoolsError()) {
                // set the cities option
                City city = new City();
                city.setName("My city is not listed");
                List<City> cities = new ArrayList<City>();
                cities.add(0, city);
                command.setCityList(cities);
                return new ModelAndView(_viewName, model);
            }
        }

        return new ModelAndView(getSuccessView());

    }

    protected void doMySchoolStats(User user, NewslettersSignUpCommand command, List<Subscription> newSubscriptions,
                                   HttpServletRequest request) {
        List<Subscription> previousMyStatsSubs = _subscriptionDao.findMssSubscriptionsByUser(user);
        Set<String> stateIdStringSetFromDB = new HashSet<String>(previousMyStatsSubs.size());
        Map<String, Subscription> stringToSubscriptionMap =
                new HashMap<String, Subscription>(previousMyStatsSubs.size());
        for (Subscription sub : previousMyStatsSubs) {
            String key = sub.getState().getAbbreviation() + sub.getSchoolId();
            stateIdStringSetFromDB.add(key);
            stringToSubscriptionMap.put(key, sub);
        }

        // pull out the schools represented on the page
        String[] stateIdStringsFromPage = request.getParameterValues("uniqueStateId");
        Set<String> stateIdStringSetFromPage = new HashSet<String>();
        if (stateIdStringsFromPage != null) {
            stateIdStringSetFromPage.addAll(Arrays.asList(stateIdStringsFromPage));
            // get rid of entry from template
            stateIdStringSetFromPage.remove("REPLACE-ME");
        }

        if (stateIdStringSetFromDB.size() + stateIdStringSetFromPage.size() > 4) {
            command.setTooManySchoolsError(true);
        } else {
            // any school on the page that isn't in the existing set, add it
            int counter = 1;
            // reset messages
            command.setSchool1(false);
            command.setSchool2(false);
            command.setSchool3(false);
            command.setSchool4(false);
            for (String stateIdStringFromPage : stateIdStringSetFromPage) {
                if (!stateIdStringSetFromDB.contains(stateIdStringFromPage)) {
                    State stateToAdd;
                    Integer schoolIdToAdd;
                    try {
                        stateToAdd = State.fromString(stateIdStringFromPage.substring(0, 2));
                        schoolIdToAdd = new Integer(stateIdStringFromPage.substring(2));
                        if (schoolIdToAdd < 1) {
                            _log.warn("Invalid school id from page: " + schoolIdToAdd);
                            continue;
                        }
                    } catch (Exception e) {
                        _log.warn("Failed to convert mss from page: " + e, e);
                        continue;
                    }
                    School newSchool = _schoolDao.getSchoolById(stateToAdd, schoolIdToAdd);
                    if (newSchool == null) {
                        _log.warn("Failed to find school from page: " + stateToAdd + ":" + schoolIdToAdd);
                        continue;
                    }
                    // update command for messages
                    if (counter == 1) {
                        command.setSchool1(true);
                        command.setName1(newSchool.getName());
                    } else if (counter == 2) {
                        command.setSchool2(true);
                        command.setName2(newSchool.getName());
                    } else if (counter == 3) {
                        command.setSchool3(true);
                        command.setName3(newSchool.getName());
                    } else if (counter == 4) {
                        command.setSchool4(true);
                        command.setName4(newSchool.getName());
                    } else {
                        _log.warn("Too many schools on page.");
                        break;
                    }
                    if (!stateIdStringSetFromDB.contains(stateIdStringFromPage)) {
                        Subscription s = new Subscription(user, SubscriptionProduct.MYSTAT, stateToAdd);
                        s.setSchoolId(schoolIdToAdd);
                        newSubscriptions.add(s);
                    }
                    counter++;
                }

            }
        }

    }

    public void submitMyNthList(User user,
                                NewslettersSignUpCommand command,
                                State state) {
        List<Student> mynthSubscriptions = _subscriptionDao.findMynthSubscriptionsByUser(user);
        Map<SubscriptionProduct, Integer> myNthMap = new HashMap<SubscriptionProduct, Integer>();
        for (Student myNth : mynthSubscriptions) {
            myNthMap.put(myNth.getMyNth(), myNth.getId());
        }

        for (SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER) {
            if (myNthMap.get(myNth) == null && command.checkedBox(myNth) && !(command.getMyNthId(myNth) > 0)) {
                addMyNth(myNth, user, state);
            }
        }
    }

//    public void doMyNthList(User user, NewslettersSignUpCommand command) {
//        //store existing My Nth Grade subscriptions in myNthMap
//        List<Student> mynthSubscriptions = _subscriptionDao.findMynthSubscriptionsByUser(user);
//        Map<SubscriptionProduct, Integer> myNthMap = new HashMap<SubscriptionProduct, Integer>();
//        for (Student myNth : mynthSubscriptions) {
//            myNthMap.put(myNth.getMyNth(), myNth.getId());
//        }
//
//        //set checkboxes and hidden ids to correct values
//        for (SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER) {
//            if (myNthMap.get(myNth) == null) {
//                continue;
//            }
//            Integer mnId = myNthMap.get(myNth);
//            if (myNth.equals(SubscriptionProduct.MY_PRESCHOOLER)) {
//                command.setMypk(true);
//                command.setMypkId(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_KINDERGARTNER)) {
//                command.setMyk(true);
//                command.setMykId(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_FIRST_GRADER)) {
//                command.setMy1(true);
//                command.setMy1Id(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_SECOND_GRADER)) {
//                command.setMy2(true);
//                command.setMy2Id(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_THIRD_GRADER)) {
//                command.setMy3(true);
//                command.setMy3Id(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_FOURTH_GRADER)) {
//                command.setMy4(true);
//                command.setMy4Id(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_FIFTH_GRADER)) {
//                command.setMy5(true);
//                command.setMy5Id(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_MS)) {
//                command.setMyms(true);
//                command.setMymsId(mnId);
//            } else if (myNth.equals(SubscriptionProduct.MY_HS)) {
//                command.setMyhs(true);
//                command.setMyhsId(mnId);
//            }
//        }
//    }
//
//    protected void checkRequestParams(HttpServletRequest request, ManagementCommand command) {
//        for (SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER) {
//            if (request.getParameter("set" + myNth.getName()) == null) {
//                continue;
//            }
//            // this ensures that if a request param sets an nth newsletter to checked, the
//            // greatnews will also be checked
//            command.setGreatnews(true);
//            if (myNth.equals(SubscriptionProduct.MY_PRESCHOOLER)) {
//                command.setMypk(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_KINDERGARTNER)) {
//                command.setMyk(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_FIRST_GRADER)) {
//                command.setMy1(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_SECOND_GRADER)) {
//                command.setMy2(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_THIRD_GRADER)) {
//                command.setMy3(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_FOURTH_GRADER)) {
//                command.setMy4(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_FIFTH_GRADER)) {
//                command.setMy5(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_MS)) {
//                command.setMyms(true);
//            } else if (myNth.equals(SubscriptionProduct.MY_HS)) {
//                command.setMyhs(true);
//            }
//        }
//    }

    public void addMyNth(SubscriptionProduct sp, User user, State state) {
        Student student = new Student();
        student.setSchoolId(-1);
        student.setGrade(sp.getGrade());
        student.setState(state);
        user.addStudent(student);
        _userDao.saveUser(user);

    }

    public void removeMyNth(SubscriptionProduct myNth, User user) {
        Set<Student> students = user.getStudents();
        Student studentToDelete = null;
        for (Student student : students) {
            if (student.getGrade() != null && student.getGrade().equals(myNth.getGrade())) {
                studentToDelete = student;
                break;
            }
        }
        if (studentToDelete != null) {
            students.remove(studentToDelete);
            user.setStudents(students);
            _userDao.saveUser(user);
        }
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

}
