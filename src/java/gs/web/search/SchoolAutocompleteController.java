package gs.web.search;

import gs.data.geo.City;
import gs.data.school.district.District;
import gs.data.search.*;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.fields.AddressFields;
import gs.data.search.fields.DocumentType;
import gs.data.search.fields.SchoolFields;
import gs.data.search.fields.SolrField;
import gs.data.search.filters.SchoolFilters;
import gs.data.search.services.SchoolSearchServiceSolrImpl;
import gs.data.state.State;
import gs.web.pagination.RequestedPage;
import gs.web.util.HttpCacheInterceptor;
import org.apache.commons.lang.ArrayUtils;
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
import java.util.ArrayList;
import java.util.List;

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
                    suggestions = searchForSchools(state, searchString);

                } else if (schoolDistrict != null && schoolDistrict) {
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

    protected List<String> searchForSchools(String state, String searchString) {
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<SolrSchoolSearchResult>());

        GsSolrQuery q = createGsSolrQuery();

        q.filter(DocumentType.SCHOOL).page(0, SUGGEST_COUNT);

        if (state != null) {
            q.filter(SchoolFields.SCHOOL_DATABASE_STATE, state.toLowerCase());
        }

        q.query(searchString);

        List<String> autocompleteResults = new ArrayList<String>();

        try {
            searchResultsPage = _gsSolrSearcher.search(q, SolrSchoolSearchResult.class, true);

            List<SolrSchoolSearchResult> results = searchResultsPage.getSearchResults();


            for(SolrSchoolSearchResult result : results) {
                String s = result.getId() + "-" + result.getName() + "-" + result.getCity();
                autocompleteResults.add(s);
            }

        } catch (SearchException e) {
            _log.error("Problem occurred while getting schools", e);
        }

        return autocompleteResults;
    }

    public GsSolrQuery createGsSolrQuery() {
        return new GsSolrQuery(QueryType.SCHOOL_SEARCH);
    }

    public SchoolSearchServiceSolrImpl getSolrSchoolSearchService() {
        return _solrSchoolSearchService;
    }

    @Autowired
    public void setSolrSchoolSearchService(SchoolSearchServiceSolrImpl solrSchoolSearchService) {
        _solrSchoolSearchService = solrSchoolSearchService;
    }
}
