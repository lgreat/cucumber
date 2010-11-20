package gs.web.search;

import gs.data.search.GSQueryParser;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistrictSearchServiceImpl implements DistrictSearchService {

    Searcher _searcher;
    DistrictResultBuilder _resultBuilder;
    GSQueryParser _queryParser;

    public DistrictSearchServiceImpl() {
        _queryParser = new GSQueryParser();
    }

    public List<IDistrictSearchResult> search(String searchString, State state) throws SchoolSearchService.SearchException {

        Query query = buildQuery(searchString, state);

        Hits hits = getSearcher().search(query, null, null, null);

        List<IDistrictSearchResult> results;

        try {
            results = getResultBuilder().build(hits);
        } catch (IOException e) {
            throw new SchoolSearchService.SearchException("Problem processing results.", e);
        }

        return results;
    }

    public Query buildQuery(String searchString, State state) throws SchoolSearchService.SearchException {
        BooleanQuery districtQuery = new BooleanQuery();

        if (searchString != null) {
            try {
                Query keywordQuery = _queryParser.parse(searchString);
                districtQuery.add(keywordQuery, BooleanClause.Occur.MUST);
            } catch (ParseException e) {
                throw new SchoolSearchService.SearchException("Could not parse query.", e);
            }
        }

        Query stateQuery = new TermQuery(new Term("state", state.getAbbreviationLowerCase()));
        districtQuery.add(new TermQuery(new Term("type", "district")), BooleanClause.Occur.MUST);
        districtQuery.add(stateQuery, BooleanClause.Occur.MUST);
        return districtQuery;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public DistrictResultBuilder getResultBuilder() {
        return _resultBuilder;
    }

    public void setResultBuilder(DistrictResultBuilder resultBuilder) {
        _resultBuilder = resultBuilder;
    }
}

class DistrictResultBuilder implements LuceneResultBuilder {
    StateManager _stateManager;

    public DistrictResultBuilder() {
        _stateManager = new StateManager();
    }

    public List<IDistrictSearchResult> build(Hits hits) throws IOException {
        List<IDistrictSearchResult> searchResults = new ArrayList<IDistrictSearchResult>();

        int length = hits.length();

        for (int i = 0; i < length; i++) {
            Document document = hits.doc(i);
            DistrictSearchResult result = new DistrictSearchResult();
            result.setDistrict(document.get("district"));
            result.setState(getStateManager().getState(document.get("state")));
            searchResults.add(result);
        }

        return searchResults;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}