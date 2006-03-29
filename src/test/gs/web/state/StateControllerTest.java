/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StateControllerTest.java,v 1.8 2006/03/29 21:31:00 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.SessionContext;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

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
        c.setViewName("/unorderedList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Object header = modelAndView.getModel().get(ListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Districts", header);
        List results = (List) modelAndView.getModel().get(ListModel.RESULTS);
        assertNotNull(results);
        assertEquals(6, results.size());
        Anchor districtAnchor = (Anchor) results.get(0);
        assertEquals("Fresno Unified", districtAnchor.getContents());
        Anchor last = (Anchor) results.get(4);
        assertEquals("San Francisco Unified", last.getContents());
        assertEquals("/schools.page?district=717&amp;state=CA", last.getHref());
        assertNotNull(modelAndView.getModel().get(ListModel.RESULTS));

        Anchor veryLast = (Anchor) results.get(5);
        assertEquals("/modperl/distlist/CA", veryLast.getHref());
        assertEquals("View all California districts", veryLast.getContents());

        // Test AK districts-- this should produce what?
        SessionContext context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.AK);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get(ListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Alaska Districts", header);
        results = (List) modelAndView.getModel().get(ListModel.RESULTS);
        assertNotNull(results);
        assertEquals(5, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("AK District A", districtAnchor.getContents());
        districtAnchor = (Anchor) results.get(3);
        assertEquals("AK District D", districtAnchor.getContents());
        assertEquals("/schools.page?district=25&amp;state=AK", districtAnchor.getHref());
        last = (Anchor) results.get(4);
        assertEquals("View all Alaska districts", last.getContents());
        assertEquals("/modperl/distlist/AK", last.getHref());

        // Test AK districts-- this should produce what?
        context.setState(State.HI);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get(ListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Hawaii District", header);
        results = (List) modelAndView.getModel().get(ListModel.RESULTS);
        assertNotNull(results);
        assertEquals(1, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("HI District A", districtAnchor.getContents());
        assertEquals("/schools.page?district=1&amp;state=HI", districtAnchor.getHref());
    }

    public void testTopCitiesController() throws Exception {
        TopCitiesController c = new TopCitiesController();
        c.setApplicationContext(getApplicationContext());
        c.setViewName("/unorderedList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        final Object header = modelAndView.getModel().get(ListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Cities", header);

        List results = (List) modelAndView.getModel().get(ListModel.RESULTS);
        assertNotNull(results);
        assertTrue(results.size() > 4);
        Anchor la = (Anchor) results.get(0);
        assertEquals("Los Angeles schools", la.getContents());
        assertEquals("/schools.page?state=CA&city=Los+Angeles", la.getHref());
        Anchor sf = (Anchor) results.get(3);
        assertEquals("San Francisco schools", sf.getContents());
        assertEquals("/schools.page?state=CA&city=San+Francisco", sf.getHref());
        assertNotNull(modelAndView.getModel().get(ListModel.RESULTS));

        Anchor veryLast = (Anchor) results.get(results.size() - 1);
        assertEquals("/modperl/citylist/CA/", veryLast.getHref());
        assertEquals("View all California cities", veryLast.getContents());

        // Special case DC
        SessionContext context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.DC);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        results = (List) modelAndView.getModel().get(ListModel.RESULTS);
        assertNotNull(results);
        assertEquals(1, results.size());
        la = (Anchor) results.get(0);
        assertEquals("View all schools", la.getContents());
        assertEquals("/cgi-bin/schoollist/DC", la.getHref());

        // Special case NYC
        context = (SessionContext) SessionFacade.getInstance(getRequest());
        context.setState(State.NY);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        results = (List) modelAndView.getModel().get(ListModel.RESULTS);
        assertNotNull(results);
        Anchor nyc = (Anchor) results.get(0);
        assertEquals("New York City schools", nyc.getContents());
        assertEquals("/schools.page?state=NY&city=New+York", nyc.getHref());
        assertNotNull(modelAndView.getModel().get(ListModel.RESULTS));


    }


    public void testSelectAStateController() throws Exception {
        SelectAStateController c = new SelectAStateController();
        c.setApplicationContext(getApplicationContext());
        c.setViewName("/stateLauncher");

        // Check no parameters, which should just go to the home page
        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView) modelAndView.getView();
        assertEquals("/", view.getUrl());

        // View will build links:
        // {url}CA{extraParams}
        // So we make sure URL gets set to what we want.
        // Currently extraParams isn't needed

        // We want it to add a state param, "?state="
        getRequest().setParameter("url", "/welcome.page");

        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        assertEquals("/stateLauncher", modelAndView.getViewName());
        Map model = modelAndView.getModel();
        assertEquals("/welcome.page?state=", model.get("url"));
        assertEquals("", model.get("extraParams"));
        assertEquals("", model.get("promotext"));


        getRequest().setParameter("url", "/districts.page?city=Lincoln");

        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        assertEquals("/stateLauncher", modelAndView.getViewName());
        model = modelAndView.getModel();
        assertEquals("/districts.page?city=Lincoln&amp;state=", model.get("url"));
        assertEquals("", model.get("extraParams"));
        assertEquals("", model.get("promotext"));


    }

}
