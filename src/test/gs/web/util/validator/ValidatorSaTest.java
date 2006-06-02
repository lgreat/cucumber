/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ValidatorSaTest.java,v 1.2 2006/06/02 19:22:08 dlee Exp $
 */
package gs.web.util.validator;

import gs.data.state.State;
import gs.web.community.newsletters.popup.NewsletterCheckBoxValidator;
import gs.web.community.newsletters.popup.NewsletterCommand;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Test Validators.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ValidatorSaTest extends TestCase {
    private static final Log _log = LogFactory.getLog(ValidatorSaTest.class);

    public void testEmailValidator() {
        final String GOOD_EMAIL = "dlee@greatschools.net";
        final String BAD_EMAIL = "dleegreatschools.net";

        Validator v = new EmailValidator();
        NewsletterCommand command = new NewsletterCommand();
        Errors errors = new BindException(command,"");

        command.setEmail(GOOD_EMAIL);
        v.validate(command,errors);
        assertFalse(errors.hasErrors());

        command.setEmail(BAD_EMAIL);
        v.validate(command,errors);
        assertTrue(errors.hasErrors());
    }

    public void testStateValidator() {
        final State GOOD_STATE = State.CA;
        final State BAD_STATE = null;

        Validator v = new StateValidator();
        NewsletterCommand command = new NewsletterCommand();
        Errors errors = new BindException(command, "");

        command.setState(GOOD_STATE);
        v.validate(command, errors);
        assertFalse(errors.hasErrors());

        command.setState(BAD_STATE);
        v.validate(command, errors);
        assertTrue(errors.hasErrors());

    }

    public void testSchoolIdValidator() {
        final int GOOD_SCHOOLID = 1;
        int BAD_SCHOOLID = 0;

        Validator v = new SchoolIdValidator();
        NewsletterCommand command = new NewsletterCommand();
        Errors errors = new BindException(command, "");

        command.setSchoolId(GOOD_SCHOOLID);
        v.validate(command, errors);
        assertFalse(errors.hasErrors());

        command.setSchoolId(BAD_SCHOOLID);
        v.validate(command, errors);
        assertTrue(errors.hasErrors());

        errors = new BindException(command, "");
        BAD_SCHOOLID = -1;
        v.validate(command, errors);
        assertTrue(errors.hasErrors());
    }

    public void testNewsCommandCheckboxValidator() {
        Validator v = new NewsletterCheckBoxValidator();
        NewsletterCommand command = new NewsletterCommand();
        Errors errors = new BindException(command, "");

        //no check boxes
        v.validate(command, errors);
        assertTrue(errors.hasErrors());


        command.setGn(true);
        command.setMystat(true);
        command.setMyMs(true);
        command.setMyHs(true);
        command.setMy1(true);
        command.setMy2(true);
        command.setMy3(true);
        command.setMy4(true);
        command.setMy5(true);
        command.setMyk(true);
        assertTrue(command.isChecked());
        //reset since previous validator triggered an error
        errors = new BindException(command, "");
        v.validate(command, errors);
        assertFalse(errors.hasErrors());
    }
}
