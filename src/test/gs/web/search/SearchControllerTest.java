package gs.web.search;

import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
import gs.web.BaseControllerTestCase;
import gs.web.util.ListModel;
import gs.data.search.Indexer;
import gs.data.search.GSAnalyzer;
import gs.data.search.SearchCommand;
import gs.data.state.State;
import gs.data.school.district.District;
import gs.data.util.Address;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriter;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Chris Kimm <chriskimm@greatschools.net>
 */
public class SearchControllerTest extends BaseControllerTestCase {

    private SearchController _controller;
    private Directory _testDir;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp () throws Exception {
        super.setUp ();
        /*
        sc = new SearchController();
        IndexDir indexDir = new IndexDir(new RAMDirectory(), new RAMDirectory());
        Searcher searcher = new Searcher(indexDir);
        SpellCheckSearcher scs = new SpellCheckSearcher(searcher);
        sc.setSpellCheckSearcher(scs);
        */
        _controller = (SearchController) getApplicationContext().getBean(SearchController.BEAN_ID);

        Indexer indexer = (Indexer) getApplicationContext().getBean(Indexer.BEAN_ID);
        _testDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(_testDir, new GSAnalyzer(), true);
        indexer.indexCities(State.AK, writer);
        indexer.indexDistricts(getDistricts(), writer);
    }

    public void testQueryOnly () throws Exception {

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("q", "San Bruno");
        //ModelAndView mv = sc.handleRequestInternal(request, response);
        //Map model = mv.getModel();
    }

    // This might be better off in in the gs.data.search tests.
    public void testSuggestion() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("q", "Alamefa");
        //ModelAndView mv = sc.handleRequestInternal(request, response);
        //Map model = mv.getModel();
        //String suggestion = (String)model.get("suggestedQuery");
        //assertNotNull(suggestion); todo

    }

    public void testAllParams1() throws Exception {

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("p", "1");
        request.setParameter("c", "schools");
        request.setParameter("q", "Alameda");
        request.setParameter("s", "1");
        //ModelAndView mv = sc.handleRequestInternal(request, response);
        //Map model = mv.getModel();
    }



    public void testCities() throws Exception {

        final GsMockHttpServletRequest request = getRequest();
        request.setParameter("q", "Alameda");
        request.setParameter("state", "CA");
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        SearchCommand command = new SearchCommand();
        command.setQ("Alameda");
        command.setState(State.CA);
        BindException errors = new BindException(command, null);
        ModelAndView mv = _controller.processFormSubmission(request, (HttpServletResponse) getResponse(), command, errors);

        ListModel cities = (ListModel) mv.getModel().get(SearchController.MODEL_CITIES);
        assertNotNull(cities);
    }

    public void testNoResultsQuery() throws Exception {

        final GsMockHttpServletRequest request = getRequest();
        request.setParameter("q", "xxx");
        request.setParameter("state", "CA");
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        SearchCommand command = new SearchCommand();
        command.setQ("xxx");
        command.setState(State.CA);
        BindException errors = new BindException(command, null);
        ModelAndView mv = _controller.processFormSubmission(request, (HttpServletResponse) getResponse(), command, errors);

        ListModel cities = (ListModel) mv.getModel().get(SearchController.MODEL_CITIES);
        assertNull(cities);
    }

    private List getDistricts() {
        List districts = new ArrayList();

        districts.add(createDistrict("District A"));

        return districts;
    }

    private District createDistrict(String name) {
        District district = new District();
        district.setName(name);
        Address address = new Address();
        address.setStreet("1234 Foo Lane");
        address.setCity("Fooville");
        address.setState(State.CA);
        address.setZip("12345");
        district.setPhysicalAddress(address);
        return district;
    }

}
