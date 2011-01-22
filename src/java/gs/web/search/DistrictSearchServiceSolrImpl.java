package gs.web.search;

import gs.data.search.SolrConnectionManager;
import gs.data.search.indexers.DistrictIndexer;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.ArrayList;
import java.util.List;

public class DistrictSearchServiceSolrImpl extends BaseLuceneSearchService implements DistrictSearchService {

    private SolrConnectionManager _solrConnectionManager;

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
            SolrServer server = getSolrConnectionManager().getReadOnlySolrServer();
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

        String q = "";
        if (searchString != null) {
            q += "+" + DistrictIndexer.DISTRICT_NAME + ":" + searchString + " ";
        }
        
        if (state != null) {
            q += "+" + DistrictIndexer.STATE + ":" + searchString;
        }

        query.setQuery(q);
        return query;
    }

    public SolrConnectionManager getSolrConnectionManager() {
        return _solrConnectionManager;
    }

    public void setSolrConnectionManager(SolrConnectionManager solrConnectionManager) {
        _solrConnectionManager = solrConnectionManager;
    }
}