package gs.web.search;


import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

public abstract class AbstractSchoolSearchHelper {

    public static final String MODEL_REL_CANONICAL = "relCanonical";
    protected static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final String MODEL_OMNITURE_PAGE_NAME = "omniturePageName";
    public static final String MODEL_OMNITURE_HIERARCHY = "omnitureHierarchy";
    public static final String MODEL_TITLE = "title";
    public static final String MODEL_META_DESCRIPTION = "metaDescription";
    public static final String MODEL_META_KEYWORDS = "metaKeywords";
    public static final String MODEL_CITY_SEARCH_RESULTS = "citySearchResults";
    public static final String MODEL_DISTRICT_SEARCH_RESULTS = "districtSearchResults";

    public static final int DISTRICTS_COUNT = 11;
    public static final int NEARBY_CITIES_COUNT = 33;
    public static final float NEARBY_CITIES_RADIUS = 50f;


    public ModelAndView redirectTo404(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView(VIEW_NOT_FOUND);
    }

}
