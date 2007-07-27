package gs.web.school.review;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.ReadWriteController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class UpdateRatingsController extends SimpleFormController implements ReadWriteController {

    public static final String BEAN_ID = "updateRatings";

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    protected final Log _log = LogFactory.getLog(getClass());

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        ReviewCommand rc = (ReviewCommand) command;
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);

        if (school != null) {
            //fatal error - exit immediately
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        ReviewCommand rc = (ReviewCommand) command;
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        User user = getUserDao().findUserFromEmailIfExists(rc.getEmail());

        if (user == null) {
            _log.error("User with email not in db yet: " + rc.getEmail());
            return showThankYouPage(school);
        } else {
            //existing user, only updating ratings part of review
            Review review = getReviewDao().findReview(user, school);

            if (review == null) {
                _log.error("could not find a review for school:" + school + " and user: " + user);
                return showThankYouPage(school);
            }

            getReviewDao().saveReview(updateCategoryRatingsFromCommand(review, rc));
            return showThankYouPage(school);
        }
    }

    protected Review updateCategoryRatingsFromCommand(Review review, final ReviewCommand rc) {
        review.setPrincipal(rc.getPrincipal());
        review.setTeachers(rc.getTeacher());
        review.setActivities(rc.getActivities());
        review.setParents(rc.getParent());
        review.setSafety(rc.getSafety());
        
        return review;
    }

    protected ModelAndView showThankYouPage(final School school) {
        ModelAndView mAndV = new ModelAndView();
        mAndV.getModel().put("school", school);
        mAndV.setViewName(getSuccessView());
        return mAndV;
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
}
