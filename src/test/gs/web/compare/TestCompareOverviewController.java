package gs.web.compare;

import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareOverviewController extends BaseControllerTestCase {
    private CompareOverviewController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareOverviewController();

        _controller.setSuccessView("success");
    }

//    private void replayAllMocks() {
//        replayMocks();
//    }

//    private void verifyAllMocks() {
//        verifyMocks();
//    }

//    private void resetAllMocks() {
//        resetMocks();
//    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolOverviewStruct.class, _controller.getStruct().getClass());
    }
}