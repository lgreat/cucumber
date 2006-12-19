package gs.web.search;

import gs.web.jsp.BaseTagHandlerTestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.state.State;
import gs.data.school.School;
import gs.data.school.LevelCode;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;


/**
 * @author thuss
 */
public class SchoolTableTagHandlerTest extends BaseTagHandlerTestCase {

    private SchoolTableTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() {
        MockPageContext jspContext = new MockPageContext();
        _jspContext = jspContext;
        _jspContext.setAttribute(PageContext.PAGECONTEXT, jspContext);
        _tag = new SchoolTableTagHandlerTestCase();
        _tag.setApplicationContext(getApplicationContext());

        _tag.setJspContext(jspContext);

    }

    public void testNoSchools() throws IOException, JspException {
        List schools = new ArrayList();
        _tag.setSchools(schools);
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("No schools found") > -1);
        assertTrue(output.indexOf("body content") < 0);
    }

    public void testHasSchools() throws IOException, JspException {
        List schools = new ArrayList();
        School school = new School();
        schools.add(school);
        _tag.setSchools(schools);
        _tag.setJspBodyXhtml("body content");
        _tag.setSrcQuery("state=AK&q=anchorage&c=school&x=0&y=0");

        // Execute the tag
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("found") < 0);
        assertTrue(output.indexOf("body content") > -1);
    }

    public void testRefinementFilteredToNone() throws IOException, JspException {
        _tag.setLevelCode(LevelCode.ELEMENTARY);
        _tag.setSchoolType(new String[]{"public", "private", "charter"});
        _tag.setJspBodyXhtml("body content");
        _tag.setSrcQuery("state=AK&q=anchorage&c=school&x=0&y=0");

        // Execute the tag
        _tag.doTag();
        String output = ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
        assertTrue(output.indexOf("Your refinement did not return any results") > -1);
        assertTrue(output.indexOf("body content") < 0);
    }

    /**
     * A more easily testable version
     */
    public class SchoolTableTagHandlerTestCase extends SchoolTableTagHandler {

        private final StringBuffer _output = new StringBuffer();

        private String _jspBodyXhtml;

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }

        public void setJspBodyXhtml(String xhtml) {
            _jspBodyXhtml = xhtml;
        }

        public StringBuffer getOutput() {
            return _output;
        }

        protected void writeOutput(StringBuffer xhtml) throws IOException {
            _output.append(xhtml);
        }

        protected JspFragment getJspBody() {
            return new JspFragment() {
                public void invoke(Writer writer) throws IOException {
                    writer.write(_jspBodyXhtml);
                }

                public JspContext getJspContext() {
                    return null;
                }
            };
        }

        protected State getState() {
            return State.AK;
        }
    }


}
