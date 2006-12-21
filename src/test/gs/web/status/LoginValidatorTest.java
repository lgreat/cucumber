package gs.web.status;

import junit.framework.TestCase;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LoginValidatorTest extends TestCase {

    public void testSupports () throws Exception {
        LoginValidator validator = new LoginValidator();
        assertTrue(validator.supports(Identity.class));
        assertFalse(validator.supports(String.class));
    }

    public void testValidate() throws Exception {
        LoginValidator validator = new LoginValidator();

        Identity identity = new Identity();
        identity.setUsername("gsadmin");
        identity.setPassword("!nd8x");
        Errors errors = new BindException(identity, "identity");
        validator.validate(identity, errors);
        assertEquals(0, errors.getErrorCount());

        identity.setUsername("gsadmin");
        identity.setPassword("badpass");
        errors = new BindException(identity, "identity");
        validator.validate(identity, errors);
        assertEquals(1, errors.getErrorCount());

        identity.setUsername("baduser");
        identity.setPassword("!nd8x");
        errors = new BindException(identity, "identity");
        validator.validate(identity, errors);
        assertEquals(1, errors.getErrorCount());

        identity.setUsername("gsadmin");
        identity.setPassword("!nd8x");
        errors = new BindException(identity, "identity");
        validator.validate(null, errors);
        assertEquals(1, errors.getErrorCount());
    }
}
