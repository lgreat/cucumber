package gs.web.search;

import gs.data.search.Searcher;
import gs.data.search.SolrConnectionManager;
import gs.data.search.indexers.CityIndexer;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;

import java.util.ArrayList;
import java.util.List;

public class CitySearchServiceSolrImpl extends BaseLuceneSearchService implements CitySearchService {

    private Searcher _searcher;

    private SolrConnectionManager _solrConnectionManager;

    private Logger _log = Logger.getLogger(CitySearchServiceImpl.class);

    public CitySearchServiceSolrImpl() {
        //_queryParser = new QueryParser("city", new GSAnalyzer());
    }

    public List<? extends ICitySearchResult> search(String searchString, State state) throws SchoolSearchServiceImpl.SearchException {
        return search(searchString, state, 0, 0);
    }

    public List<? extends CitySearchResult> search(String searchString, State state, int offset, int count) throws SchoolSearchServiceImpl.SearchException {
        List<CitySearchResult> resultList = new ArrayList<CitySearchResult>();

        QueryResponse response = null;

        try {
            SolrServer server = getSolrConnectionManager().getReadOnlySolrServer();
            SolrQuery query = buildQuery(searchString, state);

            if (query != null) {
                query.setStart(offset);
                query.setRows(count);
                response = server.query(query);
            }

            if (response != null && response.getResults().size() > 0) {
                resultList = response.getBeans(CitySearchResult.class);
                //resultList = new CityResultBuilder().build(hits, offset, count);//TODO: find better way to get result builder
            }
        } catch (SolrServerException e) {
            throw new SchoolSearchService.SearchException("Problem accessing search results.", e);
        } catch (Exception e) {
            throw new SchoolSearchService.SearchException("Problem accessing search results.", e);
        }

        return resultList;
    }

    public SolrQuery buildQuery(String searchString, State state) {
        if (StringUtils.isBlank(searchString) && state == null) {
            throw new IllegalArgumentException("Cannot find cities without a searchString or a state");
        }

        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit regardless of field constraints
            }
        }

        SolrQuery query = new SolrQuery();
        query.addFilterQuery(CityIndexer.DOCUMENT_TYPE + ":" + CityIndexer.DOCUMENT_TYPE_CITY);

        if (searchString != null) {
            query.add(CommonParams.Q, CityIndexer.CITY_NAME + ":" + searchString);
        }

        if (state != null) {
            query.add(CommonParams.Q, CityIndexer.STATE + ":" + searchString);
        }

        return query;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public SolrConnectionManager getSolrConnectionManager() {
        return _solrConnectionManager;
    }

    public void setSolrConnectionManager(SolrConnectionManager solrConnectionManager) {
        _solrConnectionManager = solrConnectionManager;
    }
}
