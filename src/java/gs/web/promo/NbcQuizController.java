package gs.web.promo;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.promo.IQuizDao;
import gs.data.promo.QuizResponse;
import gs.data.promo.QuizTaken;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Controller for 2011 NBC Education Nation parent quiz
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class NbcQuizController extends AbstractController implements ReadWriteController {
    protected static final Log _log = LogFactory.getLog(NbcQuizController.class);

    protected enum SaveStatus {
        SUCCESS ("Success", "Success"),
        ERROR_NO_CHILD_AGE ("Error", "Child age must be provided -- parameter '" + PARAM_CHILD_AGE + "' missing or empty"),
        ERROR_NO_AGE_CATEGORY ("Error", "Child age category must be provided -- parameter '" + PARAM_CHILD_AGE_CATEGORY + "' missing or empty"),
        ERROR_AGE_CATEGORY_MISMATCH ("Error", "Child age category mismatch! Parameter '" + PARAM_CHILD_AGE_CATEGORY +
                "' must match the first character of every response and score parameter"),
        ERROR_TOO_FEW_RESPONSES ("Error", "Not enough responses provided: need at least " + MIN_NUM_RESPONSES +
                " matching the regex " + PATTERN_PARAM_ANSWER.pattern()),
        ERROR_TOO_FEW_SCORES ("Error", "Not enough scores provided: need at least " + MIN_NUM_SCORES +
                " matching the regex " + PATTERN_PARAM_SCORE.pattern()),
        ERROR_INVALID_SCORE ("Error", "Scores must be numeric values in the range 0.0-100.0"),
        ERROR_SERVER ("Error", "Server error");

        private final String _status;
        private final String _message;
        SaveStatus(String status, String message) {
            _status = status;
            _message = message;
        }
        public void writeToJSON(JSONObject o) throws JSONException {
            o.put(KEY_JSON_STATUS, _status);
            o.put(KEY_JSON_MESSAGE, _message);
        }
        public String toString() {
            return "SaveStatus{" +_status + ":" + _message + "}";
        }
        public String getStatus()  {return _status;}
        public String getMessage() {return _message;}
    }

//    protected static final String CACHE_NAME = "gs.web.promo.NbcQuizController.Aggregate";
//    protected static final String CACHE_KEY_PREFIX = "aggregate.json.string.";
//    protected static final ReentrantLock DB_LOCK = new ReentrantLock();
//    protected static final String[] AGE_CATEGORIES = {"1", "2", "3"};

    public static final String PARAM_CHILD_AGE = "childAge";
    public static final String PARAM_PARENT_AGE = "parentAge";
    public static final String PARAM_ZIP = "zip";
    public static final String PARAM_GENDER = "gender";
    public static final String PARAM_CHILD_AGE_CATEGORY = "type";
//    public static final String PARAM_REFRESH_CACHE = "refresh";
    public static final Pattern PATTERN_PARAM_ANSWER = Pattern.compile("^\\d(bd|m|a)\\d+$");
    public static final Pattern PATTERN_PARAM_SCORE = Pattern.compile("^\\d(bd|m|a)$");
    public static final String KEY_JSON_STATUS = "status";
    public static final String KEY_JSON_MESSAGE = "message";
//    public static final String KEY_JSON_AGGREGATE = "aggregate";
//    public static final String KEY_JSON_BRAIN_DEVELOPMENT = "averageBrain";
//    public static final String KEY_JSON_MOTIVATION = "averageMotivation";
//    public static final String KEY_JSON_ACADEMICS = "averageAcademics";
//    public static final String KEY_JSON_COUNT = "count";
    /** Minimum number of quiz responses to merit a save.*/
    public static final int MIN_NUM_RESPONSES = 1;
    /** Minimum number of scores. 1 for each of the 3 categories */
    public static final int MIN_NUM_SCORES = 3;

    /** Age of cache in ms */
//    private long _cacheAgeMillis = 60000L; // default value, overridden by spring configuration
    private long _quizId = 1L; // default value, overridden by spring configuration
    private IQuizDao _quizDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        if (StringUtils.equalsIgnoreCase("post", request.getMethod())) {
            submit(request, response);
//        } else if (StringUtils.equalsIgnoreCase("get", request.getMethod())) {
//            if (StringUtils.isNotBlank(request.getParameter(PARAM_REFRESH_CACHE))) {
//                if (DB_LOCK.tryLock()) {
//                    // This code can only reached by a single thread at a time. Other threads get false from tryLock
//                    try {
//                        for (String ageCategory: AGE_CATEGORIES) {
//                            JSONObject aggregate = getAggregateFromDB(ageCategory);
//                            cacheAggregate(aggregate, ageCategory);
//                        }
//                    } finally {
//                        DB_LOCK.unlock();
//                    }
//                }
//                response.getWriter().write("1");
//            }
        }
        return null;
    }

    /**
     * saveStatus = saveResponse
     * getAggregate
     * writeOut(saveStatus, aggregate)
     *
     * Contract on response status:
     * 200 - output includes valid aggregate data, status field, and status message
     * 500 - no guaranteed output
     */
    public void submit(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        try {
//            long startTime;
//            startTime = System.currentTimeMillis();
            SaveStatus saveStatus = saveResponses(request); // should not throw or return null
//            logDuration(System.currentTimeMillis() - startTime, "Parsing and saving response");
//            JSONObject aggregateData = getAggregate(request.getParameter(PARAM_CHILD_AGE_CATEGORY)); // should not throw
//            if (aggregateData != null) {
                // combine aggregateData with saveStatus and print out
                JSONObject output = new JSONObject();
//                output.put(KEY_JSON_AGGREGATE, aggregateData);
                saveStatus.writeToJSON(output);
//                addCountToOutput(output);
            if (saveStatus == SaveStatus.ERROR_SERVER) {
                response.setStatus(500);
            } else if (saveStatus != SaveStatus.SUCCESS) {
                response.setStatus(400);
            }
                output.write(response.getWriter()); // throws on error writing to response
//            } else {
//                no aggregate means return 500
//                response.setStatus(500);
//            }
        } catch (Exception e) {
            // there was a failure writing out. Return a 500
            response.setStatus(500);
            _log.error("Error writing to response: " + e, e);
        }
    }

