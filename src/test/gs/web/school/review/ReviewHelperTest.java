package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.data.state.State;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: samson
 * Date: Oct 11, 2010
 * Time: 1:00:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewHelperTest extends TestCase {

    School _school;
    ReviewHelper _reviewHelper;

    public void setUp() {
        _school = new School();
        _school.setDatabaseState(State.CA);
        _school.setId(6397);
        _school.setName("Lowell High School");
        _school.setCity("San Francisco");
        _school.setActive(true);

        _reviewHelper = new ReviewHelper();
    }

    public void testUpdateReview() throws Exception {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();

        Review review = new Review();
        review.setComments("some old comments");
        review.setPoster(Poster.STUDENT);
        review.setTeachers(CategoryRating.RATING_4);
        review.setPosted(yesterday);
        review.setProcessDate(yesterday);
        review.setNote("some notes");
        review.setSchool(_school);

        ReviewCommand reviewCommand = new ReviewCommand();
        reviewCommand.setPoster(Poster.PARENT);

        _reviewHelper.updateReview(review, _school, reviewCommand);

        assertEquals("School should have remained on review", _school, review.getSchool());
        assertEquals("Poster should have remained on review", reviewCommand.getPoster(), review.getPoster());
        assertTrue("Posted should have been updated on review", review.getPosted().after(yesterday));
        assertNull("Notes should have been resest.", review.getNote());
    }

    public void testCreateReview() throws Exception {
        User _user = new User();
        _user.setEmail("ssprouse@greatschools.org");
        _user.setId(1);

        ReviewCommand _command = new ReviewCommand();
        _command.setEmail(_user.getEmail());

        _command.setPrincipalAsString("1");
        _command.setTeacherAsString("2");
        _command.setParentAsString("4");
        _command.setOverallAsString("3");

        _command.setComments("this school rocks");
        _command.setPosterAsString("parent");
        _command.setAllowContact(false);
        _command.setFirstName("dave");

        Review r = _reviewHelper.createReview(_user, _school, _command);

        assertEquals(CategoryRating.RATING_1, r.getPrincipal());
        assertEquals(CategoryRating.RATING_2, r.getTeachers());
        assertEquals(CategoryRating.RATING_4, r.getParents());
        assertEquals(CategoryRating.RATING_3, r.getQuality());

        assertEquals(_command.getComments(), r.getComments());
        assertEquals(_command.getComments(), r.getOriginal());

        assertEquals(_command.getPoster(), r.getPoster());
        assertEquals(_command.isAllowContact(), r.isAllowContact());
        assertTrue(r.isAllowName());
    }

    public void testCreateReview2() throws Exception {

        User user = new User();
        user.setId(1);

        ReviewCommand reviewCommand = new ReviewCommand();
        reviewCommand.setPoster(Poster.STUDENT);

        Review review = _reviewHelper.createReview(user, _school, reviewCommand);

        assertEquals("School should have been set on review", _school, review.getSchool());
        assertEquals("User should have been set on review", user, review.getUser());
        assertEquals("Poster should have been set on review", reviewCommand.getPoster(), review.getPoster());
    }

    public void testSetRatingsOnReview_HighschoolParent() throws Exception {

        ReviewCommand command = new ReviewCommand();

        Review review = new Review();

        command.setTeacher(CategoryRating.RATING_1);
        command.setParent(CategoryRating.RATING_2);
        command.setPrincipal(CategoryRating.RATING_3);
        command.setOverall(CategoryRating.RATING_4);

        _reviewHelper.setRatingsOnReview(LevelCode.HIGH, command,  review, Poster.PARENT);

        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_1, review.getTeachers());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_2, review.getParents());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_3, review.getPrincipal());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_4, review.getQuality());
    }

    public void testSetRatingsOnReview_HighschoolStudent() throws Exception {

        ReviewCommand command = new ReviewCommand();

        Review review = new Review();

        command.setTeacher(CategoryRating.RATING_1);
        command.setParent(CategoryRating.RATING_2);
        command.setPrincipal(CategoryRating.RATING_3);
        command.setOverall(CategoryRating.RATING_4);

        _reviewHelper.setRatingsOnReview(LevelCode.HIGH, command,  review, Poster.STUDENT);

        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_1, review.getTeachers());
        assertNull("Rating should not have been set.", review.getParents());
        assertNull("Rating should not have been set.", review.getPrincipal());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_4, review.getQuality());
    }

    public void testSetRatingsOnReview_PreschoolStudent() throws Exception {

        ReviewCommand command = new ReviewCommand();

        Review review = new Review();

        command.setTeacher(CategoryRating.RATING_1);
        command.setParent(CategoryRating.RATING_2);
        command.setPrincipal(CategoryRating.RATING_3);

        _reviewHelper.setRatingsOnReview(LevelCode.PRESCHOOL, command,  review, Poster.STUDENT);

        assertNull("Rating should not have been set.", review.getTeachers());
        assertNull("Rating should not have been set.", review.getParents());
        assertNull("Rating should not have been set.", review.getPrincipal());
    }

    public void testSetRatingsOnReview_PreschoolParent() throws Exception {

        ReviewCommand command = new ReviewCommand();

        Review review = new Review();

        command.setPTeachers(CategoryRating.RATING_1);
        command.setPParents(CategoryRating.RATING_2);
        command.setPFacilities(CategoryRating.RATING_3);
        command.setPOverall(CategoryRating.RATING_4);

        _reviewHelper.setRatingsOnReview(LevelCode.PRESCHOOL, command,  review, Poster.PARENT);

        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_1, review.getPTeachers());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_2, review.getPParents());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_3, review.getPFacilities());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_4, review.getPOverall());
    }

    public void testPopulateReviewFromCommand() throws Exception {

        ReviewCommand command = new ReviewCommand();

        Review review = new Review();

        command.setTeacher(CategoryRating.RATING_1);
        command.setParent(CategoryRating.RATING_2);
        command.setPrincipal(CategoryRating.RATING_3);
        command.setOverall(CategoryRating.RATING_4);
        command.setComments("test comment");
        command.setPoster(Poster.PARENT);
        command.setLastName("test");

        _reviewHelper.populateReviewFromCommand(review, _school, command);

        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_1, review.getTeachers());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_2, review.getParents());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_3, review.getPrincipal());
        assertEquals("Rating should have been set correctly.", CategoryRating.RATING_4, review.getQuality());

        assertEquals("Poster should have been set on review", Poster.PARENT, review.getPoster());
        assertEquals("Comments should have been set on review", command.getComments(), review.getComments());
        assertEquals("Original Comments should have been set on review", command.getComments(), review.getOriginal());
        assertTrue("Allow contact should have been set to true.", review.isAllowName());
    }


    public void testSetRatingsOnReviewPreschoolParent() throws Exception {
        LevelCode levelCode = LevelCode.PRESCHOOL;
        Review review = new Review();
        ReviewCommand reviewCommand = new ReviewCommand();

        reviewCommand.setTeacherAsString("1");
        reviewCommand.setParentAsString("1");
        reviewCommand.setPrincipalAsString("1");
        reviewCommand.setPFacilitiesAsString("1");
        reviewCommand.setOverallAsString("1");

        _reviewHelper.setRatingsOnReview(levelCode, reviewCommand, review, Poster.PARENT);

        assertEquals(CategoryRating.RATING_1, review.getPTeachers());
        assertEquals(CategoryRating.RATING_1, review.getPParents());
        assertEquals(CategoryRating.RATING_1, review.getPFacilities());
        assertEquals(CategoryRating.RATING_1, review.getPOverall());

        assertNull(review.getPrincipal());
        assertNull(review.getQuality());

    }

    public void testSetRatingsOnReviewParent() throws Exception {
        LevelCode levelCode = LevelCode.ELEMENTARY_HIGH;
        Review review = new Review();
        ReviewCommand reviewCommand = new ReviewCommand();

        reviewCommand.setTeacherAsString("1");
        reviewCommand.setParentAsString("1");
        reviewCommand.setPrincipalAsString("1");
        reviewCommand.setPFacilitiesAsString("1");
        reviewCommand.setOverallAsString("1");

        _reviewHelper.setRatingsOnReview(levelCode, reviewCommand, review, Poster.PARENT);

        assertEquals(CategoryRating.RATING_1, review.getTeachers());
        assertEquals(CategoryRating.RATING_1, review.getParents());
        assertEquals(CategoryRating.RATING_1, review.getPrincipal());
        assertEquals(CategoryRating.RATING_1, review.getQuality());

        assertNull(review.getPFacilities());
        assertNull(review.getPOverall());

    }

}
