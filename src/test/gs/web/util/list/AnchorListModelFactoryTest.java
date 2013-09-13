/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: AnchorListModelFactoryTest.java,v 1.22 2012/10/22 20:48:48 yfan Exp $
 */

package gs.web.util.list;

import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.*;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.UrlBuilder;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;

import org.apache.lucene.search.Hits;
import org.easymock.classextension.EasyMock;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests AnchorListModelFactory.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class AnchorListModelFactoryTest extends BaseTestCase {

    AnchorListModelFactory _anchorListModelFactory;
    private HttpServletRequest _request;
    private Searcher _searcher;
    private GsSolrSearcher _gsSolrSearcher;

    protected void setUp() throws Exception {
        super.setUp();
        _anchorListModelFactory = (AnchorListModelFactory) getApplicationContext().getBean(AnchorListModelFactory.BEAN_ID);
        _searcher = (Searcher) getApplicationContext().getBean(Searcher.BEAN_ID);
        final GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setRequestURI("http://www.greatschools.org/index.html");
        request.setRemoteHost("www.greatschools.org");
        _request = request;

        _gsSolrSearcher = createStrictMock(GsSolrSearcher.class);
        _anchorListModelFactory.setGsSolrSearcher(_gsSolrSearcher);
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
        expect(schoolDao.countSchools(State.AK, SchoolType.CHARTER, null, "Anchorage")).andReturn(2);
        expect(schoolDao.countSchools(State.AK, SchoolType.PRIVATE, null, "Anchorage")).andReturn(6);
        expect(schoolDao.countSchools(State.AK, SchoolType.CHARTER, null, "Anchorage")).andReturn(2);
        replay(schoolDao);

        AnchorListModel anchorListModel = _anchorListModelFactory.createSchoolSummaryModel(State.AK, "Anchorage", "Anchorage", _request);

        verify(schoolDao);

        List list = anchorListModel.getResults();
        assertEquals(7, list.size());

        Anchor preschoolAnchor = (Anchor) list.get(0);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(), LevelCode.PRESCHOOL)).asSiteRelative(null),
                preschoolAnchor.getHref());
        assertEquals("Preschools", preschoolAnchor.getContents());
        assertEquals(" (1)", preschoolAnchor.getAfter());

        Anchor elementaryAnchor = (Anchor) list.get(1);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(), LevelCode.ELEMENTARY)).asSiteRelative(null),
                elementaryAnchor.getHref());
        assertEquals("Elementary Schools", elementaryAnchor.getContents());
        assertEquals(" (2)", elementaryAnchor.getAfter());

        Anchor middleAnchor = (Anchor) list.get(2);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(), LevelCode.MIDDLE)).asSiteRelative(null),
                middleAnchor.getHref());
        assertEquals(" (3)", middleAnchor.getAfter());

        Anchor highAnchor = (Anchor) list.get(3);
        assertEquals("High Schools", highAnchor.getContents());
        assertEquals(" (4)", highAnchor.getAfter());

        Anchor publicAnchor = (Anchor) list.get(4);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(SchoolType.PUBLIC, SchoolType.CHARTER), null)).asSiteRelative(null),
                publicAnchor.getHref());

        Anchor privateAnchor = (Anchor) list.get(5);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(SchoolType.PRIVATE), null)).asSiteRelative(null),
                privateAnchor.getHref());

        Anchor charterAnchor = (Anchor) list.get(6);
        assertEquals((new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", createSchoolTypeSet(SchoolType.CHARTER), null)).asSiteRelative(null),
                charterAnchor.getHref());

        _anchorListModelFactory.setSchoolDao(springSchoolDao);
    }

    public void testFindDistricts() throws Exception {
        AnchorListModel anchorListModel = _anchorListModelFactory.createDistrictList(State.NY, "Dolgeville", "Dolgeville", _request);

        List list = anchorListModel.getResults();
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= 2);
        assertEquals("/new-york/dolgeville/Dolgeville-Central-School-District/", ((Anchor) list.get(0)).getHref());        
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

            Address address = new Address();
            address.setCity("Washington");
            d1.setPhysicalAddress(address);
            returnList.add(d1);

            expect(mockDao.findDistrictsInCity(State.DC, "Washington", true)).andReturn(returnList);
            mockDao.sortDistrictsByName(returnList);
            replay(mockDao);

            AnchorListModel model = anchorListModelFactory.createDistrictList(State.DC, "Washington", "Washington, DC",_request);
            List list = model.getResults();
            assertTrue(list.size() == 1);
            assertEquals("Arts &amp; Technology", ((Anchor) list.get(0)).getContents());
        } finally {
            anchorListModelFactory.setDistrictDao(backupDistrictDao);
        }
    }

    public void testCreateCitiesListModel() throws Exception{
        //This is a useless test because I couldn't load any sample dc data
         AnchorListModelFactory anchorListModelFactory = (AnchorListModelFactory) getApplicationContext().getBean(AnchorListModelFactory.BEAN_ID);
        Hits cityHits = _searcher.searchForCities("anchorage",State.AK);
            AnchorListModel anchorListModel =
                    anchorListModelFactory.createCitiesListModel(_request,
                            cityHits,
                            SchoolType.PUBLIC,
                            20,
                            false);
                assertEquals(1, anchorListModel.getResults().size());
            cityHits = _searcher.searchForCities("laurel",State.DC);
            anchorListModel =
                    anchorListModelFactory.createCitiesListModel(_request,
                            cityHits,
                            SchoolType.PUBLIC,
                            20,
                            false);
            assertEquals(0,anchorListModel.getResults().size());
    }

    public void testCreateBrowseLinksWithFilter() throws SearchException {
        Integer collectionId = null;
        State state = null;
        String city = null;

        // test with null state, query should not throw exception TODO: add fail
        Anchor anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, null, state, city);
        assertNull(anchor);

        // test with null filter, solr query should not be run
        state = State.DC;
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, null, state, city);
        assertNull(anchor);

        //test with null city for school types and level codes
        EasyMock.reset(_gsSolrSearcher);
        SchoolType schoolType = SchoolType.PUBLIC;
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, schoolType, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNull(anchor);

        EasyMock.reset(_gsSolrSearcher);
        LevelCode levelCode = LevelCode.ELEMENTARY;
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, levelCode, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNull(anchor);

        // test with city for solr query that returns empty result object, anchor object should be null.
        city = "asdfgh";
        EasyMock.reset(_gsSolrSearcher);
        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class), eq(false))).
                andReturn(new SearchResultsPage<SolrSchoolSearchResult>(0, null));
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, schoolType, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNull(anchor);

        EasyMock.reset(_gsSolrSearcher);
        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class), eq(false))).
                andReturn(new SearchResultsPage<SolrSchoolSearchResult>(0, null));
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, levelCode, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNull(anchor);

        //  test with non-empty search result and null city
        city = null;
        final SolrSchoolSearchResult schoolSearchResult = new SolrSchoolSearchResult(){{
            setDatabaseState("DC");
            setName("ZXCVB Public School");
            setId(1);
            setCity("asdfgh");
            setSchoolType("Public");
            setLevelCode(new String[]{"elementary"});
        }};
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = new SearchResultsPage<SolrSchoolSearchResult>(1, new ArrayList<SolrSchoolSearchResult>(){{
            add(schoolSearchResult);
        }});
        EasyMock.reset(_gsSolrSearcher);
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, schoolType, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNull(anchor);

        EasyMock.reset(_gsSolrSearcher);
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, levelCode, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNull(anchor);

        // test with all valid input, should give anchor object with href and content.
        city = "asdfgh";
        EasyMock.reset(_gsSolrSearcher);
        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class), eq(false))).
                andReturn(searchResultsPage);
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, schoolType, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNotNull(anchor);
        assertEquals("Expect one school to be returned", new Integer(1), anchor.getCount());
        assertEquals("Expect public school to be returned", "Public Schools", anchor.getContents());

        EasyMock.reset(_gsSolrSearcher);
        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class), eq(false))).
                andReturn(searchResultsPage);
        EasyMock.replay(_gsSolrSearcher);
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(_request, collectionId, levelCode, state, city);
        EasyMock.verify(_gsSolrSearcher);
        assertNotNull(anchor);assertEquals("Expect one school to be returned", new Integer(1), anchor.getCount());
        assertEquals("Expect elementary school to be returned", "Elementary Schools", anchor.getContents());
    }

    private Set<SchoolType> createSchoolTypeSet(SchoolType... types) {
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        for (SchoolType type : types) {
            schoolTypes.add(type);
        }
        return schoolTypes;
    }
}
