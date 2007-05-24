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
 * Provides testing for the CommunityLandingController.
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

    public void testHandleRequestInternal() {
        // verify user is added to the model
        User user = new User();
        SessionContext context = SessionContextUtil.getSessionContext(getRequest());
        context.setUser(user);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals("/successView", _controller.getViewName());
        assertEquals("/successView", mAndV.getViewName());
        assertEquals(mAndV.getModel().get("user"), user);
    }

    public void testEscapeMessage() {
        // test normal xml escaping
        getRequest().setParameter("message", "It is a <dog>");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals("/successView", mAndV.getViewName());
        assertEquals("Angled brackets should be escaped",
                "It is a &lt;dog&gt;",
                mAndV.getModel().get("escapedMessage"));

        // verify apostrophe is not escaped
        getRequest().setParameter("message", "It's a <dog>");
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals("/successView", mAndV.getViewName());
        assertEquals("Apostropher should not be escaped",
                "It's a &lt;dog&gt;",
                mAndV.getModel().get("escapedMessage"));
    }
}
