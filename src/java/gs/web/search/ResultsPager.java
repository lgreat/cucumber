package gs.web.search;

import org.apache.lucene.search.Hits;
import org.apache.lucene.document.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.content.IArticleDao;
import gs.data.state.StateManager;
import gs.data.state.State;

/**
 * Created by IntelliJ IDEA.
 * User: Bishop
 * Date: Aug 6, 2005
 * Time: 8:47:16 AM
 */
public class ResultsPager {

    private Hits _hits;
    private List _articles;
    private List _schools;
    private boolean _loaded = false;
    private ISchoolDao _schoolDao;
    private IArticleDao _articleDao;
    private StateManager _stateManager;

    private static final Log _log = LogFactory.getLog(ResultsPager.class);

    public ResultsPager (){    }

    public ResultsPager(Hits hits) {
        _hits = hits;
    }

    public List getArticles (int page, int pageSize) {
        checkLoaded();
        return getPage(_articles, page, pageSize);
    }

    public int getArticlesTotal() {
        checkLoaded();
        return _articles.size();
    }

    public List getSchools(int page, int pageSize){
        checkLoaded();
        return getPage(_schools, page, pageSize);
    }

    /**
     *
     * @param list
     * @param page
     * @param pageSize
     * @return A sub<code>List</code> containing
     */
    private List getPage(List list, int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        int startIndex = (page-1) * pageSize;
        int endIndex = startIndex + pageSize;

        if (startIndex > list.size()) {
            // undefined - return null
            return null;
        }

        if (endIndex > list.size()) {
            endIndex = list.size();
        }
        return list.subList(startIndex, endIndex);
    }

    public int getSchoolsTotal() {
        checkLoaded();
        return _schools.size();
    }

    private void checkLoaded() {
        if (!_loaded) {

            _articles = new ArrayList();
            _schools = new ArrayList();

            if (_hits != null) {
                try {
                    for (int i = 0; i < _hits.length(); i++) {
                        Document doc = _hits.doc(i);
                        String type = doc.get("type");
                        if (type.equals("school")) {
                            if ("true".equals(doc.get("active"))) {
                                _schools.add(new SchoolResult(doc));
                            }
                        } else if (type.equals("article")) {
                            _articles.add(new ArticleResult(doc));
                        }
                    }
                } catch (Exception e) {
                    _log.warn(e);
                }
            }
            _loaded = true;
        }
    }

    public void setHits(Hits hits) {
        _hits = hits;
        _loaded = false;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao _schoolDao) {
        this._schoolDao = _schoolDao;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao _articleDao) {
        this._articleDao = _articleDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
