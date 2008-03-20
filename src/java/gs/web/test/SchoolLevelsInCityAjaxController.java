package gs.web.test;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.PrintWriter;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;

/**
 * This class provides is a service provider for ajax requests from /test/landing.page
 * for school lists.  The required request parameters are:
 *     state: a 2-letter abbreviation
 *     city: the cannonical city name
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolLevelsInCityAjaxController implements Controller {

    public static final String BEAN_ID = "/test/schoolLevelsInCity.page";
    private ISchoolDao _schoolDao;
    private static final StateManager _stateManager = new StateManager();

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        State state = _stateManager.getState(request.getParameter("state"));
        String city = request.getParameter("city");
        List<School> schools = _schoolDao.findSchoolsInCity(state, city, false);

        Set<LevelCode.Level> levels = new TreeSet<LevelCode.Level>();
        
        for (School school : schools) {
            levels.addAll(school.getLevelCode().getIndividualLevelCodes());
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"levels\":[");
        for (Iterator<LevelCode.Level> iter = levels.iterator(); iter.hasNext();) {
            LevelCode.Level lev = iter.next();
            json.append("\"");
            json.append(lev.getName());
            json.append("\"");
            if (iter.hasNext()) {
                json.append(",");
            }
        }
        json.append("]}");
        out.print(json.toString());
        return null;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}