/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: NewsletterCheckBoxValidator.java,v 1.8 2009/12/04 22:27:13 chriskimm Exp $
 */
package gs.web.community.newsletters.popup;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.org>
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
