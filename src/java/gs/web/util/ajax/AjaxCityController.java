package gs.web.util.ajax;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import gs.data.school.ISchoolDao;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.geo.ICity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

/**
 * General ajax controller for city/school lists
 */
public class AjaxCityController  implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/util/ajax/ajaxCity.page";

    private ISchoolDao _schoolDao;
    private StateManager _stateManager;
    private IGeoDao _geoDao;

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
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

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String city = request.getParameter("city");
        State state = _stateManager.getState(request.getParameter("state"));
        String grade = request.getParameter("grade");
        String notListedOption = request.getParameter("notListedOption");

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try {
            if (city != null) {
                outputSchoolOptions(state, city, grade, out, notListedOption);
            } else {
                outputCityOptions(state, out, notListedOption);
            }
        } catch (Exception e) {
            out.print("<option>Error retrieving results</option>");
        }
        return null;
    }

    protected void outputCityOptions(State state, PrintWriter out, String notListedOption) {
        List<City> cities = _geoDao.findCitiesByState(state);
        if (StringUtils.isBlank(notListedOption)) {
            notListedOption = "--";
        }
        outputOption(out, "", notListedOption, true);

        City notListed = new City();
        notListed.setName("My city is not listed");
        cities.add(0, notListed);
        for (ICity icity : cities) {
            outputOption(out, icity.getName(), icity.getName());
        }
        out.print("</select>");
    }

    protected void outputSchoolOptions(State state, String city, String grade, PrintWriter out, String notListedOption) {
        List<School> schools;
        if (StringUtils.isNotBlank(grade)) {
            schools = _schoolDao.findSchoolsInCityByGrade(state, city, Grade.getGradeLevel(grade));
        } else {
            String[] types = {"public","charter"};
            schools = _schoolDao.findSchoolsInCityByType(state, city, 2000,types); // 2000 is arbitrary - CK
        }
        if (StringUtils.isBlank(notListedOption)) {
            notListedOption = "- Choose School -";
        }
        outputOption(out, "0", notListedOption, true);
        outputOption(out, "-1", "My child's school is not listed");
        for (School school : schools) {
            String idString = ((school.getId() != null) ? school.getId().toString() : "");
            outputOption(out, idString, school.getName());
        }
        out.print("</select>");
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

}