package gs.web.community.registration.popup;

import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.state.State;
import gs.web.community.registration.*;
import gs.web.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.community.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/community/registration/popup")
public class RegistrationHoverController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ExactTargetAPI _exactTargetAPI;
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;
    private String _errorView = "/community/registration/registrationSoapError";
    private String _formView = "/community/registration/popup/registrationHover";
    private IUserDao _userDao;
    private EmailVerificationEmail _emailVerificationEmail;
    private ISubscriptionDao _subscriptionDao;
    private JavaMailSender _mailSender;

    @RequestMapping(value = "/mssRegistrationHover.page", method = RequestMethod.POST)
    public ModelAndView onMssSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 MssRegistrationHoverCommand registrationHoverCommand,
                                 BindingResult errors) throws Exception {
        // Need to check if user's IP is blocked
        if (_userRegistrationOrLoginService.isIPBlocked(request)) return new ModelAndView(getErrorView());

        ModelAndView mAndV = new ModelAndView();
        String hoverToShow;

        // Subscriptions need to be tracked in omniture
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);

        // We need to know if a user exists, and if so whether their email is verified
        User user = getUserDao().findUserFromEmailIfExists(registrationHoverCommand.getEmail());

        // Determine if we should send a confirmation email. In logic before r232, this could never be false
        boolean shouldSendConfirmationEmail = shouldSendConfirmationEmailForMss(user, user != null);

        // Get a basic RegistrationBehavior
        RegistrationOrLoginBehavior registrationBehavior = createRegistrationBehavior(registrationHoverCommand);

        // MSS accounts never get the standard welcome email
        registrationBehavior.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);

        if (shouldSendConfirmationEmail) {
            // if we're confirming their subscription and not requiring verification, show them a confirmation hover too
            hoverToShow = "subscriptionEmailValidated";
        } else {
            // if they don't get a confirmation email, then they get a verification email instead
            registrationBehavior.setSendVerificationEmail(true);
            registrationBehavior.setRedirectUrl(calculateRedirectUrlForValidationEmail(registrationHoverCommand, request));

            // If we send a verification email, show them a hover telling them so
            hoverToShow = "validateEmail";
        }

        UserRegistrationOrLoginService.Summary registrationSummary =
            _userRegistrationOrLoginService.registerUser(registrationHoverCommand, registrationBehavior, errors, request);

        if (registrationSummary != null) {
            user = registrationSummary.getUser();

            try {
                saveRegistrations(registrationHoverCommand, user, ot);
            } catch (Exception e) {
                _log.error("Error in RegistrationHoverController", e);
                mAndV.setViewName(getErrorView());
                return mAndV;
            }

            if (shouldSendConfirmationEmail) {
                // send MSS welcome email
                sendEmailSubscriptionWelcomeEmail(
                    user, registrationHoverCommand.getNewsletter(), registrationHoverCommand.getPartnerNewsletter()
                );
            }

            // Set "showHover" in our SitePrefCookie, so that the user will see a hover on next page load
            SitePrefCookie cookie = new SitePrefCookie(request, response);
            cookie.setProperty("showHover", hoverToShow);
        }

        String redirect = registrationHoverCommand.getRedirectUrl();
        mAndV.setViewName("redirect:" + redirect);

        return mAndV;
    }

    @RequestMapping(value = "/registrationHover.page", method = RequestMethod.POST)
    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 RegistrationHoverCommand registrationHoverController,
                                 BindingResult errors) throws Exception {
        // Need to check if user's IP is blocked
        if (_userRegistrationOrLoginService.isIPBlocked(request)) return new ModelAndView(getErrorView());

        User user;
        ModelAndView mAndV = new ModelAndView();

        // Registration and subscriptions need to be tracked in omniture
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);

        // Get a basic RegistrationBehavior
        RegistrationOrLoginBehavior registrationBehavior = createRegistrationBehavior(registrationHoverController);

        // Don't send a welcome email now. For new users a welcome email will get sent when they verify their account
        registrationBehavior.setWelcomeMessageStatus(WelcomeMessageStatus.DO_NOT_SEND);

        UserRegistrationOrLoginService.Summary registrationSummary =
            _userRegistrationOrLoginService.registerUser(registrationHoverController, registrationBehavior, errors, request);

        if (registrationSummary != null) {
            user = registrationSummary.getUser();

            try {
                saveRegistrations(registrationHoverController, user, ot);
            } catch (Exception e) {
                _log.error("Error in RegistrationHoverController", e);
                mAndV.setViewName(getErrorView());
                return mAndV;
            }

            // Determine redirect URL for validation email
            String emailRedirectUrl = calculateRedirectUrlForValidationEmail(registrationHoverController, request);

            sendValidationEmail(
                request,
                user,
                emailRedirectUrl,
                RegistrationHoverCommand.JoinHoverType.SchoolReview == registrationHoverController.getJoinHoverType()
            );

            // Omniture tracking of registration
            if (registrationSummary.wasUserRegistered()){
                ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);
            }

            // Set "showHover" in our SitePrefCookie, so that the user will see a hover on next page load
            SitePrefCookie cookie = new SitePrefCookie(request, response);
            String hoverToShow = calculateHoverToShow(registrationHoverController);
            cookie.setProperty("showHover", hoverToShow);
        }

        String redirect = registrationHoverController.getRedirectUrl();
        mAndV.setViewName("redirect:" + redirect);

        return mAndV;
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

    protected RegistrationOrLoginBehavior createRegistrationBehavior(RegistrationHoverCommand userCommand) {
        RegistrationOrLoginBehavior behavior = new RegistrationOrLoginBehavior();
        behavior.setHow(userCommand.getHow());

        // This controller will handle sending confirmation and verification emails
        behavior.setSendVerificationEmail(false);

        return behavior;
    }

    protected String calculateRedirectUrlForValidationEmail(RegistrationHoverCommand userCommand, HttpServletRequest request) {
        // Determine redirect URL for validation email
        String emailRedirectUrl;

        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            emailRedirectUrl = "/index.page";
        } else {
            emailRedirectUrl = "/";
        }

        // calculate the redirect URL
        if (RegistrationHoverCommand.JoinHoverType.MSL == userCommand.getJoinHoverType()) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
            emailRedirectUrl = urlBuilder.asSiteRelative(request);
        }

        return emailRedirectUrl;
    }

    protected boolean shouldSendConfirmationEmailForMss(User user, boolean userAlreadyExisted) {

        // determine whether to use email verification
        boolean shouldSendConfirmationEmail = (
            !userAlreadyExisted || Boolean.TRUE.equals(user.getEmailVerified()) || user.isEmailValidated()
        );

        return shouldSendConfirmationEmail;
    }

    protected String calculateHoverToShow(RegistrationHoverCommand userCommand) {

        if (RegistrationHoverCommand.JoinHoverType.SchoolReview.equals(userCommand.getJoinHoverType())) {
            return "validateEmailSchoolReview";
        } else {
            return "validateEmail";
        }
    }

    private void saveRegistrations(RegistrationHoverCommand userCommand, User user, OmnitureTracking ot) {
        // TODO: I switched the ternary values since state was resulting to null.
        // TODO: Why was it not causing a problem before?
        State state = userCommand.getState() == null ? State.CA : userCommand.getState();

        List<Subscription> subscriptions = new ArrayList<Subscription>();

        List<RegistrationHoverCommand.NthGraderSubscription> nthGraderSubscriptions = userCommand.getGradeNewsletters();
        if (nthGraderSubscriptions != null) {
            _log.info("nthGraderSubscriptions.size()=" + nthGraderSubscriptions.size());
            for (RegistrationHoverCommand.NthGraderSubscription sub : nthGraderSubscriptions) {
                Student student = new Student();
                student.setSchoolId(-1);
                student.setGrade(sub.getSubProduct().getGrade());
                student.setState(state);
                user.addStudent(student);
            }
            getUserDao().updateUser(user);
        }

        if (userCommand.getJoinHoverType() == RegistrationHoverCommand.JoinHoverType.Auto
                && userCommand.getMystatSchoolId() > 0) {
            addMssSubscription(userCommand, user, subscriptions);
        }

        if (userCommand.getNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.PARENT_ADVISOR, state));
            if (userCommand.getJoinHoverType() == RegistrationHoverCommand.JoinHoverType.SchoolReview
                    && userCommand.getMystatSchoolId() > 0) {
                addMssSubscription(userCommand, user, subscriptions);
            }
        }

        if (userCommand.getPartnerNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("sponsor"), state));
        }

        if (subscriptions.size() > 0) {
            getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ot);
        }
    }

    private void addMssSubscription(RegistrationHoverCommand userCommand, User user, List<Subscription> subscriptions) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setProduct(SubscriptionProduct.MYSTAT);
        sub.setState(userCommand.getMystatSchoolState());
        sub.setSchoolId(userCommand.getMystatSchoolId());
        subscriptions.add(sub);
    }

    private void sendEmailSubscriptionWelcomeEmail(User user,
                                                   boolean addedParentAdvisorSubscription,
                                                   boolean addedSponsorOptInSubscription) {
        Map<String, String> attributes = ExactTargetUtil.getEmailSubWelcomeAttributes(
                ExactTargetUtil.getEmailSubWelcomeParamValue(addedParentAdvisorSubscription, false, true, addedSponsorOptInSubscription));
        _exactTargetAPI.sendTriggeredEmail(ExactTargetUtil.EMAIL_SUB_WELCOME_TRIGGER_KEY, user, attributes);
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public void setUserRegistrationOrLoginService(UserRegistrationOrLoginService userRegistrationOrLoginService) {
        _userRegistrationOrLoginService = userRegistrationOrLoginService;
    }

    public String getErrorView() {
        return _errorView;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public UserRegistrationOrLoginService getUserRegistrationOrLoginService() {
        return _userRegistrationOrLoginService;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
