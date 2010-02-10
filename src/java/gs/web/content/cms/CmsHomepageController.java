package gs.web.content.cms;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsHomepage;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.util.CmsUtil;
import gs.data.admin.IPropertyDao;
import gs.data.community.*;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class CmsHomepageController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    public static final String BEAN_ID = "/index.page";

    public static final int RAISE_YOUR_HAND_NUM_REPLIES = 5;

    public static final String MODEL_RAISE_YOUR_HAND_DISCUSSION = "ryhDiscussion";
    public static final String MODEL_RAISE_YOUR_HAND_REPLIES = "ryhReplies";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_VALID_USER = "validUser";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";

    private IPublicationDao _publicationDao;
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;
    private IPropertyDao _propertyDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;

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
                                discussion, 1, RAISE_YOUR_HAND_NUM_REPLIES,
                                IDiscussionReplyDao.DiscussionReplySort.NEWEST_FIRST, false);
                        userContents.addAll(replies);

                        discussion.setDiscussionBoard(discussionBoard);
                        _userDao.populateWithUsers(userContents);

                        model.put(MODEL_RAISE_YOUR_HAND_DISCUSSION, discussion);
                        model.put(MODEL_RAISE_YOUR_HAND_REPLIES, replies);
                        model.put(MODEL_CURRENT_DATE, new Date());

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
}
