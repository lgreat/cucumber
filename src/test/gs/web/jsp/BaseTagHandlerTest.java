package gs.web.jsp;

import gs.web.BaseTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.MockSessionContext;
import gs.data.state.State;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BaseTagHandlerTest extends BaseTestCase {

    private BaseTagHandlerTestCase _tag;


    protected void setUp() {
        _tag = new BaseTagHandlerTestCase();
        MockPageContext jspContext = new MockPageContext();
        MockSessionContext sessionContext = new MockSessionContext();
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext, PageContext.REQUEST_SCOPE);
        _tag.setJspContext(jspContext);
    }

    public void testEscapeLongState() {
        String s = "This is a $LONGSTATE string";
        // note two spaces between "a" and "california"
        String expected = "This is a  California string";
        String escapedString = _tag.escapeLongstate(s);
        assertEquals(expected, escapedString);
    }

    public void testGetSchool() {
        assertNotNull(_tag.getSchool(State.CA, new Integer(10)));
        assertNull(_tag.getSchool(State.CA, new Integer(99999)));
        assertNull(_tag.getSchool(State.CA, null));
        assertNull(_tag.getSchool(null, new Integer(10)));
        assertNull(_tag.getSchool(null, null));
    }

    public void testHostname() {
        assertEquals("www.greatschools.net", _tag.getHostname());
    }

    public void testDaoGetters() {
        assertNotNull(_tag.getArticleDao());
        assertNotNull(_tag.getDistrictDao());
        assertNotNull(_tag.getSchoolDao());
    }

        /**
     * A more easily testable version of the tag handler
     */
    public class BaseTagHandlerTestCase extends BaseTagHandler {
        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }
    }
}

