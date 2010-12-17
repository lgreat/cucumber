package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.school.ISchoolDao;
import gs.data.school.Grade;
import gs.data.school.School;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class AccountInformationAjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/accountInformationAjax.page";

    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private StateManager _stateManager;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();

        String city = request.getParameter("city");
        State state = _stateManager.getState(request.getParameter("state"));
        String grade = request.getParameter("grade");
        if (grade != null && city != null) {
            outputSchoolSelect(state, city, grade, out);
        } else {
            boolean showNotListed = true;
            if (request.getParameter("showNotListed") != null && !Boolean.valueOf(request.getParameter("showNotListed"))) {
                showNotListed = false;
            }

            outputCitySelect(state, out, showNotListed);
        }

        return null;
    }

    protected void outputSchoolSelect(State state, String city, String grade, PrintWriter out) {
        List<School> schools = _schoolDao.findSchoolsInCityByGrade(state, city, Grade.getGradeLevel(grade));
        outputOption(out, "-2", "--", true);
        outputOption(out, "-1", "My child's school is not listed");
        for (School school : schools) {
            String idString = ((school.getId() != null) ? school.getId().toString() : "");
            outputOption(out, idString, school.getName());
        }
    }

    protected void outputCitySelect(State state, PrintWriter out, boolean showNotListed) {
        List<City> cities = _geoDao.findCitiesByState(state);
        if (showNotListed) {
            City notListed = new City();
            notListed.setName("My city is not listed");
            cities.add(0, notListed);
        }
        if (cities.size() > 0) {
            outputOption(out, "", "Choose city", true);
            for (City city : cities) {
                outputOption(out, city.getName(), city.getName());
            }
        }
    }

    protected void outputOption(PrintWriter out, String value, String name) {
        outputOption(out, value, name, false);
    }

    protected void outputOption(PrintWriter out, String value, String name, boolean selected) {
        out.print("<option ");
        if (selected) {
            out.print("selected=\"selected\" ");
        }
        out.print("value=\"" + value + "\">");
        out.print(name);
        out.print("</option>");
    }
    
    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
