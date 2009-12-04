package gs.web.content;

import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import junit.framework.TestCase;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * @author thuss
 */
public class BlogFeedTagHandlerTest extends TestCase {
    private BlogFeedTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() {
        _tag = new BlogFeedTagHandlerTestCase();
        resetJspContext();
    }

    private void resetJspContext() {
        _jspContext = new MockPageContext();
        _tag.setJspContext(_jspContext);
    }

    private String getJspContextOutput() {
        return ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
    }

    public void testVoid() {
        assertTrue(true);
    }
    public void xtestDoTag() throws IOException, JspException {
        _tag.setAtomUrl("http://blogs.greatschools.org/billsblog/atom.xml");
        _tag.setDefaultTitle("Bill's thoughts on education");
        _tag.setDefaultUrl("http://blogs.greatschools.org/billsblog/");
        _tag.setType("splashBlog");
        _tag.setShowDate(true);
        _tag.doTag();
        String output = getJspContextOutput();
        System.out.println(output);
    }
    public void testBlogTag() throws IOException, JspException {
        _tag.setAtomUrl("http://feeds2.feedburner.com/GreatschoolsBlogAdvanced");

        _tag.setDefaultUrl("http://blogs.greatschools.org/billsblog/");
        _tag.setType("splashBlog");
        _tag.setShowDate(true);
        _tag.doTag();
        String output = getJspContextOutput();
        System.out.println(output);
    }
    /*
    public void testDoTag() throws IOException, JspException {
        // Check error handling
        _tag.setDefaultTitle("defaulttitle");
        _tag.setDefaultUrl("http://aurl");
        _tag.setAtomUrl("http://x");
        _tag.doTag();
        String output = getJspContextOutput();
        assertTrue(output.indexOf("defaulttitle") > -1);
        assertTrue(output.indexOf("aurl") > -1);

        // Now try it with a feed that works
        // @todo change this to use a mock SyndFeed
        resetJspContext();
        _tag.setAtomUrl("http://billsblog.greatschools.org/atom.xml");
        _tag.doTag();
        output = getJspContextOutput();
        assertTrue(output.indexOf("onclick") > -1);
        assertTrue(output.indexOf("http://billsblog.greatschools.org") > -1);
    }
    */

    /**
     * A more easily testable version of the tag handler
     */
    public class BlogFeedTagHandlerTestCase extends BlogFeedTagHandler {

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }
    }

}