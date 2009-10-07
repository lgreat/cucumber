package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.cms.IPublicationDao;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ContentKey;
import gs.data.util.CmsUtil;

import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionSubmissionControllerTest extends BaseControllerTestCase {
    private static final String VALID_LENGTH_REPLY_POST =
            "The body of my post, which is awesome.";
    private static final String SHORT_REPLY_POST =
            "woo";
    private static final String VALID_LENGTH_DISCUSSION_POST =
            "The body of my discussion, which is awesome.";
    private static final String SHORT_DISCUSSION_POST =
            "um";
    private static final String VALID_LENGTH_DISCUSSION_TITLE =
            "The title of my discussion";
    private static final String SHORT_DISCUSSION_TITLE =
            "la";
    private DiscussionSubmissionController _controller;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IPublicationDao _publicationDao;
    private User _user;
    private DiscussionSubmissionCommand _command;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        CmsUtil.enableCms();

        _controller = new DiscussionSubmissionController();

        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);
        _cmsDiscussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);

        _controller.setDiscussionDao(_discussionDao);
        _controller.setDiscussionReplyDao(_discussionReplyDao);
        _controller.setCmsDiscussionBoardDao(_cmsDiscussionBoardDao);
        _controller.setPublicationDao(_publicationDao);

        _user = new User();
        _user.setId(5);
        _user.setPlaintextPassword("password");

        _command = new DiscussionSubmissionCommand();

        getRequest().setServerName("localhost");
        
        SessionContext sessionContext = new SessionContext();
        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        SessionContextUtil scu = new SessionContextUtil();
        sessionContext.setSessionContextUtil(scu);

        CookieGenerator scGen = new CookieGenerator();
        scGen.setCookieName("user_pref");
        scu.setSitePrefCookieGenerator(scGen);
        CookieGenerator otGen = new CookieGenerator();
        otGen.setCookieName("omniture");
        scu.setOmnitureSubCookieGenerator(otGen);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        CmsUtil.disableCms();
    }

    private void replayAllMocks() {
        replayMocks(_discussionDao, _discussionReplyDao, _cmsDiscussionBoardDao, _publicationDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_discussionDao, _discussionReplyDao, _cmsDiscussionBoardDao, _publicationDao);
    }

