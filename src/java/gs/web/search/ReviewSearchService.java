package gs.web.search;

import gs.data.search.*;

import java.util.List;
import java.util.Map;

public interface ReviewSearchService {

    public SearchResultsPage<IReviewResult> search(String queryString) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, int offset, int count) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, FieldSort fieldSort) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, FieldSort fieldSort, int offset, int count) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, List<FilterGroup> filterGroups, FieldSort fieldSort) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filterGroups, FieldSort fieldSort) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filterGroups, FieldSort fieldSort, Double lat, Double lon, Float distance, int offset, int count) throws SearchException;

    public SearchResultsPage<IReviewResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filterGroups, FieldSort fieldSort, int offset, int count) throws SearchException;
}


