package gs.web.content.cms;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.*;
import gs.data.search.Indexer;
import gs.data.search.Searcher;
import gs.data.util.CmsUtil;
import gs.data.admin.IPropertyDao;
import gs.data.community.*;
import gs.web.search.ResultsPager;
import gs.web.search.SearchResult;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class CmsHomepageController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    public static final String BEAN_ID = "/index.page";

    public static final int RAISE_YOUR_HAND_MAX_NUM_REPLIES = 5;
    public static final int GRADE_BY_GRADE_NUM_CATEGORIES = 9;
    public static final int GRADE_BY_GRADE_NUM_CMS_CONTENT = 6;
    public static final int GRADE_BY_GRADE_NUM_ITEMS = 6;
    public static final int GRADE_BY_GRADE_NUM_DISCUSSIONS = 2;

    public static final Map<Long, Long> categoryIdToTopicCenterIdMap = new HashMap<Long, Long>(GRADE_BY_GRADE_NUM_CATEGORIES);

    public static final String MODEL_RAISE_YOUR_HAND_DISCUSSION = "ryhDiscussion";
    public static final String MODEL_RAISE_YOUR_HAND_REPLIES = "ryhReplies";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_VALID_USER = "validUser";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";
    public static final String MODEL_RECENT_CMS_CONTENT = "recentCmsContent";
    public static final String MODEL_RAISE_YOUR_HAND_MAX_NUM_REPLIES = "ryhMaxNumReplies";

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
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
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

            String raiseYourHandDiscussionId = _propertyDao.getProperty(IPropertyDao.RAISE_YOUR_HAND_DISCUSSION_ID);
            if (raiseYourHandDiscussionId != null) {
                try {
                    int discussionId = Integer.parseInt(raiseYourHandDiscussionId);
                    Discussion discussion = _discussionDao.findById(discussionId);
                    if (discussion != null) {
                        List<UserContent> userContents = new ArrayList<UserContent>();
                        userContents.add(discussion);
                        CmsDiscussionBoard discussionBoard = _cmsDiscussionBoardDao.get(discussion.getBoardId());
                        List<DiscussionReply> replies = _discussionReplyDao.getRepliesForPage(
                                discussion, 1, RAISE_YOUR_HAND_MAX_NUM_REPLIES,
                                IDiscussionReplyDao.DiscussionReplySort.NEWEST_FIRST, false);
                        userContents.addAll(replies);

                        discussion.setDiscussionBoard(discussionBoard);
                        _userDao.populateWithUsers(userContents);

                        model.put(MODEL_RAISE_YOUR_HAND_DISCUSSION, discussion);
                        model.put(MODEL_RAISE_YOUR_HAND_REPLIES, replies);
                        model.put(MODEL_CURRENT_DATE, new Date());
                        model.put(MODEL_RAISE_YOUR_HAND_MAX_NUM_REPLIES, RAISE_YOUR_HAND_MAX_NUM_REPLIES);

                        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
                        User user = null;
                        if (PageHelper.isMemberAuthorized(request)) {
                            user = sessionContext.getUser();
                            if (user != null) {
                                model.put(MODEL_VALID_USER, user);
                            }
                        }
                        if (user == null) {
                            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, "/");
                            model.put(MODEL_LOGIN_REDIRECT, urlBuilder.asSiteRelative(request));
                        } else {
                            model.put(MODEL_LOGIN_REDIRECT, "#");
                        }
                    } else {
                        _log.warn("RAISE_YOUR_HAND_DISCUSSION_ID property specifies invalid discussion ID " + raiseYourHandDiscussionId);
                    }

                } catch (NumberFormatException e) {
                    _log.warn("RAISE_YOUR_HAND_DISCUSSION_ID property specifies malformatted number " + raiseYourHandDiscussionId);
                }
            }
        }

        return new ModelAndView(_viewName, model);
    }

    public void populateModelWithRecentCMSContent(Map<String, Object> model) {
        String idList = "198,199,200,201,202,203,204,205,206";
        List<CmsCategory> cats = _cmsCategoryDao.getCmsCategoriesFromIds(idList);
        if (cats != null && cats.size() == GRADE_BY_GRADE_NUM_CATEGORIES) {
            Map<String, List<RecentContent>> catToResultMap = new HashMap<String, List<RecentContent>>(GRADE_BY_GRADE_NUM_CATEGORIES);
            for (CmsCategory category: cats) {
                // first get the cms content for the category
                // this returns up to GRADE_BY_GRADE_NUM_CMS_CONTENT pieces of content
                List<Object> cmsContentForCat = getCmsContentForCategory(category);
                // Then try to find GRADE_BY_GRADE_NUM_DISCUSSIONS discussions for that category
                List<Discussion> discussions = getDiscussionsForCat(category);
                // for each discussion returned, put it in the list, replacing content if necessary
                // to keep the total number of items at GRADE_BY_GRADE_NUM_ITEMS
                List<RecentContent> recentContentList = new ArrayList<RecentContent>(GRADE_BY_GRADE_NUM_ITEMS);
                for (Discussion d: discussions) {
                    RecentContent recentContent = new RecentContent(d, "TODO");
                    recentContentList.add(recentContent);
                }
                while (recentContentList.size() < GRADE_BY_GRADE_NUM_ITEMS && !cmsContentForCat.isEmpty()) {
                    SearchResult result = (SearchResult) cmsContentForCat.remove(0);
                    recentContentList.add(new RecentContent(result));
                }
                if (!recentContentList.isEmpty()) {
                    catToResultMap.put(category.getName(), recentContentList);
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

    protected List<Discussion> getDiscussionsForCat(CmsCategory cat) {
        Long topicCenterId = categoryIdToTopicCenterIdMap.get(cat.getId());
        if (topicCenterId != null) {
            CmsTopicCenter topicCenter = _publicationDao.populateByContentId(topicCenterId, new CmsTopicCenter());
            if (topicCenter != null) {
                Long discussionBoardId = topicCenter.getDiscussionBoardId();
                if (discussionBoardId != null) {
                    CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussionBoardId);
                    List<Discussion> myDiscussions =
                            _discussionDao.getDiscussionsForPage(board, 1, 10,
                                                                 IDiscussionDao.DiscussionSort.NEWEST_FIRST);
                    if (myDiscussions != null) {
                        if (myDiscussions.size() <= GRADE_BY_GRADE_NUM_DISCUSSIONS) {
                            return myDiscussions;
                        } else {
                            // pick GRADE_BY_GRADE_NUM_DISCUSSIONS to return
                            return myDiscussions.subList(0, GRADE_BY_GRADE_NUM_DISCUSSIONS);
                        }
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

    protected List<Object> getCmsContentForCategory(CmsCategory category) {
        TermQuery term = new TermQuery(new Term(Indexer.CMS_GRADE_ID, String.valueOf(category.getId())));
        Filter filterOnlyCmsFeatures = new CachingWrapperFilter(new QueryFilter(new TermQuery(
                new Term(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_CMS_FEATURE))));
        Sort sortByDateCreatedDescending = new Sort(new SortField(Indexer.CMS_DATE_CREATED, SortField.STRING, true));
        Hits hits = _searcher.search(term, sortByDateCreatedDescending, null, filterOnlyCmsFeatures);
        if (hits != null && hits.length() > 0) {
            ResultsPager resultsPager = new ResultsPager(hits, ResultsPager.ResultType.topic);
            return resultsPager.getResults(1, GRADE_BY_GRADE_NUM_CMS_CONTENT);
        } else {
            _log.warn("Can't find any search results for category " + category.getName());
        }
        return new ArrayList<Object>(0);
    }

    public static class RecentContent {
        private enum ContentType {cms, discussion}

        private int _id;
        private String _contentKey;
        private String _fullUri;
        private String _title;
        private ContentType _contentType;

        public RecentContent(SearchResult cmsResult) {
            _contentType = ContentType.cms;
            _contentKey = cmsResult.getContentKey();
            _fullUri = cmsResult.getFullUri();
            _title = cmsResult.getHeadline();
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
}
