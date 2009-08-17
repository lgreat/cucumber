package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.data.content.cms.ICmsFeatureDao;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.CmsCategory;
import gs.data.content.IArticleDao;
import gs.data.util.CmsUtil;

import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class CmsFeatureControllerTest extends BaseControllerTestCase {
    private CmsFeatureController _controller;
    private ICmsFeatureDao _cmsFeatureDao;
    private IArticleDao _legacyArticleDao;

    private CmsFeature getSampleFeature() {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", 23L));

        feature.setBody("Hello! Visit us " +
                "<a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>." +
                " Also, you may want to go <a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>.");

        feature.setSummary("Hello again! Visit us " +
                "<a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>." +
                " Also, you may want to go <a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>.");

        CmsCategory firstCat = new CmsCategory();
        firstCat.setId(1);
        firstCat.setName("Category 1");
        CmsCategory secondCat = new CmsCategory();
        firstCat.setId(2);
        secondCat.setName("Category 2");
        CmsCategory thirdCat = new CmsCategory();
        firstCat.setId(3);
        thirdCat.setName("Category 3");

        CmsCategory secondaryFirstCatNum = new CmsCategory();
        secondaryFirstCatNum.setId(1);
        secondaryFirstCatNum.setName("2nd Cat 1");
        CmsCategory secondarySecondCatNum = new CmsCategory();
        secondarySecondCatNum.setId(4);
        secondarySecondCatNum.setName("2nd Cat 2");
        CmsCategory secondaryFirstCatLet = new CmsCategory();
        secondaryFirstCatLet.setId(5);
        secondaryFirstCatLet.setName("2nd Cat A");
        CmsCategory secondarySecondCatLet = new CmsCategory();
        secondarySecondCatLet.setId(6);
        secondarySecondCatLet.setName("2nd Cat B");

        feature.setPrimaryKategory(thirdCat);
        List<CmsCategory> breadcrumbs = Arrays.asList(firstCat, secondCat, thirdCat);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);

        List<CmsCategory> secondaryBreadcrumbsNum = Arrays.asList(secondaryFirstCatNum, secondarySecondCatNum);
        List<CmsCategory> secondaryBreadcrumbsLet = Arrays.asList(secondaryFirstCatLet, secondarySecondCatLet);
        List<List<CmsCategory>> secondaryBreadcrumbs = Arrays.asList(secondaryBreadcrumbsNum, secondaryBreadcrumbsLet);
        feature.setSecondaryKategoryBreadcrumbs(secondaryBreadcrumbs);

        return feature;
    }

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CmsFeatureController();

        _cmsFeatureDao = createStrictMock(ICmsFeatureDao.class);
        _legacyArticleDao = createStrictMock(IArticleDao.class);

        _controller.setCmsFeatureDao(_cmsFeatureDao);
        _controller.setArticleDao(_legacyArticleDao);
        _controller.setCmsFeatureEmbeddedLinkResolver(new CmsContentLinkResolver());

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    public void replayAll() {
        replay(_cmsFeatureDao);
        replay(_legacyArticleDao);
    }

    public void verifyAll() {
        verify(_cmsFeatureDao);
        verify(_legacyArticleDao);
    }
    
    public void testOmnitureTracking() {
        CmsUtil.enableCms();

        getRequest().setRequestURI("/blah/blah/blah.gs");
        getRequest().setParameter("content", "23");

        CmsFeature feature = getSampleFeature();
        feature.setTitle("title with \"double quotes\", and commas");
        CmsCategory first = new CmsCategory();
        first.setName("First, with a comma");
        CmsCategory second = new CmsCategory();
        second.setName("\"Second\"");
        List<CmsCategory> breadcrumbs = Arrays.asList(first, second);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAll();

        Object titleForOmniture = mAndV.getModel().get("titleForOmniture");
        assertNotNull(titleForOmniture);
        assertEquals("title with double quotes and commas", titleForOmniture);

        Object csvKategoryNames = mAndV.getModel().get("commaSeparatedPrimaryKategoryNames");
        assertNotNull(csvKategoryNames);
        assertEquals("First with a comma,Second", csvKategoryNames);

        CmsUtil.disableCms();
    }

    public void testInsertCurrentPageIntoModel() {

        getRequest().setRequestURI("/blah/blah/blah.gs");
        getRequest().setParameter("content", "23");

        CmsFeature feature = getSampleFeature();
        feature.setTitle("title with \"double quotes\", and commas");
        CmsCategory first = new CmsCategory();
        first.setName("First, with a comma");
        CmsCategory second = new CmsCategory();
        second.setName("\"Second\"");
        List<CmsCategory> breadcrumbs = Arrays.asList(first, second);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        feature.setBody("<p>first</p><p>second</p><p>third</p>");
        feature.setSidebar("<p>sidebar</p>");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        CmsUtil.enableCms();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        Object currentPage = mAndV.getModel().get("currentPage");
        assertNotNull("Expect current article page to be injected into model", currentPage);
        assertEquals("<p>first</p><p>second</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div><p>third</p>", currentPage);
    }

    public void testInsertCurrentPageIntoModelNoSidebar() {

        getRequest().setRequestURI("/blah/blah/blah.gs");
        getRequest().setParameter("content", "23");

        CmsFeature feature = getSampleFeature();
        feature.setTitle("title with \"double quotes\", and commas");
        CmsCategory first = new CmsCategory();
        first.setName("First, with a comma");
        CmsCategory second = new CmsCategory();
        second.setName("\"Second\"");
        List<CmsCategory> breadcrumbs = Arrays.asList(first, second);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        feature.setBody("<p>first</p><p>second</p><p>third</p>");
        feature.setSidebar(null);

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        CmsUtil.enableCms();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        Object currentPage = mAndV.getModel().get("currentPage");
        assertNotNull("Expect current article page to be injected into model", currentPage);
        assertEquals("<p>first</p><p>second</p><p>third</p>", currentPage);
    }

    public void testInsertSidebarIntoPageSinglePageThreeParagraphBody() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><p>second</p><p>third</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(-1); // single page view

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><p>second</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div><p>third</p>", currentPage);
    }

    public void testInsertSidebarIntoPageSinglePageTwoParagraphBody() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><p>second</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(-1); // single page view

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><p>second</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div>", currentPage);
    }

    public void testInsertSidebarIntoPageSinglePageLessThanTwoParagraphBody() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(-1); // single page view

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div>", currentPage);
    }

    public void testInsertSidebarIntoPageFirstPage() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><p>second</p><p>third</p><hr class=\"page-break\"/><p>pg2-first</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(1);

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><p>second</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div><p>third</p>", currentPage);
    }

    public void testInsertSidebarIntoPageSecondPage() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><p>second</p><p>third</p><hr class=\"page-break\"/><p>pg2-first</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(2);

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>pg2-first</p>", currentPage);
    }

    public void testInsertSidebarIntoPageFirstPageOneParagraphAndHasSecondPage() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><hr class=\"page-break\"/><p>pg2-first</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(1);

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div>", currentPage);
    }

    public void testInsertSidebarIntoPageMultiPageThreeParagraphsTotal() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><hr class=\"page-break\"/><p>pg2-first</p><p>pg2-second</p><p>pg2-third</p>");
        feature.setSidebar("<p>sidebar</p>");
        feature.setCurrentPageNum(-1); // single page view

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><p>pg2-first</p><div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div><p>sidebar</p><div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div><p>pg2-second</p><p>pg2-third</p>", currentPage);
    }

    public void testInsertSidebarIntoPageNoSidebar() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("<p>first</p><hr class=\"page-break\"/><p>pg2-first</p><p>pg2-second</p><p>pg2-third</p>");
        feature.setSidebar(null);
        feature.setCurrentPageNum(-1); // single page view

        String currentPage = _controller.insertSidebarIntoPage(feature.getCurrentPage(), feature);
        assertEquals("<p>first</p><p>pg2-first</p><p>pg2-second</p><p>pg2-third</p>", currentPage);
    }

    public void testAdKeywords() {
        CmsUtil.enableCms();

        getRequest().setRequestURI("/blah/blah/blah.gs");
        getRequest().setParameter("content", "23");

        CmsFeature feature = getSampleFeature();
        feature.setTitle("title with \"double quotes\", and commas");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAll();

        PageHelper referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("state", "CA");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 1");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 2");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 3");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat2");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatA");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatB");
        referencePageHelper.addAdKeyword("article_id", "23");

        PageHelper pageHelper = (PageHelper) getRequest().getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        assertEquals("Expected identical ad keywords", referencePageHelper.getAdKeywords(), pageHelper.getAdKeywords());

        CmsUtil.disableCms();
    }

    public void testCheckTargetSupplyList() {
        Map<String, Object> model;
        CmsFeature feature = new CmsFeature();
        ContentKey key;

        // elementary
        model = new HashMap<String, Object>();
        key = new ContentKey("Article", 1082l);
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNotNull("Expect supply list in model for article 1082", model.get("targetSupplyItems"));

        // middle
        model = new HashMap<String, Object>();
        key = new ContentKey("Article", 1084l);
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNotNull("Expect supply list in model for article 1084", model.get("targetSupplyItems"));

        // high
        model = new HashMap<String, Object>();
        key = new ContentKey("Article", 1085l);
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNotNull("Expect supply list in model for article 1085", model.get("targetSupplyItems"));

        // generic
        model = new HashMap<String, Object>();
        key = new ContentKey("Article", 109l);
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNotNull("Expect supply list in model for article 109", model.get("targetSupplyItems"));

        // other
        model = new HashMap<String, Object>();
        key = new ContentKey("Article", 1083l);
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNull("Expect no supply list in model for article 1083", model.get("targetSupplyItems"));

        // edge conditions
        // ask the experts
        model = new HashMap<String, Object>();
        key = new ContentKey("Ask the experts", 1082l);
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNull("Expect supply list in model for ask the experts 1082", model.get("targetSupplyItems"));

        // null identifier
        model = new HashMap<String, Object>();
        key = new ContentKey();
        key.setType("Article");
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNull("Expect no supply list in model for article 1083", model.get("targetSupplyItems"));

        // null type
        model = new HashMap<String, Object>();
        key = new ContentKey();
        feature.setContentKey(key);
        _controller.checkTargetSupplyList(feature, model);
        assertNull("Expect no supply list in model for article 1083", model.get("targetSupplyItems"));

        // null key
        model = new HashMap<String, Object>();
        feature.setContentKey(null);
        _controller.checkTargetSupplyList(feature, model);
        assertNull("Expect no supply list in model for article 1083", model.get("targetSupplyItems"));

        // null feature
        model = new HashMap<String, Object>();
        _controller.checkTargetSupplyList(null, model);
        assertNull("Expect no supply list in model for article 1083", model.get("targetSupplyItems"));
    }
}