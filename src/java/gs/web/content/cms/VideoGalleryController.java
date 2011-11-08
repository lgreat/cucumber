package gs.web.content.cms;

import gs.data.content.cms.*;
import gs.data.pagination.DefaultPaginationConfig;
import gs.data.pagination.PaginationConfig;
import gs.data.school.LevelCode;
import gs.data.search.GsSolrQuery;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.DocumentType;
import gs.data.util.CmsUtil;
import gs.web.i18n.LanguageToggleHelper;
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
 * This class should probably share code with CmsTopicCenterController2010 through composition rather than inheritance,
 * but I don't want to refactor CmsTopicCenterController2010 anymore at this point.
 */
public class VideoGalleryController extends CmsTopicCenterController2010 {
    private static final Logger _log = Logger.getLogger(VideoGalleryController.class);

    /**
     * Spring Bean ID
     */
    public static final String BEAN_ID = "/content/cms/videoGallery.page";

    private String _viewName;

    private CmsFeatureSearchService _cmsFeatureSearchService;

    public static final PaginationConfig VIDEO_GALLERY_PAGINATION_CONFIG;

    public static final String VIDEO_GALLERY_GAM_ATTRIBUTE_KEY = "video_gallery_topic_center_id";

    public static Map<String,String> GRADE_CHOICES = new LinkedHashMap<String,String>();

    public static Map<String,String> TOPIC_CHOICES = new LinkedHashMap<String,String>();
    public static String TOPIC_CHOICES_PARAM = "topicChoices";
    public static String GRADE_CHOICES_PARAM = "gradeChoices";


    static {
        VIDEO_GALLERY_PAGINATION_CONFIG = new PaginationConfig(
                DefaultPaginationConfig.DEFAULT_PAGE_SIZE_PARAM,
                DefaultPaginationConfig.DEFAULT_PAGE_NUMBER_PARAM,
                DefaultPaginationConfig.DEFAULT_OFFSET_PARAM,
                15,
                DefaultPaginationConfig.DEFAULT_MAX_PAGE_SIZE,
                DefaultPaginationConfig.ZERO_BASED_OFFSET,
                DefaultPaginationConfig.ZERO_BASED_PAGES
        );

        //used for dropdowns in jspx
        GRADE_CHOICES.put("-1", "All Grades");
        GRADE_CHOICES.put("199", "Kindergarten");
        GRADE_CHOICES.put("200", "First Grade");
        GRADE_CHOICES.put("201", "Second Grade");
        GRADE_CHOICES.put("202", "Third Grade");
        GRADE_CHOICES.put("203", "Fourth Grade");
        GRADE_CHOICES.put("204", "Fifth Grade");

        TOPIC_CHOICES.put("-1", "All Topics");
        TOPIC_CHOICES.put("133", "Academic Skills");
        TOPIC_CHOICES.put("140", "Homework Help");
        TOPIC_CHOICES.put("378", "Motivation &amp; Confidence");
        TOPIC_CHOICES.put("241", "Parental Power");
        TOPIC_CHOICES.put("240", "Understanding the System");
        TOPIC_CHOICES.put("124", "Behavior &amp; Discipline");
        TOPIC_CHOICES.put("157", "Social Skills");
        TOPIC_CHOICES.put("149", "Bullying");
        TOPIC_CHOICES.put("379", "Learning and Development");
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
            //content creators might place a link into video's description
            getCmsFeatureEmbeddedLinkResolver().replaceEmbeddedLinks(topicCenter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //add things needed for sidebars to work
        model.put(MODEL_BROWSE_BY_GRADE_SUBTOPICS, getBrowseByGradeForTopicCenter(topicCenter.getContentKey().getIdentifier()));

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


        if(isAjaxRequest(request) && StringUtils.isNotBlank(request.getParameter("requestType")) && request.getParameter("requestType").equals("ajax")){
           return new ModelAndView("/content/cms/videoGalleryTable", model);
        } else {
            return new ModelAndView("/content/cms/videoGallery", model);
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
            pageHelper.addAdKeyword(VIDEO_GALLERY_GAM_ATTRIBUTE_KEY, String.valueOf(topicCenter.getContentKey().getIdentifier()));
        }
    }



    public void addPageSpecificContentToModel(HttpServletRequest request, CmsTopicCenter cmsTopicCenter, Map<String, Object> model) {

        LanguageToggleHelper.Language currentLanguage = LanguageToggleHelper.handleLanguageToggle(request, model);

        RequestedPage requestedPage = Pagination.getPageFromRequest(request, VIDEO_GALLERY_PAGINATION_CONFIG);

        //find the "videos" subtopic for the topic center we're on
        List<CmsSubtopic> subtopics = cmsTopicCenter.getSubtopics();
        CmsSubtopic videoSubtopic = null;
        for (CmsSubtopic topic : subtopics) {
            if ("videos".equals(topic.getTitle())) {
                videoSubtopic = topic;
                break;
            }
        }

        GsSolrQuery query = new GsSolrQuery();
        query.filter(DocumentType.CMS_FEATURE);
        query.filter(CmsFeatureFields.FIELD_CONTENT_TYPE, CmsConstants.VIDEO_CONTENT_TYPE);
        query.filter(CmsFeatureFields.FIELD_LANGUAGE, currentLanguage.name());

        //'videos' subtopic can be viewed within the context of a topic center.Hence the contentId(topicCenterId) is needed.
        //A 'videos' subtopic can be categorized with any number of grades,subjects,topics.
        //The categories for the 'videos' subtopics are entered in the cms on the topic center template.

        //Search for videos categorized with the grades, subjects and topics.
        //If grades/subjects/topics parameters are requested, use them. otherwise, use whatever was configured
        //in the cms for the videos subtopic
        if (StringUtils.isNotBlank(request.getParameter("grades"))) {
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(request.getParameter("grades").split(",")));
        } else if (videoSubtopic != null && !StringUtils.isBlank(videoSubtopic.getGradeIDs())){
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(videoSubtopic.getGradeIDs().split(",")));
        }
        if (StringUtils.isNotBlank(request.getParameter("subjects"))) {
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(request.getParameter("subjects").split(",")));
        } else if (videoSubtopic != null && !StringUtils.isBlank(videoSubtopic.getSubjectIDs())){
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(videoSubtopic.getSubjectIDs().split(",")));
        }
        if (StringUtils.isNotBlank(request.getParameter("topics"))) {
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(request.getParameter("topics").split(",")));
        } else if (videoSubtopic != null && !StringUtils.isBlank(videoSubtopic.getTopicIDs())){
            query.addQuery(CmsFeatureFields.FIELD_CMS_CATEGORY_ID, Arrays.asList(videoSubtopic.getTopicIDs().split(",")));
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
        model.put(TOPIC_CHOICES_PARAM, TOPIC_CHOICES);



        try {
            SearchResultsPage<ICmsFeatureSearchResult> searchResults = getCmsFeatureSearchService().search(query.getSolrQuery());
            addPagingDataToModel(
                    requestedPage.getValidatedOffset(VIDEO_GALLERY_PAGINATION_CONFIG, searchResults.getTotalResults()),
                    requestedPage.pageSize,
                    searchResults.getTotalResults(),
                    model
            );

            if (searchResults != null && searchResults.getTotalResults() > 0) {
                model.put(MODEL_VIDEO_RESULTS, searchResults.getSearchResults());
            } else {
                model.put(MODEL_VIDEO_RESULTS, new ArrayList());
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
