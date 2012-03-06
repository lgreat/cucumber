package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.WelcomeMessageStatus;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.state.State;
import gs.web.util.HttpCacheInterceptor;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin/createEspUsers.page")
public class EspCreateUsersController implements ReadWriteAnnotationController {

    public static final String VIEW = "admin/espCreateUsers";
    public static final String DEFAULT_JOB_TITLE = "administrator (other)";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @RequestMapping(method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request) {
        return VIEW;
    }

//    @RequestMapping(method = RequestMethod.POST)
//    public void createUser(HttpServletRequest request, HttpServletResponse response) {
//        String email = request.getParameter("email");
//        String stateStr = request.getParameter("state");
//        String schoolIdStr = request.getParameter("schoolId");
//        String firstName = request.getParameter("firstName");
//        String lastName = request.getParameter("lastName");
//        String jobTitle = request.getParameter("jobTitle");
//
//        if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(stateStr) && StringUtils.isNotBlank(schoolIdStr)) {
//            email = email.trim();
//            stateStr = stateStr.trim();
//            schoolIdStr = schoolIdStr.trim();
//            State state = State.fromString(stateStr);
//            if (state != null) {
//                School school = _schoolDao.getSchoolById(state, new Integer(schoolIdStr));
//                if (school != null) {
//                    User user = _userDao.findUserFromEmailIfExists(email);
//                    if (user == null) {
//                        user = new User();
//                        user.setEmail(email);
//                        user.setFirstName(StringUtils.isNotBlank(firstName) ? firstName : null);
//                        user.setLastName(StringUtils.isNotBlank(lastName) ? lastName : null);
//                        user.setHow("esp_pre_approved");
//                        user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
//                        _userDao.saveUser(user);
//                    }
//                    saveEspMembership(user, state, school, jobTitle);
//                }
//
//            }
//        }
//    }

    @RequestMapping(method = RequestMethod.POST)
    public void createUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map returnValues = new HashMap();

        String data = request.getParameter("data");
        if (StringUtils.isBlank(data)) {
            appendDebugOutput(returnValues, "debugOutput", "ERROR: No data received");
        } else {
            String[] lines = data.split("\n");
            if (lines.length == 0) {
                appendDebugOutput(returnValues, "debugOutput", "ERROR: No data received");
            } else {
                State state = getState(request.getParameter("state"), returnValues);
                for (int i = 0; i < lines.length; i++) {
                    try {
                        String[] fields = lines[i].split("\t");
                        String email = fields[0];
                        String schoolIdStr = fields[1];
                        String firstName = fields[2];
                        String lastName = fields[3];
                        String jobTitle = fields[4];
                        if (state != null) {
                            try {
                                createUser(email, state, schoolIdStr, firstName, lastName, jobTitle, returnValues);
                            } catch (Exception e) {
                                appendDebugOutput(returnValues, "debugOutput", "ERROR: at creating user for email:" + email + " Exception:" + e);
                                appendDebugOutput(returnValues, "usersWithErrors", email);
                            }
                        }
                    } catch (Exception e) {
                        appendDebugOutput(returnValues, "debugOutput", "ERROR: at line number:" + i + " Exception:" + e);
                    }
                }

            }
        }

