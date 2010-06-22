package gs.web.school;

import gs.web.BaseControllerTestCase;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.classextension.EasyMock.*;

public class TestScoresControllerTest extends BaseControllerTestCase {

    TestScoresController _controller;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;


    public void setUp() throws Exception {
        super.setUp();
        _controller = new TestScoresController();
        _controller.setPerlContentPath("/cgi-bin/test");

        _schoolProfileHeaderHelper = createStrictMock(SchoolProfileHeaderHelper.class);
        _controller.setSchoolProfileHeaderHelper(_schoolProfileHeaderHelper);
    }

    public void testBasics() {
        assertSame(_schoolProfileHeaderHelper, _controller.getSchoolProfileHeaderHelper());
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
        getRequest().setServerName("dev.greatschools.org");
        _controller.setPerlContentPath("/cgi-bin/testpage.cgi");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        assertTrue(mAndV.getModelMap().containsKey(TestScoresController.HTML_ATTRIBUTE));
    }

    public void testBean() throws Exception {
        TestScoresController controller = (TestScoresController) getApplicationContext().getBean("/school/testScores.page");
        getRequest().setServerName("localhost");

        controller.setPerlContentPath("/cgi-bin/test");

        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());

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

        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), response);

        assertTrue(response.getStatus() == 404);
        assertTrue("/status/error404".equals(mAndV.getViewName()));

    }

}
