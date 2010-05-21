package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.data.geo.ICity;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

public class CompareFooterModuleControllerTest extends BaseControllerTestCase {

    private CompareFooterModuleController _controller;

    public void setUp() throws Exception {
        super.setUp();
        getRequest().setMethod("GET");
        _controller = (CompareFooterModuleController)getApplicationContext().
                getBean(CompareFooterModuleController.BEAN_ID);
    }

    public void testWithNoParams() throws Exception {
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull(mAndV.getModel().get(CompareFooterModuleController.MODEL_CITIES));
        assertEquals(CompareFooterModuleController.VIEW_NAME, mAndV.getViewName());
    }

    public void testWithValidParameters() throws Exception {
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("city", "Alameda");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        List<ICity> cities = (List<ICity>)mAndV.getModel().get(CompareFooterModuleController.MODEL_CITIES);
        assertTrue("There should be more than 1000 cities in CA, getting only " + cities.size(), cities.size() > 1000);
        ICity c = (ICity)mAndV.getModel().get(CompareFooterModuleController.MODEL_CITY);
        assertEquals("City name in model doesn't match city parameter", "Alameda", c.getName());
    }

    // Just return an empty model and don't throw errors.
    public void testWithInvalidParameters() throws Exception {
        getRequest().setParameter("state", "XX");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        Map model = mAndV.getModel();
        assertNull(model.get(CompareFooterModuleController.MODEL_CITIES));

        getRequest().setParameter("state", "AK");
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        model = mAndV.getModel();
        assertNotNull(model.get(CompareFooterModuleController.MODEL_CITIES));
        assertNull(model.get(CompareFooterModuleController.MODEL_CITY));
        
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("city", "xxxxxxx");
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        model = mAndV.getModel();
        assertNotNull(model.get(CompareFooterModuleController.MODEL_CITIES));
        assertNull(model.get(CompareFooterModuleController.MODEL_CITY));
    }
}
