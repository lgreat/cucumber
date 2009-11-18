package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.community.*;
import gs.data.util.email.MockJavaMailSender;

import static org.easymock.EasyMock.*;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ReportContentAjaxControllerTest extends BaseControllerTestCase {
    private ReportContentAjaxController _controller;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private MockJavaMailSender _mailSender;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new ReportContentAjaxController();

        _cmsDiscussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);
        _userDao = createStrictMock(IUserDao.class);

        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");        

        _controller.setCmsDiscussionBoardDao(_cmsDiscussionBoardDao);
        _controller.setDiscussionDao(_discussionDao);
        _controller.setDiscussionReplyDao(_discussionReplyDao);
        _controller.setUserDao(_userDao);
        _controller.setMailSender(_mailSender);
        getRequest().setServerName("localhost");
    }

    public void replayAllMocks() {
        replayMocks(_cmsDiscussionBoardDao, _discussionDao, _discussionReplyDao, _userDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_cmsDiscussionBoardDao, _discussionDao, _discussionReplyDao, _userDao);
    }

    public void resetAllMocks() {
        resetMocks(_cmsDiscussionBoardDao, _discussionDao, _discussionReplyDao, _userDao);
    }

    public void testBasics() {
        assertSame(_cmsDiscussionBoardDao, _controller.getCmsDiscussionBoardDao());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_discussionReplyDao, _controller.getDiscussionReplyDao());
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_mailSender, _controller.getMailSender());
    }

    public void testGetLinkForDiscussion() {
        Discussion d = new Discussion();
        d.setId(1);
        d.setBoardId(2L);
        CmsDiscussionBoard b = new CmsDiscussionBoard();
        b.setFullUri("/testGetLinkForDiscussion");
        expect(_discussionDao.findById(1)).andReturn(d);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(b);
        replayAllMocks();
        String url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.discussion);
        verifyAllMocks();
        assertEquals("http://localhost/testGetLinkForDiscussion/community/discussion.gs?content=1", url);
    }

    public void testGetLinkForDiscussionNull() {
        expect(_discussionDao.findById(1)).andReturn(null);
        replayAllMocks();
        String url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.discussion);
        verifyAllMocks();
        assertNull(url);

        resetAllMocks();

        expect(_discussionDao.findById(1)).andReturn(new Discussion());
        replayAllMocks();
        url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.discussion);
        verifyAllMocks();
        assertNull(url);

        resetAllMocks();

        Discussion d = new Discussion();
        d.setBoardId(2L);
        expect(_discussionDao.findById(1)).andReturn(d);
        expect(_cmsDiscussionBoardDao.get(2L)).andReturn(null);
        replayAllMocks();
        url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.discussion);
        verifyAllMocks();
        assertNull(url);
    }

    public void testGetLinkForDiscussionReply() {
        DiscussionReply r = new DiscussionReply();
        r.setId(1);
        Discussion d = new Discussion();
        d.setId(2);
        d.setBoardId(3L);
        r.setDiscussion(d);
        CmsDiscussionBoard b = new CmsDiscussionBoard();
        b.setFullUri("/testGetLinkForDiscussionReply");
        expect(_discussionReplyDao.findById(1)).andReturn(r);
        expect(_cmsDiscussionBoardDao.get(3L)).andReturn(b);
        
        replayAllMocks();
        String url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.reply);
        verifyAllMocks();
        assertEquals("http://localhost/testGetLinkForDiscussionReply/community/discussion.gs?content=2&discussionReplyId=1#reply_1", url);
    }

    public void testGetLinkForDiscussionReplyNull() {
        DiscussionReply r = new DiscussionReply();
        r.setId(1);
        Discussion d = new Discussion();
        d.setId(2);
        r.setDiscussion(d);

        expect(_discussionReplyDao.findById(1)).andReturn(null);

        replayAllMocks();
        String url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.reply);
        verifyAllMocks();
        assertNull(url);

        resetAllMocks();

        expect(_discussionReplyDao.findById(1)).andReturn(new DiscussionReply());
        replayAllMocks();
        url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.reply);
        verifyAllMocks();
        assertNull(url);

        resetAllMocks();

        expect(_discussionReplyDao.findById(1)).andReturn(r);
        replayAllMocks();
        url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.reply);
        verifyAllMocks();
        assertNull(url);

        resetAllMocks();

        d.setBoardId(3L);
        expect(_discussionReplyDao.findById(1)).andReturn(r);
        expect(_cmsDiscussionBoardDao.get(3L)).andReturn(null);
        replayAllMocks();
        url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.reply);
        verifyAllMocks();
        assertNull(url);
    }

    public void testGetLinkForMember() {
        User u = new User();
        u.setId(1);
        UserProfile up = new UserProfile();
        up.setScreenName("Anthony");
        u.setUserProfile(up);
        expect(_userDao.findUserFromId(1)).andReturn(u);
        replayAllMocks();
        String url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.member);
        verifyAllMocks();
        assertEquals("http://localhost/members/Anthony/", url);
    }

    public void testGetLinkForMemberNull() {
        User u = new User();
        u.setId(1);
        expect(_userDao.findUserFromId(1)).andThrow(new ObjectRetrievalFailureException("foo", u));
        replayAllMocks();
        String url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.member);
        verifyAllMocks();
        assertNull(url);

        resetAllMocks();

        expect(_userDao.findUserFromId(1)).andReturn(u);
        replayAllMocks();
        url = _controller.getLinkForContent(getRequest(), 1, ReportContentCommand.ReportType.member);
        verifyAllMocks();
        assertNull(url);
    }

    public void testSendMail() {
        User u = new User();
        u.setId(1);
        u.setEmail("aroy@greatschools.net");
        u.setUserProfile(new UserProfile());
        u.getUserProfile().setScreenName("Anthony");

        replayAllMocks();
        _controller.sendEmail("url", ReportContentCommand.ReportType.discussion, u, "reason");
        verifyAllMocks();
        assertNotNull(_mailSender.getSentMessages());
        assertEquals(1, _mailSender.getSentMessages().size());
    }
}
