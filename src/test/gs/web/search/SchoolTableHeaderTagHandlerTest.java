package gs.web.search;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.web.BaseControllerTestCase;
import gs.data.state.State;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;

/**
 * @author thuss
 */
public class SchoolTableHeaderTagHandlerTest extends BaseControllerTestCase {
    private SchoolTableHeaderTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() throws Exception {
        super.setUp();
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
    }

    public void testCreateFilterBuffer() throws Exception {
        String expected = "Elementary (<a href=\"/schools.page?city=Alameda&amp;state=CA\">remove</a>)";
        _tag.setLevelCode(LevelCode.ELEMENTARY);
        StringBuffer result = _tag.createFilterBuffer("city=Alameda&amp;state=CA&amp;lc=e", getRequest());
        assertEquals(expected, result.toString());

        expected = "Elementary (<a href=\"/schools.page?city=Alameda&state=CA\">remove</a>)";
        result = _tag.createFilterBuffer("city=Alameda&state=CA&lc=e", getRequest());
        assertEquals(expected, result.toString());


        expected = "Public (<a href=\"/schools.page?city=Alameda&amp;state=CA\">remove</a>)";
        _tag.setLevelCode(null);
        _tag.setSchoolType(new String[] {SchoolType.PUBLIC.getSchoolTypeName()});
        result = _tag.createFilterBuffer("city=Alameda&amp;state=CA&amp;st=public", getRequest());
        assertEquals(expected, result.toString());
    }

    public void testPossibleAddLinebreak() throws Exception {
        MockJspWriter out = (MockJspWriter)_jspContext.getOut();

        out.clear();
        _tag.possiblyAddLinebreak("Alameda", out);
        assertEquals("linebreak should not be written for short input", "",
                out.getOutputBuffer().toString());

        out.clear();
        _tag.possiblyAddLinebreak("Alameda County District Court of Appeals", out);
        assertEquals("linebreak should be written for long input", "<br/>",
                out.getOutputBuffer().toString());
    }
    /**
     * A more easily testable version of the tag handler
     */
    public class SchoolTableHeaderTagHandlerTestCase extends SchoolTableHeaderTagHandler {

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