//    protected void addCountToOutput(JSONObject output) {
//        try {
//            output.put(KEY_JSON_COUNT, _quizDao.getCountQuizTaken());
//        } catch (Exception e) {
//            _log.error("Error adding count(quiz_taken) to output: " + e, e);
//        }
//    }

    /**
     * Parse request params into a QuizTaken and save it.
     *
     * @param request HttpServletRequest containing quiz response
     * @return SaveStatus enum indicating success or error, never null.
     */
    public SaveStatus saveResponses(HttpServletRequest request) {
        try {
            QuizTaken quizTaken = parseQuizTaken(request);
            _quizDao.save(quizTaken);
            return SaveStatus.SUCCESS;
        } catch (ParseQuizTakenException pqte) {
            _log.warn("Error parsing quiz response: " + pqte.toString());
            return pqte.getSaveStatus();
        } catch (Exception e) {
            // could be thrown by _dao.save
            // In which case there is no meaningful message we can communicate to view, so just log it
            _log.error("Unexpected error saving quiz taken: " + e, e);
            return SaveStatus.ERROR_SERVER;
        }
    }

    protected void addQuizResponse(QuizTaken quizTaken, String key, String value) {
        if (value != null) {
            QuizResponse response = new QuizResponse();
            response.setQuestionKey(key);
            response.setValue(value);
            response.setQuizTaken(quizTaken);
            quizTaken.getQuizResponses().add(response);
        } else {
            _log.warn("Question param " + key + " has null value");
        }
    }

