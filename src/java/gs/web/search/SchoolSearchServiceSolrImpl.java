package gs.web.search;

import gs.data.search.GSQueryParser;
import gs.data.search.SolrConnectionManager;
import gs.data.search.indexers.SchoolIndexer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.*;

public class SchoolSearchServiceSolrImpl extends BaseLuceneSearchService implements SchoolSearchService {

    private GSQueryParser _queryParser;
    private SolrConnectionManager _solrConnectionManager;

    public static final Logger _log = Logger.getLogger(SchoolSearchServiceSolrImpl.class);

    private Map<String,String> _filters;

    {
        _filters = new HashMap<String,String>();

        _filters.put("preschool", "school_grade_level:p");
        _filters.put("elementary", "school_grade_level:e");
        _filters.put("middle", "school_grade_level:m");
        _filters.put("high", "school_grade_level:h");

        _filters.put("private", "school_type:private");
        _filters.put("public", "school_type:public");
        _filters.put("charter", "school_type:charter");
    }

    public SchoolSearchServiceSolrImpl() {
        _queryParser = new GSQueryParser();
    }

    //TODO: best way to get correct builder?
    SchoolSearchResultBuilder _resultsBuilder = new SchoolSearchResultBuilder();

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), new ArrayList<FilterGroup>(), null);
    }

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, int offset, int count) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), new ArrayList<FilterGroup>(), null, offset, count);
    }

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, FieldSort fieldSort) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), new ArrayList<FilterGroup>(), fieldSort);
    }

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, FieldSort fieldSort, int offset, int count) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), new ArrayList<FilterGroup>(), fieldSort, offset, count);
    }

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, List<FilterGroup> filterGroups, FieldSort fieldSort) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), filterGroups, fieldSort);
    }
    
    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), filterGroups, fieldSort, offset, count);
    }
    
    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, Map<FieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort) throws SearchException {
        return search(queryString, fieldConstraints, filters, fieldSort, 0, 0);
    }


    /**
     * @param queryString
     * @param filters     An array of filters to OR together, so that results within any filter's bitset will be returned
     * @param fieldSort
     * @return
     */
    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, Map<FieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {

        QueryResponse response = null;
        int totalResults = 0;
        List<SolrSchoolSearchResult> results = new ArrayList<SolrSchoolSearchResult>();
        SearchResultsPage<SolrSchoolSearchResult> searchResults;

        SolrQuery query = buildQuery(queryString, fieldConstraints);

        if (filters.size() > 0) {
            String[] filterQueries = createFilterQueries(filters);
            query.addFilterQuery(filterQueries);
        }

        if (fieldSort != null) {
            SolrQuery.ORDER order = fieldSort.isDescending()? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc;
            query.addSortField(fieldSort.getField(), order);
        }

        try {
            SolrServer server = getSolrConnectionManager().getReadOnlySolrServer();

            if (query != null) {

                query.setStart(offset);
                query.setRows(count);
                response = server.query(query);

                if (response != null && response.getResults().size() > 0) {
                    results = response.getBeans(SolrSchoolSearchResult.class);
                    totalResults = (int) response.getResults().getNumFound();
                }

            }

        } catch (IllegalArgumentException e) {
            _log.debug("Error building query", e);
            //search string or field constraints contained bad data, eat exception and return no hits
        } catch (Exception e) {
            throw new SchoolSearchService.SearchException("Problem accessing search results.", e);
        }

        searchResults = new SearchResultsPage<SolrSchoolSearchResult>(totalResults, results);
        return searchResults;
    }

    /**
     * @param searchString
     * @param fieldConstraints
     * @return
     * @throws Exception
     */
    protected SolrQuery buildQuery(String searchString, Map<FieldConstraint, String> fieldConstraints) {


        if (StringUtils.isBlank(searchString) && (fieldConstraints == null || fieldConstraints.size() == 0)) {
            throw new IllegalArgumentException("Cannot build query with no search string and no constraints");
        }

        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit regardless of field constraints
            }
        }

        StringBuilder query = new StringBuilder();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.addFilterQuery(SchoolIndexer.DOCUMENT_TYPE + ":" + SchoolIndexer.DOCUMENT_TYPE_SCHOOL);

        if (!StringUtils.isBlank(searchString)) {
            // Check for zipcode searches
            if (searchString.length() == 5 && StringUtils.isNumeric(searchString)) {
                searchString = "zip:" + searchString;
                query.append(SchoolIndexer.ADDRESS_ZIP + ":" + searchString).append(" ");
            }
            query.append(SchoolIndexer.SCHOOL_NAME + ":" + searchString).append(" ");
            query.append(SchoolIndexer.ADDRESS_CITY_KEYWORD + ":" + searchString + "^3.0").append(" ");
            query.append("+" + SchoolIndexer.TEXT + ":" + searchString).append(" ");
            query.append(SchoolIndexer.TEXT + ":\"" + searchString + "\"").append(" ");
        }

        if (fieldConstraints != null && fieldConstraints.size() > 0) {
            Set<Map.Entry<FieldConstraint, String>> entrySet = fieldConstraints.entrySet();
            for (Map.Entry<FieldConstraint, String> entry : entrySet) {
                query.append("+").append(entry.getKey().getFieldName() + ":" + StringUtils.lowerCase(entry.getValue())).append(" ");
            }
        }

        solrQuery.setQuery(query.toString());
        return solrQuery;
    }


    public String[] createFilterQueries(List<FilterGroup> filterGroups) {
        String[] subFilters = new String[filterGroups.size()];
        String filterQuery = "";

        int j = 0;

        for (FilterGroup filterGroup : filterGroups) {
            FieldFilter[] filters = filterGroup.getFieldFilters();
            String[] filtersToAdd = new String[filters.length];
            for (int i = 0; i < filters.length; i++) {
                String filterToAdd = _filters.get(filters[i].getFilterName());
                filtersToAdd[i] = filterToAdd;
            }

            String f = "";
            for (int i = 0; i < filters.length; i++) {
                String filterToAdd = filtersToAdd[i];
                if (i > 0) {
                    f+= " OR ";
                }
                f += filterToAdd;
            }
            subFilters[j++] = f;
        }

        return subFilters;
    }

    public GSQueryParser getQueryParser() {
        return _queryParser;
    }

    public void setQueryParser(GSQueryParser queryParser) {
        _queryParser = queryParser;
    }

    public SchoolSearchResultBuilder getResultsBuilder() {
        return _resultsBuilder;
    }

    public void setResultsBuilder(SchoolSearchResultBuilder resultsBuilder) {
        _resultsBuilder = resultsBuilder;
    }

    public SolrConnectionManager getSolrConnectionManager() {
        return _solrConnectionManager;
    }

    public void setSolrConnectionManager(SolrConnectionManager solrConnectionManager) {
        _solrConnectionManager = solrConnectionManager;
    }
}

