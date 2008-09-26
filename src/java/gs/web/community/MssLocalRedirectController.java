package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.util.Map;

/**
 * Created by chriskimm@greatschools.net
 */
public class MssLocalRedirectController extends AbstractController {

    private static final Logger _log = Logger.getLogger(MssLocalRedirectController.class);

    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "id";
    public static final String PARAM_PAGE = "page";

    /** patterns used for city & state replacement in urls */
    public static final String CITY_PATTERN = "%CITY%";
    public static final String STATE_PATTERN = "%STATE%";    

    // Used to get cities from schools
    private ISchoolDao _schoolDao;

    /** Maps page keys to url pattern Strings */
    private Map<String, String> _pageMap;

    /** Used to get {@link gs.data.state.State} objects from Strings */
    private static final StateManager _stateManager = new StateManager();

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String stateParam = request.getParameter(PARAM_STATE);
        String idParam = request.getParameter(PARAM_SCHOOL_ID);
        String pageParam = request.getParameter(PARAM_PAGE);

        State state = _stateManager.getState(stateParam);
        School school = getSchool(state, idParam);
        String city = school.getCity();

        String url = getLocalUrl(state, city, pageParam);
        return new ModelAndView(new RedirectView(url));
    }

    School getSchool(State state, String id) {
        School school = null;
        try {
            Integer i = Integer.parseInt(id);
            school = _schoolDao.getSchoolById(state, i);
        } catch (Exception e) {
            _log.error("Could not get school from params: " + state + "/" + id, e);
        }
        return school;
    }

    String getLocalUrl(State state, String city, String pageKey) {
        String urlPattern = _pageMap.get(pageKey);
        if (StringUtils.isNotBlank(urlPattern)) {
            String formattedCity = formatPlace(city);
            urlPattern = urlPattern.replace(CITY_PATTERN, formattedCity);
            String formattedState = formatPlace(state.getLongName());
            urlPattern = urlPattern.replace(STATE_PATTERN, formattedState);
        }
        return urlPattern;
    }

    /**
     * Returns a string with the following formatting:
     * -> "-" replaced with "_"
     * -> spaces replaced with "-"
     *
     * @param place a String
     * @return a formatted String or null if null is passed in
     */
    String formatPlace(String place) {
        String formatted = place;
        if (StringUtils.isNotBlank(place)) {
            formatted = formatted.replaceAll("-", "_");
            formatted = formatted.replaceAll(" ", "-");
        }
        return formatted;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public Map getPageMap() {
        return _pageMap;
    }

    public void setPageMap(Map<String, String> pm) {
        _pageMap = pm;
    }
}
