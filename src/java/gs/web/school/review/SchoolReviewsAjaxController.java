package gs.web.school.review;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.json.JSONObject;
import gs.data.school.IHeldSchoolDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.util.email.EmailHelperFactory;
import gs.web.community.IReportContentService;
import gs.web.school.SchoolPageInterceptor;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private EmailHelperFactory _emailHelperFactory;

    private Boolean _jsonPage = Boolean.FALSE;

    private Boolean _xmlPage = Boolean.FALSE;

    public ModelAndView handle(HttpServletRequest request,
                               HttpServletResponse response,
                               Object command,
                               BindException errors) throws Exception {

        if (errors.hasErrors()) {
            return errorJSON(response, errors);
        }

        ReviewCommand reviewCommand = (ReviewCommand) command;

        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        String check = request.getParameter("isAddNewParentReview");
        if (!StringUtils.isBlank(check)) {
            if (request.getParameter("schoolId") != null && request.getParameter("schoolState") != null) {
                school = _schoolDao.getSchoolById(State.fromString(request.getParameter("schoolState")), Integer.parseInt(request.getParameter("schoolId")));
            }
        }

        User user = getUserDao().findUserFromEmailIfExists(reviewCommand.getEmail());
        boolean isNewUser = false;

        //new user - create user, add entry to list_active table,
        if (user == null) {
            user = new User();
            user.setEmail(reviewCommand.getEmail());
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

            //Must perform omniture tracking since we're creating this user here.
            // aroy: This doesn't work since cookies aren't set by an ajax response
            // So for now I'm commenting it out. It appears that
            // RegistrationHoverController.updateUserProfile handles this case anyway
//            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
//            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);

            isNewUser = true;
        }

        Review review = createOrUpdateReview(user, school, reviewCommand, isNewUser, check);
        if (StringUtils.length(review.getComments()) < 15) {
            // error out early
            Map<Object, Object> values = new HashMap<Object, Object>();
            values.put("reviewPosted", "false");
            String jsonString = new JSONObject(values).toString();

            response.setContentType("text/x-json");
            _log.info("Writing JSON response -" + jsonString);
            response.getWriter().write(jsonString);
            response.getWriter().flush();
            return null;
        }

        boolean newUser = user.isPasswordEmpty() || user.isEmailProvisional();
        Poster poster = reviewCommand.getPoster();

        boolean reviewPosted = true;
        boolean reviewHasReallyBadWords = false;
        boolean reviewShouldBeReported = false;
        StringBuffer reason = null;

        Map<IAlertWordDao.alertWordTypes, Set<String>> alertWordMap = getAlertWordDao().getAlertWords(review.getComments());
        if (alertWordMap != null) {
            Set<String> alertWords = alertWordMap.get(IAlertWordDao.alertWordTypes.WARNING);
            Set<String> reallyBadWords = alertWordMap.get(IAlertWordDao.alertWordTypes.REALLY_BAD);

            boolean reviewHasAlertWords = alertWords != null && alertWords.size() > 0;
            reviewHasReallyBadWords = reallyBadWords != null && reallyBadWords.size() > 0;
            boolean reviewHasAnyBadWords = reviewHasAlertWords || reviewHasReallyBadWords;

            if (reviewHasAnyBadWords) {
                reason = new StringBuffer("Review contained ");

                if (reviewHasAlertWords) {
                    reason.append("warning words (").append(StringUtils.join(alertWords, ",")).append(")");
                }
                if (reviewHasReallyBadWords) {
                    if (!(alertWords.isEmpty())) {
                        reason.append(" and ");
                    }
                    reason.append("really bad words (").append(StringUtils.join(reallyBadWords, ",")).append(")");
                    reviewPosted = false;
                }

                reviewShouldBeReported = true;
            }
        }

        if (newUser) {
            if (Poster.STUDENT.equals(poster)) {
                review.setStatus("pu");
            } else {
                if (reviewHasReallyBadWords) {
                    review.setStatus("pd");
                } else {
                    review.setStatus("pp");
                }
            }

        } else {
            if (Poster.STUDENT.equals(poster)) {
                review.setStatus("u");
            } else {
                if (reviewHasReallyBadWords) {
                    review.setStatus("d");
                } else {
                    review.setStatus("p");
                }
            }
        }

        checkHoldList(school, review);


        Integer existingId = review.getId();

        //if review posted automatically, set the process date now
        if (reviewPosted) {
            review.setProcessDate((Calendar.getInstance()).getTime());
        }

        //save the review
        getReviewDao().saveReview(review);

        // Before any reports are created, we should check something:
        // if this review existed before (if the user is overwriting an existing review)
        // then we need to delete any reports on it
        // otherwise the auto filter will fail, and anyway they could be totally irrelevant
        if (existingId != null) {
            _reportedEntityDao.deleteReportsFor(ReportedEntity.ReportedEntityType.schoolReview, review.getId());
        }

        if (reviewShouldBeReported) {
            getReportContentService().reportContent(getAlertWordFilterUser(), user, request, review.getId(), ReportedEntity.ReportedEntityType.schoolReview, reason.toString());
        }

        if (Poster.STUDENT.equals(reviewCommand.getPoster())
                || StringUtils.equals("h", review.getStatus())
                || StringUtils.equals("ph", review.getStatus())) {
            reviewPosted = false;
        }



        if (reviewPosted && (!isNewUser)) {
            Map<String,String> emailAttributes = new HashMap<String,String>();
            emailAttributes.put("schoolName", school.getName());
            emailAttributes.put("HTML__review", "<p>" + review.getComments() + "</p>");

            StringBuffer reviewLink = new StringBuffer("<a href=\"");
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            urlBuilder.addParameter("lr", "true");
            reviewLink.append(urlBuilder.asFullUrl(request)).append("#ps").append(review.getId());
            reviewLink.append("\">your review</a>");
            emailAttributes.put("HTML__reviewLink", reviewLink.toString());

            _exactTargetAPI.sendTriggeredEmail("review_posted_trigger",user, emailAttributes);
        }


        Map<Object, Object> values = new HashMap<Object, Object>();
        values.put("status", "true");
        values.put("userId", user.getId().toString());
        //trigger the success events
        if (StringUtils.isNotBlank(reviewCommand.getComments())) {
            values.put("reviewEvent", OmnitureTracking.SuccessEvent.ParentReview.toOmnitureString());
        }
        values.put("reviewPosted", String.valueOf(reviewPosted));
        values.put("redirectUrl", new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS).asFullUrl(request));
        String jsonString = new JSONObject(values).toString();

        response.setContentType("text/x-json");
        _log.info("Writing JSON response -" + jsonString);
        response.getWriter().write(jsonString);
        response.getWriter().flush();
        return null;
    }

    protected void checkHoldList(School school, Review review) {
        try {
            if (_heldSchoolDao.isSchoolOnHoldList(school)) {
                if (StringUtils.length(review.getStatus()) == 2) {
                    review.setStatus("ph");
                } else {
                    review.setStatus("h");
                }
            }
        } catch (Exception e) {
            _log.warn("Error checking hold list: " + e, e);
        }
    }

    protected Review createOrUpdateReview(final User user, final School school,
                                          final ReviewCommand command, final boolean isNewUser, String check) {

        Review review = null;

        Poster poster = command.getPoster();

        //existing user, check if they have previously left a review for this school
        if (!isNewUser) {
            review = getReviewDao().findReview(user, school);

            if (review != null) {
                //old review is not blank and current review is blank
                if (StringUtils.isNotBlank(review.getComments()) && StringUtils.isBlank(command.getComments())) {
                    //only update the overall quality rating and who they are
                    if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                        review.setPOverall(command.getOverall());
                    } else {
                        review.setQuality(command.getOverall());
                    }
                    review.setPoster(command.getPoster());
                    return review;
                } else {

                    review.setPosted(new Date());
                    review.setProcessDate(null);
                    //new review submitted, so set the processor to null
                    review.setSubmitter(null);
                    review.setNote(null);
                    _log.warn("updating a non empty review with a new comment.\nOld Comment: "
                            + review.getComments() +
                            "\nNew Comment: " + command.getComments() + "\nOld processor: " + review.getSubmitter()
                            + "\nOld Note: " + review.getNote());
                }

                setRatingsOnReview(school.getLevelCode(), command, review, poster);
            }
        }

        if (null == review) {
            review = new Review();

            review.setUser(user);
            review.setSchool(school);

            setRatingsOnReview(school.getLevelCode(), command, review, poster);


        }
        review.setHow(command.getClient());
        review.setPoster(command.getPoster());
        review.setComments(StringUtils.abbreviate(command.getComments(), 1200));
        review.setOriginal(command.getComments());
        review.setAllowContact(command.isAllowContact());

        if (StringUtils.isNotEmpty(command.getFirstName()) || StringUtils.isNotEmpty(command.getLastName())) {
            review.setAllowName(true);
        }

        return review;
    }

    public void setRatingsOnReview(LevelCode schoolLevelCode, ReviewCommand command, Review review, Poster poster) {
        if (LevelCode.PRESCHOOL.equals(schoolLevelCode)) {
            if (command.getOverallAsString() != null) {
                review.setPOverall(CategoryRating.getCategoryRating(command.getOverallAsString()));
            }

            if (Poster.PARENT.equals(poster)) {
                if (command.getTeacherAsString() != null) {
                    review.setPTeachers(CategoryRating.getCategoryRating(command.getTeacherAsString()));
                }
                if (command.getParentAsString() != null) {
                    review.setPParents(CategoryRating.getCategoryRating(command.getParentAsString()));
                }
                if (command.getPFacilitiesAsString() != null) {
                    review.setPFacilities(CategoryRating.getCategoryRating(command.getPFacilitiesAsString()));
                }
            }
        } else {
            if (command.getOverallAsString() != null) {
                review.setQuality(CategoryRating.getCategoryRating(command.getOverallAsString()));
            }

            if (Poster.PARENT.equals(poster)) {
                if (command.getTeacherAsString() != null) {
                    review.setTeachers(CategoryRating.getCategoryRating(command.getTeacherAsString()));
                }
                if (command.getParentAsString() != null) {
                    review.setParents(CategoryRating.getCategoryRating(command.getParentAsString()));
                }
                if (command.getPrincipalAsString() != null) {
                    review.setPrincipal(CategoryRating.getCategoryRating(command.getPrincipalAsString()));
                }
            } else if (Poster.STUDENT.equals(poster)) {
                if (command.getTeacherAsString() != null) {
                    review.setTeachers(CategoryRating.getCategoryRating(command.getTeacherAsString()));
                }
            }
        }
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
}