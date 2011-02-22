package gs.web.search;

import gs.data.search.ChainedFilter;
import gs.data.search.GSQueryParser;
import gs.data.search.Searcher;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class SchoolSearchServiceImpl extends BaseLuceneSearchService<ISchoolSearchResult> implements SchoolSearchService {

    private Searcher _searcher;
    private GSQueryParser _queryParser;
    public static final Logger _log = Logger.getLogger(SchoolSearchServiceImpl.class);

    public SchoolSearchServiceImpl() {
        _queryParser = new GSQueryParser();
    }

    //TODO: best way to get correct builder?
    SchoolSearchResultBuilder _resultsBuilder = new SchoolSearchResultBuilder();


    /**
     * @param queryString
     * @param filters     An array of filters to OR together, so that results within any filter's bitset will be returned
     * @param fieldSort
     * @return
     */
    public SearchResultsPage<ISchoolSearchResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {

        ChainedFilter luceneFilter = null;
        if (filters.size() > 0) {
            luceneFilter = createChainedFilter(filters);
        }

        Sort luceneSort = null;
        if (fieldSort != null) {
            luceneSort = new Sort(fieldSort.getField(), fieldSort.isDescending());
        }

        Hits hits = searchLucene(queryString, fieldConstraints, luceneFilter, luceneSort);
        List<ISchoolSearchResult> resultList = new ArrayList<ISchoolSearchResult>();
        int totalResults = 0;

        if (hits != null && hits.length() > 0) {
            totalResults = hits.length();

            if (offset >= totalResults) {
                offset = 0;
            }

            try {
                resultList = new SchoolSearchResultBuilder().build(hits, offset, count);
            } catch (IOException e) {
                throw new SearchException("Problem accessing search results.", e);
            }
        }

        SearchResultsPage searchResults = new SearchResultsPage(totalResults, resultList);

        return searchResults;
    }

    public Hits searchLucene(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, Filter luceneFilter, Sort luceneSort) throws SearchException {

        //for now, we'll just search using the "old" method of searching, by passing a SearchCommand to Searcher.
        //This should be rewritten, and probably will when we move to solr
        //Hits hits = getSearcher().search(searchCommand);

        Hits hits = null;

        try {
            Query query = buildQuery(queryString, fieldConstraints);
            if (query != null) {
                hits = getSearcher().search(query, luceneSort, null, luceneFilter);
            }

        } catch (ParseException e) {
            _log.debug("Parse exception: ", e);
            throw new SearchException("Problem when performing search ", e);
        } catch (IllegalArgumentException e) {
            _log.debug("Error building query", e);
            //search string or field constraints contained bad data, eat exception and return no hits
        }

        return hits;
    }

    /**
     * @param searchString
     * @param fieldConstraints
     * @return
     * @throws Exception
     */
    protected Query buildQuery(String searchString, Map<? extends IFieldConstraint, String> fieldConstraints) throws ParseException {
        BooleanQuery mixedQuery = null;

        if (StringUtils.isBlank(searchString) && (fieldConstraints == null || fieldConstraints.size() == 0)) {
            throw new IllegalArgumentException("Cannot build query with no search string and no constraints");
        }

        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit regardless of field constraints
            }
        }

        mixedQuery = new BooleanQuery();
        mixedQuery.add(new TermQuery(new Term("type", "school")), BooleanClause.Occur.MUST);

        if (!StringUtils.isBlank(searchString)) {
            // Check for zipcode searches
            if (searchString.length() == 5 && StringUtils.isNumeric(searchString)) {
                searchString = "zip:" + searchString;
            }
            Query query = _queryParser.parse(searchString);
            mixedQuery.add(query, BooleanClause.Occur.MUST);
        }

        if (fieldConstraints != null && fieldConstraints.size() > 0) {
            Set<? extends Map.Entry<? extends IFieldConstraint,String>> entrySet = fieldConstraints.entrySet();
            for (Map.Entry<? extends IFieldConstraint, String> entry : entrySet) {
                PhraseQuery phraseQuery = new PhraseQuery();
                phraseQuery.add(new Term(entry.getKey().getFieldName(), StringUtils.lowerCase(entry.getValue())));
                mixedQuery.add(phraseQuery, BooleanClause.Occur.MUST);
            }
        }

        return mixedQuery;
    }

    protected ChainedFilter createChainedFilter(List<FilterGroup> filterGroups) {
        ChainedFilter[] subFilters = new ChainedFilter[filterGroups.size()];
        int j = 0;

        for (FilterGroup filterGroup : filterGroups) {
            FieldFilter[] filters = filterGroup.getFieldFilters();
            Filter[] filtersToAdd = new Filter[filters.length];
            for (int i = 0; i < filters.length; i++) {
                Filter filterToAdd = Searcher.getFilters().get(filters[i].getFilterName());
                filtersToAdd[i] = filterToAdd;
            }
            subFilters[j++] = new ChainedFilter(filtersToAdd, ChainedFilter.OR);
        }

        ChainedFilter chainedFilter = new ChainedFilter(subFilters, ChainedFilter.AND);
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

    public SchoolSearchResultBuilder getResultsBuilder() {
        return _resultsBuilder;
    }

    public void setResultsBuilder(SchoolSearchResultBuilder resultsBuilder) {
        _resultsBuilder = resultsBuilder;
    }

    
}

