package gs.web.jsp;

import gs.web.BaseTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.MockSessionContext;
import gs.data.state.State;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BaseTagHandlerTest extends BaseTestCase {

    private BaseTagHandlerTestCase _tag;
    private MockPageContext jspContext;


    protected void setUp() {
        _tag = new BaseTagHandlerTestCase();
        jspContext = new MockPageContext();
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

    public void testHostnameIsRetrievedFromSessionContext() {
        String expectedHostname = "www.greatschools.net";

        MockControl mockSessionContext = MockClassControl.createControl(SessionContext.class);
        SessionContext sessionContext = (SessionContext) mockSessionContext.getMock();
        sessionContext.getHostName();
        mockSessionContext.setReturnValue(expectedHostname);
        mockSessionContext.replay();
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext, PageContext.REQUEST_SCOPE);

        assertEquals("Unexpected hostname", expectedHostname, _tag.getHostname());

        mockSessionContext.verify();
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

