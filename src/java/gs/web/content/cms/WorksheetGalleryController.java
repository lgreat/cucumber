package gs.web.content.cms;

import gs.data.content.cms.CmsConstants;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.ContentKey;
import gs.data.pagination.DefaultPaginationConfig;
import gs.data.pagination.PaginationConfig;
import gs.data.school.LevelCode;
import gs.data.search.GsSolrQuery;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.DocumentType;
import gs.data.util.CmsUtil;
import gs.web.pagination.Pagination;
import gs.web.pagination.RequestedPage;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This class should probably share code with CmsTopicCenterController2010 through composition rather than inheritance
 */
public class WorksheetGalleryController extends CmsTopicCenterController2010 {
    private static final Logger _log = Logger.getLogger(WorksheetGalleryController.class);

    /**
     * Spring Bean ID
     */
    public static final String BEAN_ID = "/content/cms/worksheetGallery.page";

    private String _viewName;

    private CmsFeatureSearchService _cmsFeatureSearchService;

    public static final PaginationConfig WORKSHEET_GALLERY_PAGINATION_CONFIG;

    public static final String WORKSHEET_GALLERY_GAM_ATTRIBUTE_KEY = "worksheet_gallery_topic_center_id";

    public static Map<String,String> GRADE_CHOICES = new LinkedHashMap<String,String>();
    public static Map<String,String> SUBJECT_CHOICES = new LinkedHashMap<String,String>();

    public static Map<String,String> META_KEYWORDS_SUBJECT_ONLY = new HashMap<String,String>();
    public static Map<String,String> META_KEYWORDS_GRADE_ONLY = new HashMap<String,String>();
    public static Map<String,String> META_GRADE_KEY_TO_NUMERIC_NAME = new HashMap<String,String>();
    public static Map<String,String> META_GRADE_KEY_TO_SHORT_NAME = new HashMap<String,String>();
    public static Map<String,String> META_DESCRIPTION_PK_K_GRADE_SUBJECT = new HashMap<String,String>();
    public static Map<String,String> META_KEYWORDS_PK_K_GRADE_SUBJECT = new HashMap<String,String>();

    public static String SUBJECT_CHOICES_KEY = "subjectChoices";
    public static String GRADE_CHOICES_KEY = "gradeChoices";
    public static String REQUESTED_GRADE_KEY = "requestedGrade";
    public static String REQUESTED_SUBJECT_KEY = "requestedSubject";
    public static String REQUESTED_GRADE_NAME_KEY = "requestedGradeName";
    public static String REQUESTED_SUBJECT_NAME_KEY = "requestedSubjectName";
    public static String WORKSHEETS_PATH_KEY = "worksheetsPath";

    public static String GRADE_ID_REQUEST_PARAM = "gradeId";
    public static String SUBJECT_ID_REQUEST_PARAM = "subjectId";

    public static Map<String,Long> SUBJECT_URL_COMPONENT_LOOKUP = new HashMap<String,Long>();
    public static Map<String,Long> GRADE_URL_COMPONENT_LOOKUP = new HashMap<String,Long>();

    public static final int WORKSHEET_DEFAULT_PAGE_SIZE = 6;

    public static final String MODEL_WORKSHEET_RESULTS = "worksheetResults";
    public static final String MODEL_META_DESCRIPTION = "metaDescription";
    public static final String MODEL_META_KEYWORDS = "metaKeywords";

    public static final String WORKSHEETS_PATH = "/worksheets";

