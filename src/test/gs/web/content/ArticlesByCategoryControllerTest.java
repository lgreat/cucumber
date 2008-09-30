package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.search.Searcher;

import static org.easymock.classextension.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ArticlesByCategoryControllerTest extends BaseControllerTestCase {
    private ArticlesByCategoryController _controller;

    private IArticleCategoryDao _dao;
    private Searcher _searcher;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new ArticlesByCategoryController();

        _dao = createStrictMock(IArticleCategoryDao.class);
        _controller.setArticleCategoryDao(_dao);

        _searcher = createStrictMock(Searcher.class);
        _controller.setSearcher(_searcher);

        _controller.setGetParents(true);
    }

    public void testBasics() {
        assertSame(_dao, _controller.getArticleCategoryDao());
        assertSame(_searcher, _controller.getSearcher());
        assertTrue(_controller.isGetParents());
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

    public void testGetResultsForCategory() {
        ArticleCategory category = new ArticleCategory();
        category.setType("my_type");
        Map<String, Object> model = new HashMap<String, Object>();
        TermQuery termQuery = new TermQuery(new Term("category", "my_type"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(termQuery, BooleanClause.Occur.MUST);

        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
        replay(_searcher);

        _controller.storeResultsForCategory(category, model, 1);
        verify(_searcher);
    }

    public void testHandleRequestInternal() {
        getRequest().setRequestURI("/articles/15/blah-blah");

        ArticleCategory category = new ArticleCategory();
        category.setType("adhd");
        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(termQuery, BooleanClause.Occur.MUST);

        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
        replay(_searcher);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_searcher);

        assertNotNull(mAndV);

        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    public void testHandleRequestInternalNoResults() {
        getRequest().setRequestURI("/articles/15/blah-blah");

        expect(_dao.getArticleCategory(15)).andReturn(null);
        replay(_dao);

        replay(_searcher);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_searcher);

        assertNotNull(mAndV);

        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    public void testHandleRequestParameterOverride() {
        getRequest().setRequestURI("/articles/20/blah-blah");
        getRequest().setParameter(ArticlesByCategoryController.PARAM_ID, "15");

        ArticleCategory category = new ArticleCategory();
        category.setType("adhd");
        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(termQuery, BooleanClause.Occur.MUST);

        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
        replay(_searcher);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_searcher);

        assertNotNull(mAndV);

        assertEquals(new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    public void testHandleRequestPageNumber() {
        getRequest().setRequestURI("/articles/15/blah-blah");
        getRequest().setParameter(ArticlesByCategoryController.PARAM_PAGE, "2");

        ArticleCategory category = new ArticleCategory();
        category.setType("adhd");
        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(termQuery, BooleanClause.Occur.MUST);

        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
        replay(_searcher);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_searcher);

        assertNotNull(mAndV);

        assertEquals("Expect page to pick up request param value of 2",
                new Integer(2), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    public void testHandleRequestInvalidPageNumber() {
        getRequest().setRequestURI("/articles/15/blah-blah");
        getRequest().setParameter(ArticlesByCategoryController.PARAM_PAGE, "garbage");

        ArticleCategory category = new ArticleCategory();
        category.setType("adhd");
        TermQuery termQuery = new TermQuery(new Term("category", "adhd"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(termQuery, BooleanClause.Occur.MUST);

        expect(_dao.getArticleCategory(15)).andReturn(category);
        replay(_dao);

        expect(_searcher.search(eqBooleanQuery(bq), (Sort)isNull(),
                (HitCollector)isNull(), isA(Filter.class))).andReturn(null);
        replay(_searcher);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
        verify(_searcher);

        assertNotNull(mAndV);

        assertEquals("Expect page to default to 1",
                new Integer(1), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE).toString()));
        assertSame(category, mAndV.getModel().get(ArticlesByCategoryController.MODEL_SUBCATEGORY));
        assertEquals(new Integer(ArticlesByCategoryController.PAGE_SIZE), new Integer(mAndV.getModel().get(ArticlesByCategoryController.MODEL_PAGE_SIZE).toString()));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_TOTAL_HITS));
        assertNull(mAndV.getModel().get(ArticlesByCategoryController.MODEL_RESULTS));
    }

    public static BooleanQuery eqBooleanQuery(BooleanQuery in) {
        reportMatcher(new BooleanQueryEquals(in));
        return in;
    }

    public static class BooleanQueryEquals implements IArgumentMatcher {
        private BooleanQuery _expected;

        public BooleanQueryEquals(BooleanQuery expected) {
            _expected = expected;
        }

        public boolean matches(Object actualObj) {
            if (!(actualObj instanceof BooleanQuery)) {
                return false;
            }
            BooleanQuery actual = (BooleanQuery) actualObj;
            return (StringUtils.equals(actual.toString(), _expected.toString()));
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append(_expected.toString());
        }
    }
}
