package gs.web.jsp;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.ISessionFacade;
import gs.web.SessionContext;
import gs.web.search.SearchResult;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.functions.Functions;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * This abstract class provides access to varios DAOs.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class BaseTagHandler extends SimpleTagSupport {

    protected static final Logger _log = Logger.getLogger(BaseTagHandler.class);
    private static ISchoolDao _schoolDao;
    private static IArticleDao _articleDao;
    private static IDistrictDao _districtDao;
    private static StateManager _stateManager = new StateManager();
    protected String _query = "";

    /**
     * @return <code>ISchoolDao</code>
     */
    protected ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            try {
                ISessionFacade sc = getSessionContext();
                if (sc != null) {
                    _schoolDao = (ISchoolDao) sc.getApplicationContext().getBean(ISchoolDao.BEAN_ID);
                }
            } catch (Exception e) {
                _log.warn("problem getting ISchoolDao: ", e);
            }
        }
        return _schoolDao;
    }

    protected IArticleDao getArticleDao() {
        if (_articleDao == null) {
            try {
                ISessionFacade sc = getSessionContext();
                if (sc != null) {
                    _articleDao = (IArticleDao) sc.getApplicationContext().getBean(IArticleDao.BEAN_ID);
                }
            } catch (Exception e) {
                _log.warn("problem getting IArticleDao: ", e);
            }
        }
        return _articleDao;
    }

    protected Article getArticle(SearchResult sr) {
        return getArticleDao().getArticleFromId(Integer.decode(sr.getId()));
    }

    protected School getSchool(SearchResult sr) {
        School school = null;
        try {
            State state = _stateManager.getState(sr.getState());
            if (state != null) {
                school = getSchoolDao().getSchoolById(state, Integer.valueOf(sr.getId()));
            }
        } catch (Exception e) {
            _log.warn("error retrieving school: ", e);
        }
        return school;
    }

    /**
     * @return <code>IDistrictDao</code>
     */
    protected IDistrictDao getDistrictDao() {
        if (_districtDao == null) {
            try {
                ISessionFacade sc = getSessionContext();
                if (sc != null) {
                    _districtDao = (IDistrictDao) sc.getApplicationContext().getBean(IDistrictDao.BEAN_ID);
                }
            } catch (Exception e) {
                _log.warn("problem getting IDistrictDao: ", e);
            }
        }
        return _districtDao;
    }

    protected String escapeLongstate(String title) {
        String stateString = getStateOrDefault().getLongName();
        String outString = title.replace('$', ' ');
        return outString.replaceAll("LONGSTATE", stateString);
    }

    protected ISessionFacade getSessionContext() {
        JspContext jspContext = getJspContext();
        ISessionFacade sc = null;
        if (jspContext != null) {
            //String o = (String)jspContext.findAttribute("state"); // why doesn't this work?
            sc = (ISessionFacade) jspContext.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, PageContext.REQUEST_SCOPE);
        }
        return sc;
    }

    /**
     * @return The current <code>State</code> based on knowledge of location
     *         awareness in the <code>SessionConetext</code> object, or CA if there
     *         is no current location awareness.
     */
    protected State getState() {
        ISessionFacade sc = getSessionContext();
        State state = State.CA;
        if (sc != null) {
            state = sc.getStateOrDefault();
        }
        return state;
    }

    /**
     * Another convenience method to get the hostname.
     *
     * @return <code>String</code>
     */
    protected String getHostname() {
        ISessionFacade sc = getSessionContext();
        if (sc != null) {
            return sc.getHostName();
        }
        return null;
    }

    /**
     * @return a non-null <code>State</code> object.
     */
    protected State getStateOrDefault() {
        State s = getState();
        if (s == null) {
            s = State.CA;
        }
        return s;
    }

    /**
     * Writes an <a href> tag which links to the all articles page.
     *
     * @throws IOException
     */
    protected void writeBrowseAllArticlesLink(JspWriter out) throws IOException {
        out.print("<a href=\"http://");
        out.print(getHostname());
        out.print("/content/allArticles.page?state=");
        out.print(getStateOrDefault().getAbbreviation());
        out.print("\">Browse all articles</a>");
    }

    public void setQuery(String q) {
        _query = Functions.escapeXml(q);
    }
}
