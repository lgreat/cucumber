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

            if (hits != null && hits.length() > 0) {
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
        if (StringUtils.isBlank(searchString) && state == null) {
            throw new IllegalArgumentException("Cannot find districts without a searchString or a state");
        }

        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit regardless of field constraints
            }
        }

        BooleanQuery districtQuery = new BooleanQuery();
        districtQuery.add(new TermQuery(new Term("type", "district")), BooleanClause.Occur.MUST);

        if (searchString != null) {
            Query keywordQuery = _queryParser.parse(searchString);
            districtQuery.add(keywordQuery, BooleanClause.Occur.MUST);
        }

        if (state != null) {
            districtQuery.add(new TermQuery(new Term(Indexer.STATE, state.getAbbreviationLowerCase())), BooleanClause.Occur.MUST);
        }

        return districtQuery;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}

class DistrictResultBuilder extends AbstractLuceneResultBuilder<IDistrictSearchResult> {
    StateManager _stateManager;

    public DistrictResultBuilder() {
        _stateManager = new StateManager();
    }

    public IDistrictSearchResult build(Document document) {
        DistrictSearchResult result = new DistrictSearchResult();
        result.setName(document.get(Indexer.DISTRICT_NAME));
        String id = document.get(Indexer.ID);
        if (id != null) {
            result.setId(Integer.valueOf(id));
        }
        result.setState(getStateManager().getState(document.get("state")));
        result.setCity(document.get(Indexer.CITY));
        return result;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}