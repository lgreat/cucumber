package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class StartDiscussionHoverController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String MODEL_DISCUSSION_TOPICS = "discussionTopics";
    public static final String MODEL_TOPIC_CENTER_ID = "topicCenterId";

    private IPublicationDao _publicationDao;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String, Object>();

        populateModelWithListOfValidDiscussionTopics(model);

        model.put(MODEL_TOPIC_CENTER_ID, request.getParameter("topicCenterId"));

        return new ModelAndView(_viewName, model);
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

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
