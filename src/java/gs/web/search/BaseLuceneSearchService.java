package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseLuceneSearchService<T extends ISearchResult> {
    protected static final String PUNCTUATION_AND_WHITESPACE_PATTERN = "^[\\p{Punct}\\s]*$";

    private Map<FieldFilter,String> _filters;
    
    {
        _filters = new HashMap<FieldFilter,String>();

        _filters.put(FieldFilter.GradeLevelFilter.PRESCHOOL, "school_grade_level:p");
        _filters.put(FieldFilter.GradeLevelFilter.ELEMENTARY, "school_grade_level:e");
        _filters.put(FieldFilter.GradeLevelFilter.MIDDLE, "school_grade_level:m");
        _filters.put(FieldFilter.GradeLevelFilter.HIGH, "school_grade_level:h");

        _filters.put(FieldFilter.SchoolTypeFilter.PRIVATE, "school_type:private");
        _filters.put(FieldFilter.SchoolTypeFilter.PUBLIC, "school_type:public");
        _filters.put(FieldFilter.SchoolTypeFilter.CHARTER, "school_type:charter");

        //using the range * TO * prevents null/empty values from being included
        _filters.put(FieldFilter.AffiliationFilter.RELIGIOUS, "+(-school_affiliation:Non-Sectarian OR school_affiliation:None)");
        //http://stackoverflow.com/questions/1343794/searching-for-date-range-or-null-no-field-in-solr
        _filters.put(FieldFilter.AffiliationFilter.NONSECTARIAN, "+(school_affiliation:Non-Sectarian OR school_affiliation:Nonsectarian OR school_affiliation:None)");

        _filters.put(FieldFilter.StudentTeacherRatio.UNDER_10, "school_student_teacher_ratio:[1 TO 9]");
        _filters.put(FieldFilter.StudentTeacherRatio.UNDER_15, "school_student_teacher_ratio:[1 TO 14]");
        _filters.put(FieldFilter.StudentTeacherRatio.UNDER_20, "school_student_teacher_ratio:[1 TO 19]");
        _filters.put(FieldFilter.StudentTeacherRatio.UNDER_25, "school_student_teacher_ratio:[1 TO 24]");

        _filters.put(FieldFilter.SchoolSize.UNDER_20, "school_size:[1 TO 19]");
        _filters.put(FieldFilter.SchoolSize.UNDER_50, "school_size:[1 TO 49]");
        _filters.put(FieldFilter.SchoolSize.UNDER_200, "school_size:[1 TO 199]");
        _filters.put(FieldFilter.SchoolSize.UNDER_500, "school_size:[1 TO 499]");
        _filters.put(FieldFilter.SchoolSize.UNDER_1000, "school_size:[1 TO 999]");
        _filters.put(FieldFilter.SchoolSize.OVER_1000, "school_size:[1000 TO *]");

        _filters.put(FieldFilter.LowestAgeAccepted.UNDER_ONE, "school_student_lowest_age_accepted:[0 TO 0]");
        _filters.put(FieldFilter.LowestAgeAccepted.ONE, "school_student_lowest_age_accepted:[0 TO 1]");
        _filters.put(FieldFilter.LowestAgeAccepted.TWO, "school_student_lowest_age_accepted:[0 TO 2]");
        _filters.put(FieldFilter.LowestAgeAccepted.THREE, "school_student_lowest_age_accepted:[0 TO 3]");
        _filters.put(FieldFilter.LowestAgeAccepted.FOUR_AND_ABOVE, "school_student_lowest_age_accepted:[0 TO *]");
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
        List<String> subFilters = new ArrayList<String>();

        int j = 0;

        for (FilterGroup filterGroup : filterGroups) {
            if (filterGroup == null || filterGroup.getFieldFilters() == null || filterGroup.getFieldFilters().length == 0) {
                continue;
            }
            
            FieldFilter[] filters = filterGroup.getFieldFilters();
            String[] filtersToAdd = new String[filters.length];
            for (int i = 0; i < filters.length; i++) {
                String filterToAdd = _filters.get(filters[i]);
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
            subFilters.add("+(" + f + ")");
        }

        return subFilters.toArray(new String[0]);
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
