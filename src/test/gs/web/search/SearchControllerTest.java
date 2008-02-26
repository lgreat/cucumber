package gs.web.search;

import gs.data.content.Article;
import gs.data.school.ISchoolDao;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;
import gs.data.school.district.IDistrictDao;
import gs.data.search.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Andrew Peterson <apeterson@greatschools.net>
 */
public class SearchControllerTest extends BaseControllerTestCase {

    private SearchController _controller;
    private SessionContextUtil _sessionContextUtil;
    private ISchoolDao _schoolDao;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = (SearchController) getApplicationContext().getBean(SearchController.BEAN_ID);

        _schoolDao = createMock(ISchoolDao.class);

        _controller.setSchoolDao(_schoolDao);
        IDistrictDao districtDao = (IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID);

        Indexer indexer = (Indexer) getApplicationContext().getBean(Indexer.BEAN_ID);

        Directory directory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(directory, new GSAnalyzer(), true);
        indexer.indexCities(State.AK, writer);
        indexer.indexCities(State.CA, writer);
        indexer.indexCities(State.NY, writer);
        indexer.indexDistricts(districtDao.getDistricts(State.AK, true), writer);

        List<Article> articles = new ArrayList<Article>();
        Article article;
        article = new Article();
        article.setTitle("What Your Child Should Be Learning: Kindergarten Language Arts");
        article.setAbstract("kindergarten");
        article.setActive(true);
        article.setArticleText("Children learn to read at different ages, but most discover the connection between letters and sounds in kindergarten.");
        article.setStatesAsString("ak^ca");
        article.setId(377);
        articles.add(article);
        article = new Article();
        article.setTitle("How to not get picked by search results");
        article.setAbstract("search results");
        article.setActive(true);
        article.setArticleText("Try not to use any of the words that the searcher is looking for.");
        article.setStatesAsString("ak^ca");
        article.setId(191);
        articles.add(article);
//        articles.add(_articleDao.getArticleFromId(246));
//        articles.add(_articleDao.getArticleFromId(377));
//        articles.add(_articleDao.getArticleFromId(355));
//        articles.add(_articleDao.getArticleFromId(191));
        indexer.indexArticles(articles, writer);
        writer.close();

