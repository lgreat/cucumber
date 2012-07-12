package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.web.request.RequestAttributeHelper;
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
public class SchoolProfileDataHelper {

    private final static String ESP_DATA_REQUEST_ATTRIBUTE = "espData";
    private final static String SCHOOL_MEDIA_REQUEST_ATTRIBUTE = "schoolMedia";
    private final static String SCHOOL_REQUEST_ATTRIBUTE = "school";
    private final static String SCHOOL_RATINGS_ATTRIBUTE = "ratings";
    private final static String PUBLISHED_REVIEW_COUNT = "publishedReviewCount";
    private final static String REVIEWS = "reviews";
    private final static String REVIEWS_COUNT = "reviewsCount";
    private final static String SCHOOL_MEDIA_REPORTS_BY_USER = "reportsByUser";

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

    protected Map<String, List<EspResponse>> getEspDataForSchool( HttpServletRequest request ) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Map<String, List<EspResponse>> espData = (Map<String, List<EspResponse>>) request.getAttribute( ESP_DATA_REQUEST_ATTRIBUTE );

        // If it isn't in the request try to retrieve it
        if( espData == null ) {
            List<EspResponse> results = _espResponseDao.getResponses( school );

            if( results != null && !results.isEmpty() ) {

                // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
                // and the value will be the corresponding list of EspResponse objects
                espData = espResultsToMap(results);

                request.setAttribute( ESP_DATA_REQUEST_ATTRIBUTE, espData ); // Save in request for future use
            }
        }

        return espData;
    }

    protected static Map<String, List<EspResponse>> espResultsToMap(List<EspResponse> results) {

        Map<String, List<EspResponse>> resultsMap = new HashMap<String, List<EspResponse>>();

        // Loop over the incoming results and construct the Map
        for( EspResponse r : results ) {
            String key = r.getKey();
            List<EspResponse> existingList = resultsMap.get( key );
            if( existingList != null ) {
                // add to existing list
                existingList.add( r );
            }
            else {
                // Create new list and add to HashMap
                List<EspResponse> newList = new ArrayList<EspResponse>();
                newList.add( r );
                resultsMap.put( key, newList );
            }
        }
        return resultsMap;
    }

    protected List<SchoolMedia> getSchoolMedia(HttpServletRequest request) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        List<SchoolMedia> schoolMedia = (List<SchoolMedia>) request.getAttribute( SCHOOL_MEDIA_REQUEST_ATTRIBUTE );

        // If it isn't in the request try to retrieve it
        if( schoolMedia == null ) {
            schoolMedia =_schoolMediaDao.getAllActiveBySchool(school);

            if( schoolMedia != null && !schoolMedia.isEmpty() ) {
               request.setAttribute( SCHOOL_MEDIA_REQUEST_ATTRIBUTE, schoolMedia ); // Save in request for future use
            }
        }
        return schoolMedia;
    }

    protected Map<Integer, Boolean> getReportsForSchoolMedia(HttpServletRequest request, User user, List<SchoolMedia> schoolMediaList) {
        if (schoolMediaList == null || user == null) {
            return null;
        }

        // First see if it is already in the request
        Map<Integer, Map<Integer, Boolean>> schoolMediaReportsByUser = (Map<Integer, Map<Integer, Boolean>>) request.getAttribute( SCHOOL_MEDIA_REPORTS_BY_USER );
        // Case 1 - no data at all - create the data and save it
        if( schoolMediaReportsByUser == null ) {
            // Create structure for schoolMediaReportsByUser and provide an empty structure for this user which will be filled in in the next step
            Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(schoolMediaList.size());
            schoolMediaReportsByUser = new HashMap<Integer, Map<Integer, Boolean>>(1);
            schoolMediaReportsByUser.put( user.getId(), reports );
            request.setAttribute( SCHOOL_MEDIA_REPORTS_BY_USER, schoolMediaReportsByUser );
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


    protected Ratings getSchoolRatings (HttpServletRequest request) {

        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // Get Data
        // First see if it is already in the request
        Ratings ratings =  (Ratings)request.getAttribute( SCHOOL_RATINGS_ATTRIBUTE );

        // If it isn't in the request try to retrieve it
        if( ratings == null ) {
            ratings = _reviewDao.findRatingsBySchool(school);

            if( ratings != null ) {
                request.setAttribute( SCHOOL_RATINGS_ATTRIBUTE, ratings ); // Save in request for future use
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
            numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);

            if( numberOfReviews != null ) {
                request.setAttribute(PUBLISHED_REVIEW_COUNT, numberOfReviews ); // Save in request for future use
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
        List<Review> reviews = (List<Review>)request.getAttribute( REVIEWS );
        if( reviews == null ) {
            reviews = new ArrayList<Review>(0);
        }

        // Keep a count of the number of reviews available in the DB if more than that number have been requested.
        // This is to prevent asking for more than are available.
//        Integer reviewsCount = (Integer)request.getAttribute( REVIEWS_COUNT );

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
            Integer reviewsCount = (Integer)request.getAttribute( REVIEWS_COUNT );
            if( reviewsCount != null ) {
                // See if we got fewer than we wanted and if so ???
                // We have a count and this is the max we can return, so return them
                return reviews;
            }
            else {
                // Go to the DB for the request number
                reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, countRequested);
                if( reviews == null ) {
                    request.setAttribute( REVIEWS_COUNT, new Integer(0) );  // Save the count (of 0) so we don't hit the DB again
                    return null;    // No data is available, return null
                }
                else {
                    // Store for future use
                    request.setAttribute( REVIEWS, reviews );
                    // If we got fewer than requested save that count so we don't again ask for more than are present
                    if( reviews.size() < countRequested ) {
                        request.setAttribute( REVIEWS_COUNT, new Integer(reviews.size()) );  // Save the count since we know the max now
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
        return set;
    }

    /**
     * @return map of CensusDataType --> SchoolCensusValue
     */
    protected Map<CensusDataType, SchoolCensusValue> getSchoolCensusValues(HttpServletRequest request) {
        // CensusDataSet ID --> SchoolCensusValue
        Map<Integer, SchoolCensusValue> schoolCensusValueMap = _schoolProfileCensusHelper.getSchoolCensusValues(request);

        Map<CensusDataType, SchoolCensusValue> dataTypeIdSchoolValueMap = new HashMap<CensusDataType, SchoolCensusValue>();
        for (Map.Entry<Integer, SchoolCensusValue> entry : schoolCensusValueMap.entrySet()) {
            dataTypeIdSchoolValueMap.put(entry.getValue().getDataSet().getDataType(), entry.getValue());
        }

        return dataTypeIdSchoolValueMap;
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
}



