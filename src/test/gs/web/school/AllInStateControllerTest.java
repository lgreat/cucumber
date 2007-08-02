package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.search.GSAnalyzer;
import gs.data.search.Indexer;
import gs.data.search.Searcher;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.Hits;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AllInStateControllerTest extends BaseControllerTestCase {

    private AllInStateController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (AllInStateController)getApplicationContext().
                getBean(AllInStateController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");

        request.setPathInfo("");
        ModelAndView mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("status/error", mAndView.getViewName());

        request.setPathInfo("/Foo/Bar");
        mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("status/error", mAndView.getViewName());

        request.setPathInfo("/California/CA");
        mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("school/allInState", mAndView.getViewName());
        assertEquals("Type should be 'school'", "school", 
                (String)mAndView.getModel().get(AllInStateController.MODEL_TYPE));

        request.setPathInfo("/cities/Florida/FL");
        mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("school/allInState", mAndView.getViewName());
        assertEquals("Type should be 'city'", "city",
                (String)mAndView.getModel().get(AllInStateController.MODEL_TYPE));
        assertEquals("State should be Florida", State.FL,
                (State)mAndView.getModel().get(AllInStateController.MODEL_STATE));

        request.setPathInfo("/districts/Alabama/AL");
        mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("school/allInState", mAndView.getViewName());
        assertEquals("Type should be 'district'", "district",
                (String)mAndView.getModel().get(AllInStateController.MODEL_TYPE));
        assertEquals("State should be AL", State.AL,
                (State)mAndView.getModel().get(AllInStateController.MODEL_STATE));
    }

    /*
    public void testPagingLinks() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setPathInfo("/California/CA");
        ModelAndView mAndView = _controller.handleRequest(request, getResponse());
        String links = (String)mAndView.getModel().get("pageLinks");
        System.out.println ("links:\n" + links);
    }
    */

    public void testGetAlphaGroups() throws Exception {
        QueryParser parser = new QueryParser("text", new GSAnalyzer());
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query query = parser.parse("type:school AND state:ak");

        Searcher _searcher = (Searcher)getApplicationContext().getBean(Searcher.BEAN_ID);
        Hits hits = _searcher.search(query,
                new Sort(Indexer.SORTABLE_NAME), null, null);
        List<List> alphaGroups = _controller.getAlphaGroups("school", hits);
        assertEquals("There should be 25 alpha groups in AK", 25, alphaGroups.size());
    }

    public void testBuildPageLink() throws Exception {
        String link = _controller.buildPageLink(AllInStateController.DISTRICTS_TYPE,
                State.CA, 1, 1, "A-B");
        assertEquals("<span class=\"pageLink\">A-B</span>\n", link);

        link = _controller.buildPageLink(AllInStateController.DISTRICTS_TYPE,
                State.CA, 1, 2, "A-B");
        assertEquals("<span class=\"pageLink\"><a href=\"/schools/districts/California/CA\">A-B</a></span>\n", link);

        link = _controller.buildPageLink(AllInStateController.SCHOOLS_TYPE,
                State.AK, 3, 7, "Fr-Fz");
        assertEquals("<span class=\"pageLink\"><a href=\"/schools/Alaska/AK/3\">Fr-Fz</a></span>\n", link);
    }

    public void testGetSpan() throws Exception {
        assertEquals("A null list should make a blank span", "", _controller.getSpan(null, 0));

        List<Map> list = new ArrayList<Map>();
        assertEquals("An empty list should make a blank span", "", _controller.getSpan(list, 0));

        list.add(new HashMap() {{ put("name", "albert"); }});
        list.add(new HashMap() {{ put("name", "Anderson"); }});
        assertEquals("A", _controller.getSpan(list, 1));

        list.add(new HashMap() {{ put("name", "Belle"); }});
        assertEquals("A-B", _controller.getSpan(list, 1));

        assertEquals("A-B", _controller.getSpan(list, 7));
        assertEquals("AL-BE", _controller.getSpan(list, 2));
    }

    public void testBuildTitle() throws Exception {
        String title = _controller.buildTitle(AllInStateController.DISTRICTS_TYPE,
                State.CA, "Aa-Ar");
        assertEquals("All school districts in California, CA: Aa-Ar", title);
    }

    public void testLoadModel() throws Exception {
        Map model = new HashMap();
        _controller.loadModel("school", model, State.AK, 1, 50);
        List list = (List)model.get(AllInStateController.MODEL_LIST);
        assertEquals(50, list.size());
        String pageLinks = (String)model.get(AllInStateController.MODEL_LINKS);

        model = new HashMap();
        _controller.loadModel("city", model, State.AK, 1, 10);
        list = (List)model.get(AllInStateController.MODEL_LIST);
        pageLinks = (String)model.get(AllInStateController.MODEL_LINKS);
    }

    public void testGetStateFromPath() throws Exception {
        assertEquals(State.CA, _controller.getStateFromPath("/California/CA"));
        assertNull("Lower case state abrev is not allowed",  _controller.getStateFromPath("/California/ca"));
    }

    public void testGetPageFromPath() throws Exception {
        assertEquals("Empty path == page 1", 1, _controller.getPageFromPath(""));
        assertEquals("Unrecognized path should return page 1", 1, _controller.getPageFromPath("foobar"));
        assertEquals("Path with no page == page 1", 1, _controller.getPageFromPath("/California/CA"));
        assertEquals("Path with page 0 == page 1", 1, _controller.getPageFromPath("/California/CA/0"));
        assertEquals(12, _controller.getPageFromPath("12"));
        assertEquals(32, _controller.getPageFromPath("/California/CA/32"));
        assertEquals(33, _controller.getPageFromPath("/California/CA/33/"));
    }
}
