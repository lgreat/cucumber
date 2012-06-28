package gs.web.request;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.school.AbstractSchoolController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author aroy@greatschools.org
 */
@Component
public class RequestAttributeHelper {
    private static final Log _log = LogFactory.getLog(RequestAttributeHelper.class.getName());
    @Autowired
    private ISchoolDao _schoolDao;

    public School getSchool(HttpServletRequest request) {
        School school = (School) request.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);
        if (school == null) {
            State state = getState(request);
            Integer schoolId = getSchoolId(request);
            if (state != null && schoolId != null) {
                try {
                    school = _schoolDao.getSchoolById(state, schoolId);
                } catch (Exception e) {
                    // no school
                    _log.warn("Can't find school: " + e, e);
                }
            }
            request.setAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE, school);
        }
        return school;
    }

    public static DirectoryStructureUrlFields getDirectoryStructureUrlFields(HttpServletRequest request) {
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute
                (IDirectoryStructureUrlController.FIELDS);
        if (fields == null) {
            fields = new DirectoryStructureUrlFields(request);
            request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        }
        return fields;
    }

    public static State getState(HttpServletRequest request) {
        State state = (State) request.getAttribute("state");
        if (state == null) {
            DirectoryStructureUrlFields fields = getDirectoryStructureUrlFields(request);
            if (fields != null && fields.hasState()) {
                state = fields.getState();
            } else {
                String stateStr = request.getParameter("state");
                if (stateStr != null) {
                    try {
                        state = State.fromString(stateStr);
                    } catch (Exception e) {
                        // for some reason these parameters mean something else, let's ignore and move on
                    }
                }
            }
            request.setAttribute("state", state);
        }
        return state;
    }

    public static Integer getSchoolId(HttpServletRequest request) {
        Integer schoolId = (Integer) request.getAttribute("schoolId");
        if (schoolId == null) {
            DirectoryStructureUrlFields fields = getDirectoryStructureUrlFields(request);
            if (fields != null && fields.hasSchoolID()) {
                schoolId = new Integer(fields.getSchoolID());
            } else {
                String schoolIdStr = request.getParameter("schoolId");
                if (schoolIdStr == null) {
                    schoolIdStr = request.getParameter("id");
                }
                if (schoolIdStr != null) {
                    try {
                        schoolId = new Integer(schoolIdStr);
                    } catch (Exception e) {
                        // for some reason these parameters mean something else, let's ignore and move on
                    }
                }
            }
            request.setAttribute("schoolId", schoolId);
        }
        return schoolId;
    }


}
