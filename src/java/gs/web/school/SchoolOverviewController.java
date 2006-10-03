package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewController extends AbstractController {

    private static final Logger _log = Logger.getLogger(SchoolOverviewController.class);

    private String _viewName;
    private ISchoolDao _schoolDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String schoolIdStr = request.getParameter("id");
        if (StringUtils.isNumeric(schoolIdStr)) {
            Integer schoolId = new Integer(schoolIdStr);
            State state = SessionContextUtil.getSessionContext(request).getState();
            School school = _schoolDao.getSchoolById(state, schoolId);
            model.put("school", school);
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

}
