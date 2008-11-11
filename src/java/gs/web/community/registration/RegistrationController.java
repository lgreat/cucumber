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
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureSuccessEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

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
    private AuthenticationManager _authenticationManager;
    private CreateOrUpdateUserRequest _soapRequest;
    public static final String NEWSLETTER_PARAMETER = "newsletterStr";
    public static final String TERMS_PARAMETER = "termsStr";
    public static final String BETA_PARAMETER = "betaStr";
    public static final String SPREADSHEET_ID_FIELD = "ip";

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        UserCommand userCommand = (UserCommand) command;
        userCommand.setRedirectUrl(request.getParameter("redirect"));
        loadCityList(request, userCommand);

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

    public void onBind(HttpServletRequest request, Object command) {
        UserCommand userCommand = (UserCommand) command;
        userCommand.setCity(request.getParameter("city"));
        String terms = request.getParameter(TERMS_PARAMETER);
        userCommand.setTerms("on".equals(terms));
        String newsletter = request.getParameter(NEWSLETTER_PARAMETER);
        userCommand.setNewsletter("on".equals(newsletter));
        String beta = request.getParameter(BETA_PARAMETER);
        userCommand.setBeta("on".equals(beta));

        loadCityList(request, userCommand);
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
        List<City> cities = _geoDao.findCitiesByState(state);
        City city = new City();
        city.setName("My city is not listed");
        cities.add(0, city);
        userCommand.setCityList(cities);
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

        // First, check to see if the request is from a blocked IP address. If so,
        // then, log the attempt and show the error view.
        String requestIP = (String)request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        try {
            if (_tableDao.getFirstRowByKey(SPREADSHEET_ID_FIELD, requestIP) != null) {
                _log.warn("Request from blocked IP Address: " + requestIP);
                return new ModelAndView(getErrorView());
            }
        } catch (Exception e) {
            _log.warn("Error checking IP address", e);
        }

        UserCommand userCommand = (UserCommand) command;
        User user = _userDao.findUserFromEmailIfExists(userCommand.getEmail());

        OmnitureSuccessEvent ose = new OmnitureSuccessEvent(request, response);

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

        try {
            user.setPlaintextPassword(userCommand.getPassword());
            if (_requireEmailValidation || userCommand.getNumSchoolChildren() > 0) {
                // mark account as provisional until they complete stage 2
                user.setEmailProvisional(userCommand.getPassword());
            }
            _userDao.updateUser(userCommand.getUser());
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage());
            if (!userExists) {
                // for new users, cancel the account on error
                _userDao.removeUser(user.getId());
            }
            throw e;
        }

        if (_requireEmailValidation) {
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

        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            userProfile = user.getUserProfile();
            userProfile.setCity(userCommand.getCity());
            userProfile.setNumSchoolChildren(userCommand.getNumSchoolChildren());
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setState(userCommand.getState());
        } else {
            // gotten this far, now let's update their user profile
            userProfile = userCommand.getUserProfile();

            userProfile.setUser(user);
            user.setUserProfile(userProfile);

            ose.add(OmnitureSuccessEvent.SuccessEvent.CommunityRegistration);
        }

        if (userProfile.getNumSchoolChildren() == -1) {
            userProfile.setNumSchoolChildren(0);
        }

        _userDao.updateUser(user);

        ModelAndView mAndV = new ModelAndView();

        if (userProfile.getNumSchoolChildren() > 0) {
            mAndV.setViewName(getSuccessView());
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            mAndV.getModel().put("marker", hash);
            if (_requireEmailValidation) {
                mAndV.getModel().put("email", user.getEmail());
            }
            if (StringUtils.isNotEmpty(userCommand.getRedirectUrl())) {
                mAndV.getModel().put("redirect", userCommand.getRedirectUrl());
            }
            // mAndV.getModel().put("followUpCmd", fupCommand);
            mAndV.getModel().put("id", user.getId());
            request.setAttribute("password", userCommand.getPassword());
        } else {
            // only subscribe to newsletter on final step
            if (userCommand.getNewsletter()) {
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
                communityNewsletterSubscription.setState(userCommand.getState());
                subs.add(communityNewsletterSubscription);

                NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ose);
                _subscriptionDao.addNewsletterSubscriptions(user, subs);
            }
            if (userCommand.isBeta()) {
                if (_subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.BETA_GROUP) == null) {
                    Subscription betaSubscription = new Subscription();
                    betaSubscription.setUser(user);
                    betaSubscription.setProduct(SubscriptionProduct.BETA_GROUP);
                    betaSubscription.setState(userCommand.getState());
                    _subscriptionDao.saveSubscription(betaSubscription);
                }
            }
            // only notify community on final step
            try {
                notifyCommunity(user.getId(), userProfile.getScreenName(), user.getEmail(),
                        user.getPasswordMd5(), userProfile.getUpdated(), request);
            } catch (SoapRequestException couure) {
                _log.error("SOAP error - " + couure.getErrorCode() + ": " + couure.getErrorMessage());
                // undo registration
                user.setEmailProvisional(userCommand.getPassword());
                _userDao.updateUser(user);
                // send to error page
                mAndV.setViewName(getErrorView());
                return mAndV; // early exit!
            }

            if (!user.isEmailProvisional()) {
                try {
                    // registration is done, let's send a confirmation email
                    _registrationConfirmationEmail.sendToUser(user, userCommand.getPassword(), request);
                } catch (Exception ex) {
                    _log.error("Error sending community registration confirmation email to " +
                            user);
                    _log.error(ex);
                }
            }
            PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
            UrlUtil urlUtil = new UrlUtil();
            if (StringUtils.isEmpty(userCommand.getRedirectUrl()) ||
                    !urlUtil.isCommunityContentLink(userCommand.getRedirectUrl())) {
                String redirectUrl = "http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/members/" + user.getUserProfile().getScreenName() + "/profile/interests?registration=1"; 
                userCommand.setRedirectUrl(redirectUrl);
            }
            mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());
        }

        return mAndV;
    }

    protected void notifyCommunity(Integer userId, String screenName, String email, String password,
                                   Date dateCreated,
                                   HttpServletRequest request) throws SoapRequestException {
        String requestIP = (String)request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
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

    public AuthenticationManager getAuthenticationManager() {
        return _authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        _authenticationManager = authenticationManager;
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
}
