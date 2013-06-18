package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import gs.web.school.usp.EspResponseData;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import gs.web.search.CmsRelatedFeatureSearchService;
import gs.web.search.CmsRelatedFeatureSearchServiceSolrImpl;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.search.SolrCmsFeatureSearchResult;
import org.springframework.beans.factory.BeanFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

//import static org.easymock.EasyMock.createStrictMock;
//import static org.easymock.EasyMock.createStrictMock;
//import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * Tester for SchoolProfileDataHelper
 * User: rraker
 * Date: 6/18/12
 */
public class SchoolProfileDataHelperTest extends BaseControllerTestCase {

//    SchoolProfileOverviewController _schoolProfileOverviewController;
    SchoolProfileDataHelper _schoolProfileDataHelper;
    private IEspResponseDao _espResponseDao;
    private ISchoolMediaDao _schoolMediaDao;
//    private ICensusInfo _censusInfo;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private RequestAttributeHelper _requestAttributeHelper;
    private IGeoDao _geoDao;
    //private CmsRelatedFeatureSearchService _cmsRelatedFeatureSearchService;
    private CmsRelatedFeatureSearchService _cmsRelatedFeatureSearchService;

    State _state;
    School _school;
    BeanFactory _beanFactory;
    EspStatusManager _espStatusManager;

    public void setUp() throws Exception {
        super.setUp();
        _beanFactory = org.easymock.classextension.EasyMock.createStrictMock(BeanFactory.class);
        _espStatusManager = org.easymock.classextension.EasyMock.createStrictMock(EspStatusManager.class);
        _espResponseDao = createStrictMock(IEspResponseDao.class);
        _schoolMediaDao = createStrictMock(ISchoolMediaDao.class);
//        _censusInfo = createStrictMock( ICensusInfo.class );
        _reviewDao = createStrictMock(IReviewDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _requestAttributeHelper = createStrictMock( RequestAttributeHelper.class );
        _geoDao = createStrictMock( IGeoDao.class );
        _cmsRelatedFeatureSearchService = createStrictMock( CmsRelatedFeatureSearchServiceSolrImpl.class );

        _schoolProfileDataHelper = new SchoolProfileDataHelper();
        _schoolProfileDataHelper.setEspResponseDao( _espResponseDao );
        _schoolProfileDataHelper.setSchoolMediaDao( _schoolMediaDao );
        _schoolProfileDataHelper.setReviewDao( _reviewDao );
        _schoolProfileDataHelper.setReportedEntityDao( _reportedEntityDao );
        _schoolProfileDataHelper.setRequestAttributeHelper( _requestAttributeHelper );
        _schoolProfileDataHelper.setGeoDao( _geoDao );
        _schoolProfileDataHelper.setCmsRelatedFeatureSearchService( _cmsRelatedFeatureSearchService );
        _schoolProfileDataHelper.setBeanFactory(_beanFactory);

        StateManager sm = new StateManager();
        _state = sm.getState( "CA" );
        _school = new School();
        getRequest().setAttribute( "school", _school );
//        _school.setCensusInfo( _censusInfo );
        expect( _requestAttributeHelper.getSchool( getRequest() ) ).andReturn( _school );
        expectLastCall().anyTimes();
        replay( _requestAttributeHelper );
    }

    /*
        ************ Tests ************
     */
    // Blank test
    public void XtestBlank() {

    }

    // Tests for EspData
    public void testEspNoEntries() {

        // Data the controller needs to load for this test

        expect( _espResponseDao.getResponses( _school ) ).andReturn( null );
        replay(_espResponseDao);
        Map<String, List<EspResponse>> response1 = _schoolProfileDataHelper.getEspDataForSchool( getRequest() );
        // This request should not cause another DB request because the fact that there is no data should have been saved
        Map<String, List<EspResponse>> response2 = _schoolProfileDataHelper.getEspDataForSchool( getRequest() );
        verify(_espResponseDao);

        assertNull("testEspNoEntries: not null response1", response1);
        assertNull("testEspNoEntries: not null response2", response2);
    }

    public void testEspOneEntry() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("staff_languages", "arabic"));

        expect( _espResponseDao.getResponses( _school ) ).andReturn( l );
        replay(_espResponseDao);
        Map<String, List<EspResponse>> response = _schoolProfileDataHelper.getEspDataForSchool( getRequest() );
        verify(_espResponseDao);

