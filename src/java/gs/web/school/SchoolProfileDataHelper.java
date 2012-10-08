package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.test.*;
import gs.web.request.RequestAttributeHelper;
import gs.web.school.review.ParentReviewHelper;
import gs.web.search.CmsRelatedFeatureSearchService;
import gs.web.search.CmsRelatedFeatureSearchServiceSolrImpl;
import gs.web.search.ICmsFeatureSearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Helper that gets data for all school profile pages.  Retrieves data from the following sources:
 * 1. ESP - All esp data for the specified school
 *
 * User: rraker
 * Date: 6/27/12
 */

@Component("schoolProfileDataHelper")
public class SchoolProfileDataHelper extends AbstractDataHelper {

    protected static final Log _log = LogFactory.getLog(SchoolProfileDataHelper.class.getName());

    private final static String ESP_DATA_REQUEST_ATTRIBUTE = "espData";
    private final static String SCHOOL_MEDIA_REQUEST_ATTRIBUTE = "schoolMedia";
    private final static String COMMUNITY_RATINGS_ATTRIBUTE = "communityRatings";
    private final static String GS_RATINGS_ATTRIBUTE = "gsRatings";
    private final static String PUBLISHED_REVIEW_COUNT = "publishedReviewCount";
    private final static String REVIEWS = "reviews";
    private final static String REVIEWS_COUNT = "reviewsCount";
    private final static String REVIEWS_PAGE_PARAM = "page";
    private final static Integer REVIEWS_PER_PAGE = 20;
    private final static String SCHOOL_MEDIA_REPORTS_BY_USER = "reportsByUser";
    private final static String NEARBY_SCHOOLS = "nearbySchools";
    private final static String ENROLLMENT = "enrollment";
    private final static String SPERLINGS = "sperlings";
    private final static String RELATED_CONTENT = "relatedContent";
    private final static String SCHOOL_VIDEOS = "schoolVideos";

    private final static String CENSUS_DATA = "censusData";

    @Autowired
    private IEspResponseDao _espResponseDao;

    @Autowired
    private ISchoolMediaDao _schoolMediaDao;

    @Autowired
    private IReviewDao _reviewDao;

    @Autowired
    private IReportedEntityDao _reportedEntityDao;

    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;

    @Autowired
    private ICensusDataSchoolValueDao _censusDataSchoolValueDao;

    @Autowired
    private ICensusDataSetDao _censusDataSetDao;

    @Autowired
    ICensusDataConfigEntryDao _censusDataConfigDao;

    @Autowired
    ICensusDataConfigEntryDao _censusStateConfigDao;

    @Autowired
    SchoolProfileCensusHelper _schoolProfileCensusHelper;

    @Autowired
    ISchoolDao _schoolDao;

    @Autowired
    IGeoDao _geoDao;

    @Autowired
    CmsRelatedFeatureSearchService _cmsRelatedFeatureSearchService;

    @Autowired
    private ITestDataSetDao _testDataSetDao;

    @Autowired
    private ITestDataSchoolValueDao _testDataSchoolValueDao;

    @Autowired
    private ITestDataStateValueDao _testDataStateValueDao;

    @Autowired
    private ParentReviewHelper _parentReviewHelper;

