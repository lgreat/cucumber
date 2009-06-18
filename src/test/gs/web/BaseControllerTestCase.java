/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: BaseControllerTestCase.java,v 1.10 2009/06/18 23:08:23 eingenito Exp $
 */

package gs.web;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BaseControllerTestCase extends BaseTestCase {
    protected GsMockHttpServletRequest _request;
    protected MockHttpServletResponse _response;
    protected SessionContext _sessionContext = null;
    private static final String HOST_NAME = "www.greatschools.net";

    protected void setUp() throws Exception {
        super.setUp();
        _request = new GsMockHttpServletRequest();
        _request.setServerName(HOST_NAME);
        // Note: you can override or reset them at the beginning of your test.

        _sessionContext = (SessionContext) getApplicationContext().getBean(SessionContext.BEAN_ID);
        _sessionContext.setCobrand(null);
        _sessionContext.setHostName(HOST_NAME);
        _sessionContext.setState(State.CA);
        _sessionContext.setUser(null);
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        ServletRequestAttributes attrbs = new ServletRequestAttributes(_request);
        RequestContextHolder.setRequestAttributes(attrbs);

        _response = new MockHttpServletResponse();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public SessionContext getSessionContext() {
        return (SessionContext) SessionContextUtil.getSessionContext(_request);
    }

    public GsMockHttpServletRequest getRequest() {
        return _request;
    }

    public MockHttpServletResponse getResponse() {
        return _response;
    }


}
