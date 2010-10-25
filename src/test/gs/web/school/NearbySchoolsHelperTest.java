package gs.web.school;

import gs.data.school.NearbySchool;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.state.State;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import static org.easymock.EasyMock.*;

public class NearbySchoolsHelperTest extends TestCase {
    NearbySchoolsHelper helper;

    IReviewDao reviewDao;

    public void setUp() {
        helper = new NearbySchoolsHelper();
        reviewDao = createStrictMock(IReviewDao.class);
        helper.setReviewDao(reviewDao);
    }

    public void testGetRatingsForNearbySchools() throws Exception {
        List<MapSchool> mapSchools;
        List<NearbySchool> nearbySchools = new ArrayList<NearbySchool>();

        School school1 = new School();
        school1.setId(1);
        school1.setDatabaseState(State.CA);
        School neighbor1 = new School();
        neighbor1.setId(10);
        neighbor1.setDatabaseState(State.CA);
        NearbySchool nearbySchool1 = new NearbySchool();
        nearbySchool1.setSchool(school1);
        nearbySchool1.setNeighbor(neighbor1);
        nearbySchool1.setDistance(1f);
        nearbySchool1.setRating(4);
        
        School school2 = new School();
        school2.setId(2);
        school2.setDatabaseState(State.CA);
        School neighbor2 = new School();
        neighbor2.setId(20);
        neighbor2.setDatabaseState(State.CA);
        NearbySchool nearbySchool2 = new NearbySchool();
        nearbySchool2.setSchool(school2);
        nearbySchool2.setNeighbor(neighbor2);
        nearbySchool2.setDistance(1f);
        nearbySchool2.setRating(4);

        nearbySchools.add(nearbySchool1);
        nearbySchools.add(nearbySchool2);

        Ratings ratings1 = new Ratings();
        Ratings ratings2 = new Ratings();

        expect(reviewDao.findRatingsBySchool(neighbor1)).andReturn(ratings1);
        expect(reviewDao.findRatingsBySchool(neighbor2)).andReturn(ratings2);
        replay(reviewDao);
        mapSchools = helper.getRatingsForNearbySchools(nearbySchools);

        verify(reviewDao);

        assertEquals("mapSchools should be same size as input nearbySchools list", nearbySchools.size(), mapSchools.size());
        MapSchool mapSchool1 = mapSchools.get(0);
        MapSchool mapSchool2 = mapSchools.get(1);

        assertTrue("mapSchool should match neighbor in nearby school", mapSchool1.getNeighbor() == neighbor1 || mapSchool1.getNeighbor() == neighbor2);
        assertTrue("mapSchool should match neighbor in nearby school", mapSchool2.getNeighbor() == neighbor1 || mapSchool2.getNeighbor() == neighbor2);

        assertTrue("Distance should match distance in nearby school", mapSchool1.getDistance() == 1f);

        assertTrue("mapSchool should contain ratings for either nearbySchool1 or nearbySchool2", mapSchool1.getParentRatings() == ratings1 || mapSchool1.getParentRatings() == ratings2);
        assertTrue("mapSchool should contain ratings for either nearbySchool1 or nearbySchool2", mapSchool2.getParentRatings() == ratings1 || mapSchool2.getParentRatings() == ratings2);
    }
}
