/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: TopCitiesControllerTest.java,v 1.12 2009/10/06 18:42:00 droy Exp $
 */

package gs.web.state;

import gs.data.state.State;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.Anchor;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Tests TopCitiesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TopCitiesControllerTest extends BaseControllerTestCase {
    public void testTopCitiesController() throws Exception {
        TopCitiesController c = new TopCitiesController();
        c.setApplicationContext(getApplicationContext());
        c.setViewName("/unorderedList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        final Object header = modelAndView.getModel().get(AnchorListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Cities", header);

        List results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertNotNull(results);
        assertTrue(results.size() > 4);
        Anchor la = (Anchor) results.get(0);
        assertEquals("Los Angeles schools", la.getContents());
        assertEquals("/california/los-angeles/", la.getHref());
        Anchor sf = (Anchor) results.get(3);
        assertEquals("San Francisco schools", sf.getContents());
        assertEquals("/california/san-francisco/", sf.getHref());
        assertNotNull(modelAndView.getModel().get(AnchorListModel.RESULTS));

        Anchor veryLast = (Anchor) results.get(results.size() - 1);
        assertEquals("/schools/cities/California/CA", veryLast.getHref());
        assertEquals("View all California cities", veryLast.getContents());

        // Special case DC
        SessionContext context = SessionContextUtil.getSessionContext(getRequest());
        context.setState(State.DC);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertNotNull(results);
        assertEquals(2, results.size());
        la = (Anchor) results.get(0);
        assertEquals("View all schools", la.getContents());
        assertEquals("/washington-dc/washington/schools/", la.getHref());
        la = (Anchor) results.get(1);
        assertEquals("View city information", la.getContents());
        assertEquals("/washington-dc/washington/", la.getHref());

        // Special case NYC
        context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setState(State.NY);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertNotNull(results);
        Anchor nyc = (Anchor) results.get(0);
        assertEquals("New York City schools", nyc.getContents());
        assertEquals("/new-york/new-york-city/", nyc.getHref());
        assertNotNull(modelAndView.getModel().get(AnchorListModel.RESULTS));
    }



}
