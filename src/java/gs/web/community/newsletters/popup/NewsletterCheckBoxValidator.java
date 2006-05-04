/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCheckBoxValidator.java,v 1.3 2006/05/04 18:03:36 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NewsletterCheckBoxValidator implements Validator {
    private static String ERROR_MSG = "Please check at least one box next time.";

    public boolean supports(Class aClass) {
        return aClass.equals(NewsletterCommand.class);
    }

    public void validate(Object object, Errors errors) {
        NewsletterCommand cmd = (NewsletterCommand) object;

        if (!cmd.isChecked()) {
            errors.reject(ERROR_MSG, null, ERROR_MSG);
        }
    }
}
