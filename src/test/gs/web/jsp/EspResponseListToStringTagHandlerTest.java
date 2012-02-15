package gs.web.jsp;

import static junit.framework.Assert.*;
import gs.data.school.EspResponse;

import java.util.Arrays;

import javax.servlet.jsp.JspContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class EspResponseListToStringTagHandlerTest {

	class TestTag extends EspResponseListToStringTagHandler {

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
		EspResponse er1 = new EspResponse();
		er1.setKey("key1");
		er1.setValue("value_1");
        er1.setPrettyValue("Value 1");
	    
        EspResponse er2 = new EspResponse();
        er2.setKey("key2");
        er2.setValue("value_2");
        er2.setPrettyValue("Value 2");
        
        EspResponse er3 = new EspResponse();
        er3.setKey("key3");
        er3.setValue("value_3");
        er3.setPrettyValue("Value 3");
        
        tag.setList(Arrays.asList(er1, er2, er3));
		tag.doTag();
		String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
		assertEquals("Value 1, Value 2, Value 3", output);
	}

    @Test
    public void testDoTagEmptyElement() throws Exception {
        EspResponse er1 = new EspResponse();
        er1.setKey("key1");
        er1.setValue("value_1");
        er1.setPrettyValue("Value 1");
        
        EspResponse er2 = new EspResponse();
        er2.setKey("key2");
        er2.setValue("");
        er2.setPrettyValue("");
        
        EspResponse er3 = new EspResponse();
        er3.setKey("key3");
        er3.setValue("value_3");
        er3.setPrettyValue("Value 3");
        
        tag.setList(Arrays.asList(er1, er2, er3));
        tag.doTag();
        String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("Value 1, Value 3", output);
    }

    @Test
    public void testDoTagNullElement() throws Exception {
        EspResponse er1 = new EspResponse();
        er1.setKey("key1");
        er1.setValue("value_1");
        er1.setPrettyValue("Value 1");
        
        EspResponse er3 = new EspResponse();
        er3.setKey("key3");
        er3.setValue("value_3");
        er3.setPrettyValue("Value 3");

        tag.setList(Arrays.asList(er1, null, er3));
        tag.doTag();
        String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("Value 1, Value 3", output);
    }

    @Test
    public void testDoNoPretty() throws Exception {
        EspResponse er1 = new EspResponse();
        er1.setKey("key1");
        er1.setValue("value_1");
        er1.setPrettyValue("Value 1");
        
        EspResponse er2 = new EspResponse();
        er2.setKey("key2");
        er2.setValue("");
        er2.setPrettyValue("");
        
        tag.setPretty(Boolean.FALSE);
        tag.setList(Arrays.asList(er1, er2));
        tag.doTag();
        String output = ((MockJspWriter) jspContext.getOut()).getOutputBuffer().toString();
        assertEquals("value_1", output);
    }
}
