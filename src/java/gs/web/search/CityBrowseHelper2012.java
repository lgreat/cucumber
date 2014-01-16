package gs.web.search;

import gs.data.geo.City;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.state.State;
import gs.web.pagination.RequestedPage;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView302;
import gs.web.util.UrlBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Component("cityBrowseHelper")
public class CityBrowseHelper2012 extends AbstractBrowseHelper {

    @Autowired
    SearchAdHelper _searchAdHelper;

    @Autowired
    NearbyCitiesController _nearbyCitiesController;

    Logger _log = Logger.getLogger(CityBrowseHelper2012.class);

    public Map<String, Object> getMetaData(SchoolSearchCommandWithFields commandWithFields){
        return getMetaData(commandWithFields, false);
    }

    public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields, boolean mobile) {
        City city = commandWithFields.getCityFromUrl();
        String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
        LevelCode levelCode = commandWithFields.getLevelCode();

        Map<String,Object> model = new HashMap<String,Object>();
        model.put(MODEL_TITLE, getTitle(city.getDisplayName(), city.getState(), levelCode, schoolSearchTypes, mobile));
        model.put(MODEL_META_DESCRIPTION, getMetaDescription(city, levelCode, schoolSearchTypes));
        return model;
    }

    public static String getTitle(String cityDisplayName, State cityState, LevelCode levelCode, String[] schoolType) {
        return getTitle(cityDisplayName, cityState, levelCode, schoolType, false);
    }

    public static String getTitle(String cityDisplayName, State cityState, LevelCode levelCode, String[] schoolType, boolean mobile) {
        StringBuffer sb = new StringBuffer();
        if (mobile) {
            sb.append("Search Results for ");
            if ("Washington, DC".equals(cityDisplayName)) {
                sb.append(cityDisplayName);
            } else {
                sb.append(cityDisplayName).append(", ").append(cityState.getAbbreviation());
            }
        } else {
            sb.append(cityDisplayName);
        }

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
        } else if (!mobile) {
            sb.append(" Schools");
        }

        if (!mobile) {
            sb.append(" - ");
            if ("Washington, DC".equals(cityDisplayName)) {
                sb.append(cityDisplayName);
            } else {
                sb.append(cityDisplayName).append(", ").append(cityState.getAbbreviation());
            }
            sb.append(" | GreatSchools");
        }
        return sb.toString();
    }

    protected UrlBuilder getRelCanonical(SchoolSearchCommandWithFields commandWithFields) {
        City city = commandWithFields.getCityFromUrl();
        State state = commandWithFields.getState();
        
        if (city == null || state == null) {
            throw new IllegalArgumentException("request, city, and state are required and cannot be null");
        }

        return new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                state,
                city.getName(),
                SchoolType.getSetContainingOnlyLowestSchoolType(commandWithFields.getSchoolTypes()),
                LevelCode.createLevelCode(commandWithFields.getGradeLevels()).getLowestNonPreSchoolLevelCode(),
                commandWithFields.getRequestedPage().offset);
    }

    public String getOmnitureHierarchy(int currentPage, int totalResults) {
        String hierarchy = "Search,Schools,City," + currentPage;

        return hierarchy;
    }

    public String getOmnitureMapHierarchy(int currentPage, int totalResults) {
        String hierarchy = "Search,Schools,City,Map" + currentPage;

        return hierarchy;
    }

    protected String getMetaDescription(City city, LevelCode levelCode, String[] schoolTypes) {
        return calcMetaDesc(null, city.getDisplayName(), city.getState(), levelCode, schoolTypes);
    }

    public String calcMetaDesc(String districtDisplayName, String cityDisplayName,
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
                // cityWithModifier.append(cityDisplayName).append(state.abbr).append(commaspace).append(modifier)
                cityWithModifier.append(cityDisplayName).append(", ").
                        append(state.getAbbreviation()).append(" ").append(modifier);
                sb.append("View and map all ").append(cityWithModifier).
                        append("schools. Plus, compare or save ").
                        append(modifier).append("schools.");
            }
        } else {
            sb.append("View and map all ").append(modifier).
                    append("schools in the ").append(districtDisplayName).
                    append(", ").append(state.getAbbreviation()).
                    append(". Plus, compare or save ").append(modifier).
                    append("schools in this district.");
        }

        return sb.toString();
    }

    protected String getOmniturePageName(HttpServletRequest request, int currentPage, int totalResults) {
        String pageName = "";

        String map = "map".equals(request.getParameter("view")) ? "Map" : "";

        if(totalResults > 0) {
            pageName = "schools:city:" + map + currentPage;
        }
        else {
            pageName = "schools:city:noresults" + map;
        }

        return pageName;
    }

    public Map<String,Object> getOmnitureHierarchyAndPageName(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, int totalResults) {
        Map<String,Object> model = new HashMap<String,Object>();
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        String omniturePageName = getOmniturePageName(request, requestedPage.pageNumber, totalResults);
        String omnitureHierarchy = "";
        if("map".equals(request.getParameter("view"))) {
            omnitureHierarchy = getOmnitureMapHierarchy(requestedPage.pageNumber, totalResults);
        }
        else {
            omnitureHierarchy = getOmnitureHierarchy(requestedPage.pageNumber, totalResults);
        }
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);
        return model;
    }

    protected void addGamAttributes(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, List<SolrSchoolSearchResult> schoolResults, boolean showAdvancedFilters) {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        // GS-10448 - search results
        if (StringUtils.isNotBlank(commandAndFields.getSearchString()) && schoolResults != null) {
            _searchAdHelper.addSearchResultsAdKeywords(pageHelper, schoolResults);
        }

        // GS-10003 - school type
        _searchAdHelper.addSchoolTypeAdKeywords(pageHelper, commandAndFields.getSchoolTypes());

        // GS-6875 - level
        _searchAdHelper.addLevelCodeAdKeywords(pageHelper, commandAndFields.getGradeLevels());

        // GS-5786 - city browse, GS-7809 - adsense hints for realtor.com
        City city = commandAndFields.getCity();
        State state = commandAndFields.getState();
        if (commandAndFields.isCityBrowse()) {
            // GS-5786 - city browse
            _searchAdHelper.addCityAdKeyword(pageHelper, city);

            // GS-7809 - adsense hints for realtor.com
            _searchAdHelper.addRealtorDotComAdKeywords(pageHelper, state, city);
        }

        if (showAdvancedFilters) {
            _searchAdHelper.addAdvancedFiltersKeywords(pageHelper, showAdvancedFilters);
        }

        // GS-13607 - county
        _searchAdHelper.addCountyAdKeywords(pageHelper, schoolResults);

        // GS-13671 - template: srchbrowse
        _searchAdHelper.addSearchBrowseAdKeyword(pageHelper);
    }

    public List<ICitySearchResult> putNearbyCitiesInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

        citySearchResults = ListUtils.typedList(_nearbyCitiesController.getNearbyCities(commandAndFields.getLatitude(), commandAndFields.getLongitude(), SchoolSearchHelper.NEARBY_CITIES_RADIUS, SchoolSearchHelper.NEARBY_CITIES_COUNT, commandAndFields.getCityFromUrl() // exclude this city from results
        ), ICitySearchResult.class);

        model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        return citySearchResults;
    }

    @Override
    public Logger getLogger() {
        return _log;
    }

    protected ModelAndView checkForRedirectConditions(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields) {
        // City Browse and District Browse Specific:  We're in a city browse or district browse page, so get the city
        // from the URL. If it's not a real city, then 302 to the state home. If we can't find the state home for
        // some reason, then 404. Otherwise add city to the model
        if (commandAndFields.getCityFromUrl() == null) {
            if (commandAndFields.getState() != null) {
                UrlBuilder stateHome = new UrlBuilder(UrlBuilder.RESEARCH, commandAndFields.getState());
                RedirectView302 view302 = new RedirectView302(stateHome.asSiteRelative(request));
                return new ModelAndView(view302);
            }
            return redirectTo404(response);
        }

        return null;
    }
}
