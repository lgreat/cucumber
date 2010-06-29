package gs.web.api;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import gs.data.api.ApiAccount;

import java.util.List;

/**
 * Created by chriskimm@greatschools.org
 */
public class ApiAccountCommandValidatorTest {

    ApiAccountCommandValidator _validator;

    @Before
    public void setup() {
        _validator = new ApiAccountCommandValidator();
    }

    @Test
    public void testValidatorSupports() {
        assertFalse(_validator.supports(String.class));
        assertTrue(_validator.supports(ApiAccount.class));
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testValidateCommand() {
        ApiAccount command = new ApiAccount();
        Errors errors = new BindException(command, "commmand");
        _validator.validate(command, errors);
        assertEquals(9, errors.getErrorCount());

        command.setIndustry("education");
        command.setWebsite("www.greatschools.org");
        command.setName("name");
        command.setEmail("test@test.com");
        command.setConfirmEmail("test@test.com");
        errors = new BindException(command, "commmand");
        _validator.validate(command, errors);
        assertEquals(4, errors.getErrorCount());
        assertNotNull(errors.getFieldError("termsApproved"));
        assertNotNull(errors.getFieldError("organization"));

        command = new ApiAccount();
        command.setIndustry("education");
        command.setWebsite("www.greatschools.org");
        command.setOrganization("Big Co");
        command.setName("name");
        command.setEmail("test@test.com");
        command.setConfirmEmail("test@test.com");
        command.setPhone("(415)-595-0505");
        command.setIntendedUse("test");
        command.setTermsApproved(true);
        errors = new BindException(command, "commmand");
        _validator.validate(command, errors);
        assertEquals(0, errors.getErrorCount());
        assertNull(errors.getFieldError("confirmEmail"));

        command = new ApiAccount();
        command.setIndustry("education");
        command.setWebsite("www.greatschools.org");
        command.setName("name");
        command.setEmail("test@test.com");
        command.setConfirmEmail("testx@test.com");
        command.setPhone("(415)-595-0505");
        command.setIntendedUse("test");
        errors = new BindException(command, "commmand");
        _validator.validate(command, errors);
        assertEquals(3, errors.getErrorCount());
        assertNotNull(errors.getFieldError("confirmEmail"));
    }
}
