package gs.web.promo;

import gs.web.util.PageHelper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Controller
public class K12AdvertiserPageController {
    final public static String VIEW_NAME = "/promo/k12/k12";
    final public static String PARAM_SCHOOL = "school";
    final public static String MODEL_K12_SCHOOL = "k12School";
    final public static String MODEL_SCHOOL_NAME = "schoolName";
    final public static String MODEL_HAS_BOTTOM_COPY = "hasBottomCopy";

    @RequestMapping(value = "/online-education.page", method = RequestMethod.GET)
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String schoolParam = request.getParameter(PARAM_SCHOOL);
        if (!K12AdvertiserPageHelper.isValidK12School(schoolParam)) {
            return new ModelAndView(new RedirectView("/online-education.page?school=INT"));
        }

        // Google ad attribute for key "k12school" with values like "CA", "INT"
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        pageHelper.addAdKeyword("k12school", schoolParam);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_K12_SCHOOL, schoolParam);
        model.put(MODEL_SCHOOL_NAME, K12AdvertiserPageHelper.getK12SchoolName(schoolParam));
        model.put(MODEL_HAS_BOTTOM_COPY, K12AdvertiserPageHelper.hasBottomCopy(schoolParam));
        return new ModelAndView(VIEW_NAME, model);
    }
}
