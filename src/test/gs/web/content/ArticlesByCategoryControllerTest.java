package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.search.Searcher;

import static org.easymock.classextension.EasyMock.*;

import java.util.List;

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
    }

    public void testBasics() {
        assertSame(_dao, _controller.getArticleCategoryDao());
        assertSame(_searcher, _controller.getSearcher());
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
}