        List<EspResponse> staff_lang = response.get("staff_languages");
        assertEquals("testEspOneEntry: size wrong", 1, staff_lang.size());
        assertEquals("testEspOneEntry: contents wrong", "arabic", staff_lang.get(0).getValue());
    }

    public void testEspTwoEntries() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("staff_languages", "arabic"));
        l.add(createEspResponse("staff_languages", "german"));

        expect( _espResponseDao.getResponses( _school ) ).andReturn(l);
        replay(_espResponseDao);

        Map<String, List<EspResponse>> response = _schoolProfileDataHelper.getEspDataForSchool( getRequest() );
        List<EspResponse> staff_lang = response.get("staff_languages");
        assertEquals("testEspTwoEntries: size wrong", 2, staff_lang.size());
        assertEquals("testEspTwoEntries: contents wrong", "arabic", staff_lang.get(0).getValue());
        assertEquals("testEspTwoEntries: contents wrong", "german", staff_lang.get(1).getValue());

        // Make a second call which should succeed because there won't be a second trip to the DB.  EasyMock is only set up for one DB call
        Map<String, List<EspResponse>> response2 = _schoolProfileDataHelper.getEspDataForSchool( getRequest() );
        // No need for asserts because easyMock will throw an exception here if a second DB access is attempted
        verify(_espResponseDao);

   }

    // Test for census info
    public void testGetEnrollment() {

        School school = createStrictMock( School.class );
        getRequest().setAttribute( "school", school );
        reset(_requestAttributeHelper);       // reset so we don't use the one from setUp()
        expect( _requestAttributeHelper.getSchool( getRequest() ) ).andReturn( school ).times(2); // This will be called for each getEnrollment call
        replay(_requestAttributeHelper);

        expect( school.getEnrollmentOrCapacity() ).andReturn(500);
        replay(school);
        _schoolProfileDataHelper.getEnrollment(getRequest());
        // getEnrollment again and if it calls school.getEnrollment() EasyMock will fail because only one call is expected
        Integer enrollment = _schoolProfileDataHelper.getEnrollment( getRequest() );
        verify(school);
        verify(_requestAttributeHelper);

        assertEquals("Incorrect value", 500, enrollment.intValue());
    }

    // Test for osp status
    public void testGetOspStatus() {

        School school = createStrictMock( School.class );
        getRequest().setAttribute( "school", school );
        reset(_requestAttributeHelper);       // reset so we don't use the one from setUp()
        org.easymock.classextension.EasyMock.reset(_beanFactory);
        org.easymock.classextension.EasyMock.reset(_espStatusManager);

        expect( _requestAttributeHelper.getSchool( getRequest() ) ).andReturn( school ).times(2); // This will be called for each getEnrollment call
        org.easymock.classextension.EasyMock.expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class), isA(EspResponseData.class))).andReturn(
                _espStatusManager
        );
        org.easymock.classextension.EasyMock.expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.OSP_PREFERRED);
        replay(_requestAttributeHelper);

        org.easymock.classextension.EasyMock.replay(_beanFactory);
        org.easymock.classextension.EasyMock.replay(_espStatusManager);
        replay(school);
        _schoolProfileDataHelper.getOspStatus(getRequest(), new HashMap<String, List<EspResponse>>());
        // getOspStatus again, this time it gets from shared data in the request
        EspStatus ospStatus = _schoolProfileDataHelper.getOspStatus(getRequest(), new HashMap<String, List<EspResponse>>());
        verify(school);
        verify(_requestAttributeHelper);
        org.easymock.classextension.EasyMock.verify(_beanFactory);
        org.easymock.classextension.EasyMock.verify(_espStatusManager);

        assertEquals(EspStatus.OSP_PREFERRED, ospStatus);
    }

    // Tests for SchoolMedia
    public void testSchoolMedia() {

        // Data the controller needs to load for this test
        List<SchoolMedia> l = new ArrayList<SchoolMedia>();
        Integer mediaId1 = new Integer(1);
        l.add(createSchoolMedia("file1", mediaId1 ));
        Integer mediaId2 = new Integer(2);
        l.add(createSchoolMedia("file1", mediaId2 ));

        expect( _schoolMediaDao.getAllActiveBySchool( _school ) ).andReturn(l);
        replay(_schoolMediaDao);
        List<SchoolMedia> sm = _schoolProfileDataHelper.getSchoolMedia( getRequest() );
        verify(_schoolMediaDao);

        assertEquals("testSchoolMedia: size wrong", 2, sm.size());
        assertEquals("testSchoolMedia: contents wrong", "file1", sm.get(0).getOriginalFileName());

        // Now test the getReportsForSchoolMedia()
        User user = new User();
        user.setId( new Integer(25) );
        // Expect a DB call for each different Media ID
        expect(_reportedEntityDao.hasUserReportedEntity(user, ReportedEntity.ReportedEntityType.schoolMedia, mediaId1)).andReturn( new Boolean(true));
        expect(_reportedEntityDao.hasUserReportedEntity(user, ReportedEntity.ReportedEntityType.schoolMedia, mediaId2)).andReturn( new Boolean(false) );
        replay(_reportedEntityDao);
        // The following call will create a DB request for each media ID (and there are 2)
        Map<Integer, Boolean> reportsForUser1 = _schoolProfileDataHelper.getReportsForSchoolMedia( getRequest(), user, l );
        // This should no create any additional DB calls because we have cashed the results from the previous call in the request.  If DB calls occurred there would be a Mock exception thrown.
        Map<Integer, Boolean> reportsForUser2 = _schoolProfileDataHelper.getReportsForSchoolMedia( getRequest(), user, l );
        verify(_reportedEntityDao);


        assertEquals( "testSchoolMedia: for media 1", true, reportsForUser1.get(mediaId1).booleanValue() );
        assertEquals( "testSchoolMedia: for media 2", false, reportsForUser2.get(mediaId2).booleanValue() );
    }

    // Tests for SchoolRatings
    public void testRatings() {

        // Data the controller needs to load for this test
        Ratings r = (createRatings(4));

        expect( _reviewDao.findRatingsBySchool(_school) ).andReturn(r);
        replay(_reviewDao);
        Ratings result = _schoolProfileDataHelper.getCommunityRatings( getRequest() );
        verify(_reviewDao);

        assertEquals("testRatings: contents wrong", 4, result.getAvgQuality().intValue());
    }

    // Tests for Reviews
    public void testReviewsCounts() {

        expect( _reviewDao.countPublishedNonPrincipalReviewsBySchool(_school) ).andReturn( new Long(4));
        replay(_reviewDao);
        Long result = _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews(getRequest());
        verify(_reviewDao);

        assertEquals("testReviewsCounts: count wrong", 4, result.intValue());
    }

    public void testReviews3() {

        HttpServletRequest servletReq = getRequest();

        List<Review> reviews3 = createReviews( "Set1", 3 );
        List<Review> reviews5 = createReviews( "Set2", 5 );

        expect( _reviewDao.findPublishedNonPrincipalReviewsBySchool( _school ) ).andReturn( reviews3 );
        expect( _reviewDao.findPublishedNonPrincipalReviewsBySchool( _school ) ).andReturn( reviews5 );
        replay(_reviewDao);

        // Since there are no reviews stored in the request these should be added
        List<Review> results3 = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 3 );
        assertEquals("testReviews3: count wrong", 3, results3.size());

        // Because there are already more than 2 stored no DB hit should occur
        List<Review> results2 = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 2 );
        assertEquals("testReviews3: count wrong", 2, results2.size());

        // Now ask for 6, but the 2nd expect statement provides 5 so we should get 5 back
