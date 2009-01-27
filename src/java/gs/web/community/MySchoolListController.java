package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.community.IUserDao;
import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller for the My School List page.
 */
public class MySchoolListController extends AbstractController implements ReadWriteController {
    protected static final Log _log = LogFactory.getLog(MySchoolListController.class);

    /** Spring bean id */
    public static final String BEAN_ID = "/mySchoolList.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private StateManager _stateManager;
    private IGeoDao _geoDao;

    /** query parameters accepted by this controller */
    public static final String PARAM_COMMAND = "command";
    public static final String PARAM_SCHOOL_IDS = "ids";
    public static final String PARAM_STATE = "state";

    /** commands supported by this controller */
    public static final String COMMAND_ADD = "add";
    public static final String COMMAND_REMOVE = "remove";

    /** view names */
    public static final String INTRO_VIEW_NAME = "/community/mySchoolListIntro";
    public static final String LIST_VIEW_NAME = "/community/mySchoolList";

    /** model keys */
    public static final String MODEL_SCHOOLS = "schools";
    public static final String MODEL_COMPARE_STATES = "compareStates";
    public static final String MODEL_COMPARE_LEVELS = "compareLevels";
    public static final String MODEL_PRESCHOOL_ONLY = "preschoolOnly";
    public static final String MODEL_CITY_ID = "cityId";
    public static final String MODEL_CITY_NAME = "cityName";

    /** Used to sort schools by name */
    private Comparator<School> _schoolNameComparator;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String view;
        User user = sessionContext.getUser();
        String command = request.getParameter(PARAM_COMMAND);

        Map<String, Object> model = null;

