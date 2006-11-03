/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import org.springframework.web.servlet.ModelAndView;
import gs.data.community.User;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityLandingControllerTest extends BaseControllerTestCase {

    private CommunityLandingController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityLandingController();
        _controller.setViewName("/successView");
    }

    public void testHandleRequestInternal() throws Exception {
        User user = new User();

        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setUser(user);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals("/successView", mAndV.getViewName());
    }

    public void testNoUser() throws Exception {
        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setUser(null);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertTrue(mAndV.getViewName().indexOf("redirect:") > -1);
    }
}
