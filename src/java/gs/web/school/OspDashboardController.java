package gs.web.school;

import com.restfb.util.StringUtils;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * The request mapping does not have a trailing slash.This handles the url with and without a trailing slash.
 */
@Controller
@RequestMapping("/official-school-profile/dashboard")
public class OspDashboardController implements BeanFactoryAware{
    private static final Log _log = LogFactory.getLog(OspDashboardController.class);
    public static final String VIEW = "school/ospDashboard";
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

    private BeanFactory _beanFactory;

    @RequestMapping(method = RequestMethod.GET)
    public String showLandingPage(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                                  @RequestParam(value=PARAM_STATE, required=false) State state) {
        User user = getValidUser(request);
        if (user == null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        // if school is explicitly specified in the URL, grab it here
        School school = getSchool(state, schoolId, modelMap);
        boolean isProvisional = false;
        
        if (state != null) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            if (sessionContext != null) {
                sessionContext.setState(state);
            }
        }

        if (user.hasRole(Role.ESP_SUPERUSER)) {
            // super users need nothing else besides the school
            modelMap.put("espSuperuser", true);
        } else if (user.hasRole(Role.ESP_MEMBER)) {
            // for regular esp members
            // Let's find the list of schools they have access to, which could appear in some sort of list
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), true);
            List<EspMembership> validMemberships = new ArrayList<EspMembership>(espMemberships.size());
            for (EspMembership membership: espMemberships) {
                School membershipSchool = getSchool(membership);
                if (membershipSchool != null) {
                    // this is a valid membership
                    membership.setSchool(membershipSchool);
                    validMemberships.add(membership);
                }
            }
            Collections.sort(validMemberships, new Comparator<EspMembership>() {
                public int compare(EspMembership o1, EspMembership o2) {
                    try {
                        return o1.getSchool().getName().compareToIgnoreCase(o2.getSchool().getName());
                    } catch (Exception e) {
                        return 0;
                    }
                }
            });
            modelMap.put("espMemberships", validMemberships);
            // now let's have the page default to a school if it makes sense
            if (school == null) {
                if (validMemberships.size() == 1) {
                    // If only one membership, always default view to that school
                    school = validMemberships.get(0).getSchool();
                }
            }
        } else {
            EspMembership provisionalMembership = null;
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
            for (EspMembership membership: espMemberships) {
                if (membership.getStatus() == EspMembershipStatus.PROVISIONAL) {
                    provisionalMembership = membership;
                    isProvisional = true;
                }
            }
            if (provisionalMembership != null) {
                school = getSchool(provisionalMembership);
                modelMap.put("isProvisional", isProvisional);
            }
        }

        modelMap.put("school", school);

        if (school != null) {
            //Get the information about who else has ESP access to this school
            List<EspMembership> otherEspMemberships = getEspMembersForSchool(school);

            //Use the BeanFactoryAware so that we get the espStatusManager component with auto injections.Otherwise we have to
            //manually set the espResponseDao on the espStatusManager.
            EspStatusManager statusManager = (EspStatusManager) _beanFactory.getBean("espStatusManager", new Object[]{school});
            modelMap.put("schoolEspStatus", statusManager.getEspStatus());
            boolean showOspGateway = false;

            //If there is a provisional user for the school, then block out other users.GS-13363.
            if (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER)) {
                String redirect = checkForProvisionalMemberships(school, request, otherEspMemberships, user);
                if (StringUtils.isBlank(redirect) && user.hasRole(Role.ESP_MEMBER)
                        && !statusManager.getEspStatus().equals(EspStatus.OSP_PREFERRED)) {
                    showOspGateway = true;
                }
            } else if (isProvisional && !statusManager.getEspStatus().equals(EspStatus.OSP_PREFERRED)) {
                List<EspResponse> responses = _espResponseDao.getResponses(school, user.getId(), true, "_page_osp_gateway_keys");
                if (responses == null || responses.isEmpty()) {
                    showOspGateway = true;
                }
            }

            modelMap.put("showOspGateway", showOspGateway);
            modelMap.put("allEspMemberships", otherEspMemberships);
            
            // get percent completion info
            Map<Long, Boolean> pageStartedMap = new HashMap<Long, Boolean>(8);
            boolean anyPageStarted = false;
            for (long x=1; x < 9; x++) {
                boolean pageStarted = _espResponseDao.getKeyCount(school, OspFormController.KEYS_BY_PAGE.get((int)x), false) > 0;
                pageStartedMap.put(x, pageStarted);
                anyPageStarted |= pageStarted;
            }
            modelMap.put("pageStarted", pageStartedMap);
            modelMap.put("anyPageStarted", anyPageStarted);
            modelMap.put("isFruitcakeSchool", OspFormController.isFruitcakeSchool(school) && school.getType() == SchoolType.PRIVATE);
        }

        return VIEW;
    }

    public String checkForProvisionalMemberships(School school,HttpServletRequest request,List<EspMembership> otherEspMemberships,User user){
        EspMembership provisionalMembership = _espFormValidationHelper.getProvisionalMembershipForSchool(otherEspMemberships, user);
        if (provisionalMembership != null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION_ERROR);
            urlBuilder.addParameter("message", "page3");
            urlBuilder.addParameter("schoolId", school.getId().toString());
            urlBuilder.addParameter("state", school.getStateAbbreviation().toString());
            urlBuilder.addParameter("provisionalUserName",
                    provisionalMembership.getUser().getFirstName() + " " + provisionalMembership.getUser().getLastName());
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        return "";
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
        if (school != null && (school.isActive() || school.isDemoSchool())) {
            return school;
        }
        return null;
    }

    /**
     * Get the information about who has ESP access to the given school
     *
     * @param school
     */
    protected List<EspMembership> getEspMembersForSchool(School school) {
        List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsBySchool(school, false);
        if (espMemberships != null && !espMemberships.isEmpty()) {
            Iterator<EspMembership> iter = espMemberships.iterator();

            while (iter.hasNext()) {
                EspMembership membership =  iter.next();
                if (membership.getStatus().equals(EspMembershipStatus.DISABLED) || membership.getStatus().equals(EspMembershipStatus.REJECTED)) {
                    //remove rejected and disabled users.
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
    public User getValidUser(HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }
        if (user != null && (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER))) {
            return user;
        }
        if (user != null) {
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
            if (!espMemberships.isEmpty()) {
                for (EspMembership membership: espMemberships) {
                    if (membership.getStatus() == EspMembershipStatus.PROVISIONAL) {
                        return user;
                    }
                }
            }
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
        if (school == null || (!school.isActive() && !school.isDemoSchool())) {
            if (school != null && (!school.isActive() && !school.isDemoSchool())) {
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

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}