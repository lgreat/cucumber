package gs.web.school.review;

import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class AddParentReviewsController extends SimpleFormController implements ReadWriteController {

    protected final Log _log = LogFactory.getLog(getClass());

    public final static String BEAN_ID = "addParentReviews";
    public final static String AJAX_BEAN_ID = "ajaxAddParentReviews";

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private ISubscriptionDao _subscriptionDao;

    private Boolean _ajaxPage = Boolean.FALSE;

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        ReviewCommand rc = (ReviewCommand) command;
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);

        if (school != null) {
            //fatal error - exit immediately
        }
    }

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
        } else {
            getReviewDao().removeReviews(user, school);
        }

        //does user want mss?
        if (rc.isWantMssNL()) {
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

        //save the review
        getReviewDao().saveReview(createReview(user, school, rc));

        if (isAjaxPage()) {
            successJSON(response);
            return null;
        } else {
            PageHelper.setMemberCookie(request, response, user);
            ModelAndView mAndV = new ModelAndView();
            mAndV.setViewName(getSuccessView());
            return mAndV;
        }
    }

    protected Review createReview(final User user, final School school, final ReviewCommand command) {
        Review review = new Review();
        review.setUser(user);
        review.setSchool(school);

        review.setComments(command.getComments());
        review.setOriginal(command.getComments());

        review.setPrincipal(command.getPrincipal());
        review.setTeachers(command.getTeacher());
        review.setActivities(command.getActivities());
        review.setParents(command.getParent());
        review.setSafety(command.getSafety());
        review.setQuality(command.getOverall());

        review.setPoster(command.getPoster());
        review.setAllowContact(command.isAllowContact());

        if (StringUtils.isNotEmpty(command.getFirstName()) || StringUtils.isNotEmpty(command.getLastName())) {
            review.setAllowName(true);
        }

        return review;
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
}
