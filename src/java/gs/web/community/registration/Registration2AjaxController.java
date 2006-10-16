package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;
import gs.data.school.School;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Registration2AjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/registration2Ajax.page";
    public static final int MAX_RESULTS_TO_RETURN = 10;

    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String param3 = request.getParameter("param3");
        String fn = request.getParameter("fn");
        String q = request.getParameter("q");
        State state = _stateManager.getState(request.getParameter("state"));

        PrintWriter out = response.getWriter();

        List schools = _schoolDao.findSchoolLike(state, q);

        if (schools != null && schools.size() > 0) {
            for (int x=0; x < schools.size() && x < MAX_RESULTS_TO_RETURN; x++) {
                School school = (School) schools.get(x);
                out.print("<div ");
                out.print("onSelect=\"" + fn + "('" + school.getId() + "', ");
                out.print("'" + school.getName() + "'");
                if (param3 != null) {
                    out.print(", '" + param3 + "'");
                }
                out.print(");\">");
                out.print(school.getName());
                out.println("</div>");
            }
        } else {
            out.print("No matching school found");
        }
        
        return null;
    }
}