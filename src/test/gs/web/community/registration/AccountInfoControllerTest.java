/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AccountInfoControllerTest extends BaseControllerTestCase {

    private AccountInfoController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new AccountInfoController();
        _controller.setViewName("/successView");
    }

    public void testHandleRequestInternal() throws Exception {
        User user = new User();

        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setUser(user);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals("/successView", mAndV.getViewName());
        assertEquals(user, mAndV.getModel().get("user"));
    }
}