package gs.web.search;

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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/search/schoolAutocomplete.page")
public class SchoolAutocompleteController {
    protected final Logger _log = Logger.getLogger(SchoolAutocompleteController.class);

    SchoolSearchServiceSolrImpl _solrSchoolSearchService;

    HttpCacheInterceptor cacheInterceptor = new HttpCacheInterceptor();

    @RequestMapping(method= RequestMethod.GET)
    public void handleRequestInternal(@RequestParam(value = "q", required = false) String searchString,
                                      @RequestParam(value = "state", required = false) String state,
                                      @RequestParam(value = "schoolDistrict", required = false) Boolean schoolDistrict,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
            List<String> suggestions = new ArrayList<String>();
            if (!StringUtils.isBlank(state) && !StringUtils.isBlank(searchString)) {
                if (schoolDistrict != null && schoolDistrict) {
                    suggestions = _solrSchoolSearchService.suggestSchoolDistrict(searchString, StringUtils.lowerCase(state), 0, 150);
                } else {
                    suggestions = _solrSchoolSearchService.suggest(searchString, StringUtils.lowerCase(state), 0, 150);
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

    public SchoolSearchServiceSolrImpl getSolrSchoolSearchService() {
        return _solrSchoolSearchService;
    }

    @Autowired
    public void setSolrSchoolSearchService(SchoolSearchServiceSolrImpl solrSchoolSearchService) {
        _solrSchoolSearchService = solrSchoolSearchService;
    }
}
