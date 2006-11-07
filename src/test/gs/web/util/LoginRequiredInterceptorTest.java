/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
import gs.data.community.User;
import gs.data.util.DigestUtil;

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
        user.setId(new Integer(1));
        user.setEmail("eford@greatschools.net");

        SessionContext sc = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        Object[] hashInput = new Object[] {User.SECRET_NUMBER, user.getId(), user.getEmail()};
        String hash = DigestUtil.hashObjectArray(hashInput);
        sc.setUserHash(hash);

        assertNotNull(SessionContextUtil.getSessionContext(getRequest()).getUser());
        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertTrue(rval);
        assertNull(getResponse().getRedirectedUrl());
    }

    public void testNoUser() throws NoSuchAlgorithmException, IOException {
        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertFalse(rval);
        assertNotNull(getResponse().getRedirectedUrl());
    }

    public void testWrongUser() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(new Integer(1));
        user.setEmail("eford@greatschools.net");

        SessionContext sc = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        Object[] hashInput = new Object[] {User.SECRET_NUMBER, new Integer(2), "someOtheremail@address.org"};
        String hash = DigestUtil.hashObjectArray(hashInput);
        sc.setUserHash(hash);

        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertFalse(rval);
        assertNotNull(getResponse().getRedirectedUrl());
    }

    public void testWrongHash() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(new Integer(1));
        user.setEmail("eford@greatschools.net");

        SessionContext sc = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        sc.setUser(user);
        Object[] hashInput = new Object[] {User.SECRET_NUMBER, user.getId(), user.getEmail()};
        String hash = DigestUtil.hashObjectArray(hashInput);
        sc.setUserHash(hash);

        User user2 = new User();
        user2.setId(new Integer(2));
        user2.setEmail("dford@greatschools.net");
        sc.setUser(user2);

        assertNull(getResponse().getRedirectedUrl());
        boolean rval = _interceptor.preHandle(getRequest(), getResponse(), _dummyHandler);
        assertFalse(rval);
        assertNotNull(getResponse().getRedirectedUrl());
    }
}
