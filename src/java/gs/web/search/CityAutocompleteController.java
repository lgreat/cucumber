package gs.web.search;

import gs.data.search.services.CitySearchServiceSolrImpl;
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

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
@Controller
@RequestMapping("/search/cityAutocomplete.page")
public class CityAutocompleteController {
    protected final Logger _log = Logger.getLogger(CityAutocompleteController.class);

    CitySearchServiceSolrImpl _solrCitySearchService;

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @RequestMapping(method= RequestMethod.GET)
    public void handleRequestInternal(@RequestParam(value = "q", required = false) String searchString,
//                                      @RequestParam(value = "state", required = false) String state,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
            List<String> suggestions = new ArrayList<String>();
            if (StringUtils.isNotBlank(searchString)) {
                suggestions = _solrCitySearchService.suggest(searchString, 0, 150);
            }

            response.setContentType("application/json");
            _cacheInterceptor.setCacheHeaders(response);

            PrintWriter writer = response.getWriter();
            for (String suggestion : suggestions) {
                writer.println(suggestion);
            }
            writer.flush();
        } catch (IOException e) {
            _log.error("Error when searching for suggestions for city autosuggest.",e);
        } catch (Exception e) {
            _log.error("Unexpected error when searching for suggestions for city autosuggest.",e);
        }
    }

    public CitySearchServiceSolrImpl getSolrCitySearchService() {
        return _solrCitySearchService;
    }

    @Autowired
    public void setSolrCitySearchService(CitySearchServiceSolrImpl solrCitySearchService) {
        _solrCitySearchService = solrCitySearchService;
    }

}
