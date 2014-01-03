package gs.web.compare;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.hubs.HubCityMapping;
import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.geo.CityHubHelper;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public abstract class AbstractCompareSchoolController extends AbstractController {
    private static final Logger _log = Logger.getLogger(AbstractCompareSchoolController.class);
    public static final String PARAM_SCHOOLS = "schools";
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_SOURCE = "source";
    public static final String MODEL_SOURCE = "source";
    public static final String MODEL_STATE = "state";
    public static final String MODEL_SCHOOLS_STRING = "schoolsString";
    public static final String MODEL_SCHOOL_IDS_STRING = "schoolIdsString";
    public static final String MODEL_SCHOOLS = "schools";
    public static final String MODEL_START_INDEX = "startIndex";
    public static final String MODEL_END_INDEX = "endIndex";
    public static final String MODEL_TOTAL_SCHOOLS = "totalSchools";
    public static final String MODEL_TAB = "tab";
    public static final String MODEL_PAGE_NUMBER = "page";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_RETURN_LINK = "returnLink";
    public static final String MODEL_RETURN_LINK_EXTRA = "returnLinkExtra";
    public static final String MODEL_REDIRECT_WITH_VALID_SCHOOLS = "RedirectWithValidSchools";
    public static final int DEFAULT_PAGE_SIZE = 4;
    public static final int MIN_SCHOOLS = 0;
    public static final int MAX_SCHOOLS = 8;
    public static final Pattern SPHEAD_SOURCE_PATTERN = Pattern.compile("sphead_([a-zA-Z\\-]+)_([a-zA-Z]{2})(\\d+)");

    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private IReviewDao _reviewDao;
    private IGeoDao _geoDao;
    private TestManager _testManager;
    private String _errorView = "/compare/error";
    private int _pageSize = DEFAULT_PAGE_SIZE;
    private ITestDataSetDao _testDataSetDao;

    @Autowired
    private IHubConfigDao _hubConfigDao;

    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;

    @Override
    /**
     * Handles common behavior, such as validation, error-handling, and pagination.
     * Delegates to handleCompareRequest
     */
    protected final ModelAndView handleRequestInternal(HttpServletRequest request,
                                                       HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<ComparedSchoolBaseStruct> schools = getSchools(request, model);
        if (schools == null) {
            return getErrorResponse("invalid school string",response);
        }else if(model.get(MODEL_REDIRECT_WITH_VALID_SCHOOLS)!= null){
            UrlBuilder builder = (UrlBuilder)model.get(MODEL_REDIRECT_WITH_VALID_SCHOOLS);
            return new ModelAndView(new RedirectView301(builder.asSiteRelative(request)));
        }
        model.put(MODEL_SCHOOLS_STRING, request.getParameter(PARAM_SCHOOLS));
        try {
            handleCompareRequest(request, response, schools, model);
            model.put(MODEL_SCHOOLS, schools);
            handleAdKeywords(request, schools);
            handleReturnLink(request, response, model);
            handleMSL(request, schools, model);
        } catch (Exception e) {
            _log.error(e, e);
            return getErrorResponse("unknown exception",response);
        }

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        Set<Integer> collectionIds = getCollectionIdsFromCompareList(schools);
        if (schools != null && !collectionIds.isEmpty() && pageHelper != null)   {
            boolean shouldHideAds = findIfAddsShouldbeHiddenOnPage(collectionIds);
            pageHelper.setHideAds(shouldHideAds);
            final HubCityMapping hubInfo= _hubCityMappingDao.getMappingObjectByCollectionID(collectionIds.iterator().next());
            pageHelper.clearHubCookiesForNavBar(request, response);
            pageHelper.setHubCookiesForNavBar(request, response, hubInfo.getState(), hubInfo.getCity());
            pageHelper.setHubUserCookie(request, response);
            model.put("isHubUserSet", "y");
            model.put("isLocal", hubInfo != null);
        }
      return new ModelAndView(getSuccessView(), model);
    }

    protected void handleMSL(HttpServletRequest request, List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) {
        if (schools == null || schools.size() == 0) {
            return;
        }
        // default msl list to every school
        String schoolIds = (String) model.get(MODEL_SCHOOLS_STRING);
        String mslSchoolIds = "";
        for (String schoolId: schoolIds.split(",")) {
          mslSchoolIds += schoolId.substring(2) + ",";
        }
        if (mslSchoolIds.length() > 0) {
            mslSchoolIds = mslSchoolIds.substring(0, mslSchoolIds.length()-1);
        }
        model.put(MODEL_SCHOOL_IDS_STRING, mslSchoolIds);
        model.put(MODEL_STATE, schools.get(0).getState());
        SessionContext sc = SessionContextUtil.getSessionContext(request);
        User user = sc.getUser();
        if (user == null) {
            return;
        }
        Map<Integer, Boolean> schoolIdToInMsl = new HashMap<Integer, Boolean>();
        State state = schools.get(0).getState();
        Set<FavoriteSchool> faveSchools = user.getFavoriteSchools();
        if (faveSchools == null || faveSchools.size() == 0) {
            return;
        }
        for (FavoriteSchool faveSchool: faveSchools) {
            if (state.equals(faveSchool.getState())) {
                schoolIdToInMsl.put(faveSchool.getSchoolId(), true);
            }
        }

        for (ComparedSchoolBaseStruct school: schools) {
            school.setInMsl(schoolIdToInMsl.get(school.getId()) != null);
        }
        mslSchoolIds = "";
        for (String schoolId: schoolIds.split(",")) { // for each school being compared
           if (schoolIdToInMsl.get(Integer.valueOf(schoolId.substring(2))) == null) {
               mslSchoolIds += schoolId.substring(2) + ","; // if it isn't in msl, add it to the eligibility list
           }
        }
        if (mslSchoolIds.length() > 0) {
            mslSchoolIds = mslSchoolIds.substring(0, mslSchoolIds.length()-1);
        }
        model.put(MODEL_SCHOOL_IDS_STRING, mslSchoolIds);
    }

    protected UrlBuilder handleSchoolProfileHeaderReturnLink(String source) {
        UrlBuilder urlBuilder = null;
        Matcher m = SPHEAD_SOURCE_PATTERN.matcher(source);
        if (!m.matches()) {
            _log.warn("Invalid format for source string \"" + source + "\"");
            return null;
        }
        try {
            String sourceCode = m.group(1);
            State state = State.fromString(m.group(2));
            Integer id = Integer.parseInt(m.group(3));
            School school = _schoolDao.getSchoolById(state, id);

            String subtab = "";
            if (sourceCode.indexOf("-") > -1) {
                subtab = sourceCode.substring(sourceCode.indexOf("-")+1);
                sourceCode = sourceCode.substring(0, sourceCode.indexOf("-"));
            }

            if (StringUtils.equals("overview", sourceCode)) {
                urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            } else if (StringUtils.equals("parentReviews", sourceCode)) {
                urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            } else if (StringUtils.equals("esp", sourceCode)) {
                urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP);
            } else if (StringUtils.equals("schoolMap", sourceCode)) {
                urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_MAP);
            } else if (StringUtils.equals("schoolStats", sourceCode) && StringUtils.isNotBlank(subtab)) {
                if (StringUtils.equals("teachersStudents", subtab)) {
                    urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS);
                } else if (StringUtils.equals("testScores", subtab)) {
                    urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
                } else if (StringUtils.equals("rating", subtab)) {
                    urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_RATINGS);
                } else if (StringUtils.equals("survey", subtab)) {
                    urlBuilder = new UrlBuilder(school, UrlBuilder.START_SURVEY_RESULTS);
                } else if (StringUtils.equals("official", subtab)) {
                    urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_PRINCIPAL_VIEW);
                }
            } else if (StringUtils.equals("survey", sourceCode)) {
                urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_START_SURVEY);
            }
            if (urlBuilder == null) {
                _log.warn("Unknown school profile source page to compare, defaulting to overview: " + source);
                // default to overview
                urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            }
        } catch (Exception e) {
            _log.warn("Can't find school, or other error from source string \"" + source + "\"", e);
        }

        return urlBuilder;
    }

    protected void handleReturnLink(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        String source = request.getParameter(PARAM_SOURCE);
        if (source != null) {
            model.put(MODEL_SOURCE, source);
            String schoolsString = StringUtils.upperCase(String.valueOf(model.get(MODEL_SCHOOLS_STRING)));
            if (StringUtils.startsWith(source, "http") || StringUtils.startsWith(source, "/")) {
                if (StringUtils.contains(source, "compareSchools=")) {
                    // need to replace parameter
                    int startReplace = source.indexOf("compareSchools=") + 15;
                    int endReplace = source.indexOf("&", startReplace);
                    if (endReplace == -1) {
                        endReplace = source.length();
                    }
                    source = source.substring(0, startReplace) + schoolsString +
                            (endReplace == -1 ? "" : source.substring(endReplace));
                } else {
                    source = UrlUtil.addParameter(source, "compareSchools=" + schoolsString);
                }
                model.put(MODEL_RETURN_LINK, source);
            } else {
                UrlBuilder urlBuilder = null;
                boolean maintainSelection = true;
                if (StringUtils.startsWith(source, "msl")) {
                    urlBuilder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
                } else if (StringUtils.startsWith(source, "spoverview")) {
                    try {
                        State state = State.fromString(source.substring(10, 12));
                        Integer id = Integer.parseInt(source.substring(12));
                        urlBuilder = new UrlBuilder(_schoolDao.getSchoolById(state, id), UrlBuilder.SCHOOL_PROFILE);
                    } catch (Exception e) {
                        _log.warn("Can't find school from source string \"" + source + "\"", e);
                    }
                } else if (StringUtils.startsWith(source, "spreviews")) {
                    try {
                        State state = State.fromString(source.substring(9, 11));
                        Integer id = Integer.parseInt(source.substring(11));
                        urlBuilder = new UrlBuilder(_schoolDao.getSchoolById(state, id), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                    } catch (Exception e) {
                        _log.warn("Can't find school from source string \"" + source + "\"", e);
                    }
                } else if (StringUtils.startsWith(source, "sphead_")) {
                    maintainSelection=false;
                    urlBuilder = handleSchoolProfileHeaderReturnLink(source);
                } else if (StringUtils.startsWith(source, "city")) {
                    maintainSelection = false;
                    try {
                        City city = _geoDao.findCityById(Integer.parseInt(source.substring(4)));
                        urlBuilder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
                        model.put(MODEL_RETURN_LINK_EXTRA, city.getDisplayName());
                    } catch (Exception e) {
                        _log.warn("Can't find city from source string \"" + source + "\"", e);
                    }
                } else if (StringUtils.startsWith(source, "distprof")) {
                    maintainSelection = false;
                    try {
                        State state = State.fromString(source.substring(8, 10));
                        urlBuilder = new UrlBuilder(UrlBuilder.DISTRICT_PROFILE, state, source.substring(10));
                    } catch (Exception e) {
                        _log.warn("Can't find city from source string \"" + source + "\"", e);
                    }
                }
                if (urlBuilder != null) {
                    if (maintainSelection) {
                        urlBuilder.setParameter("compareSchools", schoolsString);
                    }
                    model.put(MODEL_RETURN_LINK, urlBuilder.asFullUrl(request));
                }
            }
        }
    }

    /**
     * Insert into model whatever data needed by the view for the implementing class.
     * */
    protected abstract void handleCompareRequest(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 List<ComparedSchoolBaseStruct> schools,
                                                 Map<String, Object> model) throws Exception;
    /** Return the appropriate success view for the implementing class. */
    public abstract String getSuccessView();
    /** Return an instance of the struct appropriate for the implementing class. */
    protected abstract ComparedSchoolBaseStruct getStruct();
    /** Return the appropriate tab name for the implementing class. */
    protected abstract String getTabName();
    /**
     * Returns an error view.
     */
    protected ModelAndView getErrorResponse(String details,HttpServletResponse response) {
        Map<String, String> errorModel = new HashMap<String, String>(1);
        errorModel.put("details", details);
        response.setStatus(500);
        return new ModelAndView(_errorView, errorModel);
    }

    /**
     * Takes a split schools array (e.g. {"ca1", "ca2"} and paginates it. Puts appropriate pagination values
     * into the model and returns the sub-array for the desired page.
     */
    protected String[] paginateSchools(HttpServletRequest request, String[] schoolsArray,
                                       Map<String, Object> model) {
        model.put(MODEL_TOTAL_SCHOOLS, schoolsArray.length);
        model.put(MODEL_START_INDEX, 1);
        model.put(MODEL_END_INDEX, getPageSize());
        model.put(MODEL_PAGE_NUMBER, 1);
        model.put(MODEL_PAGE_SIZE, getPageSize());
        if (schoolsArray.length <= getPageSize()) {
            model.put(MODEL_END_INDEX, schoolsArray.length);
            return schoolsArray;
        }
        String pageNumber = request.getParameter(PARAM_PAGE);
        if (StringUtils.isBlank(pageNumber) || StringUtils.equals("1", pageNumber)
                || !StringUtils.isNumeric(pageNumber)) {
            return (String[]) ArrayUtils.subarray(schoolsArray, 0, getPageSize());
        }
        int startIndex, endIndex;
        try {
            int nPageNumber = Integer.parseInt(pageNumber);
            model.put(MODEL_PAGE_NUMBER, nPageNumber);
            endIndex = getPageSize() * nPageNumber;
            if (endIndex > schoolsArray.length) {
                endIndex = schoolsArray.length;
            }
            startIndex = endIndex - getPageSize();
            model.put(MODEL_START_INDEX, startIndex+1);
            model.put(MODEL_END_INDEX, endIndex);
            return (String[]) ArrayUtils.subarray(schoolsArray, startIndex, endIndex);
        } catch (NumberFormatException nfe) {
            _log.warn("Invalid page number: " + pageNumber);
            return (String[]) ArrayUtils.subarray(schoolsArray, 0, getPageSize());
        }
    }

    /**
     * Takes a split array of schools (e.g. {"ca1", "ca2"}) and validates it.
     * Checks number of schools, proper format of each school string, disparate states,
     * duplicate schools.
     *
     * If validation fails, returns false. Otherwise returns true.
     */
    protected boolean validateSchools(String[] schools) {
        if (schools.length < MIN_SCHOOLS) {
            _log.error("Compare schools with fewer than " + MIN_SCHOOLS +
                    " schools. String used: \"" + StringUtils.join(schools, ",") + "\"");
            return false;
        } else if (schools.length > MAX_SCHOOLS) {
            _log.error("Compare schools with more than " + MAX_SCHOOLS +
                    " schools. String used: \"" + StringUtils.join(schools, ",") + "\"");
            return false;
        }

        State theState = null;
        Set<Integer> idSet = new HashSet<Integer>(8);
        for (String school: schools) {
            if (StringUtils.length(school) < 3) {
                _log.error("Compare schools String invalid: \"" + school + "\"");
                return false;
            }
            try {
                State state = State.fromString(school.substring(0, 2));
                int id = Integer.parseInt(school.substring(2));
                if (theState == null) {
                    theState = state;
                } else if (!theState.equals(state)) {
                    _log.error("Differing states in compare schools string: " + StringUtils.join(schools, ","));
                    return false;
                }
                if (idSet.contains(id)) {
                    _log.error("Duplicate school in compare schools string: " + StringUtils.join(schools, ","));
                    return false;
                } else {
                    idSet.add(id);
                }
            } catch (IllegalArgumentException iae) {
                _log.error("Compare schools String invalid: \"" + school + "\"");
                return false;
            }
        }
        return true;
    }

    /**
     * Fetches the schools for the current page. This method performs validation on the
     * schools request parameter, paginates it, fetches each school, and places them into
     * the appropriate struct.
     *
     * Returns null on error. Otherwise returns a list of school structs.
     */
    protected List<ComparedSchoolBaseStruct> getSchools(HttpServletRequest request, Map<String, Object> model) {
        String schoolsParamValue = request.getParameter(PARAM_SCHOOLS);
        if (StringUtils.isBlank(schoolsParamValue)) {
//            _log.error("Compare schools string empty");
//            return null;
            return new ArrayList<ComparedSchoolBaseStruct>(0);
        }
        String[] splitSchools = schoolsParamValue.split(",");

        int numSchoolsOriginal =  splitSchools.length;

        //Store all the schools in the request param in a list.
        List<String> schoolsList = new ArrayList(Arrays.asList(splitSchools));

        if (!validateSchools(splitSchools)) {
            return null;
        }

        splitSchools = paginateSchools(request, splitSchools, model);

        //Remove all the schools that are on the current page from the list.
        //Only valid schools will be added back in the list.
        List<String> schoolsToRemove = new ArrayList(Arrays.asList(splitSchools));
        for (int i = 0; i < splitSchools.length; i++) {
            for (String school : schoolsList) {
                if (school.equals(splitSchools[i])) {
                    schoolsToRemove.add(school);
                }
            }
        }
        schoolsList.removeAll(schoolsToRemove);

        List<ComparedSchoolBaseStruct> rval = new ArrayList<ComparedSchoolBaseStruct>(splitSchools.length);
        for (String splitSchool: splitSchools) {
            try {
                State state = State.fromString(splitSchool.substring(0, 2));
                int id = Integer.parseInt(splitSchool.substring(2));
                School school = _schoolDao.getSchoolById(state, id);
                if (school == null) {
                    _log.error("Compare schools School not found. School " + splitSchool +
                        " from \"" + schoolsParamValue + "\"");
                } else if (school.isPreschoolOnly()) {
                    _log.error("Compare does not support preschools.");
                } else if (!school.isActive()){
                    _log.error("School is not active.");
                }else{
                    ComparedSchoolBaseStruct struct = getStruct();
                    struct.setSchool(school);
                    rval.add(struct);
                    //Add valid schools to the list.
                    schoolsList.add(splitSchool);
                }

            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Compare schools School not found. School " + splitSchool +
                        " from \"" + schoolsParamValue + "\"");
                return null;
            }
        }

        if(numSchoolsOriginal != schoolsList.size() && schoolsList.size()>0){
            getRedirectWithValidSchools(request,schoolsList,model);
        }

        return rval.isEmpty() ? null : rval;
    }

    protected void getRedirectWithValidSchools(HttpServletRequest request, List<String> schoolsList,
                                               Map<String, Object> model) {

        UrlBuilder builder = getUrlBuilderForCompareTool(getTabName());
        Map<String, String> queryParams = UrlUtil.getParamsFromQueryString(request.getQueryString());

        for (String param : queryParams.keySet()) {
            if (!param.equals(PARAM_SCHOOLS)) {
                builder.setParameter(param, queryParams.get(param));
            }
        }

        String[] schoolsArr = schoolsList.toArray(new String[schoolsList.size()]);
        builder.setParameter(PARAM_SCHOOLS, StringUtils.join(schoolsArr, ","));
        model.put(MODEL_REDIRECT_WITH_VALID_SCHOOLS, builder);
    }

    /**
     * For each school, set the appropriate Google ad keywords.
     */
    protected void handleAdKeywords(HttpServletRequest request, List<ComparedSchoolBaseStruct> structs) {
        try {
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

            Set<String> typeSet = new HashSet<String>();
            Set<String> levelSet = new HashSet<String>();
            Set<String> countySet = new HashSet<String>();
            Set<String> citySet = new HashSet<String>();
            Set<String> schoolIdSet = new HashSet<String>();
            Set<String> zipSet = new HashSet<String>();
            Set<String> districtNameSet = new HashSet<String>();
            Set<String> districtIdSet = new HashSet<String>();
            if (null != pageHelper) {
                for (ComparedSchoolBaseStruct struct: structs) {
                    School school = struct.getSchool();
                    String schoolType = school.getType().getSchoolTypeName();
                    // GS-5064
                    String county = school.getCounty();
                    String city = school.getCity();
                    typeSet.add(schoolType);
                    for (LevelCode.Level level : school.getLevelCode().getIndividualLevelCodes()) {
                        levelSet.add(level.getName());
                    }
                    countySet.add(county);
                    citySet.add(city);
                    schoolIdSet.add(school.getId().toString());
                    zipSet.add(school.getZipcode());

                    // set district name and id ad attributes only if there's a district and school is not preschool-only
                    if (school.getDistrictId() != 0 && school.getLevelCode() != null
                            && !school.getLevelCode().toString().equals("p")) {
                        districtNameSet.add(school.getDistrict().getName());
                        districtIdSet.add(String.valueOf(school.getDistrictId()));
                    }
                }
                addAdKeywordMulti("type", typeSet, pageHelper);
                addAdKeywordMulti("level", levelSet, pageHelper);
                addAdKeywordMulti("county", countySet, pageHelper);
                addAdKeywordMulti("city", citySet, pageHelper);
                addAdKeywordMulti("school_id", schoolIdSet, pageHelper);
                addAdKeywordMulti("zipcode", zipSet, pageHelper);
                addAdKeywordMulti("district_name", districtNameSet, pageHelper);
                addAdKeywordMulti("district_id", districtIdSet, pageHelper);
            }
        } catch (Exception e) {
            _log.warn("Error constructing ad keywords in new compare");
        }
    }

    /**
     * Helper method to add all the keywords in a set.
     */
    protected void addAdKeywordMulti(String key, Set<String> values, PageHelper pageHelper) {
        for (String value: values) {
            pageHelper.addAdKeywordMulti(key, value);
        }
    }

    // SHARED METHODS FOR SUB-CLASSES

    /**
     * Sets the gsRating field for the provided schools.
     */
    protected void handleGSRating(HttpServletRequest request, List<ComparedSchoolBaseStruct> schools) throws IOException {
        if (schools.size() == 0) {
            return; // early exit
        }

        for (ComparedSchoolBaseStruct baseStruct : schools) {
            Integer rating = baseStruct.getSchool().getGSRating();
            if( rating != null ) {
                baseStruct.setGsRating(rating);
            }
        }
    }

    /**
     * Find the most recent review for each school and add it to the struct.
     */
    protected void handleRecentReview(List<ComparedSchoolBaseStruct> structs) {
        for (ComparedSchoolBaseStruct struct: structs) {
            School school = struct.getSchool();
            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school, 1);
            if (reviews != null && reviews.size() == 1) {
                struct.setRecentReview(reviews.get(0));
            }
            Long numReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);
            if (numReviews != null && numReviews > 0) {
                struct.setNumReviews(numReviews.intValue());
            }
        }
    }

     /**
     * Determine the average overall rating for each school
     */
    protected void handleCommunityRating(List<ComparedSchoolBaseStruct> structs) {
        for (ComparedSchoolBaseStruct struct: structs) {
            School school = struct.getSchool();
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            if (ratings == null || ratings.getOverall() == null) {
                struct.setCommunityRating(0);
            } else {
                struct.setCommunityRating(ratings.getOverall());
                if (ratings.getCount() != null) {
                    struct.setNumRatings(ratings.getCount());
                }
            }
        }
    }

    public static UrlBuilder getUrlBuilderForCompareTool(String tabName) {
        UrlBuilder builder;
        if (StringUtils.equals(CompareOverviewController.TAB_NAME, tabName)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_OVERVIEW);
        } else if (StringUtils.equals(CompareRatingsController.TAB_NAME, tabName)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_RATINGS);
        } else if (StringUtils.equals(CompareTestScoresController.TAB_NAME, tabName)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_TEST_SCORES);
        } else if (StringUtils.equals(CompareStudentTeacherController.TAB_NAME, tabName)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_STUDENT_TEACHER);
        } else if (StringUtils.equals(CompareProgramsExtracurricularsController.TAB_NAME, tabName)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_PROGRAMS_EXTRACURRICULARS);
        } else if (StringUtils.equals(CompareMapController.TAB_NAME, tabName)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_MAP);
        } else {
            throw new IllegalArgumentException("Tab not recognized for compare: " + tabName);
        }
        return builder;
    }

    /** @param collectionIds listofSchoolCollectionIDs
     * Get the set of collection ids to which each school in the compare list belongs to from school metadata. From the
     * set of collection ids get the hub config records with key "showAds". Hide if the value is "false". Show ads if
     * doesn't exist or if the value is "true".
     */
    public boolean findIfAddsShouldbeHiddenOnPage(final Set<Integer> collectionIds) {
        boolean shouldHideAds = false;
                List<HubConfig> hubConfigs = _hubConfigDao.getConfigFromCollectionIdsAndKey(collectionIds, CityHubHelper.SHOW_ADS_KEY);
                if (hubConfigs != null) {
                    for (int i = 0; i < hubConfigs.size(); i++) {
                        HubConfig hubConfig = hubConfigs.get(i);

                        if (hubConfig != null && CityHubHelper.SHOW_ADS_KEY.equals(hubConfig.getQuay())
                                &&   "false".equals(hubConfig.getValue())) {
                            shouldHideAds = true;
                            break;
                        }
                    }
                }


        return shouldHideAds;
    }

    public Set<Integer> getCollectionIdsFromCompareList(List<ComparedSchoolBaseStruct> schools) {
        Set<Integer> collectionIds = new HashSet<Integer>();
        for (int i = 0; i < schools.size(); i++) {
            ComparedSchoolBaseStruct schoolBaseStruct = schools.get(i);
            School school = schoolBaseStruct.getSchool();
            String collectionIdAsString = school.getMetadataValue(School.METADATA_COLLECTION_ID_KEY);
            if(collectionIdAsString != null) {
                try {
                    Integer collectionId = Integer.parseInt(collectionIdAsString);
                    collectionIds.add(collectionId);
                }
                catch (NumberFormatException ex) {
                    _log.error("AbstractCompareSchoolController - Error while trying to convert collection id in" +
                            "string to integer.\n", ex.fillInStackTrace());
                }
            }
        }

        return collectionIds;
    }

    // FIELD ACCESSOR/MUTATORS

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
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
    
    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        this._reviewDao = reviewDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public String getErrorView() {
        return _errorView;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }

    public int getPageSize() {
        return _pageSize;
    }

    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public IHubConfigDao getHubConfigDao() {
        return _hubConfigDao;
    }

    public void setHubConfigDao(IHubConfigDao hubConfigDao) {
        _hubConfigDao = hubConfigDao;
    }
}
