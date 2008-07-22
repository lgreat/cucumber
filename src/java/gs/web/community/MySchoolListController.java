package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.community.IUserDao;
import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.ReadWriteController;
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
    public static final String BEAN_ID = "/community/mySchoolList.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private StateManager _stateManager;

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

    /** Used to sort schools by name */
    private Comparator _schoolNameComparator;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String view;
        User user = sessionContext.getUser();
        String command = request.getParameter(PARAM_COMMAND);

        Map<String, Object> model = null;

        if (StringUtils.isBlank(command)) {
            if (user != null) {
                view = LIST_VIEW_NAME;
                model = buildModel(user);
            } else {
                view = INTRO_VIEW_NAME;                
            }
        } else {
            if (user != null) {
                processCommand(command, request, user);
                view = LIST_VIEW_NAME;
                model = buildModel(user);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("/community/mySchoolListLogin.page?command=");
                sb.append(command);
                sb.append("&ids=");
                sb.append(request.getParameter(PARAM_SCHOOL_IDS));
                sb.append("&state=");
                sb.append(request.getParameter(PARAM_STATE));
                return new ModelAndView(new RedirectView(sb.toString()));
            }
        }

        return new ModelAndView(view, model);
    }

    protected Map<String, Object> buildModel(User user) {
        Map<String, Object> model = new HashMap<String, Object>();
        Set<FavoriteSchool> favs = user.getFavoriteSchools();
        List<School> schools = convertFavoriteSchoolsToSchools(new ArrayList<FavoriteSchool>(favs));
        Collections.sort(schools, getSchoolNameComparator());
        model.put(MODEL_SCHOOLS, schools);
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

    protected void processCommand(String command, HttpServletRequest request, User user) {
        String stateString = request.getParameter(PARAM_STATE);
        State state = getStateManager().getState(stateString);
        List<Integer> ids = parseIds(request.getParameter(PARAM_SCHOOL_IDS));
        if (COMMAND_ADD.equals(command)) {
            addToMSL(state, ids, user);
        } else if (COMMAND_REMOVE.equals(command)) {
            removeFromMSL(state, ids, user);
        }
    }

    /**
     * @param ids - a comma-delimited set if int ids.
     * @return a List<Integer> with the parsed school ids. Returns an empty list if
     * there are no valid ids.
     */
    protected List<Integer> parseIds(String ids) {
        List<Integer> list = new ArrayList<Integer>();
        if (StringUtils.isNotBlank(ids)) {
            String[] sa = ids.split(",");
            for (String s : sa) {
                try {
                    list.add(Integer.decode(s.trim()));
                } catch (NumberFormatException nfe) {
                    _log.error("Could not parse id: " + s);
                }
            }
        }
        return list;
    }

    protected void addToMSL(State state, List<Integer> ids, User user) {
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

    protected void removeFromMSL(State state, List<Integer> ids, User user) {
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
                return new ArrayList<School>();
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
}
