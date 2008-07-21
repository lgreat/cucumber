package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.community.IUserDao;
import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
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
public class MySchoolListController extends AbstractController {
    protected static final Log _log = LogFactory.getLog(MySchoolListController.class);

    /** Spring bean id */
    public static final String BEAN_ID = "/community/mySchoolList.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;

    /** query parameters accepted by this controller */
    public static final String PARAM_SCHOOL_IDS = "sids";

    /** view names */
    public static final String INTRO_VIEW_NAME = "/community/mySchoolListIntro";
    public static final String LIST_VIEW_NAME = "/community/mySchoolList";

    /** model keys */
    public static final String MODEL_SCHOOLS = "schools";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String view;
        User user = sessionContext.getUser();
        String sids = request.getParameter(PARAM_SCHOOL_IDS);

        Map<String, Object> model = null;

        if (StringUtils.isBlank(sids)) {
            if (user != null) {
                view = LIST_VIEW_NAME;
                model = buildModel(user);
            } else {
                view = INTRO_VIEW_NAME;                
            }
        } else {
            if (user != null) {
                addSchoolsToMSL(sids);
                view = LIST_VIEW_NAME;
                model = buildModel(user);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("/community/mySchoolListLogin.page");
                State state = sessionContext.getState();
                if (state != null) {
                    sb.append("?state=");
                    sb.append(state.getAbbreviation());
                    sb.append("&ids=");
                    sb.append(sids);
                }
                return new ModelAndView(new RedirectView(sb.toString()));
            }
        }

        return new ModelAndView(view, model);
    }

    protected Map<String, Object> buildModel(User user) {
        Map<String, Object> model = new HashMap<String, Object>();
        Set<FavoriteSchool> favs = user.getFavoriteSchools();
        model.put(MODEL_SCHOOLS, convertFavoriteSchoolsToSchools(new ArrayList<FavoriteSchool>(favs)));
        return model;
    }

    protected void addSchoolsToMSL(String schoolIds) {

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
}
