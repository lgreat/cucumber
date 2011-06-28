package gs.web.splash;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class IPhoneSplashController extends AbstractController {
    private static final Logger _log = Logger.getLogger(IPhoneSplashController.class);

    public static final String BEAN_ID = "/splash/iphone.page";

    public static final String PARAM_REFERRER = "l";
    public static final String MODEL_REFERRER = "referrer";

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        // default to /
        model.put(MODEL_REFERRER, "/");

        String referrer = request.getParameter(PARAM_REFERRER);
        if (referrer != null && referrer.startsWith("/")) {
            String escapedUrl = StringEscapeUtils.escapeHtml(referrer);
            model.put(MODEL_REFERRER, escapedUrl);
        }

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
