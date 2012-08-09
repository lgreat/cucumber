package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsFeature;
import gs.data.search.SearchResultsPage;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmsRelatedFeatureCacheManagerEhcacheImpl implements CmsRelatedFeatureCacheManager, InitializingBean {

    private Cache _cache;
    private CmsFeatureSearchService _cmsFeatureSearchService;

    public List<Long> findFeatureIds(CmsCategory category) {
        Element element = _cache.get(category.getId());
        // object not in cache or expired
        if (element==null){
            List<Long> featureIds = findFeatureIdsFromSolr(category);
            Collections.shuffle(featureIds);
            element = new Element(category.getId(), featureIds);
            _cache.put(element);
        }
        return (List<Long>) element.getObjectValue();
    }

    private List<Long> findFeatureIdsFromSolr(CmsCategory cmsCategory) {
        List<Long> ids = new ArrayList();
        List<CmsCategory> categories = new ArrayList();
        categories.add(cmsCategory);
        CmsFeature.CmsCategoryGroup triplet = CmsFeature.separateCategories(categories);
        List<CmsCategory> topics = triplet.topics;
        List<CmsCategory> grades = triplet.grades;
        List<CmsCategory> subjects = triplet.subjects;
        List<CmsCategory> locations = triplet.locations;
        List<CmsCategory> outcomes = triplet.outcomes;

        SearchResultsPage<ICmsFeatureSearchResult> resultsPage = _cmsFeatureSearchService.getCmsFeatures(topics, grades, subjects, locations, outcomes, true, null, null, 5, 0);
        if (resultsPage.getSearchResults()!=null) {
            for (ICmsFeatureSearchResult features : resultsPage.getSearchResults()){
                ids.add(features.getContentId());
            }
        }
        return ids;
    }

    public Cache getCache() {
        return _cache;
    }

    public void setCache(Cache cache) {
        _cache = cache;
    }

    public CmsFeatureSearchService getCmsFeatureSearchService() {
        return _cmsFeatureSearchService;
    }

    public void setCmsFeatureSearchService(CmsFeatureSearchService cmsFeatureSearchService) {
        _cmsFeatureSearchService = cmsFeatureSearchService;
    }

    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = CacheManager.getInstance();
        _cache = cacheManager.getCache("gs.web.search.CmsRelatedFeatureCacheManager");
    }
}
