package gs.web.school.review;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ParentReviewFormController extends SimpleFormController implements ReadWriteController {

    public final static String BEAN_ID = "/school/addComments.page";
    public final static String AJAX_BEAN_ID = "/school/ajaxAddComments.page";
    public final static String RATING_ONLY_BEAN_ID = "/school/addRatings.page";

    private final static String SCHOOL_ATTRIBUTE = "school";

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private Boolean _ajaxPage = Boolean.FALSE;
    private Boolean _updateRatings = Boolean.FALSE;

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        ReviewCommand rc = (ReviewCommand) command;
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        if (school != null) {
            //fatal error - exit immediately
        }
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Object command,
                                             BindException errors) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        if (school == null) {
            //fatal error - exit immediately
        }

        if (isAjaxPage()) {
            return null;
        } else {
            return super.processFormSubmission(request,response, command, errors);
        }
    }


    /**
     * everything validated at this point
     * @param request
     * @param response
     * @param command
     * @param errors
     * @return
     */
    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) {

        ReviewCommand rc = (ReviewCommand) command;
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
        User user = getUserDao().findUserFromEmailIfExists(rc.getEmail());
        boolean existingUser = true;

        if (user == null) {
            user = new User();
            user.setEmail(rc.getEmail());
            getUserDao().saveUser(user);
            //this is for unit test purposes.  so I can return any user.
            user = getUserDao().findUserFromEmailIfExists(rc.getEmail());
            existingUser = false;
        }

        if (isUpdateRatings() && existingUser) {
            Review review = getReviewDao().findReview(user, school);
            getReviewDao().saveReview(review);
            return null;
        } else {
            //should not get here, forward them to thank you page or resubmit page.
        }

        if (existingUser) {
            getReviewDao().removeReviews(user, school);
        }
        Review review = new Review();
        review.setSchool(school);
        review.setUser(user);
        getReviewDao().saveReview(review);

        if (isAjaxPage()) {
            return null;
        } else {
            PageHelper.setMemberCookie(request, response, user);
            ModelAndView mAndV = new ModelAndView();
            return mAndV;
        }
    }

    public static class AjaxPageValidator implements Validator {
        public boolean supports(Class clazz) {
            return clazz.equals(ReviewCommand.class);
        }

        public void validate(Object object, Errors errors) {
            ReviewCommand command = (ReviewCommand) object;

            if (!command.isGivePermission()) {
                errors.reject("addComments_terms_of_use", "Please check the box to agree to our terms of use.");
            }

            if (StringUtils.isBlank(command.getComments()) && CategoryRating.DECLINE_TO_STATE.equals(command.getOverall())) {
                errors.reject("addComments_empty_submission", "You did not enter any reviews or ratings.");
            }
        }
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

    public Boolean isAjaxPage() {
        return _ajaxPage;
    }

    public void setAjaxPage(Boolean ajaxPage) {
        _ajaxPage = ajaxPage;
    }

    public Boolean isUpdateRatings() {
        return _updateRatings;
    }

    public void setUpdateRatings(Boolean updateRatings) {
        _updateRatings = updateRatings;
    }
}
