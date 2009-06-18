package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

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

        PrintWriter out = response.getWriter();
        try {
            if (city != null) {
                String onChange = request.getParameter("onchange");
                String onClick = request.getParameter("onclick");
                outputSchoolSelect(state, city, grade, out, childNum, onChange, onClick);
            } else {
                outputCitySelect(state, out, childNum);
            }
        } catch (Exception e) {
            out.print("Error retrieving results");
        }
        return null;
    }

    protected void outputCitySelect(State state, PrintWriter out, String childNum) {
        List<City> cities = _geoDao.findCitiesByState(state);
        City notListed = new City();
        notListed.setName("My city is not listed");
        cities.add(0, notListed);
        if (StringUtils.isNotBlank(childNum)) {
            openSelectTag(out, "citySelect" + childNum, "citySelect" + childNum, "selectChildCity", "cityChange(this, " + childNum + ");", null);
        } else {
            openSelectTag(out, "citySelect", "citySelect", "selectChildCity", "cityChange(this);", null);
        }
        outputOption(out, "", "--", true);
        for (ICity icity : cities) {
            outputOption(out, icity.getName(), icity.getName());
        }
        out.print("</select>");
    }

    protected void outputSchoolSelect(State state, String city, String grade, PrintWriter out, String childNum, String onChange, String onClick) {
        List<School> schools;
        boolean selectedSchools = false;
        if (state != null && !StringUtils.isBlank(city)) {
            if (StringUtils.isNotBlank(grade)) {
                schools = _schoolDao.findSchoolsInCityByGrade(state, city, Grade.getGradeLevel(grade));
            } else {
                schools = _schoolDao.findSchoolsInCity(state, city, 2000); // 2000 is arbitrary - CK
            }

            selectedSchools = true;
        } else {
            schools = new ArrayList<School>();
        }

        if (StringUtils.isNotBlank(childNum)) {
            openSelectTag(out, "school" + childNum, "school" + childNum, "selectChildSchool", onChange, onClick);
        } else {
            openSelectTag(out, "school", "school", "selectChildSchool", onChange, onClick);
        }
        outputOption(out, "", "- Choose School -", true);
        if (selectedSchools) {
            outputOption(out, "-1", "My child's school is not listed");
        }
        for(School school : schools) {
            String idString = ((school.getId() != null)?school.getId().toString():"");
            outputOption(out, idString, school.getName());
        }
        out.print("</select>");
    }

    protected void openSelectTag(PrintWriter out, String name, String cssId, String cssClass, String onChange, String onClick) {
        out.print("<select");
        out.print(" name=\"" + name + "\"");
        if (StringUtils.isNotEmpty(cssId)) {
            out.print(" id=\"" + cssId + "\"");
        }
        if (StringUtils.isNotEmpty(cssClass)) {
            out.print(" class=\"" + cssClass + "\"");
        }
        if (StringUtils.isNotEmpty(onChange)) {
            out.print(" onchange=\"" + onChange + "\"");
        }
        if (StringUtils.isNotEmpty(onClick)) {
            out.print(" onclick=\"" + onClick + "\"");
        }
        out.println(">");
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