        Searcher searcher = new Searcher(new IndexDir(directory, new RAMDirectory()));
        _controller.setSearcher(searcher);

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);

    }


    public void testCities() throws Exception {

        final GsMockHttpServletRequest request = getRequest();
        request.setParameter("q", "Alameda");
        request.setParameter("state", "CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        SearchCommand command = new SearchCommand();
        command.setQ("Alameda");
        command.setState(State.CA);
        BindException errors = new BindException(command, null);
        ModelAndView mv = _controller.processFormSubmission(request, getResponse(), command, errors);

        AnchorListModel cities = (AnchorListModel) mv.getModel().get(SearchController.MODEL_CITIES);
        assertNotNull(cities);
    }

    public void testNoResultsQuery() throws Exception {

        final GsMockHttpServletRequest request = getRequest();
        request.setParameter("q", "xxx");
        request.setParameter("state", "CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        SearchCommand command = new SearchCommand();
        command.setQ("xxx");
        command.setState(State.CA);
        BindException errors = new BindException(command, null);
        ModelAndView mv = _controller.processFormSubmission(request, getResponse(), command, errors);

        AnchorListModel cities = (AnchorListModel) mv.getModel().get(SearchController.MODEL_CITIES);
        assertNull(cities);
    }


    public void testDistrictsRollup() {
        final GsMockHttpServletRequest request = getRequest();
        request.setParameter("q", "xxx");
        request.setParameter("state", "CA");
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
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setQ("kindergarten");
        searchCommand.setState(State.AK);
        searchCommand.setType("topic");

        replay(_schoolDao);

        Map map = _controller.createModel(request, searchCommand, sessionContext, false);

        verify(_schoolDao);
        List results = (List) map.get(SearchController.MODEL_RESULTS);
        assertNotNull(results);
        assertEquals(1, results.size());
        int kindergartenHits = results.size();

        searchCommand.setQ("kindergarden");

        reset(_schoolDao);
        replay(_schoolDao);

        map = _controller.createModel(request, searchCommand, sessionContext, false);
        verify(_schoolDao);
        results = (List) map.get(SearchController.MODEL_RESULTS);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(kindergartenHits, results.size());
    }

    /**
     * Regression test for GS-2028
     *
     * @throws IOException
     */
    public void testPrivateRollup() throws IOException {
        final GsMockHttpServletRequest request = getRequest();
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setQ("private anchorage");
        searchCommand.setState(State.AK);

        expect(_schoolDao.countSchools(State.AK, SchoolType.PRIVATE, null, "Anchorage")).andReturn(1);
        replay(_schoolDao);

        Map map = _controller.createModel(request, searchCommand, sessionContext, false);

        verify(_schoolDao);

        assertNotNull(map);
        AnchorListModel filteredAnchorListModel = (AnchorListModel) map.get(SearchController.MODEL_FILTERED_CITIES);
        assertNotNull(filteredAnchorListModel);
        assertNotNull(filteredAnchorListModel.getResults());
        assertTrue(filteredAnchorListModel.getResults().size() >= 1);
        Anchor anchorage = (Anchor) filteredAnchorListModel.getResults().get(0);
        assertEquals("/schools.page?city=Anchorage&st=private&state=AK", anchorage.getHref());
    }

    /**
     * Regression test for GS-1935
     *
     * @throws IOException
     */
    public void testShouldntCrashOnMiddleSchoolQuery() throws IOException {
        final GsMockHttpServletRequest request = getRequest();
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        final SessionContext sessionContext = _sessionContextUtil.guaranteeSessionContext(request);


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setQ("middle anchorage");
        searchCommand.setState(State.AK);

        expect(_schoolDao.countSchools(State.AK, null, LevelCode.MIDDLE, "Anchorage")).andReturn(1);
        replay(_schoolDao);

        Map map = _controller.createModel(request, searchCommand, sessionContext, false);

        verify(_schoolDao);

        assertNotNull(map);
        AnchorListModel filteredAnchorListModel = (AnchorListModel) map.get(SearchController.MODEL_FILTERED_CITIES);
        assertNotNull(filteredAnchorListModel);
        assertNotNull(filteredAnchorListModel.getResults());
        assertTrue(filteredAnchorListModel.getResults().size() >= 1);
        Anchor anchorage = (Anchor) filteredAnchorListModel.getResults().get(0);
        assertEquals("/schools.page?city=Anchorage&lc=m&state=AK", anchorage.getHref());
    }

    public void testSendEmptyTopicQueriesToAllArticles() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setParameter("state", "WY");
        request.setParameter("type", "topic");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequest(request, getResponse());

        assertTrue(mav.getView() instanceof RedirectView);
        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/content/allArticles.page", view.getUrl());

    }

    public void testSendEmptyTopicByConstriantQueriesToAllArticles() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setParameter("state", "WY");
        request.setParameter("c", "topic"); // pass in the type via "c" for backward compatibility
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequest(request, getResponse());

        assertTrue(mav.getView() instanceof RedirectView);
        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/content/allArticles.page", view.getUrl());

    }

    public void testSendEmptySchoolQueriesToStateHome() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setParameter("state", "WY");
        request.setParameter("type", "school");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequest(request, getResponse());

        assertTrue(mav.getView() instanceof RedirectView);
        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/modperl/go/WY", view.getUrl());
    }

    public void testSendEmptyGeneralQueriesToStateHome() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setParameter("state", "WY");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView mav = _controller.handleRequest(request, getResponse());

        assertTrue(mav.getView() instanceof RedirectView);
        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/modperl/go/WY", view.getUrl());
    }
}
