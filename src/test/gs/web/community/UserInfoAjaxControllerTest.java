package gs.web.community;

import gs.data.community.*;
import gs.data.school.EspMembershipDaoHibernate;
import gs.data.school.IEspMembershipDao;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;

import static org.easymock.EasyMock.*;
import org.springframework.web.util.CookieGenerator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class UserInfoAjaxControllerTest extends BaseControllerTestCase {
    private UserInfoAjaxController _controller;
    private IUserDao _userDao;
    private IAlertWordDao _alertWordDao;
    private IReportContentService _reportContentService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new UserInfoAjaxController();

        _userDao = createStrictMock(IUserDao.class);
        _alertWordDao = createStrictMock(IAlertWordDao.class);
        _reportContentService = createStrictMock(IReportContentService.class);

        _controller.setUserDao(_userDao);
        _controller.setAlertWordDao(_alertWordDao);
        _controller.setReportContentService(_reportContentService);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao, _alertWordDao, _reportContentService);
    }

    private void replayAllMocks() {
        replayMocks(_userDao, _alertWordDao, _reportContentService);
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_alertWordDao, _controller.getAlertWordDao());
        assertSame(_reportContentService, _controller.getReportContentService());
    }

    public void testNoSessionContext() throws Exception {
        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, null);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNoUser() throws Exception {
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNoUserProfile() throws Exception {
        User user = new User();
        user.setEmail("testNoUserProfile@greatschools.org");
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNoAboutMe() throws Exception {
        User user = new User();
        user.setEmail("testNoUserProfile@greatschools.org");
        UserProfile userProfile = new UserProfile();
        userProfile.setAboutMe("duh");
        user.setUserProfile(userProfile);
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testNotAuthed() throws Exception {
        User user = new User();
        user.setEmail("testNoUserProfile@greatschools.org");
        UserProfile userProfile = new UserProfile();
        userProfile.setAboutMe("duh");
        user.setUserProfile(userProfile);
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        getRequest().setParameter(UserInfoAjaxController.PARAM_ABOUT_ME, "This is about me.");

        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testHandlePost() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("testNoUserProfile@greatschools.org");
        user.setPlaintextPassword("password");
        UserProfile userProfile = new UserProfile();
        userProfile.setAboutMe("duh");
        user.setUserProfile(userProfile);
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        SessionContextUtil scu = new SessionContextUtil();

        CookieGenerator memIdCG = new CookieGenerator();
        memIdCG.setCookieName("MEMID");
        scu.setMemberIdCookieGenerator(memIdCG);

        CookieGenerator newMemCG = new CookieGenerator();
        newMemCG.setCookieName("isMember");
        scu.setNewMemberCookieGenerator(newMemCG);

        CookieGenerator sessionCacheCG = new CookieGenerator();
        sessionCacheCG.setCookieName("session_cache");
        scu.setSessionCacheCookieGenerator(sessionCacheCG);

        CookieGenerator communityCG = new CookieGenerator();
        communityCG.setCookieName("community_" + SessionContextUtil.getServerName(getRequest()));
        scu.setCommunityCookieGenerator(communityCG);

        sc.setSessionContextUtil(scu);
        sc.setUser(user);
        PageHelper.setMemberAuthorized(getRequest(), getResponse(), user);
        getRequest().setCookies(getResponse().getCookies());
        getRequest().setParameter(UserInfoAjaxController.PARAM_ABOUT_ME, "This is about me.");
        getRequest().setParameter(UserInfoAjaxController.PARAM_MEMBER_ID, "1");

        _userDao.saveUser(user);
        expect(_alertWordDao.hasAlertWord(getRequest().getParameter(UserInfoAjaxController.PARAM_ABOUT_ME))).andReturn(null);
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("This is about me.", user.getUserProfile().getAboutMe());
    }

    public void testHandlePostWithAlertWords() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("testNoUserProfile@greatschools.org");
        user.setPlaintextPassword("password");
        UserProfile userProfile = new UserProfile();
        userProfile.setAboutMe("duh");
        userProfile.setScreenName("bob");
        user.setUserProfile(userProfile);
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        SessionContextUtil scu = new SessionContextUtil();

        CookieGenerator memIdCG = new CookieGenerator();
        memIdCG.setCookieName("MEMID");
        scu.setMemberIdCookieGenerator(memIdCG);

        CookieGenerator newMemCG = new CookieGenerator();
        newMemCG.setCookieName("isMember");
        scu.setNewMemberCookieGenerator(newMemCG);

        CookieGenerator sessionCacheCG = new CookieGenerator();
        sessionCacheCG.setCookieName("session_cache");
        scu.setSessionCacheCookieGenerator(sessionCacheCG);

        CookieGenerator communityCG = new CookieGenerator();
        communityCG.setCookieName("community_" + SessionContextUtil.getServerName(getRequest()));
        scu.setCommunityCookieGenerator(communityCG);

        sc.setSessionContextUtil(scu);
        sc.setUser(user);
        PageHelper.setMemberAuthorized(getRequest(), getResponse(), user);
        getRequest().setCookies(getResponse().getCookies());
        getRequest().setParameter(UserInfoAjaxController.PARAM_ABOUT_ME, "This is some nasty text.");
        getRequest().setParameter(UserInfoAjaxController.PARAM_MEMBER_ID, "1");

        _userDao.saveUser(user);

        expect(_alertWordDao.hasAlertWord(getRequest().getParameter(UserInfoAjaxController.PARAM_ABOUT_ME)))
                .andReturn("foo");
        User reporter = new User();
        reporter.setId(-1);
        reporter.setEmail("moderation@greatschools.org");
//        reporter.setUserProfile(new UserProfile());
//        reporter.getUserProfile().setScreenName("gs_alert_word_filter");
        expect(_reportContentService.getModerationEmail()).andReturn("moderation@greatschools.org");
        _reportContentService.reportContent(reporter, user, getRequest(), 1,
                                            ReportedEntity.ReportedEntityType.member, "Bio contains alert word \"foo\"");
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("Expect bio to be posted anyway", "This is some nasty text.", user.getUserProfile().getAboutMe());
    }
}
