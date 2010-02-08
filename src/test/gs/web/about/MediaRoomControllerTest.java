package gs.web.about;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MediaRoomControllerTest extends BaseControllerTestCase {
    private MediaRoomController _controller;
    private IPublicationDao _publicationDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new MediaRoomController();
        _controller.setViewName("view");

        _publicationDao = createStrictMock(IPublicationDao.class);
        _controller.setPublicationDao(_publicationDao);
    }

    public void testBasics() {
        assertEquals("view", _controller.getViewName());
        assertSame(_publicationDao, _controller.getPublicationDao());
    }

    public void testGetTopicCenter() throws Exception {
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        expect(_publicationDao.populateByContentId(
                eq(MediaRoomController.MEDIA_ROOM_TOPIC_ID), isA(CmsTopicCenter.class)))
                .andReturn(topicCenter);
        replayMocks(_publicationDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyMocks(_publicationDao);
        assertNotNull(mAndV.getModel().get(MediaRoomController.MODEL_TOPIC_CENTER));
        assertSame(topicCenter, mAndV.getModel().get(MediaRoomController.MODEL_TOPIC_CENTER));
    }

    public void testGetTopicCenterNull() throws Exception {
        expect(_publicationDao.populateByContentId(
                eq(MediaRoomController.MEDIA_ROOM_TOPIC_ID), isA(CmsTopicCenter.class)))
                .andReturn(null);
        replayMocks(_publicationDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyMocks(_publicationDao);
        assertNull(mAndV.getModel().get(MediaRoomController.MODEL_TOPIC_CENTER));
    }
}
