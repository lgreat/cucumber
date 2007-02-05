package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.PrintWriter;

import gs.data.geo.IGeoDao;
import gs.data.geo.ICity;
import gs.data.geo.City;
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
        outputCitySelect(request, out);
        return null;
    }

    protected void outputCitySelect(HttpServletRequest request, PrintWriter out) {
        State state = _stateManager.getState(request.getParameter("state"));
        List cities = _geoDao.findCitiesByState(state);
        City notListed = new City();
        notListed.setName("My city is not listed");
        cities.add(0, notListed);
        if (cities.size() > 0) {
            out.print("<select id=\"citySelect\" name=\"city\" class=\"selectCity\" tabindex=\"10\">");
            outputOption(out, "", "Choose city", true);
            for (int x=0; x < cities.size(); x++) {
                ICity city = (ICity) cities.get(x);
                outputOption(out, city.getName(), city.getName());
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
        out.print(name);
        out.print("</option>");
    }
}
