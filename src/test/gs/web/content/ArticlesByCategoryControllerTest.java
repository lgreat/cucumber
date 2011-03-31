package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ContentKey;
import gs.data.search.Searcher;
import gs.data.search.Indexer;
import gs.data.search.IndexDir;

import static org.easymock.classextension.EasyMock.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ArticlesByCategoryControllerTest extends BaseControllerTestCase {
    private ArticlesByCategoryController _controller;

    private IArticleCategoryDao _dao;
//    private ICmsCategoryDao _cmsCategoryDao;
//    private Searcher _searcher;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new ArticlesByCategoryController();
        _controller.setViewName("content/articleCategory");

        _dao = createStrictMock(IArticleCategoryDao.class);
//        _controller.setArticleCategoryDao(_dao);

//        _searcher = createStrictMock(Searcher.class);
//        _controller.setSearcher(_searcher);

//        _cmsCategoryDao = createStrictMock(ICmsCategoryDao.class);
        //_cmsCategoryDao.setSearcher(_searcher);
//        _controller.setCmsCategoryDao(_cmsCategoryDao);

        _controller.setGetParents(true);

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    public void testBasics() {
//        assertSame(_dao, _controller.getArticleCategoryDao());
//        assertSame(_searcher, _controller.getSearcher());
        assertTrue(_controller.isGetParents());
        assertEquals("content/articleCategory", _controller.getViewName());
    }

    protected CmsFeature getFeature(long index) {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", index));
        feature.setTitle("title" + index);
        feature.setBody("body" + index);
        feature.setSummary("summary" + index);
        feature.setFullUri("fullUri" + index);
        CmsCategory cat = new CmsCategory();
        cat.setName("kategory" + index);
        feature.setPrimaryKategory(cat);
        List<CmsCategory> breadcrumbs = Arrays.asList(cat);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        return feature;
    }

    protected CmsCategory getCmsCategory(String name, String fullUri, int id) {
        CmsCategory cat = new CmsCategory();
        cat.setName(name);
        cat.setFullUri(fullUri);
        cat.setId(id);
        return cat;
    }

    private Searcher setupSearcher() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);

        List<CmsFeature> features = new ArrayList<CmsFeature>();

        // feature id: primary cat id
        // 1:1
        // 2:2
        // feature id: secondary cat ids
        // 1:2,3
        // 2:3,4
        CmsFeature feature1 = getFeature(1);
        feature1.setLanguage("EN");
        CmsCategory cat = getCmsCategory("Improve your school",
                "improve-your-school", 1);
        feature1.setPrimaryKategory(cat);
        CmsCategory cat2 = getCmsCategory("Building community",
                "improve-your-school/building-community", 2);
        List<CmsCategory> breadcrumbs = Arrays.asList(cat);
        feature1.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        List<CmsCategory> secondaryCats = new ArrayList<CmsCategory>(2);
        secondaryCats.add(cat2);
        CmsCategory cat3 = getCmsCategory("Secondary Information",
                "improve-your-school/secondary-information", 3);
        secondaryCats.add(cat3);
        feature1.setSecondaryKategories(secondaryCats);
        feature1.setSecondaryKategoryBreadcrumbs(Arrays.asList(Arrays.asList(cat, cat2), Arrays.asList(cat, cat3)));
        features.add(feature1);
        CmsFeature feature2 = getFeature(2);
        feature2.setLanguage("EN");
        feature2.setPrimaryKategory(cat2);
        // purposely incomplete breadcrumbs - tested in IndexerTest.java
        breadcrumbs = Arrays.asList(cat2);
        feature2.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        secondaryCats = new ArrayList<CmsCategory>(2);
        secondaryCats.add(cat3);
        CmsCategory elemCat = getCmsCategory("Elementary school",
                "elementary-school", 4);
        secondaryCats.add(elemCat);
        feature2.setSecondaryKategories(secondaryCats);
        feature2.setSecondaryKategoryBreadcrumbs(Arrays.asList(Arrays.asList(cat, cat3), Arrays.asList(elemCat)));
        features.add(feature2);

        Indexer indexer = new Indexer();
        indexer.indexCategories(indexer.indexCmsFeatures(features, writer), writer);
        writer.close();

        IndexDir indexDir = new IndexDir(dir, null);
        return new Searcher(indexDir);
    }

    private Searcher setupLanguageSearcher() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);

        List<CmsFeature> features = new ArrayList<CmsFeature>();

        // feature id: primary cat id
        // 1:1
        // 2:1
        // 3:1
        // feature id: language
        // 1:EN
        // 2:EN
        // 3:ES
        CmsCategory cat = getCmsCategory("Improve your school",
                "improve-your-school", 1);
        CmsFeature feature1 = getFeature(1);
        feature1.setLanguage("EN");
        feature1.setPrimaryKategory(cat);
        feature1.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat));
        features.add(feature1);
        CmsFeature feature2 = getFeature(2);
        feature2.setLanguage("EN");
        feature2.setPrimaryKategory(cat);
        feature2.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat));
        features.add(feature2);
        CmsFeature feature3 = getFeature(3);
        feature3.setLanguage("ES");
        feature3.setPrimaryKategory(cat);
        feature3.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat));
        features.add(feature3);

        Indexer indexer = new Indexer();
        indexer.indexCategories(indexer.indexCmsFeatures(features, writer), writer);
        writer.close();

        IndexDir indexDir = new IndexDir(dir, null);
        return new Searcher(indexDir);
    }

    private Searcher setupSearcherForTitleTests() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);

        List<CmsFeature> features = new ArrayList<CmsFeature>();

        // feature id: primary cat id
        // 1:1
        // 2:1
        // feature id: secondary cat ids
        // 3:1
        // 4:1
        // features 2 and 3 have same title terms as category name
        CmsFeature feature1 = getFeature(1);
        CmsCategory cat1 = getCmsCategory("Improve your school",
                "improve-your-school", 1);
        feature1.setPrimaryKategory(cat1);
        feature1.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat1));
        features.add(feature1);

        CmsFeature feature2 = getFeature(2);
        feature2.setPrimaryKategory(cat1);
        feature2.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat1));
        feature2.setTitle("Improving my school");
        features.add(feature2);

        CmsFeature feature3 = getFeature(3);
        CmsCategory cat2 = getCmsCategory("Building community",
                "improve-your-school/building-community", 2);
        feature3.setPrimaryKategory(cat2);
        feature3.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat2));
        List<CmsCategory> secondaryCats = new ArrayList<CmsCategory>(1);
        secondaryCats.add(cat1);
        feature3.setSecondaryKategories(secondaryCats);
        feature3.setSecondaryKategoryBreadcrumbs(Arrays.asList(Arrays.asList(cat1)));
        feature3.setTitle("Improving my school");
        features.add(feature3);

        CmsFeature feature4 = getFeature(4);
        feature4.setPrimaryKategory(cat2);
        feature4.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat2));
        secondaryCats = new ArrayList<CmsCategory>(1);
        secondaryCats.add(cat1);
        feature4.setSecondaryKategories(secondaryCats);
        feature4.setSecondaryKategoryBreadcrumbs(Arrays.asList(Arrays.asList(cat1)));
        features.add(feature4);

        CmsFeature feature5 = getFeature(5);
        CmsCategory cat5 = getCmsCategory("Category not the same as the title",
                "category-not-the-same-as/the-title", 5);
        feature5.setPrimaryKategory(cat5);
        feature5.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat5));
        feature5.setTitle("This feature title is the title of a category");
        features.add(feature5);

        CmsFeature feature6 = getFeature(6);
        CmsCategory cat6 = getCmsCategory("Feature title is the title of a category",
                "feature-title-is-the-title-of/a-category", 6);
        feature6.setPrimaryKategory(cat6);
        feature6.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat6));
        feature6.setTitle("Title of the expected feature");
        features.add(feature6);


        Indexer indexer = new Indexer();
        indexer.indexCategories(indexer.indexCmsFeatures(features, writer), writer);
        writer.close();

        IndexDir indexDir = new IndexDir(dir, null);
        return new Searcher(indexDir);
    }

    // Test scope is to determine that this method switches based on the URI
    // Testing actual search is out of scope! Use tests that target those methods specifically instead
