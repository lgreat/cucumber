package gs.web.school.review;

import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import static gs.data.util.XMLUtil.*;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;
import gs.web.tracking.OmnitureSuccessEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class AddParentReviewsController extends SimpleFormController implements ReadWriteController {

    protected final static Log _log = LogFactory.getLog(AddParentReviewsController.class);

    public final static String BEAN_ID = "addParentReviews";
    public final static String AJAX_BEAN_ID = "ajaxAddParentReviews";
    public final static String REST_BEAN_ID = "restAddParentReviews";

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private ISubscriptionDao _subscriptionDao;

    private EmailHelperFactory _emailHelperFactory;

    private Boolean _ajaxPage = Boolean.FALSE;

    private Boolean _restPage = Boolean.FALSE;

    private static Pattern BAD_WORDS = Pattern.compile(".*(fuck|poop[\\s\\.,]|poopie|[\\s\\.,]ass[\\s\\.,]|faggot|[\\s\\.,]gay[\\s\\.,]|nigger|shit|prick[\\s\\.,]|ass-kicker|suck|asshole|dick[\\s\\.,]|Satan|dickhead|piss[\\s\\.,]).*");

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object command,
                                                 BindException errors) throws Exception {
        if (isAjaxPage() && errors.hasErrors()) {
            errorJSON(response, errors);
            return null;
        } else if (isRestPage() && errors.hasErrors()) {
            errorREST(response, errors);
            return null;
        } else {
            return super.processFormSubmission(request, response, command, errors);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        OmnitureSuccessEvent omnitureSuccessEvent = new OmnitureSuccessEvent(request, response);

        ReviewCommand rc = (ReviewCommand) command;
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        User user = getUserDao().findUserFromEmailIfExists(rc.getEmail());
        boolean isNewUser = false;

        //new user - create user, add entry to list_active table,
        if (user == null) {
            user = new User();
            user.setEmail(rc.getEmail());
            getUserDao().saveUser(user);
            //this is for unit test purposes.  so I can return any user.
            user = getUserDao().findUserFromEmailIfExists(rc.getEmail());

            Subscription sub = new Subscription(user, SubscriptionProduct.RATING, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            getSubscriptionDao().saveSubscription(sub);
            isNewUser = true;
        }

        //does user want mss? if user maxed out MSS subs, just don't add.  do not throw error message.
        if (rc.isWantMssNL() && !user.hasReachedMaximumMssSubscriptions()) {
            Subscription sub = new Subscription(user, SubscriptionProduct.MYSTAT, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, omnitureSuccessEvent);
            getSubscriptionDao().addNewsletterSubscriptions(user, Arrays.asList(sub));
        }

        //has user's name changed?
        boolean nameChange = false;
        if (StringUtils.isNotBlank(rc.getFirstName())) {
            user.setFirstName(rc.getFirstName());
            nameChange = true;
        }

        if (StringUtils.isNotBlank(rc.getLastName())) {
            user.setLastName(rc.getLastName());
            nameChange = true;
        }

        if (nameChange) {
            getUserDao().saveUser(user);
        }

        Review r = createOrUpdateReview(user, school, rc, isNewUser);
        //save the review
        getReviewDao().saveReview(r);

        //only send them an email if they submitted a message that is not blank
        if ("a".equals(r.getStatus()) && StringUtils.isNotBlank(r.getComments())) {
            sendMessage(user, r.getComments(), school, "rejectEmail.txt");
        } else if ("u".equals(r.getStatus()) && StringUtils.isNotBlank(r.getComments())) {
            sendMessage(user, r.getComments(), school, "communityEmail.txt");
        }


        //trigger the success events
        if (userRatedOneOrMoreCategories(rc)) {
            omnitureSuccessEvent.add(OmnitureSuccessEvent.SuccessEvent.ParentRating);
        }

        if (StringUtils.isNotBlank(rc.getComments())) {
            omnitureSuccessEvent.add(OmnitureSuccessEvent.SuccessEvent.ParentReview);
        }

        if (isAjaxPage()) {
            successJSON(response);
            return null;
        } else if (isRestPage()) {
            successREST(response);
            return null;
        } else {
            ModelAndView mAndV = new ModelAndView();
            mAndV.setViewName(getSuccessView());
            return mAndV;
        }
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
                                          final ReviewCommand command, final boolean isNewUser) {

        Review review = null;
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
                    //only set the category rating if one was given
                    if (!CategoryRating.DECLINE_TO_STATE.equals(command.getOverall()) && command.getOverall() != null) {
                        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                            review.setPOverall(command.getOverall());
                        } else {
                            review.setQuality(command.getOverall());
                        }
                    }
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
            }
        }

        if (null == review) {
            review = new Review();

            review.setUser(user);
            review.setSchool(school);

            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                review.setPOverall(command.getPFacilities());
                review.setPOverall(command.getPParents());
                review.setPOverall(command.getPProgram());
                review.setPOverall(command.getPSafety());
                review.setPOverall(command.getPTeachers());
                review.setPOverall(command.getOverall());
            } else {
                review.setPrincipal(command.getPrincipal());
                review.setTeachers(command.getTeacher());
                review.setActivities(command.getActivities());
                review.setParents(command.getParent());
                review.setSafety(command.getSafety());
                review.setQuality(command.getOverall());
            }
        }
        review.setHow(command.getClient());
        review.setPoster(command.getPoster());
        review.setComments(command.getComments());
        review.setOriginal(command.getComments());
        review.setAllowContact(command.isAllowContact());

        if (StringUtils.isNotEmpty(command.getFirstName()) || StringUtils.isNotEmpty(command.getLastName())) {
            review.setAllowName(true);
        }

        review.setStatus("u");

        if (StringUtils.isNotBlank(review.getComments())) {
            if (review.getComments().length() < 25) {
                review.setStatus("a");
            }

            if (hasBadWord(review.getComments())) {
                review.setStatus("r");
            }
        } else {
            review.setStatus("a");
        }
        return review;
    }

    protected boolean hasBadWord(final String comments) {
        Matcher m = BAD_WORDS.matcher(comments);
        return m.matches();
    }

    protected void sendMessage(final User user, final String comments, final School school, String emailTemplate) throws MessagingException, IOException {
        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject("Thanks for your feedback");
        emailHelper.setFromEmail("editorial@greatschools.net");
        emailHelper.setFromName("GreatSchools");

        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource("gs/web/school/review/" + emailTemplate);

        emailHelper.setSentToCustomMessage(emailHelper.getHTML_THIS_EMAIL_SENT_TO_EMAIL_MSG());
        emailHelper.addInlineReplacement("EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("STATE", school.getDatabaseState().getAbbreviation());
        emailHelper.addInlineReplacement("USER_COMMENTS", comments);
        emailHelper.addInlineReplacement("SCHOOLNAME", school.getName());
        emailHelper.addInlineReplacement("SCHOOLID", school.getId().toString());

        emailHelper.send();
    }


    protected void errorJSON(HttpServletResponse response, BindException errors) throws IOException {
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
    }

    protected void successJSON(HttpServletResponse response) throws IOException {
        response.setContentType("text/x-json");
        response.getWriter().print("{\"status\":true}");
        response.getWriter().flush();
    }

    protected void errorREST(HttpServletResponse response, BindException errors) throws IOException, ParserConfigurationException, TransformerException {
        Document doc = getDocument("errors");

        List messages = errors.getAllErrors();
        for (Object e : errors.getAllErrors()) {
            ObjectError error = (ObjectError) e;
            Element errorElem = appendElement(doc, "error", error.getDefaultMessage());
            errorElem.setAttribute("key", error.getCode());
        }
        response.setContentType("application/xml");
        serializeDocument(response.getWriter(), doc);
        response.getWriter().flush();
    }

    protected void successREST(HttpServletResponse response) throws IOException {
        response.setContentType("application/xml");
        response.getWriter().print("<success/>");
        response.getWriter().flush();
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

    public Boolean isAjaxPage() {
        return _ajaxPage;
    }

    public void setAjaxPage(Boolean ajaxPage) {
        _ajaxPage = ajaxPage;
    }

    public Boolean isRestPage() {
        return _restPage;
    }

    public void setRestPage(Boolean restPage) {
        _restPage = restPage;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }
}
