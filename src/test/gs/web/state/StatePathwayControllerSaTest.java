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
        final String pathway_single_search = "search_single";
        final String pathway_double_search = "search_double";

        // Set up the resourec bundle
        ResourceBundleMessageSource msgs = new ResourceBundleMessageSource();
        controller.setMessageSource(msgs);

        String url = null;
        ModelAndView modelAndView = null;
        String state = "";

        //get the pathways map: key is the pathway name, value is the url to go to
        Map pathways = (Map) getApplicationContext().getBean("pathwaysMap");
        controller.setPathways(pathways);
        Set keys = pathways.keySet();
        Iterator reUseIter = keys.iterator();
        Iterator iter = reUseIter;

        //bogus pathway
        request.addParameter("p", "boguspathway");
        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get("default");
        assertEquals(buildTestUrl(url, state), modelAndView.getModel().get("url"));

        //no pathway
        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get("default");
        assertEquals(buildTestUrl(url, state), modelAndView.getModel().get("url"));

        //no state param
        while (iter.hasNext()) {
            String key = (String) iter.next();
            url = (String) pathways.get(key);
            if (key.equals(pathway_single_search) || key.equals(pathway_double_search)) {
                continue;
            }

            request.addParameter("p", key);

            modelAndView = controller.handleRequestInternal(request, response);
            assertEquals(buildTestUrl(url, state), modelAndView.getModel().get("url"));
        }

        RedirectView view = null;


        //no state param, single search
        iter = keys.iterator();

        request.addParameter("p", pathway_single_search);
        request.addParameter("q", "San Francisco");

        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get(pathway_single_search);
        assertEquals(url+"?state=", modelAndView.getModel().get("url"));
        assertEquals("&q=San+Francisco", modelAndView.getModel().get("extraParams"));

        //no state param, double search
        request.addParameter("p", pathway_double_search);
        request.addParameter("field1", "Lowell");
        request.addParameter("field2", "San Francisco");
        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get(pathway_double_search);
        state = "";
        assertEquals(buildTestUrl(url, state), modelAndView.getModel().get("url"));
        assertEquals("?selector=by_school&field1=Lowell&field2=San+Francisco",
                        modelAndView.getModel().get("extraParams"));

        //state param, no search param
        state = "CA";
        iter = keys.iterator();
        request = new MockHttpServletRequest();

        while (iter.hasNext()) {
            String key = (String) iter.next();

            if (key.equals(pathway_single_search) || key.equals(pathway_double_search)) {
                continue;
            }
            url = (String) pathways.get(key);

            request.addParameter("p", key);
            request.addParameter("state", state);


            modelAndView = controller.handleRequestInternal(request, response);
            assertNull(modelAndView.getModel().get("url"));
            view = null;
            try {
                view = (RedirectView) modelAndView.getView();
            } catch (ClassCastException e) {
                fail("We expected a redirect view but received something else!");
            }
            assertEquals(buildRedirectUrl(url, state, request), view.getUrl());
        }

        //valid state param, double search
        state = "CA";
        request.addParameter("p", pathway_double_search);
        request.addParameter("field1", "Lowell");
        request.addParameter("field2", "San Francisco");
        request.addParameter("state", state);
        modelAndView = controller.handleRequestInternal(request, response);
        url = (String) pathways.get(pathway_double_search);
        view = null;
        try {
            view = (RedirectView) modelAndView.getView();
        } catch (ClassCastException e) {
            fail("We expected a redirect view but received something else!");
        }
        assertEquals(buildRedirectUrl(url, state, request) + "?selector=by_school&field1=Lowell&field2=San+Francisco",
                    view.getUrl());

        //valid state param, single search
        state = "CA";
        request.addParameter("p", pathway_single_search);
        request.addParameter("q", "San Francisco");
        request.addParameter("state", state);
        modelAndView = controller.handleRequestInternal(request, response);
        view = null;
        try {
            view = (RedirectView) modelAndView.getView();
        } catch (ClassCastException e) {
            fail("We expected a redirect view but received something else!");
        }
        url = (String) pathways.get(pathway_single_search);
        assertEquals(buildRedirectUrl(url, state, request) + "&q=San+Francisco", view.getUrl());

    }

    private String buildTestUrl(final String url, String state) {
        UrlUtil urlUtil = new UrlUtil();

        String retUrl = url;

        if (urlUtil.smellsLikePerl(url)) {
            retUrl += "/";
        } else {
            retUrl += "?state=";
        }

        if (state != "") {
            retUrl += state;
        }

        return retUrl;
    }

    private String buildRedirectUrl (final String url, String state, MockHttpServletRequest request) {
        UrlUtil urlUtil = new UrlUtil();

        return urlUtil.buildUrl(buildTestUrl(url, state), request);
    }
}
