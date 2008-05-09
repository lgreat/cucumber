package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.FavoriteSchool;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller for the MSL page.
 */
public class MySchoolListController extends AbstractController {

    /** Spring bean id */
    public static final String BEAN_ID = "/community/msl.page";
    
    private String _viewName;
    private ISchoolDao _schoolDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        User user = context.getUser();
        Map<String, Object> model = new HashMap<String, Object>();
        if (user != null) {
            List<School> schools = getSchoolList(user.getFavoriteSchools());
            model.put("schools", schools);
        }
        return new ModelAndView(getViewName(), model);
    }

    List<School> getSchoolList(Set<FavoriteSchool> favs) {
        List<School> schools = new ArrayList<School>();
        for (FavoriteSchool fav : favs) {
            School s = getSchoolDao().getSchoolById(fav.getState(), fav.getSchoolId());
            schools.add(s);
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
}
