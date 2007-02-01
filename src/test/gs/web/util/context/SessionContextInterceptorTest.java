package gs.web.util.context;

import gs.web.util.PageHelper;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.state.State;

import javax.servlet.http.Cookie;

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
    }
}
