package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;

import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.ICmsCategoryDao;
import gs.data.search.Searcher;
import gs.data.search.Indexer;
import gs.data.search.GSAnalyzer;
import gs.data.util.CmsUtil;
import gs.web.search.ResultsPager;
import gs.data.search.SearchResult;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.web.content.cms.CmsContentUtils;

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
    protected static final String MODEL_TOPIC_IDS = "topicIds";
    protected static final String MODEL_GRADE_IDS = "gradeIds";
    protected static final String MODEL_SUBJECT_IDS = "subjectIds";
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

    private Searcher _searcher;
    private IArticleCategoryDao _articleCategoryDao;
    private ICmsCategoryDao _cmsCategoryDao;
    /** Whether to look up the subcategory's parent categories */
    private boolean _getParents = false;
    private String _viewName;
    private boolean _showAdTargeting;
    private boolean _randomResults = false;
    private int _maxResults = -1;

    // GS-7210: Used to boost articles by relevance.
    private QueryParser _titleParser;

    public ArticlesByCategoryController() {
        _titleParser = new QueryParser(Indexer.ARTICLE_TITLE, new GSAnalyzer());
        _titleParser.setDefaultOperator(QueryParser.Operator.OR);
    }

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
        // check for page number
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }

        if (categoryId != null && StringUtils.isNumeric(categoryId)) {
            // legacy category

            // GS-8660
            if (GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.containsKey(categoryId)) {
                return new ModelAndView(new RedirectView301(
                    GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.get(categoryId).asSiteRelative(request)));
            }

            model = handleLegacyCategoryRequest(request, page);
        } else {
            // cms category

            // GS-8546 - handle old-style browse urls
            CmsCategory category = _cmsCategoryDao.getCmsCategoryFromURI(request.getRequestURI());
            if (category != null && CmsCategory.TYPE_TOPIC.equals(category.getType())) {
                UrlBuilder redirectUrlBuilder =
                        new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, String.valueOf(category.getId()), (String)null, (String)null, request.getParameter("language"));
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

            model = handleCmsCategoryRequest(request, page);
            List<CmsCategory> categories = (List<CmsCategory>)model.get(MODEL_CATEGORIES);
            if (isShowAdTargeting()) {
                setAdTargetingForCmsCategories(request, categories);
            }
        }

        model.put(MODEL_PAGE, page);
        model.put(PARAM_LANGUAGE, request.getParameter(PARAM_LANGUAGE));
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE); // results per page
        model.put(MODEL_ISA_LD_CATEGORY,isAnLDCategory(request.getRequestURI()));

        return new ModelAndView(_viewName, model);
    }

    /**
     * Map from legacy category IDs to CMS grade IDs for grade-level categories
     */
    final private static Map<String,UrlBuilder> GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP = new HashMap<String,UrlBuilder>();
    static {
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("103", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "198", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("33", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "199", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("32", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "200", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("34", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "201", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("35", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "202", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("52", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "203", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("53", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "204", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("54", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "205", null, "EN"));
        GRADE_LEVEL_OLD_ID_NEW_URLBUILDER_MAP.put("104", new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, null, "206", null, "EN"));
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
                return new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, StringUtils.join(newTopicIds, ","), paramGrades, paramSubjects, paramLanguage);
            }
        }
        return null;
    }

    /**
     * Searches the indexes for CMS features tagged with a particular CMS category.
     */
    protected Map<String, Object> handleCmsCategoryRequest(HttpServletRequest request, int page) {
        Map<String, Object> model = new HashMap<String, Object>();
        String language = request.getParameter(PARAM_LANGUAGE);

        boolean strict = false;
        ContentKey excludeContentKey = null;

        String type = request.getParameter(PARAM_EXCLUDE_TYPE);
        String contentId = request.getParameter(PARAM_EXCLUDE_CONTENT_ID);
        String strictStr = request.getParameter(PARAM_STRICT);

        if (StringUtils.isNotBlank(strictStr)) {
            strict = Boolean.parseBoolean(strictStr);
        }
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(contentId)) {
            excludeContentKey = new ContentKey();
            excludeContentKey.setType(type);
            excludeContentKey.setIdentifier(Long.parseLong(contentId));
        }

        List<CmsCategory> topics = _cmsCategoryDao.getCmsCategoriesFromIds(request.getParameter(PARAM_TOPICS));
        List<CmsCategory> grades = _cmsCategoryDao.getCmsCategoriesFromIds(request.getParameter(PARAM_GRADES));
        List<CmsCategory> subjects = _cmsCategoryDao.getCmsCategoriesFromIds(request.getParameter(PARAM_SUBJECTS));


        String maxResultsParam = request.getParameter(PARAM_MAX_RESULTS);

        if (topics.size() > 0 || grades.size() > 0 || subjects.size() > 0) {
            List<CmsCategory> categories = storeResultsForCmsCategories(topics, grades, subjects, model, page, strict, excludeContentKey, language, maxResultsParam);

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
            model.put(MODEL_CATEGORIES, categories);

            model.put(MODEL_TOPIC_IDS, CmsUtil.getCommaSeparatedCategoryIds(topics));
            model.put(MODEL_GRADE_IDS, CmsUtil.getCommaSeparatedCategoryIds(grades));
            model.put(MODEL_SUBJECT_IDS, CmsUtil.getCommaSeparatedCategoryIds(subjects));

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

    /**
     * Potentially populates MODEL_TOTAL_HITS and MODEL_RESULTS with the results of the search. If no results,
     * will not populate those variables.
     */
    protected List<CmsCategory> storeResultsForCmsCategories(List<CmsCategory> topics, List<CmsCategory> grades, List<CmsCategory> subjects, Map<String, Object> model, int page, boolean strict, ContentKey excludeContentKey, String language, String maxResultsParam) {
        // set up search query
        // articles should be in the particular category
        BooleanQuery bq = new BooleanQuery();

        for (CmsCategory category : topics) {
            BooleanQuery innerBq = new BooleanQuery();
            TermQuery primaryCategoryTerm = new TermQuery(
                    new Term(Indexer.CMS_PRIMARY_CATEGORY_ID, String.valueOf(category.getId())));
            // set boost on term to give primary category precedence
            primaryCategoryTerm.setBoost(99f); // default boost is 1.0
            innerBq.add(primaryCategoryTerm, BooleanClause.Occur.SHOULD);

            if (!strict) {
                // OR articles can just be tagged with the particular category
                TermQuery secondaryCategoryTerm = new TermQuery(
                        new Term(Indexer.CMS_TOPIC_ID, String.valueOf(category.getId())));
                innerBq.add(secondaryCategoryTerm, BooleanClause.Occur.SHOULD);
            }

            bq.add(innerBq, BooleanClause.Occur.MUST);
        }

        for (CmsCategory category : grades) {
            TermQuery term = new TermQuery(
                    new Term(Indexer.CMS_GRADE_ID, String.valueOf(category.getId())));
            bq.add(term, BooleanClause.Occur.MUST);
        }

        for (CmsCategory category : subjects) {
            TermQuery term = new TermQuery(
                    new Term(Indexer.CMS_SUBJECT_ID, String.valueOf(category.getId())));
            bq.add(term, BooleanClause.Occur.MUST);
        }

        if (excludeContentKey != null) {
            TermQuery excludeContentKeyTerm = new TermQuery(
                    new Term(Indexer.ID, excludeContentKey.toString()));
            bq.add(excludeContentKeyTerm, BooleanClause.Occur.MUST_NOT);
        }

        if (language != null) {
            TermQuery languageTerm = new TermQuery(
                    new Term(Indexer.LANGUAGE, language));
            bq.add(languageTerm, BooleanClause.Occur.MUST);
        }

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

        // This should give higher placement to articles with terms from the category name in their title
        for (CmsCategory category : categories) {
            String typeDisplay = category.getName();
            if (StringUtils.isNotBlank(typeDisplay)) {
                try {
                    Query titleQuery = _titleParser.parse(typeDisplay);
                    bq.add(titleQuery, BooleanClause.Occur.SHOULD);
                } catch (ParseException pe) {
                    _log.warn("Couldn't parse article category.", pe);
                }
            }
        }

        // Filter to only CMS features
        Filter typeFilter;
        typeFilter = new CachingWrapperFilter(new QueryFilter(new TermQuery(
                    new Term("type", Indexer.DOCUMENT_TYPE_CMS_FEATURE))));

        // execute search
        Hits hits = _searcher.search(bq, null, null, typeFilter);

        // pass hits object through ResultsPager to paginate and stuff in model as MODEL_RESULTS
        if (hits != null && hits.length() > 0) {
            model.put(MODEL_TOTAL_HITS, hits.length());
            ResultsPager resultsPager = new ResultsPager(hits, ResultsPager.ResultType.topic);

            // allow parameter override of maxResults from spring config
            int maxResults = getMaxResults();
            if (maxResultsParam != null) {
                try {
                    maxResults = Integer.parseInt(maxResultsParam);
                } catch (NumberFormatException e) {
                    // keep maxResults as value from spring config
                }
            }

            if (isRandomResults() && maxResults > 0) {
                model.put(MODEL_RESULTS, getRandomResults(hits, maxResults));
                model.put(MODEL_MAX_RESULTS, maxResults);
            } else {
                model.put(MODEL_RESULTS, resultsPager.getResults(page, PAGE_SIZE));
            }
        }

        return categories;
    }

    private static List<SearchResult> getRandomResults(Hits hits, int maxResults) {
        if (hits == null || hits.length() == 0) {
            throw new IllegalArgumentException("Hits cannot be null or have 0 length");
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("MaxResults must be 1 or higher");
        }

        List<SearchResult> searchResults = new ArrayList<SearchResult>();
        Set<Integer> picked = new HashSet<Integer>();

        try {

        while (searchResults.size() < maxResults && searchResults.size() < hits.length()) {
            Random rand = new Random();
            int n = rand.nextInt(hits.length());
            if (picked.contains(n)) {
                continue;
            }
            picked.add(n);
            searchResults.add(new SearchResult(hits.doc(n)));
        }

        } catch (IOException e) {
            _log.warn(e);
        }

        return searchResults;
    }

    /**
     * Potentially populates MODEL_TOTAL_HITS and MODEL_RESULTS with the results of the search. If no results,
     * will not populate those variables.
     * @param strict - if true, only return matches for primary category, not secondary category
     */
    protected void storeResultsForCmsCategory(CmsCategory category, Map<String, Object> model, int page, boolean strict, ContentKey excludeContentKey, String language) {
        // Articles in the specific category should come before articles tagged (2ndary) with that category
        // Also, articles with terms from the category name in their title are given precedence, all else equal
        // QUESTION: Should we try to set up weights such that a single search gives us what we want? Yes
        //           Or should we just do two searches and concatenate them? No

        // set up search query
        // articles should be in the particular category
        BooleanQuery bq = new BooleanQuery();

        BooleanQuery innerBq = new BooleanQuery();
        TermQuery primaryCategoryTerm = new TermQuery(
                new Term(Indexer.CMS_PRIMARY_CATEGORY_ID, String.valueOf(category.getId())));
        // set boost on term to give primary category precedence
        primaryCategoryTerm.setBoost(99f); // default boost is 1.0
        innerBq.add(primaryCategoryTerm, BooleanClause.Occur.SHOULD);

        if (!strict) {
            // OR articles can just be tagged with the particular category
            TermQuery secondaryCategoryTerm = new TermQuery(
                    new Term(Indexer.CMS_SECONDARY_CATEGORY_ID, String.valueOf(category.getId())));
            innerBq.add(secondaryCategoryTerm, BooleanClause.Occur.SHOULD);
        }

        bq.add(innerBq, BooleanClause.Occur.MUST);

        if (excludeContentKey != null) {
            TermQuery excludeContentKeyTerm = new TermQuery(
                    new Term(Indexer.ID, excludeContentKey.toString()));
            bq.add(excludeContentKeyTerm, BooleanClause.Occur.MUST_NOT);
        }

        if (language != null) {
            TermQuery languageTerm = new TermQuery(
                    new Term(Indexer.LANGUAGE, language));
            bq.add(languageTerm, BooleanClause.Occur.MUST);
        }

        // This should give higher placement to articles with terms from the category name in their title
        String typeDisplay = category.getName();
        if (StringUtils.isNotBlank(typeDisplay)) {
            try {
                Query titleQuery = _titleParser.parse(typeDisplay);
                bq.add(titleQuery, BooleanClause.Occur.SHOULD);
            } catch (ParseException pe) {
                _log.warn("Couldn't parse article category.", pe);
            }
        }

        // Filter to only CMS features
        Filter typeFilter;
        typeFilter = new CachingWrapperFilter(new QueryFilter(new TermQuery(
                    new Term("type", Indexer.DOCUMENT_TYPE_CMS_FEATURE))));

        // execute search
        Hits hits = _searcher.search(bq, null, null, typeFilter);

        // pass hits object through ResultsPager to paginate and stuff in model as MODEL_RESULTS
        if (hits != null && hits.length() > 0) {
            model.put(MODEL_TOTAL_HITS, hits.length());
            ResultsPager resultsPager = new ResultsPager(hits, ResultsPager.ResultType.topic);
            model.put(MODEL_RESULTS, resultsPager.getResults(page, PAGE_SIZE));
        }
    }

    protected Map<String, Object> handleLegacyCategoryRequest(HttpServletRequest request, int page) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<ArticleCategory> categories;
        if (request.getParameter(PARAM_ID) == null) {
            categories = getCategoriesFromURI(request.getRequestURI());
        } else {
            // allow request parameter to override URI
            categories = getCategoriesFromId(request.getParameter(PARAM_ID));
        }

        // if we found a category, ask the searcher for results and put them in the model
        if (categories != null && categories.size() > 0) {
            model.put(MODEL_SUBCATEGORY, categories.get(0));
            storeResultsForCategory(categories.get(0), model, page);
        }

        return model;
    }

    /**
     * Queries the search indexes for any articles tagged with this category and stores the results
     * in the model
     *
     * @param category Category to search for
     * @param model Model to place results
     * @param page the page of results
     */
    protected void storeResultsForCategory(ArticleCategory category, Map<String, Object> model, int page) {
        // articles must be in the particular category
        BooleanQuery bq = new BooleanQuery();
        TermQuery termQuery = new TermQuery(new Term("category", category.getType()));
        bq.add(termQuery, BooleanClause.Occur.MUST);

        // Begin: GS-7210
        // The SHOULD gives higher placement to articles with terms from the category typeDisplay in their title
        String typeDisplay = category.getTypeDisplay();
        if (StringUtils.isNotBlank(typeDisplay)) {
            try {
                Query titleQuery = _titleParser.parse(typeDisplay);
                bq.add(titleQuery, BooleanClause.Occur.SHOULD);
            } catch (ParseException pe) {
                _log.warn("Couldn't parse article category.", pe);
            }
        }
        // End: GS-7210

        Filter typeFilter;
        // Filter to only articles or CMS features
        if (CmsUtil.isCmsEnabled()) {
            typeFilter = new CachingWrapperFilter(new QueryFilter(new TermQuery(
                        new Term("type", Indexer.DOCUMENT_TYPE_CMS_FEATURE))));
        } else {
            typeFilter = new CachingWrapperFilter(new QueryFilter(new TermQuery(
                        new Term("type", Indexer.DOCUMENT_TYPE_ARTICLE))));
        }

        Hits hits = _searcher.search(bq, null, null, typeFilter);

        if (hits != null && hits.length() > 0) {
            model.put(MODEL_TOTAL_HITS, hits.length());
            ResultsPager resultsPager = new ResultsPager(hits, ResultsPager.ResultType.topic);
            model.put(MODEL_RESULTS, resultsPager.getResults(page, PAGE_SIZE));
        }
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

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
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
}