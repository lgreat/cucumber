/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MaximumMssValidator.java,v 1.1 2006/06/15 18:14:10 dlee Exp $
 */
package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *  Validation passes if can sign up for more MSS
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class MaximumMssValidator implements Validator {
    public static final String ERROR_CODE = "max_mss_reached";
    IUserDao _userDao;

    public boolean supports(Class aClass) {
        return aClass.getClass().equals(User.class);
    }

    public void validate(Object object, Errors errors) {
        if (object == null) {
            return;
        }

        User user = (User) object;
        if (user.hasReachedMaximumMssSubscriptions()) {
            errors.reject(MaximumMssValidator.ERROR_CODE, "You have reached the maximum number of My School Stats.");
        }
    }
}
