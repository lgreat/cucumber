package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
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
    public void XtestSportsArtsMusicNoData() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("staff_languages", "arabic"));

        Map map = runController( convertToEspData( l ) );

        String result = (String) map.get("SportsArtsMusic");

        assertEquals("testSportsArtsMusicNoData: expected no results", "Hide", result);
    }

    // Tests for Sports/Arts/Music data
    public void XtestSportsArtsMusicSports() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("boys_sports", "football"));
        l.add( createEspResponse( "boys_sports", "baseball" ) );
        l.add( createEspResponse( "girls_sports", "none" ) );

        Map map = runController( convertToEspData( l ) );

        List<String> boysSports = (List<String>) map.get("boys_sports");
        List<String> girlsSports = (List<String>) map.get("girls_sports");

        assertEquals("testSportsArtsMusicSports: wrong number of boys_sport", 2, boysSports.size());
        assertEquals("testSportsArtsMusicSports: wrong boys_sports(0)", "Baseball", boysSports.get(0));
        assertEquals("testSportsArtsMusicSports: wrong boys_sports(1)", "Football", boysSports.get(1));
        assertEquals("testSportsArtsMusicSports: expected girls_sports None", "None", girlsSports.get(0));
    }

    // Tests that a list is truncated
    public void XtestListTruncation() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("boys_sports", "football"));
        l.add( createEspResponse( "boys_sports", "baseball" ) );
        l.add( createEspResponse( "boys_sports", "basketball" ) );
        l.add( createEspResponse( "boys_sports", "soccer" ) );
        l.add( createEspResponse( "boys_sports", "track" ) );

        Map map = runController( convertToEspData( l ) );

        List<String> boysSports = (List<String>) map.get("boys_sports");

        assertEquals("testListTruncation: expected list length is wrong", 4, boysSports.size());
        assertEquals("testListTruncation: expected last item to be More...", "More...", boysSports.get(3));
    }

    // Verifies that all data loaded is returned.  For this test pick keys without allowed lists
    public void XtestSportsArtsMusic1() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();

        l.add( createEspResponse( "staff_languages", "arabic" ) );
        l.add( createEspResponse( "staff_languages", "cantonese" ) );
        l.add( createEspResponse( "staff_languages_other", "Afghani" ) );
        l.add( createEspResponse( "staff_languages_other", "American Sign Language" ) );
        l.add( createEspResponse( "staff_languages_other", "Amharic" ) );
        l.add( createEspResponse( "staff_languages_other", "Armenian" ) );

        l.add( createEspResponse( "immersion_language", "cantonese" ) );
        l.add( createEspResponse( "immersion_language", "french" ) );
        l.add( createEspResponse( "immersion_language", "german" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actualStaffLanguages = resultsModel.get( "highlights/Language/staff_languages" );
        List<String> immersionLanguages = resultsModel.get( "highlights/Language/immersion_language" );

        int expected_count = l.size();
        int actual_count = actualStaffLanguages.size() + immersionLanguages.size();
        assertEquals("testAllDataReturned: Expected number of results is wrong", expected_count, actual_count);

        // This is also a good place to perform some basic tests on the DISPLAY structure returned by the controller
        // Both the "highlights" and "highlightsMap" should have information for 1 section since all data was for 1 section
        List<SchoolProfileDisplayBean> highlightsDisplay = (List<SchoolProfileDisplayBean>) map.get("highlights");
        int actualHightlightsCount =     highlightsDisplay.size();
        assertEquals( "testAllDataReturned: Wrong number of display sections", 1, actualHightlightsCount);

        // This should have one entry for "Language" and that entry should be a list of 2 items for "staff_language" and "immersion_language"
        Map<String, List<SchoolProfileDisplayBean>> highlightsDisplayMap = (Map<String, List<SchoolProfileDisplayBean>>) map.get("highlightsMap");
        assertEquals( "testAllDataReturned: Wrong number of display map entries", 1, highlightsDisplayMap.size());
        List<SchoolProfileDisplayBean> highlightsDisplayRowsForLanguage = highlightsDisplayMap.get( "Language" );
        assertEquals( "testAllDataReturned: Wrong number of display map entries for \"Language\"", 2, highlightsDisplayRowsForLanguage.size());
    }

    // Verifies that only data in the allowed list is returned
    public void XtestAllowedList() {

        // Data the controller needs to load for this test
        // Special Ed - is the first thing in the display.  This is useful for debugging
        // Also can be used to verify only allowed values are returned
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "academic_focus", "special_education" ) );
        l.add( createEspResponse( "academic_focus", "medical" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actual = resultsModel.get( "highlights/SpecEd/academic_focus" );

        List<String> expected = new ArrayList<String>();
        expected.add("Special education");

        assertNotNull( "testAllowedList: SpecEd academic_focus list is null", actual);
        boolean equals = actual.equals( expected );
        assertTrue("testAllowedList: Special Ed academic focus lists are not equal", equals);
    }

    // Verifies that only data in the allowed list is returned when multiple choices are allowed
    public void XtestAllowedListMultiple() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // *** Health ***
        l.add( createEspResponse( "facilities", "farm" ) );
        l.add( createEspResponse( "facilities", "sports_fields" ) );
        l.add( createEspResponse( "facilities", "garden" ) );
        l.add( createEspResponse( "facilities", "shop" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actual = resultsModel.get( "highlights/Health/facilities" );

        String[] expected = new String[] {"Farm", "Sports fields", "Garden"};
        List<String> expectedList = Arrays.asList( expected );

        assertNotNull( "testAllowedListMultiple: result List is null", actual);
        boolean equals = actual.equals( expectedList );
        assertTrue( "testAllowedListMultiple: Filtering of results where there are multiple allowed values fails", equals );

    }

    // Verifies that the ADDITIONAL_PAGE_DATA data is getting populated and merged correctly
    public void XtestAdditionalDataMerge() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        //  *** student_club related **
        // This is in allowed list
        l.add( createEspResponse( "student_clubs", "student_newspaper" ) );
        // This is in allowed list
        l.add( createEspResponse( "student_clubs", "yearbook" ) );
        // This is NOT in allowed list
        l.add( createEspResponse( "student_clubs", "car_club" ) );
        // This is additional data
        l.add( createEspResponse( "student_clubs_dance", "jazzercise" ) );

        Map map = runController(convertToEspData(l));

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actualStudentClubData = resultsModel.get("highlights/Arts/student_clubs");

        String[] expected = new String[] {"Student newspaper", "Yearbook", "Jazzercise"};
        List<String> expectedStudentClubData = Arrays.asList( expected );

        assertNotNull( "testAdditionalDataMerge: Arts student_clubs list is null", actualStudentClubData);
        boolean clubsEqual = actualStudentClubData.equals( expectedStudentClubData );
        assertTrue( "testAdditionalDataMerge: Clubs are different", clubsEqual );
    }

    // Verifies that the ADDITIONAL_PAGE_DATA data is getting populated and merged correctly for the case
    // where the primary DISPLAY_CONFIG does not contribute any data
    public void XtestAdditionalDataMergeWithNoResultData() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        //  *** student_club related **
        // This is in ADDITIONAL_PAGE_DATA
        l.add( createEspResponse( "foreign_language_other", "After School  Spanish Tuition-based Programs" ) );
        l.add( createEspResponse( "foreign_language_other", "American Sign Language" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actualStudentClubData = resultsModel.get( "highlights/Language/foreign_language" );

        String[] expected = new String[] {"After School  Spanish Tuition-based Programs", "American Sign Language" };
        List<String> expectedStudentClubData = Arrays.asList( expected );

        assertNotNull( "testAdditionalDataMergeWithNoResultData: result is null", actualStudentClubData);
        boolean clubsEqual = actualStudentClubData.equals( expectedStudentClubData );
        assertTrue( "testAdditionalDataMergeWithNoResultData: results are different", clubsEqual );
    }

    // Tests the applyUniqueDataRules() for admissions_contact_school
    // where the primary DISPLAY_CONFIG does not contribute any data
    public void XtestSpecialRuleAdmissionsContactSchool() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - two entries, expect URL
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "admissions_contact_school", "yes" ) );
        String admissions_url = "http:/www.someSchool.edu";
        l.add( createEspResponse( "admissions_url", admissions_url ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> admissionInfo = resultsModel.get( "application_info/AppEnroll/admissions_contact_school" );
        assertNotNull( "testSpecialRuleAdmissionsContactSchool: Expected a URL but got null", admissionInfo );
        assertEquals( "testSpecialRuleAdmissionsContactSchool: Expected 2 admission_contact_school values", 2, admissionInfo.size());
        assertTrue("testSpecialRuleAdmissionsContactSchool: Expected \"Call the school\"", admissionInfo.contains("Call the school"));
        assertTrue( "testSpecialRuleAdmissionsContactSchool: Expected a URL message", admissionInfo.contains("Visit the school's website: http:/www.someSchool.edu") );

    }

    // Tests the applyUniqueDataRules() for immersion / immersion_language
    public void XtestSpecialRuleImmersion1() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - one entry - Yes, should get yes back
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get("programs_resources/Programs/immersion");
        assertEquals("testSpecialRuleImmersion1: Expected Yes", "Yes", results.get(0));

    }

    public void XtestSpecialRuleImmersion2() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 2 - one entry - No, should not get any data back
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "no" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/immersion" );
        assertNull("testSpecialRuleImmersion2: Expected no data but got: " + results, results);

    }

    public void XtestSpecialRuleImmersion3() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 3 - two entries: yes and cantonese, should get Cantonese back
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        l.add( createEspResponse( "immersion_language", "cantonese" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/immersion" );
        assertEquals( "testSpecialRuleImmersion3: Expected Cantonese", "Cantonese", results.get(0) );
        assertEquals("testSpecialRuleImmersion3: Expected 1 result", 1, results.size());

    }

    // Tests the support info part of the display bean
    public void XtestSupportData() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - two entries, expect URL
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "yes" ) );
        String routeInfo = "51, 72, 99";
        l.add( createEspResponse( "transportation_shuttle_other", routeInfo ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> shuttleOtherInfo = resultsModel.get( "programs_resources/Basics/transportation_shuttle_other" );
        assertEquals( "testSupportData: retrieval of transportation_shuttle_other failed", routeInfo, shuttleOtherInfo.get(0) );
        List<String> shuttleInfo = resultsModel.get( "programs_resources/Basics/transportation_shuttle" );
        assertTrue("testSupportData: transportation_shuttle_other does not contain the route info",
                (shuttleInfo.get(0).indexOf(routeInfo) > 0));

    }

    // Tests the None handling part of the display bean
    public void XtestNoneHandlingOneValue() {

        // A field with None handling is programs_resources/Programs/instructional_model and it is set to SHOW_IF_ONLY_VALUE
        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - None and no other entries
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "instructional_model", "none" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/instructional_model" );
        assertEquals("testNoneHandling: expected None", "None", results.get(0));

    }

    public void XtestNoneHandlingTwoValues() {

        // A field with None handling is programs_resources/Programs/instructional_model and it is set to SHOW_IF_ONLY_VALUE
        // This test runs the controller multiple times for the different test cases
        // *** Test 2 - None and other entries
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "instructional_model", "none" ) );
        l.add( createEspResponse( "instructional_model_other", "gifted" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/instructional_model" );
        assertTrue( "testNoneHandling: expected one result but got: " + results.size(), results.size() == 1);
        assertEquals( "testNoneHandling: expected None to be suppressed", "Gifted", results.get(0) );

    }

    // =========== Tests for Tile 8 - Spec Ed / Extended care ====================
    // Tests the Special Education substitute display is selected
    public void XtestSpecEd1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );  // There has to be something in the EspResponse list or the non-ESP path runs


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEd1: Substitute content expected", "substitute", resultsModel.get( "SpecEdDisplaySelected") );
        System.out.println( "testSpecEd1 successful" );
    }

    // Tests the Special Education Default with display option a is selected
    public void XtestSpecEdA() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "yes" ) );  // This should trigger default display
        l.add( createEspResponse( "special_ed_programs", "blindness" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEdA: default content expected", "yes", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdA: wrong option", "a", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println( "testSpecEdA successful" );
    }

    // Tests the Special Education Default with display option b is selected
    public void XtestSpecEdB() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "yes" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEdB: default content expected", "yes", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdB: wrong option", "b", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println( "testSpecEdB successful" );
    }

    // Tests the Special Education Default with display option c is selected
    public void XtestSpecEdC1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "spec_ed_level", "basic" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEdC12: default content expected", "yes", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdC1: wrong option", "c", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println( "testSpecEdC1 successful" );
    }

    // Tests the Special Education Default with display option c is selected
    public void XtestSpecEdC2() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "academic_focus", "special_ed" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEdC2: default content expected", "yes", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdC2: wrong option", "c", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println( "testSpecEdC2 successful" );
    }

    // Tests the Special Education Default with display option d is selected
    public void XtestSpecEdD() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );  // This should trigger default display


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testSpecEd2: default content expected", "no", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEd2: wrong option", "d", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println( "testSpecEdD successful" );
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

        expect( _schoolProfileDataHelper.getEspDataForSchool( getRequest(), _school ) ).andReturn( espData );
        expect( _schoolProfileDataHelper.getSchoolMedia(getRequest(), _school) ).andReturn( null );
        expect( _schoolProfileDataHelper.getSchoolRatings(getRequest(), _school) ).andReturn( null );
        expect( _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews( getRequest(), _school ) ).andReturn( new Long(0l));
        expect( _schoolProfileDataHelper.getNonPrincipalReviews( getRequest(), _school, 5 ) ).andReturn( null );
        replay(_schoolProfileDataHelper);
        _schoolProfileOverviewController.handle(map, getRequest(), 1, _state);
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
