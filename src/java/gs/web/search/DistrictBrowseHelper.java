package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.seo.SeoUtil;
import gs.web.util.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class DistrictBrowseHelper {

    private SchoolSearchCommandWithFields commandWithFields;

    DistrictBrowseHelper(SchoolSearchCommandWithFields commandWithFields) {
        this.commandWithFields = commandWithFields;
    }

    public Map<String, Object> getMetaData() {
        return getMetaData(false);
    }

    public Map<String,Object> getMetaData(boolean mobile) {
        District district = commandWithFields.getDistrict();
        String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
        LevelCode levelCode = commandWithFields.getLevelCode();
        Map<String,Object> model = new HashMap<String,Object>();
        if (!mobile){
            model.put(SchoolSearchController.MODEL_TITLE, SeoUtil.generatePageTitle(district, levelCode, schoolSearchTypes));
            model.put(SchoolSearchController.MODEL_META_DESCRIPTION, SeoUtil.generateMetaDescription(district));
        
            String metaKeywords = null;
            if (district != null) {
                metaKeywords = SeoUtil.generateMetaKeywords(district);
            }
            model.put(SchoolSearchController.MODEL_META_KEYWORDS, metaKeywords);
        }
        else {
            model.put(SchoolSearchController.MODEL_TITLE, "School District Search Results");
        }
        return model;
    }

    protected String getRelCanonical(HttpServletRequest request) {
        District district = commandWithFields.getDistrict();

        if (request == null || district == null) {
            throw new IllegalArgumentException("HttpServletRequest and District are required and cannot be null");
        }
        UrlBuilder urlBuilder = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
        String url = urlBuilder.asFullUrl(request);

        return url;
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

}
