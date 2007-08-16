/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BrowseCitiesControllerTest.java,v 1.3 2007/08/16 20:00:52 chriskimm Exp $
 */

package gs.web.geo;

import gs.web.*;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Provides tests for NearbyCitiesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BrowseCitiesControllerTest extends BaseControllerTestCase {

    private BrowseCitiesController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new BrowseCitiesController();
    }

    public void testAlaskaMaps() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        getSessionContext().setState(State.AK);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/schools/cities/Alaska/AK", view.getUrl());
    }

    public void testAlaskaMapsFromDevWorkstation() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setServerName("localhost");
        request.setContextPath("/gs-web");
        request.setParameter("state", "AK");
        getSessionContext().setState(State.AK);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/gs-web/schools/cities/Alaska/AK", view.getUrl());
    }

    public void testCaliforniaMaps() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        getSessionContext().setState(State.CA);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        RedirectView view = (RedirectView) mav.getView();
        assertNotNull(view.getUrl());
        assertEquals("/schools/cities/California/CA", view.getUrl());
    }

}
