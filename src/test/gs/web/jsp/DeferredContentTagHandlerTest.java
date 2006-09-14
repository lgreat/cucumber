package gs.web.jsp;

import junit.framework.TestCase;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;

import java.io.IOException;
import java.io.Writer;

/**
 * Tests the deferred content handler tag to verify it prints out the marker
 * tag and that it puts the evaluated XHTML into the request scope.
 *
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class DeferredContentTagHandlerTest extends TestCase {

    public void testDeferredContent() throws IOException, JspException {
        final String divId = "h1hello";
        final String htmlToDefer = "<h1>hello</h1>";

        // Setup the tag for testing
        JspContext jspContext = new MockPageContext();
        DeferredContentTagHandlerTestCase tag = new DeferredContentTagHandlerTestCase();
        tag.setJspContext(jspContext);
        tag.setId(divId);
        tag.setJspBodyXhtml(htmlToDefer);

        // Execute the tag
        tag.doTag();

        // Make sure it output the correct marker HTML
        String expectedMarkerXhtml = "<div id=\"" + divId + "\"></div>";
        assertEquals(expectedMarkerXhtml, tag.getOutput().toString());

        // Make sure it deferred the content by putting it in the request scope
        String expectedDeferredXhtml = "<div id=\"defer-" + divId + "\">" + htmlToDefer + "</div>";
        assertEquals(expectedDeferredXhtml, jspContext.getAttribute("deferredContent", PageContext.REQUEST_SCOPE));

        // Now if we run the tag a 2nd time it should append to existing deferred content
        tag.doTag();
        assertEquals(expectedDeferredXhtml + expectedDeferredXhtml,
                jspContext.getAttribute("deferredContent", PageContext.REQUEST_SCOPE));

    }

    /**
     * A more easily testable DeferredContentTagHandler that
     * allows the test to provide a mock or stubbed JspContext,
     * access the resulting output of the tag, and set what the body of
     * the tag should evaluate to.
     */
    public class DeferredContentTagHandlerTestCase extends DeferredContentTagHandler {

        private final StringBuffer _output = new StringBuffer();

        private String _jspBodyXhtml;

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }

        public void setJspBodyXhtml(String xhtml) {
            _jspBodyXhtml = xhtml;
        }

        public StringBuffer getOutput() {
            return _output;
        }

        protected void writeOutput(StringBuffer xhtml) throws IOException {
            _output.append(xhtml);
        }

        protected JspFragment getJspBody() {
            return new JspFragment() {
                public void invoke(Writer writer) throws IOException {
                    writer.write(_jspBodyXhtml);
                }

                public JspContext getJspContext() {
                    return null;
                }
            };
        }
    }

}
