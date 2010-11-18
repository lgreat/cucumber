package gs.web.compare;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareRatingsController extends BaseControllerTestCase {
    private CompareRatingsController _controller;
    private IReviewDao _reviewDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareRatingsController();

        _reviewDao = createStrictMock(IReviewDao.class);

        _controller.setSuccessView("success");
        _controller.setReviewDao(_reviewDao);
    }

    private void replayAllMocks() {
        replayMocks(_reviewDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_reviewDao);
    }

    private void resetAllMocks() {
        resetMocks(_reviewDao);
    }

    public void testBasics() {
        assertSame(_reviewDao, _controller.getReviewDao());
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolRatingsStruct.class, _controller.getStruct().getClass());
    }

    public void testEmptyList() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(),
                                         new ArrayList<ComparedSchoolBaseStruct>(), model);
        verifyAllMocks();
        assertEquals(CompareRatingsController.TAB_NAME, model.get(MODEL_TAB));
    }


    public void testHandleCommunityRating() {
        List<ComparedSchoolBaseStruct> schools = new ArrayList<ComparedSchoolBaseStruct>();

        replayAllMocks();
        _controller.handleCommunityRating(schools);
        verifyAllMocks();

        // ok

        School school1 = new School();
        ComparedSchoolRatingsStruct struct1 = new ComparedSchoolRatingsStruct();
        struct1.setSchool(school1);
        schools.add(struct1);
        Ratings ratings1 = new Ratings();

        resetAllMocks();

        expect(_reviewDao.findRatingsBySchool(school1)).andReturn(ratings1);
        replayAllMocks();
        _controller.handleCommunityRating(schools);
        verifyAllMocks();

        assertEquals(0, struct1.getCommunityRating());
        assertEquals(0, struct1.getNumRatings());
        assertEquals(0, struct1.getParentRating());
        assertEquals(0, struct1.getPrincipalRating());
        assertEquals(0, struct1.getTeacherRating());

        resetAllMocks();

        ratings1.setNumberOfReviews(5L);
        ratings1.setAvgQuality(5);
        ratings1.setAvgParents(4);
        ratings1.setAvgPrincipal(3);
        ratings1.setAvgTeachers(2);
        expect(_reviewDao.findRatingsBySchool(school1)).andReturn(ratings1);
        replayAllMocks();
        _controller.handleCommunityRating(schools);
        verifyAllMocks();

        assertEquals(5, struct1.getCommunityRating());
        assertEquals(5, struct1.getNumRatings());
        assertEquals(4, struct1.getParentRating());
        assertEquals(3, struct1.getPrincipalRating());
        assertEquals(2, struct1.getTeacherRating());
    }
}
