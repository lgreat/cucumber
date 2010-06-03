package gs.web.admin;

import gs.data.school.HeldSchool;
import gs.data.school.IHeldSchoolDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.ReadWriteController;
import gs.web.util.SitePrefCookie;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class HeldSchoolAdminController extends AbstractController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID="/admin/schoolReview/holdList.page";

    public static final String MODEL_HOLD_LIST="holdList";
    public static final String PARAM_ACTION="action";
    public static final String PARAM_SCHOOL_ID="schoolId";
    public static final String PARAM_SCHOOL_STATE="schoolState";
    public static final String PARAM_NOTES="notes";
    public static final String PARAM_HELD_SCHOOL_ID="heldSchoolId";
    public static final String ACTION_ADD="add";
    public static final String ACTION_DELETE="delete";

    private IHeldSchoolDao _heldSchoolDao;
    private ISchoolDao _schoolDao;
    private String _viewName;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        boolean redirect = false;
        String alert = null;
        if (StringUtils.equals(ACTION_ADD, request.getParameter(PARAM_ACTION))) {
            if (addSchoolToHold(request)) {
                alert = "schoolAdded";
                redirect = true;
            }
        } else if (StringUtils.equals(ACTION_DELETE, request.getParameter(PARAM_ACTION))) {
            if (removeSchoolFromHold(request)) {
                alert = "schoolRemoved";
                redirect = true;
            }
        }
        if (redirect) {
            SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
            sitePrefCookie.setProperty("holdSchoolAlert", alert);
            return new ModelAndView("redirect:" + BEAN_ID);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_HOLD_LIST, getAllHeldSchools());

        return new ModelAndView(_viewName, model);
    }

    protected List<HeldSchool> getAllHeldSchools() {
        List<HeldSchool> heldSchools = _heldSchoolDao.getAll();

        for (HeldSchool heldSchool: heldSchools) {
            heldSchool.setSchoolDao(_schoolDao);
        }
        return heldSchools;
    }

    protected boolean removeSchoolFromHold(HttpServletRequest request) {
        try {
            int heldSchoolId = Integer.parseInt(request.getParameter(PARAM_HELD_SCHOOL_ID));
            return removeSchoolFromHold(heldSchoolId);
        } catch (Exception e) {
            _log.warn("Error parsing parameters", e);
        }
        return false;
    }

    protected boolean removeSchoolFromHold(int heldSchoolId) {
        try {
            _heldSchoolDao.delete(_heldSchoolDao.getById(heldSchoolId));
            return true;
        } catch (Exception e) {
            _log.warn("Error deleting HeldSchool " + heldSchoolId, e);
        }
        return false;
    }

    protected boolean addSchoolToHold(HttpServletRequest request) {
        try {
            int schoolId = Integer.parseInt(request.getParameter(PARAM_SCHOOL_ID));
            State state = State.fromString(request.getParameter(PARAM_SCHOOL_STATE));
            String notes = request.getParameter(PARAM_NOTES);
            return addSchoolToHold(schoolId, state, notes);
        } catch (Exception e) {
            _log.warn("Error parsing parameters", e);
        }
        return false;
    }

    protected boolean addSchoolToHold(int schoolId, State state, String notes) {
        if (state != null) {
            try {
                School school = _schoolDao.getSchoolById(state, schoolId);
                if (school != null) {
                    _heldSchoolDao.save(new HeldSchool(schoolId, state, notes));
                    return true;
                }
            } catch (Exception e) {
                _log.warn("Error saving HeldSchool", e);
            }
        }
        return false;
    }

    public IHeldSchoolDao getHeldSchoolDao() {
        return _heldSchoolDao;
    }

    public void setHeldSchoolDao(IHeldSchoolDao heldSchoolDao) {
        _heldSchoolDao = heldSchoolDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