        JSONObject rval = new JSONObject(returnValues);
        _cacheInterceptor.setNoCacheHeaders(response);
        response.setContentType("application/json");
        response.getWriter().print(rval.toString());
        response.getWriter().flush();
    }

    protected State getState(String stateStr, Map returnValues) {
        State state = null;
        if (StringUtils.isBlank(stateStr)) {
            appendDebugOutput(returnValues, "debugOutput", "ERROR: State:" + stateStr + " cannot be blank.");
        } else {
            state = State.fromString(stateStr);
        }
        if (state == null) {
            appendDebugOutput(returnValues, "debugOutput", "ERROR: State:" + stateStr + " not found.");
        }
        return state;
    }

    protected void createUser(String email, State state, String schoolIdStr,
                              String firstName, String lastName, String jobTitle, Map returnValues) {
        if (StringUtils.isNotBlank(email) && state != null && StringUtils.isNotBlank(schoolIdStr)) {
            email = email.trim();
            schoolIdStr = schoolIdStr.trim();
            School school = _schoolDao.getSchoolById(state, new Integer(schoolIdStr));
            if (school != null) {
                User user = _userDao.findUserFromEmailIfExists(email);
                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user.setFirstName(StringUtils.isNotBlank(firstName) ? firstName : null);
                    user.setLastName(StringUtils.isNotBlank(lastName) ? lastName : null);
                    user.setHow("esp_pre_approved");
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                    _userDao.saveUser(user);
                } else {
                    // TODO ?Example NL users.
                }
                saveEspMembership(user, state, school, jobTitle, returnValues);
            } else {
                appendDebugOutput(returnValues, "debugOutput", "ERROR: School:" + schoolIdStr + " not found for email:" + email);
                appendDebugOutput(returnValues, "usersWithErrors", email);
            }

        } else {
            appendDebugOutput(returnValues, "debugOutput", "ERROR: Email:" + email + " State:" + state + " and SchoolId:" + schoolIdStr + " cannot be blank.");
            appendDebugOutput(returnValues, "usersWithErrors", email);
        }
    }

    protected void saveEspMembership(User user, State state, School school, String jobTitle, Map returnValues) {
        if (state != null && school != null && school.getId() != null && school.getId() > 0 && user != null
                && user.getId() != null) {
            EspMembership espMembership = _espMembershipDao.findEspMembershipByStateSchoolIdUserId(state, school.getId(), user.getId(), false);

            if (espMembership == null) {
                EspMembership esp = new EspMembership();
                esp.setActive(false);
                esp.setJobTitle(StringUtils.isNotBlank(jobTitle) ? jobTitle : DEFAULT_JOB_TITLE);
                esp.setState(state);
                esp.setSchoolId(school.getId());
                esp.setStatus(EspMembershipStatus.PRE_APPROVED);
                esp.setUser(user);
                _espMembershipDao.saveEspMembership(esp);
                appendDebugOutput(returnValues, "debugOutput", "INFO: created a new pre-approved user." + user.getEmail());
            } else if ((espMembership.getStatus().equals(EspMembershipStatus.DISABLED) ||
                    espMembership.getStatus().equals(EspMembershipStatus.REJECTED) ||
                    espMembership.getStatus().equals(EspMembershipStatus.PROCESSING)) && !espMembership.getActive()) {
                espMembership.setStatus(EspMembershipStatus.PRE_APPROVED);
                espMembership.setUpdated(new Date());
                _espMembershipDao.saveEspMembership(espMembership);
                appendDebugOutput(returnValues, "debugOutput", "INFO: updated user to pre-approved:" + user.getEmail());
            } else if (espMembership.getStatus().equals(EspMembershipStatus.APPROVED)) {
                appendDebugOutput(returnValues, "usersAlreadyApproved", user.getEmail());
                appendDebugOutput(returnValues, "usersWithErrors", user.getEmail());
            } else if (espMembership.getStatus().equals(EspMembershipStatus.PRE_APPROVED)) {
                appendDebugOutput(returnValues, "usersAlreadyPreApproved", user.getEmail());
                appendDebugOutput(returnValues, "usersWithErrors", user.getEmail());
            }
        } else {
            appendDebugOutput(returnValues, "debugOutput", "ERROR: State:" + state + " School:" + school + " and User:" + user + " cannot be null.");
        }
    }

    protected void appendDebugOutput(Map<String, List<String>> returnValues, String key, String debugOutput) {
        List debugOutputs = returnValues.get(key);
        if (debugOutputs != null) {
            debugOutputs.add(debugOutput);
        } else {
            debugOutputs = new ArrayList<String>();
            debugOutputs.add(debugOutput);
            returnValues.put(key, debugOutputs);
        }
    }

}