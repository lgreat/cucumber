package gs.web.search;

import gs.data.search.*;
import gs.data.search.beans.CitySearchResult;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.fields.CityFields;
import gs.data.search.services.CitySearchService;
import gs.data.search.services.DistrictSearchService;
import gs.data.state.State;
import gs.web.pagination.RequestedPage;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("schoolSearchHelper")
public class SchoolSearchHelper extends AbstractSchoolSearchHelper {
    private static final Logger _log = Logger.getLogger(SchoolSearchHelper.class);

    @Autowired
    SearchAdHelper _searchAdHelper;

    @Autowired
    private NearbyCitiesController _nearbyCitiesController;

    @Autowired
    private CitySearchService _citySearchService;

    @Autowired
    private GsSolrSearcher _gsSolrSearcher;

    @Autowired
    private DistrictSearchService _districtSearchService;


    public static final String MODEL_OMNITURE_PAGE_NAME = "omniturePageName";
    public static final String MODEL_OMNITURE_HIERARCHY = "omnitureHierarchy";


    protected static String getOmniturePageName(HttpServletRequest request, int currentPage, int totalResults,
                                                boolean foundDidYouMeanSuggestions) {
        String pageName = "";

        String paramMap = request.getParameter("map");

        if (totalResults > 0) {
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
                                                 List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults) {
        String hierarchy = "";

        boolean hasCityResults = (citySearchResults != null && citySearchResults.size() > 0);
        boolean hasDistrictResults = (districtSearchResults != null && districtSearchResults.size() > 0);

        if (totalResults > 0) {
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


    public Map<String,Object> getOmnitureHierarchyAndPageName(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, int totalResults, List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults, boolean isDidYouMeanResults) {
        // QueryString Search Specific: Use a school search helper to calculate omniture page name and hierarchy and put them into model
        Map<String,Object> model = new HashMap<String,Object>();
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        String omniturePageName = getOmniturePageName(request, requestedPage.pageNumber, totalResults, isDidYouMeanResults);
        String omnitureHierarchy = getOmnitureHierarchy(requestedPage.pageNumber, totalResults, citySearchResults, districtSearchResults);
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);
        return model;
    }

    public void addGamAttributes(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, List<SolrSchoolSearchResult> schoolResults) {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        String searchString = commandAndFields.getSearchString();

        // GS-10448 - search results
        if (StringUtils.isNotBlank(searchString) && schoolResults != null) {
            _searchAdHelper.addSearchResultsAdKeywords(pageHelper, schoolResults);
        }

        // GS-10003 - school type
        _searchAdHelper.addSchoolTypeAdKeywords(pageHelper, commandAndFields.getSchoolTypes());

        // GS-6875 - level
        _searchAdHelper.addLevelCodeAdKeywords(pageHelper, commandAndFields.getGradeLevels());

        // GS-10642 - query, GS-9323 zip code
        if (StringUtils.isNotBlank(searchString)) {
            // GS-10642 - query
            _searchAdHelper.addSearchQueryAdKeywords(pageHelper, searchString);

            // GS-9323 zip code
            _searchAdHelper.addZipCodeAdKeyword(pageHelper, searchString);
        }

        // GS-11511 - nearby search by zip code
        _searchAdHelper.addNearbySearchInfoKeywords(pageHelper, request);
    }

    public ModelAndView checkForRedirectConditions(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields) {
        // QueryString Search Specific (not city browse and not district browse and no lat/lon)
        // if user did not enter search term (and this is not a nearby search), redirect to state browse
        if (!commandAndFields.isNearbySearch() && commandAndFields.isSearch() && StringUtils.isBlank(commandAndFields.getSearchString())) {
            return stateBrowseRedirect(request, SessionContextUtil.getSessionContext(request));
        }
        return null;
    }

    protected ModelAndView stateBrowseRedirect(HttpServletRequest request, SessionContext sessionContext) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESEARCH, sessionContext.getState());
        final String url = builder.asSiteRelative(request);
        final RedirectView view = new RedirectView(url, false);
        return new ModelAndView(view);
    }

