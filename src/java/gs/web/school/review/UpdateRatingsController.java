package gs.web.school.review;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for parent review hover form
 */
public class UpdateRatingsController extends SimpleFormController implements ReadWriteController {

    public static final String BEAN_ID = "updateRatings";

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    protected final Log _log = LogFactory.getLog(getClass());

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
            PageHelper.setMemberCookie(request, response, user);
            return showThankYouPage(school);
        }
    }

    protected Review updateCategoryRatingsFromCommand(Review review, final ReviewCommand rc) {
        review.setPrincipal(rc.getPrincipal());
        review.setTeachers(rc.getTeacher());
        review.setActivities(rc.getActivities());
        review.setParents(rc.getParent());
        review.setSafety(rc.getSafety());
        
        review.setPParents(rc.getPParents());
        _log.error("setPParents=" + rc.getPParents());
        review.setPProgram(rc.getPProgram());
        _log.error("setPProgram=" + rc.getPProgram());
        review.setPSafety(rc.getPSafety());
        _log.error("setPSafety=" + rc.getPSafety());
        review.setPTeachers(rc.getPTeachers());
        _log.error("setPTeachers=" + rc.getPTeachers());
        review.setPFacilities(rc.getPFacilities());
        _log.error("setPFacilities=" + rc.getPFacilities());

        return review;
    }

    protected ModelAndView showThankYouPage(final School school) {
        List reviews = getReviewDao().getPublishedReviewsBySchool(school);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(ThankYouController.MODEL_SCHOOL, school);
        model.put(ThankYouController.MODEL_HAS_REVIEW, reviews.size() != 0);
        return new ModelAndView(getSuccessView(), model);
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
