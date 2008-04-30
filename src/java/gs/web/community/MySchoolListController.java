package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Provide the "member" and "limit". Returns data in the AnchorListModel format.
 * 
 * @see AnchorListModel
 */
public class MySchoolListController extends AbstractController {

    /** Spring bean id */
    public static final String BEAN_ID = "/community/msl.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    public static final int DEFAULT_SCHOOL_LIMIT = 3;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        User user = context.getUser();
        Map<String, Object> model = new HashMap<String, Object>();
        if (user != null) {
            Set schools = user.getFavoriteSchools();
            model.put("schools", schools);
        }
        return new ModelAndView(_viewName, model);
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
