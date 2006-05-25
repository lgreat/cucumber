/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ListModelFactoryTest.java,v 1.2 2006/05/25 17:23:19 apeterson Exp $
 */

package gs.web;

import gs.data.state.State;
import gs.web.util.Anchor;
import gs.web.util.ListModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Tests ListModelFactory.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ListModelFactoryTest extends BaseTestCase {

    ListModelFactory _listModelFactory;
    private HttpServletRequest _request;

    protected void setUp() throws Exception {
        super.setUp();
        _listModelFactory = (ListModelFactory) getApplicationContext().getBean(ListModelFactory.BEAN_ID);
        final GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setRequestURI("http://www.greatschools.net/index.html");
        request.setRemoteHost("www.greatschools.net");
                _request = request;
    }

    public void testSchoolBreakdown() throws Exception {

        ListModel listModel = _listModelFactory.createSchoolSummaryModel(State.AK, "Anchorage", "Anchorage", _request);

        List list = listModel.getResults();
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
        ListModel listModel = _listModelFactory.createDistrictList(State.NY, "Dolgeville", _request);

        List list = listModel.getResults();
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= 2);
        assertEquals("/cgi-bin/ny/district_profile/1/", ((Anchor) list.get(0)).getHref());
    }

}
