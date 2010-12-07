package gs.web.search;

import gs.data.search.GSQueryParser;
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

public class DistrictSearchServiceImpl extends BaseLuceneSearchService implements DistrictSearchService {

    Searcher _searcher;

    GSQueryParser _queryParser;

    private Logger _log = Logger.getLogger(DistrictSearchServiceImpl.class);

    public DistrictSearchServiceImpl() {
        _queryParser = new GSQueryParser();
    }

    public List<IDistrictSearchResult> search(String searchString, State state) throws SchoolSearchService.SearchException {
        return search(searchString, state, 0, 0);
    }

    public List<IDistrictSearchResult> search(String searchString, State state, int offset, int count) throws SchoolSearchService.SearchException {
        List<IDistrictSearchResult> resultList = new ArrayList<IDistrictSearchResult>();

        Hits hits = null;
        try {
            Query query = buildQuery(searchString, state);

            if (query != null) {
                hits = getSearcher().search(query, null, null, null);
            }

            if (hits != null) {
                resultList = new DistrictResultBuilder().build(hits, offset, count);
            }
        } catch (ParseException e) {
            _log.debug("Parse exception: ", e);
            throw new SchoolSearchService.SearchException("Problem when performing search ", e);
        } catch (IOException e) {
            throw new SchoolSearchService.SearchException("Problem processing results.", e);
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

        BooleanQuery districtQuery = new BooleanQuery();

        if (searchString != null) {
            Query keywordQuery = _queryParser.parse(searchString);
            districtQuery.add(keywordQuery, BooleanClause.Occur.MUST);
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
}

class DistrictResultBuilder implements LuceneResultBuilder {
    StateManager _stateManager;

    public DistrictResultBuilder() {
        _stateManager = new StateManager();
    }

    public List<IDistrictSearchResult> build(Hits hits) throws IOException {
        return build(hits, 0, 0);
    }

    public List<IDistrictSearchResult> build(Hits hits, int offset, int count) throws IOException {
        int length = hits.length();
        if (count == 0) {
            count = hits.length();
        }

        List<IDistrictSearchResult> searchResults = new ArrayList<IDistrictSearchResult>();

        for (int i = offset; (i < length && i < offset + count); i++ ) {
            Document document = hits.doc(i);
            DistrictSearchResult result = new DistrictSearchResult();
            result.setName(document.get(Indexer.DISTRICT_NAME));
            String id = document.get(Indexer.ID);
            if (id != null) {
                result.setId(Integer.valueOf(id));
            }
            result.setState(getStateManager().getState(document.get("state")));
            result.setCity(document.get(Indexer.CITY));
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