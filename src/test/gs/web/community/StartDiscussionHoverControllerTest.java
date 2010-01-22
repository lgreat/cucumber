package gs.web.community;

import gs.data.community.Discussion;
import gs.data.community.IDiscussionDao;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.web.BaseControllerTestCase;
import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsTopicCenter;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class StartDiscussionHoverControllerTest extends BaseControllerTestCase {
    private StartDiscussionHoverController _controller;
    private IPublicationDao _publicationDao;
    private ILocalBoardDao _localBoardDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new StartDiscussionHoverController();

        _publicationDao = createStrictMock(IPublicationDao.class);
        _localBoardDao = createStrictMock(ILocalBoardDao.class);
        _cmsDiscussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);

        _controller.setPublicationDao(_publicationDao);
        _controller.setLocalBoardDao(_localBoardDao);
        _controller.setCmsDiscussionBoardDao(_cmsDiscussionBoardDao);
        _controller.setDiscussionDao(_discussionDao);
    }

    private void replayAllMocks() {
        replayMocks(_publicationDao, _localBoardDao, _cmsDiscussionBoardDao, _discussionDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_publicationDao, _localBoardDao, _cmsDiscussionBoardDao, _discussionDao);
    }

    public void testBasics() {
        replayAllMocks();
        assertSame(_publicationDao, _controller.getPublicationDao());
        assertSame(_localBoardDao, _controller.getLocalBoardDao());
        assertSame(_cmsDiscussionBoardDao, _controller.getCmsDiscussionBoardDao());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        verifyAllMocks();
    }

    public void testPopulateModelWithListOfValidDiscussionTopics() {
        Map<String, Object> model = new HashMap<String, Object>();

        CmsTopicCenter topicCenter1 = new CmsTopicCenter();
        topicCenter1.setDiscussionBoardId(1L);
        topicCenter1.setTitle("Z Title");

        CmsTopicCenter topicCenter2 = new CmsTopicCenter();

        CmsTopicCenter topicCenter3 = new CmsTopicCenter();
        topicCenter3.setDiscussionBoardId(2L);
        topicCenter3.setTitle("B Title");

        List<CmsTopicCenter> topicCenters = new ArrayList<CmsTopicCenter>();
        topicCenters.add(topicCenter1);
        topicCenters.add(topicCenter2);
        topicCenters.add(topicCenter3);

        expect(_publicationDao.populateAllByContentType(eq("TopicCenter"), isA(CmsTopicCenter.class))).andReturn(topicCenters);

        replayAllMocks();
        _controller.populateModelWithListOfValidDiscussionTopics(model);
        verifyAllMocks();

        assertNotNull(model.get(StartDiscussionHoverController.MODEL_DISCUSSION_TOPICS));
        Set<CmsTopicCenter> sortedSet = (Set<CmsTopicCenter>) model.get(StartDiscussionHoverController.MODEL_DISCUSSION_TOPICS);
        assertEquals(2, sortedSet.size());
        CmsTopicCenter[] tcArr = new CmsTopicCenter[2];
        tcArr = sortedSet.toArray(tcArr);
        assertEquals("B Title", tcArr[0].getTitle());
        assertEquals("Z Title", tcArr[1].getTitle());
    }

    public void testHandleRequestInternalWithTitle() throws Exception {
        expect(_publicationDao.populateAllByContentType(eq("TopicCenter"), isA(CmsTopicCenter.class))).andReturn(new ArrayList<CmsTopicCenter>());
        expect(_localBoardDao.getLocalBoards()).andReturn(new ArrayList<LocalBoard>());
        expect(_cmsDiscussionBoardDao.get(isA(Set.class))).andReturn(new HashMap<Long, CmsDiscussionBoard>());

        getRequest().setParameter("title", "My title");
        
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals("My title", mAndV.getModel().get(StartDiscussionHoverController.MODEL_TITLE));
    }

    public void testHandleRequestInternalWithEdit() throws Exception {
        expect(_publicationDao.populateAllByContentType(eq("TopicCenter"), isA(CmsTopicCenter.class))).andReturn(new ArrayList<CmsTopicCenter>());
        expect(_localBoardDao.getLocalBoards()).andReturn(new ArrayList<LocalBoard>());
        expect(_cmsDiscussionBoardDao.get(isA(Set.class))).andReturn(new HashMap<Long, CmsDiscussionBoard>());

        getRequest().setParameter("title", "My title");
        getRequest().setParameter("discussionId", "1");

        Discussion d = new Discussion();
        d.setId(1);
        d.setTitle("My real title");
        d.setBody("My body");
        expect(_discussionDao.findById(1)).andReturn(d);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals("My real title", mAndV.getModel().get(StartDiscussionHoverController.MODEL_TITLE));
        assertEquals("My body", mAndV.getModel().get(StartDiscussionHoverController.MODEL_BODY));
        assertEquals(1, mAndV.getModel().get(StartDiscussionHoverController.MODEL_DISCUSSION_ID));
    }
}
