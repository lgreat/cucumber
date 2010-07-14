package gs.web.school;

import gs.data.community.Subscription;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.survey.*;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolOverview2010Controller extends AbstractSchoolController implements IDirectoryStructureUrlController {
    protected static final Log _log = LogFactory.getLog(SchoolOverview2010Controller.class.getName());

    public static final String BEAN_ID = "/school/overview.page";

    private String _viewName;

    private IReviewDao _reviewDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;
    private ISurveyDao _surveyDao;

    private IPQDao _PQDao;

    private static final String[] SURVEY_ANSWERS_TO_SAMPLE = {"Arts", "Sports", "Other special programs"};
    public static String SCHOOL_HIGHLIGHTS_ATTRIBUTE = "schoolHighlights";

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        String schoolIdStr = request.getParameter("id");

        if (schoolIdStr == null) {
            schoolIdStr = (String) request.getAttribute(AbstractSchoolController.SCHOOL_ID_ATTRIBUTE);
        }


        // GS-3044 - number1expert cobrand specific code
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext.isCobranded() && "number1expert".equals(sessionContext.getCobrand())) {
            if (handleNumber1ExpertLeadGen(request, response, schoolIdStr, sessionContext)) {
                return null;
            }
        }

        if (StringUtils.isNumeric(schoolIdStr)) {
            School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
            model.put("school", school);

            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school,3);
            model.put("reviews", reviews);
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            model.put("ratings", ratings);

            /*
             * get PQ data to find quote if it exists
             */
            PQ pq = _PQDao.findBySchool(school);
            if (pq != null) {
                String bestKnownFor = StringUtils.capitalize(pq.getBestKnownFor());

                if (bestKnownFor != null) {
                    model.put("bestKnownFor", bestKnownFor);
                } else {
                    model.put("espLink", "profile");
                }
            } else {
                model.put("espLink", "principal");
            }

            /*
             * obtain nearby schools and place into model
             */
            //TODO: use findNearbySchoolsWithRatings but add distance field to SchoolWithRatings...and/or write new map tagx
            //List<SchoolWithRatings> nearbySchools = getSchoolDao().findNearbySchoolsWithRatings(school.getDatabaseState(), school.getLat(), school.getLon(), 50f, 25, null);
            List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchools(school, 5);
            request.setAttribute("mapSchools", getRatingsForNearbySchools(nearbySchools));
            //request.setAttribute("mapSchools", nearbySchools);

            Integer gsRating = getGSRatingFromDao(pageHelper, school);

            model.put("gs_rating", gsRating);

            _schoolProfileHeaderHelper.updateModel(request, response, school, model);

            populateModelWithSchoolHighlights(school, model);

            // if confirm=true is passed in as a parameter to theoverview page, always show the
            // school choice pack promo thank you
            String confirmStr = request.getParameter("confirm");
            if ("true".equals(confirmStr)) {
                model.put("showSchoolChooserPackPromo", true);
            } else {
                model.put("showSchoolChooserPackPromo", showSchoolChooserPackPromo(request, response));
            }

            KindercareLeadGenHelper.checkForKindercare(request, response, school, model);

            // TODO: is this necessary?
            String tempMsg = sessionContext.getTempMsg();
            if (StringUtils.isNotBlank(tempMsg) && tempMsg.matches("^fromSurvey[A-Z][A-Z]\\p{Digit}+")) {
                String stateParam = tempMsg.substring(10, 12);
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

    public void populateModelWithSchoolHighlights(School school, Map<String, Object> model) {

        //I want guaranteed ordering otherwise the capitalization and concatenation of the items in the collection gets tricky
        Set<String> highlights = new TreeSet();

        if (LevelCode.PRESCHOOL.equals(school.getLevelCode()) && school.getAgeRangeAsString() != null) {
            highlights.add("Accepts " + school.getAgeRangeAsString());
        }

        String subtypesCSL = SchoolSubtype.getSubtypes(school);
        if (subtypesCSL != null) {
            String[] subtypes = StringUtils.split(subtypesCSL, ',');
            for (String subtype : subtypes) {
                highlights.add(StringUtils.capitalize(subtype));
            }
        }

        if (school.getAffiliation() != null) {
            highlights.add(StringUtils.capitalize(school.getAffiliation()));
        }

        if (school.getAssociation() != null) {
            highlights.add("Associations: " + school.getAssociation());
        }

        for (String answer : SURVEY_ANSWERS_TO_SAMPLE) {
            String token = getOneResponseTokenForAnswer(school, answer);
            token = StringUtils.replace(token, "_", " ");
            if (token != null) {
                highlights.add(token);
            }
        }

        model.put(SCHOOL_HIGHLIGHTS_ATTRIBUTE, StringUtils.join(highlights, "; "));
    }

    /**
     * Fetches a Map of questions and answers from the survey dao, using the given answer bean title property.
     * Uses the first question and answer in map to fetch a list of responses from the dao. Throws away all
     * but the first response. Splits the response on comma, and returns the first token.
     *
     * @param school
     * @param answerTitle
     * @return
     */
    public String getOneResponseTokenForAnswer(School school, String answerTitle) {

        Integer surveyId = _surveyDao.findSurveyIdWithMostResultsForSchool(school);

        Survey survey = _surveyDao.findSurveyById(surveyId);

        Map<Question, Answer> qAndA = _surveyDao.extractQuestionAnswerMapByAnswerTitle(survey, answerTitle);

        List<List<String>> responses = new ArrayList<List<String>>();

        if (qAndA != null && qAndA.size() > 0) {
            Set<Map.Entry<Question, Answer>> entrySet = qAndA.entrySet();
            for (Map.Entry<Question, Answer> entry : entrySet) {
                responses = _surveyDao.findFriendlyResultsBySchoolQuestionAnswer(school, entry.getKey().getId(), entry.getValue().getId(), surveyId);
                if (responses != null && responses.size() > 0) {
                    break;
                }
            }
        }

        String item = null;
        List<String> tokens;

        if (responses.size() > 0) {
            tokens = responses.get(0);
            if (tokens != null && tokens.size() > 0) {
                item = tokens.get(0);
            }
        }

        return item;
    }


    /**
     * Obtain GS rating from Ratings DAO. Use caching unless is a dev environment
     *
     * @param pageHelper
     * @param school
     * @return
     * @throws IOException
     */
    public Integer getGSRatingFromDao(PageHelper pageHelper, School school) throws IOException {
        boolean isFromCache = true;
        if (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
            isFromCache = false;
        }

        return getGSRatingFromDao(pageHelper, school, isFromCache);
    }

    public Integer getGSRatingFromDao(PageHelper pageHelper, School school, boolean useCache) {
        Integer greatSchoolsRating = null;
        IRatingsConfig ratingsConfig = null;

        try {
            ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(school.getDatabaseState(), useCache);
        } catch (IOException e) {
            _log.debug("Failed to get ratings config from ratings config dao", e);
        }

        if (null != ratingsConfig) {
            SchoolTestValue schoolTestValue = _testManager.getOverallRating(school, ratingsConfig.getYear());

            if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {

                greatSchoolsRating = schoolTestValue.getValueInteger();

                if (schoolTestValue.getValueInteger() > 0 && schoolTestValue.getValueInteger() < 11) {
                    //TODO: do we need this?
                    pageHelper.addAdKeyword("gs_rating", String.valueOf(schoolTestValue.getValueInteger()));
                }
            }
        }

        return greatSchoolsRating;
    }

    // Checks to see if the user has any "School Chooser Pack" subscription
    // products.  Returns false if they do.

    public static boolean showSchoolChooserPackPromo(HttpServletRequest request, HttpServletResponse response) {
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

        String schoolChoicePackAlreadySubmitted = cookie.getProperty("schoolChoicePackAlreadySubmitted");
        String showSchoolChoicePackConfirm = cookie.getProperty("showSchoolChoicePackConfirm");

        if ("true".equals(schoolChoicePackAlreadySubmitted) && !"true".equals(showSchoolChoicePackConfirm)) {
            show = false;
        }

        return show;
    }

    protected boolean handleNumber1ExpertLeadGen(HttpServletRequest request, HttpServletResponse response, String schoolIdStr, SessionContext sessionContext) throws IOException {
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

                            StringUtils.isNotEmpty(cookies[i].getValue()) && !cookies[i].getValue().equals("0")) {
                        foundCookie = true;
                    }
                }
                if (!foundCookie) {
                    // send to bireg
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.GET_BIREG, sessionContext.getStateOrDefault(), new Integer(schoolIdStr), new Integer(agentId));
                    response.sendRedirect(urlBuilder.asFullUrl(request));
                    return true;
                }
            } // end if agentId != null
        } // end if cookies != null
        return false;
    }

    //TODO: DANGER!!! method below was copied from MapSchoolController but distance copy was added. Move into helper class

    /**
     * Returns a list of MapSchools for a given list of NearbySchools
     *
     * @param schools list of nearby schools
     * @return MapSchools
     */
    public List<MapSchool> getRatingsForNearbySchools(List<NearbySchool> schools) {
        // this is our data structure -- contains basically a school, a GS rating, and a parent rating
        List<MapSchool> mapSchools = new ArrayList<MapSchool>();
        // for each school
        for (NearbySchool nearbySchool : schools) {
            School school = nearbySchool.getNeighbor();
            // MapSchool is a subclass of NearbySchool
            MapSchool mapSchool = new MapSchool();
            // now we copy over the fields we want: school and gs rating
            // School. I don't like that it is called neighbor, but that's from the superclass NearbySchool
            mapSchool.setNeighbor(school);
            // GS rating
            mapSchool.setRating(nearbySchool.getRating());

            // Retrieve parent ratings
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            // Parent ratings
            mapSchool.setParentRatings(ratings);

            //Add distance
            mapSchool.setDistance(nearbySchool.getDistance());

            // Add data structure to list
            mapSchools.add(mapSchool);
        }

        return mapSchools;
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

    public IPQDao getPQDao() {
        return _PQDao;
    }

    public void setPQDao(IPQDao pqDao) {
        _PQDao = pqDao;
    }

    public SchoolProfileHeaderHelper getSchoolProfileHeaderHelper() {
        return _schoolProfileHeaderHelper;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }

    public IRatingsConfigDao getRatingsConfigDao() {
        return _ratingsConfigDao;
    }

    public void setRatingsConfigDao(IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public TestManager getTestManager() {
        return _testManager;
    }

    public void setTestManager(TestManager testManager) {
        _testManager = testManager;
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }
}