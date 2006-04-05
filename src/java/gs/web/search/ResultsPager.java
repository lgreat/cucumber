package gs.web.search;

import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;

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

    private static ISchoolDao _schoolDao;
    private Hits _schoolHits;
    private Hits _hits;
    private Searcher _searcher;
    private Query _explanationQuery;


    /** Constant to indicate that the page has mixed results */
    private static final int MIXED_PAGE  = 0;

    /** Constant to indicate that the page has only school results */
    private static final int SCHOOL_PAGE = 1;

    private static StateManager _stateManager = new StateManager();
    private static final Logger _log = Logger.getLogger(ResultsPager.class);

    public ResultsPager(Hits hits, String constraint) {
        _hits = hits;
        if (hits != null && constraint != null) {
            if (constraint.equals("school")) {
                _schoolHits = hits;
            }
        }
    }

    private static ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            String[] paths = {"gs/data/applicationContext-data.xml",
                    "gs/data/dao/hibernate/applicationContext-hibernate.xml",
                    "gs/data/school/performance/applicationContext-performance.xml",
                    "applicationContext.xml",
                    "modules-servlet.xml",
                    "pages-servlet.xml"
            };
            ApplicationContext _applicationContext = new ClassPathXmlApplicationContext(paths);
            _schoolDao = (ISchoolDao) _applicationContext.getBean(ISchoolDao.BEAN_ID);
        }
        return _schoolDao;
    }

    public void enableExplanation(Searcher searcher, Query q) {
        _searcher = searcher;
        _explanationQuery = q;
    }

    /**
     * Returns page of hits encapsulated as a <code>List</code> of
     * <code>SearchResult</code> objects
     * @param page
     * @param pageSize
     * @return a <code>java.util.List</code> type
     */
    public List getResults(int page, int pageSize) {
        return getPage(_hits, page,  pageSize, MIXED_PAGE);
    }

    public List getSchools(int page, int pageSize) {
        return getPage(_schoolHits, page,  pageSize, SCHOOL_PAGE);
    }

    public int getSchoolsTotal() {
        if (_schoolHits == null) return 0;
        return _schoolHits.length();
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
    List getPage(Hits hits, int page, int pageSize, int type) {
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

                    if (type == MIXED_PAGE) {
                        SearchResult sr = new SearchResult(d);
                        if (_searcher != null) {
                            sr.setExplanation(_searcher.explain(_explanationQuery, _hits.id(i)));
                        }
                        searchResults.add(sr);
                    } else {
                        State state = _stateManager.getState(d.get("state"));
                        if (state != null) {
                            String id = d.get("id");
                            if(StringUtils.isNotBlank(id)) {
                                searchResults.add(getSchoolDao().getSchoolById(state,
                                        Integer.valueOf(id)));
                            }
                        }
                    }

                }
            } catch (IOException e) {
                _log.warn(e);
            }
        }
        return searchResults;
    }
}

