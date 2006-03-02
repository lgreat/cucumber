package gs.web.search;

import gs.web.BaseTestCase;
import gs.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Chris Kimm <chriskimm@greatschools.net>
 */
public class SearchControllerTest extends BaseTestCase {

    private SearchController sc;

    protected void setUp () throws Exception {
        super.setUp ();
        /*
        sc = new SearchController();
        IndexDir indexDir = new IndexDir(new RAMDirectory(), new RAMDirectory());
        Searcher searcher = new Searcher(indexDir);
        SpellCheckSearcher scs = new SpellCheckSearcher(searcher);
        sc.setSpellCheckSearcher(scs);
        */
        sc = (SearchController) getApplicationContext().getBean(SearchController.BEAN_ID);
    }

    public void testQueryOnly () throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("q", "San Bruno");
        //ModelAndView mv = sc.handleRequestInternal(request, response);
        //Map model = mv.getModel();
    }

    // This might be better off in in the gs.data.search tests.
    public void testSuggestion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("q", "Alamefa");
        //ModelAndView mv = sc.handleRequestInternal(request, response);
        //Map model = mv.getModel();
        //String suggestion = (String)model.get("suggestedQuery");
        //assertNotNull(suggestion); todo

    }

    public void testAllParams1() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("p", "1");
        request.setParameter("c", "schools");
        request.setParameter("q", "Alameda");
        request.setParameter("s", "1");
        //ModelAndView mv = sc.handleRequestInternal(request, response);
        //Map model = mv.getModel();
    }
}
