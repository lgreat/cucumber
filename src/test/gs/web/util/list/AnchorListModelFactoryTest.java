/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModelFactoryTest.java,v 1.4 2008/07/31 20:08:08 yfan Exp $
 */

package gs.web.util.list;

import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.state.State;
import gs.data.school.district.IDistrictDao;
import gs.data.school.district.District;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.school.SchoolsController;

import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

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

        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();

        assertEquals(SchoolsController.createNewCityBrowseURI(State.AK, "Anchorage", schoolTypes, LevelCode.ELEMENTARY), ((Anchor) list.get(0)).getHref());
        assertEquals("Anchorage Elementary Schools", ((Anchor) list.get(0)).getContents());
        assertEquals(" (77)", ((Anchor) list.get(0)).getAfter());

        assertEquals(SchoolsController.createNewCityBrowseURI(State.AK, "Anchorage", schoolTypes, LevelCode.MIDDLE), ((Anchor) list.get(1)).getHref());

        assertEquals("Anchorage High Schools", ((Anchor) list.get(2)).getContents());
        assertEquals(" (30)", ((Anchor) list.get(2)).getAfter());

        schoolTypes.clear();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.CHARTER);
        assertEquals(SchoolsController.createNewCityBrowseURI(State.AK, "Anchorage", schoolTypes, null), ((Anchor) list.get(3)).getHref());
        schoolTypes.clear();
        schoolTypes.add(SchoolType.PRIVATE);
        assertEquals(SchoolsController.createNewCityBrowseURI(State.AK, "Anchorage", schoolTypes, null), ((Anchor) list.get(4)).getHref());
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
        List nearbyCities = new ArrayList();
        AnchorListModel anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, true, _request);
        assertEquals(2, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, false, true, _request);
        assertEquals(1, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, false, _request);
        assertEquals(1, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, false, false, _request);
        assertEquals(0, anchorListModel.getResults().size());

        // Washington D.C. shouldn't have an "Browse other cities in D.C."
        city = new City("Washington", State.DC);
        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, false, _request);
        assertEquals(1, anchorListModel.getResults().size());

        anchorListModel = _anchorListModelFactory.createNearbyCitiesAnchorListModel(
                "Hey!", city, nearbyCities, 4, true, true, true, _request);
        assertEquals(1, anchorListModel.getResults().size());
    }

    public void testCreateDistrictList_EscapeAmpersand() throws Exception {
        AnchorListModelFactory anchorListModelFactory = (AnchorListModelFactory) getApplicationContext().getBean(AnchorListModelFactory.BEAN_ID);
        IDistrictDao mockDao = createMock(IDistrictDao.class);
        IDistrictDao backupDistrictDao = anchorListModelFactory._districtDao;
        anchorListModelFactory.setDistrictDao(mockDao);

        try {
            List<District> returnList = new ArrayList<District>();
            District d1 = new District();
            d1.setDatabaseState(State.DC);
            d1.setName("Arts & Technology");
            d1.setId(1234);
            returnList.add(d1);

            expect(mockDao.findDistrictsInCity(State.DC, "Washington", true)).andReturn(returnList);
            mockDao.sortDistrictsByName(returnList);
            replay(mockDao);

            AnchorListModel model = anchorListModelFactory.createDistrictList(State.DC, "Washington", _request);
            List list = model.getResults();
            assertTrue(list.size() == 1);
            assertEquals("Arts &amp; Technology", ((Anchor) list.get(0)).getContents());
        } finally {
            anchorListModelFactory.setDistrictDao(backupDistrictDao);
        }
    }
}
