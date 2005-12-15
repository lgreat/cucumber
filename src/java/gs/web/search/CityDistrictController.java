package gs.web.search;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.search.Searcher;
import gs.data.search.PorterStandardAnalyzer;
import gs.data.search.SearchCommand;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the controller that manages the CityDistrict module.  This module
 * appears above the main search results on search.page.  Three parameters
 * are used to construct the model returned to the CityDistrict view:
 * <ul>
 * <li>The search query string</li>
 * <li>The current State</li>
 * </ul>
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistrictController extends AbstractController {

    public static final String BEAN_ID = "/search/citydistrict.module";
    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static int EXTENDED_LIST_SIZE = 50;
    //private static final Logger _log =
    //                Logger.getLogger(CityDistrictController.class);
    private static final String[] CITY_DIST_STOP_WORDS = {
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with",
            // City/District specific stopwords
            "charter", "city", "district", "elementary", "middle", "high",
            "junior", "public", "private", "school", "schools",
    };

    private Searcher _searcher;

    public CityDistrictController(Searcher searcher) {
        _searcher = searcher;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String queryString = (String) request.getParameter("q");
        String state = (String) request.getParameter("state");

        int cityListSize = LIST_SIZE, districtListSize = LIST_SIZE;
        if (!StringUtils.isEmpty((String) request.getParameter("morecities"))) {
            cityListSize = EXTENDED_LIST_SIZE;
        }

        if (!StringUtils.isEmpty((String) request.getParameter("moredistricts"))) {
            districtListSize = EXTENDED_LIST_SIZE;
        }


        state = (!StringUtils.isEmpty(state)) ? state.toLowerCase() : "ca";
        Map model = new HashMap();
        model.put("query", queryString);
        model.put("state", state);

        BooleanQuery baseQuery = new BooleanQuery();
        baseQuery.add(new TermQuery(new Term("state", state)), true, false);

        String st = null;
        String gl = null;

        if (!StringUtils.isEmpty(queryString)) {
            QueryParser parser =
                    new QueryParser("text", new PorterStandardAnalyzer(CITY_DIST_STOP_WORDS));
            parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
            Query keywordQuery = parser.parse(queryString);
            baseQuery.add(keywordQuery, true, false);

            StringBuffer filtersBuffer = new StringBuffer("All ");
            StringBuffer urlBuffer = new StringBuffer();

            String lowerCaseQuery = queryString.toLowerCase();

            if (lowerCaseQuery.indexOf("public") != -1) {
                filtersBuffer.append(" public");
                urlBuffer.append("&amp;st=public");
                st = "public";
            } else if (lowerCaseQuery.indexOf("private") != -1) {
                filtersBuffer.append(" private");
                urlBuffer.append("&amp;st=private");
                st = "private";
            } else if (lowerCaseQuery.indexOf("charter") != -1) {
                filtersBuffer.append(" charter");
                urlBuffer.append("&amp;st=charter");
                st = "charter";
            }

            if (lowerCaseQuery.indexOf("elementary") != -1 ||
                    lowerCaseQuery.indexOf("primary") != -1) {
                filtersBuffer.append(" elementary");
                urlBuffer.append("&amp;gl=elementary");
                gl = "elementary";
            } else if (lowerCaseQuery.indexOf("middle") != -1 ||
                    lowerCaseQuery.indexOf("junior") != -1 ||
                    lowerCaseQuery.indexOf("jr") != -1) {
                filtersBuffer.append(" middle");
                urlBuffer.append("&amp;gl=middle");
                gl = "middle";
            } else if (lowerCaseQuery.indexOf("high") != -1 ||
                    lowerCaseQuery.indexOf("senior") != -1) {
                filtersBuffer.append(" high");
                urlBuffer.append("&amp;gl=high");
                gl = "high";
            }

            filtersBuffer.append(" schools in ");
            model.put("filters", filtersBuffer.toString());
            model.put("filterparams", urlBuffer.toString());
            //}

            BooleanQuery cityQuery = new BooleanQuery();
            cityQuery.add(new TermQuery(new Term("type", "city")), true, false);
            cityQuery.add(baseQuery, true, false);

            BooleanQuery districtQuery = new BooleanQuery();
            districtQuery.add(new TermQuery(new Term("type", "district")), true, false);
            districtQuery.add(baseQuery, true, false);

            Hits cityHits = _searcher.search(cityQuery, null, null, null);
            model.put("citiestotal", new Integer(cityHits != null ? cityHits.length() : 0));

            Hits districtHits = _searcher.search(districtQuery, null, null, null);
            model.put("districtstotal", new Integer(districtHits != null ? districtHits.length() : 0));

            System.out.println("cityHits: " + cityHits.length());
            model.put("cities", cityHits);
            model.put("districts", districtHits);

            List filteredCities = new ArrayList();

            if (cityHits != null && cityHits.length() > 0 && (gl != null || st != null)) {
                // todo: limit to x cities and provide a "more" link
                for (int ii = 0; ii < cityHits.length(); ii++) {
                    SearchCommand command = new SearchCommand();
                    command.setState(state);
                    String city = cityHits.doc(ii).get("city");
                    command.setCity(city);
                    command.setC("school");
                    if (st != null) {
                        command.setSt(new String[]{st});
                    }
                    if (gl != null) {
                        command.setGl(new String[]{gl});
                    }

                    Hits hits = _searcher.search(command);
                    if (hits != null && hits.length() > 0) {
                        filteredCities.add(city);
                    }
                }
                if (filteredCities.size() > 0) {
                    model.put("filteredcities", filteredCities);
                }
            }

            List cities = new ArrayList();
            List districts = new ArrayList();

            for (int i = 0; i < cityListSize; i++) {
                if (cityHits != null && cityHits.length() > i) {
                    Document cityDoc = cityHits.doc(i);
                    cities.add(cityDoc.get("city"));
                }
            }
            for (int j = 0; j < districtListSize; j++) {
                if (districtHits != null && districtHits.length() > j) {
                    Document districtDoc = districtHits.doc(j);
                    Map dMap = new HashMap();
                    dMap.put("name", districtDoc.get("name"));
                    dMap.put("id", districtDoc.get("id"));
                    districts.add(dMap);
                }
            }


            model.put("cities", cities);
            model.put("districts", districts);
        }
        return new ModelAndView("/search/citydistrict", "model", model);
    }
}
