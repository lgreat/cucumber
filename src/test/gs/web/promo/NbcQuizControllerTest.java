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

    private void replayAllMocks() {
        super.replayMocks();
    }

    private void verifyAllMocks() {
        super.verifyMocks();
    }

    public void testParseResponseObjectNoChild() {
        // Test that no child age field results in an exception

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Expect validation error when no parameters supplied");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(NbcQuizController.SaveStatus.ERROR_NO_CHILD_AGE, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void xtestParseResponseObjectTypeConversionFailure() {
        // DELETE THIS TEST IF NO TYPE CONVERSION OCCURS
        // Test that a failure converting a request param into another type is thrown

        // TODO set some fields on request, including one that is typed incorrectly

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Error converting parameter should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            // TODO assert that the save status is the right one
            //assertEquals(NbcQuizController.SaveStatus.??, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseResponseObject() {
        // Test that a valid set of response fields results in a return value

        // set fields on request
        getRequest().setParameter(NbcQuizController.PARAM_CHILD_AGE, "3");
        replayAllMocks();
        Object rval = null;
        try {
            rval = _controller.parseQuizTaken(getRequest());
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            fail("Unexpected exception on valid set of parameters: " + pqte);
        }
        verifyAllMocks();
        assertNotNull("Expect quiz response object when valid fields are provided", rval);
        // TODO assert rval fields match request fields
    }
}
