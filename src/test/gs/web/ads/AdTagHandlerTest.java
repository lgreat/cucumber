/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagHandlerTest.java,v 1.1 2006/09/19 23:31:10 dlee Exp $
 */
package gs.web.ads;

import gs.web.BaseTestCase;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.MockSessionContext;
import gs.web.util.context.ISessionContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Test AdTagHandler
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdTagHandlerTest extends BaseTestCase {
    public void testDeferredContent() throws IOException, JspException {
        HttpServletRequest request = new MockHttpServletRequest();
        MockSessionContext sc = new MockSessionContext();
        JspContext jspContext = new MockPageContext(new MockServletContext(), request);
        jspContext.setAttribute(ISessionContext.REQUEST_ATTRIBUTE_NAME, sc);

        AdTagHandler tag = new AdTagHandler();
        tag.setJspContext(jspContext);
        tag.setPosition("x22");
        tag.doTag();

        MockJspWriter out = (MockJspWriter) jspContext.getOut();
        String output = out.getOutputBuffer().toString();
        assertEquals("<script type=\"text/javascript\">OAS_AD('x22');</script>", output);
        assertEquals(Boolean.TRUE, (Boolean) request.getAttribute("x22"));


        //try to set the same ad position
        try {
            tag.setPosition("x22");
            tag.doTag();
            fail("x22 already set so we shouldn't allow it to be set again");
        } catch (IllegalArgumentException e){

        }

        //test cobrand served ad
        sc = new MockSessionContext();
        sc.setCobrand("yahoo");

        jspContext = new MockPageContext(new MockServletContext(), request);
        jspContext.setAttribute(ISessionContext.REQUEST_ATTRIBUTE_NAME, sc);

        tag = new AdTagHandler();
        tag.setJspContext(jspContext);
        tag.setPosition("x40");
        tag.doTag();
        out = (MockJspWriter) jspContext.getOut();
        output = out.getOutputBuffer().toString();
        assertTrue(output.indexOf("yahoo") > -1);

        _log.debug(output);

    }
}
