/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: TopDistrictsControllerTest.java,v 1.6 2007/07/09 19:49:26 cpickslay Exp $
 */

package gs.web.state;

import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Tests TopDistrictsController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TopDistrictsControllerTest extends BaseControllerTestCase {
    private IDistrictDao _districtDao;
    private TopDistrictsController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _districtDao = createMock(IDistrictDao.class);
        _controller = new TopDistrictsController();
        _controller.setDistrictDao(_districtDao);
    }

    public void testShouldLoadDistrictNamesAndAllDistricts() throws Exception {
        Integer[] topDistricts = new Integer[]{6, 5};

        District district6 = new District();
        district6.setName("District 6");
        District district5 = new District();
        district5.setName("District 5");
        expect(_districtDao.findDistrictById(State.TX, 6)).andReturn(district6);
        expect(_districtDao.findDistrictById(State.TX, 5)).andReturn(district5);
        replay(_districtDao);

        State.TX.setTopDistricts(topDistricts);
        // set TX as selected state
        SessionContext context = SessionContextUtil.getSessionContext(_request);
        context.setState(State.TX);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        String heading = (String) modelAndView.getModel().get(AnchorListModel.HEADING);
        assertEquals("Unexpected heading", "Texas Districts", heading);

        List<Anchor> results = (List<Anchor>) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertEquals("Expected one result for each district, plus one for all districts", topDistricts.length + 1, results.size());

        Anchor first = results.get(0);
        assertEquals("Unexpected district name for first district", "District 6", first.getContents());
        assertEquals("Unexpected link for first district", "/schools.page?district=6&amp;state=TX", first.getHref());

        Anchor second = results.get(1);
        assertEquals("Unexpected district name for second district", "District 5", second.getContents());
        assertEquals("/schools.page?district=5&amp;state=TX", second.getHref());

        Anchor allDistricts = results.get(2);
        assertEquals("Unexpected URL for all districts page", "/modperl/distlist/TX", allDistricts.getHref());
        assertEquals("Unexpected label for all districts page", "View all Texas districts", allDistricts.getContents());
    }

    public void testShouldDefaultToCalifornia() throws Exception {
        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        Object header = modelAndView.getModel().get(AnchorListModel.HEADING);
        assertEquals("Unexpected heading", "California Districts", header);
    }

    public void testShouldHandleNoTopDistrictsCase() throws Exception {
        State.AZ.setTopDistricts(new Integer[]{});
        SessionContext context = SessionContextUtil.getSessionContext(_request);
        context.setState(State.AZ);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        List<Anchor> results = (List<Anchor>) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertEquals("Expected only one result, as there are no top districts for state", 1, results.size());

        Anchor allDistricts = results.get(0);
        assertEquals("Unexpected URL for all districts page", "/modperl/distlist/AZ", allDistricts.getHref());
        assertEquals("Unexpected label for all districts page", "View all Arizona districts", allDistricts.getContents());
    }

    public void testShouldNotAddAllDistrictsLinkForHawaii() throws Exception {
        State.HI.setTopDistricts(new Integer[]{});
        SessionContext context = SessionContextUtil.getSessionContext(_request);
        context.setState(State.HI);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        List<Anchor> results = (List<Anchor>) modelAndView.getModel().get(AnchorListModel.RESULTS);
        assertEquals("Expected no results, as there is no all districts link for Hawaii", 0, results.size());
    }
    
//    public void testTopDistrictsController() throws Exception {
//        TopDistrictsController c = new TopDistrictsController();
//        c.setApplicationContext(getApplicationContext());
//        c.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
//        c.setViewName("/unorderedList.jspx");
//
//        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());
//
//        Object header = modelAndView.getModel().get(AnchorListModel.HEADING);
//        assertNotNull(header);
//        assertTrue(header instanceof String);
//        assertEquals("California Districts", header);
//        List results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
//        assertNotNull(results);
//        assertEquals(6, results.size());
//        Anchor districtAnchor = (Anchor) results.get(0);
//        assertEquals("Fresno Unified", districtAnchor.getContents());
//        Anchor last = (Anchor) results.get(4);
//        assertEquals("San Francisco Unified", last.getContents());
//        assertEquals("/schools.page?district=717&amp;state=CA", last.getHref());
//        assertNotNull(modelAndView.getModel().get(AnchorListModel.RESULTS));
//
//        Anchor veryLast = (Anchor) results.get(5);
//        assertEquals("/modperl/distlist/CA", veryLast.getHref());
//        assertEquals("View all California districts", veryLast.getContents());
//
//        // Test AK districts-- this should produce what?
//        SessionContext context = (SessionContext) SessionContextUtil.getSessionContext(getRequest());
//        context.setState(State.AK);
//        context.setHostName("localhost");
//        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
//
//        header = modelAndView.getModel().get(AnchorListModel.HEADING);
//        assertNotNull(header);
//        assertTrue(header instanceof String);
//        assertEquals("Alaska Districts", header);
//        results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
//        assertNotNull(results);
//        assertEquals(5, results.size());
//        districtAnchor = (Anchor) results.get(0);
//        assertEquals("Anchorage School District", districtAnchor.getContents());
//        districtAnchor = (Anchor) results.get(3);
//        assertEquals("Kenai Peninsula Borough Schools", districtAnchor.getContents());
//        assertEquals("/schools.page?district=25&amp;state=AK", districtAnchor.getHref());
//        last = (Anchor) results.get(4);
//        assertEquals("View all Alaska districts", last.getContents());
//        assertEquals("/modperl/distlist/AK", last.getHref());
//
//        // Test AK districts-- this should produce what?
//        context.setState(State.HI);
//        context.setHostName("localhost");
//        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
//
//        header = modelAndView.getModel().get(AnchorListModel.HEADING);
//        assertNotNull(header);
//        assertTrue(header instanceof String);
//        assertEquals("Hawaii District", header);
//        results = (List) modelAndView.getModel().get(AnchorListModel.RESULTS);
//        assertNotNull(results);
//        assertEquals(1, results.size());
//        districtAnchor = (Anchor) results.get(0);
//        assertEquals("HI District A", districtAnchor.getContents());
//        assertEquals("/schools.page?district=1&amp;state=HI", districtAnchor.getHref());
//    }
}
