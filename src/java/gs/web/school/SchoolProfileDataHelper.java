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
import gs.web.request.RequestAttributeHelper;
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
    private final static String PUBLISHED_REVIEW_COUNT = "publishedReviewCount";
    private final static String REVIEWS = "reviews";
    private final static String REVIEWS_COUNT = "reviewsCount";
    private final static String SCHOOL_MEDIA_REPORTS_BY_USER = "reportsByUser";
    private final static String ENROLLMENT = "enrollment";
    private final static String SPERLINGS = "sperlings";
    private final static String RELATED_CONTENT = "relatedContent";

    private final static String CENSUS_DATA = "censusData";

    // The following attributes are used to keep track of no results to prevent multiple requests
    private final static String NO_ESP_DATA_REQUEST_ATTRIBUTE = "espDataEmpty";
    private final static String NO_SCHOOL_MEDIA_REQUEST_ATTRIBUTE = "schoolMediaEmpty";
    private final static String NO_COMMUNITY_RATINGS_ATTRIBUTE = "communityRatingsEmpty";
    private final static String NO_PUBLISHED_REVIEW_COUNT = "publishedReviewCountEmpty";
    private final static String NO_CENSUS_DATA = "censusDataEmpty";
    private final static String NO_ENROLLMENT = "enrollmentEmpty";
    private final static String NO_SPERLINGS = "sperlingsEmpty";
    private final static String NO_RELATED_CONTENT = "relatedContentEmpty";

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

    protected Map<String, List<EspResponse>> getEspDataForSchool( HttpServletRequest request ) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Map<String, List<EspResponse>> espData = (Map<String, List<EspResponse>>) getSharedData( request, ESP_DATA_REQUEST_ATTRIBUTE );
        // If it isn't in the request try to retrieve it
        if( espData == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_ESP_DATA_REQUEST_ATTRIBUTE ) != null ) {
                return  null;
            }

            List<EspResponse> results = _espResponseDao.getResponses( school );

            if( results != null && !results.isEmpty() ) {

                // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
                // and the value will be the corresponding list of EspResponse objects
                espData = EspResponse.rollup(results);

                setSharedData( request, ESP_DATA_REQUEST_ATTRIBUTE, espData ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_ESP_DATA_REQUEST_ATTRIBUTE, "yes" );
            }
        }

        return espData;
    }

    private void debugMsgIsNull(String msg, Object obj) {

        StackTraceElement[] st =  Thread.currentThread().getStackTrace();
        _log.error(msg + ((obj==null)?"null":"not null") + ", called from:\n  " + st[2].toString() + "\n  " + st[3].toString() + "\n");

    }

    protected Integer getEnrollment( HttpServletRequest request ) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Integer enrollment = (Integer) getSharedData( request, ENROLLMENT );

        // If it isn't in the request try to retrieve it
        if( enrollment == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_ENROLLMENT ) != null ) {
                return  null;
            }

            enrollment = school.getEnrollmentOrCapacity();

            if( enrollment != null ) {

                setSharedData( request, ENROLLMENT, enrollment ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_ENROLLMENT, "yes" );
            }
        }

        return enrollment;
    }

    protected List<SchoolMedia> getSchoolMedia(HttpServletRequest request) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        List<SchoolMedia> schoolMedia = (List<SchoolMedia>) getSharedData( request, SCHOOL_MEDIA_REQUEST_ATTRIBUTE );

        // If it isn't in the request try to retrieve it
        if( schoolMedia == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_SCHOOL_MEDIA_REQUEST_ATTRIBUTE ) != null ) {
                return  null;
            }

            schoolMedia =_schoolMediaDao.getAllActiveBySchool(school);

            if( schoolMedia != null && !schoolMedia.isEmpty() ) {
               setSharedData( request, SCHOOL_MEDIA_REQUEST_ATTRIBUTE, schoolMedia ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_SCHOOL_MEDIA_REQUEST_ATTRIBUTE, "yes" );
            }
        }
        return schoolMedia;
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

        // First see if it is already in the request
        // The Map created in the next statement contains:
        // key = userId
        // Value = Map<mediaId, booleanValue where boolean value contains:
        //    null if no value has been determined
        //    True if there is a report
        //    False if there is no report
        Map<Integer, Map<Integer, Boolean>> schoolMediaReportsByUser = (Map<Integer, Map<Integer, Boolean>>) getSharedData( request, SCHOOL_MEDIA_REPORTS_BY_USER );

        // Case 1 - no data at all - create the data and save it
        if( schoolMediaReportsByUser == null ) {
            // Create structure for schoolMediaReportsByUser and provide an empty structure for this user which will be filled in in the next step
            Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(schoolMediaList.size());
            schoolMediaReportsByUser = new HashMap<Integer, Map<Integer, Boolean>>(1);
            schoolMediaReportsByUser.put( user.getId(), reports );
            setSharedData( request, SCHOOL_MEDIA_REPORTS_BY_USER, schoolMediaReportsByUser );
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

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Ratings ratings =  (Ratings)getSharedData( request, COMMUNITY_RATINGS_ATTRIBUTE );

        // If it isn't in the request try to retrieve it
        if( ratings == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_COMMUNITY_RATINGS_ATTRIBUTE ) != null ) {
                return  null;
            }

            ratings = _reviewDao.findRatingsBySchool(school);

            if( ratings != null ) {
                setSharedData( request, COMMUNITY_RATINGS_ATTRIBUTE, ratings ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_COMMUNITY_RATINGS_ATTRIBUTE, "yes" );
            }
        }
        return ratings;
    }

    protected Long getCountPublishedNonPrincipalReviews (HttpServletRequest request) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Long numberOfReviews =  (Long)request.getAttribute(PUBLISHED_REVIEW_COUNT);

        // If it isn't in the request try to retrieve it
        if( numberOfReviews == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_PUBLISHED_REVIEW_COUNT ) != null ) {
                return  null;
            }

            numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);

            if( numberOfReviews != null ) {
                request.setAttribute(PUBLISHED_REVIEW_COUNT, numberOfReviews); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_PUBLISHED_REVIEW_COUNT, "yes" );
            }
        }
        return numberOfReviews;
    }

    protected List<Review> getNonPrincipalReviews (HttpServletRequest request, int countRequested ) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        List<Review> reviews = (List<Review>)getSharedData( request, REVIEWS );
        if( reviews == null ) {
            reviews = new ArrayList<Review>(0);
        }

        // Keep a count of the number of reviews available in the DB if more than that number have been requested.
        // This is to prevent asking for more than are available.
