package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;

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
    private DiscussionSubmissionController _controller;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private User _user;
    private DiscussionSubmissionCommand _command;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new DiscussionSubmissionController();

        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);

        _controller.setDiscussionDao(_discussionDao);
        _controller.setDiscussionReplyDao(_discussionReplyDao);

        _user = new User();
        _user.setId(5);
        _user.setPlaintextPassword("password");

        _command = new DiscussionSubmissionCommand();

        SessionContext sessionContext = new SessionContext();
        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        SessionContextUtil scu = new SessionContextUtil();
        sessionContext.setSessionContextUtil(scu);

        CookieGenerator scGen = new CookieGenerator();
        scGen.setCookieName("user_pref");
        scu.setSitePrefCookieGenerator(scGen);
    }

    private void replayAllMocks() {
        replayMocks(_discussionDao, _discussionReplyDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_discussionDao, _discussionReplyDao);
    }

//    private void resetAllMocks() {
//        resetMocks(_discussionDao, _discussionReplyDao);
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
        assertNotNull(getResponse().getCookie("user_pref"));
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

}
