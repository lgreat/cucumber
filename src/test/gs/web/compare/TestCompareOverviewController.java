package gs.web.compare;
import gs.web.BaseControllerTestCase;


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

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolOverviewStruct.class, _controller.getStruct().getClass());
    }
}