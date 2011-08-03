/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: UrlBuilder.java,v 1.255 2011/08/03 23:17:36 aroy Exp $
 */

package gs.web.util;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.*;
import gs.data.geo.ICity;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.data.util.Address;
import gs.data.util.CmsUtil;
import gs.data.util.SpringUtil;
import gs.data.community.User;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.widget.SchoolSearchWidgetController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.*;

/**
 * Provides a builder utility for our URLs. Deals with the intricacies of:
 * <ol>
 * <li>URL encoding
 * <li>modifying an existing URL string or parameters
 * <li>multiple parameters with the same name
 * <li>deleting parameters
 * <li>our specific pages, or "vpages". This provides a centralized place to
 * create URLs. These are fundamentally separate concepts, but they are
 * intertwined here, at least for the time being.
 * </ol>
 * </p>
 * In Java code, use this class to build URLs. On Jsps and within Tag files,
 * use the "link" taglib, which is a thin wrapper around this class.
 * </p>
 * Test coverage should be near 100% for this class, as its functionality is
 * quite critical.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 * @see <a href="http://www.rfc-editor.org/rfc/rfc1738.txt">RFC 1738</a>
 * @see UrlUtil
 * @see gs.web.jsp.link
 * @see gs.web.jsp.link.LinkTagHandler
 */
public class UrlBuilder {

    private static final Log _log = LogFactory.getLog(UrlBuilder.class);


    /**
     * Path relative to the host/context.
     */
    private String _path;
    private Map _parameters;
    private boolean _perlPage = false;
    private VPage _vPage; // used for some urls
    private static UrlUtil _urlUtil = new UrlUtil();

    /**
     * Provides type-safety for identifying our unique "pages".
     */
    public static class VPage {
        private String _name;

        protected VPage(String s) {
            _name = s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VPage vPage = (VPage) o;

            return _name.equals(vPage._name);
        }

        @Override
        public int hashCode() {
            return _name.hashCode();
        }
    }

    /**
     * A page that provides an overview of our entire article library.
     */
    public static final VPage ARTICLE_LIBRARY = new VPage("vpage:articleLibrary");
    public static final VPage GLOSSARY_TERM = new VPage("vpage:glossaryTerm");

    public static final VPage CITY_PAGE = new VPage("vpage:city");
    public static final VPage CITIES = new VPage("vpage:cities"); // all the cities in a state
    public static final VPage CITIES_MORE_NEARBY = new VPage("vpage:moreNearbyCities");

    public static final VPage CONTACT_US = new VPage("vpage:contactUs");
    public static final VPage TERMS_OF_USE = new VPage("vpage:termsOfUse");

    public static final VPage DISTRICT_HOME = new VPage("vpage:districtHome");
    public static final VPage DISTRICT_PROFILE = new VPage("vpage:districtProfile");

    public static final VPage SCHOOL_SEARCH = new VPage("vpage:schoolSearch");
    public static final VPage ARTICLE_SEARCH = new VPage("vpage:articleSearch");
    public static final VPage CONTENT_SEARCH = new VPage("vpage:contentSearch");

    /**
     * This page lists all districts in a state
     */
    public static final VPage DISTRICTS_PAGE = new VPage("vpage:districts");

    public static final VPage HOME = new VPage("vpage:home");

    /**
     * Allows a user to edit and create school lits.
     */
    public static final VPage MY_SCHOOL_LIST = new VPage("vpage:mySchoolList");
    public static final VPage MY_SCHOOL_LIST_LOGIN = new VPage("vpage:mySchoolListLogin");

    /**
     * Manage new or existing subscriptions.
     */
    public static final VPage NEWSLETTER_MANAGEMENT = new VPage("vpage:newsletterManagement");
    public static final VPage NEWSLETTER_UNSUBSCRIBE = new VPage("vpage:newsletterUnsubscribe");

    /**
     * New state page: research and compare, with optional state.
     */
    public static final VPage RESEARCH = new VPage("vpage:research");
    public static final VPage FIND_A_SCHOOL = new VPage("vpage:findASchool");

    public static final VPage LOGIN_OR_REGISTER = new VPage("vpage:loginOrRegister");
    public static final VPage REGISTRATION = new VPage("vpage:registration");
    public static final VPage REGISTRATION_REMOVE = new VPage("vpage:registrationRemove");
    public static final VPage REGISTRATION_VALIDATION = new VPage("vpage:registrationValidation");
    public static final VPage FORGOT_PASSWORD = new VPage("vpage:forgotPassword");
    public static final VPage RESET_PASSWORD = new VPage("vpage:resetPassword");
    public static final VPage REQUEST_EMAIL_VALIDATION = new VPage("vpage:requestEmailValidation");
    public static final VPage COMMUNITY = new VPage("vpage:community");
    public static final VPage COMMUNITY_LANDING = new VPage("vpage:communityLanding");
    public static final VPage CHANGE_EMAIL = new VPage("vpage:changeEmail");
    public static final VPage ACCOUNT_INFO = new VPage("vpage:accountInfo");
    public static final VPage USER_PROFILE = new VPage("vpage:userProfile");
    public static final VPage USER_ACCOUNT = new VPage("vpage:userAccount");
    public static final VPage RECENT_ACTIVITY = new VPage("vpage:recentActivity");

    /**
     * school profile pages
     */
    public static final VPage SCHOOL_PROFILE = new VPage("vpage:schoolProfile");
    public static final VPage SCHOOL_PARENT_REVIEWS_WITH_HOVER = new VPage("vpage:schoolParentReviewsWithHover");
    public static final VPage SCHOOL_PARENT_REVIEWS = new VPage("vpage:schoolParentReviews");
    public static final VPage SCHOOL_PROFILE_TEST_SCORE = new VPage("vpage:schoolTestscores");
    public static final VPage SCHOOL_PROFILE_CENSUS = new VPage("vpage:schoolCensus");
    public static final VPage SCHOOL_PROFILE_CENSUS_PRIVATE = new VPage("vpage:schoolCensusPrivate");
    public static final VPage SCHOOL_PROFILE_PRIVATE_QUICK_FACTS = new VPage("vpage:schoolPrivateQuickFacts");
    public static final VPage SCHOOL_PROFILE_PRINCIPAL_VIEW = new VPage("vpage:schoolPrincipalView");
    public static final VPage SCHOOL_PROFILE_RATINGS = new VPage("vpage:schoolRatings");
    public static final VPage SCHOOL_PROFILE_ADD_PARENT_REVIEW = new VPage("vpage:schoolAddParentReview");
    public static final VPage SCHOOL_PROFILE_ESP_LOGIN = new VPage("vpage:schoolEspLogin");
    public static final VPage SCHOOL_PROFILE_ESP = new VPage("vpage:schoolEspView");
    public static final VPage SCHOOL_AUTHORIZER = new VPage("vpage:schoolAuthorizer");

    public static final VPage SCHOOL_START_SURVEY = new VPage("vpage:schoolStartSurvey");
    public static final VPage SCHOOL_TAKE_SURVEY = new VPage("vpage:schoolTakeSurvey");
    public static final VPage START_SURVEY_RESULTS = new VPage("vpage:startSurveyResults");
    public static final VPage SURVEY_RESULTS = new VPage("vpage:surveyResults");

    public static final VPage SCHOOL_MAP = new VPage("vpage:schoolMap");

    public static final VPage COMPARE_SCHOOL = new VPage("vpage:compareSchool");
    public static final VPage COMPARE_SCHOOLS_OVERVIEW = new VPage("vpage:compareSchoolsOverview");
    public static final VPage COMPARE_SCHOOLS_RATINGS = new VPage("vpage:compareSchoolsRatings");
    public static final VPage COMPARE_SCHOOLS_TEST_SCORES = new VPage("vpage:compareTestScores");
    public static final VPage COMPARE_SCHOOLS_STUDENT_TEACHER = new VPage("vpage:compareSchoolsStudentTeacher");
    public static final VPage COMPARE_SCHOOLS_PROGRAMS_EXTRACURRICULARS = new VPage("vpage:compareSchoolsProgramsExtracurriculars");
    public static final VPage COMPARE_SCHOOLS_MAP = new VPage("vpage:compareSchoolsMap");

