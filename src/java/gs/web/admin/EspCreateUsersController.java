package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.WelcomeMessageStatus;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.state.State;
import gs.data.util.email.EmailUtils;
import gs.web.util.HttpCacheInterceptor;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.school.EspRequestPreApprovalEmailController;
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
@RequestMapping("/admin/")
public class EspCreateUsersController implements ReadWriteAnnotationController {

    public static final String VIEW = "admin/espCreateUsers";
    public static final String DEFAULT_JOB_TITLE = "administrator (other)";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    protected ExactTargetAPI _exactTargetAPI;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private EspRequestPreApprovalEmailController _espPreApprovalEmail;

    @RequestMapping(value = "createEspUsers.page", method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request) {
        return VIEW;
    }

    /**
     * Method that handles adding a single user.
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "createEspUser.page", method = RequestMethod.POST)
    public void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map returnValues = new HashMap();

        String email = request.getParameter("email");
        String schoolIdStr = request.getParameter("schoolId");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String jobTitle = request.getParameter("jobTitle");
        State state = getState(request.getParameter("state"), returnValues);

        addUser(request, email, state, schoolIdStr, firstName, lastName, jobTitle, returnValues);

        JSONObject rval = new JSONObject(returnValues);
        _cacheInterceptor.setNoCacheHeaders(response);
        response.setContentType("application/json");
        response.getWriter().print(rval.toString());
        response.getWriter().flush();
    }

    /**
     * Method that handles adding multiple users as a batch.
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "createEspUsersBatch.page", method = RequestMethod.POST)
    public void createUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map returnValues = new HashMap();

        String data = request.getParameter("data");
        if (StringUtils.isBlank(data)) {
            addToList(returnValues, "debugOutput", "ERROR: No data received");
        } else {
            String[] lines = data.split("\n");
            if (lines.length == 0) {
                addToList(returnValues, "debugOutput", "ERROR: No data received");
            } else {
                State state = getState(request.getParameter("state"), returnValues);
                for (int i = 0; i < lines.length; i++) {
                    try {
                        String[] fields = lines[i].split("\t");
                        if (fields.length < 2) {
                            addToList(returnValues, "debugOutput", "ERROR: email and school id are required.Please check row #:" + i);
                        } else {
                            String email = fields[0];
                            String schoolIdStr = fields[1];

                            String firstName = fields.length >= 3 ? fields[2] : "";
                            String lastName = fields.length >= 4 ? fields[3] : "";
                            String jobTitle = fields.length >= 5 ? fields[4] : "";
                            try {
                                addUser(request, email, state, schoolIdStr, firstName, lastName, jobTitle, returnValues);
                            } catch (Exception e) {
                                addToList(returnValues, "debugOutput", "ERROR: creating user for email:" + email + " Exception:" + e);
                                addToList(returnValues, "usersWithErrors", email);
                            }
                        }

                    } catch (Exception e) {
                        addToList(returnValues, "debugOutput", "ERROR: at line number:" + i + " Exception:" + e);
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
            addToList(returnValues, "debugOutput", "ERROR: State:" + stateStr + " cannot be blank.");
        } else {
            stateStr = stateStr.trim();
            try {
                state = State.fromString(stateStr);
            } catch (Exception e) {
                addToList(returnValues, "debugOutput", "ERROR: State:" + stateStr + " not found.");
            }
        }
        return state;
    }

    protected School getSchool(String schoolIdStr, State state, String email, Map returnValues) {
        School validSchool = null;
        if (StringUtils.isBlank(schoolIdStr)) {
            addToList(returnValues, "debugOutput", "ERROR: School:" + schoolIdStr + " cannot be blank.For email:" + email);
        } else {
            schoolIdStr = schoolIdStr.trim();
            School school = null;
            try {
                school = _schoolDao.getSchoolById(state, new Integer(schoolIdStr));
            } catch (Exception e) {
                addToList(returnValues, "debugOutput", "ERROR: School:" + schoolIdStr + " not found in state:" + state + ".For email:" + email);
                addToList(returnValues, "usersWithErrors", email);
            }

            if (school != null) {
                if (!school.isActive()) {
                    addToList(returnValues, "debugOutput", "ERROR: School:" + schoolIdStr + " in state:" + state + " is inactive.For email:" + email);
                    addToList(returnValues, "usersWithErrors", email);
                } else if (school.isPreschoolOnly()) {
                    addToList(returnValues, "debugOutput", "ERROR: School:" + schoolIdStr + " in state:" + state + " is preschool only.For email:" + email);
                    addToList(returnValues, "usersWithErrors", email);
                } else {
                    validSchool = school;
                }
            }
        }
        return validSchool;
    }

    protected String getValidEmail(String email, Map returnValues) {
        String validEmail = "";
        if (StringUtils.isBlank(email)) {
            addToList(returnValues, "debugOutput", "ERROR: Email:" + email + " cannot be blank");
        } else {
            email = email.trim();
            if (!EmailUtils.isValidEmail(email)) {
                addToList(returnValues, "debugOutput", "ERROR: Email:" + email + " is not valid");
                addToList(returnValues, "usersWithErrors", email);
            } else {
                validEmail = email;
            }
        }
        return validEmail;
    }

    /**
     * Helper method to add a new user to the database.
     *
     * @param request
     * @param email
     * @param state
     * @param schoolIdStr
     * @param firstName
     * @param lastName
     * @param jobTitle
     * @param returnValues
     */
     public void addUser(HttpServletRequest request, String email, State state, String schoolIdStr,
                           String firstName, String lastName, String jobTitle, Map returnValues) {
        if (state != null) {
            email = getValidEmail(email, returnValues);
            School school = getSchool(schoolIdStr, state, email, returnValues);

            if (school != null && StringUtils.isNotBlank(email)) {
                User user = _userDao.findUserFromEmailIfExists(email);

                if (user == null) {
                    //No user was found.Therefore add one.
                    user = new User();
                    user.setEmail(email);
                    user.setFirstName(StringUtils.isNotBlank(firstName) ? firstName.trim() : null);
                    user.setLastName(StringUtils.isNotBlank(lastName) ? lastName.trim() : null);
                    user.setHow("esp_pre_approved");
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                    _userDao.saveUser(user);
                } else {
                    //A user was found.Therefore update the info.
                    if (StringUtils.isBlank(user.getFirstName()) && StringUtils.isNotBlank(firstName)) {
                        user.setFirstName(firstName.trim());
                    }
                    if (StringUtils.isBlank(user.getLastName()) && StringUtils.isNotBlank(lastName)) {
                        user.setLastName(lastName.trim());
                    }
                }

                //After adding the user, pre-approve the user.
                boolean didPreApproveUser = preApproveEspMembership(user, state, school, jobTitle, returnValues);
                //If the user state was changed to "pre-approved" then send out an email.
                if (didPreApproveUser) {
                    if (!_espPreApprovalEmail.sendESPVerificationEmail(request, user)) {
                        addToList(returnValues, "debugOutput", "ERROR while sending verification email to:" + user.getEmail());
                    }
                }
            }
        }
    }