//        Integer reviewsCount = (Integer)getSharedData( request, REVIEWS_COUNT );

        // If the number stored request the number requested just return them
        if( reviews.size() == countRequested ) {
            // we are done, just return them
            return reviews;
        }
        else if( reviews.size() > countRequested ) {
            List<Review> subset = reviews.subList(0, countRequested);
            return subset;
        }
        else {
            // Fewer reviews are available than requested.
            // First see if we know how many are in the DB
            Integer reviewsCount = (Integer)getSharedData( request, REVIEWS_COUNT );
            if( reviewsCount != null ) {
                // See if we got fewer than we wanted and if so ???
                // We have a count and this is the max we can return, so return them
                return reviews;
            }
            else {
                // Go to the DB for the request number
                reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, countRequested);
                if( reviews == null ) {
                    setSharedData( request, REVIEWS_COUNT, new Integer(0) );  // Save the count (of 0) so we don't hit the DB again
                    return null;    // No data is available, return null
                }
                else {
                    // Store for future use
                    setSharedData( request, REVIEWS, reviews );
                    // If we got fewer than requested save that count so we don't again ask for more than are present
                    if( reviews.size() < countRequested ) {
                        setSharedData( request, REVIEWS_COUNT, new Integer(reviews.size()) );  // Save the count since we know the max now
                    }
                }
            }

        }

        return  reviews;    // Return what we have
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

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        BpZip sperlings =  (BpZip)getSharedData( request, SPERLINGS );

        // If it isn't in the request try to retrieve it
        if( sperlings == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_SPERLINGS ) != null ) {
                return  null;
            }

            sperlings = _geoDao.findZip(school.getZipcode());

            if( sperlings != null ) {
                setSharedData( request, SPERLINGS, sperlings ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_SPERLINGS, "yes" );
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

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        List<ICmsFeatureSearchResult> cmsResults =  (List<ICmsFeatureSearchResult>)getSharedData( request, RELATED_CONTENT );

        // If it isn't in the request try to retrieve it
        if( cmsResults == null ) {
            // Before going to DB se if we have ready done that and determined there is no data
            if( getSharedData( request, NO_RELATED_CONTENT ) != null ) {
                return  null;
            }

            cmsResults = _cmsRelatedFeatureSearchService.getRelatedFeatures( school, espData, numItems );

            if( cmsResults != null && !cmsResults.isEmpty()) {
                setSharedData( request, RELATED_CONTENT, cmsResults ); // Save in request for future use
            }
            else {
                // Set flag to prevent this DB request again
                setSharedData( request, NO_RELATED_CONTENT, "yes" );
            }
        }
        return cmsResults;
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



