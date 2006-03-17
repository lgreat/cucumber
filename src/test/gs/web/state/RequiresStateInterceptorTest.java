/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RequiresStateInterceptorTest.java,v 1.4 2006/03/17 06:09:08 apeterson Exp $
 */

package gs.web.state;

import gs.web.BaseControllerTestCase;
import gs.data.state.StateManager;

/**
 * Tests RequiresStateInterceptor.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class RequiresStateInterceptorTest extends BaseControllerTestCase {

    private RequiresStateInterceptor _interceptor;

    protected void setUp() throws Exception {
        super.setUp();
        _interceptor = new RequiresStateInterceptor();
        _interceptor.setStateManager(new StateManager());
        getRequest().setRequestURI("/path/mySchool.page");
    }

    public void testRequiresStateInterceptorNoState() throws Exception {
        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }


    public void testRequiresStateInterceptorNoStateButOtherParam() throws Exception {
        getRequest().setParameter("city", "Lincoln");
        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page?city=Lincoln",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }

    public void testRequiresStateInterceptorNoStateButTwoOtherParam() throws Exception {
        getRequest().setParameter("city", "Lincoln");
        getRequest().setParameter("zip", "12345");
        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page?city=Lincoln%26zip=12345",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }


    public void testRequiresStateInterceptorNoStateWithContext() throws Exception {
        getRequest().setContextPath("/gs-web");

        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
// Mock object differs in behavior from Tomcat: assertEquals("http://www.greatschools.net/gs-web/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/gs-web/path/mySchool.page",
        assertEquals("http://www.greatschools.net/gs-web/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }

    public void testRequiresStateInterceptorBadState() throws Exception {
        getRequest().setParameter("state", "XX"); // bad state
        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page?",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }

    public void testRequiresStateInterceptorShortState() throws Exception {
        getRequest().setParameter("state", "X"); // bad state
        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page?",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }

    public void testRequiresStateInterceptorStateAlreadySet() throws Exception {
        getRequest().setParameter("state", "CA");
        boolean b = _interceptor.preHandle(getRequest(), getResponse(), null);
        assertTrue(b);

    }


}
