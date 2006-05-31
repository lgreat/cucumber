/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModelFactoryTest.java,v 1.1 2006/05/31 21:44:29 apeterson Exp $
 */

package gs.web;

import gs.data.state.State;
import gs.web.util.Anchor;
import gs.web.util.AnchorListModel;

import javax.servlet.http.HttpServletRequest;
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

}
