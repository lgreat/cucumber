package gs.web.school.review;

import static org.junit.Assert.*;

import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Testing the refactored ParentReviewHelper code
 */
public class ParentReviewHelperTest extends BaseTestCase {

    ParentReviewHelper parentReviewHelper = new ParentReviewHelper();

    public void testFindCurrentPage() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter(ParentReviewHelper.PARAM_PAGE, "1");
        assertEquals(1, parentReviewHelper.findCurrentPage(request));

        request.setParameter(ParentReviewHelper.PARAM_PAGE, "2");
        assertEquals(2, parentReviewHelper.findCurrentPage(request));

        request.setParameter(ParentReviewHelper.PARAM_PAGE, "0");
        assertEquals(1, parentReviewHelper.findCurrentPage(request));

        request.setParameter(ParentReviewHelper.PARAM_PAGE, "TESTING");
        assertEquals(1, parentReviewHelper.findCurrentPage(request));
    }

    public void testFindFromIndex() throws Exception {
        // make sure from index is zero if no principal
        List<Review> reviews = reviews(false);
        assertEquals(0, parentReviewHelper.findFromIndex(1, reviews));

        // make sure its 1 if there is a principal
        reviews = reviews(true);
        assertEquals(1, parentReviewHelper.findFromIndex(1, reviews));
    }

     public void testFindToIndex(){
         List<Review> reviews = reviews(false);
         assertEquals(1, parentReviewHelper.findToIndex(1, 0, reviews));

         assertEquals(1, parentReviewHelper.findToIndex(10, 0, reviews));

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
