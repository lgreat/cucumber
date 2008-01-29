package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.data.admin.IPropertyDao;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GetVariantConfigurationControllerTest extends BaseControllerTestCase {
    private GetVariantConfigurationController _controller;
    private IPropertyDao _propertyDao;
    private MockHttpServletResponse _response;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new GetVariantConfigurationController();
        _propertyDao = createMock(IPropertyDao.class);
        _controller.setPropertyDao(_propertyDao);
        _response = getResponse();
    }

    public void testHandleRequest() throws Exception {
        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("8/1/1");
        replay(_propertyDao);
        _controller.handleRequest(getRequest(), _response);
        verify(_propertyDao);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                _response.getContentType());
        assertEquals("8/1/1", _response.getContentAsString());
    }

    public void testHandleRequestProblem() throws Exception {
        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn(null);
        replay(_propertyDao);
        _controller.handleRequest(getRequest(), _response);
        verify(_propertyDao);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                _response.getContentType());
        assertEquals("1", _response.getContentAsString());
    }
}
