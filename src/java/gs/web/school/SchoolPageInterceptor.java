package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Interceptor that puts a school into current request if request parameters contain valid id (or schoolId) and state
 *
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class SchoolPageInterceptor extends HandlerInterceptorAdapter {
    protected final Log _log = LogFactory.getLog(getClass());
    private ISchoolDao _schoolDao;

    /** Used when storing the school in the reqest */
    public static final String SCHOOL_ATTRIBUTE = "school";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        // make sure we have a valid school
        State state = SessionContextUtil.getSessionContext(request).getState();
        if (state != null) {
            String schoolId = request.getParameter("id");
            if (StringUtils.isBlank(schoolId)) {
                schoolId = request.getParameter("schoolId");
            }
            if (StringUtils.isNotBlank(schoolId)) {
                try {
                    Integer id = new Integer(schoolId);
                    School s = _schoolDao.getSchoolById(state, id);
                    if (s.isActive()) {
                        request.setAttribute(SCHOOL_ATTRIBUTE, s);
                        return true;
                    }
                } catch (Exception e) {
                    _log.warn("Could not get a valid or active school: " +
                            schoolId + " in state: " + state, e);
                }
            }
        }
        request.getRequestDispatcher("/school/error.page").include(request, response);
        return false;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

}

