package gs.web.search;

import gs.web.jsp.BaseTagHandlerTestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;

import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.InputSource;
//import org.w3c.dom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;

import java.io.StringReader;
import java.util.List;


/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandlerTest extends BaseTagHandlerTestCase {

    private DocumentBuilder _builder;

    protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        _builder = factory.newDocumentBuilder();
    }

    public void testNoSchools() throws Exception {
        SchoolTableTagHandler tag = new SchoolTableTagHandler();
        MockPageContext context = new MockPageContext();
        context.setAttribute(PageContext.PAGECONTEXT, context);
        tag.setJspContext(context);
        tag.doTag();


        MockJspWriter writer =(MockJspWriter)context.getOut();
        String string = writer.getOutputBuffer().toString();
        StringReader reader = new StringReader(string);
        //InputSource inputSource = new InputSource(reader);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(reader);

        // first, assert that the first two elements are form/table
        Element formElement = doc.getRootElement();
        assertTrue("form".equals(formElement.getName().trim()));
        List kids = formElement.getChildren();
        Element inputElement = (Element)kids.get(0);
        assertTrue("input".equals(inputElement.getName()));
        //System.out.println ("string: " + string);
    }
}
