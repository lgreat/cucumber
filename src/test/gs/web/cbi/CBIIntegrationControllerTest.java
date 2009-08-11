package gs.web.cbi;

import gs.web.BaseControllerTestCase;
import gs.data.integration.exacttarget.ExactTargetAPI;
import static org.easymock.classextension.EasyMock.*;

public class CBIIntegrationControllerTest extends BaseControllerTestCase {

    private CBIIntegrationController _controller;
    private ExactTargetAPI _exactTargetAPI;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CBIIntegrationController();
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        _controller.setExactTargetAPI(_exactTargetAPI);
    }

    public void testSendTriggeredEmail() throws Exception {
        _controller.handleRequest(getRequest(), getResponse());
    }
}