//    public void testHandleRequestInternalCms() {
//        getRequest().setQueryString("topics=1,2,3");
//        getRequest().setRequestURI("/articles/");
//        getRequest().setParameter("topics","1,2,3");
//
//        expect(_cmsCategoryDao.getCmsCategoryFromURI("/articles/")).andReturn(null);
//        expect(_cmsCategoryDao.getCmsCategoriesFromIds("1,2,3")).andReturn(new ArrayList<CmsCategory>());
//        expect(_cmsCategoryDao.getCmsCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
//        expect(_cmsCategoryDao.getCmsCategoriesFromIds(null)).andReturn(new ArrayList<CmsCategory>());
//
//        replay(_cmsCategoryDao);
//        replay(_dao);
//        replay(_searcher);
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_cmsCategoryDao);
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

    // Test scope is to determine that this method switches based on the URI
    // Testing actual search is out of scope! Use tests that target those methods specifically instead
//    public void testHandleRequestInternalLegacy() {
//        getRequest().setRequestURI("/articles/123/blah-blah");
//
//        expect(_dao.getArticleCategory(123)).andReturn(null);
//
//        replay(_dao);
//        replay(_searcher);
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

//    public void testGetCategoriesFromIdNull() {
//        try {
//            _controller.getCategoriesFromId(null);
//            fail("Should throw NumberFormatException with null id");
//        } catch (NumberFormatException npe) {
//            // good
//        }
//    }

