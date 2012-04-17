package gs.web.search;

import gs.data.geo.ICounty;
import gs.data.school.LevelCode;
import gs.data.search.FieldConstraint;
import gs.data.search.FieldSort;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.ISchoolSearchResult;
import gs.data.search.filters.FilterGroup;
import gs.data.search.services.SchoolSearchService;
import gs.data.state.State;
import gs.web.pagination.Page;
import gs.web.pagination.RequestedPage;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class SchoolSearchMobileController extends SchoolSearchController implements IDirectoryStructureUrlController {

    private static final Logger _log = Logger.getLogger(SchoolSearchController.class);

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("schoolSearchCommand", schoolSearchCommand);


        /*
        commenting out for mobile
        if (user != null) {
            Set<FavoriteSchool> mslSchools = user.getFavoriteSchools();
            model.put(MODEL_MSL_SCHOOLS, mslSchools);
        }*/

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


        ModelAndView modelAndView;
        if (commandAndFields.isCityBrowse()) {
            modelAndView = handleCityBrowse(request, response, commandAndFields, model);
        } else if (commandAndFields.isDistrictBrowse()) {
            modelAndView = handleDistrictBrowse(request, response, commandAndFields, model);
        } else {
            modelAndView = handleQueryStringAndNearbySearch(request, response, commandAndFields, model);
        }


        // Common: Set a cookie to record that a search has occurred;
        PageHelper.setHasSearchedCookie(request, response);

        String format = request.getParameter("format");
        if (format != null && format.equals("json")) {
            response.setContentType("application/json");
            // patch the model for now to remove data we don't want to send with json response.
            // TODO: skip logic not necessary for ajax response
            ModelAndView ajax = adaptModelForAjax(modelAndView);
            return ajax;
        }

        return modelAndView;
    }

    // TODO: this is not very good, since we're still doing tons of unnecessary work for ajax calls
    // Do more refactoring on this controller to allow an easy-to-maintain separate execution path for ajax
    // Or perhaps a different controller that reuses helper methods
    protected ModelAndView adaptModelForAjax(ModelAndView modelAndView) {
        Map<String,Object> existingModel = modelAndView.getModel();
        Map<String,Object> ajaxModel = new HashMap<String,Object>();
        ajaxModel.put(MODEL_TOTAL_RESULTS, existingModel.get(MODEL_TOTAL_RESULTS));
        List<ISchoolSearchResult> schoolSearchResults = (List<ISchoolSearchResult>)existingModel.get(MODEL_SCHOOL_SEARCH_RESULTS);
        if (schoolSearchResults != null) {
            List<SchoolSearchResultAjaxView> schoolSearchResultsAjax = new ArrayList<SchoolSearchResultAjaxView>(schoolSearchResults.size());
            for (ISchoolSearchResult result : schoolSearchResults) {
                schoolSearchResultsAjax.add(new SchoolSearchResultAjaxView(result));
            }
            ajaxModel.put(MODEL_SCHOOL_SEARCH_RESULTS, schoolSearchResultsAjax);
            Page page = (Page) existingModel.get(MODEL_PAGE);
            ajaxModel.put(MODEL_PAGE, page.getMap());
        }

        return new ModelAndView("json", ajaxModel);
    }

    private ModelAndView handleQueryStringAndNearbySearch(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        // TODO: make these spring-managed singleton beans
        CityBrowseHelper cityBrowseHelper = new CityBrowseHelper(commandAndFields);
        DistrictBrowseHelper districtBrowseHelper = new DistrictBrowseHelper(commandAndFields);


        // QueryString Search Specific (not city browse and not district browse and no lat/lon)
        // if user did not enter search term (and this is not a nearby search), redirect to state browse
        ModelAndView redirect = checkForQueryStringSearchRedirectConditions(request, commandAndFields);
        if (redirect != null) {
            return redirect;
        }


        // Common: Add a bunch of model attributes about the request information
        // TODO: make this method not require HttpServletRequest
        addSearchRequestInfoToModel(request, model, commandAndFields);


        // Common: perform school, city, and district searches and add relevant data to the model
        SchoolCityDistrictSearchSummary summary = handleSchoolCityDistrictSearch(commandAndFields, model);


        // Common: Call superclass method to put tons of GAM attributes into the model
        // TODO: Refactor this method into multiple flow-specific methods
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        addGamAttributes(request, response, pageHelper, summary.fieldConstraints, summary.filterGroups, commandAndFields.getSearchString(), summary.searchResultsPage.getSearchResults(), commandAndFields.getCity(), commandAndFields.getDistrict());


        // QueryString Search Specific:  Put rel canonical value into the model
        // TODO: review this to see if logic is correct
        putRelCanonicalIntoModel(request, model, commandAndFields, cityBrowseHelper, districtBrowseHelper, summary.citySearchResults);


        // Nearby Search and QueryString Search Specific
        // TODO: Split Nearby / QueryString Search?
        // TODO: Review this
        Map nearbySearchInfo = (Map) request.getAttribute("nearbySearchInfo");
        putMetaDataInModel(model, commandAndFields, nearbySearchInfo);


         // QueryString Search Specific: Use a city browse helper to calculate omniture page name and hierarchy and put them into model
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        boolean foundDidYouMeanSuggestions = (summary.searchResultsPage.isDidYouMeanResults());
        String omniturePageName = getOmniturePageName(request, requestedPage.pageNumber, summary.searchResultsPage.getTotalResults(), foundDidYouMeanSuggestions);
        String omnitureHierarchy = getOmnitureHierarchy(requestedPage.pageNumber, summary.searchResultsPage.getTotalResults(), summary.citySearchResults, summary.districtSearchResults);
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        putCommonOmnitureAttributesInModel(request, model, commandAndFields, summary.searchResultsPage);


        // Common: Calculate the view name
        String viewName = determineViewName(request, commandAndFields, summary.searchResultsPage);


        return new ModelAndView(viewName, model);
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


        // Common: Add a bunch of model attributes about the request information
        // TODO: make this method not require HttpServletRequest
        addSearchRequestInfoToModel(request, model, commandAndFields);


        // Common: perform school, city, and district searches and add relevant data to the model
        SchoolCityDistrictSearchSummary summary = handleSchoolCityDistrictSearch(commandAndFields, model);


        // Common: Call superclass method to put tons of GAM attributes into the model
        // TODO: Refactor this method into multiple flow-specific methods
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        addGamAttributes(request, response, pageHelper, summary.fieldConstraints, summary.filterGroups, commandAndFields.getSearchString(), summary.searchResultsPage.getSearchResults(), commandAndFields.getCity(), commandAndFields.getDistrict());


        // City Browse Specific:  Put rel canonical value into the model
        putCityBrowseRelCanonicalIntoModel(request, model, commandAndFields);


        // City Browse Specific:  Use a city browse helper to calculate title and description and put them into model
       // TODO: Make CityBrowseHelper a spring singleton bean
        CityBrowseHelper cityBrowseHelper = new CityBrowseHelper(commandAndFields);
        model.putAll(cityBrowseHelper.getMetaData());


        // City Browse Specific: Use a city browse helper to calculate omniture page name and hierarchy and put them into model
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        String omniturePageName = cityBrowseHelper.getOmniturePageName(request, requestedPage.pageNumber);
        String omnitureHierarchy = cityBrowseHelper.getOmnitureHierarchy(requestedPage.pageNumber, summary.searchResultsPage.getTotalResults());
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        putCommonOmnitureAttributesInModel(request, model, commandAndFields, summary.searchResultsPage);


        // Common: Calculate the view name
        String viewName = determineViewName(request, commandAndFields, summary.searchResultsPage);

        return new ModelAndView(viewName, model);
    }



    public ModelAndView handleDistrictBrowse(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        SchoolSearchCommand schoolSearchCommand = commandAndFields.getSchoolSearchCommand();

        // District Browse Specific: check valid city, valid district, wrong param combination, etc
        ModelAndView redirect = checkDistrictBrowseForRedirectConditions(request, response, commandAndFields);
        if (redirect != null) {
            return redirect;
        }

        // TODO: can we clean up code by always putting city and model into model, or will this break the view?
        // City Browse And District Browse Specific:  Put City into model
        model.put(MODEL_CITY, commandAndFields.getCity());


        // District Browse Specific:  Put District into model
        model.put(MODEL_DISTRICT, commandAndFields.getDistrict());


        // Common: Add a bunch of model attributes about the request information
        // TODO: make this method not require HttpServletRequest
        addSearchRequestInfoToModel(request, model, commandAndFields);


        // Common: perform school, city, and district searches and add relevant data to the model
        SchoolCityDistrictSearchSummary summary = handleSchoolCityDistrictSearch(commandAndFields, model);


        // Common: Call superclass method to put tons of GAM attributes into the model
        // TODO: Refactor this method into multiple flow-specific methods
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        addGamAttributes(request, response, pageHelper, summary.fieldConstraints, summary.filterGroups, commandAndFields.getSearchString(), summary.searchResultsPage.getSearchResults(), commandAndFields.getCity(), commandAndFields.getDistrict());


        // District Browse Specific:  Put rel canonical value into the model
        putDistrictBrowseRelCanonicalIntoModel(request, model, commandAndFields);


        // District Browse Specific:  Use a district browse helper to calculate title and description and put them into model
       // TODO: Make CityBrowseHelper a spring singleton bean
        DistrictBrowseHelper districtBrowseHelper = new DistrictBrowseHelper(commandAndFields);
        model.putAll(districtBrowseHelper.getMetaData());


        // District Browse Specific: Use a district browse helper to calculate omniture page name and hierarchy and put them into model
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        String omniturePageName = districtBrowseHelper.getOmniturePageName(request, requestedPage.pageNumber);
        String omnitureHierarchy = districtBrowseHelper.getOmnitureHierarchy(requestedPage.pageNumber, summary.searchResultsPage.getTotalResults());
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);


        // Common: put common omniture attributes into model
        // Must happen after search is complete
        putCommonOmnitureAttributesInModel(request, model, commandAndFields, summary.searchResultsPage);


        // Common: Calculate the view name
        String viewName = determineViewName(request, commandAndFields, summary.searchResultsPage);

        return new ModelAndView(viewName, model);
    }

    protected SchoolCityDistrictSearchSummary handleSchoolCityDistrictSearch(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        SchoolSearchCommand schoolSearchCommand = commandAndFields.getSchoolSearchCommand();

        // Common: calculate the field "constraints" a.k.a. filters that will be added to the search query
        // TODO: Get rid of fieldConstraints and use GsSolrSearcher redirectly. Create a GsSolrQuery factory for
        // common or complicated queries
        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(commandAndFields.getState(), commandAndFields.getCity(), commandAndFields.getDistrict());
        List<FilterGroup> filterGroups = createFilterGroups(commandAndFields);


        // Common: Use request info to determine the field to sort by, put it into the model, and return it
        FieldSort sort = commandAndFields.getFieldSort();
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());


        // Common: Perform a search. Search might find spelling suggestions and then run another search to see if the
        // spelling suggestion actually yieleded results. If so, record the "didYouMean" suggestion into the model
        SearchResultsPage<ISchoolSearchResult> searchResultsPage = searchForSchools(schoolSearchCommand, commandAndFields.getState(), fieldConstraints, filterGroups, sort);
        model.put(MODEL_DID_YOU_MEAN, searchResultsPage.getDidYouMeanQueryString());


        // Common: Search for nearby cities and place them into the model
        List<ICitySearchResult> citySearchResults = putNearbyCitiesInModel(commandAndFields, model);


        // Common: Now search for nearby districts and place those into the model
        List<IDistrictSearchResult> districtSearchResults = putNearbyDistrictsInModel(commandAndFields, model);


        // Common: Put pagination-related numbers into the model
        addPagingDataToModel(commandAndFields.getRequestedPage().getValidatedOffset(SCHOOL_SEARCH_PAGINATION_CONFIG, searchResultsPage.getTotalResults()), commandAndFields.getRequestedPage().pageSize, commandAndFields.getRequestedPage().pageNumber, searchResultsPage.getTotalResults(), model);


        // Common: Put info about the search resultset / result counts into the model
        putSearchResultInfoIntoModel(model, searchResultsPage);

        return new SchoolCityDistrictSearchSummary(fieldConstraints, filterGroups, sort, searchResultsPage, citySearchResults, districtSearchResults);
    }

    protected class SchoolCityDistrictSearchSummary {
        public final Map<FieldConstraint,String> fieldConstraints;
        public final List<FilterGroup> filterGroups;
        public final FieldSort fieldSort;

        public final SearchResultsPage<ISchoolSearchResult> searchResultsPage;
        public final List<ICitySearchResult> citySearchResults;
        public final List<IDistrictSearchResult> districtSearchResults;

        public SchoolCityDistrictSearchSummary(Map<FieldConstraint, String> fieldConstraints, List<FilterGroup> filterGroups, FieldSort fieldSort, SearchResultsPage<ISchoolSearchResult> searchResultsPage, List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults) {
            this.fieldConstraints = fieldConstraints;
            this.filterGroups = filterGroups;
            this.fieldSort = fieldSort;
            this.citySearchResults = citySearchResults;
            this.districtSearchResults = districtSearchResults;
            this.searchResultsPage = searchResultsPage;
        }
    }

    protected ModelAndView checkCityBrowseForRedirectConditions(HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields) {
        // City Browse and District Browse Specific:  We're in a city browse or district browse page, so get the city
        // from the URL. If it's not a real city then 404. Otherwise add city to the model
        if (commandAndFields.getCityFromUrl() == null) {
            return redirectTo404(response);
        }

        return null;
    }

    protected ModelAndView checkDistrictBrowseForRedirectConditions(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields) {
        // City Browse and District Browse Specific:  We're in a city browse or district browse page, so get the city
        // from the URL. If it's not a real city then 404. Otherwise add city to the model
        if (commandAndFields.getCityFromUrl() == null) {
            return redirectTo404(response);
        }


        // District Browse Specific:  We're in a district browse page, so get the district from the URL.
        // If it's not a real city then 404. Otherwise add district to the model
        if (commandAndFields.getDistrict() == null) {
            return redirectTo404(response);
        }


        // District Browse Specific:
        // if district browse *and* lc parameter was specified, 301-redirect to use directory-structure schools label instead of lc parameter
        String lc = request.getParameter("lc");
        if (StringUtils.isNotBlank(lc) && !commandAndFields.isAjaxRequest()) {
            LevelCode levelCode = LevelCode.createLevelCode(lc);
            UrlBuilder urlBuilder = new UrlBuilder(commandAndFields.getDistrict(), levelCode, UrlBuilder.SCHOOLS_IN_DISTRICT);
            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
        }

        return null;
    }

    public ModelAndView checkForQueryStringSearchRedirectConditions(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields) {
        // QueryString Search Specific (not city browse and not district browse and no lat/lon)
        // if user did not enter search term (and this is not a nearby search), redirect to state browse
        if (!commandAndFields.isNearbySearch() && commandAndFields.isSearch() && StringUtils.isBlank(commandAndFields.getSearchString())) {
            return stateBrowseRedirect(request, SessionContextUtil.getSessionContext(request));
        }
        return null;
    }

    private SchoolSearchCommandWithFields createSchoolSearchCommandWithFields(SchoolSearchCommand schoolSearchCommand, DirectoryStructureUrlFields fields, Map nearbySearchInfo) {
        SchoolSearchCommandWithFields commandAndFields = new SchoolSearchCommandWithFields(schoolSearchCommand, fields, nearbySearchInfo);
        commandAndFields.setDistrictDao(getDistrictDao());
        commandAndFields.setGeoDao(getGeoDao());
        return commandAndFields;
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

    private String determineViewName(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, SearchResultsPage<ISchoolSearchResult> searchResultsPage) {

        // Determine View name
        String viewName;
        if (searchResultsPage.getTotalResults() == 0) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.HOME);
            builder.addParameter("noResults","true");
            if (commandAndFields.isSearch()) {
                builder.addParameter("searchString",commandAndFields.getSearchString());
            }
            viewName = "redirect:" + builder.asSiteRelative(request);
        } else {
            viewName = getMobileViewName();
        }
        return viewName;
    }

    private void putSearchResultInfoIntoModel(Map<String, Object> model, SearchResultsPage<ISchoolSearchResult> searchResultsPage) {
        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());
        model.put(MODEL_TOTAL_RESULTS, searchResultsPage.getTotalResults());
    }

    protected void putRelCanonicalIntoModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields, CityBrowseHelper cityBrowseHelper, DistrictBrowseHelper districtBrowseHelper, List<ICitySearchResult> citySearchResults) {

        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (commandAndFields.getState() != null) {
            if (StringUtils.isNotBlank(commandAndFields.getSearchString())) {
                relCanonicalUrl = getRelCanonicalForSearch(request, commandAndFields.getSearchString(), commandAndFields.getState(), citySearchResults);
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

    protected void putCityBrowseRelCanonicalIntoModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields) {
        CityBrowseHelper cityBrowseHelper = new CityBrowseHelper(commandAndFields);

        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (commandAndFields.getState() != null) {
            relCanonicalUrl = cityBrowseHelper.getRelCanonical(request);

            if (relCanonicalUrl != null) {
                model.put(MODEL_REL_CANONICAL, relCanonicalUrl);
            }
        }
    }

    protected void putDistrictBrowseRelCanonicalIntoModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields) {
        DistrictBrowseHelper districtBrowseHelper = new DistrictBrowseHelper(commandAndFields);

        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (commandAndFields.getState() != null) {
            relCanonicalUrl = districtBrowseHelper.getRelCanonical(request);

            if (relCanonicalUrl != null) {
                model.put(MODEL_REL_CANONICAL, relCanonicalUrl);
            }
        }
    }

    protected void putMetaDataInModel(Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields, Map nearbySearchInfo) {
        if (commandAndFields.isNearbySearch()) {
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
    }

    public List<ICitySearchResult> putNearbyCitiesInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

        NearbyCitiesSearch nearbyCitiesFacade = new NearbyCitiesSearch(commandAndFields);
        nearbyCitiesFacade.setCitySearchService(getCitySearchService());
        if (commandAndFields.isCityBrowse() || commandAndFields.isDistrictBrowse()) {
            citySearchResults = nearbyCitiesFacade.getNearbyCities();
           //districtSearchResults = nearbyDistrictsFacade.getNearbyDistricts(); commented out until we figure out why district lat/lons are inaccurate
        } else if (commandAndFields.isNearbySearchByLocation()) {
            citySearchResults = nearbyCitiesFacade.getNearbyCitiesByLatLon();
        } else if (commandAndFields.getSearchString() != null) {
            citySearchResults = nearbyCitiesFacade.searchForCities();
        }
        model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        return citySearchResults;
    }

    public List<IDistrictSearchResult> putNearbyDistrictsInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

        NearbyDistrictsSearch nearbyDistrictsFacade = new NearbyDistrictsSearch(commandAndFields);
        nearbyDistrictsFacade.setDistrictSearchService(getDistrictSearchService());
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

    protected SearchResultsPage<ISchoolSearchResult> searchForSchools(SchoolSearchCommand schoolSearchCommand, State state, Map<FieldConstraint, String> fieldConstraints, List<FilterGroup> filterGroups, FieldSort sort) {
        RequestedPage requestedPage = schoolSearchCommand.getRequestedPage();
        SearchResultsPage<ISchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<ISchoolSearchResult>());
        String searchString = schoolSearchCommand.getSearchString();

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
                        requestedPage.pageSize,
                        (!schoolSearchCommand.isNearbySearch())
                );

                if (searchResultsPage.isDidYouMeanResults()) {
                    // adapting old existing logic to new code: If the search results we got back are the result
                    // of an automatic second search using a Solr spelling suggestion, then we want it to appear
                    // that we never received any results so the site will display the No Results page with the
                    // "did you mean" suggestion
                    searchResultsPage.setTotalResults(0);
                    searchResultsPage.setSearchResults(new ArrayList<ISchoolSearchResult>());
                }

            } catch (SearchException e) {
                _log.debug("something when wrong when attempting to use SchoolSearchService. Eating exception", e);
            }
        }
        return searchResultsPage;
    }

    private void putCommonOmnitureAttributesInModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields, SearchResultsPage<ISchoolSearchResult> searchResultsPage) {
        String[] schoolSearchTypes = commandAndFields.getSchoolTypes();
        boolean sortChanged = commandAndFields.getSchoolSearchCommand().isSortChanged();
        String omnitureQuery = commandAndFields.isSearch()? getOmnitureQuery(commandAndFields.getSearchString()) : null;
        model.put(MODEL_OMNITURE_QUERY, omnitureQuery);
        model.put(MODEL_OMNITURE_SCHOOL_TYPE, getOmnitureSchoolType(schoolSearchTypes));
        model.put(MODEL_OMNITURE_SCHOOL_LEVEL, getOmnitureSchoolLevel(commandAndFields.getLevelCode()));
        model.put(MODEL_OMNITURE_SORT_SELECTION, getOmnitureSortSelection(sortChanged ? commandAndFields.getFieldSort() : null));
        model.put(MODEL_OMNITURE_RESULTS_PER_PAGE, getOmnitureResultsPerPage(commandAndFields.getRequestedPage().pageSize, searchResultsPage.getTotalResults()));
        model.put(MODEL_OMNITURE_ADDRESS_SEARCH, false);
        model.put(MODEL_OMNITURE_NAME_SEARCH, false);

        if (commandAndFields.isNearbySearchByLocation()) {
            model.put(MODEL_OMNITURE_ADDRESS_SEARCH, true);
        } else if (StringUtils.equals(request.getParameter("search_type"), "1")) {
            model.put(MODEL_OMNITURE_NAME_SEARCH, true);
        }
    }


    private void addSearchRequestInfoToModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields) {
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

    private String getNearbySearchTitlePrefix(SchoolSearchCommand schoolSearchCommand) {
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


}
