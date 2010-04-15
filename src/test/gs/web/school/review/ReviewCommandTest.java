package gs.web.school.review;

import gs.data.school.review.CategoryRating;
import gs.web.BaseTestCase;

public class ReviewCommandTest extends BaseTestCase {

    ReviewCommand _command = new ReviewCommand();

    public void setUp() throws Exception {

    }

    //make sure calling getters doesnt cause NPEs
    public void testReviewCommandIsNullSafe() {

        _command.setOverall(null);
        _command.setActivities(null);
        _command.setParent(null);
        _command.setPFacilities(null);
        _command.setPParents(null);
        _command.setTeacher(null);

        CategoryRating cr = null;
        String rating = null;

        rating = _command.getOverallAsString();
        cr = _command.getActivities();
        rating = _command.getActivitiesAsString();
        cr = _command.getOverall();
        rating = _command.getParentAsString();
        cr = _command.getParent();
        rating = _command.getPFacilitiesAsString();
        cr = _command.getPFacilities();
        rating = _command.getPParentsAsString();
        cr = _command.getPParents();
        rating = _command.getPrincipalAsString();
        cr = _command.getPrincipal();
        rating = _command.getTeacherAsString();
        cr = _command.getTeacher();

        _command.setOverallAsString(null);
        _command.setActivitiesAsString(null);
        _command.setParentAsString(null);
        _command.setPFacilitiesAsString(null);
        _command.setPParentsAsString(null);
        _command.setTeacherAsString(null);

        rating = _command.getOverallAsString();
        cr = _command.getActivities();
        rating = _command.getActivitiesAsString();
        cr = _command.getOverall();
        rating = _command.getParentAsString();
        cr = _command.getParent();
        rating = _command.getPFacilitiesAsString();
        cr = _command.getPFacilities();
        rating = _command.getPParentsAsString();
        cr = _command.getPParents();
        rating = _command.getPrincipalAsString();
        cr = _command.getPrincipal();
        rating = _command.getTeacherAsString();
        cr = _command.getTeacher();
    }

    public void testReviewCommandBasics() {
        _command.setOverall(CategoryRating.DECLINE_TO_STATE);

        assertEquals("Getters and setters should work logically", _command.getOverall(), CategoryRating.DECLINE_TO_STATE);
    }

}
