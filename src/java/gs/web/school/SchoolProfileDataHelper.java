package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final static String SCHOOL_MEDIA_REPORTS_BY_USER = "reportsByUser";

    @Autowired
    private IEspResponseDao _espResponseDao;

    @Autowired
    private ISchoolMediaDao _schoolMediaDao;

    @Autowired
    private IReviewDao _reviewDao;

    @Autowired
    private IReportedEntityDao _reportedEntityDao;

    protected Map<String, List<EspResponse>> getEspDataForSchool( HttpServletRequest request, School school ) {

        // Make sure we have a school
        if( school == null ) {
            school = (School) request.getAttribute(SCHOOL_REQUEST_ATTRIBUTE);

            if (school == null) {
                return null;
            }
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

    protected List<SchoolMedia> getSchoolMedia(HttpServletRequest request, School school) {

        // Make sure we have a school
        if( school == null ) {
            school = (School) request.getAttribute(SCHOOL_REQUEST_ATTRIBUTE);

            if (school == null) {
                return null;
            }
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


    protected Ratings getSchoolRatings (HttpServletRequest request, School school) {

        // Make sure we have a school
        if( school == null ) {
            school = (School) request.getAttribute(SCHOOL_REQUEST_ATTRIBUTE);

            if (school == null) {
                return null;
            }
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

    protected Long getCountPublishedNonPrincipalReviews (HttpServletRequest request, School school) {

        // Make sure we have a school
        if( school == null ) {
            school = (School) request.getAttribute(SCHOOL_REQUEST_ATTRIBUTE);

            if (school == null) {
                return null;
            }
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

    protected List<Review> getNonPrincipalReviews (HttpServletRequest request, School school, int countRequested ) {

        // Make sure we have a school
        if( school == null ) {
            school = (School) request.getAttribute(SCHOOL_REQUEST_ATTRIBUTE);

            if (school == null) {
                return null;
            }
        }

        // Get Data
        List<Review> reviews = (List<Review>)request.getAttribute( REVIEWS );
        if( reviews == null ) {
            reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, countRequested);
            if( reviews == null ) {
                return null;    // No data is available, return null
            }
            else {
                // Store for future use
                request.setAttribute( REVIEWS, reviews );
            }
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

}



