package gs.web.search;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.geo.City;
import gs.data.geo.ICounty;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.*;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.ISchoolSearchResult;
import gs.data.search.filters.FilterFactory;
import gs.data.search.filters.FilterGroup;
import gs.data.search.filters.SchoolFilters;
import gs.data.search.services.CitySearchService;
import gs.data.search.services.DistrictSearchService;
import gs.data.search.services.SchoolSearchService;
import gs.data.seo.SeoUtil;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import gs.data.pagination.DefaultPaginationConfig;
import gs.web.mobile.IControllerWithMobileSupport;
import gs.web.pagination.Page;
import gs.data.pagination.PaginationConfig;
import gs.web.pagination.RequestedPage;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class SchoolSearchController extends AbstractCommandController implements IDirectoryStructureUrlController, IControllerWithMobileSupport {

    private IDistrictDao _districtDao;

    private IGeoDao _geoDao;

    private SchoolSearchService _schoolSearchService;

    private SchoolSearchService _looseSchoolSearchService;

    private CitySearchService _citySearchService;

    private DistrictSearchService _districtSearchService;

    private ILocalBoardDao _localBoardDao;

    private StateManager _stateManager;

    private String _mobileViewName;

    private boolean _beanSupportsMobileRequests;

    private boolean _beanSupportsDesktopRequests;

    private String _noResultsViewName;
    private String _noResultsAjaxViewName;
    private String _viewName;
    private String _ajaxViewName;

    private static final Logger _log = Logger.getLogger(SchoolSearchController.class);
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
    public static final String MODEL_META_DESCRIPTION = "metaDescription";
    public static final String MODEL_META_KEYWORDS = "metaKeywords";

    public static final String MODEL_REL_CANONICAL = "relCanonical";

    public static final String MODEL_OMNITURE_PAGE_NAME = "omniturePageName";
    public static final String MODEL_OMNITURE_HIERARCHY = "omnitureHierarchy";
    public static final String MODEL_OMNITURE_QUERY = "omnitureQuery";
    public static final String MODEL_OMNITURE_SCHOOL_TYPE = "omnitureSchoolType";
    public static final String MODEL_OMNITURE_SCHOOL_LEVEL = "omnitureSchoolLevel";
    public static final String MODEL_OMNITURE_SORT_SELECTION = "omnitureSortSelection";
    public static final String MODEL_OMNITURE_RESULTS_PER_PAGE = "omnitureResultsPerPage";
    public static final String MODEL_OMNITURE_ADDRESS_SEARCH = "omnitureAddressSearch";
    public static final String MODEL_OMNITURE_NAME_SEARCH = "omnitureNameSearch";

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
    public static final int NEARBY_CITIES_PAGE_SIZE = 33;

    protected static final String VIEW_NOT_FOUND = "/status/error404.page";

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

    @Override
    /*
     * TODO: this method needs to be refactored. first step: switch it to use  GsSolrQuery instead of SchoolSearchServiceSolrImpl
     */
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        String searchString = schoolSearchCommand.getSearchString();
        FilterFactory filterFactory = new FilterFactory();

        boolean foundDidYouMeanSuggestions = false;

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("schoolSearchCommand", schoolSearchCommand);
        List<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
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
        if (!schoolSearchCommand.isNearbySearch() && isSearch && StringUtils.isBlank(searchString)) {
            return stateBrowseRedirect(request, sessionContext);
        }

        // if district browse *and* lc parameter was specified, 301-redirect to use directory-structure schools label instead of lc parameter
        String lc = request.getParameter("lc");
        if (commandAndFields.isDistrictBrowse() && StringUtils.isNotBlank(lc) && !schoolSearchCommand.isAjaxRequest()) {
            LevelCode levelCode = LevelCode.createLevelCode(lc);
            UrlBuilder urlBuilder = new UrlBuilder(district, levelCode, UrlBuilder.SCHOOLS_IN_DISTRICT);
            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
        }

        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(state, city, district);

        //If we have school types, create a filter group for it
        if (commandAndFields.hasSchoolTypes()) {
            FilterGroup filterGroup = filterFactory.createFilterGroup(SchoolFilters.SchoolTypeFilter.class,commandAndFields.getSchoolTypes());
            filterGroups.add(filterGroup);
        }

        //If we have level code(s), create a filter group for it
        LevelCode levelCode = commandAndFields.getLevelCode();
        if (levelCode != null) {
            FilterGroup filterGroup = new FilterGroup();
            SchoolFilters[] filters = getGradeLevelFilters(levelCode).toArray(new SchoolFilters[0]);
            if (filters != null && filters.length > 0) {
                filterGroup.setFieldFilters(filters);
                filterGroups.add(filterGroup);
            }
        }

        //Create a filter group for the Affiliation filters (currently Religious or Nonsectarian)
        if (commandAndFields.hasAffiliations()) {
            FilterGroup affiliationGroup = filterFactory.createFilterGroup(SchoolFilters.AffiliationFilter.class, commandAndFields.getAffiliations());
            filterGroups.add(affiliationGroup);
        }

        if (!StringUtils.isEmpty(commandAndFields.getStudentTeacherRatio())) {
            FilterGroup studentTeacherRatioGroup = filterFactory.createFilterGroup(SchoolFilters.StudentTeacherRatio.class, new String[] {commandAndFields.getStudentTeacherRatio()});
            filterGroups.add(studentTeacherRatioGroup);
        }

        if (!StringUtils.isEmpty(commandAndFields.getSchoolSize())) {
            FilterGroup schoolSizeGroup = filterFactory.createFilterGroup(SchoolFilters.SchoolSize.class, new String[] {commandAndFields.getSchoolSize()});
            filterGroups.add(schoolSizeGroup);
        }

        boolean sortChanged = "true".equals(request.getParameter("sortChanged"));

        FieldSort sort = schoolSearchCommand.getSortBy() == null ? ((isCityBrowse || isDistrictBrowse) && !sortChanged ? FieldSort.GS_RATING_DESCENDING : null) : FieldSort.valueOf(schoolSearchCommand.getSortBy());
        if (sort != null) {
            schoolSearchCommand.setSortBy(sort.name());
        } else {
            schoolSearchCommand.setSortBy(null);
        }
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());

        RequestedPage requestedPage = schoolSearchCommand.getRequestedPage();

        SearchResultsPage<ISchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<ISchoolSearchResult>());
        // allow nearby searches to go through even if they specify no school types or grade levels, per GS-11931
        if (state != null && (!schoolSearchCommand.isAjaxRequest() || (schoolSearchCommand.hasSchoolTypes() && schoolSearchCommand.hasGradeLevels())) &&
                (!schoolSearchCommand.isNearbySearchByLocation() || (schoolSearchCommand.hasSchoolTypes() && schoolSearchCommand.hasGradeLevels()))) {
            try {
                SchoolSearchService service = getSchoolSearchService();
                if (schoolSearchCommand.getSearchType() == SchoolSearchType.LOOSE) {
                    service = getLooseSchoolSearchService();
                }

                searchResultsPage = service.search(
                        // for nearby searches, do not search by name
                        schoolSearchCommand.isNearbySearch()?null:searchString,
                        fieldConstraints,
                        filterGroups,
                        sort,
                        schoolSearchCommand.getLat(),
                        schoolSearchCommand.getLon(),
                        schoolSearchCommand.getDistanceAsFloat(),
                        requestedPage.offset,
                        requestedPage.pageSize
                );

                if (searchResultsPage.getTotalResults() == 0 && searchResultsPage.getSpellCheckResponse() != null &&
                    !schoolSearchCommand.isNearbySearch()) {
                    String didYouMean = getSearchSuggestion(searchString, searchResultsPage.getSpellCheckResponse());

                    if (didYouMean != null) {
                        SearchResultsPage<ISchoolSearchResult> didYouMeanResultsPage = service.search(
                                didYouMean,
                                fieldConstraints,
                                filterGroups,
                                sort,
                                schoolSearchCommand.getLat(),
                                schoolSearchCommand.getLon(),
                                schoolSearchCommand.getDistanceAsFloat(),
                                requestedPage.offset,
                                requestedPage.pageSize
                        );

                        if(didYouMeanResultsPage != null && didYouMeanResultsPage.getTotalResults() > 0) {
                            model.put(MODEL_DID_YOU_MEAN, didYouMean);
                            foundDidYouMeanSuggestions = true;
                        }
                    }
                }
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use SchoolSearchService. Eating exception", e);
            }
        }

        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

        //get nearby cities and districts and put into model.
        NearbyCitiesFacade nearbyCitiesFacade = new NearbyCitiesFacade(commandAndFields);
        NearbyDistrictsFacade nearbyDistrictsFacade = new NearbyDistrictsFacade(commandAndFields);
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

        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(commandAndFields.getSchoolTypes()));
        if (levelCode != null) {
            model.put(MODEL_LEVEL_CODE, levelCode.getCommaSeparatedString());
        }

        addPagingDataToModel(requestedPage.getValidatedOffset(SCHOOL_SEARCH_PAGINATION_CONFIG, searchResultsPage.getTotalResults()), requestedPage.pageSize, requestedPage.pageNumber, searchResultsPage.getTotalResults(), model);
        addGamAttributes(request, response, pageHelper, fieldConstraints, filterGroups, searchString, searchResultsPage.getSearchResults(), city, district);

        City localCity = (city != null ? city : commandAndFields.getCityFromSearchString());
        if (localCity != null) {
            LocalBoard localBoard = _localBoardDao.findByCityId(localCity.getId());
            if (localBoard != null) {
                model.put(MODEL_LOCAL_BOARD_ID, localBoard.getBoardId());
                model.put(MODEL_LOCAL_CITY_NAME, localCity.getName());
            }
        }

        model.put(MODEL_SEARCH_STRING, searchString);
        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());
        model.put(MODEL_TOTAL_RESULTS, searchResultsPage.getTotalResults());

        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (state != null) {
            if (StringUtils.isNotBlank(searchString)) {
                relCanonicalUrl = getRelCanonicalForSearch(request, searchString, state, citySearchResults);
            } else {
                if (district != null) {
                    relCanonicalUrl = getRelCanonicalForDistrictBrowse(request, district);
                } else if (city != null) {
                    relCanonicalUrl = getRelCanonicalForCityBrowse(request, city, state);
               }
            }

            if (relCanonicalUrl != null) {
                model.put(MODEL_REL_CANONICAL, relCanonicalUrl);
            }
        }

        if (commandAndFields.isCityBrowse()) {
            model.putAll(new CityMetaDataHelper().getMetaData(commandAndFields));
        } else if (commandAndFields.isDistrictBrowse()) {
            model.putAll(new DistrictMetaDataHelper().getMetaData(commandAndFields));
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

        model.put(MODEL_OMNITURE_PAGE_NAME,
                getOmniturePageName(request, requestedPage.pageNumber, searchResultsPage.getTotalResults(),
                        isCityBrowse, isDistrictBrowse, foundDidYouMeanSuggestions)
        );
        model.put(MODEL_OMNITURE_HIERARCHY,
                getOmnitureHierarchy(requestedPage.pageNumber, searchResultsPage.getTotalResults(),
                        isCityBrowse, isDistrictBrowse,
                        citySearchResults, districtSearchResults)
        );

        String omnitureQuery = isSearch? getOmnitureQuery(searchString) : null;
        model.put(MODEL_OMNITURE_QUERY, omnitureQuery);
        model.put(MODEL_OMNITURE_SCHOOL_TYPE, getOmnitureSchoolType(schoolSearchTypes));
        model.put(MODEL_OMNITURE_SCHOOL_LEVEL, getOmnitureSchoolLevel(levelCode));
        model.put(MODEL_OMNITURE_SORT_SELECTION, getOmnitureSortSelection(sortChanged ? sort : null));
        model.put(MODEL_OMNITURE_RESULTS_PER_PAGE, getOmnitureResultsPerPage(schoolSearchCommand.getPageSize(), searchResultsPage.getTotalResults()));
        model.put(MODEL_OMNITURE_ADDRESS_SEARCH, false);
        model.put(MODEL_OMNITURE_NAME_SEARCH, false);

        if (schoolSearchCommand.isNearbySearchByLocation()) {
            model.put(MODEL_OMNITURE_ADDRESS_SEARCH, true);
        } else if (StringUtils.equals(request.getParameter("search_type"), "1")) {
            model.put(MODEL_OMNITURE_NAME_SEARCH, true);
        }
        if (schoolSearchCommand.isAjaxRequest()) {
            if (searchResultsPage.getTotalResults() == 0) {
                return new ModelAndView(getNoResultsAjaxViewName(), model);
            } else {
                return new ModelAndView(getAjaxViewName(), model);
            }
        } else {
            if (searchResultsPage.getTotalResults() == 0 && !schoolSearchCommand.isNearbySearchByLocation()) {
                return new ModelAndView(getNoResultsViewName(), model);
            } else {
                return new ModelAndView(getViewName(), model);
            }
        }
    }

    public String getSearchSuggestion(String searchString, SpellCheckResponse spellCheckResponse) {
        String suggestedSearch = searchString;

        Map<String,SpellCheckResponse.Suggestion> suggestionMap = spellCheckResponse.getSuggestionMap();

        String[] tokens = StringUtils.splitPreserveAllTokens(suggestedSearch);

        if (tokens != null) {
            for (int i = 0; i < tokens.length; i++) {
                if (suggestionMap.containsKey(tokens[i])) {
                    tokens[i] = suggestionMap.get(tokens[i]).getAlternatives().get(0);
                } else if (suggestionMap.containsKey(StringUtils.lowerCase(tokens[i]))) {
                    tokens[i] = suggestionMap.get(StringUtils.lowerCase(tokens[i])).getAlternatives().get(0);
                }
            }
        }

        String result = StringUtils.join(tokens, ' ');

        if (searchString.equalsIgnoreCase(result)) {
            result = null;
        }

        return result;
    }

    protected class MetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            String searchString = commandWithFields.getSearchString();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(searchString));
            return model;
        }
    }

    protected class NearbyMetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            String searchString = commandWithFields.getSearchString();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(searchString));
            return model;
        }

        public Map<String,Object> getMetaData(Map nearbySearchInfo) {
            Map<String,Object> model = new HashMap<String,Object>();

            if (nearbySearchInfo != null) {
                Object zipCodeObj = nearbySearchInfo.get("zipCode");
                if (zipCodeObj != null && zipCodeObj instanceof String) {
                    model.put(MODEL_TITLE, getTitle((String) zipCodeObj));
                }
            }

            return model;
        }
    }

    protected class DistrictMetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            District district = commandWithFields.getDistrict();
            String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
            LevelCode levelCode = commandWithFields.getLevelCode();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(district, levelCode, schoolSearchTypes));
            model.put(MODEL_META_DESCRIPTION, getMetaDescription(district));
            model.put(MODEL_META_KEYWORDS, getMetaKeywords(district));
            return model;
        }
    }

    protected class CityMetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            City city = commandWithFields.getCityFromUrl();
            String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
            LevelCode levelCode = commandWithFields.getLevelCode();

            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(city.getDisplayName(), city.getState(), levelCode, schoolSearchTypes));
            model.put(MODEL_META_DESCRIPTION, getMetaDescription(city, levelCode, schoolSearchTypes));
            return model;
        }
    }

    protected State getState(HttpServletRequest request, SchoolSearchCommand schoolSearchCommand, DirectoryStructureUrlFields fields) {
        State state;
        if (schoolSearchCommand.getState() != null) {
            try {
                state = State.fromString(schoolSearchCommand.getState());
            } catch (IllegalArgumentException iae) {
                // invalid state, use default
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }
        } else if (fields != null && fields.getState() != null) {
            state = fields.getState();
        } else {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        return state;
    }

    private ModelAndView stateBrowseRedirect(HttpServletRequest request, SessionContext sessionContext) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESEARCH, sessionContext.getState());
        final String url = builder.asSiteRelative(request);
        final RedirectView view = new RedirectView(url, false);
        return new ModelAndView(view);
    }

    public ModelAndView redirectTo404(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView(VIEW_NOT_FOUND);
    }

    class InvalidDistrictException extends Exception {
        public InvalidDistrictException(String message) {
            super(message);
        }
    }
    class InvalidCityException extends Exception {
        public InvalidCityException(String message) {
            super(message);
        }
    }

    //-------------------------------------------------------------------------
    // omniture
    //-------------------------------------------------------------------------

    protected static String getOmniturePageName(HttpServletRequest request, int currentPage, int totalResults,
                                                boolean isCityBrowse, boolean isDistrictBrowse,
                                                boolean foundDidYouMeanSuggestions) {
        String pageName = "";

        String paramMap = request.getParameter("map");

        if (isCityBrowse) {
            pageName = "schools:city:" + currentPage + ("1".equals(paramMap) ? ":map" : "");
        } else if (isDistrictBrowse) {
            pageName = "schools:district:" + currentPage + ("1".equals(paramMap) ? ":map" : "");
        } else if (totalResults > 0) {
            pageName = "School Search:Page" + currentPage;
        } else {
            String pageNamePartTwo = null;
            if (foundDidYouMeanSuggestions) {
                pageNamePartTwo = "noresults_Didyoumean";
            } else {
                pageNamePartTwo = "noresults";
            }
            pageName = "School Search:" + pageNamePartTwo;
        }
        return pageName;
    }

    protected static String getOmnitureHierarchy(int currentPage, int totalResults,
                                                boolean isCityBrowse, boolean isDistrictBrowse,
                                                List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults) {
        String hierarchy = "";

        boolean hasCityResults = (citySearchResults != null && citySearchResults.size() > 0);
        boolean hasDistrictResults = (districtSearchResults != null && districtSearchResults.size() > 0);

        if (isCityBrowse) {
            hierarchy = "Search,Schools,City," + (totalResults > 0 ? currentPage : "noresults");
        } else if (isDistrictBrowse) {
            hierarchy = "Search,Schools,District," + (totalResults > 0 ? currentPage : "noresults");
        } else if (totalResults > 0) {
            hierarchy = "Search,School Search," + currentPage;
        } else {
            String hierarchyPartTwo;
            if (hasCityResults) {
                if (hasDistrictResults) {
                    hierarchyPartTwo = "City and District only";
                } else {
                    hierarchyPartTwo = "City only";
                }
            } else {
                if (hasDistrictResults) {
                    hierarchyPartTwo = "District Only";
                } else {
                    hierarchyPartTwo = "Pagenoresults";
                }
            }
            hierarchy = "Search,School Search," + hierarchyPartTwo;
        }
        return hierarchy;
    }

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
    // title, meta description, meta keywords
    //-------------------------------------------------------------------------

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

    public static String getTitle(String cityDisplayName, State cityState, LevelCode levelCode, String[] schoolType) {
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
        sb.append(" - ");
        if ("Washington, DC".equals(cityDisplayName)) {
            sb.append(cityDisplayName);
        } else {
            sb.append(cityDisplayName).append(", ").append(cityState.getAbbreviation());
        }
        sb.append(" | GreatSchools");
        return sb.toString();
    }

    protected static String getTitle(boolean isCityBrowse, boolean isDistrictBrowse, City city, District district, LevelCode levelCode, String[] schoolTypes, String searchString) {
        if (isCityBrowse) {
            return getTitle(city.getDisplayName(), city.getState(), levelCode, schoolTypes);
        } else if (isDistrictBrowse) {
            return getTitle(district, levelCode, schoolTypes);
        } else {
            return getTitle(searchString);
        }
    }
    protected static String getTitle(District district, LevelCode levelCode, String[] schoolTypes) {
        return SeoUtil.generatePageTitle(district, levelCode, schoolTypes);
    }
    protected static String getTitle(String searchString) {
        String title;
        if (StringUtils.isNotBlank(searchString)) {
            title = "GreatSchools.org Search: " + StringEscapeUtils.escapeHtml(searchString);
        } else {
            title = "GreatSchools.org Search";
        }
        return title;
    }

    protected static String getMetaDescription(boolean isCityBrowse, boolean isDistrictBrowse, City city, District district, LevelCode levelCode, String[] schoolTypes) {
        if (isCityBrowse) {
            return calcMetaDesc(null, city.getDisplayName(), city.getState(), levelCode, schoolTypes);
        } else if (isDistrictBrowse) {
            return SeoUtil.generateMetaDescription(district);
        } else {
            return null;
        }
    }
    protected static String getMetaDescription(City city, LevelCode levelCode, String[] schoolTypes) {
        return calcMetaDesc(null, city.getDisplayName(), city.getState(), levelCode, schoolTypes);
    }
    protected static String getMetaDescription(District district) {
        return SeoUtil.generateMetaDescription(district);
    }

    protected static String getMetaKeywords(District district) {
        if (district != null) {
            return SeoUtil.generateMetaKeywords(district);
        }
        return null;
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

    //-------------------------------------------------------------------------
    // GAM
    //-------------------------------------------------------------------------

    protected void addGamAttributes(HttpServletRequest request, HttpServletResponse response, PageHelper pageHelper,
                                    Map<FieldConstraint,String> constraints, List<FilterGroup> filterGroups, String searchString,
                                    List<ISchoolSearchResult> schoolResults,
                                    City city, District district) {
        if (pageHelper == null || constraints == null || filterGroups == null || schoolResults == null) {
            // search string can be null
            throw new IllegalArgumentException("PageHelper, constraints, filters, and school results must not be null");
        }

        Set<SchoolFilters> filtersSet = new HashSet<SchoolFilters>();
        for (FilterGroup filterGroup : filterGroups) {
            filtersSet.addAll(Arrays.asList(filterGroup.getFieldFilters()));
        }

        // GS-10003 - school type
        for (SchoolFilters.SchoolTypeFilter schoolTypeFilter : SchoolFilters.SchoolTypeFilter.values()) {
            if (filtersSet.contains(schoolTypeFilter)) {
                pageHelper.addAdKeywordMulti("type", schoolTypeFilter.toString().toLowerCase());
            }
        }

        // GS-6875 - level
        Set<SchoolFilters.GradeLevelFilter> gradeLevelFilters = new HashSet<SchoolFilters.GradeLevelFilter>();
        for (SchoolFilters.GradeLevelFilter gradeLevelFilter : SchoolFilters.GradeLevelFilter.values()) {
            if (filtersSet.contains(gradeLevelFilter)) {
                gradeLevelFilters.add(gradeLevelFilter);
            }
        }
        if (gradeLevelFilters.size() == 0) {
            gradeLevelFilters.addAll(Arrays.asList(SchoolFilters.GradeLevelFilter.values()));
        }
        for (SchoolFilters.GradeLevelFilter gradeLevelFilter : gradeLevelFilters) {
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

        // GS-11511 - nearby search by zip code
        if (request.getAttribute("nearbySearchInfo") != null && request.getAttribute("nearbySearchInfo") instanceof Map) {
            Map nearbySearchInfo = (Map) request.getAttribute("nearbySearchInfo");
            Object nearbyZipCode = nearbySearchInfo.get("zipCode");
            Object nearbyState = nearbySearchInfo.get("state");
            Object nearbyCity = nearbySearchInfo.get("city");

            if (nearbyZipCode != null && nearbyZipCode instanceof String) {
                pageHelper.addAdKeyword("zipcode", (String)nearbyZipCode);
            }
            if (nearbyState != null && nearbyState instanceof State) {
                // this overrides the state GAM attribute
                pageHelper.addAdKeyword("state", ((State)nearbyState).getAbbreviation());
            }
            if (nearbyCity != null && nearbyCity instanceof String) {
                pageHelper.addAdKeywordMulti("city", (String)nearbyCity);
            }
        }
    }

    //-------------------------------------------------------------------------
    // rel canonical
    //-------------------------------------------------------------------------
    protected String getRelCanonicalForDistrictBrowse(HttpServletRequest request, District district) {
        if (request == null || district == null) {
            throw new IllegalArgumentException("HttpServletRequest and District are required and cannot be null");
        }
        UrlBuilder urlBuilder = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
        String url = urlBuilder.asFullUrl(request);

        return url;
    }

    protected String getRelCanonicalForCityBrowse(HttpServletRequest request, City city, State state) {
        if (request == null || city == null || state == null) {
            throw new IllegalArgumentException("request, city, and state are required and cannot be null");
        }
        HashSet<SchoolType> schoolTypeSet = new HashSet<SchoolType>(1);
        schoolTypeSet.add(SchoolType.PUBLIC);
        schoolTypeSet.add(SchoolType.CHARTER);
        schoolTypeSet.add(SchoolType.PRIVATE);

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                state,
                city.getName(),
                schoolTypeSet, null);
        String url = urlBuilder.asFullUrl(request);

        return url;
    }

    protected String getRelCanonicalForSearch(HttpServletRequest request, String searchString, State state, List<ICitySearchResult> citySearchResults) {
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
        }
        return url;
    }

    protected String getRelCanonical(HttpServletRequest request, State state, List<ICitySearchResult> citySearchResults, City city, District district,
                                     List<FilterGroup> filterGroups, LevelCode levelCode, String searchString) {
        if (request == null || state == null) {
            throw new IllegalArgumentException("Request and state must not be null");
        }
        String url = null;

        if (StringUtils.isNotBlank(searchString) && state != null) {
            url = getRelCanonicalForSearch(request, searchString, state, citySearchResults);
        } else {
            if (district != null) {
                url = getRelCanonicalForDistrictBrowse(request, district);
            } else if (city != null) {
                url = getRelCanonicalForCityBrowse(request, city, state);
           }
        }

        return url;
    }

    //-------------------------------------------------------------------------
    // helper methods for filters, constraints
    //-------------------------------------------------------------------------

    protected Map<FieldConstraint,String> getFieldConstraints(State state, City city, District district) {
        Map<FieldConstraint,String> fieldConstraints = new HashMap<FieldConstraint,String>();

        if (state != null) {
            fieldConstraints.put(FieldConstraint.STATE, state.getAbbreviationLowerCase());
        }

        // GS-11129 if constraining by district, don't also constrain by city
        // or else we'll omit schools in the district that aren't in the same city as the distict
        if (district != null) {
            fieldConstraints.put(FieldConstraint.DISTRICT_ID, String.valueOf(district.getId()));
        } else if (city != null) {
            fieldConstraints.put(FieldConstraint.CITY, city.getName());
        }

        return fieldConstraints;
    }

    protected List<SchoolFilters> getGradeLevelFilters(String[] gradeLevelStrings) {
        List<SchoolFilters> filters = new ArrayList<SchoolFilters>();

        if (gradeLevelStrings != null) {
            for (String levelCode : gradeLevelStrings) {
                LevelCode.Level level = LevelCode.Level.getLevelCode(levelCode);
                if (level != null) {
                    SchoolFilters filter = getGradeLevelFilter(level);
                    if (filter != null) {
                        filters.add(filter);
                    }
                }
            }
        }

        return filters;
    }

    protected List<SchoolFilters> getGradeLevelFilters(LevelCode levelCode) {
        List<SchoolFilters> filters = new ArrayList<SchoolFilters>();

        if (levelCode != null) {
            for (LevelCode.Level level : levelCode.getIndividualLevelCodes()) {
                SchoolFilters filter = getGradeLevelFilter(level);
                if (filter != null) {
                    filters.add(filter);
                }
            }
        }

        return filters;
    }

    protected SchoolFilters getSchoolTypeFilter(SchoolType schoolType) {
        if (schoolType == null) {
            throw new IllegalArgumentException("Cannot get filter for null SchoolType");
        }
        SchoolFilters filter = SchoolFilters.SchoolTypeFilter.valueOf(StringUtils.upperCase(schoolType.getName()));
        return filter;
    }
    protected SchoolFilters getGradeLevelFilter(LevelCode.Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Cannot get filter for null LevelCode");
        }
        SchoolFilters filter = SchoolFilters.GradeLevelFilter.valueOf(StringUtils.upperCase(level.getLongName()));
        return filter;
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

    //-------------------------------------------------------------------------
    // spring-injected accessors
    //-------------------------------------------------------------------------

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

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
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

    /**
     * In the event SchoolSearchController is refactored, this class can be modified to accept as input a different
     * class as long as it contains lat, lon, and a way of determining the current city when on a browse page.
     */
    class NearbyCitiesFacade {
        public static final int DEFAULT_RADIUS = 50;
        SchoolSearchCommandWithFields _commandAndFields;

        public NearbyCitiesFacade(SchoolSearchCommandWithFields commandAndFields) {
            this._commandAndFields = commandAndFields;
        }

        /**
         * In the future this method should probably be decoupled from the "SchoolSearchCommandWithFields" class,
         * but only if this SchoolSearchController is refactored as well.
         *
         * @return
         */
        public List<ICitySearchResult> getNearbyCities() {
            Float browseLat = _commandAndFields.getLatitude();
            Float browseLon = _commandAndFields.getLongitude();
            City city = _commandAndFields.getCityFromUrl();
            List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
            try {
                SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().getCitiesNear(browseLat, browseLon, DEFAULT_RADIUS, null, 0, NEARBY_CITIES_PAGE_SIZE);

                citySearchResults = cityPage.getSearchResults();

                //if nearby city matches this city, remove it from nearby list
                if (citySearchResults != null && city != null) {
                    Iterator<ICitySearchResult> iterator = citySearchResults.listIterator();
                    while (iterator.hasNext()) {
                        ICitySearchResult result = iterator.next();
                        if (result.getCity().equals(city.getName())) {
                            iterator.remove();
                        }
                    }
                }
            } catch (SearchException ex) {
                _log.debug("something went wrong when attempting to use CitySearchService. Eating exception", ex);
            }

            return citySearchResults;
        }

        public List<ICitySearchResult> getNearbyCitiesByLatLon() {
            Float lat = _commandAndFields.getLatitude();
            Float lon = _commandAndFields.getLongitude();
            List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
            try {
                if (lat != null && lon != null) {
                    SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().getCitiesNear(lat, lon, DEFAULT_RADIUS, null, 0, NEARBY_CITIES_PAGE_SIZE);
                    citySearchResults = cityPage.getSearchResults();
                }
            } catch (SearchException ex) {
                _log.debug("Something went wrong when attempting to use CitySearchService. Eating exception", ex);
            }

            return citySearchResults;
        }

        /**
         * Try to decouple this class from "SchoolSearchCommandWithFields" if SchoolSearchController changes.
         * Method does not actually look for "nearby" cities... just cities matching a string
         *
         * @return
         */
        public List<ICitySearchResult> searchForCities() {
            String searchString = _commandAndFields.getSearchString();
            State state = _commandAndFields.getState();
            List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

            if (state == null) {
                //don't try to find cities without a state
                return citySearchResults;
            }

            Map<IFieldConstraint, String> cityConstraints = new HashMap<IFieldConstraint, String>();
            cityConstraints.put(CitySearchFieldConstraints.STATE, state.getAbbreviationLowerCase());

            try {
                if (searchString != null) {
                    try {
                        // when searching for "anchorage, ak", do not search for cities matching "ak"
                        // primarily an issue with city autocomplete as implemented for GS-11928 Find a School by location
                        if (StringUtils.endsWithIgnoreCase(searchString, " " + state.getAbbreviation()) ||
                                StringUtils.endsWithIgnoreCase(searchString, "," + state.getAbbreviation())) {
                            searchString = StringUtils.substring(searchString, 0, searchString.length()-2);
                        }
                    } catch (Exception e) {/* ignore */}
                    SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().search(searchString, cityConstraints, null, null, 0, 33);
                    citySearchResults = cityPage.getSearchResults();
                }
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", ex);
            }

            return citySearchResults;
        }
    }


    /**
     * In the event SchoolSearchController is refactored, this class can be modified to accept as input a different
     * class as long as it contains lat, lon, and a way of determining the current city when on a browse page.
     */
    class NearbyDistrictsFacade {
        public static final int DEFAULT_RADIUS = 50;
        public static final int DEFAULT_LIMIT = 11;
        SchoolSearchCommandWithFields _commandAndFields;

        public NearbyDistrictsFacade(SchoolSearchCommandWithFields commandAndFields) {
            this._commandAndFields = commandAndFields;
        }

        /**
         * In the future this method should probably be decoupled from the "SchoolSearchCommandWithFields" class,
         * but only if this SchoolSearchController is refactored as well.
         *
         * @return
         */
        public List<IDistrictSearchResult> getNearbyDistricts() {
            Float browseLat = _commandAndFields.getLatitude();
            Float browseLon = _commandAndFields.getLongitude();
            City city = _commandAndFields.getCityFromUrl();
            List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();
            try {
                SearchResultsPage<IDistrictSearchResult> districtPage = getDistrictSearchService().getDistrictsNear(browseLat, browseLon, DEFAULT_RADIUS, null, 0, DEFAULT_LIMIT);

                districtSearchResults = districtPage.getSearchResults();

                //if nearby city matches this city, remove it from nearby list
                if (districtSearchResults != null && city != null) {
                    Iterator<IDistrictSearchResult> iterator = districtSearchResults.listIterator();
                    while (iterator.hasNext()) {
                        IDistrictSearchResult result = iterator.next();
                        if (result.getCity().equals(city.getName())) {
                            iterator.remove();
                        }
                    }
                }
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", ex);
            }

            return districtSearchResults;
        }

        /**
         * Try to decouple this class from "SchoolSearchCommandWithFields" if SchoolSearchController changes.
         * Method does not actually look for "nearby" cities... just cities matching a string
         *
         * @return
         */
        public List<IDistrictSearchResult> searchForDistricts() {
            String searchString = _commandAndFields.getSearchString();
            State state = _commandAndFields.getState();
            List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

            if (state == null) {
                //don't try to find districts without a state
                return districtSearchResults;
            }

            Map<IFieldConstraint, String> districtConstraints = new HashMap<IFieldConstraint, String>();
            districtConstraints.put(DistrictSearchFieldConstraints.STATE, state.getAbbreviationLowerCase());

            try {
                if (searchString != null) {
                    SearchResultsPage<IDistrictSearchResult> districtPage = getDistrictSearchService().search(searchString, districtConstraints, null, null, 0, DEFAULT_LIMIT);
                    districtSearchResults = districtPage.getSearchResults();
                }
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", ex);
            }

            return districtSearchResults;
        }
    }

    public String getMobileViewName() {
        return _mobileViewName;
    }

    public void setMobileViewName(String mobileViewName) {
        _mobileViewName = mobileViewName;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public boolean beanSupportsMobileRequests() {
        return _beanSupportsMobileRequests;
    }

    public boolean beanSupportsDesktopRequests() {
        return _beanSupportsDesktopRequests;
    }

    public void setBeanSupportsMobileRequests(boolean beanSupportsMobileRequests) {
        _beanSupportsMobileRequests = beanSupportsMobileRequests;
    }

    public void setBeanSupportsDesktopRequests(boolean beanSupportsDesktopRequests) {
        _beanSupportsDesktopRequests = beanSupportsDesktopRequests;
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
}


/*


public static boolean isOldStyleCityBrowseRequest(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }

        // state is in SessionContext already
        return (!isDistrictBrowseRequest(request) &&
                request.getRequestURI().contains("/schools.page") &&
                request.getParameter(PARAM_CITY) != null);
    }

    public static String createNewCityBrowseURI(HttpServletRequest request) {
        // school type(s)
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        final String[] paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);
        if (paramSchoolType != null) {
            for (String schoolType : paramSchoolType) {
                if (SchoolType.PUBLIC.getSchoolTypeName().equals(schoolType)) {
                    schoolTypes.add(SchoolType.PUBLIC);
                } else if (SchoolType.PRIVATE.getSchoolTypeName().equals(schoolType)) {
                    schoolTypes.add(SchoolType.PRIVATE);
                } else if (SchoolType.CHARTER.getSchoolTypeName().equals(schoolType)) {
                    schoolTypes.add(SchoolType.CHARTER);
                }
            }
        }

        // level code
        LevelCode levelCode = null;
        final String[] paramLevelCode = request.getParameterValues(PARAM_LEVEL_CODE);
        if (paramLevelCode != null) {
            levelCode = LevelCode.createLevelCode(paramLevelCode);
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
            SessionContextUtil.getSessionContext(request).getState(), request.getParameter(PARAM_CITY), schoolTypes, levelCode);
        return urlBuilder.asSiteRelative(request);
    }


*/