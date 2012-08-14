package gs.web.search;

import gs.data.content.cms.CmsCategory;

import java.util.List;

public interface CmsRelatedFeatureCacheManager {

    void refresh();
    List<Long> findFeatureIds(CmsCategory category);
    List<CmsCategory> findPaddingCategories();

}