    /**
     * Method to "pre-approve" a user.
     *
     * @param user
     * @param state
     * @param school
     * @param jobTitle
     * @param returnValues
     */
    protected boolean preApproveEspMembership(User user, State state, School school, String jobTitle, Map returnValues) {
        boolean didPreApproveUser = false;

        if (state != null && school != null && school.getId() != null && school.getId() > 0 && user != null
                && user.getId() != null) {

            //check if the user is eligible for pre-approval.If yes then pre-approve a user.
            if (isUserEligibleForPreApproval(user, returnValues)) {
                addOrUpdatePreApprovedEspMembership(user, state, school, jobTitle, returnValues);
                didPreApproveUser = true;
            }

        } else {
            addToList(returnValues, "debugOutput", "ERROR: State:" + state + " School:" + school + " and User:" + user + " cannot be null.");
        }
        return didPreApproveUser;
    }

    /**
     * Method to add a new esp_membership row for a user in a "pre-approved" status or update the status to "pre-approved".
     *
     * @param user
     * @param state
     * @param school
     * @param jobTitle
     * @param returnValues
     */
    protected void addOrUpdatePreApprovedEspMembership(User user, State state, School school, String jobTitle, Map returnValues) {
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
            addToList(returnValues, "debugOutput", "INFO: created a new pre-approved user." + user.getEmail());
        } else {
            espMembership.setStatus(EspMembershipStatus.PRE_APPROVED);
            espMembership.setUpdated(new Date());
            _espMembershipDao.saveEspMembership(espMembership);
            addToList(returnValues, "debugOutput", "INFO: updated user to pre-approved:" + user.getEmail());
        }
    }

    /**
     * Method to check if a user can be pre-approved or not.
     * Currently a user is eligible if he does not already have a "pre-approved"  or "approved" row(irrespective of the school).
     *
     * @param user
     * @param returnValues
     */
    protected boolean isUserEligibleForPreApproval(User user, Map returnValues) {
        boolean isEligible = true;
        //When in the future, a user can have multi-school ESP access, then
        // use findEspMembershipByStateSchoolIdUserId method to check for eligibility.
        List<EspMembership> espMemberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);

        if (espMemberships != null && !espMemberships.isEmpty()) {

            for (EspMembership espMembership : espMemberships) {
                if (espMembership.getStatus().equals(EspMembershipStatus.APPROVED)) {
                    addToList(returnValues, "usersAlreadyApproved", user.getEmail());
                    addToList(returnValues, "usersWithErrors", user.getEmail());
                    isEligible = false;
                    break;
                } else if (espMembership.getStatus().equals(EspMembershipStatus.PRE_APPROVED)) {
                    addToList(returnValues, "usersAlreadyPreApproved", user.getEmail());
                    addToList(returnValues, "usersWithErrors", user.getEmail());
                    isEligible = false;
                    break;
                }
            }
        }
        return isEligible;
    }

    /**
     * Helper method to add debug messages to a map that is returned to the view.
     *
     * @param returnValues - returnValues is a Map of key(String) to a list of messages(String).
     * @param key          - key to append the message to.
     * @param debugOutput  - the debug message.
     */
    protected void addToList(Map<String, List<String>> returnValues, String key, String debugOutput) {
        List debugOutputs = returnValues.get(key);
        if (debugOutputs != null) {
            debugOutputs.add(debugOutput);
        } else {
            debugOutputs = new ArrayList<String>();
            debugOutputs.add(debugOutput);
            returnValues.put(key, debugOutputs);
        }
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

}