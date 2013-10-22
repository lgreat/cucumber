package gs.web.search;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.geo.City;
import gs.data.geo.ICounty;
import gs.data.geo.IGeoDao;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.pagination.DefaultPaginationConfig;
import gs.data.pagination.PaginationConfig;
import gs.data.school.LevelCode;
import gs.data.school.SchoolHelper;
import gs.data.school.SchoolType;
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
import gs.data.state.State;
import gs.data.util.CommunityUtil;
import gs.data.zillow.ZillowRegionDao;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.community.LocalBoardHelper;
import gs.web.jsp.Util;
import gs.web.pagination.Page;
import gs.web.pagination.RequestedPage;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.request.RequestInfo;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *   Adding the changes needed for GS-14144
 *   @author sarora@greatschools.org   - Shomi Arora
 */

public class SchoolSearchController2012  extends AbstractCommandController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {

    @Autowired
    private IDistrictDao _districtDao;
    @Autowired
    private IGeoDao _geoDao;
    @Autowired
    private LocalBoardHelper _localBoardHelper;
    @Autowired
    private CityBrowseHelper2012 _cityBrowseHelper;
    @Autowired
    private DistrictBrowseHelper2012 _districtBrowseHelper;
    @Autowired
    private SchoolSearchHelper _queryStringSearchHelper;
    @Autowired
    private SchoolSearchCommonHelper commonSearchHelper;
    @Autowired
    private GsSolrSearcher _gsSolrSearcher;

    @Autowired
    private ZillowRegionDao _zillowDao;
    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;
    @Autowired
    private IHubConfigDao _hubConfigDao;

    private String _noResultsViewName;
    private String _noResultsAjaxViewName;
    private String _viewName;
    private String _ajaxViewName;
    private String _mobileViewName;
    private String _mapViewName;

    private ControllerFamily _controllerFamily;

    private static final Logger _log = Logger.getLogger(SchoolSearchController2012.class);
    public static final String BEAN_ID = "/search/search.page";

    public static final String MODEL_SORT = "sort";

    public static final String MODEL_SCHOOL_SEARCH_RESULTS = "schoolSearchResults";
    public static final String MODEL_CITY_SEARCH_RESULTS = "citySearchResults";
    public static final String MODEL_DISTRICT_SEARCH_RESULTS = "districtSearchResults";

    public static final String MODEL_USE_PAGING = "usePaging";
    public static final String MODEL_PAGE = "page";

    public static final String MODEL_MSL_SCHOOLS = "mslSchools";

    public static final String MODEL_CITY = "city";
    public static final String MODEL_DISTRICT = "district";
    public static final String MODEL_STATE = "state";

    public static final String MODEL_REL_CANONICAL = "relCanonical";
    public static final String MODEL_CURRENT_URL_MATCHES_REL_CANONICAL = "currentUrlMatchesRelCanonical";
    public static final String MODEL_NEARBY_SEARCH_TITLE_PREFIX = "nearbySearchTitlePrefix";
    public static final String MODEL_NEARBY_SEARCH_IS_ESTABLISHMENT= "nearbySearchIsEstablishment";
    public static final String MODEL_NEARBY_SEARCH_ZIP_CODE = "nearbySearchZipCode";

    public static final String MODEL_DID_YOU_MEAN = "didYouMean";
    public static final String MODEL_SEARCH_STRING = "searchString";

    public static final String MODEL_CITY_BROWSE = "isCityBrowse";
    public static final String MODEL_DISTRICT_BROWSE = "isDistrictBrowse";
    public static final String MODEL_IS_NEARBY_SEARCH = "isNearbySearch";
    public static final String MODEL_IS_SEARCH = "isSearch";
    public static final String MODEL_IS_AJAX_REQUEST = "isAjaxRequest";

    public static final String MODEL_SEO_TITLE = "seoTitle";

    public static final int MAX_PAGE_SIZE = 100;

    protected static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final String MODEL_IS_HUBS_LOCAL_SEARCH = "isHubsLocalSearch";
    public static final String MODEL_IS_AD_FREE_HUB = "isAdFreeHub";

    public static final PaginationConfig SCHOOL_SEARCH_PAGINATION_CONFIG;

    public static final String[] FACEBOOK_FACEPILE_PILOT_CITIES = new String[] {"San Francisco", "Oakland", "Berkeley"};
    public static final String FACEBOOK_FACEPILES_ENABLED_MODEL_KEY = "facebookFacepilesEnabled";

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

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;



        Map<String,Object> model = new HashMap<String,Object>();


        model.put("schoolSearchCommand", schoolSearchCommand);
        model.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();




        // add in MSL schools, so results view knows whether each school is in user's MSL
        if (user != null) {
            model.put(MODEL_MSL_SCHOOLS, user.getFavoriteSchools());
        }

        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        if (schoolSearchCommand.getCity()!=null && schoolSearchCommand.getState()!=null   )
        {


        }

