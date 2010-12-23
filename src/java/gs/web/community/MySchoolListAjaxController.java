package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    private MySchoolListHelper _mySchoolListHelper;

    private MySchoolListConfirmationEmail _mySchoolListConfirmationEmail;

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView handleAdd(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("mySchoolListCommand")MySchoolListCommand command) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        School school = getSchoolFromCommand(command);

        if (user == null) {
            String email = command.getEmail();
            if (email != null) {
                
                user = _userDao.findUserFromEmailIfExists(email);

                if (user == null) {
                    user = _mySchoolListHelper.createNewMSLUser(email);
                    sendConfirmationEmail(user, request);
                }
                _mySchoolListHelper.addToMSL(user, school);
                PageHelper.setMemberCookie(request, response, user);
                return referer(command, request);
            } else {
                Map<Object,Object> data = new HashMap<Object,Object>();
                data.put("success", false);
                data.put("error", "unauthorized");
                jsonResponse(response, data);
            }
        }

        _mySchoolListHelper.addToMSL(user, school);
        jsonResponse(response, "success", true);
        //TODO: figure out how to record omniture events in JS, then do so after ajax response is received
        return null;
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ModelAndView handleDelete(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("mySchoolListCommand")MySchoolListCommand command) {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        if (user == null) {
            jsonResponse(response, "success", false);
        }

        School school = getSchoolFromCommand(command);
        _mySchoolListHelper.removeFromMSL(user, school);
        //TODO: figure out how to record omniture events in JS, then do so after ajax response is received
        //TODO: what to do if user is null? redirect? -- use email to fetch user
        return null;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String,Object> handleList(HttpServletRequest request, HttpServletResponse response) {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        if (user == null) {
            jsonResponse(response, "success", false);
        }

        Map<String,Object> model = buildModel(user);

        return model;
    }

    protected ModelAndView referer(MySchoolListCommand command, HttpServletRequest request) {
        ModelAndView mAndV = new ModelAndView();
        UrlUtil urlUtil = new UrlUtil();
        String redirectUrl = urlUtil.buildUrl(command.getRedirectUrl(), request);
        mAndV.setViewName("redirect:" + redirectUrl);
        return mAndV;
    }

    // Sends a confirmation email to the new user
    protected void sendConfirmationEmail(User user, HttpServletRequest request) {
        try {
            _mySchoolListConfirmationEmail.sendToUser(user, request);
        } catch (Exception ex) {
            _log.error("Error sending msl confirmation email to " + user, ex);
        }
    }

    public void jsonResponse(HttpServletResponse response, String key, Object value) {
        Map<Object,Object> data = new HashMap<Object,Object>();
        data.put(key,value);
        jsonResponse(response, data);
    }

    public void jsonResponse(HttpServletResponse response, Map<Object,Object> data) {
        try {
            response.setContentType("application/json");
            JSONObject rval = new JSONObject(data);
            response.getWriter().print(rval.toString());
            response.getWriter().flush();
        } catch (IOException e) {
            _log.info("Failed to get response writer");
            //give up
        }
    }
    /**
     * Method preserved from older code. Method is limited by the fact that all school IDs must point to schools
     * all within the same state.
     *
     * @param state
     * @param ids
     * @return
     */
    protected Set<School> convertIdsToSchools(State state, Set<Integer> ids) {
       Set<School> schools = new HashSet<School>();
        for (Integer id : ids) {
            School school = _schoolDao.getSchoolById(state, id);
            schools.add(school);
        }
       return schools;
    }

    protected School getSchoolFromCommand(MySchoolListCommand command) {
        State state = new StateManager().getState(command.getSchoolDatabaseState());
        School school = _schoolDao.getSchoolById(state, command.getSchoolId());
        return school;
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

    public MySchoolListHelper getMySchoolListHelper() {
        return _mySchoolListHelper;
    }

    public void setMySchoolListHelper(MySchoolListHelper mySchoolListHelper) {
        _mySchoolListHelper = mySchoolListHelper;
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

    public MySchoolListConfirmationEmail getMySchoolListConfirmationEmail() {
        return _mySchoolListConfirmationEmail;
    }

    public void setMySchoolListConfirmationEmail(MySchoolListConfirmationEmail mySchoolListConfirmationEmail) {
        _mySchoolListConfirmationEmail = mySchoolListConfirmationEmail;
    }
}
