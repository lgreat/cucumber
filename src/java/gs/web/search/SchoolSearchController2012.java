package gs.web.search;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.geo.City;
import gs.data.geo.ICounty;
import gs.data.geo.IGeoDao;
import gs.data.pagination.DefaultPaginationConfig;
import gs.data.pagination.PaginationConfig;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.*;
import gs.data.search.SpellChecking;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.fields.AddressFields;
import gs.data.search.fields.DocumentType;
import gs.data.search.fields.SchoolFields;
import gs.data.search.fields.SolrField;
import gs.data.search.filters.SchoolFilters;
import gs.data.search.services.CitySearchService;
import gs.data.search.services.DistrictSearchService;
import gs.data.search.services.SchoolSearchService;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.CommunityUtil;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.community.LocalBoardHelper;
import gs.web.pagination.Page;
import gs.web.pagination.RequestedPage;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;



public class SchoolSearchController2012  extends AbstractCommandController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {

    private IDistrictDao _districtDao;

    private IGeoDao _geoDao;

    private SchoolSearchService _schoolSearchService;

    private SchoolSearchService _looseSchoolSearchService;

    @Autowired
    private LocalBoardHelper _localBoardHelper;

    @Autowired
    private SearchAdHelper _searchAdHelper;

    private CitySearchService _citySearchService;

    private DistrictSearchService _districtSearchService;

    @Autowired
    private CityBrowseHelper2012 _cityBrowseHelper;
    @Autowired
    private DistrictBrowseHelper2012 _districtBrowseHelper;
    @Autowired
    private SchoolSearchHelper _queryStringSearchHelper;
    @Autowired
    private SchoolSearchCommonHelper commonSearchHelper;

    @Autowired
    private ILocalBoardDao _localBoardDao;

    private StateManager _stateManager;

    private String _noResultsViewName;
    private String _noResultsAjaxViewName;
    private String _viewName;
    private String _ajaxViewName;
    private String _mobileViewName;
    private String _mapViewName;

    private GsSolrSearcher _gsSolrSearcher;

    private ControllerFamily _controllerFamily;

    private static final Logger _log = Logger.getLogger(SchoolSearchController2012.class);
    public static final String BEAN_ID = "/search/search.page";

    public static final String MODEL_SCHOOL_TYPE = "schoolType";
    public static final String MODEL_LEVEL_CODE = "levelCode";
    public static final String MODEL_SORT = "sort";
    public static final String MODEL_SEARCH_STRING = "searchString";

    public static final String MODEL_SCHOOL_SEARCH_RESULTS = "schoolSearchResults";
    public static final String MODEL_CITY_SEARCH_RESULTS = "citySearchResults";
    public static final String MODEL_DISTRICT_SEARCH_RESULTS = "districtSearchResults";

    public static final String MODEL_START = "start";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_TOTAL_RESULTS = "totalResults";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_CURRENT_PAGE = "currentPage";
    public static final String MODEL_USE_PAGING = "usePaging";
    public static final String MODEL_PAGE = "page";

    public static final String MODEL_CITY_ID = "cityId";
    public static final String MODEL_MSL_SCHOOLS = "mslSchools";

    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_LOCAL_CITY_NAME = "localCityName";

    public static final String MODEL_CITY = "city";
    public static final String MODEL_DISTRICT = "district";

    public static final String MODEL_TITLE = "title";

    public static final String MODEL_REL_CANONICAL = "relCanonical";
    public static final String MODEL_IS_CITY_BROWSE = "isCityBrowse";
    public static final String MODEL_IS_DISTRICT_BROWSE = "isDistrictBrowse";
    public static final String MODEL_IS_SEARCH = "isSearch";
    public static final String MODEL_IS_FROM_BY_LOCATION = "isFromByLocation";

    public static final String MODEL_IS_NEARBY_SEARCH = "isNearbySearch";
    public static final String MODEL_NEARBY_SEARCH_TITLE_PREFIX = "nearbySearchTitlePrefix";
    public static final String MODEL_NEARBY_SEARCH_IS_ESTABLISHMENT= "nearbySearchIsEstablishment";
    public static final String MODEL_NEARBY_SEARCH_ZIP_CODE = "nearbySearchZipCode";
    public static final String MODEL_NORMALIZED_ADDRESS = "normalizedAddress";

    public static final String MODEL_STATE = "state";

    public static final String MODEL_DID_YOU_MEAN = "didYouMean";

    public static final int MAX_PAGE_SIZE = 100;

    protected static final String VIEW_NOT_FOUND = "/status/error404.page";

    public static final String[] OSP_ZIPCODES = new String[] {
            // DC
            "20001","20002","20003","20004","20005","20006","20007","20008","20009","20010","20011","20012","20013","20015","20016","20017","20018","20019","20020","20024","20026","20029","20030","20032","20033","20035","20036","20037","20038","20039","20040","20041","20042","20043","20044","20045","20046","20047","20049","20050","20051","20052","20053","20055","20056","20057","20058","20059","20060","20061","20062","20063","20064","20065","20066","20067","20068","20069","20070","20071","20073","20074","20075","20076","20077","20078","20080","20081","20082","20088","20090","20091","20097","20098","20099","20201","20202","20203","20204","20206","20207","20208","20210","20211","20212","20213","20214","20215","20216","20217","20218","20219","20220","20221","20222","20223","20224","20226","20227","20228","20229","20230","20231","20232","20233","20235","20238","20239","20240","20241","20242","20244","20245","20250","20251","20254","20260","20261","20262","20265","20266","20268","20270","20277","20289","20299","20301","20303","20306","20307","20310","20314","20315","20317","20318","20319","20330","20332","20336","20337","20338","20340","20350","20370","20372","20373","20374","20375","20380","20388","20389","20390","20391","20392","20393","20394","20395","20398","20401","20402","20403","20404","20405","20406","20407","20408","20409","20410","20411","20412","20413","20414","20415","20416","20418","20419","20420","20421","20422","20423","20424","20425","20426","20427","20428","20429","20431","20433","20434","20435","20436","20437","20439","20440","20441","20442","20444","20447","20451","20453","20456","20460","20463","20468","20469","20470","20472","20501","20502","20503","20504","20505","20506","20507","20508","20510","20515","20520","20521","20522","20523","20524","20525","20526","20527","20530","20531","20532","20533","20534","20535","20536","20537","20538","20539","20540","20541","20542","20543","20544","20546","20547","20548","20549","20550","20551","20552","20553","20554","20555","20557","20558","20559","20560","20565","20566","20570","20571","20572","20573","20575","20576","20577","20578","20579","20580","20581","20585","20586","20590","20591","20593","20594","20597","20599",
            // Milwaukee
            "53201", "53202", "53203", "53204", "53205", "53206", "53207", "53208", "53209", "53210", "53211", "53212", "53213", "53214", "53215", "53216", "53217", "53218", "53219", "53220", "53221", "53222", "53223", "53224", "53225", "53226", "53227", "53228", "53233", "53234", "53237", "53259", "53263", "53267", "53268", "53270", "53274", "53277", "53278", "53280", "53281", "53284", "53285", "53288", "53290", "53293", "53295",
            // Indianapolis
            "46201", "46202", "46203", "46204", "46205", "46206", "46207", "46208", "46209", "46211", "46214", "46216", "46217", "46218", "46219", "46220", "46221", "46222", "46223", "46224", "46225", "46226", "46227", "46228", "46229", "46230", "46231", "46234", "46235", "46236", "46237", "46239", "46240", "46241", "46242", "46244", "46247", "46249", "46250", "46251", "46253", "46254", "46255", "46256", "46259", "46260", "46266", "46268", "46274", "46275", "46277", "46278", "46280", "46282", "46283", "46285", "46290", "46291", "46295", "46298"
    };