    public static final VPage SCHOOLS_IN_CITY = new VPage("vpage:schoolsInCity");
    public static final VPage SCHOOLS_IN_DISTRICT = new VPage("vpage:schoolsInDistrict");
    public static final VPage SCHOOLS_IN_STATE = new VPage("vpage:schoolsInState");

    public static final VPage ABOUT_US = new VPage("vpage:aboutUs");
    public static final VPage BOARD_OF_DIRECTORS = new VPage("vpage:boardOfDirectors");
    public static final VPage ADVISORS = new VPage("vpage:advisors");
    public static final VPage SENIOR_MANAGEMENT = new VPage("vpage:seniorManagement");
    public static final VPage PRIVACY_POLICY = new VPage("vpage:privacyPolicy");
    public static final VPage PRESS_ROOM = new VPage("vpage:pressRoom");
    public static final VPage PRESS_RELEASES = new VPage("vpage:pressReleases");
    public static final VPage PRESS_CLIPPINGS = new VPage("vpage:pressClippings");

    public static final VPage BETA_SIGNUP = new VPage("vpage:betaSignup");
    public static final VPage BETA_UNSUBSCRIBE = new VPage("vpage:betaUnsubscribe");

    public static final VPage SIGN_IN = new VPage("vpage:signIn");
    public static final VPage SIGN_OUT = new VPage("vpage:signOut");

    public static final VPage ADMIN_NEWS_ITEMS = new VPage("vpage:newItems");
    public static final VPage ADMIN_NEWS_ITEMS_CREATE = new VPage("vpage:newItemsCreate");
    public static final VPage ADMIN_NEWS_ITEMS_DELETE = new VPage("vpage:newItemsDelete");

    public static final VPage B2S_POLL_LANDING_PAGE = new VPage("vpage:b2sPollLandingPage");

    /**
     * Page that allows users to search for a school in order to add a parent review
     */
    public static final VPage ADD_PARENT_REVIEW_SEARCH = new VPage("vpage:addParentReviewSearch");

    /**
     * number1schools cobrand leadgen page
     */
    public static final VPage GET_BIREG = new VPage("vpage:getBireg");

    /**
     * webby award thank you page
     */
    public static final VPage WEBBY_AWARD_THANKS = new VPage("vpage:webbyAwardThanks");

    /**
     * parent review info pages*
     */
    public static final VPage PARENT_REVIEW_GUIDELINES = new VPage("vpage:parentReviewGuidelines");
    public static final VPage PARENT_REVIEW_LEARN_MORE = new VPage("vpage:parentReviewLearnMore");
    public static final VPage PARENT_RATING_EXPLAINED = new VPage("vpage:parentRatingExplained");
    public static final VPage PARENT_RATING_PRESCHOOL_EXPLAINED = new VPage("vpage:parentRatingPreschoolExplained");

    /**
     * Editorial Microsites
     */
    public static final VPage SCHOOL_CHOICE_CENTER = new VPage("vpage:schoolChoiceCenter");
    public static final VPage HEALTHY_KIDS = new VPage("vpage:healthyKids");
    public static final VPage SPECIAL_NEEDS = new VPage("vpage:specialNeeds");
    public static final VPage MEDIA_CHOICES = new VPage("vpage:mediaChoices");
    public static final VPage MOVING_WITH_KIDS = new VPage("vpage:movingWithKids");
    public static final VPage COUNTDOWN_TO_COLLEGE = new VPage("vpage:countdownToCollege");
    public static final VPage HOLIDAY_LEARNING = new VPage("vpage:holidayLearning");
    public static final VPage STATE_STANDARDS = new VPage("vpage:stateStandards");
    public static final VPage SUMMER_PLANNING = new VPage("vpage:summerPlanning");
    public static final VPage SUMMER_READING = new VPage("vpage:summerReading");
    public static final VPage BACK_TO_SCHOOL = new VPage("vpage:backToSchool");
    public static final VPage TUTORING = new VPage("vpage:tutoring");
    public static final VPage TRAVEL = new VPage("vpage:travel");
    public static final VPage PRESCHOOL = new VPage("vpage:preschool");
    public static final VPage ELEMENTARY_SCHOOL = new VPage("vpage:elementarySchool");
    public static final VPage MIDDLE_SCHOOL = new VPage("vpage:middleSchool");
    public static final VPage HIGH_SCHOOL = new VPage("vpage:highSchool");
    public static final VPage COLLEGE = new VPage("vpage:college");

    /**
     * Elementary school grade topic centers
     */
    public static final VPage KINDERGARTEN = new VPage("vpage:kindergarten");
    public static final VPage FIRST_GRADE = new VPage("vpage:firstGrade");
    public static final VPage SECOND_GRADE = new VPage("vpage:secondGrade");
    public static final VPage THIRD_GRADE = new VPage("vpage:thirdGrade");
    public static final VPage FOURTH_GRADE = new VPage("vpage:fourthGrade");
    public static final VPage FIFTH_GRADE = new VPage("vpage:fifthGrade");

    /**
     * test score landing page
     */
    public static final VPage TEST_SCORE_LANDING = new VPage("vpage:testScoreLanding");

    /**
     * submit school pages
     */
    public static final VPage SUBMIT_PRESCHOOL = new VPage("vpage:submitPreschool");
    public static final VPage SUBMIT_PRIVATE_SCHOOL = new VPage("vpage:submitPrivateSchool");

    /**
     * browse pages
     */
    public static final VPage BROWSE_PRESCHOOLS = new VPage("vpage:browsePreschools");

    public static final VPage DONORS_CHOOSE_EXPLAINED = new VPage("vpage:donorsChooseExplained");

    public static final VPage SUBMIT_PARENT_REVIEW_PRESCHOOL = new VPage("vpage:submitParentReviewPreschool");

    public static final VPage SCHOOL_FINDER_CUSTOMIZATION = new VPage("vpage:schoolFinderCustomization");

    public static final VPage SCHOOL_FINDER_WIDGET = new VPage("vpage:schoolFinderWidget");

    public static final VPage CMS_CATEGORY_BROWSE = new VPage("vpage:cmsCategoryBrowse");
    public static final VPage CMS_VIDEO_GALLERY = new VPage("vpage:cmsVideoGallery");
    public static final VPage CMS_WORKSHEET_GALLERY = new VPage("vpage:cmsWorksheetGallery");

    public static final VPage K12_ADVERTISER_PAGE = new VPage("vpage:k12AdvertiserPage");

    /**
     * Api Pages
     */
    public static final VPage API_ADMIN_LOGIN = new VPage("vpage:apiAdminLogin");
    public static final VPage API_DOCS_MAIN = new VPage("vpage:apiDocsMain");

    /**
     * Community Pages
     */
    public static final VPage COMMUNITY_DISCUSSION = new VPage("vpage:communityDiscussion");
    public static final VPage COMMUNITY_DISCUSSION_BOARD = new VPage("vpage:communityDiscussionBoard");
    public static final VPage RAISE_YOUR_HAND_LANDING = new VPage("vpage:raiseYourHandLanding");
    public static final VPage RAISE_YOUR_HAND_FEATURED_QUESTIONS = new VPage("vpage:raiseYourHandFeaturedQuestions");
    public static final VPage RECENT_CONVERSATIONS = new VPage("vpage:recentConversations");

    private static void populateVPageNameMap() {
        Field[] fields = UrlBuilder.class.getFields();

        for (Field f : fields) {
            int modifier = f.getModifiers();
            if (f.getType() == VPage.class && Modifier.isFinal(modifier) && Modifier.isStatic(modifier)) {
                try {
                    vpageConstantObjectMap.put(f.getName(), (VPage) f.get(null));
                } catch (IllegalAccessException e) {
                    _log.debug("Problem when trying to access static field on UrlBuilder", e);
                    //nothing else to do
                }
            }
        }
    }

