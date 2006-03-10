package gs.web.search;

import junit.framework.TestCase;

import java.io.StringWriter;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MainResultsTagTest extends TestCase {

    public void testDoTag() throws Exception {
        MainResultsTagHandler tag = new MainResultsTagHandler();
        StringWriter writer = new StringWriter();
        tag.setWriter(writer);
        assertNotNull(tag.getWriter());

        tag.setWriter(null);
        //assertNotNull(tag.getWriter());
        //tag.doTag();
    }

    public void testSettersAndGetters() {
        MainResultsTagHandler tag = new MainResultsTagHandler();
        tag.setConstraint("schools");
        assertEquals("schools", tag.getConstraint());

        //JspContext context = new MockPageContext();
        //tag.setJspContext(context);
        //assertNotNull(tag.getWriter());

    }
}

