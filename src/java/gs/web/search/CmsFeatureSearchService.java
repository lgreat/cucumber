package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ContentKey;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;

import java.util.List;
import java.util.Map;

public interface CmsFeatureSearchService {
    public SearchResultsPage<ICmsFeatureSearchResult> search(String queryString) throws SearchException;
    public SearchResultsPage<ICmsFeatureSearchResult> search(List<CmsCategory> topics, List<CmsCategory> grades, List<CmsCategory> subjects, Map<String, Object> model, int page, boolean strict, ContentKey excludeContentKey, String language);
}