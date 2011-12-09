package gs.web.search;


import gs.data.search.ContentSearchResult;
import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.SearchResultsPage;
import gs.web.BaseControllerTestCase;
import gs.web.pagination.RequestedPage;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

public class ContentSearchControllerTest  extends BaseControllerTestCase {

    ContentSearchController _controller;
    GsSolrSearcher _searcher;

    private RequestedPage secondPage() {
        RequestedPage requestedPage = new RequestedPage(25, 2, 25);
        return requestedPage;
    }
    private RequestedPage firstPage() {
        RequestedPage requestedPage = new RequestedPage(0, 1, 25);
        return requestedPage;
    }

    private List<ContentSearchController.ContentSearchType> articlesOnlySearchType() {
        List<ContentSearchController.ContentSearchType> contentSearchTypeList = new ArrayList<ContentSearchController.ContentSearchType>();
        contentSearchTypeList.add(ContentSearchController.ContentSearchType.ARTICLES);
        return contentSearchTypeList;
    }

    @Before
    public void setUp() {
        _controller = new ContentSearchController();
        _searcher = createMock(GsSolrSearcher.class);

        _controller.setGsSolrSearcher(_searcher);
    }

    public SearchResultsPage<ContentSearchResult> bogusResults() {
        List<ContentSearchResult> contentSearchResults = new ArrayList<ContentSearchResult>();
        SearchResultsPage<ContentSearchResult> resultsPage = new SearchResultsPage<ContentSearchResult>(0, contentSearchResults);
        return resultsPage;
    }

    @Test
    public void testControllerSearchesWithCorrectPaginationValues1() throws Exception {
        String searchString = "kids";
        RequestedPage requestedPage = firstPage();

        SolrQuery solrQuery = new SolrQuery();
        // expect query asking for results starting at offset 25 (1st result on 2nd page)
        solrQuery.setStart(0);
        solrQuery.setRows(25);
        GsSolrQuery expectedQuery = new GsSolrQuery(solrQuery);

        expect(_searcher.search(eqGsSolrQuery(expectedQuery), eq(ContentSearchResult.class))).andReturn(bogusResults());
        replay(_searcher);

        ContentSearchController.ContentSearchResultsInfo resultsInfo =
                _controller.getContentSearchTypeResults(searchString, articlesOnlySearchType(), requestedPage);

        verify(_searcher);
    }

    @Test
    public void testControllerSearchesWithCorrectPaginationValues2() throws Exception {
        String searchString = "kids";
        RequestedPage requestedPage = secondPage();

        SolrQuery solrQuery = new SolrQuery();
        // expect query asking for results starting at offset 25 (1st result on 2nd page)
        solrQuery.setStart(25);
        solrQuery.setRows(25);
        GsSolrQuery expectedQuery = new GsSolrQuery(solrQuery);

        expect(_searcher.search(eqGsSolrQuery(expectedQuery), eq(ContentSearchResult.class))).andReturn(bogusResults());
        replay(_searcher);

        ContentSearchController.ContentSearchResultsInfo resultsInfo =
                _controller.getContentSearchTypeResults(searchString, articlesOnlySearchType(), requestedPage);

        verify(_searcher);
    }

    public GsSolrQuery eqGsSolrQuery(GsSolrQuery GsSolrQuery) {
        reportMatcher(new GsSolrQueryPaginationMatcher(GsSolrQuery));
        return null;
    }
    
    private class GsSolrQueryPaginationMatcher implements IArgumentMatcher {
        GsSolrQuery _expected;
        GsSolrQueryPaginationMatcher(GsSolrQuery expected) {
            _expected = expected;
        }
        public boolean matches(Object oActual) {
            if (!(oActual instanceof GsSolrQuery)) {
                return false;
            }
            GsSolrQuery actual = (GsSolrQuery) oActual;
            if ((actual.getSolrQuery().getStart().equals(_expected.getSolrQuery().getStart()))
                && actual.getSolrQuery().getRows().equals(_expected.getSolrQuery().getRows())) {
                return true;
            }
            return false;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("start:").append(_expected.getSolrQuery().getStart());
            buffer.append("rows:").append(_expected.getSolrQuery().getRows());
        }
    }
}
