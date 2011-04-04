package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ContentKey;
import gs.data.search.SearchResultsPage;

import java.util.List;

public interface CmsFeatureSearchService {
    public SearchResultsPage<ICmsFeatureSearchResult> getCmsFeatures(List<CmsCategory> topics,
                                                                     List<CmsCategory> grades,
                                                                     List<CmsCategory> subjects,
                                                                     boolean strict, ContentKey excludeContentKey,
                                                                     String language, int pageSize, int offset);
    public SearchResultsPage<ICmsFeatureSearchResult> getCmsFeaturesSortByDate(Long GradeId, int pageSize, int pageNumber);
}