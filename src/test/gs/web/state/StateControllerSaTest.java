/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StateControllerSaTest.java,v 1.4 2005/10/28 00:07:28 apeterson Exp $
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
public class StateControllerSaTest extends BaseControllerTestCase {

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
        assertEquals("/cgi-bin/ca/district_profile/717", last.getHref());
        assertNotNull(modelAndView.getModel().get("results"));

        Anchor veryLast = (Anchor) results.get(5);
        assertEquals("/modperl/distlist/CA", veryLast.getHref());
        assertEquals("View all California districts", veryLast.getContents());


        // Test LA districts-- this should produce what?
        SessionContext context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.LA);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get("header");
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Louisiana Districts", header);
        results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertEquals(5, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("LA District A", districtAnchor.getContents());
        districtAnchor = (Anchor) results.get(3);
        assertEquals("LA District D", districtAnchor.getContents());
        assertEquals("/cgi-bin/la/district_profile/19", districtAnchor.getHref());
        last = (Anchor) results.get(4);
        assertEquals("View all Louisiana districts", last.getContents());
        assertEquals("/modperl/distlist/LA", last.getHref());

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

        final List results = (List) modelAndView.getModel().get("results");
        assertNotNull(results);
        assertTrue(results.size() > 4);
        Anchor la = (Anchor) results.get(0);
        assertEquals("Los Angeles", la.getContents());
        assertEquals("/modperl/bycity/ca/?city=Los+Angeles&showall=1&level=a", la.getHref());
        Anchor sf = (Anchor) results.get(3);
        assertEquals("San Francisco", sf.getContents());
        assertEquals("/modperl/bycity/ca/?city=San+Francisco&showall=1&level=a", sf.getHref());
        assertNotNull(modelAndView.getModel().get("results"));

        Anchor veryLast = (Anchor) results.get(results.size() - 1);
        assertEquals("/modperl/citylist/CA/", veryLast.getHref());
        assertEquals("View all California cities", veryLast.getContents());
    }


}
