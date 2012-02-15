package gs.web.content.cms;

import gs.data.content.cms.*;
import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.data.content.IArticleDao;
import gs.data.util.CmsUtil;

import static org.easymock.EasyMock.*;

import gs.web.util.RedirectView301;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

public class CmsFeatureControllerTest extends BaseControllerTestCase {
    private CmsFeatureController _controller;
    private ICmsFeatureDao _cmsFeatureDao;
    private IArticleDao _legacyArticleDao;

    private CmsFeature getSampleFeature() {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", 23L));
        feature.setFullUri("/blah/blah/blah");

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
        secondCat.setId(2);
        secondCat.setName("Category 2");
        CmsCategory thirdCat = new CmsCategory();
        thirdCat.setId(3);
        thirdCat.setName("Category 3");

        CmsCategory secondaryFirstCatNum = new CmsCategory();
        secondaryFirstCatNum.setId(4);
        secondaryFirstCatNum.setName("2nd Cat 1");
        CmsCategory secondarySecondCatNum = new CmsCategory();
        secondarySecondCatNum.setId(5);
        secondarySecondCatNum.setName("2nd Cat 2");
        CmsCategory secondaryFirstCatLet = new CmsCategory();
        secondaryFirstCatLet.setId(6);
        secondaryFirstCatLet.setName("2nd Cat A");
        CmsCategory secondarySecondCatLet = new CmsCategory();
        secondarySecondCatLet.setId(7);
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
        // used to have unit test ignore 301-redirect code that is too hard to unit test due to embedded urlbuilder
        // that uses publicationDao
        _controller.setUnitTest(true);

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

    public void resetAll() {
        reset(_cmsFeatureDao);
        reset(_legacyArticleDao);
    }

    public void testShowContextualAds() {
        CmsFeature feature = getSampleFeature();
        feature.setContentKey(ContentKey.valueOf("Article#123"));
        CmsCategory first = new CmsCategory();
        first.setName("First, with a comma");
        first.setId(CmsConstants.ACADEMICS_AND_ACTIVITIES_CATEGORY_ID);
        CmsCategory second = new CmsCategory();
        second.setName("\"Second\"");
        second.setId(CmsConstants.COLLEGE_PREP_CATEGORY_ID);
        List<CmsCategory> breadcrumbs = Arrays.asList(first, second);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        assertTrue(CmsFeatureController.getShowContextualAds(feature, "", false, false));

        feature.setContentKey(ContentKey.valueOf("ArticleSlideshow#123"));
        assertFalse(CmsFeatureController.getShowContextualAds(feature, "", false, false));
        feature.setContentKey(ContentKey.valueOf("AskTheExperts#123"));
        assertFalse(CmsFeatureController.getShowContextualAds(feature, "", false, false));

        feature.setContentKey(ContentKey.valueOf("Article#123"));
        first = new CmsCategory();
        first.setName("First, with a comma");
        first.setId(CmsConstants.FIFTH_GRADE_CATEGORY_ID);
        second = new CmsCategory();
        second.setName("\"Second\"");
        second.setId(CmsConstants.COLLEGE_PREP_CATEGORY_ID);
        breadcrumbs = Arrays.asList(first, second);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        assertFalse(CmsFeatureController.getShowContextualAds(feature, "", false, false));

        first = new CmsCategory();
        first.setName("First, with a comma");
        first.setId(CmsConstants.GREAT_GIFTS_CATEGORY_ID);
        second = new CmsCategory();
        second.setName("\"Second\"");
        second.setId(CmsConstants.COLLEGE_PREP_CATEGORY_ID);
        breadcrumbs = Arrays.asList(first, second);
        List<List<CmsCategory>> secondaryKategoryBreadcrumbs = new ArrayList<List<CmsCategory>>();
        secondaryKategoryBreadcrumbs.add(breadcrumbs);
        feature.setSecondaryKategoryBreadcrumbs(secondaryKategoryBreadcrumbs);
        assertTrue(CmsFeatureController.getShowContextualAds(feature, "", false, false));

        feature.setContentKey(ContentKey.valueOf("Article#123"));
        assertTrue(CmsFeatureController.getShowContextualAds(feature, null, false, false));
        assertTrue(CmsFeatureController.getShowContextualAds(feature, "", false, false));
        assertFalse(CmsFeatureController.getShowContextualAds(feature, "123", false, false));
        feature.setContentKey(ContentKey.valueOf("Article#123"));
        assertFalse(CmsFeatureController.getShowContextualAds(feature, "890,123,456", false, false));
        feature.setContentKey(ContentKey.valueOf("Article#123"));
        assertTrue(CmsFeatureController.getShowContextualAds(feature, "890, 123, 456", false, false));
        feature.setContentKey(ContentKey.valueOf("Article#12"));
        assertTrue(CmsFeatureController.getShowContextualAds(feature, "890,123,456", false, false));

        assertFalse(CmsFeatureController.getShowContextualAds(feature, "890,123,456", true, false));
        assertFalse(CmsFeatureController.getShowContextualAds(feature, "890,123,456", false, true));
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
        assertEquals("<p>first</p><p>second</p><p>third</p>", currentPage);
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


    public void testPagination() {
        CmsFeature feature = getSampleFeature();
        feature.setBody("First page<hr class=\"page-break\"/>Second page");
        feature.setTitle("The title");

        getRequest().setRequestURI("/blah/blah/23-blah.gs");

        // Test fetching the 1st page (the default)
        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        CmsUtil.enableCms();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        Object currentPage = mAndV.getModel().get("currentPage");
        assertNotNull("Expect entire article to be injected into model", currentPage);
        assertEquals("First page", currentPage);

        // Test fetching the 1st page explicitly  
        resetAll();
        getRequest().setParameter("page", "1");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);

        replayAll();
        CmsUtil.enableCms();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        assertNotNull(mAndV);
        assertTrue(mAndV.getView() instanceof RedirectView301);
        assertEquals("/blah/blah/23-blah.gs", ((RedirectView301) mAndV.getView()).getUrl());

        // Now test fetching the 2nd page
        resetAll();
        getRequest().setParameter("page", "2");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        CmsUtil.enableCms();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        currentPage = mAndV.getModel().get("currentPage");
        assertNotNull("Expect current article page to be injected into model", currentPage);
        assertEquals("Second page", currentPage);

        // Now test fetching a page that doesn't exist.
        resetAll();
        getRequest().setParameter("page", "3");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);

        replayAll();
        CmsUtil.enableCms();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        assertNotNull("ModelAndView should not be null", mAndV);
        assertTrue("ModelAndView should be a 301 redirect", mAndV.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be canonical url", "/blah/blah/23-blah.gs", ((RedirectView301) mAndV.getView()).getUrl());

        // Now test fetching all pages
        resetAll();
        getRequest().setParameter("page", "all");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        CmsUtil.enableCms();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        currentPage = mAndV.getModel().get("currentPage");
        assertNotNull("Expect current article page to be injected into model", currentPage);
        assertEquals("First pageSecond page", currentPage);

        // Now test fetching all pages a different way
        resetAll();
        getRequest().setParameter("print", "true");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);

        replayAll();
        CmsUtil.enableCms();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        assertNotNull("ModelAndView should not be null", mAndV);
        assertTrue("ModelAndView should be a 301 redirect", mAndV.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be canonical url", "/print-view/blah/blah/23-blah.gs", ((RedirectView301) mAndV.getView()).getUrl());

        // Now test fetching all pages a different way
        resetAll();
        getRequest().setParameter("page", "-1");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);

        replayAll();
        CmsUtil.enableCms();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        CmsUtil.disableCms();
        verifyAll();

        assertNotNull("ModelAndView should not be null", mAndV);
        assertTrue("ModelAndView should be a 301 redirect", mAndV.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be canonical url", "/print-view/blah/blah/23-blah.gs", ((RedirectView301) mAndV.getView()).getUrl());

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
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat1");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat2");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatA");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatB");
        referencePageHelper.addAdKeyword("article_id", "23");