    public static final PaginationConfig SCHOOL_SEARCH_PAGINATION_CONFIG;

    static {
        SCHOOL_SEARCH_PAGINATION_CONFIG = new PaginationConfig(
                DefaultPaginationConfig.DEFAULT_PAGE_SIZE_PARAM,
                DefaultPaginationConfig.DEFAULT_PAGE_NUMBER_PARAM,
                DefaultPaginationConfig.DEFAULT_OFFSET_PARAM,
                SchoolSearchCommand.DEFAULT_PAGE_SIZE,
                MAX_PAGE_SIZE,
                DefaultPaginationConfig.ZERO_BASED_OFFSET,
                DefaultPaginationConfig.ZERO_BASED_PAGES
        );
    }

    /*@Override
    *//*
     * TODO: this method needs to be refactored. first step: switch it to use  GsSolrQuery instead of SchoolSearchServiceSolrImpl
     *//*
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        String searchString = schoolSearchCommand.getSearchString();

        boolean foundDidYouMeanSuggestions = false;

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        model.put("schoolSearchCommand", schoolSearchCommand);
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        if (user != null) {
            Set<FavoriteSchool> mslSchools = user.getFavoriteSchools();
            model.put(MODEL_MSL_SCHOOLS, mslSchools);
        }

        if (schoolSearchCommand.isNearbySearchByLocation()) {
            // check for exact county match
            ICounty county = getExactCountyMatch(schoolSearchCommand.getSearchString());
            if (county != null) {
                schoolSearchCommand.setLat((double)county.getLat());
                schoolSearchCommand.setLon((double)county.getLon());
                schoolSearchCommand.setNormalizedAddress(county.getName() + " County, " + county.getState());
            }
        }

        SchoolSearchCommandWithFields commandAndFields = new SchoolSearchCommandWithFields(schoolSearchCommand, fields);
        String[] schoolSearchTypes = commandAndFields.getSchoolTypes();
        commandAndFields.setDistrictDao(getDistrictDao());
        commandAndFields.setGeoDao(getGeoDao());
        State state = commandAndFields.getState();
        boolean isCityBrowse = commandAndFields.isCityBrowse();
        boolean isDistrictBrowse = commandAndFields.isDistrictBrowse();
        boolean isSearch = !isCityBrowse && !isDistrictBrowse;
        boolean isFromByLocation = schoolSearchCommand.isNearbySearchByLocation();

        // TODO: make these spring-managed beans
        CityBrowseHelper cityBrowseHelper = new CityBrowseHelper(commandAndFields);
        DistrictBrowseHelper districtBrowseHelper = new DistrictBrowseHelper(commandAndFields);

        Map nearbySearchInfo = null;
        if (schoolSearchCommand.isNearbySearch()) {
            nearbySearchInfo = (Map)request.getAttribute("nearbySearchInfo");
            if (nearbySearchInfo != null && nearbySearchInfo.get("state") != null && nearbySearchInfo.get("state") instanceof State) {
                state = (State)nearbySearchInfo.get("state");
                if (state != null) {
                    sessionContext.getSessionContextUtil().updateState(sessionContext, request, response, state);
                }
            }
        }

        City city = null;
        District district = null;

        if (isCityBrowse || isDistrictBrowse) {
            city = commandAndFields.getCityFromUrl();
            model.put(MODEL_CITY, city);
            if (city == null) {
                return redirectTo404(response);
            }
        }
        if (isDistrictBrowse) {
            district = commandAndFields.getDistrict();
            model.put(MODEL_DISTRICT, district);
            if (district == null) {
                return redirectTo404(response);
            }
        }
        model.put(MODEL_IS_CITY_BROWSE, isCityBrowse);
        model.put(MODEL_IS_DISTRICT_BROWSE, isDistrictBrowse);
        model.put(MODEL_IS_SEARCH, isSearch);
        model.put(MODEL_IS_NEARBY_SEARCH, schoolSearchCommand.isNearbySearch());
        model.put(MODEL_NORMALIZED_ADDRESS, schoolSearchCommand.getNormalizedAddress());
        model.put(MODEL_IS_FROM_BY_LOCATION, isFromByLocation);

        if (schoolSearchCommand.isNearbySearch()) {
            String nearbySearchTitlePrefix = "Schools";
            if (schoolSearchCommand.hasGradeLevels()) {
                String[] gradeLevels = schoolSearchCommand.getGradeLevels();
                if (gradeLevels.length == 1) {
                    LevelCode levelCode = LevelCode.createLevelCode(gradeLevels[0]);
                    if (levelCode != null && !levelCode.hasNoLevelCodes()) {
                        if (LevelCode.Level.PRESCHOOL_LEVEL.equals(levelCode.getLowestLevel())) {
                            nearbySearchTitlePrefix = levelCode.getLowestLevel().getLongName() + "s";
                        } else {
                            nearbySearchTitlePrefix = levelCode.getLowestLevel().getLongName() + " schools";
                        }
                    }
                }
            }
            model.put(MODEL_NEARBY_SEARCH_TITLE_PREFIX, nearbySearchTitlePrefix);
            if (StringUtils.contains(request.getParameter("locationType"), "establishment")) {
                model.put(MODEL_NEARBY_SEARCH_IS_ESTABLISHMENT, true);
            }
        }

        //if user did not enter search term (and this is not a nearby search), redirect to state browse
        if (!commandAndFields.isNearbySearch() && commandAndFields.isSearch() && StringUtils.isBlank(commandAndFields.getSearchString())) {
            return stateBrowseRedirect(request, SessionContextUtil.getSessionContext(request));
        }

        // if district browse *and* lc parameter was specified, 301-redirect to use directory-structure schools label instead of lc parameter
        String lc = request.getParameter("lc");
        if (commandAndFields.isDistrictBrowse() && StringUtils.isNotBlank(lc) && !schoolSearchCommand.isAjaxRequest()) {
            LevelCode levelCode = LevelCode.createLevelCode(lc);
            UrlBuilder urlBuilder = new UrlBuilder(district, levelCode, UrlBuilder.SCHOOLS_IN_DISTRICT);
            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
        }

        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(state, city, district);

        List<FilterGroup> filterGroups = createFilterGroups(commandAndFields);

        boolean sortChanged = "true".equals(request.getParameter("sortChanged"));

        FieldSort sort = schoolSearchCommand.getSortBy() == null ? ((isCityBrowse || isDistrictBrowse) && !sortChanged ? FieldSort.GS_RATING_DESCENDING : null) : FieldSort.valueOf(schoolSearchCommand.getSortBy());
        if (sort != null) {
            schoolSearchCommand.setSortBy(sort.name());
        } else {
            schoolSearchCommand.setSortBy(null);
        }
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());

        RequestedPage requestedPage = schoolSearchCommand.getRequestedPage();

        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage;

        // Common: Perform a search. Search might find spelling suggestions and then run another search to see if the
        // spelling suggestion actually yieleded results. If so, record the "didYouMean" suggestion into the model
        searchResultsPage = searchForSchools(schoolSearchCommand, commandAndFields.getState(), fieldConstraints, filterGroups, sort);
        model.put(MODEL_DID_YOU_MEAN, searchResultsPage.getDidYouMeanQueryString());

        SearchResultsPage<SolrSchoolSearchResult> searchResulsWithFacets = searchForSchools(schoolSearchCommand, commandAndFields.getState(), fieldConstraints, filterGroups, sort);

        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

        //get nearby cities and districts and put into model.
        // TODO: make NearbyCitiesSearch and NearbyDistrictsSearch spring managed beans so dependencies are injected
        NearbyCitiesSearch nearbyCitiesFacade = new NearbyCitiesSearch(commandAndFields);
        nearbyCitiesFacade.setCitySearchService(getCitySearchService());
        NearbyDistrictsSearch nearbyDistrictsFacade = new NearbyDistrictsSearch(commandAndFields);
        nearbyDistrictsFacade.setDistrictSearchService(getDistrictSearchService());
        if (isCityBrowse || isDistrictBrowse) {
            citySearchResults = nearbyCitiesFacade.getNearbyCities();
            //districtSearchResults = nearbyDistrictsFacade.getNearbyDistricts(); commented out until we figure out why district lat/lons are inaccurate
        } else if (isFromByLocation) {
            citySearchResults = nearbyCitiesFacade.getNearbyCitiesByLatLon();
        } else if (searchString != null) {
            citySearchResults = nearbyCitiesFacade.searchForCities();
            districtSearchResults = nearbyDistrictsFacade.searchForDistricts();
        }
        model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        model.put(MODEL_DISTRICT_SEARCH_RESULTS, districtSearchResults);

        PageHelper.setHasSearchedCookie(request, response);

        addPagingDataToModel(requestedPage.getValidatedOffset(SCHOOL_SEARCH_PAGINATION_CONFIG, searchResultsPage.getTotalResults()), requestedPage.pageSize, requestedPage.pageNumber, searchResultsPage.getTotalResults(), model);
        addGamAttributes(request, response, pageHelper, fieldConstraints, filterGroups, searchString, searchResultsPage.getSearchResults(), city, district);


        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (state != null) {
            if (StringUtils.isNotBlank(searchString)) {
                relCanonicalUrl = getRelCanonicalForSearch(request, searchString, state, citySearchResults);
            } else {
                if (district != null) {
                    relCanonicalUrl = districtBrowseHelper.getRelCanonical(request);
                } else if (city != null) {
                    relCanonicalUrl = cityBrowseHelper.getRelCanonical(request);
               }
            }

            if (relCanonicalUrl != null) {
                model.put(MODEL_REL_CANONICAL, relCanonicalUrl);
            }
        }

        if (commandAndFields.isCityBrowse()) {
            model.putAll(cityBrowseHelper.getMetaData());
        } else if (commandAndFields.isDistrictBrowse()) {
            model.putAll(districtBrowseHelper.getMetaData());
        } else if (schoolSearchCommand.isNearbySearch()) {
            if (nearbySearchInfo != null) {
                // nearby zip code search
                model.putAll(new NearbyMetaDataHelper().getMetaData(nearbySearchInfo));
            } else {
                // Find a School by location search
                model.putAll(new NearbyMetaDataHelper().getMetaData(commandAndFields));
            }
        } else {
            model.putAll(new MetaDataHelper().getMetaData(commandAndFields));
        }

        model.put(MODEL_STATE, state);

        if (schoolSearchCommand.isNearbySearch() && nearbySearchInfo != null) {
            model.put(MODEL_NEARBY_SEARCH_ZIP_CODE, nearbySearchInfo.get("zipCode"));
        }

        String omniturePageName;
        String omnitureHierarchy;
        if (isCityBrowse) {
            omniturePageName = cityBrowseHelper.getOmniturePageName(request, requestedPage.pageNumber);
            omnitureHierarchy = cityBrowseHelper.getOmnitureHierarchy(requestedPage.pageNumber, searchResultsPage.getTotalResults());
        } else if (isDistrictBrowse) {
            omniturePageName = districtBrowseHelper.getOmniturePageName(request, requestedPage.pageNumber);
            omnitureHierarchy = districtBrowseHelper.getOmnitureHierarchy(requestedPage.pageNumber, searchResultsPage.getTotalResults());
        } else {
            omniturePageName = getOmniturePageName(request, requestedPage.pageNumber, searchResultsPage.getTotalResults(), foundDidYouMeanSuggestions);
            omnitureHierarchy = getOmnitureHierarchy(requestedPage.pageNumber, searchResultsPage.getTotalResults(), citySearchResults, districtSearchResults);
        }
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);

        String omnitureQuery = isSearch? getOmnitureQuery(searchString) : null;
        model.put(MODEL_OMNITURE_QUERY, omnitureQuery);
        model.put(MODEL_OMNITURE_SCHOOL_TYPE, getOmnitureSchoolType(schoolSearchTypes));
        model.put(MODEL_OMNITURE_SCHOOL_LEVEL, getOmnitureSchoolLevel(commandAndFields.getLevelCode()));
        model.put(MODEL_OMNITURE_SORT_SELECTION, getOmnitureSortSelection(sortChanged ? sort : null));
        model.put(MODEL_OMNITURE_RESULTS_PER_PAGE, getOmnitureResultsPerPage(schoolSearchCommand.getPageSize(), searchResultsPage.getTotalResults()));
        model.put(MODEL_OMNITURE_ADDRESS_SEARCH, false);
        model.put(MODEL_OMNITURE_NAME_SEARCH, false);

        handleOspFilters(schoolSearchCommand, model);


    }*/


    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("schoolSearchCommand", schoolSearchCommand);
        model.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        if (user != null) {
            Set<FavoriteSchool> mslSchools = user.getFavoriteSchools();
            model.put(MODEL_MSL_SCHOOLS, mslSchools);
        }

        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        if (schoolSearchCommand.isNearbySearchByLocation()) {
            // check for exact county match
            ICounty county = getExactCountyMatch(schoolSearchCommand.getSearchString());
            if (county != null) {
                schoolSearchCommand.setLat((double)county.getLat());
                schoolSearchCommand.setLon((double)county.getLon());
                schoolSearchCommand.setNormalizedAddress(county.getName() + " County, " + county.getState());
            }
        }

