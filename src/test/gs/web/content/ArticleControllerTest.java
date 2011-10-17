package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.state.State;
import gs.data.util.CmsUtil;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.classextension.EasyMock.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;

/**
 * Unit tests for ArticleController.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class ArticleControllerTest extends BaseControllerTestCase {

    private ArticleController _controller;
    private IArticleDao _articleDao;
    private IArticleCategoryDao _articleCategoryDao;
    private State _state;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new ArticleController();

        _articleDao = createStrictMock(IArticleDao.class);
        _articleCategoryDao = createStrictMock(IArticleCategoryDao.class);

        _controller.setArticleDao(_articleDao);
        _controller.setArticleCategoryDao(_articleCategoryDao);

        _state = State.CA;
    }

    public void testNewStyleArticle() {
        getRequest().setParameter(ArticleController.PARAM_AID, "78");

        Article article = new Article();
        article.setArticleText("<div id=\"article-main\">Don't hit your child</div>");
        expect(_articleDao.getArticleFromId(78, true)).andReturn(article);
        replay(_articleDao);

        expect(_articleCategoryDao.getArticleCategoriesByArticle(article)).andReturn(new HashSet<ArticleCategory>());
        replay(_articleCategoryDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_articleDao);
        assertEquals(Boolean.TRUE, mAndV.getModel().get(ArticleController.MODEL_NEW_ARTICLE));
    }

    public void testOldStyleArticle() {
        getRequest().setParameter(ArticleController.PARAM_AID, "78");

        Article article = new Article();
        article.setArticleText("<div>Don't hit your child</div>");
        expect(_articleDao.getArticleFromId(78, true)).andReturn(article);
        replay(_articleDao);
        expect(_articleCategoryDao.getArticleCategoriesByArticle(article)).andReturn(new HashSet<ArticleCategory>());
        replay(_articleCategoryDao);
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

    public void testProcessHier1() {
        assertEquals("Contains commas commas and commas", _controller.processHier1("Contains commas, commas, and commas"));
        assertEquals("Contains \\\"double quotes\\\"", _controller.processHier1("Contains \"double quotes\""));
        assertEquals("Contains lots of spaces", _controller.processHier1("Contains   lots    of     spaces"));
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

    public void testParseArticleId() {
        assertEquals(-1, _controller.parseArticleId(_state, null));

        assertEquals(1, _controller.parseArticleId(_state, "1"));

        assertEquals(-1, _controller.parseArticleId(_state, "blah"));

        assertEquals("Expect 51 states in achievement map",
                51, ArticleController._achievementMap.size());

        assertEquals(866, _controller.parseArticleId(_state, "achievement"));
        assertEquals(862, _controller.parseArticleId(State.TX, "achievement"));
        assertEquals(887, _controller.parseArticleId(State.IN, "achievement"));
        assertEquals(894, _controller.parseArticleId(State.OK, "achievement"));
        assertEquals(899, _controller.parseArticleId(State.DC, "achievement"));
        assertEquals(912, _controller.parseArticleId(State.ME, "achievement"));
        assertEquals(921, _controller.parseArticleId(State.SD, "achievement"));
    }

    public void testGetMetaDescriptor() {
        Article article = new Article();
        assertNull(article.getMetaDescriptor());
        article.setAbstract("<tag>inside first</tag> less than sign < <more>inside second</more>");
        assertEquals("inside first less than sign < inside second",article.getMetaDescriptor());
    }

    public void testArticleServedByLegacyCms() {
        int articleServedByLegacyCms = 622;

        CmsUtil.enableCms();

        getRequest().setParameter(ArticleController.PARAM_AID, String.valueOf(articleServedByLegacyCms));
        expect(_articleDao.getArticleFromId(articleServedByLegacyCms, true)).andReturn(null);
        replay(_articleDao);
        _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals(HttpServletResponse.SC_OK, getResponse().getStatus());
        verify(_articleDao);

        CmsUtil.disableCms();
    }

    public void testArticleServedByCmsButNotPublished() {
        int articleServedByCmsButNotPublished = 1;

        CmsUtil.enableCms();

        UrlBuilder mock = createStrictMock(UrlBuilder.class);
        _controller._setUrlBuilderForArticleId(mock);

        getRequest().removeParameter(ArticleController.PARAM_AID);
        getRequest().setParameter(ArticleController.PARAM_AID, String.valueOf(articleServedByCmsButNotPublished));
        replay(_articleDao);
        expect(mock.asSiteRelative(getRequest())).andReturn("/cgi-bin/showarticle/" + articleServedByCmsButNotPublished);
        replay(mock);
        _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals(HttpServletResponse.SC_NOT_FOUND, getResponse().getStatus());
        verify(_articleDao);
        verify(mock);

        reset(_articleDao);
        reset(mock);

        CmsUtil.disableCms();
    }

    public void testArticleServedByCmsAndPublished() {
        int articleServedByCmsButPublished = 41;

        CmsUtil.enableCms();

        UrlBuilder mock = createStrictMock(UrlBuilder.class);
        _controller._setUrlBuilderForArticleId(mock);

        getRequest().removeParameter(ArticleController.PARAM_AID);
        getRequest().setParameter(ArticleController.PARAM_AID, String.valueOf(articleServedByCmsButPublished));
        replay(_articleDao);
        expect(mock.asSiteRelative(getRequest())).andReturn("/anything_not_starting_with_cgi-bin");
        replay(mock);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView301);
        verify(_articleDao);
        verify(mock);

        CmsUtil.disableCms();
    }
 }
