package gs.web.content.cms;

import gs.data.community.IRaiseYourHandDao;
import gs.data.community.RaiseYourHandFeature;
import gs.data.community.User;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolWithRatings;
import gs.data.school.review.IReviewDao;
import gs.data.security.Permission;
import gs.data.state.State;
import gs.web.school.SchoolOverviewController;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import gs.data.content.cms.*;
import gs.data.cms.IPublicationDao;
import gs.data.util.CmsUtil;
import gs.web.util.PageHelper;

public class CmsTopicCenterController2010 extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsTopicCenterController2010.class);

    /**
     * Spring Bean ID
     */
    public static final String BEAN_ID = "/content/cms/topicCenter.page";

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";

    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;
    private IPublicationDao _publicationDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IRaiseYourHandDao _raiseYourHandDao;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private IGeoDao _geoDao;
    private ILocalBoardDao _localBoardDao;
    private Boolean _useAdKeywords = true;
    private Long _topicCenterContentID;

    public static final int MIN_CAROUSEL_ITEMS = 3;
    public static final int MAX_TOP_SCHOOLS = 3;
    public static final int MAX_RAISE_YOUR_HAND_DISCUSSIONS_FOR_CMSADMIN = 1000;

    public static final String MODEL_TOPIC_CENTER = "topicCenter";
    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_OMNITURE_TOPIC_CENTER_NAME = "omnitureTopicCenterName";
    public static final String MODEL_CAROUSEL_ITEMS = "carouselItems";
    public static final String MODEL_BROWSE_BY_GRADE_SUBTOPICS = "browseByGradeSubtopics";
    public static final String MODEL_ALL_RAISE_YOUR_HAND_FOR_TOPIC = "allRaiseYourHandDiscussions";
    public static final String LOCAL_DISCUSSION_BOARD_ID = "localDiscussionBoardId";
    public static final String LOCAL_DISCUSSION_TOPIC = "localDiscussionTopic";
    public static final String LOCAL_DISCUSSION_TOPIC_FULL = "localDiscussionTopicFull";

    //=========================================================================
    // spring mvc methods
    //=========================================================================

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        if (CmsUtil.isCmsEnabled()) {
            String uri = request.getRequestURI();
            boolean showSample = (BEAN_ID.equals(uri));
            CmsTopicCenter topicCenter;

            Long contentId = null;
            if (showSample) {
                topicCenter = getSampleTopicCenter();
            } else {
                if (uri.startsWith("/preschool/")) {
                    contentId = CmsConstants.PRESCHOOL_TOPIC_CENTER_ID;
                } else if (uri.startsWith("/elementary-school/")) {
                    contentId = CmsConstants.ELEMENTARY_SCHOOL_TOPIC_CENTER_ID;
                } else if (uri.startsWith("/middle-school/")) {
                    contentId = CmsConstants.MIDDLE_SCHOOL_TOPIC_CENTER_ID;
                } else if (uri.startsWith("/high-school/")) {
                    contentId = CmsConstants.HIGH_SCHOOL_TOPIC_CENTER_ID;
                } else if (getTopicCenterContentID() == null) {
                    try {
                        contentId = new Long(request.getParameter("content"));

                        if (contentId == CmsConstants.SPECIAL_EDUCATION_TOPIC_CENTER_ID && uri.equals("/LD.topic")) {
                            UrlBuilder builder = new UrlBuilder(new ContentKey("TopicCenter", CmsConstants.SPECIAL_EDUCATION_TOPIC_CENTER_ID));
                            if (!builder.asSiteRelative(request).startsWith("/LD.topic")) {
                                return new ModelAndView(new RedirectView301(builder.asSiteRelative(request)));
                            }
                        }
                    } catch (Exception e) {
                        _log.warn("contentId \"" + request.getParameter("content") + "\" is not a Long");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        return new ModelAndView("/status/error404.page");
                    }
                } else {
                    contentId = getTopicCenterContentID();
                }

                topicCenter = _publicationDao.populateByContentId(contentId, new CmsTopicCenter());
            }

            if (topicCenter == null) {
                _log.warn("Error locating topic center with contentId=" + contentId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView("/status/error404.page");
            }

            if (topicCenter.getDiscussionBoardId() != null) {
                model.put(MODEL_DISCUSSION_BOARD, _cmsDiscussionBoardDao.get(topicCenter.getDiscussionBoardId()));
            }

            try {
                _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(topicCenter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (isUseAdKeywords()) {
                // Google Ad Manager ad keywords
                PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
                for (CmsCategory category : topicCenter.getUniqueKategoryBreadcrumbs()) {
                    pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
                }
                pageHelper.addAdKeyword("topic_center_id", String.valueOf(topicCenter.getContentKey().getIdentifier()));
            }

            model.put(MODEL_OMNITURE_TOPIC_CENTER_NAME, topicCenter.getTitle().replaceAll(",", "").replaceAll("\"", ""));

            model.put(MODEL_BROWSE_BY_GRADE_SUBTOPICS, getBrowseByGradeForTopicCenter(topicCenter.getContentKey().getIdentifier()));
            populateCarouselModel(topicCenter, model);

            model.put(MODEL_TOPIC_CENTER, topicCenter);


            // GS-10275
            // Show the local community module we've built on the school overview page in place of the map IF a user is
            // cookied to one of the 73 local cities. If the user isn't cookied to one of the cities, show the Map module
            // (the Local Schools module)
            boolean hasLocalCommunity = false;
            if (!topicCenter.isPreschoolTopicCenter()) {
                hasLocalCommunity = loadLocalCommunity(model,request);
            }
            if (topicCenter.isPreschoolTopicCenter() || !hasLocalCommunity) {
                // local schools module
                // check for a change of city
                SessionContext context = SessionContextUtil.getSessionContext(request);
                String cityName = request.getParameter("city");
                if (cityName != null) {
                    // if so update the user's cookie
                    String stateAbbr = request.getParameter("state");
                    City city = getGeoDao().findCity(State.fromString(stateAbbr), cityName);
                    if (city != null) {
                        context.getSessionContextUtil().changeCity(context, request, response, city);
                        context.setCity(city); // saves a DB query later
                    }
                }

                LevelCode levelCode = null;
                if (topicCenter.isPreschoolTopicCenter()) {
                    levelCode = LevelCode.PRESCHOOL;
                } else if (topicCenter.isElementarySchoolTopicCenter() || topicCenter.isElementaryGradeTopicCenter()) {
                    levelCode = LevelCode.ELEMENTARY;
                } else if (topicCenter.isMiddleSchoolTopicCenter()) {
                    levelCode = LevelCode.MIDDLE;
                } else if (topicCenter.isHighSchoolTopicCenter()) {
                    levelCode = LevelCode.HIGH;
                } else {
                    // default to elementary for non-grade-level topic centers
                    levelCode = LevelCode.ELEMENTARY;
                }
                loadTopRatedSchools(model, context, levelCode);
            }
            // GS-9770 if user is authorized and is a cms admin, add raise your hand discussions to model
            if (PageHelper.isMemberAuthorized(request)) {
                insertRaiseYourHandDiscussionsIntoModel(request, model, topicCenter);
            }

            if (topicCenter.isGradeLevelTopicCenter()) {
                model.put("showSchoolChooserPackPromo", SchoolOverviewController.showSchoolChooserPackPromo(request, response));
            }

        }



        return new ModelAndView(_viewName, model);
    }

    private void insertRaiseYourHandDiscussionsIntoModel(HttpServletRequest request, Map<String, Object> model, CmsTopicCenter topicCenter) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        user = sessionContext.getUser();
        if (user != null) {
            if (user.hasPermission(Permission.COMMUNITY_MANAGE_RAISE_YOUR_HAND)) {
                List<RaiseYourHandFeature> featureList = getRaiseYourHandDao().getFeatures(topicCenter.getContentKey(), MAX_RAISE_YOUR_HAND_DISCUSSIONS_FOR_CMSADMIN);
                model.put(MODEL_ALL_RAISE_YOUR_HAND_FOR_TOPIC, featureList);
            }
        }
    }

    //=========================================================================
    // local community
    //=========================================================================

    /**
     * Put in the model the local discussion board and discussion topic for the cookied city, if the cookied city
     * has a discussion board
     * @param model
     * @param request
     * @return true if the cookied city has a local discussion board; false otherwise (or no cookied city)
     */
    protected boolean loadLocalCommunity(Map<String, Object> model, HttpServletRequest request) {
        // determine if this is a city with a local discussion board
        City city = SessionContextUtil.getSessionContext(request).getCity();
        if (city != null) {
            LocalBoard localBoard = _localBoardDao.findByCityId(city.getId());
            if (localBoard != null) {
                model.put(LOCAL_DISCUSSION_BOARD_ID, localBoard.getBoardId());
                model.put(LOCAL_DISCUSSION_TOPIC, city.getDisplayName());
                model.put(LOCAL_DISCUSSION_TOPIC_FULL, city.getDisplayName());
                return true;
            }
        }

        return false;
    }

    //=========================================================================
    // find a school
    //=========================================================================

    protected void loadTopRatedSchools(Map<String, Object> model, SessionContext context, LevelCode levelCode) {
        City userCity;
        if (context.getCity() != null) {
            userCity = context.getCity();
        } else {
            userCity = getGeoDao().findCity(State.CA, "Los Angeles");
        }
        model.put("cityObject", userCity);
        loadCityDropdown(model, userCity.getState());

        if ("Washington".equalsIgnoreCase(userCity.getName()) && "DC".equalsIgnoreCase(userCity.getState().getAbbreviation())) {
            model.put("specialCity", "Washington, D.C.");
        }

        boolean showingTopRatedSchools = false;
        List<SchoolWithRatings> schools;
        if (levelCode.equals(LevelCode.PRESCHOOL)) {
            schools = getRandomSchoolsInCity(userCity, levelCode);
        } else {
            schools =
                    getSchoolDao().findTopRatedSchoolsWithRatingsInCity(userCity, 1, levelCode.getLowestLevel(), MAX_TOP_SCHOOLS, false);
            if (schools.size() == 0) {
                schools = getRandomSchoolsInCity(userCity, levelCode);
            } else {
                showingTopRatedSchools = true;
            }
        }
        _reviewDao.loadRatingsIntoSchoolList(schools, userCity.getState());

        model.put("schools", schools);
        model.put("showingTopRatedSchools", showingTopRatedSchools);
    }

    protected List<SchoolWithRatings> getRandomSchoolsInCity(ICity city, LevelCode levelCode) {
        List<SchoolWithRatings> schools;
        List<School> randomSchools = getSchoolDao().findRandomSchoolsInCity(city, levelCode.getLowestLevel(), MAX_TOP_SCHOOLS, false);
        schools = new ArrayList<SchoolWithRatings>(randomSchools.size());
        for (School school : randomSchools) {
            SchoolWithRatings schoolWithRatings = new SchoolWithRatings();
            schoolWithRatings.setSchool(school);
            schools.add(schoolWithRatings);
        }
        return schools;
    }

    protected void loadCityDropdown(Map<String, Object> model, State state) {
        List<City> cities = getGeoDao().findAllCitiesByState(state);
        model.put("cityList", cities);
    }

    //=========================================================================
    // carousel
    //=========================================================================

    private void populateCarouselModel(CmsTopicCenter topicCenter, Map<String, Object> model) {
        if (topicCenter.getCarouselLinks() != null && topicCenter.getCarouselLinks().size() >= MIN_CAROUSEL_ITEMS) {
            List<CmsLink> carouselItems = new ArrayList<CmsLink>(topicCenter.getCarouselLinks());
            Collections.shuffle(carouselItems);
            model.put(MODEL_CAROUSEL_ITEMS, carouselItems);
        }
    }

    //=========================================================================
    // browse by grade sidebar
    //=========================================================================

    private static Map<Long, List<CmsSubtopic>> TOPIC_CENTER_BROWSE_BY_GRADE_SUBTOPICS_MAP = new HashMap<Long, List<CmsSubtopic>>();

    private static List<CmsSubtopic> getBrowseByGradeForTopicCenter(long topicCenterID) {
        List<CmsSubtopic> subtopics;

        // check cache
        if (TOPIC_CENTER_BROWSE_BY_GRADE_SUBTOPICS_MAP.containsKey(topicCenterID)) {
            return TOPIC_CENTER_BROWSE_BY_GRADE_SUBTOPICS_MAP.get(topicCenterID);
        }

        CmsCategory cat = new CmsCategory();
        cat.setType(CmsCategory.TYPE_TOPIC);
        if (topicCenterID == CmsConstants.ELEMENTARY_SCHOOL_TOPIC_CENTER_ID) {
            subtopics = getBrowseByGradeForElementary();
        } else if (topicCenterID == CmsConstants.ACADEMICS_AND_ACTIVITIES_TOPIC_CENTER_ID) {
            cat.setId(CmsConstants.ACADEMICS_AND_ACTIVITIES_CATEGORY_ID);
            subtopics = getBrowseByGradeHelper(cat);
        } else if (topicCenterID == CmsConstants.HEALTH_AND_DEVELOPMENT_TOPIC_CENTER_ID) {
            cat.setId(CmsConstants.HEALTH_AND_DEVELOPMENT_CATEGORY_ID);
            subtopics = getBrowseByGradeHelper(cat);
        } else if (topicCenterID == CmsConstants.SPECIAL_EDUCATION_TOPIC_CENTER_ID) {
            cat.setId(CmsConstants.SPECIAL_EDUCATION_CATEGORY_ID);
            subtopics = getBrowseByGradeHelper(cat);
        } else {
            subtopics = null;
        }

        // put in cache
        TOPIC_CENTER_BROWSE_BY_GRADE_SUBTOPICS_MAP.put(topicCenterID, subtopics);

        return subtopics;
    }

    private static List<CmsSubtopic> getBrowseByGradeForElementary() {
        List<CmsSubtopic> subtopics = new ArrayList<CmsSubtopic>();
        for (CmsSubtopic subtopic : BROWSE_BY_GRADE_ELEMENTARY_SUBTOPICS_TEMPLATE) {
            CmsSubtopic copy = CmsSubtopic.deepCopy(subtopic);

            List<CmsSubSubtopic> subSubtopics = new ArrayList<CmsSubSubtopic>();
            for (CmsSubSubtopic subSubTopic : BROWSE_BY_GRADE_ELEMENTARY_SUBSUBTOPICS_TEMPLATE) {
                CmsSubSubtopic subCopy = CmsSubSubtopic.deepCopy(subSubTopic);

                List<CmsCategory> cats = subCopy.getKategories();
                cats.addAll(copy.getKategories());

                subSubtopics.add(subCopy);
            }

            copy.setSubSubtopics(subSubtopics);

            subtopics.add(copy);
        }
        return subtopics;
    }

    private static void populateBrowseByGradeElementarySubtopicsTemplate(String title, String path) {
        CmsSubtopic subtopic = new CmsSubtopic();
        subtopic.setTitle(title);
        CmsLink link = new CmsLink();
        link.setUrl(path);
        subtopic.setMoreLink(link);
        BROWSE_BY_GRADE_ELEMENTARY_SUBTOPICS_TEMPLATE.add(subtopic);
    }

    private static void populateBrowseByGradeSubtopicsTemplate(String title, List<CmsCategory> categories) {
        CmsSubtopic subtopic = new CmsSubtopic();
        subtopic.setTitle(title);
        subtopic.setKategories(categories);
        BROWSE_BY_GRADE_SUBTOPICS_TEMPLATE.add(subtopic);
    }

    private static List<CmsSubtopic> BROWSE_BY_GRADE_SUBTOPICS_TEMPLATE = new ArrayList<CmsSubtopic>();
    private static List<CmsSubSubtopic> BROWSE_BY_GRADE_ELEMENTARY_SUBSUBTOPICS_TEMPLATE = new ArrayList<CmsSubSubtopic>();
    private static List<CmsSubtopic> BROWSE_BY_GRADE_ELEMENTARY_SUBTOPICS_TEMPLATE = new ArrayList<CmsSubtopic>();

    static {
        List<CmsCategory> pCats = getCategoryList(CmsConstants.PRESCHOOL_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> kCats = getCategoryList(CmsConstants.KINDERGARTEN_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> firstCats = getCategoryList(CmsConstants.FIRST_GRADE_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> secondCats = getCategoryList(CmsConstants.SECOND_GRADE_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> thirdCats = getCategoryList(CmsConstants.THIRD_GRADE_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> fourthCats = getCategoryList(CmsConstants.FOURTH_GRADE_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> fifthCats = getCategoryList(CmsConstants.FIFTH_GRADE_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> middleCats = getCategoryList(CmsConstants.MIDDLE_SCHOOL_CATEGORY_ID, CmsCategory.TYPE_GRADE);
        List<CmsCategory> highCats = getCategoryList(CmsConstants.HIGH_SCHOOL_CATEGORY_ID, CmsCategory.TYPE_GRADE);

        // elementary school subtopics

        populateBrowseByGradeElementarySubtopicsTemplate("Kindergarten", CmsConstants.KINDERGARTEN_TOPIC_CENTER_PATH);
        populateBrowseByGradeElementarySubtopicsTemplate("First Grade", CmsConstants.FIRST_GRADE_TOPIC_CENTER_PATH);
        populateBrowseByGradeElementarySubtopicsTemplate("Second Grade", CmsConstants.SECOND_GRADE_TOPIC_CENTER_PATH);
        populateBrowseByGradeElementarySubtopicsTemplate("Third Grade", CmsConstants.THIRD_GRADE_TOPIC_CENTER_PATH);
        populateBrowseByGradeElementarySubtopicsTemplate("Fourth Grade", CmsConstants.FOURTH_GRADE_TOPIC_CENTER_PATH);
        populateBrowseByGradeElementarySubtopicsTemplate("Fifth Grade", CmsConstants.FIFTH_GRADE_TOPIC_CENTER_PATH);

        // other subtopics

        populateBrowseByGradeSubtopicsTemplate("Preschool", pCats);
        populateBrowseByGradeSubtopicsTemplate("Kindergarten", kCats);
        populateBrowseByGradeSubtopicsTemplate("First Grade", firstCats);
        populateBrowseByGradeSubtopicsTemplate("Second Grade", secondCats);
        populateBrowseByGradeSubtopicsTemplate("Third Grade", thirdCats);
        populateBrowseByGradeSubtopicsTemplate("Fourth Grade", fourthCats);
        populateBrowseByGradeSubtopicsTemplate("Fifth Grade", fifthCats);
        populateBrowseByGradeSubtopicsTemplate("Middle School", middleCats);
        populateBrowseByGradeSubtopicsTemplate("High School", highCats);
    }

    private static List<CmsCategory> getCategoryList(long categoryId, String categoryType) {
        CmsCategory cat = new CmsCategory();
        cat.setId(categoryId);
        cat.setType(categoryType);
        List<CmsCategory> cats = new ArrayList<CmsCategory>();
        cats.add(cat);
        return cats;
    }

    private static List<CmsSubtopic> getBrowseByGradeHelper(CmsCategory additionalCategory) {
        List<CmsSubtopic> subtopics = new ArrayList<CmsSubtopic>();
        for (CmsSubtopic subtopic : BROWSE_BY_GRADE_SUBTOPICS_TEMPLATE) {
            CmsSubtopic copy = CmsSubtopic.deepCopy(subtopic);

            List<CmsCategory> cats = copy.getKategories();
            cats.add(additionalCategory);

            subtopics.add(copy);
        }
        return subtopics;
    }

    //=========================================================================
    // sample topic center
    //=========================================================================

    // START sample topic center methods

    private CmsTopicCenter getSampleTopicCenter() {
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        ContentKey contentKey = new ContentKey();
        contentKey.setType("TopicCenter");
        contentKey.setIdentifier(123L);

        topicCenter.setContentKey(contentKey);
        topicCenter.setTitle("title");
        topicCenter.setMetaDescription("meta description goes here");
        topicCenter.setMetaKeywords("meta keywords");
        topicCenter.setImageUrl("/res/img/feature_image.jpg");
        topicCenter.setImageAltText("feature image alt text");
        topicCenter.setContentProviderLogoUrl("/res/img/mostPopularContent/most_popular_sm_thumb.jpg");
        topicCenter.setContentProviderLogoAltText("content provider logo alt text");

        CmsCategory firstCat = new CmsCategory();
        firstCat.setId(1);
        firstCat.setName("Category 1");
        CmsCategory secondCat = new CmsCategory();
        secondCat.setId(2);
        secondCat.setName("Category 2");
        CmsCategory thirdCat = new CmsCategory();
        thirdCat.setId(3);
        thirdCat.setName("Category 3");
        List<CmsCategory> secondaryKategories = new ArrayList<CmsCategory>();
        secondaryKategories.add(thirdCat);

        topicCenter.setSecondaryKategories(secondaryKategories);
        List<CmsCategory> categoryBreadcrumbs = Arrays.asList(firstCat, secondCat, thirdCat);
        List<List<CmsCategory>> breadcrumbs = new ArrayList<List<CmsCategory>>();
        breadcrumbs.add(categoryBreadcrumbs);
        topicCenter.setSecondaryKategoryBreadcrumbs(breadcrumbs);

        List<CmsLink> featureLinks = new ArrayList<CmsLink>();
        CmsLink link = new CmsLink();
        link.setTitle("Lorem Ipsum Dolor Sit Amet");
        link.setUrl("http://www.google.com");
        link.setDescription("Quisque eu velit a libero pellentesque ullamcorper.");
        link.setLinkText("In semper purus eget justo");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Morbi eu sollicitudin augue");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Nunc sed turpis nisl, ac lacinia sem");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.org");
        link.setLinkText("Nulla sit amet libero orci, sed euismod nisl");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("In semper purus eget justo");
        featureLinks.add(link);
        topicCenter.setFeatureLinks(featureLinks);

        List<CmsLink> communityLinks = new ArrayList<CmsLink>();
        link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 1");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 2");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 3");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.org");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 4");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 5");
        communityLinks.add(link);
        topicCenter.setCommunityLinks(communityLinks);

        link = new CmsLink();
        link.setUrl("http://community.greatschools.org");
        link.setLinkText("More discussions &gt;");
        topicCenter.setCommunityMoreLink(link);

        List<CmsSubtopic> subtopics = new ArrayList<CmsSubtopic>();
        for (int i = 0; i < 3; i++) {
            subtopics.add(getSampleSubtopic(i));
        }
        topicCenter.setSubtopics(subtopics);

        return topicCenter;
    }

    private CmsSubtopic getSampleSubtopic(int i) {
        CmsSubtopic subtopic = new CmsSubtopic();
        subtopic.setTitle("subtopic title " + i);
        subtopic.setDescription("subtopic description " + i);
        subtopic.setImageUrl("/res/img/");
        subtopic.setImageAltText("subtopic image alt text " + i);

        List<CmsLink> links = new ArrayList<CmsLink>();
        for (int m = 0; m < 4; m++) {
            CmsLink link = new CmsLink();
            link.setUrl("http://www.google.com");
            link.setLinkText("sub link lorem ipsum lorem ipsum " + i + "." + m);
            links.add(link);
        }
        subtopic.setLinks(links);

        List<CmsSubSubtopic> subs = new ArrayList<CmsSubSubtopic>();
        for (int j = 0; j < 1; j++) {
            subs.add(getSampleSubSubtopic(i, j));
        }
        subtopic.setSubSubtopics(subs);

        return subtopic;
    }

    private CmsSubSubtopic getSampleSubSubtopic(int i, int j) {
        CmsSubSubtopic sub = new CmsSubSubtopic();
        sub.setTitle("sub title " + i + "." + j);

        CmsCategory cat = new CmsCategory();
        cat.setName("sub category " + i + "." + j);
        cat.setType("topic");
        List<CmsCategory> cats = new ArrayList<CmsCategory>();
        cats.add(cat);
        sub.setKategories(cats);

        List<CmsLink> links = new ArrayList<CmsLink>();
        for (int k = 0; k < 4; k++) {
            CmsLink link = new CmsLink();
            link.setUrl("http://www.google.com");
            link.setLinkText("sub link lorem ipsum lorem ipsum " + i + "." + j + "." + k);
            links.add(link);
        }
        sub.setLinks(links);

        CmsLink link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setLinkText("sub more link lorem ipsum lorem ipsum " + i + "." + j);
        sub.setMoreLink(link);

        sub.setMoreLinkText("sub more link text " + i + "." + j);
        return sub;
    }
    // END sample topic center methods

    //=========================================================================
    // spring-injected beans
    //=========================================================================

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public Boolean isUseAdKeywords() {
        return _useAdKeywords;
    }

    public void setUseAdKeywords(Boolean useAdKeywords) {
        _useAdKeywords = useAdKeywords;
    }

    public Long getTopicCenterContentID() {
        return _topicCenterContentID;
    }

    public void setTopicCenterContentID(Long topicCenterContentID) {
        _topicCenterContentID = topicCenterContentID;
    }

    public CmsContentLinkResolver getCmsFeatureEmbeddedLinkResolver() {
        return _cmsFeatureEmbeddedLinkResolver;
    }

    public void setCmsFeatureEmbeddedLinkResolver(CmsContentLinkResolver cmsFeatureEmbeddedLinkResolver) {
        _cmsFeatureEmbeddedLinkResolver = cmsFeatureEmbeddedLinkResolver;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public IRaiseYourHandDao getRaiseYourHandDao() {
        return _raiseYourHandDao;
    }

    public void setRaiseYourHandDao(IRaiseYourHandDao raiseYourHandDao) {
        _raiseYourHandDao = raiseYourHandDao;
    }

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao _localBoardDao) {
        this._localBoardDao = _localBoardDao;
    }
}