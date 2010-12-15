package gs.web.compare;

import gs.web.BaseControllerTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareTestScoresController extends BaseControllerTestCase {
    private CompareTestScoresController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareTestScoresController();

        _controller.setSuccessView("success");
    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolBaseStruct.class, _controller.getStruct().getClass());
    }
}
