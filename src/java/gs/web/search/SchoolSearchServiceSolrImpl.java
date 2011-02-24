package gs.web.search;

import gs.data.search.SolrConnectionManager;
import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;
import gs.data.search.parsing.IGsQueryParser;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.*;

public class SchoolSearchServiceSolrImpl extends BaseLuceneSearchService<ISchoolSearchResult> implements SchoolSearchService {

    private SolrConnectionManager _solrConnectionManager;
    private IGsQueryParser _queryParser;

    private Map<String, Float> _optionalTerms = new HashMap<String, Float>();

    public static final Logger _log = Logger.getLogger(SchoolSearchServiceSolrImpl.class);

    {
        _optionalTerms.put("n", new Float(1.0));
        _optionalTerms.put("s", new Float(1.0));
        _optionalTerms.put("e", new Float(1.0));
        _optionalTerms.put("w", new Float(1.0));
        _optionalTerms.put("ave", new Float(1.0));
        _optionalTerms.put("avenu", new Float(1.0));
        _optionalTerms.put("avenue", new Float(1.0));
        _optionalTerms.put("care", new Float(1.0));
        _optionalTerms.put("charter", new Float(1.0));
        _optionalTerms.put("city", new Float(0.5));
        _optionalTerms.put("citi", new Float(0.5)); // stemmed
        _optionalTerms.put("county", new Float(0.5));
        _optionalTerms.put("counti", new Float(0.5)); // stemmed
        _optionalTerms.put("dai", new Float(1.0));
        _optionalTerms.put("day", new Float(1.0));
        _optionalTerms.put("district", new Float(0.3));
        _optionalTerms.put("east", new Float(0.5));
        _optionalTerms.put("ed", new Float(1.0));
        _optionalTerms.put("elementary", new Float(0.7));
        _optionalTerms.put("elementry", new Float(0.7)); // spelling error
        _optionalTerms.put("elementari", new Float(0.7)); // stemmed
        _optionalTerms.put("fort", new Float(0.5));
        _optionalTerms.put("ft", new Float(0.5)); // stemmed
        _optionalTerms.put("grade", new Float(1.0));
        _optionalTerms.put("height", new Float(0.5));
        _optionalTerms.put("heights", new Float(0.5));
        _optionalTerms.put("high", new Float(0.7));
        _optionalTerms.put("hill", new Float(0.5));
        _optionalTerms.put("ht", new Float(0.5));
        _optionalTerms.put("hts", new Float(0.5));
        _optionalTerms.put("isd", new Float(1.0)); // won't happen
        _optionalTerms.put("intermediate", new Float(1.0));
        _optionalTerms.put("intermediat", new Float(1.0));
        _optionalTerms.put("junior", new Float(0.7));
        _optionalTerms.put("k", new Float(0.5));
        _optionalTerms.put("kindergarten", new Float(0.5));
        _optionalTerms.put("magnet", new Float(0.7));
        _optionalTerms.put("middle", new Float(0.7));
        _optionalTerms.put("middl", new Float(0.7));  // stemmed
        _optionalTerms.put("montessori", new Float(1.0));
        _optionalTerms.put("north", new Float(0.5));
        _optionalTerms.put("nurseri", new Float(1.0));
        _optionalTerms.put("nursery", new Float(1.0));
        _optionalTerms.put("point", new Float(0.5));
        _optionalTerms.put("port", new Float(0.5));
        _optionalTerms.put("pr", new Float(0.5));
        _optionalTerms.put("pre", new Float(0.5));
        _optionalTerms.put("prek", new Float(0.5));
        _optionalTerms.put("pre-k", new Float(0.5));
        _optionalTerms.put("pre k", new Float(0.5)); // ????
        _optionalTerms.put("pre-kindergarten", new Float(0.5));
        _optionalTerms.put("preschool", new Float(1.0));
        _optionalTerms.put("primary", new Float(1.0));
        _optionalTerms.put("primar", new Float(1.0));
        _optionalTerms.put("pt", new Float(0.5));
        _optionalTerms.put("school", new Float(0.0));
        _optionalTerms.put("schools", new Float(0.0));
        _optionalTerms.put("road", new Float(0.5));
        _optionalTerms.put("saint", new Float(0.5));
        _optionalTerms.put("senior", new Float(1.0));
        _optionalTerms.put("south", new Float(0.5));
        _optionalTerms.put("street", new Float(0.5));
        _optionalTerms.put("west", new Float(0.5));
    }

