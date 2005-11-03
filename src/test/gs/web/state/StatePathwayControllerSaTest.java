package gs.web.state;

import gs.web.BaseControllerTestCase;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller tests for Davids bridge page
 *
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class StatePathwayControllerSaTest extends BaseControllerTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the pathway 1 choose page for url modperl/go
     *
     * @throws Exception
     */
    public void testPathwayChoose() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        StatePathwayController controller = new StatePathwayController();

        // Set up the resourec bundle
        ResourceBundleMessageSource msgs = new ResourceBundleMessageSource();
        controller.setMessageSource(msgs);

        // Set up the pathway map
        Map pathmap = new HashMap();
        pathmap.put("pathway1", "/modperl/go");
        controller.setPathways(pathmap);

        // Make a request without a state set
        request.addParameter("p", "pathway1");
        ModelAndView modelAndView = controller.handleRequestInternal(request, response);
        assertEquals("/modperl/go/", modelAndView.getModel().get("url"));

        // Verify if we set the state if does a redirect
        request.addParameter("state", "CA");
        modelAndView = controller.handleRequestInternal(request, response);
        assertNull(modelAndView.getModel().get("url"));
        RedirectView view = null;
        try {
            view = (RedirectView) modelAndView.getView();
        } catch (ClassCastException e) {
            fail("We expected a redirect view but received something else!");
        }
        assertEquals("/modperl/go/CA", view.getUrl());
    }

    /**
     * Test the pathway 4 search page with passing parameters through
     *
     * @throws Exception
     */
    public void testPathwaySearch() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        StatePathwayController controller = new StatePathwayController();

        // Set up the resourec bundle
        ResourceBundleMessageSource msgs = new ResourceBundleMessageSource();
        controller.setMessageSource(msgs);

        // Set up the pathway map
        Map pathmap = new HashMap();
        pathmap.put("pathway4", "/search/search.page");
        controller.setPathways(pathmap);

        // Make a request without a state set
        request.addParameter("p", "pathway4");
        request.addParameter("q", "foo bar");
        ModelAndView modelAndView = controller.handleRequestInternal(request, response);
        assertEquals("/search/search.page?q=foo+bar&state=", modelAndView.getModel().get("url"));

        // Verify if we set the state if does a redirect
        request.addParameter("state", "CA");
        modelAndView = controller.handleRequestInternal(request, response);
        assertNull(modelAndView.getModel().get("url"));
        RedirectView view = null;
        try {
            view = (RedirectView) modelAndView.getView();
        } catch (ClassCastException e) {
            fail("We expected a redirect view but received something else!");
        }
        assertEquals("/search/search.page?q=foo+bar&state=CA", view.getUrl());
    }
}
