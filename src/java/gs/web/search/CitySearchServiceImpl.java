package gs.web.search;

import gs.data.search.GSAnalyzer;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CitySearchServiceImpl implements CitySearchService {

    private Searcher _searcher;

    private CityResultBuilder _resultBuilder;

    private final QueryParser _queryParser;

    private Logger _log = Logger.getLogger(CitySearchServiceImpl.class);

    public CitySearchServiceImpl() {
        _queryParser = new QueryParser("city", new GSAnalyzer());
    }

    public List<ICitySearchResult> search(String searchString, State state) throws IOException{
        Query query = buildQuery(searchString, state);
        Hits hits = getSearcher().search(query, null, null, null);
        return getResultBuilder().build(hits);
    }

    public Query buildQuery(String searchString, State state) {
        try {
            BooleanQuery cityQuery = new BooleanQuery();
            cityQuery.add(new TermQuery(new Term("type", "city")), BooleanClause.Occur.MUST);

            Query parsedCityQuery = _queryParser.parse(searchString);
            parsedCityQuery.setBoost(3.0f);
            cityQuery.add(parsedCityQuery, BooleanClause.Occur.MUST);

            if (state != null) {
                cityQuery.add(new TermQuery(new Term("city", state.getLongName().toLowerCase())), BooleanClause.Occur.SHOULD);
                cityQuery.add(new TermQuery(new Term("city", state.getAbbreviationLowerCase())), BooleanClause.Occur.SHOULD);
            }

            return cityQuery;

        } catch (ParseException pe) {
            _log.warn("error parsing: " + searchString, pe);
            return null;
        }
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public CityResultBuilder getResultBuilder() {
        return _resultBuilder;
    }

    public void setResultBuilder(CityResultBuilder resultBuilder) {
        _resultBuilder = resultBuilder;
    }
}

class CityResultBuilder implements LuceneResultBuilder {
    StateManager _stateManager;

    public CityResultBuilder() {
        _stateManager = new StateManager();
    }

    public List<ICitySearchResult> build(Hits hits) throws IOException {
        List<ICitySearchResult> searchResults = new ArrayList<ICitySearchResult>();

        int length = hits.length();

        for (int i = 0; i < length; i++) {
            Document document = hits.doc(i);
            CitySearchResult result = new CitySearchResult();
            result.setCity(document.get("city"));
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