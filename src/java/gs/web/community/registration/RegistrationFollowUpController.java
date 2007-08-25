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
import gs.data.admin.IPropertyDao;
import gs.data.soap.CreateOrUpdateUserRequestBean;
import gs.data.soap.CreateOrUpdateUserRequest;
import gs.data.soap.SoapRequestException;
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

    public static final String ERROR_GRADE_MISSING = "Please select your child's grade and school.";
    public static final String ERROR_SCHOOL_MISSING = "Please select your child's school. " +
            "If you cannot find the school, please select \"My child's school is not listed.\"";
    public static final String ERROR_TERMS = "Please read and accept our Terms of Use to join the community.";

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private StateManager _stateManager;
    private ISchoolDao _schoolDao;
    private IPropertyDao _propertyDao;
    private IGeoDao _geoDao;
    private RegistrationConfirmationEmail _registrationConfirmationEmail;
    private AuthenticationManager _authenticationManager;
    private String _errorView;

    private Set<String> _contactSubs;

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
                x < fupCommand.getUserProfile().getNumSchoolChildren()); x++) {
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

        fupCommand.setTerms("on".equals(request.getParameter(RegistrationController.TERMS_PARAMETER)));
        fupCommand.setNewsletter("on".equals(request.getParameter(RegistrationController.NEWSLETTER_PARAMETER)));
        fupCommand.setBeta("on".equals(request.getParameter(RegistrationController.BETA_PARAMETER)));
        bindRequestData(request, fupCommand, errors);
        for (int x=0; x < fupCommand.getUserProfile().getNumSchoolChildren(); x++) {
            loadCityList(request, fupCommand, errors, x+1);
        }
    }

    protected void bindRequestData(HttpServletRequest request, FollowUpCommand fupCommand, BindException errors) throws NoSuchAlgorithmException {
        String userId = request.getParameter("id");
        String marker = request.getParameter("marker");
        fupCommand.setRecontact(request.getParameter("recontactStr"));
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
                x < fupCommand.getUserProfile().getNumSchoolChildren(); x++) {
            int childNum = x+1;
            if (request.getParameter("grade" + childNum) != null) {
                parseStudent(request, fupCommand, errors, childNum);
            }
        }
        fupCommand.getCityList().clear();

        request.setAttribute("current_academic_year",
                _propertyDao.getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR));
    }

    protected void loadCityList(HttpServletRequest request, FollowUpCommand fupCommand, BindException errors, int childNum) {
        State state;
        if (fupCommand.getNumStudents() >= childNum) {
            Student student = fupCommand.getStudents().get(childNum-1);
            state = student.getState();
        } else {
            state = fupCommand.getUserProfile().getState();
            if (state == null) {
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }
        }
        List<City> cities = _geoDao.findCitiesByState(state);
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
        student.setOrder(childNum);

        fupCommand.addStudent(student);
        fupCommand.addCityName(city);
        loadSchoolList(student, city, fupCommand);
    }

    protected void loadSchoolList(Student student, String city, FollowUpCommand fupCommand) {
        State state = student.getState();
        Grade grade = student.getGrade();
        if (grade != null) {
            List<School> schools = _schoolDao.findSchoolsInCityByGrade(state, city, grade);
            School school = new School();
            school.setId(-1);
            school.setName("My child's school is not listed");
            schools.add(0, school);
            fupCommand.addSchools(schools);
        } else {
            fupCommand.addSchools(new ArrayList<School>());
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
        for (int x=0; x < user.getUserProfile().getNumSchoolChildren(); x++) {
            Student student = fupCommand.getStudents().get(x);
            if (student.getGrade() == null) {
                errors.rejectValue("students[" + x + "]", null, ERROR_GRADE_MISSING);
            }
            School school = null;
            if (student.getSchoolId() != null && student.getSchoolId() != -1) {
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
            } else if (student.getSchoolId() != null && student.getSchoolId() == -1) {
                fupCommand.addSchoolName("My child's school is not listed");
            } else {
                // to avoid index out of bounds exceptions, we have to add something to the list
                fupCommand.addSchoolName("");
            }
        }

        if (!fupCommand.getTerms()) {
            errors.rejectValue("terms", null, ERROR_TERMS);
        }
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

        // for repeat submits of this page (useful for debugging) don't do soap request
        if (user.isEmailProvisional()) {
            // only notify community on final step
            String password = user.getPasswordMd5().substring
                    (0, user.getPasswordMd5().indexOf(User.EMAIL_PROVISIONAL_PREFIX));
            CreateOrUpdateUserRequestBean bean = new CreateOrUpdateUserRequestBean
                    (user.getId(), existingProfile.getScreenName(), user.getEmail(), password);
            CreateOrUpdateUserRequest soapRequest = new CreateOrUpdateUserRequest();
            _log.info(password);
            try {
                soapRequest.createOrUpdateUserRequest(bean);
            } catch (SoapRequestException couure) {
                _log.error("SOAP error - " + couure.getErrorCode() + ": " + couure.getErrorMessage());
                // undo registration
                // the user is already provisional at this point since they haven't agreed to the terms
                // send to error page
                mAndV.setViewName(getErrorView());
                return mAndV; // early exit!
            }
        }

        if (user.getStudents() != null) {
            user.getStudents().clear();
        }
        _contactSubs = new HashSet<String>();
        deleteSubscriptionsForProduct(user, SubscriptionProduct.PARENT_CONTACT);
        if (existingProfile.getNumSchoolChildren() > 0) {
            for (Student student: fupCommand.getStudents()) {
                if ("y".equals(fupCommand.getRecontact())) {
                    addContactSubscriptionFromStudent(student, user);
                }
                if (student.getSchoolId() != null && student.getSchoolId() == -1) {
                    student.setSchoolId(null);
                }
                user.addStudent(student);
            }
        }
        deleteSubscriptionsForProduct(user, SubscriptionProduct.COMMUNITY);
        if (fupCommand.getNewsletter()) {
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setProduct(SubscriptionProduct.COMMUNITY);
            subscription.setState(fupCommand.getUserProfile().getState());
            fupCommand.addSubscription(subscription);
        }
        if (fupCommand.isBeta()) {
            if (_subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.BETA_GROUP) == null) {
                Subscription betaSubscription = new Subscription();
                betaSubscription.setUser(user);
                betaSubscription.setProduct(SubscriptionProduct.BETA_GROUP);
                betaSubscription.setState(fupCommand.getUserProfile().getState());
                _subscriptionDao.saveSubscription(betaSubscription);
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
        //PageHelper.setMemberCookie(request, response, user);
        if (StringUtils.isEmpty(fupCommand.getRedirect())) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null, null);
            builder.addParameter("message", "Thank you for joining the GreatSchools Community! You'll be the first to know when we launch!");
            fupCommand.setRedirect(builder.asFullUrl(request));
        }
        mAndV.setViewName("redirect:" + fupCommand.getRedirect());

        return mAndV;
    }

    private void saveSubscriptionsForUser(FollowUpCommand fupCommand, User user) {
        List<Subscription> newsSubs = new ArrayList<Subscription>();
        for (Subscription sub: fupCommand.getSubscriptions()) {
            sub.setUser(user);
            if (sub.getProduct().isNewsletter()) {
                newsSubs.add(sub);
            } else {
                _subscriptionDao.saveSubscription(sub);
            }
            if (Boolean.valueOf(fupCommand.getRecontact())) {
                addContactSubscriptionFromSubscription(sub);
            }
        }
        if (!newsSubs.isEmpty()) {
            _subscriptionDao.addNewsletterSubscriptions(user, newsSubs);
        }
    }

    private void deleteSubscriptionsForProduct(User user, SubscriptionProduct product) {
        List<Subscription> oldSubs = _subscriptionDao.getUserSubscriptions(user, product);
        if (oldSubs != null) {
            for (Subscription oldSub : oldSubs) {
                _subscriptionDao.removeSubscription(oldSub.getId());
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
            addContactSubscription(user, student.getSchoolId(), student.getState());
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

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
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

    public String getErrorView() {
        return _errorView;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }
}
