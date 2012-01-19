package gs.web.mobile;

import gs.web.request.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.junit.Assert.assertEquals;


public class ParameterizableViewControllerWithMobileAndDesktopViewsTest {
    RequestInfo _requestInfo;
    ParameterizableViewControllerWithMobileAndDesktopViews _controller;

    @Before
    public void setUp() {
        _controller = new ParameterizableViewControllerWithMobileAndDesktopViews();

        _requestInfo = createStrictMock(RequestInfo.class);

    }

    @Test
    public void testChooseViewName() throws Exception {
        _controller.setViewName("view-name");
        _controller.setMobileViewName("mobile-view-name");
        
        expect(_requestInfo.shouldRenderMobileView()).andReturn(false);
        replay(_requestInfo);

        assertEquals("view-name", _controller.resolveViewName(_requestInfo));
        verify(_requestInfo);
        reset(_requestInfo);

        _controller.setViewName("view-name");
        _controller.setMobileViewName("mobile-view-name");

        expect(_requestInfo.shouldRenderMobileView()).andReturn(false);
        replay(_requestInfo);

        assertEquals("view-name", _controller.resolveViewName(_requestInfo));
        verify(_requestInfo);
    }

}