        if (StringUtils.isBlank(command)) {
            // GS-7601 Anonymous users from reg welcome email redirected to community login
            if (StringUtils.equals(request.getParameter("cpn"), "gssu_welcome")) {
                // if you aren't signed in, go to community sign in
                if (user == null) {
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, null);
                    urlBuilder.setParameter("redirect", BEAN_ID);
                    //urlBuilder.setParameter("message", "Please login or register to access My School List");
                    return new ModelAndView("redirect:" + urlBuilder.asSiteRelative(request));
                }
            }
            if (user != null) {
                view = LIST_VIEW_NAME;
                model = buildModel(user);
            } else {
                view = INTRO_VIEW_NAME;
            }
        } else {
            if (user != null) {
                processCommand(command, request, user);
                SessionContextUtil util = sessionContext.getSessionContextUtil();
                util.saveCookies(response, sessionContext);
                view = LIST_VIEW_NAME;
                model = buildModel(user);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("/mySchoolListLogin.page?command=");
                sb.append(command);
                sb.append("&ids=");
                sb.append(request.getParameter(PARAM_SCHOOL_IDS));
                sb.append("&state=");
                sb.append(request.getParameter(PARAM_STATE));
                return new ModelAndView(new RedirectView(sb.toString()));
            }
        }

        determineLocalCity(request, response, model);

        return new ModelAndView(view, model);
    }

    /**
     * GS-7380. The page wants to know the user's location. Use either the location cookie, or if it
     * isn't present determine the location by the most common city appearing in the school list.
     */
    protected void determineLocalCity(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (model != null && sessionContext.getCity() != null) {
            // if the location cookie is set, then this is easy. Just use that value
            model.put(MODEL_CITY_ID, sessionContext.getCityId());
            model.put(MODEL_CITY_NAME, sessionContext.getCity().getName());
        } else if (model != null && model.get(MODEL_SCHOOLS) != null) {
            // Otherwise, let's see if we can guess it
            List<School> schools = (List<School>) model.get(MODEL_SCHOOLS);
            Map<LocalCity, Integer> cities = new HashMap<LocalCity, Integer>();
            LocalCity cityMax = null;
            int numMax = 0;
            for (School school: schools) {
                // Look at each school and note its city
                // we'll use whichever city is used the most
                // which we'll store in cityMax and stateMax
                String city = school.getCity();
                State state = school.getDatabaseState();
                LocalCity localCity = new LocalCity(city, state);
                Integer currentNumber = cities.get(localCity);
                if (currentNumber == null) {
                    currentNumber = 1;
                } else {
                    currentNumber += 1;
                }
                cities.put(localCity, currentNumber);
                if (currentNumber > numMax) {
                    // if this city is now the most common, store it in the max vars
                    numMax = currentNumber;
                    cityMax = localCity;
                }
            }
            // Use the most common city as the user's location
            if (cityMax != null) {
                City city = _geoDao.findCity(cityMax.getState(), cityMax.getName());
                model.put(MODEL_CITY_ID, city.getId());
                model.put(MODEL_CITY_NAME, city.getName());
                // also set this permanently as their location, per GS-7380
                PageHelper.setCityIdCookie(request, response, city);
            }
        }
    }

    protected static class LocalCity {
        private String _name;
        private State _state;

        public LocalCity(String name, State state) {
            _name = name;
            _state = state;
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public State getState() {
            return _state;
        }

        public void setState(State state) {
            _state = state;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalCity localCity = (LocalCity) o;
            return _name.equals(localCity._name) && _state.equals(localCity._state);
        }

        public int hashCode() {
            return 31 * _name.hashCode() + _state.hashCode();
        }
    }

    protected Map<String, Object> buildModel(User user) {
        Map<String, Object> model = new HashMap<String, Object>();
        Set<FavoriteSchool> favs = user.getFavoriteSchools();
        List<School> schools = convertFavoriteSchoolsToSchools(new ArrayList<FavoriteSchool>(favs));
        Collections.sort(schools, getSchoolNameComparator());
        model.put(MODEL_SCHOOLS, schools);

        String preschoolOnly = "true";

        SortedSet<State> stateSet = new TreeSet<State>();
        SortedSet<LevelCode.Level> levelSet = new TreeSet<LevelCode.Level>();

        for (School school : schools) {
            stateSet.add(school.getDatabaseState());
            LevelCode lc = school.getLevelCode();
            levelSet.addAll(lc.getIndividualLevelCodes());
            if(!LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                preschoolOnly = "false";
            }
        }

        model.put(MODEL_COMPARE_STATES, new ArrayList<State>(stateSet));
        model.put(MODEL_COMPARE_LEVELS, new ArrayList<LevelCode.Level>(levelSet));
        model.put(MODEL_PRESCHOOL_ONLY, preschoolOnly);

        return model;
    }

    private Comparator<School> getSchoolNameComparator() {
        if (_schoolNameComparator == null) {
            _schoolNameComparator = new Comparator<School>() {
                public int compare(School s1, School s2) {
                    // Non-null names are > null names
                    if (s1.getName() != null) {
                        return s1.getName().compareTo(s2.getName());
                    } else {
                        if (s2.getName() != null) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            };
        }
        return _schoolNameComparator;
    }

    /**
     * A dispatch method - collects params and forwards to command-processing methods
     * @param command - A string command - see static commands above
     * @param request - Used to extract query parameters
     * @param user - User on whose data commands are performed  
     */
    protected void processCommand(String command, HttpServletRequest request, User user) {
        String stateString = request.getParameter(PARAM_STATE);
        State state = getStateManager().getState(stateString);
        Set<Integer> ids = getSchoolIds(request);
        if (COMMAND_ADD.equals(command)) {
            addToMSL(state, ids, user);
        } else if (COMMAND_REMOVE.equals(command)) {
            removeFromMSL(state, ids, user);
        }
    }

    /**
     * This method extracts ids from the request.  Ids may be in the form:
     *    &ids=1,2,3,4
     * or:
     *    &ids=1&ids=2&ids=3&ids=4
     *
     * @param request an HttpServletRequest
     * @return a Set<Integer> of ids
     */
    Set<Integer> getSchoolIds(HttpServletRequest request) {
        Set<Integer> idSet = new HashSet<Integer>();
        String[] values = request.getParameterValues(PARAM_SCHOOL_IDS);
        if (values != null) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    String[] sa = value.split(",");
                    for (String s : sa) {
                        try {
                            idSet.add(Integer.decode(s.trim()));
                        } catch (NumberFormatException nfe) {
                            _log.error("Could not parse id: " + s);
                        }
                    }
                }
            }
        }
        return idSet;
    }

    protected void addToMSL(State state, Set<Integer> ids, User user) {
        Set<FavoriteSchool> favs = user.getFavoriteSchools();
        for (Integer id : ids) {
            School school = _schoolDao.getSchoolById(state, id);
            FavoriteSchool toAdd = new FavoriteSchool();
            toAdd.setState(state);
            toAdd.setUser(user);
            toAdd.setUpdated(new Date());
            toAdd.setSchoolId(id);
            toAdd.setLevelCode(school.getLevelCode());
            favs.add(toAdd);
        }
        user.setFavoriteSchools(favs);
        _userDao.updateUser(user);
    }

    protected void removeFromMSL(State state, Set<Integer> ids, User user) {
        Set<FavoriteSchool> favs = user.getFavoriteSchools();
        FavoriteSchool toRemove = new FavoriteSchool();
        toRemove.setState(state);
        toRemove.setUser(user);
        for (Integer id : ids) {
            toRemove.setSchoolId(id);
            if (favs.contains(toRemove)) {
                favs.remove(toRemove);
            }
        }
        user.setFavoriteSchools(favs);
        _userDao.updateUser(user);
    }

    public List<School> convertFavoriteSchoolsToSchools(List<FavoriteSchool> favoriteSchools) {
        List<School> schools = new ArrayList<School>();
        if (favoriteSchools == null || favoriteSchools.isEmpty()) {
            return schools;
        }
        for (FavoriteSchool fave: favoriteSchools) {
            try {
                schools.add(_schoolDao.getSchoolById(fave.getState(), fave.getSchoolId()));
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("School in user's MSL doesn't exist in school table. User " +
                        fave.getUser().getId() + ", School " + fave.getState() + ":" + fave.getSchoolId());
            }
        }
        return schools;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
