package gs.web.school;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.CmsFeatureDao;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.Publication;
import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.SchoolCensusValue;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.CmsUtil;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import gs.web.util.SpringUtil;
import org.springframework.ui.ModelMap;

import java.util.*;

//import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.*;
//import static org.easymock.classextension.EasyMock.replay;
//import static org.easymock.classextension.EasyMock.reset;

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
    District _district;
    CmsFeatureDao _cmsFeatureDao;
    CmsFeature _cmsFeature;
    IPublicationDao _publicationDaoMock;
    int _cmsVideoContentId = 0;


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

        // Things to setup to use UrlBuilder
        CmsUtil.setCmsEnabled(true);
        // Setup CmsFeature for testing
        _cmsFeatureDao = createStrictMock( CmsFeatureDao.class );
        _cmsFeature = createStrictMock( CmsFeature.class );


        _schoolProfileOverviewController.setCmsFeatureDao( _cmsFeatureDao );

        _publicationDaoMock = createStrictMock(IPublicationDao.class);
        _schoolProfileOverviewController.setPublicationDao( _publicationDaoMock );
    }

    /*
        ************ Tests ************
     */
    // Blank test
    public void testBlank() {

    }

    // ================== Tests for GSRatings tile =================
    // Test w/ 1 academic award.  Expect one back
    public void testGsRatingsSubstitute1A() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("academic_award_1", "Award 1"));

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute1A: content wrong", "award", content);
        List<String> awards = (List<String>) resultsModel.get( "awards" );
        assertEquals("testGsRatingsSubstitute1A: awards length wrong", 1, awards.size());
        assertEquals("testGsRatingsSubstitute1A: awards wrong", "Award 1", awards.get(0));
    }

    // Test w/ 1 academic award 1 service award.  Expect two back
    public void testGsRatingsSubstitute1B() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("academic_award_1", "Award 1"));
        l.add(createEspResponse("service_award_1", "Service 1"));

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute1B: content wrong", "award", content);
        List<String> awards = (List<String>) resultsModel.get( "awards" );
        assertEquals("testGsRatingsSubstitute1B: awards length wrong", 2, awards.size());
        assertEquals("testGsRatingsSubstitute1B: academic award wrong", "Award 1", awards.get(0));
        assertEquals("testGsRatingsSubstitute1B: service award wrong", "Service 1", awards.get(1));
    }

    // Test w/ 4 awards.  Expect only 3 back because that is the max number per the spec
    public void testGsRatingsSubstitute1C() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("academic_award_1", "Award 1"));
        l.add(createEspResponse("academic_award_2", "Award 2"));
        l.add(createEspResponse("service_award_1", "Service 1"));
        l.add(createEspResponse("service_award_2", "Service 2"));

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute3: content wrong", "award", content);
        List<String> awards = (List<String>) resultsModel.get( "awards" );
        assertEquals("testGsRatingsSubstitute1C: awards length wrong", 3, awards.size());
        assertEquals("testGsRatingsSubstitute1C: academic award wrong", "Award 1", awards.get(0));
        assertEquals("testGsRatingsSubstitute1C: academic award wrong", "Award 2", awards.get(1));
        assertEquals("testGsRatingsSubstitute1: service award wrong", "Service 1", awards.get(2));
    }

    // Test Substitute 2 - Test complete sentence building
    public void XtestGsRatingsSubstitute2A() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode( LevelCode.PRESCHOOL_ELEMENTARY);
        SchoolSubtype subType = SchoolSubtype.create("special_education_program,high,all_male,nonprofit,Core_Knowledge");
        _school.setSubtype(subType);
        _school.setGradeLevels(new Grades("PK,KG,1,2,3,4,5,6"));
        _school.setAssociation("NCEA, CEC, NAEYC");
        _school.setAffiliation( "Roman Catholic" );

        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute2A: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println( "testGsRatingsSubstitute2A: autotext is " + autotext );
        assertTrue("testGsRatingsSubstitute2A: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testGsRatingsSubstitute2A: ending autotext wrong: " + autotext, autotext.endsWith("The school belongs to the following associations: NCEA, CEC, NAEYC."));
        assertTrue("testGsRatingsSubstitute2A: ending autotext wrong: " + autotext, autotext.indexOf("in grades") > 0);
        assertTrue("testGsRatingsSubstitute2A: middle autotext wrong: " + autotext, autotext.indexOf("It is all male") > 0);
    }

    // Test Substitute 2 - No associations, no affiliations and only all_male
    public void XtestGsRatingsSubstitute2B() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode( LevelCode.PRESCHOOL_ELEMENTARY);
        SchoolSubtype subType = SchoolSubtype.create("all_male");
        _school.setSubtype(subType);
        _school.setGradeLevels(new Grades("PK,KG,1,2,3,4,5,6"));

        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute2B: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println("testGsRatingsSubstitute2B: autotext is " + autotext);
        assertTrue("testGsRatingsSubstitute2B: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testGsRatingsSubstitute2B: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue("testGsRatingsSubstitute2B: middle autotext wrong: " + autotext, autotext.indexOf("It is all male") > 0);
    }

    // Test Substitute 2 - No associations, no affiliations and no subtypes
    public void XtestGsRatingsSubstitute2C() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        _school.setGradeLevels(new Grades("PK,KG,1,2,3,4,5,6"));

        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute2C: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println("testGsRatingsSubstitute2C: autotext is " + autotext);
        assertTrue("testGsRatingsSubstitute2C: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testGsRatingsSubstitute2C: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue("testGsRatingsSubstitute2C: middle autotext wrong: " + autotext, autotext.indexOf("It is ") == -1);
    }

    // Test Substitute 2 - No associations, no affiliations and no subtypes and grade AE
    public void XtestGsRatingsSubstitute2D() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));

        // This path need school data
        _school.setName( "Test school" );
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        _school.setGradeLevels(new Grades("AE"));

        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute2E: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println("testGsRatingsSubstitute2D: autotext is " + autotext);
        assertTrue("testGsRatingsSubstitute2D: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testGsRatingsSubstitute2D: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue("testGsRatingsSubstitute2D: middle autotext wrong: " + autotext, autotext.indexOf("It is ") == -1);
    }

    // Test Substitute 2 - No associations, no affiliations and no subtypes and ungraded
    public void XtestGsRatingsSubstitute2E() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        Grades grades = Grades.createGrades(Grade.UNGRADED);
        _school.setGradeLevels(grades);

        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("ratings");
        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute2E: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println( "testGsRatingsSubstitute2E: autotext is " + autotext );
        assertTrue("testGsRatingsSubstitute2E: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testGsRatingsSubstitute2E: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue( "testGsRatingsSubstitute2E: ungraded autotext wrong: " + autotext, autotext.indexOf( "ungraded")!=-1 );
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

    // =========== Tests for Tile 3 - school video ====================
    // Tests the default action of returning the url of the video
    public void testVideoDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        String url = "http://www.youtube.com/watch?v=eImToAYIq7o";
        l.add( createEspResponse( "school_video", url ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("video");
        assertEquals( "testVideoDefaultA: content wrong", "video", resultsModel.get("content") );
        assertEquals( "testVideoDefaultA: url wrong", url, resultsModel.get("videoUrl") );
        System.out.println("testVideoDefaultA successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testVideoSubstituteA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "something", "doesnt matter what" ) );

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);
        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, String> resultsModel = (Map<String, String>) map.get("video");
        assertEquals( "testVideoSubstituteA: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testVideoSubstituteA: school level wrong", "e", resultsModel.get("schoolLevel") );
        assertTrue( "testVideoSubstituteA: videoId wrong", (resultsModel.get("contentUrl").indexOf(Integer.toString(_cmsVideoContentId)))>0);
        System.out.println("testVideoSubstituteA successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testVideoSubstituteB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "something", "doesnt matter what" ) );

        // Set the school level since that controls which CMS video will be choosen
        _school.setLevelCode( LevelCode.HIGH);
        _cmsVideoContentId = 6855;  // This is contentId for high school video

        Map map = runController( convertToEspData( l ) );

        Map<String, String> resultsModel = (Map<String, String>) map.get("video");
        assertEquals( "testVideoSubstituteB: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testVideoSubstituteB: school level wrong", "h", resultsModel.get("schoolLevel") );
        assertTrue( "testVideoSubstituteB: videoId wrong", (resultsModel.get("contentUrl").indexOf(Integer.toString(_cmsVideoContentId)))>0);
        System.out.println("testVideoSubstituteB successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testVideoSubstituteC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "something", "doesnt matter what" ) );

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.PRESCHOOL);
        _cmsVideoContentId = 6857;  // This is contentId for elementary school video

        Map map = runController( convertToEspData( l ) );

        Map<String, String> resultsModel = (Map<String, String>) map.get("video");
        assertEquals( "testVideoSubstituteC: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testVideoSubstituteC: school level wrong", "e", resultsModel.get("schoolLevel") );
        assertTrue( "testVideoSubstituteC: videoId wrong", (resultsModel.get("contentUrl").indexOf(Integer.toString(_cmsVideoContentId)))>0);
        System.out.println("testVideoSubstituteC successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testVideoSubstituteD() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "something", "doesnt matter what" ) );

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.MIDDLE_HIGH);
        _cmsVideoContentId = 6856;  // This is contentId for middle school video

        Map map = runController( convertToEspData( l ) );

        Map<String, String> resultsModel = (Map<String, String>) map.get("video");
        assertEquals( "testVideoSubstituteD: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testVideoSubstituteD: school level wrong", "m", resultsModel.get("schoolLevel") );
        assertTrue( "testVideoSubstituteD: videoId wrong", (resultsModel.get("contentUrl").indexOf(Integer.toString(_cmsVideoContentId)))>0);
        System.out.println("testVideoSubstituteD successful");
    }

    // =========== Tests for Tile 8 - Spec Ed / Extended care ====================
    // Tests the Special Education substitute display is selected
    public void testSpecEd1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );  // There has to be something in the EspResponse list or the non-ESP path runs


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertFalse( "testSpecEd1: Substitute content expected", resultsModel.get("content").equals("default") );
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
    public void XtestExtdCareTitleA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );

        _school.setLevelCode( LevelCode.MIDDLE);
        _cmsVideoContentId = 6856;  // This is contentId for middle school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleA: default content expected", "Extended care", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleA successful" );
    }

    // Tests the Extended care Title
    public void XtestExtdCareTitleB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );

        _school.setLevelCode( LevelCode.HIGH);
        _cmsVideoContentId = 6855;  // This is contentId for high school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleB: default content expected", "Extended programs", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleB successful" );
    }

    // Tests the Extended care Title
    public void XtestExtdCareTitleC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );


        _school.setLevelCode(LevelCode.MIDDLE_HIGH);
        _cmsVideoContentId = 6856;  // This is contentId for middle school video

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleC: default content expected", "Extended programs", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleC successful" );
    }

    // Tests the Special Education substitute (Teachers/staff) - no administrator
    public void testSpecEdSubstitute1A() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "staff_resources", "art_teacher" ) );


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        String sentence = (String) resultsModel.get("teachersStaff");
        assertTrue( "testSpecEdSubstitute1A: Substitute content expected content not found", sentence.indexOf("Art teacher")>0 );
        assertTrue( "testSpecEdSubstitute1A: Substitute content expected content not found", sentence.indexOf("Staff includes")==0 );
        System.out.println("testSpecEdSubstitute1A successful");
    }

    // Tests the Special Education substitute (Teachers/staff) - no administrator, multiple staff_resources
    public void testSpecEdSubstitute1B() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "staff_resources", "art_teacher" ) );
        l.add( createEspResponse( "staff_resources", "computer_specialist" ) );


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        String sentence = (String) resultsModel.get("teachersStaff");
        assertTrue( "testSpecEdSubstitute1B: Substitute content expected content not found", sentence.indexOf("Art teacher")>0 );
        assertTrue( "testSpecEdSubstitute1B: Substitute content expected content not found", sentence.indexOf("Computer specialist")>0 );
        assertTrue( "testSpecEdSubstitute1B: Substitute content expected content not found", sentence.indexOf("Staff includes")==0 );
        System.out.println("testSpecEdSubstitute1B successful");
    }

    // Tests the Special Education substitute (Teachers/staff) - administrator and 'none' staff_resources
    public void testSpecEdSubstitute1C() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "administrator_name", "Ben Jones" ) );
        l.add( createEspResponse( "staff_resources", "none" ) );


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        String sentence = (String) resultsModel.get("teachersStaff");
        assertTrue( "testSpecEdSubstitute1C: Substitute content expected content not found", sentence.indexOf("Ben Jones")==0 );
        assertTrue( "testSpecEdSubstitute1C: Substitute content expected content not found", sentence.indexOf("Staff includes")==-1 );
        System.out.println("testSpecEdSubstitute1C successful");
    }

    // Tests the Special Education substitute (Teachers/staff) - no administrator
    public void testSpecEdSubstitute1D() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "administrator_name", "Ben Jones" ) );
        l.add( createEspResponse( "staff_resources", "art_teacher" ) );
        l.add( createEspResponse( "staff_resources", "ell_esl_coord" ) );
        l.add( createEspResponse( "staff_resources", "gifted_specialist" ) );


        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        String sentence = (String) resultsModel.get("teachersStaff");
        assertTrue( "testSpecEdSubstitute1C: Substitute content expected content not found", sentence.indexOf("Ben Jones")==0 );
        assertTrue( "testSpecEdSubstitute1D: Substitute content expected content not found", sentence.indexOf("Art teacher")>0 );
        assertTrue( "testSpecEdSubstitute1D: Substitute content expected content not found", sentence.indexOf("Gifted specialist")>0 );
        System.out.println("testSpecEdSubstitute1D successful");
    }

    // ========= Tests for Transportation default content ========
    // Test option a
    public void testTransportationDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "yes" ) );
        l.add( createEspResponse( "transportation_shuttle_other", "a, b, c" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultA: icon wrong", "metro", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultA: shuttleStops wrong", "A, b, c", resultsModel.get( "shuttleStops") );
        System.out.println( "testTransportationDefaultA successful" );
    }

    // Test option b
    public void testTransportationDefaultB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "yes" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultB: icon wrong", "metro", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultB: shuttleStops wrong", "Shuttles are provide to local Metro stops", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultB successful" );
    }

    // Test option c
    public void testTransportationDefaultC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "no" ) );
        l.add( createEspResponse( "transportation_shuttle_other", "a, b, c" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultC: icon wrong", "walking", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultC: shuttleStops wrong", "No transportation provided", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultC successful" );
    }

    // Test option d
    public void testTransportationDefaultD() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "passes" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultD: icon wrong", "passes", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultD: message wrong", "Passes/tokens for public transportation", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultD successful" );
    }

    // Test option e
    public void testTransportationDefaultE() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "special_ed_only" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultE: icon wrong", "handicapped", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultE: message wrong", "Transportation provided for special education students only", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultE successful" );
    }

    // Test option f
    public void testTransportationDefaultF() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "busses" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultF: icon wrong", "bus", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultF: message wrong", "Busses/vans for our students only", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultF successful" );
    }

    // Test option g
    public void testTransportationDefaultG() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "shared_bus" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultG: icon wrong", "bus", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultG: message wrong", "School shares bus/van with other schools", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultG successful" );
    }

    // Test option h
    public void testTransportationDefaultH() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "none" ) );
        l.add( createEspResponse( "transportation_other", "skateboard" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultH: icon wrong", "walking", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultH: message wrong", "Other transportation provided", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultH successful" );
    }

    // Test option i
    public void testTransportationDefaultI() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "none" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("transportation");
        assertEquals( "testTransportationDefaultI: icon wrong", "walking", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultI: message wrong", "No transportation provided", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultI successful" );
    }



    // Tests for Transportation substitute1 content
    public void testTransportSubstitute1A() {

        // TODO - come back to this after Samson has enhanced the SchoolProfileDataHelper class

        /*
        List<CensusDataSet> l = new ArrayList<CensusDataSet>();
        CensusDataSet cds1 = new CensusDataSet( CensusDataType.CLASS_SIZE, 2012 );

        SchoolCensusValue csv1 =
        cds1.
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );


        _school.setLevelCode( LevelCode.MIDDLE_HIGH);

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("specialEd");
        assertEquals( "testExtdCareTitleC: default content expected", "Extended programs", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleC successful" );
        */
    }

    // ============= Tests for Programs Tile ===============
    // immersion no, instructional model none, academic focus none
    public void testProgramsDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "no" ) );
        l.add( createEspResponse( "instructional_model", "none" ) );
        l.add( createEspResponse( "academic_focus", "none" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertNull( "testProgramsDefaultA: expected no results", resultsList );
        System.out.println( "testProgramsDefaultA successful" );
    }

    // immersion yes but no languages, instructional model one value, academic focus one value
    public void testProgramsDefaultB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        l.add( createEspResponse( "instructional_model", "honors" ) );
        l.add( createEspResponse( "academic_focus", "science" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertEquals("testProgramsDefaultB: immersion wrong", "Language immersion", resultsList.get(0));
        assertEquals("testProgramsDefaultB: instructional_model wrong", "Honors", resultsList.get(1));
        assertEquals("testProgramsDefaultB: academic_focus wrong", "School focus: Science", resultsList.get(2));
        System.out.println( "testProgramsDefaultA successful" );
    }

    // immersion yes with languages, instructional model two values, academic focus two values
    public void testProgramsDefaultC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        l.add( createEspResponse( "immersion_language", "chinese" ) );
        l.add( createEspResponse( "instructional_model", "honors" ) );
        l.add( createEspResponse( "instructional_model", "independent_study" ) );
        l.add( createEspResponse( "academic_focus", "technology" ) );
        l.add( createEspResponse( "academic_focus", "science" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertEquals("testProgramsDefaultC: immersion wrong", "Chinese immersion", resultsList.get(0));
        assertEquals("testProgramsDefaultC: instructional_model wrong", "Honors, Independent study", resultsList.get(1));
        assertEquals("testProgramsDefaultC: academic_focus wrong", "School focus: Science, Technology", resultsList.get(2));
        System.out.println( "testProgramsDefaultC successful" );
    }

    // immersion yes with language_other, instructional model two values + other, academic focus two values and other
    public void testProgramsDefaultD() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        //l.add( createEspResponse( "immersion_language", "chinese" ) );
        l.add( createEspResponse( "immersion_language_other", "vietnamese" ) );
        l.add( createEspResponse( "instructional_model", "honors" ) );
        l.add( createEspResponse( "instructional_model", "independent_study" ) );
        l.add( createEspResponse( "instructional_model_other", "basket weaving" ) );
        l.add( createEspResponse( "academic_focus", "technology" ) );
        l.add( createEspResponse( "academic_focus", "science" ) );
        l.add( createEspResponse( "academic_focus_other", "something else" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertEquals("testProgramsDefaultD: immersion wrong", "Language immersion", resultsList.get(0));
        assertEquals("testProgramsDefaultD: instructional_model wrong", "Honors, Independent study, Basket weaving", resultsList.get(1));
        assertEquals("testProgramsDefaultD: academic_focus wrong", "School focus: Science, Technology, Something else", resultsList.get(2));
        System.out.println( "testProgramsDefaultD successful" );
    }

    // immersion yes with language_other, instructional model two values + other, academic focus two values and other
    public void testProgramsDefaultE() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        l.add( createEspResponse( "immersion_language_other", "vietnamese" ) );
        l.add( createEspResponse( "instructional_model_other", "basket weaving" ) );
        l.add( createEspResponse( "academic_focus_other", "something else" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertEquals("testProgramsDefaultE: immersion wrong", "Language immersion", resultsList.get(0));
        assertEquals("testProgramsDefaultE: instructional_model wrong", "Basket weaving", resultsList.get(1));
        assertEquals("testProgramsDefaultE: academic_focus wrong", "School focus: Something else", resultsList.get(2));
        System.out.println( "testProgramsDefaultE successful" );
    }

    // Substitute 1 with parent_involvement = 'none'
    public void testProgramsSubstitute1A() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "parent_involvement", "none" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        assertEquals("testProgramsSubstitute1A: wrong", "true", resultsModel.get("substitute1None"));
        System.out.println( "testProgramsSubstitute1A successful" );
    }

    // Substitute 1 with parent_involvement of 2 values
    public void testProgramsSubstitute1B() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "parent_involvement", "parent_nights_req" ) );
        l.add( createEspResponse( "parent_involvement", "chaperone_req" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        List<String> results = (List<String>) resultsModel.get( "substitute1List" );
        assertEquals( "testProgramsSubstitute1B: wrong", 2, results.size() );
        assertEquals( "testProgramsSubstitute1B: wrong", "Chaperone req", results.get(0) );
        assertEquals("testProgramsSubstitute1B: wrong", "Parent nights req", results.get(1));
        System.out.println( "testProgramsSubstitute1B successful" );
    }

    // Substitute 1 with parent_involvement = 'none'
    public void testProgramsSubstitute2A() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "parent_involvementXXX", "none" ) );

        SchoolSubtype subType = SchoolSubtype.create("high, magnet, all_male");
        _school.setSubtype(subType);
        _school.setAssociation( "NCEA" );
        _school.setAffiliation( "Roman Catholic" );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("programs");
        assertEquals("testProgramsSubstitute2A: wrong content value", "substitute2", resultsModel.get("content"));
        List<String> values = (List<String>) resultsModel.get( "substitute2List" );
        assertEquals("testProgramsSubstitute2A: wrong size", 4, values.size() );
        assertEquals("testProgramsSubstitute2A: wrong value 0", "Magnet", values.get(0) );
        assertEquals("testProgramsSubstitute2A: wrong value 1", "All male", values.get(1) );
        assertEquals("testProgramsSubstitute2A: wrong value 2", "Affiliation: Roman Catholic", values.get(2) );
        assertEquals("testProgramsSubstitute2A: wrong value 3", "Associations: NCEA", values.get(3) );
        System.out.println( "testProgramsSubstitute2A successful" );
    }


    // ============= Tests for Application Info Tile ===============
    // all data provided
    public void testApplInfoDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "yes" ) );
        l.add( createEspResponse( "students_accepted", "123" ) );
        l.add( createEspResponse( "applications_received", "592" ) );
        l.add( createEspResponse( "applications_received_year", "2011-2012" ) );
        l.add( createEspResponse( "students_accepted_year", "2011-2012" ) );

        l.add( createEspResponse( "application_deadline", "date" ) );
        l.add( createEspResponse( "application_deadline_date", "July 30, 2012" ) );

        l.add( createEspResponse( "vouchers", "blank" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("applInfo");
        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoDefaultA: content wrong", "applInfoV1", contentType );
        String percent = (String) resultsModel.get( "acceptanceRatePercent" );
        System.out.println( "testApplInfoDefaultA acceptance Rate Percent = " + percent );
        assertNotNull( "testApplInfoDefaultA: acceptanceRatePercent should not be null", percent );

        assertEquals( "testApplInfoDefaultA: applicationDeadlineMsg wrong", "apply", (String) resultsModel.get( "applicationDeadlineMsg" ) );
        assertEquals( "testApplInfoDefaultA: applicationDeadlineDate wrong", "July 30, 2012", (String) resultsModel.get( "applicationDeadlineDate" ) );

        assertEquals( "testApplInfoDefaultA: vouchers wrong", "blank", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoDefaultA successful" );
    }

    // application deadline = date but the date is missing and vouchers
    public void testApplInfoDefaultB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "yes" ) );
        l.add( createEspResponse( "students_accepted", "123" ) );
        l.add( createEspResponse( "applications_received", "592" ) );
        l.add( createEspResponse( "applications_received_year", "2011-2012" ) );
        l.add( createEspResponse( "students_accepted_year", "2011-2012" ) );

        l.add( createEspResponse( "application_deadline", "date" ) );

        l.add( createEspResponse( "vouchers", "yes" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("applInfo");
        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoDefaultB: content wrong", "applInfoV1", contentType );
        String percent = (String) resultsModel.get( "acceptanceRatePercent" );
        System.out.println( "testApplInfoDefaultB acceptance Rate Percent = " + percent );
        assertNotNull("testApplInfoDefaultB: acceptanceRatePercent should not be null", percent);

        assertEquals( "testApplInfoDefaultB: applicationDeadlineMsg wrong", "call", (String) resultsModel.get( "applicationDeadlineMsg" ) );

        assertEquals( "testApplInfoDefaultB: vouchers wrong", "yes", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoDefaultB successful" );
    }

    // application deadline = yearround and no vouchers
    public void testApplInfoDefaultC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "yes" ) );
        l.add( createEspResponse( "students_accepted", "123" ) );
        l.add( createEspResponse( "applications_received", "592" ) );
        l.add( createEspResponse( "applications_received_year", "2011-2012" ) );
        l.add( createEspResponse( "students_accepted_year", "2011-2012" ) );

        l.add( createEspResponse( "application_deadline", "parents_contact" ) );

        l.add( createEspResponse( "vouchers", "no" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("applInfo");
        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoDefaultC: content wrong", "applInfoV1", contentType );
        String percent = (String) resultsModel.get( "acceptanceRatePercent" );
        System.out.println( "testApplInfoDefaultC acceptance Rate Percent = " + percent );
        assertNotNull("testApplInfoDefaultC: acceptanceRatePercent should not be null", percent);

        assertEquals( "testApplInfoDefaultC: applicationDeadlineMsg wrong", "call", (String) resultsModel.get( "applicationDeadlineMsg" ) );

        assertEquals( "testApplInfoDefaultC: vouchers wrong", "no", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoDefaultC successful" );
    }

    // application process yes but no data to calc acceptance rate, deadline = date with date supplied and no vouchers
    public void testApplInfoSubstitute1A() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "yes" ) );

        l.add( createEspResponse( "application_deadline", "date" ) );
        l.add( createEspResponse( "application_deadline_date", "July 30, 2012" ) );

        l.add( createEspResponse( "vouchers", "no" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("applInfo");
        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoSubstitute1A: content wrong", "applInfoV2", contentType );

        assertEquals( "testApplInfoSubstitute1A: applicationDeadlineMsg wrong", "apply", (String) resultsModel.get( "applicationDeadlineMsg" ) );
        assertEquals( "testApplInfoSubstitute1A: applicationDeadlineDate wrong", "July 30, 2012", (String) resultsModel.get( "applicationDeadlineDate" ) );

        assertEquals( "testApplInfoSubstitute1A: vouchers wrong", "no", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoSubstitute1A successful" );
    }

    // application process yes but no data to calc acceptance rate, deadline = date with date supplied and no vouchers
    public void testApplInfoSubstitute2A() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "no" ) );

        l.add( createEspResponse( "application_deadline", "date" ) );
        l.add( createEspResponse( "application_deadline_date", "July 30, 2012" ) );

        l.add( createEspResponse( "vouchers", "no" ) );

        Map map = runController( convertToEspData( l ) );

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("applInfo");
        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoSubstitute2A: content wrong", "visitChecklist", contentType );

        System.out.println("testApplInfoSubstitute2A successful");
    }

    // Local info substitute 1 - District info
    public void testLocalInfoSubstitute1A() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "no" ) );      // Need something

        _district = new District();
        _district.setNumberOfSchools(25);
        Grades grades = Grades.createGrades(Grade.G_1, Grade.G_6);
        _district.setGradeLevels(grades);
        IDistrictDao districtDao = createStrictMock(IDistrictDao.class);

        _school.setDistrictDao( districtDao );
        _school.setDistrictId(13);
        _school.setDatabaseState(_state);

        expect(districtDao.findDistrictById(_state, 13)).andReturn(_district);
        replay(districtDao);


        Map map = runController( convertToEspData( l ) );

        verify(districtDao);

        Map<String, Object> resultsModel = (Map<String, Object>) map.get("localInfo");
        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoSubstitute2A: content wrong", "districtInfo", contentType );

        System.out.println("testApplInfoSubstitute2A successful");
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

        expect( _schoolProfileDataHelper.getEspDataForSchool(getRequest()) ).andReturn( espData );
        expect( _schoolProfileDataHelper.getSchoolMedia(getRequest())).andReturn(null);
        expect( _schoolProfileDataHelper.getSchoolRatings(getRequest())).andReturn(null);
        expect( _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews(getRequest()) ).andReturn( new Long(0l));
        expect(_schoolProfileDataHelper.getNonPrincipalReviews(getRequest(), 5)).andReturn(null);
        expect( _schoolProfileDataHelper.getSchoolCensusValues(getRequest()) ).andReturn(null).times(1, 2);
        expect( _schoolProfileDataHelper.getSperlingsInfo(getRequest())).andReturn(null).anyTimes();
