package gs.web.search;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.search.*;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.fields.*;
import gs.data.search.services.SchoolSearchServiceSolrImpl;
import gs.web.util.HttpCacheInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/search/schoolAutocomplete.page")
public class SchoolAutocompleteController {
    protected final Logger _log = Logger.getLogger(SchoolAutocompleteController.class);

    SchoolSearchServiceSolrImpl _solrSchoolSearchService;

    @Autowired
    private GsSolrSearcher _gsSolrSearcher;

    HttpCacheInterceptor cacheInterceptor = new HttpCacheInterceptor();

    public static int SUGGEST_COUNT = 150;

    @RequestMapping(method= RequestMethod.GET)
    public void handleRequestInternal(@RequestParam(value = "q", required = false) String searchString,
                                      @RequestParam(value = "state", required = false) String state,
                                      @RequestParam(value = "schoolDistrict", required = false) Boolean schoolDistrict,
                                      @RequestParam(value = "schoolCity", required = false) Boolean schoolCity,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
            List<String> suggestions = new ArrayList<String>();
            if (!StringUtils.isBlank(state) && !StringUtils.isBlank(searchString)) {
                if (schoolCity != null && schoolCity) {
                    boolean excludePreschools = StringUtils.equals("true", request.getParameter("excludePreschools"));
                    List<SolrSchoolSearchResult> solrResults = searchForSchools(state, searchString, excludePreschools);
                    JSONObject json = getSchoolDetailsJson(solrResults, request, response);
                    json.write(response.getWriter());
                    response.getWriter().flush();
                    return; // early exit
                }

                if (schoolDistrict != null && schoolDistrict) {
                    suggestions = _solrSchoolSearchService.suggestSchoolDistrict(StringUtils.trim(searchString), StringUtils.lowerCase(state), 0, SUGGEST_COUNT);
                } else {
                    suggestions = _solrSchoolSearchService.suggest(StringUtils.trim(searchString), StringUtils.lowerCase(state), 0, SUGGEST_COUNT);
                }
            }

            response.setContentType("application/json");
            cacheInterceptor.setCacheHeaders(response);

            PrintWriter writer = response.getWriter();
            for (String suggestion : suggestions) {
                writer.println(suggestion);
            }
            writer.flush();
        } catch (IOException e) {
            _log.warn("Error when searching for suggestions for school autosuggest.",e);
        } catch (Exception e) {
            _log.warn("Unexpected error when searching for suggestions for school autosuggest.",e);
        }
    }

    protected JSONObject getSchoolDetailsJson(List<SolrSchoolSearchResult> schools, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        JSONObject schoolsResponseJson = new JSONObject();
        response.setContentType("application/json");
        JSONArray schoolsJsonArray = new JSONArray();

        for (SolrSchoolSearchResult school : schools) {
            Map<String, String> schoolMap = new HashMap<String, String>();

            schoolMap.put("id", String.valueOf(school.getId()));
            schoolMap.put("state", school.getState().getAbbreviation());
            schoolMap.put("name", school.getName());

            schoolMap.put("type", school.getSchoolType());
            //schoolMap.put("gradeRange", school.getGradeLevels() == null ? "" : school.getGradeLevels().getRangeString());
            schoolMap.put("city", school.getCity() == null ? "" : school.getCity());
            schoolMap.put("street", school.getAddress().getStreet());
            schoolMap.put("streetLine2", school.getAddress().getStreetLine2());
            schoolMap.put("cityStateZip", school.getAddress().getCityStateZip());
            schoolMap.put("enrollment", String.valueOf(school.getEnrollment()));
            schoolMap.put("gradeRange", String.valueOf(school.getGrades().getRangeString()));
            schoolMap.put("levelCode", String.valueOf(school.getLevelCode()));

            schoolsJsonArray.put(schoolMap);
        }
        schoolsResponseJson.put("schools", schoolsJsonArray);

        return schoolsResponseJson;
    }

    /*protected void getSchoolDetailsJson(List<SolrSchoolSearchResult> schools, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        JSONObject schoolsResponseJson = new JSONObject();
        response.setContentType("application/json");
        JSONArray schoolsJsonArray = new JSONArray();

        for (SolrSchoolSearchResult school : schools) {
            Map<String, String> schoolMap = new HashMap<String, String>();

            schoolMap.put("id", String.valueOf(school.getId()));
            schoolMap.put("state", school.getState().getAbbreviation());
            schoolMap.put("name", school.getName());

            schoolMap.put("type", school.getSchoolType());
            //schoolMap.put("gradeRange", school.getGradeLevels() == null ? "" : school.getGradeLevels().getRangeString());
            schoolMap.put("city", school.getCity() == null ? "" : school.getCity());
            schoolMap.put("street", school.getAddress().getStreet());
            schoolMap.put("streetLine2", school.getAddress().getStreetLine2());
            schoolMap.put("cityStateZip", school.getAddress().getCityStateZip());
            schoolMap.put("enrollment", String.valueOf(school.getEnrollment()));

            schoolsJsonArray.put(schoolMap);
        }
        schoolsResponseJson.put("schools", schoolsJsonArray);
        schoolsResponseJson.write(response.getWriter());
        response.getWriter().flush();
    }*/

    protected List<SolrSchoolSearchResult> searchForSchools(String state, String searchString, boolean excludePreschools) {
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage;

        searchString = StringUtils.lowerCase(searchString);
        GsSolrQuery q = createGsSolrQuery();

        q.filter(DocumentType.SCHOOL).page(0, SUGGEST_COUNT);

        q.query(searchString);
        q.setSpellCheckEnabled(false);

        if (state != null) {
            q.filter(SchoolFields.SCHOOL_DATABASE_STATE, state.toLowerCase());
        }

        if (excludePreschools) {
            q.filter(SchoolFields.GRADE_LEVEL, Arrays.asList("e", "m", "h"));
        }

        q.addBeginsWithQuery(AutosuggestFields.SCHOOLDISTRICT_AUTOSUGGEST, searchString);

        List<SolrSchoolSearchResult> results = new ArrayList<SolrSchoolSearchResult>();

        try {
            searchResultsPage = _gsSolrSearcher.search(q, SolrSchoolSearchResult.class, true);

            results = searchResultsPage.getSearchResults();

        } catch (SearchException e) {
            _log.error("Problem occurred while getting schools", e);
        }

        return results;
    }

    public GsSolrQuery createGsSolrQuery() {
        return new GsSolrQuery(QueryType.STANDARD);
    }

    public SchoolSearchServiceSolrImpl getSolrSchoolSearchService() {
        return _solrSchoolSearchService;
    }

    @Autowired
    public void setSolrSchoolSearchService(SchoolSearchServiceSolrImpl solrSchoolSearchService) {
        _solrSchoolSearchService = solrSchoolSearchService;
    }
}
