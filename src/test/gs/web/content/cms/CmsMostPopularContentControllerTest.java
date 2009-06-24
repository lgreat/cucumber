package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.util.CmsUtil;
import org.springframework.web.servlet.ModelAndView;

public class CmsMostPopularContentControllerTest extends BaseControllerTestCase {
    private CmsMostPopularContentController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CmsMostPopularContentController();
    }

    // TODO: fixme
    public void testModel() throws Exception {
        /*
        CmsUtil.enableCms();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV.getModel().get("links"));

        CmsUtil.disableCms();
        */
    }
}
