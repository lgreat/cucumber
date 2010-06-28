package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.classextension.EasyMock.*;

public class PerlFetchControllerTest extends BaseControllerTestCase {

    private PerlFetchController _controller;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private School _school;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new PerlFetchController();
        _controller.setPerlContentPath("/cgi-bin/test/$STATE/$ID");
        _controller.setViewName("view");

        _schoolProfileHeaderHelper = createStrictMock(SchoolProfileHeaderHelper.class);
        _controller.setSchoolProfileHeaderHelper(_schoolProfileHeaderHelper);

        _school = new School();
        _school.setId(1);
        _school.setDatabaseState(State.CA);

        getRequest().setAttribute("school", _school);
    }

    public void testBasics() {
        assertSame(_schoolProfileHeaderHelper, _controller.getSchoolProfileHeaderHelper());
        assertEquals("/cgi-bin/test/$STATE/$ID", _controller.getPerlContentPath());
        assertEquals("view", _controller.getViewName());
    }

    public void testGetAbsoluteHrefDev() throws Exception {
        String href = _controller.getAbsoluteHref(_school, getRequest());
        assertEquals("http://www.greatschools.org/cgi-bin/test/ca/1", href);
    }

    public void testGetAbsoluteHrefDevWhenDeveloperWorkstation() throws Exception {
        getRequest().setServerName("localhost");
        String href = _controller.getAbsoluteHref(_school, getRequest());
        assertEquals("http://profile.dev.greatschools.org/cgi-bin/test/ca/1", href);
    }

    public void testGetResponseFromUrl() throws Exception {
        String response = _controller.getResponseFromUrl("http://ssprouse.dev.greatschools.org/cgi-bin/test");

        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

    public void testHandleInternal() throws Exception {
        getRequest().setServerName("dev.greatschools.org");
        _controller.setPerlContentPath("/cgi-bin/testpage.cgi");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        assertTrue(mAndV.getModelMap().containsKey(PerlFetchController.HTML_ATTRIBUTE));
    }

    public void testFailure() throws Exception {
        getRequest().setServerName("localhost");

        MockHttpServletResponse response = getResponse();

        _controller.setPerlContentPath("/cgi-bin/doesnotexist");

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), response);

        assertTrue(response.getStatus() == 404);
        assertTrue("/status/error404".equals(mAndV.getViewName()));

    }

}
