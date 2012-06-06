package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.search.*;
import gs.data.search.beans.CitySearchResult;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.services.CitySearchService;
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


    public Map<String,Object> getOmnitureHierarchyAndPageName(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, SchoolSearchController2012.SchoolCityDistrictSearchSummary summary) {
        // QueryString Search Specific: Use a school search helper to calculate omniture page name and hierarchy and put them into model
        Map<String,Object> model = new HashMap<String,Object>();
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        boolean foundDidYouMeanSuggestions = (summary.searchResultsPage.isDidYouMeanResults());
        String omniturePageName = getOmniturePageName(request, requestedPage.pageNumber, summary.searchResultsPage.getTotalResults(), foundDidYouMeanSuggestions);
        String omnitureHierarchy = getOmnitureHierarchy(requestedPage.pageNumber, summary.searchResultsPage.getTotalResults(), summary.citySearchResults, summary.districtSearchResults);
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

    public ModelAndView checkForQueryStringSearchRedirectConditions(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields) {
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

    public List<CitySearchResult> getNearbyCitiesByLatLon(SchoolSearchCommandWithFields commandAndFields) {
        int DEFAULT_RADIUS = 50;
        int pageSize = 33;
        Float lat = commandAndFields.getLatitude();
        Float lon = commandAndFields.getLongitude();
        return _nearbyCitiesController.getNearbyCities(lat, lon, DEFAULT_RADIUS, pageSize);
    }

    /**
     * Try to decouple this class from "SchoolSearchCommandWithFields" if SchoolSearchController changes.
     * Method does not actually look for "nearby" cities... just cities matching a string
     *
     * @return
     */
    public List<ICitySearchResult> searchForCities(SchoolSearchCommandWithFields commandAndFields) {
        String searchString = commandAndFields.getSearchString();
        State state = commandAndFields.getState();
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

    public CitySearchService getCitySearchService() {
        return _citySearchService;
    }

    public void setCitySearchService(CitySearchService citySearchService) {
        _citySearchService = citySearchService;
    }
}
