package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseLuceneSearchService<T extends ISearchResult> {
    protected static final String PUNCTUATION_AND_WHITESPACE_PATTERN = "^[\\p{Punct}\\s]*$";

    private Map<String,String> _filters;
    
    {
        _filters = new HashMap<String,String>();

        _filters.put("preschool", "school_grade_level:p");
        _filters.put("elementary", "school_grade_level:e");
        _filters.put("middle", "school_grade_level:m");
        _filters.put("high", "school_grade_level:h");

        _filters.put("private", "school_type:private");
        _filters.put("public", "school_type:public");
        _filters.put("charter", "school_type:charter");

        //using the range * TO * prevents null/empty values from being included
        _filters.put("religious", "+(!affiliation:Nonsectarian AND !affiliation:Non-Sectarian AND affiliation:[* TO *])");
        _filters.put("nonsectarian", "+affiliation:(Non-Sectarian || Nonsectarian)");
    }

    static String padCommasAndNormalizeExtraSpaces(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll(",", ", ").replaceAll("\\s+", " ");
    }

    public String cleanseSearchString(String searchString) {
        searchString = StringUtils.trimToNull(searchString);
        searchString = StringUtils.lowerCase(searchString);

        if (searchString != null && searchString.matches(PUNCTUATION_AND_WHITESPACE_PATTERN)) {
            return null;//TODO: throw exception instead?
        }

        searchString = padCommasAndNormalizeExtraSpaces(searchString);

        if (searchString != null) {
            searchString = QueryParser.escape(searchString);
        }

        searchString = StringUtils.trimToNull(searchString);

        return searchString;
    }

    public String[] createFilterQueries(List<FilterGroup> filterGroups) {
        String[] subFilters = new String[filterGroups.size()];

        int j = 0;

        for (FilterGroup filterGroup : filterGroups) {
            FieldFilter[] filters = filterGroup.getFieldFilters();
            String[] filtersToAdd = new String[filters.length];
            for (int i = 0; i < filters.length; i++) {
                String filterToAdd = _filters.get(filters[i].getFilterName());
                filtersToAdd[i] = filterToAdd;
            }

            String f = "";
            for (int i = 0; i < filters.length; i++) {
                String filterToAdd = filtersToAdd[i];
                if (i > 0) {
                    f+= " OR ";
                }
                f += filterToAdd;
            }
            subFilters[j++] = "+(" + f + ")";
        }

        return subFilters;
    }

    public SearchResultsPage<T> search(String queryString) throws SearchException {
        return search(queryString, 0, 0);
    }

    public SearchResultsPage<T> search(String queryString, int offset, int count) throws SearchException {
        return search(queryString, new HashMap<IFieldConstraint, String>(), new ArrayList<FilterGroup>(), null, offset, count);
    }

    public SearchResultsPage<T> search(String queryString, FieldSort fieldSort) throws SearchException {
        return search(queryString, fieldSort, 0, 0);
    }

    public SearchResultsPage<T> search(String queryString, FieldSort fieldSort, int offset, int count) throws SearchException {
        return search(queryString, new HashMap<IFieldConstraint, String>(), new ArrayList<FilterGroup>(), fieldSort, offset, count);
    }

    public SearchResultsPage<T> search(String queryString, List<FilterGroup> filterGroups, FieldSort fieldSort) throws SearchException {
        return search(queryString, filterGroups, fieldSort, 0, 0);
    }
    
    public SearchResultsPage<T> search(String queryString, List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count) throws SearchException {
        return search(queryString, new HashMap<IFieldConstraint, String>(), filterGroups, fieldSort, offset, count);
    }

    public SearchResultsPage<T> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort) throws SearchException {
        return search(queryString, fieldConstraints, filters, fieldSort, 0, 0);
    }

    public abstract SearchResultsPage<T> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException;

}
