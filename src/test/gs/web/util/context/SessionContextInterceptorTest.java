package gs.web.util.context;

import gs.web.util.PageHelper;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.state.State;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author thuss
 */
public class SessionContextInterceptorTest extends BaseControllerTestCase {
    
    private SessionContextInterceptor _sci;

    public void setUp() throws Exception {
        _sci = new SessionContextInterceptor();
        SessionContextUtil ctxUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
        _sci.setSessionContextUtil(ctxUtil);
        super.setUp();
    }

    public void testInterceptor() throws Exception {
        _request.removeAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        _request.removeAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        // Run the interceptor with no cookies
        assertTrue(_sci.preHandle(_request, _response, null));
        _sci.afterCompletion(null, null, null, null);
        _sci.postHandle(null, null, null, null);
        PageHelper pageHelper = (PageHelper) _request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        assertNotNull(pageHelper);
        SessionContext sessionContext = (SessionContext) SessionContextUtil.getSessionContext(_request);
        assertNotNull(sessionContext);
        assertNull(sessionContext.getMemberId());
        assertFalse(sessionContext.getHasSearched());
        assertEquals(null, sessionContext.getState());
        assertNull(sessionContext.getPathway());


        // Now test it with cookies and a new request object
        _request = new GsMockHttpServletRequest();
        _request.removeAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        _request.removeAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        Cookie[] cookies = new Cookie[]{
                new Cookie(SessionContextUtil.MEMBER_ID_COOKIE, "1"),
                new Cookie("HAS_SEARCHED", "1"),
                new Cookie("STATE", "OR")
        };
        _request.setCookies(cookies);
        assertTrue(_sci.preHandle(_request, _response, null));
        _sci.afterCompletion(null, null, null, null);
        _sci.postHandle(null, null, null, null);
        pageHelper = (PageHelper) _request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        assertNotNull(pageHelper);
        sessionContext = (SessionContext) SessionContextUtil.getSessionContext(_request);
        assertNotNull(sessionContext);
        assertEquals(new Integer(1), sessionContext.getMemberId());
        assertTrue(sessionContext.getHasSearched());
        assertEquals(State.OR, sessionContext.getState());
        assertFalse(sessionContext.isTopicPage());

        _request.removeAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        _request.removeAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        _request.setRequestURI("/content/backToSchool.page");
        SessionContextUtil ctxUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
        sessionContext = ctxUtil.prepareSessionContext(_request, new MockHttpServletResponse());
        assertTrue(sessionContext.isTopicPage());

        _request.removeAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        _request.removeAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        _request.setRequestURI("/search/search.page");
        _request.setQueryString("q=nclb&state=ca&c=topic");
        sessionContext = ctxUtil.prepareSessionContext(_request, new MockHttpServletResponse());
        assertTrue(sessionContext.isTopicPage());

        _request.removeAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        _request.removeAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        _request.setRequestURI("/education-topics/");
        _request.setQueryString("");
        sessionContext = ctxUtil.prepareSessionContext(_request, new MockHttpServletResponse());
        assertTrue(sessionContext.isTopicPage());        
    }
}
