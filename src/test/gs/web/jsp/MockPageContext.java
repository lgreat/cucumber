package gs.web.jsp;

import javax.servlet.jsp.JspWriter;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MockPageContext extends org.springframework.mock.web.MockPageContext {

    private MockJspWriter _out;

    public JspWriter getOut() {
        if (_out == null) {
            _out = new MockJspWriter();
        }
        return _out;
    }
}
