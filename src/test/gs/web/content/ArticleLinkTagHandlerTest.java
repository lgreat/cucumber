package gs.web.content;

import gs.data.cms.IPublicationDao;
import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.CmsContent;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.CmsConstants;
import gs.data.state.State;
import gs.data.util.CmsUtil;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class ArticleLinkTagHandlerTest extends TestCase {
    private ArticleLinkTagHandler _tagHandler;
    private IArticleDao _articleDao;
    private IPublicationDao _publicationDao;
    private MockJspWriter _out;
    private boolean _cmsEnabled;

    protected void setUp() throws Exception {
        super.setUp();
        _cmsEnabled = CmsUtil.isCmsEnabled();

        _tagHandler = new ArticleLinkTagHandler() {
            protected IArticleDao getArticleDao() {
                return _articleDao;
            }

            protected IPublicationDao getPublicationDao() {
                return _publicationDao;
            }
        };

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, new MockSessionContext());
        request.setAttribute(PageContext.PAGECONTEXT, new MockPageContext());

        MockPageContext pageContext = new MockPageContext(new MockServletContext(), request);
        _out = (MockJspWriter) pageContext.getOut();
        _tagHandler.setJspContext(pageContext);

        _articleDao = createMock(IArticleDao.class);
        _publicationDao = createMock(IPublicationDao.class);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        CmsUtil.setCmsEnabled(_cmsEnabled);
    }

    public void testLinkByArticleId() throws IOException {
        Article article = new Article();
        article.setId(1);
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticleId(2);
        expect(_articleDao.getArticleFromId(2)).andReturn(article);
        replay(_articleDao);

        _tagHandler.doTag();

        verify(_articleDao);

        assertEquals("<a href=\"/cgi-bin/showarticle/1\">title</a>", _out.getOutputBuffer().toString());
    }
/*
    public void testLinkByArticleIdRendersArticleContentLinkWhenCmsEnabledAndCmsContentMissing() throws IOException {
        CmsUtil.enableCms();

        expect(_publicationDao.populateByLegacyId(eq(2L), isA(CmsContent.class))).andReturn(null);

        Article article = new Article();
        article.setId(1);
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        expect(_articleDao.getArticleFromId(2)).andReturn(article);

        replay(_publicationDao);
        replay(_articleDao);

        _tagHandler.setArticleId(2);
        _tagHandler.doTag();

        verify(_publicationDao);
        verify(_articleDao);

        assertEquals("<a href=\"/\">title</a>", _out.getOutputBuffer().toString());
        // Fallthrough: assertEquals("<a href=\"/cgi-bin/showarticle/1\">title</a>", _out.getOutputBuffer().toString());
    }
*/
    public void testLinkByArticleIdRendersCmsContentLinkWhenEnabled() throws IOException {
        CmsUtil.enableCms();

        CmsFeature content = new CmsFeature();
        content.setContentKey(new ContentKey("Article", 102L));
        content.setFullUri("/Topic/Category/Title");
        content.setTitle("Title");

        _tagHandler.setArticleId(2);

        expect(_publicationDao.populateByLegacyId(eq(2L), isA(CmsContent.class))).andReturn(content);
        replay(_publicationDao);
        replay(_articleDao);

        _tagHandler.doTag();

        verify(_publicationDao);
        verify(_articleDao);

        assertEquals("<a href=\"/Topic/Category/102-Title.gs\">Title</a>", _out.getOutputBuffer().toString());
    }

    public void testLinkByArticleIdFallbackBehavior() throws IOException {
        CmsUtil.enableCms();

        CmsFeature content = new CmsFeature();
        content.setContentKey(new ContentKey("Article", 102L));
        content.setFullUri("/Topic/Category/Title");
        content.setTitle("Title");

        int legacyArticleId = CmsConstants.getArticlesServedByLegacyCms().iterator().next().intValue();
        _tagHandler.setArticleId(legacyArticleId);

        Article article = new Article();
        article.setId(legacyArticleId);
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);

        _tagHandler.doTag();

        assertEquals("<a href=\"/cgi-bin/showarticle/" + legacyArticleId + "\">title</a>", _out.getOutputBuffer().toString());
    }

    public void testLinkByArticle() throws IOException {
        Article article = new Article();
        article.setId(1);
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);

        _tagHandler.doTag();

        assertEquals("<a href=\"/cgi-bin/showarticle/1\">title</a>", _out.getOutputBuffer().toString());
    }

    public void testLinkFeatured() throws IOException {
        Article article = new Article();
        article.setId(1);
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
        article.setId(1);
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

    public void testLinkId() throws IOException {
        Article article = new Article();
        article.setId(1);
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);
        _tagHandler.setStyleId("link-id");

        _tagHandler.doTag();

        assertEquals("<a href=\"/cgi-bin/showarticle/1\" id=\"link-id\">title</a>",
                _out.getOutputBuffer().toString());
    }

    public void testNoArticleId() throws IOException {
        _tagHandler.doTag();
        assertEquals("", _out.getOutputBuffer().toString());
    }

    public void testMissingArticleId() throws IOException {
        _tagHandler.setArticleId(15);

        expect(_articleDao.getArticleFromId(15)).andReturn(null);
        replay(_articleDao);

        _tagHandler.doTag();
        verify(_articleDao);

        assertEquals("Expected no output since article id was invalid",
                "", _out.getOutputBuffer().toString());
    }

    public void testEncoding() throws IOException {
        Article article = new Article();
        article.setId(1);
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

        String output = _tagHandler.formatArticleTitle(new ArticleLinkTagHandler.LinkableContent(article), State.CA);

        assertEquals("Expected substitution of state long name",
                "Tests in California are fun!", output);
    }

    public void testFormatArticleTitleReplacesAmpersandsButNotEntities() {
        Article article = new Article();
        article.setTitle("Beyond PB&J: Healthy Lunch Ideas");
        assertEquals("Expected to replace ampersand with entity", "Beyond PB&amp;J: Healthy Lunch Ideas",
                _tagHandler.formatArticleTitle(new ArticleLinkTagHandler.LinkableContent(article), State.CA));

        article.setTitle("Beyond PB&amp;J: Healthy Lunch Ideas");
        assertEquals("Shouldn't replace ampersand in entity", "Beyond PB&amp;J: Healthy Lunch Ideas",
                _tagHandler.formatArticleTitle(new ArticleLinkTagHandler.LinkableContent(article), State.CA));

        article.setTitle("Beyond PB&J &#8212; Healthy Lunch Ideas");
        assertEquals("Should replace only ampersands that aren't part of an entity", "Beyond PB&amp;J &#8212; Healthy Lunch Ideas",
                _tagHandler.formatArticleTitle(new ArticleLinkTagHandler.LinkableContent(article), State.CA));
    }

    public void testGetAndValidateArticle() {
        _tagHandler.setArticleId(18);

        expect(_articleDao.getArticleFromId(18)).andReturn(null);
        replay(_articleDao);

        Article article;

        article = _tagHandler.getAndValidateArticle();
        verify(_articleDao);
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
