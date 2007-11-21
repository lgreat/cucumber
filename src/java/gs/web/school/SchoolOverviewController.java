package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.IGroupDataTypeDao;
import gs.data.school.census.SchoolCensusValue;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.web.jsp.Util;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This controller handles requests for the School Profile Overview page:
 * http://www.greatschools.net/school/overview.page?state=tx&id=10683
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewController extends AbstractSchoolController {

    /**
     * Spring Bean id
     */
    public static final String BEAN_ID = "/school/overview.page";

    /**
     * The allowed length of the parent review blurb
     */
    public static final int REVIEW_LENGTH = 100;

    private String _viewName;
    private IReviewDao _reviewDao;
    private ITestDataSetDao _testDataSetDao;
    private IGroupDataTypeDao _groupDataTypeDao;

    /**
     * This method must be called using the standard Spring Controller workflow, that
     * is, it must be called by the superclass handleRequest() method in order to
     * assure that a valid school is available with the getSchool() method.
     *
     * @param request  provided by servlet container
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
                        return null;
                    }
                } // end if agentId != null
            } // end if cookies != null
        } // end if cobranded

        if (StringUtils.isNumeric(schoolIdStr)) {
            School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
            model.put("school", school);
            List reviews = _reviewDao.getPublishedReviewsBySchool(school);
            if (reviews != null && reviews.size() > 0) {
                model.put("reviewCount", new Integer(reviews.size()));
                Review review = (Review) reviews.get(0);
                if (review != null) {
                    model.put("reviewText", StringUtils.abbreviate(review.getComments(), REVIEW_LENGTH));
                }
            }
            model.put("preschoolOnly", school.getLevelCode().equals(LevelCode.PRESCHOOL));
            model.put("latestReviewsModel", createLatestReviewsModel(school));
            model.put("hasPrincipalView", Boolean.valueOf(getSchoolDao().hasPrincipalView(school)));
            model.put("hasAPExams", Boolean.valueOf(hasAPExams(school)));
            model.put("hasTestData", Boolean.TRUE);
//            model.put("hasTeacherData", Boolean.valueOf(_groupDataTypeDao.hasTeacherData(school)));
//            model.put("hasStudentData", Boolean.valueOf(_groupDataTypeDao.hasStudentData(school)));
            model.put("hasTeacherData", Boolean.TRUE);
            model.put("hasStudentData", Boolean.TRUE);
            model.put("hasFinanceData", Boolean.TRUE);

            String tempMsg = sessionContext.getTempMsg();
            if (StringUtils.isNotBlank(tempMsg) && tempMsg.matches("^fromSurvey[A-Z][A-Z]\\p{Digit}+")) {
                String stateParam = tempMsg.substring(10,12);
                String idParam = tempMsg.substring(12);
                String schoolState = school.getDatabaseState().getAbbreviation();
                String schoolId = String.valueOf(school.getId());
                if (schoolState.equals(stateParam) && schoolId.equals(idParam)) {
                    model.put("fromSurveyPage", Boolean.TRUE);
                }
                SessionContextUtil util = sessionContext.getSessionContextUtil();
                util.clearTempMsg(response);
            }
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

    boolean hasTestData(School s) {
        List values = _testDataSetDao.findValues(s);
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            SchoolTestValue value = (SchoolTestValue)iter.next();
            if (value.getDataSet().isActive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Throws NPE if the provided school is null.
     *
     * @param school a valid <code>School</code>
     * @return true if the school has AP exams
     */
    boolean hasAPExams(School school) {
        boolean hasAPExams = false;
        ICensusInfo ci = school.getCensusInfo();
        SchoolCensusValue value =
                ci.getLatestValue(school, CensusDataType.ADVANCED_PLACEMENT_EXAMS_OFFERED);

        if (value != null) {
            hasAPExams = StringUtils.isNotBlank(value.getValueText());
        }

        return hasAPExams;
    }

    /**
     * Populates a <code>Map</code> with the fields used by the Latest
     * Parent Reviews box on overview.page
     *
     * @param school a gs.data.school.School
     * @return a Map containing the fields to display or null if one of the
     *         user story conditions for display is not met. See GS-3204.
     */
    Map createLatestReviewsModel(School school) {

        String[] ratingStrings = {
                "unsatifactory",
                "below average",
                "average",
                "above average",
                "excellent"
        };

        String TEACHERS_CAT = "teacher quality is";
        String PRINCIPAL_CAT = "principal leadership is";
        String EXTRA_CAT = "extracurricular activities are";
        String PARENT_CAT = "parent involvement is";
        String SAFETY_CAT = "safety and discipline are";

        Map latestReviewsModel = null;
        List<Map> preschoolReviews = null;

        List reviews = getReviewDao().getPublishedReviewsBySchool(school);
        if (reviews != null && reviews.size() != 0) {
            Review latestReview = null;
            for (int i = 0; i < reviews.size(); i++) {
                Review aReview = (Review) reviews.get(i);
                if (!CategoryRating.DECLINE_TO_STATE.equals(aReview.getQuality()) && aReview.getComments() != null) {
                    if (latestReview == null) { latestReview = aReview; }
                    if (!school.getLevelCode().equals(LevelCode.PRESCHOOL) ||
                            (preschoolReviews != null && preschoolReviews.size() > 2)) {
                        break;
                    } else {
                        if (preschoolReviews == null) preschoolReviews = new ArrayList<Map>();
                        Map<String, String> preschoolData = new HashMap<String, String>();
                        preschoolData.put("psRating", aReview.getQuality().getName());
                        preschoolData.put("psComment", Util.abbreviateAtWhitespace(aReview.getComments(), REVIEW_LENGTH));
                        preschoolReviews.add(preschoolData);
                    }
                }
            }

            if (latestReview != null) {

                Ratings ratings = getReviewDao().findRatingsBySchool(school);
                if (ratings.getCount() > 2) {
                    Integer randomRating = null;
                    String randomCategory = null;

                    // First try to randomly pick a rating category:
                    int index = (int) (Math.random() * 5);
                    switch (index) {
                        case 0:
                            randomCategory = TEACHERS_CAT;
                            randomRating = ratings.getAvgTeachers();
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
                        randomCategory = TEACHERS_CAT;
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
                        latestReviewsModel.put("randomRating", ratingStrings[randomRating.intValue() - 1]);
                        latestReviewsModel.put("latestRating", latestReview.getQuality().getName());
                        latestReviewsModel.put("totalReviews", new Integer(reviews.size()));
                        latestReviewsModel.put("comment",
                                Util.abbreviateAtWhitespace(latestReview.getComments(), REVIEW_LENGTH));

                        if (preschoolReviews != null) {
                            latestReviewsModel.put("preschoolReviews", preschoolReviews);
                        }
                    }
                }
            }
        }
        return latestReviewsModel;
    }

    public void setTestDataSetDao(ITestDataSetDao _testDao) {
        this._testDataSetDao = _testDao;
    }

//    public IGroupDataTypeDao getGroupDataTypeDao() {
//        return _groupDataTypeDao;
//    }

    public void setGroupDataTypeDao(IGroupDataTypeDao groupDataTypeDao) {
        _groupDataTypeDao = groupDataTypeDao;
    }
}


