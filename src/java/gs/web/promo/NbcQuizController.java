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

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
@Controller
@RequestMapping("/nbc/quiz.page")
public class NbcQuizController implements ReadWriteAnnotationController {
    protected static final Log _log = LogFactory.getLog(NbcQuizController.class);

    @RequestMapping(method = RequestMethod.POST)
    public void submit(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("application/json");
            try {
                JSONObject output = new JSONObject();
                output.put("success", 1);
                output.write(response.getWriter());
            } catch(JSONException jsone) {
                _log.error("Error constructing JSON output object: " + jsone, jsone);
                response.setStatus(500);
                response.getWriter().write("{errorCode:1}");
            }
        } catch (IOException ioe) {
            response.setStatus(500);
            _log.error("Error writing to response: " + ioe, ioe);
        }
    }
}
