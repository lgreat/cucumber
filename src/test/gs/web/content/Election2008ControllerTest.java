package gs.web.content;

import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008ControllerTest extends BaseControllerTestCase {
    private Election2008Controller _controller;
    private Election2008Command _command;


    public void setUp() throws Exception {
        super.setUp();
        _controller = new Election2008Controller();
        _command = new Election2008Command();
    }

    /**
     * Test that the onSubmit method inserts the command into the model
     */
    public void testOnSubmit() {
        _command.setEmail("aroy@greatschools.net");

        BindException errors = new BindException(_command, "");
        ModelAndView mandv = _controller.onSubmit(getRequest(), getResponse(), _command, errors);

        assertNotNull(mandv.getModel().get("edin08Cmd"));
        Election2008EmailCommand com2 = (Election2008EmailCommand) mandv.getModel().get("edin08Cmd");
        assertEquals(_command.getEmail(), com2.getUserEmail());
        assertTrue(com2.isSuccess());
    }
}
