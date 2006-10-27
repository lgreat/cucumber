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
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;

/**
 * The AJAX controller for registration stage 2.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Registration2AjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/registration2Ajax.page";
    public static final int MAX_RESULTS_TO_RETURN = 10;

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
        String childNum = request.getParameter("childNum");

        _log.info("city=" + city);
        _log.info("state=" + state);
        _log.info("grade=" + grade);
        _log.info("childNum=" + childNum);

        PrintWriter out = response.getWriter();
        try {
            if (city != null) {
                List schools = _schoolDao.findSchoolsInCityByGrade(state, city, Grade.getGradeLevel(grade));
                School notListed = new School();
                notListed.setName("My school is not listed");
                schools.add(0, notListed);
                out.print("<select name=\"school" + childNum + "\" id=\"school" + childNum);
                out.println("\" class=\"form school\">");
                outputOption(out, "", "--", true);
                for (int x=0; x < schools.size(); x++) {
                    School school = (School) schools.get(x);
                    String idString = ((school.getId() != null)?school.getId().toString():"");
                    outputOption(out, idString, school.getName());
                }
                out.print("</select>");
            } else {
                List cities = _geoDao.findCitiesByState(state);
                City notListed = new City();
                notListed.setName("My city is not listed");
                cities.add(0, notListed);
                out.print("<select id=\"citySelect\" name=\"city\" class=\"city\"");
                out.print(" onchange=\"cityChange(this, " + childNum + ");\">");
                outputOption(out, "", "--", true);
                for (int x=0; x < cities.size(); x++) {
                    ICity icity = (ICity) cities.get(x);
                    outputOption(out, icity.getName(), icity.getName());
                }
                out.print("</select>");
            }
        } catch (Exception e) {
            out.print("Error retrieving results");
        }
        return null;
    }

    protected void outputOption(PrintWriter out, String value, String name) {
        outputOption(out, value, name, false);
    }

    protected void outputOption(PrintWriter out, String value, String name, boolean selected) {
        out.print("<option ");
        if (selected) {
            out.print("selected ");
        }
        out.print("value=\"" + value + "\">");
        out.print(name);
        out.print("</option>");
    }

}