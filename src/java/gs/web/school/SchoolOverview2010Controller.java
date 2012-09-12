package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.survey.Answer;
import gs.data.survey.ISurveyDao;
import gs.data.survey.Question;
import gs.data.survey.Survey;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfigDao;
import gs.data.util.CommunityUtil;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.content.cms.CmsHomepageController;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.request.RequestInfo;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
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
public class SchoolOverview2010Controller extends AbstractSchoolController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {
    protected static final Log _log = LogFactory.getLog(SchoolOverview2010Controller.class.getName());

    public static final String BEAN_ID = "desktopSchoolOverviewController";

    private String _viewName;

    private IReviewDao _reviewDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;
    private ISurveyDao _surveyDao;
    private NearbySchoolsHelper _nearbySchoolsHelper;
    private RatingHelper _ratingHelper;
    private IGeoDao _geoDao;
    private ISchoolMediaDao _schoolMediaDao;
    private IReportedEntityDao _reportedEntityDao;

    private IEspResponseDao _espResponseDao;

    protected static final long PRESCHOOL_CITY_POPULATION_BOUNDARY = 8000;
    private static final String[] SURVEY_ANSWERS_TO_SAMPLE = {"Arts", "Sports", "Other special programs"};
    public static String SCHOOL_HIGHLIGHTS_ATTRIBUTE = "schoolHighlights";
    public static final int MAX_SCHOOL_PHOTOS_IN_GALLERY = 10;
    public static final int MAX_SCHOOL_REVIEWS = 10;

    private ControllerFamily _controllerFamily;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        String schoolIdStr = request.getParameter("id");

        if (schoolIdStr == null) {
            schoolIdStr = (String) request.getAttribute(AbstractSchoolController.SCHOOL_ID_ATTRIBUTE);
        }


        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        //used to support the "Report It" links in recent reviews list
        if(PageHelper.isMemberAuthorized(request)){
            User user = sessionContext.getUser();
            if (user != null) {
                model.put("validUser", user);
            }
        }

        if (StringUtils.isNumeric(schoolIdStr)) {
            School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
            model.put("school", school);

            // TODO-13114 - already copied into SchoolProfileController by Anthony but should refactor
            // GS-10484
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            String fullCanonicalUrl = urlBuilder.asFullUrl(request);
            model.put("relCanonical", fullCanonicalUrl);

            // TODO-13114 - not rolling out to new profile yet but todo is to put a note in SchoolProfileController that it'll eventually be needed
            // Preschool pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                RequestInfo hostnameInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
                if (!hostnameInfo.isOnPkSubdomain() && hostnameInfo.isPkSubdomainSupported()) {
                    return new ModelAndView(new RedirectView301(fullCanonicalUrl));
                }
            }

