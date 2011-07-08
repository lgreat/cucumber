package gs.web.content.cms;

import gs.data.content.cms.*;
import gs.data.school.LevelCode;
import gs.data.search.GsSolrQuery;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.DocumentType;
import gs.data.util.CmsUtil;
import gs.web.pagination.DefaultPaginationConfig;
import gs.web.pagination.Pagination;
import gs.web.pagination.PaginationConfig;
import gs.web.pagination.RequestedPage;
import gs.web.school.SchoolOverviewController;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.search.SolrCmsFeatureSearchResult;
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
    public static String SUBJECT_CHOICES_PARAM = "subjectChoices";
    public static String GRADE_CHOICES_PARAM = "gradeChoices";
    public static final int WORKSHEET_DEFAULT_PAGE_SIZE = 6;

    public static final String MODEL_WORKSHEET_RESULTS = "worksheetResults";
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

        //used for dropdowns in jspx
        GRADE_CHOICES.put("-1", "All Grades Levels");
        GRADE_CHOICES.put("198", "Preschool");
        GRADE_CHOICES.put("217", "Elementary School");
        GRADE_CHOICES.put("205", "Middle School");
        GRADE_CHOICES.put("206", "High School");

        SUBJECT_CHOICES.put("-1", "All Subjects");
        SUBJECT_CHOICES.put("207", "Art");
        SUBJECT_CHOICES.put("208", "Foreign Language");
        SUBJECT_CHOICES.put("209", "Math");
        SUBJECT_CHOICES.put("210", "Music");
        SUBJECT_CHOICES.put("211", "Language Arts");
        SUBJECT_CHOICES.put("212", "Science");
        SUBJECT_CHOICES.put("213", "Social Studies");
        SUBJECT_CHOICES.put("214", "Study Skills");
        SUBJECT_CHOICES.put("215", "Tutoring");
        SUBJECT_CHOICES.put("216", "Writing");
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

        //determine the correct contentId and topic center. Handle redirects and 404s
        Long contentId = getContentIdFromRequest(request);

        if (contentId == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            _log.debug("Content ID is null. Returning 404.");
            return new ModelAndView("/status/error404.page");
        } else {
            ModelAndView specialCaseRedirect = getRedirectForSpecialTopicCenters(request, contentId);
            if (specialCaseRedirect != null) {
                _log.debug("Redirecting for content ID " + String.valueOf(contentId));
                return specialCaseRedirect;
            }
        }

        topicCenter = getPublicationDao().populateByContentId(contentId, new CmsTopicCenter());

        if (topicCenter == null) {
            _log.info("Error locating topic center with contentId=" + contentId);
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

        if (topicCenter.isGradeLevelTopicCenter()) {
            model.put("showSchoolChooserPackPromo", SchoolOverviewController.showSchoolChooserPackPromo(request, response));
        }


        //start adding content for middle area of page
        addPageSpecificContentToModel(request, topicCenter, model);


        if(isAjaxRequest(request) && StringUtils.isNotBlank(request.getParameter("requestType")) && request.getParameter("requestType").equals("ajax")){
           return new ModelAndView("/content/cms/worksheetGalleryTable", model);
        } else {
            return new ModelAndView("/content/cms/worksheetGallery", model);
        }
    }

    public void addGoogleAdKeywordsIfNeeded(HttpServletRequest request, CmsTopicCenter topicCenter) {
        if (isUseAdKeywords()) {
            // Google Ad Manager ad keywords
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            for (CmsCategory category : topicCenter.getUniqueKategoryBreadcrumbs()) {
                pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
            }

            pageHelper.addAdKeyword("topic_center_id", String.valueOf(topicCenter.getContentKey().getIdentifier()));
            pageHelper.addAdKeyword(WORKSHEET_GALLERY_GAM_ATTRIBUTE_KEY, String.valueOf(topicCenter.getContentKey().getIdentifier()));
        }
    }

    public void addPageSpecificContentToModel(HttpServletRequest request, CmsTopicCenter cmsTopicCenter, Map<String, Object> model) {

        RequestedPage requestedPage = Pagination.getPageFromRequest(request, WORKSHEET_GALLERY_PAGINATION_CONFIG);

        //find the "videos" subtopic for the topic center we're on
        CmsSubtopic subtopic = null;

        GsSolrQuery query = new GsSolrQuery();
        query.filter(DocumentType.CMS_FEATURE);
        query.filter(CmsFeatureFields.FIELD_CONTENT_TYPE, CmsConstants.WORKSHEET_CONTENT_TYPE);

        //Search for worksheets categorized with the grades, subjects and topics.
        //If grades/subjects/topics parameters are requested, use them. otherwise, use whatever was configured
        //in the cms for the videos subtopic
        if (StringUtils.isNotBlank(request.getParameter("grades"))) {
            query.query(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(request.getParameter("grades").split(",")));
        } else if (subtopic != null && !StringUtils.isBlank(subtopic.getGradeIDs())){
            query.query(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(subtopic.getGradeIDs().split(",")));
        }
        if (StringUtils.isNotBlank(request.getParameter("subjects"))) {
            query.query(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(request.getParameter("subjects").split(",")));
        } else if (subtopic != null && !StringUtils.isBlank(subtopic.getSubjectIDs())){
            query.query(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(subtopic.getSubjectIDs().split(",")));
        }
        if (StringUtils.isNotBlank(request.getParameter("topics"))) {
            query.query(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(request.getParameter("topics").split(",")));
        } else if (subtopic != null && !StringUtils.isBlank(subtopic.getTopicIDs())){
            query.query(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(subtopic.getTopicIDs().split(",")));
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
        model.put(GRADE_CHOICES_PARAM, GRADE_CHOICES);
        model.put(SUBJECT_CHOICES_PARAM, SUBJECT_CHOICES);

        try {
            SearchResultsPage<ICmsFeatureSearchResult> searchResults = getCmsFeatureSearchService().search(query.getSolrQuery());

            for (int i = 0; i < 14; i++) {
                ICmsFeatureSearchResult r = new SolrCmsFeatureSearchResult();
                r.setContentType(CmsConstants.WORKSHEET_CONTENT_TYPE);
                r.setPreviewImageUrl("/cms/49/4849.png");
                r.setPreviewImageAltText("test all text");
                r.setPreviewImageTitle("img title");
                r.setPreviewImageUrl("/cms/49/4849.png");
                r.setPreviewImageAltText("test all text");
                r.setPreviewImageTitle("img title");

            }

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
