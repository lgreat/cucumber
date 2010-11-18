package gs.web.compare;

import gs.data.school.review.IReviewDao;
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

//    private void resetAllMocks() {
//        resetMocks(_reviewDao);
//    }

    public void testBasics() {
        assertSame(_reviewDao, _controller.getReviewDao());
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolMapStruct.class, _controller.getStruct().getClass());
        assertEquals(8, _controller.getPageSize());
    }

    public void testEmptyList() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(),
                                         new ArrayList<ComparedSchoolBaseStruct>(), model);
        verifyAllMocks();
        assertEquals(CompareMapController.TAB_NAME, model.get(MODEL_TAB));
    }

    public void testDetermineCenterOfMap() {
        List<ComparedSchoolBaseStruct> structs = new ArrayList<ComparedSchoolBaseStruct>();
        Map<String, Object> model = new HashMap<String, Object>();

        replayAllMocks();
        _controller.determineCenterOfMap(structs, model);
        verifyAllMocks(); // handles empty case
    }
}