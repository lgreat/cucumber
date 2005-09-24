package gs.web.jsp;

import gs.web.SessionContext;
import gs.web.search.SearchResult;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;

import org.apache.log4j.Logger;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class BaseTagHandler extends SimpleTagSupport {

    private static final Logger _log = Logger.getLogger(BaseTagHandler.class);
    private ISchoolDao _schoolDao;
    private static StateManager _stateManager = new StateManager();

    protected ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            try {
                JspContext jspContext = getJspContext();

                if (jspContext != null) {
                    SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
                    if (sc != null) {
                        _schoolDao = sc.getSchoolDao();
                    }
                }

            } catch (Exception e) {
                _log.warn("problem getting ISchoolDao: ", e);
            }
        }
        return _schoolDao;
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

    protected String escapleLongstate(String title) {

        String stateString = " your state ";
        State s = getState();
        if (s != null) {
            stateString = s.getLongName();
        }

        String outString = title.replace('$', ' ');
        return outString.replaceAll("LONGSTATE", stateString);
    }

    /**
     * @return The current <code>State</code> based on knowledge of location
     * awareness in the <code>SessionConetext</code> object, or null if there
     * is no current location awareness.
     */
    protected State getState() {
        JspContext jspContext = getJspContext();
        State s = null;
        if (jspContext != null) {
            SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
            if (sc != null) {
                s = sc.getState();
            }
        }
        return s;
    }
}
