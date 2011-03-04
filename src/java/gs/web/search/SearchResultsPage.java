package gs.web.search;

import org.apache.solr.client.solrj.response.SpellCheckResponse;

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

    public SearchResultsPage(int totalResults, List<SR> searchResults) {
        _totalResults = totalResults;
        _searchResults = searchResults;
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
}
