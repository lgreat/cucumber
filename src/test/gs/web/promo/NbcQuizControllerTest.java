package gs.web.promo;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.promo.IQuizDao;
import gs.data.promo.QuizTaken;
import gs.web.BaseControllerTestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.UnsupportedEncodingException;

import static org.easymock.EasyMock.*;
import static gs.web.promo.NbcQuizController.*;
import static gs.web.promo.NbcQuizController.SaveStatus.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class NbcQuizControllerTest extends BaseControllerTestCase {
    private NbcQuizController _controller;
    private IQuizDao _quizDao;
    private String _type;
    private String _cacheKey;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new NbcQuizController();

        _quizDao = createStrictMock(IQuizDao.class);
        _controller.setQuizDao(_quizDao);
        _controller.setCacheAgeMillis(60000L); // long enough to not be an issue for unit tests
        _controller.setQuizId(1L);

        _type="1";
        _cacheKey = CACHE_KEY_PREFIX + _type;
    }

    public void testBasics() {
        assertNotNull(_controller);
        assertSame(_quizDao, _controller.getQuizDao());
        assertEquals(60000L, _controller.getCacheAgeMillis());
        assertEquals(1L, _controller.getQuizId());
    }

    private void replayAllMocks() {
        super.replayMocks(_quizDao);
    }

    private void verifyAllMocks() {
        super.verifyMocks(_quizDao);
    }

    public void testAnswerRegEx() {
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("1bd1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("2bd1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("3bd1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("1bd2").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("3bd5").matches());

        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("1m1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("2m1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("3m1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("1m2").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("3m5").matches());

        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("1a1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("2a1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("3a1").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("1a2").matches());
        assertTrue(NbcQuizController.PATTERN_PARAM_ANSWER.matcher("3a5").matches());
    }

    public void testParseQuizTakenNoType() {
        // Test that no type field results in an exception

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Expect validation error when no parameters supplied");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(ERROR_NO_AGE_CATEGORY, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenNoChildAge() {
        setValidStaticFieldsOnRequest();
        getRequest().removeParameter("childAge");

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Expect validation error when no childAge parameter supplied");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(ERROR_NO_CHILD_AGE, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenNoResponses() {
        // Test that no question responses results in an exception
        setValidStaticFieldsOnRequest();

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Expect validation error when no parameters supplied");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            pqte.toString();
            assertEquals(ERROR_TOO_FEW_RESPONSES, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenScoreOutOfRange() {
        setValidStaticFieldsOnRequest();
        getRequest().setParameter("1bd", "0.0");
        getRequest().setParameter("1a", "66.7");
        getRequest().setParameter("1m", "100.1");

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Out of range score parameter should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(SaveStatus.ERROR_INVALID_SCORE, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenScoreNonNumeric() {
        setValidStaticFieldsOnRequest();
        getRequest().setParameter("1bd", "0.0");
        getRequest().setParameter("1a", "66.7");
        getRequest().setParameter("1m", "ninety-nine");

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Out of range score parameter should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(SaveStatus.ERROR_INVALID_SCORE, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenTooFewScores() {
        setValidStaticFieldsOnRequest();
        getRequest().setParameter("1bd1", "1");
        getRequest().setParameter("1a1", "1");
        getRequest().setParameter("1m1", "1");
        getRequest().setParameter("1bd", "1");
        getRequest().setParameter("1m", "1");
        // missing 1a

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("Only 2  scores provided should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(SaveStatus.ERROR_TOO_FEW_SCORES, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenNoScores() {
        setValidStaticFieldsOnRequest();
        getRequest().setParameter("1bd1", "1");
        getRequest().setParameter("1a1", "1");
        getRequest().setParameter("1m1", "1");
        // missing 1a

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("No scores provided should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(SaveStatus.ERROR_TOO_FEW_SCORES, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenTypeMismatchQuestion() {
        setValidStaticFieldsOnRequest();
        getRequest().setParameter("1bd1", "1");
        getRequest().setParameter("1a1", "1");
        getRequest().setParameter("2m1", "1"); // wrong age type
        getRequest().setParameter("1bd", "0.0");
        getRequest().setParameter("1a", "66.7");
        getRequest().setParameter("1m", "100.0");

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("No scores provided should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(SaveStatus.ERROR_AGE_CATEGORY_MISMATCH, pqte.getSaveStatus());
        }
        verifyAllMocks();
    }

    public void testParseQuizTakenTypeMismatchScore() {
        setValidStaticFieldsOnRequest();
        getRequest().setParameter("1bd1", "1");
        getRequest().setParameter("1a1", "1");
        getRequest().setParameter("1m1", "1");
        getRequest().setParameter("1bd", "0.0");
        getRequest().setParameter("2a", "66.7"); // wrong age type
        getRequest().setParameter("1m", "100.0");

        replayAllMocks();
        try {
            _controller.parseQuizTaken(getRequest());
            fail("No scores provided should throw exception");
        } catch (NbcQuizController.ParseQuizTakenException pqte) {
            assertEquals(SaveStatus.ERROR_AGE_CATEGORY_MISMATCH, pqte.getSaveStatus());
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
        assertEquals(ERROR_NO_AGE_CATEGORY, rval);
    }

    public void testSaveResponses() {
        // test successful save
        setValidFieldsOnRequest();

        _quizDao.save(isA(QuizTaken.class));

        replayAllMocks();
        NbcQuizController.SaveStatus rval = _controller.saveResponses(getRequest());
        verifyAllMocks();
        assertEquals(SUCCESS, rval);
    }

    public void testSaveResponsesDBError() {
        // test DB error when saving
        setValidFieldsOnRequest();

        _quizDao.save(isA(QuizTaken.class));
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
            ERROR_SERVER.toString();
            assertNotNull(o.get(KEY_JSON_STATUS));
            assertEquals(ERROR_SERVER.getStatus(), o.get(KEY_JSON_STATUS));
            assertNotNull(o.get(KEY_JSON_MESSAGE));
            assertEquals(ERROR_SERVER.getMessage(), o.get(KEY_JSON_MESSAGE));
        } catch (JSONException e) {
            fail("Unexpected JSON error: " + e);
        }
    }

    public void testIsExpiredOrNull() {
        Element elem = null;
        assertTrue(_controller.isExpiredOrNull(elem));
        elem = new Element("key", null);
        assertTrue(_controller.isExpiredOrNull(elem));
        elem = new Element("key", "value");
        _controller.setCacheAgeMillis(5000L);
        assertFalse(_controller.isExpiredOrNull(elem));
        _controller.setCacheAgeMillis(-5000L); // cache expires in the future
        assertTrue(_controller.isExpiredOrNull(elem));
        _controller.setCacheAgeMillis(5000L);
        assertFalse(_controller.isExpiredOrNull(elem));
    }

//    public void testFetchAggregateFromCacheMiss() {
//        Cache cache = CacheManager.create().getCache(CACHE_NAME);
//        assertNotNull("Expect ehcache to be configured", cache);
//        int cacheMisses = cache.getStatistics().getCacheMisses();
//        assertNull("Did not expect any cached items in unit test", cache.get(CACHE_KEY));
//        assertEquals(cacheMisses+1, cache.getStatistics().getCacheMisses());
//
//        JSONObject aggregate = _controller.fetchAggregateFromCache();
//        assertNull(aggregate);
//        assertEquals("Expect a cache miss from controller method", cacheMisses+2, cache.getStatistics().getCacheMisses());
//    }

    public void testFetchAggregateFromCacheHit() throws JSONException {
        Cache cache = CacheManager.create().getCache(CACHE_NAME);
        assertNotNull("Expect ehcache to be configured", cache);
        int cacheMisses = cache.getStatistics().getCacheMisses();
        int cacheHits = cache.getStatistics().getCacheHits();

        cache.put(new Element(_cacheKey, "{\"key\":\"value\"}"));
        try {
            assertNotNull(cache.get(_cacheKey));
            assertEquals("Expect a cache hit from test method", cacheHits+1, cache.getStatistics().getCacheHits());

            replayAllMocks();
            JSONObject aggregate = _controller.getAggregate(_type);
            verifyAllMocks();
            assertNotNull(aggregate);
            assertEquals("Expect no cache misses from controller method", cacheMisses, cache.getStatistics().getCacheMisses());
            assertEquals("Expect a cache hit from controller method", cacheHits+2, cache.getStatistics().getCacheHits());
            assertEquals("value", aggregate.getString("key"));
        } finally {
            cache.remove(_cacheKey); // clean up
        }
    }

    public void testFetchAggregateFromCacheErrorConvertingToJSON() throws JSONException {
        Cache cache = CacheManager.create().getCache(CACHE_NAME);
        assertNotNull("Expect ehcache to be configured", cache);
        int cacheMisses = cache.getStatistics().getCacheMisses();
        int cacheHits = cache.getStatistics().getCacheHits();

        cache.put(new Element(_cacheKey, "{This is not valid JSON}"));
        try {
            assertNotNull(cache.get(_cacheKey));
            assertEquals("Expect a cache hit from test method", cacheHits+1, cache.getStatistics().getCacheHits());

            replayAllMocks();
            JSONObject aggregate = _controller.getAggregate(_type);
            verifyAllMocks();
            assertNull(aggregate);
            assertEquals("Expect no cache misses from controller method", cacheMisses, cache.getStatistics().getCacheMisses());
            assertEquals("Expect a cache hit from controller method", cacheHits+2, cache.getStatistics().getCacheHits());
            assertNull("Expect invalid value to have been cleared from cache", cache.get(_cacheKey));
        } finally {
            cache.remove(_cacheKey); // clean up
        }
    }

    public void testCacheAggregate() throws JSONException, UnsupportedEncodingException {
        Cache cache = CacheManager.create().getCache(CACHE_NAME);
        assertNotNull("Expect ehcache to be configured", cache);
        assertNull("Expect no cached items when this unit test begins", cache.get(_cacheKey));

        JSONObject aggregate = new JSONObject("{key:\"value\"}", "UTF-8");
        replayAllMocks();
        _controller.cacheAggregate(aggregate, _type);
        verifyAllMocks();
        try {
            assertNotNull("Expect aggregate to have been cached by controller", cache.get(_cacheKey));
            assertEquals("Expect cached value to match toString() on json object",
                         aggregate.toString(), cache.get(_cacheKey).getObjectValue().toString());
        } finally {
            cache.remove(_cacheKey); // clean up
        }
    }

    private void setValidFieldsOnRequest() {
        setValidStaticFieldsOnRequest();
        setValidDynamicFieldsOnRequest();
    }
    private void setValidStaticFieldsOnRequest() {
        getRequest().setParameter(PARAM_CHILD_AGE_CATEGORY, "1");
        getRequest().setParameter(PARAM_CHILD_AGE, "0-3");
        getRequest().setParameter(PARAM_PARENT_AGE, "33");
        getRequest().setParameter(PARAM_ZIP, "92130");
        getRequest().setParameter(PARAM_GENDER, "m");
    }
    private void setValidDynamicFieldsOnRequest() {
        getRequest().setParameter("1bd1", "a");
        getRequest().setParameter("1bd2", "b");
        getRequest().setParameter("1bd3", "c");
        getRequest().setParameter("1bd", "0.0");
        getRequest().setParameter("1a", "66.7");
        getRequest().setParameter("1m", "100.0");
    }
}
