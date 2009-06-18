package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.util.CmsUtil;
import org.springframework.web.servlet.ModelAndView;

public class CmsTopicCenterControllerTest extends BaseControllerTestCase {
    private CmsTopicCenterController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CmsTopicCenterController();
        _controller.setCmsFeatureEmbeddedLinkResolver(new CmsContentLinkResolver());
    }

    public void testModel() throws Exception {
        CmsUtil.enableCms();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV.getModel().get("topicCenter"));

        CmsUtil.disableCms();
    }
}
