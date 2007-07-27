package gs.web.school.review;

import gs.data.school.review.CategoryRating;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class AddParentReviewsValidator implements Validator {
        public boolean supports(Class clazz) {
            return clazz.equals(ReviewCommand.class);
        }

        public void validate(Object object, Errors errors) {
            ReviewCommand command = (ReviewCommand) object;

            if (!StringUtils.equals(command.getEmail(), command.getConfirmEmail())) {
                errors.rejectValue("confirmEmail", "addPR_error_confirmation_email", "The confirmation email is not the same as your email.");
            }

            if (null == command.getPoster()) {
                errors.rejectValue("posterAsString", "addPR_error_poster", "Please indicate your relationship to this school (parent, student, teacher, etc.).");
            }

            if (!command.isGivePermission()) {
                errors.rejectValue("givePermission", "addPR_error_permission", "Please accept our terms of use.");
            }

            if (StringUtils.isBlank(command.getComments()) && CategoryRating.DECLINE_TO_STATE.equals(command.getOverall())) {
                errors.rejectValue("comments", "addPR_error_comments", "Please enter a review or rating.");
            }
        }
    }