    protected Map<String, List<EspResponse>> getEspDataForSchool( HttpServletRequest request ) {

        String key = ESP_DATA_REQUEST_ATTRIBUTE;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Map<String, List<EspResponse>> espData = (Map<String, List<EspResponse>>) getSharedData( request, key );
        // If it isn't in the request try to retrieve it
        if( espData == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            List<EspResponse> results = _espResponseDao.getResponses( school );

            if( results != null && !results.isEmpty() ) {

                // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
                // and the value will be the corresponding list of EspResponse objects
                espData = EspResponse.rollup(results);

                setSharedData( request, key, espData ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }

        return espData;
    }

    private void debugMsgIsNull(String msg, Object obj) {

        StackTraceElement[] st =  Thread.currentThread().getStackTrace();
        _log.error(msg + ((obj==null)?"null":"not null") + ", called from:\n  " + st[2].toString() + "\n  " + st[3].toString() + "\n");

    }

    protected Integer getEnrollment( HttpServletRequest request ) {

        String key = ENROLLMENT;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Integer enrollment = (Integer) getSharedData( request, key );

        // If it isn't in the request try to retrieve it
        if( enrollment == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            enrollment = school.getEnrollmentOrCapacity();

            if( enrollment != null ) {

                setSharedData( request, key, enrollment ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }

        return enrollment;
    }

    protected List<SchoolMedia> getSchoolMedia(HttpServletRequest request) {

        String key = SCHOOL_MEDIA_REQUEST_ATTRIBUTE;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        List<SchoolMedia> schoolMedia = (List<SchoolMedia>) getSharedData( request, key );

        // If it isn't in the request try to retrieve it
        if( schoolMedia == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            schoolMedia =_schoolMediaDao.getAllActiveBySchool(school);

            if( schoolMedia != null && !schoolMedia.isEmpty() ) {
               setSharedData( request, key, schoolMedia ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }
        return schoolMedia;
    }

    /**
     * Get a list of the 25 nearest schools
     * @param request
     * @return
     */
    protected List<NearbySchool> getNearbySchools(HttpServletRequest request) {

        String key = NEARBY_SCHOOLS;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        List<NearbySchool> schools = (List<NearbySchool>) getSharedData( request, key );

        // If it isn't in the request try to retrieve it
        if( schools == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            schools =_schoolDao.findNearbySchoolsNoRating(school, 12);
            List<NearbySchool> filtered = new ArrayList();
            for (NearbySchool nearby : schools) {
                if (LevelCode.PRESCHOOL.equals(nearby.getNeighbor().getLevelCode())
                        || nearby.getNeighbor().getDatabaseState() != school.getDatabaseState()) {
                    continue;
                }
                filtered.add(nearby);
            }
            schools = filtered;

            if( !filtered.isEmpty() ) {
                setSharedData( request, key, filtered); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }

        return schools;
    }

    /**
     * Get "reports" information for a list of media.  A report is an indication that an authorized individual has flagged
     * an image in some what - NEED BETTER EXPLANATION FROM ANTHONY
     * @param request
     * @param user - the user to get this information for
     * @param schoolMediaList - a list of media to check
     * @return - a Map with a key of mediaId and a boolean of true if this has been flagged
     */
    protected Map<Integer, Boolean> getReportsForSchoolMedia(HttpServletRequest request, User user, List<SchoolMedia> schoolMediaList) {
        if (schoolMediaList == null || user == null) {
            return null;
        }

        String key = SCHOOL_MEDIA_REPORTS_BY_USER;

        // First see if it is already in the request
        // The Map created in the next statement contains:
        // key = userId
        // Value = Map<mediaId, booleanValue where boolean value contains:
        //    null if no value has been determined
        //    True if there is a report
        //    False if there is no report
        Map<Integer, Map<Integer, Boolean>> schoolMediaReportsByUser = (Map<Integer, Map<Integer, Boolean>>) getSharedData( request, key );

        // Case 1 - no data at all - create the data and save it
        if( schoolMediaReportsByUser == null ) {
            // Create structure for schoolMediaReportsByUser and provide an empty structure for this user which will be filled in in the next step
            Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(schoolMediaList.size());
            schoolMediaReportsByUser = new HashMap<Integer, Map<Integer, Boolean>>(1);
            schoolMediaReportsByUser.put( user.getId(), reports );
            setSharedData( request, key, schoolMediaReportsByUser );
        }

        // next process the schoolMediaList from the call and process it against the data stored in the request
        Map<Integer, Boolean> reportsStoredInRequest = schoolMediaReportsByUser.get( user.getId() );
        Map<Integer, Boolean> results = new HashMap<Integer, Boolean>();
        for (SchoolMedia schoolMedia: schoolMediaList) {
            // First see if there is already data available and use it
            Boolean status = reportsStoredInRequest.get( schoolMedia.getId() );
            if( status == null ) {
                // Not yet stored - need to get it and stored in request and add to results
                Boolean hasReport = _reportedEntityDao.hasUserReportedEntity
                        (user, ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId());
                reportsStoredInRequest.put( schoolMedia.getId(), hasReport );
                results.put( schoolMedia.getId(), hasReport );
            }
            else {
                // Already available
                results.put(schoolMedia.getId(), status );
            }
        }

        return results;
    }


    protected Ratings getCommunityRatings (HttpServletRequest request) {

        String key = COMMUNITY_RATINGS_ATTRIBUTE;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Ratings ratings =  (Ratings)getSharedData( request, key );

        // If it isn't in the request try to retrieve it
        if( ratings == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            ratings = _reviewDao.findRatingsBySchool(school);

            if( ratings != null ) {
                setSharedData( request, key, ratings ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }
        return ratings;
    }

    protected Long getCountPublishedNonPrincipalReviews (HttpServletRequest request) {

        String key = PUBLISHED_REVIEW_COUNT;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Long numberOfReviews =  (Long)request.getAttribute(key);

        // If it isn't in the request try to retrieve it
        if( numberOfReviews == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);

            if( numberOfReviews != null ) {
                request.setAttribute(key, numberOfReviews); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }
        return numberOfReviews;
    }

    protected List<Review> getNonPrincipalReviews(HttpServletRequest request) {
        String key = REVIEWS;
        String countKey = REVIEWS_COUNT;

        School school = _requestAttributeHelper.getSchool( request );
        if ( school == null ) {
            throw new IllegalArgumentException("The request must already contain a school object");
        }

        List<Review> reviews = (List<Review>) getSharedData( request, key );
        if ( reviews == null ) {
            reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school);
            setSharedData(request, key, reviews);
            setSharedData(request, countKey, reviews.size());
        }
        return reviews;
    }

    protected List<Review> getNonPrincipalReviews (HttpServletRequest request, int countRequested ) {

        // always return the full principal
        List<Review> reviews = getNonPrincipalReviews(request);

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // If the number stored request the number requested just return them
        if( reviews.size() == countRequested ) {
            // we are done, just return them
            return reviews;
        }
        else if( reviews.size() > countRequested ) {
            List<Review> subset = reviews.subList(0, countRequested);
            return subset;
        }

        return  reviews;    // Return what we have
    }

    protected List<String> getSchoolsVideos(HttpServletRequest request) {

        String key = SCHOOL_VIDEOS;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool(request);
        if (school == null) {
            throw new IllegalArgumentException("The request must already contain a school object");
        }

        // Get Data
        // First see if it is already in the request
        List<String> schoolVideos = (List<String>) getSharedData(request, key);

        // If it isn't in the request try to retrieve it
        if (schoolVideos == null) {
            // Before going to DB see if we have ready done that and determined there is no data
            if (getSharedData(request, noKey) != null) {
                return null;
            }

            schoolVideos = school.getMetadataAsList(School.METADATA_SCHOOL_VIDEO);
            if (!schoolVideos.isEmpty()) {
                setSharedData(request, key, schoolVideos); // Save in request for future use
            }else{
                // Set flag to prevent this DB request again
                setSharedData(request, noKey, "yes");
            }
        }

        return schoolVideos;
    }

    protected Integer getReviewsTotalPages( HttpServletRequest request ) {
        List<Review> reviews = getNonPrincipalReviews( request );
        return _parentReviewHelper.getReviewsTotalPages(reviews.size(), REVIEWS_PER_PAGE);
    }

    protected Integer getReviewsCurrentPage( HttpServletRequest request ) {
        Integer page = new Integer(1);
        if (request.getParameter(REVIEWS_PAGE_PARAM)!=null) {
            try {
                page = Integer.valueOf(request.getParameter(REVIEWS_PAGE_PARAM));
            } catch (NumberFormatException e) {
                page = new Integer(1);
            }
        }
        return page;
    }

    protected Set<Integer> getCensusDataTypeIdsForOverview() {
        Set set = new HashSet<Integer>();
        set.add(CensusDataType.CLASS_SIZE.getId());
        set.add(CensusDataType.STUDENT_TEACHER_RATIO.getId());
        set.add(CensusDataType.HEAD_OFFICIAL_NAME.getId());
        set.add(CensusDataType.HEAD_OFFICIAL_EMAIL.getId());
        set.add(CensusDataType.STUDENTS_ENROLLMENT.getId());     // This value is needed at both the school and district levels
        set.add(CensusDataType.ADVANCED_PLACEMENT_EXAMS_OFFERED.getId());
        set.add(CensusDataType.STUDENTS_ENROLLMENT.getId());
        set.add(CensusDataType.STUDENTS_ETHNICITY.getId());
        return set;
    }




    /**
     * @return map of CensusDataType --> SchoolCensusValue
     */
    protected Map<CensusDataType, List<CensusDataSet>> getSchoolCensusValues(HttpServletRequest request) {
        // CensusDataSet ID --> CensusDataSet
        Map<Integer, CensusDataSet> censusDataSets = _schoolProfileCensusHelper.getCensusDataSets(request);
        CensusDataHolder cdh = _schoolProfileCensusHelper.getGroupedCensusDataSets(request);

        // CensusDataSet ID --> CensusDataSet
        Map<Integer, CensusDataSet> censusDataSetMap = _schoolProfileCensusHelper.getCensusDataSetsWithSchoolData(request);

        Map<CensusDataType, List<CensusDataSet>> censusDataTypeToDataSetMap = new HashMap<CensusDataType, List<CensusDataSet>>();

        for (Map.Entry<Integer, CensusDataSet> dataSetEntry : censusDataSets.entrySet()) {
            Integer censusDataSetId = dataSetEntry.getKey();
            CensusDataSet censusDataSet = dataSetEntry.getValue();

            List<CensusDataSet> censusDataSetsForDataType = censusDataTypeToDataSetMap.get(censusDataSet.getDataType());
            if (censusDataSetsForDataType == null) {
                censusDataSetsForDataType = new ArrayList<CensusDataSet>();
            }
            censusDataSetsForDataType.add(censusDataSet);
            censusDataTypeToDataSetMap.put(censusDataSet.getDataType(), censusDataSetsForDataType);
        }

        return censusDataTypeToDataSetMap;
    }

    /**
     * Helper to get the right SchoolCensusValue from all of the census data available
     * @param censusDataType
     * @return
     */
    public SchoolCensusValue getSchoolCensusValue(HttpServletRequest request, CensusDataType censusDataType) {
        Map<CensusDataType, List<CensusDataSet>> censusValues = getSchoolCensusValues(request);
        if( censusValues!=null ) {
            List<CensusDataSet> censusDataSets = censusValues.get( censusDataType );
            if( censusDataSets != null && censusDataSets.size() > 0 ) {
                //  Go through the list and only examine those with year not 0
                for( CensusDataSet cds : censusDataSets ) {
                    if( cds.getYear() > 0 ) {
                        SchoolCensusValue csv = cds.getSchoolOverrideValue();
                        if( csv != null ) {
                            return csv;
                        }
                        else {
                            return cds.getTheOnlySchoolValue();
                        }
                    }
                }
                // if we get here we need to go back and use the first CDS
                SchoolCensusValue [] csv = (SchoolCensusValue[])(censusDataSets.get(0).getSchoolData().toArray(new SchoolCensusValue[1]));
                return csv[0];
            }
        }

        return null;
    }

    protected List<NearbySchool> getNearbySchools( HttpServletRequest request, int numSchools ) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool(request);
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        return _schoolDao.findNearbySchools(school, numSchools);
    }

    /**
     * Gets Neighborhood info from the sperlings database
     * @param request
     * @return
     */
    protected BpZip getSperlingsInfo (HttpServletRequest request) {

        String key = SPERLINGS;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        BpZip sperlings =  (BpZip)getSharedData( request, key );

        // If it isn't in the request try to retrieve it
        if( sperlings == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            sperlings = _geoDao.findZip(school.getZipcode());

            if( sperlings != null ) {
                setSharedData( request, key, sperlings ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }
        return sperlings;
    }

    /**
     * Gets article and video content from CMS based on the school and EspResponse data
     * @param request
     * @param espData EspResponse data to analyze
     * @param numItems Max number of items to return
     * @return
     */
    protected List<ICmsFeatureSearchResult> getCmsRelatedContent (HttpServletRequest request, Map <String, List<EspResponse>> espData, int numItems) {

        String key = RELATED_CONTENT;
        String noKey = "NO_" + key;

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        List<ICmsFeatureSearchResult> cmsResults =  (List<ICmsFeatureSearchResult>)getSharedData( request, key );

        // If it isn't in the request try to retrieve it
        if( cmsResults == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, noKey ) != null ) {
                return  null;
            }

            cmsResults = _cmsRelatedFeatureSearchService.getRelatedFeatures( school, espData, numItems );

            if( cmsResults != null && !cmsResults.isEmpty()) {
                setSharedData( request, key, cmsResults ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, noKey, "yes" );
            }
        }
        return cmsResults;
    }

    // TODO-13012 TEMPORARY! move these constants and sets out of here or replace with config file/XML
    // TODO-13012 only used by state (maybe city as well, if using equivalent schema)
    // test data types for ratings
    private static final Set<Integer> RATING_TEST_DATA_TYPE_IDS = new HashSet<Integer>();

    static {
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_ACADEMIC_ACHIEVEMENT);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_ACADEMIC_VALUE_ADDED);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_ACADEMIC_POST_SECONDARY_READINESS);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_OVERALL_ACADEMIC);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_CLIMATE_CULTURE_HIGH_EXPECTATIONS);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_CLIMATE_FAMILY_ENGAGEMENT);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_CLIMATE_TEACHER_SUPPORT);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_CLIMATE_SCHOOL_ENVIRONMENT);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_CLIMATE_SOCIAL_EMOTIONAL_LEARNING);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_OVERALL_CLIMATE);
        RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_OVERALL);
    }

    // test data types for state ratings
    private static final Set<Integer> STATE_RATING_TEST_DATA_TYPE_IDS = new HashSet<Integer>();

    static {
        STATE_RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_ACADEMIC_ACHIEVEMENT);
        STATE_RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_ACADEMIC_VALUE_ADDED);
        STATE_RATING_TEST_DATA_TYPE_IDS.add(TestDataType.RATING_ACADEMIC_POST_SECONDARY_READINESS);
    }
