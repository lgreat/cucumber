package gs.web.search;

import org.apache.lucene.search.Hits;
import org.apache.lucene.document.Document;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import gs.data.state.StateManager;

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
    private Hits _termHits;
    private Hits _cityHits;
    private String _query;
    private StateManager _stateManager;
    private static final Logger _log = Logger.getLogger(ResultsPager.class);

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

    public void setCities(Hits hits) {
        _cityHits = hits;
    }

    public List getCities(int page, int pageSize) {
        return getPage(_cityHits, page, pageSize);
    }

    public int getCitiesTotal() {
        if (_cityHits == null) return 0;
        return _cityHits.length();
    }

    public void setTerms(Hits hits) {
        _termHits = hits;
    }

    public List getTerms(int page, int pageSize) {
        return getPage(_termHits, page, pageSize);
    }

    public int getTermsTotal() {
        if (_termHits == null) return 0;
        return _termHits.length();
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
                    searchResults.add(new SearchResult(d, _query));
                }
            } catch (IOException e) {
                _log.warn(e);
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

    /**
     * This is the query string that created the results loaded by this
     * pager.
     * @param q
     */
    public void setQuery(String q) {
        _query = q;
    }
}