         if (schoolSearchCommand.getCity()!=null && schoolSearchCommand.getState()!=null   )
          {
            final Integer zillowRegionId=_zillowDao.findRegionId(schoolSearchCommand.getCity(), schoolSearchCommand.getState());
            final String formattedURLForZillowIntegration= StringUtils.lowerCase(StringUtils.replace(schoolSearchCommand.getCity(), " ", "-") + "-" + schoolSearchCommand.getState()) ;
            model.put("formattedUrl",formattedURLForZillowIntegration)  ;
            model.put("regionID",zillowRegionId)  ;
         } else if (fields.getCityName() !=null && fields.getState()!=null )
         {
             final Integer zillowRegionId=_zillowDao.findRegionId(fields.getCityName(), fields.getState().getAbbreviation());
             final String formattedURLForZillowIntegration= StringUtils.lowerCase(StringUtils.replace(fields.getCityName(), " ", "-") + "-" + fields.getState().getAbbreviation()) ;
             model.put("formattedUrl",formattedURLForZillowIntegration)  ;
             model.put("regionID",zillowRegionId)  ;
         }


        // GS-13174 : was decided to use the lat lon returned by google maps instead of using the values in us_geo.bp_census
        /*if (schoolSearchCommand.isNearbySearchByLocation()) {
            String searchString = schoolSearchCommand.getLocationSearchString() != null ? schoolSearchCommand.getLocationSearchString() : schoolSearchCommand.getSearchString();
            // check for exact county match
            ICounty county = getExactCountyMatch(searchString);
            if (county != null) {
                schoolSearchCommand.setLat((double)county.getLat());
                schoolSearchCommand.setLon((double)county.getLon());
                schoolSearchCommand.setNormalizedAddress(county.getName() + " County, " + county.getState());
            }
        }*/

        if(schoolSearchCommand.isAjaxRequest() && !schoolSearchCommand.hasGradeLevels()) {
            fields.setLevelCode(null);
        }

        if(schoolSearchCommand.isAjaxRequest() && !schoolSearchCommand.hasSchoolTypes()) {
            fields.setSchoolTypesParams(null);
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

        //if by name search, check if the search string is a exact osp city state match, and redirect to the city browse page
        if (shouldRedirectFromByNameToCityBrowse(schoolSearchCommand, commandAndFields)) {
            return new ModelAndView(getCityBrowseRedirectView(request, schoolSearchCommand, commandAndFields));
        }
        /**
         * Adding the logic for packard search GS-14110  -Shomi Arora .
         */
        final  boolean shouldShowPackardFilter= isPackardFilters(schoolSearchCommand, commandAndFields);
        if (shouldShowPackardFilter) {
            final String MODEL_SHOW_PACKARD_FILTERS = "showPackardFilters";
            model.put(MODEL_SHOW_PACKARD_FILTERS, true);
        }

        // support new additional OSP filters for redesigned search
        boolean showAdvancedFilters = showAdvancedFilters(schoolSearchCommand, commandAndFields, model) || shouldShowPackardFilter;


        // local board module support
        _localBoardHelper.putLocalBoardInfoIntoModel(model, commandAndFields);


        // mobile support
        if ((commandAndFields.isCityBrowse() || commandAndFields.isDistrictBrowse()) && !commandAndFields.isAjaxRequest()) {
            model.put("hasMobileView", true);
        }


        model.put("commandAndFields", commandAndFields);
        model.put(MODEL_STATE, commandAndFields.getState());
        model.put(MODEL_IS_NEARBY_SEARCH, schoolSearchCommand.isNearbySearch());
        model.put(MODEL_IS_AJAX_REQUEST, schoolSearchCommand.isAjaxRequest());
        boolean isSearch = !commandAndFields.isCityBrowse() && !commandAndFields.isDistrictBrowse();
        model.put(MODEL_IS_SEARCH, isSearch);

        boolean isHubsLocalSearch = commandAndFields.isHubsLocalSearch();
        model.put(MODEL_IS_HUBS_LOCAL_SEARCH, isHubsLocalSearch);
        boolean isAdFreeHub = commandAndFields.isHubAdsFree();
        model.put(MODEL_IS_AD_FREE_HUB, (isHubsLocalSearch && isAdFreeHub));

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if(pageHelper != null && isHubsLocalSearch && isAdFreeHub) {
            pageHelper.setHideAds(true);
        }

        // City Browse Specific: Include facebook "facepile" functionality if on pilot city
        // Added here for now, so that we can force facepiles to be included with a url param, even on non-city-browse
        // search results pages
        model.put(
                FACEBOOK_FACEPILES_ENABLED_MODEL_KEY, shouldIncludeFacebookFacepiles(request, commandAndFields)
        );

        ModelAndView modelAndView;
        if (commandAndFields.isCityBrowse()) {
            model.put(MODEL_SEO_TITLE, buildSeoTitle(commandAndFields));
            modelAndView = handleCityBrowse(request, response, commandAndFields, model, showAdvancedFilters);
        } else if (commandAndFields.isDistrictBrowse()) {
            modelAndView = handleDistrictBrowse(request, response, commandAndFields, model, showAdvancedFilters);
        } else {
            modelAndView = handleQueryStringAndNearbySearch(request, response, commandAndFields, model, schoolSearchCommand.isNearbySearchByLocation(), showAdvancedFilters);
        }

        // hack to get school types / level code checkboxes to reflect browse prefilters
        schoolSearchCommand.setGradeLevels(commandAndFields.getGradeLevels());
        schoolSearchCommand.setSt(commandAndFields.getSchoolTypes());

        // Common: Set a cookie to record that a search has occurred;
        PageHelper.setHasSearchedCookie(request, response);

        if("POST".equalsIgnoreCase(request.getMethod()) && "map".equals(request.getParameter("view"))) {
            JSONObject responseJson = new JSONObject();
            response.setContentType("application/json");

            buildJsonResponse(model, request, sessionContext, schoolSearchCommand, responseJson);

            responseJson.write(response.getWriter());
            response.getWriter().flush();
            return null;
        }

        return modelAndView;
    }

