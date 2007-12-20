package gs.web.content;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.easymock.MockControl;

import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ArticleLinkTagHandlerTest extends TestCase {
    private ArticleLinkTagHandler _tagHandler;
    private IArticleDao _articleDao;
    private MockControl _articleControl;
    private MockJspWriter _out;

    protected void setUp() throws Exception {
        super.setUp();

        _tagHandler = new ArticleLinkTagHandler() {
            protected IArticleDao getArticleDao() {
                return _articleDao;
            }
        };

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, new MockSessionContext());
        request.setAttribute(PageContext.PAGECONTEXT, new MockPageContext());

        MockPageContext pageContext = new MockPageContext(new MockServletContext(), request);
        _out = (MockJspWriter) pageContext.getOut();
        _tagHandler.setJspContext(pageContext);

        _articleControl = MockControl.createControl(IArticleDao.class);
        _articleDao = (IArticleDao) _articleControl.getMock();

    }

    public void testLinkByArticleId() throws IOException {
        Article article = new Article();
        article.setId(new Integer(1));
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticleId(new Integer(2));
        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(2)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("<a href=\"/cgi-bin/showarticle/1\">title</a>", _out.getOutputBuffer().toString());
    }


    public void testLinkByArticle() throws IOException {
        Article article = new Article();
        article.setId(new Integer(1));
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);

        _tagHandler.doTag();

        assertEquals("<a href=\"/cgi-bin/showarticle/1\">title</a>", _out.getOutputBuffer().toString());
    }

    public void testLinkFeatured() throws IOException {
        Article article = new Article();
        article.setId(new Integer(1));
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);
        _tagHandler.setFeatured(true);

        _tagHandler.doTag();

        assertEquals("Expecting link to featured version of article",
                "<a href=\"/cgi-bin/showarticlefeature/1\">title</a>", _out.getOutputBuffer().toString());
    }

    public void testLinkTargetStyle() throws IOException {
        Article article = new Article();
        article.setId(new Integer(1));
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);
        _tagHandler.setTarget("_blank");
        _tagHandler.setStyleClass("class");

        _tagHandler.doTag();

        assertEquals("<a href=\"/cgi-bin/showarticle/1\" target=\"_blank\" class=\"class\">title</a>",
                _out.getOutputBuffer().toString());
    }

    public void testNoArticleId() throws IOException {
        _tagHandler.doTag();
        assertEquals("", _out.getOutputBuffer().toString());
    }

    public void testMissingArticleId() throws IOException {
        _tagHandler.setArticleId(new Integer(15));

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(15)), null);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("Expected no output since article id was invalid",
                "", _out.getOutputBuffer().toString());
    }

    public void testEncoding() throws IOException {
        Article article = new Article();
        article.setId(new Integer(1));
        article.setActive(true);
        article.setStatesAsString("--");
        article.setTitle("& is the new !");

        _tagHandler.setWrappingElement("li");
        _tagHandler.setArticle(article);
        _tagHandler.doTag();


        assertEquals("Expected ampersand to be escaped",
                "<li><a href=\"/cgi-bin/showarticle/1\">&amp; is the new !</a></li>",
                _out.getOutputBuffer().toString());
    }

    public void testFormatArticleTitle() {
        Article article = new Article();
        article.setTitle("Tests in $LONGSTATE are fun!");

        String output = _tagHandler.formatArticleTitle(article, State.CA);

        assertEquals("Expected substitution of state long name",
                "Tests in California are fun!", output);
    }

    public void testFormatArticleTitleReplacesAmpersandsButNotEntities() {
        Article article = new Article();
        article.setTitle("Beyond PB&J: Healthy Lunch Ideas");
        assertEquals("Expected to replace ampersand with entity", "Beyond PB&amp;J: Healthy Lunch Ideas",
                _tagHandler.formatArticleTitle(article, State.CA));

        article.setTitle("Beyond PB&amp;J: Healthy Lunch Ideas");
        assertEquals("Shouldn't replace ampersand in entity", "Beyond PB&amp;J: Healthy Lunch Ideas",
                _tagHandler.formatArticleTitle(article, State.CA));

        article.setTitle("Beyond PB&J &#8212; Healthy Lunch Ideas");
        assertEquals("Should replace only ampersands that aren't part of an entity", "Beyond PB&amp;J &#8212; Healthy Lunch Ideas",
                _tagHandler.formatArticleTitle(article, State.CA));
    }

    public void testGetAndValidateArticle() {
        _tagHandler.setArticleId(new Integer(18));

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(18)), null);
        _articleControl.replay();

        Article article;

        article = _tagHandler.getAndValidateArticle();
        _articleControl.verify();
        assertNull(article);

        _tagHandler.setArticleId(null);

        article = new Article();
        _tagHandler.setArticle(article);
        article.setActive(false);
        article.setStatesAsString("CA");
        article = _tagHandler.getAndValidateArticle();
        assertNull("Expected null for inactive article", article);

        article = new Article();
        _tagHandler.setArticle(article);
        article.setActive(true);
        article.setStatesAsString("AK");
        article = _tagHandler.getAndValidateArticle();
        assertNull("Expected null for wrong state article", article);

        article = new Article();
        _tagHandler.setArticle(article);
        article.setActive(true);
        article.setStatesAsString("CA");
        article = _tagHandler.getAndValidateArticle();
        assertNotNull("Expected valid article", article);
    }
}
