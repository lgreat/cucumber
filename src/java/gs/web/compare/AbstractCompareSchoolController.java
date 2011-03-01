package gs.web.compare;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

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
    public static final int DEFAULT_PAGE_SIZE = 4;
    public static final int MIN_SCHOOLS = 0;
    public static final int MAX_SCHOOLS = 8;

    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private IReviewDao _reviewDao;
    private IGeoDao _geoDao;
    private TestManager _testManager;
    private String _errorView = "/compare/error";
    private int _pageSize = DEFAULT_PAGE_SIZE;

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
            return getErrorResponse("invalid school string");
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
            return getErrorResponse("unknown exception");
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
                        urlBuilder = new UrlBuilder(state, id, UrlBuilder.SCHOOL_PARENT_REVIEWS);
                    } catch (Exception e) {
                        _log.warn("Can't find school from source string \"" + source + "\"", e);
                    }
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
                    model.put(MODEL_RETURN_LINK, urlBuilder.asSiteRelative(request));
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

    /**
     * Returns an error view.
     */
    protected ModelAndView getErrorResponse(String details) {
        Map<String, String> errorModel = new HashMap<String, String>(1);
        errorModel.put("details", details);
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
        if (!validateSchools(splitSchools)) {
            return null;
        }

        splitSchools = paginateSchools(request, splitSchools, model);
        List<ComparedSchoolBaseStruct> rval = new ArrayList<ComparedSchoolBaseStruct>(splitSchools.length);
        for (String splitSchool: splitSchools) {
            try {
                State state = State.fromString(splitSchool.substring(0, 2));
                int id = Integer.parseInt(splitSchool.substring(2));
                School school = _schoolDao.getSchoolById(state, id);
                if (school == null) {
                    _log.error("Compare schools School not found. School " + splitSchool +
                        " from \"" + schoolsParamValue + "\"");
                    return null;
                } else if (school.isPreschoolOnly()) {
                    _log.error("Compare does not support preschools.");
                    return null;
                }
                ComparedSchoolBaseStruct struct = getStruct();
                struct.setSchool(school);
                rval.add(struct);
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Compare schools School not found. School " + splitSchool +
                        " from \"" + schoolsParamValue + "\"");
                return null;
            }
        }
        return rval;
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
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean isFromCache = true;
        if (pageHelper != null && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
            isFromCache = false;
        }

        State state = schools.get(0).getSchool().getDatabaseState();
        IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(state, isFromCache);

        if (null != ratingsConfig) {
            for (ComparedSchoolBaseStruct baseStruct: schools) {
                handleGSRating(baseStruct, ratingsConfig);
            }
        }
    }

    /**
     * Sets the gsRating field for the provided school.
     */
    protected void handleGSRating(ComparedSchoolBaseStruct struct, IRatingsConfig ratingsConfig) {
        SchoolTestValue schoolTestValue =
                _testManager.getOverallRating(struct.getSchool(), ratingsConfig.getYear());
        if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
            struct.setGsRating(schoolTestValue.getValueInteger());
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

}
