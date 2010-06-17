package gs.web.school;

import gs.web.BaseControllerTestCase;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;


public class TestScoresControllerTest extends BaseControllerTestCase {

    TestScoresController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new TestScoresController();
        _controller.setPerlContentPath("/cgi-bin/test");
    }

    public void testGetAbsoluteHrefDev() throws Exception {
        String href = _controller.getAbsoluteHref(getRequest());
        assertEquals("http://www.greatschools.org:80/cgi-bin/test", href);
    }

    public void testGetAbsoluteHrefDevWhenDeveloperWorkstation() throws Exception {
        getRequest().setServerName("localhost");
        String href = _controller.getAbsoluteHref(getRequest());
        assertEquals("http://ssprouse.dev.greatschools.org/cgi-bin/test", href);
    }

    public void testGetResponseFromUrl() throws Exception {
        TestScoresController controller = new TestScoresController();

        String response = controller.getResponseFromUrl("http://ssprouse.dev.greatschools.org/cgi-bin/test");

        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    public void testHandleInternal() throws Exception {

        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());

        assertTrue(mAndV.getModelMap().containsKey(TestScoresController.HTML_ATTRIBUTE));
    }

    public void testBean() throws Exception {
        TestScoresController controller = (TestScoresController) getApplicationContext().getBean("/school/testScores.page");
        getRequest().setServerName("localhost");

        controller.setPerlContentPath("/cgi-bin/test");

        ModelAndView mAndV = controller.handleRequest(getRequest(), getResponse());

        assertTrue(mAndV.getModelMap().containsKey(TestScoresController.HTML_ATTRIBUTE));
        
        String r = (String) mAndV.getModelMap().get(TestScoresController.HTML_ATTRIBUTE);
        assertEquals("<?xml version=\"1.0\"?>\n" +
                "<methodResponse>\n" +
                "    <fault>\n" +
                "        <value>\n" +
                "            <struct>\n" +
                "                <member>\n" +
                "                    <name>faultCode</name>\n" +
                "                    <value><int>4</int></value>\n" +
                "                </member>\n" +
                "                <member>\n" +
                "                    <name>faultString</name>\n" +
                "                    <value><string>Too many parameters.</string></value>\n" +
                "                </member>\n" +
                "            </struct>\n" +
                "        </value>\n" +
                "    </fault>\n" +
                "</methodResponse>", StringUtils.trim(r));
    }

    public void testFailure() throws Exception {

        TestScoresController controller = new TestScoresController();
        getRequest().setServerName("localhost");

        MockHttpServletResponse response = getResponse();

        controller.setPerlContentPath("/cgi-bin/doesnotexist");

        ModelAndView mAndV = controller.handleRequest(getRequest(), response);

        assertTrue(response.getStatus() == 404);
        assertTrue("/status/error404".equals(controller.getViewName()));

    }

}
