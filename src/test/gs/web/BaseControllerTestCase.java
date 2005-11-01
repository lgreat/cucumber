/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: BaseControllerTestCase.java,v 1.2 2005/11/01 00:30:06 thuss Exp $
 */

package gs.web;

import gs.data.state.State;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BaseControllerTestCase extends BaseTestCase {
    private MockHttpServletRequest _request;
    private MockHttpServletResponse _response;

    protected void setUp() throws Exception {
        super.setUp();

        _request = new MockHttpServletRequest();

        SessionContext sessionContext = new SessionContext();
        sessionContext.setApplicationContext(getApplicationContext());
        sessionContext.setCobrand(null);
        sessionContext.setHostName("www.greatschools.net");
        sessionContext.setState(State.CA);
        sessionContext.setUser(null);
        // Note: you can override or reset them at the beginning of your test.

        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);

        _response = new MockHttpServletResponse();
    }

    public MockHttpServletRequest getRequest() {
        return _request;
    }

    public MockHttpServletResponse getResponse() {
        return _response;
    }



}
