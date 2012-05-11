package gs.web.search;

import gs.data.geo.City;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.state.State;
import gs.web.util.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class CityBrowseHelper {

    private SchoolSearchCommandWithFields commandWithFields;

    public CityBrowseHelper(SchoolSearchCommandWithFields commandWithFields) {
        this.commandWithFields = commandWithFields;
    }

    public Map<String, Object> getMetaData(){
        return getMetaData(false);
    }

    public Map<String,Object> getMetaData(boolean mobile) {
        City city = commandWithFields.getCityFromUrl();
        String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
        LevelCode levelCode = commandWithFields.getLevelCode();

        Map<String,Object> model = new HashMap<String,Object>();
        model.put(SchoolSearchController.MODEL_TITLE, getTitle(city.getDisplayName(), city.getState(), levelCode, schoolSearchTypes, mobile));
        model.put(SchoolSearchController.MODEL_META_DESCRIPTION, getMetaDescription(city, levelCode, schoolSearchTypes));
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

    protected String getRelCanonical(HttpServletRequest request) {
        City city = commandWithFields.getCityFromUrl();
        State state = commandWithFields.getState();
        
        if (request == null || city == null || state == null) {
            throw new IllegalArgumentException("request, city, and state are required and cannot be null");
        }
        HashSet<SchoolType> schoolTypeSet = new HashSet<SchoolType>(1);
        schoolTypeSet.add(SchoolType.PUBLIC);
        schoolTypeSet.add(SchoolType.CHARTER);
        schoolTypeSet.add(SchoolType.PRIVATE);

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                state,
                city.getName(),
                schoolTypeSet, null);
        String url = urlBuilder.asFullUrl(request);

        return url;
    }

    public String getOmnitureHierarchy(int currentPage, int totalResults) {
        String hierarchy = "Search,Schools,City," + (totalResults > 0 ? currentPage : "noresults");

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
            cityWithModifier.append(cityDisplayName).append(" ").append(modifier);
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
                sb.append("View and map all ").append(cityWithModifier).
                        append("schools. Plus, compare or save ").
                        append(modifier).append("schools.");
            }
        } else {
            sb.append("View and map all ").append(modifier).
                    append("schools in the ").append(districtDisplayName).
                    append(". Plus, compare or save ").append(modifier).
                    append("schools in this district.");
        }

        return sb.toString();
    }

    protected String getOmniturePageName(HttpServletRequest request, int currentPage) {
        String pageName = "";

        String paramMap = request.getParameter("map");

        pageName = "schools:city:" + currentPage + ("1".equals(paramMap) ? ":map" : "");

        return pageName;
    }
}