    protected RedirectView getCityBrowseRedirectView(HttpServletRequest request, SchoolSearchCommand schoolSearchCommand, SchoolSearchCommandWithFields commandAndFields) {
        LevelCode levelCodeInCommandOrDefault = (commandAndFields.getLevelCode() != null) ? commandAndFields.getLevelCode() : LevelCode.ALL_LEVELS;
        UrlBuilder cityBrowseUrl = new UrlBuilder
                (UrlBuilder.SCHOOLS_IN_CITY, State.fromString(schoolSearchCommand.getState()),
                schoolSearchCommand.getSearchString(), new HashSet<SchoolType>(), levelCodeInCommandOrDefault);
        return new RedirectView(cityBrowseUrl.asSiteRelative(request));
    }

    protected boolean shouldRedirectFromByNameToCityBrowse(SchoolSearchCommand schoolSearchCommand, SchoolSearchCommandWithFields commandAndFields) {
        if(commandAndFields.isByNameSearch() && schoolSearchCommand.getCollectionId() == null) {
            String stateAbb = schoolSearchCommand.getState();
            String city = schoolSearchCommand.getSearchString();
            if(city != null && stateAbb !=null && (SchoolHelper.isLocal(city, stateAbb) || SchoolHelper.isNewAdvanceSearch(city, stateAbb))) {
                return true;
            }
        }
        return false;
    }

    protected String buildSeoTitle(SchoolSearchCommandWithFields commandAndFields) {
        StringBuilder seoTitle = new StringBuilder();
        String[] schoolTypes = commandAndFields.getSchoolTypes();
        int numSchoolTypes = schoolTypes != null ? schoolTypes.length : 0;
        String[] gradeLevels = commandAndFields.getGradeLevels() != null ? commandAndFields.getGradeLevelNames() : null;
        int numGradeLevels = gradeLevels != null ? gradeLevels.length : 0;
        if(numGradeLevels > 0) {
            for(int i = 0; i < numGradeLevels; i++) {
                seoTitle.append(WordUtils.capitalizeFully(gradeLevels[i]));
                if(i == numGradeLevels - 2 && numSchoolTypes == 0) {
                    seoTitle.append(" and ");
                }
                else if(i == numGradeLevels - 1) {
                    seoTitle.append(" Schools");
                }
                else {
                    seoTitle.append(", ");
                }
            }
        }
        if(numSchoolTypes > 0) {
            if(numGradeLevels > 0) {
                seoTitle.append(", ");
            }
            for(int i = 0; i < numSchoolTypes; i++) {
                if("charter".equals(schoolTypes[i])) {
                    seoTitle.append("Public Charter");
                }
                else {
                    seoTitle.append(WordUtils.capitalizeFully(schoolTypes[i]));
                }
                if(i == numSchoolTypes - 2) {
                    seoTitle.append(" and ");
                }
                else if(i == numSchoolTypes - 1) {
                    seoTitle.append(" Schools");
                }
                else {
                    seoTitle.append(", ");
                }
            }
        }
        return seoTitle.toString();
    }

