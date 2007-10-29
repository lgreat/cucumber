package gs.web.school.review;

import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private ISubscriptionDao _subscriptionDao;

    private EmailHelperFactory _emailHelperFactory;

    private Boolean _ajaxPage = Boolean.FALSE;

    private static Pattern BAD_WORDS = Pattern.compile(".*(fuck|poop[\\s\\.,]|poopie|[\\s\\.,]ass[\\s\\.,]|faggot|[\\s\\.,]gay[\\s\\.,]|nigger|shit|prick[\\s\\.,]|ass-kicker|suck|asshole|dick[\\s\\.,]|Satan|dickhead|piss[\\s\\.,]).*");

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object command,
                                                 BindException errors) throws Exception {
        if (isAjaxPage() && errors.hasErrors()) {
            errorJSON(response, errors);
            return null;
        } else {
            return super.processFormSubmission(request, response, command, errors);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

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
            sendMessage(user, r.getComments(), school.getDatabaseState());
        }

        if (isAjaxPage()) {
            successJSON(response);
            return null;
        } else {
            ModelAndView mAndV = new ModelAndView();
            mAndV.setViewName(getSuccessView());
            return mAndV;
        }
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
                    review.setQuality(command.getOverall());
                    review.setPoster(command.getPoster());
                    return review;
                } else {
                    //only set the category rating if one was given
                    if (!CategoryRating.DECLINE_TO_STATE.equals(command.getOverall()) && command.getOverall() != null) {
                        review.setQuality(command.getOverall());
                    }
                    review.setPosted(new Date());
                    review.setProcessDate(null);
                }
            }
        }

        if (null == review) {
            review = new Review();

            review.setUser(user);
            review.setSchool(school);

            review.setPrincipal(command.getPrincipal());
            review.setTeachers(command.getTeacher());
            review.setActivities(command.getActivities());
            review.setParents(command.getParent());
            review.setSafety(command.getSafety());
            review.setQuality(command.getOverall());
        }

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

    protected void sendMessage(final User user, final String comments, final State state) throws MessagingException, IOException {
        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject("Thanks for your feedback");
        emailHelper.setFromEmail("editorial@greatschools.net");
        emailHelper.setFromName("GreatSchools");

        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource("gs/web/school/review/rejectEmail.txt");

        emailHelper.addInlineReplacement("EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("STATE", state.getAbbreviation());
        emailHelper.addInlineReplacement("USER_COMMENTS", comments);

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

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }
}