//    public void testGetCategoriesFromIdInvalid() {
//        try {
//            _controller.getCategoriesFromId("notANumber");
//            fail("Should throw NumberFormatException with non-numeric id");
//        } catch (NumberFormatException nfe) {
//            // good
//        }
//    }

//    public void testGetCategoriesFromIdNoResult() {
//        expect(_dao.getArticleCategory(15)).andReturn(null);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
//        verify(_dao);
//
//        assertNotNull(cats);
//        assertEquals(0, cats.size());
//    }
//
//    public void testGetCategoriesFromIdWithNoParent() {
//        ArticleCategory category = new ArticleCategory();
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
//        verify(_dao);
//
//        assertNotNull(cats);
//        assertEquals(1, cats.size());
//        assertSame(category, cats.get(0));
//    }

//    public void testGetCategoriesFromIdWithParents() {
//        ArticleCategory category = new ArticleCategory();
//        ArticleCategory category2 = new ArticleCategory();
//        ArticleCategory category3 = new ArticleCategory();
//
//        category.setType("cat1");
//        category.setParentType("cat2");
//        category2.setType("cat2");
//        category2.setParentType("cat3");
//        category3.setType("cat3");
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        expect(_dao.getArticleCategoryByType("cat2")).andReturn(category2);
//        expect(_dao.getArticleCategoryByType("cat3")).andReturn(category3);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
//        verify(_dao);
//
//        assertNotNull(cats);
//        assertEquals(3, cats.size());
//        assertSame(category, cats.get(0));
//        assertSame(category2, cats.get(1));
//        assertSame(category3, cats.get(2));
//    }

//    public void testGetCategoriesFromIdWithParentsButNoGetParents() {
//        ArticleCategory category = new ArticleCategory();
//        ArticleCategory category2 = new ArticleCategory();
//        ArticleCategory category3 = new ArticleCategory();
//
//        category.setType("cat1");
//        category.setParentType("cat2");
//        category2.setType("cat2");
//        category2.setParentType("cat3");
//        category3.setType("cat3");
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        _controller.setGetParents(false);
//        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
//        _controller.setGetParents(true);
//        verify(_dao);
//
//        assertNotNull(cats);
//        assertEquals(1, cats.size());
//        assertSame(category, cats.get(0));
//    }

//    public void testGetCategoriesFromIdWithLoop() {
//        ArticleCategory category = new ArticleCategory();
//        ArticleCategory category2 = new ArticleCategory();
//
//        category.setType("cat1");
//        category.setParentType("cat2");
//        category2.setType("cat2");
//        category2.setParentType("cat2");
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        expect(_dao.getArticleCategoryByType("cat2")).andReturn(category2);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
//        verify(_dao);
//
//        assertNotNull(cats);
//        assertEquals(2, cats.size());
//        assertSame(category, cats.get(0));
//        assertSame(category2, cats.get(1));
//    }

//    public void testGetCategoriesFromIdWithInvalidParentWorks() {
//        ArticleCategory category = new ArticleCategory();
//        ArticleCategory category2 = new ArticleCategory();
//
//        category.setType("cat1");
//        category.setParentType("cat2");
//        category2.setType("cat2");
//        category2.setParentType("no_such_cat");
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        expect(_dao.getArticleCategoryByType("cat2")).andReturn(category2);
//        expect(_dao.getArticleCategoryByType("no_such_cat")).andReturn(null);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromId("15");
//        verify(_dao);
//
//        assertNotNull(cats);
//        assertEquals(2, cats.size());
//        assertSame(category, cats.get(0));
//        assertSame(category2, cats.get(1));
//    }

