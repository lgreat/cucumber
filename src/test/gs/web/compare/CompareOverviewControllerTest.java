package gs.web.compare;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareOverviewControllerTest extends BaseControllerTestCase {
    private CompareOverviewController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareOverviewController();

        _controller.setSuccessView("success");
    }

    private void replayAllMocks() {
        replayMocks();
    }

    private void verifyAllMocks() {
        verifyMocks();
    }

//    private void resetAllMocks() {
//        resetMocks();
//    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolOverviewStruct.class, _controller.getStruct().getClass());
    }

    public void testEmptyList() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(),
                                         new ArrayList<ComparedSchoolBaseStruct>(), model);
        verifyAllMocks();
        assertEquals(CompareOverviewController.TAB_NAME, model.get(MODEL_TAB));
    }
}