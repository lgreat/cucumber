package gs.web.promo;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.promo.IQuizDao;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;
import static gs.web.promo.NbcQuizController.*;
import static gs.web.promo.NbcQuizController.SaveStatus.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class NbcQuizControllerTest extends BaseControllerTestCase {
    private NbcQuizController _controller;
    private IQuizDao _quizDao;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new NbcQuizController();

        _quizDao = createStrictMock(IQuizDao.class);
        _controller.setQuizDao(_quizDao);
    }

    public void testBasics() {
        assertNotNull(_controller);
        assertSame(_quizDao, _controller.getQuizDao());
    }

    private void replayAllMocks() {
        super.replayMocks(_quizDao);
    }

    private void verifyAllMocks() {
        super.verifyMocks(_quizDao);
    }

    public void testParseQuizTakenNoChild() {
        // Test that no child age field results in an exception

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Expect validation error when no parameters supplied");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(ERROR_NO_CHILD_AGE, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void xtestParseQuizTakenTypeConversionFailure() {
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

    public void testParseQuizTaken() {
        // Test that a valid set of response fields results in a return value

        setValidFieldsOnRequest();

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

    public void testParseQuizTakenWeirdParameters() {
        // Test that extra parameters are not put into the quiz

        setValidFieldsOnRequest();
        getRequest().setParameter("xq1", "11");
        getRequest().setParameter("q1x", "11");
        getRequest().setParameter("1q1_", "11");
        getRequest().setParameter("totalGarbage", "11");

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
        // assert rval fields do NOT contain extra params
    }

    public void testSaveResponsesOnValidationError() {
        // test what happens when no child age specified
        replayAllMocks();
        NbcQuizController.SaveStatus rval = _controller.saveResponses(getRequest());
        verifyAllMocks();
        assertEquals(ERROR_NO_CHILD_AGE, rval);
    }

    public void testSaveResponses() {
        // test successful save
        setValidFieldsOnRequest();

        // TODO set expectation on dao.save

        replayAllMocks();
        NbcQuizController.SaveStatus rval = _controller.saveResponses(getRequest());
        verifyAllMocks();
        assertEquals(NbcQuizController.SaveStatus.SUCCESS, rval);
    }

    public void xtestSaveResponsesDBError() {
        // test DB error when saving
        setValidFieldsOnRequest();

        // TODO set expectation on dao.save
        expectLastCall().andThrow(new RuntimeException("Mocked error on save"));

        replayAllMocks();
        NbcQuizController.SaveStatus rval = _controller.saveResponses(getRequest());
        verifyAllMocks();
        assertEquals(ERROR_SERVER, rval);
    }

    public void testEnumWriteToJSON() {
        JSONObject o = new JSONObject();
        try {
            SUCCESS.writeToJSON(o);
            assertNotNull(o.get(KEY_JSON_STATUS));
            assertEquals(SUCCESS.getStatus(), o.get(KEY_JSON_STATUS));
            assertNotNull(o.get(KEY_JSON_MESSAGE));
            assertEquals(SUCCESS.getMessage(), o.get(KEY_JSON_MESSAGE));

            o = new JSONObject();
            ERROR_SERVER.writeToJSON(o);
            assertNotNull(o.get(KEY_JSON_STATUS));
            assertEquals(ERROR_SERVER.getStatus(), o.get(KEY_JSON_STATUS));
            assertNotNull(o.get(KEY_JSON_MESSAGE));
            assertEquals(ERROR_SERVER.getMessage(), o.get(KEY_JSON_MESSAGE));
        } catch (JSONException e) {
            fail("Unexpected JSON error: " + e);
        }
    }

    private void setValidFieldsOnRequest() {
        getRequest().setParameter(PARAM_CHILD_AGE, "3");
        getRequest().setParameter(PARAM_PARENT_AGE, "33");
        getRequest().setParameter(PARAM_ZIP, "92130");
        getRequest().setParameter("q1", "1");
        getRequest().setParameter("q2", "2");
        getRequest().setParameter("q3", "3");
    }
}