        PageHelper pageHelper = (PageHelper) getRequest().getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        Collection referenceEditorialKeywords = (Collection)referencePageHelper.getAdKeywords().get("editorial");
        Collection actualEditorialKeywords = (Collection)pageHelper.getAdKeywords().get("editorial");
        assertEquals(referenceEditorialKeywords.size(), actualEditorialKeywords.size());

        SortedSet referencedEditorialKeywordsSorted = new TreeSet();
        referencedEditorialKeywordsSorted.addAll(referenceEditorialKeywords);
        SortedSet actualEditorialKeywordsSorted = new TreeSet();
        actualEditorialKeywordsSorted.addAll(actualEditorialKeywords);
        assertEquals("Expected identical ad keywords", referencedEditorialKeywordsSorted, actualEditorialKeywordsSorted);

        CmsUtil.disableCms();
    }

    public void testCompanionAd() {
        CmsUtil.enableCms();

        getRequest().setRequestURI("/blah/blah/blah.gs");
        getRequest().setParameter("content", "23");

        CmsFeature feature = getSampleFeature();
        feature.setTitle("title with \"double quotes\", and commas");
        feature.setBody("Some text with a link to a video http://assets.delvenetworks.com/player/loader.swf");

        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAll();

        assertNotNull(mAndV.getModel().get("showCompanionAd"));
        assertEquals(true, mAndV.getModel().get("showCompanionAd"));


        resetAll();

        feature.setBody("Some text without a link to a video.");
        expect(_cmsFeatureDao.get(23L)).andReturn(feature);
        expect(_legacyArticleDao.getArticleComments(feature.getContentKey())).andReturn(null);

        replayAll();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAll();

        assertNull(mAndV.getModel().get("showCompanionAd"));
        CmsUtil.disableCms();
    }


    public void testSetAdsInFeatureWithFeatureAd() {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", 368L));
        feature.setBody("<p>The first paragraph</p><hr/><p class=\"feature-ad hidden\">[FEATURE AD]</p><p>Just some random other paragraph</p>");
        _controller.setAdsInFeature(feature, getRequest());
        //only one ad can be set.Either a feature ad or the bts list ad.
        assertNotNull(feature.getFeatureAdCode());
        assertNull(feature.getBtsListAdCode());
    }

    public void testSetAdsInFeatureWithNoFeatureAd() {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", 368L));
        feature.setBody("<p>The first paragraph</p><hr/><p>Just some random other paragraph</p>");
        _controller.setAdsInFeature(feature, getRequest());
        assertNull(feature.getFeatureAdCode());
        assertNull(feature.getBtsListAdCode());
    }
    
}