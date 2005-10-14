package gs.web.jsp;

import gs.web.SessionContext;
import gs.web.ISessionFacade;
import gs.web.search.SearchResult;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.content.IArticleDao;
import gs.data.content.Article;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class BaseTagHandler extends SimpleTagSupport {

    private static final Logger _log = Logger.getLogger(BaseTagHandler.class);
    private static ISchoolDao _schoolDao;
    private static IArticleDao _articleDao;
    private static StateManager _stateManager = new StateManager();

    protected ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            try {
                ISessionFacade sc = getSessionContext();
                if (sc != null) {
                    _schoolDao = sc.getSchoolDao();
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
                    _articleDao = sc.getArticleDao();
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
            sc = (ISessionFacade) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
        }
        return sc;
    }

    /**
     * @return The current <code>State</code> based on knowledge of location
     * awareness in the <code>SessionConetext</code> object, or null if there
     * is no current location awareness.
     */
    protected State getState() {
        ISessionFacade sc = getSessionContext();
        State state = null; //State.CA;
        if (sc != null) {
            state = sc.getState();
        }
        return state;
    }

    /**
     * Another convenience method to get the hostname.
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
     * @param out
     * @throws IOException
     */
    protected void writeBrowseAllArticlesLink(JspWriter out) throws IOException {
        out.print("<a href=\"http://");
        out.print(getHostname());
        out.print("/content/allArticles.page?state=");
        out.print(getStateOrDefault().getAbbreviation());
        out.print("\">Browse all articles</a>");
    }
}
