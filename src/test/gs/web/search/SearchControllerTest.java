package gs.web.search;

import gs.web.BaseTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

/**
 * @author Chris Kimm <chriskimm@greatschools.net>
 */
public class SearchControllerTest extends BaseTestCase {

    private SearchController sc;

    protected void setUp () throws Exception {
        super.setUp ();
        sc = (SearchController)_sApplicationContext.getBean(SearchController.BEAN_ID);
    }

    public void testQueryOnly () throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("q", "San Bruno");
        ModelAndView mv = sc.handleRequestInternal(request, response);
        Map model = mv.getModel();
    }

    // This might be better off in in the gs.data.search tests.
    public void testSuggestion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("q", "Alamefa");
        ModelAndView mv = sc.handleRequestInternal(request, response);
        Map model = mv.getModel();
        String suggestion = (String)model.get("suggestedQuery");
        //assertNotNull(suggestion); todo

    }

    public void testAllParams1() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("p", "1");
        request.addParameter("c", "schools");
        request.addParameter("q", "Alameda");
        request.addParameter("s", "1");
        ModelAndView mv = sc.handleRequestInternal(request, response);
        Map model = mv.getModel();
    }
}