//        List<Review> results5 = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 6 );
//        assertEquals("testReviews3: count wrong", 5, results5.size());
//
//        // Now ask for 6, but 5 is the most available from the DB so we should get 5 back with no DB call
//        List<Review> results5b = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 6 );
//        assertEquals("testReviews3: count wrong", 5, results5.size());


    }

    // Tests for Geo data
    public void testGeo() {

        BpZip sperlings = new BpZip();
        String neighborhoodType = "Inner city";
        sperlings.setNeighborhoodType( neighborhoodType );

        String zipCode = "12345";

        School school = createStrictMock( School.class );
        getRequest().setAttribute("school", school);
        reset(_requestAttributeHelper);       // reset so we don't use the one from setUp()
        expect( _requestAttributeHelper.getSchool( getRequest() ) ).andReturn( school ); // This will be called for each getDistrictInfo call
        replay(_requestAttributeHelper);

        expect( school.getZipcode() ).andReturn(zipCode);
        replay( school );

        expect( _geoDao.findZip(zipCode) ).andReturn(sperlings);
        replay(_geoDao);
        BpZip result = _schoolProfileDataHelper.getSperlingsInfo(getRequest());
        verify(_geoDao);
        verify(school);
        verify(_requestAttributeHelper);

        assertEquals("testGeo: neighborhood type wrong", neighborhoodType, result.getNeighborhoodType());
    }

    // Tests for Geo data
    public void testCmsRelatedContent1() {

        // Setup results based on spreadsheet from Jira GS-12954
        // First setup the school
        SchoolSubtype subtype = SchoolSubtype.create( "all_male" ); // Gets cmsId 434
        _school.setSubtype(subtype);

        // Setup some EspData
        List<EspResponse> l = new ArrayList<EspResponse>();
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        // Setup the expected results
        List<ICmsFeatureSearchResult> cmsResults = new ArrayList<ICmsFeatureSearchResult>();
        ICmsFeatureSearchResult cmsResult1 = new SolrCmsFeatureSearchResult();
        cmsResult1.setTitle( "Single sex schools" );
        Long contentId = 434L;
        cmsResult1.setContentId(contentId);
        cmsResults.add(cmsResult1);
        expect(_cmsRelatedFeatureSearchService.getRelatedFeatures(_school, espData, 5)).andReturn(cmsResults);
        replay( _cmsRelatedFeatureSearchService );

        List<ICmsFeatureSearchResult> results = _schoolProfileDataHelper.getCmsRelatedContent(getRequest(), espData, 5);
        verify(_cmsRelatedFeatureSearchService);

        assertTrue("testCmsRelatedContent1: ContentId: " + contentId + " not found.", isCmsIdInResult( results, contentId ) );
    }

    private boolean isCmsIdInResult( List<ICmsFeatureSearchResult> results, Long expectedCmsId ) {

        for( ICmsFeatureSearchResult r : results ) {
            if( r.getContentId().equals( expectedCmsId ) ) {
                return true;
            }
        }

        // No match
        return false;
    }

    /*
           ************ End of Tests - support functions follow ************
        */
