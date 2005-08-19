package gs.web.search;

import org.apache.lucene.search.Hits;
import org.apache.lucene.document.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import gs.data.state.StateManager;
import gs.web.search.SearchResult;

/**
 * This class handles the organization of <code>Hits</code> into
 * page-ready parcels that can be retrieved using the <code>getPage(..)</code>
 * method.
 * @author Chris Kimm<mailto:chriskimm@greatschools.net>
 */
public class ResultsPager {

    private Hits _schoolHits;
    private Hits _articleHits;
    private Hits _districtHits;

    private StateManager _stateManager;

    private static final Log _log = LogFactory.getLog(ResultsPager.class);

    public ResultsPager() {
    }

    public void setArticles(Hits hits) {
        _articleHits = hits;
    }

    public List getArticles(int page, int pageSize) {
        return getPage(_articleHits, page, pageSize);
    }

    public int getArticlesTotal() {
        if (_articleHits == null) return 0;
        return _articleHits.length();
    }

    public void setSchools(Hits hits) {
        _schoolHits = hits;
    }

    public List getSchools(int page, int pageSize) {
        return getPage(_schoolHits, page, pageSize);
    }

    public int getSchoolsTotal() {
        if (_schoolHits == null) return 0;
        return _schoolHits.length();
    }

    public void setDistricts(Hits hits) {
        _districtHits = hits;
    }

    public List getDistricts(int page, int pageSize) {
        return getPage(_districtHits, page, pageSize);
    }

    public int getDistrictsTotal() {
        if (_districtHits == null) return 0;
        return _districtHits.length();
    }

    /**
     * @param hits
     * @param page
     * @param pageSize
     * @return A sub<code>List</code> containing
     */
    private List getPage(Hits hits, int page, int pageSize) {
        List searchResults = new ArrayList();
        if (hits != null) {
            if (page < 1) {
                page = 1;
            }
            int startIndex = (page - 1) * pageSize;
            int endIndex = startIndex + pageSize;

            if (startIndex > hits.length()) {
                return null;
            }

            if (endIndex > hits.length()) {
                endIndex = hits.length();
            }


            try {
                for (int i = startIndex; i < endIndex; i++) {
                    Document d = hits.doc(i);
                    searchResults.add(new SearchResult(d));
                }
            } catch (IOException e) {
                // todo
            }
        }
        return searchResults;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}


/**
 queryString (String)
 analyzer (Analyzer)
 abstract (String)

        Query query = QueryParser.parse(queryString, "text", analyzer);

        Hits hits = searcher.search(query, new Sort("type", true));

        QueryScorer scorer = new QueryScorer(query);
        SimpleHTMLFormatter formatter =
                new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
        Highlighter highlighter = new Highlighter(formatter, scorer);
        Fragmenter fragmenter = new NonFragmenter();
        highlighter.setTextFragmenter(fragmenter);

        if (abs != null) {
            TokenStream stream = new SimpleAnalyzer().tokenStream("abstract", new StringReader(abs));
            String formattedAbs = highlighter.getBestFragment(stream, abs);
            if (formattedAbs != null && !formattedAbs.equalsIgnoreCase("")) {
                abs = formattedAbs;
            }
        }
 */
