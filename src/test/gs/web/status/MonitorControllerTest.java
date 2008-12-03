package gs.web.status;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.data.admin.IPropertyDao;
import java.text.ParseException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * Tests the MonitorController
 */
public class MonitorControllerTest extends BaseControllerTestCase {

    private MonitorController _controller;
    private IPropertyDao _propertyDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (MonitorController) getApplicationContext().getBean(MonitorController.BEAN_ID);
        _propertyDao = createStrictMock(IPropertyDao.class);
        _controller.setPropertyDao(_propertyDao);
    }

    /**
     * The handle request method connects to the database and gets the build version
     */
    public void testHandleRequest() throws IOException, ServletException, ParseException {
        ModelAndView mv = _controller.handleRequest(getRequest(), getResponse());

        assertTrue(mv.getViewName().indexOf("status") > -1);
        Map model = mv.getModel();
        assertNotNull(model);
        assertTrue(((String) model.get("version")).length() > 0);
        assertTrue(((String) model.get("hostname")).length() > 0);
        assertTrue(((String) model.get("branch")).length() > 3);
        assertTrue(((String) model.get("fisheyeGsweb")).indexOf("GSWeb") > -1);
        assertTrue(((String) model.get("fisheyeGsdata")).indexOf("GSData") > -1);
        assertTrue((Boolean) model.get("mainReadWrite"));
        assertEquals(model.get("mainError"), "");
        assertTrue((Boolean) model.get("stateReadWrite"));
        assertEquals(model.get("stateError"), "");
        assertEquals(1, getRequest().getSession(false).getAttribute("hitcount"));
        assertNotNull(getRequest().getSession(false).getAttribute("thishost"));
        assertNotNull(getRequest().getSession(false).getAttribute("lasthost"));
        assertTrue(((String) model.get("indexVersion")).length() > 0);
        Map environment = (Map)model.get("environment");
        assertNotNull(environment);
        // assertNotNull(environment.get("log4j.configuration"));
        assertNotNull(environment.get("log4j.mailappender"));
        assertTrue(((String) model.get("abConfiguration")).length() > 0);

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("logmessage", "test");
        request.setMethod("POST");
        // This exercises the logging code and tests hitcount incrementing
        mv = _controller.handleRequest(getRequest(), getResponse());
        assertNotNull(mv);
        HttpSession session = request.getSession(true);
        assertEquals(2, session.getAttribute("hitcount"));
    }

    public void testIncrementVersion() {
        String cookieValue = "1";
        Cookie cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        getRequest().setCookies(new Cookie[] {cookie});

        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1").times(3);
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertEquals("2", getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER).getValue());
    }

    public void testIncrementVersion2() {
        String cookieValue = "1";
        Cookie cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        getRequest().setCookies(new Cookie[] {cookie});

        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("5/1").times(6);
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertEquals("5", getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER).getValue());
    }

    public void testIncrementVersion3() {
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertNull(getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER));
    }

    public void testIncrementVersion4() {
        String cookieValue = "1";
        Cookie cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        getRequest().setCookies(new Cookie[] {cookie});

        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1").times(102);
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertEquals("Expect original value to be incremented 101 times",
                "102", getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER).getValue());
    }
}
