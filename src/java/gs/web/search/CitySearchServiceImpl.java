package gs.web.search;

import gs.data.search.GSAnalyzer;
import gs.data.search.Indexer;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CitySearchServiceImpl extends BaseLuceneSearchService implements CitySearchService {

    private Searcher _searcher;

    private CityResultBuilder _resultBuilder;

    private final QueryParser _queryParser;

    private Logger _log = Logger.getLogger(CitySearchServiceImpl.class);

    public CitySearchServiceImpl() {
        _queryParser = new QueryParser("city", new GSAnalyzer());
    }

    public List<ICitySearchResult> search(String searchString, State state) throws SchoolSearchServiceImpl.SearchException {
        return search(searchString, state, 0, 0);
    }

    public List<ICitySearchResult> search(String searchString, State state, int offset, int count) throws SchoolSearchServiceImpl.SearchException {
        List<ICitySearchResult> resultList = new ArrayList<ICitySearchResult>();

        Hits hits = null;
        try {
            Query query = buildQuery(searchString, state);

            if (query != null) {
                hits = getSearcher().search(query, null, null, null);
            }

            if (hits != null && hits.length() > 0) {
                resultList = new CityResultBuilder().build(hits, offset, count);//TODO: find better way to get result builder
            }
        } catch (ParseException e) {
            _log.debug("Parse exception: ", e);
            throw new SchoolSearchService.SearchException("Problem when performing search ", e);
        } catch (IOException e) {
            throw new SchoolSearchService.SearchException("Problem accessing search results.", e);
        }

        return resultList;
    }

    public Query buildQuery(String searchString, State state) throws ParseException {
        if (searchString != null) {
            searchString = StringUtils.trimToNull(searchString);
            searchString = StringUtils.lowerCase(searchString);

            if (searchString != null && searchString.matches(PUNCTUATION_AND_WHITESPACE_PATTERN)) {
                return null;//TODO: throw exception instead
            }

            searchString = padCommasAndNormalizeExtraSpaces(searchString);

            if (searchString != null) {
                searchString = QueryParser.escape(searchString);
            }

            //Query should be built using the given searchString; however, caller should be able to provide
            //an actual districtId, city, or state as well, since we cannot currently parse those out of the search string.

            searchString = StringUtils.trimToNull(searchString);
            if (searchString != null) {
                searchString = searchString.replaceFirst("\\?$", ""); // GS-7244 - trim question marks
            }
        }

        try {
            BooleanQuery cityQuery = new BooleanQuery();
            cityQuery.add(new TermQuery(new Term("type", "city")), BooleanClause.Occur.MUST);

            if (searchString != null) {
                Query parsedCityQuery = _queryParser.parse(searchString);
                parsedCityQuery.setBoost(3.0f);
                cityQuery.add(parsedCityQuery, BooleanClause.Occur.MUST);
            }

            if (state != null) {
                cityQuery.add(new TermQuery(new Term(Indexer.STATE, state.getAbbreviationLowerCase())), BooleanClause.Occur.MUST);
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
}

class CityResultBuilder extends AbstractLuceneResultBuilder<ICitySearchResult> {
    StateManager _stateManager;

    public CityResultBuilder() {
        _stateManager = new StateManager();
    }
    
    public ICitySearchResult build(Document document) {
        CitySearchResult result = new CitySearchResult();
        result.setCity(document.get("city"));
        result.setState(getStateManager().getState(document.get("state")));
        return result;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}