    /**
     * For converting from constant names to the corresponding VPage constants
     */
    private static Map<String,VPage> vpageConstantObjectMap = new HashMap<String,VPage>();
    static {
        populateVPageNameMap();
        vpageConstantObjectMap.put("B2S_POLL_LANDING_PAGE",B2S_POLL_LANDING_PAGE);
        vpageConstantObjectMap.put("HEALTHY_KIDS",HEALTHY_KIDS);
        vpageConstantObjectMap.put("MEDIA_CHOICES",MEDIA_CHOICES);
        vpageConstantObjectMap.put("SCHOOL_CHOICE_CENTER",SCHOOL_CHOICE_CENTER);
        vpageConstantObjectMap.put("MOVING_WITH_KIDS",MOVING_WITH_KIDS);
        vpageConstantObjectMap.put("BACK_TO_SCHOOL",BACK_TO_SCHOOL);
        vpageConstantObjectMap.put("ELEMENTARY_SCHOOL",ELEMENTARY_SCHOOL);
    }

    public static VPage getVPage(String constantName) {
        return vpageConstantObjectMap.get(constantName);
    }

    /**
     * Creates a builder to the page specified in the provided URL code. This only supports the
     * pages needed for the CMS.
     */
    public UrlBuilder(String gsUrl) {
        int paramIndex = gsUrl.indexOf("?");
        String vpageName;
        Map<String, String> params = new HashMap<String, String>(0);
        if (paramIndex > 0) {
            vpageName = gsUrl.substring(0, paramIndex);
            // want part after the ?
            params = UrlUtil.getParamsFromQueryString(gsUrl.substring(paramIndex + 1));
        } else {
            vpageName = gsUrl;
        }
        VPage vPage = new VPage("vpage:" + vpageName);

        if (HOME.equals(vPage)) {
            init(HOME, null, null);
        } else if (SCHOOL_PROFILE.equals(vPage)) {
            State state = State.fromString(params.get("state"));
            String schoolIdStr = params.get("id");
            ISchoolDao schoolDao = (ISchoolDao) SpringUtil.getApplicationContext().getBean(ISchoolDao.BEAN_ID);
            School school = schoolDao.getSchoolById(state, Integer.parseInt(schoolIdStr));
            handleSchoolProfile(school, false);
        } else if (RESEARCH.equals(vPage)) {
            State state = null;
            if (params.get("state") != null) {
                try {
                    state = State.fromString(params.get("state"));
                } catch (IllegalArgumentException iae) {
                    _log.warn("Cannot find state " + params.get("state"));
                }
            }
            init(RESEARCH, state, null);
        } else if (DISTRICT_PROFILE.equals(vPage)) {
            State state = State.fromString(params.get("state"));
            String districtIdStr = params.get("id");
            init(DISTRICT_PROFILE, state, districtIdStr);
        } else if (CITY_PAGE.equals(vPage)) {
            State state = State.fromString(params.get("state"));
            String city = params.get("city");
            init(CITY_PAGE, state, city);
        } else if (TEST_SCORE_LANDING.equals(vPage)) {
            State state = State.fromString(params.get("state"));
            init(TEST_SCORE_LANDING, state, params.get("tid"));
        } else if (checkMicrosites(vPage)) {
            // good
        } else {
            throw new IllegalArgumentException("VPage unknown: " + vpageName);
        }
    }