//    private void resetAllMocks() {
//        resetMocks(_discussionDao, _discussionReplyDao, _cmsDiscussionBoardDao, _publicationDao);
//    }

    public void testBasics() {
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_discussionReplyDao, _controller.getDiscussionReplyDao());
    }

    private void insertUserIntoRequest() {
        try {
            SessionContextUtil.getSessionContext(getRequest()).setUser(_user);
            Cookie comCookie = new Cookie("community_" + SessionContextUtil.getServerName(getRequest()),
                    AuthenticationManager.generateCookieValue(_user));
            Cookie[] cookies = new Cookie[1];
            cookies[0] = comCookie;
            getRequest().setCookies(cookies);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error setting authorized user: " + e);
        }
    }

    public void testHandleDiscussionSubmission() {
        insertUserIntoRequest();

        _command.setBody(VALID_LENGTH_DISCUSSION_POST);
        _command.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setDiscussionBoardId(2L);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("DiscussionBoard", 2L));

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(board);

        Discussion discussion = new Discussion();
        discussion.setBoardId(2L);
        discussion.setBody(VALID_LENGTH_DISCUSSION_POST);
        discussion.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        discussion.setAuthorId(_user.getId());

        _discussionDao.save(eqDiscussion(discussion));

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("redirect", _command.getRedirect());
        assertNotNull(getResponse().getCookie("omniture"));
        assertEquals("events%24%24%3A%24%24event16%3B", getResponse().getCookie("omniture").getValue());
    }

    public void testHandleDiscussionSubmissionNoRedirect() {
        insertUserIntoRequest();

        _command.setBody(VALID_LENGTH_DISCUSSION_POST);
        _command.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        _command.setTopicCenterId(1L);

        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setDiscussionBoardId(2L);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("DiscussionBoard", 2L));
        board.setFullUri("/board");

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(board);

        Discussion discussion = new Discussion();
        discussion.setBoardId(2L);
        discussion.setBody(VALID_LENGTH_DISCUSSION_POST);
        discussion.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        discussion.setAuthorId(_user.getId());

        _discussionDao.save(eqDiscussion(discussion));

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("/board/community.gs?content=2", _command.getRedirect());
    }

    public void testHandleDiscussionSubmissionNoUser() {
        _command.setBody(VALID_LENGTH_REPLY_POST);
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
            fail("Expect to receive an exception because there is no authorized user in the request");
        } catch (IllegalStateException ise) {
            // ok
        }
        verifyAllMocks();
    }

    public void testHandleDiscussionSubmissionWithNoTopicCenter() {
        insertUserIntoRequest();

        _command.setBody(VALID_LENGTH_DISCUSSION_POST);
        _command.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(null);

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
            fail("Expect to receive an exception because topic center id does not exist");
        } catch (IllegalStateException ise) {
            // ok
        }
        verifyAllMocks();
    }

    public void testHandleDiscussionSubmissionWithNoDiscussionBoard() {
        insertUserIntoRequest();

        _command.setBody(VALID_LENGTH_DISCUSSION_POST);
        _command.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setDiscussionBoardId(2L);

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(null);

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
            fail("Expect to receive an exception because topic center id does not exist");
        } catch (IllegalStateException ise) {
            // ok
        }
        verifyAllMocks();
    }

    public void testHandleDiscussionSubmissionWithTooShortTitle() {
        insertUserIntoRequest();

        _command.setBody(VALID_LENGTH_DISCUSSION_POST);
        _command.setTitle(SHORT_DISCUSSION_TITLE);
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setDiscussionBoardId(2L);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("DiscussionBoard", 2L));
        board.setFullUri("/board");

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(board);

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on submission that fails validation: " + ise);
        }
        verifyAllMocks();

        assertEquals("/board/community.gs?content=2", _command.getRedirect());
    }

    public void testHandleDiscussionSubmissionWithTooShortBody() {
        insertUserIntoRequest();

        _command.setBody(SHORT_DISCUSSION_POST);
        _command.setTitle(VALID_LENGTH_DISCUSSION_TITLE);
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setDiscussionBoardId(2L);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("DiscussionBoard", 2L));
        board.setFullUri("/board");

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(board);

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on submission that fails validation: " + ise);
        }
        verifyAllMocks();

        assertEquals("/board/community.gs?content=2", _command.getRedirect());
    }

    public void testHandleDiscussionSubmissionWithTooLongBodyAndTitle() {
        insertUserIntoRequest();

        StringBuffer longBody = new StringBuffer(DiscussionSubmissionController.DISCUSSION_BODY_MAXIMUM_LENGTH);
        for (int x=0; x < DiscussionSubmissionController.DISCUSSION_BODY_MAXIMUM_LENGTH+1; x++) {
            longBody.append(String.valueOf(x % 10));
        }

        StringBuffer longTitle = new StringBuffer(DiscussionSubmissionController.DISCUSSION_TITLE_MAXIMUM_LENGTH);
        for (int x=0; x < DiscussionSubmissionController.DISCUSSION_TITLE_MAXIMUM_LENGTH+1; x++) {
            longTitle.append(String.valueOf(x % 10));
        }

        _command.setBody(longBody.toString());
        _command.setTitle(longTitle.toString());
        _command.setTopicCenterId(1L);
        _command.setRedirect("redirect");

        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setDiscussionBoardId(2L);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("DiscussionBoard", 2L));

        expect(_publicationDao.populateByContentId(eq(1L), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(board);

        Discussion discussion = new Discussion();
        discussion.setBoardId(2L);
        discussion.setBody(StringUtils.abbreviate(longBody.toString(),
                DiscussionSubmissionController.DISCUSSION_BODY_MAXIMUM_LENGTH));
        discussion.setTitle(StringUtils.abbreviate(longTitle.toString(),
                DiscussionSubmissionController.DISCUSSION_TITLE_MAXIMUM_LENGTH));
        discussion.setAuthorId(_user.getId());

        _discussionDao.save(eqDiscussion(discussion));

        replayAllMocks();
        try {
            _controller.handleDiscussionSubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("redirect", _command.getRedirect());
    }

    public void testHandleDiscussionReplySubmission() {
        insertUserIntoRequest();

        Discussion discussion = new Discussion();
        discussion.setId(1);
        _command.setBody(VALID_LENGTH_REPLY_POST);
        _command.setDiscussionId(1);
        _command.setRedirect("redirect");

        expect(_discussionDao.findById(1)).andReturn(discussion);

        DiscussionReply reply = new DiscussionReply();
        reply.setAuthorId(_user.getId());
        reply.setBody(VALID_LENGTH_REPLY_POST);
        reply.setDiscussion(discussion);
        _discussionReplyDao.save(eqDiscussionReply(reply));

        replayAllMocks();
        try {
            _controller.handleDiscussionReplySubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("redirect", _command.getRedirect());
        assertNotNull(getResponse().getCookie("omniture"));
        assertEquals("events%24%24%3A%24%24event17%3B", getResponse().getCookie("omniture").getValue());        
    }

    public void testHandleDiscussionReplySubmissionNoRedirect() {
        insertUserIntoRequest();

        Discussion discussion = new Discussion();
        discussion.setId(1);
        _command.setBody(VALID_LENGTH_REPLY_POST);
        _command.setDiscussionId(1);
        _command.setRedirect(null);

        expect(_discussionDao.findById(1)).andReturn(discussion);

        DiscussionReply reply = new DiscussionReply();
        reply.setAuthorId(_user.getId());
        reply.setBody(VALID_LENGTH_REPLY_POST);
        reply.setDiscussion(discussion);
        _discussionReplyDao.save(eqDiscussionReply(reply));

        replayAllMocks();
        try {
            _controller.handleDiscussionReplySubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("/community/discussion.gs?content=1", _command.getRedirect());
    }

    public void testHandleDiscussionReplySubmissionWithNoUser() {
        _command.setBody(VALID_LENGTH_REPLY_POST);
        _command.setDiscussionId(1);
        _command.setRedirect("redirect");

        replayAllMocks();
        try {
            _controller.handleDiscussionReplySubmission(getRequest(), getResponse(), _command);
            fail("Expect to receive an exception because there is no authorized user in the request");
        } catch (IllegalStateException ise) {
            // ok
        }
        verifyAllMocks();

        assertEquals("redirect", _command.getRedirect());
    }

    public void testHandleDiscussionReplySubmissionWithNoDiscussion() {
        insertUserIntoRequest();

        _command.setBody(VALID_LENGTH_REPLY_POST);
        _command.setDiscussionId(1);
        _command.setRedirect("redirect");

        expect(_discussionDao.findById(1)).andReturn(null);        

        replayAllMocks();
        try {
            _controller.handleDiscussionReplySubmission(getRequest(), getResponse(), _command);
            fail("Expect to receive an exception because there is no authorized user in the request");
        } catch (IllegalStateException ise) {
            // ok
        }
        verifyAllMocks();

        assertEquals("redirect", _command.getRedirect());
    }

    public void testHandleDiscussionReplySubmissionWithTooShortBody() {
        insertUserIntoRequest();

        Discussion discussion = new Discussion();
        discussion.setId(1);
        _command.setBody(SHORT_REPLY_POST);
        _command.setDiscussionId(1);
        _command.setRedirect("redirect");

        expect(_discussionDao.findById(1)).andReturn(discussion);

        replayAllMocks();
        try {
            _controller.handleDiscussionReplySubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("/community/discussion.gs?content=1", _command.getRedirect());
//        assertNotNull(getResponse().getCookie("user_pref"));
    }

    public void testHandleDiscussionReplySubmissionWithTooLongBody() {
        insertUserIntoRequest();

        StringBuffer longBody = new StringBuffer(DiscussionSubmissionController.REPLY_BODY_MAXIMUM_LENGTH);
        for (int x=0; x < DiscussionSubmissionController.REPLY_BODY_MAXIMUM_LENGTH+1; x++) {
            longBody.append(String.valueOf(x % 10));
        }

        Discussion discussion = new Discussion();
        discussion.setId(1);
        _command.setBody(longBody.toString());
        _command.setDiscussionId(1);
        _command.setRedirect("redirect");

        expect(_discussionDao.findById(1)).andReturn(discussion);

        DiscussionReply reply = new DiscussionReply();
        reply.setAuthorId(_user.getId());
        reply.setBody(StringUtils.abbreviate(longBody.toString(),
                DiscussionSubmissionController.REPLY_BODY_MAXIMUM_LENGTH));
        reply.setDiscussion(discussion);
        _discussionReplyDao.save(eqDiscussionReply(reply));

        replayAllMocks();
        try {
            _controller.handleDiscussionReplySubmission(getRequest(), getResponse(), _command);
        } catch (IllegalStateException ise) {
            fail("Should not receive exception on valid submission: " + ise);
        }
        verifyAllMocks();

        assertEquals("redirect", _command.getRedirect());
    }

    public DiscussionReply eqDiscussionReply(DiscussionReply reply) {
        reportMatcher(new DiscussionReplyMatcher(reply));
        return null;
    }

    private class DiscussionReplyMatcher implements IArgumentMatcher {
        DiscussionReply _expected;
        DiscussionReplyMatcher(DiscussionReply expected) {
            _expected = expected;
        }
        public boolean matches(Object oActual) {
            if (!(oActual instanceof DiscussionReply)) {
                return false;
            }
            DiscussionReply actual = (DiscussionReply) oActual;
            if (actual.getId() == null) {actual.setId(1234);} // this mimics the save call in the dao
            return StringUtils.equals(actual.getBody(), _expected.getBody())
                    && actual.getAuthorId().equals(_expected.getAuthorId())
                    && actual.getDiscussion().getId().equals(_expected.getDiscussion().getId());
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("body:").append(_expected.getBody())
                    .append(", authorId:").append(_expected.getAuthorId())
                    .append(", discussionId:").append(_expected.getDiscussion().getId());
        }
    }

    public Discussion eqDiscussion(Discussion discussion) {
        reportMatcher(new DiscussionMatcher(discussion));
        return null;
    }

    private class DiscussionMatcher implements IArgumentMatcher {
        Discussion _expected;
        DiscussionMatcher(Discussion expected) {
            _expected = expected;
        }
        public boolean matches(Object oActual) {
            if (!(oActual instanceof Discussion)) {
                return false;
            }
            Discussion actual = (Discussion) oActual;
            if (actual.getId() == null) {actual.setId(1234);} // this mimics the save call in the dao
            return StringUtils.equals(actual.getBody(), _expected.getBody())
                    && StringUtils.equals(actual.getTitle(), _expected.getTitle())
                    && actual.getAuthorId().equals(_expected.getAuthorId())
                    && actual.getBoardId().equals(_expected.getBoardId());
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("body:").append(_expected.getBody())
                    .append(", title:").append(_expected.getTitle())
                    .append(", authorId:").append(_expected.getAuthorId())
                    .append(", boardId:").append(_expected.getBoardId());
        }
    }
}
