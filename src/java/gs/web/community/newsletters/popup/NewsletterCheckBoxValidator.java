/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCheckBoxValidator.java,v 1.5 2006/06/02 19:22:08 dlee Exp $
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
    private static String ERROR_MSG = "Please check at least one box.";

    public boolean supports(Class aClass) {
        return aClass.equals(NewsletterCommand.class);
    }

    public void validate(Object object, Errors errors) {
        NewsletterCommand cmd = (NewsletterCommand) object;

        if (!cmd.isChecked()) {
            errors.reject("no_checkboxes_checked", "Please check at least one box.");
        }
    }
}
