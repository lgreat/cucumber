package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.data.community.IUserContentDao;
import gs.data.community.Discussion;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

import static org.easymock.EasyMock.*;


public class CommunityLandingControllerTest extends BaseControllerTestCase {
    private CommunityLandingController _controller;
    private String _landingView = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING).asSiteRelative(getRequest());
    private IUserContentDao _mockUserContentDao;
    private ICmsDiscussionBoardDao _mockCmsDiscussionBoardDao;

    public void testBasics() {
        assertEquals("Default view should be community landing page", _landingView, _controller.getViewName());
    }

    public void testNoIdParam() {
        replay(_mockUserContentDao);
        replay(_mockCmsDiscussionBoardDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull("ModelAndView should not be null", mAndV);
        assertEquals("View should be community landing page", _landingView, mAndV.getViewName());

        PageHelper referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("community", "true");

        PageHelper pageHelper = (PageHelper) getRequest().getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        Collection actualCommunityKeywords = (Collection)pageHelper.getAdKeywords().get("community");
        Collection referenceCommunityKeywords = (Collection)referencePageHelper.getAdKeywords().get("community");

        assertEquals(1, actualCommunityKeywords.size());
        assertEquals(actualCommunityKeywords, referenceCommunityKeywords);

        verify(_mockUserContentDao);
        verify(_mockCmsDiscussionBoardDao);
    }

    public void testInvalidIdParam() {
        getRequest().setParameter(CommunityLandingController.PARAM_ID, "x");

        replay(_mockUserContentDao);
        replay(_mockCmsDiscussionBoardDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull("ModelAndView should not be null", mAndV);
        assertTrue("ModelAndView should be a 301 redirect", mAndV.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be community landing page", _landingView, ((RedirectView301) mAndV.getView()).getUrl());

        verify(_mockUserContentDao);
        verify(_mockCmsDiscussionBoardDao);
    }

    public void testRedirect() {
        getRequest().setParameter(CommunityLandingController.PARAM_ID, "6");

        Discussion discussion = new Discussion();
        discussion.setId(1);
        discussion.setBoardId(101L);
        expect(_mockUserContentDao.findByLegacyId(6, Discussion.class)).andReturn(discussion);
        replay(_mockUserContentDao);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setFullUri("/test-board-name");
        expect(_mockCmsDiscussionBoardDao.get(discussion.getBoardId())).andReturn(board);
        replay(_mockCmsDiscussionBoardDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull("ModelAndView should not be null", mAndV);
        assertTrue("ModelAndView should be a 301 redirect", mAndV.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be the discussion url", "/test-board-name/community/discussion.gs?content=1", ((RedirectView301) mAndV.getView()).getUrl());
               
        verify(_mockUserContentDao);
        verify(_mockCmsDiscussionBoardDao);
    }

    public void testNotFound() {
        getRequest().setParameter(CommunityLandingController.PARAM_ID, "9");

        expect(_mockUserContentDao.findByLegacyId(9, Discussion.class)).andReturn(null);
        replay(_mockUserContentDao);
        replay(_mockCmsDiscussionBoardDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull("ModelAndView should not be null", mAndV);
        assertTrue("ModelAndView should be a 301 redirect", mAndV.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be community landing page", _landingView, ((RedirectView301) mAndV.getView()).getUrl());

        verify(_mockUserContentDao);
        verify(_mockCmsDiscussionBoardDao);
    }

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityLandingController();

        _controller.setViewName(_landingView);

        _mockUserContentDao = createStrictMock(IUserContentDao.class);
        _controller.setUserContentDao(_mockUserContentDao);
        _mockCmsDiscussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _controller.setCmsDiscussionBoardDao(_mockCmsDiscussionBoardDao);

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }
}