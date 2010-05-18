package gs.web.school.review;

import gs.data.school.review.CategoryRating;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class SchoolReviewValidator implements Validator {
        public boolean supports(Class clazz) {
            return clazz.equals(ReviewCommand.class);
        }

        public void validate(Object object, Errors errors) {
            ReviewCommand command = (ReviewCommand) object;

            if (null == command.getPoster()) {
                errors.rejectValue("posterAsString", "addPR_error_poster", "Please indicate your relationship to this school (parent, student, teacher, etc.).");
            }

            if (StringUtils.isBlank(command.getComments()) || StringUtils.split(command.getComments(), "\t\n ").length < 15) {
                errors.rejectValue("comments", "Review must contain at least 15 words");
            }

            if (null != command.getComments() && command.getComments().length() > 1200) {
                errors.rejectValue("comments", "Review must be shorter than 1,200 characters.");    
            }

            if (CategoryRating.DECLINE_TO_STATE.getName().equals(command.getOverallAsString()) || "0".equals(command.getOverallAsString())) {
                errors.rejectValue("overallAsString", "Please specify a school rating");
            }

        }
    }