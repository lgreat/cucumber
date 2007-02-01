package gs.web.content;

import gs.data.content.Article;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ArticleLinkTagHandlerTest extends TestCase {
    private ArticleLinkTagHandler _tag;
    private MockPageContext _pageContext;

    protected void setUp() throws Exception {
        super.setUp();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, new MockSessionContext());
        request.setAttribute(PageContext.PAGECONTEXT, new MockPageContext());

        _pageContext = new MockPageContext(new MockServletContext(), request);
        _tag = new ArticleLinkTagHandler();
        _tag.setJspContext(_pageContext);
        _tag.setWrappingElement("li");
    }

    public void testEncoding() throws IOException {
        Article article = new Article();
        article.setId(new Integer(1));
        article.setActive(true);
        article.setStatesAsString("--");
        article.setTitle("& is the new !");

        _tag.setArticle(article);
        _tag.doTag();


        MockJspWriter writer = (MockJspWriter) _pageContext.getOut();
        assertEquals("<li><a href=\"/cgi-bin/showarticle/ca/1\">&amp; is the new !</a></li>", writer.getOutputBuffer().toString());
    }
}
