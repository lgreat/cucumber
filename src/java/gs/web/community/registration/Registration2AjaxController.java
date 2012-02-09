package gs.web.community.registration;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;

/**
 * The AJAX controller for registration stage 2.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class Registration2AjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/registration2Ajax.page";
    final public static String FORMAT_PARAM = "format";
    final public static String JSONP_CALLBACK_PARAM = "jsoncallback";
    final public static String TYPE_PARAM = "type";
    final public static String SCHOOL_TYPE = "school";

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
        String schoolTypes = request.getParameter("schoolTypes");
        boolean excludePreschoolsOnly = (StringUtils.isNotBlank(request.getParameter("excludePreschoolsOnly"))) ?
                new Boolean(request.getParameter("excludePreschoolsOnly")) : false;
        PrintWriter out = response.getWriter();

        if (StringUtils.equals("json", request.getParameter(FORMAT_PARAM))) {
            String type = request.getParameter(TYPE_PARAM);
            if (StringUtils.equals(SCHOOL_TYPE, type)) {
                outputSchoolJson(state, city, grade, schoolTypes, excludePreschoolsOnly, out, request);
            }
            return null;
        }


        String childNum = request.getParameter("childNum");

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
        cities.add(notListed);
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
        for(School school : schools) {
            String idString = ((school.getId() != null)?school.getId().toString():"");
            outputOption(out, idString, school.getName());
        }
        if (selectedSchools) {
            outputOption(out, "-1", "My child's school is not listed");
        }
        out.print("</select>");
    }

    protected void outputSchoolJson(State state, String city, String grades, String schoolTypes, boolean excludePreschoolsOnly, PrintWriter out, HttpServletRequest request) {
        List<School> schools = null;
        JSONObject rval = new JSONObject();
        if (state != null && !StringUtils.isBlank(city)) {
            Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();
            Grades gs = null;
            if (StringUtils.isNotBlank(schoolTypes)) {
                String[] schoolTypesArr = StringUtils.split(schoolTypes, ", ");
                for (String schoolTypeStr : schoolTypesArr) {
                    schoolTypeSet.add(SchoolType.getSchoolType(schoolTypeStr));
                }
            }
            if (StringUtils.isNotBlank(grades)) {                 
                //We have to convert the passed in string into a Grades object so
                // that if 'k' is passed in its converted to 'KG'.
                gs = new Grades(grades);
            }

            if (!schoolTypeSet.isEmpty() || gs != null) {
                schools = _schoolDao.findSchoolsInCityByGradesAndTypes(state, city, gs, schoolTypeSet);
            }else if(excludePreschoolsOnly){
                schools = _schoolDao.findSchoolsInCity(state, city, 2000,excludePreschoolsOnly);
            } else {
                schools = _schoolDao.findSchoolsInCity(state, city, 2000); // 2000 is arbitrary - CK
            }
        }
        if (schools == null) {
            schools = new ArrayList<School>();
        }

        try {
            List<JSONObject> schoolList = new ArrayList<JSONObject>(schools.size());
            JSONObject chooseSchool = new JSONObject();
            chooseSchool.put("name", "- Choose school -");
            chooseSchool.put("id", "-1");
            schoolList.add(chooseSchool);
            for (School school : schools) {
                JSONObject cityJson = new JSONObject();
                cityJson.put("name", school.getName());
                cityJson.put("id", String.valueOf(school.getId()));
                schoolList.add(cityJson);
            }
            rval.put("schools", schoolList);
        } catch (JSONException jsone) {
            _log.error("Error converting school list to JSON: " + jsone, jsone);
        }

        String jsonCallbackParam = getSanitizedJsonpParam(request);
        if (jsonCallbackParam != null) {
            String res = jsonCallbackParam + "(" + rval + ");";
            out.print(res);
        } else {
            out.print(rval.toString());
        }
    }

    protected String getSanitizedJsonpParam(HttpServletRequest request) {
        String jsonpParam = request.getParameter(JSONP_CALLBACK_PARAM);
        if ( StringUtils.isEmpty(jsonpParam)) return null;
        if ( StringUtils.length(jsonpParam) > 128 ) return null;
        if ( !StringUtils.startsWithIgnoreCase(jsonpParam,"jsonp") && !StringUtils.startsWithIgnoreCase(jsonpParam,"jQuery")) return null;
        return jsonpParam;
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