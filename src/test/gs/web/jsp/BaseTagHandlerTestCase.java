package gs.web.jsp;

import gs.web.BaseTestCase;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;

import org.springframework.mock.web.MockPageContext;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BaseTagHandlerTestCase extends BaseTestCase {

    public JspContext getJspContext() {
        return null;
    }
}
