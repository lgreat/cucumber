package gs.web.search;


import gs.data.school.LevelCode;
import gs.data.search.FieldSort;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.SolrSchoolSearchResult;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component("schoolSearchCommonHelper")
public class SchoolSearchCommonHelper extends AbstractSchoolSearchHelper {

    public static final String MODEL_OMNITURE_QUERY = "omnitureQuery";
    public static final String MODEL_OMNITURE_SCHOOL_TYPE = "omnitureSchoolType";
    public static final String MODEL_OMNITURE_SCHOOL_LEVEL = "omnitureSchoolLevel";
    public static final String MODEL_OMNITURE_SORT_SELECTION = "omnitureSortSelection";
    public static final String MODEL_OMNITURE_RESULTS_PER_PAGE = "omnitureResultsPerPage";
    public static final String MODEL_OMNITURE_ADDRESS_SEARCH = "omnitureAddressSearch";
    public static final String MODEL_OMNITURE_NAME_SEARCH = "omnitureNameSearch";


    public Map<String,Object> getCommonOmnitureAttributes(HttpServletRequest request, SchoolSearchCommandWithFields commandAndFields, SearchResultsPage<SolrSchoolSearchResult> searchResultsPage) {
        Map<String,Object> map = new HashMap<String,Object>();
        String[] schoolSearchTypes = commandAndFields.getSchoolTypes();
        boolean sortChanged = commandAndFields.getSchoolSearchCommand().isSortChanged();
        String searchString = (commandAndFields.getSearchString() != null) ? commandAndFields.getSearchString() :
                commandAndFields.getSchoolSearchCommand().getLocationSearchString();
        String omnitureQuery = commandAndFields.isSearch()? getOmnitureQuery(searchString) : null;
        map.put(MODEL_OMNITURE_QUERY, omnitureQuery);
        map.put(MODEL_OMNITURE_SCHOOL_TYPE, getOmnitureSchoolType(schoolSearchTypes));
        map.put(MODEL_OMNITURE_SCHOOL_LEVEL, getOmnitureSchoolLevel(commandAndFields.getLevelCode()));
        map.put(MODEL_OMNITURE_SORT_SELECTION, getOmnitureSortSelection(sortChanged ? commandAndFields.getFieldSort() : null));
        map.put(MODEL_OMNITURE_RESULTS_PER_PAGE, getOmnitureResultsPerPage(commandAndFields.getRequestedPage().pageSize, searchResultsPage.getTotalResults()));
        map.put(MODEL_OMNITURE_ADDRESS_SEARCH, false);
        map.put(MODEL_OMNITURE_NAME_SEARCH, false);

        if (commandAndFields.isNearbySearchByLocation()) {
            map.put(MODEL_OMNITURE_ADDRESS_SEARCH, true);
        } else if (StringUtils.equals(request.getParameter("search_type"), "1")) {
            map.put(MODEL_OMNITURE_NAME_SEARCH, true);
        }
        return map;
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
        if (schoolSearchTypes == null) {
            return null;
        } else {
            return StringUtils.join(schoolSearchTypes, ",");
        }
    }

    protected static String getOmnitureSchoolLevel(LevelCode levelCode) {
        // currently, there's no url that will take you to a page with all level code filters unchecked,
        // for which we should be logging "nothing checked" in Omniture;
        // that's why the code here doesn't ever return it. it can be implemented if we ever add such a url
        if (levelCode != null) {
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
                resultsPerPage = "100";
                break;
        }
        return resultsPerPage;
    }


}
