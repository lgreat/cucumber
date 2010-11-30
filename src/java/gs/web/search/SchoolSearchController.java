package gs.web.search;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.seo.SeoUtil;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class SchoolSearchController extends AbstractCommandController implements IDirectoryStructureUrlController {

    private IDistrictDao _districtDao;

    private IGeoDao _geoDao;

    private SchoolSearchService _schoolSearchService;

    private CitySearchService _citySearchService;

    private DistrictSearchService _districtSearchService;

    private StateManager _stateManager;

    private static final Logger _log = Logger.getLogger(SchoolSearchController.class);

    public static final String MODEL_SCHOOL_TYPE = "schoolType";
    public static final String MODEL_LEVEL_CODE = "levelCode";
    public static final String MODEL_SORT = "sort";
    public static final String MODEL_SCHOOL_SEARCH_RESULTS = "schoolSearchResults";
    public static final String MODEL_CITY_SEARCH_RESULTS = "citySearchResults";
    public static final String MODEL_DISTRICT_SEARCH_RESULTS = "districtSearchResults";
    public static final String MODEL_START = "start";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_TOTAL_RESULTS = "totalResults";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_CURRENT_PAGE = "currentPage";
    public static final String MODEL_USE_PAGING = "usePaging";
    public static final String MODEL_CITY_ID = "cityId";
    public static final String MODEL_REL_CANONICAL = "relCanonical";
    public static final String MODEL_SEARCH_STRING = "searchString";
    public static final String MODEL_MSL_SCHOOLS = "mslSchools";
    public static final String MODEL_TITLE = "title";
    public static final String MODEL_META_DESCRIPTION = "metaDescription";
    public static final String MODEL_META_KEYWORDS = "metaKeywords";

    public static final int MAX_PAGE_SIZE = 100;

    // TODO: Omniture tracking

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        if (e.hasErrors()) {
            //TODO: handle errors
        }

        Map<String,Object> model = new HashMap<String,Object>();

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        if (user != null) {
            Set<FavoriteSchool> mslSchools = user.getFavoriteSchools();
            model.put(MODEL_MSL_SCHOOLS, mslSchools);
        }

        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        State state = null;
        if (schoolSearchCommand.getState() != null) {
            try {
                state = State.fromString(schoolSearchCommand.getState());
            } catch (IllegalArgumentException iae) {
                // invalid state
            }
        } else if (fields != null && fields.getState() != null) {
            state = fields.getState();
        } else {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }

        City city = null;
        District district = null;

        if (fields != null) {
            String cityName = fields.getCityName();
            String districtName = fields.getDistrictName();

            if (StringUtils.isNotBlank(districtName) && state != null && StringUtils.isNotBlank(cityName)) {
                // might be null
                district = getDistrictDao().findDistrictByNameAndCity(state, districtName, cityName);
            }

            if (StringUtils.isNotBlank(cityName) && state != null) {
                // might be null
                city = getGeoDao().findCity(state, cityName);
            }
        }

        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(state, city, district);

        String[] schoolSearchTypes = null;
        LevelCode levelCode = null;

        List<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
        if (fields != null) {
            schoolSearchTypes = fields.getSchoolTypesParams();
            levelCode = fields.getLevelCode();
        } else {
            schoolSearchTypes = schoolSearchCommand.getSchoolTypes();
            levelCode = LevelCode.createLevelCode(schoolSearchCommand.getGradeLevels());
        }
        if (schoolSearchTypes != null && schoolSearchTypes.length > 0) {
            FilterGroup filterGroup = new FilterGroup();
            filterGroup.setFieldFilters(getSchoolTypeFilters(schoolSearchTypes).toArray(new FieldFilter[0]));
            filterGroups.add(filterGroup);
        }
        if (levelCode != null) {
            FilterGroup filterGroup = new FilterGroup();
            FieldFilter[] filters = getGradeLevelFilters(levelCode).toArray(new FieldFilter[0]);
            if (filters != null && filters.length > 0) {
                filterGroup.setFieldFilters(getGradeLevelFilters(levelCode).toArray(new FieldFilter[0]));
                filterGroups.add(filterGroup);
            }
        }

        FieldSort sort = this.getChosenSort(schoolSearchCommand);

        SearchResultsPage<ISchoolSearchResult> searchResultsPage = getSchoolSearchService().search(
                schoolSearchCommand.getSearchString(),
                fieldConstraints,
                filterGroups,
                sort,
                schoolSearchCommand.getStart(),
                schoolSearchCommand.getPageSize()
        );

        List<ICitySearchResult> citySearchResults = null;
        if (schoolSearchCommand.getSearchString() != null) {
            citySearchResults = getCitySearchService().search(schoolSearchCommand.getSearchString(), state);
            model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        }
        //List<IDistrictSearchResult> districtSearchResults = getDistrictSearchService().search(schoolSearchCommand.getSearchString(), state);

        PageHelper.setHasSearchedCookie(request, response);

        //TODO: write district lists to model

        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(schoolSearchTypes));
        if (levelCode != null) {
            model.put(MODEL_LEVEL_CODE, levelCode.getCommaSeparatedString());
        }
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());

        addPagingDataToModel(schoolSearchCommand.getStart(), schoolSearchCommand.getPageSize(), searchResultsPage.getTotalResults(), model); //TODO: fix
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        addGamAttributes(request, response, pageHelper, fieldConstraints, filterGroups, schoolSearchCommand.getSearchString(), searchResultsPage.getSearchResults(), city, district);

        // city id needed for local community module
        SessionContext context = SessionContextUtil.getSessionContext(request);
        if (context.getCityId() != null) {
            model.put(MODEL_CITY_ID, context.getCityId());
        }

        model.put(MODEL_SEARCH_STRING, schoolSearchCommand.getSearchString());
        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());
        model.put(MODEL_TOTAL_RESULTS, searchResultsPage.getTotalResults());
        model.put(MODEL_REL_CANONICAL,  getRelCanonical(request, state, citySearchResults, city, district,
                                     filterGroups, levelCode, schoolSearchCommand.getSearchString()));
        model.put(MODEL_TITLE, getTitle(city, district, levelCode, schoolSearchTypes, schoolSearchCommand.getSearchString()));
        model.put(MODEL_META_DESCRIPTION, getMetaDescription(city, district, levelCode, schoolSearchTypes));
        model.put(MODEL_META_KEYWORDS, getMetaKeywords(district));


        if (schoolSearchCommand.isJsonFormat()) {
            return new ModelAndView("/search/schoolSearchResultsTable", model);
        } else {
            if (searchResultsPage.getTotalResults() == 0) {
                return new ModelAndView("/search/schoolSearchNoResults", model);
            } else {
                return new ModelAndView("/search/schoolSearchResults", model);
            }
        }
    }

    public static String calcMetaDesc(String districtDisplayName, String cityDisplayName,
                                      State state, LevelCode levelCode, String[] schoolType) {
        StringBuffer sb = new StringBuffer();
        StringBuffer cityWithModifier = new StringBuffer();
        StringBuffer modifier = new StringBuffer();

        if (schoolType != null && schoolType.length == 1) {
            if ("private".equals(schoolType[0])) {
                modifier.append("private");
            } else if ("charter".equals(schoolType[0])) {
                modifier.append("charter");
            } else {
                modifier.append("public");
            }
            modifier.append(" ");

        }

        if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1) {
            if (levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                modifier.append("preschool");
            } else if (levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                modifier.append("elementary");
            } else if (levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                modifier.append("middle");
            } else if (levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                modifier.append("high");
            }
            modifier.append(" ");

        }


        if (districtDisplayName == null) {
            cityWithModifier.append(cityDisplayName).append(" ").append(modifier);
            // for preschools, do a special SEO meta description
            if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1 &&
                    levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                sb.append("Find the best preschools in ").append(cityDisplayName);
                if (state != null) {
                    // pretty sure State can never be null here, but why take a chance?
                    sb.append(", ").append(state.getLongName()).append(" (").
                            append(state.getAbbreviation()).
                            append(")");
                }
                sb.append(" - view preschool ratings, reviews and map locations.");
            } else {
                sb.append("View and map all ").append(cityWithModifier).
                        append("schools. Plus, compare or save ").
                        append(modifier).append("schools.");
            }
        } else {
            sb.append("View and map all ").append(modifier).
                    append("schools in the ").append(districtDisplayName).
                    append(". Plus, compare or save ").append(modifier).
                    append("schools in this district.");
        }

        return sb.toString();
    }

    public static String calcCitySchoolsTitle(String cityDisplayName, State cityState, LevelCode levelCode, String[] schoolType) {
        StringBuffer sb = new StringBuffer();
        sb.append(cityDisplayName);
        if (schoolType != null && (schoolType.length == 1 || schoolType.length == 2)) {
            for (int x=0; x < schoolType.length; x++) {
                if (x == 1) {
                    sb.append(" and");
                }
                if ("private".equals(schoolType[x])) {
                    sb.append(" Private");
                } else if ("charter".equals(schoolType[x])) {
                    sb.append(" Public Charter");
                } else {
                    sb.append(" Public");
                }
            }
        }
        if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1) {
            if (levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                sb.append(" Preschool");
            } else if (levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                sb.append(" Elementary");
            } else if (levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                sb.append(" Middle");
            } else if (levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                sb.append(" High");
            }
        }
        if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1 &&
                levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
            sb.append("s and Daycare Centers");
        } else {
            sb.append(" Schools");
        }
        sb.append(" - ").append(cityDisplayName).append(", ").append(cityState.getAbbreviation());
        sb.append(" | GreatSchools");
        return sb.toString();
    }

    protected String getTitle(City city, District district, LevelCode levelCode, String[] schoolTypes, String searchString) {
        if (city != null) {
            return calcCitySchoolsTitle(city.getDisplayName(), city.getState(), levelCode, schoolTypes);
        } else if (district != null) {
            return SeoUtil.generatePageTitle(district, levelCode, schoolTypes);
        } else if (StringUtils.isNotBlank(searchString)) {
            return "GreatSchools.org Search: " + StringEscapeUtils.escapeHtml(searchString);
        } else {
            return "GreatSchools.org Search";
        }
    }

    protected String getMetaDescription(City city, District district, LevelCode levelCode, String[] schoolTypes) {
        if (city != null) {
            return calcMetaDesc(null, city.getDisplayName(), city.getState(), levelCode, schoolTypes);
        } else if (district != null) {
            return SeoUtil.generateMetaDescription(district);
        } else {
            return null;
        }
    }

    protected String getMetaKeywords(District district) {
        if (district != null) {
            return SeoUtil.generateMetaKeywords(district);
        }
        return null;
    }

    /**
     * Calculates paging info and adds it to model. Paging is zero-based. First search result has start=0
     * 
     * @param start
     * @param pageSize
     * @param totalResults
     * @param model
     */
    protected void addPagingDataToModel(int start, Integer pageSize, int totalResults, Map<String,Object> model) {

        //TODO: perform validation to only allow no paging when results are a certain size
        if (pageSize > 0) {
            int currentPage = (int) Math.ceil((start+1) / pageSize.floatValue());
            int numberOfPages = (int) Math.ceil(totalResults / pageSize.floatValue());
            model.put(MODEL_CURRENT_PAGE, currentPage);
            model.put(MODEL_TOTAL_PAGES, numberOfPages);
            model.put(MODEL_USE_PAGING, Boolean.valueOf(true));
        } else {
            model.put(MODEL_USE_PAGING, Boolean.valueOf(false));
        }

        model.put(MODEL_START, start);
        model.put(MODEL_PAGE_SIZE, pageSize);
    }

    protected void addGamAttributes(HttpServletRequest request, HttpServletResponse response, PageHelper pageHelper,
                                    Map<FieldConstraint,String> constraints, List<FilterGroup> filterGroups, String searchString,
                                    List<ISchoolSearchResult> schoolResults,
                                    City city, District district) {
        if (pageHelper == null || constraints == null || filterGroups == null || schoolResults == null) {
            // search string can be null
            throw new IllegalArgumentException("PageHelper, constraints, filters, and school results must not be null");
        }

        Set<FieldFilter> filtersSet = new HashSet<FieldFilter>();
        for (FilterGroup filterGroup : filterGroups) {
            filtersSet.addAll(Arrays.asList(filterGroup.getFieldFilters()));
        }

        // GS-10003 - school type
        for (FieldFilter.SchoolTypeFilter schoolTypeFilter : FieldFilter.SchoolTypeFilter.values()) {
            if (filtersSet.contains(schoolTypeFilter)) {
                pageHelper.addAdKeywordMulti("type", schoolTypeFilter.toString().toLowerCase());
            }
        }

        // GS-6875 - level
        Set<FieldFilter.GradeLevelFilter> gradeLevelFilters = new HashSet<FieldFilter.GradeLevelFilter>();
        for (FieldFilter.GradeLevelFilter gradeLevelFilter : FieldFilter.GradeLevelFilter.values()) {
            if (filtersSet.contains(gradeLevelFilter)) {
                gradeLevelFilters.add(gradeLevelFilter);
            }
        }
        if (gradeLevelFilters.size() == 0) {
            gradeLevelFilters.addAll(Arrays.asList(FieldFilter.GradeLevelFilter.values()));
        }
        for (FieldFilter.GradeLevelFilter gradeLevelFilter : gradeLevelFilters) {
            pageHelper.addAdKeywordMulti("level", String.valueOf(gradeLevelFilter.name().toLowerCase().charAt(0)));
        }

        // GS-10157 - district browse
        State state = null;
        Integer districtId = null;
        try {
            state = State.fromString(constraints.get(FieldConstraint.STATE));
            districtId = Integer.parseInt(constraints.get(FieldConstraint.DISTRICT_ID));
        } catch (IllegalArgumentException e) {
            // nothing to do, invalid state or district ID
        }

        if (district != null) {
            pageHelper.addAdKeyword("district_id", constraints.get(FieldConstraint.DISTRICT_ID));
            pageHelper.addAdKeyword("district_name", district.getName());
        }

        // GS-10448 - search results
        if (StringUtils.isNotBlank(searchString) && schoolResults != null) {
            Set<String> cityNames = new HashSet<String>();
            for (ISchoolSearchResult schoolResult : schoolResults) {
                Address address = schoolResult.getAddress();
                if (address != null) {
                    String cityName = address.getCity();
                    if (StringUtils.isNotBlank(cityName)) {
                        cityNames.add(cityName);
                    }
                }
            }
            for (String cityName : cityNames) {
                pageHelper.addAdKeywordMulti("city", cityName);
            }
        }

        // GS-5786 - city browse, GS-7809 - adsense hints for realtor.com, GS-6971 - city id cookie
        String cityName = constraints.get(FieldConstraint.CITY);
        if (city != null) {
            // GS-5786 - city browse
            cityName = WordUtils.capitalize(city.getName());
            cityName = WordUtils.capitalize(cityName, new char[]{'-'});
            pageHelper.addAdKeywordMulti("city", cityName);

            // GS-7809 - adsense hints for realtor.com
            StringBuilder adSenseHint = new StringBuilder();
            adSenseHint.append(cityName.toLowerCase());
            adSenseHint.append(" ");
            adSenseHint.append(state.getLongName().toLowerCase());
            adSenseHint.append(" real estate house homes for sale");
            pageHelper.addAdSenseHint(adSenseHint.toString());

            // GS-6971 - city id cookie
            PageHelper.setCityIdCookie(request, response, city);
        }

        // GS-10642 - query, GS-9323 zip code
        if (StringUtils.isNotBlank(searchString)) {
            // GS-10642 - query
            // also consider hyphens to be token separators
            String queryString = searchString.replaceAll("-"," ");
            String[] tokens = StringUtils.split(queryString);
            List<String> tokenList = Arrays.asList(tokens);

            Set<String> terms = new HashSet<String>(tokenList);
            for (String term : terms) {
                pageHelper.addAdKeywordMulti("query", term);
            }

            // GS-9323 zip code
            if (searchString.trim().matches("^\\d{5}$")) {
                pageHelper.addAdKeyword("zipcode", searchString.trim());
            }
        }
    }

    // TODO: fixme rel="canonical" not working for city browse with school types, level codes
    protected String getRelCanonical(HttpServletRequest request, State state, List<ICitySearchResult> citySearchResults, City city, District district,
                                     List<FilterGroup> filterGroups, LevelCode levelCode, String searchString) {
        if (request == null || state == null) {
            throw new IllegalArgumentException("Request and state must not be null");
        }
        String url = null;

        if (StringUtils.isNotBlank(searchString) && state != null) {
            // GS-10036 - search pages
            // search string that matches city, e.g. q=alameda&state=CA
            if (citySearchResults != null) {
                for (ICitySearchResult cityResult : citySearchResults) {
                    try {
                        if (StringUtils.equalsIgnoreCase(searchString, cityResult.getCity())
                                && StringUtils.equalsIgnoreCase(state.getAbbreviation(), cityResult.getState().toString())) {
                            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, state, searchString);
                            url = urlBuilder.asFullUrl(request);
                        }
                    } catch (Exception e) {
                        _log.warn("Error determining city URL for canonical: " + e, e);
                    }
                }
            }
            // if no valid city is discernible, the result should be canonicalized to the appropriate state home page
            if (url == null) {
                try {
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, state);
                    url = urlBuilder.asFullUrl(request);
                } catch (Exception e) {
                    _log.warn("Error determining state URL for canonical: " + e, e);
                }
            }
        } else {
            // GS-10144, GS-10400 - browse pages

            Set<FieldFilter> filtersSet = new HashSet<FieldFilter>();
            if (filterGroups != null) {
                for (FilterGroup filterGroup : filterGroups) {
                    filtersSet.addAll(Arrays.asList(filterGroup.getFieldFilters()));
                }
            }

            if (district != null) {
                // district browse
                UrlBuilder urlBuilder = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
                url = urlBuilder.asFullUrl(request) + (levelCode != null ? "?lc=" + levelCode : "");
            } else if (city != null) {
                // city browse
                boolean publicSelected = filtersSet.contains(FieldFilter.SchoolTypeFilter.PUBLIC);
                boolean charterSelected = filtersSet.contains(FieldFilter.SchoolTypeFilter.CHARTER);
                boolean privateSelected = filtersSet.contains(FieldFilter.SchoolTypeFilter.PRIVATE);

                HashSet<SchoolType> schoolTypeSet = new HashSet<SchoolType>(1);
                if (publicSelected && !charterSelected && !privateSelected) {
                    schoolTypeSet.add(SchoolType.PUBLIC);
                } else if (!publicSelected && charterSelected && !privateSelected) {
                    schoolTypeSet.add(SchoolType.CHARTER);
                } else if (!publicSelected && !charterSelected && privateSelected) {
                    schoolTypeSet.add(SchoolType.PRIVATE);
                }

                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                        state,
                        city.getName(),
                        schoolTypeSet, levelCode);
                url = urlBuilder.asFullUrl(request);
            }
        }

        return url;
    }

        // TODO: rel="canonical"
        // GS-10036
        // SearchController:
        // lines 336-340, 419-426
        //                    for (int x=0; x < cityHits.length(); x++) {
        //                        Document cityDoc = cityHits.doc(x);
        //                        try {
        //                            if (StringUtils.equalsIgnoreCase(queryString, cityDoc.get("city"))
        //                                    && StringUtils.equalsIgnoreCase(state.getAbbreviation(), cityDoc.get("state"))) {
        //                                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, state, queryString);
        //                                model.put(MODEL_REL_CANONICAL, urlBuilder.asFullUrl(request));
        //                            }
        //                        } catch (Exception e) {
        //                            _log.warn("Error determining city URL for canonical: " + e, e);
        //                        }
        //                    }

        //
        //        if (searchCommand.isSchoolsOnly() && model.get(MODEL_REL_CANONICAL) == null) {
        //            try {
        //                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, state);
        //                model.put(MODEL_REL_CANONICAL, urlBuilder.asFullUrl(request));
        //            } catch (Exception e) {
        //                _log.warn("Error determining state URL for canonical: " + e, e);
        //            }
        //        }

        // GS-10144, GS-10400
        // schoolsTable.jspx: (SchoolsController)
        // lines 385-414
        //        if (request.getAttribute("districtObject") != null) {
        //            LevelCode levelCode = (LevelCode) request.getAttribute("lc");
        //            UrlBuilder myCanonical = new UrlBuilder((District) request.getAttribute("districtObject"),
        //                                                    UrlBuilder.SCHOOLS_IN_DISTRICT);
        //            request.setAttribute("myCanonicalUrl", myCanonical.asFullUrl(request) + (levelCode != null ? "?lc=" + levelCode : ""));
        //        } else if (request.getAttribute("cityName") != null) {
        //            HashSet&lt;SchoolType> schoolTypeSet = new HashSet&lt;SchoolType>(1);
        //            if (request.getAttribute("publicChecked") != null
        //                    &amp;&amp; request.getAttribute("charterChecked") == null
        //                    &amp;&amp; request.getAttribute("privateChecked") == null) {
        //                schoolTypeSet.add(SchoolType.PUBLIC);
        //            } else if (request.getAttribute("publicChecked") == null
        //                    &amp;&amp; request.getAttribute("charterChecked") != null
        //                    &amp;&amp; request.getAttribute("privateChecked") == null) {
        //                schoolTypeSet.add(SchoolType.CHARTER);
        //            } else if (request.getAttribute("publicChecked") == null
        //                    &amp;&amp; request.getAttribute("charterChecked") == null
        //                    &amp;&amp; request.getAttribute("privateChecked") != null) {
        //                schoolTypeSet.add(SchoolType.PRIVATE);
        //            }
        //            UrlBuilder myCanonical = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
        //                                                    SessionContextUtil.getSessionContext(request).getStateOrDefault(),
        //                                                    (String)request.getAttribute("cityName"),
        //                                                    schoolTypeSet, (LevelCode)request.getAttribute("lc"));
        //            request.setAttribute("myCanonicalUrl", myCanonical.asFullUrl(request));
        //        }
        //    </jsp:scriptlet>
        //    <c:if test="${not empty myCanonicalUrl}">
        //        <link rel="canonical" href="${myCanonicalUrl}"/>
        //    </c:if>

    protected Map<FieldConstraint,String> getFieldConstraints(State state, City city, District district) {
        Map<FieldConstraint,String> fieldConstraints = new HashMap<FieldConstraint,String>();

        if (state != null) {
            fieldConstraints.put(FieldConstraint.STATE, state.getAbbreviationLowerCase());
        }

        if (city != null) {
            fieldConstraints.put(FieldConstraint.CITY, city.getName());
        }

        if (district != null) {
            fieldConstraints.put(FieldConstraint.DISTRICT_ID, String.valueOf(district.getId()));
        }

        return fieldConstraints;
    }

    protected FieldSort getChosenSort(SchoolSearchCommand schoolSearchCommand) {
        String sortBy = schoolSearchCommand.getSortBy();
        FieldSort fieldSort = null;
        if (sortBy != null) {
            fieldSort = FieldSort.valueOf(sortBy);
        }
        return fieldSort;
    }

    protected List<FieldFilter> getSchoolTypeFilters(String[] schoolTypeStrings) {
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (schoolTypeStrings != null) {
            for (String schoolTypeString : schoolTypeStrings) {
                SchoolType schoolType = SchoolType.getSchoolType(schoolTypeString);
                if (schoolType != null) {
                    FieldFilter filter = getSchoolTypeFilter(schoolType);
                    if (filter != null) {
                        filters.add(filter);
                    }
                }
            }
        }

        return filters;
    }

    protected List<FieldFilter> getGradeLevelFilters(String[] gradeLevelStrings) {
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (gradeLevelStrings != null) {
            for (String levelCode : gradeLevelStrings) {
                LevelCode.Level level = LevelCode.Level.getLevelCode(levelCode);
                if (level != null) {
                    FieldFilter filter = getGradeLevelFilter(level);
                    if (filter != null) {
                        filters.add(filter);
                    }
                }
            }
        }

        return filters;
    }

    protected List<FieldFilter> getGradeLevelFilters(LevelCode levelCode) {
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (levelCode != null) {
            for (LevelCode.Level level : levelCode.getIndividualLevelCodes()) {
                FieldFilter filter = getGradeLevelFilter(level);
                if (filter != null) {
                    filters.add(filter);
                }
            }
        }

        return filters;
    }

    protected FieldFilter getSchoolTypeFilter(SchoolType schoolType) {
        if (schoolType == null) {
            throw new IllegalArgumentException("Cannot get filter for null SchoolType");
        }
        FieldFilter filter = FieldFilter.SchoolTypeFilter.valueOf(StringUtils.upperCase(schoolType.getName()));
        return filter;
    }
    protected FieldFilter getGradeLevelFilter(LevelCode.Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Cannot get filter for null LevelCode");
        }
        FieldFilter filter = FieldFilter.GradeLevelFilter.valueOf(StringUtils.upperCase(level.getLongName()));
        return filter;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public SchoolSearchService getSchoolSearchService() {
        return _schoolSearchService;
    }

    public void setSchoolSearchService(SchoolSearchService schoolSearchService) {
        _schoolSearchService = schoolSearchService;
    }

    public CitySearchService getCitySearchService() {
        return _citySearchService;
    }

    public void setCitySearchService(CitySearchService citySearchService) {
        _citySearchService = citySearchService;
    }

    public DistrictSearchService getDistrictSearchService() {
        return _districtSearchService;
    }

    public void setDistrictSearchService(DistrictSearchService districtSearchService) {
        _districtSearchService = districtSearchService;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        boolean schoolsController = false;
        boolean cityController = false;
        boolean districtController = false;
        // level code is optional

        //city browse
        if (fields.hasState() && fields.hasCityName() && fields.hasSchoolsLabel()
                && !fields.hasSchoolName()) {
            schoolsController = true;
        }

        //district browse
        if (fields.hasState() && fields.hasCityName() && fields.hasDistrictName() && fields.hasSchoolsLabel()) {
            schoolsController =  true;
        }
        
        return schoolsController || cityController || districtController;

    }
}