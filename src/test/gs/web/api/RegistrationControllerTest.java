package gs.web.api;

//import gs.web.BaseControllerTestCase;
import static org.junit.Assert.*;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.BeforeTransaction;


/**
 * @author chriskimm
 */
public class RegistrationControllerTest { // extends BaseControllerTestCase {

    private RegistrationController _controller;
    
    @Before
    public void init() throws Exception {
        _controller = new RegistrationController();
    }

    @Test
    public void testShowForm() throws Exception {
        String out = _controller.showForm();
        assertEquals(RegistrationController.FORM_VIEW_NAME, out);
    }

    public void testFormSubmit() throws Exception {
        // todo
    }
}