    //-------------------------------------------------------------------------
    // rel canonical
    //-------------------------------------------------------------------------
    protected String getRelCanonical(HttpServletRequest request, String searchString, State state, List<ICitySearchResult> citySearchResults) {
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

    protected Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandAndFields, HttpServletRequest request) {
        Map nearbySearchInfo = (Map) request.getAttribute("nearbySearchInfo");

        String titleString = null;

        if (commandAndFields.isNearbySearch() && nearbySearchInfo != null) {
            Object zipCodeObj = nearbySearchInfo.get("zipCode");
            if (zipCodeObj != null && zipCodeObj instanceof String) {
                titleString = (String) zipCodeObj;
            }
        } else {
            titleString = commandAndFields.getSearchString();
        }

        Map<String,Object> metaData = new HashMap<String,Object>();
        metaData.put(MODEL_TITLE, getTitle(titleString));

        return metaData;
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

    public List<ICitySearchResult> putNearbyCitiesInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

        if (commandAndFields.isNearbySearch()) {
            citySearchResults = ListUtils.typedList(
                    _nearbyCitiesController.getNearbyCities(
                            commandAndFields.getLatitude(),
                            commandAndFields.getLongitude(),
                            SchoolSearchHelper.NEARBY_CITIES_RADIUS,
                            SchoolSearchHelper.NEARBY_CITIES_COUNT
                    ),
                    ICitySearchResult.class
            );
        } else if (commandAndFields.getSearchString() != null) {
            citySearchResults = ListUtils.typedList(
                    searchForCities(commandAndFields.getState(), commandAndFields.getSearchString()),
                    ICitySearchResult.class
            );
        }

        model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        return citySearchResults;
    }


    public List<CitySearchResult> searchForCities(State state, String searchString) {
        List<CitySearchResult> citySearchResults = new ArrayList<CitySearchResult>();

        if (state == null || searchString == null) {
            //don't try to find cities without a state
            return citySearchResults;
        }

        // when searching for "anchorage, ak", do not search for cities matching "ak"
        // primarily an issue with city autocomplete as implemented for GS-11928 Find a School by location
        if (StringUtils.endsWithIgnoreCase(searchString, " " + state.getAbbreviation()) ||
                StringUtils.endsWithIgnoreCase(searchString, "," + state.getAbbreviation())) {
            searchString = StringUtils.substring(searchString, 0, searchString.length()-2);
        }

        searchString = Searching.cleanseSearchString(searchString);

        GsSolrQuery gsSolrQuery = createCityGsSolrQuery();
        gsSolrQuery.filter(CityFields.STATE, state.getAbbreviationLowerCase());
        gsSolrQuery.query(searchString);
        gsSolrQuery.page(0, NEARBY_CITIES_COUNT);
        citySearchResults = _gsSolrSearcher.simpleSearch(gsSolrQuery, CitySearchResult.class);

        return citySearchResults;
    }

    public List<IDistrictSearchResult> searchForDistricts(SchoolSearchCommandWithFields commandAndFields) {
        String searchString = commandAndFields.getSearchString() != null ? commandAndFields.getSearchString() :
                commandAndFields.getSchoolSearchCommand().getLocationSearchString();
        State state = commandAndFields.getState();
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

        if (state == null) {
            //don't try to find districts without a state
            return districtSearchResults;
        }

        Map<IFieldConstraint, String> districtConstraints = new HashMap<IFieldConstraint, String>();
        districtConstraints.put(DistrictSearchFieldConstraints.STATE, state.getAbbreviationLowerCase());

        try {
            if (searchString != null) {
                SearchResultsPage<IDistrictSearchResult> districtPage = _districtSearchService.getNonCharterDistrictsNear(commandAndFields.getLatitude(),
                        commandAndFields.getLongitude(), 50, searchString, null, 0, DISTRICTS_COUNT);
                districtSearchResults = districtPage.getSearchResults();
            }
        } catch (SearchException ex) {
            _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", ex);
        }

        return districtSearchResults;
    }

    GsSolrQuery createCityGsSolrQuery() {
        GsSolrQuery gsSolrQuery = new GsSolrQuery(QueryType.CITY_SEARCH);
        gsSolrQuery.getSolrQuery().add("df","city_name"); // make city_name the default field to search on
        return gsSolrQuery;
    }

}
