package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.PrintWriter;

import gs.data.geo.*;
import gs.data.state.State;
import gs.data.state.StateManager;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationAjaxController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/registrationAjax.page";

    private IGeoDao _geoDao;
    private StateManager _stateManager;

    final public static String TYPE_PARAM = "type";
    final public static String CITY_TYPE = "city";
    final public static String COUNTY_TYPE = "county";

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
        PrintWriter out = response.getWriter();
        String type = request.getParameter(TYPE_PARAM);
        if (CITY_TYPE.equals(type)) {
            outputCitySelect(request, out);
        } else if (COUNTY_TYPE.equals(type)) {
            outputCountySelect(request,out);
        }
        return null;
    }

    protected void outputCitySelect(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        List cities = _geoDao.findCitiesByState(state);
        String onChange = request.getParameter("onchange");

        if (request.getParameter("showNotListed") != null && Boolean.valueOf(request.getParameter("showNotListed"))) {
            City notListed = new City();
            notListed.setName("My city is not listed");
            cities.add(0, notListed);
        }

        if (cities.size() > 0) {
            out.print("<select id=\"citySelect\" name=\"city\" class=\"selectCity\" tabindex=\"10\"" +
                 (StringUtils.isNotBlank(onChange) ? " onchange=\"" + onChange + "\"" : "") +  ">");
            outputOption(out, "", "Choose city", true);
            for (int x=0; x < cities.size(); x++) {
                ICity city = (ICity) cities.get(x);
                outputOption(out, city.getName(), city.getName());
            }
            out.print("</select>");
        }
    }

    protected void outputCountySelect(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        List<ICounty> counties = _geoDao.findCounties(state);
        if (counties.size() > 0) {
            out.print("<select id=\"countySelect\" name=\"county\" class=\"selectCounty\">");
            outputOption(out, "", "Choose county", true);
            for (ICounty county : counties) {
                outputOption(out, county.getName(), county.getName());
            }
            out.print("</select>");
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
        out.print(StringEscapeUtils.escapeHtml(name));
        out.print("</option>");
    }
}
