package gs.web.state;

import gs.web.BaseControllerTestCase;
import gs.web.util.UrlUtil;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
     * Test all pathways
     *
     * @throws Exception
     */
    public void testPathways() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        StatePathwayController controller = new StatePathwayController();

        // Set up the resourec bundle
        ResourceBundleMessageSource msgs = new ResourceBundleMessageSource();
        controller.setMessageSource(msgs);

        String url = null;
        ModelAndView modelAndView = null;
        String searchParam = "";
        String state = "";

        //get the pathways map: key is the pathway name, value is the url to go to
        Map pathways = (Map) getApplicationContext().getBean("pathwaysMap");
        controller.setPathways(pathways);
        Set keys = pathways.keySet();
        Iterator reUseIter = keys.iterator();
        Iterator iter = reUseIter;

        //no state param, no search param
        while (iter.hasNext()) {
            String key = (String) iter.next();
            url = (String) pathways.get(key);

            request.addParameter("p", key);
            modelAndView = controller.handleRequestInternal(request, response);
            assertEquals(buildTestUrl(url, searchParam, state), modelAndView.getModel().get("url"));
        }

        //bogus pathway
        request.addParameter("p", "boguspathway");
        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get("default");
        assertEquals(buildTestUrl(url, searchParam, state), modelAndView.getModel().get("url"));

        //no pathway
        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get("default");
        assertEquals(buildTestUrl(url, searchParam, state), modelAndView.getModel().get("url"));

        //no state param, search param
        searchParam = "San Francisco";
        iter = keys.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            url = (String) pathways.get(key);

            request.addParameter("p", key);
            request.addParameter("q", searchParam);
            modelAndView = controller.handleRequestInternal(request, response);
            assertEquals(buildTestUrl(url, searchParam, state), modelAndView.getModel().get("url"));
        }

        //empty state param, search param
        searchParam = "San Francisco";
        iter = keys.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            url = (String) pathways.get(key);

            request.addParameter("p", key);
            request.addParameter("q", searchParam);
            request.addParameter("state", state);
            modelAndView = controller.handleRequestInternal(request, response);
            assertEquals(buildTestUrl(url, searchParam, state), modelAndView.getModel().get("url"));
        }

        //state param, query param
        searchParam = "San Francisco";
        state = "CA";
        iter = keys.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            url = (String) pathways.get(key);

            request.addParameter("p", key);
            request.addParameter("q", searchParam);
            request.addParameter("state", state);

            modelAndView = controller.handleRequestInternal(request, response);
            assertNull(modelAndView.getModel().get("url"));
            RedirectView view = null;
            try {
                view = (RedirectView) modelAndView.getView();
            } catch (ClassCastException e) {
                fail("We expected a redirect view but received something else!");
            }
            assertEquals(view.getUrl(), buildTestUrl(url, searchParam, state));
        }
    }

    private String buildTestUrl(final String url, String searchParam, String state) {
        UrlUtil urlUtil = new UrlUtil();

        String retUrl = url;

        if (searchParam != "") {
            searchParam = searchParam.replaceAll(" ", "+");
            retUrl += "?q=" + searchParam + "&state=";
        } else {
            if (urlUtil.smellsLikePerl(url)) {
                retUrl += "/";
            } else {
                retUrl += "?state=";
            }
        }

        if (state != "") {
            retUrl += state;
        }

        return retUrl;
    }
}