            // page only needs up to three, but we need the total number as well
            // should probably add a method _reviewDao.getTotalPublishedReviewsBySchool(school)
            // and limit the following query to 12
            List<Review> reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, MAX_SCHOOL_REVIEWS);
            Long numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);
            model.put("reviews", reviews);
            model.put("numberOfReviews", numberOfReviews);
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            model.put("ratings", ratings);

            // this code is no longer needed and can be removed due to GS-12127 that rolled back GS-11676.
            // shouldIndex is only false when the school is null. numberOfReviews isn't used. Also need to remove from
            // overview2010.jspx
            model.put("noIndexFlag", !shouldIndex(school, numberOfReviews));

            setLastModifiedDateInModel(model, school, reviews, numberOfReviews);

            /*
             * get PQ data to find quote if it exists
             */
            Set<String> pqKeys = new HashSet<String>(1);
            pqKeys.add("best_known_for");
            List<EspResponse> espResponses = _espResponseDao.getResponsesByKeys(school, pqKeys);
            if (espResponses != null && espResponses.size() > 0) {
                String bestKnownFor = espResponses.get(0).getSafeValue();
                if (StringUtils.isNotBlank(bestKnownFor)) {
                    if (!StringUtils.endsWith(bestKnownFor, ".")) {
                        bestKnownFor += ".";
                    }
                    model.put("bestKnownFor", bestKnownFor);
                }
            }

            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

            /*
             * obtain nearby schools and place into model
             */
            //TODO: use findNearbySchoolsWithRatings but add distance field to SchoolWithRatings...and/or write new map tagx
            //List<SchoolWithRatings> nearbySchools = getSchoolDao().findNearbySchoolsWithRatings(school.getDatabaseState(), school.getLat(), school.getLon(), 50f, 25, null);
            List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchoolsNewGSRating(school, 20);
            request.setAttribute("mapSchools", getNearbySchoolsHelper().getRatingsForNearbySchools(nearbySchools));
            //request.setAttribute("mapSchools", nearbySchools);

            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

            if (gsRating != null && gsRating > 0 && gsRating < 11) {
                pageHelper.addAdKeyword("gs_rating", String.valueOf(gsRating));
            }

            model.put("gs_rating", gsRating);

            _schoolProfileHeaderHelper.updateModel(request, response, school, model);

            populateModelWithSchoolHighlights(school, model);

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

            addSchoolPhotosToModel(school, model, request);
        }

        model.put("hasMobileView", true);
        return new ModelAndView(_viewName, model);
    }

    private static void setLastModifiedDateInModel(Map<String, Object> model, School school, List<Review> reviews, Long numberOfReviews) {
        Review latestNonPrincipalReview = null;
        if (numberOfReviews > 0 && reviews != null && reviews.size() > 0) {
            latestNonPrincipalReview = reviews.get(0);
        }
        Date lastModifiedDate = SchoolProfileHelper.getSchoolLastModified(school, latestNonPrincipalReview);
        if (lastModifiedDate != null) {
            model.put("lastModifiedDate", lastModifiedDate);
        }
    }

    private void addSchoolPhotosToModel(School school, Map<String, Object> model, HttpServletRequest request) {
        List<SchoolMedia> photoGalleryImages = getSchoolPhotos(school);
        model.put("basePhotoPath",CommunityUtil.getMediaPrefix());
        model.put("photoGalleryImages",photoGalleryImages);
        if (model.get("validUser") != null){
            User user = (User) model.get("validUser");
            model.put("photoReports", getReportsForSchoolMedia(user, photoGalleryImages));
        }

    }
    
    protected List<SchoolMedia> getSchoolPhotos(School school) {
        ISchoolMediaDao schoolMediaDao = getSchoolMediaDao();
        List<SchoolMedia> schoolPhotos = schoolMediaDao.getActiveBySchool(school,MAX_SCHOOL_PHOTOS_IN_GALLERY);
        return schoolPhotos;
    }

    private Map<Integer, Boolean> getReportsForSchoolMedia(User user, List<SchoolMedia> schoolMediaList) {
        if (schoolMediaList == null || user == null) {
            return null;
        }
        Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(schoolMediaList.size());
        for (SchoolMedia schoolMedia: schoolMediaList) {
            reports.put(schoolMedia.getId(),
                    _reportedEntityDao.hasUserReportedEntity
                            (user, ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId()));
        }
        return reports;
    }

    public static String getMediaPrefix() {
        return CommunityUtil.getMediaPrefix();
    }

    protected boolean shouldIndex(School school, Long numberOfReviews) {
        if (school == null) {
            return false;
        }

        return true;
    }

    public void populateModelWithSchoolHighlights(School school, Map<String, Object> model) {

        //I want guaranteed ordering otherwise the capitalization and concatenation of the items in the collection gets tricky
        Set<String> highlights = new TreeSet();

        if (LevelCode.PRESCHOOL.equals(school.getLevelCode()) && StringUtils.trimToNull(school.getAgeRangeAsString()) != null) {
            highlights.add("Accepts " + StringUtils.trim(school.getAgeRangeAsString()));
        }

        String subtypesCSL = SchoolSubtype.getSubtypes(school);
        if (subtypesCSL != null) {
            String[] subtypes = StringUtils.split(subtypesCSL, ',');
            for (String subtype : subtypes) {
                highlights.add(StringUtils.capitalize(StringUtils.trim(subtype)));
            }
        }

        if (StringUtils.trimToNull(school.getAffiliation()) != null) {
            highlights.add(StringUtils.capitalize(school.getAffiliation()));
        }

        if (StringUtils.trimToNull(school.getAssociation()) != null) {
            highlights.add("Associations: " + school.getAssociation());
        }

        for (String answer : SURVEY_ANSWERS_TO_SAMPLE) {
            String token = StringUtils.trimToNull(getOneResponseTokenForAnswer(school, answer));
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

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
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

    public NearbySchoolsHelper getNearbySchoolsHelper() {
        return _nearbySchoolsHelper;
    }

    public void setNearbySchoolsHelper(NearbySchoolsHelper nearbySchoolsHelper) {
        _nearbySchoolsHelper = nearbySchoolsHelper;
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ISchoolMediaDao getSchoolMediaDao() {
        return _schoolMediaDao;
    }

    public void setSchoolMediaDao(ISchoolMediaDao schoolMediaDao) {
        _schoolMediaDao = schoolMediaDao;
    }

    public IReportedEntityDao getReportedEntityDao() {
        return _reportedEntityDao;
    }

    public void setReportedEntityDao(IReportedEntityDao reportedEntityDao) {
        _reportedEntityDao = reportedEntityDao;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}