//    protected void addQuizScore(QuizTaken quizTaken, String category, double value) {
//        QuizScore score = new QuizScore();
//        score.setCategory(category);
//        score.setScore(value);
//        score.setQuizTaken(quizTaken);
//        quizTaken.getQuizScores().add(score);
//    }

    /**
     * parse static parameters
     * parse dynamic parameters
     * return populated hibernate QuizTaken object
     *
     * @param request HttpServletRequest containing quiz taken data
     * @return QuizTaken object containing quiz response, never null
     * @throws ParseQuizTakenException on any error constructing the QuizTaken object
     */
    protected QuizTaken parseQuizTaken(HttpServletRequest request) throws ParseQuizTakenException {
        QuizTaken quizTaken = new QuizTaken();
        quizTaken.setQuizId(_quizId);
        // parse static parameters
        String ageCategory = request.getParameter(PARAM_CHILD_AGE_CATEGORY);
        if (StringUtils.isBlank(ageCategory)) {
            throw new ParseQuizTakenException(SaveStatus.ERROR_NO_AGE_CATEGORY);
        }
        String childAge = request.getParameter(PARAM_CHILD_AGE);
        if (StringUtils.isBlank(childAge)) {
            throw new ParseQuizTakenException(SaveStatus.ERROR_NO_CHILD_AGE);
        }
        addQuizResponse(quizTaken, PARAM_CHILD_AGE, childAge);
        addQuizResponse(quizTaken, PARAM_PARENT_AGE, request.getParameter(PARAM_PARENT_AGE));
        addQuizResponse(quizTaken, PARAM_ZIP, request.getParameter(PARAM_ZIP));
        addQuizResponse(quizTaken, PARAM_GENDER, request.getParameter(PARAM_GENDER));

        // parse dynamic parameters
        Enumeration paramNames = request.getParameterNames();
        int numScores = 0;
        int numResponses = 0;
        while (paramNames.hasMoreElements()) {
            String questionKey = String.valueOf(paramNames.nextElement());
            String questionValue = request.getParameter(questionKey);
            if (PATTERN_PARAM_ANSWER.matcher(questionKey).matches()) {
                if (!StringUtils.equals(ageCategory, questionKey.substring(0, 1))) {
                    // age category must not vary
                    throw new ParseQuizTakenException(SaveStatus.ERROR_AGE_CATEGORY_MISMATCH);
                }
                addQuizResponse(quizTaken, questionKey, questionValue);
                numResponses++;
            } else if (PATTERN_PARAM_SCORE.matcher(questionKey).matches()) {
                // for now, scores are identical to quiz responses
                if (!StringUtils.equals(ageCategory, questionKey.substring(0, 1))) {
                    // age category must not vary
                    throw new ParseQuizTakenException(SaveStatus.ERROR_AGE_CATEGORY_MISMATCH);
                }
                try {
                    Double parsedScore = Double.parseDouble(questionValue);
                    if (parsedScore >= 0.0D && parsedScore <= 100.0D) {
//                        addQuizScore(quizTaken, questionKey, parsedScore);
                        addQuizResponse(quizTaken, questionKey, questionValue);
                        numScores++;
                    } else {
                        throw new ParseQuizTakenException(SaveStatus.ERROR_INVALID_SCORE);
                    }
                } catch (NumberFormatException nfe) {
                    throw new ParseQuizTakenException(SaveStatus.ERROR_INVALID_SCORE);
                }
            }
        }
        if (numResponses < MIN_NUM_RESPONSES) {
            throw new ParseQuizTakenException(SaveStatus.ERROR_TOO_FEW_RESPONSES);
        }
        if (numScores < MIN_NUM_SCORES) {
            throw new ParseQuizTakenException(SaveStatus.ERROR_TOO_FEW_SCORES);
        }
        return quizTaken;
    }

    /**
     * Fetches aggregate data from cache or database
     * Converts to JSON
     *
     * @return JSONObject describing aggregate data, or null on error
     */
