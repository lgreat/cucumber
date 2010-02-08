package gs.web.about;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsTopicCenter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MediaRoomController extends AbstractController {
    public static final long MEDIA_ROOM_TOPIC_ID = 2074L;
    public static final String MODEL_TOPIC_CENTER = "topicCenter";
    private String _viewName;
    private IPublicationDao _publicationDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(MODEL_TOPIC_CENTER, _publicationDao.populateByContentId
                (MEDIA_ROOM_TOPIC_ID, new CmsTopicCenter()));
        
        return new ModelAndView(getViewName(), model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
