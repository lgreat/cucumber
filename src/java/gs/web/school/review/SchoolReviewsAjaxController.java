package gs.web.school.review;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.json.JSONObject;
import gs.data.school.IHeldSchoolDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.util.DigestUtil;
import gs.data.util.email.EmailHelperFactory;
import gs.web.community.IReportContentService;
import gs.web.community.registration.EmailVerificationReviewOnlyEmail;
import gs.web.school.AbstractSchoolController;
import gs.web.school.SchoolPageInterceptor;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.*;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

import static gs.data.util.XMLUtil.*;


/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 *         <p/>
 *         http://wiki.greatschools.org/bin/view/Greatschools/ParentReviewWebService
 */
public class SchoolReviewsAjaxController extends AbstractCommandController implements ReadWriteController {

    protected final static Log _log = LogFactory.getLog(SchoolReviewsAjaxController.class);

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private ISubscriptionDao _subscriptionDao;
    private String _viewName;
    private IReportedEntityDao _reportedEntityDao;

    private IReportContentService _reportContentService;

    private ExactTargetAPI _exactTargetAPI;

    private ISchoolDao _schoolDao;

    private IAlertWordDao _alertWordDao;

    private IHeldSchoolDao _heldSchoolDao;

    private IBannedIPDao _bannedIPDao;

    private EmailHelperFactory _emailHelperFactory;

    private Boolean _jsonPage = Boolean.FALSE;

    private Boolean _xmlPage = Boolean.FALSE;

    private ReviewHelper _reviewHelper;

    private EmailVerificationReviewOnlyEmail _emailVerificationReviewOnlyEmail;

