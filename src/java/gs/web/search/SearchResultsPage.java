package gs.web.search;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.SpellCheckResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the total number of results found, and the page of results to return
 *
 * @param <SR>
 */
public class SearchResultsPage<SR extends ISearchResult> {

    private int _totalResults;

    private List<SR> _searchResults;

    private SpellCheckResponse _spellCheckResponse;

    private List<FacetField> _facetFields;

    public SearchResultsPage(int totalResults, List<SR> searchResults) {
        _totalResults = totalResults;
        _searchResults = searchResults;
    }

    public List<String> getSuggestionsForFirstFacetField() {

        List<String> suggestions = new ArrayList<String>();

        if (_facetFields != null && _facetFields.size() > 0) {
            FacetField facetField = _facetFields.get(0);

            List<FacetField.Count> counts = facetField.getValues();

            if (counts != null && counts.size() > 0) {
                suggestions = new ArrayList<String>(counts.size());

                for (FacetField.Count c : counts) {
                    suggestions.add(c.getName());
                }
            }
        }
        return suggestions;
    }

    public int getTotalResults() {
        return _totalResults;
    }

    public void setTotalResults(int totalResults) {
        _totalResults = totalResults;
    }

    public List<SR> getSearchResults() {
        return _searchResults;
    }

    public void setSearchResults(List<SR> searchResults) {
        _searchResults = searchResults;
    }

    public SpellCheckResponse getSpellCheckResponse() {
        return _spellCheckResponse;
    }

    public void setSpellCheckResponse(SpellCheckResponse spellCheckResponse) {
        _spellCheckResponse = spellCheckResponse;
    }

    public List<FacetField> getFacetFields() {
        return _facetFields;
    }

    public void setFacetFields(List<FacetField> facetFields) {
        _facetFields = facetFields;
    }
}
