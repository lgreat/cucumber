package gs.web.search;

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
import java.util.List;

@Controller
@RequestMapping("/search/schoolAutocomplete.page")
public class SchoolAutocompleteController {
    protected final Logger _log = Logger.getLogger(SchoolAutocompleteController.class);

    @Autowired
    CitySearchServiceSolrImpl _solrCitySearchService;

    @RequestMapping(method= RequestMethod.GET)
    public void handleRequestInternal(@RequestParam("q") String searchString, @RequestParam("state") String state, HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<String> suggestions = _solrCitySearchService.suggest(searchString, StringUtils.lowerCase(state), 0, 6);

        response.setContentType("application/json");
        try {
            PrintWriter writer = response.getWriter();
            for (String suggestion : suggestions) {
                writer.println(suggestion);
            }
            writer.flush();
        } catch (IOException e) {
            _log.warn("Error when searching for suggestions for school autosuggest.",e);
        }
    }

    public CitySearchServiceSolrImpl getSolrCitySearchService() {
        return _solrCitySearchService;
    }

    public void setSolrCitySearchService(CitySearchServiceSolrImpl solrCitySearchService) {
        _solrCitySearchService = solrCitySearchService;
    }
}
