package gs.web.community.registration;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.security.Role;
import gs.data.state.State;
import gs.data.util.DigestUtil;
import gs.web.community.HoverHelper;
import gs.web.school.EspRegistrationHelper;
import gs.web.school.review.ReviewService;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Final stage in the confirmation process when using email validation.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationConfirmController extends AbstractCommandController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationConfirm.page";
    
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private ReviewService _reviewService;
    private ISubscriptionDao _subscriptionDao;
    private ExactTargetAPI _exactTargetAPI;
    private IEspMembershipDao _espMembershipDao;
    private ISchoolDao _schoolDao;
    private IEspResponseDao _espResponseDao;

    @Autowired
    private EspRegistrationHelper _espRegistrationHelper;

    protected enum UserState {
        EMAIL_ONLY,
        PROVISIONAL,
        REGISTERED;
    }

    protected ModelAndView redirectToRegistration(HttpServletRequest request) {
        return redirectToRegistration(request, null);
    }

    protected ModelAndView redirectToRegistration(HttpServletRequest request, String email) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
        String redirectUrl = builder.asFullUrl(request);
        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName("redirect:" + redirectUrl);
        return mAndV;
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, org.springframework.validation.BindException errors) throws Exception {

        EmailVerificationLinkCommand emailVerificationLinkCommand = (EmailVerificationLinkCommand) command;
        User user = emailVerificationLinkCommand.getUser();

        if (errors.hasFieldErrors("dateSentAsString")) {
            _log.warn("Email verification link has expired, redirecting.");
            if (user != null) {
                return handleExpiredLink(request, response, user);
            } else {
                return redirectToRegistration(request);
            }
        } else if (errors.hasErrors()) {
            _log.warn("Email verification link had errors, redirecting.");
            return redirectToRegistration(request);
        } 

        UserState userState = getUserState(user);
        boolean isEditEmailRequest = (request.getParameter("edit") != null);
        String requestedRedirect = request.getParameter("redirect");
        SitePrefCookie cookie = new SitePrefCookie(request, response);
        HoverHelper hoverHelper = new HoverHelper(cookie); // :(  I'd prefer to provide the request and response rather than cookie
        String viewName = null;
        UrlBuilder urlBuilder;


        /* TODO: figure out if we still need to do this
        if (user.isPasswordEmpty()) {
            // request for new password has expired, redirect to registration
            _log.warn("Community registration request has expired for user " +
                    user.getEmail() + ", redirecting to registration");
            return redirectToRegistration(request, user.getEmail());
        }
        */

        // sorry, to reduce risk this release, I did not modify existing behavior of User setEmailValidated.
        // Instead, a new db field now tracks this, and emailVerified is the new associated bean method. That is why
        // there are two methods which *should* do the same thing.
        user.setEmailVerified(true);

        ReviewService.ReviewUpgradeSummary summary;

        switch (userState) {
            case REGISTERED:
                // already confirmed email, so just sign them in and redirect to /account/
                //or if they are an esp member take them to the dashboard.
                PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
                viewName = "/account/";
                if (user.hasRole(Role.ESP_MEMBER) && StringUtils.isNotBlank(requestedRedirect)) {
                    hoverHelper.setHoverCookie(HoverHelper.Hover.ESP_ACCOUNT_VERIFIED);
                    viewName = requestedRedirect;
                }
                else if ("radar".equals(user.getHow()) && StringUtils.isNotBlank(requestedRedirect)) {
                    viewName = requestedRedirect;
                }

                // upgradeProvisional=true is set when users created review from the Review Landing Page using an email
                // that has a GreatSchools account. When they validate their email (even though they had a GS account)
                // then this code path will execute
                if ("true".equalsIgnoreCase(request.getParameter("upgradeProvisional"))) {
                    summary = getReviewService().upgradeProvisionalReviewsAndSummarize(user);
                    switch (summary.getStatus()) {
                        case REVIEW_UPGRADED_PUBLISHED:
                            sendReviewPostedEmail(request, summary.getFirstPublishedReview());
                            hoverHelper.setHoverCookie(HoverHelper.Hover.SCHOOL_REVIEW_POSTED);
                            urlBuilder = new UrlBuilder(summary.getFirstPublishedReview().getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                            viewName = urlBuilder.asFullUrl(request);
                            break;

                        case REVIEW_UPGRADED_NOT_PUBLISHED:
                            hoverHelper.setHoverCookie(HoverHelper.Hover.SCHOOL_REVIEW_QUEUED);
                            urlBuilder = new UrlBuilder(summary.getUpgradedReviews().get(0).getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                            viewName = urlBuilder.asFullUrl(request);
                            break;

                        case NO_REVIEW_UPGRADED:  //This should not happen except with MSL users, since an email-only user
                            //created from review process would have a review to upgrade
                            if (StringUtils.isNotBlank(requestedRedirect)) {
                                viewName = requestedRedirect;
                            } else {
                                viewName = "/account/";
                            }
                            break;

                        default:
                    }
                }

                return new ModelAndView(new RedirectView(viewName)); // E A R L Y   E X I T

            case PROVISIONAL:

                summary = getReviewService().upgradeProvisionalReviewsAndSummarize(user);

                // GS-9787 Users who have a review posted may get a custom welcome message
                // per GS-8290 All users who complete registration should get a welcome message
                // but only users who haven't already been sent one
                switch(summary.getStatus()) {
                    case REVIEW_UPGRADED_PUBLISHED:
                        user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                        sendReviewPostedWelcomeEmail(request, summary.getFirstPublishedReview());
                        hoverHelper.setHoverCookie(HoverHelper.Hover.EMAIL_VERIFIED_SCHOOL_REVIEW_POSTED);
                        urlBuilder = new UrlBuilder(summary.getFirstPublishedReview().getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                        viewName = "redirect:" + urlBuilder.asFullUrl(request);
                        break;

                    case REVIEW_UPGRADED_NOT_PUBLISHED:
                        if (user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
                            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
                            hoverHelper.setHoverCookie(HoverHelper.Hover.EMAIL_VERIFIED_SCHOOL_REVIEW_QUEUED);
                        }
                        urlBuilder = new UrlBuilder(summary.getUpgradedReviews().get(0).getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                        viewName = "redirect:" + urlBuilder.asFullUrl(request);
                        break;

                    case NO_REVIEW_UPGRADED:
                        if (user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
                            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
                            hoverHelper.setHoverCookie(HoverHelper.Hover.EMAIL_VERIFIED);
                        }
                        // when registering, send them to where they were before, or to the /account/ page
                        if (StringUtils.isNotBlank(requestedRedirect)) {
                            viewName = "redirect:" + requestedRedirect;
                        } else {
                            viewName = "redirect:/account/";
                        }
                        break;

                    default:
                }

                //TODO refactor all esp/osp/usp into a method or service.
                if(StringUtils.isNotBlank(request.getParameter("schoolId")) && StringUtils.isNotBlank(request.getParameter("state"))){
                    School school = null;
                    try{
                        Integer schoolId = Integer.parseInt(request.getParameter("schoolId"));
                        State state = State.fromString(request.getParameter("state"));
                        school = getSchoolDao().getSchoolById(state,schoolId);
                    }catch (Exception ex){
                        school = null;
                    }

                    if(school != null && school.isActive()){
                        //TODO check the state of the school.
                        //TODO check for the 9 keys?
                        //TODO check the source as well?
                        List<EspResponse> responses = getEspResponseDao().getResponsesByUserAndSchool(school,user.getId(),true);
                        if(!responses.isEmpty()){
                            //TODO Do we need to keep the inactive responses?
                            _espResponseDao.activateResponsesByUserForSchoolAndSource(school,user.getId(),EspResponseSource.usp);
                        }
                    }
                }

                if (user.hasRole(Role.ESP_MEMBER)) {
                    hoverHelper.setHoverCookie(HoverHelper.Hover.ESP_ACCOUNT_VERIFIED);
                    urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                    viewName = "redirect:" + urlBuilder.asFullUrl(request);
                } else {
                    // check if user has an esp membership row in processing state
                    EspMembership membership = getProcessingMembershipForUser(user);
                    if (membership != null && membership.getSchoolId() !=null && membership.getState() != null) {
                        boolean isUserEligibleForProvisional = _espRegistrationHelper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
                        if (isUserEligibleForProvisional) {
                            // bump this user to provisional
                            membership.setStatus(EspMembershipStatus.PROVISIONAL);
                            membership.setActive(false);
                            getEspMembershipDao().updateEspMembership(membership);
                            urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                        }else{
                            urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION_ERROR);
                            urlBuilder.addParameter("message", "page1");
                        }
                        viewName = "redirect:" + urlBuilder.asFullUrl(request);
                    }
                }

                user.setEmailValidated();  //upgrades the user to registered member
                _userDao.saveUser(user);
                PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community

                break;
            
            case EMAIL_ONLY:

                summary = getReviewService().upgradeProvisionalReviewsAndSummarize(user);

                switch (summary.getStatus()) {
                    case REVIEW_UPGRADED_PUBLISHED:
                        sendReviewPostedEmail(request, summary.getFirstPublishedReview());
                        hoverHelper.setHoverCookie(HoverHelper.Hover.SCHOOL_REVIEW_POSTED);
                        urlBuilder = new UrlBuilder(summary.getFirstPublishedReview().getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                        viewName = "redirect:" + urlBuilder.asFullUrl(request);
                        break;

                    case REVIEW_UPGRADED_NOT_PUBLISHED:
                        hoverHelper.setHoverCookie(HoverHelper.Hover.SCHOOL_REVIEW_QUEUED);
                        urlBuilder = new UrlBuilder(summary.getUpgradedReviews().get(0).getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                        viewName = "redirect:" + urlBuilder.asFullUrl(request);
                        break;

                    case NO_REVIEW_UPGRADED:  //This should not happen except with MSL users, since an email-only user
                                              //created from review process would have a review to upgrade
                        if (StringUtils.isNotBlank(requestedRedirect)) {
                            viewName = "redirect:" + requestedRedirect;
                        } else {
                            viewName = "redirect:/account/";
                        }
                        break;
                    
                    default:
                }

                //remember that user has verified their email so that they dont have to do it again as long as they have the cookie
                String emailHash = UrlUtil.urlEncode(DigestUtil.hashString(user.getEmail()));
                _log.debug("Hashed email " + user.getEmail() + " to hash " + emailHash + " - writing cookie property");
                cookie.setProperty("emailVerified", emailHash);
                break;

            default:
        }

        //if user changed their email and received this email verification link, overwrite any hovers and show
        //edit email validated hover. Always send them back to the change email page. TODO: find more elegant solution
        if (isEditEmailRequest) {
            hoverHelper.setHoverCookie(HoverHelper.Hover.NEW_EMAIL_VERIFIED);
            urlBuilder = new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null, "");
            viewName = "redirect:" + urlBuilder.asSiteRelative(request);
        }

        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        ot.addSuccessEvent(OmnitureTracking.SuccessEvent.EmailVerified);

        processPendingSubscriptions(user, ot, hoverHelper, requestedRedirect);
        sendTriggeredEmails(request, user);


        _log.info("Email confirmed, forwarding user to " + viewName);
        return new ModelAndView(viewName);

    }

    protected EspMembership getProcessingMembershipForUser(User user) {
        List<EspMembership> memberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
        if (memberships != null && memberships.size() > 0) {
            for (EspMembership membership: memberships) {
                if (membership.getStatus() == EspMembershipStatus.PROCESSING) {
                    return membership;
                }
            }
        }
        return null;
    }

    public void sendTriggeredEmails(HttpServletRequest request, User user) {
        // GS-12317
        String esw = request.getParameter(ExactTargetUtil.EMAIL_SUB_WELCOME_PARAM);
        boolean sendEmailSubscriptionWelcomeEmail = StringUtils.isNotBlank(esw);
        if (sendEmailSubscriptionWelcomeEmail) {
            Map<String, String> attributes = ExactTargetUtil.getEmailSubWelcomeAttributes(esw);
            if (!attributes.isEmpty()) {
                _exactTargetAPI.sendTriggeredEmail(ExactTargetUtil.EMAIL_SUB_WELCOME_TRIGGER_KEY, user, attributes);
            }
        }
    }

    // GS-11567 GS-11799
    // if it exists, convert existing not-verified subscription to verified subscription
    public void processPendingSubscriptions(User user, OmnitureTracking ot, HoverHelper hoverHelper, String requestedRedirect) {
        boolean alreadySubscribedToWeeklyProd = false;
        boolean alreadySubscribedToDailyProd = false;

        Subscription pendingWeeklySubscription = null;
        Subscription pendingDailySubscription = null;

        final SubscriptionProduct WEEKLY_PROD = SubscriptionProduct.PARENT_ADVISOR;
        final SubscriptionProduct WEEKLY_PROD_NOT_VERIFIED = SubscriptionProduct.PARENT_ADVISOR_NOT_VERIFIED;
        final SubscriptionProduct DAILY_PROD = SubscriptionProduct.DAILY_TIP;
        final SubscriptionProduct DAILY_PROD_NOT_VERIFIED = SubscriptionProduct.DAILY_TIP_NOT_VERIFIED;

        // get subscription id for not-verified version, if it exists, and find out if already subscribed to regular version
        List<Subscription> subscriptions = _subscriptionDao.getUserSubscriptions(user);
        if(subscriptions != null){
            for (Subscription subscription : subscriptions) {
                if (subscription.getProduct().equals(WEEKLY_PROD_NOT_VERIFIED)) {
                    pendingWeeklySubscription = subscription;
                } else if (subscription.getProduct().equals(DAILY_PROD_NOT_VERIFIED)) {
                    pendingDailySubscription = subscription;
                } else if (subscription.getProduct().equals(WEEKLY_PROD)) {
                    alreadySubscribedToWeeklyProd = true;
                } else if (subscription.getProduct().equals(DAILY_PROD)) {
                    alreadySubscribedToDailyProd = true;
                }
            }
        }

        // warning: order matters in the following statements because we need both processPendingSubscriptionsHelper
        //          calls to be made -- no shortcuts!
        boolean convertedToVerifiedSubscription =
                processPendingSubscriptionsHelper(alreadySubscribedToWeeklyProd, pendingWeeklySubscription, WEEKLY_PROD);
        convertedToVerifiedSubscription =
                processPendingSubscriptionsHelper(alreadySubscribedToDailyProd, pendingDailySubscription, DAILY_PROD) ||
                convertedToVerifiedSubscription;

        if (convertedToVerifiedSubscription) {
            // add success event to track having signed up for emails
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.EmailModuleSignup);

        }

        boolean showThankYouHover = isShowSubscriptionThankYouHover(requestedRedirect);
        if (showThankYouHover || convertedToVerifiedSubscription) {
            // set cookie to show confirmation hover on page load
            hoverHelper.setHoverCookie(HoverHelper.Hover.SUBSCRIPTION_EMAIL_VERIFIED);
        }
    }

    private boolean processPendingSubscriptionsHelper(boolean alreadySubscribed, Subscription pendingSubscription, SubscriptionProduct subscriptionProduct) {
        if (pendingSubscription != null) {
            if (!alreadySubscribed) {
                // change not-verified subscription to verified subscription
                pendingSubscription.setProduct(subscriptionProduct);
                _subscriptionDao.saveSubscription(pendingSubscription);
                return true;
            } else {
                // unsubscribe user from not-verified subscription -- just in case they already had a verified subscription
                _subscriptionDao.removeSubscription(pendingSubscription.getId());
            }
        }
        return false;
    }

    protected UserState getUserState(User user) {
        if (user.isPasswordEmpty()) {
            return UserState.EMAIL_ONLY;
        } else if (user.isEmailValidated()) {
            return UserState.REGISTERED;
        } else {
            return UserState.PROVISIONAL;
        }
    }

    public ModelAndView handleExpiredLink(HttpServletRequest request, HttpServletResponse response, User user) throws UnsupportedEncodingException {
        // If a user clicks on an expired link, they should be taken to the GS homepage
        // with the [verification link expired hover].
        _log.info("Validation link expired for " + user.getEmail());
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME, null);
        String redirect = urlBuilder.asSiteRelative(request);
        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            redirect += "index.page";
        }

        String email = URLEncoder.encode(user.getEmail(), "UTF-8");
        email = email.replace("+", "%2B");
        SitePrefCookie cookie = new SitePrefCookie(request, response);
        cookie.setProperty("showHover", "validationLinkExpired");
        return new ModelAndView("redirect:" + UrlUtil.addParameter(redirect, "email=" + email));
    }

    private void sendReviewPostedWelcomeEmail(HttpServletRequest request, Review anUpgradedReview) {
        Map<String,String> emailAttributes = new HashMap<String,String>();
        emailAttributes.put("schoolName", anUpgradedReview.getSchool().getName());
        emailAttributes.put("HTML__review", "<p>" + anUpgradedReview.getComments() + "</p>");

        StringBuffer reviewLink = new StringBuffer("<a href=\"");
        UrlBuilder urlBuilder = new UrlBuilder(anUpgradedReview.getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
        urlBuilder.addParameter("lr", "true");
        reviewLink.append(urlBuilder.asFullUrl(request)).append("#ps").append(anUpgradedReview.getId());
        reviewLink.append("\">your review</a>");

        emailAttributes.put("HTML__reviewLink", reviewLink.toString());
        _exactTargetAPI.sendTriggeredEmail("review_posted_plus_welcome_trigger",anUpgradedReview.getUser(), emailAttributes);
    }

    private void sendReviewPostedEmail(HttpServletRequest request, Review anUpgradedReview) {
        Map<String,String> emailAttributes = new HashMap<String,String>();
        emailAttributes.put("schoolName", anUpgradedReview.getSchool().getName());
        emailAttributes.put("HTML__review", "<p>" + anUpgradedReview.getComments() + "</p>");

        StringBuffer reviewLink = new StringBuffer("<a href=\"");
        UrlBuilder urlBuilder = new UrlBuilder(anUpgradedReview.getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
        urlBuilder.addParameter("lr", "true");
        reviewLink.append(urlBuilder.asFullUrl(request)).append("#ps").append(anUpgradedReview.getId());
        reviewLink.append("\">your review</a>");

        emailAttributes.put("HTML__reviewLink", reviewLink.toString());
        _exactTargetAPI.sendTriggeredEmail("review_posted_trigger",anUpgradedReview.getUser(), emailAttributes);
    }

    private boolean isShowSubscriptionThankYouHover(String redirectParam) {
        boolean showSubscriptionThankYouHover = false;
        if (StringUtils.isNotBlank(redirectParam) && redirectParam.indexOf("?showSubscriptionThankYouHover=true") > 0) {
            showSubscriptionThankYouHover = true;
        }
        return showSubscriptionThankYouHover;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public ReviewService getReviewService() {
        return _reviewService;
    }

    public void setReviewService(ReviewService reviewService) {
        _reviewService = reviewService;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

    public EspRegistrationHelper get_espRegistrationHelper() {
        return _espRegistrationHelper;
    }

    public void set_espRegistrationHelper(EspRegistrationHelper _espRegistrationHelper) {
        this._espRegistrationHelper = _espRegistrationHelper;
    }

}