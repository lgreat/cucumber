package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.review.*;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;

import java.util.*;

/**
 * Testing the refactored ParentReviewHelper code
 */
public class ParentReviewHelperTest extends BaseTestCase {

    private ParentReviewHelper _parentReviewHelper = new ParentReviewHelper();

    public void testHandleGetReviewsBy() throws Exception {
        // handle default case
        Set<Poster> reviews = _parentReviewHelper.handleGetReviewsBy("a");
        assertEquals(0, reviews.size());

        reviews = _parentReviewHelper.handleGetReviewsBy("p");
        assertEquals(1, reviews.size());
        assertEquals(Poster.PARENT, reviews.toArray()[0]);


    }

    public void testFindCurrentPage() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter(ParentReviewHelper.PARAM_PAGE, "1");
        assertEquals(1, _parentReviewHelper.findCurrentPage(request));

        request.setParameter(ParentReviewHelper.PARAM_PAGE, "2");
        assertEquals(2, _parentReviewHelper.findCurrentPage(request));

        request.setParameter(ParentReviewHelper.PARAM_PAGE, "0");
        assertEquals(1, _parentReviewHelper.findCurrentPage(request));

        request.setParameter(ParentReviewHelper.PARAM_PAGE, "TESTING");
        assertEquals(1, _parentReviewHelper.findCurrentPage(request));
    }

    public void testFindFromIndex() throws Exception {
        // make sure from index is zero if no principal
        List<Review> reviews = reviews(false);
        assertEquals(0, _parentReviewHelper.findFromIndex(1, reviews));

        // make sure its 1 if there is a principal
        reviews = reviews(true);
        assertEquals(1, _parentReviewHelper.findFromIndex(1, reviews));
    }

     public void testFindToIndex(){
         List<Review> reviews = reviews(false);
         assertEquals(1, _parentReviewHelper.findToIndex(1, 0, reviews));

         assertEquals(1, _parentReviewHelper.findToIndex(10, 0, reviews));

    }

    public void testHandleLastModifiedDateInModelNoAnything() {
        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNull("Expect no last modified date when there is nothing to calculate it against",
                model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelNoReviews() {
        Date daysAgo_2 = getDateXDaysAgo(2);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_2);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from school when no reviews",
                daysAgo_2, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelReviewsLater() {
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_2);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        Review review = new Review();
        review.setPosted(daysAgo_1);
        review.setPoster(Poster.PARENT);
        review.setStatus(Review.ReviewStatus.PUBLISHED);
        reviews.add(review);
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from review when more recent than school",
                daysAgo_1, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelIgnoresPrincipalReview() {
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_2);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        Review review = new Review();
        review.setPosted(daysAgo_1);
        review.setPoster(Poster.PRINCIPAL);
        review.setStatus(Review.ReviewStatus.PUBLISHED);
        reviews.add(review);
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from school and ignore principal review",
                daysAgo_2, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelIgnoresNonPublishedReview() {
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_2);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        Review review = new Review();
        review.setPosted(daysAgo_1);
        review.setPoster(Poster.PARENT);
        review.setStatus(Review.ReviewStatus.REJECTED);
        reviews.add(review);
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from school and ignore rejected review",
                daysAgo_2, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelTopicalReviewLater() {
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_2);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();
        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setCreated(daysAgo_1);
        review.setWho(Poster.PARENT);
        review.setStatus(Review.ReviewStatus.PUBLISHED.getStatusCode());
        topicalReviews.add(review);

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from topical review when more recent than school",
                daysAgo_1, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelIgnoresNonPublishedTopicalReview() {
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_2);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();
        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setCreated(daysAgo_1);
        review.setWho(Poster.PARENT);
        review.setStatus(Review.ReviewStatus.REJECTED.getStatusCode());
        topicalReviews.add(review);

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from school when topical review is rejected",
                daysAgo_2, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelMultipleReviewsOverallLater() {
        Date daysAgo_3 = getDateXDaysAgo(3);
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_3);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        Review review = new Review();
        review.setPosted(daysAgo_1);
        review.setPoster(Poster.PARENT);
        review.setStatus(Review.ReviewStatus.PUBLISHED);
        reviews.add(review);
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();
        TopicalSchoolReview topicalReview = new TopicalSchoolReview();
        topicalReview.setCreated(daysAgo_2);
        topicalReview.setWho(Poster.PARENT);
        topicalReview.setStatus(Review.ReviewStatus.PUBLISHED.getStatusCode());
        topicalReviews.add(topicalReview);

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from overall review when more recent than school and topical review",
                daysAgo_1, model.get("lastModifiedDate"));
    }

    public void testHandleLastModifiedDateInModelMultipleReviewsTopicalLater() {
        Date daysAgo_3 = getDateXDaysAgo(3);
        Date daysAgo_2 = getDateXDaysAgo(2);
        Date daysAgo_1 = getDateXDaysAgo(1);

        Map<String, Object> model = new HashMap<String, Object>();
        School school = new School();
        school.setModified(daysAgo_3);

        Set<Poster> reviewsBy = new HashSet<Poster>();
        List<Review> reviews = new ArrayList<Review>();
        Review review = new Review();
        review.setPosted(daysAgo_2);
        review.setPoster(Poster.PARENT);
        review.setStatus(Review.ReviewStatus.PUBLISHED);
        reviews.add(review);
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();
        TopicalSchoolReview topicalReview = new TopicalSchoolReview();
        topicalReview.setCreated(daysAgo_1);
        topicalReview.setWho(Poster.PARENT);
        topicalReview.setStatus(Review.ReviewStatus.PUBLISHED.getStatusCode());
        topicalReviews.add(topicalReview);

        _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews, topicalReviews);

        assertNotNull(model.get("lastModifiedDate"));
        assertEquals("Expect last modified date to come from overall review when more recent than school and topical review",
                daysAgo_1, model.get("lastModifiedDate"));
    }

    public void testHandleSortAllReviewsRatingDescending() {
        List<ISchoolReview> reviewsToSort = new ArrayList<ISchoolReview>();

        reviewsToSort.add(getReview(1, CategoryRating.RATING_5, 0));
        reviewsToSort.add(getTopicalReview(2, CategoryRating.RATING_4, 1));
        reviewsToSort.add(getReview(3, CategoryRating.RATING_3, 2));
        reviewsToSort.add(getTopicalReview(4, CategoryRating.RATING_2, 3));
        reviewsToSort.add(getReview(5, CategoryRating.RATING_1, 4));
        reviewsToSort.add(getReview(6, CategoryRating.DECLINE_TO_STATE, 5));
        reviewsToSort.add(getTopicalReview(7, CategoryRating.DECLINE_TO_STATE, 6));

        _parentReviewHelper.handleSortAllReviews("rd", reviewsToSort);

        // first by rating desc, then by date. Ignore rating on topical reviews
        assertEquals(1, reviewsToSort.get(0).getId().intValue());
        assertEquals(3, reviewsToSort.get(1).getId().intValue());
        assertEquals(5, reviewsToSort.get(2).getId().intValue());
        assertEquals(2, reviewsToSort.get(3).getId().intValue());
        assertEquals(4, reviewsToSort.get(4).getId().intValue());
        assertEquals(6, reviewsToSort.get(5).getId().intValue());
        assertEquals(7, reviewsToSort.get(6).getId().intValue());
    }

    public void testHandleSortAllReviewsRatingAscending() {
        List<ISchoolReview> reviewsToSort = new ArrayList<ISchoolReview>();

        reviewsToSort.add(getReview(1, CategoryRating.RATING_5, 0));
        reviewsToSort.add(getTopicalReview(2, CategoryRating.RATING_4, 1));
        reviewsToSort.add(getReview(3, CategoryRating.RATING_3, 2));
        reviewsToSort.add(getTopicalReview(4, CategoryRating.RATING_2, 3));
        reviewsToSort.add(getReview(5, CategoryRating.RATING_1, 4));
        reviewsToSort.add(getReview(6, CategoryRating.DECLINE_TO_STATE, 5));
        reviewsToSort.add(getTopicalReview(7, CategoryRating.DECLINE_TO_STATE, 6));

        _parentReviewHelper.handleSortAllReviews("ra", reviewsToSort);

        // first by rating asc, then by date. Ignore rating on topical reviews
        assertEquals(2, reviewsToSort.get(0).getId().intValue());
        assertEquals(4, reviewsToSort.get(1).getId().intValue());
        assertEquals(6, reviewsToSort.get(2).getId().intValue());
        assertEquals(7, reviewsToSort.get(3).getId().intValue());
        assertEquals(5, reviewsToSort.get(4).getId().intValue());
        assertEquals(3, reviewsToSort.get(5).getId().intValue());
        assertEquals(1, reviewsToSort.get(6).getId().intValue());
    }

    public ISchoolReview getTopicalReview(int id, CategoryRating rating, int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(id);
        review.setRating(rating);
        review.setCreated(cal.getTime());
        review.setWho(Poster.PARENT);
        return review;
    }

    public ISchoolReview getReview(int id, CategoryRating rating, int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        Review review = new Review();
        review.setId(id);
        review.setQuality(rating);
        review.setPosted(cal.getTime());
        review.setPoster(Poster.PARENT);
        return review;
    }

    private Date getDateXDaysAgo(int x) {
        Calendar daysAgo = Calendar.getInstance();
        daysAgo.add(Calendar.DAY_OF_YEAR, -x);
        return daysAgo.getTime();
    }

    private List<Review> reviews(boolean includePrincipal){
        List<Review> reviews = new ArrayList<Review>();
        if (includePrincipal){
            reviews.add(new Review());
            reviews.get(0).setPoster(Poster.PRINCIPAL);
        }
        Review second = new Review();
        reviews.add(second);
        return reviews;
    }
}
