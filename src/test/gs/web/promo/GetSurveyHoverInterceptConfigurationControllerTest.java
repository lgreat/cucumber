package gs.web.promo;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.mock.web.MockHttpServletResponse;
import gs.web.BaseControllerTestCase;
import gs.data.admin.IPropertyDao;

import static org.easymock.EasyMock.*;

/**
 * Created by IntelliJ IDEA.
 * User: john
 * Date: Jul 15, 2008
 * Time: 6:06:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSurveyHoverInterceptConfigurationControllerTest extends BaseControllerTestCase {
    private IPropertyDao _propertyDao;
    private MockHttpServletResponse _response;
    private GetSurveyHoverInterceptConfigurationController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new GetSurveyHoverInterceptConfigurationController();
        _propertyDao = createMock(IPropertyDao.class);
        _controller.setPropertyDao(_propertyDao);
        _response = getResponse();
    }

    public void testHandleRequest() throws Exception {
        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("32");
        replay(_propertyDao);
        _controller.handleRequest(getRequest(), _response);
        verify(_propertyDao);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                _response.getContentType());
        assertEquals("32", _response.getContentAsString());
    }

    public void testHandleRequestProblem() throws Exception {
        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn(null);
        replay(_propertyDao);
        _controller.handleRequest(getRequest(), _response);
        verify(_propertyDao);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                _response.getContentType());
        assertEquals("0", _response.getContentAsString());
    }

}
