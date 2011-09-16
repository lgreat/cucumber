/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: BaseControllerTestCase.java,v 1.14 2011/09/16 00:18:08 ssprouse Exp $
 */

package gs.web;

import gs.web.request.RequestInfo;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.Cookie;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class BaseControllerTestCase extends BaseTestCase {
    protected GsMockHttpServletRequest _request;
    protected MockHttpServletResponse _response;
    protected SessionContext _sessionContext = null;
    private static final String HOST_NAME = "www.greatschools.org";

    protected void setUp() throws Exception {
        super.setUp();
        _request = new GsMockHttpServletRequest();
        _request.setServerName(HOST_NAME);

        RequestInfo hostnameInfo = new RequestInfo(_request);
        _request.setAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME, hostnameInfo);
        // Note: you can override or reset them at the beginning of your test.

        _sessionContext = (SessionContext) getApplicationContext().getBean(SessionContext.BEAN_ID);
        _sessionContext.setCobrand(null);
        _sessionContext.setHostName(HOST_NAME);
        _sessionContext.setState(State.CA);
        _sessionContext.setUser(null);
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        ServletRequestAttributes attrbs = new ServletRequestAttributes(_request);
        RequestContextHolder.setRequestAttributes(attrbs);

        // Overridden so that successive calls to addCookie with the same cookie will overwrite the previous value
        // This is the behavior of the real HttpServletResponse so I'm unclear on why the mock one fails so hard
        _response = new MockHttpServletResponse() {
            @Override
            public void addCookie(Cookie cookie) {
                if (getCookie(cookie.getName()) != null) {
                    getCookie(cookie.getName()).setValue(cookie.getValue());
                }
                super.addCookie(cookie);
            }
        };
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public SessionContext getSessionContext() {
        return SessionContextUtil.getSessionContext(_request);
    }

    public GsMockHttpServletRequest getRequest() {
        return _request;
    }

    public MockHttpServletResponse getResponse() {
        return _response;
    }


}
