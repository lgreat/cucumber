package gs.web.school;

import gs.data.school.NearbySchool;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;

import java.util.ArrayList;
import java.util.List;

public class NearbySchoolsHelper {

    IReviewDao _reviewDao;

    /**
     * Returns a list of MapSchools for a given list of NearbySchools
     *
     * @param schools list of nearby schools
     * @return MapSchools
     */
    public List<MapSchool> getRatingsForNearbySchools(List<NearbySchool> schools) {
        // this is our data structure -- contains basically a school, a GS rating, and a parent rating
        List<MapSchool> mapSchools = new ArrayList<MapSchool>();
        // for each school
        for (NearbySchool nearbySchool : schools) {
            School school = nearbySchool.getNeighbor();
            // MapSchool is a subclass of NearbySchool
            MapSchool mapSchool = new MapSchool();
            // now we copy over the fields we want: school and gs rating
            // School. I don't like that it is called neighbor, but that's from the superclass NearbySchool
            mapSchool.setNeighbor(school);
            // GS rating
            mapSchool.setRating(nearbySchool.getRating());

            // Retrieve parent ratings
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            // Parent ratings
            mapSchool.setParentRatings(ratings);

            //Add distance
            mapSchool.setDistance(nearbySchool.getDistance());

            // Add data structure to list
            mapSchools.add(mapSchool);
        }

        return mapSchools;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
