package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.data.util.CmsUtil;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsTopicCenter;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Arrays;

public class CmsTopicCenterControllerTest extends BaseControllerTestCase {
    private CmsTopicCenterController _controller;

    private CmsTopicCenter getSampleTopicCenter() {
        CmsTopicCenter topicCenter = new CmsTopicCenter();

        CmsCategory firstCat = new CmsCategory();
        firstCat.setName("Category 1");
        CmsCategory secondCat = new CmsCategory();
        secondCat.setName("Category 2");
        CmsCategory thirdCat = new CmsCategory();
        thirdCat.setName("Category 3");

        topicCenter.setPrimaryKategory(thirdCat);
        List<CmsCategory> breadcrumbs = Arrays.asList(firstCat, secondCat, thirdCat);
        topicCenter.setPrimaryKategoryBreadcrumbs(breadcrumbs);

        return topicCenter;
    }

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CmsTopicCenterController();
        _controller.setCmsFeatureEmbeddedLinkResolver(new CmsContentLinkResolver());

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    public void testModel() throws Exception {
        CmsUtil.enableCms();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV.getModel().get("topicCenter"));

        CmsUtil.disableCms();
    }

    // TODO: fix this to use expect once controller code uses dao to return a CmsTopicCenter; see CmsFeatureControllerTest.testAdKeywords()
    /*
    public void testAdKeywords() {
        CmsUtil.enableCms();

        CmsTopicCenter topicCenter = getSampleTopicCenter();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        PageHelper referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("state", "CA");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 1");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 2");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 3");

        PageHelper pageHelper = (PageHelper) getRequest().getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        assertEquals("Expected identical ad keywords", referencePageHelper.getAdKeywords(), pageHelper.getAdKeywords());

        CmsUtil.disableCms();
    }
    */
}
