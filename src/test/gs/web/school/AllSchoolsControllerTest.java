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

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AllSchoolsControllerTest extends BaseControllerTestCase {

    private AllSchoolsController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (AllSchoolsController)getApplicationContext().
                getBean(AllSchoolsController.BEAN_ID);
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
        assertEquals("school/allSchools", mAndView.getViewName());
        assertEquals("Type should be 'school'", "school", 
                (String)mAndView.getModel().get(AllSchoolsController.MODEL_TYPE));

        request.setPathInfo("/cities/Florida/FL");
        mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("school/allSchools", mAndView.getViewName());
        assertEquals("Type should be 'city'", "city",
                (String)mAndView.getModel().get(AllSchoolsController.MODEL_TYPE));
        assertEquals("State should be Florida", State.FL,
                (State)mAndView.getModel().get(AllSchoolsController.MODEL_STATE));

        request.setPathInfo("/districts/Alabama/AL");
        mAndView = _controller.handleRequest(request, getResponse());
        assertEquals("school/allSchools", mAndView.getViewName());
        assertEquals("Type should be 'district'", "district",
                (String)mAndView.getModel().get(AllSchoolsController.MODEL_TYPE));
        assertEquals("State should be AL", State.AL,
                (State)mAndView.getModel().get(AllSchoolsController.MODEL_STATE));
    }

    public void testPagingLinks() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setPathInfo("/California/CA");
        ModelAndView mAndView = _controller.handleRequest(request, getResponse());
        String links = (String)mAndView.getModel().get("pageLinks");
    }

    public void testGetAlphaGroups() throws Exception {
        QueryParser parser = new QueryParser("text", new GSAnalyzer());
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query query = parser.parse("type:school AND state:ak");

        Searcher _searcher = (Searcher)getApplicationContext().getBean(Searcher.BEAN_ID);
        Hits hits = _searcher.search(query,
                new Sort(Indexer.SORTABLE_NAME), null, null);
        List alphaGroups = _controller.getAlphaGroups("school", hits);

    }
    public void testBuildPageLinksAndModel() throws Exception {
        Map model = new HashMap();
        _controller.buildPageLinksAndModel("school", model, State.AK, 1, 100);
        List list = (List)model.get(AllSchoolsController.MODEL_LIST);
        String pageLinks = (String)model.get(AllSchoolsController.MODEL_LINKS);

//        System.out.println ("links:\n" + pageLinks);
//        System.out.println ("list:\n" + list);
        
        model = new HashMap();
        _controller.buildPageLinksAndModel("city", model, State.AK, 1, 100);
        list = (List)model.get(AllSchoolsController.MODEL_LIST);
        pageLinks = (String)model.get(AllSchoolsController.MODEL_LINKS);
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
