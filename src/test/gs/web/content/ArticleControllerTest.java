package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.content.Article;
import gs.data.content.IArticleDao;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;
/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleControllerTest extends BaseControllerTestCase {

    private ArticleController _controller;
    private IArticleDao _articleDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new ArticleController();

        _articleDao = createStrictMock(IArticleDao.class);
        _controller.setArticleDao(_articleDao);
    }

    // AR: this was in here already so I left it as-is
    public void testHandleRequestInternal() {
        ArticleController controller =
                (ArticleController)getApplicationContext().
                        getBean(ArticleController.BEAN_ID);

        GsMockHttpServletRequest request = getRequest();
        request.setParameter(ArticleController.PARAM_AID, "2");
        ModelAndView mAndV =
                controller.handleRequestInternal(request, getResponse());
        Article article =
                (Article)mAndV.getModel().get("article");
        assertEquals("Article title does not match.",
                "Who Makes School Discipline Decisions?", article.getTitle());
    }

    public void testNewStyleArticle() {
        getRequest().setParameter(ArticleController.PARAM_AID, "78");

        Article article = new Article();
        article.setArticleText("<div id=\"article-main\">Don't hit your child</div>");
        expect(_articleDao.getArticleFromId(78)).andReturn(article);
        replay(_articleDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_articleDao);
        assertEquals(Boolean.TRUE, mAndV.getModel().get(ArticleController.MODEL_NEW_ARTICLE));
    }

    public void testOldStyleArticle() {
        getRequest().setParameter(ArticleController.PARAM_AID, "78");

        Article article = new Article();
        article.setArticleText("<div>Don't hit your child</div>");
        expect(_articleDao.getArticleFromId(78)).andReturn(article);
        replay(_articleDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_articleDao);
        assertEquals(Boolean.FALSE, mAndV.getModel().get(ArticleController.MODEL_NEW_ARTICLE));
    }

    public void testBadArticleId() {
        getRequest().setParameter(ArticleController.PARAM_AID, "that_one_about_the_mean_girls");

        replay(_articleDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_articleDao);

        assertNull("Expect failure to retrieve article", mAndV.getModel().get(ArticleController.MODEL_ARTICLE));
    }
}
