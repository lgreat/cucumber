package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.review.Review;
import gs.data.school.review.IReviewDao;
import gs.data.community.IUserDao;
import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.school.review.ReviewFacade;
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
    private boolean _showRecentReviews = false;
    private IReviewDao _reviewDao;

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
    public static final String MODEL_RECENT_REVIEWS = "recentReviews";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_SHOW_PYOC_MODULE = "showPYOCModule"; // pyoc = Print Your Own Chooser
    public static final String MODEL_SHOW_MILWAUKEE_PDF = "showMilwaukeePdf";
    public static final String MODEL_SHOW_INDIANAPOLIS_PDF = "showIndianapolisPdf";
    public static final String MODEL_SHOW_DC_PDF = "showDcPdf";

    // states/cities that have application instruction PDF's
    public static Map<String, String> CHOOSER_CITY_PDF_MODEL_MAP = new HashMap<String, String>();
    static {
        // comparisons are to lowercase
        CHOOSER_CITY_PDF_MODEL_MAP.put("milwaukee, wi", MODEL_SHOW_MILWAUKEE_PDF);
        CHOOSER_CITY_PDF_MODEL_MAP.put("washington, dc", MODEL_SHOW_DC_PDF);
        CHOOSER_CITY_PDF_MODEL_MAP.put("indianapolis, in", MODEL_SHOW_INDIANAPOLIS_PDF);
        CHOOSER_CITY_PDF_MODEL_MAP.put("speedway, in", MODEL_SHOW_INDIANAPOLIS_PDF);
        CHOOSER_CITY_PDF_MODEL_MAP.put("beech grove, in", MODEL_SHOW_INDIANAPOLIS_PDF);
    }

    /** constants */
    public static final int RECENT_REVIEWS_LIMIT = 3;

    /** Used to sort schools by name */
    private Comparator<School> _schoolNameComparator;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String view;
        User user = sessionContext.getUser();
        String command = request.getParameter(PARAM_COMMAND);

        Map<String, Object> model = null;

        if (!PageHelper.isMemberAuthorized(request)) {
            System.err.println("Member rejected!");
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, (String)null);
            UrlBuilder mslUrl = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
            if (StringUtils.isNotBlank(request.getParameter(PARAM_COMMAND))) {
                mslUrl.addParameter(PARAM_COMMAND, request.getParameter(PARAM_COMMAND));
            }
            if (StringUtils.isNotBlank(request.getParameter(PARAM_SCHOOL_IDS))) {
                mslUrl.addParameter(PARAM_SCHOOL_IDS, request.getParameter(PARAM_SCHOOL_IDS));
            }
            if (StringUtils.isNotBlank(request.getParameter(PARAM_STATE))) {
                mslUrl.addParameter(PARAM_STATE, request.getParameter(PARAM_STATE));
            }
            urlBuilder.setParameter("redirect", mslUrl.asSiteRelative(request));
            //urlBuilder.setParameter("message", "Please login or register to access My School List");
            return new ModelAndView("redirect:" + urlBuilder.asSiteRelative(request));
        }

        if (StringUtils.isBlank(command)) {
            // GS-7601 Anonymous users from reg welcome email redirected to community login
            if (StringUtils.equals(request.getParameter("cpn"), "gssu_welcome")) {
                // if you aren't signed in, go to community sign in
                if (user == null) {
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, (String)null);
                    urlBuilder.setParameter("redirect", BEAN_ID);
                    //urlBuilder.setParameter("message", "Please login or register to access My School List");
                    return new ModelAndView("redirect:" + urlBuilder.asSiteRelative(request));
                }
            }
            if (user != null) {
                if (getViewName() != null) {
                    view = getViewName();
                } else {
                    view = LIST_VIEW_NAME;
                }
                model = buildModel(user);
            } else {
                view = INTRO_VIEW_NAME;
            }
        } else {
            if (user != null) {
                processCommand(command, request, response, user);
                SessionContextUtil util = sessionContext.getSessionContextUtil();
                util.saveCookies(response, sessionContext);
                if (getViewName() != null) {
                    view = getViewName();
                } else {
                    view = LIST_VIEW_NAME;
                }
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

        if (isShowRecentReviews()) {
            List<School> schools = (List<School>)model.get(MODEL_SCHOOLS);
            createRecentReviewsModel(schools, model);
        }

        return new ModelAndView(view, model);
    }

    // GS-8811
    protected void createRecentReviewsModel(Collection<School> schools, Map<String, Object> model) {
        List<Review> reviews = new ArrayList<Review>();
        List<ReviewFacade> recentReviews = new ArrayList<ReviewFacade>();

        // get recent reviews for each school in MSL
        for (School school : schools) {
            reviews.addAll(getReviewDao().getPublishedReviewsBySchool(school, RECENT_REVIEWS_LIMIT));
        }

        // sort them
        Collections.sort(reviews, Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

        // take only the most recent RECENT_REVIEWS_LIMIT number of reviews and create ReviewFacades for them
        int numReviews = reviews.size();
        for (int i = 1; i <= RECENT_REVIEWS_LIMIT && i <= numReviews; i++) {
            Review review = reviews.get(i-1);
            recentReviews.add(new ReviewFacade(review.getSchool(), review));
        }

        model.put(MODEL_RECENT_REVIEWS, recentReviews);
        model.put(MODEL_CURRENT_DATE, new Date());
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

        // For PYOC: Show the download box if there are any schools in the list
        model.put(MODEL_SHOW_PYOC_MODULE, (schools.size()>0) ? true : false );
        String preschoolOnly = "true";

        SortedSet<State> stateSet = new TreeSet<State>();
        SortedSet<LevelCode.Level> levelSet = new TreeSet<LevelCode.Level>();

        boolean showPYOCModule = false;
        for (School school : schools) {
            stateSet.add(school.getDatabaseState());
            LevelCode lc = school.getLevelCode();
            levelSet.addAll(lc.getIndividualLevelCodes());
            if(!LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                preschoolOnly = "false";
            }
            String modelVar = CHOOSER_CITY_PDF_MODEL_MAP.get((school.getCity() + ", " + school.getStateAbbreviation()).toLowerCase());
            if (modelVar != null) {
                model.put(modelVar, true);
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
    protected void processCommand(String command, HttpServletRequest request, HttpServletResponse response, User user) {
        OmnitureTracking omnitureTracking = new CookieBasedOmnitureTracking(request, response);
        String stateString = request.getParameter(PARAM_STATE);
        State state = getStateManager().getState(stateString);
        Set<Integer> ids = getSchoolIds(request);
        if (COMMAND_ADD.equals(command)) {
            addToMSL(state, ids, user);
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.MSLAddSchool);
        } else if (COMMAND_REMOVE.equals(command)) {
            removeFromMSL(state, ids, user);
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.MSLDeleteSchool);
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
