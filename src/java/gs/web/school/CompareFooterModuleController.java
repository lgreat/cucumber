package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.geo.IGeoDao;
import gs.data.geo.ICity;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.util.Map;
import java.util.HashMap;

/**
 * This is the controller for the CompareFooterModule.  There are
 * 2 required query parameters:
 * state -> 2-letter state abbreviation
 * city -> a city name
 */
public class CompareFooterModuleController extends AbstractController {

    public static final String BEAN_ID = "/school/compareFooter.module";
    public static final String VIEW_NAME = "/school/compareFooter";

    public static final String PARAM_STATE = "state";
    public static final String PARAM_CITY = "city";

    public static final String MODEL_CITIES = "cities";
    public static final String MODEL_CITY = "city";

    private String _viewName = null;

    private IGeoDao _geoDao;
    private StateManager _stateManager;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();

        String stateParam = request.getParameter(PARAM_STATE);
        if (StringUtils.isNotBlank(stateParam)) {
            State state = _stateManager.getState(stateParam);
            if (state != null) {
                model.put(MODEL_CITIES, _geoDao.findCitiesByState(state));
                String cityParam = request.getParameter(PARAM_CITY);
                if (StringUtils.isNotBlank(cityParam)) {
                    ICity city = _geoDao.findCity(state, cityParam);
                    model.put(MODEL_CITY, city);
                }
            }
        }

        return new ModelAndView((_viewName != null ? _viewName : VIEW_NAME), model);
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
