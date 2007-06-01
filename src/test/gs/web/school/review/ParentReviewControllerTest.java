package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import org.easymock.MockControl;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ParentReviewControllerTest extends BaseControllerTestCase {
    private ParentReviewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (ParentReviewController)getApplicationContext().getBean(ParentReviewController.BEAN_ID);
        MockControl mockControl = MockControl.createControl(IReviewDao.class);
        IReviewDao reviewDao = (IReviewDao) mockControl.getMock();

        Ratings ratings = new Ratings();
        ratings.setCount(10);

        reviewDao.findRatingsBySchool(null);
        mockControl.setDefaultReturnValue(ratings);

        reviewDao.getPublishedReviewsBySchool(null);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        List<Review> reviews = new ArrayList<Review>();
        Review r1 = new Review();
        r1.setId(1);
        r1.setQuality(CategoryRating.RATING_1);
        r1.setPosted(df.parse("2002-1-1"));

        Review r2 = new Review();
        r2.setId(2);
        r2.setQuality(CategoryRating.RATING_2);
        r2.setPosted(df.parse("2002-1-2"));

        Review r3 = new Review();
        r3.setId(3);
        r3.setQuality(CategoryRating.RATING_3);
        r3.setPosted(df.parse("2002-1-3"));

        Review r4 = new Review();
        r4.setId(4);
        r4.setQuality(CategoryRating.RATING_4);
        r4.setPosted(df.parse("2002-1-4"));

        Review r5 = new Review();
        r5.setId(5);
        r5.setQuality(CategoryRating.RATING_5);
        r5.setPosted(df.parse("2002-1-5"));

        reviews.add(r5);
        reviews.add(r4);
        reviews.add(r3);
        reviews.add(r2);
        reviews.add(r1);

        mockControl.setDefaultReturnValue(reviews);
        mockControl.replay();

        _controller.setReviewDao(reviewDao);
    }

    public void testHandleRequestSortDateDescDefaultCase() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");

        School school = cmd.getSchool();
        assertNotNull(school);
        assertEquals("Alameda High School", school.getName());
        assertNotNull(cmd.getRatings());

        List<Review> reviews = cmd.getReviews();
        assertNotNull(reviews);
        assertEquals(5, reviews.size());
        assertEquals(new Integer(5), reviews.get(0).getId());
    }

    public void testHandleRequestSortDateAsc() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "da");
        request.setMethod("GET");

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");

        List<Review> reviews = cmd.getReviews();
        assertNotNull(reviews);
        assertEquals(5, reviews.size());
        assertEquals(new Integer(1), reviews.get(0).getId());                
    }

    public void testHandleRequestSortRatingDesc() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "rd");
        request.setMethod("GET");

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");

        List<Review> reviews = cmd.getReviews();
        assertNotNull(reviews);
        assertEquals(5, reviews.size());
        assertEquals("sorted by rating desc should yield id 5 as the first one", new Integer(5), reviews.get(0).getId());
        assertEquals("not a crawler so don't get all ratings on one page",ParentReviewController.MAX_REVIEWS_PER_PAGE, cmd.getMaxReviewsPerPage());
    }

    public void testHandleRequestSortRatingAsc() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");

        List<Review> reviews = cmd.getReviews();
        assertNotNull(reviews);
        assertEquals(5, reviews.size());
        assertEquals("sorted by rating asc yields rating 1 first", new Integer(1), reviews.get(0).getId());
    }

    public void testCrawlerShowsAllReviewsOnPage() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");
        MockSessionContext context = new MockSessionContext();
        context.setCrawler(true);
        context.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, context);

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
        assertTrue("crawler should get all ratings on one page", cmd.getMaxReviewsPerPage() > ParentReviewController.MAX_REVIEWS_PER_PAGE);
    }
}
