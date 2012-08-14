package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsCategoryDao;
import gs.data.content.related.RelatedCmsCategoryComparator;
import gs.data.content.related.SchoolCmsCategoryMapper;
import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.CommonFields;

import java.util.*;

/**
 * Get the related content for Schools from the cms content in Solr
 */
public class CmsRelatedFeatureSearchServiceSolrImpl implements CmsRelatedFeatureSearchService {

    private static final RelatedCmsCategoryComparator COMPARATOR = new RelatedCmsCategoryComparator();

    private SchoolCmsCategoryMapper _schoolCmsCategoryMapper;
    private CmsFeatureSearchService _cmsFeatureSearchService;
    private CmsRelatedFeatureCacheManager _cmsRelatedFeatureCacheManager;
    private GsSolrSearcher _gsSolrSearcher;

    /**
     * Get the related features from solr based on the school, esp responses and the
     * maximum number of rows
     * @param school
     * @param espResponses
     * @param rows
     * @return java.util.List
     */
    public List<ICmsFeatureSearchResult> getRelatedFeatures(School school, Map<String, List<EspResponse>> espResponses, int rows) {

        Set<CmsCategory> categories = new HashSet<CmsCategory>();
        // get all the cms categories related to a school
        categories.addAll(_schoolCmsCategoryMapper.related(school));

        // get all the cms categories related to esp responses
        if (espResponses!=null && !espResponses.isEmpty()){
            for (String key : espResponses.keySet()){
                List<EspResponse> responses = espResponses.get(key);
                if (responses!=null && !responses.isEmpty()){
                    for ( EspResponse response : responses ) {
                        categories.addAll(_schoolCmsCategoryMapper.related(response));
                    }
                }
            }
        }

        // only when we actually have related categories
        if (!categories.isEmpty()) {

            // create a list from the set so we can sort based on prioritization
            List<CmsCategory> categoryList = new ArrayList<CmsCategory>(categories);
            Collections.sort(categoryList, COMPARATOR);

            // return ICmsFeatureSearchResult object based on the prioritized list
            // of cms categories
            return populate(categoryList, rows);
        }

        return new ArrayList();
    }

    /**
     * Create the list of CMS Content according to the list of cms categories
     * @param categories
     * @param rows
     * @return java.util.List
     */
    private List<ICmsFeatureSearchResult> populate(List<CmsCategory> categories, int rows) {
        long start = System.currentTimeMillis();
        if (categories==null || categories.isEmpty()){
            return new ArrayList<ICmsFeatureSearchResult>();
        }

        List<Long> features = new ArrayList<Long>();
        fill(categories, features, rows);
        pad(features, (rows-features.size()));

        return content(features);
    }

    /**
     * populate the features id with the articles or videos
     * that match based on the categories
     * @param categories
     * @param features
     * @param rows
     */
    private void fill(List<CmsCategory> categories, List<Long> features, int rows){

        if (categories!=null && features.size()<rows) {
            // store a queue of potential stories for each cms category
            HashMap<CmsCategory, LinkedList<Long>> content = new HashMap<CmsCategory, LinkedList<Long>>();


            // loop until features has the correct number
            // of stories or all article queues are empty
            boolean allEmpty = false;

            while (features.size()<rows && !allEmpty){
                allEmpty = true;
                for (CmsCategory category : categories) {
                    // get the next article for this category if one exists
                    Long nextContentId = next(category, content);

                    // if this article is already in the list, don't include
                    // it twice
                    if (features.contains(nextContentId)) {
                        boolean found = false;
                        while (nextContentId!=null && !found) {
                            nextContentId = next(category, content);
                            if (nextContentId!=null && !features.contains(nextContentId)){
                                found = true;
                            }
                        }
                    }

                    // if we found another article then we can
                    // continue to add more articles
                    if (nextContentId!=null) {
                        allEmpty = false;
                        features.add(nextContentId);
                        if (features.size()>=rows){
                            break;
                        }
                    }
                }
            }

        }
    }

    /**
     * find the next potential article from a particular cms category or return null if
     * there are no longer any articles left from this category.
     * @param category
     * @param content
     * @return ICmsFeatureSearchResult
     */
    private Long next(CmsCategory category, HashMap<CmsCategory, LinkedList<Long>> content) {

        // if content doesn't contain key yet, pull a few stories
        // to populate the queue
        if (!content.containsKey(category)){
            content.put(category, new LinkedList<Long>(_cmsRelatedFeatureCacheManager.findFeatureIds(category)));
        }

        // if the content is empty, then return null
        if (content.get(category).isEmpty()) {
            return null;
        }

        // otherwise return next story in the queue
        return content.get(category).remove();
    }

    /**
     * Get the content from Solr based on cms category id
     * @param ids
     * @return
     */
    private List<ICmsFeatureSearchResult> content(List<Long> ids) {
        if (!ids.isEmpty()){
            List<String> idList = new ArrayList<String>();
            for (Long id : ids){
                idList.add(String.valueOf(id));
            }
            GsSolrQuery solrQuery = new GsSolrQuery()
                .filter(CmsFeatureFields.FIELD_CONTENT_ID, idList)
                .addQuery(CommonFields.DOCUMENT_TYPE, "cms_feature")
                .build();
            List<SolrCmsFeatureSearchResult> results = _gsSolrSearcher.simpleSearch(solrQuery, SolrCmsFeatureSearchResult.class);
            List<ICmsFeatureSearchResult> ordered = new ArrayList<ICmsFeatureSearchResult>();

            // reorder the content since solr will return it in
            // no particular order
            for (Long id : ids) {
                for (SolrCmsFeatureSearchResult result : results) {
                    if (id.equals(result.getContentId())){
                        ordered.add(result);
                    }
                }
            }
            return ordered;

        }
        return new LinkedList<ICmsFeatureSearchResult>();
    }

    /**
     * Method will take the list of features and will add generic stories to
     * ensure that there is always content for featured stories
     * @param features
     * @param rows
     */
    private void pad(List<Long> features, int rows) {
        if (rows>0){
            List<CmsCategory> categories = _cmsRelatedFeatureCacheManager.findPaddingCategories();
            fill(categories, features, rows);
        }
    }

    public SchoolCmsCategoryMapper getSchoolCmsCategoryMapper() {
        return _schoolCmsCategoryMapper;
    }

    public void setSchoolCmsCategoryMapper(SchoolCmsCategoryMapper schoolCmsCategoryMapper) {
        _schoolCmsCategoryMapper = schoolCmsCategoryMapper;
    }

    public CmsFeatureSearchService getCmsFeatureSearchService() {
        return _cmsFeatureSearchService;
    }

    public void setCmsFeatureSearchService(CmsFeatureSearchService cmsFeatureSearchService) {
        _cmsFeatureSearchService = cmsFeatureSearchService;
    }

    public CmsRelatedFeatureCacheManager getCmsRelatedFeatureCacheManager() {
        return _cmsRelatedFeatureCacheManager;
    }

    public void setCmsRelatedFeatureCacheManager(CmsRelatedFeatureCacheManager cmsRelatedFeatureCacheManager) {
        _cmsRelatedFeatureCacheManager = cmsRelatedFeatureCacheManager;
    }

    public void setGsSolrSearcher(GsSolrSearcher gsSolrSearcher) {
        _gsSolrSearcher = gsSolrSearcher;
    }
}
