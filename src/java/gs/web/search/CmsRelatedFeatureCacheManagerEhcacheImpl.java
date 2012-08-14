package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsCategoryDao;
import gs.data.content.cms.CmsFeature;
import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.SearchResultsPage;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.CommonFields;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmsRelatedFeatureCacheManagerEhcacheImpl implements CmsRelatedFeatureCacheManager, InitializingBean {

    private static final String CONTENT_TYPE_ARTICLE = "Article";
    private static final String CONTENT_TYPE_VIDEO = "Video";

    private static final Logger _log = LoggerFactory.getLogger(CmsRelatedFeatureCacheManagerEhcacheImpl.class);

    private Cache _cache;
    private CmsCategoryDao _cmsCategoryDao;
    private CmsFeatureSearchService _cmsFeatureSearchService;
    private GsSolrSearcher _gsSolrSearcher;

    /**
     * refresh the caches with a new set of randomly
     * shuffled article/video id's
     */
    public void refresh(){
        _log.info("refreshing CMS related features cache");
        long start = System.currentTimeMillis();
        List<CmsCategory> categories = _cmsCategoryDao.getAllCmsCategories();
        for (CmsCategory category : categories) {
            List<Long> featureIds = findFeatureIdsFromSolr(category);
            Element element = new Element(category.getId(), featureIds);
            _cache.put(element);
        }
        _log.info("finished refreshing CMS related features cache in " + (System.currentTimeMillis()-start) + "ms");
    }


    public List<Long> findFeatureIds(CmsCategory category) {
        Element element = _cache.get(category.getId());
        // object not in cache or expired
        if (element==null){
            long start = System.currentTimeMillis();
            List<Long> featureIds = findFeatureIdsFromSolr(category);
            element = new Element(category.getId(), featureIds);
            _cache.put(element);
            _log.warn("Cms related feature cache miss for cms category id " + category.getId() + ".  Loaded cache in " + (System.currentTimeMillis()-start) + "ms");
        }
        return (List<Long>) element.getObjectValue();
    }

    public List<CmsCategory> findPaddingCategories() {
        Element element = _cache.get("padding");
        if (element==null) {
            long start = System.currentTimeMillis();
            List<CmsCategory> categories = _cmsCategoryDao.getCmsCategoriesFromIds(new Long[]{405l});
            element = new Element("padding", categories);
            _cache.put(element);
            _log.warn("Cms related feature cache miss for padding categories.  Lookup took " + (System.currentTimeMillis() - start) + "ms");
        }
        return (List<CmsCategory>) element.getObjectValue();
    }

    /**
     * Find the CMS Features from Solr based on the CMS category id
     * @param cmsCategory
     * @return
     */
    private List<Long> findFeatureIdsFromSolr(CmsCategory cmsCategory) {

        GsSolrQuery solrQuery = new GsSolrQuery()
                .addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, String.valueOf(cmsCategory.getId()))
                .addQuery(CmsFeatureFields.FIELD_CONTENT_TYPE, CONTENT_TYPE_ARTICLE + " OR " + CONTENT_TYPE_VIDEO)
                .addQuery(CommonFields.DOCUMENT_TYPE, "cms_feature")
                .build();

        List<Long> articles = new ArrayList<Long>();
        List<Long> videos = new ArrayList<Long>();

        List<SolrCmsFeatureSearchResult> results = _gsSolrSearcher.simpleSearch(solrQuery, SolrCmsFeatureSearchResult.class);
        if (results!=null) {
            for (SolrCmsFeatureSearchResult result : results) {
                if (result.getContentType().equals(CONTENT_TYPE_VIDEO)){
                    videos.add(result.getContentId());
                } else {
                    articles.add(result.getContentId());
                }
            }
        }

        // shuffle articles and video ids
        // but always return videos higher
        Collections.shuffle(articles);
        Collections.shuffle(videos);
        List<Long> combined = new ArrayList<Long>();
        combined.addAll(videos);
        combined.addAll(articles);

        return combined;
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

    public void setCmsCategoryDao(CmsCategoryDao cmsCategoryDao) {
        _cmsCategoryDao = cmsCategoryDao;
    }

    public GsSolrSearcher getGsSolrSearcher() {
        return _gsSolrSearcher;
    }

    public void setGsSolrSearcher(GsSolrSearcher gsSolrSearcher) {
        _gsSolrSearcher = gsSolrSearcher;
    }

    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = CacheManager.getInstance();
        _cache = cacheManager.getCache("gs.web.search.CmsRelatedFeatureCacheManager");
    }
}
