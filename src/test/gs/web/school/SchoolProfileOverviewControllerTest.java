package gs.web.school;

import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ContentKey;
import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.SchoolCensusValue;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.school.review.Review;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.CmsUtil;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
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
    District _district;
    CmsFeature _cmsFeature;
    boolean _cmsEnabled;

    CmsFeatureSearchService _cmsFeatureSearchService;


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
        _cmsEnabled = CmsUtil.isCmsEnabled();
        CmsUtil.enableCms();
        // Setup CmsFeature for testing
        _cmsFeature = createStrictMock( CmsFeature.class );
//        _cmsFeatureDao = createStrictMock( CmsFeatureDao.class );
//        _schoolProfileOverviewController.setCmsFeatureDao( _cmsFeatureDao );
//
//        _publicationDaoMock = createStrictMock(IPublicationDao.class);
//        _schoolProfileOverviewController.setPublicationDao( _publicationDaoMock );

        _cmsFeatureSearchService = createStrictMock(CmsFeatureSearchService.class);
        _schoolProfileOverviewController.setCmsFeatureSearchService(_cmsFeatureSearchService);
    }

    public void tearDown() {
        CmsUtil.setCmsEnabled(_cmsEnabled);
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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getGsRatingsEspTile( _request, _school, espData );

        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute1A: content wrong", "awards", content);
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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getGsRatingsEspTile( _request, _school, espData );

        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute1B: content wrong", "awards", content);
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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getGsRatingsEspTile( _request, _school, espData );

        String content = (String) resultsModel.get("content");
        assertEquals("testGsRatingsSubstitute1C: content wrong", "awards", content);
        List<String> awards = (List<String>) resultsModel.get( "awards" );
        assertEquals("testGsRatingsSubstitute1C: awards length wrong", 3, awards.size());
        assertEquals("testGsRatingsSubstitute1C: academic award wrong", "Award 1", awards.get(0));
        assertEquals("testGsRatingsSubstitute1C: academic award wrong", "Award 2", awards.get(1));
        assertEquals("testGsRatingsSubstitute1C: service award wrong", "Service 1", awards.get(2));
    }

    // Test Substitute 2 - Test complete sentence building
    public void testSchoolAutotextA() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));
        Map<String, List<EspResponse>> espData = convertToEspData(l);

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

        //Map resultsModel = _schoolProfileOverviewController.getGsRatingsEspTile( _request, _school, espData );
        Map resultsModel = runGsRatingsEspTileWithCensusMockController( _request, _school, espData, 500 );
        String content = (String) resultsModel.get("content");
        assertEquals("testSchoolAutotextA: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println( "testSchoolAutotextA: autotext is " + autotext );
        assertTrue("testSchoolAutotextA: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testSchoolAutotextA: ending autotext wrong: " + autotext, autotext.endsWith("The school belongs to the following associations: NCEA, CEC, NAEYC."));
        assertTrue("testSchoolAutotextA: ending autotext wrong: " + autotext, autotext.indexOf("in grades") > 0);
        assertTrue("testSchoolAutotextA: middle autotext wrong: " + autotext, autotext.indexOf("It is all male") > 0);
        assertTrue("testSchoolAutotextA: number of students autotext wrong: " + autotext, autotext.indexOf("serving 500 students") > 0);
    }

    // Test Substitute 2 - No associations, no affiliations and only all_male
    public void testSchoolAutotextB() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode( LevelCode.PRESCHOOL_ELEMENTARY);
        SchoolSubtype subType = SchoolSubtype.create("all_male");
        _school.setSubtype(subType);
        _school.setGradeLevels(new Grades("PK"));

        Map resultsModel = runGsRatingsEspTileWithCensusMockController( _request, _school, espData, 512 );
        String content = (String) resultsModel.get("content");
        assertEquals("testSchoolAutotextB: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println("testSchoolAutotextB: autotext is " + autotext);
        assertTrue("testSchoolAutotextB: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testSchoolAutotextB: grades autotext wrong: " + autotext, autotext.indexOf("in grade ") > 0);
        assertTrue("testSchoolAutotextB: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue("testSchoolAutotextB: middle autotext wrong: " + autotext, autotext.indexOf("It is all male") > 0);
    }

    // Test Substitute 2 - No associations, no affiliations and no subtypes
    public void testSchoolAutotextC() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        _school.setGradeLevels(new Grades("PK,KG,1,2,3,4,5,6"));

        Map resultsModel = runGsRatingsEspTileWithCensusMockController( _request, _school, espData, 512 );
        String content = (String) resultsModel.get("content");
        assertEquals("testSchoolAutotextC: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println("testSchoolAutotextC: autotext is " + autotext);
        assertTrue("testSchoolAutotextC: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testSchoolAutotextC: grades autotext wrong: " + autotext, autotext.indexOf("in grades ") > 0);
        assertTrue("testSchoolAutotextC: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue("testSchoolAutotextC: middle autotext wrong: " + autotext, autotext.indexOf("It is ") == -1);
    }

    // Test Substitute 2 - No associations, no affiliations and no subtypes and grade AE
    public void testSchoolAutotextD() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        // This path need school data
        _school.setName( "Test school" );
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        _school.setGradeLevels(new Grades("AE"));

        Map resultsModel = runGsRatingsEspTileWithCensusMockController( _request, _school, espData, 512 );
        String content = (String) resultsModel.get("content");
        assertEquals("testSchoolAutotextD: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println("testSchoolAutotextD: autotext is " + autotext);
        assertTrue("testSchoolAutotextD: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testSchoolAutotextD: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue("testSchoolAutotextD: middle autotext wrong: " + autotext, autotext.indexOf("It is ") == -1);
    }

    // Test Substitute 2 - No associations, no affiliations and no subtypes and ungraded
    public void testSchoolAutotextE() {

        // No data is needed for this test but use the following as it is not needed for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add(createEspResponse("XXX", "yyy"));
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        // This path need school data
        _school.setName("Test school");
        _school.setCity("San Francisco");
        _school.setType(SchoolType.PUBLIC);
        _school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        Grades grades = Grades.createGrades(Grade.UNGRADED);
        _school.setGradeLevels(grades);

        Map resultsModel = runGsRatingsEspTileWithCensusMockController( _request, _school, espData, 512 );
        String content = (String) resultsModel.get("content");
        assertEquals("testSchoolAutotextE: content wrong", "schoolAutotext", content);
        String autotext = (String) resultsModel.get( "autotext" );
        System.out.println( "testSchoolAutotextE: autotext is " + autotext );
        assertTrue("testSchoolAutotextE: beginning autotext wrong: " + autotext, autotext.startsWith("San Francisco's Test school is a public school serving"));
        assertTrue("testSchoolAutotextE: ending autotext wrong: " + autotext, autotext.indexOf("The school belongs to the following associations") == -1);
        assertTrue( "testGsRatingsSchoolAutotextSubstitute2E: ungraded autotext wrong: " + autotext, autotext.indexOf( "ungraded")!=-1 );
    }

    private Map<String, Object> runGsRatingsEspTileWithCensusMockController( HttpServletRequest request, School school, Map<String, List<EspResponse>> espData, Integer numStudents ) {

        Map<CensusDataType, List<CensusDataSet>> censusValues = new HashMap<CensusDataType, List<CensusDataSet>>(2);

        // setup avg class size census data
        if( numStudents > 0 ) {
            List<CensusDataSet> enrollment = new ArrayList<CensusDataSet>();
            censusValues.put( CensusDataType.STUDENTS_ENROLLMENT, enrollment );
            CensusDataSet enrollmentCDS = new CensusDataSet( CensusDataType.STUDENTS_ENROLLMENT, 2011 );
            enrollment.add(enrollmentCDS);
            Set<SchoolCensusValue> enrollmentSet = new HashSet<SchoolCensusValue>(1);
            enrollmentCDS.setSchoolData(enrollmentSet);
            SchoolCensusValue enrollmentCSV = new SchoolCensusValue();
            enrollmentSet.add(enrollmentCSV);
            enrollmentCSV.setSchool(_school);
            enrollmentCSV.setValueInteger(numStudents);
        }

        //school has no ratings.
        Map<String, Object> ratingsMap = new HashMap();
        expect( _schoolProfileDataHelper.getGsRatings(getRequest())).andReturn(ratingsMap);
        expect( _schoolProfileDataHelper.getSchoolCensusValues(getRequest()) ).andReturn( censusValues );

        replay(_schoolProfileDataHelper);

        Map<String, Object> model = _schoolProfileOverviewController.getGsRatingsEspTile(request, _school, espData);

        verify(_schoolProfileDataHelper);

        return model;
    }

    // Tests for no Sports/Arts/Music data
    public void testSportsArtsMusicNoData() {

        // Data the controller needs to load for this test
        List<EspResponse> l = new ArrayList<EspResponse>();
        // Need to add something that is not Sorts/Arts/Music related just so the NonEsp route is not taken
        l.add(createEspResponse("staff_languages", "arabic"));
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSportsArtsMusicEspTile( espData );

        String result = (String) resultsModel.get("content");
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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSportsArtsMusicEspTile( espData );

        List<String> boysSports = (List<String>) resultsModel.get("boys_sports");
        List<String> girlsSports = (List<String>) resultsModel.get("girls_sports");

        assertEquals("testSportsArtsMusicSports: wrong number of boys_sport", 2, boysSports.size());
        assertEquals("testSportsArtsMusicSports: wrong boys_sports(0)", "Baseball", boysSports.get(0));
        assertEquals("testSportsArtsMusicSports: wrong boys_sports(1)", "Football", boysSports.get(1));
        assertTrue("testSportsArtsMusicSports: expected girls_sports size = 0", (girlsSports.size() == 0) );
    }

    // =========== Tests for Tile 3 - school video ====================
    // Tests the default action of returning the url of the video
    public void testVideoDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        String url = "http://www.youtube.com/watch?v=eImToAYIq7o";
        l.add( createEspResponse( "school_video", url ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

//        Map map = runController( convertToEspData( l ) );

        Map resultsModel = _schoolProfileOverviewController.getVideoEspTile(_request, _school, espData);
//        Map<String, Object> resultsModel = (Map<String, Object>) map.get("video");
        assertEquals( "testVideoDefaultA: content wrong", "video", resultsModel.get("content") );
        assertEquals( "testVideoDefaultA: url wrong", url, resultsModel.get("videoUrl") );
        System.out.println("testVideoDefaultA successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testSchoolTourVideoA() {

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);
        String cmsVideoContentId = SchoolProfileOverviewController.VIDEO_ELEMENTARY;

        Map<String, Object> resultsModel = runCmsVideoTourModel(cmsVideoContentId);

        assertEquals( "testSchoolTourVideoA: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testSchoolTourVideoA: school level wrong", "e", resultsModel.get("schoolLevel") );
        String contentUrl = (((String)resultsModel.get("contentUrl")));
        assertTrue( "testSchoolTourVideoA: videoId wrong", contentUrl.indexOf(cmsVideoContentId)>=0);
        System.out.println("testSchoolTourVideoA successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testSchoolTourVideoB() {

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.HIGH);
        String cmsVideoContentId = SchoolProfileOverviewController.VIDEO_HIGH;

        Map<String, Object> resultsModel = runCmsVideoTourModel(cmsVideoContentId);

        assertEquals( "testSchoolTourVideoB: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testSchoolTourVideoB: school level wrong", "h", resultsModel.get("schoolLevel") );
        assertTrue( "testSchoolTourVideoB: videoId wrong", (((String)resultsModel.get("contentUrl")).indexOf(cmsVideoContentId))>0);
        System.out.println("testSchoolTourVideoB successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testSchoolTourVideoC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "something", "doesnt matter what" ) );

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.PRESCHOOL);
        String cmsVideoContentId = SchoolProfileOverviewController.VIDEO_ELEMENTARY;

        Map<String, Object> resultsModel = runCmsVideoTourModel(cmsVideoContentId);

        assertEquals( "testSchoolTourVideoC: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testSchoolTourVideoC: school level wrong", "p", resultsModel.get("schoolLevel") );
        assertTrue( "testSchoolTourVideoC: videoId wrong", (((String)resultsModel.get("contentUrl")).indexOf(cmsVideoContentId))>0);
        System.out.println("testSchoolTourVideoC successful");
    }

    // Tests the substitute action of returning the lowest school level
    public void testSchoolTourVideoD() {

        // Set the school level since that controls which CMS video will be chosen
        _school.setLevelCode( LevelCode.MIDDLE_HIGH);
        String cmsVideoContentId = SchoolProfileOverviewController.VIDEO_MIDDLE;

        Map<String, Object> resultsModel = runCmsVideoTourModel(cmsVideoContentId);

        assertEquals( "testSchoolTourVideoD: content wrong", "schoolTourVideo", resultsModel.get("content") );
        assertEquals( "testSchoolTourVideoD: school level wrong", "m", resultsModel.get("schoolLevel") );
        assertTrue( "testSchoolTourVideoD: videoId wrong", (((String)resultsModel.get("contentUrl")).indexOf(cmsVideoContentId))>0);
        System.out.println("testSchoolTourVideoD successful");
    }

    // ========== Tests for Tile 8 - Spec Ed / Extended care ====================
    // Tests the Special Education substitute display is selected
    public void testSpecEd1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );  // There has to be something in the EspResponse list or the non-ESP path runs
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertFalse( "testSpecEd1: Substitute content expected", resultsModel.get("content").equals("default") );
        System.out.println("testSpecEd1 successful");
    }

    // Tests the Special Education Default with display option a is selected
    public void testSpecEdA() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "yes" ) );  // This should trigger default display
        l.add( createEspResponse( "special_ed_programs", "blindness" ) );  // This should trigger default display
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals("testSpecEdB: default content expected", "yes", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdB: wrong option", "b", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdB successful");
    }

    // Tests the Special Education Default with display option c* is selected
    public void testSpecEdC1Basic() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "spec_ed_level", "basic" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals("testSpecEdC1Basic: default content expected", "basic", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdC1Basic: wrong option", "c-basic", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdC1Basic successful");
    }

    public void testSpecEdC1Moderate() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exist", "moderate" ) );
        l.add( createEspResponse( "spec_ed_level", "moderate" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals("testSpecEdC1Moderate: default content expected", "moderate", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdC1Moderate: wrong option", "c-moderate", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdC1Moderate successful");
    }

    public void testSpecEdC1Intensive() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "spec_ed_level", "intensive" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals("testSpecEdC1Intensive: default content expected", "intensive", resultsModel.get("SpecEdPgmsProvided"));
        assertEquals( "testSpecEdC1Intensive: wrong option", "c-intensive", resultsModel.get( "SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdC1Intensive successful");
    }

    // Tests the Special Education Default with display option d is selected
    public void testSpecEdD1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "academic_focus", "special_ed" ) );
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testSpecEdD1: default content expected", "focuses", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdD2: wrong option", "d", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdD1 successful");
    }

    // Tests the Special Education Default with display option d is selected
    public void testSpecEdD2() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "academic_focus", "special_education" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testSpecEdD2: default content expected", "focuses", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdD2: wrong option", "d", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdD2 successful");
    }

    // Tests the Special Education Default with display option e is selected
    public void testSpecEdE1() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );
        l.add( createEspResponse( "staff_resources", "special_ed_coordinator" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testSpecEdE1: default content expected", "coordinator", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdE1: wrong option", "e", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdE1 successful");
    }

    public void testSpecEdE2() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );
        l.add( createEspResponse( "staff_resources", "special_ed_coordinator" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testSpecEdE2: default content expected", "coordinator", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdE2: wrong option", "e", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdE2 successful");
    }

    // Tests the Special Education Default with display option d is selected
    public void testSpecEdF() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testSpecEdD2: default content expected", "no", resultsModel.get( "SpecEdPgmsProvided") );
        assertEquals( "testSpecEdD2: wrong option", "f", resultsModel.get("SpecEdPgmsOptSelected") );
        System.out.println("testSpecEdD2 successful");
    }

    // Tests the Extended care Default with no Before After
    public void testExtdCareA() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "special_ed_programs_exists", "no" ) );  // This should trigger default display
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareB: default content expected", "noInfo", resultsModel.get("ExtdCareProvided") );
        System.out.println("testExtdCareA successful");
    }

    // Tests the Extended care Default with Before only
    public void testExtdCareB() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareB: default content expected", new Boolean(true), resultsModel.get("ExtdCareBefore") );
        assertEquals( "testExtdCareB: default content expected", new Boolean(false), resultsModel.get("ExtdCareAfter") );
        System.out.println("testExtdCareB successful");
    }

    // Tests the Extended care Default with Before with time
    public void testExtdCareC() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareC: default content expected", new Boolean(true), resultsModel.get("ExtdCareBefore") );
        assertEquals( "testExtdCareC: default content expected", new Boolean(false), resultsModel.get("ExtdCareAfter") );
        assertEquals( "testExtdCareC: default content expected", "7:00 AM", resultsModel.get( "ExtdCareBeforeTime") );
        System.out.println( "testExtdCareC successful" );
    }

    // Tests the Extended care Default with After only
    public void testExtdCareD() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "after" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareD: default content expected", new Boolean(false), resultsModel.get("ExtdCareBefore") );
        assertEquals( "testExtdCareD: default content expected", new Boolean(true), resultsModel.get("ExtdCareAfter") );
        System.out.println( "testExtdCareD successful" );
    }

    // Tests the Extended care Default with After with time
    public void testExtdCareE() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareE: default content expected", new Boolean(true), resultsModel.get("ExtdCareAfter") );
        assertEquals( "testExtdCareE: default content expected", "4:00 PM", resultsModel.get( "ExtdCareAfterTime") );
        System.out.println( "testExtdCareE successful" );
    }

    // Tests the Extended care Default with Before and After with no times
    public void testExtdCareF() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareF: default content expected", new Boolean(true), resultsModel.get("ExtdCareBefore") );
        assertEquals( "testExtdCareF: default content expected", new Boolean(true), resultsModel.get("ExtdCareAfter") );
        System.out.println( "testExtdCareF successful" );
    }

    // Tests the Extended care Default with Before and After with times
    public void testExtdCareG() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareF: default content expected", new Boolean(true), resultsModel.get("ExtdCareBefore") );
        assertEquals( "testExtdCareF: default content expected", new Boolean(true), resultsModel.get("ExtdCareAfter") );
        assertEquals( "testExtdCareC: default content expected", "7:00 AM", resultsModel.get( "ExtdCareBeforeTime") );
        assertEquals( "testExtdCareE: default content expected", "4:00 PM", resultsModel.get( "ExtdCareAfterTime") );
        System.out.println( "testExtdCareF successful" );
    }

    // Tests the Extended care Title
    public void testExtdCareTitleA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "before_after_care", "before" ) );
        l.add( createEspResponse( "before_after_care", "after" ) );
        l.add( createEspResponse( "before_after_care_start", "7:00 AM" ) );
        l.add( createEspResponse( "before_after_care_end", "4:00 PM" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        _school.setLevelCode( LevelCode.MIDDLE);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        _school.setLevelCode( LevelCode.HIGH);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        _school.setLevelCode(LevelCode.MIDDLE_HIGH);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

        assertEquals( "testExtdCareTitleC: default content expected", "Extended programs", resultsModel.get( "ExtdCareTitle") );
        System.out.println( "testExtdCareTitleC successful" );
    }

    // Tests the Special Education substitute (Teachers/staff) - no administrator
    public void testSpecEdSubstitute1A() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "staff_resources", "art_teacher" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSpecialEdEspTile( _request, _school, espData );

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
        l.add( createEspResponse( "staff_resources", "none" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        String adminName = "Ben Jones";
        Map resultsModel = runSpecialEdEspTileWithCensusMockController( _request, _school, espData, adminName );
        String sentence = (String) resultsModel.get("teachersStaff");
        assertTrue( "testSpecEdSubstitute1C: Substitute content expected content not found", sentence.indexOf(adminName)==0 );
        assertTrue( "testSpecEdSubstitute1C: Substitute content expected content not found", sentence.indexOf("Staff includes")==-1 );
        System.out.println("testSpecEdSubstitute1C successful");
    }

    // Tests the Special Education substitute (Teachers/staff) - no administrator
    public void testSpecEdSubstitute1D() {

        // *** Test Default content not selected
        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "staff_resources", "art_teacher" ) );
        l.add( createEspResponse( "staff_resources", "ell_esl_coord" ) );
        l.add( createEspResponse( "staff_resources", "gifted_specialist" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        String adminName = "Ben Jones";
        Map resultsModel = runSpecialEdEspTileWithCensusMockController( _request, _school, espData, adminName );

        String sentence = (String) resultsModel.get("teachersStaff");
        assertTrue( "testSpecEdSubstitute1C: Substitute content expected content not found", sentence.indexOf("Ben Jones")==0 );
        assertTrue( "testSpecEdSubstitute1D: Substitute content expected content not found", sentence.indexOf("Art teacher")>0 );
        assertTrue( "testSpecEdSubstitute1D: Substitute content expected content not found", sentence.indexOf("Gifted specialist")>0 );
        System.out.println("testSpecEdSubstitute1D successful");
    }

    private Map<String, Object> runSpecialEdEspTileWithCensusMockController( HttpServletRequest request, School school, Map<String, List<EspResponse>> espData, String administrator ) {

        Map<CensusDataType, List<CensusDataSet>> censusValues = new HashMap<CensusDataType, List<CensusDataSet>>(2);

        // setup avg class size census data
        if( administrator != null ) {
            List<CensusDataSet> adminCDSs = new ArrayList<CensusDataSet>();
            censusValues.put( CensusDataType.HEAD_OFFICIAL_NAME, adminCDSs );
            CensusDataSet adminCDS = new CensusDataSet( CensusDataType.HEAD_OFFICIAL_NAME, 2011 );
            adminCDSs.add(adminCDS);
            Set<SchoolCensusValue> adminSet = new HashSet<SchoolCensusValue>(1);
            adminCDS.setSchoolData(adminSet);
            SchoolCensusValue enrollmentCSV = new SchoolCensusValue();
            adminSet.add(enrollmentCSV);
            enrollmentCSV.setSchool(_school);
            enrollmentCSV.setValueText(administrator);
        }

        expect( _schoolProfileDataHelper.getSchoolCensusValues(getRequest()) ).andReturn( censusValues );

        replay(_schoolProfileDataHelper);

        Map<String, Object> model = _schoolProfileOverviewController.getSpecialEdEspTile(request, _school, espData);

        verify(_schoolProfileDataHelper);

        return model;
    }

    // ========= Tests for Transportation default content ========
    // Test option a
    public void testTransportationDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "yes" ) );
        l.add( createEspResponse( "transportation_shuttle_other", "a, b, c" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultA: icon wrong", "metro", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultA: shuttleStops wrong", "A, b, c", resultsModel.get( "shuttleStops") );
        System.out.println( "testTransportationDefaultA successful" );
    }

    // Test option b
    public void testTransportationDefaultB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "yes" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultB: icon wrong", "metro", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultB: shuttleStops wrong", "Shuttles are provide to local Metro stops", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultB successful" );
    }

    // Test option c
    public void testTransportationDefaultC() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation_shuttle", "no" ) );
        l.add( createEspResponse( "transportation_shuttle_other", "a, b, c" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultC: icon wrong", "walking", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultC: shuttleStops wrong", "No transportation provided", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultC successful" );
    }

    // Test option d
    public void testTransportationDefaultD() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "passes" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultD: icon wrong", "passes", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultD: message wrong", "Passes/tokens provided for public transportation", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultD successful" );
    }

    // Test option e
    public void testTransportationDefaultE() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "special_ed_only" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultE: icon wrong", "handicapped", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultE: message wrong", "Transportation provided for special education students", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultE successful" );
    }

    // Test option f
    public void testTransportationDefaultF() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "busses" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultF: icon wrong", "bus", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultF: message wrong", "Busses/vans provide for students only", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultF successful" );
    }

    // Test option g
    public void testTransportationDefaultG() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "shared_bus" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultG: icon wrong", "bus", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultG: message wrong", "Busses/vans shared with other schools", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultG successful" );
    }

    // Test option h
    public void testTransportationDefaultH() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "none" ) );
        l.add( createEspResponse( "transportation_other", "skateboard" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultH: icon wrong", "walking", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultH: message wrong", "Other transportation provided", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultH successful" );
    }

    // Test option i
    public void testTransportationDefaultI() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "transportation", "none" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getTransportationEspTile( _request, _school, espData );

        assertEquals( "testTransportationDefaultI: icon wrong", "walking", resultsModel.get( "icon") );
        assertEquals( "testTransportationDefaultI: message wrong", "No transportation provided", resultsModel.get( "transMsg") );
        System.out.println( "testTransportationDefaultI successful" );
    }



    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1A() {

        // Test special case for TX and NY where only students per teacher is useful
        String stateAbbrev = "TX";
        float classSizeValue = 10.0f;
        int classSizeYear = 2012;
        float studentsPerTeacherValue = 24.0f;
        int studentsPerTeacherYear = 2011;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1StudentsPerTeacher");

        assertEquals( "testTransportSubstitute1A: students per teacher wrong", (int)studentsPerTeacherValue, resultInteger.intValue() );
        System.out.println( "testTransportSubstitute1A successful" );
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1B() {

        // Test special case for TX and NY where only students per teacher is useful
        String stateAbbrev = "TX";
        float classSizeValue = 10.0f;
        int classSizeYear = 2012;
        float studentsPerTeacherValue = 0;
        int studentsPerTeacherYear = 0;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1StudentsPerTeacher");

        assertEquals( "testTransportSubstitute1B: substitute2 content expected", "substitute2", resultsModel.get("content") );
        System.out.println("testTransportSubstitute1B successful");
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1C() {

        // Test using latest year
        String stateAbbrev = "CA";
        float classSizeValue = 10.0f;
        int classSizeYear = 2012;
        float studentsPerTeacherValue = 24.0f;
        int studentsPerTeacherYear = 2011;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1ClassSize");

        assertEquals( "testTransportSubstitute1C: class size expected", (int)classSizeValue, resultInteger.intValue() );
        System.out.println( "testTransportSubstitute1C successful" );
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1D() {

        // Test same year for both
        String stateAbbrev = "CA";
        float classSizeValue = 10.0f;
        int classSizeYear = 2011;
        float studentsPerTeacherValue = 24.0f;
        int studentsPerTeacherYear = 2011;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1ClassSize");

        assertEquals( "testTransportSubstitute1D: class size expected", (int)classSizeValue, resultInteger.intValue() );
        System.out.println( "testTransportSubstitute1D successful" );
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1E() {

        // Test students per teacher more recent
        String stateAbbrev = "CA";
        float classSizeValue = 10.0f;
        int classSizeYear = 2011;
        float studentsPerTeacherValue = 24.0f;
        int studentsPerTeacherYear = 2012;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1StudentsPerTeacher");

        assertEquals( "testTransportSubstitute1E: students per teacher expected", (int)studentsPerTeacherValue, resultInteger.intValue() );
        System.out.println( "testTransportSubstitute1E successful" );
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1F() {

        // Test no students per teacher
        String stateAbbrev = "CA";
        float classSizeValue = 10.0f;
        int classSizeYear = 2012;
        float studentsPerTeacherValue = 0f;
        int studentsPerTeacherYear = 0;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1ClassSize");

        assertEquals( "testTransportSubstitute1F: class size expected", (int)classSizeValue, resultInteger.intValue() );
        System.out.println( "testTransportSubstitute1F successful" );
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1G() {

        // Test no class size data
        String stateAbbrev = "CA";
        float classSizeValue = 0f;
        int classSizeYear = 0;
        float studentsPerTeacherValue = 24.0f;
        int studentsPerTeacherYear = 2011;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1StudentsPerTeacher");

        assertEquals( "testTransportSubstitute1G: students per teacher expected", (int)studentsPerTeacherValue, resultInteger.intValue() );
        System.out.println( "testTransportSubstitute1G successful" );
    }

    // Tests for Transportation substitute1 content which is students/teacher and avg class size
    public void testTransportSubstitute1H() {

        // Test no census data
        String stateAbbrev = "CA";
        float classSizeValue = 0f;
        int classSizeYear = 0;
        float studentsPerTeacherValue = 0f;
        int studentsPerTeacherYear = 0;

        Map<String, Object> resultsModel = runTransportationForCensusController(stateAbbrev, classSizeValue, classSizeYear, studentsPerTeacherValue, studentsPerTeacherYear);
        Integer resultInteger = (Integer) resultsModel.get("substitute1StudentsPerTeacher");

        assertEquals( "testTransportSubstitute1H: default content expected", "substitute2", resultsModel.get("content") );
        System.out.println( "testTransportSubstitute1H successful" );
    }

    private Map<String, Object> runTransportationForCensusController(String stateAbbrev, float classSizeValue, int classSizeYear, float studentsPerTeacherValue, int studentsPerTeacherYear) {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "something", "none" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        StateManager sm = new StateManager();
        State state = sm.getState( stateAbbrev );
        _school.setStateAbbreviation(state);

        Map<CensusDataType, List<CensusDataSet>> censusValues = new HashMap<CensusDataType, List<CensusDataSet>>(2);

        // setup avg class size census data
        if( classSizeYear > 0 ) {
            List<CensusDataSet> avgClassSize = new ArrayList<CensusDataSet>();
            censusValues.put( CensusDataType.CLASS_SIZE, avgClassSize );
            CensusDataSet classSizeCDS = new CensusDataSet( CensusDataType.CLASS_SIZE, classSizeYear );
            avgClassSize.add(classSizeCDS);
            Set<SchoolCensusValue> classSizeSet = new HashSet<SchoolCensusValue>(1);
            classSizeCDS.setSchoolData(classSizeSet);
            SchoolCensusValue classSizeCSV = new SchoolCensusValue();
            classSizeSet.add(classSizeCSV);
            classSizeCSV.setSchool(_school);
            classSizeCSV.setValueFloat(new Float(classSizeValue));
        }


        // setup student per teacher census data
        if( studentsPerTeacherYear > 0 ) {
            List<CensusDataSet> studentsPerTeacher = new ArrayList<CensusDataSet>();
            censusValues.put( CensusDataType.STUDENT_TEACHER_RATIO, studentsPerTeacher );
            CensusDataSet studentsPerTeacherCDS = new CensusDataSet( CensusDataType.STUDENT_TEACHER_RATIO, studentsPerTeacherYear );
            studentsPerTeacher.add(studentsPerTeacherCDS);
            Set<SchoolCensusValue> studentsPerTeacherSet = new HashSet<SchoolCensusValue>(1);
            studentsPerTeacherCDS.setSchoolData(studentsPerTeacherSet);
            SchoolCensusValue studentsPerTeacherCSV = new SchoolCensusValue();
            studentsPerTeacherSet.add(studentsPerTeacherCSV);
            studentsPerTeacherCSV.setSchool(_school);
            studentsPerTeacherCSV.setValueFloat(new Float(studentsPerTeacherValue));
        }

        expect( _schoolProfileDataHelper.getSchoolCensusValues(getRequest()) ).andReturn( censusValues );

        replay(_schoolProfileDataHelper);

        Map<String, Object> model = _schoolProfileOverviewController.getTransportationEspTile(getRequest(), _school, espData );

        verify(_schoolProfileDataHelper);

        return model;
    }

    // ============= Tests for Programs Tile ===============
    // immersion no, instructional model none, academic focus none
    public void testProgramsDefaultA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "no" ) );
        l.add( createEspResponse( "instructional_model", "none" ) );
        l.add( createEspResponse( "academic_focus", "none" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertEquals("testProgramsDefaultC: immersion wrong", "Chinese immersion", resultsList.get(0));
        assertEquals("testProgramsDefaultC: instructional_model wrong", "Honors", resultsList.get(1));
        assertEquals("testProgramsDefaultC: instructional_model wrong", "Independent study", resultsList.get(2));
        assertEquals("testProgramsDefaultC: academic_focus wrong", "School focus: Science, Technology", resultsList.get(3));
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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

        List<String> resultsList = (List<String>) resultsModel.get( "resultsList" );
        assertEquals("testProgramsDefaultD: immersion wrong", "Language immersion", resultsList.get(0));
        assertEquals("testProgramsDefaultD: instructional_model wrong", "Honors", resultsList.get(1));
        assertEquals("testProgramsDefaultD: instructional_model wrong", "Independent study", resultsList.get(2));
        assertEquals("testProgramsDefaultD: instructional_model wrong", "Basket weaving", resultsList.get(3));
        assertEquals("testProgramsDefaultD: academic_focus wrong", "School focus: Science, Technology, Something else", resultsList.get(4));
        System.out.println( "testProgramsDefaultD successful" );
    }

    // immersion yes with language_other, instructional model two values + other, academic focus two values and other
    public void testProgramsDefaultE() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "immersion", "yes" ) );
        l.add( createEspResponse( "immersion_language_other", "vietnamese" ) );
        l.add( createEspResponse( "instructional_model_other", "basket weaving" ) );
        l.add( createEspResponse( "academic_focus_other", "something else" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

        assertEquals("testProgramsSubstitute1A: wrong", "true", resultsModel.get("substitute1None"));
        System.out.println( "testProgramsSubstitute1A successful" );
    }

    // Substitute 1 with parent_involvement of 2 values
    public void testProgramsSubstitute1B() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "parent_involvement", "parent_nights_req" ) );
        l.add( createEspResponse( "parent_involvement", "chaperone_req" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

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
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        SchoolSubtype subType = SchoolSubtype.create("high, magnet, all_male");
        _school.setSubtype(subType);
        _school.setAssociation( "NCEA" );
        _school.setAffiliation( "Roman Catholic" );

        Map resultsModel = _schoolProfileOverviewController.getProgramsEspTile( _request, _school, espData );

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

        l.add( createEspResponse( "students_vouchers", "blank" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getApplInfoEspTile(_request, _school, espData);

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoDefaultA: content wrong", "applInfoV1", contentType );
        String rate = (String) resultsModel.get( "acceptanceRate" );
        System.out.println( "testApplInfoDefaultA acceptance Rate = " + rate );
        assertNotNull( "testApplInfoDefaultA: acceptanceRate should not be null", rate );
        assertEquals( "testApplInfoDefaultA: acceptanceRate wrong", "2", rate );

        assertEquals( "testApplInfoDefaultA: applicationDeadlineMsg wrong", "apply", (String) resultsModel.get( "applicationDeadlineMsg" ) );
        assertEquals( "testApplInfoDefaultA: applicationDeadlineDate wrong", "July 30, 2012", (String) resultsModel.get( "applicationDeadlineDate" ) );

        assertEquals( "testApplInfoDefaultA: vouchers wrong", "blank", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoDefaultA successful" );
    }

    // application deadline = date but the date is missing and vouchers
    public void testApplInfoDefaultB() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "yes" ) );
        l.add( createEspResponse( "students_accepted", "5" ) );
        l.add( createEspResponse( "applications_received", "592" ) );
        l.add( createEspResponse( "applications_received_year", "2011-2012" ) );
        l.add( createEspResponse( "students_accepted_year", "2011-2012" ) );

        l.add( createEspResponse( "application_deadline", "date" ) );

        l.add( createEspResponse( "students_vouchers", "yes" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getApplInfoEspTile( _request, _school, espData );

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoDefaultB: content wrong", "applInfoV1", contentType );
        String rate = (String) resultsModel.get( "acceptanceRate" );
        System.out.println( "testApplInfoDefaultB acceptance Rate Percent = " + rate );
        assertNotNull("testApplInfoDefaultB: acceptanceRate should not be null", rate);
        assertEquals("testApplInfoDefaultB: acceptanceRate wrong", "1", rate);

        assertEquals( "testApplInfoDefaultB: applicationDeadlineMsg wrong", "call", (String) resultsModel.get( "applicationDeadlineMsg" ) );

        assertEquals( "testApplInfoDefaultB: vouchers wrong", "yes", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoDefaultB successful" );
    }

    // application deadline = date but the date is missing and vouchers
    public void testApplicationRateRounding() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "yes" ) );
        l.add( createEspResponse( "students_accepted", "16" ) );
        l.add( createEspResponse( "applications_received", "100" ) );
        l.add( createEspResponse( "applications_received_year", "2011-2012" ) );
        l.add( createEspResponse( "students_accepted_year", "2011-2012" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getApplInfoEspTile( _request, _school, espData );

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "content wrong", "applInfoV1", contentType );
        String rate = (String) resultsModel.get( "acceptanceRate" );
        assertNotNull("acceptanceRate should not be null", rate);
        assertEquals("acceptanceRate wrong, expect it to be rounded", "2", rate);
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

        l.add( createEspResponse( "students_vouchers", "no" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getApplInfoEspTile( _request, _school, espData );

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoDefaultC: content wrong", "applInfoV1", contentType );
        String rate = (String) resultsModel.get( "acceptanceRate" );
        System.out.println( "testApplInfoDefaultC acceptance Rate Percent = " + rate );
        assertNotNull("testApplInfoDefaultC: acceptanceRatePercent should not be null", rate);

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

        l.add( createEspResponse( "students_vouchers", "no" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getApplInfoEspTile( _request, _school, espData );

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testApplInfoSubstitute1A: content wrong", "applInfoV2", contentType );

        assertEquals( "testApplInfoSubstitute1A: applicationDeadlineMsg wrong", "apply", (String) resultsModel.get( "applicationDeadlineMsg" ) );
        assertEquals( "testApplInfoSubstitute1A: applicationDeadlineDate wrong", "July 30, 2012", (String) resultsModel.get( "applicationDeadlineDate" ) );

        assertEquals( "testApplInfoSubstitute1A: vouchers wrong", "no", (String) resultsModel.get( "vouchers" ) );

        System.out.println( "testApplInfoSubstitute1A successful" );
    }

    // application process yes but no data to calc acceptance rate, deadline = date with date supplied and no vouchers
    public void testSchoolVisitChecklistA() {

        List<EspResponse> l = new ArrayList<EspResponse>();
        l.add( createEspResponse( "application_process", "no" ) );

        l.add( createEspResponse( "application_deadline", "date" ) );
        l.add( createEspResponse( "application_deadline_date", "July 30, 2012" ) );

        l.add( createEspResponse( "students_vouchers", "no" ) );
        Map<String, List<EspResponse>> espData = convertToEspData(l);

        Map resultsModel = _schoolProfileOverviewController.getSchoolVisitChecklistTile(_request, _school);

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testSchoolVisitChecklistA: content wrong", "visitChecklist", contentType );

        System.out.println("testSchoolVisitChecklistA successful");
    }

    // Local info substitute 1 - District info
    public void testLocalInfoSubstitute1A() {

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

        Map resultsModel = _schoolProfileOverviewController.getLocalInfoEspTile(_request, _school);

        verify(districtDao);

        String contentType = (String) resultsModel.get( "content" );
        assertEquals( "testLocalInfoSubstitute1A: content wrong", "districtInfo", contentType );
        assertEquals( "testLocalInfoSubstitute1A: district  num schools wrong", 25, resultsModel.get( "districtNumSchools" ));

        System.out.println("testLocalInfoSubstitute1A successful");
    }

    public void testGetLastModifiedDateForSchool() {
        // Only school
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        _school.setModified(cal.getTime());
        expect(_schoolProfileDataHelper.getNonPrincipalReviews(_request, 1)).andReturn(new ArrayList<Review>());
        replay(_schoolProfileDataHelper);
        Date rval = _schoolProfileOverviewController.getLastModifiedDateForSchool(_request, _school);
        verify(_schoolProfileDataHelper);
        assertNotNull("Expect school's last modified to be returned", rval);
        assertSame("Expect school's last modified to be returned", _school.getModified(), rval);

        // Review more recent
        reset(_schoolProfileDataHelper);
        Review review = new Review();
        review.setPosted(new Date());
        List<Review> reviews = new ArrayList<Review>(1);
        reviews.add(review);
        expect(_schoolProfileDataHelper.getNonPrincipalReviews(_request, 1)).andReturn(reviews);
        replay(_schoolProfileDataHelper);
        rval = _schoolProfileOverviewController.getLastModifiedDateForSchool(_request, _school);
        verify(_schoolProfileDataHelper);
        assertNotNull("Expect review's last modified to be returned", rval);
        assertSame("Expect review's last modified to be returned", review.getPosted(), rval);

        // School more recent
        reset(_schoolProfileDataHelper);
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_YEAR, -14);
        review.setPosted(cal2.getTime());
        expect(_schoolProfileDataHelper.getNonPrincipalReviews(_request, 1)).andReturn(reviews);
        replay(_schoolProfileDataHelper);
        rval = _schoolProfileOverviewController.getLastModifiedDateForSchool(_request, _school);
        verify(_schoolProfileDataHelper);
        assertNotNull("Expect school's last modified to be returned", rval);
        assertSame("Expect school's last modified to be returned", _school.getModified(), rval);
    }

    /*
       ************ End of Tests - support functions follow ************
    */

    /**
     * Runs the entire SchoolProfileOverviewController.  This has now worked out well because a varying set of
     * mocked objects are needed.
     * @param espData
     * @return  The model from the controller
     *
     * @deprecated
     */
    private Map runController( Map<String, List<EspResponse>> espData) {
        ModelMap map = new ModelMap();

        expect( _schoolProfileDataHelper.getEspDataForSchool(getRequest()) ).andReturn( espData );
        expect( _schoolProfileDataHelper.getSchoolMedia(getRequest())).andReturn(null);
        expect( _schoolProfileDataHelper.getCommunityRatings(getRequest())).andReturn(null);
        expect( _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews(getRequest()) ).andReturn( new Long(0l));
        expect(_schoolProfileDataHelper.getNonPrincipalReviews(getRequest(), 5)).andReturn(null);
        expect( _schoolProfileDataHelper.getSchoolCensusValues(getRequest()) ).andReturn(null).times(1, 2);
        expect( _schoolProfileDataHelper.getSperlingsInfo(getRequest())).andReturn(null).anyTimes();

        return map;
    }

    private Map<String, Object> runCmsVideoTourModel(String cmsVideoContentId) {

//        ICmsFeatureSearchResult iResult = createStrictMock(ICmsFeatureSearchResult.class);
//        expect(iResult.getFullUri()).andReturn("/fullUri");
//        ContentKey ck = new ContentKey();
//        ck.setType("Video");
//        ck.setIdentifier(Long.parseLong(cmsVideoContentId));
//        expect(iResult.getContentKey()).andReturn(ck);
//        expect(iResult.getImageUrl()).andReturn("imageUrl");
//        expect(iResult.getImageAltText()).andReturn("alt text");
//
//        List<ICmsFeatureSearchResult> resultList = new ArrayList<ICmsFeatureSearchResult>();
//        resultList.add(iResult);
//        SearchResultsPage<ICmsFeatureSearchResult> searchResults = new SearchResultsPage<ICmsFeatureSearchResult>(1,resultList);
//
//        try {
//            expect( _cmsFeatureSearchService.search(isA(SolrQuery.class))).andReturn(searchResults);
//        } catch (SearchException e) {
//            fail("Exception creating mock for CmsFeatureSearchService: " + e.toString() );
//        }

        replay(_cmsFeatureSearchService);
//        replay(iResult);

        Map<String, Object> resultsModel = _schoolProfileOverviewController.getTourVideoModel(_request, _school);

//        verify(iResult);
        verify(_cmsFeatureSearchService);
        return resultsModel;
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