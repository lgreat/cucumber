package gs.web.search;

import gs.data.search.SolrConnectionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseSingleFieldSolrSearchService<RESULT_TYPE extends ISearchResult> extends BaseLuceneSearchService<RESULT_TYPE> {

    private SolrConnectionManager _solrConnectionManager;

    public Logger _log = Logger.getLogger(BaseSingleFieldSolrSearchService.class);

    public abstract List<RESULT_TYPE> getResultBeans(QueryResponse response);

    public abstract void addDocumentTypeFilter(SolrQuery query);

    public void setQueryType(SolrQuery query) {
        query.setQueryType("standard");
    }

    public SearchResultsPage<RESULT_TYPE> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {
        return search(queryString, fieldConstraints, filters, fieldSort, null, null, null, offset, count);
    }

    /**
     * @param queryString
     * @param filters     An array of filters to OR together, so that results within any filter's bitset will be returned
     * @param fieldSort
     * @return
     */
    public SearchResultsPage<RESULT_TYPE> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, Double lat, Double lon, Float distanceInMiles, int offset, int count) throws SearchException {

        QueryResponse response;
        int totalResults = 0;
        List<RESULT_TYPE> results = new ArrayList<RESULT_TYPE>();
        SpellCheckResponse spellCheckResponse = null;
        SearchResultsPage<RESULT_TYPE> searchResults = new SearchResultsPage<RESULT_TYPE>(0, results);

        SolrQuery query = new SolrQuery();

        setQueryType(query);

        query.setQuery(buildQuery(queryString));

        query.setStart(offset);

        if (count > 0) {
           query.setRows(count);
        }
        
        addDocumentTypeFilter(query);

        if (filters != null && filters.size() > 0) {
            String[] filterQueries = createFilterQueries(filters);
            query.addFilterQuery(filterQueries);
        }

        if (fieldConstraints != null && fieldConstraints.size() > 0) {
            addFieldConstraintsToQuery(fieldConstraints, query);
        }

        if (fieldSort != null) {
            SolrQuery.ORDER order = fieldSort.isDescending() ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc;
            query.addSortField(fieldSort.getField(), order);
        }

        if (lat != null && lon != null && distanceInMiles != null && distanceInMiles > 0.0f) {
            // convert distanceInMiles to distanceInKilometers;
            float distanceInKilometers = distanceInMiles * 1.6f;
            query.addFilterQuery("{!spatial circles=" + lat + "," + lon + "," + distanceInKilometers + "}");
        }

        try {
            SolrServer server = getSolrConnectionManager().getReadOnlySolrServer();

            response = server.query(query);

            totalResults = (int) response.getResults().getNumFound();

            //if a starting result is requested (via paging) that is greater than total number of results
            //search again with start of zero
            if (offset > totalResults) {
                query.setStart(0);
                response = server.query(query);
                totalResults = (int) response.getResults().getNumFound();
            }

            results = getResultBeans(response);

            spellCheckResponse = response.getSpellCheckResponse();

            searchResults = new SearchResultsPage<RESULT_TYPE>(totalResults, results);
            
            searchResults.setSpellCheckResponse(spellCheckResponse);

        } catch (IllegalArgumentException e) {
            _log.debug("Error building query", e);
            //search string or field constraints contained bad data, eat exception and return no hits
        } catch (Exception e) {
            throw new SearchException("Problem accessing search results.", e);
        }

        return searchResults;
    }

    public String buildQuery (String searchString) {
        String defaultQuery = " ";
        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return defaultQuery; //Provided search string was garbage, early exit
            }
        } else {
            return defaultQuery;
        }

        return searchString;
    }

    private void addFieldConstraintsToQuery(Map<? extends IFieldConstraint, String> fieldConstraints, SolrQuery query) {
        Set<? extends Map.Entry<? extends IFieldConstraint,String>> entrySet = fieldConstraints.entrySet();
        for (Map.Entry<? extends IFieldConstraint, String> entry : entrySet) {
            query.addFilterQuery("+" + entry.getKey().getFieldName() + ":\"" + StringUtils.lowerCase(entry.getValue()) + "\"");
        }
    }

    public SolrConnectionManager getSolrConnectionManager() {
        return _solrConnectionManager;
    }

    public void setSolrConnectionManager(SolrConnectionManager solrConnectionManager) {
        _solrConnectionManager = solrConnectionManager;
    }
}
