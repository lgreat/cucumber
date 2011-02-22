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
import java.util.Map;

public class DistrictSearchServiceImpl extends BaseLuceneSearchService<IDistrictSearchResult> implements DistrictSearchService {

    Searcher _searcher;

    GSQueryParser _queryParser;

    private Logger _log = Logger.getLogger(DistrictSearchServiceImpl.class);

    public DistrictSearchServiceImpl() {
        _queryParser = new GSQueryParser();
    }


    @Override
    public SearchResultsPage<IDistrictSearchResult> search(String searchString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {
        State state = null;
        if (fieldConstraints != null) {
            String s = fieldConstraints.get(SchoolSearchServiceSolrImpl.SchoolSearchFieldConstraints.STATE);
            if (s != null) {
                state = new StateManager().getState(s);
            }
        }

        List<IDistrictSearchResult> results = search(searchString, state, offset, count);
        SearchResultsPage page;
        if (results != null && results.size() > 0) {
            page = new SearchResultsPage(results.size(), results);
        } else {
            page = new SearchResultsPage(0, new ArrayList<IDistrictSearchResult>());
        }

        return page;
    }

    public List<IDistrictSearchResult> search(String searchString, State state) throws SearchException {
        return search(searchString, state, 0, 0);
    }

    public List<IDistrictSearchResult> search(String searchString, State state, int offset, int count) throws SearchException {
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
            throw new SearchException("Problem when performing search ", e);
        } catch (IOException e) {
            throw new SearchException("Problem processing results.", e);
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