    public ModelAndView handle(HttpServletRequest request,
                               HttpServletResponse response,
                               Object command,
                               BindException errors) throws Exception {

        if (errors.hasErrors()) {
            return errorJSON(response, errors);
        }

        Map<Object, Object> responseValues = new HashMap<Object, Object>();

        ReviewCommand reviewCommand = (ReviewCommand) command;
        reviewCommand.setIp(getIPFromRequest(request));

        SitePrefCookie cookie = new SitePrefCookie(request, response);
        String verifiedEmailHash = cookie.getProperty("emailVerified");
        String thisEmailHash = UrlUtil.urlEncode(DigestUtil.hashString(reviewCommand.getEmail()));
        _log.debug("Hashed email " + reviewCommand.getEmail() + " to hash " + thisEmailHash);
        boolean emailVerifiedRecently = thisEmailHash.equals(verifiedEmailHash);

        School school = getSchool(request);

        User user = getUserDao().findUserFromEmailIfExists(reviewCommand.getEmail());
        boolean isNewUser = false;

        //boolean userAuthorizedWithPassword = PageHelper.isMemberAuthorized(request);
        //TODO: Use interceptor to ensure that full user account with password cannot get here unless logged in.
        boolean userAuthorizedWithPassword = user!=null && !user.isPasswordEmpty() && !user.isEmailProvisional();

        if (reviewCommand.isFromReviewLandingPage()) {
            User loggedInUser = null;
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            if(PageHelper.isMemberAuthorized(request)) {
                loggedInUser = sessionContext.getUser();
            }


            if (loggedInUser != null && !loggedInUser.equals(user)) {
                return errorJSON(response, "Logged in user must match owner of provided email.");
            }

            userAuthorizedWithPassword = loggedInUser != null;
        }

        Review review;
        //The user submitting the review might not exist yet.
        if (user == null) {
            isNewUser = true;
            user = createUserForReview(reviewCommand, school);
            review = getReviewHelper().createReview(user, school, reviewCommand);
        } else {

            /*
            //I don't like this here, but I couldn't come up with a more elegant solution
            //If review_only user already exists and has no password, and their email has never been verified,
            //then show them a special hover
            if (user.isPasswordEmpty() && (user.isEmailVerified() == null || !user.isEmailVerified()) && "review_only".equals(user.getHow())) {
                responseValues.put("showHover", "emailNotValidated");
                successJSON(response, responseValues);
                return null; // E A R L Y    E X I T
            }
            */

            //look for existing review
            review = getReviewDao().findReview(user, school);
            if (review != null) {
                getReviewHelper().updateReview(review, school, reviewCommand);
            } else {
                review = getReviewHelper().createReview(user, school, reviewCommand);
            }
        }

        Poster poster = reviewCommand.getPoster();

        boolean reviewHasReallyBadWords = false;
        boolean reviewShouldBeReported = false;
        StringBuilder reason = null;

        boolean userIpOnBanList = isIPBlocked(request);
        if (userIpOnBanList) {
            reviewShouldBeReported = true;
            reason = new StringBuilder("IP " + getIPFromRequest(request) + " was on ban list at time of submission.");
        }

        Map<IAlertWordDao.alertWordTypes, Set<String>> alertWordMap = getAlertWordDao().getAlertWords(review.getComments());
        if (!userIpOnBanList && alertWordMap != null) {
            Set<String> alertWords = alertWordMap.get(IAlertWordDao.alertWordTypes.WARNING);
            Set<String> reallyBadWords = alertWordMap.get(IAlertWordDao.alertWordTypes.REALLY_BAD);

            boolean reviewHasAlertWords = alertWords != null && alertWords.size() > 0;
            reviewHasReallyBadWords = reallyBadWords != null && reallyBadWords.size() > 0;
            boolean reviewHasAnyBadWords = reviewHasAlertWords || reviewHasReallyBadWords;

            if (reviewHasAnyBadWords) {
                reason = new StringBuilder("Review contained ");

                if (reviewHasAlertWords) {
                    reason.append("warning words (").append(StringUtils.join(alertWords, ",")).append(")");
                }
                if (reviewHasReallyBadWords) {
                    if (!(alertWords.isEmpty())) {
                        reason.append(" and ");
                    }
                    reason.append("really bad words (").append(StringUtils.join(reallyBadWords, ",")).append(")");
                }

                reviewShouldBeReported = true;
            }
        }

        boolean reviewProvisional = (isNewUser || (!userAuthorizedWithPassword && !emailVerifiedRecently));

        if (reviewProvisional) {
            if (userIpOnBanList || Poster.STUDENT.equals(poster)) {
                review.setStatus("pu");
            } else {
                if (reviewHasReallyBadWords) {
                    review.setStatus("pd");
                } else {
                    review.setStatus("pp");
                }
            }

        } else {
            if (userIpOnBanList || Poster.STUDENT.equals(poster)) {
                review.setStatus("u");
            } else {
                if (reviewHasReallyBadWords) {
                    review.setStatus("d");
                } else {
                    review.setStatus("p");
                }
            }
        }

        boolean schoolOnHoldList = checkHoldList(school, review);


        Integer existingReviewId = review.getId();

        //if review posted automatically, set the process date now
        if (!reviewProvisional) {
            review.setProcessDate((Calendar.getInstance()).getTime());
        }

        //save the review
        try {
            getReviewDao().saveReview(review);
        } catch (DataIntegrityViolationException dive) {
            _log.warn("Ignoring duplicate review attempt: " + review.getMemberId() + "-" +
                              review.getSchool().getStateAbbreviation() + "-" + review.getSchool().getId());
            ThreadLocalTransactionManager.setRollbackOnly();
            return null;
        }

        // Before any reports are created, we should check something:
        // if this review existed before (if the user is overwriting an existing review)
        // then we need to delete any reports on it
        // otherwise the auto filter will fail, and anyway they could be totally irrelevant
        if (existingReviewId != null) {
            _reportedEntityDao.deleteReportsFor(ReportedEntity.ReportedEntityType.schoolReview, existingReviewId);
        }

        if (reviewShouldBeReported) {
            getReportContentService().reportContent(getAlertWordFilterUser(), user, request, review.getId(), ReportedEntity.ReportedEntityType.schoolReview, reason.toString());
        }

        boolean reviewPosted = !(reviewProvisional || reviewHasReallyBadWords || schoolOnHoldList || userIpOnBanList || Poster.STUDENT.equals(reviewCommand.getPoster()));
        
        if (reviewPosted) {
            sendReviewPostedEmail(request, review);
        }

        if (reviewCommand.isMssSub() != null && reviewCommand.isMssSub()) {
            addMssSubForSchool(user, school);
        }else if(reviewCommand.isMssSub() != null && !reviewCommand.isMssSub()){
            removeMssSubForSchool(user,school);
        }

        responseValues.put("userId", user.getId().toString());

        if (reviewProvisional) {
            responseValues.put("showHover", "validateEmailSchoolReview");
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            String redirectUrl = urlBuilder.asFullUrl(request);
            if (reviewCommand.isFromReviewLandingPage()) {
                Map<String,String> otherParams = new HashMap<String,String>();
                otherParams.put("upgradeProvisional", "true");
                getEmailVerificationReviewOnlyEmail().sendSchoolReviewVerificationEmail(request, user, redirectUrl, otherParams);
            } else {
                getEmailVerificationReviewOnlyEmail().sendSchoolReviewVerificationEmail(request, user, redirectUrl);
            }
        }

        //trigger the success events
        if (StringUtils.isNotBlank(reviewCommand.getComments())) {
            responseValues.put("reviewEvent", OmnitureTracking.SuccessEvent.ParentReview.toOmnitureString());
        }
        responseValues.put("reviewPosted", String.valueOf(reviewPosted));
        UrlBuilder urlBuilder;
        if (school.isSchoolForNewProfile()) {
            urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            urlBuilder.addParameter("tab", AbstractSchoolController.NewProfileTabs.reviews.getParameterValue());
        } else {
            urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        }
        responseValues.put("redirectUrl", urlBuilder.asFullUrl(request));

        successJSON(response, responseValues);

        
        return null;
    }
    
