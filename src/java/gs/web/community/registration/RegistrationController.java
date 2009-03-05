package gs.web.community.registration;

import gs.data.community.*;
import gs.data.util.DigestUtil;
import gs.data.util.table.ITableDao;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.State;
import gs.data.soap.SoapRequestException;
import gs.data.soap.CreateOrUpdateUserRequestBean;
import gs.data.soap.CreateOrUpdateUserRequest;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:aroy@urbanasoft.com">Anthony Roy</a>
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ITableDao _tableDao;
    private ISubscriptionDao _subscriptionDao;
    private JavaMailSender _mailSender;
    private RegistrationConfirmationEmail _registrationConfirmationEmail;
    private boolean _requireEmailValidation = true;
    private String _errorView;
    private CreateOrUpdateUserRequest _soapRequest;
    private boolean _chooserRegistration;
    public static final String NEWSLETTER_PARAMETER = "newsletterStr";
    public static final String PARENT_ADVISOR_NEWSLETTER = "parentAdvisorNewsletter";
    public static final String TERMS_PARAMETER = "termsStr";
    public static final String BETA_PARAMETER = "betaStr";
    public static final String SPREADSHEET_ID_FIELD = "ip";

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        UserCommand userCommand = (UserCommand) command;
        userCommand.setRedirectUrl(request.getParameter("redirect"));
        if (isChooserRegistration()) {
            loadSchoolChoiceCityList(request, userCommand);
            setupChooserRegistration(userCommand);
        } else {
            loadCityList(request, userCommand);
        }

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
                if (request.getParameter("reset") != null &&
                        request.getParameter("reset").equals("true") &&
                        user.isEmailProvisional()) {
                    // reset provisional status
                    user.setPasswordMd5(null);
                    _userDao.updateUser(user);
                }
            }
        }
    }

    private void setupChooserRegistration(UserCommand userCommand) {
        // set up defaults for data not collected in chooser registration
        userCommand.setChooserRegistration(true);
        userCommand.setNewsletter(false);
        userCommand.setBeta(false);
        userCommand.setGender("u");
        userCommand.setNumSchoolChildren(0);
    }

    public void onBind(HttpServletRequest request, Object command) {
        UserCommand userCommand = (UserCommand) command;
        if (isChooserRegistration()) {
            if (StringUtils.isNotBlank(request.getParameter("schoolChoiceState"))) {
                State state = State.fromString(request.getParameter("schoolChoiceState"));
                userCommand.setSchoolChoiceState(state);
            }
            userCommand.setSchoolChoiceCity(request.getParameter("schoolChoiceCity"));
            loadSchoolChoiceCityList(request, userCommand);
            // need to call this so that userCommand.isChooserRegistration() == true during validation
            setupChooserRegistration(userCommand);
        } else {
            userCommand.setCity(request.getParameter("city"));
            loadCityList(request, userCommand);
        }

        String parentAdvisorNewsletter = request.getParameter(PARENT_ADVISOR_NEWSLETTER);
        userCommand.setParentAdvisorNewsletter("on".equals(parentAdvisorNewsletter));
        String terms = request.getParameter(TERMS_PARAMETER);
        userCommand.setTerms("on".equals(terms));
        String newsletter = request.getParameter(NEWSLETTER_PARAMETER);
        userCommand.setNewsletter("on".equals(newsletter));
        String beta = request.getParameter(BETA_PARAMETER);
        userCommand.setBeta("on".equals(beta));
    }

    protected void loadCityList(HttpServletRequest request, UserCommand userCommand) {
        loadCityListHelper(request, userCommand, false);
    }

    protected void loadSchoolChoiceCityList(HttpServletRequest request, UserCommand userCommand) {
        loadCityListHelper(request, userCommand, true);
    }

    protected void loadCityListHelper(HttpServletRequest request, UserCommand userCommand, boolean isSchoolChoiceLocation) {
        State state = (isSchoolChoiceLocation ? userCommand.getSchoolChoiceState() : userCommand.getState());
        if (state == null) {
            if (SessionContextUtil.getSessionContext(request).getCity() != null) {
                City userCity = SessionContextUtil.getSessionContext(request).getCity();
                state = userCity.getState();
                SessionContextUtil.getSessionContext(request).setState(state);
                if (isSchoolChoiceLocation) {
                    userCommand.setSchoolChoiceCity(userCity.getName());
                    userCommand.setSchoolChoiceState(state);
                } else {
                    userCommand.setCity(userCity.getName());
                }

            } else {
                if (isSchoolChoiceLocation) {
                    state = SessionContextUtil.getSessionContext(request).getState();
                } else {
                    state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
                }
            }
        }

        if (state != null) {
            List<City> cities = _geoDao.findCitiesByState(state);
            City city = new City();
            city.setName("My city is not listed");
            cities.add(0, city);
            if (isSchoolChoiceLocation) {
                userCommand.setSchoolChoiceCityList(cities);
            } else {
                userCommand.setCityList(cities);
            }
        }
    }

    public void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBindAndValidate(request, command, errors);
        UserCommandValidator validator = new UserCommandValidator();
        validator.setUserDao(_userDao);
        validator.validate(request, command, errors);
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;
        User user = _userDao.findUserFromEmailIfExists(userCommand.getEmail());
        ModelAndView mAndV = new ModelAndView();
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
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
            _userDao.saveUser(userCommand.getUser());
            user = userCommand.getUser();
        }

        setUsersPassword(user, userCommand, userExists);

        if (_requireEmailValidation) {
            sendValidationEmail(user, userCommand, userExists, request);
        }

        UserProfile userProfile = updateUserProfile(user, userCommand, ot);

        // save
        _userDao.updateUser(user);
        // Because of hibernate caching, it's possible for a list_active record
        // (with list_member id) to be commited before the list_member record is
        // committed. Adding this commitOrRollback prevents this.
        ThreadLocalTransactionManager.commitOrRollback();

        if (userProfile.getNumSchoolChildren() != null && userProfile.getNumSchoolChildren() > 0) {
            // send to page 2
            mAndV.setViewName(getSuccessView());
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            mAndV.getModel().put("marker", hash);
            if (_requireEmailValidation) {
                mAndV.getModel().put("email", user.getEmail());
            }
            if (StringUtils.isNotEmpty(userCommand.getRedirectUrl())) {
                mAndV.getModel().put("redirect", userCommand.getRedirectUrl());
            }
            mAndV.getModel().put("id", user.getId());
            request.setAttribute("password", userCommand.getPassword());
        } else {
            // complete registration
            // if a user registers for the community through the hover and selects the Parent advisor newsletter subscription - Jira -7968
            if (userCommand.getNewsletter() || (isChooserRegistration() && (userCommand.getParentAdvisorNewsletter() == true))) {
                processNewsletterSubscriptions(user, userCommand, ot);
            }
            if (userCommand.isBeta()) {
                subscribeToBetaGroup(user, userCommand);
            }
            if (!notifyCommunity(user, userCommand, mAndV, request)) {
                return mAndV; // early exit!
            }
            if (!user.isEmailProvisional()) {
                if (!isChooserRegistration()) {
                    sendConfirmationEmail(user, userCommand, request);
                }
            }

            // if a user registers for the community through the hover and selects the Parent advisor newsletter subscription
            // and even if this is their first subscription no do send the NL welcome email. -Jira -7968
            if(isChooserRegistration() && (userCommand.getParentAdvisorNewsletter() == true)){
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.updateUser(user);
            }

            PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
            if (!isChooserRegistration() && (StringUtils.isEmpty(userCommand.getRedirectUrl()) ||
                    !UrlUtil.isCommunityContentLink(userCommand.getRedirectUrl()))) {
                String redirectUrl = "http://" +
                        SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                        "/members/" + user.getUserProfile().getScreenName() + "/profile/interests?registration=1";
                userCommand.setRedirectUrl(redirectUrl);
            }
            mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());
        }

        return mAndV;
    }

    protected void sendConfirmationEmail(User user, UserCommand userCommand, HttpServletRequest request) {
        try {
            // registration is done, let's send a confirmation email
            _registrationConfirmationEmail.sendToUser(user, userCommand.getPassword(), request);
        } catch (Exception ex) {
            _log.error("Error sending community registration confirmation email to " +
                    user);
            _log.error(ex);
        }
    }

    protected boolean notifyCommunity(User user, UserCommand userCommand, ModelAndView mAndV, HttpServletRequest request) {
        // only notify community on final step
        try {
            notifyCommunity(user.getId(), user.getUserProfile().getScreenName(), user.getEmail(),
                    user.getPasswordMd5(), user.getUserProfile().getUpdated(), request);
        } catch (SoapRequestException couure) {
            _log.error("SOAP error - " + couure.getErrorCode() + ": " + couure.getErrorMessage());
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            _userDao.updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return false;
        }
        return true;
    }

    protected void subscribeToBetaGroup(User user, UserCommand userCommand) {
        if (_subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.BETA_GROUP) == null) {
            Subscription betaSubscription = new Subscription();
            betaSubscription.setUser(user);
            betaSubscription.setProduct(SubscriptionProduct.BETA_GROUP);
            betaSubscription.setState(userCommand.getState());
            _subscriptionDao.saveSubscription(betaSubscription);
        }
    }

    protected void processNewsletterSubscriptions(User user, UserCommand userCommand, OmnitureTracking ot) {
        List<Subscription> subs = new ArrayList<Subscription>();
        // GS-7479 Swap out BoC newsletter with Parent Advisor
        // subscribe to three newsletters
        // best of community
//                Subscription communityNewsletterSubscription = new Subscription();
//                communityNewsletterSubscription.setUser(user);
//                communityNewsletterSubscription.setProduct(SubscriptionProduct.COMMUNITY);
//                communityNewsletterSubscription.setState(userCommand.getState());
//                subs.add(communityNewsletterSubscription);
        // best of city
//                communityNewsletterSubscription = new Subscription();
//                communityNewsletterSubscription.setUser(user);
//                communityNewsletterSubscription.setProduct(SubscriptionProduct.CITY_COMMUNITY);
//                communityNewsletterSubscription.setState(userCommand.getState());
//                subs.add(communityNewsletterSubscription);
        // best of school
//                communityNewsletterSubscription = new Subscription();
//                communityNewsletterSubscription.setUser(user);
//                communityNewsletterSubscription.setProduct(SubscriptionProduct.SCHOOL_COMMUNITY);
//                communityNewsletterSubscription.setState(userCommand.getState());
//                subs.add(communityNewsletterSubscription);

        Subscription communityNewsletterSubscription = new Subscription();
        communityNewsletterSubscription.setUser(user);
        communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        // When a user registers through a hover then the state and city field are null for that user
        //instead we use schoolChoiceState and schoolChoiceCity fields.Therefore the if and else block below. Jira - 7915 and 7968(Parent Advisor Newsletter)
        if(userCommand.getState() != null){
            communityNewsletterSubscription.setState(userCommand.getState());
        }
        else if(userCommand.getSchoolChoiceState() != null)
        {
            communityNewsletterSubscription.setState(userCommand.getSchoolChoiceState());
        }

        subs.add(communityNewsletterSubscription);

        NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ot);
        System.out.println("------------------------"+_subscriptionDao.getUserSubscriptions(user)+"-------------------");
        _subscriptionDao.addNewsletterSubscriptions(user, subs);
    }

    protected UserProfile updateUserProfile(User user, UserCommand userCommand, OmnitureTracking ot) {
        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            // note: this code is not reached during Chooser Registration
            userProfile = user.getUserProfile();
            userProfile.setNumSchoolChildren(userCommand.getNumSchoolChildren());
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setCity(userCommand.getCity());
            userProfile.setState(userCommand.getState());
        } else {
            // gotten this far, now let's update their user profile
            userProfile = userCommand.getUserProfile();

            userProfile.setUser(user);
            user.setUserProfile(userProfile);

            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);
            if (isChooserRegistration()) {
                user.getUserProfile().setHow("chooser");
                ot.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.RegistrationSegment, "Chooser Reg"));
            }
        }
        user.getUserProfile().setUpdated(new Date());
        if (userProfile.getNumSchoolChildren() == -1) {
            userProfile.setNumSchoolChildren(0);
        }
        return userProfile;
    }

    protected void sendValidationEmail(User user, UserCommand userCommand, boolean userExists, HttpServletRequest request) throws NoSuchAlgorithmException, MessagingException {
        MimeMessage mm = RequestEmailValidationController.buildMultipartEmail
                (_mailSender.createMimeMessage(), request, userCommand);
        try {
            _mailSender.send(mm);
        } catch (MailException me) {
            _log.error("Error sending email message.", me);
            if (userExists) {
                // for existing users, set them back to no password
                user.setPasswordMd5(null);
                _userDao.updateUser(user);
            } else {
                // for new users, cancel the account on error
                _userDao.removeUser(user.getId());
            }
            throw me;
        }
    }

    protected void setUsersPassword(User user, UserCommand userCommand, boolean userExists) throws Exception {
        try {
            user.setPlaintextPassword(userCommand.getPassword());
            if (_requireEmailValidation ||
                    (userCommand.getNumSchoolChildren() != null && userCommand.getNumSchoolChildren() > 0)) {
                // mark account as provisional until they complete stage 2
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

    protected void notifyCommunity(Integer userId, String screenName, String email, String password,
                                   Date dateCreated,
                                   HttpServletRequest request) throws SoapRequestException {
        String requestIP = (String) request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        CreateOrUpdateUserRequestBean bean = new CreateOrUpdateUserRequestBean
                (userId, screenName, email, password, dateCreated, requestIP);
        CreateOrUpdateUserRequest soapRequest = getSoapRequest();
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            soapRequest.setTarget("http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/soap/user");
        }
        soapRequest.createOrUpdateUserRequest(bean);
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

    public RegistrationConfirmationEmail getRegistrationConfirmationEmail() {
        return _registrationConfirmationEmail;
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

    public String getErrorView() {
        return _errorView;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }

    public void setRegistrationConfirmationEmail(RegistrationConfirmationEmail registrationConfirmationEmail) {
        _registrationConfirmationEmail = registrationConfirmationEmail;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    // this eases unit testing by allowing this to be mocked out
    public CreateOrUpdateUserRequest getSoapRequest() {
        if (_soapRequest == null) {
            _soapRequest = new CreateOrUpdateUserRequest();
        }
        return _soapRequest;
    }

    public void setSoapRequest(CreateOrUpdateUserRequest soapRequest) {
        _soapRequest = soapRequest;
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
}
