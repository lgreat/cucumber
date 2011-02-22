package gs.web.search;

import java.util.List;
import java.util.Map;

public interface SchoolSearchService {

    public SearchResultsPage<ISchoolSearchResult> search(String queryString) throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, int offset, int count) throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, FieldSort fieldSort)
            throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, FieldSort fieldSort, int offset, int count)
            throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, List<FilterGroup> filterGroups,
            FieldSort fieldSort)
            throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, List<FilterGroup> filterGroups,
            FieldSort fieldSort, int offset, int count)
            throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, Map<? extends IFieldConstraint,String> fieldConstraints,
            List<FilterGroup> filterGroups, FieldSort fieldSort)
            throws SearchException;

    public SearchResultsPage<ISchoolSearchResult> search(String queryString, Map<? extends IFieldConstraint,String> fieldConstraints,
            List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count)
            throws SearchException;
}