    protected void buildJsonResponse(Map<String, Object> model,
                                     HttpServletRequest request,
                                     SessionContext sessionContext,
                                     SchoolSearchCommand schoolSearchCommand,
                                     JSONObject responseJson) throws JSONException {
        Map<String, Object> searchResults;
        List<SolrSchoolSearchResult> solrSchoolSearchResults = (List<SolrSchoolSearchResult>) model.get(MODEL_SCHOOL_SEARCH_RESULTS);

        if(solrSchoolSearchResults == null || (solrSchoolSearchResults != null && solrSchoolSearchResults.size() == 0)) {
            responseJson.accumulate(MODEL_PAGE, new HashMap<String, Object>());
            searchResults = new HashMap<String, Object>();
            searchResults.put("noSchoolsFound", true);
            searchResults.put("omniturePageName", model.get("omniturePageName"));
            searchResults.put("seoTitle", model.get(MODEL_SEO_TITLE));
            responseJson.accumulate(MODEL_PAGE, searchResults);
            return;
        }

        responseJson.accumulate(MODEL_SCHOOL_SEARCH_RESULTS, new HashMap<String, Object>());
        Set<FavoriteSchool> mslSchools = (Set<FavoriteSchool>) model.get(MODEL_MSL_SCHOOLS);

        for(int i = 0; i < solrSchoolSearchResults.size(); i++) {
            searchResults = new HashMap<String, Object>();
            SolrSchoolSearchResult schoolSearchResult = solrSchoolSearchResults.get(i);
            searchResults.put("id", schoolSearchResult.getId());
            searchResults.put("state", schoolSearchResult.getDatabaseState());
            searchResults.put("name", schoolSearchResult.getName());
            searchResults.put("jsEscapeName", StringEscapeUtils.escapeJavaScript(schoolSearchResult.getName()));
            searchResults.put("physicalAddress", schoolSearchResult.getAddress());
            searchResults.put("levelCode", schoolSearchResult.getLevelCode());
            searchResults.put("schoolType", schoolSearchResult.getSchoolType());
            searchResults.put("rangeString", schoolSearchResult.getGrades().getRangeString());
            searchResults.put("street", schoolSearchResult.getStreet());
            searchResults.put("city", schoolSearchResult.getCity());
            searchResults.put("zip", schoolSearchResult.getZip());
            searchResults.put("parentRating", schoolSearchResult.getParentRating());
            searchResults.put("latitude", schoolSearchResult.getLatitude());
            searchResults.put("longitude", schoolSearchResult.getLongitude());
            searchResults.put("greatSchoolsRating", schoolSearchResult.getGreatSchoolsRating());
            searchResults.put("mslHasSchool", false);
            searchResults.put("distance", (schoolSearchResult.getDistance() != null) ? Util.roundTwoDecimal(schoolSearchResult.getDistance()) : null );
            searchResults.put("isNewGSRating", schoolSearchResult.getIsNewGSRating());

            UrlBuilder schoolUrl = new UrlBuilder(UrlBuilder.SCHOOL_PROFILE, schoolSearchResult.getId(),
                    schoolSearchResult.getDatabaseState(), schoolSearchResult.getName(), schoolSearchResult.getAddress(),
                    LevelCode.createLevelCode(schoolSearchResult.getLevelCode()), false, null);
            searchResults.put("schoolUrl", schoolUrl.asFullUrlXml(request));

            UrlBuilder communityRatingUrl = new UrlBuilder(schoolSearchResult.getDatabaseState(), schoolSearchResult.getId(),
                    LevelCode.createLevelCode(schoolSearchResult.getLevelCode()), UrlBuilder.SCHOOL_PARENT_REVIEWS);
            searchResults.put("communityRatingUrl", communityRatingUrl.asFullUrl(request));

            UrlBuilder gsRatingUrl = new UrlBuilder(schoolSearchResult.getDatabaseState(), schoolSearchResult.getId(),
                    LevelCode.createLevelCode(schoolSearchResult.getLevelCode()), UrlBuilder.SCHOOL_PROFILE_RATINGS);
            searchResults.put("gsRatingUrl", gsRatingUrl.asFullUrl(request));

            if(mslSchools != null && mslSchools.size() > 0) {
                Iterator<FavoriteSchool> iterator = mslSchools.iterator();
                while(iterator.hasNext()) {
                    FavoriteSchool favoriteSchool = iterator.next();
                    if(favoriteSchool.getSchoolId().equals(schoolSearchResult.getId()) &&
                            favoriteSchool.getState().equals(schoolSearchResult.getDatabaseState())) {
                        searchResults.put("mslHasSchool", true);
                    }
                }
            }

            responseJson.accumulate(MODEL_SCHOOL_SEARCH_RESULTS, searchResults);
        }

        responseJson.accumulate("LatLon", new HashMap<String,Object>());
        District district = (District) model.get(MODEL_DISTRICT);
        City city = (City) model.get(MODEL_CITY);
        if(district != null) {
            searchResults = new HashMap<String, Object>();
            searchResults.put("calculatedLat", district.getLat());
            searchResults.put("calculatedLon", district.getLon());
            responseJson.accumulate("LatLon", searchResults);
        }
        else if(city != null) {
            searchResults = new HashMap<String, Object>();
            searchResults.put("calculatedLat", city.getLat());
            searchResults.put("calculatedLon", city.getLon());
            responseJson.accumulate("LatLon", searchResults);
        }
        else if(schoolSearchCommand != null) {
            searchResults = new HashMap<String, Object>();
            searchResults.put("calculatedLat", schoolSearchCommand.getLat());
            searchResults.put("calculatedLon", schoolSearchCommand.getLon());
            responseJson.accumulate("LatLon", searchResults);
        }
        else {
            searchResults = new HashMap<String, Object>();
            searchResults.put("calculatedLat", 0);
            searchResults.put("calculatedLon", 0);
            responseJson.accumulate("LatLon", searchResults);
        }

        responseJson.accumulate(MODEL_PAGE, new HashMap<String, Object>());
        Page page = (Page) model.get(MODEL_PAGE);
        int pageNum = page.getPageNumber();
        int totalPages = page.getPager().getTotalPages();
        searchResults.put("offset", page.getOffset() + 1);
        searchResults.put("lastOffsetOnPage", page.getLastOffsetOnPage() + 1);
        searchResults.put("totalResults", page.getPager().getTotalItems());
        if(pageNum > 1) {
            searchResults.put("previousPage", page.getPreviousPage().getPageNumber());
        }
        if(pageNum < totalPages) {
            searchResults.put("nextPage", page.getNextPage().getPageNumber());
        }
        searchResults.put("pageSize", page.getPager().getPageSize());
        searchResults.put("pageNumber", pageNum);
        searchResults.put("totalPages", totalPages);
        searchResults.put("firstPageNum", page.getPager().getFirstPage().getPageNumber());
        searchResults.put("lastPageNum", page.getPager().getLastPage().getPageNumber());
        List<Page> pages = page.getPageSequence();
        List<Integer> pageSeqNumbers = new ArrayList<Integer>();
        for(Page p : pages) {
            pageSeqNumbers.add(p.getPageNumber());
        }
        searchResults.put("pageSequence", pageSeqNumbers);
        searchResults.put("omniturePageName", model.get("omniturePageName"));
        responseJson.accumulate(MODEL_PAGE, searchResults);

        searchResults = new HashMap<String, Object>();
        responseJson.accumulate("salePromo", new HashMap<String, Object>());
        if(sessionContext.isShowRealtorDotComPromos()) {
            searchResults.put("homesForSale", true);
        }
        else {
            searchResults.put("homesForSale", false);
        }
        responseJson.accumulate("salePromo", searchResults);
        responseJson.accumulate("seoTitle", model.get(MODEL_SEO_TITLE));
        responseJson.accumulate("pageview_candidate", "pageview_candidate");
    }

