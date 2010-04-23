package gs.web.community;

import gs.data.community.Discussion;
import gs.data.community.IDiscussionDao;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.util.HtmlUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class StartDiscussionHoverController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String MODEL_DISCUSSION_TOPICS = "discussionTopics";
    public static final String MODEL_LOCAL_BOARDS= "localBoards";
    public static final String MODEL_TOPIC_CENTER_ID = "topicCenterId";
    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_BODY = "discussionBody";
    public static final String MODEL_TITLE = "discussionTitle";
    public static final String MODEL_DISCUSSION_ID = "discussionId";
    public static final String MODEL_SELECT_GENERAL_PARENTING_BOARD = "selectGeneralParentingBoard";

    private IPublicationDao _publicationDao;
    private ILocalBoardDao _localBoardDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private String _viewName;

    private void logDuration(long durationInMillis, String eventName) {
        float seconds = ((float)durationInMillis)/1000f;
        _log.info(eventName + " took " + seconds + " seconds");
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String, Object>();

        _log.info("---------BEGIN PROFILING FOR StartDiscussionHoverController--------");
        long startTime = System.currentTimeMillis();
        populateModelWithListOfValidDiscussionTopics(model);
        logDuration(System.currentTimeMillis() - startTime, "Determining list of valid discussion topics");
        startTime = System.currentTimeMillis();
        populateModelWithListOfValidCities(model);
        logDuration(System.currentTimeMillis() - startTime, "Determining list of valid discussion local cities");
        _log.info("---------END PROFILING FOR StartDiscussionHoverController--------");

        model.put(MODEL_TOPIC_CENTER_ID, request.getParameter("topicCenterId"));
        model.put(MODEL_LOCAL_BOARD_ID, request.getParameter("discussionBoardId"));
        model.put(MODEL_SELECT_GENERAL_PARENTING_BOARD, request.getParameter("selectGeneralParentingBoard"));
        if (request.getParameter("title") != null) {
            model.put(MODEL_TITLE, request.getParameter("title"));
        }

        if (request.getParameter("discussionId") != null) {
            try {
                Discussion d = _discussionDao.findById(Integer.parseInt(request.getParameter("discussionId")));
                if (d != null) {
                    model.put(MODEL_TITLE, d.getTitle());
                    model.put(MODEL_DISCUSSION_ID, d.getId());
                    String body = d.getBody();
                    if (StringUtils.isNotBlank(body)) {
                        body = StringUtils.replace(body, "<br/>", "\n");
                        body = StringUtils.replace(body, "<br>", "\n");
                        body = HtmlUtils.htmlUnescape(body);
                        model.put(MODEL_BODY, body);
                    }
                }
            } catch (Exception e) {
                _log.error("Error retrieving discussion " + request.getParameter("discussionId") +
                        ": " + e, e);
            }
        }

        return new ModelAndView(_viewName, model);
    }

    protected void populateModelWithListOfValidCities(Map<String, Object> model) {
        long startTime = System.currentTimeMillis();
        List<LocalBoard> localBoards = _localBoardDao.getLocalBoards();
        logDuration(System.currentTimeMillis() - startTime, "Retrieving all local boards");
        Set<Long> ids = new HashSet<Long>();
        for (LocalBoard localBoard : localBoards) {
            ids.add(localBoard.getBoardId());
        }

        // Get a mapping of all the board ids to their discussion board objects
        startTime = System.currentTimeMillis();
        Map<Long, CmsDiscussionBoard> boardMap = _cmsDiscussionBoardDao.get(ids);
        logDuration(System.currentTimeMillis() - startTime, "Retrieving set of " + ids.size() + " discussion boards");

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
        // TODO: this call pulls all topic center data out of the db, when we only need some topic centers.
        // When running on localhost there is significant network traffic during this call (some 5MB) and the delay
        // is noticeable.  Can we make a DAO method that 
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

    public IDiscussionDao getDiscussionDao() {
        return _discussionDao;
    }

    public void setDiscussionDao(IDiscussionDao discussionDao) {
        _discussionDao = discussionDao;
    }
}
