package gs.web.community.registration.popup;

import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.state.State;
import gs.web.community.registration.*;
import gs.web.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.community.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverController extends RegistrationController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    private boolean _requireEmailValidation = true;
    private ExactTargetAPI _exactTargetAPI;
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;

    public static final String BEAN_ID = "/community/registration/popup/registrationHover.page";

    public void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;

        String[] gradeNewsletters = request.getParameterValues("grades");
        _log.info("gradeNewsletters=" + gradeNewsletters);
        if (gradeNewsletters != null) {
            List<RegistrationHoverCommand.NthGraderSubscription> nthGraderSubscriptions = new ArrayList<RegistrationHoverCommand.NthGraderSubscription>();

            for (String grade : gradeNewsletters) {
                _log.info("Adding " + grade + " to nthGraderSubscriptions");
                nthGraderSubscriptions.add(new RegistrationHoverCommand.NthGraderSubscription(true, SubscriptionProduct.getSubscriptionProduct(grade)));
            }

            userCommand.setGradeNewsletters(nthGraderSubscriptions);
        }

        if (StringUtils.equals("Loading...", userCommand.getCity())) {
            userCommand.setCity(null);
        }

        userCommand.setHow(userCommand.joinTypeToHow());
        userCommand.setTerms(true); // Users agree to terms of use just by submitting new join hover
    }

    @Override
    public void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;
        boolean isMssJoin = (RegistrationHoverCommand.JoinHoverType.Auto == userCommand.getJoinHoverType());

        /*if (isMssJoin) {
            UserCommandValidator validator = new UserCommandValidator();
            validator.validateEmailBasic(userCommand, errors);
        } else {
            super.onBindAndValidate(request, command, errors);
        }*/
        // TODO: Make sure UserRegistrationOrLoginService validates only email if isMssJoin, otherwise all
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        // Need to check if user's IP is blocked
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        ModelAndView mAndV = new ModelAndView();
        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;

        // Registration needs to be tracked in omniture
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);

        // Look for existing user with provided email
        // TODO: we could set userAlreadyExisted = registrationSummary.wasUserRegistered, except that the service
        // sets that true if existing user has no password (email only user). Summary object could have additional
        // Flags to indicate more details of what kind of user created
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        boolean userAlreadyExisted = user != null;

        RegistrationOrLoginBehavior registrationBehavior = createRegistrationBehavior(userCommand);

        UserRegistrationOrLoginService.Summary registrationSummary =
            _userRegistrationOrLoginService.registerUser(userCommand, registrationBehavior, errors, request);

        // Users who get here from the MSS hover should have email validated with regex / library and
        // set to true and welcome message set to NEVER_SEND
        // User's "email verified" flag set true
        // Users who get here from the MSS hover should not have omniture tracking
        // Users who get here OTHER than from MSS hover should have omniture tracking when new user profile is created:
        //   ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);

        if (registrationSummary != null) {
            user = registrationSummary.getUser();

            try {
                saveRegistrations(userCommand, user, ot);
            } catch (Exception e) {
                _log.error("Error in RegistrationHoverController", e);
                mAndV.setViewName(getErrorView());
                return mAndV;
            }

            // determine whether to use email verification
            boolean shouldSendConfirmationEmail = shouldSendConfirmationEmail(
                user, userAlreadyExisted, userCommand
            );

            if (shouldSendConfirmationEmail) {
                sendConfirmationEmail(user, userCommand.getNewsletter(), userCommand.getPartnerNewsletter());
            }

            if (_requireEmailValidation && !shouldSendConfirmationEmail) {
                // Determine redirect URL for validation email
                String emailRedirectUrl = calculateRedirectUrlForValidationEmail(userCommand, request);

                sendValidationEmail(
                    request,
                    user,
                    emailRedirectUrl,
                    RegistrationHoverCommand.JoinHoverType.SchoolReview == userCommand.getJoinHoverType()
                );
            }

            // Set "showHover" in our SitePrefCookie, so that the user will see a hover on next page load
            SitePrefCookie cookie = new SitePrefCookie(request, response);
            String hoverToShow = calculateHoverToShow(user, userAlreadyExisted, userCommand);
            cookie.setProperty("showHover", hoverToShow);
        }

        String redirect = userCommand.getRedirectUrl();
        mAndV.setViewName("redirect:" + redirect);

        return mAndV;
    }

    protected RegistrationOrLoginBehavior createRegistrationBehavior(RegistrationHoverCommand userCommand) {
        RegistrationOrLoginBehavior behavior = new RegistrationOrLoginBehavior();
        behavior.setHow(userCommand.getHow());

        // This controller will handle sending confirmation and verification emails
        behavior.setSendConfirmationEmail(false);
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

    protected boolean shouldSendConfirmationEmail(User user, boolean userAlreadyExisted, RegistrationHoverCommand userCommand) {

        // determine whether to use email verification
        boolean shouldSendConfirmationEmail = (
            userCommand.isMssJoin()
            &&
            (!userAlreadyExisted || Boolean.TRUE.equals(user.getEmailVerified()) || user.isEmailValidated())
        );

        return shouldSendConfirmationEmail;
    }

    protected String calculateHoverToShow(User user, boolean userAlreadyExisted, RegistrationHoverCommand userCommand) {

        if (RegistrationHoverCommand.JoinHoverType.SchoolReview.equals(userCommand.getJoinHoverType())) {
            return "validateEmailSchoolReview";
        } else if (userCommand.isMssJoin() && !shouldSendConfirmationEmail(user, userAlreadyExisted, userCommand)) {
            return "subscriptionEmailValidated";
        } else {
            return "validateEmail";
        }
    }

    private void saveRegistrations(RegistrationHoverCommand userCommand, User user, OmnitureTracking ot) {
        // TODO: I switched the ternary values since state was resulting null.
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

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        _requireEmailValidation = requireEmailValidation;
    }

    private void sendConfirmationEmail(User user, boolean addedParentAdvisorSubscription, boolean addedSponsorOptInSubscription) {
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

}