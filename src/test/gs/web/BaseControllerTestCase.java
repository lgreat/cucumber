/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: BaseControllerTestCase.java,v 1.6 2006/03/23 18:21:38 apeterson Exp $
 */

package gs.web;

import gs.data.state.State;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.context.ApplicationContext;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BaseControllerTestCase extends BaseTestCase {
    private GsMockHttpServletRequest _request;
    private MockHttpServletResponse _response;
    private SessionContext _sessionContext;

    protected void setUp() throws Exception {
        super.setUp();
        _request = new GsMockHttpServletRequest();
        String hostname = "www.greatschools.net";
        _request.setServerName(hostname);
        _sessionContext = new SessionContext() {
            // Implement a lazy
            public ApplicationContext getApplicationContext() {
                return BaseControllerTestCase.this.getApplicationContext();
            }
        };
        //_sessionContext.setApplicationContext();
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

    public GsMockHttpServletRequest getRequest() {
        return _request;
    }

    public MockHttpServletResponse getResponse() {
        return _response;
    }



}
