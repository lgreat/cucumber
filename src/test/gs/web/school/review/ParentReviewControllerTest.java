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
        r1.setQuality(CategoryRating.RATING_3);
        r1.setPosted(df.parse("2002-1-1"));

        Review r2 = new Review();
        r2.setId(2);
        r2.setQuality(CategoryRating.RATING_2);
        r2.setPosted(df.parse("2002-1-2"));

        Review r3 = new Review();
        r3.setId(3);
        r3.setQuality(CategoryRating.RATING_1);
        r3.setPosted(df.parse("2002-1-3"));

        Review r4 = new Review();
        r4.setId(4);
        r4.setQuality(CategoryRating.RATING_5);
        r4.setPosted(df.parse("2002-1-4"));

        Review r5 = new Review();
        r5.setId(5);
        r5.setQuality(CategoryRating.RATING_4);
        r5.setPosted(df.parse("2002-1-5"));

        //this review is oldest by date but it was submitted by a principal
        //so it should be sorted first always
        Review r6 = new Review();
        r6.setId(6);
        r6.setQuality(CategoryRating.RATING_3);
        r6.setPosted(df.parse("2000-1-1"));
        r6.setWho("principal");

        reviews.add(r6);
        reviews.add(r5);
        reviews.add(r4);
        reviews.add(r3);
        reviews.add(r2);
        reviews.add(r1);

        mockControl.setDefaultReturnValue(reviews);
        mockControl.replay();

        _controller.setReviewDao(reviewDao);
        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);
        school.setName("Alameda High School");
        _request.setAttribute("school", school);
    }

    public void testHandleRequestSortPrincipalDateDescDefaultCase() throws Exception {
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
        assertEquals(6, reviews.size());
        assertEquals(new Integer(6), reviews.get(0).getId());
        assertEquals(new Integer(5), reviews.get(1).getId());
        assertEquals(new Integer(4), reviews.get(2).getId());
        assertEquals(new Integer(3), reviews.get(3).getId());
        assertEquals(new Integer(2), reviews.get(4).getId());
        assertEquals(new Integer(1), reviews.get(5).getId());
    }

    public void testHandleRequestSortPrincipalDateAsc() throws Exception {
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
        assertEquals(6, reviews.size());
        assertEquals(new Integer(6), reviews.get(0).getId());
        assertEquals(new Integer(1), reviews.get(1).getId());
        assertEquals(new Integer(2), reviews.get(2).getId());
        assertEquals(new Integer(3), reviews.get(3).getId());
        assertEquals(new Integer(4), reviews.get(4).getId());
        assertEquals(new Integer(5), reviews.get(5).getId());

    }

    public void testHandleRequestSortPrincipalRatingDesc() throws Exception {
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
        assertEquals(6, reviews.size());
        assertEquals("not a crawler so don't get all ratings on one page",ParentReviewController.MAX_REVIEWS_PER_PAGE, cmd.getMaxReviewsPerPage());

        assertEquals(new Integer(6), reviews.get(0).getId());
        assertEquals(new Integer(4), reviews.get(1).getId());
        assertEquals(new Integer(5), reviews.get(2).getId());
        assertEquals(new Integer(1), reviews.get(3).getId());
        assertEquals(new Integer(2), reviews.get(4).getId());
        assertEquals(new Integer(3), reviews.get(5).getId());

    }

    public void testHandleRequestSortPrincipalRatingAsc() throws Exception {
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
        assertEquals(6, reviews.size());
        assertEquals(new Integer(6), reviews.get(0).getId());
        assertEquals(new Integer(3), reviews.get(1).getId());
        assertEquals(new Integer(2), reviews.get(2).getId());
        assertEquals(new Integer(1), reviews.get(3).getId());
        assertEquals(new Integer(5), reviews.get(4).getId());
        assertEquals(new Integer(4), reviews.get(5).getId());

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

    public void testShowAllParameter() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");
        request.setParameter(ParentReviewController.PARAM_VIEW_ALL, "");

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
        assertEquals("show all on one page", ParentReviewController.MAX_REVIEWS_PER_PAGE, cmd.getMaxReviewsPerPage());        
    }

    public void testShowParentReviewModuleFormOnlyAppearsOnFirstPage() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");
        request.setParameter(ParentReviewController.PARAM_PAGER_OFFSET, "1");
        MockSessionContext context = new MockSessionContext();
        context.setCrawler(true);
        context.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, context);

        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
        assertFalse("not on first page since offset is not zero so don't show form", cmd.isShowParentReviewForm());

        request.setParameter(ParentReviewController.PARAM_PAGER_OFFSET, "0");
        mAndV = _controller.handleRequest(request, getResponse());
        cmd = (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
        assertTrue("on first page since offset is zero so show form", cmd.isShowParentReviewForm());

        request.removeParameter(ParentReviewController.PARAM_PAGER_OFFSET);
        mAndV = _controller.handleRequest(request, getResponse());
        cmd = (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
        assertTrue("on first page since offset is not set so show form", cmd.isShowParentReviewForm());
    }
}
