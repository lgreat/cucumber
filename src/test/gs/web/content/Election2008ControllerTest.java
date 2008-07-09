package gs.web.content;

import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;

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
        assertNotNull(com2.getAlert());
    }

    public void testSyncInfoWithConstantContactEuccess() {
        _command.setEmail("aroy@greatschools.net");
        _command.setZip("92130");

        assertTrue("Expect successful sync", _controller.syncInfoWithConstantContact(_command));
    }

    public void testSyncInfoWithConstantContactFailure() {
        _command.setEmail("");
        _command.setZip("92130");

        assertFalse("Expect failure to sync with empty email address",
                _controller.syncInfoWithConstantContact(_command));
    }

    public void testReferenceData() {
        Map map = _controller.referenceData(getRequest());

        assertNotNull("Expect presence of random stat in reference data", map.get("startlingStat"));
        assertTrue("Expect random stat to be drawn from array",
                ArrayUtils.contains(Election2008Controller.stats.toArray(), map.get("startlingStat")));
    }
}
