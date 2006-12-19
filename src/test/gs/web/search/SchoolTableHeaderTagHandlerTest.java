package gs.web.search;

import junit.framework.TestCase;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;
import java.io.Writer;

import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.web.BaseControllerTestCase;
import gs.data.state.State;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

/**
 * @author thuss
 */
public class SchoolTableHeaderTagHandlerTest extends BaseControllerTestCase {

    public void testSchoolTableHeaderTag() throws IOException, JspException {
        // Setup the tag for testing
        MockPageContext jspContext = new MockPageContext();
        jspContext.setAttribute(PageContext.PAGECONTEXT, jspContext);
        SchoolTableHeaderTagHandlerTestCase tag = new SchoolTableHeaderTagHandlerTestCase();
        tag.setApplicationContext(getApplicationContext());
        tag.setDistrictId(new Integer(18));
        tag.setJspContext(jspContext);

        // Execute the tag
        tag.doTag();
        String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("Schools in Anchorage School District") > -1);
        assertTrue(output.indexOf("/cgi-bin/ak/district_profile/") > -1);
        assertTrue(output.indexOf("Elementary") > -1);
        assertTrue(output.indexOf("Middle") > -1);
        assertTrue(output.indexOf("High") > -1);

        // Try it on a city
//        tag.setDistrictId(null);
//        tag.setCityName("Anchorage");
//        tag.doTag();
//        output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
//        System.out.println(output);

    }

    /**
     * A more easily testable DeferredContentTagHandler that
     * allows the test to provide a mock or stubbed JspContext,
     * access the resulting output of the tag, and set what the body of
     * the tag should evaluate to.
     */
    public class SchoolTableHeaderTagHandlerTestCase extends SchoolTableHeaderTagHandler {

        private final StringBuffer _output = new StringBuffer();

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }

        protected State getState() {
            return State.AK;
        }


    }

}
