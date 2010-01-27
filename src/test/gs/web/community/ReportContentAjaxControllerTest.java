package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import gs.data.community.*;

import static org.easymock.EasyMock.*;

import javax.servlet.http.Cookie;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ReportContentAjaxControllerTest extends BaseControllerTestCase {
    private ReportContentAjaxController _controller;
    private IUserDao _userDao;
    private IReportContentService _reportContentService;
    private IUserContentDao _userContentDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new ReportContentAjaxController();

        _userDao = createStrictMock(IUserDao.class);
        _userContentDao = createStrictMock(IUserContentDao.class);
        _reportContentService = createStrictMock(IReportContentService.class);

        _controller.setReportContentService(_reportContentService);
        _controller.setUserDao(_userDao);
        _controller.setUserContentDao(_userContentDao);
        getRequest().setServerName("localhost");
    }

    public void replayAllMocks() {
        replayMocks(_reportContentService, _userDao, _userContentDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_reportContentService, _userDao, _userContentDao);
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_userContentDao, _controller.getUserContentDao());
        assertSame(_reportContentService, _controller.getReportContentService());
    }

    public void testReportContent() {
        ReportContentCommand command = new ReportContentCommand();
        command.setContentId(1);
        command.setReason("no reason");
        command.setReporterId(18283);
        command.setType(ReportedEntity.ReportedEntityType.discussion);

        User reporter = new User();
        reporter.setId(18283);
        reporter.setUserProfile(new UserProfile());

        UserContent offensiveContent = new UserContent();
        offensiveContent.setAuthorId(99);

        User reportee = new User();
        reportee.setId(offensiveContent.getAuthorId());
        reportee.setEmail("fido@greatschools.org");
        reportee.setUserProfile(new UserProfile());
        reportee.getUserProfile().setScreenName("fido");

        expect(_userContentDao.findById(command.getContentId())).andReturn(offensiveContent);
        expect(_userDao.findUserFromId(reportee.getId())).andReturn(reportee);

        _reportContentService.reportContent(reporter, reportee, getRequest(), command.getContentId(), command.getType(), command.getReason());
        replayAllMocks();
        try {
            SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
            sc.setUser(reporter);
            String hash = AuthenticationManager.generateCookieValue(reporter);
            sc.setHostName("dev.greatschools.org");
            getRequest().setCookies(new Cookie[] {new Cookie("community_dev", hash)});

            _controller.onSubmit(getRequest(), getResponse(), command, null);
        } catch (Exception e) {
            fail("ReportContentAjaxController should not throw exception: " + e.getMessage());
        }

        verifyAllMocks();
    }
}