//    public void testGetCategoriesFromURINull() {
//        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/");
//        assertNull(cats);
//
//        cats = _controller.getCategoriesFromURI("/gs-web/articles/");
//        assertNull(cats);
//    }
//
//    public void testGetCategoriesFromURINonNumeric() {
//        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/not-a-number");
//        assertNull(cats);
//    }
//
//    public void testGetCategoriesFromURIEmpty() {
//        expect(_dao.getArticleCategory(15)).andReturn(null);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/15");
//        verify(_dao);
//        assertNotNull(cats);
//        assertEquals(0, cats.size());
//    }

//    public void testGetCategoriesFromURI() {
//        ArticleCategory category = new ArticleCategory();
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/15");
//        verify(_dao);
//        assertNotNull(cats);
//        assertEquals(1, cats.size());
//        assertSame(category, cats.get(0));
//    }
//
//    public void testGetCategoriesFromURIIgnoresAfterId() {
//        ArticleCategory category = new ArticleCategory();
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        List<ArticleCategory> cats = _controller.getCategoriesFromURI("/articles/15/blah-blah-blah/blah/more-blah/la-di-dah");
//        verify(_dao);
//        assertNotNull(cats);
//        assertEquals(1, cats.size());
//        assertSame(category, cats.get(0));
//    }

//    public void testGetResultsForCategory() {
//        ArticleCategory category = new ArticleCategory();
//        category.setType("my_type");
//        Map<String, Object> model = new HashMap<String, Object>();
//        TermQuery termQuery = new TermQuery(new Term("category", "my_type"));
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(termQuery, BooleanClause.Occur.MUST);
//
//        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
//                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
//        replay(_searcher);
//
//        _controller.storeResultsForCategory(category, model, 1);
//        verify(_searcher);
//    }

//    public void testHandleRequestInternal() {
//        getRequest().setRequestURI("/articles/15/blah-blah");
//
//        ArticleCategory category = new ArticleCategory();
//        category.setType("adhd");
//        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(termQuery, BooleanClause.Occur.MUST);
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
//                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
//        replay(_searcher);
//
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

//    public void testHandleRequestInternalNoResults() {
//        getRequest().setRequestURI("/articles/15/blah-blah");
//
//        expect(_dao.getArticleCategory(15)).andReturn(null);
//        replay(_dao);
//
//        replay(_searcher);
//
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

//    public void testHandleRequestParameterOverride() {
//        getRequest().setRequestURI("/articles/20/blah-blah");
//        getRequest().setParameter(ArticlesByCategoryController.PARAM_ID, "15");
//
//        ArticleCategory category = new ArticleCategory();
//        category.setType("adhd");
//        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(termQuery, BooleanClause.Occur.MUST);
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
//                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
//        replay(_searcher);
//
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
//        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

//    public void testHandleRequestPageNumber() {
//        getRequest().setRequestURI("/articles/15/blah-blah");
//        getRequest().setParameter(ArticlesByCategoryController.PARAM_PAGE, "2");
//
//        ArticleCategory category = new ArticleCategory();
//        category.setType("adhd");
//        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(termQuery, BooleanClause.Occur.MUST);
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
//                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
//        replay(_searcher);
//
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertEquals("Expect page to pick up request param value of 2",
//                new Integer(2), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
//        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

//    public void testHandleRequestInvalidPageNumber() {
//        getRequest().setRequestURI("/articles/15/blah-blah");
//        getRequest().setParameter(ArticlesByCategoryController.PARAM_PAGE, "garbage");
//
//        ArticleCategory category = new ArticleCategory();
//        category.setType("adhd");
//        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(termQuery, BooleanClause.Occur.MUST);
//
//        expect(_dao.getArticleCategory(15)).andReturn(category);
//        replay(_dao);
//
//        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
//                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
//        replay(_searcher);
//
//        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
//        verify(_dao);
//        verify(_searcher);
//
//        assertNotNull(mAndV);
//
//        assertEquals("Expect page to default to 1",
//                new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
//        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
//        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
//        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
//    }

//    public static BooleanQuery eqBooleanQuery(BooleanQuery in) {
//        reportMatcher(new BooleanQueryEquals(in));
//        return in;
//    }
//
//    public static class BooleanQueryEquals implements IArgumentMatcher {
//        private BooleanQuery _expected;
//
//        public BooleanQueryEquals(BooleanQuery expected) {
//            _expected = expected;
//        }
//
//        public boolean matches(Object actualObj) {
//            if (!(actualObj instanceof BooleanQuery)) {
//                return false;
//            }
//            BooleanQuery actual = (BooleanQuery) actualObj;
//            return (StringUtils.equals(actual.toString(), _expected.toString()));
//        }
//
//        public void appendTo(StringBuffer buffer) {
//            buffer.append(_expected.toString());
//        }
//    }
}
