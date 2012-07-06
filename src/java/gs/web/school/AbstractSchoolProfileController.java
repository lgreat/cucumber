package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.request.RequestAttributeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSchoolProfileController {
    protected final Log _log = LogFactory.getLog(getClass().getName());

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;

    public School getSchool(HttpServletRequest request, State state, Integer schoolId) {

        School school = _requestAttributeHelper.getSchool(request);

        // allow passing in state and schoolId so that modules can be used standalone
        if (school == null) {
            try {
                school = _schoolDao.getSchoolById(state, schoolId);
            } catch (Exception e) {
                _log.error("Cannot find school from request: state=" + state + ";schoolId=" + schoolId);
            }
        }

        return school;
    }

    public void setRequestAttributeHelper(RequestAttributeHelper requestAttributeHelper) {
        _requestAttributeHelper = requestAttributeHelper;
    }
}
