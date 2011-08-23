package gs.web.promo;

import gs.web.BaseControllerTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class NbcQuizControllerTest extends BaseControllerTestCase {
    private NbcQuizController _controller;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new NbcQuizController();
    }

    public void testBasics() {
        assertNotNull(_controller);
    }
}