//    private ModelMap runController( List<EspResponse> espResponses) {
//        ModelMap map = new ModelMap();
//
//        expect( _espResponseDao.getResponsesByKeys(_school, _schoolProfileOverviewController.getKeyValuesToExtract() ) ).andReturn( espResponses );
//        replay(_espResponseDao);
//        _schoolProfileOverviewController.showHighlightsPage(map, getRequest(), 1, _state);
//        verify(_espResponseDao);
//        return map;
//    }

//    private Map runController( Map<String, List<EspResponse>> espData) {
//        ModelMap map = new ModelMap();
//
//        expect( _schoolProfileDataHelper.getEspDataForSchool( getRequest() ) ).andReturn( espData );
//        expect( _schoolProfileDataHelper.getSchoolMedia(getRequest() )).andReturn( null );
//        expect( _schoolProfileDataHelper.getSchoolRatings(getRequest() )).andReturn( null );
//        expect( _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews( getRequest() ) ).andReturn( new Long(0l));
//        expect( _schoolProfileDataHelper.getNonPrincipalReviews( getRequest(), 5 ) ).andReturn( null );
//        replay(_schoolProfileDataHelper);
//        _schoolProfileOverviewController.handle(map, getRequest(), 1, _state);
//        verify(_schoolProfileDataHelper);
//        return map;
//    }

    private EspResponse createEspResponse( String key, String value ) {
        EspResponse response = new EspResponse();
        response.setActive( true );
        response.setKey( key );
        response.setValue( value );
        response.setPrettyValue( createPrettyValue(value) );
        return response;
    }

    private Map<String,List<EspResponse>> convertToEspData(List<EspResponse> l) {
//        return SchoolProfileDataHelper.espResultsToMap( l );
        return EspResponse.rollup(l);
    }

    // Create a pretty value by capitalizing thr first character and removing underscores
    private String createPrettyValue( String value ) {
        StringBuilder sb = new StringBuilder();

        sb.append( Character.toUpperCase( value.charAt(0) ) );
        for( int i = 1; i < value.length(); i++ ) {
            char c = value.charAt(i);
            if( c == '_' ) {
                sb.append( ' ' );
            }
            else {
                sb.append( c );
            }
        }

        return sb.toString();
    }

    private SchoolMedia createSchoolMedia( String name, Integer id ) {
        SchoolMedia sm = new SchoolMedia( _school.getId(), _state );
        sm.setContentType( "image/jpeg" );
        sm.setOriginalFileName( name );
        sm.setId( id );
        return sm;
    }

    private Ratings createRatings( int rating ) {
        Ratings r = new Ratings();
        r.setAvgQuality( new Integer(rating));
        return r;
    }

    private List<Review> createReviews( String prefix, int count ) {
        List<Review> l = new ArrayList<Review>(count);
        for( int i = 0; i< count; i++ ) {
            Review r = new Review();
            r.setComments( prefix + "" + i+1 );
            l.add(r);
        }
        return l;
    }

//    private class MySchoolMedia implements IS
}
