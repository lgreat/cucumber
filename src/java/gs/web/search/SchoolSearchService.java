package gs.web.search;

import java.util.List;
import java.util.Map;

public interface SchoolSearchService {

    public static class SearchException extends Exception {

        public SearchException() {
        }

        public SearchException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString) throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, int offset, int count) throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, FieldSort fieldSort)
            throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, FieldSort fieldSort, int offset, int count)
            throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, List<FilterGroup> filterGroups,
            FieldSort fieldSort)
            throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, List<FilterGroup> filterGroups,
            FieldSort fieldSort, int offset, int count)
            throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, Map<FieldConstraint,String> fieldConstraints,
            List<FilterGroup> filterGroups, FieldSort fieldSort)
            throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<? extends ISchoolSearchResult> search(String queryString, Map<FieldConstraint,String> fieldConstraints,
            List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count)
            throws SchoolSearchServiceImpl.SearchException;
}
