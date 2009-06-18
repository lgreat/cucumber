package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.util.CmsUtil;
import org.springframework.web.servlet.ModelAndView;

public class MostPopularContentControllerTest extends BaseControllerTestCase {
    private MostPopularContentController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new MostPopularContentController();
    }

    public void testModel() throws Exception {
        CmsUtil.enableCms();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV.getModel().get("links"));

        CmsUtil.disableCms();
    }
}
