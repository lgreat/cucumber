package gs.web.community;

import gs.data.community.*;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;
import static gs.data.community.ReportedEntity.ReportedEntityType.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ReportContentServiceTest extends BaseControllerTestCase {
    private ReportContentService _service;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private IReportedEntityDao _reportedEntityDao;
    private MockJavaMailSender _mailSender;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _service = new ReportContentService();

        _cmsDiscussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);

        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.org");

        _service.setCmsDiscussionBoardDao(_cmsDiscussionBoardDao);
        _service.setDiscussionDao(_discussionDao);
        _service.setDiscussionReplyDao(_discussionReplyDao);
        _service.setUserDao(_userDao);
        _service.setReportedEntityDao(_reportedEntityDao);
        _service.setMailSender(_mailSender);
        _service.setModerationEmail("ReportContentServiceTest@greatschools.org");
    }

    public void replayAllMocks() {
        super.replayMocks(_cmsDiscussionBoardDao, _discussionDao, _discussionReplyDao, _userDao, _reportedEntityDao);
    }

    public void verifyAllMocks() {
        super.verifyMocks(_cmsDiscussionBoardDao, _discussionDao, _discussionReplyDao, _userDao, _reportedEntityDao);
    }

    public void testBasics() {
        replayAllMocks();
        assertSame(_cmsDiscussionBoardDao, _service.getCmsDiscussionBoardDao());
        assertSame(_discussionDao, _service.getDiscussionDao());
        assertSame(_discussionReplyDao, _service.getDiscussionReplyDao());
        assertSame(_userDao, _service.getUserDao());
        assertSame(_reportedEntityDao, _service.getReportedEntityDao());
        assertSame(_mailSender, _service.getMailSender());
        assertEquals("ReportContentServiceTest@greatschools.org", _service.getModerationEmail());
        verifyAllMocks();
    }

    private User getUser(int id, String screenName, String email) {
        User user = new User();
        user.setId(id);
        user.setUserProfile(new UserProfile());
        user.getUserProfile().setScreenName(screenName);
        user.setEmail(email);
        return user;
    }

    public void testReportDiscussion() {
        User reporter = getUser(1, "Reporter", "reporter@greatschools.org");
        User reportee = getUser(2, "Reportee", "reportee@greatschools.org");

        Discussion d = new Discussion();
        d.setId(1);
        d.setBoardId(2l);
        expect(_discussionDao.findById(1)).andReturn(d);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        expect(_cmsDiscussionBoardDao.get(2l)).andReturn(board);

        expect(_reportedEntityDao.getNumberTimesReported(ReportedEntity.ReportedEntityType.discussion, 1)).andReturn(0);
        _reportedEntityDao.reportEntity(reporter, ReportedEntity.ReportedEntityType.discussion, 1);
        replayAllMocks();
        _service.reportContent(reporter, reportee, getRequest(), 1,
                               discussion, "because");
        verifyAllMocks();
    }

    public void testReportDiscussionAutoDisable() {
        User reporter = getUser(1, "Reporter", "reporter@greatschools.org");
        User reportee = getUser(2, "Reportee", "reportee@greatschools.org");

        Discussion d = new Discussion();
        d.setId(1);
        d.setBoardId(2l);
        expect(_discussionDao.findById(1)).andReturn(d);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        expect(_cmsDiscussionBoardDao.get(2l)).andReturn(board);

        expect(_reportedEntityDao.getNumberTimesReported(ReportedEntity.ReportedEntityType.discussion, 1)).andReturn(1);
        _reportedEntityDao.reportEntity(reporter, ReportedEntity.ReportedEntityType.discussion, 1);
        _discussionDao.save(d);
        replayAllMocks();
        _service.reportContent(reporter, reportee, getRequest(), 1,
                               discussion, "because");
        verifyAllMocks();
    }

    public void testReportDiscussionReply() {
        User reporter = getUser(1, "Reporter", "reporter@greatschools.org");
        User reportee = getUser(2, "Reportee", "reportee@greatschools.org");

        DiscussionReply reply = new DiscussionReply();
        reply.setId(1);
        Discussion d = new Discussion();
        d.setId(2);
        d.setBoardId(3l);
        reply.setDiscussion(d);
        expect(_discussionReplyDao.findById(1)).andReturn(reply);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        expect(_cmsDiscussionBoardDao.get(3l)).andReturn(board);

        expect(_reportedEntityDao.getNumberTimesReported(ReportedEntity.ReportedEntityType.reply, 1)).andReturn(0);
        _reportedEntityDao.reportEntity(reporter, ReportedEntity.ReportedEntityType.reply, 1);
        replayAllMocks();
        _service.reportContent(reporter, reportee, getRequest(), 1,
                               ReportedEntity.ReportedEntityType.reply, "because");
        verifyAllMocks();
    }

    public void testReportDiscussionReplyAutoDisable() {
        User reporter = getUser(1, "Reporter", "reporter@greatschools.org");
        User reportee = getUser(2, "Reportee", "reportee@greatschools.org");

        DiscussionReply reply = new DiscussionReply();
        reply.setId(1);
        Discussion d = new Discussion();
        d.setId(2);
        d.setBoardId(3l);
        reply.setDiscussion(d);
        expect(_discussionReplyDao.findById(1)).andReturn(reply);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        expect(_cmsDiscussionBoardDao.get(3l)).andReturn(board);

        expect(_reportedEntityDao.getNumberTimesReported(ReportedEntity.ReportedEntityType.reply, 1)).andReturn(1);
        _reportedEntityDao.reportEntity(reporter, ReportedEntity.ReportedEntityType.reply, 1);
        _discussionReplyDao.save(reply);
        replayAllMocks();
        _service.reportContent(reporter, reportee, getRequest(), 1,
                               ReportedEntity.ReportedEntityType.reply, "because");
        verifyAllMocks();
    }

    public void testReportMember() {
        User reporter = getUser(1, "Reporter", "reporter@greatschools.org");
        User reportee = getUser(2, "Reportee", "reportee@greatschools.org");

        expect(_userDao.findUserFromId(2)).andReturn(reportee);

        _reportedEntityDao.reportEntity(reporter, ReportedEntity.ReportedEntityType.member, 2);

        replayAllMocks();
        _service.reportContent(reporter, reportee, getRequest(), 2,
                               member, "because");
        verifyAllMocks();
    }

}
