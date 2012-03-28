package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class ReviewHelper {

    protected final static Log _log = LogFactory.getLog(ReviewHelper.class);

    /**
     * Updates Review with data from ReviewCommand. Does not save Review.
     * @param review
     * @param school
     * @param command
     */
    public void updateReview(Review review, final School school, final ReviewCommand command) {

        reset(review);
        _log.warn("updating a non empty review with a new comment.\nOld Comment: "
                + review.getComments() +
                "\nNew Comment: " + command.getComments() + "\nOld processor: " + review.getSubmitter()
                + "\nOld Note: " + review.getNote());

        populateReviewFromCommand(review, school, command);

    }

    /**
     * Reset necessary fields when updating existing review.
     * @param review
     */
    private void reset(Review review) {
        review.setPosted(new Date());
        review.setProcessDate(null);
        //new review submitted, so set the processor to null
        review.setSubmitter(null);
        review.setNote(null);
    }

    /**
     * Use ReviewCommand to set data on a Review
     * @param review
     * @param school
     * @param command
     */
    public void populateReviewFromCommand(Review review, School school, ReviewCommand command) {
        setRatingsOnReview(school.getLevelCode(), command, review, command.getPoster());

        review.setHow(command.getClient());
        review.setPoster(command.getPoster());
        review.setComments(StringUtils.abbreviate(command.getComments(), 1200));
        review.setOriginal(command.getComments());
        review.setAllowContact(command.isAllowContact());
        review.setIp(command.getIp());

        if (StringUtils.isNotEmpty(command.getFirstName()) || StringUtils.isNotEmpty(command.getLastName())) {
            review.setAllowName(true);
        }
    }

    /**
     * Instantiate and set data on a new Review. Does not save review
     * @param user
     * @param school
     * @param command
     * @return
     */
    public Review createReview(final User user, final School school, final ReviewCommand command) {

        Review review = new Review();
        review.setUser(user);
        review.setSchool(school);

        populateReviewFromCommand(review, school, command);

        return review;
    }

    /**
     * Logic required to set Review ratings, based on School level code and given Poster
     * @param schoolLevelCode
     * @param command
     * @param review
     * @param poster
     */
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

}