    static {

        WORKSHEET_GALLERY_PAGINATION_CONFIG = new PaginationConfig(
                DefaultPaginationConfig.DEFAULT_PAGE_SIZE_PARAM,
                DefaultPaginationConfig.DEFAULT_PAGE_NUMBER_PARAM,
                DefaultPaginationConfig.DEFAULT_OFFSET_PARAM,
                WORKSHEET_DEFAULT_PAGE_SIZE,
                DefaultPaginationConfig.DEFAULT_MAX_PAGE_SIZE,
                DefaultPaginationConfig.ZERO_BASED_OFFSET,
                DefaultPaginationConfig.ZERO_BASED_PAGES
        );

        //used for dropdowns, page title, hier1 in jsp
        GRADE_CHOICES.put("", "All Grades");
        GRADE_CHOICES.put("preschool", "Preschool");
        GRADE_CHOICES.put("kindergarten", "Kindergarten");
        GRADE_CHOICES.put("first-grade", "First Grade");
        GRADE_CHOICES.put("second-grade", "Second Grade");
        GRADE_CHOICES.put("third-grade", "Third Grade");
        GRADE_CHOICES.put("fourth-grade", "Fourth Grade");
        GRADE_CHOICES.put("fifth-grade", "Fifth Grade");
        GRADE_CHOICES.put("elementary-school", "Elementary School");

        //reverse lookup of GRADE_CHOICES. Maybe could have used a bidirectional map with string manipulation
        GRADE_URL_COMPONENT_LOOKUP.put("preschool",CmsConstants.PRESCHOOL_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("kindergarten",CmsConstants.KINDERGARTEN_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("first-grade",CmsConstants.FIRST_GRADE_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("second-grade",CmsConstants.SECOND_GRADE_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("third-grade",CmsConstants.THIRD_GRADE_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("fourth-grade",CmsConstants.FOURTH_GRADE_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("fifth-grade",CmsConstants.FIFTH_GRADE_CATEGORY_ID);
        GRADE_URL_COMPONENT_LOOKUP.put("elementary-school",CmsConstants.ELEMENTARY_SCHOOL_CATEGORY_ID);

        //used for dropdowns in jspx
        SUBJECT_CHOICES.put("", "All Subjects");
        SUBJECT_CHOICES.put("math", "Math");
        SUBJECT_CHOICES.put("reading", "Reading");
        SUBJECT_CHOICES.put("writing", "Writing");

        SUBJECT_URL_COMPONENT_LOOKUP.put("math",CmsConstants.MATH_CATEGORY_ID);
        SUBJECT_URL_COMPONENT_LOOKUP.put("reading",CmsConstants.READING_CATEGORY_ID);
        SUBJECT_URL_COMPONENT_LOOKUP.put("writing",CmsConstants.WRITING_CATEGORY_ID);

        // GS-12144 START constants for meta keywords & meta descriptions

        META_KEYWORDS_SUBJECT_ONLY.put("math", "math worksheets, math worksheet, mathematics worksheet, maths worksheet, worksheets for math");
        META_KEYWORDS_SUBJECT_ONLY.put("reading", "reading worksheets, reading comprehension worksheets, language worksheets, nouns worksheets, grammar worksheets");
        META_KEYWORDS_SUBJECT_ONLY.put("writing", "writing worksheets, writing worksheet");

        META_GRADE_KEY_TO_NUMERIC_NAME.put("kindergarten","kindergarten");
        META_GRADE_KEY_TO_NUMERIC_NAME.put("first-grade","1st grade");
        META_GRADE_KEY_TO_NUMERIC_NAME.put("second-grade","2nd grade");
        META_GRADE_KEY_TO_NUMERIC_NAME.put("third-grade","3rd grade");
        META_GRADE_KEY_TO_NUMERIC_NAME.put("fourth-grade","4th grade");
        META_GRADE_KEY_TO_NUMERIC_NAME.put("fifth-grade","5th grade");

        META_GRADE_KEY_TO_SHORT_NAME.put("kindergarten","kindergarten");
        META_GRADE_KEY_TO_SHORT_NAME.put("first-grade","first grade");
        META_GRADE_KEY_TO_SHORT_NAME.put("second-grade","second grade");
        META_GRADE_KEY_TO_SHORT_NAME.put("third-grade","third grade");
        META_GRADE_KEY_TO_SHORT_NAME.put("fourth-grade","fourth grade");
        META_GRADE_KEY_TO_SHORT_NAME.put("fifth-grade","fifth grade");

        META_KEYWORDS_GRADE_ONLY.put("preschool", "preschool worksheets, preschool worksheet, pre school worksheets, worksheets for preschool kids");
        META_KEYWORDS_GRADE_ONLY.put("kindergarten", "worksheet for kindergarten, kindergarten worksheets, worksheets for kindergarten, kindergarten worksheet");

        for (String gradeKey : META_GRADE_KEY_TO_SHORT_NAME.keySet()) {
            if (!META_KEYWORDS_GRADE_ONLY.containsKey(gradeKey)) {
                META_KEYWORDS_GRADE_ONLY.put(gradeKey,
                        "worksheets for " + META_GRADE_KEY_TO_NUMERIC_NAME.get(gradeKey) + "rs, " +
                        META_GRADE_KEY_TO_NUMERIC_NAME.get(gradeKey) + " worksheets, " +
                        META_GRADE_KEY_TO_SHORT_NAME.get(gradeKey) + " worksheets, " +
                        "worksheets for " + META_GRADE_KEY_TO_SHORT_NAME.get(gradeKey));
            }
        }

        META_DESCRIPTION_PK_K_GRADE_SUBJECT.put("preschool/math", "Free printable preschool math worksheets to help your child practice numbers, counting, and addition.");
        META_DESCRIPTION_PK_K_GRADE_SUBJECT.put("preschool/reading", "Free printable preschool reading worksheets to help your child practice the alphabet, letter sounds, and new words.");
        META_DESCRIPTION_PK_K_GRADE_SUBJECT.put("preschool/writing", "Free printable preschool writing worksheets to help your child practice tracing and coloring shapes and letters.");
        META_KEYWORDS_PK_K_GRADE_SUBJECT.put("preschool/math", "preschool math worksheets");
        META_KEYWORDS_PK_K_GRADE_SUBJECT.put("preschool/reading", "preschool reading worksheets, alphabet worksheets");
        META_KEYWORDS_PK_K_GRADE_SUBJECT.put("preschool/writing", "preschool writing worksheets, coloring worksheets");

        META_DESCRIPTION_PK_K_GRADE_SUBJECT.put("kindergarten/math", "Free printable kindergarten math worksheets to help your child practice addition, matching, and identifying shapes and patterns.");
        META_DESCRIPTION_PK_K_GRADE_SUBJECT.put("kindergarten/reading", "Free printable kindergarten reading worksheets to help your child with spelling, language, grammar, and more.");
        META_DESCRIPTION_PK_K_GRADE_SUBJECT.put("kindergarten/writing", "Free printable kindergarten writing worksheets to help your child practice tracing and writing shapes and letters.");
        META_KEYWORDS_PK_K_GRADE_SUBJECT.put("kindergarten/math", "kindergarten math worksheets");
        META_KEYWORDS_PK_K_GRADE_SUBJECT.put("kindergarten/reading", "kindergarten reading worksheets, grammar worksheets");
        META_KEYWORDS_PK_K_GRADE_SUBJECT.put("kindergarten/writing", "kindergarten writing worksheets");

        // GS-12144 END constants for meta keywords & meta descriptions
    }

    //=========================================================================
    // spring mvc methods
    //=========================================================================

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        if (!CmsUtil.isCmsEnabled()) {
            return new ModelAndView(_viewName, model);
        }

        CmsTopicCenter topicCenter;
        SessionContext context = SessionContextUtil.getSessionContext(request);

        topicCenter = getPublicationDao().populateByContentId(CmsConstants.WORKSHEETS_TOPIC_CENTER_ID, new CmsTopicCenter());

        if (topicCenter == null) {
            _log.info("Error locating topic center with contentId=" + CmsConstants.WORKSHEETS_TOPIC_CENTER_ID);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }
        //done getting topic center

        //general things needed for topic center sidebars/content to work
        addOmnitureDataToModel(model, topicCenter);
        model.put(MODEL_TOPIC_CENTER, topicCenter);
        UrlBuilder builder = new UrlBuilder(new ContentKey(CmsConstants.TOPIC_CENTER_CONTENT_TYPE, topicCenter.getContentKey().getIdentifier()));
        model.put(MODEL_URI, builder.asSiteRelative(request));
        addGoogleAdKeywordsIfNeeded(request, topicCenter);

        try {
            //content creators might place a link into worksheet's description
            getCmsFeatureEmbeddedLinkResolver().replaceEmbeddedLinks(topicCenter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // GS-10275
        // Show the local community module we've built on the school overview page in place of the map IF a user is
        // cookied to one of the 73 local cities. If the user isn't cookied to one of the cities, show the Map module
        // (the Local Schools module)
        boolean hasLocalCommunity = false;
        if (!topicCenter.isPreschoolTopicCenter()) {
            hasLocalCommunity = loadLocalCommunity(model,request);
        }
        if (topicCenter.isPreschoolTopicCenter() || !hasLocalCommunity) {
            // local schools module
            // check for a change of city
            updateCityCookieIfNeeded(request, response);

            LevelCode levelCode = getLevelCodeFromTopicCenter(topicCenter);

            loadTopRatedSchools(model, context, levelCode);
        }

        //start adding content for middle area of page
        addPageSpecificContentToModel(request, topicCenter, model);

        // IMPORTANT: must be called after addPageSpecificContentToModel
        addMetaDataToModel(model);

        if(isAjaxRequest(request) && StringUtils.isNotBlank(request.getParameter("requestType")) && request.getParameter("requestType").equals("ajax")){
           return new ModelAndView("/content/cms/worksheetGalleryTable", model);
        } else {
            return new ModelAndView("/content/cms/worksheetGallery", model);
        }
    }

    /**
     * must be called after addPageSpecificContentToModel
     * @param model
     */
    public static void addMetaDataToModel(Map<String, Object> model) {
        String metaDescription = null;
        String metaKeywords = null;

        String requestedGradeKey = (String)model.get(REQUESTED_GRADE_KEY);
        String requestedSubjectKey = (String)model.get(REQUESTED_SUBJECT_KEY);

        // worksheet gallery not filtered by grade, so just use topic center's CMS-entered meta description and keywords
        if (requestedGradeKey == null && requestedSubjectKey == null) {
            return;
        }

        if (requestedGradeKey == null && requestedSubjectKey != null) {
            // subject-specific gallery
            metaDescription = "Free " + requestedSubjectKey + " worksheets you can print and use with children in preschool through fifth grade.";
            metaKeywords = META_KEYWORDS_SUBJECT_ONLY.get(requestedSubjectKey);
        } else if (requestedGradeKey != null && requestedSubjectKey == null) {
            // grade-specific gallery
            if ("preschool".equals(requestedGradeKey)) {
                metaDescription = "Free printable preschool worksheets to help your child develop early math, reading, and writing skills.";
                metaKeywords = META_KEYWORDS_GRADE_ONLY.get(requestedGradeKey);
            } else if ("elementary-school".equals(requestedGradeKey)) {
                metaDescription = "Free elementary worksheets for kids in grades K-5; support learning with a supply of fun printables.";
                metaKeywords = "elementary worksheets, worksheets for kids";
            } else {
                metaDescription = "Free printable " + META_GRADE_KEY_TO_NUMERIC_NAME.get(requestedGradeKey) + " worksheets to help your whiz kid practice math, reading, and writing skills.";
                metaKeywords = META_KEYWORDS_GRADE_ONLY.get(requestedGradeKey);
            }
        } else {
            // grade- and subject-specific gallery
            if ("preschool".equals(requestedGradeKey) || "kindergarten".equals(requestedGradeKey)) {
                metaDescription = META_DESCRIPTION_PK_K_GRADE_SUBJECT.get(requestedGradeKey + "/" + requestedSubjectKey);
                metaKeywords = META_KEYWORDS_PK_K_GRADE_SUBJECT.get(requestedGradeKey + "/" + requestedSubjectKey);
            } else {
                metaDescription = "Free printable " + META_GRADE_KEY_TO_NUMERIC_NAME.get(requestedGradeKey) + " " +
                        requestedSubjectKey + " worksheets to help your child practice skills while having fun.";
                metaKeywords = META_GRADE_KEY_TO_NUMERIC_NAME.get(requestedGradeKey) + " " + requestedSubjectKey + " worksheets, " +
                        META_GRADE_KEY_TO_SHORT_NAME.get(requestedGradeKey) + " " + requestedSubjectKey + " worksheets";
            }
        }

        // TODO-12144 what about /worksheets/elementary-school/<subject>/ or /worksheets/ ???

        model.put(MODEL_META_DESCRIPTION, metaDescription);
        model.put(MODEL_META_KEYWORDS, metaKeywords);
    }

    public void addGoogleAdKeywordsIfNeeded(HttpServletRequest request, CmsTopicCenter topicCenter) {
        if (isUseAdKeywords()) {
            // Google Ad Manager ad keywords
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            /*for (CmsCategory category : topicCenter.getUniqueKategoryBreadcrumbs()) {
                pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
            }*/
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, "WorksheetGallery");
            pageHelper.addAdKeyword("topic_center_id", String.valueOf(topicCenter.getContentKey().getIdentifier()));
            pageHelper.addAdKeyword(WORKSHEET_GALLERY_GAM_ATTRIBUTE_KEY, String.valueOf(topicCenter.getContentKey().getIdentifier()));
        }
    }

    public void addPageSpecificContentToModel(HttpServletRequest request, CmsTopicCenter cmsTopicCenter, Map<String, Object> model) {

        RequestedPage requestedPage = Pagination.getPageFromRequest(request, WORKSHEET_GALLERY_PAGINATION_CONFIG);

        String requestUri = request.getRequestURI();
        String[] pathComponents = requestUri.split("/");

        String requestedGradeId;
        String requestedGrade = null;
        String requestedSubjectId;
        String requestedSubject = null;

        //we need to locate requested grade and/or subject if they exist. Give priority to querystring params over URL structure

        requestedGradeId = request.getParameter(GRADE_ID_REQUEST_PARAM);
        if (StringUtils.isNotBlank(requestedGradeId)) {
            //TODO: should probably use a bidirectional map to do this
            for (Map.Entry<String,Long> entry : GRADE_URL_COMPONENT_LOOKUP.entrySet()) {
                if (requestedGradeId.equals(String.valueOf(entry.getValue()))) {
                    requestedGrade = entry.getKey();
                }
            }
            if (requestedGrade == null) {
                requestedGradeId = null;
            }
        }

        requestedSubjectId = request.getParameter(SUBJECT_ID_REQUEST_PARAM);
        if (StringUtils.isNotBlank(requestedSubjectId)) {
            for (Map.Entry<String,Long> entry : SUBJECT_URL_COMPONENT_LOOKUP.entrySet()) {
                if (requestedSubjectId.equals(String.valueOf(entry.getValue()))) {
                    requestedSubject = entry.getKey();
                }
            }
            if (requestedSubject == null) {
                requestedSubjectId = null;
            }
        }

        if (StringUtils.isBlank(requestedGradeId) && StringUtils.isBlank(requestedSubjectId)) {
            //let's just look at the last two "url components" and try to match them to grades and subjects
            if (pathComponents.length >= 2) {
                for (int i = pathComponents.length-2; i<pathComponents.length; i++ ) {
                    String name = pathComponents[i];
                    if (requestedGradeId == null) {
                        Long value = GRADE_URL_COMPONENT_LOOKUP.get(name);
                        if (value != null) {
                            requestedGradeId = String.valueOf(value);
                            requestedGrade = name;
                        }
                    }
                    if (requestedSubjectId == null) {
                        Long value = SUBJECT_URL_COMPONENT_LOOKUP.get(name);
                        if (value != null) {
                            requestedSubjectId = String.valueOf(value);
                            requestedSubject = name;
                        }
                    }
                }
            }
        }

        GsSolrQuery query = new GsSolrQuery();
        query.filter(DocumentType.CMS_FEATURE);
        query.filter(CmsFeatureFields.FIELD_CONTENT_TYPE, CmsConstants.WORKSHEET_CONTENT_TYPE);

        query.sort(CmsFeatureFields.FIELD_SORTABLE_TITLE, false).sort(CmsFeatureFields.FIELD_SORTABLE_LOWEST_GRADE, false);

        if (requestedGradeId != null) {
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(requestedGradeId.split(",")));
        }
        if (requestedSubjectId != null) {
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(requestedSubjectId.split(",")));
        }

        query.page(requestedPage.offset, requestedPage.pageSize);

        String queryString = request.getQueryString();
        //TODO: do we really need this, since we have isAjaxRequest()?
        queryString = UrlUtil.removeParamsFromQueryString(queryString, "requestType","decorator");
        String url = request.getRequestURL().toString();
        if (queryString != null) {
            url+= "?" + queryString;
        }

        model.put(MODEL_FULL_URL, url);
        model.put(GRADE_CHOICES_KEY, GRADE_CHOICES);
        model.put(SUBJECT_CHOICES_KEY, SUBJECT_CHOICES);
        model.put(REQUESTED_GRADE_KEY, requestedGrade);
        model.put(REQUESTED_SUBJECT_KEY, requestedSubject);
        model.put(REQUESTED_GRADE_NAME_KEY,GRADE_CHOICES.get(requestedGrade));
        model.put(REQUESTED_SUBJECT_NAME_KEY,SUBJECT_CHOICES.get(requestedSubject));
        model.put(WORKSHEETS_PATH_KEY, WORKSHEETS_PATH);

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if(requestedGradeId == null) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, "Preschool");
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, "Elementary");
        } else if (Long.valueOf(requestedGradeId).equals(CmsConstants.PRESCHOOL_CATEGORY_ID)) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, "Preschool");
        } else {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, "Elementary");
        }

        try {
            SearchResultsPage<ICmsFeatureSearchResult> searchResults = getCmsFeatureSearchService().search(query.getSolrQuery());

            addPagingDataToModel(
                    requestedPage.getValidatedOffset(WORKSHEET_GALLERY_PAGINATION_CONFIG, searchResults.getTotalResults()),
                    requestedPage.pageSize,
                    searchResults.getTotalResults(),
                    model
            );

            if (searchResults != null && searchResults.getTotalResults() > 0) {
                model.put(MODEL_WORKSHEET_RESULTS, searchResults.getSearchResults());
            } else {
                model.put(MODEL_WORKSHEET_RESULTS, new ArrayList());
            }
        } catch (SearchException e) {
            _log.debug("Error when searching for cms features using categories", e);
        }
    }

    /**
     * Calculates paging info and adds it to model.
     *
     * @param pageSize
     * @param totalResults
     * @param model
     */
    protected void addPagingDataToModel(int start, Integer pageSize, int totalResults, Map<String,Object> model) {

        //TODO: perform validation to only allow no paging when results are a certain size
        if (pageSize > 0) {
            gs.web.pagination.Page page = new gs.web.pagination.Page(start, pageSize, totalResults);
            model.put("page", page);
        } else {
            model.put(MODEL_USE_PAGING, Boolean.valueOf(false));
        }
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest"
                .equals(request.getHeader("X-Requested-With"));
    }

    public CmsFeatureSearchService getCmsFeatureSearchService() {
        return _cmsFeatureSearchService;
    }

    public void setCmsFeatureSearchService(CmsFeatureSearchService cmsFeatureSearchService) {
        _cmsFeatureSearchService = cmsFeatureSearchService;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
