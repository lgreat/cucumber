package gs.web.school;

import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.Ratings;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * This controller handles requests for the School Profile Overview page:
 * http://www.greatschools.net/school/overview.page?state=tx&id=10683
 * 
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewController extends AbstractSchoolController {

    /** Spring Bean id */
    public static final String BEAN_ID = "/school/overview.page";

    /** The allowed length of the parent review blurb */
    public static final int REVIEW_LENGTH = 100;

    private String _viewName;
    private IReviewDao _reviewDao;

    /**
     * This method must be called using the standard Spring Controller workflow, that
     * is, it must be called by the superclass handleRequest() method in order to
     * assure that a valid school is available with the getSchool() method.
     *
     * @param request provided by servlet container
     * @param response provided by servlet container
     * @return a ModelAndView
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String schoolIdStr = request.getParameter("id");

        // GS-3044 - number1expert cobrand specific code
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext.isCobranded() && "number1expert".equals(sessionContext.getCobrand())) {
            javax.servlet.http.Cookie[] cookies = request.getCookies();
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
                    for (int i=0; cookies.length > i; i++) {
                        if (biregCookieName.equals(cookies[i].getName()) &&
                                StringUtils.isNotEmpty(cookies[i].getValue()) && 
                                !cookies[i].getValue().equals("0")){
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
                        return null;
                    }
                } // end if agentId != null
            } // end if cookies != null
        } // end if cobranded

        if (StringUtils.isNumeric(schoolIdStr)) {
            School school = (School)request.getAttribute(SCHOOL_ATTRIBUTE);
            model.put("school", school);
            List reviews = _reviewDao.getPublishedReviewsBySchool(school);
            if (reviews != null && reviews.size() > 0) {
                model.put("reviewCount", new Integer(reviews.size()));
                Review review = (Review)reviews.get(0);
                if (review != null) {
                    model.put("reviewText", StringUtils.abbreviate(review.getComments(), REVIEW_LENGTH));
                }
            }
            model.put("latestReviewsModel", createLatestReviewsModel(school));
        }

        return new ModelAndView(_viewName, model);
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

    /**
     * Populates a <code>Map</code> with the fields used by the Latest
     * Parent Reviews box on overview.page
     * @param school a gs.data.school.School
     * @return a Map containing the fields to display or null if one of the
     * user story conditions for display is not met. See GS-3204.
     */
    Map createLatestReviewsModel(School school) {

        String[] ratingStrings = {
                "unsatifactory",
                "below average",
                "average",
                "above average",
                "excellent"
        };
        String QUALITY_CAT = "teacher quality is";
        String PRINCIPAL_CAT = "principal leadership is";
        String EXTRA_CAT = "extracurricular activities are";
        String PARENT_CAT = "parent involvement is";
        String SAFETY_CAT = "safety and discipline are";

        Map latestReviewsModel = null;
        List reviews = getReviewDao().getPublishedReviewsBySchool(school);
        if (reviews != null && reviews.size() > 0) {
            Review review = null;
            for (int i = 0; i < reviews.size(); i++) {
                review = (Review)reviews.get(i);
                if (review.getQuality() != null && review.getComments() != null) {
                    break;
                }
            }

            if (review != null) {

                Ratings ratings = getReviewDao().findRatingsBySchool(school);
                Integer randomRating = null;
                String randomCategory = null;

                // First try to randomly pick a rating category:
                int index = (int)(Math.random() * 5);
                switch (index) {
                    case 0:
                        randomCategory = QUALITY_CAT;
                        randomRating = ratings.getAvgQuality();
                        break;
                    case 1:
                        randomCategory = PRINCIPAL_CAT;
                        randomRating = ratings.getAvgPrincipal();
                        break;
                    case 2:
                        randomCategory = EXTRA_CAT;
                        randomRating = ratings.getAvgActivities();
                        break;
                    case 3:
                        randomCategory = PARENT_CAT;
                        randomRating = ratings.getAvgParents();
                        break;
                    case 4:
                        randomCategory = SAFETY_CAT;
                        randomRating = ratings.getAvgSafety();
                        break;
                }

                // If a rating does not exist for the randomly-selected category, look
                // in the other categories for a rating.
                if (randomRating == null) {
                    randomCategory = QUALITY_CAT;
                    randomRating = ratings.getAvgQuality();
                    if (randomRating == null) {
                        randomCategory = PRINCIPAL_CAT;
                        randomRating = ratings.getAvgPrincipal();
                        if (randomRating == null) {
                            randomCategory = EXTRA_CAT;
                            randomRating = ratings.getAvgActivities();
                            if (randomRating == null) {
                                randomCategory = PARENT_CAT;
                                randomRating = ratings.getAvgParents();
                                if (randomRating == null) {
                                    randomCategory = SAFETY_CAT;
                                    randomRating = ratings.getAvgSafety();
                                }
                            }
                        }
                    }
                }

                // If we don't find one, return null.
                if (randomRating != null) {
                    latestReviewsModel = new HashMap();
                    latestReviewsModel.put("randomCategory", randomCategory);
                    latestReviewsModel.put("randomRating", ratingStrings[randomRating.intValue()]);
                    latestReviewsModel.put("latestRating", review.getQuality().getName());
                    latestReviewsModel.put("total", ratings.getCount());
                    latestReviewsModel.put("comment", abbreviateAtWhitespace(review.getComments(), REVIEW_LENGTH));
                }
            }
        }

        return latestReviewsModel;
    }


    /**
     * Abbreviates a string - if a string is longer than maxLength characters, then
     * truncate at a word boundary and append "..."  The resulting string will be
     * no longer than maxLength <em>inlucding</em> the "..."
     * Null will be returned if a null String is passed as the comment
     * @param s a comment String
     * @param maxLength the maximum lenght the comment may be before truncation, must be
     * 3 or more.
     * @return a formatted String
     */
    String abbreviateAtWhitespace(String s, int maxLength) {
        if (maxLength > 2) {
            if (StringUtils.isNotBlank(s)) {
                s = s.trim();
                if (s.length() > maxLength) {
                    int ind = s.lastIndexOf(" ", maxLength);
                    if (ind < 0) ind = maxLength;
                    s = s.substring(0, ind);
                    if (!s.matches(".*[\\.\\?\\!]$")) {
                        if (s.length() > maxLength-3) {
                            int ind2 = s.lastIndexOf(" ", s.length()-3);
                            if (ind2 < 0) { ind2 = s.length()-3; }
                            s = s.substring(0, ind2);
                        }
                        if (!s.matches(".*[\\.\\?\\!]$")) {
                            s = s + "...";
                        }
                    }
                }
            }
            return s;
        } else {
            throw new IllegalArgumentException("maxLength must be > 2; now: " + maxLength);
        }
    }
}


