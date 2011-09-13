package gs.web.jsp.link;

import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.util.CmsUtil;
import junit.framework.TestCase;

public class CmsCategoryBrowseTagHandlerTest extends TestCase {
    private CmsCategoryBrowseTagHandler _handler;

    public void setUp() throws Exception {
        super.setUp();
        CmsUtil.enableCms();
        _handler = new CmsCategoryBrowseTagHandler();
        CmsUtil.disableCms();
    }

    public void testDoTagWithLanguage() throws Exception {
        MockPageContext pc = new MockPageContext();
        _handler.setPageContext(pc);
        _handler.setTopicIDs("1");
        _handler.setLanguage("ES");

        _handler.doStartTag();
        _handler.doAfterBody();
        _handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"http://localhost/articles/?topics=1&amp;language=ES\"></a>",
                out.getOutputBuffer().toString());
    }

    public void testDoTagWithoutBody() throws Exception {
        MockPageContext pc = new MockPageContext();
        _handler.setPageContext(pc);
        _handler.setTopicIDs("1");

        _handler.doStartTag();
        _handler.doAfterBody();
        _handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"http://localhost/articles/?topics=1\"></a>",
                out.getOutputBuffer().toString());
    }
}
