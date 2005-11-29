/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: BaseControllerTestCase.java,v 1.4 2005/11/29 23:24:49 thuss Exp $
 */

package gs.web;

import gs.data.state.State;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BaseControllerTestCase extends BaseTestCase {
    private MockHttpServletRequest _request;
    private MockHttpServletResponse _response;
    private SessionContext _sessionContext;

    protected void setUp() throws Exception {
        super.setUp();
        _request = new MockHttpServletRequest();
        String hostname = "www.greatschools.net";
        _request.setServerName(hostname);
        _sessionContext = new SessionContext();
        _sessionContext.setApplicationContext(getApplicationContext());
        _sessionContext.setCobrand(null);
        _sessionContext.setHostName(hostname);
        _sessionContext.setState(State.CA);
        _sessionContext.setUser(null);
        // Note: you can override or reset them at the beginning of your test.

        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _response = new MockHttpServletResponse();
    }

    protected void tearDown () throws Exception {
        super.tearDown();
    }

    public SessionContext getSessionContext() {
        return _sessionContext;
    }

    public MockHttpServletRequest getRequest() {
        return _request;
    }

    public MockHttpServletResponse getResponse() {
        return _response;
    }



}
