package gs.web.school;

import gs.data.community.Subscription;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolOverview2010Controller extends
        AbstractSchoolController implements IDirectoryStructureUrlController {
    private String _viewName;

    private IReviewDao _reviewDao;
    //private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest
            request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        String schoolIdStr = request.getParameter("id");

        if (schoolIdStr == null) {
            schoolIdStr = (String) request.getAttribute(AbstractSchoolController.SCHOOL_ID_ATTRIBUTE);
        }

        // GS-3044 - number1expert cobrand specific code
        SessionContext sessionContext =
                SessionContextUtil.getSessionContext(request);
        if (sessionContext.isCobranded() &&
                "number1expert".equals(sessionContext.getCobrand())) {
            if (handleNumber1ExpertLeadGen(request, response,
                    schoolIdStr, sessionContext)) {
                return null;
            }
        }


        if (StringUtils.isNumeric(schoolIdStr)) {
            School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
            model.put("school", school);

            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school);
            model.put("reviews", reviews);
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            model.put("ratings", ratings);

            //_schoolProfileHeaderHelper.updateModel(model);

            // if confirm=true is passed in as a parameter to theoverview page, always show the
            // school choice pack promo thank you
            String confirmStr = request.getParameter("confirm");
            if ("true".equals(confirmStr)) {
                model.put("showSchoolChooserPackPromo", true);
            } else {
                model.put("showSchoolChooserPackPromo",
                        showSchoolChooserPackPromo(request, response));
            }

            KindercareLeadGenHelper.checkForKindercare(request,
                    response, school, model);

            // TODO: is this necessary?
            String tempMsg = sessionContext.getTempMsg();
            if (StringUtils.isNotBlank(tempMsg) &&
                    tempMsg.matches("^fromSurvey[A-Z][A-Z]\\p{Digit}+")) {
                String stateParam = tempMsg.substring(10, 12);
                String idParam = tempMsg.substring(12);
                String schoolState =
                        school.getDatabaseState().getAbbreviation();
                String schoolId = String.valueOf(school.getId());
                if (schoolState.equals(stateParam) &&
                        schoolId.equals(idParam)) {
                    model.put("fromSurveyPage", Boolean.TRUE);
                }
                SessionContextUtil util =
                        sessionContext.getSessionContextUtil();
                util.clearTempMsg(response);
            }
        }

        return new ModelAndView(_viewName, model);
    }

    // Checks to see if the user has any "School Chooser Pack" subscription
    // products.  Returns false if they do.

    public static boolean showSchoolChooserPackPromo(HttpServletRequest
            request, HttpServletResponse response) {
        boolean show = true;
        SessionContext sc = SessionContextUtil.getSessionContext(request);
        User u = sc.getUser();
        if (u != null) {
            Set<Subscription> subs = u.getSubscriptions();
            if (subs != null && subs.size() > 0) {
                for (Subscription sub : subs) {
                    String prod = sub.getProduct().getName();
                    if (prod != null && prod.startsWith("chooserpack")) {
                        show = false;
                        break;
                    }
                }
            }
        }

        SitePrefCookie cookie = new SitePrefCookie(request, response);

        String schoolChoicePackAlreadySubmitted =
                cookie.getProperty("schoolChoicePackAlreadySubmitted");
        String showSchoolChoicePackConfirm =
                cookie.getProperty("showSchoolChoicePackConfirm");

        if ("true".equals(schoolChoicePackAlreadySubmitted) &&
                !"true".equals(showSchoolChoicePackConfirm)) {
            show = false;
        }

        return show;
    }

    protected boolean handleNumber1ExpertLeadGen(HttpServletRequest
            request, HttpServletResponse response,
                                                 String schoolIdStr,
                                                 SessionContext sessionContext) throws IOException {
        Cookie[] cookies = request.getCookies();
        String agentId = null;
        if (cookies != null) {
            // Collect all the cookies
            for (int i = 0; cookies.length > i; i++) {
                // find the agent id cookie
                if ("AGENTID".equals(cookies[i].getName())) {
                    // store its value
                    agentId = cookies[i].getValue();
                }
            }
            // if there's no agent id, no lead gen necessary
            if (agentId != null) {
                boolean foundCookie = false;
                String biregCookieName = "BIREG" + agentId;
                for (int i = 0; cookies.length > i; i++) {
                    if (biregCookieName.equals(cookies[i].getName()) &&

                            StringUtils.isNotEmpty(cookies[i].getValue()) &&
                            !cookies[i].getValue().equals("0")) {
                        foundCookie = true;
                    }
                }
                if (!foundCookie) {
                    // send to bireg
                    UrlBuilder urlBuilder = new UrlBuilder
                            (UrlBuilder.GET_BIREG,
                                    sessionContext.getStateOrDefault(),
                                    new Integer(schoolIdStr),
                                    new Integer(agentId));
                    response.sendRedirect(urlBuilder.asFullUrl(request));
                    return true;
                }
            } // end if agentId != null
        } // end if cookies != null
        return false;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}