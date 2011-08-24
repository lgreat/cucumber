package gs.web.promo;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
@Controller
@RequestMapping("/nbc/quiz.page")
public class NbcQuizController implements ReadWriteAnnotationController {
    protected static final Log _log = LogFactory.getLog(NbcQuizController.class);
    public static final String PARAM_CHILD_AGE = "childAge";
    public static final String PARAM_PARENT_AGE = "parentAge";
    public static final String PARAM_ZIP = "zip";
    public static final Pattern PATTERN_PARAM_ANSWER = Pattern.compile("q(\\d+)");

    @RequestMapping(method = RequestMethod.POST)
    /**
     * saveResponse
     * getAggregate
     * writeOut(aggregate)
     */
    public void submit(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        try {
            saveResponse(request); // does not throw
            JSONObject aggregateData = getAggregate(); // does not throw
            if (aggregateData != null) {
                // writeOut(aggregateData, response)
                aggregateData.write(response.getWriter()); // throws on error writing to response
            } else {
                response.setStatus(500);
                response.getWriter().write("{error:\"Cannot determine aggregate\"}"); // throws ioexception
            }
        } catch (Exception e) {
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
    public void saveResponse(HttpServletRequest request) {
        try {
            Object responseObject = parseResponseObject(request);
            if (responseObject != null) {
                // _dao.save(responseObject)
            }
        } catch (Exception e) {
            _log.error("Error saving quiz response: " + e, e);
        }
    }

    /**
     * parse static parameters
     * parse dynamic parameters
     * return populated hibernate object
     *
     * @param request HttpServletRequest containing quiz response
     * @return hibernate object containing quiz response, or null on error
     */
    public Object parseResponseObject(HttpServletRequest request) {
        try {
            // parse static parameters
            String childAge = request.getParameter(PARAM_CHILD_AGE);
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
            return new Object();
        } catch (Exception e) {
            _log.error("Error parsing response object: " + e, e);
        }

        return null;
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
            // if (aggregate != null) cache(aggregate)
            // return aggregate
            return new JSONObject();
        } catch (Exception e) {
            _log.error("Error constructing aggregate data: " + e, e);
        }
        return null;
    }
}
