package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

/**
 * Unit tests for ArticleController.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleControllerTest extends BaseControllerTestCase {

    private ArticleController _controller;
    private IArticleDao _articleDao;
    private State _state;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new ArticleController();

        _articleDao = createStrictMock(IArticleDao.class);
        _controller.setArticleDao(_articleDao);

        _state = State.CA;
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

    public void testProcessArticleString() {
        assertEquals("in California",
                _controller.processArticleString(_state,
                        "in $LONGSTATE"));

        assertEquals("for (California)",
                _controller.processArticleString(_state,
                        "for ($LONGSTATE)"));

        assertEquals("for a longstate",
                _controller.processArticleString(_state,
                        "for a longstate"));

        assertEquals("in CA",
                _controller.processArticleString(_state,
                        "in $STATE"));

        assertEquals("/cgi-bin/showarticle/CA/174/impove",
                _controller.processArticleString(_state,
                        "/cgi-bin/showarticle/$STATE/174/impove"));

        assertEquals("Hello",
                _controller.processArticleString(_state,
                        "<span id=\"nopagebreaks\"/>Hello"));

        assertEquals("Hello. Good-bye. We meet again.",
                _controller.processArticleString(_state,
                        "Hello. <span id=\"pagebreak\"/>Good-bye. <span id=\"pagebreak\"/>We meet again."));
    }

    public void testProcessArticleForStateSubstrings() {
        assertEquals("", _controller.processArticleForStateSubstrings(_state, ""));

        assertEquals("Hello!", _controller.processArticleForStateSubstrings(_state,
                "^gstate=\"ca\"^Hello!^/gstate^"));

        assertEquals("Hello!", _controller.processArticleForStateSubstrings(_state,
                "^gstate=\"ak\"^what?^/gstate^^gstate=\"ca\"^Hello!^/gstate^"));

        assertEquals("what?", _controller.processArticleForStateSubstrings(_state,
                "^gstate=\"ca\"^what?^/gstate^^gstate=\"!ca\"^Hello!^/gstate^"));

        assertEquals("what? Hello!", _controller.processArticleForStateSubstrings(_state,
                "^gstate=\"ca,ak\"^what?^/gstate^ ^gstate=\"ak,ca\"^Hello!^/gstate^"));

        assertEquals("For some states, including California, these things come naturally",
                _controller.processArticleForStateSubstrings(_state,
                "For some states^gstate=\"ca\"^, including California,^/gstate^" +
                        " these things come naturally"));

        assertEquals("For some states these things come naturally",
                _controller.processArticleForStateSubstrings(_state,
                "For some states^gstate=\"ak\"^, including California,^/gstate^" +
                        " these things come naturally"));

        assertEquals("For some states these things come naturally",
                _controller.processArticleForStateSubstrings(_state,
                "For some states these things come naturally"));
    }
}
