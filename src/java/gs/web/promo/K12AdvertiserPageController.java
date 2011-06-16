package gs.web.promo;

import gs.web.util.AdUtil;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Controller
public class K12AdvertiserPageController {
    final public static String VIEW_NAME = "/promo/k12/k12";
    final public static String PARAM_SCHOOL = "school";
    final public static String PARAM_TRAFFIC_DRIVER = "t";
    final public static String MODEL_K12_SCHOOL = "k12School";
    final public static String MODEL_SCHOOL_NAME = "schoolName";
    final public static String MODEL_HAS_BOTTOM_COPY = "hasBottomCopy";
    final public static String MODEL_K12_CLICK_THROUGH_URL = "k12ClickThroughUrl";

    @RequestMapping(value = "/online-education.page", method = RequestMethod.GET)
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String trafficDriverParam = request.getParameter(PARAM_TRAFFIC_DRIVER);
        String schoolParam = request.getParameter(PARAM_SCHOOL);
        if (!K12AdvertiserPageHelper.isValidK12School(schoolParam)) {
            return new ModelAndView(new RedirectView301("/online-education.page?school=INT"));
        }

        // Google ad attribute for key "k12school" with values like "CA", "INT"
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        pageHelper.addAdKeyword("k12school", schoolParam);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_K12_SCHOOL, schoolParam);
        model.put(MODEL_SCHOOL_NAME, K12AdvertiserPageHelper.getK12SchoolName(schoolParam));
        model.put(MODEL_HAS_BOTTOM_COPY, K12AdvertiserPageHelper.hasBottomCopy(schoolParam));

        String referrer = request.getHeader("referer");
        model.put(MODEL_K12_CLICK_THROUGH_URL, AdUtil.getK12ClickThroughUrl(referrer, schoolParam, trafficDriverParam));

        return new ModelAndView(VIEW_NAME, model);
    }
}