    public boolean showAdvancedFilters(SchoolSearchCommand schoolSearchCommand, SchoolSearchCommandWithFields commandWithFields,
                                    Map<String, Object> model) {
        boolean showAdvancedFilters = false;
        final String MODEL_SHOW_ADDITIONAL_FILTERS = "showAdditionalFilters";
        // special-case check for DC
        if (schoolSearchCommand.getState() != null && schoolSearchCommand.getState().equalsIgnoreCase(State.DC.getAbbreviation())) {
            showAdvancedFilters = true;
        } else if(commandWithFields.isCityBrowse() || commandWithFields.isDistrictBrowse()) {
            City city = commandWithFields.getCity();
            if(city != null && SchoolHelper.isLocal(city.getName(), city.getState().getAbbreviation())) {
                showAdvancedFilters = true;
            }
        }
        else if (commandWithFields.isNearbySearch()) {
            if (SchoolHelper.isZipForNewSearchFilters(schoolSearchCommand.getZipCode()) || SchoolHelper.isLocal(schoolSearchCommand.getCity(), schoolSearchCommand.getState())) {
                showAdvancedFilters = true;
            }
        }
        else {
            if (SchoolHelper.isLocal(schoolSearchCommand.getSearchString(), schoolSearchCommand.getState())) {
                showAdvancedFilters = true;
            }
        }

        if (showAdvancedFilters) {
            model.put(MODEL_SHOW_ADDITIONAL_FILTERS, true);
        }

        return showAdvancedFilters;
    }

    /**
     *  Adding the logic to find is a page should have packard filters enabled or not -GS-14110 -Shomi Arora.
     * @param schoolSearchCommand      The schoolSearchCommand.
     * @param commandWithFields        The commandWithFields.
     * @return
     */

    protected boolean isPackardFilters(final SchoolSearchCommand schoolSearchCommand, final SchoolSearchCommandWithFields commandWithFields) {
        boolean isPackardFilters = false;
        if (commandWithFields.isCityBrowse() || commandWithFields.isDistrictBrowse()) {
            City city = commandWithFields.getCity();
            if (city != null && SchoolHelper.isNewAdvanceSearch(city.getName(), city.getState().getAbbreviation())) {
                isPackardFilters = true;
            }
        }
        else if (commandWithFields.isNearbySearch()) {
            if (SchoolHelper.isZipForNewAdvanceSearchFilters(schoolSearchCommand.getZipCode()) || SchoolHelper.isNewAdvanceSearch(schoolSearchCommand.getCity(), schoolSearchCommand.getState())) {
                isPackardFilters = true;
            }
        }
        else {
             // Added for the Default Search
           if (SchoolHelper.isNewAdvanceSearch(schoolSearchCommand.getSearchString(), schoolSearchCommand.getState())) {
                isPackardFilters = true;
            }
        }
     return isPackardFilters;
    }

    private String getNoResultsView(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields) {
        if (commandAndFields.isAjaxRequest()) {
            return getNoResultsAjaxViewName();
        } else {
            return getNoResultsViewName();
        }
    }

    protected SearchResultsPage<SolrSchoolSearchResult> searchForSchools(SchoolSearchCommandWithFields commandAndFields) {
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        SchoolSearchCommand schoolSearchCommand = commandAndFields.getSchoolSearchCommand();
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<SolrSchoolSearchResult>());
        String searchString = commandAndFields.getSearchString();

        GsSolrQuery q = createGsSolrQuery();

        q.filter(DocumentType.SCHOOL).page(requestedPage.offset, requestedPage.pageSize);

        if (commandAndFields.getState() != null) {
            q.filter(SchoolFields.SCHOOL_DATABASE_STATE, commandAndFields.getState().getAbbreviationLowerCase());
        }

        q.filter(SchoolFields.SCHOOL_TYPE, commandAndFields.getSchoolTypes());

