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

import java.util.Enumeration;

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
        State state = null; //State.CA;
        System.out.println ("BaseTagHandler.getState() foo 1");
        if (jspContext != null) {
            System.out.println ("BaseTagHandler.getState() bar 2");

            Enumeration e1 = jspContext.getAttributeNamesInScope(PageContext.REQUEST_SCOPE);
            while(e1.hasMoreElements()) {
                System.out.println ("request attribute: " + e1.nextElement());
            }

            Enumeration e3 = jspContext.getAttributeNamesInScope(PageContext.APPLICATION_SCOPE);
            while(e3.hasMoreElements()) {
                System.out.println ("application attribute: " + e3.nextElement());
            }

            Enumeration e2 = jspContext.getAttributeNamesInScope(PageContext.SESSION_SCOPE);
            while(e2.hasMoreElements()) {
                System.out.println ("session attribute: " + e2.nextElement());
            }

            String stateString = (String)jspContext.getAttribute("state");
            if (stateString != null) {
                // ok to do this since null can be returned
                System.out.println ("BaseTagHandler.getState() 3");
                state = _stateManager.getState(stateString);
            }
            /*
            SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
            if (sc != null) {
                State s = sc.getState();
                if (s != null) {
                    state = s;
                }
            }
            */
        }
        //_log.info("setting state in BaseTagHandler to: " + state.toString());
        System.out.println("setting state in BaseTagHandler to: " + state);
        return state;
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
}
