/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModelFactoryTest.java,v 1.2 2006/06/03 05:09:37 apeterson Exp $
 */

package gs.web;

import gs.data.geo.ICity;
import gs.data.geo.City;
import gs.data.state.State;
import gs.web.util.Anchor;
import gs.web.util.AnchorListModel;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests AnchorListModelFactory.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class AnchorListModelFactoryTest extends BaseTestCase {

    AnchorListModelFactory _anchorListModelFactory;
    private HttpServletRequest _request;

    protected void setUp() throws Exception {
        super.setUp();
        _anchorListModelFactory = (AnchorListModelFactory) getApplicationContext().getBean(AnchorListModelFactory.BEAN_ID);
        final GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setRequestURI("http://www.greatschools.net/index.html");
        request.setRemoteHost("www.greatschools.net");
                _request = request;
    }

    public void testSchoolBreakdown() throws Exception {

        AnchorListModel anchorListModel = _anchorListModelFactory.createSchoolSummaryModel(State.AK, "Anchorage", "Anchorage", _request);

        List list = anchorListModel.getResults();
        assertEquals(5, list.size());

        assertEquals("/schools.page?city=Anchorage&lc=e&state=AK", ((Anchor) list.get(0)).getHref());
        assertEquals("Anchorage Elementary Schools", ((Anchor) list.get(0)).getContents());
        assertEquals(" (77)", ((Anchor) list.get(0)).getAfter());

        assertEquals("/schools.page?city=Anchorage&lc=m&state=AK", ((Anchor) list.get(1)).getHref());

        assertEquals("Anchorage High Schools", ((Anchor) list.get(2)).getContents());
        assertEquals(" (30)", ((Anchor) list.get(2)).getAfter());

        assertEquals("/schools.page?city=Anchorage&st=public&st=charter&state=AK", ((Anchor) list.get(3)).getHref());
        assertEquals("/schools.page?city=Anchorage&st=private&state=AK", ((Anchor) list.get(4)).getHref());
    }


    public void testFindDistricts() throws Exception {
        AnchorListModel anchorListModel = _anchorListModelFactory.createDistrictList(State.NY, "Dolgeville", _request);

        List list = anchorListModel.getResults();
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= 2);
        assertEquals("/cgi-bin/ny/district_profile/1/", ((Anchor) list.get(0)).getHref());
    }

    public void testNearbyCities() {

        ICity city = new City("Here", State.AK);
        List nearbyCities= new ArrayList();
        AnchorListModel anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, true, _request);
        assertEquals(2, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, false, true, _request);
        assertEquals(1, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, false,  _request);
        assertEquals(1, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, false, false,  _request);
        assertEquals(0, anchorListModel.getResults().size());

        // Washington D.C. shouldn't have an "Browse other cities in D.C."
        city= new City("Washington",State.DC);
        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, false, _request);
        assertEquals(1, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, true, _request);
        assertEquals(1, anchorListModel.getResults().size());


    }
}
