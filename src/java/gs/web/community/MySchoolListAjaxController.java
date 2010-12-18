package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller for the My School List page.
 */
@Controller
@RequestMapping("/mySchoolListAjax.page")
public class MySchoolListAjaxController implements ReadWriteAnnotationController {
    protected static final Log _log = LogFactory.getLog(MySchoolListAjaxController.class);

    /** Spring bean id */
    public static final String BEAN_ID = "/mySchoolList.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private StateManager _stateManager;
    private IGeoDao _geoDao;
    private boolean _showRecentReviews = false;
    private IReviewDao _reviewDao;

    /** query parameters accepted by this controller */
    public static final String PARAM_COMMAND = "command";
    public static final String PARAM_SCHOOL_IDS = "ids";
    public static final String PARAM_STATE = "state";

    /** view names */
    public static final String INTRO_VIEW_NAME = "/community/mySchoolListIntro";
    public static final String LIST_VIEW_NAME = "/community/mySchoolList";

    public static final String MODEL_SCHOOLS = "schools";

    /** Used to sort schools by name */
    private Comparator<School> _schoolNameComparator;

    @RequestMapping(method = RequestMethod.POST)
    public String handleAdd(HttpServletRequest request, HttpServletResponse response) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        String stateString = request.getParameter(PARAM_STATE);
        State state = getStateManager().getState(stateString);
        Set<Integer> ids = getSchoolIds(request);
        addToMSL(state, ids, user);
        //TODO: figure out how to record omniture events in JS, then do so after ajax response is received

        return null;
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public String handleDelete(HttpServletRequest request, HttpServletResponse response) {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        String stateString = request.getParameter(PARAM_STATE);
        State state = getStateManager().getState(stateString);
        Set<Integer> ids = getSchoolIds(request);
        removeFromMSL(state, ids, user);
        //TODO: figure out how to record omniture events in JS, then do so after ajax response is received
        //TODO: what to do if user is null? redirect? -- use email to fetch user
        return null;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String,Object> handleList(HttpServletRequest request, HttpServletResponse response) {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        Map<String,Object> model = buildModel(user);

        return model;

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

    public boolean isShowRecentReviews() {
        return _showRecentReviews;
    }

    public void setShowRecentReviews(boolean showRecentReviews) {
        _showRecentReviews = showRecentReviews;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
