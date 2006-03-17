/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RequiresStateInterceptorTest.java,v 1.1 2006/03/17 05:33:43 apeterson Exp $
 */

package gs.web.state;

import gs.web.BaseControllerTestCase;

/**
 * Tests RequiresStateInterceptor.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class RequiresStateInterceptorTest extends BaseControllerTestCase {

    public void testRequiresStateInterceptorNoState() throws Exception {
        RequiresStateInterceptor intercept = new RequiresStateInterceptor();
        intercept.setApplicationContext(getApplicationContext());

        getRequest().setRequestURI("/path/mySchool.page");
        boolean b = intercept.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }


    public void testRequiresStateInterceptorNoStateButOtherParam() throws Exception {
        RequiresStateInterceptor intercept = new RequiresStateInterceptor();
        intercept.setApplicationContext(getApplicationContext());

        getRequest().setRequestURI("/path/mySchool.page");
        getRequest().setParameter("city","Lincoln");
        boolean b = intercept.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page?city=Lincoln",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }

    public void testRequiresStateInterceptorNoStateButTwoOtherParam() throws Exception {
        RequiresStateInterceptor intercept = new RequiresStateInterceptor();
        intercept.setApplicationContext(getApplicationContext());

        getRequest().setRequestURI("/path/mySchool.page");
        getRequest().setParameter("city","Lincoln");
        getRequest().setParameter("zip","12345");
        boolean b = intercept.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/path/mySchool.page?city=Lincoln%26zip=12345",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }


    public void testRequiresStateInterceptorNoStateWithContext() throws Exception {
        RequiresStateInterceptor intercept = new RequiresStateInterceptor();
        intercept.setApplicationContext(getApplicationContext());

        getRequest().setContextPath("/gs-web");
        getRequest().setRequestURI("/path/mySchool.page");
        boolean b = intercept.preHandle(getRequest(), getResponse(), null);
        assertEquals("http://www.greatschools.net/gs-web/selectAState.page?prompt=Please+select+a+state+to+continue.&url=/gs-web/path/mySchool.page",
                getResponse().getRedirectedUrl());
        assertFalse(b);
    }

    public void testRequiresStateInterceptorBadState() throws Exception {
        RequiresStateInterceptor intercept = new RequiresStateInterceptor();
        intercept.setApplicationContext(getApplicationContext());


        getRequest().setParameter("state", "X"); // bad state
        boolean b = intercept.preHandle(getRequest(), getResponse(), null);
        assertFalse(b);


    }

    public void testRequiresStateInterceptorStateAlreadySet() throws Exception {
        RequiresStateInterceptor intercept = new RequiresStateInterceptor();
        intercept.setApplicationContext(getApplicationContext());

        getRequest().setParameter("state", "CA");
        boolean b = intercept.preHandle(getRequest(), getResponse(), null);
        assertTrue(b);

    }


}
