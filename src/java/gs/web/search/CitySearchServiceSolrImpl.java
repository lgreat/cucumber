package gs.web.search;

import gs.data.search.SolrConnectionManager;
import gs.data.search.indexers.documentBuilders.CityDocumentBuilder;
import gs.data.search.parsing.IGsQueryParser;
import gs.data.state.State;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CitySearchServiceSolrImpl extends BaseLuceneSearchService<ICitySearchResult> implements CitySearchService {

    private SolrConnectionManager _solrConnectionManager;

    private Logger _log = Logger.getLogger(CitySearchServiceImpl.class);

    private IGsQueryParser _queryParser;

    public CitySearchServiceSolrImpl() {
        //_queryParser = new QueryParser("city", new GSAnalyzer());
    }

    public List<ICitySearchResult> search(String searchString, State state) throws SearchException {
        return search(searchString, state, 0, 0);
    }

    @Override
    public SearchResultsPage<ICitySearchResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<ICitySearchResult> search(String searchString, State state, int offset, int count) throws SearchException {
        List<ICitySearchResult> resultList = new ArrayList<ICitySearchResult>();

        QueryResponse response = null;

        try {
            SolrServer server = getSolrConnectionManager().getReadOnlySolrServer();
            SolrQuery query = buildQuery(searchString);
            query.addFilterQuery(CityDocumentBuilder.STATE + ":" + state.getAbbreviationLowerCase());

            if (query != null) {
                query.setStart(offset);
                query.setRows(count);
                response = server.query(query);
            }

            if (response != null && response.getResults().size() > 0) {
                List<CitySearchResult> r = response.getBeans(CitySearchResult.class);
                resultList = ListUtils.typedList(r, ICitySearchResult.class);
                //resultList = new CityResultBuilder().build(hits, offset, count);//TODO: find better way to get result builder
            }
        } catch (SolrServerException e) {
            throw new SearchException("Problem accessing search results.", e);
        } catch (Exception e) {
            throw new SearchException("Problem accessing search results.", e);
        }

        return resultList;
    }

    public SolrQuery buildQuery(String searchString) throws ParseException {
        SolrQuery query = new SolrQuery();
        query.addFilterQuery(CityDocumentBuilder.DOCUMENT_TYPE + ":" + CityDocumentBuilder.DOCUMENT_TYPE_CITY);
        query.setQueryType("standard");

        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit regardless of field constraints
            }
        }

        String q = "*:*";
        if (!StringUtils.isBlank(searchString)) {
            if (_queryParser != null) {
                q = getQueryParser().parse(searchString).toString();
                query.setQueryType("standard"); //use our already-parsed query
            } else {
                q = "+" + CityDocumentBuilder.CITY_NAME + ":(" + searchString + ")^3.0";
            }
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

    public IGsQueryParser getQueryParser() {
        return _queryParser;
    }

    public void setQueryParser(IGsQueryParser queryParser) {
        _queryParser = queryParser;
    }
}
