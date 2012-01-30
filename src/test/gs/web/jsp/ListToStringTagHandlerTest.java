package gs.web.jsp;

import java.util.Arrays;

import javax.servlet.jsp.JspContext;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ListToStringTagHandlerTest {

	class TestTag extends ListToStringTagHandler {

		@Override
		public JspContext getJspContext() {
			return jspContext;
		}
	}

	MockPageContext jspContext;

	TestTag tag;

	@Before
	public void setup() {
		jspContext = new MockPageContext(null, new MockHttpServletRequest());
		tag = new TestTag();
	}

	@Test
	public void testDoTag() throws Exception {
		tag.setList(Arrays.asList("a", "b", "c"));
		tag.doTag();
		String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
		assertEquals("a, b, c", output);
	}

    @Test
    public void testDoTagEmptyElement() throws Exception {
        tag.setList(Arrays.asList("a", "", "c"));
        tag.doTag();
        String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("a, c", output);
    }

    @Test
    public void testDoTagNullElement() throws Exception {
        tag.setList(Arrays.asList("a", null, "c"));
        tag.doTag();
        String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("a, c", output);
    }
}
