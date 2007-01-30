/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.BaseTestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.content.IArticleDao;
import gs.data.content.Article;
import org.easymock.MockControl;

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
        };
        MockPageContext pageContext = new MockPageContext();
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

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(1)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("<a href=\"http://forum\">title</a>", _out.getOutputBuffer().toString());
    }

    public void testLinkTargetStyle() throws IOException {
        _tagHandler.setArticleId(new Integer(1));
        _tagHandler.setTarget("_blank");
        _tagHandler.setStyleClass("class");

        Article article = new Article();
        article.setId(new Integer(1));
        article.setForumUrl("http://forum");
        article.setTitle("title");
        article.setActive(true);

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(1)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("<a class=\"class\" target=\"_blank\" href=\"http://forum\">title</a>",
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
        
        assertEquals("", _out.getOutputBuffer().toString());
    }

    public void testMissingForumUrl() throws IOException {
        _tagHandler.setArticleId(new Integer(16));

        Article article = new Article();
        article.setId(new Integer(16));
        article.setTitle("title");
        article.setActive(true);

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(16)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("", _out.getOutputBuffer().toString());
    }

    public void testInactiveArticle() throws IOException {
        _tagHandler.setArticleId(new Integer(17));

        Article article = new Article();
        article.setId(new Integer(17));
        article.setForumUrl("http://forum");
        article.setActive(false);
        article.setTitle("title");

        _articleControl.expectAndReturn(_articleDao.getArticleFromId(new Integer(17)), article);
        _articleControl.replay();

        _tagHandler.doTag();
        _articleControl.verify();

        assertEquals("", _out.getOutputBuffer().toString());
    }

}
