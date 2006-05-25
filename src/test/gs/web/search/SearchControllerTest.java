package gs.web.search;

import gs.data.school.district.IDistrictDao;
import gs.data.school.ISchoolDao;
import gs.data.search.*;
import gs.data.state.State;
import gs.data.content.IArticleDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContext;
import gs.web.SessionContextUtil;
import gs.web.util.ListModel;
import gs.web.util.Anchor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Andrew Peterson <apeterson@greatschools.net>
 */
public class SearchControllerTest extends BaseControllerTestCase {

    private SearchController _controller;
    private SessionContextUtil _sessionContextUtil;
    private IDistrictDao _districtDao;
    private IArticleDao _articleDao;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = (SearchController) getApplicationContext().getBean(SearchController.BEAN_ID);

        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _districtDao = (IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID);
        _articleDao = (IArticleDao) getApplicationContext().getBean(IArticleDao.BEAN_ID);

        Indexer indexer = (Indexer) getApplicationContext().getBean(Indexer.BEAN_ID);

        Directory directory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(directory, new GSAnalyzer(), true);
        indexer.indexCities(State.AK, writer);
        indexer.indexCities(State.CA, writer);
        indexer.indexCities(State.NY, writer);
        indexer.indexDistricts(_districtDao.getDistricts(State.AK, true), writer);
        List articles = new ArrayList();
        articles.add(_articleDao.getArticleFromId(new Integer(246)));
        articles.add(_articleDao.getArticleFromId(new Integer(377)));
        articles.add(_articleDao.getArticleFromId(new Integer(355)));
        articles.add(_articleDao.getArticleFromId(new Integer(191)));
        indexer.indexArticles(articles, writer);
        writer.close();

        Searcher searcher = new Searcher(new IndexDir(directory, new RAMDirectory()));
        _controller.setSearcher(searcher);
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
        ModelAndView mv = _controller.processFormSubmission(request, getResponse(), command, errors);

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


    public void testFindCities() throws IOException {
        Hits hits = _controller.searchForCities("Anchorage", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Anchorage, AK", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Anchorage, Alaska", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Flush*", State.AK);
//        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Flushing, New York", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Alameda", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Alameda, CA", State.AK);
        assertTrue(hits.length() > 0);

        hits = _controller.searchForCities("Alameda, California", State.AK);
        assertTrue(hits.length() > 0);

        // We want the results for the current state to show up first.
        hits = _controller.searchForCities("Springs", null);
        assertTrue(hits.length() >= 3);

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


    public void testDistrictsRollup() {
        final GsMockHttpServletRequest request = getRequest();
        request.setParameter("q", "xxx");
        request.setParameter("state", "CA");
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);

        BooleanQuery baseQuery = _controller.createBaseQuery(sessionContext, State.AK, "Anchorage");
        Hits hits = _controller.searchForDistricts(baseQuery);
        assertEquals(3, hits.length());

        baseQuery = _controller.createBaseQuery(sessionContext, State.AK, "Anchorage middle schools");
        hits = _controller.searchForDistricts(baseQuery);
        assertEquals(3, hits.length());
    }

    public void testKindergarden() throws IOException {
        final GsMockHttpServletRequest request = getRequest();
        _sessionContextUtil = (SessionContextUtil) getApplicationContext(). getBean(SessionContextUtil.BEAN_ID);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setQ("kindergarten");
        searchCommand.setState(State.AK);
        searchCommand.setType("topic");
        Map map = _controller.createModel(request, searchCommand, sessionContext, false);
        List results = (List) map.get(SearchController.MODEL_RESULTS);
        assertTrue(results.size() >= 3);
        int kindergartenHits = results.size();

        searchCommand.setQ("kindergarden");
        map = _controller.createModel(request, searchCommand, sessionContext, false);
        results = (List) map.get(SearchController.MODEL_RESULTS);
        assertTrue(results.size() >= 3);
        assertEquals(kindergartenHits, results.size());
    }

    /**
     * Regression test for GS-2028
     * @throws IOException
     */
    public void testPrivateRollup() throws IOException {
        final GsMockHttpServletRequest request = getRequest();
        _sessionContextUtil = (SessionContextUtil) getApplicationContext(). getBean(SessionContextUtil.BEAN_ID);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setQ("private anchorage");
        searchCommand.setState(State.AK);

        Map map = _controller.createModel(request, searchCommand, sessionContext, false);

        assertNotNull(map);
        ListModel filteredListModel = (ListModel) map.get(SearchController.MODEL_FILTERED_CITIES);
        assertNotNull(filteredListModel);
        assertNotNull(filteredListModel.getResults());
        assertTrue(filteredListModel.getResults().size() >= 1);
        Anchor anchorage = (Anchor) filteredListModel.getResults().get(0);
        assertEquals("/schools.page?city=Anchorage&st=private&state=AK", anchorage.getHref());
    }
    /**
     * Regression test for GS-1935
     * @throws IOException
     */
    public void testShouldntCrashOnMiddleSchoolQuery() throws IOException {
        final GsMockHttpServletRequest request = getRequest();
        _sessionContextUtil = (SessionContextUtil) getApplicationContext(). getBean(SessionContextUtil.BEAN_ID);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setQ("middle anchorage");
        searchCommand.setState(State.AK);

        Map map = _controller.createModel(request, searchCommand, sessionContext, false);

        assertNotNull(map);
        ListModel filteredListModel = (ListModel) map.get(SearchController.MODEL_FILTERED_CITIES);
        assertNotNull(filteredListModel);
        assertNotNull(filteredListModel.getResults());
        assertTrue(filteredListModel.getResults().size() >= 1);
        Anchor anchorage = (Anchor) filteredListModel.getResults().get(0);
        assertEquals("/schools.page?city=Anchorage&lc=m&state=AK", anchorage.getHref());
    }
}
