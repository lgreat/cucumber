/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StateControllerTest.java,v 1.4 2005/11/15 23:44:40 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.SessionFacade;
import gs.web.SessionContext;
import gs.web.util.Anchor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class StateControllerTest extends BaseControllerTestCase {

    public void testTopDistrictsController() throws Exception {
        TopDistrictsController c = new TopDistrictsController();
        c.setApplicationContext(getApplicationContext());
        c.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        c.setViewName("/resultList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Object header = modelAndView.getModel().get("header");
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Districts", header);
        List results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertEquals(6, results.size());
        Anchor districtAnchor = (Anchor) results.get(0);
        assertEquals("Fresno Unified", districtAnchor.getContents());
        Anchor last = (Anchor) results.get(4);
        assertEquals("San Francisco Unified", last.getContents());
        assertEquals("/modperl/browse_district/717/ca", last.getHref());
        assertNotNull(modelAndView.getModel().get("results"));

        Anchor veryLast = (Anchor) results.get(5);
        assertEquals("/modperl/distlist/CA", veryLast.getHref());
        assertEquals("View all California districts", veryLast.getContents());


        // Test AK districts-- this should produce what?
        SessionContext context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.AK);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get("header");
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Alaska Districts", header);
        results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertEquals(5, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("AK District A", districtAnchor.getContents());
        districtAnchor = (Anchor) results.get(3);
        assertEquals("AK District D", districtAnchor.getContents());
        assertEquals("/modperl/browse_district/25/ak", districtAnchor.getHref());
        last = (Anchor) results.get(4);
        assertEquals("View all Alaska districts", last.getContents());
        assertEquals("/modperl/distlist/AK", last.getHref());


        // Test AK districts-- this should produce what?
        context.setState(State.HI);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get("header");
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Hawaii District", header);
        results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertEquals(1, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("HI District A", districtAnchor.getContents());
        assertEquals("/modperl/browse_district/1/hi", districtAnchor.getHref());
    }

    public void testTopCitiesController() throws Exception {
        TopCitiesController c = new TopCitiesController();
        c.setApplicationContext(getApplicationContext());
        c.setViewName("/resultList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        final Object header = modelAndView.getModel().get("header");
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Cities", header);

        List results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertTrue(results.size() > 4);
        Anchor la = (Anchor) results.get(0);
        assertEquals("Los Angeles schools", la.getContents());
        assertEquals("/modperl/bycity/ca/?city=Los+Angeles", la.getHref());
        Anchor sf = (Anchor) results.get(3);
        assertEquals("San Francisco schools", sf.getContents());
        assertEquals("/modperl/bycity/ca/?city=San+Francisco", sf.getHref());
        assertNotNull(modelAndView.getModel().get("results"));

        Anchor veryLast = (Anchor) results.get(results.size() - 1);
        assertEquals("/modperl/citylist/CA/", veryLast.getHref());
        assertEquals("View all California cities", veryLast.getContents());


        // Special case DC
        SessionContext context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.DC);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertEquals(1, results.size());
        la = (Anchor) results.get(0);
        assertEquals("View all schools", la.getContents());
        assertEquals("/cgi-bin/schoollist/DC", la.getHref());


        // Special case NYC
        context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.NY);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        Anchor nyc = (Anchor) results.get(0);
        assertEquals("New York City schools", nyc.getContents());
        assertEquals("/modperl/bycity/ny/?city=New+York", nyc.getHref());
        assertNotNull(modelAndView.getModel().get("results"));


    }


}
