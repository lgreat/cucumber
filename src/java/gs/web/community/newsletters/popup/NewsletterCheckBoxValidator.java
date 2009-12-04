/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: NewsletterCheckBoxValidator.java,v 1.7 2009/12/04 20:54:13 npatury Exp $
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
            errors.reject("no_checkboxes_checked", ERROR_MSG);
        }
    }
}
