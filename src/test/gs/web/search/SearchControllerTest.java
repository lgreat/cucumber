package gs.web.search;

import gs.data.school.district.District;
import gs.data.search.*;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
import gs.web.util.ListModel;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * @author Chris Kimm <chriskimm@greatschools.net>
 */
public class SearchControllerTest extends BaseControllerTestCase {

    private SearchController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp () throws Exception {
        super.setUp ();

        _controller = (SearchController) getApplicationContext().getBean(SearchController.BEAN_ID);

        Indexer indexer = (Indexer) getApplicationContext().getBean(Indexer.BEAN_ID);

        Directory testDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(testDir, new GSAnalyzer(), true);
        indexer.indexCities(State.AK, writer);
        indexer.indexCities(State.CA, writer);
        indexer.indexCities(State.NY, writer);
        indexer.indexDistricts(getDistricts(), writer);

        Searcher searcher = new Searcher(new IndexDir(testDir, new RAMDirectory()));
        _controller.setSearcher(searcher);
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

    public void testFindCities() throws IOException {
        Hits hits = _controller.searchForCities("Anchorage", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Anchorage, AK", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Anchorage, Alaska", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Flush*", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Flushing, New York", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Alameda", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Alameda, CA", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Alameda, California", State.AK);
        assertTrue(hits.length() > 0);

        // We want the results for the current state to show up first.
        hits = _controller.searchForCities("Springs", State.AK);
        assertTrue(hits.length() >= 3);
        Document d = hits.doc(0);
        assertEquals("ak", d.get("state")); // Manley
        d = hits.doc(1);
        assertEquals("ak", d.get("state")); // Tenekee
        d = hits.doc(2);
        assertEquals("ca", d.get("state")); // CA city
        hits = _controller.searchForCities("Springs", State.CA); // and now from CA
        assertTrue(hits.length() >= 3);
        d = hits.doc(0);
        assertEquals("ca", d.get("state"));
        d = hits.doc(1);
        assertEquals("ak", d.get("state"));
        d = hits.doc(2);
        assertEquals("ak", d.get("state"));
    }

}
