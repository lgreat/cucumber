/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModelFactoryTest.java,v 1.8 2008/09/25 00:47:32 yfan Exp $
 */

package gs.web.util.list;

import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.DirectoryStructureUrlFactory;
import gs.web.util.UrlBuilder;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        //avoid breaking downstream tests
        ISchoolDao springSchoolDao = _anchorListModelFactory.getSchoolDao();

        ISchoolDao schoolDao = createMock(ISchoolDao.class);
        _anchorListModelFactory.setSchoolDao(schoolDao);
        expect(schoolDao.countSchools(State.AK, null, LevelCode.PRESCHOOL, "Anchorage")).andReturn(1);
        expect(schoolDao.countSchools(State.AK, null, LevelCode.ELEMENTARY, "Anchorage")).andReturn(2);
        expect(schoolDao.countSchools(State.AK, null, LevelCode.MIDDLE, "Anchorage")).andReturn(3);
        expect(schoolDao.countSchools(State.AK, null, LevelCode.HIGH, "Anchorage")).andReturn(4);
        expect(schoolDao.countSchools(State.AK, SchoolType.PUBLIC, null, "Anchorage")).andReturn(4);
        expect(schoolDao.countSchools(State.AK, SchoolType.PRIVATE, null, "Anchorage")).andReturn(6);
        replay(schoolDao);

        AnchorListModel anchorListModel = _anchorListModelFactory.createSchoolSummaryModel(State.AK, "Anchorage", "Anchorage", _request);

        verify(schoolDao);

        List list = anchorListModel.getResults();
        assertEquals(6, list.size());

        Anchor preschoolAnchor = (Anchor) list.get(0);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(), LevelCode.PRESCHOOL)).asSiteRelative(null),
            preschoolAnchor.getHref());
        assertEquals("Anchorage Preschools", preschoolAnchor.getContents());
        assertEquals(" (1)", preschoolAnchor.getAfter());

        Anchor elementaryAnchor = (Anchor) list.get(1);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(), LevelCode.ELEMENTARY)).asSiteRelative(null),
            elementaryAnchor.getHref());
        assertEquals("Anchorage Elementary Schools", elementaryAnchor.getContents());
        assertEquals(" (2)", elementaryAnchor.getAfter());

        Anchor middleAnchor = (Anchor) list.get(2);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(), LevelCode.MIDDLE)).asSiteRelative(null),
            middleAnchor.getHref());
        assertEquals(" (3)", middleAnchor.getAfter());

        Anchor highAnchor = (Anchor) list.get(3);
        assertEquals("Anchorage High Schools", highAnchor.getContents());
        assertEquals(" (4)", highAnchor.getAfter());

        Anchor publicAnchor = (Anchor) list.get(4);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(SchoolType.PUBLIC), null)).asSiteRelative(null),
            publicAnchor.getHref());

        Anchor privateAnchor = (Anchor) list.get(5);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(SchoolType.PRIVATE), null)).asSiteRelative(null),
            privateAnchor.getHref());

        _anchorListModelFactory.setSchoolDao(springSchoolDao);
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

    private Set<SchoolType> createSchoolTypeSet(SchoolType... types) {
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        for (SchoolType type : types) {
            schoolTypes.add(type);
        }
        return schoolTypes;
    }
}
