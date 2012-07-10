package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import org.springframework.ui.ModelMap;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

/**
 * Tester for SchoolProfileOverviewController
 * User: rraker
 * Date: 6/18/12
 */
public class SchoolProfileOverviewControllerTest extends BaseControllerTestCase {

    SchoolProfileOverviewController _schoolProfileOverviewController;
    SchoolProfileDataHelper _schoolProfileDataHelper;
    State _state;
    School _school;

    public void setUp() throws Exception {
        super.setUp();
        _schoolProfileDataHelper = createStrictMock( SchoolProfileDataHelper.class );
        _schoolProfileOverviewController = new SchoolProfileOverviewController();
        _schoolProfileOverviewController.setSchoolProfileDataHelper( _schoolProfileDataHelper );
        _schoolProfileOverviewController.setRequestAttributeHelper(new RequestAttributeHelper());
        StateManager sm = new StateManager();
        _state = sm.getState( "CA" );
        _school = new School();
        getRequest().setAttribute( "school", _school );
    }

    /*
        ************ Tests ************
     */
    // Blank test
    public void testBlank() {

    }

    // Tests for no Sports/Arts/Music data
    public void testSportsArtsMusicNoData() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("staff_languages", "arabic"));

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("sports");
        String result = (String) resultsModel.get("SportsArtsMusic");
        assertEquals("testSportsArtsMusicNoData: expected no results", "Hide", result);
    }

    // Tests for Sports/Arts/Music data
    public void testSportsArtsMusicSports() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("boys_sports", "football"));
        l.add( createEspResponse( "boys_sports", "baseball" ) );
        l.add( createEspResponse( "girls_sports", "none" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("sports");
        List<String> boysSports = (List<String>) resultsModel.get("boys_sports");
        List<String> girlsSports = (List<String>) resultsModel.get("girls_sports");

        assertEquals("testSportsArtsMusicSports: wrong number of boys_sport", 2, boysSports.size());
        assertEquals("testSportsArtsMusicSports: wrong boys_sports(0)", "Baseball", boysSports.get(0));
        assertEquals("testSportsArtsMusicSports: wrong boys_sports(1)", "Football", boysSports.get(1));
        assertEquals("testSportsArtsMusicSports: expected girls_sports None", "None", girlsSports.get(0));
    }

    // Tests that a list is truncated
    public void testListTruncation() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("boys_sports", "football"));
        l.add( createEspResponse( "boys_sports", "baseball" ) );
        l.add( createEspResponse( "boys_sports", "basketball" ) );
        l.add( createEspResponse( "boys_sports", "soccer" ) );
        l.add( createEspResponse( "boys_sports", "track" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("sports");
        List<String> boysSports = (List<String>) resultsModel.get("boys_sports");

        assertEquals("testListTruncation: expected list length is wrong", 4, boysSports.size());
        assertEquals("testListTruncation: expected last item to be More...", "More...", boysSports.get(3));
    }

    // =========== Tests for Tile 8 - Spec Ed / Extended care ====================
    // Tests the Special Education substitute display is selected
    public void testSpecEd1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );  // There has to be something in the EspResponse list or the non-ESP path runs


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEd1: Substitute content expected", "substitute", resultsModel.get("SpecEdDisplaySelected") );
        System.out.println("testSpecEd1 successful");
    }

    // Tests the Special Education Default with display option a is selected
    public void testSpecEdA() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "yes" ) );  // This should trigger default display
        l.add( createEspResponse( "special_ed_programs", "blindness" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        List<String> specEdPgms = (List<String>) resultsModel.get("SpecEdPgms");
        assertTrue("testSpecEdA: expected list of spec ed pgms", specEdPgms.size() > 0);
        assertEquals("testSpecEdA: wrong option", "a", resultsModel.get("SpecEdPgmsOptSelected"));
        System.out.println("testSpecEdA successful");
    }

    // Tests the Special Education Default with display option b is selected
    public void testSpecEdB() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "yes" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals("testSpecEdB: default content expected", "yes", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdB: wrong option", "b", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdB successful");
    }

    // Tests the Special Education Default with display option c is selected
    public void testSpecEdC1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "spec_ed_level", "basic" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals("testSpecEdC12: default content expected", "yes", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdC1: wrong option", "c", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdC1 successful");
    }

    // Tests the Special Education Default with display option c is selected
    public void testSpecEdC2() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "academic_focus", "special_ed" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals("testSpecEdC2: default content expected", "yes", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdC2: wrong option", "c", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdC2 successful");
    }

    // Tests the Special Education Default with display option d is selected
    public void testSpecEdD() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEd2: default content expected", "no", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEd2: wrong option", "d", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdD successful");
    }

    // Tests the Extended care Default with no Before After
    public void testExtdCareA() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareB: default content expected", "call", resultsModel.get("ExtdCareProvided") );
        System.out.println("testExtdCareA successful");
    }

    // Tests the Extended care Default with Before only
    public void testExtdCareB() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareB: default content expected", "Before school", resultsModel.get("ExtdCareBefore") );
        System.out.println("testExtdCareB successful");
    }

    // Tests the Extended care Default with Before with time
    public void testExtdCareC() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareC: default content expected", "Before school: Starts 7:00 AM", resultsModel.get( "ExtdCareBefore") );
        System.out.println( "testExtdCareC successful" );
    }

    // Tests the Extended care Default with After only
    public void testExtdCareD() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "after" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareD: default content expected", "After school", resultsModel.get( "ExtdCareAfter") );
        System.out.println( "testExtdCareD successful" );
    }

    // Tests the Extended care Default with After with time
    public void testExtdCareE() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareE: default content expected", "After school: Ends 4:00 PM", resultsModel.get( "ExtdCareAfter") );
        System.out.println( "testExtdCareE successful" );
    }

    // Tests the Extended care Default with Before and After with no times
    public void testExtdCareF() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareF: default content expected", "Before school", resultsModel.get( "ExtdCareBefore") );
        assertEquals( "testExtdCareF: default content expected", "After school", resultsModel.get( "ExtdCareAfter") );
        System.out.println( "testExtdCareF successful" );
    }

    // Tests the Extended care Default with Before and After with times
    public void testExtdCareG() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareC: default content expected", "Before school: Starts 7:00 AM", resultsModel.get( "ExtdCareBefore") );
        assertEquals( "testExtdCareE: default content expected", "After school: Ends 4:00 PM", resultsModel.get( "ExtdCareAfter") );
        System.out.println( "testExtdCareF successful" );
    }

    // Tests the Extended care Title
    public void testExtdCareTitleA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );

        _school.setLevelCode( LevelCode.MIDDLE);

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleA: default content expected", "Extended care", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleA successful" );
    }

    // Tests the Extended care Title
    public void testExtdCareTitleB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );

        _school.setLevelCode( LevelCode.HIGH);

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleB: default content expected", "Extended programs", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleB successful" );
    }

    // Tests the Extended care Title
    public void testExtdCareTitleC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );


        _school.setLevelCode( LevelCode.MIDDLE_HIGH);

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleC: default content expected", "Extended programs", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleC successful" );
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

    private Map runController( Map<String, List<EspResponse>> espData) {
        ModelMap map = new ModelMap();

        expect( _schoolProfileDataHelper.getEspDataForSchool( getRequest() ) ).andReturn( espData );
        expect( _schoolProfileDataHelper.getSchoolMedia(getRequest() )).andReturn(null);
        expect( _schoolProfileDataHelper.getSchoolRatings(getRequest() )).andReturn(null);
        expect( _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews( getRequest() ) ).andReturn( new Long(0l));
        expect( _schoolProfileDataHelper.getNonPrincipalReviews( getRequest(), 5 ) ).andReturn( null );
        replay(_schoolProfileDataHelper);
        _schoolProfileOverviewController.handle(map, getRequest());
        verify(_schoolProfileDataHelper);
        return map;
    }

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

}
