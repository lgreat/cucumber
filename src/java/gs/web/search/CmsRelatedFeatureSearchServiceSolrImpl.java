package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.related.RelatedCmsCategoryComparator;
import gs.data.content.related.SchoolCmsCategoryMapper;
import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.search.SearchResultsPage;

import java.util.*;

/**
 * Get the related content for Schools from the cms content in Solr
 */
public class CmsRelatedFeatureSearchServiceSolrImpl implements CmsRelatedFeatureSearchService {

    private static final RelatedCmsCategoryComparator COMPARATOR = new RelatedCmsCategoryComparator();

    private SchoolCmsCategoryMapper _schoolCmsCategoryMapper;
    private CmsFeatureSearchService _cmsFeatureSearchService;

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

        if (categories==null || categories.isEmpty()){
            return new ArrayList<ICmsFeatureSearchResult>();
        }
        // store a list of all the results
        List<ICmsFeatureSearchResult> features = new ArrayList<ICmsFeatureSearchResult>();

        // store a queue of potential stories for each cms category
        HashMap<CmsCategory, LinkedList<ICmsFeatureSearchResult>> content = new HashMap<CmsCategory, LinkedList<ICmsFeatureSearchResult>>();

        // loop until features has the correct number
        // of stories or all article queues are empty
        boolean allEmpty = false;

        while (features.size()<rows && !allEmpty){
            allEmpty = true;
            for (CmsCategory category : categories) {
                // get the next article for this category if one exists
                ICmsFeatureSearchResult result = next(category, content, rows);
                if (result!=null) {
                    allEmpty = false;
                    features.add(result);
                    if (features.size()>=rows){
                        break;
                    }
                }
            }
        }

        // if the features are not the correct size, pad with general features
        if (features.size()<rows){
            pad(features, rows - features.size());
        }
        return features;
    }

    /**
     * find the next potential article from a particular cms category or return null if
     * there are no longer any articles left from this category.
     * @param category
     * @param content
     * @param rows
     * @return ICmsFeatureSearchResult
     */
    private ICmsFeatureSearchResult next(CmsCategory category, HashMap<CmsCategory, LinkedList<ICmsFeatureSearchResult>> content, int rows) {

        // if content doesn't contain key yet, pull a few stories
        // to populate the queue
        if (!content.containsKey(category)){
            content.put(category, content(category, rows));
        }

        // if the content is empty, then return null
        if (content.get(category).isEmpty()) {
            return null;
        }

        // otherwise return next story in the queue
        return content.get(category).pop();
    }

    /**
     * Get the content from Solr based on cms category id
     * @param cmsCategory
     * @param rows
     * @return
     */
    private LinkedList<ICmsFeatureSearchResult> content(CmsCategory cmsCategory, int rows){

        // TODO: need to add caching layer to prevent too many calls to SOLR
        SearchResultsPage resultsPage = _cmsFeatureSearchService.getCmsFeaturesByType(Arrays.asList(new CmsCategory[]{cmsCategory}), "Article", rows, 0);
        if (!resultsPage.getSearchResults().isEmpty()){
            return new LinkedList<ICmsFeatureSearchResult>(resultsPage.getSearchResults());
        }
        return new LinkedList<ICmsFeatureSearchResult>();
    }

    /**
     * Method will take the list of features and will add generic stories to
     * ensure that there is always content for featured stories
     * @param features
     * @param rows
     */
    private void pad(List<ICmsFeatureSearchResult> features, int rows) {
        // TODO: need to pad with generic content (cms category 405 according to specs)
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
}
