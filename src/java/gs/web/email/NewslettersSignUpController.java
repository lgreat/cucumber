package gs.web.email;

import gs.data.community.*;
import gs.data.geo.City;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NewslettersSignUpController extends SimpleFormController implements ReadWriteController {

    private EmailVerificationEmail _emailVerificationEmail;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    protected final Log _log = LogFactory.getLog(getClass());

//    protected void onBindOnNewForm(HttpServletRequest request, Object o) throws Exception {
//        NewslettersSignUpCommand command = (NewslettersSignUpCommand) o;
//        // set the cities option
//        setCitiesOptions(command);
//    }

    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws Exception {
        if (errors.hasErrors()) {
            return;
        }

    }

    protected ModelAndView showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException be)
            throws Exception {
        ModelAndView mAndV = super.showForm(request, response, be);
        NewslettersSignUpCommand command = (NewslettersSignUpCommand) be.getTarget();

        Map<String, Object> model = mAndV.getModel();

        setCitiesOptions(command);
        model.put(getCommandName(), command);

        return new ModelAndView(getFormView(), model);

    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException be) throws Exception {
        NewslettersSignUpCommand command = (NewslettersSignUpCommand) o;
        Map<String, Object> model = new HashMap<String, Object>();
        String email = StringEscapeUtils.escapeHtml(command.getEmail());

        if (email != null) {

            User user = getUserDao().findUserFromEmailIfExists(email);

            // If the user does not yet exist, add to list_member
            boolean isNewMember = false;
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.saveUser(user);
                isNewMember = true;
            } else if (!user.getEmailVerified()) {
                isNewMember = true;
            }

            State state = user.getState();
            if (user.getUserProfile() != null) {
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

            if (command.isTooManySchoolsError()) {
                // set the cities option
                setCitiesOptions(command);
                return new ModelAndView(getFormView(), model);
            }

            _subscriptionDao.addNewsletterSubscriptions(user, subscriptions);

            model.put(getCommandName(), command);

            // Send verification email to new users.
            if (isNewMember) {
                sendVerificationEmail(request, user);
            }

            return new ModelAndView(getSuccessView(), model);
        }

        return new ModelAndView(getFormView(), model);
    }

    protected void setCitiesOptions(NewslettersSignUpCommand command) {
        City city = new City();
        city.setName("My city is not listed");
        List<City> cities = new ArrayList<City>();
        cities.add(0, city);
        command.setCityList(cities);
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
            int counter = 1;
            command.setSchool1(false);
            command.setSchool2(false);
            command.setSchool3(false);
            command.setSchool4(false);
            for (String stateIdStringFromPage : stateIdStringSetFromPage) {
                // any school on the page that isn't in the existing set, add it
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

    protected void submitMyNthList(User user,
                                   NewslettersSignUpCommand command,
                                   State state) {
        List<Student> myExistingNthSubscriptions = _subscriptionDao.findMynthSubscriptionsByUser(user);

        Map<SubscriptionProduct, Integer> myExistingNthMap = new HashMap<SubscriptionProduct, Integer>();
        for (Student myNth : myExistingNthSubscriptions) {
            myExistingNthMap.put(myNth.getMyNth(), myNth.getId());
        }

        //Add Nth subscription if user does not already have it.
        for (SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER) {
            if (myExistingNthMap.get(myNth) == null && command.checkedBox(myNth) && !(command.getMyNthId(myNth) > 0)) {
                addMyNth(myNth, user, state);
            }
        }
    }

    protected void addMyNth(SubscriptionProduct sp, User user, State state) {
        Student student = new Student();
        student.setSchoolId(-1);
        student.setGrade(sp.getGrade());
        student.setState(state);
        user.addStudent(student);
        _userDao.saveUser(user);
    }

    private void sendVerificationEmail(HttpServletRequest request, User user)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.NL_SIGN_UP_PAGE);
        String redirectUrl = urlBuilder.asFullUrl(request);
        getEmailVerificationEmail().sendVerificationEmail(request, user, redirectUrl);
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

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

}
