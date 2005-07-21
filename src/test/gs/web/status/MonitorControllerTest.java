package gs.web.status;

import gs.web.BaseTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Tests the MonitorController
 */
public class MonitorControllerTest extends BaseTestCase {

    private MonitorController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (MonitorController) _sApplicationContext.getBean(MonitorController.BEAN_ID);
    }

    /**
     * The handle request method connects to the database and gets the build version
     */
    public void testHandleRequest() throws IOException, ServletException {
        ModelAndView mv = _controller.handleRequest(null, null);

        assertTrue(mv.getViewName().indexOf("status") > -1);
        Map model = mv.getModel();
        assertNotNull(model);
        assertTrue(((String) model.get("version")).length() > 0);
        assertTrue(((String) model.get("hostname")).length() > 0);
        assertTrue(((Boolean) model.get("mainReadWrite")).booleanValue());
        assertEquals(model.get("mainError"), "");
        assertTrue(((Boolean) model.get("stateReadWrite")).booleanValue());
        assertEquals(model.get("stateError"), "");
    }

}
