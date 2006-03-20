package gs.web.search;

import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;

/**
 * This class handles the organization of <code>Hits</code> into
 * page-ready parcels that can be retrieved using the <code>getPage(..)</code>
 * method.
 * @author Chris Kimm<mailto:chriskimm@greatschools.net>
 * @noinspection CanBeFinal
 */
public class ResultsPager {

    /** Spring bean id */
    public static final String BEAN_ID = "resultsPager";

    private ISchoolDao _schoolDao;
    private Hits _schoolHits;
    private Hits _articleHits;
    private Hits _districtHits;
    private Hits _termHits;
    private Hits _cityHits;
    private Hits _hits;
    private Searcher _searcher;
    private Query _explanationQuery;

    private static StateManager _stateManager = new StateManager();
    private static final Logger _log = Logger.getLogger(ResultsPager.class);

    public void enableExplanation(Searcher searcher, Query q) {
        _searcher = searcher;
        _explanationQuery = q;
    }

    public void load(Hits hits, String constraint) {
        _hits = hits;
        if (hits != null && constraint != null) {
            if (constraint.equals("school")) {
                _schoolHits = hits;
            } else if (constraint.equals("article")) {
                _articleHits = hits;
            } else if (constraint.equals("city")) {
                setCities(hits);
            } else if (constraint.equals("term")) {
                setTerms(hits);
            } else {
                setDistricts(hits);
            }
        }
    }

    public List getResults(int page, int pageSize) {
        return getPage(_hits, page,  pageSize);
    }

    public List getSchools(int page, int pageSize) {
        List schools = new ArrayList();
        if (_schoolHits != null) {
            if (page < 1) {
                page = 1;
            }

            int startIndex = pageSize > 0 ? (page - 1) * pageSize : 0;
            int endIndex =   pageSize > 0 ? startIndex + pageSize : _schoolHits.length();

            if (startIndex > _schoolHits.length()) {
                return null;
            }

            if (endIndex > _schoolHits.length()) {
                endIndex = _schoolHits.length();
            }

            try {
                for (int i = startIndex; i < endIndex; i++) {
                    Document d = _schoolHits.doc(i);
                    try {
                        State state = _stateManager.getState(d.get("state"));
                        if (state != null) {
                            schools.add(_schoolDao.getSchoolById(state,
                                    Integer.valueOf(d.get("id"))));
                        }
                    } catch (Exception e) {
                        _log.warn("error retrieving school: ", e);
                    }
                }
            } catch (IOException e) {
                _log.warn(e);
            }
        }
        return schools;
    }

    public int getSchoolsTotal() {
        if (_schoolHits == null) return 0;
        return _schoolHits.length();
    }

    private void setDistricts(Hits hits) {
        _districtHits = hits;
    }

    private int getArticlesTotal() {
        if (_articleHits == null) return 0;
        return _articleHits.length();
    }

    private int getDistrictsTotal() {
        if (_districtHits == null) return 0;
        return _districtHits.length();
    }

    private void setCities(Hits hits) {
        _cityHits = hits;
    }

    private int getCitiesTotal() {
        if (_cityHits == null) return 0;
        return _cityHits.length();
    }

    private void setTerms(Hits hits) {
        _termHits = hits;
    }

    private int getTermsTotal() {
        if (_termHits == null) return 0;
        return _termHits.length();
    }

    public int getResultsTotal() {
        return getSchoolsTotal() + getArticlesTotal() + getCitiesTotal() +
                getDistrictsTotal() + getTermsTotal();
    }

    /**
     * This method returns a List of <code>SearchResults</code> of pageSize
     * length based on the supplied page parameter.  Always returns a non-null
     * <code>java.util.List</code> object
     * @param hits
     * @param page
     * @param pageSize
     * @return A sub<code>List</code> containing
     */
    List getPage(Hits hits, int page, int pageSize) {
        List searchResults = new ArrayList();
        if (hits != null) {
            if (page < 1) {
                page = 1;
            }

            int startIndex = pageSize > 0 ? (page - 1) * pageSize : 0;
            int endIndex =   pageSize > 0 ? startIndex + pageSize : hits.length();

            if (startIndex > hits.length()) {
                return null;
            }

            if (endIndex > hits.length()) {
                endIndex = hits.length();
            }

            try {
                for (int i = startIndex; i < endIndex; i++) {
                    Document d = hits.doc(i);
                    SearchResult sr = new SearchResult(d);
                    if (_searcher != null) {
                        sr.setExplanation(_searcher.explain(_explanationQuery, _hits.id(i)));
                    }
                    searchResults.add(sr);
                }
            } catch (IOException e) {
                _log.warn(e);
            }
        }
        return searchResults;
    }

    /**
     * Spring setter
     * @param schoolDao
     */
    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}

