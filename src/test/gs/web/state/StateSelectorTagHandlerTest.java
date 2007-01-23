package gs.web.state;

import junit.framework.TestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.state.State;

import javax.servlet.jsp.JspContext;
import java.io.IOException;

/**
 * @author thuss
 */
public class StateSelectorTagHandlerTest extends TestCase {
    private StateSelectorTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() {
        _tag = new StateSelectorTagHandlerTestCase();
        _jspContext = new MockPageContext();
        _tag.setJspContext(_jspContext);
    }

    public void testDoTag() throws IOException {
        // Start with a multi-select (no default selection even if state set)
        _tag.setMultiple(true);
        _tag.setName("name");
        _tag.setOnChange("onchange");
        _tag.setSize(20);
        _tag.setState(State.AK);
        _tag.setStyleClass("style");
        _tag.setStyleId("id");
        _tag.setUsingLongNames(true);
        _tag.setTabIndex(15);
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue("Output is not expected: " + output, output.indexOf("<select id=\"id\" name=\"name\" class=\"style\" onchange=\"onchange\" multiple=\"multiple\" size=\"20\" tabindex=\"15\">") > -1);
        assertTrue(output.indexOf("<option value=\"AK\">Alaska</option>") > -1);

        // Single select with longnames disabled
        _jspContext = new MockPageContext();
        _tag.setJspContext(_jspContext);
        _tag.setMultiple(false);
        _tag.setUsingLongNames(false);
        _tag.doTag();
        output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("<select id=\"id\" name=\"name\" class=\"style\" onchange=\"onchange\" tabindex=\"15\">") > -1);
        assertTrue(output.indexOf("<option value=\"AK\" selected=\"selected\">AK</option>") > -1);

        // Single select with no state selected and overriden no state label
        _jspContext = new MockPageContext();
        _tag.setJspContext(_jspContext);
        _tag.setUseNoState(true);
        _tag.setNoStateLabel("XX");
        _tag.doTag();
        output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("<select id=\"id\" name=\"name\" class=\"style\" onchange=\"onchange\" tabindex=\"15\">") > -1);
        assertTrue(output.indexOf("<option value=\"\" selected=\"selected\">XX</option>") > -1);
        assertTrue(output.indexOf("<option value=\"AK\">AK</option>") > -1);
    }

    public class StateSelectorTagHandlerTestCase extends StateSelectorTagHandler {

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }
    }
}
