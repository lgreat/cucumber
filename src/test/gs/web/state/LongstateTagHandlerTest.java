package gs.web.state;

import junit.framework.TestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import gs.data.state.State;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * @author thuss
 */
public class LongstateTagHandlerTest extends TestCase {
    
    private LongstateTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() {
        _tag = new LongstateTagHandlerTestCase();
        _jspContext = new MockPageContext();
        _tag.setJspContext(_jspContext);
        MockSessionContext sessionContext = new MockSessionContext();
        sessionContext.setState(State.AK);
        _jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext, PageContext.REQUEST_SCOPE);
    }

    public void testDoTag() throws IOException {
        _tag.setText("X$LONGSTATEX");
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("XAlaskaX", output);
    }

    public class LongstateTagHandlerTestCase extends LongstateTagHandler {

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }
    }


}
