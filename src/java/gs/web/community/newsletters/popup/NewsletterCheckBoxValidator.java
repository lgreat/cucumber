/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCheckBoxValidator.java,v 1.1 2006/05/03 01:16:16 dlee Exp $
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

    public boolean supports(Class aClass) {
        return aClass.equals(NewsletterCommand.class);
    }

    public void validate(Object object, Errors errors) {
        NewsletterCommand cmd = (NewsletterCommand)object;

        if (!cmd.isGn() && !cmd.isMystat()) {
            errors.reject("errors", "Please check at least one box");
        }
    }
}
