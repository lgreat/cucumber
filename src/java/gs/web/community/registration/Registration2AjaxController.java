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
        String function = request.getParameter("fn");
        String query = request.getParameter("q");
        State state = _stateManager.getState(request.getParameter("state"));

        PrintWriter out = response.getWriter();

        handleRequestWithParams(out, state, param3, function, query);
        return null;
    }

    protected void handleRequestWithParams(PrintWriter out, State state, String param3,
                                           String function, String query) {
        List schools = _schoolDao.findSchoolLike(state, query);

        if (schools != null && schools.size() > 0) {
            for (int x=0; x < schools.size() && x < MAX_RESULTS_TO_RETURN; x++) {
                School school = (School) schools.get(x);
                out.print(getSchoolDiv(function, school.getId(), school.getName(), param3));
            }
        } else {
            out.print("No matching school found");
        }
    }

    protected String getSchoolDiv(String function, Integer id, String name, String param3) {
        StringBuffer rval = new StringBuffer();
        rval.append("<div ");
        rval.append("onSelect=\"").append(function).append("('").append(id).append("', ");
        rval.append("'").append(name).append("'");
        if (param3 != null) {
            rval.append(", '").append(param3).append("'");
        }
        rval.append(");\">");
        rval.append(name);
        rval.append("</div>");

        return rval.toString();
    }
}