        q.filter(SchoolFields.GRADE_LEVEL, commandAndFields.getGradeLevels());

        // TODO: the client should make a request with minSchoolSize and maxSchoolSize, rather than pass a string as it
        // does currently. Maintaining this UNDER_XX approach for backward compatibility
        String schoolSize = commandAndFields.getSchoolSize();
        if (schoolSize != null) {
            String minSchoolSize;
            String maxSchoolSize;
            SchoolFilters.SchoolSize size;
            try {
                size = SchoolFilters.SchoolSize.valueOf(schoolSize);
            } catch (IllegalArgumentException _iae) {
                size = null;
            }
            if (size != null) {
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
        }

        // handle logic that used to be taken care of with old "FieldConstraints"
        // Filter on school's district ID or city name or collection id
        District district = commandAndFields.getDistrict();
        City city = commandAndFields.getCity();
        boolean useCollectionIdAsSolrFilter = (commandAndFields.isHubsLocalSearch() && commandAndFields.getCollectionId() != null);
        if (district != null) {
            q.filter(SchoolFields.SCHOOL_DISTRICT_ID, String.valueOf(district.getId()));
        } else if (city != null && !useCollectionIdAsSolrFilter) {
            q.filter(AddressFields.CITY_UNTOKENIZED, "\"" + city.getName().toLowerCase() + "\"");
        } else if(useCollectionIdAsSolrFilter) {
            q.filter(SchoolFields.SCHOOL_COLLECTION_ID, "\"" + commandAndFields.getCollectionId() + "\"");
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
        /**
         * Adding the summer program and after school and grouping services  to the solr query - GS-14144 Shomi Arora
         */
        if (schoolSearchCommand.getSummerProgram() != null) {
                q.filter(SchoolFields.SUMMER_PROGRAM, "yes");
        }
        if (schoolSearchCommand.getAfterSchool() != null) {
                q.filter(SchoolFields.AFTER_SCHOOL, "yes");
        }

        if (schoolSearchCommand.getServices() != null) {
            q.filterAnyField(new SolrField[]{
                    SchoolFields.BEFORE_AFTER_CARE,
                    SchoolFields.TRANSPORTATION
            }, schoolSearchCommand.getServices());
        }
        /**
         * Change Done for GS-14144 Ends
         */



        if (schoolSearchCommand.getSpecialEdPrograms() != null) {
            q.filterAnyField(new SolrField[]{
                    SchoolFields.SPECIAL_ED_PROGRAMS,
                    SchoolFields.ELL_LEVEL
            }, schoolSearchCommand.getSpecialEdPrograms());
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
                gsRatings.add("7");
            }
            if (ArrayUtils.contains(ratingCategories, "high")) {
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
            q.query(Searching.cleanseSearchString(searchString));
        }
        else {
            q.setSpellCheckEnabled(false);
        }

        // apply sorting
        if (commandAndFields.getFieldSort() != null) {
            q.sort(commandAndFields.getFieldSort());
        }

        try {
            searchResultsPage = _gsSolrSearcher.search(q, SolrSchoolSearchResult.class, true);

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

    public ModelAndView handleCityBrowse(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model, boolean showAdvancedFilters) {
        // City Browse Specific: check valid city
        ModelAndView redirect = _cityBrowseHelper.checkForRedirectConditions(request, response, commandAndFields);
        if (redirect != null) {
            return redirect;
        }

        // TODO: can we clean up code by always putting city and model into model, or will this break the view?
        // City Browse And District Browse Specific:  Put City into model
        model.put(MODEL_CITY, commandAndFields.getCity());


        // Common: perform school search and add relevant data to the model
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = putSchoolSearchResultsIntoModel(commandAndFields, model);


        // Common: Search for nearby cities and place them into the model
        _cityBrowseHelper.putNearbyCitiesInModel(commandAndFields, model);


        // City Browse Specific: Ad GAM attributes for city browse
        _cityBrowseHelper.addGamAttributes(request, commandAndFields, searchResultsPage.getSearchResults(), showAdvancedFilters);


        // City Browse Specific:  Put rel canonical value into the model
        putRelCanonicalIntoModel(request, _cityBrowseHelper.getRelCanonical(commandAndFields), model);

        // City Browse Specific:  Use a city browse helper to calculate title and description and put them into model
        model.putAll(
                _cityBrowseHelper.getMetaData(commandAndFields)
        );


        // City Browse Specific: Use a city browse helper to calculate omniture page name and hierarchy and put them into model
        model.putAll(
                _cityBrowseHelper.getOmnitureHierarchyAndPageName(request, commandAndFields, searchResultsPage.getTotalResults())
        );

        // Common: put common omniture attributes into model
        // Must happen after search is complete
        model.putAll(
                commonSearchHelper.getCommonOmnitureAttributes(request, commandAndFields, searchResultsPage)
        );

        // GS-6971 - city id cookie
        PageHelper.setCityIdCookie(request, response, commandAndFields.getCity());

        // Common: Calculate the view name
        String viewName = determineViewName(commandAndFields.getSchoolSearchCommand(), searchResultsPage);

        model.put(MODEL_CITY_BROWSE, true);

        return new ModelAndView(viewName, model);
    }

    public ModelAndView handleDistrictBrowse(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model, boolean showAdvancedFilters) {

        // District Browse Specific: check valid city, valid district, wrong param combination, etc
        ModelAndView redirect = _districtBrowseHelper.checkForRedirectConditions(request, response, commandAndFields);
        if (redirect != null) {
            return redirect;
        }

        // TODO: can we clean up code by always putting city and model into model, or will this break the view?
        // City Browse And District Browse Specific:  Put City into model
        model.put(MODEL_CITY, commandAndFields.getCity());


        // District Browse Specific:  Put District into model
        model.put(MODEL_DISTRICT, commandAndFields.getDistrict());


        // Common: perform school search and add relevant data to the model
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = putSchoolSearchResultsIntoModel(commandAndFields, model);


        // Common: Search for nearby cities and place them into the model
        _districtBrowseHelper.putNearbyCitiesInModel(commandAndFields, model);


        // District Browse Specific: put GAM attributes for district browse into model
        _districtBrowseHelper.addGamAttributes(request, commandAndFields, searchResultsPage.getSearchResults(), showAdvancedFilters);

        // District Browse Specific:  Put rel canonical value into the model
        putRelCanonicalIntoModel(request, _districtBrowseHelper.getRelCanonical(commandAndFields), model);

        // District Browse Specific:  Use a district browse helper to calculate title and description and put them into model
        model.putAll(
                _districtBrowseHelper.getMetaData(commandAndFields)
        );


        // District Browse Specific: Use a district browse helper to calculate omniture page name and hierarchy and put them into model
        model.putAll(
                _districtBrowseHelper.getOmnitureHierarchyAndPageName(request, commandAndFields, searchResultsPage.getTotalResults())
        );


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        model.putAll(
                commonSearchHelper.getCommonOmnitureAttributes(request, commandAndFields, searchResultsPage)
        );


        // Common: Calculate the view name
        String viewName = determineViewName(commandAndFields.getSchoolSearchCommand(), searchResultsPage);

        model.put(MODEL_DISTRICT_BROWSE, true);

        return new ModelAndView(viewName, model);
    }

    private ModelAndView handleQueryStringAndNearbySearch(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model, boolean isNearbySearchByLocation, boolean showAdvancedFilters) {

        // QueryString Search Specific (not city browse and not district browse and no lat/lon)
        // if user did not enter search term (and this is not a nearby search), redirect to state browse
        ModelAndView redirect = _queryStringSearchHelper.checkForRedirectConditions(request, commandAndFields);
        if (redirect != null) {
            return redirect;
        }


        // Common: perform school search and add relevant data to the model
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = putSchoolSearchResultsIntoModel(commandAndFields, model);


        // QueryString Search Specific: Search for nearby cities and place them into the model
        List<ICitySearchResult> citySearchResults = _queryStringSearchHelper.putNearbyCitiesInModel(commandAndFields, model);


        // QueryString Search Specific: Now search for districts and place those into the model
        List<IDistrictSearchResult> districtSearchResults = _queryStringSearchHelper.searchForDistricts(commandAndFields);
        model.put(MODEL_DISTRICT_SEARCH_RESULTS, districtSearchResults);


        // Nearby Search and QueryString Search Specific: put GAM attributes for these flows into model
        _queryStringSearchHelper.addGamAttributes(request, commandAndFields, searchResultsPage.getSearchResults(), showAdvancedFilters);


        // QueryString Search Specific:  Put rel canonical value into the model
        UrlBuilder relCanonical = null;
        if (isNearbySearchByLocation) {
            City city = null;
            if (commandAndFields.getState() != null && commandAndFields.getSchoolSearchCommand() != null && commandAndFields.getSchoolSearchCommand().getCity() != null) {
                city = commandAndFields.getCity(commandAndFields.getState(), commandAndFields.getSchoolSearchCommand().getCity());
            }
            if (city != null) {
                relCanonical = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                        commandAndFields.getState(),
                        city.getName(),
                        SchoolType.getSetContainingOnlyLowestSchoolType(commandAndFields.getSchoolTypes()),
                        LevelCode.createLevelCode(commandAndFields.getGradeLevels()).getLowestNonPreSchoolLevelCode());
            } else if (commandAndFields.getState() != null) {
                relCanonical = new UrlBuilder(UrlBuilder.RESEARCH, commandAndFields.getState());
            }
        } else {
            relCanonical = _queryStringSearchHelper.getRelCanonical(commandAndFields.getSearchString() == null ?
                    commandAndFields.getSchoolSearchCommand().getLocationSearchString() : commandAndFields.getSearchString(), commandAndFields.getState(),
                    SchoolType.getSetContainingOnlyLowestSchoolType(commandAndFields.getSchoolTypes()),
                    LevelCode.createLevelCode(commandAndFields.getGradeLevels()).getLowestNonPreSchoolLevelCode(),
                    citySearchResults);
        }
        if (relCanonical != null) {
            model.put(MODEL_REL_CANONICAL, relCanonical.asFullUrlXml(request));
        }


        // Nearby Search and QueryString Search Specific
        // TODO: Split Nearby / QueryString Search?
        // TODO: Review this
        // QueryString Search Specific:  Use a district browse helper to calculate title and description and put them into model
        model.putAll(
                _queryStringSearchHelper.getMetaData(commandAndFields, request)
        );


        // QueryString Search Specific: Use a school search helper to calculate omniture page name and hierarchy and put them into model
        model.putAll(
            _queryStringSearchHelper.getOmnitureHierarchyAndPageName(request, commandAndFields, searchResultsPage.getTotalResults(), citySearchResults, districtSearchResults, searchResultsPage.isDidYouMeanResults())
        );


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        model.putAll(
                commonSearchHelper.getCommonOmnitureAttributes(request, commandAndFields, searchResultsPage)
        );


        // Common: Calculate the view name
        String viewName = determineViewName(commandAndFields.getSchoolSearchCommand(), searchResultsPage);


        // QueryString Search Specific: Put nearby search title prefix and establishment and search zip code into model
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

        model.put(MODEL_SEARCH_STRING, commandAndFields.getSchoolSearchCommand().getSearchString());

        return new ModelAndView(viewName, model);
    }


    protected SearchResultsPage<SolrSchoolSearchResult> putSchoolSearchResultsIntoModel(SchoolSearchCommandWithFields commandAndFields, Map<String, Object> model) {

        // Common: Perform a search. Search might find spelling suggestions and then run another search to see if the
        // spelling suggestion actually yieleded results. If so, record the "didYouMean" suggestion into the model
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = searchForSchools(commandAndFields);
        if (searchResultsPage.isDidYouMeanResults()) {
            model.put(MODEL_DID_YOU_MEAN, SpellChecking.getSearchSuggestion(commandAndFields.getSearchString(), searchResultsPage.getSpellCheckResponse()));
        }


        // Common: Put Page object which contains pagination-related info into the model
        model.put(MODEL_PAGE, new Page(
                commandAndFields.getRequestedPage().getValidatedOffset(SCHOOL_SEARCH_PAGINATION_CONFIG, searchResultsPage.getTotalResults()),
                commandAndFields.getRequestedPage().pageSize,
                searchResultsPage.getTotalResults())
        );


        // Common: Put info about the search resultset / result counts into the model
        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());

        return searchResultsPage;
    }

    private static void putRelCanonicalIntoModel(HttpServletRequest request, UrlBuilder relCanonical, Map<String,Object> model) {
        if (relCanonical != null) {
            model.put(MODEL_REL_CANONICAL, relCanonical.asFullUrlXml(request));

            String currentUrl = UrlUtil.getRequestURL(request);

            model.put(MODEL_CURRENT_URL_MATCHES_REL_CANONICAL, currentUrl.equals(relCanonical.asFullUrl(request)));
        } else {
            model.put(MODEL_CURRENT_URL_MATCHES_REL_CANONICAL, false);
        }
    }

    private SchoolSearchCommandWithFields createSchoolSearchCommandWithFields(SchoolSearchCommand schoolSearchCommand, DirectoryStructureUrlFields fields, Map nearbySearchInfo) {
        SchoolSearchCommandWithFields commandAndFields = new SchoolSearchCommandWithFields(schoolSearchCommand, fields, nearbySearchInfo);
        commandAndFields.setDistrictDao(_districtDao);
        commandAndFields.setGeoDao(_geoDao);
        commandAndFields.setHubCityMappingDao(_hubCityMappingDao);
        commandAndFields.setHubConfigDao(_hubConfigDao);
        return commandAndFields;
    }

    protected String getNearbySearchTitlePrefix(SchoolSearchCommand schoolSearchCommand) {
        String nearbySearchTitlePrefix = "Schools";
        /*if (schoolSearchCommand.hasGradeLevels()) {
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
        } */
        return nearbySearchTitlePrefix;
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
            if (searchResultsPage.getTotalResults() == 0) {
                return getNoResultsViewName();
            } else {
                return getViewName();
            }
        }
    }

    public boolean shouldIncludeFacebookFacepiles(HttpServletRequest request, SchoolSearchCommandWithFields commandWithFields) {
        boolean includeFacebook = false;
        State enabledState = State.CA;
        RequestInfo requestInfo = RequestInfo.getRequestInfo(request);

        if (commandWithFields.isCityBrowse() && enabledState.equals(commandWithFields.getState()) && commandWithFields.getCity() != null) {
            for (String city : FACEBOOK_FACEPILE_PILOT_CITIES) {
                if (city.equals(commandWithFields.getCity().getName())) {
                    includeFacebook = true;
                }
            }
        }

        if ((requestInfo.isDeveloperWorkstation() || requestInfo.isDevEnvironment())
                && "true".equals(request.getParameter("facebook_facepiles_enabled"))) {
            includeFacebook = true;
        }

        return includeFacebook;
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

    public String getMobileViewName() {
        return _mobileViewName;
    }

    public void setMobileViewName(String mobileViewName) {
        _mobileViewName = mobileViewName;
    }

    /** For unit tests */
    void setGsSolrSearcher(GsSolrSearcher gsSolrSearcher) {
        _gsSolrSearcher = gsSolrSearcher;
    }
}