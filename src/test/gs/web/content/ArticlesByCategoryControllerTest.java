package gs.web.content;

import gs.data.content.ArticleCategory;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ICmsCategoryDao;
import gs.data.search.ISearchResult;
import gs.data.search.SearchResultsPage;
import gs.web.BaseControllerTestCase;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.search.SolrCmsCategorySearchResult;
import gs.web.util.PageHelper;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ArticlesByCategoryControllerTest extends BaseControllerTestCase {
    private ArticlesByCategoryController _controller;
    private CmsFeatureSearchService _cmsFeatureSearchService;
    private IArticleCategoryDao _dao;
    private ICmsCategoryDao _cmsCategoryDao;
    public void setUp() throws Exception {
        super.setUp();

        _controller = new ArticlesByCategoryController();
        _controller.setViewName("content/articleCategory");

        _dao = createStrictMock(IArticleCategoryDao.class);
        _controller.setArticleCategoryDao(_dao);

        _cmsCategoryDao = createStrictMock(ICmsCategoryDao.class);
        _cmsFeatureSearchService = createMock(CmsFeatureSearchService.class);
        _controller.setSolrCmsFeatureSearchService(_cmsFeatureSearchService);
        _controller.setCmsCategoryDao(_cmsCategoryDao);

        _controller.setGetParents(true);

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    public void testBasics() {
        assertSame(_cmsFeatureSearchService, _controller.getSolrCmsFeatureSearchService());
        assertSame(_dao, _controller.getArticleCategoryDao());
        assertTrue(_controller.isGetParents());
        assertEquals("content/articleCategory", _controller.getViewName());
    }

    // Test scope is to determine that this method switches based on the URI
    // Testing actual search is out of scope! Use tests that target those methods specifically instead

    /**
     * Test with valid url with topic ids.
     */
    public void testHandleRequestInternalCms() {
        getRequest().setQueryString("topics=1,2,3");
        getRequest().setRequestURI("/articles/");
        getRequest().setParameter("topics","1,2,3");

        expect(_cmsCategoryDao.getCmsCategoryFromURI("/articles/")).andReturn(null);
        expect(_cmsCategoryDao.getCmsCategoriesFromIds(isA(Long[].class))).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategoryDao.separateCategories(isA(ArrayList.class))).andReturn(new ICmsCategoryDao.CmsCategoryTriplet());

        replay(_cmsCategoryDao);
        replay(_dao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_cmsCategoryDao);
        verify(_dao);

        assertNotNull(mAndV);

        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    // Test scope is to determine that this method switches based on the URI
    // Testing actual search is out of scope! Use tests that target those methods specifically instead
    public void testHandleRequestInternalLegacy() {
        String testURI = "/articles/123/blah-blah";
        getRequest().setRequestURI(testURI);
        Long[] nullLong = null;

        expect(_cmsCategoryDao.getCmsCategoryFromURI(eq(testURI))).andReturn(new CmsCategory());
        expect(_cmsCategoryDao.getCmsCategoriesFromIds(isA(Long[].class))).andReturn(new ArrayList<CmsCategory>());
        expect(_cmsCategoryDao.separateCategories(isA(ArrayList.class))).andReturn(new ICmsCategoryDao.CmsCategoryTriplet());

        replay(_dao);
        replay(_cmsCategoryDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_cmsCategoryDao);

        assertNotNull(mAndV);

        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    public void testGetCategoriesFromIdNull() {
        try {
            _controller.getCategoriesFromId(null);
            fail("Should throw NumberFormatException with null id");
        } catch (NumberFormatException npe) {
            // good
        }
    }

    public void testGetCategoriesFromIdInvalid() {
        try {
            _controller.getCategoriesFromId("notANumber");
            fail("Should throw NumberFormatException with non-numeric id");
        } catch (NumberFormatException nfe) {
            // good
        }
    }

    public void testGetCategoriesFromIdNoResult() {
        expect(_dao.getArticleCategory(15)).andReturn(null);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
        verify(_dao);

        assertNotNull(cats);
        assertEquals(0, cats.size());
    }

    public void testGetCategoriesFromIdWithNoParent() {
        ArticleCategory category = new ArticleCategory();

        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
        verify(_dao);

        assertNotNull(cats);
        assertEquals(1, cats.size());
        assertSame(category, cats.get(0));
    }

    public void testGetCategoriesFromIdWithParents() {
        ArticleCategory category = new ArticleCategory();
        ArticleCategory category2 = new ArticleCategory();
        ArticleCategory category3 = new ArticleCategory();

        category.setType("cat1");
        category.setParentType("cat2");
        category2.setType("cat2");
        category2.setParentType("cat3");
        category3.setType("cat3");

        expect(_dao.getArticleCategory(15)).andReturn(category);
        expect(_dao.getArticleCategoryByType("cat2")).andReturn(category2);
        expect(_dao.getArticleCategoryByType("cat3")).andReturn(category3);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
        verify(_dao);

        assertNotNull(cats);
        assertEquals(3, cats.size());
        assertSame(category, cats.get(0));
        assertSame(category2, cats.get(1));
        assertSame(category3, cats.get(2));
    }

    public void testGetCategoriesFromIdWithParentsButNoGetParents() {
        ArticleCategory category = new ArticleCategory();
        ArticleCategory category2 = new ArticleCategory();
        ArticleCategory category3 = new ArticleCategory();

        category.setType("cat1");
        category.setParentType("cat2");
        category2.setType("cat2");
        category2.setParentType("cat3");
        category3.setType("cat3");

        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        _controller.setGetParents(false);
        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
        _controller.setGetParents(true);
        verify(_dao);

        assertNotNull(cats);
        assertEquals(1, cats.size());
        assertSame(category, cats.get(0));
    }

    public void testGetCategoriesFromIdWithLoop() {
        ArticleCategory category = new ArticleCategory();
        ArticleCategory category2 = new ArticleCategory();

        category.setType("cat1");
        category.setParentType("cat2");
        category2.setType("cat2");
        category2.setParentType("cat2");

        expect(_dao.getArticleCategory(15)).andReturn(category);
        expect(_dao.getArticleCategoryByType("cat2")).andReturn(category2);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
        verify(_dao);

        assertNotNull(cats);
        assertEquals(2, cats.size());
        assertSame(category, cats.get(0));
        assertSame(category2, cats.get(1));
    }

    public void testGetCategoriesFromIdWithInvalidParentWorks() {
        ArticleCategory category = new ArticleCategory();
        ArticleCategory category2 = new ArticleCategory();

        category.setType("cat1");
        category.setParentType("cat2");
        category2.setType("cat2");
        category2.setParentType("no_such_cat");

        expect(_dao.getArticleCategory(15)).andReturn(category);
        expect(_dao.getArticleCategoryByType("cat2")).andReturn(category2);
        expect(_dao.getArticleCategoryByType("no_such_cat")).andReturn(null);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
        verify(_dao);

        assertNotNull(cats);
        assertEquals(2, cats.size());
        assertSame(category, cats.get(0));
        assertSame(category2, cats.get(1));
    }

    public void testGetCategoriesFromURINull() {
        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/");
        assertNull(cats);

        cats = _controller.getCategoriesFromURI("/gs-web/articles/");
        assertNull(cats);
    }

    public void testGetCategoriesFromURINonNumeric() {
        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/not-a-number");
        assertNull(cats);
    }

    public void testGetCategoriesFromURIEmpty() {
        expect(_dao.getArticleCategory(15)).andReturn(null);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/15");
        verify(_dao);
        assertNotNull(cats);
        assertEquals(0, cats.size());
    }

    public void testGetCategoriesFromURI() {
        ArticleCategory category = new ArticleCategory();
        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/15");
        verify(_dao);
        assertNotNull(cats);
        assertEquals(1, cats.size());
        assertSame(category, cats.get(0));
    }

    public void testGetCategoriesFromURIIgnoresAfterId() {
        ArticleCategory category = new ArticleCategory();
        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/15/blah-blah-blah/blah/more-blah/la-di-dah");
        verify(_dao);
        assertNotNull(cats);
        assertEquals(1, cats.size());
        assertSame(category, cats.get(0));
    }

    /*public void testHandleRequestInternalNoResults() {
        String testURI = "/articles/15/blah-blah";
        getRequest().setRequestURI(testURI);

        expect(_dao.getArticleCategory(15)).andReturn(null);
        expect(_cmsCategoryDao.getCmsCategoryFromURI(eq(testURI))).andReturn(new CmsCategory());
        replay(_dao);
        replay(_cmsCategoryDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_cmsCategoryDao);

        assertNotNull(mAndV);
        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }*/

     /**
     * Test with nulls
     */
    public void testStoreResultsForCmsCategoriesNoFeatures(){
        expect(_cmsFeatureSearchService.getCmsFeatures(null,null,null,null,null,false,null,null,10,0)).andReturn(null);
        replay(_cmsFeatureSearchService);
        _controller.setRandomResults(true);
        _controller.storeResultsForCmsCategories(null,null,null,null,null,null,0,false,null,null,String.valueOf(10));
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
        expect(_cmsFeatureSearchService.getCmsFeatures(null,null,null,null,null,false,null,null,10,0)).andReturn(searchResultsPage);
        replay(_cmsFeatureSearchService);
        _controller.setRandomResults(true);
        _controller.storeResultsForCmsCategories(null, null, null, null, null, new HashMap(), 0, false, null, null, String.valueOf(10));
        verify(_cmsFeatureSearchService);
    }


}
