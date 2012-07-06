package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

//import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * Tester for SchoolProfileOverviewController
 * User: rraker
 * Date: 6/18/12
 */
public class SchoolProfileDataHelperTest extends BaseControllerTestCase {

//    SchoolProfileOverviewController _schoolProfileOverviewController;
    SchoolProfileDataHelper _schoolProfileDataHelper;
    private IEspResponseDao _espResponseDao;
    private ISchoolMediaDao _schoolMediaDao;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private RequestAttributeHelper _requestAttributeHelper;

    State _state;
    School _school;

    public void setUp() throws Exception {
        super.setUp();
        _espResponseDao = createStrictMock(IEspResponseDao.class);
        _schoolMediaDao = createStrictMock(ISchoolMediaDao.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _requestAttributeHelper = createStrictMock( RequestAttributeHelper.class );
        _schoolProfileDataHelper = new SchoolProfileDataHelper();
        _schoolProfileDataHelper.setEspResponseDao( _espResponseDao );
        _schoolProfileDataHelper.setSchoolMediaDao( _schoolMediaDao );
        _schoolProfileDataHelper.setReviewDao( _reviewDao );
        _schoolProfileDataHelper.setReportedEntityDao( _reportedEntityDao );
        _schoolProfileDataHelper.setRequestAttributeHelper( _requestAttributeHelper );
        StateManager sm = new StateManager();
        _state = sm.getState( "CA" );
        _school = new School();
        getRequest().setAttribute( "school", _school );
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

    // Tests for SchoolMedia
    public void testSchoolMedia() {

        // Data the controller needs to load for this test
        List<SchoolMedia> l = new ArrayList<SchoolMedia>();
        l.add(createSchoolMedia("file1"));

        expect( _schoolMediaDao.getAllActiveBySchool( _school ) ).andReturn(l);
        replay(_schoolMediaDao);
        List<SchoolMedia> sm = _schoolProfileDataHelper.getSchoolMedia( getRequest() );
        verify(_schoolMediaDao);

        assertEquals("testSchoolMedia: size wrong", 1, sm.size());
        assertEquals("testSchoolMedia: contents wrong", "file1", sm.get(0).getOriginalFileName());
    }

    // Tests for SchoolMedia
    public void testRatings() {

        // Data the controller needs to load for this test
        Ratings r = (createRatings(4));

        expect( _reviewDao.findRatingsBySchool(_school) ).andReturn(r);
        replay(_reviewDao);
        Ratings result = _schoolProfileDataHelper.getSchoolRatings( getRequest() );
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

        expect( _reviewDao.findPublishedNonPrincipalReviewsBySchool( _school, 3) ).andReturn( reviews3 );
        expect( _reviewDao.findPublishedNonPrincipalReviewsBySchool( _school, 6) ).andReturn( reviews5 );
        replay(_reviewDao);

        // Since there are no reviews stored in the request these should be added
        List<Review> results3 = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 3 );
        assertEquals("testReviews3: count wrong", 3, results3.size());

        // Because there are already more than 2 stored no DB hit should occur
        List<Review> results2 = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 2 );
        assertEquals("testReviews3: count wrong", 2, results2.size());

        // Now ask for 6, but the 2nd expect statement provides 5 so we should get 5 back
        List<Review> results5 = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 6 );
        assertEquals("testReviews3: count wrong", 5, results5.size());

        // Now ask for 6, but 5 is the most available from the DB so we should get 5 back with no DB call
        List<Review> results5b = _schoolProfileDataHelper.getNonPrincipalReviews( servletReq, 6 );
        assertEquals("testReviews3: count wrong", 5, results5.size());


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
        return SchoolProfileDataHelper.espResultsToMap( l );
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

    private SchoolMedia createSchoolMedia( String name ) {
        SchoolMedia sm = new SchoolMedia( _school.getId(), _state );
        sm.setContentType( "image/jpeg" );
        sm.setOriginalFileName( name );
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
