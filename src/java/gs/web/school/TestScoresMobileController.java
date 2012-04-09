package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import gs.web.path.DirectoryStructureUrlFields;
import org.springframework.orm.ObjectRetrievalFailureException;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class TestScoresMobileController implements Controller,IDeviceSpecificControllerPartOfPair {

    public static final String PARAM_SCHOOL_ID = "id";

    public static final String PARAM_STATE = "state";

    public static final String VIEW = "school/testScores-mobile";

    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;

    private ISchoolDao _schoolDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String schoolId = request.getParameter(PARAM_SCHOOL_ID);
        String stateStr = request.getParameter(PARAM_STATE);


        Map<String, Object> model = new HashMap<String, Object>();
        try {
            State state = State.fromString(stateStr);
            School school = _schoolDao.getSchoolById(state, new Integer(schoolId));
            model.put("school", school);
        } catch (ObjectRetrievalFailureException e) {

        }
        return new ModelAndView(VIEW, model);
    }


    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        return true;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }

}