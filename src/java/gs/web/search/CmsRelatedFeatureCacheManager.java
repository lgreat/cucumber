package gs.web.search;

import gs.data.content.cms.CmsCategory;

import java.util.List;

public interface CmsRelatedFeatureCacheManager {

    List<Long> findFeatureIds(CmsCategory category);

}
