package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.community.*;
import gs.data.cms.IPublicationDao;

import java.util.*;

public class RaiseYourHandRecentQuestionsController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IPublicationDao _publicationDao;
    private IUserDao _userDao;
    private IRaiseYourHandDao _raiseYourHandDao;

    public static final int MAX_RYH_PER_TOPIC = 7;
    public static final Long[] TOPIC_DISCUSSION_BOARD_IDS_COL1 = new Long[] {1636L, 1637L, 1638L, 1639L};
    public static final Long[] TOPIC_DISCUSSION_BOARD_IDS_COL2 = new Long[] {1640L, 1641L, 1642L, 1643L};
    public static final ContentKey HOME_PAGE_CONTENT_KEY = new ContentKey("TopicCenter#2077");

    public static final String MODEL_DISCUSSION_BOARDS_COL1 = "discussionBoardsCol1";
    public static final String MODEL_DISCUSSION_BOARDS_COL2 = "discussionBoardsCol2";
    public static final String MODEL_DISCUSSION_LISTS_COL1 = "discussionListsCol1";
    public static final String MODEL_DISCUSSION_LISTS_COL2 = "discussionListsCol2";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        List<CmsDiscussionBoard> dbCol1 = new ArrayList<CmsDiscussionBoard>();
        List<CmsDiscussionBoard> dbCol2 = new ArrayList<CmsDiscussionBoard>();

        List<List<Discussion>> ryhCol1 = new ArrayList<List<Discussion>>();
        List<List<Discussion>> ryhCol2 = new ArrayList<List<Discussion>>();

        // featured questions

        // fake discussion board for featured questions, so we don't have to special case front-end code too much
        CmsDiscussionBoard cmsDiscussionBoard = new CmsDiscussionBoard();
        cmsDiscussionBoard.setTitle("Featured Questions");
        dbCol1.add(cmsDiscussionBoard);
        List<RaiseYourHandFeature> features = _raiseYourHandDao.getFeatures(HOME_PAGE_CONTENT_KEY, MAX_RYH_PER_TOPIC);
        ryhCol1.add(convertRYHFeaturesToDiscussions(features));

        // rest of column 1
        populateRecentRYH(TOPIC_DISCUSSION_BOARD_IDS_COL1, dbCol1, ryhCol1);

        // column 2
        populateRecentRYH(TOPIC_DISCUSSION_BOARD_IDS_COL2, dbCol2, ryhCol2);

        model.put(MODEL_DISCUSSION_BOARDS_COL1, dbCol1);
        model.put(MODEL_DISCUSSION_BOARDS_COL2, dbCol2);
        model.put(MODEL_DISCUSSION_LISTS_COL1, ryhCol1);
        model.put(MODEL_DISCUSSION_LISTS_COL2, ryhCol2);

        return new ModelAndView(_viewName, model);
    }

    private void populateRecentRYH(Long[] discussionBoardIDs, List<CmsDiscussionBoard> boards, List<List<Discussion>> discussionLists) {
        for (long discussionBoardId : discussionBoardIDs) {
            CmsDiscussionBoard cmsDiscussionBoard = _cmsDiscussionBoardDao.get(discussionBoardId);
            boards.add(cmsDiscussionBoard);

            List<Discussion> discussions = _discussionDao.getDiscussionsForPage(cmsDiscussionBoard, 1, MAX_RYH_PER_TOPIC, IDiscussionDao.DiscussionSort.NEWEST_RAISE_YOUR_HAND_DATE, false, true);
            discussionLists.add(discussions);
        }
    }

    private List<Discussion> convertRYHFeaturesToDiscussions(List<RaiseYourHandFeature> features) {
        List<Discussion> discussions = new ArrayList<Discussion>();
        for (RaiseYourHandFeature feature : features) {
            discussions.add(feature.getDiscussion());
        }
        return discussions;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
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

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IRaiseYourHandDao getRaiseYourHandDao() {
        return _raiseYourHandDao;
    }

    public void setRaiseYourHandDao(IRaiseYourHandDao raiseYourHandDao) {
        _raiseYourHandDao = raiseYourHandDao;
    }
}
