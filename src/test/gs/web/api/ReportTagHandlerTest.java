package gs.web.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import gs.web.BaseTestCase;

import javax.servlet.jsp.PageContext;

import static org.easymock.classextension.EasyMock.*;

import java.io.IOException;

/**
 * @author chriskimm@greatschools.org
 */
public class ReportTagHandlerTest extends BaseTestCase {

    private ReportTagHandler _tag;
    private MockJspWriter _out;
    private HttpClient _httpClient;

    @Override
    public void setUp() throws Exception {
        _tag = new ReportTagHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, new MockSessionContext());
        request.setAttribute(PageContext.PAGECONTEXT, new MockPageContext());
        MockPageContext pageContext = new MockPageContext(new MockServletContext(), request);
        _out = (MockJspWriter) pageContext.getOut();
        _tag.setJspContext(pageContext);
        _httpClient = createMock(HttpClient.class);
        _tag.setHttpClient(_httpClient);        
    }

    public void testDoTag() throws Exception {
        String url = "/reports/all?key=1234";
        GetMethod getMethod = new GetMethod(url) {
            public String getResponseBodyAsString() throws IOException {
                return "foo fala";
            }
        };
        _tag.setType("all");
        _tag.setMethod(getMethod);
        expect(_httpClient.executeMethod(getMethod)).andReturn(HttpStatus.SC_OK);
        replay(_httpClient);

        assertEquals ("http://localhost/apiservice/reports/all", _tag.getReportUrl());
        
        _tag.doTag();
        verify(_httpClient);
        System.out.println (_out.getOutputBuffer().toString());
    }

    public void testDecorateWithAccountInfo() {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><report><result>" +
                "<field type=\"api_key\"><value>1234abc</value></field><field type=\"count\"><value>50</value></field>" +
                "</result><result><field type=\"api_key\"><value>1234abcx</value></field><field type=\"count\">" +
                "<value>3</value></field></result></report>";

        _tag.setApplicationContext(getApplicationContext());
        _tag.decorateWithAccountInfo(input);
    }

    @Ignore
    public void debugTest() throws Exception {
        ReportTagHandler _tag = new ReportTagHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, new MockSessionContext());
        request.setAttribute(PageContext.PAGECONTEXT, new MockPageContext());
        request.setLocalPort(8080);
        MockPageContext pageContext = new MockPageContext(new MockServletContext(), request);
        _out = (MockJspWriter) pageContext.getOut();
        _tag.setJspContext(pageContext);
        _tag.setType("all");
        _tag.setDisplay("table");
        _tag.doTag();
        System.out.println (_out.getOutputBuffer().toString());
    }
}
