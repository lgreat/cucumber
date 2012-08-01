package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.school.SchoolSubtype;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import org.springframework.ui.ModelMap;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

/**
 * Tester for SchoolProfileProgramsController
 * User: rraker
 * Date: 6/18/12
 */
public class SchoolProfileProgramsControllerTest extends BaseControllerTestCase {

    SchoolProfileProgramsController _schoolProfileProgramsHighlightsController;
//    IEspResponseDao _espResponseDao;
    SchoolProfileDataHelper _schoolProfileDataHelper;
    State _state;
    School _school;

    public void setUp() throws Exception {
        super.setUp();
//        _espResponseDao = createStrictMock( IEspResponseDao.class );
        _schoolProfileDataHelper = createStrictMock( SchoolProfileDataHelper.class );
        _schoolProfileProgramsHighlightsController = new SchoolProfileProgramsController();
//        _schoolProfileProgramsHighlightsController.setIEspResponseDao( _espResponseDao );
        _schoolProfileProgramsHighlightsController.setSchoolProfileDataHelper( _schoolProfileDataHelper );
        _schoolProfileProgramsHighlightsController.setRequestAttributeHelper(new RequestAttributeHelper());
        StateManager sm = new StateManager();
        _state = sm.getState( "CA" );
        _school = new School();
        getRequest().setAttribute( "school", _school );
    }

    /*
        ************ Tests ************
     */
    // Verifies no exceptions are thrown when the database does not return any data
    public void testNoData() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // No data provided
        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");

