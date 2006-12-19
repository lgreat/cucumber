package gs.web.jsp;

import gs.data.content.IArticleDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.util.context.ISessionContext;
import gs.web.util.context.SessionContext;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.functions.Functions;
import org.springframework.context.ApplicationContext;

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
    protected String _query = "";
    private ApplicationContext _applicationContext;

    private ApplicationContext getApplicationContext() {
        if (_applicationContext == null) {
            ISessionContext sc = getSessionContext();
            _applicationContext = sc.getApplicationContext();
        }
        return _applicationContext;
    }

    /**
     * Used for unit testing.
     */
    public void setApplicationContext(ApplicationContext context) {
        _applicationContext = context;
    }

    /**
     * @return <code>ISchoolDao</code>
     */
    protected ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            _schoolDao = (ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        }
        return _schoolDao;
    }

    protected IArticleDao getArticleDao() {
        if (_articleDao == null) {
            _articleDao = (IArticleDao) getApplicationContext().getBean(IArticleDao.BEAN_ID);
        }
        return _articleDao;
    }

    /**
     * Returns a <code>School</code> obeject from the provided state.  If either
     * the state or id parameters are null, null will be returned.  Excepections
     * throws by the schoolDao are swallowed and logged in this method.
     *
     * @return a <code>School</code> object or null
     */
    protected School getSchool(State state, Integer id) {
        School school = null;
        if (state != null && id != null) {
            try {
                school = getSchoolDao().getSchoolById(state, id);
            } catch (Exception e) {
                _log.warn("error retrieving school: ", e);
            }
        }
        return school;
    }

    /**
     * @return <code>IDistrictDao</code>
     */
    protected IDistrictDao getDistrictDao() {
        if (_districtDao == null) {
            _districtDao = (IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID);
        }
        return _districtDao;
    }

    protected String escapeLongstate(String title) {
        String stateString = getStateOrDefault().getLongName();
        String outString = title.replace('$', ' ');
        return outString.replaceAll("LONGSTATE", stateString);
    }

    protected ISessionContext getSessionContext() {
        JspContext jspContext = getJspContext();
        ISessionContext sc = null;
        if (jspContext != null) {
            sc = (ISessionContext) jspContext.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, PageContext.REQUEST_SCOPE);
        }
        return sc;
    }

    /**
     * @return The current <code>State</code> based on knowledge of location
     *         awareness in the <code>SessionConetext</code> object, or CA if there
     *         is no current location awareness.
     */
    protected State getState() {
        ISessionContext sc = getSessionContext();
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
        ISessionContext sc = getSessionContext();
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
     * @todo rewrite to use UrlBuilder (which needs the request)
     */
    protected void writeBrowseAllArticlesLink(JspWriter out) throws IOException {
        //UrlBuilder builder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, getStateOrDefault());
        //out.print(builder.asAHref(request, "Browse all articles"));

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
