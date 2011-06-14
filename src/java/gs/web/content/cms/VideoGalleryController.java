package gs.web.content.cms;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsConstants;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.ContentKey;
import gs.data.school.LevelCode;
import gs.data.search.SearchResultsPage;
import gs.data.util.CmsUtil;
import gs.web.pagination.Pagination;
import gs.web.pagination.RequestedPage;
import gs.web.school.SchoolOverviewController;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.util.UrlBuilder;
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
            return new ModelAndView("/status/error404.page");
        } else {
            ModelAndView specialCaseRedirect = getRedirectForSpecialTopicCenters(request, contentId);
            if (specialCaseRedirect != null) {
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

        if (topicCenter.isGradeLevelTopicCenter()) {
            model.put("showSchoolChooserPackPromo", SchoolOverviewController.showSchoolChooserPackPromo(request, response));
        }


        //start adding content for middle area of page
        addPageSpecificContentToModel(request, model);


        if(isAjaxRequest(request) && StringUtils.isNotBlank(request.getParameter("requestType")) && request.getParameter("requestType").equals("ajax")){
           return new ModelAndView("/content/cms/videoGalleryTable", model);
        } else {
            return new ModelAndView("/content/cms/videoGallery", model);
        }
    }

    public void addPageSpecificContentToModel(HttpServletRequest request, Map<String, Object> model) {
        Set categoryIds = getCategoryIdsFromRequest(request);

        if (categoryIds.size() > 0) {
            String categoryIdsStr = StringUtils.join(categoryIds, ",");
            List<CmsCategory> categories = getCmsCategoryDao().getCmsCategoriesFromIds(categoryIdsStr);
            //Search for videos categorized with the grades, subjects and topics.

            RequestedPage requestedPage = Pagination.getPageFromRequest(request);

            String queryString = request.getQueryString();
            String url = request.getRequestURL().toString();
            if (queryString != null) {
                url+= "?" + queryString;
            }
            
            model.put(MODEL_FULL_URL, url);

            SearchResultsPage<ICmsFeatureSearchResult> searchResults = getCmsFeatureSearchService().getCmsFeaturesByType(categories, "Video", 2, requestedPage.offset);

            addPagingDataToModel(requestedPage.offset, 2, searchResults.getTotalResults(), model);

            if (searchResults != null && searchResults.getTotalResults() > 0) {
                model.put(MODEL_VIDEO_RESULTS, searchResults.getSearchResults());
            } else {
                model.put(MODEL_VIDEO_RESULTS, new ArrayList());
            }
        }


    }

    private Set getCategoryIdsFromRequest(HttpServletRequest request) {
        Set categoryIds = new TreeSet();
        //'videos' subtopic can be viewed within the context of a topic center.Hence the contentId(topicCenterId) is needed.
        //A 'videos' subtopic can be categorized with any number of grades,subjects,topics.
        //The categories for the 'videos' subtopics are entered in the cms on the topic center template.
        if (StringUtils.isNotBlank(request.getParameter("grades"))) {
            categoryIds.addAll(Arrays.asList(request.getParameter("grades").split(",")));
        }
        if (StringUtils.isNotBlank(request.getParameter("subjects"))) {
            categoryIds.addAll(Arrays.asList(request.getParameter("subjects").split(",")));
        }
        if (StringUtils.isNotBlank(request.getParameter("topics"))) {
            categoryIds.addAll(Arrays.asList(request.getParameter("topics").split(",")));
        }
        return categoryIds;
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