    protected String getIPFromRequest(HttpServletRequest request) {
        String requestIP = (String) request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        return requestIP;
    }

    protected boolean isIPBlocked(HttpServletRequest request) {
        return _bannedIPDao.isIPBanned(getIPFromRequest(request), IBannedIPDao.DEFAULT_DAYS_BANNED);
    }

    protected void addMssSubForSchool(User user, School school) {
        try {
            List<Subscription> userSubs = _subscriptionDao.findMssSubscriptionsByUser(user);
            //If the user has more than the max number of subs allowed then do not add the sub for the school being reviewed.
            if (userSubs == null || (userSubs != null && userSubs.size() < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER)) {
                List subscriptions = new ArrayList();
                Subscription sub = new Subscription(user, SubscriptionProduct.MYSTAT, school);
                subscriptions.add(sub);
                getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);
                //getSubscriptionDao().saveSubscription(sub);


            }
        } catch (Exception e) {
            _log.debug("Error while adding subscription: "+e);
        }
    }

    protected void removeMssSubForSchool(User user, School school) {
        try {
            Subscription sub = getSubscriptionDao().findMssSubscriptionByUserAndSchool(user, school);
            if (sub != null) {
                getSubscriptionDao().removeSubscription(sub.getId());
            }
        } catch (Exception e) {
           _log.debug("Error while removing subscription: "+e);
        }
    }

    private User createUserForReview(ReviewCommand reviewCommand, School school) {
        User user;
        user = new User();
        user.setEmail(reviewCommand.getEmail());
        String how;
        if ("morganstanley".equals(reviewCommand.getHow())) {
            how = "morganstanley";
        } else {
            how = "review_only";
        }
        user.setHow(how);
        getUserDao().saveUser(user);
        // Because of hibernate caching, it's possible for a list_active record
        // (with list_member id) to be commited before the list_member record is
        // committed. Adding this commitOrRollback prevents this.
        ThreadLocalTransactionManager.commitOrRollback();
        //this is for unit test purposes.  so I can return any user.
        user = getUserDao().findUserFromEmailIfExists(reviewCommand.getEmail());

        Subscription sub = new Subscription(user, SubscriptionProduct.RATING, school.getDatabaseState());
        sub.setSchoolId(school.getId());
        getSubscriptionDao().saveSubscription(sub);
        return user;
    }

    private boolean isValid(Review review) {
        return StringUtils.length(review.getComments()) >= 15;
    }

    private School getSchool(HttpServletRequest request) {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        String isAddNewParentReview = request.getParameter("isAddNewParentReview");
        if (!StringUtils.isBlank(isAddNewParentReview)) {
            if (request.getParameter("schoolId") != null && request.getParameter("schoolState") != null) {
                school = _schoolDao.getSchoolById(State.fromString(request.getParameter("schoolState")), Integer.parseInt(request.getParameter("schoolId")));
            }
        }
        return school;
    }

    private void sendReviewPostedEmail(HttpServletRequest request, Review review) {
        Map<String,String> emailAttributes = new HashMap<String,String>();
        emailAttributes.put("schoolName", review.getSchool().getName());
        emailAttributes.put("HTML__review", "<p>" + review.getComments() + "</p>");

        StringBuffer reviewLink = new StringBuffer("<a href=\"");
        UrlBuilder urlBuilder = new UrlBuilder(review.getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
        urlBuilder.addParameter("lr", "true");
        reviewLink.append(urlBuilder.asFullUrl(request)).append("#ps").append(review.getId());
        reviewLink.append("\">your review</a>");
        emailAttributes.put("HTML__reviewLink", reviewLink.toString());

        getExactTargetAPI().sendTriggeredEmail("review_posted_trigger",review.getUser(), emailAttributes);
    }

    protected boolean checkHoldList(School school, Review review) {
        boolean onHoldList = false;
        try {
            onHoldList = _heldSchoolDao.isSchoolOnHoldList(school);
            if (onHoldList) {
                if (StringUtils.length(review.getStatus()) == 2) {
                    review.setStatus("ph");
                } else {
                    review.setStatus("h");
                }
            }
        } catch (Exception e) {
            _log.warn("Error checking hold list: " + e, e);
        }
        return onHoldList;
    }

    protected ModelAndView errorJSON(HttpServletResponse response, BindException errors) throws IOException {
        StringBuffer buff = new StringBuffer(400);
        buff.append("{\"status\":false,\"reviewPosted\":false,\"errors\":");
        buff.append("[");
        List messages = errors.getAllErrors();

        for (Iterator iter = messages.iterator(); iter.hasNext();) {
            ObjectError error = (ObjectError) iter.next();
            buff.append("\"").append(error.getDefaultMessage()).append("\"");
            if (iter.hasNext()) {
                buff.append(",");
            }
        }
        buff.append("]}");
        response.setContentType("text/x-json");
        response.getWriter().print(buff.toString());
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView errorJSON(HttpServletResponse response, String... errors) throws IOException {
        StringBuffer buff = new StringBuffer(400);
        buff.append("{\"status\":false,\"reviewPosted\":false,\"errors\":");
        buff.append("[");
        List<String> messages = Arrays.asList(errors);

        for (Iterator iter = messages.iterator(); iter.hasNext();) {
            ObjectError error = (ObjectError) iter.next();
            buff.append("\"").append(error.getDefaultMessage()).append("\"");
            if (iter.hasNext()) {
                buff.append(",");
            }
        }
        buff.append("]}");
        response.setContentType("text/x-json");
        response.getWriter().print(buff.toString());
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView errorXML(HttpServletResponse response, BindException errors) throws IOException, ParserConfigurationException, TransformerException {
        Document doc = getDocument("errors");
        for (Object e : errors.getAllErrors()) {
            ObjectError error = (ObjectError) e;
            Element errorElem = appendElement(doc, "error", error.getDefaultMessage());
            errorElem.setAttribute("key", error.getCode());
        }
        response.setContentType("application/xml");
        serializeDocument(response.getWriter(), doc);
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView successJSON(HttpServletResponse response) throws IOException {
        response.setContentType("text/x-json");
        response.getWriter().print("{\"status\":true}");
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView successJSON(HttpServletResponse response, Map<Object,Object> responseValues) throws IOException {
        responseValues.put("status", "true");
        response.setContentType("text/x-json");
        if (responseValues != null && responseValues.size() > 0) {
            String jsonString = new JSONObject(responseValues).toString();
            response.getWriter().print(jsonString);
        }
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView successXML(HttpServletResponse response) throws IOException {
        response.setContentType("application/xml");
        response.getWriter().print("<success/>");
        response.getWriter().flush();
        return null;
    }

    protected User getAlertWordFilterUser() {
        User reporter = new User();
        reporter.setId(-1);
        reporter.setEmail(_reportContentService.getModerationEmail());
        reporter.setUserProfile(new UserProfile());
        reporter.getUserProfile().setScreenName("gs_alert_word_filter");

        return reporter;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public Boolean isJsonPage() {
        return _jsonPage;
    }

    public void setJsonPage(Boolean jsonPage) {
        _jsonPage = jsonPage;
    }

    public Boolean isXmlPage() {
        return _xmlPage;
    }

    public void setXmlPage(Boolean xmlPage) {
        _xmlPage = xmlPage;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IAlertWordDao getAlertWordDao() {
        return _alertWordDao;
    }

    public void setAlertWordDao(IAlertWordDao reviewAlertWordDao) {
        _alertWordDao = reviewAlertWordDao;
    }

    public IReportContentService getReportContentService() {
        return _reportContentService;
    }

    public void setReportContentService(IReportContentService reportContentService) {
        this._reportContentService = reportContentService;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public IReportedEntityDao getReportedEntityDao() {
        return _reportedEntityDao;
    }

    public void setReportedEntityDao(IReportedEntityDao reportedEntityDao) {
        _reportedEntityDao = reportedEntityDao;
    }

    public IHeldSchoolDao getHeldSchoolDao() {
        return _heldSchoolDao;
    }

    public void setHeldSchoolDao(IHeldSchoolDao heldSchoolDao) {
        _heldSchoolDao = heldSchoolDao;
    }

    public EmailVerificationReviewOnlyEmail getEmailVerificationReviewOnlyEmail() {
        return _emailVerificationReviewOnlyEmail;
    }

    public void setEmailVerificationReviewOnlyEmail(EmailVerificationReviewOnlyEmail emailVerificationReviewOnlyEmail) {
        _emailVerificationReviewOnlyEmail = emailVerificationReviewOnlyEmail;
    }

    public ReviewHelper getReviewHelper() {
        return _reviewHelper;
    }

    public void setReviewHelper(ReviewHelper reviewHelper) {
        _reviewHelper = reviewHelper;
    }

    public IBannedIPDao getBannedIPDao() {
        return _bannedIPDao;
    }

    public void setBannedIPDao(IBannedIPDao bannedIPDao) {
        _bannedIPDao = bannedIPDao;
    }
}