// ===================== DATA ===================================

    public static final String DATA_OVERALL_RATING = "overallRating"; // TestDataType.id = 174
    public static final String DATA_OVERALL_ACADEMIC_RATING = "overallAcademicRating"; // TestDataType.id = 167
    public static final String DATA_OVERALL_CLIMATE_RATING = "overallClimateRating"; // TestDataType.id = 173

    public static final String DATA_TEST_SCORE_RATING_YEAR = "testScoreRatingYear"; // TestDataType.id = 164 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_TEST_SCORE_RATING = "schoolTestScoreRating";  // TestDataType.id = 164
    public static final String DATA_STATE_TEST_SCORE_RATING = "stateTestScoreRating";  // TestDataType.id = 164

    public static final String DATA_STUDENT_GROWTH_RATING_YEAR = "studentGrowthRatingYear"; // TestDataType.id = 165 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_STUDENT_GROWTH_RATING = "schoolStudentGrowthRating"; // TestDataType.id = 165
    public static final String DATA_STATE_STUDENT_GROWTH_RATING = "stateStudentGrowthRating"; // TestDataType.id = 165

    public static final String DATA_POST_SECONDARY_READINESS_RATING_YEAR = "postSecondaryReadinessRatingYear"; // TestDataType.id = 166 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_POST_SECONDARY_READINESS_RATING = "schoolPostSecondaryReadinessRating"; // TestDataType.id = 166
    public static final String DATA_STATE_POST_SECONDARY_READINESS_RATING = "statePostSecondaryReadinessRating"; // TestDataType.id = 166

    public static final String DATA_CLIMATE_RATING_NUM_RESPONSES = "climateRatingNumResponses"; // TestDataType.id = 173 (TestDataSchoolValue.number_tested)
    public static final String DATA_SCHOOL_ENVIRONMENT_RATING = "schoolEnvironmentRating"; // TestDataType.id = 172
    public static final String DATA_SOCIAL_EMOTIONAL_LEARNING_RATING = "socialEmotionalLearningRating"; // TestDataType.id = 172
    public static final String DATA_HIGH_EXPECTATIONS_RATING = "highExpectationsRating"; // TestDataType.id = 168
    public static final String DATA_TEACHER_SUPPORT_RATING = "teacherSupportRating"; // TestDataType.id = 170
    public static final String DATA_FAMILY_ENGAGEMENT_RATING = "familyEngagementRating"; // TestDataType.id = 169

    public Map<String, Object> getGsRatings(HttpServletRequest request) {

        String key = GS_RATINGS_ATTRIBUTE;
        String noKey = "NO_" + key;

        Set<String> displayTarget = new HashSet<String>();
        displayTarget.add(TestDataSetDisplayTarget.ratings.name());

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool(request);
        if (school == null) {
            throw new IllegalArgumentException("The request must already contain a school object");
        }

        Map<String, Object> dataMap = (Map) getSharedData(request, key);
        if (dataMap == null) {
            // Before going to DB se if we have ready done that and determined there is no data
            if (getSharedData(request, noKey) != null) {
                return null;
            }

            //Get the test data sets for all the data type ids(school and state data type ids).
            List<TestDataSet> testDataSets = _testDataSetDao.findDataSets(
                    school.getDatabaseState(), null, RATING_TEST_DATA_TYPE_IDS,
                    null, null, null, null, true, null, displayTarget);

            //Get the school test values.
            List<SchoolTestValue> schoolTestValues = new ArrayList<SchoolTestValue>();
            if (!testDataSets.isEmpty()) {
                schoolTestValues = _testDataSchoolValueDao.findValues(testDataSets, school);
            }

            //Construct a list that contains the test data sets for only state rating.This is the subset of the
            //testDataSets defined above.Therefore no need to make an additional call to the database to
            //get the data sets.We already have them.
            List<TestDataSet> stateTestDataSets = new ArrayList<TestDataSet>();
            for (TestDataSet dataSet : testDataSets) {
                if (STATE_RATING_TEST_DATA_TYPE_IDS.contains(dataSet.getDataTypeId())) {
                    stateTestDataSets.add(dataSet);
                }
            }

            if (!schoolTestValues.isEmpty() || !stateTestDataSets.isEmpty()) {
                dataMap = new HashMap<String, Object>();
            }

            //Get the school ratings.
            // TODO-13012 what object type should be in dataMap? float or int? different for overall vs. other ratings?
            for (SchoolTestValue value : schoolTestValues) {
                switch (value.getDataSet().getDataTypeId()) {
                    // overall ratings
                    case TestDataType.RATING_OVERALL:
                        dataMap.put(DATA_OVERALL_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_OVERALL_ACADEMIC:
                        dataMap.put(DATA_OVERALL_ACADEMIC_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_OVERALL_CLIMATE:
                        dataMap.put(DATA_OVERALL_CLIMATE_RATING, value.getValueFloat().intValue());
                        dataMap.put(DATA_CLIMATE_RATING_NUM_RESPONSES, value.getNumberTested());
                        break;

                    // academic ratings
                    case TestDataType.RATING_ACADEMIC_ACHIEVEMENT:
                        dataMap.put(DATA_TEST_SCORE_RATING_YEAR, value.getDataSet().getYear());
                        dataMap.put(DATA_SCHOOL_TEST_SCORE_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_ACADEMIC_VALUE_ADDED:
                        dataMap.put(DATA_STUDENT_GROWTH_RATING_YEAR, value.getDataSet().getYear());
                        dataMap.put(DATA_SCHOOL_STUDENT_GROWTH_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_ACADEMIC_POST_SECONDARY_READINESS:
                        dataMap.put(DATA_POST_SECONDARY_READINESS_RATING_YEAR, value.getDataSet().getYear());
                        dataMap.put(DATA_SCHOOL_POST_SECONDARY_READINESS_RATING, value.getValueFloat().intValue());
                        break;

                    // climate ratings
                    case TestDataType.RATING_CLIMATE_SCHOOL_ENVIRONMENT:
                        dataMap.put(DATA_SCHOOL_ENVIRONMENT_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_CLIMATE_SOCIAL_EMOTIONAL_LEARNING:
                        dataMap.put(DATA_SOCIAL_EMOTIONAL_LEARNING_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_CLIMATE_CULTURE_HIGH_EXPECTATIONS:
                        dataMap.put(DATA_HIGH_EXPECTATIONS_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_CLIMATE_TEACHER_SUPPORT:
                        dataMap.put(DATA_TEACHER_SUPPORT_RATING, value.getValueFloat().intValue());
                        break;
                    case TestDataType.RATING_CLIMATE_FAMILY_ENGAGEMENT:
                        dataMap.put(DATA_FAMILY_ENGAGEMENT_RATING, value.getValueFloat().intValue());
                        break;
                }
            }

            //Get the state ratings only if there is school rating info.
            if (!stateTestDataSets.isEmpty() && (dataMap.containsKey(DATA_SCHOOL_TEST_SCORE_RATING) || dataMap.containsKey(DATA_SCHOOL_STUDENT_GROWTH_RATING))
                    && (SchoolProfileRatingsController.isShowStateTestScoreRating(school.getDatabaseState()) ||
                    SchoolProfileRatingsController.isShowStateStudentGrowthRating(school.getDatabaseState()))) {
                List<StateTestValue> stateTestValues = _testDataStateValueDao.findValues(stateTestDataSets, school.getDatabaseState());

                // TODO-13012 what object type should be in dataMap? float or int? different for overall vs. other ratings?
                for (StateTestValue value : stateTestValues) {
                    switch (value.getDataSet().getDataTypeId()) {
                        case TestDataType.RATING_ACADEMIC_ACHIEVEMENT:
                            dataMap.put(DATA_STATE_TEST_SCORE_RATING, value.getValueFloat());
                            break;
                        case TestDataType.RATING_ACADEMIC_VALUE_ADDED:
                            dataMap.put(DATA_STATE_STUDENT_GROWTH_RATING, value.getValueFloat());
                            break;
                        case TestDataType.RATING_ACADEMIC_POST_SECONDARY_READINESS:
                            dataMap.put(DATA_STATE_POST_SECONDARY_READINESS_RATING, value.getValueFloat());
                            break;
                    }
                }
            }

            if (dataMap == null || dataMap.isEmpty()) {
                // Set flag to prevent this DB request again
                setSharedData(request, noKey, "yes");
            } else {
                // Store map in request
                setSharedData(request, key, dataMap); // Save in request for future use
            }
        }
        return dataMap;
    }

    // ============== The following setters are just for unit testing ===================
    public void setEspResponseDao( IEspResponseDao espResponseDao ) {
        _espResponseDao = espResponseDao;
    }

    public  void setSchoolMediaDao( ISchoolMediaDao schoolMediaDao ) {
        _schoolMediaDao = schoolMediaDao;
    }

    public void setReviewDao( IReviewDao reviewDao ) {
        _reviewDao = reviewDao;
    }

    public void setReportedEntityDao( IReportedEntityDao reportedEntityDao ) {
        _reportedEntityDao = reportedEntityDao;
    }

    public void setRequestAttributeHelper( RequestAttributeHelper requestAttributeHelper ) {
        _requestAttributeHelper = requestAttributeHelper;
    }

    public void setCensusDataSchoolValueDao(ICensusDataSchoolValueDao censusDataSchoolValueDao) {
        _censusDataSchoolValueDao = censusDataSchoolValueDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }

    public void setCensusDataConfigDao(ICensusDataConfigEntryDao censusDataConfigDao) {
        _censusDataConfigDao = censusDataConfigDao;
    }

    public void setSchoolDao( ISchoolDao schoolDao ) {
        _schoolDao = schoolDao;
    }

    public void setGeoDao( IGeoDao geoDao ) {
        _geoDao = geoDao;
    }

    public void setCmsRelatedFeatureSearchService( CmsRelatedFeatureSearchService cmsRelatedFeatureSearchService) {
        _cmsRelatedFeatureSearchService = cmsRelatedFeatureSearchService;
    }
}



