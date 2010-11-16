package gs.web.compare;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareMapController extends BaseControllerTestCase {
    private CompareMapController _controller;
    private IReviewDao _reviewDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareMapController();

        _reviewDao = createStrictMock(IReviewDao.class);

        _controller.setReviewDao(_reviewDao);
        _controller.setSuccessView("success");
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
        assertEquals(ComparedSchoolMapStruct.class, _controller.getStruct().getClass());
        assertEquals(8, _controller.getPageSize());
    }

    public void testHandleCommunityRating() {
        List<ComparedSchoolBaseStruct> structs = new ArrayList<ComparedSchoolBaseStruct>();
        replayAllMocks();
        _controller.handleCommunityRating(structs);
        verifyAllMocks(); // handles empty case
        resetAllMocks();
        
        ComparedSchoolBaseStruct struct1 = new ComparedSchoolBaseStruct();
        School school1 = new School();
        struct1.setSchool(school1);
        structs.add(struct1);

        expect(_reviewDao.findRatingsBySchool(school1)).andReturn(null);
        replayAllMocks();
        _controller.handleCommunityRating(structs);
        verifyAllMocks();
        assertEquals(0, struct1.getCommunityRating());
        resetAllMocks();

        Ratings ratings1 = new Ratings();
        ratings1.setAvgQuality(4);
        expect(_reviewDao.findRatingsBySchool(school1)).andReturn(ratings1);
        replayAllMocks();
        _controller.handleCommunityRating(structs);
        verifyAllMocks();
        assertEquals(4, struct1.getCommunityRating());
    }

    public void testDetermineCenterOfMap() {
        List<ComparedSchoolBaseStruct> structs = new ArrayList<ComparedSchoolBaseStruct>();
        Map<String, Object> model = new HashMap<String, Object>();

        replayAllMocks();
        _controller.determineCenterOfMap(structs, model);
        verifyAllMocks(); // handles empty case
    }
}
