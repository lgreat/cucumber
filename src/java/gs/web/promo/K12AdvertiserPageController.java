package gs.web.promo;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.AdUtil;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Controller
public class K12AdvertiserPageController {
    final public static String VIEW_NAME = "/promo/k12/k12";
    final public static String PARAM_SCHOOL = "school";
    final public static String PARAM_TRAFFIC_DRIVER = "t";
    final public static String MODEL_K12_SCHOOL = "k12School";
    final public static String MODEL_SCHOOL_NAME = "schoolName";
    final public static String MODEL_SCHOOL_CODE = "schoolCode";
    final public static String MODEL_K12_CLICK_THROUGH_URL = "k12ClickThroughUrl";
    final public static String MODEL_STATE = "state";
    final public static String OTHER_TRAFFIC_DRIVER = "ot";

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "/online-education.page", method = RequestMethod.GET)
    public ModelAndView handleRequest(HttpServletRequest request) throws Exception {
        String trafficDriverParam = request.getParameter(PARAM_TRAFFIC_DRIVER);
        String page;
        if (StringUtils.isNotBlank(request.getHeader("referer")) && trafficDriverParam != null && trafficDriverParam.matches("^\\w{2}$")) {
            page = trafficDriverParam;
        } else {
            page = OTHER_TRAFFIC_DRIVER;
        }
        // Redirect requests for state specific pages to the appropriate k12 school profile in that state.
        try {
            State stateFromParam = State.fromString(request.getParameter(PARAM_SCHOOL)); // this will fail for INT
            if (stateFromParam == State.CA) {
                // Special handling: CA has multiple k12 schools. Go instead to a search results page.
                UrlBuilder searchUrl = new UrlBuilder(UrlBuilder.SCHOOL_SEARCH, State.CA, "California Virtual Academies");
                return new ModelAndView(new RedirectView301(searchUrl.asSiteRelative(request)));
            }
            Integer schoolId = K12AdvertiserPageHelper.K12_STATE_TO_SCHOOL_ID.get(stateFromParam);
            if (schoolId == null) {
                return new ModelAndView(new RedirectView301("/online-education.page?school=INT&page=" + page));
            }
            try {
                School s = _schoolDao.getSchoolById(stateFromParam, schoolId);
                UrlBuilder schoolUrl = new UrlBuilder(s, UrlBuilder.SCHOOL_PROFILE);
                return new ModelAndView(new RedirectView301(schoolUrl.asSiteRelative(request)));
            } catch (ObjectRetrievalFailureException orfe) {
                return new ModelAndView(new RedirectView301("/online-education.page?school=INT&page=" + page));
            }
        } catch (IllegalArgumentException iae) {
            if (!StringUtils.equals("INT", request.getParameter(PARAM_SCHOOL))) {
                return new ModelAndView(new RedirectView301("/online-education.page?school=INT&page=" + page));
            }
        }
        String schoolParam = request.getParameter(PARAM_SCHOOL);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_K12_SCHOOL, schoolParam);
        model.put(MODEL_SCHOOL_NAME, K12AdvertiserPageHelper.getK12SchoolName(schoolParam));
        try {
            State state = State.fromString(schoolParam);
            if (state != null) {
                model.put(MODEL_STATE, state.getAbbreviationLowerCase());
            }
        } catch (IllegalArgumentException iae) {
            // INT has no state
        }

        String clickthruSchoolParam = K12AdvertiserPageHelper.getClickthruSchoolParam(schoolParam);
        model.put(MODEL_SCHOOL_CODE, clickthruSchoolParam);

        model.put(MODEL_K12_CLICK_THROUGH_URL, AdUtil.getK12ClickThroughUrl(clickthruSchoolParam, page));

        // needed so for header/footer/box ads
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            pageHelper.setShowingLeaderboard(false);
            pageHelper.setShowingBelowNavAds(false);
            pageHelper.setShowingFooterAd(false);
        }

        return new ModelAndView(VIEW_NAME, model);
    }
}
