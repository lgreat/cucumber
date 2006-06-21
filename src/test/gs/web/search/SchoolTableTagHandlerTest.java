package gs.web.search;

import gs.web.jsp.BaseTagHandlerTestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;

import javax.servlet.jsp.PageContext;
import java.io.StringReader;


/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandlerTest extends BaseTagHandlerTestCase {

    public void testNoSchools() throws Exception {
        SchoolTableTagHandler tag = new SchoolTableTagHandler();
        MockPageContext context = new MockPageContext();
        context.setAttribute(PageContext.PAGECONTEXT, context);
        tag.setJspContext(context);
        tag.doTag();


        MockJspWriter writer =(MockJspWriter)context.getOut();
        String string = "<x>"+writer.getOutputBuffer().toString() + "</x>";
        StringReader reader = new StringReader(string);
        //InputSource inputSource = new InputSource(reader);
    }
}
