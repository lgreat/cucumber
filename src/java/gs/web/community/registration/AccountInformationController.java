package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.lang.StringUtils;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.community.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.school.ISchoolDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AccountInformationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/accountInformation.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object commandObj,
                                   BindException errors) {
        AccountInformationCommand command = (AccountInformationCommand) commandObj;

        State state = command.getState();
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            command.setState(state);
        }

        if (command.getUser() == null) {
            command.setUser(SessionContextUtil.getSessionContext(request).getUser());
        }

        if (command.getUser() == null || !command.getUser().isEmailValidated()) {
            errors.rejectValue("user", "Cannot find a user");
        } else {
            User user = command.getUser();
            command.setState(user.getUserProfile().getState());

//            bindRequestData(request, command);

            loadProfileCityList(command);
            
            String city = user.getUserProfile().getCity();
            int counter=1;
            for (Student student: user.getStudents()) {
                command.addStudent(student);
                School school = _schoolDao.getSchoolById(student.getState(), student.getSchoolId());
                command.addCityName(school.getCity());
                loadCityList(request, command, counter++);
                loadSchoolList(student, school.getCity(), command);
            }
//            for (int x = 0; x < 1 || (command.getUser().getUserProfile().getNumSchoolChildren() != null &&
//                     x < command.getUser().getUserProfile().getNumSchoolChildren()); x++) {
//                Student student = new Student();
//                student.setState(state);
//                command.addStudent(student);
//                command.addCityName(city);
//                loadCityList(request, command, x+1);
//            }
        }
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        ModelAndView mAndV = super.showForm(request, response, errors);
        if (errors.hasFieldErrors("user")) {
            State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, state, BEAN_ID);
            mAndV.setViewName("redirect:" + urlBuilder.asSiteRelative(request));
        }
        return mAndV;
    }

    protected void loadProfileCityList(AccountInformationCommand command) {
        command.setProfileCityList(_geoDao.findCitiesByState(command.getState()));
    }

    protected void loadCityList(HttpServletRequest request, AccountInformationCommand command, int childNum) {
        State state;
        if (command.getNumStudents() >= childNum) {
            Student student = command.getStudents().get(childNum-1);
            state = student.getState();
        } else {
            state = command.getUser().getState();
            if (state == null) {
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }
        }
//        City city = new City();
//        city.setName("My child's city is not listed");
//        cities.add(0, city);
        command.addCityList(_geoDao.findCitiesByState(state));
    }

    protected void parseStudent(HttpServletRequest request, AccountInformationCommand command, int childNum) {
        String sGrade = request.getParameter("grade" + childNum);
        State state = _stateManager.getState(request.getParameter("state" + childNum));
        String sSchoolId = request.getParameter("school" + childNum);
        String city = request.getParameter("city" + childNum);

        Student student = new Student();

        if (!StringUtils.isEmpty(sGrade)) {
            student.setGrade(Grade.getGradeLevel(sGrade));
        }
        if (!StringUtils.isEmpty(sSchoolId)) {
            student.setSchoolId(new Integer(sSchoolId));
        }
        student.setState(state);
        student.setOrder(childNum);

        command.addStudent(student);
        command.addCityName(city);
        loadSchoolList(student, city, command);
    }

    protected void loadSchoolList(Student student, String city, AccountInformationCommand command) {
        State state = student.getState();
        Grade grade = student.getGrade();
        if (grade != null) {
            List<School> schools = _schoolDao.findSchoolsInCityByGrade(state, city, grade);
            command.addSchools(schools);
        } else {
            command.addSchools(new ArrayList<School>());
        }
        command.addSchoolName("");
    }

    protected void bindRequestData(HttpServletRequest request, AccountInformationCommand command) {
        command.setParentAmbassador(request.getParameter("parentAmbassadorStr"));

        UserProfile profile = command.getUser().getUserProfile();
        for (int x=0; profile.getNumSchoolChildren() != null &&
                x < profile.getNumSchoolChildren(); x++) {
            int childNum = x+1;
            if (request.getParameter("grade" + childNum) != null) {
                parseStudent(request, command, childNum);
            } else {
//                command.addStudent(command.getUser().getStudents().)
            }
        }
        command.getCityList().clear();
    }


    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
