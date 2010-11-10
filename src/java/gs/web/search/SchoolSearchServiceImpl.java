package gs.web.search;

import gs.data.search.*;
import gs.data.search.Searcher;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class SchoolSearchServiceImpl implements SchoolSearchService {

    private Searcher _searcher;
    private GSQueryParser _queryParser;
    public static final Logger _log = Logger.getLogger(SchoolSearchServiceImpl.class);

    //TODO: best way to get correct builder?
    SchoolSearchResultsBuilder _resultsBuilder = new SchoolSearchResultsBuilder();

    public List<ISchoolSearchResult> search(String queryString) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), new FieldFilter[0], null);
    }

    public List<ISchoolSearchResult> search(String queryString, Map<FieldConstraint, String> fieldConstraints) throws SearchException {
        return search(queryString, fieldConstraints, new FieldFilter[0], null);
    }

    public List<ISchoolSearchResult> search(String queryString, FieldFilter[] fieldFilters, FieldSort fieldSort) throws SearchException {
        return search(queryString, new HashMap<FieldConstraint, String>(), fieldFilters, fieldSort);
    }

    /**
     * @param queryString
     * @param filters     An array of filters to OR together, so that results within any filter's bitset will be returned
     * @param fieldSort
     * @return
     */
    public List<ISchoolSearchResult> search(String queryString, Map<FieldConstraint, String> fieldConstraints, FieldFilter[] filters, FieldSort fieldSort) throws SearchException {

        ChainedFilter luceneFilter = null;
        if (filters.length > 0) {
            luceneFilter = createChainedFilter(filters);
        }

        Sort luceneSort = null;
        if (fieldSort != null) {
            luceneSort = new Sort(fieldSort.getField(), fieldSort.isDescending());
        }

        Hits hits = searchLucene(queryString, fieldConstraints, luceneFilter, luceneSort);

        List<ISchoolSearchResult> searchResults = null;

        try {
            searchResults = new SchoolSearchResultsBuilder().build(hits);
        } catch (IOException e) {
            throw new SearchException("Problem accessing search results.", e);
        }

        return searchResults;
    }

    public Hits searchLucene(String queryString, Map<FieldConstraint, String> fieldConstraints, Filter luceneFilter, Sort luceneSort) throws SearchException {

        //for now, we'll just search using the "old" method of searching, by passing a SearchCommand to Searcher.
        //This should be rewritten, and probably will when we move to solr
        //Hits hits = getSearcher().search(searchCommand);

        Hits hits = null;

        try {
            Query query = buildQuery(queryString, fieldConstraints);
            System.out.println(query.toString());
            hits = getSearcher().search(query, luceneSort, null, luceneFilter);

        } catch (ParseException e) {
            _log.debug("Parse exception: ", e);
            throw new SearchException("Problem when performing search ", e);
        }

        return hits;
    }

    /**
     * @param searchString
     * @param fieldConstraints
     * @return
     * @throws Exception
     */
    protected Query buildQuery(String searchString, Map<FieldConstraint, String> fieldConstraints) throws ParseException {

        //Query should be built using the given searchString; however, caller should be able to provide
        //an actual districtId, city, or state as well, since we cannot currently parse those out of the search string.

        searchString = StringUtils.trimToNull(searchString);
        if (searchString != null) {
            searchString = searchString.replaceFirst("\\?$", ""); // GS-7244 - trim question marks
        }

        // Check for zipcode searches
        if (searchString != null && searchString.length() == 5 && StringUtils.isNumeric(searchString)) {
            searchString = "zip:" + searchString;
        }

        BooleanQuery mixedQuery = new BooleanQuery();

        if (searchString != null) {
            Query query = _queryParser.parse(searchString);
            mixedQuery.add(query, BooleanClause.Occur.MUST);
        }

        mixedQuery.add(new TermQuery(new Term("type", "school")), BooleanClause.Occur.MUST);

        Set<Map.Entry<FieldConstraint, String>> entrySet = fieldConstraints.entrySet();
        for (Map.Entry<FieldConstraint, String> entry : entrySet) {
            PhraseQuery phraseQuery = new PhraseQuery();
            phraseQuery.add(new Term(entry.getKey().getFieldName(), StringUtils.lowerCase(entry.getValue())));
            mixedQuery.add(phraseQuery, BooleanClause.Occur.MUST);
        }

        return mixedQuery;
    }

    protected ChainedFilter createChainedFilter(FieldFilter[] filters) {

        Filter[] filtersToAdd = new Filter[filters.length];

        for (int i = 0; i < filters.length; i++) {
            Filter filterToAdd = Searcher.getFilters().get(filters[i].getFilterName());
            filtersToAdd[i] = filterToAdd;
        }

        ChainedFilter chainedFilter = new ChainedFilter(filtersToAdd, ChainedFilter.AND);
        return chainedFilter;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public GSQueryParser getQueryParser() {
        return _queryParser;
    }

    public void setQueryParser(GSQueryParser queryParser) {
        _queryParser = queryParser;
    }

    public SchoolSearchResultsBuilder getResultsBuilder() {
        return _resultsBuilder;
    }

    public void setResultsBuilder(SchoolSearchResultsBuilder resultsBuilder) {
        _resultsBuilder = resultsBuilder;
    }

    
}

