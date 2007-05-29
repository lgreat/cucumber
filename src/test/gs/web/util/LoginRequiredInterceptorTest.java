/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
import gs.data.community.User;

import java.security.NoSuchAlgorithmException;
import java.io.IOException;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginRequiredInterceptorTest extends BaseControllerTestCase {
    private LoginRequiredInterceptor _interceptor;
    private Object _dummyHandler;

    public void setUp() throws Exception {
        _interceptor = new LoginRequiredInterceptor();
        _dummyHandler = new Object();
        super.setUp();
    }

    public void testPreHandle() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(1);

        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        String hash = AuthenticationManager.generateCookieValue(user);
        sc.setUserHash(hash);

        assertNotNull(SessionContextUtil.getSessionContext(getRequest()).getUser());
        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertTrue("Authentication should be accepted", rval);
        assertNull(getResponse().getRedirectedUrl());
    }

    public void testNoUser() throws NoSuchAlgorithmException, IOException {
        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertFalse("Authentication should be rejected", rval);
        assertNotNull(getResponse().getRedirectedUrl());
    }

    public void testWrongUser() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(1);

        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        // hash generated for different user
        User user2 = new User();
        user2.setId(2);
        String hash = AuthenticationManager.generateCookieValue(user2);
        sc.setUserHash(hash);

        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertFalse("Authentication should be rejected", rval);
        assertNotNull(getResponse().getRedirectedUrl());
    }
}
