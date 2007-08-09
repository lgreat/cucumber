package gs.web.school.review;

import gs.data.school.School;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ThankYouControllerTest extends BaseControllerTestCase {
    ThankYouController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (ThankYouController) getApplicationContext().getBean(ThankYouController.BEAN_ID);
        getRequest().setParameter("id", "1");
        getRequest().setParameter("state", "CA");
        getRequest().setMethod("GET");
    }

    public void testModel() throws Exception {                
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());

        assertEquals(new Integer(1), ((School) mAndV.getModel().get(ThankYouController.MODEL_SCHOOL)).getId());
        assertTrue((Boolean) mAndV.getModel().get(ThankYouController.MODEL_HAS_REVIEW));
    }
}
