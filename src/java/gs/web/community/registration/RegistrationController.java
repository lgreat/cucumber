package gs.web.community.registration;

import gs.data.community.*;
import gs.data.util.table.ITableDao;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.web.util.*;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author <a href="mailto:aroy@greatschools.org">Anthony Roy</a>
 * @author <a href="mailto:droy@greatschools.org">Dave Roy</a>
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ITableDao _tableDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;
    private JavaMailSender _mailSender;
    private EmailVerificationEmail _emailVerificationEmail;
    private boolean _requireEmailValidation = true;
    private String _errorView;
    /** If defined, the view that this controller should redirect to. Special casing for hovers. **/
    private String _hoverView;
    private boolean _chooserRegistration;
    private String _how;
    public static final String CITY_PARAMETER = "city";
    public static final String SPREADSHEET_ID_FIELD = "ip";

    // Allows subclasses to skip out on child processing
    protected boolean hasChildRows() {
        return true;
    }

    //set up defaults if none supplied
    @Override
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        UserCommand userCommand = (UserCommand) command;
        userCommand.setRedirectUrl(request.getParameter("redirect"));
        if (isChooserRegistration()) {
            setupChooserRegistration(userCommand);
            userCommand.setNewsletter(true);
        }
        loadCityList(request, userCommand);

        // AR: There are a number of ways that provisional users could still end up here, and I
        // think we need this for them
        // This ensures that when provisional users arrive here, they are hooked up to their list_member
        // rows, so they don't see the "email already taken" error
        if (StringUtils.isNotEmpty(userCommand.getEmail())) {
            User user = _userDao.findUserFromEmailIfExists(userCommand.getEmail());
            if (user != null && !user.isEmailValidated()) {
                // only allow setting the password on people with empty or provisional password
                // existing users have to authenticate and change account settings through other channels
                userCommand.setUser(user);
                // detach user from session so clearing the names has no effect
                _userDao.evict(user);
                // clear first/last name for existing users
                userCommand.setFirstName(null);
                userCommand.setLastName(null);
            }
        }
    }

    @Override
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        UserCommand userCommand = (UserCommand) super.formBackingObject(httpServletRequest);
        // note: We have to create the right number of students here so that the databinder will succeed
        // in binding child properties. Otherwise, it attempts to bind to e.g. userCommand.studentRows[0] and
        // gets an index out of bounds exception
        // (Since the page is configured to always submit 9 children, I have to add all 9 here)
        if (hasChildRows()) {
            for (int x=0; x < 9; x++) {
                UserCommand.StudentCommand student = new UserCommand.StudentCommand();
                userCommand.addStudentRow(student);
            }
        }
        return userCommand;
    }

    private void setupChooserRegistration(UserCommand userCommand) {
        // set up defaults for data not collected in chooser registration
        userCommand.setChooserRegistration(true);
//        userCommand.setNewsletter(false);
        // Gender u because no gender input on chooser reg
        userCommand.setGender("u");
        userCommand.setNumSchoolChildren(0);
    }

    @Override
    public void onBind(HttpServletRequest request, Object command) {
        UserCommand userCommand = (UserCommand) command;

        if (isChooserRegistration()) {
            // need to call this so that userCommand.isChooserRegistration() == true during validation
            setupChooserRegistration(userCommand);
        }

        userCommand.setCity(request.getParameter("city"));
        loadCityList(request, userCommand);

        int numChildFields = 1;
        while (request.getParameter("grade" + numChildFields) != null) {
            parseStudent(request, userCommand, numChildFields);
            numChildFields++;
        }
    }

    protected void parseStudent(HttpServletRequest request, UserCommand userCommand, int childNum) {
        String sGrade = request.getParameter("grade" + childNum);
        String sSchoolId = request.getParameter("school" + childNum);

        State state;
        String city;

        // pull existing student out of command (see formBackingObject)
        UserCommand.StudentCommand student = userCommand.getStudentRows().get(childNum-1);

        if (!student.isLocationOverride()) {
            // Default to user's state, city
            state = userCommand.getState();
            city = userCommand.getCity();
        } else {
            // Use child's state, city
            String stateParam = request.getParameter("state" + childNum);
            state = _stateManager.getState(stateParam);
            city = request.getParameter("city" + childNum);
            // if the location is the same as the parent, it is no longer considered an override
            if (state != null && state.equals(userCommand.getState()) && city != null && city.equals(userCommand.getCity())) {
                student.setLocationOverride(false);
            }
        }

        if (!StringUtils.isEmpty(sGrade)) {
            student.setGradeSelected(Grade.getGradeLevel(sGrade));
        }

        student.setStateSelected(state);
        student.setCitySelected(city);

        if (state != null && !StringUtils.isEmpty(city)) {
            if (!StringUtils.isEmpty(sSchoolId)) {
                student.setSchoolIdSelected(new Integer(sSchoolId));
            }
        } else {
            student.setSchoolIdSelected(-1);
        }

        loadSchoolList(student, city);

        // if the student has overriden their location, make sure to populate the view with the list of cities
        // at that location
        if (student.isLocationOverride()) {
            loadCityList(student);
        }
    }

    protected void loadSchoolList(UserCommand.StudentCommand student, String city) {
        State state = student.getStateSelected();
        Grade grade = student.getGradeSelected();
        if (state != null && grade != null) {
            List<School> schools = _schoolDao.findSchoolsInCityByGrade(state, city, grade);
            School school = new School();
            school.setId(-1);
            school.setName("My child's school is not listed");
            schools.add(school);
            student.setSchools(schools);
        } else {
            student.setSchools(new ArrayList<School>());
        }
    }

    protected void loadCityList(UserCommand.StudentCommand student) {
        State state = student.getStateSelected();
        if (state != null) {
            List<City> cities = _geoDao.findAllCitiesByState(state);
            City city = new City();
            city.setName("My city is not listed");
            cities.add(city);
            student.setCities(cities);
        }
    }

    protected void loadCityList(HttpServletRequest request, UserCommand userCommand) {
        State state = userCommand.getState();
        if (state == null) {
            if (SessionContextUtil.getSessionContext(request).getCity() != null) {
                City userCity = SessionContextUtil.getSessionContext(request).getCity();
                state = userCity.getState();
                SessionContextUtil.getSessionContext(request).setState(state);
                userCommand.setCity(userCity.getName());
            } else {
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }
        }

        if (state != null) {
            List<City> cities = _geoDao.findCitiesByState(state);
            City city = new City();
            city.setName("My city is not listed");
            cities.add(city);
            userCommand.setCityList(cities);
        }
    }

    @Override
    public void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBindAndValidate(request, command, errors);
        UserCommandValidator validator = new UserCommandValidator();
        validator.setUserDao(_userDao);
        validator.validate(request, command, errors);
    }

    @Override
    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;

        boolean userExists = updateCommandUser(userCommand);
        User user = userCommand.getUser();

        setUsersPassword(user, userCommand, userExists);

        if (_requireEmailValidation) {
            // Determine redirect URL for validation email
            String emailRedirectUrl = userCommand.getRedirectUrl();
            if (!isChooserRegistration() && (StringUtils.isEmpty(userCommand.getRedirectUrl()) ||
                    !UrlUtil.isCommunityContentLink(userCommand.getRedirectUrl()))) {
                if (_requireEmailValidation) {
                    if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                        emailRedirectUrl = "/index.page";
                    } else {
                        emailRedirectUrl = "/";
                    }
                }
            }
            sendValidationEmail(request, user, emailRedirectUrl);
        }

        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        updateUserProfile(user, userCommand, ot);

        if (hasChildRows()) {
            persistChildren(userCommand);
        }

        // per GS-8290 All users who complete registration should get a welcome message
        // but only users who haven't already been sent one
        if (!_requireEmailValidation && !isChooserRegistration()
                && user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
        }

        // save
        _userDao.updateUser(user);
        // Because of hibernate caching, it's possible for a list_active record
        // (with list_member id) to be commited before the list_member record is
        // committed. Adding this commitOrRollback prevents this.
        ThreadLocalTransactionManager.commitOrRollback();

        // User object loses its session and this might fix that.
        user = getUserDao().findUserFromId(user.getId());
        userCommand.setUser(user);

        ModelAndView mAndV = new ModelAndView();
        try {
            // if a user registers for the community through the hover and selects the Parent advisor newsletter subscription
            // and even if this is their first subscription no do send the NL welcome email. -Jira -7968
            if(isChooserRegistration() && (userCommand.getNewsletter())){
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.updateUser(user);
            }
            // complete registration
            if (userCommand.getNewsletter()) {
                processNewsletterSubscriptions(userCommand);
            }

            if (userCommand.getPartnerNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
                subscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(subscription);

                Subscription savvySubscription = new Subscription();
                savvySubscription.setUser(user);
                savvySubscription.setProduct(SubscriptionProduct.SAVVY_OPT_IN);
                savvySubscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(savvySubscription);
            }

            if (userCommand.getBrainDrainNewsletter()) {
                _subscriptionDao.addSeasonal(userCommand.getStartweek(),user,userCommand.getUserProfile().getState());
            }

            if (userCommand.getLdNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.LEARNING_DIFFERENCES);
                subscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(subscription);
            }

            saveSubscriptionsForUser(userCommand, ot);
        } catch (Exception e) {
            // if there is any sort of error prior to notifying community,
            // the user MUST BE ROLLED BACK to provisional status
            // otherwise our database is out of sync with community! Bad!
            _log.error("Unexpected error during registration", e);
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            _userDao.updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV;
        }

        if (!_requireEmailValidation) {
            PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
        }
        // Determine redirect URL
        if(StringUtils.isNotBlank(getHoverView())) {
            userCommand.setRedirectUrl(getHoverView());
        } else if (!isChooserRegistration() && (StringUtils.isEmpty(userCommand.getRedirectUrl()) ||
                !UrlUtil.isCommunityContentLink(userCommand.getRedirectUrl()))) {
            if (_requireEmailValidation) {
                SitePrefCookie cookie = new SitePrefCookie(request, response);
                cookie.setProperty("showHover", "validateEmail");
                if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                    userCommand.setRedirectUrl("/index.page");
                } else {
                    userCommand.setRedirectUrl("/");
                }
            } else {
                userCommand.setRedirectUrl("/account/");
            }
        }

        mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());

        return mAndV;
    }

    /**
     * Update the userCommand object's User object with the existing user from the database, if necessary.
     * @param userCommand Command object to place the pre-existing user object into
     * @return True if the user already existsed.
     */
    protected boolean updateCommandUser(UserCommand userCommand) {
        User user = _userDao.findUserFromEmailIfExists(userCommand.getEmail());
        boolean userExists = false;

        if (user != null) {
            userExists = true;
            // update the user's name if they specified a new one
            if (StringUtils.isNotEmpty(userCommand.getFirstName())) {
                user.setFirstName(userCommand.getFirstName());
            }
            if (StringUtils.isNotEmpty(userCommand.getLastName())) {
                user.setLastName(userCommand.getLastName());
            }
            String gender = userCommand.getGender();
            if (StringUtils.isNotEmpty(gender)) {
                user.setGender(userCommand.getGender());
            }
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            user = userCommand.getUser();
            _userDao.saveUser(user);
        }

        if (StringUtils.isBlank(user.getGender())) {
            // Existing and new users that didn't get a gender set should get the default
            user.setGender("u");
        }

        return userExists;
    }

    /**
     * Update the user object with the children submitted
     * @param userCommand Commmand object with the User object and the list of children submitted.
     */
    protected void persistChildren(UserCommand userCommand) {
        User user = userCommand.getUser();
        
        if (user.getStudents() != null) {
            user.getStudents().clear();
        }

        int numRealChildren = 0;
        if (userCommand.getStudentRows().size() > 0) {
            for (UserCommand.StudentCommand student: userCommand.getStudentRows()) {
                Grade grade = student.getGradeSelected();
                if (grade != null) {
                    Student newStudent = new Student();
                    numRealChildren++;
                    newStudent.setOrder(numRealChildren);
                    newStudent.setGrade(grade);

                    int schoolId = student.getSchoolIdSelected();
                    if (schoolId == -1) {
                        newStudent.setSchoolId(null);
                    } else {
                        newStudent.setSchoolId(schoolId);
                    }

                    State state = student.getStateSelected();
                    if (student.isLocationOverride()) {
                        newStudent.setState(state);
                    } else {
                        newStudent.setState(userCommand.getState());
                    }

                    user.addStudent(newStudent);
                }
            }
        }
        user.getUserProfile().setNumSchoolChildren(numRealChildren);
    }

    /**
     * Add a Subscription object to the userCommand for Parent Advisor (aka greatnews)
     * @param userCommand Command object that holds the user to subscribe and the list of subscriptions
     */
    protected void processNewsletterSubscriptions(UserCommand userCommand) {
        Subscription communityNewsletterSubscription = new Subscription();
        communityNewsletterSubscription.setUser(userCommand.getUser());
        communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        // When a user registers through a hover then the state and city field are null for that user
        //instead we use schoolChoiceState and schoolChoiceCity fields.Therefore the if and else block below. Jira - 7915 and 7968(Parent Advisor Newsletter)
        if(userCommand.getState() != null){
            communityNewsletterSubscription.setState(userCommand.getState());
        }

        userCommand.addSubscription(communityNewsletterSubscription);
    }

    /**
     * Save any subscriptions stored in the userCommand.  They will be registered to the user in the userCommand.
     * @param userCommand Command object that has the list of subscriptions
     * @param ot OmnitureTracking object for omniture update
     */
    protected void saveSubscriptionsForUser(UserCommand userCommand, OmnitureTracking ot) {
        List<Subscription> newsSubs = userCommand.getSubscriptions();
        if (newsSubs.size() > 0) {
            User user = userCommand.getUser();
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ot);
            _subscriptionDao.addNewsletterSubscriptions(user, newsSubs);
        }
    }

    protected UserProfile updateUserProfile(User user, UserCommand userCommand, OmnitureTracking ot) {
        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            // note: this code is not reached during Chooser Registration
            userProfile = user.getUserProfile();
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setCity(userCommand.getCity());
            userProfile.setState(userCommand.getState());
        } else {
            // gotten this far, now let's update their user profile
            userProfile = userCommand.getUserProfile();
            userProfile.setHow(getHow());

            userProfile.setUser(user);
            user.setUserProfile(userProfile);

            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);
            if (isChooserRegistration()) {
                ot.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.RegistrationSegment, "Chooser Reg"));
            }
        }
        userProfile.setUpdated(new Date());

        return userProfile;
    }

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl) {
        sendValidationEmail(request, user, redirectUrl, false);
    }

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl,
                                       boolean schoolReviewFlow) {
        try {
            if (schoolReviewFlow) {
                getEmailVerificationEmail().sendSchoolReviewVerificationEmail(request, user, redirectUrl);
            } else {
                getEmailVerificationEmail().sendVerificationEmail(request, user, redirectUrl);
            }
        } catch (Exception e) {
            _log.error("Error sending email message: " + e, e);
        }
    }

    protected void setUsersPassword(User user, UserCommand userCommand, boolean userExists) throws Exception {
        try {
            user.setPlaintextPassword(userCommand.getPassword());
            if (_requireEmailValidation) {
                // mark account as provisional if we require email validation
                user.setEmailProvisional(userCommand.getPassword());
            }
            _userDao.updateUser(user);
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage(), e);
            if (!userExists) {
                // for new users, cancel the account on error
                _userDao.removeUser(user.getId());
            }
            throw e;
        }
    }

    protected boolean isIPBlocked(HttpServletRequest request) {
        // First, check to see if the request is from a blocked IP address. If so,
        // then, log the attempt and show the error view.
        String requestIP = (String) request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        try {
            if (_tableDao.getFirstRowByKey(SPREADSHEET_ID_FIELD, requestIP) != null) {
                _log.warn("Request from blocked IP Address: " + requestIP);
                return true;
            }
        } catch (Exception e) {
            _log.warn("Error checking IP address", e);
        }
        return false;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        this._requireEmailValidation = requireEmailValidation;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }

    public String getErrorView() {
        return _errorView;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }

    public String getHoverView() {
        return _hoverView;
    }

    public void setHoverView(String hoverView) {
        _hoverView = hoverView;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public boolean isChooserRegistration() {
        return _chooserRegistration;
    }

    public void setChooserRegistration(boolean chooserRegistration) {
        _chooserRegistration = chooserRegistration;
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

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }
}