//    protected JSONObject getAggregate(String ageCategory) {
//        if (StringUtils.isEmpty(ageCategory)) {
//            _log.error("Must supply a '" + PARAM_CHILD_AGE_CATEGORY + "' param to calculate aggregate");
//            return null;
//        }
//        JSONObject aggregate = null;
//        try {
//            Cache cache = getCache();
//            Element cacheElement = cache.get(CACHE_KEY_PREFIX + ageCategory);
//            // if there is no cache or the cache is expired, attempt to get a concurrency lock to
//            // rebuild the aggregate from the DB
//            if (isExpiredOrNull(cacheElement) && DB_LOCK.tryLock()) {
//                // This code can only reached by a single thread at a time. Other threads get false from tryLock
//                try {
//                    aggregate = getAggregateFromDB(ageCategory);
//                    cacheAggregate(aggregate, ageCategory);
//                } finally {
//                    DB_LOCK.unlock();
//                }
//            } else if (cacheElement != null && cacheElement.getObjectValue() != null) {
//                // Either the cache is fresh or another thread is busy rebuilding the aggregate from the DB.
//                // Either way, let's use the currently cached version if available.
//                try {
//                    aggregate = new JSONObject(cacheElement.getObjectValue().toString(), "UTF-8");
//                } catch (JSONException je) {
//                    _log.error("Error converting cache string to JSONObject. Clearing cache", je);
//                    cache.remove(CACHE_KEY_PREFIX + ageCategory);
//                }
//            } else {
//                // There is no cache and another thread is busy building it
//                // fall through and return null since we have nothing else
//                _log.warn("Cache is empty, unable to get aggregate data");
//            }
//        } catch (Exception e) {
//            _log.error("Error constructing aggregate data: " + e, e);
//        }
//        return aggregate;
//    }

    /**
     * Retrieve the aggregate data from the DB and store it in the cache.
     * @return Fresh JSONObject constructed from the DB
     */
//    protected JSONObject getAggregateFromDB(String parentType) throws JSONException {
//        long startTime = System.currentTimeMillis();
//        Map<String, Number> averages = _quizDao.getAverageScoresAndCount(parentType);
//        Double avgBd = (Double) averages.get("bd");
//        Double avgM = (Double) averages.get("m");
//        Double avgA = (Double) averages.get("a");
//        DecimalFormat decimalFormat = new DecimalFormat("##0.0");
//        JSONObject aggregate = new JSONObject();
//        aggregate.put(KEY_JSON_BRAIN_DEVELOPMENT, decimalFormat.format((avgBd != null)?avgBd:0D));
//        aggregate.put(KEY_JSON_MOTIVATION, decimalFormat.format((avgM != null)?avgM:0D));
//        aggregate.put(KEY_JSON_ACADEMICS, decimalFormat.format((avgA != null)?avgA:0D));
//        logDuration(System.currentTimeMillis() - startTime, "Fetching aggregate from DB for type '" + parentType + "'");
//        return aggregate;
//    }

    /**
     * Returns true if the element is null or if its creationTime is more than _cacheAgeMillis ago
     */
//    protected boolean isExpiredOrNull(Element cacheElement) {
//        return cacheElement == null
//                || cacheElement.getObjectValue() == null
//                || cacheElement.getCreationTime() < (System.currentTimeMillis() - _cacheAgeMillis);
//    }

//    protected static Cache getCache() {
//        return CacheManager.create().getCache(CACHE_NAME);
//    }

//    protected void cacheAggregate(JSONObject aggregate, String parentType) {
//        try {
//            Element cacheElement = new Element(CACHE_KEY_PREFIX + parentType, aggregate.toString());
//            getCache().put(cacheElement);
//        } catch (Exception e) {
//            _log.error("Error saving to ehcache " + CACHE_NAME + ": " + e.toString(), e);
//        }
//    }
    
    private void logDuration(long durationInMillis, String eventName) {
        _log.info(eventName + " took " + durationInMillis + " milliseconds");
    }

    public IQuizDao getQuizDao() {
        return _quizDao;
    }

    public void setQuizDao(IQuizDao quizDao) {
        _quizDao = quizDao;
    }

//    public long getCacheAgeMillis() {
//        return _cacheAgeMillis;
//    }
//
//    public void setCacheAgeMillis(long cacheAgeMillis) {
//        _cacheAgeMillis = cacheAgeMillis;
//    }

    public long getQuizId() {
        return _quizId;
    }

    public void setQuizId(long quizId) {
        _quizId = quizId;
    }

    /**
     * Thrown on any sort of error parsing the request params or constructing the QuizTaken object.
     * Field saveStatus indicates the error.
     */
    class ParseQuizTakenException extends Exception {
        private SaveStatus _saveStatus;
        protected ParseQuizTakenException(SaveStatus status) {
            super();
            _saveStatus = status;
        }
        public SaveStatus getSaveStatus() {
            return _saveStatus;
        }
        public String toString() {
            return "ParseQuizTakenException:" + _saveStatus.getMessage();
        }
    }
}
