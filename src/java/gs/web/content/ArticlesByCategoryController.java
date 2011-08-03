package gs.web.content;

import gs.data.content.ArticleCategory;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.ICmsCategoryDao;
import gs.data.search.SearchResultsPage;
import gs.data.util.CmsUtil;
import gs.web.content.cms.CmsContentUtils;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ArticlesByCategoryController extends AbstractController {
    private static final Log _log = LogFactory.getLog(ArticlesByCategoryController.class);

    protected static final String MODEL_SUBCATEGORY = "subcategory";
    protected static final String MODEL_PAGE = "p";
    protected static final String MODEL_PAGE_SIZE = "pageSize";
    protected static final String MODEL_RESULTS = "mainResults";
    protected static final String MODEL_TOTAL_HITS = "total";
    protected static final String MODEL_ISA_LD_CATEGORY = "isAnLDCategory";
    protected static final String MODEL_CATEGORY = "category";
    protected static final String MODEL_CATEGORIES = "categories";
    protected static final String MODEL_TOPICS = "topics";
    protected static final String MODEL_GRADES = "grades";
    protected static final String MODEL_SUBJECTS = "subjects";
    protected static final String MODEL_LOCATIONS = "locations";
    protected static final String MODEL_OUTCOMES = "outcomes";
    protected static final String MODEL_TOPIC_IDS = "topicIds";
    protected static final String MODEL_GRADE_IDS = "gradeIds";
    protected static final String MODEL_SUBJECT_IDS = "subjectIds";
    protected static final String MODEL_LOCATION_IDS = "locationIds";
    protected static final String MODEL_OUTCOME_IDS = "outcomeIds";
    protected static final String MODEL_MAX_RESULTS = "maxResults";
    protected static final String MODEL_BREADCRUMBS = "breadcrumbs";
    protected static final String MODEL_STYLE = "style";
    protected static final String MODEL_ALMOND_NET_CATEGORY = "almondNetCategory";

    /** Page number */
    public static final String PARAM_PAGE = "p";
    /** Allow override of category id */
    public static final String PARAM_ID = "id";

    // CMS features only:
    /** CMS topic IDs, comma-separated */
    public static final String PARAM_TOPICS = "topics";
    /** CMS grade IDs, comma-separated */
    public static final String PARAM_GRADES = "grades";
    /** CMS subject IDs, comma-separated */
    public static final String PARAM_SUBJECTS = "subjects";
    /** CMS subject IDs, comma-separated */
    public static final String PARAM_LOCATIONS = "locations";
    /** CMS subject IDs, comma-separated */
    public static final String PARAM_OUTCOMES = "outcomes";
    /** CMS feature type (i.e. article or askTheExperts) to exclude - must be used with PARAM_EXCLUDE_CONTENT_ID */
    public static final String PARAM_EXCLUDE_TYPE = "excludeType";
    /** CMS content identifier (i.e. ID number of article or askTheExperts) to exclude - must be used with PARAM_EXCLUDE_TYPE*/
    public static final String PARAM_EXCLUDE_CONTENT_ID = "excludeContentId";
    /** Whether to apply strict rules for matching, i.e. just on primary category, not on secondary category */
    public static final String PARAM_STRICT = "strict";
    /** Language (e.g. "ES" or "EN") to limit matches to */
    public static final String PARAM_LANGUAGE = "language";
    /** Style information (e.g. "Fall 2009") to decide how to present the view */
    public static final String PARAM_STYLE = "style";
    /** Max number of results to show. Optional, but if specified, it overrides the number from spring config (modules/pages-servlet.xml). */
    public static final String PARAM_MAX_RESULTS = "maxResults";

    /** Results per page */
    public static final int PAGE_SIZE = 10;

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";
    public static final String GAM_AD_ATTRIBUTE_KEY_CATEGORY_ID = "category_id";

    private IArticleCategoryDao _articleCategoryDao;
    private ICmsCategoryDao _cmsCategoryDao;
    /** Whether to look up the subcategory's parent categories */
    private boolean _getParents = false;
    private String _viewName;
    private boolean _showAdTargeting;
    private boolean _randomResults = false;
    private int _maxResults = -1;

    private CmsFeatureSearchService _cmsFeatureSearchService;


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String categoryId = null;
        String requestUri = request.getRequestURI().
                replaceAll("/gs-web", "").
                replaceAll("/articles/", "");
        String[] rs = StringUtils.split(requestUri, "/");
        if (rs.length >= 1) {
            categoryId = rs[0];
        }
        Map<String, Object> model;

        int page = 1;
        int offset = 1;
        // check for page number
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                page = Integer.parseInt(p);
                offset = gs.data.pagination.Pagination.getOffset(PAGE_SIZE, page);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }

        CmsCategory category = _cmsCategoryDao.getCmsCategoryFromURI(request.getRequestURI());
            if (category != null && CmsCategory.TYPE_TOPIC.equals(category.getType())) {
                UrlBuilder redirectUrlBuilder =
                        new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, String.valueOf(category.getId()), (String)null, (String)null, (String)null, (String)null, request.getParameter("language"));
                return new ModelAndView(new RedirectView301(redirectUrlBuilder.asSiteRelative(request)));
            }

            // GS-9397 handle old Campaigns and LD browse urls
            UrlBuilder redirectUrlBuilder = ArticlesByCategoryController.getGs9397RedirectUrlBuilder(
                    request.getParameter(PARAM_TOPICS),
                    request.getParameter(PARAM_GRADES),
                    request.getParameter(PARAM_SUBJECTS),
                    request.getParameter(PARAM_LANGUAGE));
            if (redirectUrlBuilder != null) {
                return new ModelAndView(new RedirectView301(redirectUrlBuilder.asSiteRelative(request)));
            }

            model = handleCmsCategoryRequest(request, offset);
            List<CmsCategory> categories = (List<CmsCategory>)model.get(MODEL_CATEGORIES);
            if (isShowAdTargeting()) {
                setAdTargetingForCmsCategories(request, categories);
            }

        model.put(MODEL_PAGE, page);
        model.put(PARAM_LANGUAGE, request.getParameter(PARAM_LANGUAGE));
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE); // results per page
        model.put(MODEL_ISA_LD_CATEGORY,isAnLDCategory(request.getRequestURI()));

        return new ModelAndView(_viewName, model);
    }


    final private static Map<String,String> GS_9397_RECATEGORIZATION = new HashMap<String,String>();
    static {
        GS_9397_RECATEGORIZATION.put("146","136");
        GS_9397_RECATEGORIZATION.put("196","162");
        GS_9397_RECATEGORIZATION.put("135","134");
        GS_9397_RECATEGORIZATION.put("137","134");
        GS_9397_RECATEGORIZATION.put("138","219");
        GS_9397_RECATEGORIZATION.put("139","219");
        GS_9397_RECATEGORIZATION.put("147","220");
        GS_9397_RECATEGORIZATION.put("130","225");
        GS_9397_RECATEGORIZATION.put("169","168");
        GS_9397_RECATEGORIZATION.put("164","185");
        GS_9397_RECATEGORIZATION.put("186","185");
        GS_9397_RECATEGORIZATION.put("178","185");
        GS_9397_RECATEGORIZATION.put("187","225");
        GS_9397_RECATEGORIZATION.put("183","225");
        GS_9397_RECATEGORIZATION.put("171","225");
        GS_9397_RECATEGORIZATION.put("131","225");
        GS_9397_RECATEGORIZATION.put("182","225");
        GS_9397_RECATEGORIZATION.put("172","225");
        GS_9397_RECATEGORIZATION.put("176","226");
        GS_9397_RECATEGORIZATION.put("180","226");
        GS_9397_RECATEGORIZATION.put("193","226");
        GS_9397_RECATEGORIZATION.put("174","226");
        GS_9397_RECATEGORIZATION.put("163","228");
        GS_9397_RECATEGORIZATION.put("191","231");
        GS_9397_RECATEGORIZATION.put("132","234");
    };

    protected void setAdTargetingForCmsCategories(HttpServletRequest request, List<CmsCategory> categories) {
        if (categories == null) {
            return;
        }

        Set<String> categoryNames = new HashSet<String>();

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        for (CmsCategory category : categories) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY_CATEGORY_ID, String.valueOf(category.getId()));
            categoryNames.add(category.getName());
            for (CmsCategory breadcrumb : getCmsCategoryBreadcrumbs(category)) {
                categoryNames.add(breadcrumb.getName());
            }
        }

        for (String categoryName : categoryNames) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, categoryName);
        }
    }

    /**
     * Take the topic, grade, subject, and language request parameters, and return a UrlBuilder to redirect to
     * if any of the topic IDs specified in paramTopics is among those that require redirects to other topic IDs,
     * as specified by GS-9397 category_redirects.xlsx. If the specified topics do not include any of those in the
     * "Old category ID" column in that spreadsheet, return null; no redirect is necessary. The parameters besides
     * paramTopics are used solely for constructing the redirect UrlBuilder; they are neither inspected nor transformed
     * in any way. Ignores page numbers and other parameters; only inspects topic IDs.
     * @param paramTopics comma-separated list of topic IDs
     * @param paramGrades comma-separated list of grade IDs
     * @param paramSubjects comma-separated list of subject IDs
     * @param paramLanguage two-character language code, e.g "EN"
     * @return
     */
    private static UrlBuilder getGs9397RedirectUrlBuilder(String paramTopics, String paramGrades, String paramSubjects, String paramLanguage) {
        boolean needsRedirect = false;
        Set<String> newTopicIds = new HashSet<String>();
        if (paramTopics != null) {
            for (String topicId : paramTopics.split(",")) {
                if (GS_9397_RECATEGORIZATION.containsKey(topicId)) {
                    needsRedirect = true;
                    newTopicIds.add(GS_9397_RECATEGORIZATION.get(topicId));
                } else {
                    newTopicIds.add(topicId);
                }
            }

            if (needsRedirect) {
                return new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, StringUtils.join(newTopicIds, ","), paramGrades, paramSubjects, null, null, paramLanguage);
            }
        }
        return null;
    }

    /**
     * Searches the indexes for CMS features tagged with a particular CMS category.
     */
    protected Map<String, Object> handleCmsCategoryRequest(HttpServletRequest request, int offset) {
        Map<String, Object> model = new HashMap<String, Object>();
        String language = request.getParameter(PARAM_LANGUAGE);

        boolean strict = false;
        ContentKey excludeContentKey = null;

        String excludeType = request.getParameter(PARAM_EXCLUDE_TYPE);
        String excludeContentId = request.getParameter(PARAM_EXCLUDE_CONTENT_ID);
        String strictStr = request.getParameter(PARAM_STRICT);

        if (StringUtils.isNotBlank(strictStr)) {
            strict = Boolean.parseBoolean(strictStr);
        }
        if (StringUtils.isNotBlank(excludeType) && StringUtils.isNotBlank(excludeContentId)) {
            excludeContentKey = new ContentKey();
            excludeContentKey.setType(excludeType);
            excludeContentKey.setIdentifier(Long.parseLong(excludeContentId));
        }

        String topicsParam = request.getParameter(PARAM_TOPICS);
        String gradesParam = request.getParameter(PARAM_GRADES);
        String subjectsParam = request.getParameter(PARAM_SUBJECTS);
        String locationsParam = request.getParameter(PARAM_LOCATIONS);
        String outcomesParam = request.getParameter(PARAM_OUTCOMES);

        Long[] topicCategoryIds = new Long[0];
        Long[] gradeCategoryIds = new Long[0];
        Long[] subjectCategoryIds = new Long[0];
        Long[] locationCategoryIds = new Long[0];
        Long[] outcomeCategoryIds = new Long[0];

        if (!StringUtils.isEmpty(topicsParam)) {
            topicCategoryIds = gs.data.util.string.StringUtils.stringOfSeparatedLongsToLongArray(topicsParam);
        }
        if (!StringUtils.isEmpty(gradesParam)) {
            gradeCategoryIds = gs.data.util.string.StringUtils.stringOfSeparatedLongsToLongArray(gradesParam);
        }
        if (!StringUtils.isEmpty(subjectsParam)) {
            subjectCategoryIds = gs.data.util.string.StringUtils.stringOfSeparatedLongsToLongArray(subjectsParam);
        }
        if (!StringUtils.isEmpty(locationsParam)) {
            locationCategoryIds = gs.data.util.string.StringUtils.stringOfSeparatedLongsToLongArray(locationsParam);
        }
        if (!StringUtils.isEmpty(outcomesParam)) {
            outcomeCategoryIds = gs.data.util.string.StringUtils.stringOfSeparatedLongsToLongArray(outcomesParam);
        }

        Set<Long> allIds = new HashSet<Long>();
        CollectionUtils.addAll(allIds, topicCategoryIds);
        CollectionUtils.addAll(allIds, gradeCategoryIds);
        CollectionUtils.addAll(allIds, subjectCategoryIds);
        CollectionUtils.addAll(allIds, locationCategoryIds);
        CollectionUtils.addAll(allIds, outcomeCategoryIds);

        List<CmsCategory> categoryList = _cmsCategoryDao.getCmsCategoriesFromIds(allIds.toArray(new Long[0]));
        // TODO-11715 FIXME rename triplet
        CmsFeature.CmsCategoryGroup triplet = CmsFeature.separateCategories(categoryList);
        List<CmsCategory> topics = triplet.topics;
        List<CmsCategory> grades = triplet.grades;
        List<CmsCategory> subjects = triplet.subjects;
        List<CmsCategory> locations = triplet.locations;
        List<CmsCategory> outcomes = triplet.outcomes;

        String maxResultsParam = request.getParameter(PARAM_MAX_RESULTS);

        if (categoryList.size() > 0) {
            List<CmsCategory> categories = storeResultsForCmsCategories(topics, grades, subjects, locations, outcomes, model, offset, strict, excludeContentKey, language, maxResultsParam);

            if (categories.size() == 1) {
                List<CmsCategory> breadcrumbs = getCmsCategoryBreadcrumbs(categories.get(0));
                if (breadcrumbs.size() > 0) {
                    model.put(MODEL_BREADCRUMBS, CmsContentUtils.getBreadcrumbs(breadcrumbs, language, request));
                }
            }

            Set<CmsCategory> uniqueCategories = new HashSet<CmsCategory>();
            // breadcrumbs don't include the categories themselves!
            uniqueCategories.addAll(categories);
            // now add the breadcrumb categories
            for (CmsCategory category : categories) {
                uniqueCategories.addAll(getCmsCategoryBreadcrumbs(category));
            }
            model.put(MODEL_ALMOND_NET_CATEGORY, CmsContentUtils.getAlmondNetCategory(uniqueCategories));

            model.put(MODEL_TOPICS, topics);
            model.put(MODEL_GRADES, grades);
            model.put(MODEL_SUBJECTS, subjects);
            model.put(MODEL_LOCATIONS, locations);
            model.put(MODEL_OUTCOMES, outcomes);
            model.put(MODEL_CATEGORIES, categories);

            model.put(MODEL_TOPIC_IDS, CmsUtil.getCommaSeparatedCategoryIds(topics));
            model.put(MODEL_GRADE_IDS, CmsUtil.getCommaSeparatedCategoryIds(grades));
            model.put(MODEL_SUBJECT_IDS, CmsUtil.getCommaSeparatedCategoryIds(subjects));
            model.put(MODEL_LOCATION_IDS, CmsUtil.getCommaSeparatedCategoryIds(locations));
            model.put(MODEL_OUTCOME_IDS, CmsUtil.getCommaSeparatedCategoryIds(outcomes));

            if (categories.size() == 1) {
                model.put(MODEL_CATEGORY, categories.get(0));
            }

            model.put(MODEL_STYLE, request.getParameter(PARAM_STYLE));
        }

        return model;
    }

    /**
     * Creates a list of parent categories for specified category
     */
    protected List<CmsCategory> getCmsCategoryBreadcrumbs(CmsCategory category) {
        List<CmsCategory> breadcrumbs = new ArrayList<CmsCategory>();
        String currentUri = category.getFullUri();
        int indexOfLastSlash = currentUri.lastIndexOf("/");

        while (indexOfLastSlash > -1) {
            currentUri = currentUri.substring(0, currentUri.lastIndexOf("/"));
            CmsCategory currentCategory = _cmsCategoryDao.getCmsCategoryFromURI(currentUri);
            if (currentCategory != null) {
                breadcrumbs.add(0, currentCategory);
            }
            indexOfLastSlash = currentUri.lastIndexOf("/");
        }

        return breadcrumbs;
    }


    protected List<CmsCategory> storeResultsForCmsCategories(List<CmsCategory> topics, List<CmsCategory> grades, List<CmsCategory> subjects, List<CmsCategory> locations, List<CmsCategory> outcomes, Map<String, Object> model, int offset, boolean strict, ContentKey excludeContentKey, String language, String maxResultsParam) {

        List<CmsCategory> categories = new ArrayList<CmsCategory>();
        if (topics != null) {
            categories.addAll(topics);
        }
        if (grades != null) {
            categories.addAll(grades);
        }
        if (subjects != null) {
            categories.addAll(subjects);
        }
        if (locations != null) {
            categories.addAll(locations);
        }
        if (outcomes != null) {
            categories.addAll(outcomes);
        }

        // search for articles in the particular category
        CmsFeatureSearchService service = getSolrCmsFeatureSearchService();
        SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = service.getCmsFeatures(topics, grades, subjects, locations, outcomes, strict, excludeContentKey, language, PAGE_SIZE, offset);

        if (searchResultsPage != null && searchResultsPage.getTotalResults() > 0) {
            int totalResults = searchResultsPage.getTotalResults();
            model.put(MODEL_TOTAL_HITS, totalResults);
            int maxResults = getMaxResults();
            if (maxResultsParam != null) {
                try {
                    maxResults = Integer.parseInt(maxResultsParam);
                } catch (NumberFormatException e) {
                    // keep maxResults as value from spring config
                }
            }
            if (isRandomResults() && maxResults > 0) {
                model.put(MODEL_RESULTS, getRandomResults(searchResultsPage, maxResults));
                model.put(MODEL_MAX_RESULTS, maxResults);
            } else {
                model.put(MODEL_RESULTS, searchResultsPage.getSearchResults());
            }
        }

        return categories;
    }


    private static List<ICmsFeatureSearchResult> getRandomResults(SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage, int maxResults) {

        if (searchResultsPage == null || searchResultsPage.getSearchResults() == null
                || searchResultsPage.getSearchResults().size() == 0) {
            throw new IllegalArgumentException("Hits cannot be null or have 0 length");
        }

        if (maxResults <= 0) {
            throw new IllegalArgumentException("MaxResults must be 1 or higher");
        }

        int totalResults = searchResultsPage.getSearchResults().size();

        List<ICmsFeatureSearchResult> searchResults = new ArrayList<ICmsFeatureSearchResult>();
        Set<Integer> picked = new HashSet<Integer>();

        while (searchResults.size() < maxResults && searchResults.size() < totalResults) {
            Random rand = new Random();
            int n = rand.nextInt(totalResults);

            if (picked.contains(n)) {
                continue;
            }

            picked.add(n);
            searchResults.add((searchResultsPage.getSearchResults().get(n)));
        }

        return searchResults;
    }

    protected List<ArticleCategory> getCategoriesFromURI(String requestURI) {
        String categoryId = null;
        String requestUri = requestURI.replaceAll("/gs-web", "");
        requestUri = requestUri.replaceAll("/articles/", "");
        String[] rs = StringUtils.split(requestUri, "/");
        if (rs.length >= 1) {
            categoryId = rs[0];
        }
        if (categoryId != null && StringUtils.isNumeric(categoryId)) {
            return getCategoriesFromId(categoryId);
        } else {
            _log.error("Can't interpret first parameter as integer id: " + requestURI);
        }

        return null;
    }

    protected boolean isAnLDCategory(String requestURI) {

        String requestUri = requestURI.replaceAll("/gs-web", "");
        requestUri = requestUri.replaceAll("/articles/", "");
        String[] rs = StringUtils.split(requestUri, "/");
        if (rs.length >= 2) {
            if (rs[1].equals("LD")) {
                return true;
            }
        }
        return false;
    }

    protected List<ArticleCategory> getCategoriesFromId(String categoryId) {
        List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
        ArticleCategory category = _articleCategoryDao.getArticleCategory(Integer.valueOf(categoryId));
        if (category != null) {
            categories.add(category);

            // only proceed if the parent type looks valid
            // it doesn't seem this page needs to know what the parent categories are
            // I've disabled this loop for now, but can re-enable it by setting getParents to true
            if (_getParents && category.getParentType() != null &&
                    !StringUtils.equals(category.getType(), category.getParentType())) {
                // grab parent
                ArticleCategory parent = _articleCategoryDao.getArticleCategoryByType(category.getParentType());
                while (parent != null) {
                    // add parent to the list
                    categories.add(parent);
                    // if the parent's parent looks invalid, break out of the loop
                    if (StringUtils.equals(parent.getType(), parent.getParentType())) {
                        _log.warn("Category's type \"" + parent.getType() +
                                "\" equals parent \"" + parent.getParentType() + "\"");
                        break;
                    } else if (parent.getParentType() == null) {
                        break;
                    }
                    // otherwise grab the parent's parent
                    parent = _articleCategoryDao.getArticleCategoryByType(parent.getParentType());
                } // end while loop
            } // end if parentType != null
        } // end if category != null
        return categories;
    }

    public IArticleCategoryDao getArticleCategoryDao() {
        return _articleCategoryDao;
    }

    public void setArticleCategoryDao(IArticleCategoryDao articleCategoryDao) {
        _articleCategoryDao = articleCategoryDao;
    }

    public ICmsCategoryDao getCmsCategoryDao() {
        return _cmsCategoryDao;
    }

    public void setCmsCategoryDao(ICmsCategoryDao cmsCategoryDao) {
        _cmsCategoryDao = cmsCategoryDao;
    }

    public boolean isGetParents() {
        return _getParents;
    }

    public void setGetParents(boolean getParents) {
        _getParents = getParents;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getViewName() {
        return _viewName;
    }

    public boolean isShowAdTargeting() {
        return _showAdTargeting;
    }

    public void setShowAdTargeting(boolean showAdTargeting) {
        _showAdTargeting = showAdTargeting;
    }

    public boolean isRandomResults() {
        return _randomResults;
    }

    public void setRandomResults(boolean randomResults) {
        _randomResults = randomResults;
    }

    public int getMaxResults() {
        return _maxResults;
    }

    public void setMaxResults(int maxResults) {
        _maxResults = maxResults;
    }

    public CmsFeatureSearchService getSolrCmsFeatureSearchService() {
        return _cmsFeatureSearchService;
    }
    public void setSolrCmsFeatureSearchService(CmsFeatureSearchService cmsFeatureSearchService) {
        _cmsFeatureSearchService = cmsFeatureSearchService;
    }

}