    /**
     * @param queryString
     * @param filters     An array of filters to OR together, so that results within any filter's bitset will be returned
     * @param fieldSort
     * @return
     */
    public SearchResultsPage<ISchoolSearchResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {

        QueryResponse response;
        int totalResults = 0;
        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        SearchResultsPage<ISchoolSearchResult> searchResults = new SearchResultsPage<ISchoolSearchResult>(0, results);

        SolrQuery query;
        try {
            query = buildQuery(queryString);
        } catch (ParseException e) {
            throw new SearchException("Problem parsing query.", e);
        }

        if (query != null) {

            if (filters.size() > 0) {
                String[] filterQueries = createFilterQueries(filters);
                query.addFilterQuery(filterQueries);
            }

            if (fieldConstraints != null && fieldConstraints.size() > 0) {
                Set<? extends Map.Entry<? extends IFieldConstraint,String>> entrySet = fieldConstraints.entrySet();
                for (Map.Entry<? extends IFieldConstraint, String> entry : entrySet) {
                    query.addFilterQuery("+" + entry.getKey().getFieldName() + ":\"" + StringUtils.lowerCase(entry.getValue()) + "\"");
                }
            }

            if (fieldSort != null) {
                SolrQuery.ORDER order = fieldSort.isDescending() ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc;
                query.addSortField(fieldSort.getField(), order);
            }

            try {
                SolrServer server = getSolrConnectionManager().getReadOnlySolrServer();

                if (query != null) {

                    query.setStart(offset);
                    query.setRows(count);
                    response = server.query(query);
                    totalResults = (int) response.getResults().getNumFound();
                    if (offset > totalResults) {
                        query.setStart(0);
                        response = server.query(query);
                        totalResults = (int) response.getResults().getNumFound();
                    }

                    List<SolrSchoolSearchResult> r = response.getBeans(SolrSchoolSearchResult.class);
                    results = ListUtils.typedList(r, SolrSchoolSearchResult.class);

                }

            } catch (IllegalArgumentException e) {
                _log.debug("Error building query", e);
                //search string or field constraints contained bad data, eat exception and return no hits
            } catch (Exception e) {
                throw new SearchException("Problem accessing search results.", e);
            }

            searchResults = new SearchResultsPage<ISchoolSearchResult>(totalResults, results);
        }
        
        return searchResults;
    }

    /**
     * @param searchString
     * @return
     * @throws Exception
     */
    protected SolrQuery buildQuery(String searchString) throws ParseException {

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQueryType("standard");
        solrQuery.setQuery("*:*");
        solrQuery.addFilterQuery(SchoolDocumentBuilder.DOCUMENT_TYPE + ":" + SchoolDocumentBuilder.DOCUMENT_TYPE_SCHOOL);

        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit
            }
        }

        if (!StringUtils.isBlank(searchString)) {
            if (_queryParser != null) {
                String parsed = getQueryParser().parse(searchString).toString();
                solrQuery.setQuery(parsed);
                solrQuery.setQueryType("standard"); //use our already-parsed query
            } else {
                solrQuery.setQuery(requireNonOptionalWords(searchString));
                solrQuery.setQueryType("school-search"); //solr will parse our query for us
            }
        }
        return solrQuery;
    }

    public String requireNonOptionalWords(String queryString) {
        String[] tokens = StringUtils.splitPreserveAllTokens(queryString);

        for (int i = 0; i < tokens.length; i++) {
            String lctoken = tokens[i].toLowerCase();
            if (!_optionalTerms.containsKey(lctoken)) {
                tokens[i] = "+" + tokens[i];
            }
        }

        return StringUtils.join(tokens, " ");
    }

    public IGsQueryParser getQueryParser() {
        return _queryParser;
    }

    public void setQueryParser(IGsQueryParser queryParser) {
        _queryParser = queryParser;
    }

    public SolrConnectionManager getSolrConnectionManager() {
        return _solrConnectionManager;
    }

    public void setSolrConnectionManager(SolrConnectionManager solrConnectionManager) {
        _solrConnectionManager = solrConnectionManager;
    }

    enum SchoolSearchFieldConstraints  implements IFieldConstraint {
        DISTRICT_ID(SchoolDocumentBuilder.SCHOOL_DISTRICT_ID),
        CITY(SchoolDocumentBuilder.ADDRESS_CITY),
        STATE(SchoolDocumentBuilder.ADDRESS_STATE);

        private String _fieldName;
        SchoolSearchFieldConstraints(String fieldName) {
            _fieldName = fieldName;
        }

        public String getFieldName() {
            return _fieldName;
        }

        public void setFieldName(String fieldName) {
            _fieldName = fieldName;
        }
    }
}

