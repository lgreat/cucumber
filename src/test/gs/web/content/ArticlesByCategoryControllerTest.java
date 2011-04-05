package gs.web.content;

import gs.data.search.ISearchResult;
import gs.data.search.SearchResultsPage;
import gs.web.BaseControllerTestCase;
import gs.web.search.*;
import gs.web.util.PageHelper;
import gs.data.content.cms.CmsCategory;

import static org.easymock.classextension.EasyMock.*;

import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ArticlesByCategoryControllerTest extends BaseControllerTestCase {
    private ArticlesByCategoryController _controller;
    private CmsFeatureSearchService _cmsFeatureSearchService;
    private CmsCategorySearchService _cmsCategorySearchService;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new ArticlesByCategoryController();
        _controller.setViewName("content/articleCategory");

        _cmsFeatureSearchService = createMock(CmsFeatureSearchService.class);
        _cmsCategorySearchService = createMock(CmsCategorySearchService.class);
        _controller.setCmsFeatureSearchService(_cmsFeatureSearchService);
        _controller.setCmsCategorySearchService(_cmsCategorySearchService);

        _controller.setGetParents(true);

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    public void testBasics() {
        assertSame(_cmsFeatureSearchService, _controller.getCmsFeatureSearchService());
        assertSame(_cmsCategorySearchService, _controller.getCmsCategorySearchService());
        assertTrue(_controller.isGetParents());
        assertEquals("content/articleCategory", _controller.getViewName());
    }

    // Test scope is to determine that this method switches based on the URI
    // Testing actual search is out of scope! Use tests that target those methods specifically instead

    /**
     * Test with valid url with topic ids.
     */
    public void testHandleRequestInternalCms() {
        getRequest().setRequestURI("/articles/");
        getRequest().setParameter("topics", "1,2,3");

        expect(_cmsCategorySearchService.getCategoriesFromIds("1,2,3")).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoryFromURI("/articles/")).andReturn(null);

        replay(_cmsCategorySearchService);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        verify(_cmsCategorySearchService);

        assertNotNull(mAndV);
        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    /**
     * Test with legacy url support
     */
    public void testHandleRequestInternalLegacyUrl() {
        getRequest().setRequestURI("/articles/123/blah-blah");

        expect(_cmsCategorySearchService.getCategoryFromURI("/articles/123/blah-blah")).andReturn(null);
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());

        replay(_cmsCategorySearchService);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_cmsCategorySearchService);

        assertNotNull(mAndV);

        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    /**
     * Test with legacy url support that returns a category
     */
    public void testHandleRequestInternalLegacyUrlWithCategory() {
        getRequest().setRequestURI("/articles/123/blah-blah");
        CmsCategory cat = new CmsCategory();
        cat.setId(1);
        cat.setType(CmsCategory.TYPE_TOPIC);
        expect(_cmsCategorySearchService.getCategoryFromURI("/articles/123/blah-blah")).andReturn(cat);
        replay(_cmsCategorySearchService);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_cmsCategorySearchService);

        assertNotNull(mAndV);
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    /**
     * Test with empty url and null categoryIds.
     */
    public void testHandleRequestInternalBlankAndNull() {
        expect(_cmsCategorySearchService.getCategoryFromURI("")).andReturn(null);
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategorySearchService.getCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());

        replay(_cmsCategorySearchService);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_cmsCategorySearchService);

        assertNotNull(mAndV);
        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

     /**
     * Test with nulls
     */
    public void testStoreResultsForCmsCategoriesNoFeatures(){
        expect(_cmsFeatureSearchService.getCmsFeatures(null,null,null,false,null,null,10,1)).andReturn(null);
        replay(_cmsFeatureSearchService);
        _controller.setRandomResults(true);
        _controller.storeResultsForCmsCategories(null,null,null,null,1,false,null,null,String.valueOf(10));
        verify(_cmsFeatureSearchService);
    }

     /**
     * Test with returning one search result.
     */
    public void testStoreResultsForCmsCategoriesFewFeature(){
        SolrCmsCategorySearchResult cmsFeature= new SolrCmsCategorySearchResult();
        List<ISearchResult> searchResults =  new ArrayList();
        searchResults.add(cmsFeature);
        SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = new SearchResultsPage(1,searchResults);
        expect(_cmsFeatureSearchService.getCmsFeatures(null,null,null,false,null,null,10,1)).andReturn(searchResultsPage);
        replay(_cmsFeatureSearchService);
        _controller.setRandomResults(true);
        _controller.storeResultsForCmsCategories(null,null,null,new HashMap(),1,false,null,null,String.valueOf(10));
        verify(_cmsFeatureSearchService);
    }


}