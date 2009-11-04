package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.cms.IPublicationDao;
import gs.data.community.local.LocalBoard;
import gs.data.community.local.ILocalBoardDao;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class StartDiscussionHoverController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String MODEL_DISCUSSION_TOPICS = "discussionTopics";
    public static final String MODEL_LOCAL_BOARDS= "localBoards";
    public static final String MODEL_TOPIC_CENTER_ID = "topicCenterId";
    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";

    private IPublicationDao _publicationDao;
    private ILocalBoardDao _localBoardDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String, Object>();

        populateModelWithListOfValidDiscussionTopics(model);
        populateModelWithListOfValidCities(model);

        model.put(MODEL_TOPIC_CENTER_ID, request.getParameter("topicCenterId"));
        model.put(MODEL_LOCAL_BOARD_ID, request.getParameter("discussionBoardId"));

        return new ModelAndView(_viewName, model);
    }

    protected void populateModelWithListOfValidCities(Map<String, Object> model) {
        List<LocalBoard> localBoards = _localBoardDao.getLocalBoards();
        Set<Long> ids = new HashSet<Long>();
        for (LocalBoard localBoard : localBoards) {
            ids.add(localBoard.getBoardId());
        }

        // Get a mapping of all the board ids to their discussion board objects
        Map<Long, CmsDiscussionBoard> boardMap = _cmsDiscussionBoardDao.get(ids);

        List<LocalBoard> rval = new ArrayList<LocalBoard>();
        for (LocalBoard localBoard: localBoards) {
            CmsDiscussionBoard discussionBoard = boardMap.get(localBoard.getBoardId());
            if (discussionBoard != null) {
                localBoard.setDiscussionBoard(discussionBoard);
                rval.add(localBoard);
            }
        }

        model.put(MODEL_LOCAL_BOARDS, rval);
    }

    protected void populateModelWithListOfValidDiscussionTopics(Map<String, Object> model) {
        Collection<CmsTopicCenter> topicCenters =
                _publicationDao.populateAllByContentType("TopicCenter", new CmsTopicCenter());
        SortedSet<CmsTopicCenter> sortedTopics = new TreeSet<CmsTopicCenter>(new Comparator<CmsTopicCenter>() {
            public int compare(CmsTopicCenter o1, CmsTopicCenter o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        for (CmsTopicCenter topicCenter: topicCenters) {
            if (topicCenter.getDiscussionBoardId() != null && topicCenter.getDiscussionBoardId() > 0) {
                sortedTopics.add(topicCenter);
            }
        }
        model.put(MODEL_DISCUSSION_TOPICS, sortedTopics);
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
