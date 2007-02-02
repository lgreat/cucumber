/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.BaseTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.MockSessionContext;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.data.state.State;
import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * Provides testing for the ArticleForumLinkTagHandler class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ArticleForumLinkTagHandlerTest extends BaseTestCase {
    private ArticleForumLinkTagHandler _tagHandler;
    private IArticleDao _articleDao;
    private MockControl _articleControl;
    private MockJspWriter _out;

    public void setUp() throws Exception {
        super.setUp();
        _tagHandler = new ArticleForumLinkTagHandler() {
            protected IArticleDao getArticleDao() {
                return _articleDao;
            }
            protected SessionContext getSessionContext() {
                MockSessionContext sc = new MockSessionContext();
                sc.setState(State.CA);
                return sc;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, new MockSessionContext());
        request.setAttribute(PageContext.PAGECONTEXT, new MockPageContext());

        MockPageContext pageContext = new MockPageContext(new MockServletContext(), request);

        _tagHandler.setJspContext(pageContext);
        _out = (MockJspWriter) pageContext.getOut();
        _articleControl = MockControl.createControl(IArticleDao.class);
        _articleDao = (IArticleDao) _articleControl.getMock();
        
    }

    public void testLink() throws IOException {
        _tagHandler.setArticleId(new Integer(1));

        Article article = new Article();
        article.setId(new Integer(1));
        article.setForumUrl("http://forum");
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(1)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("<a href=\"http://forum\">title</a>", _out.getOutputBuffer().toString());
    }

    public void testMissingForumUrl() throws IOException {
        _tagHandler.setArticleId(new Integer(16));

        Article article = new Article();
        article.setId(new Integer(16));
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(16)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("", _out.getOutputBuffer().toString());
    }

    public void testGetAndValidateArticle() {
        Article article = new Article();
        article.setId(new Integer(16));
        article.setTitle("title");
        article.setActive(true);
        article.setStatesAsString("CA");

        _tagHandler.setArticle(article);

        Article result = _tagHandler.getAndValidateArticle();
        assertNull("Expected null for missing forum_url", result);

        article.setForumUrl("something");
        result = _tagHandler.getAndValidateArticle();
        assertNotNull("Expected valid article", result);
    }

    public void testInactiveArticle() throws IOException {
        _tagHandler.setArticleId(new Integer(17));

        Article article = new Article();
        article.setId(new Integer(17));
        article.setForumUrl("http://forum");
        article.setActive(false);
        article.setTitle("title");
        article.setStatesAsString("CA");

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(17)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("", _out.getOutputBuffer().toString());
    }

    public void testEscapingForumUrl() throws IOException {
        _tagHandler.setArticleId(new Integer(17));

        Article article = new Article();
        article.setId(new Integer(17));
        article.setForumUrl("http://forum/?foo=bar&taz=mil");
        article.setActive(true);
        article.setTitle("title");
        article.setStatesAsString("CA");

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(17)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("Expected output string with escaped ampersand not received",
                "<a href=\"http://forum/?foo=bar&amp;taz=mil\">title</a>",
                _out.getOutputBuffer().toString());
    }

}
