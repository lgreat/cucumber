package gs.web.search;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.web.BaseControllerTestCase;
import gs.data.state.State;

/**
 * @author thuss
 */
public class SchoolTableHeaderTagHandlerTest extends BaseControllerTestCase {
    private SchoolTableHeaderTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() {
        MockPageContext jspContext = new MockPageContext();
        _jspContext = jspContext;
        _jspContext.setAttribute(PageContext.PAGECONTEXT, jspContext);
        _tag = new SchoolTableHeaderTagHandlerTestCase();
        _tag.setApplicationContext(getApplicationContext());

        _tag.setJspContext(jspContext);

    }

    public void testDistrict() throws IOException, JspException {
        // Setup the tag for testing
        _tag.setDistrictId(new Integer(18));

        // Execute the tag
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("Schools in Anchorage School District") > -1);
        assertTrue(output.indexOf("/cgi-bin/ak/district_profile/") > -1);
        assertTrue(output.indexOf("Elementary") > -1);
        assertTrue(output.indexOf("Middle") > -1);
        assertTrue(output.indexOf("High") > -1);
    }

    public void testCity() throws IOException, JspException {
        // Setup the tag for testing
        _tag.setCityName("Long City Name");
        _tag.setCityDisplayName("Some City");

        // Execute the tag
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("Some City Schools") > -1);
        assertTrue(output.indexOf("/city/Long_City_Name/AK") > -1);
        assertTrue(output.indexOf("public schools") > -1);
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
