package gs.web.api;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import gs.data.api.ApiAccount;

/**
 * Created by chriskimm@greatschools.net
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
        assertEquals(3, errors.getErrorCount());

        command.setName("name");
        command.setEmail("test@test.com");
        command.setConfirmEmail("test@test.com");
        errors = new BindException(command, "commmand");
        _validator.validate(command, errors);
        assertEquals(0, errors.getErrorCount());

        command = new ApiAccount();
        command.setName("name");
        command.setEmail("test@test.com");
        command.setConfirmEmail("testx@test.com");
        errors = new BindException(command, "commmand");
        _validator.validate(command, errors);
        assertEquals(1, errors.getErrorCount());        
    }
}