        assertNull("testNoData: expected no results", resultsModel);
    }

    // Verifies that all data loaded is returned.  For this test pick keys without allowed lists
    public void testAllDataReturned() {

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

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actualStaffLanguages = resultsModel.get( "highlights/Language/staff_languages" );
        List<String> immersionLanguages = resultsModel.get( "highlights/Language/immersion_language" );

        int expected_count = l.size();
        int actual_count = actualStaffLanguages.size() + immersionLanguages.size();
        assertEquals("testAllDataReturned: Expected number of results is wrong", expected_count, actual_count);

        // This is also a good place to perform some basic tests on the DISPLAY structure returned by the controller
        // "highlights" (which is titles) should have one entry since only 1 section will be shown. "highlightsMap" should have information for 2 sections
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
    public void testAllowedList() {

        // Data the controller needs to load for this test
        // Special Ed - is the first thing in the display.  This is useful for debugging
        // Also can be used to verify only allowed values are returned
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "academic_focus", "special_education" ) );
        l.add( createEspResponse( "academic_focus", "medical" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actual = resultsModel.get( "highlights/SpecEd/academic_focus" );

        List<String> expected = new ArrayList<String>();
        expected.add("Special education");

        assertNotNull( "testAllowedList: SpecEd academic_focus list is null", actual);
        boolean equals = actual.equals( expected );
        assertTrue("testAllowedList: Special Ed academic focus lists are not equal", equals);
    }

    // Verifies that only data in the allowed list is returned when multiple choices are allowed
    public void testAllowedListMultiple() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // *** Health ***
        l.add( createEspResponse( "facilities", "farm" ) );
        l.add( createEspResponse( "facilities", "sports_fields" ) );
        l.add( createEspResponse( "facilities", "garden" ) );
        l.add( createEspResponse( "facilities", "shop" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actual = resultsModel.get( "highlights/Health/facilities" );

        String[] expected = new String[] {"Farm", "Sports fields", "Garden"};
        List<String> expectedList = Arrays.asList( expected );

        assertNotNull( "testAllowedListMultiple: result List is null", actual);
        boolean equals = actual.equals( expectedList );
        assertTrue( "testAllowedListMultiple: Filtering of results where there are multiple allowed values fails", equals );

    }

    // Verifies that the ADDITIONAL_PAGE_DATA data is getting populated and merged correctly
    public void testAdditionalDataMerge() {

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

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> actualStudentClubData = resultsModel.get( "highlights/Arts/student_clubs" );

        String[] expected = new String[] {"Student newspaper", "Yearbook", "Jazzercise"};
        List<String> expectedStudentClubData = Arrays.asList( expected );

        assertNotNull( "testAdditionalDataMerge: Arts student_clubs list is null", actualStudentClubData);
        boolean clubsEqual = actualStudentClubData.equals( expectedStudentClubData );
        assertTrue( "testAdditionalDataMerge: Clubs are different", clubsEqual );
    }

    // Verifies that the ADDITIONAL_PAGE_DATA data is getting populated and merged correctly for the case
    // where the primary DISPLAY_CONFIG does not contribute any data
    public void testAdditionalDataMergeWithNoResultData() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        //  *** student_club related **
        // This is in ADDITIONAL_PAGE_DATA
        l.add( createEspResponse( "foreign_language_other", "After School  Spanish Tuition-based Programs" ) );
        l.add( createEspResponse( "foreign_language_other", "American Sign Language" ) );

        ModelMap map = runController( convertToEspData( l ) );

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

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> admissionInfo = resultsModel.get( "application_info/AppEnroll/admissions_contact_school" );
        assertNotNull( "testSpecialRuleAdmissionsContactSchool: Expected a URL but got null", admissionInfo );
        assertEquals( "testSpecialRuleAdmissionsContactSchool: Expected 2 admission_contact_school values", 2, admissionInfo.size());
        assertTrue( "testSpecialRuleAdmissionsContactSchool: Expected \"Call the school\"", admissionInfo.contains("Call the school"));
        assertTrue( "testSpecialRuleAdmissionsContactSchool: Expected a URL message", admissionInfo.contains("Visit the school's website: http:/www.someSchool.edu") );

    }

    // Tests the applyUniqueDataRules() for immersion / immersion_language
    public void testSpecialRuleImmersion1() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - one entry - Yes, should get yes back
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/immersion" );
        assertEquals( "testSpecialRuleImmersion1: Expected Yes", "Yes", results.get(0) );

    }

    public void testSpecialRuleImmersion2() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 2 - one entry - No, should not get any data back
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "no" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/immersion" );
        assertNull( "testSpecialRuleImmersion2: Expected no data but got: " + results, results );

    }

    public void testSpecialRuleImmersion3() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 3 - two entries: yes and cantonese, should get Cantonese back
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        l.add( createEspResponse( "immersion_language", "cantonese" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/immersion" );
        assertEquals( "testSpecialRuleImmersion3: Expected Cantonese", "Cantonese", results.get(0) );
        assertEquals( "testSpecialRuleImmersion3: Expected 1 result", 1, results.size() );

    }

    // Tests the support info part of the display bean
    public void testSupportData() {

        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - two entries, expect URL
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "yes" ) );
        String routeInfo = "51, 72, 99";
        l.add( createEspResponse( "transportation_shuttle_other", routeInfo ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> shuttleOtherInfo = resultsModel.get( "programs_resources/Resources/transportation_shuttle_other" );
        assertEquals( "testSupportData: retrieval of transportation_shuttle_other failed", routeInfo, shuttleOtherInfo.get(0) );
    }

    // Tests the None handling part of the display bean
    public void testNoneHandlingOneValue() {

        // A field with None handling is programs_resources/Programs/instructional_model and it is set to SHOW_IF_ONLY_VALUE
        // This test runs the controller multiple times for the different test cases
        // *** Test 1 - None and no other entries
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "instructional_model", "none" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/instructional_model" );
        assertEquals( "testNoneHandling: expected None", "None", results.get(0) );

    }

    public void testNoneHandlingTwoValues() {

        // A field with None handling is programs_resources/Programs/instructional_model and it is set to SHOW_IF_ONLY_VALUE
        // This test runs the controller multiple times for the different test cases
        // *** Test 2 - None and other entries
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "instructional_model", "none" ) );
        l.add( createEspResponse( "instructional_model_other", "gifted" ) );

        ModelMap map = runController( convertToEspData( l ) );

        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");
        List<String> results = resultsModel.get( "programs_resources/Programs/instructional_model" );
        assertTrue( "testNoneHandling: expected one result but got: " + results.size(), results.size() == 1);
        assertEquals( "testNoneHandling: expected None to be suppressed", "Gifted", results.get(0) );
    }

    /**
     * School subtype values will be used in certain cases if there is no corresponding ESP data.
     * These are defined in the applyUniqueDataRules() method by calling the enhance_results() method
     */
    public void testSchoolSubtype1() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "instructional_model", "continuation" ) );
        l.add( createEspResponse( "coed", "coed" ) );

        _school.setSubtype( SchoolSubtype.create("continuation") );
        _school.setSubtype( SchoolSubtype.create("all_female") );

        ModelMap map = runController( convertToEspData( l ) );
        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");

        List<String> results = resultsModel.get( "programs_resources/Programs/instructional_model" );
        assertEquals( "testSchoolSubtype1: expected Continuation to be added", "Continuation", results.get(0) );

        results = resultsModel.get( "programs_resources/Basics/coed" );
        assertEquals( "testSchoolSubtype1: expected EspResponse:coed to take precedence over school.subtype", "Coed", results.get(0) );

    }

    public void testSchoolSubtype2() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "instructional_model", "continuation" ) );

        _school.setSubtype( SchoolSubtype.create("all_female") );

        ModelMap map = runController( convertToEspData( l ) );
        Map<String, List<String>> resultsModel = (Map<String, List<String>>) map.get("ProfileData");

        List<String> results = resultsModel.get( "programs_resources/Basics/coed" );
        assertEquals( "All Girls", results.get(0) );

    }

    /*
           ************ End of Tests - support functions follow ************
        */
//    private ModelMap runController( List<EspResponse> espResponses) {
//        ModelMap map = new ModelMap();
//
//        expect( _espResponseDao.getResponsesByKeys(_school, _schoolProfileProgramsHighlightsController.getKeyValuesToExtract() ) ).andReturn( espResponses );
//        replay(_espResponseDao);
//        _schoolProfileProgramsHighlightsController.showHighlightsPage(map, getRequest(), 1, _state);
//        verify(_espResponseDao);
//        return map;
//    }

    private ModelMap runController( Map<String, List<EspResponse>> espData) {
        ModelMap map = new ModelMap();

        expect( _schoolProfileDataHelper.getEspDataForSchool( getRequest() ) ).andReturn( espData );
        replay(_schoolProfileDataHelper);
        _schoolProfileProgramsHighlightsController.showHighlightsPage(map, getRequest());
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

}
