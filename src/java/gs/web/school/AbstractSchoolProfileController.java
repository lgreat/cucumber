package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSchoolProfileController {
    protected final Log _log = LogFactory.getLog(getClass().getName());

    @Autowired
    private ISchoolDao _schoolDao;

    public School getSchool(HttpServletRequest request, State state, Integer schoolId) {

        School school = (School) request.getAttribute("school");

        if (school == null) {
            try {
                school = _schoolDao.getSchoolById(state, schoolId);
            } catch (Exception e) {
                _log.error("Cannot find school from request: state=" + state + ";schoolId=" + schoolId);
            }
        }

        return school;
    }
}
