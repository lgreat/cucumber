package gs.web.school.review;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.util.email.EmailContentHelper;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.web.community.IReportContentService;
import gs.web.school.SchoolPageInterceptor;
import gs.web.tracking.CookieBasedOmnitureTracking;
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
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.mail.MessagingException;
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
    private EmailContentHelper _emailContentHelper;
    private String _viewName;
    private IReportedEntityDao _reportedEntityDao;

    private IReportContentService _reportContentService;

    private ISchoolDao _schoolDao;

    private IAlertWordDao _alertWordDao;

    private EmailHelperFactory _emailHelperFactory;

    private Boolean _jsonPage = Boolean.FALSE;

    private Boolean _xmlPage = Boolean.FALSE;

    public ModelAndView handle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        OmnitureTracking omnitureTracking = new CookieBasedOmnitureTracking(request, response);

        ReviewCommand rc = (ReviewCommand) command;

        boolean reviewPosted = false;

        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        String check = request.getParameter("isAddNewParentReview");
        if (!StringUtils.isBlank(check)) {
            if (request.getParameter("schoolId") != null && request.getParameter("schoolState") != null) {
                school = _schoolDao.getSchoolById(State.fromString(request.getParameter("schoolState")), Integer.parseInt(request.getParameter("schoolId")));
            }
        }

        User user = getUserDao().findUserFromEmailIfExists(rc.getEmail());
        boolean isNewUser = false;

        //new user - create user, add entry to list_active table,
        if (user == null) {
            user = new User();
            user.setEmail(rc.getEmail());
            getUserDao().saveUser(user);
            // Because of hibernate caching, it's possible for a list_active record
            // (with list_member id) to be commited before the list_member record is
            // committed. Adding this commitOrRollback prevents this.
            ThreadLocalTransactionManager.commitOrRollback();
            //this is for unit test purposes.  so I can return any user.
            user = getUserDao().findUserFromEmailIfExists(rc.getEmail());

            Subscription sub = new Subscription(user, SubscriptionProduct.RATING, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            getSubscriptionDao().saveSubscription(sub);
            isNewUser = true;
        }

        Review r = createOrUpdateReview(user, school, rc, isNewUser, check);

        //save the review
        getReviewDao().saveReview(r);
        reviewPosted = true;

        Map<IAlertWordDao.alertWordTypes, Set<String>> alertWordMap = getAlertWordDao().getAlertWords(r.getComments());
        if (alertWordMap != null) {
            Set<String> alertWords = alertWordMap.get(IAlertWordDao.alertWordTypes.WARNING);
            Set<String> reallyBadWords = alertWordMap.get(IAlertWordDao.alertWordTypes.REALLY_BAD);

            boolean hasAlertWords = alertWords != null && alertWords.size() > 0;
            boolean hasReallyBadWords = reallyBadWords != null && reallyBadWords.size() > 0;
            boolean hasAnyBadWords = hasAlertWords || hasReallyBadWords;

            if (hasAnyBadWords) {

                StringBuffer reason = new StringBuffer("Review contained ");

                if (hasAlertWords) {
                    reason.append("warning words (").append(StringUtils.join(alertWords, ",")).append(")");
                }
                if (hasReallyBadWords) {
                    if (!(alertWords.isEmpty())) {
                        reason.append(" and ");
                    }
                    reason.append("really bad words (").append(StringUtils.join(reallyBadWords, ",")).append(")");
                    reviewPosted = false;
                }

                getReportContentService().reportContent(getAlertWordFilterUser(), user, request, r.getId(), ReportedEntity.ReportedEntityType.schoolReview, reason.toString());
            }
        }

        if (Poster.STUDENT.equals(rc.getPoster())) {
            reviewPosted = false;
        }

        if ((!isNewUser) && reviewPosted) {
            //sendMessage(user, r.getComments(), school, "communityEmail.txt");
        }

        //only send them an email if they submitted a message that is not blank
        /*
        if ("a".equals(r.getStatus()) && StringUtils.isNotBlank(r.getComments())) {
            sendRejectMessage(user, r.getComments(), school, "rejectEmail.txt");
        } else if ("u".equals(r.getStatus()) && StringUtils.isNotBlank(r.getComments())) {
            sendMessage(user, r.getComments(), school, "communityEmail.txt");
        }

        //trigger the success events
        if (userRatedOneOrMoreCategories(rc)) {
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.ParentRating);
        }

        if (StringUtils.isNotBlank(rc.getComments())) {
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.ParentReview);
        }
        */
        Map<Object,Object> values = new HashMap<Object,Object>();
        values.put("status", "true");
        values.put("userId", user.getId().toString());
        values.put("reviewPosted", new Boolean(reviewPosted).toString());
        String jsonString = new JSONObject(values).toString();

        response.setContentType("text/x-json");
        _log.info("Writing JSON response -" + jsonString);
        response.getWriter().write(jsonString);
        response.getWriter().flush();
        return null;
    }

    protected static boolean userRatedOneOrMoreCategories(ReviewCommand rc) {
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getActivities()) && null != rc.getActivities()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getOverall()) && null != rc.getOverall()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getParent()) && null != rc.getParent()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPrincipal()) && null != rc.getPrincipal()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getSafety()) && null != rc.getSafety()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getTeacher()) && null != rc.getTeacher()) return true;

        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPProgram()) && null != rc.getPProgram()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPOverall()) && null != rc.getPOverall()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPParents()) && null != rc.getPParents()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPTeachers()) && null != rc.getPTeachers()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPSafety()) && null != rc.getPSafety()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPFacilities()) && null != rc.getPFacilities()) return true;
        return false;
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
        review.setComments(command.getComments());
        review.setOriginal(command.getComments());
        review.setAllowContact(command.isAllowContact());

        if (StringUtils.isNotEmpty(command.getFirstName()) || StringUtils.isNotEmpty(command.getLastName())) {
            review.setAllowName(true);
        }

        boolean newUser = user.isEmailProvisional();

        if (newUser) {
            if (Poster.STUDENT.equals(poster)) {
                review.setStatus("pu");
            } else {
                review.setStatus("pp");
            }
        } else {
            if (Poster.STUDENT.equals(poster)) {
                review.setStatus("u");
            } else {
                review.setStatus("p");
            }
        }


        return review;
    }

    public void setRatingsOnReview(LevelCode schoolLevelCode, ReviewCommand command, Review review, Poster poster) {
        if (LevelCode.PRESCHOOL.equals(schoolLevelCode)) {
            if (!CategoryRating.DECLINE_TO_STATE.equals(command.getOverallAsString()) && command.getOverallAsString() != null) {
                review.setPOverall(CategoryRating.getCategoryRating(command.getOverallAsString()));
            }

            if (Poster.PARENT.equals(poster) || Poster.OTHER.equals(poster)) {
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getTeacherAsString()) && command.getTeacherAsString() != null) {
                    review.setPTeachers(CategoryRating.getCategoryRating(command.getTeacherAsString()));
                }
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getParentAsString()) && command.getParentAsString() != null) {
                    review.setPParents(CategoryRating.getCategoryRating(command.getParentAsString()));
                }
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getPFacilitiesAsString()) && command.getPFacilitiesAsString() != null) {
                    review.setPFacilities(CategoryRating.getCategoryRating(command.getPFacilitiesAsString()));
                }
            }
        } else {
            if (!CategoryRating.DECLINE_TO_STATE.equals(command.getOverallAsString()) && command.getOverallAsString() != null) {
                review.setQuality(CategoryRating.getCategoryRating(command.getOverallAsString()));
            }

            if (Poster.PARENT.equals(poster) || Poster.OTHER.equals(poster)) {
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getTeacherAsString()) && command.getTeacherAsString() != null) {
                    review.setTeachers(CategoryRating.getCategoryRating(command.getTeacherAsString()));
                }
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getParentAsString()) && command.getParentAsString() != null) {
                    review.setParents(CategoryRating.getCategoryRating(command.getParentAsString()));
                }
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getPrincipalAsString()) && command.getPrincipalAsString() != null) {
                    review.setPrincipal(CategoryRating.getCategoryRating(command.getPrincipalAsString()));
                }
            } else if (Poster.TEACHER.equals(poster)) {
                if (!CategoryRating.DECLINE_TO_STATE.equals(command.getTeacherAsString()) && command.getTeacherAsString() != null) {
                    review.setTeachers(CategoryRating.getCategoryRating(command.getTeacherAsString()));
                }
            }
        }
    }

    protected void sendMessage(final User user, final String comments, final School school, String emailTemplate) throws MessagingException, IOException {

        if (user.getUndeliverable()) {
            _log.warn("Not sending to user marked undeliverable: " + user);
            return;
        }

        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject("Thanks for your feedback");
        emailHelper.setFromEmail("editorial@greatschools.org");
        emailHelper.setFromName("GreatSchools");

        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource("gs/web/school/review/" + emailTemplate);

        emailHelper.setSentToCustomMessage(emailHelper.getHTML_THIS_EMAIL_SENT_TO_EMAIL_MSG());
        emailHelper.addInlineReplacement("EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("STATE", school.getDatabaseState().getAbbreviation());
        emailHelper.addInlineReplacement("USER_COMMENTS", comments);
        emailHelper.addInlineReplacement("SCHOOL_NAME", school.getName());
        emailHelper.addInlineReplacement("SCHOOL_ID", school.getId().toString());
        _emailContentHelper.setCityAndLocalQuestions(school, emailHelper.getInlineReplacements(), "ParentReviewEmail");

        emailHelper.send();
    }

    protected void sendRejectMessage(final User user, final String comments, final School school, String emailTemplate) throws MessagingException, IOException {

        if (user.getUndeliverable()) {
            _log.warn("Not sending to user marked undeliverable: " + user);
            return;
        }

        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject("Thanks for your feedback");
        emailHelper.setFromEmail("editorial@greatschools.org");
        emailHelper.setFromName("GreatSchools");

        emailHelper.setToEmail(user.getEmail());
        emailHelper.readPlainTextFromResource("gs/web/school/review/" + emailTemplate);

        emailHelper.setSentToCustomMessage(emailHelper.getHTML_THIS_EMAIL_SENT_TO_EMAIL_MSG());
        emailHelper.addInlineReplacement("EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("STATE", school.getDatabaseState().getAbbreviation());
        emailHelper.addInlineReplacement("USER_COMMENTS", comments);
        emailHelper.addInlineReplacement("SCHOOLNAME", school.getName());
        emailHelper.addInlineReplacement("SCHOOLID", school.getId().toString());

        emailHelper.send();
    }


    protected ModelAndView errorJSON(HttpServletResponse response, BindException errors) throws IOException {
        StringBuffer buff = new StringBuffer(400);
        buff.append("{\"status\":false,\"errors\":");
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

    public void setEmailContentHelper(EmailContentHelper emailContentHelper) {
        _emailContentHelper = emailContentHelper;
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
}