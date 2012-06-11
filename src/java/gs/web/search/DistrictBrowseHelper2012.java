package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.seo.SeoUtil;
import gs.web.pagination.RequestedPage;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component("districtBrowseHelper")
class DistrictBrowseHelper2012 extends AbstractBrowseHelper {

    @Autowired
    SearchAdHelper _searchAdHelper;

    @Autowired
    NearbyCitiesController _nearbyCitiesController;

    Logger _log = Logger.getLogger(DistrictBrowseHelper2012.class);

    public Map<String, Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
        return getMetaData(commandWithFields, false);
    }

    public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields, boolean mobile) {
        District district = commandWithFields.getDistrict();
        String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
        LevelCode levelCode = commandWithFields.getLevelCode();
        Map<String,Object> model = new HashMap<String,Object>();
        if (!mobile){
            model.put(MODEL_TITLE, SeoUtil.generatePageTitle(district, levelCode, schoolSearchTypes));
            model.put(MODEL_META_DESCRIPTION, SeoUtil.generateMetaDescription(district));
        
            String metaKeywords = null;
            if (district != null) {
                metaKeywords = SeoUtil.generateMetaKeywords(district);
            }
            model.put(MODEL_META_KEYWORDS, metaKeywords);
        }
        else {
            model.put(MODEL_TITLE, "School District Search Results");
        }
        return model;
    }

    protected String getRelCanonical(SchoolSearchCommandWithFields commandWithFields, HttpServletRequest request) {
        District district = commandWithFields.getDistrict();

        if (request == null || district == null) {
            throw new IllegalArgumentException("HttpServletRequest and District are required and cannot be null");
        }
        UrlBuilder urlBuilder = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
        String url = urlBuilder.asFullUrl(request);

        return url;
    }

    public Map<String,Object> getOmnitureHierarchyAndPageName(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, int totalResults) {
        Map<String,Object> model = new HashMap<String,Object>();
        RequestedPage requestedPage = commandAndFields.getRequestedPage();
        String omniturePageName = getOmniturePageName(request, requestedPage.pageNumber);
        String omnitureHierarchy = getOmnitureHierarchy(requestedPage.pageNumber, totalResults);
        model.put(MODEL_OMNITURE_PAGE_NAME, omniturePageName);
        model.put(MODEL_OMNITURE_HIERARCHY, omnitureHierarchy);
        return model;
    }

    public String getOmnitureHierarchy(int currentPage, int totalResults) {
        String hierarchy = "Search,Schools,District," + (totalResults > 0 ? currentPage : "noresults");
        
        return hierarchy;
    }

    protected String getOmniturePageName(HttpServletRequest request, int currentPage) {
        String pageName = "";

        String paramMap = request.getParameter("map");

        pageName = "schools:district:" + currentPage + ("1".equals(paramMap) ? ":map" : "");

        return pageName;
    }

    protected ModelAndView checkForRedirectConditions(HttpServletRequest request, HttpServletResponse response, SchoolSearchCommandWithFields commandAndFields) {
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

    protected void addGamAttributes(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, List<SolrSchoolSearchResult> schoolResults) {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        // GS-10448 - search results
        if (StringUtils.isNotBlank(commandAndFields.getSearchString()) && schoolResults != null) {
            _searchAdHelper.addSearchResultsAdKeywords(pageHelper, schoolResults);
        }

        // GS-10003 - school type
        _searchAdHelper.addSchoolTypeAdKeywords(pageHelper, commandAndFields.getSchoolTypes());

        // GS-6875 - level
        _searchAdHelper.addLevelCodeAdKeywords(pageHelper, commandAndFields.getGradeLevels());

        // GS-10157 - district browse
        if (commandAndFields.isDistrictBrowse()) {
            _searchAdHelper.addDistrictAdKeywords(pageHelper, commandAndFields.getDistrict());
        }
    }

    protected void putRelCanonicalIntoModel(HttpServletRequest request, Map<String, Object> model, SchoolSearchCommandWithFields commandAndFields) {

        // determine the correct canonical URL based on if this controller is handling a string search that matches
        // a city or not, and whether or not the controller is handling a city browse or district browse request
        String relCanonicalUrl = null;
        if (commandAndFields.getState() != null) {
            relCanonicalUrl = getRelCanonical(commandAndFields, request);

            if (relCanonicalUrl != null) {
                model.put(MODEL_REL_CANONICAL, relCanonicalUrl);
            }
        }
    }

    public List<ICitySearchResult> putNearbyCitiesInModel(SchoolSearchCommandWithFields commandAndFields, Map<String,Object> model) {
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

        citySearchResults = ListUtils.typedList(_nearbyCitiesController.getNearbyCities(commandAndFields.getLatitude(), commandAndFields.getLongitude(), SchoolSearchHelper.NEARBY_CITIES_RADIUS, SchoolSearchHelper.NEARBY_CITIES_COUNT, commandAndFields.getCityFromUrl() // exclude this city from results
        ), ICitySearchResult.class);

        model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);
        return citySearchResults;
    }



    public Logger getLogger() {
        return _log;
    }
}