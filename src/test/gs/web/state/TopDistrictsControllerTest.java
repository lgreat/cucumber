/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: TopDistrictsControllerTest.java,v 1.5 2006/07/13 07:54:00 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.Anchor;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContext;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Tests TopDistrictsController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TopDistrictsControllerTest extends BaseControllerTestCase {

    public void testTopDistrictsController() throws Exception {
        TopDistrictsController c = new TopDistrictsController();
        c.setApplicationContext(getApplicationContext());
        c.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        c.setViewName("/unorderedList.jspx");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Object header = modelAndView.getModel().get(AnchorListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("California Districts", header);
        List results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertNotNull(results);
        assertEquals(6, results.size());
        Anchor districtAnchor = (Anchor) results.get(0);
        assertEquals("Fresno Unified", districtAnchor.getContents());
        Anchor last = (Anchor) results.get(4);
        assertEquals("San Francisco Unified", last.getContents());
        assertEquals("/schools.page?district=717&amp;state=CA", last.getHref());
        assertNotNull(modelAndView.getModel().get(AnchorListModel.RESULTS));

        Anchor veryLast = (Anchor) results.get(5);
        assertEquals("/modperl/distlist/CA", veryLast.getHref());
        assertEquals("View all California districts", veryLast.getContents());

        // Test AK districts-- this should produce what?
        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
        context.setState(State.AK);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get(AnchorListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Alaska Districts", header);
        results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertNotNull(results);
        assertEquals(5, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("Anchorage School District", districtAnchor.getContents());
        districtAnchor = (Anchor) results.get(3);
        assertEquals("Kenai Peninsula Borough Schools", districtAnchor.getContents());
        assertEquals("/schools.page?district=25&amp;state=AK", districtAnchor.getHref());
        last = (Anchor) results.get(4);
        assertEquals("View all Alaska districts", last.getContents());
        assertEquals("/modperl/distlist/AK", last.getHref());

        // Test AK districts-- this should produce what?
        context.setState(State.HI);
        context.setHostName("localhost");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        header = modelAndView.getModel().get(AnchorListModel.HEADING);
        assertNotNull(header);
        assertTrue(header instanceof String);
        assertEquals("Hawaii District", header);
        results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertNotNull(results);
        assertEquals(1, results.size());
        districtAnchor = (Anchor) results.get(0);
        assertEquals("HI District A", districtAnchor.getContents());
        assertEquals("/schools.page?district=1&amp;state=HI", districtAnchor.getHref());
    }
}