        // Get nearbySearchInfo from the request and update session context's state attribute if needed
        Map nearbySearchInfo = updateSessionContextStateFromNearbySearchInfo(request, response, schoolSearchCommand);

        // Store the SchoolSearchCommand, DirectoryStructureURLFields, and nearbySearchInfo in a wrapper
        // and allow it to simplify retrieval of various search request attributes
        SchoolSearchCommandWithFields commandAndFields = createSchoolSearchCommandWithFields(schoolSearchCommand, fields, nearbySearchInfo);

        // validate the request data
        if (!commandAndFields.isNearbySearch() && commandAndFields.getState() == null) {
            return new ModelAndView(getNoResultsView(request, commandAndFields), model);
        }

        // Common: Add a bunch of model attributes about the request information
        addSearchRequestInfoToModel(request, model, commandAndFields);

        handleOspFilters(schoolSearchCommand, model);

        _localBoardHelper.putLocalBoardInfoIntoModel(model, commandAndFields);

        if ((commandAndFields.isCityBrowse() || commandAndFields.isDistrictBrowse()) && !commandAndFields.isAjaxRequest()) {
            model.put("hasMobileView", true);
        }

        ModelAndView modelAndView;
        if (commandAndFields.isCityBrowse()) {
            modelAndView = handleCityBrowse(request, response, commandAndFields, model);
        } else if (commandAndFields.isDistrictBrowse()) {
            modelAndView = handleDistrictBrowse(request, response, commandAndFields, model);
        } else {
            modelAndView = handleQueryStringAndNearbySearch(request, response, commandAndFields, model);
        }

