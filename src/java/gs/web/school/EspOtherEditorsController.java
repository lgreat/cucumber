package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.admin.EspCreateUsersController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * The request mapping does not have a trailing slash.This handles the url with and without a trailing slash.
 */
@Controller
@RequestMapping("/official-school-profile/memberships")
public class EspOtherEditorsController {
    private static final Log _log = LogFactory.getLog(EspOtherEditorsController.class);
    public static final String VIEW = "school/espDashboard";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final String MODEL_SUPERUSER_ERROR = "superUserError";

    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private EspFormValidationHelper _espFormValidationHelper;
    @Autowired
    private EspCreateUsersController _espCreateUsersController;
    
    @RequestMapping(method = RequestMethod.POST, value="/add")
    public void add(ModelMap modelMap, HttpServletRequest request,
          @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
          @RequestParam(value=PARAM_STATE, required=false) State state,
          @RequestParam(value="email", required=false) String email,
          @RequestParam(value="firstName", required=false) String firstName,
          @RequestParam(value="lastName", required=false) String lastName,
          @RequestParam(value="jobTitle", required=false) String jobTitle
          ) {
        
        _espCreateUsersController.addUser(request, email, state, schoolId.toString(), firstName, lastName, jobTitle, new HashMap<String,String>());

        return;
    }


    /**
     * Adds EspMemberships for given school to model for memberships that don't belong to provided user
     * @param modelMap
     * @param request
     * @param schoolId
     * @param state
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value="/list.page")
    public String list(ModelMap modelMap, HttpServletRequest request,
                                  @RequestParam(value=PARAM_SCHOOL_ID, required=true) Integer schoolId,
                                  @RequestParam(value=PARAM_STATE, required=true) State state) {
        User user = getValidUser(request);
        if (user == null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }

        School school = getSchool(state, schoolId, modelMap);

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext != null) {
            sessionContext.setState(state);
        }

        if (school != null) {

            //Get the information about who else has ESP access to this school
            List<EspMembership> otherEspMemberships = getOtherEspMembersForSchool(school, user);
            if (otherEspMemberships != null && !otherEspMemberships.isEmpty()) {
                modelMap.put("otherEspMemberships", otherEspMemberships);
            }

        }

        return VIEW;
    }

    /**
     * Get the esp membership for the user.
     *
     * @param user
     */

    protected EspMembership getEspMembershipForUser(User user) {
        if (user != null && (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER))) {
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), true);
            if (!espMemberships.isEmpty()) {
                //Take the user to the first active school.
                return espMemberships.get(0);
            }
        }
        return null;
    }

    /**
     * Get the school for the esp membership for the user.
     *
     * @param espMembership
     */
    protected School getSchool(EspMembership espMembership) {
        School school = getSchoolDao().getSchoolById(espMembership.getState(), espMembership.getSchoolId());
        if (school != null && school.isActive()) {
            return school;
        }
        return null;
    }

    /**
     * Get the information about who else has ESP access to the same school as the user
     *
     * @param school
     * @param user
     */
    protected List<EspMembership> getOtherEspMembersForSchool(School school, User user) {
        List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsBySchool(school, true);
        if (espMemberships != null && !espMemberships.isEmpty() && user != null && user.getId() != null) {
            Iterator<EspMembership> iter = espMemberships.iterator();
            //remove the current user from the list.
            while (iter.hasNext()) {
                if (iter.next().getUser().getId() == user.getId()) {
                    iter.remove();
                }
            }
        }
        return espMemberships;
    }

    /**
     * Pulls the user out of the session context. Returns null if there is no user, or if the user fails
     * checkUserAccess
     */
    protected User getValidUser(HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }
        if (user != null && (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER))) {
            return user;
        }
        return null;
    }

    /**
     * Parses the state and schoolId out of the request and fetches the school. Returns null if
     * it can't parse parameters, can't find school, or the school is inactive
     */
    protected School getSchool(State state, Integer schoolId, ModelMap modelMap) {
        if (state == null || schoolId == null) {
            if (state != null || schoolId != null) {
                modelMap.put(MODEL_SUPERUSER_ERROR, "Please choose both a state and school.");
            }
            return null;
        }
        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolId);
        } catch (Exception e) {
            modelMap.put(MODEL_SUPERUSER_ERROR, "No school found in " + state + " with id " + schoolId);
            // handled below
        }
        if (school == null || !school.isActive()) {
            if (school != null && !school.isActive()) {
                modelMap.put(MODEL_SUPERUSER_ERROR, "The school with id " + schoolId + "(" + school.getName() + ") in " + state + " is inactive");
            }
            _log.error("School is null or inactive: " + school);
            return null;
        }

        if (school.isPreschoolOnly()) {
            modelMap.put(MODEL_SUPERUSER_ERROR, "The school with id " + schoolId + "(" + school.getName() + ") in " + state + " is a preschool");
            _log.error("School is preschool only! " + school);
            return null;
        }

        return school;
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}