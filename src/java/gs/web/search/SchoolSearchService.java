package gs.web.search;

import java.util.Map;

public interface SchoolSearchService {

    public static class SearchException extends Exception {

        public SearchException() {
        }

        public SearchException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }

    public SearchResultsPage<ISchoolSearchResult> search(String queryString) throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, Map<FieldConstraint,String> fieldConstraints) throws SchoolSearchServiceImpl.SearchException ;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, FieldFilter[] fieldFilters, FieldSort fieldSort) throws SchoolSearchServiceImpl.SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, Map<FieldConstraint,String> fieldConstraints, FieldFilter[] fieldFilters, FieldSort fieldSort) throws SchoolSearchServiceImpl.SearchException;
}
