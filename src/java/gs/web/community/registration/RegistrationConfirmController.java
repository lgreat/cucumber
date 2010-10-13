package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.WelcomeMessageStatus;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.util.DigestUtil;
import gs.web.community.HoverHelper;
import gs.web.school.review.ReviewService;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private ExactTargetAPI _exactTargetAPI;
    private ReviewService _reviewService;

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

        /* TODO: figure out if we still need to do this
        if (user.isPasswordEmpty()) {
            // request for new password has expired, redirect to registration
            _log.warn("Community registration request has expired for user " +
                    user.getEmail() + ", redirecting to registration");
            return redirectToRegistration(request, user.getEmail());
        }
        */
        SitePrefCookie cookie = new SitePrefCookie(request, response);
        HoverHelper hoverHelper = new HoverHelper(cookie); // :(  I'd prefer to provide the request and response rather than cookie

        // sorry, to reduce risk this release, I did not modify existing behavior of User setEmailValidated.
        // Instead, a new db field now tracks this, and emailVerified is the new associated bean method. That is why
        // there are two methods which *should* do the same thing.
        user.setEmailVerified(true);
        // authenticate user
        if (!user.isEmailValidated()) {


            // any school reviews in the provisional state need to be upgraded
            List<Review> upgradedReviews = getReviewService().upgradeProvisionalReviews(user);

            //if any upgraded reviews were posted, notify user about it. otherwise if any review was upgraded, notify user
            Review anUpgradedReview = findCorrectUpgradedReview(upgradedReviews);
            boolean reviewPosted = (anUpgradedReview != null && anUpgradedReview.getStatus().equals("p"));

            //we only want to send welcome email and update the user's password to non-provisional if they have a password
            //users might not have a password if they created an email-only account to create a review or for MSL access
            if (!user.isPasswordEmpty()) {
                user.setEmailValidated();
                // TODO: GS-9787 Users who have a review posted may get a custom welcome message
                // per GS-8290 All users who complete registration should get a welcome message
                // but only users who haven't already been sent one
                if (reviewPosted) {
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                    sendReviewPostedWelcomeEmail(request, anUpgradedReview);
                } else if (user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
                }

                _userDao.saveUser(user);
                PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
            } else {
                //remember that user has verified their email so that they dont have to do it again as long as they have the cookie
                String emailHash = DigestUtil.hashString(user.getEmail());
                cookie.setProperty("emailVerified", emailHash);
            }

            String target;
            if (request.getParameter("edit") != null) {
                cookie.setProperty("showHover", "editEmailValidated");
                // when editing their email address, always send them back to the change email page
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null, "");
                target = "redirect:" + urlBuilder.asSiteRelative(request);
            } else if (anUpgradedReview != null) {
                UrlBuilder urlBuilder = new UrlBuilder(anUpgradedReview.getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                target = "redirect:" + urlBuilder.asSiteRelative(request);
                if (user.isPasswordEmpty()) {
                    if (reviewPosted) {
                        hoverHelper.setHoverCookie(HoverHelper.Hover.SCHOOL_REVIEW_POSTED_THANK_YOU);
                    } else {
                        hoverHelper.setHoverCookie(HoverHelper.Hover.SCHOOL_REVIEW_NOT_POSTED_THANK_YOU);
                    }
                } else {
                    if (reviewPosted) {
                        hoverHelper.setHoverCookie(HoverHelper.Hover.EMAIL_VALIDATED_SCHOOL_REVIEW_POSTED);
                    } else {
                        hoverHelper.setHoverCookie(HoverHelper.Hover.EMAIL_VALIDATED_SCHOOL_REVIEW_QUEUED);
                    }
                }
            } else {
                hoverHelper.setHoverCookie(HoverHelper.Hover.EMAIL_VALIDATED);
                // when registering, send them to where they were before, or to the /account/ page
                if (StringUtils.isNotBlank(request.getParameter("redirect"))) {
                    target = "redirect:" + request.getParameter("redirect");
                } else {
                    target = "redirect:/account/";
                }
            }

            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.EmailVerified);
            _log.info("Email confirmed, forwarding user to " + target);
            return new ModelAndView(target);
        } else {
            // already confirmed email, so just sign them in and redirect to /account/
            PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
            return new ModelAndView(new RedirectView("/account/"));
        }

    }

    public Review findCorrectUpgradedReview(List<Review> upgradedReviews) {
        Review anUpgradedReview = null;
        if (upgradedReviews != null && upgradedReviews.size() > 0) {
            List<Review> postedReviews = findPostedReviews(upgradedReviews);

            if (postedReviews != null && postedReviews.size() > 0) {
                anUpgradedReview = postedReviews.get(0);

            } else {
                anUpgradedReview = upgradedReviews.get(0);
            }
        }
        return anUpgradedReview;
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

    public List<Review> findPostedReviews(List<Review> upgradedReviews) {
        if (upgradedReviews == null) {
            return null;
        }
        
        List<Review> postedReviews = new ArrayList<Review>();

        for (Review review : upgradedReviews) {
            if (StringUtils.equals("p", review.getStatus())) {
                postedReviews.add(review);
            }
        }

        return postedReviews;
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


}