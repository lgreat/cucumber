package gs.web.search;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.search.Searcher;
import gs.data.search.PorterStandardAnalyzer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the controller that manages the CityDistrict module.  This module
 * appears above the main search results on search.page.  Three parameters
 * are used to construct the model returned to the CityDistrict view:
 * <ul>
 *   <li>The search query string</li>
 *   <li>The current State</li>
 * </ul>
 *
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistrictController extends AbstractController {

    public static final String BEAN_ID = "/search/citydistrict.module";
    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static final Logger _log =
            Logger.getLogger(CityDistrictController.class);
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
        String queryString = (String)request.getParameter("q");
        String state = (String)request.getParameter("state");
        Map model = new HashMap();
        model.put("query", queryString);
        model.put("state", state);

        BooleanQuery baseQuery = new BooleanQuery();
        baseQuery.add(new TermQuery(new Term("state", state)), true, false);

        if (queryString != null) {
            QueryParser parser =
                    new QueryParser("text", new PorterStandardAnalyzer(CITY_DIST_STOP_WORDS));
            parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
            Query keywordQuery = parser.parse(queryString);
            baseQuery.add(keywordQuery, true, false);

            //
            StringBuffer filtersBuffer = new StringBuffer("All ");
            StringBuffer urlBuffer = new StringBuffer();

            String lowerCaseQuery = queryString.toLowerCase();

            if (lowerCaseQuery.indexOf("public") != -1) {
                filtersBuffer.append(" public");
                urlBuffer.append("&amp;st=public");
            } else if (lowerCaseQuery.indexOf("private") != -1) {
                filtersBuffer.append(" private");
                urlBuffer.append("&amp;st=private");
            } else if (lowerCaseQuery.indexOf("charter") != -1) {
                filtersBuffer.append(" charter");
                urlBuffer.append("&amp;st=charter");
            }

            if (lowerCaseQuery.indexOf("elementary") +
                    lowerCaseQuery.indexOf("primary") > 0) {
                filtersBuffer.append(" elemenatry");
                urlBuffer.append("&amp;gl=elementary");
            } else if (lowerCaseQuery.indexOf("middle") +
                    lowerCaseQuery.indexOf("junior") +
                    lowerCaseQuery.indexOf("jr") > 0) {
                filtersBuffer.append(" middle");
                urlBuffer.append("&amp;gl=middle");
            } else if (lowerCaseQuery.indexOf("high") +
                    lowerCaseQuery.indexOf("senior") > 0) {
                filtersBuffer.append(" high");
                urlBuffer.append("&amp;gl=high");
            }

            if (filtersBuffer.length() >  5) {
                filtersBuffer.append(" schools in ");
                model.put("filters", filtersBuffer.toString());
                model.put("filterparams", urlBuffer.toString());
            }
        }

        BooleanQuery cityQuery = new BooleanQuery();
        cityQuery.add(new TermQuery(new Term("type", "city")), true, false);
        cityQuery.add(baseQuery, true, false);

        BooleanQuery districtQuery = new BooleanQuery();
        districtQuery.add(new TermQuery(new Term("type", "district")), true, false);
        districtQuery.add(baseQuery, true, false);

        _log.debug("cityQuery: " + cityQuery);
        Hits cityHits = _searcher.search(cityQuery, null, null, null);
        model.put("citiestotal", new Integer(cityHits != null ? cityHits.length() : 0));

        _log.debug("districtQuery: " + districtQuery);
        Hits districtHits = _searcher.search(districtQuery, null, null, null);
        model.put("districtstotal", new Integer(districtHits != null ? districtHits.length() : 0));

        model.put("cities", cityHits);
        model.put("districts", districtHits);



        List cities = new ArrayList();
        List districts = new ArrayList();

        for (int i = 0; i < LIST_SIZE; i++) {
            if (cityHits != null && cityHits.length() > i) {
                Document cityDoc = cityHits.doc(i);
                cities.add(cityDoc.get("city"));
            }
            if (districtHits != null && districtHits.length() > i) {
                Document districtDoc = districtHits.doc(i);
                Map dMap = new HashMap();
                dMap.put("name", districtDoc.get("name"));
                dMap.put("id", districtDoc.get("id"));
                districts.add(dMap);
            }
        }


        model.put("cities", cities);
        model.put("districts", districts);

        return new ModelAndView("/search/citydistrict", "model", model);
    }
}
