package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.SchoolSubtype;
import gs.data.school.review.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.school.AbstractSchoolController;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class ParentReviewControllerTest extends BaseControllerTestCase {
    private ParentReviewController _controller;

    private IReviewDao _reviewDao;

    private void replayAllMocks() {
        replayMocks(_reviewDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_reviewDao);
    }
    
    public void setUp() throws Exception {
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
        r6.setPoster(Poster.PRINCIPAL);

        reviews.add(r6);
        reviews.add(r5);
        reviews.add(r4);
        reviews.add(r3);
        reviews.add(r2);
        reviews.add(r1);

        Map numReviewsBy = new HashMap<Poster,Integer>();

        super.setUp();

        _controller = (ParentReviewController)getApplicationContext().getBean(ParentReviewController.BEAN_ID);

        Ratings ratings = new Ratings();
        ratings.setCount(10);

        _reviewDao = createStrictMock(IReviewDao.class);
        _controller.setReviewDao(_reviewDao);

        expect(_reviewDao.getPublishedReviewsBySchool(isA(School.class), isA(Set.class))).andReturn(reviews);

        expect(_reviewDao.getNumPublishedReviewsBySchool(isA(School.class), isA(Set.class))).andReturn(numReviewsBy);
        
        expect(_reviewDao.findRatingsBySchool(isA(School.class))).andReturn(ratings);

        expect(_reviewDao.countPublishedNonPrincipalReviewsBySchool(isA(School.class), isA(Set.class))).andReturn(new Long(4l));
    }

    private void setUpHelperNonKindercare() {
        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);
        school.setName("Alameda High School");

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        SortedSet<RatingsForYear> ratingsByYear = new TreeSet<RatingsForYear>();
        expect(_reviewDao.findOverallRatingsByYear(school, currentYear, 4)).andReturn(ratingsByYear);

        _request.setAttribute("school", school);
    }

    public void testHandleRequestSortPrincipalDateDescDefaultCase() throws Exception {
        setUpHelperNonKindercare();
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();

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
        setUpHelperNonKindercare();
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "da");
        request.setMethod("GET");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();
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
        setUpHelperNonKindercare();
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "rd");
        request.setMethod("GET");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");

        List<Review> reviews = cmd.getReviews();
        assertNotNull(reviews);
        assertEquals(6, reviews.size());

        assertEquals(new Integer(6), reviews.get(0).getId());
        assertEquals(new Integer(4), reviews.get(1).getId());
        assertEquals(new Integer(5), reviews.get(2).getId());
        assertEquals(new Integer(1), reviews.get(3).getId());
        assertEquals(new Integer(2), reviews.get(4).getId());
        assertEquals(new Integer(3), reviews.get(5).getId());

    }

    public void testHandleRequestSortPrincipalRatingAsc() throws Exception {
        setUpHelperNonKindercare();
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();
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
        setUpHelperNonKindercare();
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");
        MockSessionContext context = new MockSessionContext();
        context.setCrawler(true);
        context.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, context);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
    }

    public void testShowAllParameter() throws Exception {
        setUpHelperNonKindercare();
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setParameter(ParentReviewController.PARAM_SORT_BY, "ra");
        request.setMethod("GET");
        request.setParameter(ParentReviewController.PARAM_VIEW_ALL, "");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
    }

    public void testKindercareLeadGen() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");

        School school = new School();
        school.setPreschoolSubtype(SchoolSubtype.create("kindercare"));
        school.setDatabaseState(State.CA);
        school.setId(1);

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        SortedSet<RatingsForYear> ratingsByYear = new TreeSet<RatingsForYear>();
        expect(_reviewDao.findOverallRatingsByYear(school, currentYear, 4)).andReturn(ratingsByYear);

        request.setAttribute("school", school);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        verifyAllMocks();
        ParentReviewController.ParentReviewCommand cmd =
                (ParentReviewController.ParentReviewCommand)mAndV.getModel().get("cmd");
        assertNotNull(cmd);
        assertNotNull("ParentReviewController should add kindercare attribute", mAndV.getModelMap().get("leadGenModule"));

    }

    public void testGetReviewsFilterSortTracking() throws Exception {
        setUpHelperNonKindercare();
        Set<Poster> reviewsBy = new HashSet<Poster>();
        int numReviews = 0;
        assertEquals("", _controller.getReviewsFilterSortTracking(numReviews, reviewsBy, "dd"));
        numReviews = 1;
        assertEquals("Default", _controller.getReviewsFilterSortTracking(numReviews, reviewsBy, "dd"));
        reviewsBy.add(Poster.PARENT);
        assertEquals("Parent review", _controller.getReviewsFilterSortTracking(numReviews, reviewsBy, "dd"));
        reviewsBy.add(Poster.STUDENT);
        assertEquals("Parent review,Student review", _controller.getReviewsFilterSortTracking(numReviews, reviewsBy, "dd"));
        reviewsBy.add(Poster.TEACHER);
        assertEquals("Parent review,Student review,Teacher review", _controller.getReviewsFilterSortTracking(numReviews, reviewsBy, "dd"));
        assertEquals("Lowest rating first,Parent review,Student review,Teacher review", _controller.getReviewsFilterSortTracking(numReviews, reviewsBy, "ra"));
    }

    public void testResetPageParam() throws Exception {
        String queryString = "id=842&state=PA&sortBy=dd&page=3";
        assertEquals("id=842&state=PA&sortBy=dd&page=1", ParentReviewController.resetPageParam(queryString));

        queryString = "page=4&id=842&state=PA&sortBy=dd&page=3";
        assertEquals("page=1&id=842&state=PA&sortBy=dd&page=1", ParentReviewController.resetPageParam(queryString));

        queryString = "page=4&id=842&state=PA&sortBy=dd";
        assertEquals("page=1&id=842&state=PA&sortBy=dd", ParentReviewController.resetPageParam(queryString));

        queryString = "id=842&state=PA&page=5&sortBy=dd";
        assertEquals("id=842&state=PA&page=1&sortBy=dd", ParentReviewController.resetPageParam(queryString));

        queryString = "id=842&state=PA&sortBy=dd";
        assertEquals("id=842&state=PA&sortBy=dd", ParentReviewController.resetPageParam(queryString));
    }
}
