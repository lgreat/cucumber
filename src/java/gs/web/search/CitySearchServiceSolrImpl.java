package gs.web.search;

import gs.data.search.indexers.documentBuilders.CityDocumentBuilder;
import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.ArrayList;
import java.util.List;

public class CitySearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<ICitySearchResult> implements CitySearchService {

    private Logger _log = Logger.getLogger(CitySearchServiceImpl.class);

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(CityDocumentBuilder.DOCUMENT_TYPE + ":" + CityDocumentBuilder.DOCUMENT_TYPE_CITY);
    }

    @Override
    public void setQueryType(SolrQuery query) {
        query.add("df","city_name");
    }

    @Override
    public String buildQuery(String searchString) {
        String defaultQuery = "*:*";
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

    public List<ICitySearchResult> getResultBeans(QueryResponse response) {
        List<CitySearchResult> r = response.getBeans(CitySearchResult.class);
        return ListUtils.typedList(r, ICitySearchResult.class);
    }

    public List<String> suggest(String queryString, String state, int offset, int count) throws SearchException {

        SolrQuery query = new SolrQuery();

        String[] tokens = StringUtils.split(queryString);

        //extract the last word from the string
        String partialWord = tokens[tokens.length-1];

        tokens = (String[]) ArrayUtils.remove(tokens,tokens.length-1);

        //join all the completed words from beginning of the string
        String completedPhrase = StringUtils.join(tokens);

        //setQueryType(query);
        query.setQueryType("school-search");

        String q = "";
        if (completedPhrase != null && completedPhrase.length() > 0) {
            q = buildQuery(completedPhrase);
        }

        if (q != null && q.length() > 0) {
            query.setQuery(q);
        }
        
        query.setStart(offset);
        query.setFacet(true);

        query.addFacetField("school_autosuggest");

        //since we're faceting on a non-tokenized field, set the facet prefix to the entire query string instead
        //of just the last (incomplete) word in the query string
        //query.setFacetPrefix(partialWord);
        query.setFacetPrefix(queryString);

        if (state != null && state.length() > 0) {
            query.addFilterQuery(SchoolDocumentBuilder.ADDRESS_STATE + ":" + state);
        }

        query.setFacetMinCount(1);

        if (count > 0) {
           //query.setRows(count);
           query.setFacetLimit(count);
        }

        //addDocumentTypeFilter(query);
        query.addFilterQuery("document_type:school");

        SearchResultsPage<ICitySearchResult> searchResults = search(query);

        List<FacetField> facetFields = searchResults.getFacetFields();

        List<String> suggestions = new ArrayList<String>();

        if (facetFields != null && facetFields.size() > 0) {
            FacetField cityNameFacet = facetFields.get(0);

            List<FacetField.Count> counts = cityNameFacet.getValues();

            if (counts != null && counts.size() > 0) {
                suggestions = new ArrayList<String>(counts.size());

                for (FacetField.Count c : counts) {
                    suggestions.add(c.getName());
                }
            }
        }
        
        return suggestions;
    }

}
