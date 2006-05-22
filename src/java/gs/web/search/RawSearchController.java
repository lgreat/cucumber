package gs.web.search;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;
import java.net.URLEncoder;

import gs.data.search.Searcher;
import gs.data.search.GSAnalyzer;


/**
 * This controller sends search requests to the <code>gs.data.search.Searcher</code>
 * without using a <code>SearchCommand</code>.  The idea is to use only Lucene classes
 * to view the index.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class RawSearchController extends AbstractController {

    public static final String BEAN_ID = "/search/raw.page";
    private Searcher _searcher;
    private Map _analyzerMap;
    private static final int RESULT_LIMIT = 100;

    public RawSearchController() {
        super();
        _analyzerMap = new HashMap();
        _analyzerMap.put("gs", new GSAnalyzer());
        _analyzerMap.put("standard", new StandardAnalyzer());
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {

        ModelAndView mAndV = new ModelAndView("search/raw");
        String queryString = httpServletRequest.getParameter("q");

        String analyzerKey = httpServletRequest.getParameter("analyzer");
        Analyzer analyzer = (Analyzer)_analyzerMap.get(analyzerKey);

        if (StringUtils.isNotBlank(queryString)) {

            httpServletRequest.setAttribute("q", URLEncoder.encode(queryString, "UTF-8"));
            Query query = QueryParser.parse(queryString, "text", analyzer);
            mAndV.getModel().put("query", query.toString());
            Hits hits = _searcher.search(query, null, null, null);
            if (hits != null) {
                mAndV.getModel().put("total", String.valueOf(hits.length()));
                mAndV.getModel().put("results", makeResultList(query, hits));
            }
        }
        return mAndV;
    }

    private List makeResultList(Query q, Hits hits) throws IOException {

        List list = new ArrayList();
        if (hits != null) {
            int length = hits.length();
            for (int i = 0; i < length; i++) {
                Map result = new HashMap();
                result.put("number", String.valueOf(i+1)); // start at 1
                Document d = hits.doc(i);
                result.put("id", d.get("id"));
                String type = d.get("type");
                result.put("type", type);
                result.put("state", d.get("state"));
                if ("school".equals(type)) {
                    result.put("val", d.get("name"));
                } else if ("article".equals(type)) {
                    result.put("val", d.get("title"));
                } else if ("city".equals(type)) {
                    result.put("val", d.get("city"));
                } else if ("district".equals(type)) {
                    result.put("val", d.get("name"));
                } else if ("term".equals(type)) {
                    result.put("val", d.get("term"));
                } else {
                    result.put("val", d.get("text"));
                }

                result.put("explanation", _searcher.explain(q, hits.id(i)).toHtml());
                list.add(result);
                if (i >= RESULT_LIMIT - 1) break;
            }
        }
        return list;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}