//        expect( _schoolProfileDataHelper.getNearbySchools( getRequest(), 20 ) ).andReturn(null);
//        expectLastCall().anyTimes();

        Long contentId = new Long(_cmsVideoContentId );
        ContentKey contentKey = new ContentKey( "video", new Long(_cmsVideoContentId) );
        expect( _cmsFeature.getContentKey() ).andReturn(contentKey).anyTimes();
        expect( _cmsFeature.getImageUrl() ).andReturn( "/imageUrl.gs" ).anyTimes();
        expect( _cmsFeature.getImageAltText() ).andReturn( "alt text" ).anyTimes();

        expect( _cmsFeatureDao.get( contentId) ).andReturn(_cmsFeature).anyTimes();

        Publication pub = new Publication();
        pub.setFullUri("http://somehost/fullUri");
        expect( _publicationDaoMock.findByContentKey(contentKey) ).andReturn(pub).anyTimes();

        replay( _cmsFeature );
        replay( _cmsFeatureDao );
        replay(_schoolProfileDataHelper);
        replay(_publicationDaoMock);
        _schoolProfileOverviewController.handle(map, getRequest());
        verify(_publicationDaoMock);
        verify(_schoolProfileDataHelper);
        verify( _cmsFeatureDao );
        verify( _cmsFeature );

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
