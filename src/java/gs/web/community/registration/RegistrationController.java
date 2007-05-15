package gs.web.community.registration;

import gs.data.community.*;
import gs.data.util.DigestUtil;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.State;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.context.SessionContextUtil;
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

/**
 * @author <a href="mailto:aroy@urbanasoft.com">Anthony Roy</a>
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ISubscriptionDao _subscriptionDao;
    private JavaMailSender _mailSender;
    private RegistrationConfirmationEmail _registrationConfirmationEmail;
    private boolean _requireEmailValidation = true;
    private String _completeView;
    private AuthenticationManager _authenticationManager;
    public static final String NEWSLETTER_PARAMETER = "newsletterStr";
    public static final String TERMS_PARAMETER = "termsStr";

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
        if (terms != null) {
            userCommand.setTerms(terms.equals("y"));
        }
        String newsletter = request.getParameter(NEWSLETTER_PARAMETER);
        userCommand.setNewsletter("y".equals(newsletter));
        loadCityList(request, userCommand);
    }

    protected void loadCityList(HttpServletRequest request, UserCommand userCommand) {
        State state = userCommand.getState();
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        List cities = _geoDao.findCitiesByState(state);
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
        UserCommand userCommand = (UserCommand) command;
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
                if (gender.equals("m") || gender.equals("f")) {
                    user.setGender(userCommand.getGender());
                }
            }
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            _userDao.saveUser(userCommand.getUser());
            user = userCommand.getUser();
        }

        try {
            user.setPlaintextPassword(userCommand.getPassword());
            if (_requireEmailValidation || request.getParameter("join") == null) {
                // TODO: mark account as provisional until they complete stage 2
                // mark password as unauthenticated
                user.setEmailProvisional();
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
        }

        if (userProfile.getNumSchoolChildren().intValue() == -1) {
            userProfile.setNumSchoolChildren(new Integer(0));
        }

        _userDao.updateUser(user);

        ModelAndView mAndV = new ModelAndView();

        if (request.getParameter("join") == null) {
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
        } else {
            // only subscribe to newsletter on final step
            if (userCommand.getNewsletter()) {
                Subscription communityNewsletterSubscription = new Subscription();
                communityNewsletterSubscription.setUser(user);
                communityNewsletterSubscription.setProduct(SubscriptionProduct.COMMUNITY);
                communityNewsletterSubscription.setState(userCommand.getState());
                _subscriptionDao.saveSubscription(communityNewsletterSubscription);
            }

            if (!user.isEmailProvisional()) {
                try {
                    // registration is done, let's send a confirmation email
                    _registrationConfirmationEmail.sendToUser(user, request);
                } catch (Exception ex) {
                    _log.error("Error sending community registration confirmation email to " +
                            user);
                    _log.error(ex);
                }
            }
            //PageHelper.setMemberAuthorized(request, response, user);
            PageHelper.setMemberCookie(request, response, user);
            //AuthenticationManager.AuthInfo authInfo = _authenticationManager.generateAuthInfo(user);
            if (StringUtils.isEmpty(userCommand.getRedirectUrl())) {
                UrlBuilder builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null, null);
                builder.addParameter("message", "Thank you for joining!");
                userCommand.setRedirectUrl(builder.asFullUrl(request));
            }
            // bounce to webcrossing so they can create user
//            mAndV.setViewName("redirect:" + _authenticationManager.generateRedirectUrl
//                (userCommand.getRedirectUrl(), authInfo));
            mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());
        }

        // generate secure hash so if the followup profile page is submitted, we know who it is
        return mAndV;
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

    public void setCompleteView(String completeView) {
        _completeView = completeView;
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

}
