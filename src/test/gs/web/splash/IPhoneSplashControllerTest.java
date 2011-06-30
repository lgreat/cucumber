package gs.web.splash;

import gs.web.BaseControllerTestCase;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import gs.web.GsMockHttpServletRequest;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class IPhoneSplashControllerTest extends BaseControllerTestCase {


    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testURI() throws Exception {
        IPhoneSplashController splashController = new IPhoneSplashController();
        ModelAndView mav;

        // Test that parameters work
        GsMockHttpServletRequest request = getRequest();
        request.setParameter(IPhoneSplashController.PARAM_REFERRER, "/index.page?x=1&y=2");
        mav = splashController.handleRequestInternal(request, getResponse());
        assertTrue(mav.getModel().containsKey(IPhoneSplashController.MODEL_REFERRER));
        assertEquals("Referring url with query args not HTML escaped properly", "/index.page?x=1&amp;y=2", mav.getModel().get(IPhoneSplashController.MODEL_REFERRER));

        // Explicitly test against HTML injection
        request.setParameter(IPhoneSplashController.PARAM_REFERRER, "/\"/><b>injection</b><a href=\"/");
        mav = splashController.handleRequestInternal(request, getResponse());
        assertTrue(mav.getModel().containsKey(IPhoneSplashController.MODEL_REFERRER));
        assertEquals("Referring url with HTML injection not escaped properly", "/&quot;/&gt;&lt;b&gt;injection&lt;/b&gt;&lt;a href=&quot;/", mav.getModel().get(IPhoneSplashController.MODEL_REFERRER));
    }

    public GsMockHttpServletRequest getRequest() {
        return _request;
    }

    public MockHttpServletResponse getResponse() {
        return _response;
    }
}
