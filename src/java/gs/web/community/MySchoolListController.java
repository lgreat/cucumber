package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.community.IUserDao;
import gs.data.community.FavoriteSchool;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller for the MSL page.
 */
public class MySchoolListController extends AbstractController {
    protected static final Log _log = LogFactory.getLog(MySchoolListController.class);

    /** Spring bean id */
    public static final String BEAN_ID = "/community/msl.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        return new ModelAndView(getViewName(), model);
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
