package gs.web.search;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.SpringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hit;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.io.IOException;
import java.util.*;

/**
 * This class handles the organization of <code>Hits</code> into
 * page-ready parcels that can be retrieved using the <code>getPage(..)</code>
 * method.
 *
 * @author Chris Kimm<mailto:chriskimm@greatschools.net>
 * @noinspection CanBeFinal
 */
public class ResultsPager {
    private static final Logger _log = Logger.getLogger(ResultsPager.class);
    private static StateManager _stateManager;
    private static ISchoolDao _schoolDao;
    private Hits _hits;
    private ResultType _type;
    private Searcher _searcher;
    private Query _explanationQuery;
    private List<Hit> _hitList;

    static {
        _stateManager = (StateManager) SpringUtil.getApplicationContext().getBean(StateManager.BEAN_ID);
        _schoolDao = (ISchoolDao) SpringUtil.getApplicationContext().getBean(ISchoolDao.BEAN_ID);
    }


    /**
     * Spring bean id
     */
    public static final String BEAN_ID = "resultsPager";

    public StateManager getStateManager() {
        return _stateManager;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public enum ResultType {
        school,
        topic
    }

    public ResultsPager(Hits hits, ResultType type) {
        _hits = hits;
        _type = type;
    }

    public ResultsPager(Hits hits, ResultType type, Comparator comparator) throws ClassCastException{
        this(hits, type);

        if (_hits != null){
         _hitList = new ArrayList<Hit>();

            for(Iterator<Hit> iter = _hits.iterator(); iter.hasNext(); ) {
                Hit h = iter.next();
                _hitList.add(h);
            }
            Collections.sort(_hitList, comparator);           
        }
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public void enableExplanation(Searcher searcher, Query q) {
        _searcher = searcher;
        _explanationQuery = q;
    }

    /**
     * Returns page of hits encapsulated as a <code>List</code> of
     * <code>SearchResult</code> objects
     *
     * @return If _type is SCHOOLS, a List of School objects. Otherwise, a List of SearchResult objects 
     * @param page  The numbered page to retrieve. 1 for the first page, 2 for the second, etc.
     * @param pageSize  The number of results to include in each page
     */
    public List<Object> getResults(int page, int pageSize) {
        List<Object> searchResults = new ArrayList<Object>();
        if (_hits != null) {
            if (page < 1) {
                page = 1;
            }

            int startIndex = pageSize > 0 ? (page - 1) * pageSize : 0;
            int endIndex = pageSize > 0 ? startIndex + pageSize : _hits.length();

            if (startIndex > _hits.length()) {
                return null;
            }

            if (endIndex > _hits.length()) {
                endIndex = _hits.length();
            }

            try {

                for (int i = startIndex; i < endIndex; i++){
                    Document d;
                    Hit hit = null;

                    // if the _hitList, the results aren't specially sorted.  So acceess _hits to get the document
                    if (_hitList != null){
                        hit = _hitList.get(i);
                        d = hit.getDocument();
                    } else {
                        d = _hits.doc(i);
                    }

                    if (_type == ResultType.school) {
                        State state = _stateManager.getState(d.get("state"));
                        if (state != null) {
                            String id = d.get("id");
                            if (StringUtils.isNotBlank(id)) {
                                try {
                                    final School schoolById = _schoolDao.getSchoolById(state,
                                            Integer.valueOf(id));
                                    SchoolSearchResult result = new SchoolSearchResult(d);
                                    result.setSchool(schoolById);
                                    searchResults.add(result);
                                } catch (NumberFormatException e) {
                                    _log.warn("Couldn't find school " + id + " in " + state, e);
                                } catch (ObjectRetrievalFailureException e) {
                                    _log.error("Couldn't find school " + id + " in " + state, e);
                                }
                            }
                        }
                    } else {
                        SearchResult sr = new SearchResult(d);
                        if (_searcher != null) {

                            // if hit is null, then the list is not specially sorted and should get the explaination using _hits...
                            if (hit != null){
                                sr.setExplanation(_searcher.explain(_explanationQuery, hit.getId()));
                            } else {
                                sr.setExplanation(_searcher.explain(_explanationQuery, _hits.id(i)));
                            }
                        }
                        searchResults.add(sr);
                    }

                }
            } catch (IOException e) {
                _log.warn(e);
            }
        }
        return searchResults;
    }

    public ResultType getType() {
        return _type;
    }
}

