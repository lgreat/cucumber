package gs.web.search;

import gs.data.search.Searcher;
import gs.data.search.SolrService;
import gs.data.search.indexers.DistrictIndexer;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;

import java.util.ArrayList;
import java.util.List;

public class DistrictSearchServiceSolrImpl extends BaseLuceneSearchService implements DistrictSearchService {

    Searcher _searcher;

    private SolrService _solrService;

    private Logger _log = Logger.getLogger(DistrictSearchServiceImpl.class);

    public DistrictSearchServiceSolrImpl() {

    }

    public List<? extends IDistrictSearchResult> search(String searchString, State state) throws SchoolSearchService.SearchException {
        return search(searchString, state, 0, 0);
    }

    public List<? extends IDistrictSearchResult> search(String searchString, State state, int offset, int count) throws SchoolSearchService.SearchException {
        List<DistrictSearchResult> resultList = new ArrayList<DistrictSearchResult>();

        QueryResponse response = null;

        try {
            SolrServer server = getSolrService().getReadOnlySolrServer();
            SolrQuery query = buildQuery(searchString, state);

            if (query != null) {
                query.setStart(offset);
                query.setRows(count);
                response = server.query(query);
            }

            if (response != null && response.getResults().size() > 0) {
                resultList = response.getBeans(DistrictSearchResult.class);
            }
        } catch (SolrServerException e) {
            throw new SchoolSearchService.SearchException("Problem accessing search results.", e);
        } catch (Exception e) {
            throw new SchoolSearchService.SearchException("Problem accessing search results.", e);
        }

        return resultList;
    }


    public SolrQuery buildQuery(String searchString, State state) throws ParseException {
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
        query.addFilterQuery(DistrictIndexer.DOCUMENT_TYPE + ":" + DistrictIndexer.DOCUMENT_TYPE_DISTRICT);

        if (searchString != null) {
            query.add(CommonParams.Q, DistrictIndexer.DISTRICT_NAME + ":" + searchString);
        }
        
        if (state != null) {
            query.add(CommonParams.Q, DistrictIndexer.STATE + ":" + searchString);
        }

        return query;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public SolrService getSolrService() {
        return _solrService;
    }

    public void setSolrService(SolrService solrService) {
        _solrService = solrService;
    }
}