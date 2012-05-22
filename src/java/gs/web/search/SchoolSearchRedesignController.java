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
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.*;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.fields.DocumentType;
import gs.data.search.fields.SchoolFields;
import gs.data.search.fields.SolrField;
import gs.data.search.filters.FilterFactory;
import gs.data.search.filters.FilterGroup;
import gs.data.search.filters.SchoolFilters;
import gs.data.search.services.CitySearchService;
import gs.data.search.services.DistrictSearchService;
import gs.data.search.services.SchoolSearchService;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import gs.data.util.CommunityUtil;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.pagination.Page;
import gs.web.pagination.RequestedPage;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;



public class SchoolSearchRedesignController extends SchoolSearchController {

    private static final Logger _log = Logger.getLogger(SchoolSearchRedesignController.class);

    @Override
    /*
     * TODO: this method needs to be refactored. first step: switch it to use  GsSolrQuery instead of SchoolSearchServiceSolrImpl
     */
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

        // OSP facet counts:
        Map<String,Map<String,Long>> facetCounts = searchResulsWithFacets.getAllFacetCounts();
        model.put("beforeAfterCareCounts", facetCounts.get(SchoolFields.BEFORE_AFTER_CARE.getName()));
        model.put("facetCounts", facetCounts);

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

        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(commandAndFields.getSchoolTypes()));

        if (commandAndFields.getLevelCode() != null) {
            model.put(MODEL_LEVEL_CODE, commandAndFields.getLevelCode().getCommaSeparatedString());
        }

        addPagingDataToModel(requestedPage.getValidatedOffset(SCHOOL_SEARCH_PAGINATION_CONFIG, searchResultsPage.getTotalResults()), requestedPage.pageSize, requestedPage.pageNumber, searchResultsPage.getTotalResults(), model);
        addGamAttributes(request, response, pageHelper, fieldConstraints, filterGroups, searchString, searchResultsPage.getSearchResults(), city, district);

        City localCity = (city != null ? city : commandAndFields.getCityFromSearchString());
        if (localCity != null) {
            LocalBoard localBoard = getLocalBoardDao().findByCityId(localCity.getId());
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

        if (schoolSearchCommand.isNearbySearchByLocation()) {
            model.put(MODEL_OMNITURE_ADDRESS_SEARCH, true);
        } else if (StringUtils.equals(request.getParameter("search_type"), "1")) {
            model.put(MODEL_OMNITURE_NAME_SEARCH, true);
        }
        if ((commandAndFields.isCityBrowse() || commandAndFields.isDistrictBrowse()) && !commandAndFields.isAjaxRequest()) {
            model.put("hasMobileView", true);
        }
        return new ModelAndView(getViewName(schoolSearchCommand, searchResultsPage), model);
    }

}
