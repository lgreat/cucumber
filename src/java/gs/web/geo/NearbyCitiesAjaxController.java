package gs.web.geo;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
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
 * Provides a list of cities in a state on demand.
 * 
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NearbyCitiesAjaxController  implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());

    private StateManager _stateManager;
    private IGeoDao _geoDao;

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
        State state = _stateManager.getState(request.getParameter("state"));

        PrintWriter out = response.getWriter();
        try {
            outputCitySelect(state, out);
        } catch (Exception e) {
            out.print("Error retrieving results");
        }
        return null;
    }

    protected void outputCitySelect(State state, PrintWriter out) {
        List<City> cities = _geoDao.findCitiesByState(state);
        openSelectTag(out, "city", "horizontalCompareCitySelect", "compareCitySelect", "");
        outputOption(out, "Choose city", "Choose city");
        for (ICity city : cities) {
            outputOption(out, city.getName(), city.getName());
        }
        out.println("</select>");
    }

    protected void openSelectTag(PrintWriter out, String name, String cssId, String cssClass, String onChange) {
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
