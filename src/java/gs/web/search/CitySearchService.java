package gs.web.search;

import java.util.List;
import java.util.Map;

public interface CitySearchService {

    public SearchResultsPage<ICitySearchResult> search(String queryString) throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, int offset, int count) throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, FieldSort fieldSort)
            throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, FieldSort fieldSort, int offset, int count)
            throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, List<FilterGroup> filterGroups,
            FieldSort fieldSort)
            throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, List<FilterGroup> filterGroups,
            FieldSort fieldSort, int offset, int count)
            throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, Map<? extends IFieldConstraint,String> fieldConstraints,
            List<FilterGroup> filterGroups, FieldSort fieldSort)
            throws SearchException;

    public SearchResultsPage<ICitySearchResult> search(String queryString, Map<? extends IFieldConstraint,String> fieldConstraints,
            List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count)
            throws SearchException;

}