    protected boolean checkMicrosites(VPage vpage) {
        try {
            init(vpage);
            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    /**
     * Create a builder to the given site page.
     *
     * @param request             the current request
     * @param contextRelativePath the requested page. Null asks for the same page, but it may or
     *                            may not work. It would be great if you could always generate the link to the
     *                            current page, but unfortunately this isn't available at all points.
     *                            Tomcat, or possibly spring, has decorated the request so that it
     *                            doesn't point to what the user really asked for. If you're in the midst of processing a page, it now points to the
     *                            Jsp page that is being shown, not the user's request. It does seem to work in the controller, though.
     *                            I know of one solution that I haven't implemented here:
     *                            Grab the path earlier on in the servlet processing
     *                            and stash it away for later retrieval.
     * @deprecated use VPage-oriented methods when possible
     */
    public UrlBuilder(HttpServletRequest request, String contextRelativePath) {
        _path = contextRelativePath;
        if (contextRelativePath == null) {
            _path = request.getRequestURI();
            _path = StringUtils.removeStart(_path, request.getContextPath());
        } else {
            _path = contextRelativePath;
        }
        _perlPage = _urlUtil.smellsLikePerl(_path);
        // _log.error("PathInfo="+request.getPathInfo()); // yields null
        // _log.error("PathTranslated="+request.getPathTranslated()); // yields null
        // _log.error("ServletPath="+request.getServletPath()); // yields "/WEB-INF/page/search/schoolsOnly.jspx"
        // _log.error("requestURI="+request.getRequestURI()); // yields "/gs-web/WEB-INF/page/search/schoolsOnly.jspx"
        // _log.error("requestURL="+request.getRequestURL()); // yields "http://apeterson.office.greatschools.org:8080/gs-web/WEB-INF/page/search/schoolsOnly.jspx"
    }

    // WARNING: THIS IS SLIGHTLY INCORRECT - IT ONLY WORKS FOR ARTICLES AND ASK THE EXPERTS
    public UrlBuilder(Integer contentId, boolean featured) {
        boolean useLegacyArticle = true;
        Publication publication = null;

        // if CMS is enabled and we can find a publication associated with that legacy article ID, then
        // generate a url to the CMS-driven content
        // otherwise, fall back to serving the legacy article url (/cgi-bin/showarticle/X/Y)
        // note: if the article should be served by the legacy cms, don't bother trying to get it the new cms version
        if (CmsUtil.isCmsEnabled() && !CmsConstants.isArticleServedByLegacyCms(contentId)) {
            publication = getPublication(contentId);
            useLegacyArticle = (publication == null);
        }

        if (useLegacyArticle) {
            initializeForLegacyArticle(contentId, featured);
        } else {
            initializeForCmsContent(publication.getContentKey(), publication.getFullUri());
        }
    }

    public Publication getPublication(Integer legacyId) {
        return getPublicationDao().findByLegacyId(new Long(legacyId));
    }

    private IPublicationDao getPublicationDao() {
        return (IPublicationDao) SpringUtil.getApplicationContext().getBean("publicationDao");
    }

    private void initializeForLegacyArticle(Integer articleId, boolean featured) {
        _perlPage = true;

        // Calculate page to use
        String page;
        if (featured) {
            page = "showarticlefeature";
        } else {
            page = "showarticle";
        }

        _path = "/cgi-bin/" +
                page +
                "/" +
                articleId;
    }

    public UrlBuilder(ContentKey contentKey) {
        if (!CmsUtil.isCmsEnabled()) {
            throw new UnsupportedOperationException("Attempting to display CMS Content when CMS is disabled.");
        }

        Publication publication = getPublicationDao().findByContentKey(contentKey);
        if (publication != null) {
            initializeForCmsContent(contentKey, publication.getFullUri());
        }
    }

    public UrlBuilder(ContentKey contentKey, String fullUri, Boolean raiseYourHand, String page) {
        if (!CmsUtil.isCmsEnabled()) {
            throw new UnsupportedOperationException("Attempting to display CMS Content when CMS is disabled.");
        }

        initializeForCmsContent(contentKey, fullUri, raiseYourHand, page);
    }

    public UrlBuilder(ContentKey contentKey, String fullUri, Boolean raiseYourHand) {
        if (!CmsUtil.isCmsEnabled()) {
            throw new UnsupportedOperationException("Attempting to display CMS Content when CMS is disabled.");
        }

        initializeForCmsContent(contentKey, fullUri, raiseYourHand, null);
    }

    public UrlBuilder(ContentKey contentKey, String fullUri) {
        if (!CmsUtil.isCmsEnabled()) {
            throw new UnsupportedOperationException("Attempting to display CMS Content when CMS is disabled.");
        }

        initializeForCmsContent(contentKey, fullUri, false, null);
    }

    private void initializeForCmsContent(ContentKey contentKey, String fullUri) {
        initializeForCmsContent(contentKey, fullUri, false, null);
    }

    // unfortunately, changes to this also necessitate changes to PublicationDao.populateHttpUrlForContentUrl()
    // WARNING: if this changes, GSFeed's SiteMapFeedGenerator needs to be changed too!!!
    private void initializeForCmsContent(ContentKey contentKey, String fullUri, Boolean raiseYourHand, String page) {
        _perlPage = false;
        if (fullUri != null) {
            _path = CmsUtil.getUri(contentKey, fullUri, raiseYourHand);
            // GS-11495
            if (!CmsUtil.isCmsFeature(contentKey) && !CmsUtil.hasSpecialCaseUrl(contentKey)) {
                setParameter("content", contentKey.getIdentifier().toString());
            }

            if (page != null) {
                setParameter("page", page);
            }
        } else {
            _path = "";
        }
    }

    public UrlBuilder(State state, Integer schoolId, VPage page) {
        if (SCHOOL_PARENT_REVIEWS.equals(page)) {
            _perlPage = false;
            _path = "/school/parentReviews.page";
            // http://stackoverflow.com/questions/3131865/why-does-string-valueofnull-throw-a-nullpointerexception
            setParameter("id", String.valueOf((Object)schoolId));
            if (state != null) {
                setParameter("state", state.getAbbreviation());
            }
        } else if (SCHOOL_PROFILE_RATINGS.equals(page)) {
            _perlPage = false;
            _path = "/school/rating.page";
            // http://stackoverflow.com/questions/3131865/why-does-string-valueofnull-throw-a-nullpointerexception
            setParameter("id", String.valueOf((Object)schoolId));
            if (state != null) {
                setParameter("state", state.getAbbreviation());
            }
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(School school, VPage page) {
        if (SCHOOL_PROFILE.equals(page)) {
            handleSchoolProfile(school, false);
        } else if (SCHOOL_PARENT_REVIEWS_WITH_HOVER.equals(page)) {
            _perlPage = false;
            _path = "/school/parentReviews.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
            setParameter("showThankyouHover", "true");
        }  else if (SCHOOL_PARENT_REVIEWS.equals(page)) {
            _perlPage = false;
            _path = "/school/parentReviews.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        }else if (SCHOOL_PROFILE_CENSUS.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/other/" +
                    school.getId();
        } else if (SCHOOL_PROFILE_CENSUS_PRIVATE.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/otherprivate/" +
                    school.getId();
        } else if (SCHOOL_PROFILE_PRIVATE_QUICK_FACTS.equals(page)) {
            _perlPage = true;
            _path = "/modperl/quickprivate/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/" +
                    school.getId();
        } else if (SCHOOL_PROFILE_TEST_SCORE.equals(page)) {
            _perlPage = true;
            _path = "/modperl/achievement/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/" +
                    school.getId();
        } else if (SCHOOL_PROFILE_PRINCIPAL_VIEW.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/pqview/" +
                    school.getId();
        } else if (SCHOOL_PROFILE_RATINGS.equals(page)) {
            State state = school.getDatabaseState();
            _perlPage = false;
            _path = "/school/rating.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", state.getAbbreviation());
        } else if (SCHOOL_PROFILE_ADD_PARENT_REVIEW.equals(page)) {
            _perlPage = false;
            _path = "/school/addComments.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else if (COMPARE_SCHOOL.equals(page)) {
            //href="/cgi-bin/cs_compare/ca/
            _perlPage = true;
            _path = "/cgi-bin/cs_compare/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/";
        } else if (SCHOOL_AUTHORIZER.equals(page)) {
            _perlPage = false;
            _perlPage = false;
            _path = "/school/authorizers.page";
            setParameter("school", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else if (SCHOOL_MAP.equals(page)) {
            _perlPage = false;
            _path = "/school/mapSchool.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else if (SCHOOL_PROFILE_ESP_LOGIN.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/pq_start.cgi/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/" + school.getId();
        } else if (SCHOOL_PROFILE_ESP.equals(page)) {
            _perlPage = false;
            _path = "/cgi-bin/" + school.getDatabaseState().getAbbreviationLowerCase() +
                    "/pqview/" + school.getId();
        } else if (SCHOOL_TAKE_SURVEY.equals(page)) {
            _perlPage = false;
            _path = "/survey/form.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else if (SCHOOL_START_SURVEY.equals(page)) {
            _perlPage = false;
            _path = "/survey/start.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else if (START_SURVEY_RESULTS.equals(page)) {
            _perlPage = false;
            _path = "/survey/startResults.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else if (SURVEY_RESULTS.equals(page)) {
            _perlPage = false;
            _path = "/survey/results.page";
            setParameter("id", String.valueOf(school.getId().intValue()));
            setParameter("state", school.getDatabaseState().getAbbreviation());
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(District district, LevelCode levelCode, VPage page) {
        if (SCHOOLS_IN_DISTRICT.equals(page)) {
            _perlPage = false;
            _path = DirectoryStructureUrlFactory.createNewDistrictBrowseURI(district.getDatabaseState(), district, levelCode);
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(District district, VPage page) {
        if (DISTRICT_PROFILE.equals(page)) {
            _perlPage = true;

            _path = "/cgi-bin/" +
                    district.getDatabaseState().getAbbreviationLowerCase() +
                    "/district-profile/" +
                    district.getId();
        } else if (DISTRICT_HOME.equals(page)) {
            _perlPage = false;
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = DirectoryStructureUrlFactory.createNewDistrictHomeURI(district.getDatabaseState(), district);
        } else if (SCHOOLS_IN_DISTRICT.equals(page)) {
            _perlPage = false;
            _path = DirectoryStructureUrlFactory.createNewDistrictBrowseURI(district.getDatabaseState(), district, null);
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(State state, Integer districtId, String districtName, String districtCity, LevelCode levelCode, VPage page) {
        if (SCHOOLS_IN_DISTRICT.equals(page)) {
            _perlPage = false;
            _path = DirectoryStructureUrlFactory.createNewDistrictBrowseURI(state, districtId, districtName, districtCity, levelCode);
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(State state, Integer districtId, String districtName, String districtCity, VPage page) {
        if (DISTRICT_HOME.equals(page)) {
            _perlPage = false;
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = DirectoryStructureUrlFactory.createNewDistrictHomeURI(state, districtId, districtName, districtCity);
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(ICity city, VPage page) {
        _vPage = page;
        if (CITY_PAGE.equals(page)) {
            _perlPage = false;
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(city.getState(), city.getName());
        } else if (CITIES_MORE_NEARBY.equals(page)) {
            _perlPage = false;
            _path = "/cities.page";
            this.setParameter("city", city.getName());
            this.setParameter("state", city.getState().getAbbreviation());
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(String stringParam, VPage page) {
        _vPage = page;
        if (USER_PROFILE.equals(page)) {
            // stringParam needs to be username
            _perlPage = false;
            _path = "/members/" + stringParam + "/";
        } else if (K12_ADVERTISER_PAGE.equals(page)) {
            // stringParam needs to be k12School param, e.g. CA or INT
            _perlPage = false;
            _path = "/online-education.page?school=" + stringParam;
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(User user, VPage page) {
        _vPage = page;
        if (USER_PROFILE.equals(page)) {
            _perlPage = false;
            _path = "/members/" + user.getUserProfile().getScreenName() + "/";
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, State state) {
        init(page, state, null);
    }

    /**
     * for pages that do not require parameters in URL.  Default is perlpage is false.
     *
     * @param page VPage
     */
    public UrlBuilder(VPage page) {
        init(page);
    }

    public UrlBuilder(VPage page, String fullUri, Long contentIdentifier) {
        _perlPage = false;
        if (COMMUNITY_DISCUSSION.equals(page)) {
            _path = fullUri + "/community/discussion.gs";
            setParameter("content", String.valueOf(contentIdentifier));
        } else if (COMMUNITY_DISCUSSION_BOARD.equals(page)) {
            _path = fullUri + "/community.gs";
            setParameter("content", String.valueOf(contentIdentifier));
        }
    }

    protected void init(VPage page) {
        _perlPage = false;

        if (SCHOOL_CHOICE_CENTER.equals(page)) {
            _path = "/school-choice/";
        } else if (HEALTHY_KIDS.equals(page)) {
            _path = "/healthy-kids.topic?content=2504";
        } else if (SPECIAL_NEEDS.equals(page)) {
            _path = "/LD.topic?content=1541";
        } else if (KINDERGARTEN.equals(page)) {
            _path = CmsConstants.KINDERGARTEN_TOPIC_CENTER_PATH;
        } else if (FIRST_GRADE.equals(page)) {
            _path = CmsConstants.FIRST_GRADE_TOPIC_CENTER_PATH;
        } else if (SECOND_GRADE.equals(page)) {
            _path = CmsConstants.SECOND_GRADE_TOPIC_CENTER_PATH;
        } else if (THIRD_GRADE.equals(page)) {
            _path = CmsConstants.THIRD_GRADE_TOPIC_CENTER_PATH;
        } else if (FOURTH_GRADE.equals(page)) {
            _path = CmsConstants.FOURTH_GRADE_TOPIC_CENTER_PATH;
        } else if (FIFTH_GRADE.equals(page)) {
            _path = CmsConstants.FIFTH_GRADE_TOPIC_CENTER_PATH;
        } else if (MEDIA_CHOICES.equals(page)) {
            _path = "/media-choices.topic?content=2439";
        } else if (MOVING_WITH_KIDS.equals(page)) {
            _path = "/moving.topic?content=2220";
        } else if (COUNTDOWN_TO_COLLEGE.equals(page)) {
            _path = "/college/";
        } else if (HOLIDAY_LEARNING.equals(page)) {
            _path = "/content/holidayLearning.page";
        } else if (STATE_STANDARDS.equals(page)) {
            _path = "/content/stateStandards.page";
        } else if (SUMMER_PLANNING.equals(page)) {
            _path = "/content/summerPlanning.page";
        } else if (SUMMER_READING.equals(page)) {
            _path = "/content/summerReading.page";
        } else if (BACK_TO_SCHOOL.equals(page)) {
            _path = "/content/backToSchool.page";
        } else if (TUTORING.equals(page)) {
            _path = "/homework-help.topic?content=1544";
        } else if (TRAVEL.equals(page)) {
            _path = "/content/travel.page";
        } else if (MY_SCHOOL_LIST.equals(page)) {
            _path = "/mySchoolList.page";
        } else if (MY_SCHOOL_LIST_LOGIN.equals(page)) {
            _path = "/mySchoolListLogin.page";
        } else if (PRESCHOOL.equals(page)) {
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = "/preschool/";
        } else if (ELEMENTARY_SCHOOL.equals(page)) {
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = "/elementary-school/";
        } else if (MIDDLE_SCHOOL.equals(page)) {
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = "/middle-school/";
        } else if (HIGH_SCHOOL.equals(page)) {
            // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = "/high-school/";
        } else if (COLLEGE.equals(page)) {
             // WARNING: if this url changes, also change the entry in CmsUtil.GRADE_LEVEL_TOPIC_CENTER_URI_MAP
            _path = "/college/";
        } else if (DONORS_CHOOSE_EXPLAINED.equals(page)) {
            _path = "/content/donorsChooseExplained.html";
        } else if (SUBMIT_PARENT_REVIEW_PRESCHOOL.equals(page)) {
            _path = "/school/parentReviews/submit.page";
        } else if (SCHOOL_FINDER_CUSTOMIZATION.equals(page)) {
            _path = "/schoolfinder/widget/customize.page";
        } else if (SCHOOL_FINDER_WIDGET.equals(page)) {
            _path = SchoolSearchWidgetController.BEAN_ID;
        } else if (B2S_POLL_LANDING_PAGE.equals(page)) {
            _path = "/news/back-to-school-poll-results.page";
        } else if (COMMUNITY_DISCUSSION.equals(page)) {
            _path = "/community/discussion.gs";
        } else if (ABOUT_US.equals(page)) {
            // WARNING: if this changes, GSFeed's SiteMapFeedGenerator needs to be changed too!!!
            _path = "/about/aboutUs.page";
        } else if (BOARD_OF_DIRECTORS.equals(page)) {
            _path = "/about/boardOfDirectors.page";
        } else if (ADVISORS.equals(page)) {
            _path = "/about/advisors.page";
        } else if (SENIOR_MANAGEMENT.equals(page)) {
            _path = "/about/senior-management.page";
        } else if (COMMUNITY_LANDING.equals(page)) {
            _path = "/community/";
        } else if (RAISE_YOUR_HAND_LANDING.equals(page)) {
            _path = "/community/questions/";
        } else if (RAISE_YOUR_HAND_FEATURED_QUESTIONS.equals(page)) {
            _path = "/featured/questions/";
        } else if (RECENT_CONVERSATIONS.equals(page)) {
            _path = "/community/activity/";
        } else if (CONTACT_US.equals(page)) {
            _perlPage = true;
            _path = "/about/feedback.page";
        } else if (COMPARE_SCHOOLS_OVERVIEW.equals(page)) {
            _path="/school-comparison-tool/results.page";
        } else if (COMPARE_SCHOOLS_RATINGS.equals(page)) {
            _path="/school-comparison-tool/ratings.page";
        } else if (COMPARE_SCHOOLS_TEST_SCORES.equals(page)) {
            _path="/school-comparison-tool/testScores.page";
        } else if (COMPARE_SCHOOLS_STUDENT_TEACHER.equals(page)) {
            _path="/school-comparison-tool/studentTeacher.page";
        } else if (COMPARE_SCHOOLS_PROGRAMS_EXTRACURRICULARS.equals(page)) {
            _path="/school-comparison-tool/programsExtracurriculars.page";
        } else if (COMPARE_SCHOOLS_MAP.equals(page)) {
            _path="/school-comparison-tool/map.page";
        } else if (CMS_WORKSHEET_GALLERY.equals(page)) {
            _path="/worksheets";
        } else if (HOME.equals(page)) {
            init(HOME, null, null);
        } else if (FIND_A_SCHOOL.equals(page)) {
            _path="/find-schools/";
        } else {
            throw new IllegalArgumentException("VPage unknown: " + page);
        }
    }

    public UrlBuilder(VPage page, String feedbackType, String cityName, Integer schoolId) {
        init(page);

        if (CONTACT_US.equals(page)) {
            if (StringUtils.isNotBlank(feedbackType)) {
                setParameter("feedbackType", feedbackType);
            }
            if (schoolId != null) {
                setParameter("schoolId", String.valueOf(schoolId));
            }
            if (StringUtils.isNotBlank(cityName)) {
                setParameter("city", String.valueOf(cityName));
            }
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, State state, String cityName, Set<SchoolType> schoolTypes, LevelCode levelCode) {
        _vPage = page;
        _perlPage = false;

        if (SCHOOLS_IN_CITY.equals(page)) {
            _path = DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, schoolTypes, levelCode);
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, State state, String param0) {
        init(page, state, param0);
    }

    public UrlBuilder(VPage page, State state, Integer schoolId, Integer agentId) {
        // GS-3044
        if (GET_BIREG.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/getBIReg/" + state.getAbbreviation() + "/" + schoolId + "/" + agentId;
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, State state, String email, String redirect) {
        if (REGISTRATION.equals(page)) {
            _perlPage = false;
            if (email != null) {
                setParameter("email", email);
            }
            if (redirect != null) {
                setParameter("redirect", redirect);
            }
            _path = "/community/registration.page";
        } else {
            throw new IllegalArgumentException("VPage not valid for this constructor: " + page);
        }
    }

    public UrlBuilder(VPage page, Integer id, State databaseState, String name, Address physicalAddress, LevelCode levelCode, boolean showConfirmation) {
        if (SCHOOL_PROFILE.equals(page)) {
            handleSchoolProfile(id, databaseState, name, physicalAddress, levelCode, showConfirmation);
        }
    }

    /**
     * Url for CMS-driven browse-content-by-category page
     * @param page Must be UrlBuilder.CMS_CATEGORY_BROWSE
     * @param topicIDs Comma-separated list of topic IDs
     * @param gradeIDs Comma-separated list of grade IDs
     * @param subjectIDs Comma-separated list of subject IDs
     * @param locationIDs Comma-separated list of location IDs
     * @param outcomeIDs Comma-separated list of outcome IDs
     * @param language
     */
    public UrlBuilder(VPage page, String topicIDs, String gradeIDs, String subjectIDs, String locationIDs, String outcomeIDs, String language) {
        if (CMS_CATEGORY_BROWSE.equals(page)) {
            StringBuilder s = new StringBuilder();
            if (StringUtils.isNotBlank(topicIDs)) {
                s.append("topics=").append(topicIDs);
            }
            if (StringUtils.isNotBlank(gradeIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("grades=").append(gradeIDs);
            }
            if (StringUtils.isNotBlank(subjectIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("subjects=").append(subjectIDs);
            }
            if (StringUtils.isNotBlank(locationIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("locations=").append(locationIDs);
            }
            if (StringUtils.isNotBlank(outcomeIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("outcomes=").append(outcomeIDs);
            }
            _perlPage = false;
            _path = "/articles/?" + s.toString() +
                        (StringUtils.isNotBlank(language) ? "&language=" + language : "");
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    /**
     * Url for CMS-driven browse-content-by-category page
     * @param page supports CMS_VIDEO_GALLERY and CMS_WORKSHEET_GALLERY
     * @param topicCenterId
     * @param topicCenterUrl
     * @param topicIDs Comma-separated list of topic IDs
     * @param gradeIDs Comma-separated list of grade IDs
     * @param subjectIDs Comma-separated list of subject IDs
     * @param language
     */
    public UrlBuilder(VPage page, Long topicCenterId, String topicCenterUrl, String topicIDs, String gradeIDs, String subjectIDs, String language) {
        if (CMS_VIDEO_GALLERY.equals(page) && StringUtils.isNotBlank(topicCenterUrl) && topicCenterId != null) {

            StringBuilder s = new StringBuilder();
            s.append("?content=").append(topicCenterId);

            if (StringUtils.isNotBlank(topicIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("topics=").append(topicIDs);
            }
            if (StringUtils.isNotBlank(gradeIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("grades=").append(gradeIDs);
            }
            if (StringUtils.isNotBlank(subjectIDs)) {
                if (s.length() > 0) {
                    s.append("&");
                }
                s.append("subjects=").append(subjectIDs);
            }
            _perlPage = false;
            _path = "/videos" + topicCenterUrl + "/" + s.toString() +
                    (StringUtils.isNotBlank(language) ? "&language=" + language : "");
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, String topic, String grade, String subject, String language) {
        if (CMS_WORKSHEET_GALLERY.equals(page)) {

            StringBuilder s = new StringBuilder("/worksheets");

            if (StringUtils.isNotBlank(grade)) {
                s.append("/").append(grade);
            }

            if (StringUtils.isNotBlank(subject)) {
                s.append("/").append(subject);
            }
            _perlPage = false;
            _path = s.toString() + (StringUtils.isNotBlank(language) ? "?language=" + language : "");
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, boolean showConfirmation) {
        // GS-7917
        if (SCHOOL_CHOICE_CENTER.equals(page)) {
            _perlPage = false;
            _path = "/school-choice/?confirm=" + showConfirmation;
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    // WARNING: if this changes, GSFeed's SiteMapFeedGenerator needs to be changed too!!!
    public void handleSchoolProfile(School school, boolean showConfirmation) {
        handleSchoolProfile(school.getId(), school.getDatabaseState(), school.getName(), school.getPhysicalAddress(), school.getLevelCode(), showConfirmation);
    }

    public void handleSchoolProfile(Integer id, State databaseState, String name, Address physicalAddress, LevelCode levelCode, boolean showConfirmation) {
        _perlPage = true;

        if (LevelCode.PRESCHOOL.equals(levelCode)) {
            // turn spaces and / into hyphens for readable and remove #
            // yes, this does mean there is no way to deterministically get the school name back
            _path = DirectoryStructureUrlFactory.createNewCityBrowseURI(databaseState,
                    physicalAddress.getCity(), new HashSet<SchoolType>(), LevelCode.PRESCHOOL) +
                    WordUtils.capitalize(name.replaceAll(" ", "-").replaceAll("/", "-").replaceAll("#", "").replaceAll("`", ""), new char[]{'-'}) +
                    "/" + id + "/" +
                    (showConfirmation ? "?confirm=true" : "");
        } else {
            StringBuffer path = new StringBuffer(
                    DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(
                        databaseState,
                        physicalAddress.getCity()
                    )
            );

            path.append(id).append("-");
            path.append(WordUtils.capitalize(
                    name.replaceAll(" ", "-")
                        .replaceAll("/", "-")
                        .replaceAll("`", "")
                        .replaceAll("#", ""),
                    new char[]{'-'}
            ));
            path.append("/");
            if (showConfirmation) {
                path.append("?confirm=true");
            }
            // GS-10949 WARNING: this requires ISOLatin1AccentFilter from Lucene, so be careful to either port this
            // when switching from Lucene to Solr, or don't remove the Lucene dependency till that's done
            _path = ISOLatin1AccentFilter.removeAccents(path.toString());
        }
    }

    /**
     * URL creation logic took from gs.api.LinkApiService / UrlBuilder.handleSchoolProfile
     * @param city
     * @param databaseState
     * @param schoolName
     * @param levelCode
     * @param schoolId
     * @return
     */
    public static String getOverviewPath(Integer schoolId, String schoolName, String city, State databaseState, LevelCode levelCode) {
        String path;
        // TODO: Copied from gs.web.util.UrlBuilder and gs.api.LinkApiService
        if (LevelCode.PRESCHOOL.equals(levelCode)) {
            path = DirectoryStructureUrlFactory.createNewCityBrowseURI(databaseState, city, new HashSet<SchoolType>(), LevelCode.PRESCHOOL) +
                WordUtils.capitalize(schoolName.replaceAll(" ", "-").replaceAll("/", "-").replaceAll("#", ""), new char[]{'-'}) +
                "/" + schoolId + "/";
        } else {
            StringBuffer path2 = new StringBuffer
                    (DirectoryStructureUrlFactory.createNewCityBrowseURIRoot
                            (databaseState, city));
            path2.append(schoolId).append("-");
            path2.append(WordUtils.capitalize(schoolName.replaceAll(" ", "-").replaceAll("/", "-").replaceAll("`", "").replaceAll("#", ""), new char[]{'-'}));
            path2.append("/");
            path = path2.toString();

        }
        return path;
    }

    /**
     * URL creation logic took from gs.api.LinkApiService
     * @param schoolId
     * @param databaseState
     * @return
     */
    public static String getParentReviewsPath(Integer schoolId, State databaseState) {
        String parentReviewsPath = "/school/parentReviews.page?state=" + databaseState.getAbbreviation() + "&id=" + schoolId;
        return parentReviewsPath;
    }

    /**
     * URL creation logic took from gs.api.LinkApiService
     * @param schoolId
     * @param databaseState
     * @return
     */
    public static String getRatingsPath(Integer schoolId, State databaseState) {
        String ratings = "/school/rating.page?state=" + databaseState.getAbbreviation() + "&id=" + schoolId;
        return ratings;
    }

    public UrlBuilder(VPage page, boolean showConfirmation, School school) {
        // GS-7917
        if (SCHOOL_PROFILE.equals(page)) {
            handleSchoolProfile(school, showConfirmation);
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, String searchQuery, Integer pageNum, String type, String sample) {
        // GS-8876
        if (CONTENT_SEARCH.equals(page)) {
            _perlPage = false;
            _path = "/search/contentSearch.page";

            if (searchQuery != null) {
                this.setParameter("q", searchQuery);
            }

            if (pageNum != null && pageNum > 1) {
                this.setParameter("page", String.valueOf(pageNum));
            }
            if (type != null) {
                this.setParameter("type", type);
            }
            if (StringUtils.isNotBlank(sample)) {
                this.setParameter("sample", sample);
            }
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    private void init(VPage page, State state, String param0) {
        _vPage = page;

        if (CITY_PAGE.equals(page)) {
            _perlPage = false;
            // WARNING: if this changes, GSFeed's SiteMapFeedGenerator needs to be changed too!!!
            _path = DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(state, param0);
        } else if (WEBBY_AWARD_THANKS.equals(page)) {
            _perlPage = false;
            _path = "/promo/webbyAwardWinner.page";
        } else if (CITIES.equals(page)) {
            _perlPage = false;
            _path = "/schools/cities/" + state.getLongName().replace(" ", "_") + "/" + state.getAbbreviation();
            if (StringUtils.isNotBlank(param0)) {
                _path += "/" + param0;
            }
        } else if (MY_SCHOOL_LIST.equals(page)) {
            _perlPage = false;
            _path = "/mySchoolList.page";
            if (state != null) {
                setParameter("command", "add");
                setParameter("state", state.getAbbreviation());
                if (null != param0) {
                    setParameter("ids", param0);
                }
            }
        } else if (ARTICLE_LIBRARY.equals(page)) {
            _perlPage = false;
            _path = "/education-topics/";
        } else if (DISTRICT_PROFILE.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/" +
                    state.getAbbreviationLowerCase() +
                    "/district-profile/" +
                    param0;
        } else if (RESEARCH.equals(page)) {
            if (state == null) {
                _perlPage = false;
                _path = "/school/research.page";
            } else {
                _perlPage = false;
                // WARNING: if this changes, GSFeed's SiteMapFeedGenerator needs to be changed too!!!
                _path = DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state);
            }
        } else if (HOME.equals(page)) {
            _perlPage = false;
            _path = "/";
        } else if (PRIVACY_POLICY.equals(page)) {
            _perlPage = false;
            _path = "/privacy/";
            setParameter("state", state.getAbbreviation());
        } else if (PRESS_ROOM.equals(page)) {
            _perlPage = false;
            // WARNING: if this changes, GSFeed's SiteMapFeedGenerator needs to be changed too!!!
            _path = "/about/pressRoom.page";
        } else if (PRESS_RELEASES.equals(page)) {
            _perlPage = false;
            _path = "/about/pressRoom/pressReleases.page";
        } else if (PRESS_CLIPPINGS.equals(page)) {
            _perlPage = false;
            _path = "/about/pressRoom/pressClippings.page";
        } else if (SIGN_IN.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/msl_login/" + state.getAbbreviationLowerCase();
        } else if (SIGN_OUT.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/logout/" + state.getAbbreviation();
            if (param0 != null) {
                if (StringUtils.isNumeric(param0)) {
                    setParameter("mid", param0);
                } else {
                    setParameter("email", param0);
                }
            }
        } else if (NEWSLETTER_MANAGEMENT.equals(page)) {
            _perlPage = false;
            _path = "/email/management.page";
            if (StringUtils.isNotBlank(param0)) {
                setParameter("email", param0);
            }
        } else if (NEWSLETTER_UNSUBSCRIBE.equals(page)) {
            _perlPage = false;
            _path = "/email/unsubscribe.page";
            if (StringUtils.isNotBlank(param0)) {
                setParameter("email", param0);
            }
        } else if (ADMIN_NEWS_ITEMS.equals(page)) {
            _perlPage = false;
            _path = "/admin/news/list.page";
        } else if (ADMIN_NEWS_ITEMS_CREATE.equals(page)) {
            _perlPage = false;
            _path = "/admin/news/create.page";
        } else if (ADMIN_NEWS_ITEMS_DELETE.equals(page)) {
            _perlPage = false;
            _path = "/admin/news/delete.page";
            setParameter("id", param0);
        } else if (COMMUNITY.equals(page)) {
            _perlPage = false;
            _path = "community";
        } else if (BETA_SIGNUP.equals(page)) {
            _perlPage = false;
            _path = "/community/beta.page";
            if (param0 != null) {
                setParameter("email", param0);
            }
            setParameter("state", state.getAbbreviation());
        } else if (BETA_UNSUBSCRIBE.equals(page)) {
            _perlPage = false;
            _path = "/community/betaUnsubscribe.page";
            if (param0 != null) {
                setParameter("email", param0);
            }
            setParameter("state", state.getAbbreviation());
        } else if (TERMS_OF_USE.equals(page)) {
            _perlPage = false;
            _path = "/terms/";
            setParameter("state", state.getAbbreviation());
        } else if (ADD_PARENT_REVIEW_SEARCH.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/regSearch/" + state.getAbbreviation();
        } else if (REGISTRATION.equals(page)) {
            _perlPage = false;
            if (param0 != null) {
                setParameter("email", param0);
            }
            _path = "/community/registration.page";
        } else if (REGISTRATION_REMOVE.equals(page)) {
            _perlPage = false;
            _path = "/community/registrationRemove.page";
            if (param0 != null) {
                setParameter("id", param0);
            }
        } else if (REGISTRATION_VALIDATION.equals(page)) {
            _perlPage = false;
            _path = "/community/registrationConfirm.page";
            setParameter("id", param0);
        } else if (FORGOT_PASSWORD.equals(page)) {
            _perlPage = false;
            _path = "/community/forgotPassword.page";
            if (param0 != null) {
                setParameter("email", param0);
            }
        } else if (RESET_PASSWORD.equals(page)) {
            _perlPage = false;
            _path = "/community/resetPassword.page";
            if (param0 != null) {
                setParameter("id", param0);
            }
        } else if (CHANGE_EMAIL.equals(page)) {
            _perlPage = false;
            _path = "/community/changeEmail.page";
        } else if (REQUEST_EMAIL_VALIDATION.equals(page)) {
            _perlPage = false;
            _path = "/community/requestEmailValidation.page";
            setParameter("email", param0);
        } else if (LOGIN_OR_REGISTER.equals(page)) {
            _perlPage = false;
            _path = "/community/loginOrRegister.page";
            if (param0 != null) {
                setParameter("redirect", param0);
            }
        } else if (COMMUNITY_LANDING.equals(page)) {
            _perlPage = false;
            _path = "/community/";
        } else if (ACCOUNT_INFO.equals(page)) {
            _perlPage = false;
            _path = "/account/";
        } else if (DISTRICTS_PAGE.equals(page)) {
            _perlPage = false;
            _path = "/schools/districts/" + state.getLongName().replace(" ", "_") + "/" + state.getAbbreviation();
        } else if (CITIES_MORE_NEARBY.equals(page)) {
            _perlPage = false;
            _path = "/cities.page";
            this.setParameter("city", param0);
            this.setParameter("state", state.getAbbreviation());
        } else if (PARENT_REVIEW_LEARN_MORE.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/static/parentcomments.html/" + state.getAbbreviationLowerCase() + "/";
        } else if (PARENT_RATING_EXPLAINED.equals(page)) {
            _perlPage = true;
            _path = "/definitions/parent_rating_categories.html";
        } else if (PARENT_RATING_PRESCHOOL_EXPLAINED.equals(page)) {
            _perlPage = true;
            _path = "/definitions/preschool_rating_categories.html";
        } else if (PARENT_REVIEW_GUIDELINES.equals(page)) {
            _perlPage = false;
            _path = "/about/guidelines.page";
        } else if (GLOSSARY_TERM.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/glossary_single/" + state.getAbbreviation() + "/";
            setParameter("id", param0);
        } else if (SCHOOLS_IN_STATE.equals(page)) {
            _perlPage = false;
            _path = "/schools/" + state.getLongName().replace(" ", "_") + "/" + state.getAbbreviation();
        } else if (SCHOOL_SEARCH.equals(page)) {
            _perlPage = false;
            _path = "/search/search.page";
            this.setParameter("c", "school");
            this.setParameter("search_type", "0");
            this.setParameter("state", state.getAbbreviation());
            this.setParameter("q", param0);
        } else if (ARTICLE_SEARCH.equals(page)) {
            _perlPage = false;
            _path = "/search/search.page";
            this.setParameter("c", "topic");
            this.setParameter("search_type", "0");
            this.setParameter("q", param0);
        } else if (TEST_SCORE_LANDING.equals(page)) {
            _perlPage = false;
            _path = "/test/landing.page";
            setParameter("state", state.getAbbreviation());
            setParameter("tid", param0);
        } else if (SUBMIT_PRESCHOOL.equals(page)) {
            _perlPage = false;
            _path = "/about/feedback/submitPreschool.page";
            setParameter("state", state.getAbbreviation());
        } else if (SUBMIT_PRIVATE_SCHOOL.equals(page)) {
            _perlPage = false;
            _path = "/about/feedback/submitPrivateSchool.page";
            setParameter("state", state.getAbbreviation());
        } else if (BROWSE_PRESCHOOLS.equals(page)) {
            _perlPage = false;
            StringBuilder sb = new StringBuilder();
            sb.append("/");
            String urlState = state.getLongName().toLowerCase();
            urlState = urlState.replace(" ", "-");
            sb.append(urlState);
            sb.append("/");
            if (param0 != null) {
                String city = param0.toLowerCase();
                city = city.replace("-", "_");
                city = city.replace(" ", "-");
                sb.append(city);
            }
            sb.append("/preschools/");
            _path = sb.toString();
        } else if (API_ADMIN_LOGIN.equals(page)) {
            StringBuilder sb = new StringBuilder();
            _perlPage = false;
            sb.append("/api/admin_login.page");
            if (StringUtils.isNotBlank(param0)) {
                sb.append("?redirect=");
                sb.append(param0);
            }
            _path = sb.toString();
        } else if (API_DOCS_MAIN.equals(page)) {
            _perlPage = false;
            _path = "/api/docs/main.page";
        } else if(USER_ACCOUNT.equals(page)){
            _perlPage = false;
            _path = "/account/";
        } else if(RECENT_ACTIVITY.equals(page)){
            _perlPage = false;
            _path = "/account/";
            setParameter("viewAllActivity","true");
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    /**
     * Set the path to the page.
     *
     * @param path context-relative path
     */
    public void setPath(String path) {
        _path = path;
        _perlPage = _urlUtil.smellsLikePerl(path);
    }

    /**
     * Takes all the parameters in the given requests and adds them to the URL.
     * If some parameters already exist, they will be appended to.
     */
    public void addParametersFromRequest(HttpServletRequest request) {
        Map parameterMap = request.getParameterMap();
        for (Iterator i = parameterMap.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            String[] value = request.getParameterValues(key);
            for (int j = 0; j < value.length; j++) {
                this.addParameter(key, value[j]);
            }
        }
    }

    /**
     * Replaces the given parameter.
     *
     * @param value previously encoded value. Spaces should be represented by "+" signs,
     *              and "=" and "&" should be encoded, along with other extended characters.
     */
    public void setParameterNoEncoding(String key, String value) {
        if (_parameters == null) {
            _parameters = new HashMap();
        }
        _parameters.put(key, new String[]{value});
    }

    /**
     * Adds the given parameter. If one exists already, this one is added as well.
     *
     * @param value previously encoded value. Spaces should be represented by "+" signs,
     *              and "=" and "&" should be encoded, along with other extended characters.
     * @see #setParameterNoEncoding(String, String)
     * @see #addParameter(String, String)
     */
    public void addParameterNoEncoding(String key, String value) {
        if (_parameters == null) {
            _parameters = new HashMap();
            _parameters.put(key, new String[]{value});
        } else {
            String[] existingValues = (String[]) _parameters.get(key);
            if (existingValues == null) {
                _parameters.put(key, new String[]{value});
            } else {
                String[] newValues = org.springframework.util.StringUtils.addStringToArray(existingValues, value);
                _parameters.put(key, newValues);
            }

        }
    }

    /**
     * Replaces the given parameter.
     *
     * @param value unencoded values. Spaces, ampersands, equal signs, etc. will be replaced.
     */
    public void setParameter(String key, String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.warn("Unable to encode parameter");
        }
        setParameterNoEncoding(key, value);
    }

    /**
     * Adds the given parameter. If it exists already, then this one is also added.
     *
     * @param value unencoded values. Spaces, ampersands, equal signs, etc. will be replaced.
     * @see #addParameterNoEncoding
     * @see #setParameter(String, String)
     */
    public void addParameter(String key, String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.warn("Unable to encode parameter");
        }
        addParameterNoEncoding(key, value);
    }

    /**
     * Take away the parameter.
     */
    public void removeParameter(String key) {
        if (_parameters != null) {
            _parameters.remove(key);
        }
    }

    public String toString() {
        return asSiteRelativeXml(null);
    }

    /**
     * Provides a site-relative path to the page, including the context path if needed.
     * Encoded correctly to dump directly to XHTML.
     *
     * @param request option request object.
     */
    public String asSiteRelativeXml(HttpServletRequest request) {
        String s = asSiteRelative(request);
        s = encodeForXml(s);
        return s;
    }

    /**
     * Provides a site-relative path to the page, including the context path if needed.
     * Not encoded for XHTML.
     *
     * @param request option request object.
     * @return A site-relative url as a String
     */
    public String asSiteRelative(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        String contextPath = request != null ? request.getContextPath() : "";
        if (!_perlPage) {
            sb.append(contextPath);
        }
        sb.append(_path);
        // Fix by Anthony for case where context path is "/" to prevent the resulting URL
        // from beginning with "//".
        if (sb.length() > 2 && sb.substring(0, 2).equals("//")) {
            sb = sb.deleteCharAt(0);
        }

        if (_parameters != null && _parameters.size() > 0) {
            sb.append("?");
            List keys = new ArrayList(_parameters.keySet());
            Collections.sort(keys);
            for (Iterator iter = keys.iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String[] values = (String[]) _parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    sb.append(key);
                    sb.append("=" + values[i]);
                    if (i < (values.length - 1) || iter.hasNext()) {
                        sb.append("&");
                    }
                }
            }

        }

        return sb.toString();
    }

    /**
     * Simple encoding of a string to put into an Xml document. Note that it doesn't deal
     * with real encoding-- only the specific XML characters, & < and >.
     */
    public static String encodeForXml(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    public Anchor asAnchor(HttpServletRequest request, String label) {
        return new Anchor(asSiteRelative(request), label);
    }

    public Anchor asAnchor(HttpServletRequest request, String label, String styleClass) {
        return new Anchor(asSiteRelative(request), label, styleClass);
    }

    public Anchor asAnchor(HttpServletRequest request, String label, String styleClass, String image) {
        return new Anchor(asSiteRelative(request), label, styleClass, image);
    }

    public Anchor asAbsoluteAnchor(HttpServletRequest request, String label) {
        return new Anchor(asFullUrl(request), label);
    }

    public String asFullUrl(String serverName, int serverPort) {
        String url = "http://" +
                serverName +
                ((serverPort != 80) ? ":" + serverPort : "") +
                asSiteRelative(null);
        return url;
    }

    /**
     * Provides a full URL to the page. This is the raw URL, not encoded correctly
     * for XHTML. This is generally not needed, but is needed for redirect usage.
     *
     * @see #asSiteRelativeXml(javax.servlet.http.HttpServletRequest)
     */
    public String asFullUrl(HttpServletRequest request) {

        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String url = "http://" +
                serverName +
                ((serverPort != 80) ? ":" + serverPort : "") +
                asSiteRelative(request);
        return url;
    }

    public static String getCommunitySiteBaseUrl(HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        return "http://" + sessionContext.getSessionContextUtil().getCommunityHost(request);
    }

    /**
     * Provides a full URL to the page. This is the raw URL, not encoded correctly
     * for XHTML. This is generally not needed, but is needed for redirect usage.
     *
     * @see #asSiteRelativeXml(javax.servlet.http.HttpServletRequest)
     */
    public String asFullUrlXml(HttpServletRequest request) {

        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String url = "http://" +
                serverName +
                ((serverPort != 80) ? ":" + serverPort : "") +
                asSiteRelative(request);
        url = encodeForXml(url);
        return url;
    }

    /**
     * Provides a site-relative link wrapped in an a tag.
     * Encoded correctly to dump directly to XHTML.
     */
    public String asAHref(HttpServletRequest request, String label) {
        return "<a href=\"" + asSiteRelativeXml(request) + "\">" + label + "</a>";
    }

    /**
     * Provides a site-relative link wrapped in an a tag.
     * Encoded correctly to dump directly to XHTML.
     *
     * @param label      the contents of the a tag
     * @param styleClass the css class attribute
     */
    public String asAHref(HttpServletRequest request, String label, String styleClass) {
        return "<a href=\"" + asSiteRelativeXml(request) + "\" class=\"" + styleClass + "\">" + label + "</a>";
    }

}
