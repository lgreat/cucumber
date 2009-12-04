package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsTopicCenter;

import static org.easymock.EasyMock.*;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class StartDiscussionHoverControllerTest extends BaseControllerTestCase {
    private StartDiscussionHoverController _controller;
    private IPublicationDao _publicationDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new StartDiscussionHoverController();

        _publicationDao = createStrictMock(IPublicationDao.class);

        _controller.setPublicationDao(_publicationDao);
    }

    private void replayAllMocks() {
        replayMocks(_publicationDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_publicationDao);
    }

    public void testBasics() {
        replayAllMocks();
        assertSame(_publicationDao, _controller.getPublicationDao());
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
}
