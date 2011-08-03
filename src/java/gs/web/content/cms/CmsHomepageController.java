package gs.web.content.cms;

import gs.data.admin.IPropertyDao;
import gs.data.cms.IPublicationDao;
import gs.data.community.*;
import gs.data.content.cms.*;
import gs.data.search.*;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.DocumentType;
import gs.data.security.Permission;
import gs.data.state.StateManager;
import gs.data.util.CmsUtil;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.util.CookieUtil;
import gs.web.util.PageHelper;
import gs.web.util.SitePrefCookie;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class CmsHomepageController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    public static final String BEAN_ID = "/index.page";

    public static final int GRADE_BY_GRADE_NUM_CATEGORIES = 9;
    public static final int GRADE_BY_GRADE_NUM_CMS_CONTENT = 6;
    public static final int GRADE_BY_GRADE_NUM_ITEMS = 6;
    public static final int GRADE_BY_GRADE_NUM_DISCUSSIONS = 2;
    public static final int GRADE_BY_GRADE_MIN_NUM_REPLIES = 3;
    public static final String MODEL_ALL_RAISE_YOUR_HAND_FOR_TOPIC = "allRaiseYourHandDiscussions";
    public static final int MAX_RAISE_YOUR_HAND_DISCUSSIONS_FOR_CMSADMIN = 1000;
    public static final String DECLINED_IPHONE_SPLASH_PAGE_COOKIE = "declinedIphoneSplashPage";
    public static final int DECLINED_IPHONE_SPLASH_PAGE_COOKIE_MAX_AGE = 60 * 60 * 24 * 90;

    public static final Map<Long, Long> categoryIdToTopicCenterIdMap = new HashMap<Long, Long>(GRADE_BY_GRADE_NUM_CATEGORIES);

    public static final String MODEL_RECENT_CMS_CONTENT = "recentCmsContent";

    private IPublicationDao _publicationDao;
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;
    private IPropertyDao _propertyDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private ICmsCategoryDao _cmsCategoryDao;
    private Searcher _searcher;
    private IRaiseYourHandDao _raiseYourHandDao;
    private CmsFeatureSearchService _cmsFeatureSearchService;

    protected static Cache _gradeByGradeCache;
    protected static Cache _recentDiscussionsCache;

    static {
        // Preschool
        categoryIdToTopicCenterIdMap.put(198L, 1573L);
        // Elementary
        categoryIdToTopicCenterIdMap.put(199L, 1574L);
        categoryIdToTopicCenterIdMap.put(200L, 1574L);
        categoryIdToTopicCenterIdMap.put(201L, 1574L);
        categoryIdToTopicCenterIdMap.put(202L, 1574L);
        categoryIdToTopicCenterIdMap.put(203L, 1574L);
        categoryIdToTopicCenterIdMap.put(204L, 1574L);
        // Middle
        categoryIdToTopicCenterIdMap.put(205L, 1575L);
        // High
        categoryIdToTopicCenterIdMap.put(206L, 1576L);

        _gradeByGradeCache = new Cache(CmsHomepageController.class.getName() + ".gradeByGrade", 10, false, false, 3 * 60 * 60, 3 * 60 * 60); //three hours
        _recentDiscussionsCache = new Cache(CmsHomepageController.class.getName() + ".recentDiscussions", 10, false, false, 10 * 60, 10 * 60); //ten minutes
        CacheManager.create().addCache(_gradeByGradeCache);
        CacheManager.create().addCache(_recentDiscussionsCache);
    }

    /**
     * See if the request is coming from a mobile device that supports our app and
     * redirect them to a page about the app.  If returning from the app page, set a cookie
     * to prevent further redirection for that browser.
     * @param request Used to determine page to return to
     * @param response Used to set cookies
     * @param includeReferrer If true, instruct the splash page to
     *        include a link back to the page they came from.  If false, link to home page.
     * @return The redirect, if needed
     */
    public static ModelAndView checkMobileTraffic(HttpServletRequest request, HttpServletResponse response, boolean includeReferrer) {
        // if referrer is iPhone splash page, set 90-day cookie
        String referrer = request.getHeader("Referer");
        if (referrer != null && referrer.contains("/splash/iphone.page")) {
            CookieUtil.setCookie(response,
                    DECLINED_IPHONE_SPLASH_PAGE_COOKIE, "true",
                    DECLINED_IPHONE_SPLASH_PAGE_COOKIE_MAX_AGE);
        } else {
            return redirectMobileTraffic(request, response, includeReferrer);
        }

        return null;
    }

    /**
     * See if the request is coming from a mobile device that supports our app and
     * redirect them to a page about the app.
     * @param request Used to determine page to return to
     * @param includeReferrer If true, instruct the splash page to
     *        include a link back to the page they came from.  If false, link to home page.
     * @return The redirect, if needed
     */
    public static ModelAndView redirectMobileTraffic(HttpServletRequest request, HttpServletResponse response, boolean includeReferrer) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        Cookie declinedIphoneSplashPageCookie = CookieUtil.getCookie(request, DECLINED_IPHONE_SPLASH_PAGE_COOKIE);
        SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);

        boolean hoverCookieSet = !StringUtils.isBlank(sitePrefCookie.getProperty("showHover"));
        boolean applicableDevice = context.isIphone() || context.isIpod();
        boolean declinedAppAlready = (declinedIphoneSplashPageCookie != null && "true".equals(declinedIphoneSplashPageCookie.getValue()));

        // if user is on iphone or ipod touch, and the iphone splash page is enabled,
        // and they're not cookied to not be shown the iphone splash page, and
        // they aren't cookied to see a special hover
        // then redirect them to the iphone splash page
        if (applicableDevice &&
                context.isIphoneSplashPageEnabled() &&
                !declinedAppAlready &&
                !hoverCookieSet) {

            String encodedOrigin = null;
            if (includeReferrer) {
                String origin = request.getRequestURI();
                if (request.getQueryString() != null) {
                    origin += "?" + request.getQueryString();
                }
                try {
                    encodedOrigin = URLEncoder.encode(origin, "UTF-8");
                } catch(UnsupportedEncodingException uee) {
                    // Ignore and leave encodedOrigin null
                }
            }

            String splashUrl = "/splash/iphone.page";
            if (encodedOrigin != null) {
                splashUrl += "?l=" + encodedOrigin;
            }
            return new ModelAndView(new RedirectView(splashUrl));
        }

        return null;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView iPhoneRedirect = checkMobileTraffic(request, response, false);
        if (iPhoneRedirect != null) {
            return iPhoneRedirect;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        CmsHomepage homepage = new CmsHomepage();
        if (CmsUtil.isCmsEnabled()) {
            Collection<CmsHomepage> homepages = _publicationDao.populateAllByContentType("Homepage", new CmsHomepage());
            if (homepages.size() > 0) {
                homepage = homepages.iterator().next();
                try {
                    _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(homepage);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            model.put("homepage", homepage);
            populateModelWithRecentCMSContent(model); // GS-9160

            // GS-9770 if user is authorized and is a cms admin, add raise your hand discussions to model
            if (PageHelper.isMemberAuthorized(request)) {
                insertRaiseYourHandDiscussionsIntoModel(request, model);
            }

            model.put("states", StateManager.getList());
        }

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            pageHelper.addAdKeyword("template", "homepage");
        }

        return new ModelAndView(_viewName, model);
    }

    protected void insertRaiseYourHandDiscussionsIntoModel(HttpServletRequest request, Map<String, Object> model) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        user = sessionContext.getUser();
        if (user != null) {
            if (user.hasPermission(Permission.COMMUNITY_MANAGE_RAISE_YOUR_HAND)) {
                List<RaiseYourHandFeature> featureList = getRaiseYourHandDao().getFeatures(new ContentKey("TopicCenter", 2077l), MAX_RAISE_YOUR_HAND_DISCUSSIONS_FOR_CMSADMIN);
                model.put(MODEL_ALL_RAISE_YOUR_HAND_FOR_TOPIC, featureList);
            }
        }

    }

    public List<String> getIdsFromCategories(List<CmsCategory> categories) {
        List<String> ids = new ArrayList<String>();
        for (CmsCategory category : categories) {
            ids.add(String.valueOf(category.getId()));
        }
        return ids;
    }

    /**
     * A version of populateModelWithRecentCMSContent that makes only one call to Solr instead of accessing Lucene nine times.
     * @param model
     */
    public void populateModelWithRecentCMSContent(Map<String, Object> model) {
        String idList = "198,199,200,201,202,203,204,205,206";
        List<CmsCategory> cats = _cmsCategoryDao.getCmsCategoriesFromIds(idList);
        Map<Long,List<ICmsFeatureSearchResult>> gradeIdToFeatureMap = new HashMap<Long,List<ICmsFeatureSearchResult>>();
        if (cats != null && cats.size() > 0) {
            gradeIdToFeatureMap = getCachedCmsContentForCategoriesUsingSolr(getIdsFromCategories(cats));
        }
        if (cats != null && cats.size() == GRADE_BY_GRADE_NUM_CATEGORIES) {
            Map<String, List<RecentContent>> catToResultMap = new HashMap<String, List<RecentContent>>(GRADE_BY_GRADE_NUM_CATEGORIES);
            for (CmsCategory category : cats) {
                // first get the cms content for the category
                // this returns up to GRADE_BY_GRADE_NUM_CMS_CONTENT pieces of content
                List<ICmsFeatureSearchResult> cmsContentForCat = gradeIdToFeatureMap.get(category.getId());
                // Then try to find GRADE_BY_GRADE_NUM_DISCUSSIONS discussions for that category
                List<Discussion> discussions = getCachedDiscussionsForCategory(category);
                // randomize discussions
                Collections.shuffle(discussions);
                // truncate to two discussions
                if (discussions.size() > GRADE_BY_GRADE_NUM_DISCUSSIONS) {
                    discussions = discussions.subList(0, GRADE_BY_GRADE_NUM_DISCUSSIONS);
                }
                // for each discussion returned, put it in the list, replacing content if necessary
                // to keep the total number of items at GRADE_BY_GRADE_NUM_ITEMS
                List<RecentContent> recentContentList = new ArrayList<RecentContent>(GRADE_BY_GRADE_NUM_ITEMS);
                for (Discussion d : discussions) {
                    RecentContent recentContent = new RecentContent(d, d.getDiscussionBoard().getFullUri());
                    recentContentList.add(recentContent);
                }
                int i = 0;
                while (recentContentList.size() < GRADE_BY_GRADE_NUM_ITEMS && cmsContentForCat != null && i < cmsContentForCat.size()) {
                    ICmsFeatureSearchResult result = cmsContentForCat.get(i++);
                    recentContentList.add(new RecentContent(result));
                }
                if (!recentContentList.isEmpty()) {
                    Collections.shuffle(recentContentList);
                    catToResultMap.put(String.valueOf(category.getId()), recentContentList);
                }
            }
            // don't add to model unless everything worked
            if (catToResultMap.size() == GRADE_BY_GRADE_NUM_CATEGORIES) {
                model.put(MODEL_RECENT_CMS_CONTENT, catToResultMap);
            }
        } else {
            _log.warn("Can't find all " + GRADE_BY_GRADE_NUM_CATEGORIES +
                    " grade level categories using id list " + idList);
        }
    }

    protected List<Discussion> getCachedDiscussionsForCategory(CmsCategory cat) {
        List<Discussion> myDiscussions;

        Element element = _recentDiscussionsCache.get(cat);

        if (element != null) {
            myDiscussions = (List<Discussion>) element.getObjectValue();
        } else {
            myDiscussions = getDiscussionsForCategory(cat);
            _recentDiscussionsCache.put(new Element(cat, myDiscussions));
        }

        return myDiscussions;
    }

    protected List<Discussion> getDiscussionsForCategory(CmsCategory cat) {
        Long topicCenterId = categoryIdToTopicCenterIdMap.get(cat.getId());
        if (topicCenterId != null) {
            CmsTopicCenter topicCenter = _publicationDao.populateByContentId(topicCenterId, new CmsTopicCenter());
            if (topicCenter != null) {
                Long discussionBoardId = topicCenter.getDiscussionBoardId();
                if (discussionBoardId != null) {
                    CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussionBoardId);
                    List<Discussion> myDiscussions =
                            _discussionDao.getDiscussionsForPage(board, 1, 10,
                                    IDiscussionDao.DiscussionSort.RECENT_ACTIVITY, false,
                                    GRADE_BY_GRADE_MIN_NUM_REPLIES);
                    if (myDiscussions != null) {
                        // set the board on each discussion for URL building later
                        for (Discussion d : myDiscussions) {
                            d.setDiscussionBoard(board);
                        }
                        return myDiscussions;
                    } else {
                        _log.warn("No discussions found for " + topicCenter.getTitle() + "'s board");
                    }
                } else {
                    _log.warn("Topic center " + topicCenter.getTitle() + " has no discussion board");
                }
            } else {
                _log.warn("Can't find topic center with id " + topicCenterId + " for category " + cat.getName());
            }
        } else {
            _log.warn("Can't find topic center id for category " + cat.getName() + ":" + cat.getId());
        }
        return new ArrayList<Discussion>(0);
    }

    protected Map<Long,List<ICmsFeatureSearchResult>> getCachedCmsContentForCategoriesUsingSolr(List<String> categoryIds) {
        Map<Long,List<ICmsFeatureSearchResult>> gradeCategoryIdToCmsFeatureResult;

        Element element = _gradeByGradeCache.get(categoryIds);

        if (element != null) {
            gradeCategoryIdToCmsFeatureResult = (Map<Long,List<ICmsFeatureSearchResult>>) element.getObjectValue();
        } else {
            gradeCategoryIdToCmsFeatureResult = getCmsContentForCategoriesUsingSolr(categoryIds);
            _gradeByGradeCache.put(new Element(categoryIds, gradeCategoryIdToCmsFeatureResult));
        }
        
        return gradeCategoryIdToCmsFeatureResult;
    }

    private Map<Long, List<ICmsFeatureSearchResult>> getCmsContentForCategoriesUsingSolr(List<String> categoryIds) {
        Map<Long, List<ICmsFeatureSearchResult>> gradeCategoryIdToCmsFeatureResult = new HashMap<Long, List<ICmsFeatureSearchResult>>();
        GsSolrQuery query = new GsSolrQuery();
        query.filter(DocumentType.CMS_FEATURE);
        query.sort(CmsFeatureFields.FIELD_CMS_DATE_CREATED, true);
        query.query(CmsFeatureFields.FIELD_CMS_GRADE_ID, categoryIds);
        query.page(0,5000);

        try {
            SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = _cmsFeatureSearchService.search(query.getSolrQuery());
            if (searchResultsPage != null) {
                gradeCategoryIdToCmsFeatureResult = aggregateByGradeId(searchResultsPage.getSearchResults());
            }
        } catch (SearchException e) {
            _log.warn("Something went wrong when trying to use CmsFeatureSearchService.", e);
        }
        return gradeCategoryIdToCmsFeatureResult;
    }

    protected Map<Long,List<ICmsFeatureSearchResult>> aggregateByGradeId(List<ICmsFeatureSearchResult> searchResults) {
        Map<Long,List<ICmsFeatureSearchResult>> gradeCategoryIdToCmsFeatureResult
                = new HashMap<Long, List<ICmsFeatureSearchResult>>();
        if (searchResults != null && searchResults.size() > 0) {
            for (ICmsFeatureSearchResult searchResult : searchResults) {
                //each feature might belong to multiple grade IDs.
                //If so we need to add it to each matching list in the map
                List<Long> resultGradeIds = searchResult.getGradeId();
                for (Long id : resultGradeIds) {
                    List<ICmsFeatureSearchResult> resultsForThisId = gradeCategoryIdToCmsFeatureResult.get(id);
                    if (resultsForThisId == null) {
                        resultsForThisId = new ArrayList<ICmsFeatureSearchResult>();
                        gradeCategoryIdToCmsFeatureResult.put(id, resultsForThisId);
                    }
                    if (resultsForThisId.size() < GRADE_BY_GRADE_NUM_CMS_CONTENT) {
                        resultsForThisId.add(searchResult);
                    }
                }
            }
        }
        return gradeCategoryIdToCmsFeatureResult;
    }

    // Used by gradeByGrade module (gradeByGrade.tagx and gradeByGradeList.tagx)
    public static class RecentContent {
        // Used for CSS classes by gradeByGrade module (gradeByGrade.tagx and gradeByGradeList.tagx)
        private enum ContentType {
            cms, discussion
        }

        private int _id;
        private String _contentKey;
        private String _fullUri;
        private String _title;
        private ContentType _contentType;

        public RecentContent(SearchResult cmsResult) {
            _contentType = ContentType.cms;
            _contentKey = cmsResult.getContentKey();
            _fullUri = cmsResult.getFullUri();
            _title = cmsResult.getPromoOrTitle();
        }

        public RecentContent(ICmsFeatureSearchResult cmsResult) {
            _contentType = ContentType.cms;
            _contentKey = cmsResult.getContentKey().toString();
            _fullUri = cmsResult.getFullUri();
            _title = cmsResult.getPromoOrTitle();
        }

        public RecentContent(Discussion discussion, String fullUri) {
            _contentType = ContentType.discussion;
            _contentKey = null;
            _id = discussion.getId();
            _fullUri = fullUri;
            _title = discussion.getTitle();
        }

        public int getId() {
            return _id;
        }

        public String getContentKey() {
            return _contentKey;
        }

        public String getFullUri() {
            return _fullUri;
        }

        public String getTitle() {
            return _title;
        }

        public String getContentType() {
            return _contentType.name();
        }
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public CmsContentLinkResolver getCmsFeatureEmbeddedLinkResolver() {
        return _cmsFeatureEmbeddedLinkResolver;
    }

    public void setCmsFeatureEmbeddedLinkResolver(CmsContentLinkResolver cmsFeatureEmbeddedLinkResolver) {
        _cmsFeatureEmbeddedLinkResolver = cmsFeatureEmbeddedLinkResolver;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public IDiscussionDao getDiscussionDao() {
        return _discussionDao;
    }

    public void setDiscussionDao(IDiscussionDao discussionDao) {
        _discussionDao = discussionDao;
    }

    public IDiscussionReplyDao getDiscussionReplyDao() {
        return _discussionReplyDao;
    }

    public void setDiscussionReplyDao(IDiscussionReplyDao discussionReplyDao) {
        _discussionReplyDao = discussionReplyDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
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

    public IRaiseYourHandDao getRaiseYourHandDao() {
        return _raiseYourHandDao;
    }

    public void setRaiseYourHandDao(IRaiseYourHandDao raiseYourHandDao) {
        _raiseYourHandDao = raiseYourHandDao;
    }

    public CmsFeatureSearchService getSolrCmsFeatureSearchService() {
        return _cmsFeatureSearchService;
    }
    public void setSolrCmsFeatureSearchService(CmsFeatureSearchService cmsFeatureSearchService) {
        _cmsFeatureSearchService = cmsFeatureSearchService;
    }
}