        // hack to get school types / level code checkboxes to reflect browse prefilters
        schoolSearchCommand.setGradeLevels(commandAndFields.getGradeLevels());
        schoolSearchCommand.setSt(commandAndFields.getSchoolTypes());

        // Common: Set a cookie to record that a search has occurred;
        PageHelper.setHasSearchedCookie(request, response);

        return modelAndView;
    }

    public void handleOspFilters(SchoolSearchCommand schoolSearchCommand, Map<String,Object> model) {
        final String MODEL_SHOW_ADDITIONAL_FILTERS = "showAdditionalFilters";
        if (ArrayUtils.contains(OSP_ZIPCODES, schoolSearchCommand.getZipCode())) {
            model.put(MODEL_SHOW_ADDITIONAL_FILTERS, true);
        }
    }

    private String getNoResultsView(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields) {
        if (commandAndFields.isAjaxRequest()) {
            return getNoResultsAjaxViewName();
        } else {
            return getNoResultsViewName();
        }
    }

    protected SearchResultsPage<SolrSchoolSearchResult> searchForSchools(SchoolSearchCommandWithFields commandAndFields, State state, FieldSort sort) {
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        SchoolSearchCommand schoolSearchCommand = commandAndFields.getSchoolSearchCommand();
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<SolrSchoolSearchResult>());
        String searchString = commandAndFields.getSearchString();

        GsSolrQuery q = createGsSolrQuery();

        q.filter(DocumentType.SCHOOL).page(requestedPage.offset, requestedPage.pageSize);

        // TODO: In Mobile api, we don't filter by state if a lat/lon is specified. Should that logic apply here as well?
        q.filter(SchoolFields.SCHOOL_DATABASE_STATE, state.getAbbreviationLowerCase());

        q.filter(SchoolFields.SCHOOL_TYPE, commandAndFields.getSchoolTypes());

        q.filter(SchoolFields.GRADE_LEVEL, commandAndFields.getGradeLevels());

        // TODO: the client should make a request with minSchoolSize and maxSchoolSize, rather than pass a string as it
        // does currently. Maintaining this UNDER_XX approach for backward compatibility
        String schoolSize = commandAndFields.getSchoolSize();
        if (schoolSize != null) {
            String minSchoolSize;
            String maxSchoolSize;
            SchoolFilters.SchoolSize size = SchoolFilters.SchoolSize.valueOf(schoolSize);
            switch (size) {
                case UNDER_20:
                    minSchoolSize = "1";
                    maxSchoolSize = "19";
                    break;
                case UNDER_50:
                    minSchoolSize = "1";
                    maxSchoolSize = "49";
                    break;
                case UNDER_200:
                    minSchoolSize = "1";
                    maxSchoolSize = "199";
                    break;
                case UNDER_500:
                    minSchoolSize = "1";
                    maxSchoolSize = "499";
                    break;
                case UNDER_1000:
                    minSchoolSize = "1";
                    maxSchoolSize = "999";
                    break;
                case OVER_1000:
                    minSchoolSize = "1000";
                    maxSchoolSize = "*";
                    break;
                default:
                    minSchoolSize = "1";
                    maxSchoolSize = "9999";
            }
            q.filter(SchoolFields.SCHOOL_ENROLLMENT, minSchoolSize, maxSchoolSize);
        }

        // handle logic that used to be taken care of with old "FieldConstraints"
        // Filter on school's district ID or city name
        District district = commandAndFields.getDistrict();
        City city = commandAndFields.getCity();
        if (district != null) {
            q.filter(SchoolFields.SCHOOL_DISTRICT_ID, String.valueOf(district.getId()));
        } else if (city != null) {
            q.filter(AddressFields.CITY_UNTOKENIZED, city.getName().toLowerCase());
        }

        if (schoolSearchCommand.getMinCommunityRating() != null) {
            q.filter(SchoolFields.COMMUNITY_RATING, String.valueOf(schoolSearchCommand.getMinCommunityRating()), "5");
        }

        if (schoolSearchCommand.getMinGreatSchoolsRating() != null) {
            q.filter(SchoolFields.OVERALL_GS_RATING, String.valueOf(schoolSearchCommand.getMinGreatSchoolsRating()), "10");
        }

        if (schoolSearchCommand.getBeforeAfterCare() != null) {
            for (String value : schoolSearchCommand.getBeforeAfterCare()) {
                q.filter(SchoolFields.BEFORE_AFTER_CARE, value);
            }
        }

        if (schoolSearchCommand.getTransportation() != null) {
            if (schoolSearchCommand.getTransportation()) {
                q.filterNotNull(SchoolFields.TRANSPORTATION);
                q.filterNot(SchoolFields.TRANSPORTATION, "none");
            }
        }

        if (schoolSearchCommand.getEll() != null) {
            if (schoolSearchCommand.getEll()) {
                q.filterNotNull(SchoolFields.ELL_LEVEL);
                q.filterNot(SchoolFields.ELL_LEVEL, "none");
            }
        }

        if (schoolSearchCommand.getStudentsVouchers() != null) {
            if (schoolSearchCommand.getStudentsVouchers()) {
                q.filter(SchoolFields.STUDENTS_VOUCHERS, "yes");
            } else {
                q.filter(SchoolFields.STUDENTS_VOUCHERS, "no");
            }
        }

        if (schoolSearchCommand.getSpecialEdPrograms() != null) {
            q.filter(SchoolFields.SPECIAL_ED_PROGRAMS, schoolSearchCommand.getSpecialEdPrograms());
        }

        if (schoolSearchCommand.getSchoolFocus() != null) {
            q.filterAnyField(new SolrField[]{
                    SchoolFields.ACADEMIC_FOCUS,
                    SchoolFields.INSTRUCTIONAL_MODEL
            }, schoolSearchCommand.getSchoolFocus());
        }

        if (schoolSearchCommand.getSports() != null) {
            q.filterAnyField(new SolrField[]{
                    SchoolFields.BOYS_SPORTS,
                    SchoolFields.GIRLS_SPORTS
            }, schoolSearchCommand.getSports());
        }

        if (schoolSearchCommand.getArtsAndMusic() != null) {
            q.filterAnyField(new SolrField[]{
                    SchoolFields.ARTS_VISUAL,
                    SchoolFields.ARTS_PERFORMING_WRITTEN,
                    SchoolFields.ARTS_MUSIC,
                    SchoolFields.ARTS_MEDIA
            }, schoolSearchCommand.getArtsAndMusic());
        }

        if (schoolSearchCommand.getStudentClubs() != null) {
            q.filter(SchoolFields.STUDENT_CLUBS, schoolSearchCommand.getStudentClubs());
        }

        if(schoolSearchCommand.getStaffResources() != null) {
            q.filterAnyField(new SolrField[]{
                    SchoolFields.STAFF_RESOURCES,
                    SchoolFields.FACILITIES
            }, schoolSearchCommand.getStaffResources());
        }

        String[] ratingCategories = schoolSearchCommand.getRatingCategories();
        if (ratingCategories != null) {
            List<String> gsRatings = new ArrayList<String>();
            if (ArrayUtils.contains(ratingCategories, "low")) {
                gsRatings.add("1");
                gsRatings.add("2");
                gsRatings.add("3");
            }
            if (ArrayUtils.contains(ratingCategories, "average")) {
                gsRatings.add("4");
                gsRatings.add("5");
                gsRatings.add("6");
            }
            if (ArrayUtils.contains(ratingCategories, "high")) {
                gsRatings.add("7");
                gsRatings.add("8");
                gsRatings.add("9");
                gsRatings.add("10");
            }
            q.filter(SchoolFields.OVERALL_GS_RATING, gsRatings);
        }

        if (schoolSearchCommand.getReligious() != null) {
            if (schoolSearchCommand.getReligious().equals(Boolean.TRUE.toString())) {
                q.filter(SchoolFields.SCHOOL_SUBTYPE, "religious");
            } else if (schoolSearchCommand.getReligious().equals(Boolean.FALSE.toString())) {
                q.filterNot(SchoolFields.SCHOOL_SUBTYPE, "religious");
            }
        }

        // filter by location
        if (schoolSearchCommand.hasLatLon()) {
            q.restrictToRadius(schoolSearchCommand.getLat().floatValue(), schoolSearchCommand.getLon().floatValue(), schoolSearchCommand.getDistanceAsFloat());
        }

        if (schoolSearchCommand.getSearchType() != SchoolSearchType.LOOSE) {
            // require all words in the query to be present unless it's configured as an optional word
            q.requireNonOptionalWords();
        }

        // set the solr query's q parameter (querystring) to be the user search string
        if (!schoolSearchCommand.hasLatLon()) {
            q.query(searchString);
        }

        // apply sorting
        if (sort != null) {
            q.sort(sort);
        }

        try {
            searchResultsPage = getGsSolrSearcher().search(q, SolrSchoolSearchResult.class, true);

            if (searchResultsPage.isDidYouMeanResults()) {
                // adapting old existing logic to new code: If the search results we got back are the result
                // of an automatic second search using a Solr spelling suggestion, then we want it to appear
                // that we never received any results so the site will display the No Results page with the
                // "did you mean" suggestion
                searchResultsPage.setTotalResults(0);
                searchResultsPage.setSearchResults(new ArrayList<SolrSchoolSearchResult>());
            }

        } catch (SearchException e) {
            _log.error("Problem occured while getting schools or reviews: ", e);
        }

        return searchResultsPage;
    }

    public ModelAndView handleCityBrowse(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        // City Browse Specific: check valid city
        ModelAndView redirect = checkCityBrowseForRedirectConditions(response, commandAndFields);
        if (redirect != null) {
            return redirect;
        }

        // TODO: can we clean up code by always putting city and model into model, or will this break the view?
        // City Browse And District Browse Specific:  Put City into model
        model.put(MODEL_CITY, commandAndFields.getCity());

        // Common: perform school, city, and district searches and add relevant data to the model
        SchoolCityDistrictSearchSummary summary = handleSchoolCityDistrictSearch(commandAndFields, model);


        // City Browse Specific: Ad GAM attributes for city browse
        _cityBrowseHelper.addGamAttributes(request, commandAndFields, summary.searchResultsPage.getSearchResults());


        // City Browse Specific:  Put rel canonical value into the model
        String relCanonical = _cityBrowseHelper.getRelCanonical(commandAndFields, request);
        if (relCanonical != null) {
            model.put(MODEL_REL_CANONICAL, relCanonical);
        }


        // City Browse Specific:  Use a city browse helper to calculate title and description and put them into model
        model.putAll(
                _cityBrowseHelper.getMetaData(commandAndFields)
        );


        // City Browse Specific: Use a city browse helper to calculate omniture page name and hierarchy and put them into model
        model.putAll(
                _cityBrowseHelper.getOmnitureHierarchyAndPageName(request, commandAndFields, summary.searchResultsPage.getTotalResults())
        );


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        model.putAll(
                commonSearchHelper.getCommonOmnitureAttributes(request, commandAndFields, summary.searchResultsPage)
        );

        // GS-6971 - city id cookie
        PageHelper.setCityIdCookie(request, response, commandAndFields.getCity());

        // Common: Calculate the view name
        String viewName = determineViewName(commandAndFields.getSchoolSearchCommand(), summary.searchResultsPage);

        return new ModelAndView(viewName, model);
    }

    public ModelAndView handleDistrictBrowse(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        SchoolSearchCommand schoolSearchCommand = commandAndFields.getSchoolSearchCommand();

        // District Browse Specific: check valid city, valid district, wrong param combination, etc
        ModelAndView redirect = _districtBrowseHelper.checkDistrictBrowseForRedirectConditions(request, response, commandAndFields);
        if (redirect != null) {
            return redirect;
        }

        // TODO: can we clean up code by always putting city and model into model, or will this break the view?
        // City Browse And District Browse Specific:  Put City into model
        model.put(MODEL_CITY, commandAndFields.getCity());


        // District Browse Specific:  Put District into model
        model.put(MODEL_DISTRICT, commandAndFields.getDistrict());


        // Common: perform school, city, and district searches and add relevant data to the model
        SchoolCityDistrictSearchSummary summary = handleSchoolCityDistrictSearch(commandAndFields, model);


        // District Browse Specific: put GAM attributes for district browse into model
        _districtBrowseHelper.addGamAttributes(request, commandAndFields, summary.searchResultsPage.getSearchResults());


        // District Browse Specific:  Put rel canonical value into the model
        _districtBrowseHelper.putRelCanonicalIntoModel(request, model, commandAndFields);
        // City Browse Specific:  Put rel canonical value into the model
        String relCanonical = _districtBrowseHelper.getRelCanonical(commandAndFields, request);
        if (relCanonical != null) {
            model.put(MODEL_REL_CANONICAL, relCanonical);
        }


        // District Browse Specific:  Use a district browse helper to calculate title and description and put them into model
        model.putAll(
                _districtBrowseHelper.getMetaData(commandAndFields)
        );


        // District Browse Specific: Use a district browse helper to calculate omniture page name and hierarchy and put them into model
        model.putAll(
                _districtBrowseHelper.getOmnitureHierarchyAndPageName(request, commandAndFields, summary.searchResultsPage.getTotalResults())
        );


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        model.putAll(
                commonSearchHelper.getCommonOmnitureAttributes(request, commandAndFields, summary.searchResultsPage)
        );


        // Common: Calculate the view name
        String viewName = determineViewName(commandAndFields.getSchoolSearchCommand(), summary.searchResultsPage);

        return new ModelAndView(viewName, model);
    }

    private ModelAndView handleQueryStringAndNearbySearch(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {

        // QueryString Search Specific (not city browse and not district browse and no lat/lon)
        // if user did not enter search term (and this is not a nearby search), redirect to state browse
        ModelAndView redirect = _queryStringSearchHelper.checkForQueryStringSearchRedirectConditions(request, commandAndFields);
        if (redirect != null) {
            return redirect;
        }


        // Common: perform school, city, and district searches and add relevant data to the model
        SchoolCityDistrictSearchSummary summary = handleSchoolCityDistrictSearch(commandAndFields, model);


        // Nearby Search and QueryString Search Specific: put GAM attributes for these flows into model
        _queryStringSearchHelper.addGamAttributes(request, commandAndFields, summary.searchResultsPage.getSearchResults());


        // QueryString Search Specific:  Put rel canonical value into the model
        String relCanonical = _queryStringSearchHelper.getRelCanonical(request, commandAndFields.getSearchString(), commandAndFields.getState(), summary.citySearchResults);
        if (relCanonical != null) {
            model.put(MODEL_REL_CANONICAL, relCanonical);
        }

        // Nearby Search and QueryString Search Specific
        // TODO: Split Nearby / QueryString Search?
        // TODO: Review this
        Map nearbySearchInfo = (Map) request.getAttribute("nearbySearchInfo");
        putMetaDataInModel(model, commandAndFields, nearbySearchInfo);

        // QueryString Search Specific: Use a school search helper to calculate omniture page name and hierarchy and put them into model
        model.putAll(_queryStringSearchHelper.getOmnitureHierarchyAndPageName(request, commandAndFields, summary));


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        model.putAll(
                commonSearchHelper.getCommonOmnitureAttributes(request, commandAndFields, summary.searchResultsPage)
        );


        // Common: Calculate the view name
        String viewName = determineViewName(commandAndFields.getSchoolSearchCommand(), summary.searchResultsPage);


        return new ModelAndView(viewName, model);
    }



    protected void putRelCanonicalIntoModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields, CityBrowseHelper cityBrowseHelper, DistrictBrowseHelper districtBrowseHelper, List<ICitySearchResult> citySearchResults) {

        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (commandAndFields.getState() != null) {
            if (StringUtils.isNotBlank(commandAndFields.getSearchString())) {
                relCanonicalUrl = _queryStringSearchHelper.getRelCanonical(request, commandAndFields.getSearchString(), commandAndFields.getState(), citySearchResults);
            } else {
                if (commandAndFields.getDistrict() != null) {
                    relCanonicalUrl = districtBrowseHelper.getRelCanonical(request);
                } else if (commandAndFields.getCity() != null) {
                    relCanonicalUrl = cityBrowseHelper.getRelCanonical(request);
                }
            }

            if (relCanonicalUrl != null) {
                model.put(MODEL_REL_CANONICAL, relCanonicalUrl);
            }
        }
    }


    protected void putMetaDataInModel(Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields, Map nearbySearchInfo) {
        if (commandAndFields.isNearbySearch()) {
            if (nearbySearchInfo != null) {
                // nearby zip code search
                model.putAll(new NearbyMetaDataMobileHelper().getMetaData(nearbySearchInfo));
            } else {
                // Find a School by location search
                model.putAll(new NearbyMetaDataMobileHelper().getMetaData(commandAndFields));
            }
        } else {
            model.putAll(new MetaDataMobileHelper().getMetaData(commandAndFields));
        }
    }





    protected SchoolCityDistrictSearchSummary handleSchoolCityDistrictSearch(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        SchoolSearchCommand schoolSearchCommand = commandAndFields.getSchoolSearchCommand();

        // Common: Use request info to determine the field to sort by, put it into the model, and return it
        FieldSort sort = commandAndFields.getFieldSort();
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());


        // Common: Perform a search. Search might find spelling suggestions and then run another search to see if the
        // spelling suggestion actually yieleded results. If so, record the "didYouMean" suggestion into the model
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = searchForSchools(commandAndFields, commandAndFields.getState(), sort);
        if (searchResultsPage.getSpellCheckResponse() != null) {
            model.put(MODEL_DID_YOU_MEAN, SpellChecking.getSearchSuggestion(commandAndFields.getSearchString(), searchResultsPage.getSpellCheckResponse()));
        }


        // Common: Search for nearby cities and place them into the model
        List<ICitySearchResult> citySearchResults = putNearbyCitiesInModel(commandAndFields, model);


        // Common: Now search for nearby districts and place those into the model
        List<IDistrictSearchResult> districtSearchResults = putNearbyDistrictsInModel(commandAndFields, model);


        // Common: Put pagination-related numbers into the model
        addPagingDataToModel(commandAndFields.getRequestedPage().getValidatedOffset(SCHOOL_SEARCH_PAGINATION_CONFIG, searchResultsPage.getTotalResults()), commandAndFields.getRequestedPage().pageSize, commandAndFields.getRequestedPage().pageNumber, searchResultsPage.getTotalResults(), model);


        // Common: Put info about the search resultset / result counts into the model
        putSearchResultInfoIntoModel(model, searchResultsPage);

        return new SchoolCityDistrictSearchSummary(sort, searchResultsPage, citySearchResults, districtSearchResults);
    }

    protected ModelAndView checkCityBrowseForRedirectConditions(HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields) {
        // City Browse and District Browse Specific:  We're in a city browse or district browse page, so get the city
        // from the URL. If it's not a real city then 404. Otherwise add city to the model
        if (commandAndFields.getCityFromUrl() == null) {
            return redirectTo404(response);
        }

        return null;
    }

    private SchoolSearchCommandWithFields createSchoolSearchCommandWithFields(SchoolSearchCommand schoolSearchCommand, DirectoryStructureUrlFields fields, Map nearbySearchInfo) {
        SchoolSearchCommandWithFields commandAndFields = new SchoolSearchCommandWithFields(schoolSearchCommand, fields, nearbySearchInfo);
        commandAndFields.setDistrictDao(getDistrictDao());
        commandAndFields.setGeoDao(getGeoDao());
        return commandAndFields;
    }

    protected void addSearchRequestInfoToModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields) {
        // TODO: make this method not require HttpServletRequest
        model.put(MODEL_STATE, commandAndFields.getState());

        model.put(MODEL_IS_CITY_BROWSE, commandAndFields.isCityBrowse());
        model.put(MODEL_IS_DISTRICT_BROWSE, commandAndFields.isDistrictBrowse());

        model.put(MODEL_IS_SEARCH, commandAndFields.isSearch());
        model.put(MODEL_SEARCH_STRING, commandAndFields.getSearchString());

        model.put(MODEL_IS_NEARBY_SEARCH, commandAndFields.isNearbySearch());
        model.put(MODEL_NORMALIZED_ADDRESS, commandAndFields.getNormalizedAddress());
        model.put(MODEL_IS_FROM_BY_LOCATION, commandAndFields.isNearbySearchByLocation());

        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(commandAndFields.getSchoolTypes()));

        if (commandAndFields.getLevelCode() != null) {
            model.put(MODEL_LEVEL_CODE, commandAndFields.getLevelCode().getCommaSeparatedString());
        }

        if (commandAndFields.isNearbySearch()) {
            String nearbySearchTitlePrefix = getNearbySearchTitlePrefix(commandAndFields.getSchoolSearchCommand());
            model.put(MODEL_NEARBY_SEARCH_TITLE_PREFIX, nearbySearchTitlePrefix);
            if (StringUtils.contains(request.getParameter("locationType"), "establishment")) {
                model.put(MODEL_NEARBY_SEARCH_IS_ESTABLISHMENT, true);
            }
        }

        if (commandAndFields.isNearbySearch() && commandAndFields.getNearbySearchInfo() != null) {
            model.put(MODEL_NEARBY_SEARCH_ZIP_CODE, commandAndFields.getNearbySearchInfo().get("zipCode"));
        }
    }

    protected String getNearbySearchTitlePrefix(SchoolSearchCommand schoolSearchCommand) {
        String nearbySearchTitlePrefix = "Schools";
        if (schoolSearchCommand.hasGradeLevels()) {
            String[] gradeLevels = schoolSearchCommand.getGradeLevels();
            if (gradeLevels.length == 1) {
                LevelCode levelCode = LevelCode.createLevelCode(gradeLevels[0]);
                if (levelCode != null && !levelCode.hasNoLevelCodes()) {
                    if (LevelCode.Level.PRESCHOOL_LEVEL.equals(levelCode.getLowestLevel())) {
                        nearbySearchTitlePrefix = levelCode.getLowestLevel().getLongName() + "s";
                    } else {
                        nearbySearchTitlePrefix = levelCode.getLowestLevel().getLongName() + " schools";
                    }
                }
            }
        }
        return nearbySearchTitlePrefix;
    }

    public List<ICitySearchResult> putNearbyCitiesInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

        NearbyCitiesSearch nearbyCitiesFacade = new NearbyCitiesSearch(commandAndFields);
        nearbyCitiesFacade.setCitySearchService(_citySearchService);
        if (commandAndFields.isCityBrowse() || commandAndFields.isDistrictBrowse()) {
            citySearchResults = _cityBrowseHelper.getNearbyCities(commandAndFields.getCityFromUrl(), commandAndFields.getLatitude(), commandAndFields.getLongitude());
            //districtSearchResults = nearbyDistrictsFacade.getNearbyDistricts(); commented out until we figure out why district lat/lons are inaccurate
        } else if (commandAndFields.isNearbySearch()) {
            citySearchResults = nearbyCitiesFacade.getNearbyCitiesByLatLon();
        } else if (commandAndFields.getSearchString() != null) {
            citySearchResults = _queryStringSearchHelper.searchForCities(commandAndFields);
        }
        model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        return citySearchResults;
    }

    public List<IDistrictSearchResult> putNearbyDistrictsInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

        NearbyDistrictsSearch nearbyDistrictsFacade = new NearbyDistrictsSearch(commandAndFields);
        nearbyDistrictsFacade.setDistrictSearchService(_districtSearchService);
        if (commandAndFields.isCityBrowse() || commandAndFields.isDistrictBrowse()) {
            //districtSearchResults = nearbyDistrictsFacade.getNearbyDistricts(); commented out until we figure out why district lat/lons are inaccurate
        } else if (commandAndFields.isNearbySearchByLocation()) {
            // do nothing
        } else if (commandAndFields.getSearchString() != null) {
            districtSearchResults = nearbyDistrictsFacade.searchForDistricts();
        }
        model.put(MODEL_DISTRICT_SEARCH_RESULTS, districtSearchResults);
        return districtSearchResults;
    }

    protected void putSearchResultInfoIntoModel(Map<String, Object> model, SearchResultsPage<SolrSchoolSearchResult> searchResultsPage) {
        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());
        model.put(MODEL_TOTAL_RESULTS, searchResultsPage.getTotalResults());
    }

    /**
     * Update State in session context if there's one in nearbySearchInfo stored in HttpServletRequest
     * @param request
     * @param response
     * @param schoolSearchCommand
     * @return
     */
    protected Map updateSessionContextStateFromNearbySearchInfo(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommand schoolSearchCommand) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        Map nearbySearchInfo = null;
        if (schoolSearchCommand.isNearbySearch()) {
            nearbySearchInfo = (Map)request.getAttribute("nearbySearchInfo");
            if (nearbySearchInfo != null && nearbySearchInfo.get("state") != null && nearbySearchInfo.get("state") instanceof State) {
                State state = (State)nearbySearchInfo.get("state");
                if (state != null) {
                    sessionContext.getSessionContextUtil().updateState(sessionContext, request, response, state);
                }
            }
        }
        return nearbySearchInfo;
    }

    public GsSolrQuery createGsSolrQuery() {
        return new GsSolrQuery(QueryType.SCHOOL_SEARCH);
    }

    //-------------------------------------------------------------------------
    // pagination
    //-------------------------------------------------------------------------

    /**
     * Calculates paging info and adds it to model. Paging is zero-based. First search result has start=0
     *
     * @param start
     * @param pageSize
     * @param totalResults
     * @param model
     */
    protected void addPagingDataToModel(int start, Integer pageSize, int currentPage, int totalResults, Map<String,Object> model) {

        //TODO: perform validation to only allow no paging when results are a certain size
        if (pageSize > 0) {
            int numberOfPages = (int) Math.ceil(totalResults / pageSize.floatValue());
            model.put(MODEL_CURRENT_PAGE, currentPage);
            model.put(MODEL_TOTAL_PAGES, numberOfPages);
            model.put(MODEL_USE_PAGING, Boolean.valueOf(true));
            Page p = new Page(start, pageSize, totalResults);
            model.put(MODEL_PAGE, p);
        } else {
            model.put(MODEL_USE_PAGING, Boolean.valueOf(false));
        }

        model.put(MODEL_START, start < totalResults? start : 0);
        model.put(MODEL_PAGE_SIZE, pageSize);
    }

    /**
     * Look for and return an exact county name match on search queries matching the format
     * "[query] county, [state]"
     * e.g. a query string "Alameda County, CA" will search for a county with the name "Alameda" in CA.
     */
    protected ICounty getExactCountyMatch(String searchQuery) {
        if (StringUtils.isBlank(searchQuery) || !StringUtils.containsIgnoreCase(searchQuery, "county")) {
            return null;
        }

        ICounty county = null;
        try {
            String trimmedQuery = StringUtils.trim(searchQuery);

            // let's try pulling a State off the end of the string
            String potentialState = StringUtils.substringAfterLast(trimmedQuery, " ");
            if (StringUtils.length(potentialState) != 2) {
                potentialState = StringUtils.substringAfterLast(trimmedQuery, ",");
            }
            try {
                State state = State.fromString(potentialState);
                if (state != null) {
                    // that worked, so let's clean up the string and remove the word "county" from the end
                    // county search strings must end with county per GS-11974/GS-11989
                    String countyStr = StringUtils.substring(trimmedQuery, 0, trimmedQuery.length()-potentialState.length());
                    countyStr = StringUtils.trim(countyStr);
                    // remove trailing comma, if any
                    if (StringUtils.endsWith(countyStr, ",")) {
                        countyStr = StringUtils.chop(countyStr);
                        countyStr = StringUtils.trim(countyStr);
                    }
                    // remove trailing "county" since bpcounty names do not contain that
                    if (StringUtils.endsWithIgnoreCase(countyStr, "county")) {
                        countyStr = StringUtils.substring(countyStr, 0, countyStr.length() - "county".length());
                        countyStr = StringUtils.trim(countyStr);
                    }
                    // now check for a county by name in the state
                    county = _geoDao.findCountyByName(countyStr, state);
                }
            } catch (IllegalArgumentException iae) {
                // Triggers from State.fromString
                // This is not an exceptional case so I am not logging anything!
                // Since we can't determine state, we won't be able to determine an exact county match
                // We'll fall through and let null be returned
            }
        } catch (Exception e) {
            // this probably indicates a coding error or a fatal problem with the DAO.
            // Either way this should be logged so it can get attention
            _log.error(e, e);
            // However, we don't want errors here affecting search
            // Let's just give up on finding the exact county and proceed with regular search.
        }

        return county;
    }



    public ModelAndView redirectTo404(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView(VIEW_NOT_FOUND);
    }

    public String determineViewName(SchoolSearchCommand schoolSearchCommand, SearchResultsPage<SolrSchoolSearchResult> searchResultsPage) {
        String viewOverride = schoolSearchCommand.getView();
        // if "view" URL query param is set to "map", use the map viewname
        if ("map".equals(viewOverride)) {
            return getMapViewName();
        }

        else if (schoolSearchCommand.isAjaxRequest()) {
            if (searchResultsPage.getTotalResults() == 0) {
                return getNoResultsAjaxViewName();
            } else {
                return getAjaxViewName();
            }
        }

        else {
            if (searchResultsPage.getTotalResults() == 0 && !schoolSearchCommand.isNearbySearchByLocation()) {
                return getNoResultsViewName();
            } else {
                return getViewName();
            }
        }
    }

    protected class SchoolCityDistrictSearchSummary {
        public final FieldSort fieldSort;

        public final SearchResultsPage<SolrSchoolSearchResult> searchResultsPage;
        public final List<ICitySearchResult> citySearchResults;
        public final List<IDistrictSearchResult> districtSearchResults;

        public SchoolCityDistrictSearchSummary(FieldSort fieldSort, SearchResultsPage<SolrSchoolSearchResult> searchResultsPage, List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults) {
            this.fieldSort = fieldSort;
            this.citySearchResults = citySearchResults;
            this.districtSearchResults = districtSearchResults;
            this.searchResultsPage = searchResultsPage;
        }
    }


    //-------------------------------------------------------------------------
    // omniture
    //-------------------------------------------------------------------------



    protected static String getOmnitureQuery(String searchString) {
        if (StringUtils.isBlank(searchString)) {
            return "[blank]";
        } else {
            return StringEscapeUtils.escapeXml(searchString.toLowerCase());
        }
    }

    // this presumes all schoolSearchTypes passed in are valid SchoolTypes.
    protected static String getOmnitureSchoolType(String[] schoolSearchTypes) {
        // currently, there's no url that will take you to a page with all school type filters unchecked,
        // for which we should be logging "nothing checked" in Omniture;
        // that's why the code here doesn't ever return it. it can be implemented if we ever add such a url
        if (schoolSearchTypes == null || schoolSearchTypes.length == 3) {
            return null;
        } else {
            return StringUtils.join(schoolSearchTypes, ",");
        }
    }

    protected static String getOmnitureSchoolLevel(LevelCode levelCode) {
        // currently, there's no url that will take you to a page with all level code filters unchecked,
        // for which we should be logging "nothing checked" in Omniture;
        // that's why the code here doesn't ever return it. it can be implemented if we ever add such a url
        if (levelCode != null && !levelCode.equals(LevelCode.ALL_LEVELS)) {
            return levelCode.getCommaSeparatedString();
        }
        return null;
    }

    protected static String getOmnitureSortSelection(FieldSort sort) {
        if (sort == null) {
            return null;
        } else {
            if (sort.name().startsWith("SCHOOL_NAME")) {
                return "School name";
            } else if (sort.name().startsWith("GS_RATING")) {
                return "GS Rating";
            } else if (sort.name().startsWith("PARENT_RATING")) {
                return "Parent Rating";
            } else if (sort.name().startsWith("DISTANCE")) {
                return "Distance";
            } else {
                return null;
            }
        }
    }

    protected static String getOmnitureResultsPerPage(int pageSize, int totalResults) {
        String resultsPerPage = "";
        // ignore pageSize = 25 per GS-11563
        switch (pageSize) {
            case 50:
                resultsPerPage = "50";
                break;
            case 100:
                if (totalResults <= 100) {
                    resultsPerPage = "All";
                } else {
                    resultsPerPage = "100";
                }
                break;
        }
        return resultsPerPage;
    }

    //-------------------------------------------------------------------------
    // required to implement interface IDirectoryStructureUrlController
    //-------------------------------------------------------------------------

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

    public SchoolSearchService getLooseSchoolSearchService() {
        return _looseSchoolSearchService;
    }

    public void setLooseSchoolSearchService(SchoolSearchService looseSchoolSearchService) {
        _looseSchoolSearchService = looseSchoolSearchService;
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

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getNoResultsViewName() {
        return _noResultsViewName;
    }

    public void setNoResultsViewName(String noResultsViewName) {
        _noResultsViewName = noResultsViewName;
    }

    public String getNoResultsAjaxViewName() {
        return _noResultsAjaxViewName;
    }

    public void setNoResultsAjaxViewName(String noResultsAjaxViewName) {
        _noResultsAjaxViewName = noResultsAjaxViewName;
    }

    public String getAjaxViewName() {
        return _ajaxViewName;
    }

    public void setAjaxViewName(String ajaxViewName) {
        _ajaxViewName = ajaxViewName;
    }

    public String getMapViewName() {
        return _mapViewName;
    }

    public void setMapViewName(String mapViewName) {
        _mapViewName = mapViewName;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    public GsSolrSearcher getGsSolrSearcher() {
        return _gsSolrSearcher;
    }

    public void setGsSolrSearcher(GsSolrSearcher gsSolrSearcher) {
        _gsSolrSearcher = gsSolrSearcher;
    }

    public String getMobileViewName() {
        return _mobileViewName;
    }

    public void setMobileViewName(String mobileViewName) {
        _mobileViewName = mobileViewName;
    }



}
