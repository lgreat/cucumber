package gs.web.promo;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
@Controller
@RequestMapping("/nbc/quiz.page")
public class NbcQuizController implements ReadWriteAnnotationController {
    protected static final Log _log = LogFactory.getLog(NbcQuizController.class);

    protected enum SaveStatus {
        SUCCESS ("Success", "Success"),
        ERROR_NO_CHILD_AGE ("Error", "Child age must be provided -- field " + PARAM_CHILD_AGE + " missing or empty"),
        ERROR_SERVER ("Error", "Server error");

        private final String _status;
        private final String _message;
        SaveStatus(String status, String message) {
            _status = status;
            _message = message;
        }
        public void writeToJSON(JSONObject o) throws JSONException {
            o.put("status", _status);
            o.put("message", _message);
        }
        public String toString() {
            return "SaveStatus{" +_status + ":" + _message + "}";
        }
    }

    public static final String PARAM_CHILD_AGE = "childAge";
    public static final String PARAM_PARENT_AGE = "parentAge";
    public static final String PARAM_ZIP = "zip";
    public static final Pattern PATTERN_PARAM_ANSWER = Pattern.compile("^q(\\d+)$");

    @RequestMapping(method = RequestMethod.POST)
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
            SaveStatus saveStatus = saveResponse(request); // should not throw or return null
            JSONObject aggregateData = getAggregate(); // should not throw
            if (aggregateData != null) {
                // combine aggregateData with saveStatus and print out
                JSONObject output = new JSONObject();
                output.put("aggregate", aggregateData);
                saveStatus.writeToJSON(output);
                output.write(response.getWriter()); // throws on error writing to response
            } else {
                // no aggregate means return 500
                response.setStatus(500);
            }
        } catch (Exception e) {
            // there was a failure writing out. Return a 500
            response.setStatus(500);
            _log.error("Error writing to response: " + e, e);
        }
    }

    /**
     * responseObject = parseResponseObject(request)
     * if (responseObject  == null) return
     * _dao.save(responseObject)
     *
     * @param request HttpServletRequest containing quiz response
     */
    public SaveStatus saveResponse(HttpServletRequest request) {
        try {
            Object quizTaken = parseQuizTaken(request);
            // _dao.save(quizTaken)
            return SaveStatus.SUCCESS;
        } catch (ParseQuizTakenException pqte) {
            return pqte.getSaveStatus();
        } catch (Exception e) {
            // could be thrown by _dao.save
            // In which case there is no meaningful message we can communicate to view, so just log it
            _log.error("Error saving quiz taken: " + e, e);
            return SaveStatus.ERROR_SERVER;
        }
    }

    /**
     * parse static parameters
     * parse dynamic parameters
     * return populated hibernate QuizTaken object
     *
     * @param request HttpServletRequest containing quiz taken data
     * @return QuizTaken object containing quiz response, never null
     * @throws ParseQuizTakenException on any error constructing the QuizTaken object
     */
    public Object parseQuizTaken(HttpServletRequest request) throws ParseQuizTakenException {
        Object rval = new Object();
        // parse static parameters
        String childAge = request.getParameter(PARAM_CHILD_AGE);
        if (StringUtils.isBlank(childAge)) { // sample validation
            throw new ParseQuizTakenException(SaveStatus.ERROR_NO_CHILD_AGE);
        }
        String parentAge = request.getParameter(PARAM_PARENT_AGE);
        String zip = request.getParameter(PARAM_ZIP);

        // parse dynamic parameters
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String questionKey = String.valueOf(paramNames.nextElement());
            if (PATTERN_PARAM_ANSWER.matcher(questionKey).matches()) {
                String questionResponse = request.getParameter(questionKey);
            }
        }
        return rval;
    }

    /**
     * Fetches aggregate data from cache or database
     * Converts to JSON
     *
     * @return JSONObject describing aggregate data, or null on error
     */
    public JSONObject getAggregate() {
        try {
            // if (cache) return cached aggregate
            // aggregate = _dao.fetchAggregate()
            JSONObject aggregate = new JSONObject();
            // if (aggregate != null) cache(aggregate)
            aggregate.put("Parent Type A", "80"); // for debugging
            aggregate.put("Parent Type B", "20"); // for debugging
            return aggregate;
        } catch (Exception e) {
            _log.error("Error constructing aggregate data: " + e, e);
        }
        return null;
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
            return _saveStatus._message;
        }
    }
}
