package gs.web.jsp;

import gs.data.state.State;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;

import org.springframework.mock.web.MockHttpServletRequest;
import junit.framework.TestCase;

/**
 * thuss
 */
public class FormTagHandlerTest extends TestCase {

    MockPageContext _jspContext;

    FormTagHandlerTestCase _tag;

    public void setUp() {
        _jspContext = new MockPageContext(null, new MockHttpServletRequest());
        _tag = new FormTagHandlerTestCase();
        _tag.setJspContext(_jspContext);
        _tag.setPageContext(_jspContext);
    }

    public void testDoTag() throws JspException {
        _tag.setAction("/somepage");
        _tag.doStartTag();
        _tag.doEndTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("<form accept-charset=\"UTF-8\" " +
                "action=\"http://dev.greatschools.net/somepage\"></form>", output);
        
        _jspContext = new MockPageContext(null, new MockHttpServletRequest());
        _tag.setJspContext(_jspContext);
        _tag.setPageContext(_jspContext);
        _tag.setId("id");
        _tag.setMethod("post");
        _tag.setOnsubmit("onsubmit");
        _tag.setTarget("target");
        _tag.setStyleClass("style");
        _tag.doStartTag();
        _tag.doEndTag();
        output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("<form accept-charset=\"UTF-8\" id=\"id\" " +
                "action=\"http://dev.greatschools.net/somepage\" method=\"post\" class=\"style\" " +
                "onsubmit=\"onsubmit\" target=\"target\"></form>", output);
    }

    /**
     * A more easily testable version of the tag handler
     */
    public class FormTagHandlerTestCase extends FormTagHandler {

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
