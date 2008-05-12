package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.data.content.Article;
import gs.data.content.IArticleDao;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class ArticleCacheControllerTest extends BaseControllerTestCase {

    private ArticleCacheController _controller;
    /** Provides access to database articles */
    private IArticleDao _articleDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (ArticleCacheController)getApplicationContext().
                getBean(ArticleCacheController.BEAN_ID);
        _articleDao = (IArticleDao)getApplicationContext().
                getBean(IArticleDao.BEAN_ID);
    }

    public void testParamIsNotANumber() throws Exception {
        MockHttpServletResponse response = getResponse();

        getRequest().setParameter("expire-aid", "abcd");

        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("", response.getContentAsString());
    }

    public void testNoParams() throws Exception {
        MockHttpServletResponse response = getResponse();

        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("", response.getContentAsString());
    }

    public void testArticleNotCached() throws Exception {
        MockHttpServletResponse response = getResponse();

        getRequest().setParameter("expire-aid", "207");

        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("Article not in cache: 207", response.getContentAsString());
    }

    public void testArticleCached() throws Exception {
        MockHttpServletResponse response = getResponse();

        // First cache the article
        Article article = _articleDao.getArticleFromId(207);

        getRequest().setParameter("expire-aid", "207");
        assertNotNull(article);
        
        // Then expire it
        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("Expired article: 207", response.getContentAsString());

        // Then verify that it cannot be expired again
        response = new MockHttpServletResponse();
        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("Article not in cache: 207", response.getContentAsString());
    }
}